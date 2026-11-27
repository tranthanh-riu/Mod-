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
 * Wave 4 Endgame Demigod Boss: Malenia, Blade of Miquella / Goddess of Rot
 * - Signature Lifesteal on every hit
 * - Waterfowl Dance (3-flurry lethal aerial slashing combo)
 * - Phase 2 (< 50% HP): Goddess of Rot — Scarlet Aeonia rot-flower nuke applying Scarlet Rot!
 */
public class MaleniaEntity extends EldenBossEntity {

    public MaleniaEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Malenia, Blade of Miquella").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED);
        this.maxPoise = 80.0f; // Authentic: lower poise like Elden Ring, but lethal speed & lifesteal
        this.currentPoise = 80.0f;
        this.xpReward = 2000;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1400.0)
                .add(Attributes.MOVEMENT_SPEED, 0.38)
                .add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 52.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "DEMIGOD FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Rapid Triple Katana Slash (with lifesteal)
        this.moveset.add(new BossAttackMove(
                "rapid_triple_slash", "animation.malenia.triple_slash",
                10, 14, 12, 0.0, 4.8, 14.0f, false, 35,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 2 || t == 7 || t == 12) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.7f, 1.4f);
                        boss.performArcHitbox(5.0, 130.0, 14.0f, 0.35, ParticleTypes.SWEEP_ATTACK);
                        applyLifestealAndRot(boss, 5.0, 22.0f);
                    }
                }
        ));

        // 2. High-Speed Piercing Lunge Thrust
        this.moveset.add(new BossAttackMove(
                "lunge_thrust", "animation.malenia.lunge_thrust",
                14, 8, 16, 5.0, 16.0, 24.0f, false, 30,
                (boss, target) -> {
                    if (boss.getStateTick() == 1 && target != null) {
                        Vec3 dash = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                        boss.setDeltaMovement(dash.x * 1.9, 0.08, dash.z * 1.9);
                        boss.playSound(SoundEvents.TRIDENT_RIPTIDE_3, 1.6f, 1.5f);
                    }
                    if (boss.getStateTick() == 5) {
                        boss.performArcHitbox(5.2, 70.0, 24.0f, 1.0, ParticleTypes.CRIT);
                        applyLifestealAndRot(boss, 5.2, 35.0f);
                    }
                }
        ));

        // 3. Signature WATERFOWL DANCE (leaps in air, then 3 rapid dashing slash flurries!)
        this.moveset.add(new BossAttackMove(
                "waterfowl_dance", "animation.malenia.waterfowl_dance",
                24, 30, 24, 0.0, 16.0, 12.0f, false, 25,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 1) {
                        boss.setDeltaMovement(0, 0.9, 0);
                        boss.playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.8f, 1.4f);
                    }
                    if ((t == 6 || t == 15 || t == 24) && target != null) {
                        Vec3 dash = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                        boss.setDeltaMovement(dash.x * 1.6, 0.15, dash.z * 1.6);
                    }
                    if (t == 8 || t == 11 || t == 17 || t == 20 || t == 26 || t == 29) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 2.0f, 1.6f);
                        boss.performAoeHitbox(5.0, 12.0f, 0.2, ParticleTypes.SWEEP_ATTACK);
                        applyLifestealAndRot(boss, 5.0, 18.0f);
                    }
                }
        ));

        // 4. Phase 2: SCARLET AEONIA (dives into a blooming scarlet rot flower explosion)
        this.moveset.add(new BossAttackMove(
                "scarlet_aeonia", "animation.malenia.scarlet_aeonia",
                22, 18, 28, 3.0, 18.0, 30.0f, true, 40,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 1 && target != null) {
                        Vec3 leap = target.position().subtract(boss.position()).normalize();
                        boss.setDeltaMovement(leap.x * 1.2, 1.1, leap.z * 1.2);
                    } else if (t == 14) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.2f, 0.6f);
                        boss.performAoeHitbox(9.5, 30.0f, 1.4, ParticleTypes.CRIMSON_SPORE);
                        AABB box = boss.getBoundingBox().inflate(9.5, 3.0, 9.5);
                        for (LivingEntity e : boss.level().getEntitiesOfClass(LivingEntity.class, box, ent -> ent != boss && ent.isAlive())) {
                            StatusBuildupManager.addBuildup(e, boss, StatusEffectType.SCARLET_ROT, 100.0f);
                        }
                    }
                }
        ));
    }

    private static void applyLifestealAndRot(EldenBossEntity boss, double radius, float healAmount) {
        AABB box = boss.getBoundingBox().inflate(radius, 2.0, radius);
        boolean hitAny = false;
        for (LivingEntity e : boss.level().getEntitiesOfClass(LivingEntity.class, box, ent -> ent != boss && ent.isAlive())) {
            hitAny = true;
            if (boss.isPhaseTwo()) {
                StatusBuildupManager.addBuildup(e, boss, StatusEffectType.SCARLET_ROT, 35.0f);
            }
        }
        if (hitAny) {
            boss.heal(healAmount);
        }
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        this.bossEvent.setName(Component.literal("Malenia, Goddess of Rot").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 2.0, getZ(), 90, 4.0, 2.0, 4.0, 0.12);
            Component quote = Component.literal("Malenia: \"Wait. The scarlet bloom flowers once more. You will witness true horror. Now, rot!\"")
                    .withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);
            for (ServerPlayer p : sl.getPlayers(pl -> pl.distanceToSqr(this) <= 56.0 * 56.0)) {
                p.sendSystemMessage(quote);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        this.spawnAtLocation(ModItems.HAND_OF_MALENIA.get());
        for (int i = 0; i < 3; i++) this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
