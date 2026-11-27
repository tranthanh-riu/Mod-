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
 * Wave 4 Final Boss: Radagon of the Golden Order (Erdtree Sanctuary)
 * - Golden Teleport Slam, Holy Lightning Spear Throw, Triple Golden Order Ground Slam
 * - Phase 2 (< 50% HP): Sacred Golden Wave & Elden Stars Holy Barrage
 */
public class RadagonEntity extends EldenBossEntity {

    public RadagonEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Radagon of the Golden Order").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                BossEvent.BossBarColor.YELLOW);
        this.maxPoise = 170.0f;
        this.currentPoise = 170.0f;
        this.xpReward = 2500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1500.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 25.0)
                .add(Attributes.ARMOR, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 56.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "GOD SLAIN";
    }

    @Override
    protected void initMoveset() {
        // 1. Golden Teleport Blink & Hammer Smash
        this.moveset.add(new BossAttackMove(
                "golden_teleport_smash", "animation.radagon.teleport_smash",
                16, 10, 16, 5.0, 22.0, 24.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 1 && target != null) {
                        Vec3 behind = target.position().subtract(target.getLookAngle().multiply(1, 0, 1).normalize().scale(2.5));
                        boss.teleportTo(behind.x, target.getY(), behind.z);
                        boss.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.8f, 1.2f);
                    }
                    if (boss.getStateTick() == 6) {
                        boss.playSound(SoundEvents.ANVIL_LAND, 2.0f, 0.7f);
                        boss.performAoeHitbox(5.5, 24.0f, 1.2, ParticleTypes.END_ROD);
                    }
                }
        ));

        // 2. Golden Lightning Spear Throw
        this.moveset.add(new BossAttackMove(
                "golden_spear", "animation.radagon.golden_spear",
                14, 6, 16, 6.0, 28.0, 20.0f, false, 30,
                (boss, target) -> {
                    if (target != null && boss.getStateTick() == 2) {
                        boss.playSound(SoundEvents.TRIDENT_THUNDER, 2.0f, 1.1f);
                        Vec3 start = boss.getEyePosition();
                        Vec3 end = target.position().add(0, 1.0, 0);
                        if (boss.level() instanceof ServerLevel sl) {
                            Vec3 dir = end.subtract(start);
                            for (int i = 1; i <= 20; i++) {
                                Vec3 p = start.add(dir.scale(i / 20.0));
                                sl.sendParticles(ParticleTypes.WAX_ON, p.x, p.y, p.z, 3, 0.1, 0.1, 0.1, 0.01);
                            }
                        }
                        if (boss.hasLineOfSight(target)) {
                            target.hurt(boss.damageSources().magic(), 20.0f);
                        }
                    }
                }
        ));

        // 3. Phase 2: Triple Golden Order Ground Slam (iconic 3-hit expanding golden rings!)
        this.moveset.add(new BossAttackMove(
                "triple_golden_slam", "animation.radagon.triple_slam",
                18, 28, 24, 0.0, 14.0, 28.0f, true, 45,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 4) {
                        boss.playSound(SoundEvents.ANVIL_LAND, 2.0f, 0.8f);
                        boss.performAoeHitbox(6.0, 20.0f, 0.9, ParticleTypes.END_ROD);
                    } else if (t == 14) {
                        boss.playSound(SoundEvents.ANVIL_LAND, 2.2f, 0.7f);
                        boss.performAoeHitbox(9.0, 24.0f, 1.2, ParticleTypes.WAX_ON);
                    } else if (t == 25) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.5f, 0.6f);
                        boss.performAoeHitbox(13.0, 32.0f, 1.6, ParticleTypes.FLASH);
                    }
                }
        ));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (int i = 0; i < 5; i++) this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
