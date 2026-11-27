package com.eldenringcore.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Pilot Demigod Boss #1: Margit, the Fell Omen
 * Features:
 * - Signature Delayed Cane Overhead Slam (long telegraph to bait early rolls)
 * - Fast Golden Dagger Slash & Ranged Golden Dagger Throw
 * - Phase 2 (< 50% HP): Golden Hammer Leap AoE Slam & Holy Sword Combo
 */
public class MargitEntity extends EldenBossEntity {

    public MargitEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Margit, the Fell Omen").withStyle(ChatFormatting.GOLD),
                BossEvent.BossBarColor.YELLOW);
        this.maxPoise = 120.0f;
        this.currentPoise = 120.0f;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "GREAT ENEMY FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Signature Delayed Cane Overhead Slam (24 ticks = 1.2s delay!)
        this.moveset.add(new BossAttackMove(
                "delayed_cane_slam", "animation.margit.delayed_slam",
                24, 6, 18, 0.0, 4.5, 16.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 2) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.6f, 0.6f);
                        boss.performArcHitbox(4.8, 90.0, 16.0f, 0.8, ParticleTypes.CRIT);
                    }
                }
        ));

        // 2. Quick Golden Dagger Double Slash
        this.moveset.add(new BossAttackMove(
                "golden_dagger_slash", "animation.margit.dagger_combo",
                8, 12, 12, 0.0, 3.5, 9.0f, false, 30,
                (boss, target) -> {
                    if (boss.getStateTick() == 2 || boss.getStateTick() == 8) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.4f, 1.3f);
                        boss.performArcHitbox(3.8, 120.0, 9.0f, 0.3, ParticleTypes.WAX_ON);
                    }
                }
        ));

        // 3. Ranged Golden Glintblade Throw (punishes healing / kiting)
        this.moveset.add(new BossAttackMove(
                "golden_dagger_throw", "animation.margit.throw_dagger",
                12, 10, 16, 6.0, 22.0, 8.0f, false, 25,
                (boss, target) -> {
                    if (target != null && (boss.getStateTick() == 2 || boss.getStateTick() == 7)) {
                        boss.playSound(SoundEvents.TRIDENT_THROW, 1.4f, 1.5f);
                        Vec3 start = boss.position().add(0, 2.0, 0);
                        Vec3 end = target.position().add(0, 1.0, 0);
                        if (boss.level() instanceof ServerLevel serverLevel) {
                            Vec3 dir = end.subtract(start);
                            int steps = 14;
                            for (int i = 1; i <= steps; i++) {
                                Vec3 p = start.add(dir.scale(i / (double) steps));
                                serverLevel.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0, 0, 0, 0);
                            }
                        }
                        if (boss.hasLineOfSight(target) && boss.distanceToSqr(target) < 24.0 * 24.0) {
                            target.hurt(boss.damageSources().mobAttack(boss), 8.0f);
                        }
                    }
                }
        ));

        // 4. Phase 2: Golden Hammer Leap Slam (jumps toward target and crashes down with AoE)
        this.moveset.add(new BossAttackMove(
                "golden_hammer_leap", "animation.margit.hammer_leap",
                18, 16, 24, 4.0, 16.0, 22.0f, true, 40,
                (boss, target) -> {
                    if (boss.getStateTick() == 1 && target != null) {
                        Vec3 leap = target.position().subtract(boss.position()).normalize();
                        boss.setDeltaMovement(leap.x * 1.15, 0.85, leap.z * 1.15);
                        boss.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.5f, 0.9f);
                    }
                    if (boss.getStateTick() == 13) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 1.8f, 0.7f);
                        boss.performAoeHitbox(5.5, 22.0f, 1.2, ParticleTypes.FLAME);
                    }
                }
        ));
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        if (this.level() instanceof ServerLevel serverLevel) {
            Component quote = Component.literal("Margit: \"Well, thou art of passing skill. Warrior blood must truly run in thy veins, Tarnished.\"")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
            for (ServerPlayer player : serverLevel.getPlayers(p -> p.distanceToSqr(this) <= 48.0 * 48.0)) {
                player.sendSystemMessage(quote);
            }
        }
    }
}
