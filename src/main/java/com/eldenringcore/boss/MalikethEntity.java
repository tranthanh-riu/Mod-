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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Wave 4 Endgame Legend Boss: Maliketh, the Black Blade (Crumbling Farum Azula)
 * - Phase 1: Beast Clergyman agile claw waves
 * - Phase 2 (< 50% HP): Destined Death black-red blade slashes applying heavy Wither burn!
 */
public class MalikethEntity extends EldenBossEntity {

    public MalikethEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Maliketh, the Black Blade").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED);
        this.maxPoise = 110.0f;
        this.currentPoise = 110.0f;
        this.xpReward = 1800;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1200.0)
                .add(Attributes.MOVEMENT_SPEED, 0.40)
                .add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85)
                .add(Attributes.FOLLOW_RANGE, 56.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "LEGEND FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Beast Claw Ground Shockwave
        this.moveset.add(new BossAttackMove(
                "beast_claw", "animation.maliketh.beast_claw",
                12, 8, 14, 0.0, 10.0, 18.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.RAVAGER_ROAR, 1.5f, 1.2f);
                        boss.performArcHitbox(9.5, 95.0, 18.0f, 1.0, ParticleTypes.CAMPFIRE_COSY_SMOKE);
                    }
                }
        ));

        // 2. Acrobatic Spinning Leap Slash
        this.moveset.add(new BossAttackMove(
                "acrobatic_slash", "animation.maliketh.acrobatic_slash",
                14, 14, 16, 4.0, 18.0, 22.0f, false, 30,
                (boss, target) -> {
                    if (boss.getStateTick() == 1 && target != null) {
                        Vec3 leap = target.position().subtract(boss.position()).normalize();
                        boss.setDeltaMovement(leap.x * 1.6, 0.85, leap.z * 1.6);
                    }
                    if (boss.getStateTick() == 10) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 0.6f);
                        boss.performArcHitbox(6.5, 150.0, 22.0f, 1.1, ParticleTypes.SMOKE);
                        applyDestinedDeath(boss, 6.5);
                    }
                }
        ));

        // 3. Phase 2: Destined Death Flurry (black-red dome of death slashes)
        this.moveset.add(new BossAttackMove(
                "destined_death", "animation.maliketh.destined_death",
                18, 18, 22, 0.0, 9.5, 28.0f, true, 45,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 4 || t == 10 || t == 16) {
                        boss.playSound(SoundEvents.WITHER_HURT, 2.0f, 0.5f);
                        boss.performAoeHitbox(8.5, 16.0f, 0.6, ParticleTypes.CRIMSON_SPORE);
                        applyDestinedDeath(boss, 8.5);
                    }
                }
        ));
    }

    private static void applyDestinedDeath(EldenBossEntity boss, double radius) {
        if (!boss.isPhaseTwo()) return;
        AABB box = boss.getBoundingBox().inflate(radius, 3.0, radius);
        for (LivingEntity e : boss.level().getEntitiesOfClass(LivingEntity.class, box, ent -> ent != boss && ent.isAlive())) {
            e.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 2));
        }
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        if (this.level() instanceof ServerLevel sl) {
            Component quote = Component.literal("Maliketh: \"O, Death. Become my blade once more.\"")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
            for (ServerPlayer p : sl.getPlayers(pl -> pl.distanceToSqr(this) <= 56.0 * 56.0)) {
                p.sendSystemMessage(quote);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (int i = 0; i < 3; i++) this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
