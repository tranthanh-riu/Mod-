package com.eldenringcore.boss;

import com.eldenringcore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
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
 * Wave 3 Legend Boss: Fire Giant (Mountaintops of the Giants)
 * - Massive HP pool (1600), Colossal Plate Sweeps, Roll Crush, and Phase 2 Fell God Flame Pillars
 */
public class FireGiantEntity extends EldenBossEntity {

    public FireGiantEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level,
                Component.literal("Fire Giant").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                BossEvent.BossBarColor.RED);
        this.maxPoise = 220.0f;
        this.currentPoise = 220.0f;
        this.xpReward = 1300;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1600.0)
                .add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 26.0)
                .add(Attributes.ARMOR, 14.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 56.0);
    }

    @Override
    protected String getVictoryBannerText() {
        return "LEGEND FELLED";
    }

    @Override
    protected void initMoveset() {
        // 1. Giant Iron Plate Snow Sweep
        this.moveset.add(new BossAttackMove(
                "plate_sweep", "animation.fire_giant.plate_sweep",
                26, 10, 24, 0.0, 9.5, 26.0f, false, 35,
                (boss, target) -> {
                    if (boss.getStateTick() == 4) {
                        boss.playSound(SoundEvents.ANVIL_LAND, 2.0f, 0.4f);
                        boss.performArcHitbox(10.0, 150.0, 26.0f, 1.8, ParticleTypes.SNOWFLAKE);
                    }
                }
        ));

        // 2. Colossal Sideways Roll Crush
        this.moveset.add(new BossAttackMove(
                "giant_roll", "animation.fire_giant.roll",
                16, 16, 22, 0.0, 14.0, 22.0f, false, 25,
                (boss, target) -> {
                    if (boss.getStateTick() == 2 && target != null) {
                        Vec3 dir = target.position().subtract(boss.position()).multiply(1, 0, 1).normalize();
                        boss.setDeltaMovement(dir.x * 1.4, 0.1, dir.z * 1.4);
                    }
                    if (boss.getStateTick() == 8 || boss.getStateTick() == 13) {
                        boss.playSound(SoundEvents.GENERIC_EXPLODE, 1.8f, 0.5f);
                        boss.performAoeHitbox(7.5, 22.0f, 1.5, ParticleTypes.CAMPFIRE_COSY_SMOKE);
                    }
                }
        ));

        // 3. Phase 2: Fell God Flame Pillars
        this.moveset.add(new BossAttackMove(
                "flame_pillars", "animation.fire_giant.flame_pillars",
                22, 20, 26, 0.0, 20.0, 28.0f, true, 45,
                (boss, target) -> {
                    if (boss.getStateTick() == 6 || boss.getStateTick() == 15) {
                        boss.playSound(SoundEvents.BLAZE_SHOOT, 2.2f, 0.5f);
                        boss.performAoeHitbox(12.5, 28.0f, 1.4, ParticleTypes.FLAME);
                    }
                }
        ));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        for (int i = 0; i < 3; i++) this.spawnAtLocation(ModItems.SOMBER_SMITHING_STONE.get());
    }
}
