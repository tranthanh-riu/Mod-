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
import net.minecraft.world.phys.Vec3;

/**
 * Wave 4 Endgame Legend Boss: Godfrey, First Elden Lord / Hoarah Loux, Warrior
 * - Phase 1: Great-Axe Cleave & Arena-Wide Seismic Stomp
 * - Phase 2 (< 50% HP): Hoarah Loux, Warrior! Unarmed high-speed rush & Earthshaker slam
 */
public class GodfreyEntity extends EldenBossEntity {

    public GodfreyEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Godfrey, First Elden Lord").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
                BossEvent.BossBarColor.YELLOW);
        this.maxPoise = 180.0f;
        this.currentPoise = 180.0f;
        this.xpReward = 1900;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1350.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.ARMOR, 15.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 52.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "LEGEND FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Godfrey's Great-Axe Cleave
        this.moveset.add(new BossAttackMove(
                "axe_cleave", "animation.godfrey.axe_cleave",
                18, 8, 16, 0.0, 6.0, 24.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.PLAYER_ATTACK_STRONG, 2.0f, 0.5f);
                        boss.performArcHitbox(6.2, 140.0, 24.0f, 1.3, ParticleTypes.CRIT);
                    }
                }
        ));

        // 2. Golden Arena Stomp (shockwave along the ground)
        this.moveset.add(new BossAttackMove(
                "arena_stomp", "animation.godfrey.stomp",
                14, 8, 16, 0.0, 14.0, 18.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 3) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.0f, 0.6f);
                        boss.performAoeHitbox(11.0, 18.0f, 1.2, ParticleTypes.EXPLOSION);
                    }
                }
        ));

        // 3. Phase 2: Hoarah Loux's Earthshaker (rushing roar + devastating twin seismic slam)
        this.moveset.add(new BossAttackMove(
                "hoarah_earthshaker", "animation.godfrey.earthshaker",
                18, 22, 22, 0.0, 16.0, 30.0f, true, 45,
                (boss, target) -> {
                    int t = boss.getStateTick();
                    if (t == 1 && target != null) {
                        Vec3 rush = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                        boss.setDeltaMovement(rush.x * 1.7, 0.2, rush.z * 1.7);
                        boss.playSound(SoundEvents.RAVAGER_ROAR, 2.2f, 0.6f);
                    } else if (t == 10) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.2f, 0.45f);
                        boss.performAoeHitbox(9.0, 26.0f, 1.6, ParticleTypes.CAMPFIRE_COSY_SMOKE);
                    } else if (t == 19) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 2.5f, 0.35f);
                        boss.performAoeHitbox(13.0, 30.0f, 2.0, ParticleTypes.EXPLOSION_EMITTER);
                    }
                }
        ));
    }

    @Override
    protected void onPhaseTwoStart() {
        super.onPhaseTwoStart();
        this.bossEvent.setName(Component.literal("Hoarah Loux, Warrior").withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        if (this.level() instanceof ServerLevel sl) {
            Component quote = Component.literal("Hoarah Loux: \"Now I fight as Hoarah Loux! WARRIOR!\"")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
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
