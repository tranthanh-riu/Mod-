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
import net.minecraft.world.phys.Vec3;

/**
 * Wave 3 Demigod Boss: Morgott, the Omen King (Leyndell)
 * - Fast holy weapons (spear, sword rain) + Phase 2 Cursed-Blood Slice (Bleed + Bloodflame)
 */
public class MorgottEntity extends EldenBossEntity {

    public MorgottEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Morgott, the Omen King").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                BossEvent.BossBarColor.YELLOW);
        this.maxPoise = 150.0f;
        this.currentPoise = 150.0f;
        this.xpReward = 1000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1050.0)
                .add(Attributes.MOVEMENT_SPEED, 0.36)
                .add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.ARMOR, 13.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    protected float getPhaseTwoThreshold() {
        return 0.55f; // Authentic Elden Ring: Morgott unleashes Cursed Bloodflame at ~55% HP
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
        // 1. Holy Spear Lunge Thrust
        this.moveset.add(new BossAttackMove(
                "holy_spear_charge", "animation.morgott.spear_charge",
                14, 10, 16, 4.0, 14.0, 20.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 1 && target != null) {
                        Vec3 lunge = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                        boss.setDeltaMovement(lunge.x * 1.5, 0.1, lunge.z * 1.5);
                        boss.playSound(SoundEvents.TRIDENT_RIPTIDE_2, 1.5f, 1.2f);
                    }
                    if (boss.getStateTick() == 6) {
                        boss.performArcHitbox(5.5, 70.0, 20.0f, 1.0, ParticleTypes.END_ROD);
                    }
                }
        ));

        // 2. Rain of Golden Swords (AoE storm)
        this.moveset.add(new BossAttackMove(
                "sword_rain", "animation.morgott.sword_rain",
                18, 20, 20, 0.0, 16.0, 10.0f, false, 25,
                (boss, target) -> {
                    if (boss.getStateTick() % 5 == 0) {
                        boss.playSound(SoundEvents.ARROW_HIT, 1.5f, 1.4f);
                        boss.performAoeHitbox(9.0, 10.0f, 0.3, ParticleTypes.WAX_ON);
                    }
                }
        ));

        // 3. Phase 2: Cursed-Blood Slice (high speed slash applying Bleed)
        this.moveset.add(new BossAttackMove(
                "cursed_blood_slice", "animation.morgott.cursed_slice",
                16, 10, 18, 0.0, 7.5, 24.0f, true, 45,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.8f, 0.6f);
                        boss.performArcHitbox(7.5, 130.0, 24.0f, 1.1, ParticleTypes.CRIMSON_SPORE);
                        AABB box = boss.getBoundingBox().inflate(7.5, 2.0, 7.5);
                        for (LivingEntity e : boss.level().getEntitiesOfClass(LivingEntity.class, box, ent -> ent != boss && ent.isAlive())) {
                            StatusBuildupManager.addBuildup(e, boss, StatusEffectType.BLEED, 55.0f);
                        }
                    }
                }
        ));
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        if (this.level() instanceof ServerLevel sl) {
            Component quote = Component.literal("Morgott: \"The thrones... stained by my curse... Such shame I cannot bear.\"")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC);
            for (ServerPlayer p : sl.getPlayers(pl -> pl.distanceToSqr(this) <= 56.0 * 56.0)) {
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
