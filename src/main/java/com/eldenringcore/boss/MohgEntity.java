package com.eldenringcore.boss;

import com.eldenringcore.combat.StatusBuildupManager;
import com.eldenringcore.combat.StatusEffectType;
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

/**
 * Wave 3 Demigod Boss: Mohg, Lord of Blood (Mohgwyn Palace)
 * - Bloodflame Trident Sweeps + Bloodboon Bleed Rain
 * - Phase 2 (< 50% HP): Iconic "NIHIL!" arena-wide Bleed burst + self-healing!
 */
public class MohgEntity extends EldenBossEntity {

    public MohgEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Mohg, Lord of Blood").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED);
        this.maxPoise = 160.0f;
        this.currentPoise = 160.0f;
        this.xpReward = 1200;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1250.0)
                .add(Attributes.MOVEMENT_SPEED, 0.29)
                .add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.95)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected net.minecraft.core.particles.ParticleOptions getPhaseTwoAuraParticle() {
        return ParticleTypes.CRIMSON_SPORE;
    }

    @Override
    protected String getVictoryBannerText() {
        return "DEMIGOD FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Delayed Bloodflame Trident Sweep
        this.moveset.add(new BossAttackMove(
                "trident_sweep", "animation.mohg.trident_sweep",
                22, 8, 18, 0.0, 6.2, 20.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.8f, 0.55f);
                        boss.performArcHitbox(6.5, 145.0, 20.0f, 0.9, ParticleTypes.CRIMSON_SPORE);
                        applyArenaBleed(boss, 6.5, 45.0f);
                    }
                }
        ));

        // 2. Bloodboon Shower (throws bloodflame across arena)
        this.moveset.add(new BossAttackMove(
                "bloodboon_rain", "animation.mohg.bloodboon",
                14, 12, 18, 4.0, 16.0, 14.0f, false, 30,
                (boss, target) -> {
                    if (boss.getStateTick() == 4) {
                        boss.playSound(SoundEvents.BLAZE_SHOOT, 1.6f, 0.6f);
                        boss.performAoeHitbox(9.0, 14.0f, 0.4, ParticleTypes.CRIMSON_SPORE);
                        applyArenaBleed(boss, 9.0, 50.0f);
                    }
                }
        ));

        // 3. Phase 2: Bloodflame Wing Slam
        this.moveset.add(new BossAttackMove(
                "bloodflame_dive", "animation.mohg.wing_dive",
                18, 14, 22, 4.0, 16.0, 26.0f, true, 40,
                (boss, target) -> {
                    if (boss.getStateTick() == 10) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 1.9f, 0.6f);
                        boss.performAoeHitbox(7.5, 26.0f, 1.3, ParticleTypes.FLAME);
                        applyArenaBleed(boss, 7.5, 65.0f);
                    }
                }
        ));
    }

    private static void applyArenaBleed(EldenBossEntity boss, double radius, float buildup) {
        AABB box = boss.getBoundingBox().inflate(radius, 3.0, radius);
        for (LivingEntity e : boss.level().getEntitiesOfClass(LivingEntity.class, box, ent -> ent != boss && ent.isAlive())) {
            StatusBuildupManager.addBuildup(e, boss, StatusEffectType.BLEED, buildup);
        }
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        // Iconic NIHIL! Bloodloss ritual heals Mohg and triggers heavy Bleed on all nearby players
        this.heal(this.getMaxHealth() * 0.15f);
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 2.0, getZ(), 100, 6.0, 2.0, 6.0, 0.1);
            Component quote = Component.literal("Mohg: \"NIHIL! NIHIL! NIHIL!\"")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
            for (ServerPlayer p : sl.getPlayers(pl -> pl.distanceToSqr(this) <= 48.0 * 48.0)) {
                p.sendSystemMessage(quote);
                StatusBuildupManager.addBuildup(p, this, StatusEffectType.BLEED, 100.0f);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (int i = 0; i < 3; i++) this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
