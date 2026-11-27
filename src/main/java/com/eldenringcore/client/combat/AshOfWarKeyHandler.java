package com.eldenringcore.client.combat;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.combat.EldenWeaponItem;
import com.eldenringcore.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.client.input.EpicFightKeyMappings;

/**
 * Casts the held Elden weapon's Ash of War with Epic Fight's "Weapon Innate Skill" key, so it sits
 * where Epic Fight players expect a weapon skill and right-click stays free for Guard.
 * Our weapon types define no Epic Fight innate skill, so the key has no other effect on them.
 */
@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AshOfWarKeyHandler {
    private static boolean wasDown;

    private AshOfWarKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        boolean down = EpicFightKeyMappings.WEAPON_INNATE_SKILL.isDown();
        boolean pressed = down && !wasDown;
        wasDown = down;
        if (!pressed || mc.player == null || mc.screen != null) return;
        if (mc.player.getMainHandItem().getItem() instanceof EldenWeaponItem) {
            ModNetwork.CHANNEL.sendToServer(new ModNetwork.AshOfWarPacket());
        }
    }
}
