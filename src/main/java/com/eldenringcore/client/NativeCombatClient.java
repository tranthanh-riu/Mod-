package com.eldenringcore.client;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.nativecombat.NativeCombatNetworking;
import com.eldenringcore.nativecombat.NativeKatanaItem;
import com.mojang.blaze3d.platform.InputConstants;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

/**
 * Client-side playerAnimator integration, custom Elden Ring keybindings (Dodge Roll + Heavy Thrust),
 * Left-Click combo intercept, and sleek Elden Ring Stamina HUD.
 */
public class NativeCombatClient {
    public static final ResourceLocation ANIM_LAYER_ID = new ResourceLocation(EldenRingCore.MOD_ID, "combat_anim");

    public static final KeyMapping KEY_DODGE_ROLL = new KeyMapping(
            "key.eldenring_core.dodge_roll",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_ALT,
            "key.categories.eldenring_core"
    );

    public static final KeyMapping KEY_HEAVY_ATTACK = new KeyMapping(
            "key.eldenring_core.heavy_attack",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            "key.categories.eldenring_core"
    );

    private static float clientStamina = 100.0f;
    private static float clientMaxStamina = 100.0f;
    private static int clientComboStep = 0;

    public static void updateStaminaHud(float stamina, float maxStamina, int comboStep) {
        clientStamina = stamina;
        clientMaxStamina = Math.max(1.0f, maxStamina);
        clientComboStep = comboStep;
    }

    @SuppressWarnings("unchecked")
    public static void playAnimationOnEntity(int entityId, String animName, float speed) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof AbstractClientPlayer player)) return;

        IAnimation rawLayer = PlayerAnimationAccess.getPlayerAssociatedData(player).get(ANIM_LAYER_ID);
        if (!(rawLayer instanceof ModifierLayer<?>)) return;
        ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) rawLayer;

        ResourceLocation animLoc = new ResourceLocation(EldenRingCore.MOD_ID, animName);
        KeyframeAnimation anim = PlayerAnimationRegistry.getAnimation(animLoc);
        if (anim == null) {
            EldenRingCore.LOGGER.warn("playerAnimator clip not found: {}", animLoc);
            return;
        }

        KeyframeAnimationPlayer animPlayer = new KeyframeAnimationPlayer(anim);
        // Show full animated arms & sword in BOTH 1st-person AND 3rd-person (Shoulder Surfing F5)!
        if (!animName.startsWith("dodge_roll")) {
            animPlayer.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            animPlayer.setFirstPersonConfiguration(new FirstPersonConfiguration(true, true, true, true));
        } else {
            animPlayer.setFirstPersonMode(FirstPersonMode.NONE);
        }

        layer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(3, Ease.OUTCUBIC), animPlayer);
    }

    @Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                    ANIM_LAYER_ID, 42, player -> new ModifierLayer<>());
            EldenRingCore.LOGGER.info("Registered playerAnimator layer {} for Native Elden Combat.", ANIM_LAYER_ID);
        }

        @SubscribeEvent
        public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
            event.register(KEY_DODGE_ROLL);
            event.register(KEY_HEAVY_ATTACK);
        }
    }

    @Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeBusEvents {
        @SubscribeEvent
        public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
            if (!event.isAttack()) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;
            if (!(mc.player.getMainHandItem().getItem() instanceof NativeKatanaItem)) return;

            // Cancel vanilla rigid 1-arm punch & run our smooth 3-hit Katana combo (or Jump/Shift Heavy)!
            event.setCanceled(true);
            event.setSwingHand(false);

            if (mc.player.isShiftKeyDown() || !mc.player.onGround()) {
                NativeCombatNetworking.sendAction(NativeKatanaItem.C2SAction.HEAVY_ATTACK, 0f, 0f);
            } else {
                NativeCombatNetworking.sendAction(NativeKatanaItem.C2SAction.LIGHT_COMBO, 0f, 0f);
            }
        }

        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.screen != null) return;

            while (KEY_DODGE_ROLL.consumeClick()) {
                float mx = mc.player.input.leftImpulse;
                float mz = mc.player.input.forwardImpulse;
                NativeCombatNetworking.sendAction(NativeKatanaItem.C2SAction.DODGE_ROLL, mx, mz);
            }

            while (KEY_HEAVY_ATTACK.consumeClick()) {
                if (mc.player.getMainHandItem().getItem() instanceof NativeKatanaItem) {
                    NativeCombatNetworking.sendAction(NativeKatanaItem.C2SAction.HEAVY_ATTACK, 0f, 0f);
                }
            }
        }

        @SubscribeEvent
        public static void onRenderHud(RenderGuiOverlayEvent.Post event) {
            if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null || mc.options.hideGui) return;

            boolean holdingNative = mc.player.getMainHandItem().getItem() instanceof NativeKatanaItem;
            if (!holdingNative && clientStamina >= clientMaxStamina - 0.5f) return;

            GuiGraphics gfx = event.getGuiGraphics();
            int sw = mc.getWindow().getGuiScaledWidth();
            int sh = mc.getWindow().getGuiScaledHeight();

            // Sleek Elden Ring Stamina bar (top-left, right below classic Souls position)
            int barW = 132;
            int barH = 7;
            int x = 14;
            int y = 14;

            // Dark metallic frame
            gfx.fill(x - 2, y - 2, x + barW + 2, y + barH + 2, 0xCC12100E);
            gfx.fill(x - 1, y - 1, x + barW + 1, y + barH + 1, 0xFF6B5B3B);
            gfx.fill(x, y, x + barW, y + barH, 0xFF1F2A1E);

            int fillW = Math.max(0, Math.min(barW, Math.round(barW * (clientStamina / clientMaxStamina))));
            gfx.fill(x, y, x + fillW, y + barH, 0xFF3FA34D);
            gfx.fill(x, y, x + fillW, y + 2, 0xFF6CD97A);

            String label = "STAMINA  " + Math.round(clientStamina) + "/" + Math.round(clientMaxStamina);
            if (holdingNative && clientComboStep > 0) {
                label += "   [COMBO " + clientComboStep + "/3]";
            }
            gfx.drawString(mc.font, label, x, y + barH + 4, 0xFFE6D5A8, true);
        }
    }
}
