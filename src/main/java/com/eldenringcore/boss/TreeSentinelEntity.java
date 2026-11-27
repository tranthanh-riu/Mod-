package com.eldenringcore.boss;

import com.eldenringcore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Wave 1 Open-Field Boss: Tree Sentinel (Limgrave)
 * - Armored mounted golden knight with heavy halberd sweeps, charges, and Phase 2 Erdtree Shield Slams.
 */
public class TreeSentinelEntity extends EldenBossEntity {

    public TreeSentinelEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Tree Sentinel").withStyle(ChatFormatting.GOLD),
                BossEvent.BossBarColor.YELLOW);
        this.maxPoise = 160.0f;
        this.currentPoise = 160.0f;
        this.xpReward = 350;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 600.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 16.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.FOLLOW_RANGE, 44.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "GREAT ENEMY FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Mounted Halberd Charge (long gap-closer)
        this.moveset.add(new BossAttackMove(
                "halberd_charge", "animation.tree_sentinel.halberd_charge",
                16, 14, 20, 6.0, 20.0, 18.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 1 && target != null) {
                        Vec3 charge = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                        boss.setDeltaMovement(charge.x * 1.4, 0.15, charge.z * 1.4);
                        boss.playSound(SoundEvents.HORSE_GALLOP, 1.8f, 0.8f);
                    }
                    if (boss.getStateTick() == 8 || boss.getStateTick() == 12) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.5f, 0.7f);
                        boss.performArcHitbox(5.0, 110.0, 18.0f, 1.1, ParticleTypes.CRIT);
                    }
                }
        ));

        // 2. Wide Golden Halberd Sweep
        this.moveset.add(new BossAttackMove(
                "halberd_sweep", "animation.tree_sentinel.halberd_sweep",
                14, 8, 16, 0.0, 5.2, 15.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.6f, 0.65f);
                        boss.performArcHitbox(5.5, 165.0, 15.0f, 0.85, ParticleTypes.SWEEP_ATTACK);
                    }
                }
        ));

        // 3. Delayed Overhead Halberd Smash
        this.moveset.add(new BossAttackMove(
                "delayed_overhead_halberd", "animation.tree_sentinel.delayed_smash",
                26, 6, 20, 0.0, 4.8, 20.0f, false, 25,
                (boss, target) -> {
                    if (boss.getStateTick() == 2) {
                        boss.playSound(SoundEvents.ANVIL_LAND, 1.5f, 0.75f);
                        boss.performArcHitbox(5.2, 85.0, 20.0f, 1.0, ParticleTypes.WAX_ON);
                    }
                }
        ));

        // 4. Phase 2: Golden Erdtree Shield Slam (leaps and slams shield creating golden shockwave)
        this.moveset.add(new BossAttackMove(
                "golden_shield_slam", "animation.tree_sentinel.shield_slam",
                20, 16, 24, 0.0, 10.0, 24.0f, true, 45,
                (boss, target) -> {
                    if (boss.getStateTick() == 1 && target != null) {
                        Vec3 leap = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                        boss.setDeltaMovement(leap.x * 0.85, 0.75, leap.z * 0.85);
                        boss.playSound(SoundEvents.HORSE_ANGRY, 1.6f, 0.8f);
                    }
                    if (boss.getStateTick() == 13) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 1.8f, 0.8f);
                        boss.performAoeHitbox(6.5, 24.0f, 1.35, ParticleTypes.END_ROD);
                    }
                }
        ));

        // 5. Phase 2: Erdtree Golden Retaliation (ranged holy beam)
        this.moveset.add(new BossAttackMove(
                "erdtree_retaliation", "animation.tree_sentinel.shield_beam",
                14, 8, 18, 8.0, 24.0, 14.0f, true, 30,
                (boss, target) -> {
                    if (target != null && boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.BEACON_ACTIVATE, 1.6f, 1.4f);
                        Vec3 start = boss.position().add(0, 2.2, 0);
                        Vec3 end = target.position().add(0, 1.0, 0);
                        if (boss.level() instanceof ServerLevel serverLevel) {
                            Vec3 dir = end.subtract(start);
                            for (int i = 1; i <= 18; i++) {
                                Vec3 p = start.add(dir.scale(i / 18.0));
                                serverLevel.sendParticles(ParticleTypes.WAX_ON, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.01);
                            }
                        }
                        if (boss.hasLineOfSight(target) && boss.distanceToSqr(target) < 26.0 * 26.0) {
                            target.hurt(boss.damageSources().magic(), 14.0f);
                        }
                    }
                }
        ));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
        this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
