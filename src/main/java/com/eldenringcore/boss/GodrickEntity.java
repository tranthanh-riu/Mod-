package com.eldenringcore.boss;

import com.eldenringcore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Wave 1 Stormveil Final Demigod Boss: Godrick the Grafted
 * - Phase 1: Delayed Axe Cleave, Storm Whirlwind, 2-Hit Earthquake Slam
 * - Phase 2 (< 50% HP): Grafted Dragon Head! Sweeping Dragon Fire Breath & Flaming Storm Whirlwind
 */
public class GodrickEntity extends EldenBossEntity {

    public GodrickEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Godrick the Grafted").withStyle(ChatFormatting.GOLD),
                BossEvent.BossBarColor.YELLOW);
        this.maxPoise = 140.0f;
        this.currentPoise = 140.0f;
        this.xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 750.0)
                .add(Attributes.MOVEMENT_SPEED, 0.29)
                .add(Attributes.ATTACK_DAMAGE, 18.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.90)
                .add(Attributes.FOLLOW_RANGE, 44.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "DEMIGOD FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Delayed Great-Axe Overhead Cleave
        this.moveset.add(new BossAttackMove(
                "delayed_axe_cleave", "animation.godrick.delayed_axe",
                26, 8, 18, 0.0, 5.0, 20.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 1.7f, 0.55f);
                        boss.performArcHitbox(5.4, 110.0, 20.0f, 1.0, ParticleTypes.CRIT);
                    }
                }
        ));

        // 2. Stormcaller Whirlwind (spinning AoE wind attack)
        this.moveset.add(new BossAttackMove(
                "storm_whirlwind", "animation.godrick.whirlwind",
                14, 18, 18, 0.0, 6.0, 12.0f, false, 30,
                (boss, target) -> {
                    int tick = boss.getStateTick();
                    if (tick == 3 || tick == 10 || tick == 16) {
                        boss.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.6f, 1.1f);
                        boss.performAoeHitbox(5.8, 12.0f, 0.7, boss.isPhaseTwo() ? ParticleTypes.FLAME : ParticleTypes.CLOUD);
                    }
                }
        ));

        // 3. 2-Hit Earthquake Ground Slam
        this.moveset.add(new BossAttackMove(
                "earthquake_slam", "animation.godrick.earthquake",
                18, 18, 22, 0.0, 8.0, 18.0f, false, 30,
                (boss, target) -> {
                    int tick = boss.getStateTick();
                    if (tick == 2) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 1.5f, 0.7f);
                        boss.performAoeHitbox(5.0, 16.0f, 0.8, ParticleTypes.CAMPFIRE_COSY_SMOKE);
                    } else if (tick == 14) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.5f);
                        boss.performAoeHitbox(7.8, 22.0f, 1.2, ParticleTypes.EXPLOSION);
                    }
                }
        ));

        // 4. Phase 2: Grafted Dragon Fire Breath (sweeping fire cone setting targets ablaze)
        this.moveset.add(new BossAttackMove(
                "dragon_fire_breath", "animation.godrick.dragon_breath",
                16, 24, 22, 3.0, 14.0, 10.0f, true, 45,
                (boss, target) -> {
                    int tick = boss.getStateTick();
                    if (tick % 4 == 0) {
                        boss.playSound(SoundEvents.BLAZE_SHOOT, 1.6f, 0.7f);
                        Vec3 forward = boss.getLookAngle().multiply(1, 0, 1).normalize();
                        if (boss.level() instanceof ServerLevel serverLevel) {
                            for (double d = 2.0; d <= 13.0; d += 1.5) {
                                Vec3 p = boss.position().add(forward.scale(d)).add(0, 1.4, 0);
                                serverLevel.sendParticles(ParticleTypes.FLAME, p.x, p.y, p.z, 8, 0.9, 0.4, 0.9, 0.04);
                            }
                        }
                        double minDot = Math.cos(Math.toRadians(35.0));
                        AABB box = boss.getBoundingBox().inflate(13.5, 2.0, 13.5);
                        for (LivingEntity entity : boss.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != boss && e.isAlive())) {
                            Vec3 toTarget = entity.position().subtract(boss.position()).multiply(1, 0, 1);
                            double dist = toTarget.length();
                            if (dist <= 13.5 && dist > 0.001 && forward.dot(toTarget.normalize()) >= minDot) {
                                entity.hurt(boss.damageSources().onFire(), 9.0f);
                                entity.setSecondsOnFire(4);
                            }
                        }
                    }
                }
        ));
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 1.8, getZ(), 60, 1.5, 1.5, 1.5, 0.12);
            Component quote = Component.literal("Godrick: \"Forefathers, one and all... BEAR WITNESS!\"")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
            for (ServerPlayer player : serverLevel.getPlayers(p -> p.distanceToSqr(this) <= 56.0 * 56.0)) {
                player.sendSystemMessage(quote);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
        this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
        this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
