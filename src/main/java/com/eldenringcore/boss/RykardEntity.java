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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Wave 3 Demigod Boss: Rykard, Lord of Blasphemy (Volcano Manor)
 * - Phase 1: Magma Sweep, Blasphemous Ground Eruption
 * - Phase 2 (< 50% HP): Rancor Skull Storm & Taker's Flames Wave
 */
public class RykardEntity extends EldenBossEntity {

    public RykardEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("God-Devouring Serpent").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED);
        this.maxPoise = 180.0f;
        this.currentPoise = 180.0f;
        this.xpReward = 1100;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1300.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 22.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 52.0);
    }

    @Override
    protected boolean usesSecondHealthBar() {
        return true; // Authentic: God-Devouring Serpent -> Rykard, Lord of Blasphemy
    }

    @Override
    protected net.minecraft.core.particles.ParticleOptions getPhaseTwoAuraParticle() {
        return ParticleTypes.LAVA;
    }

    @Override
    protected String getVictoryBannerText() {
        return "DEMIGOD FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Wide Blasphemous Blade Sweep
        this.moveset.add(new BossAttackMove(
                "magma_sweep", "animation.rykard.magma_sweep",
                20, 10, 20, 0.0, 8.0, 22.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 4) {
                        boss.playSound(SoundEvents.BLAZE_SHOOT, 2.0f, 0.5f);
                        boss.performArcHitbox(8.5, 160.0, 22.0f, 1.2, ParticleTypes.LAVA);
                    }
                }
        ));

        // 2. Magma Ground Eruption
        this.moveset.add(new BossAttackMove(
                "magma_eruption", "animation.rykard.eruption",
                22, 8, 22, 0.0, 12.0, 24.0f, false, 30,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.6f);
                        boss.performAoeHitbox(9.5, 24.0f, 1.3, ParticleTypes.FLAME);
                    }
                }
        ));

        // 3. Phase 2: Rancor Skull Storm
        this.moveset.add(new BossAttackMove(
                "rancor_storm", "animation.rykard.rancor_storm",
                20, 24, 24, 4.0, 24.0, 14.0f, true, 40,
                (boss, target) -> {
                    if (boss.getStateTick() % 6 == 0) {
                        boss.playSound(SoundEvents.WITHER_SHOOT, 1.8f, 0.6f);
                        boss.performAoeHitbox(12.0, 14.0f, 0.6, ParticleTypes.SOUL);
                    }
                }
        ));
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        this.bossEvent.setName(Component.literal("Rykard, Lord of Blasphemy").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
        if (this.level() instanceof ServerLevel sl) {
            Component quote = Component.literal("Rykard: \"TOGETHAAA, we will devour the very gods!\"")
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
