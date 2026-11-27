package com.eldenringcore.boss;

import net.minecraft.world.entity.LivingEntity;

import java.util.function.BiConsumer;

/**
 * Defines an Elden Ring boss move with explicit Telegraph (supports delayed attacks),
 * Active hitbox window, Recovery window, range bounds, and Phase gating.
 */
public record BossAttackMove(
        String id,
        String animationName,
        int windupTicks,
        int activeTicks,
        int recoveryTicks,
        double minRange,
        double maxRange,
        float baseDamage,
        boolean phase2Only,
        int weight,
        BiConsumer<EldenBossEntity, LivingEntity> onActiveTick
) {
    public boolean canUse(EldenBossEntity boss, double distanceSqr) {
        if (phase2Only && !boss.isPhaseTwo()) {
            return false;
        }
        return distanceSqr >= (minRange * minRange) && distanceSqr <= (maxRange * maxRange);
    }
}
