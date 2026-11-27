package com.eldenringcore.combat;

import com.eldenringcore.EldenRingCore;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Elden Ring Equip Load system:
 * Calculates weight from equipped armor & held weapons:
 * - LIGHT (< 30%): +10% movement speed
 * - MEDIUM (30% - 70%): normal speed
 * - HEAVY (> 70%): -15% movement speed ("Heavy Load / Fat Roll")
 */
@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EquipLoadManager {
    private static final UUID EQUIP_LOAD_SPEED_UUID = UUID.fromString("a3c1f4d0-5e8b-4f92-b7a1-9c2d6e4f8a11");
    public static final float DEFAULT_MAX_LOAD = 60.0f;

    private static final Map<UUID, LoadTier> LAST_TIER = new ConcurrentHashMap<>();

    public enum LoadTier {
        LIGHT(0.10, "Light Load", ChatFormatting.GREEN),
        MEDIUM(0.0, "Medium Load", ChatFormatting.YELLOW),
        HEAVY(-0.15, "Heavy Load", ChatFormatting.RED);

        private final double speedModifier;
        private final String displayName;
        private final ChatFormatting color;

        LoadTier(double speedModifier, String displayName, ChatFormatting color) {
            this.speedModifier = speedModifier;
            this.displayName = displayName;
            this.color = color;
        }

        public double getSpeedModifier() {
            return this.speedModifier;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public ChatFormatting getColor() {
            return this.color;
        }
    }

    public static float calculateEquipLoad(Player player) {
        float total = 0.0f;

        for (ItemStack armorStack : player.getArmorSlots()) {
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof ArmorItem armor) {
                total += armor.getDefense() * 0.95f + armor.getToughness() * 1.5f;
            }
        }

        total += getHeldItemWeight(player.getMainHandItem());
        total += getHeldItemWeight(player.getOffhandItem());
        return total;
    }

    private static float getHeldItemWeight(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0.0f;
        }
        if (stack.getItem() instanceof EldenWeaponItem) {
            return stack.getRarity().ordinal() >= 3 ? 8.0f : 6.0f;
        }
        if (stack.getItem() instanceof ShieldItem) {
            return 7.5f;
        }
        if (stack.getItem() instanceof SwordItem) {
            return 4.5f;
        }
        return 0.0f;
    }

    public static LoadTier getLoadTier(Player player) {
        float ratio = calculateEquipLoad(player) / DEFAULT_MAX_LOAD;
        if (ratio < 0.30f) {
            return LoadTier.LIGHT;
        }
        if (ratio <= 0.70f) {
            return LoadTier.MEDIUM;
        }
        return LoadTier.HEAVY;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        if (player.tickCount % 10 != 0 || !player.isAlive()) {
            return;
        }

        LoadTier tier = player.getAbilities().instabuild ? LoadTier.MEDIUM : getLoadTier(player);
        LoadTier previous = LAST_TIER.put(player.getUUID(), tier);

        AttributeInstance speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            AttributeModifier existing = speedAttr.getModifier(EQUIP_LOAD_SPEED_UUID);
            if (existing != null && Math.abs(existing.getAmount() - tier.getSpeedModifier()) > 1.0E-4) {
                speedAttr.removeModifier(EQUIP_LOAD_SPEED_UUID);
                existing = null;
            }
            if (existing == null && Math.abs(tier.getSpeedModifier()) > 1.0E-4) {
                speedAttr.addTransientModifier(new AttributeModifier(
                        EQUIP_LOAD_SPEED_UUID,
                        "Elden Ring Equip Load",
                        tier.getSpeedModifier(),
                        AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }

        if (previous != null && previous != tier && player instanceof ServerPlayer serverPlayer) {
            float load = calculateEquipLoad(player);
            serverPlayer.displayClientMessage(
                    Component.literal(String.format("Equip Load: %.1f / %.0f (%s)", load, DEFAULT_MAX_LOAD, tier.getDisplayName()))
                            .withStyle(tier.getColor()),
                    true);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        LAST_TIER.remove(event.getEntity().getUUID());
    }
}
