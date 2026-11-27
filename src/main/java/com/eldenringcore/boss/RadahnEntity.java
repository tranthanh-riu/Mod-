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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Wave 2 Demigod Boss: Starscourge Radahn (Caelid)
 * - Phase 1: Gravity Greatbow, Colossal Dual Cleave, Starcaller Cry (pull + gravity explosion)
 * - Phase 2 (< 50% HP): Meteor Crash (sky-dive comet explosion) & Gravity Meteor Volley
 */
public class RadahnEntity extends EldenBossEntity {

    public RadahnEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Starscourge Radahn").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD),
                BossEvent.BossBarColor.PURPLE);
        this.maxPoise = 200.0f;
        this.currentPoise = 200.0f;
        this.xpReward = 900;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.ARMOR, 15.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected net.minecraft.core.particles.ParticleOptions getPhaseTwoAuraParticle() {
        return ParticleTypes.DRAGON_BREATH;
    }

    @Override
    protected String getVictoryBannerText() {
        return "DEMIGOD FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Gravity Greatbow Snipe (long range purple arrow)
        this.moveset.add(new BossAttackMove(
                "gravity_greatbow", "animation.radahn.greatbow",
                18, 6, 18, 12.0, 48.0, 18.0f, false, 35,
                (boss, target) -> {
                    if (target != null && boss.getStateTick() == 2) {
                        boss.playSound(SoundEvents.CROSSBOW_SHOOT, 2.0f, 0.5f);
                        Vec3 start = boss.getEyePosition();
                        Vec3 end = target.position().add(0, 1.0, 0);
                        if (boss.level() instanceof ServerLevel serverLevel) {
                            Vec3 dir = end.subtract(start);
                            for (int i = 1; i <= 24; i++) {
                                Vec3 p = start.add(dir.scale(i / 24.0));
                                serverLevel.sendParticles(ParticleTypes.PORTAL, p.x, p.y, p.z, 3, 0.1, 0.1, 0.1, 0.02);
                            }
                        }
                        if (boss.hasLineOfSight(target)) {
                            target.hurt(boss.damageSources().mobAttack(boss), 18.0f);
                        }
                    }
                }
        ));

        // 2. Colossal Dual Greatsword Cleave
        this.moveset.add(new BossAttackMove(
                "colossal_dual_cleave", "animation.radahn.dual_cleave",
                16, 14, 18, 0.0, 6.5, 22.0f, false, 35,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 3 || t == 11) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 0.5f);
                        boss.performArcHitbox(6.8, 160.0, 22.0f, 1.2, ParticleTypes.CRIT);
                    }
                }
        ));

        // 3. Starcaller Cry (pulls nearby players in, then slams gravity wave)
        this.moveset.add(new BossAttackMove(
                "starcaller_cry", "animation.radahn.starcaller_cry",
                16, 18, 22, 0.0, 14.0, 24.0f, false, 30,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 2) {
                        boss.playSound(SoundEvents.ENDERMAN_SCREAM, 2.0f, 0.5f);
                        AABB pullBox = boss.getBoundingBox().inflate(14.0, 4.0, 14.0);
                        for (LivingEntity e : boss.level().getEntitiesOfClass(LivingEntity.class, pullBox, ent -> ent != boss && ent.isAlive())) {
                            Vec3 pull = boss.position().subtract(e.position()).normalize();
                            e.push(pull.x * 1.4, 0.3, pull.z * 1.4);
                        }
                    } else if (t == 15) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.6f);
                        boss.performAoeHitbox(8.5, 24.0f, 1.5, ParticleTypes.DRAGON_BREATH);
                    }
                }
        ));

        // 4. Phase 2: Meteor Crash (launches skyward then slams down as a fiery meteor!)
        this.moveset.add(new BossAttackMove(
                "meteor_crash", "animation.radahn.meteor_crash",
                22, 24, 28, 4.0, 26.0, 34.0f, true, 40,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 1) {
                        boss.setDeltaMovement(0, 1.8, 0);
                        boss.playSound(SoundEvents.FIREWORK_ROCKET_LAUNCH, 2.0f, 0.5f);
                    } else if (t == 12 && target != null) {
                        Vec3 dive = target.position().subtract(boss.position()).normalize();
                        boss.setDeltaMovement(dive.x * 2.2, -1.6, dive.z * 2.2);
                    } else if (t == 20) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.5f, 0.4f);
                        boss.performAoeHitbox(11.0, 34.0f, 1.8, ParticleTypes.EXPLOSION_EMITTER);
                        if (boss.level() instanceof ServerLevel sl) {
                            sl.sendParticles(ParticleTypes.FLAME, boss.getX(), boss.getY() + 1.0, boss.getZ(), 80, 5.0, 1.5, 5.0, 0.15);
                        }
                    }
                }
        ));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (int i = 0; i < 3; i++) {
            this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
        }
    }
}
