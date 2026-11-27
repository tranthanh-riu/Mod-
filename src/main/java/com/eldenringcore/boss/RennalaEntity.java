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
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Wave 2 Boss: Rennala, Queen of the Full Moon (Raya Lucaria)
 * - Phase 1: Glintstone Barrage, Lunar Barrier Knockback
 * - Phase 2 (< 50% HP): Comet Azur Beam, Full Moon Explosion, Spirit Summons
 */
public class RennalaEntity extends EldenBossEntity {

    public RennalaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Rennala, Queen of the Full Moon").withStyle(ChatFormatting.AQUA),
                BossEvent.BossBarColor.BLUE);
        this.maxPoise = 90.0f;
        this.currentPoise = 90.0f;
        this.xpReward = 650;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 700.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.75)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "LEGEND FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Glintstone Pebble Barrage (ranged magic bursts)
        this.moveset.add(new BossAttackMove(
                "glintstone_barrage", "animation.rennala.glintstone_barrage",
                14, 16, 16, 5.0, 26.0, 10.0f, false, 35,
                (boss, target) -> {
                    if (target != null && (boss.getStateTick() == 2 || boss.getStateTick() == 8 || boss.getStateTick() == 14)) {
                        boss.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.5f, 1.3f);
                        fireMagicBeam(boss, target, 10.0f, 26.0, ParticleTypes.SOUL_FIRE_FLAME);
                    }
                }
        ));

        // 2. Lunar Barrier (close-range knockback wave)
        this.moveset.add(new BossAttackMove(
                "lunar_barrier", "animation.rennala.lunar_barrier",
                12, 6, 16, 0.0, 5.5, 13.0f, false, 30,
                (boss, target) -> {
                    if (boss.getStateTick() == 2) {
                        boss.playSound(SoundEvents.BEACON_DEACTIVATE, 1.6f, 1.4f);
                        boss.performAoeHitbox(6.0, 13.0f, 1.5, ParticleTypes.ENCHANT);
                    }
                }
        ));

        // 3. Phase 2: Comet Azur (sustained devastating blue beam)
        this.moveset.add(new BossAttackMove(
                "comet_azur", "animation.rennala.comet_azur",
                22, 24, 24, 4.0, 28.0, 12.0f, true, 40,
                (boss, target) -> {
                    if (boss.getStateTick() % 4 == 0) {
                        boss.playSound(SoundEvents.BEACON_AMBIENT, 2.0f, 1.8f);
                        Vec3 forward = boss.getLookAngle().normalize();
                        if (boss.level() instanceof ServerLevel serverLevel) {
                            for (double d = 1.5; d <= 24.0; d += 1.2) {
                                Vec3 p = boss.getEyePosition().add(forward.scale(d));
                                serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 6, 0.35, 0.35, 0.35, 0.02);
                                AABB box = new AABB(p.x - 1.2, p.y - 1.2, p.z - 1.2, p.x + 1.2, p.y + 1.2, p.z + 1.2);
                                for (LivingEntity e : serverLevel.getEntitiesOfClass(LivingEntity.class, box, ent -> ent != boss && ent.isAlive())) {
                                    e.hurt(boss.damageSources().magic(), 9.0f);
                                }
                            }
                        }
                    }
                }
        ));

        // 4. Phase 2: Rennala's Full Moon (massive lunar AoE detonation)
        this.moveset.add(new BossAttackMove(
                "full_moon_orb", "animation.rennala.full_moon",
                26, 8, 24, 0.0, 14.0, 26.0f, true, 30,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 1.2f);
                        boss.performAoeHitbox(9.0, 26.0f, 1.4, ParticleTypes.END_ROD);
                    }
                }
        ));

        // 5. Phase 2: Spirit Summon Wolves
        this.moveset.add(new BossAttackMove(
                "spirit_summon", "animation.rennala.summon",
                18, 6, 26, 4.0, 26.0, 0.0f, true, 20,
                (boss, target) -> {
                    if (boss.getStateTick() == 3 && boss.level() instanceof ServerLevel serverLevel && target != null) {
                        boss.playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 1.6f, 1.2f);
                        for (int i = 0; i < 2; i++) {
                            Wolf spirit = EntityType.WOLF.create(serverLevel);
                            if (spirit != null) {
                                spirit.moveTo(boss.getX() + (i == 0 ? 2.5 : -2.5), boss.getY(), boss.getZ(), 0, 0);
                                spirit.setTarget(target);
                                spirit.setCustomName(Component.literal("Lone Wolf Spirit").withStyle(ChatFormatting.AQUA));
                                serverLevel.addFreshEntity(spirit);
                            }
                        }
                    }
                }
        ));
    }

    private static void fireMagicBeam(EldenBossEntity boss, LivingEntity target, float damage, double maxRange, net.minecraft.core.particles.ParticleOptions particle) {
        Vec3 start = boss.getEyePosition();
        Vec3 end = target.position().add(0, 1.0, 0);
        if (boss.level() instanceof ServerLevel serverLevel) {
            Vec3 dir = end.subtract(start);
            for (int i = 1; i <= 16; i++) {
                Vec3 p = start.add(dir.scale(i / 16.0));
                serverLevel.sendParticles(particle, p.x, p.y, p.z, 2, 0.08, 0.08, 0.08, 0.01);
            }
        }
        if (boss.hasLineOfSight(target) && boss.distanceToSqr(target) <= maxRange * maxRange) {
            target.hurt(boss.damageSources().magic(), damage);
        }
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        this.bossEvent.setColor(BossEvent.BossBarColor.BLUE);
        if (this.level() instanceof ServerLevel serverLevel) {
            Component quote = Component.literal("Ranni: \"Upon my name as Ranni the Witch. Mother's rich slumber shall not be disturbed by thee.\"")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC);
            for (ServerPlayer p : serverLevel.getPlayers(pl -> pl.distanceToSqr(this) <= 56.0 * 56.0)) {
                p.sendSystemMessage(quote);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
        this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
