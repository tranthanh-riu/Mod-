package com.eldenringcore.combat;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Focus Points (FP / Mana) manager for Ash of War skills.
 * Stores FP in player persistent NBT data, regenerates slowly over time,
 * and refills automatically on respawn or when resting at a Site of Grace.
 */
@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FocusPointsManager {
    private static final String TAG_ROOT = "eldenring_core_fp";
    private static final String TAG_CURRENT = "current";
    private static final String TAG_MAX = "max";
    private static final String TAG_LAST_USE_TICK = "last_use_tick";

    public static final float DEFAULT_MAX_FP = 100.0f;
    private static final float REGEN_PER_SECOND = 2.5f;
    private static final int REGEN_DELAY_TICKS = 40; // 2 seconds after spending FP

    private static CompoundTag getOrCreateTag(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(TAG_ROOT)) {
            CompoundTag fpTag = new CompoundTag();
            fpTag.putFloat(TAG_CURRENT, DEFAULT_MAX_FP);
            fpTag.putFloat(TAG_MAX, DEFAULT_MAX_FP);
            fpTag.putLong(TAG_LAST_USE_TICK, 0L);
            persistent.put(TAG_ROOT, fpTag);
        }
        return persistent.getCompound(TAG_ROOT);
    }

    public static float getFp(Player player) {
        return getOrCreateTag(player).getFloat(TAG_CURRENT);
    }

    public static float getMaxFp(Player player) {
        CompoundTag tag = getOrCreateTag(player);
        float max = tag.getFloat(TAG_MAX);
        return max > 0.0f ? max : DEFAULT_MAX_FP;
    }

    public static void setFp(Player player, float value) {
        CompoundTag tag = getOrCreateTag(player);
        float clamped = Math.max(0.0f, Math.min(value, getMaxFp(player)));
        tag.putFloat(TAG_CURRENT, clamped);
    }

    public static void setMaxFp(Player player, float maxFp) {
        CompoundTag tag = getOrCreateTag(player);
        float clampedMax = Math.max(10.0f, maxFp);
        tag.putFloat(TAG_MAX, clampedMax);
        if (tag.getFloat(TAG_CURRENT) > clampedMax) {
            tag.putFloat(TAG_CURRENT, clampedMax);
        }
    }

    public static void restoreFp(Player player, float amount) {
        if (amount <= 0.0f) {
            return;
        }
        setFp(player, getFp(player) + amount);
    }

    public static void restoreFullFp(Player player) {
        setFp(player, getMaxFp(player));
    }

    /**
     * Attempts to consume FP for an Ash of War skill.
     * Creative mode players do not spend FP.
     */
    public static boolean tryConsumeFp(Player player, float cost) {
        if (cost <= 0.0f || player.getAbilities().instabuild) {
            return true;
        }
        CompoundTag tag = getOrCreateTag(player);
        float current = tag.getFloat(TAG_CURRENT);
        if (current < cost) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                        Component.literal(String.format("Not enough FP (%d / %d)", (int) current, (int) cost))
                                .withStyle(ChatFormatting.RED),
                        true);
            }
            player.playSound(SoundEvents.NOTE_BLOCK_BASS.get(), 0.8f, 0.6f);
            return false;
        }

        float remaining = current - cost;
        tag.putFloat(TAG_CURRENT, remaining);
        tag.putLong(TAG_LAST_USE_TICK, player.level().getGameTime());

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                    Component.literal(String.format("FP: %d / %d", (int) remaining, (int) getMaxFp(player)))
                            .withStyle(ChatFormatting.AQUA),
                    true);
        }
        return true;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide()) {
            return;
        }
        Player player = event.player;
        if (player.tickCount % 20 != 0 || !player.isAlive()) {
            return;
        }
        CompoundTag tag = getOrCreateTag(player);
        float current = tag.getFloat(TAG_CURRENT);
        float max = getMaxFp(player);
        if (current >= max) {
            return;
        }
        long lastUse = tag.getLong(TAG_LAST_USE_TICK);
        if (player.level().getGameTime() - lastUse >= REGEN_DELAY_TICKS) {
            setFp(player, current + REGEN_PER_SECOND);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        float maxFp = getMaxFp(oldPlayer);
        setMaxFp(newPlayer, maxFp);
        if (event.isWasDeath()) {
            restoreFullFp(newPlayer);
        } else {
            setFp(newPlayer, getFp(oldPlayer));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        getOrCreateTag(event.getEntity());
    }

    @SubscribeEvent
    public static void onSiteOfGraceInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (event.getLevel().getBlockState(event.getPos()).is(ModBlocks.SITE_OF_GRACE.get())) {
            restoreFullFp(event.getEntity());
        }
    }
}
