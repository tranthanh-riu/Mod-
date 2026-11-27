package com.eldenringcore.combat;

import com.eldenringcore.boss.EldenBossEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Base Elden Ring Weapon with built-in Status Build-up, Poise Damage, and Ash of War (Right-Click skill).
 */
public class EldenWeaponItem extends SwordItem {
    private final AshOfWarType ashOfWar;
    private final StatusEffectType statusType;
    private final float statusBuildup;
    private final float poiseDamage;

    public EldenWeaponItem(Tier tier, int attackDamageModifier, float attackSpeedModifier, Properties properties,
                           AshOfWarType ashOfWar, StatusEffectType statusType, float statusBuildup, float poiseDamage) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        this.ashOfWar = ashOfWar;
        this.statusType = statusType;
        this.statusBuildup = statusBuildup;
        this.poiseDamage = poiseDamage;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (this.statusType != StatusEffectType.NONE) {
            StatusBuildupManager.addBuildup(target, attacker, this.statusType, this.statusBuildup);
        }
        if (target instanceof EldenBossEntity boss) {
            boss.applyPoiseDamage(this.poiseDamage);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.ashOfWar == AshOfWarType.NONE || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            float fpCost = getAshFpCost(player);
            if (!FocusPointsManager.tryConsumeFp(player, fpCost)) {
                return InteractionResultHolder.fail(stack);
            }
            int cooldownTicks = executeAshOfWar(serverLevel, player);
            player.getCooldowns().addCooldown(this, cooldownTicks);
        }
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    protected float getAshFpCost(Player player) {
        if (this.ashOfWar == AshOfWarType.CORPSE_PILER) {
            int stage = AshOfWarComboManager.getActiveComboStage(player, AshOfWarType.CORPSE_PILER);
            return stage == 0 ? this.ashOfWar.getBaseFpCost() : 10.0f;
        }
        if (this.ashOfWar == AshOfWarType.TRANSIENT_MOONLIGHT && player.isShiftKeyDown()) {
            return 20.0f;
        }
        return this.ashOfWar.getBaseFpCost();
    }

    private int getAshCooldownTicks() {
        return switch (this.ashOfWar) {
            case CORPSE_PILER -> 45;
            case TRANSIENT_MOONLIGHT -> 40;
            case MOONLIGHT_WAVE -> 60;
            case WATERFOWL_DANCE -> 80;
            case TAKERS_FLAMES -> 55;
            case STARCALLER_CRY -> 70;
            case DESTINED_DEATH -> 75;
            case BLOODHOUNDS_FINESSE -> 40;
            case GOLDEN_VOW -> 120;
            case ANCIENT_LIGHTNING_SPEAR -> 55;
            default -> 20;
        };
    }

    private int executeAshOfWar(ServerLevel level, Player player) {
        Vec3 forward = player.getLookAngle().normalize();
        switch (this.ashOfWar) {
            case CORPSE_PILER -> {
                return executeCorpsePiler(level, player);
            }
            case TRANSIENT_MOONLIGHT -> {
                return executeTransientMoonlight(level, player, forward);
            }
            case MOONLIGHT_WAVE -> {
                player.playSound(SoundEvents.GLASS_BREAK, 1.4f, 0.9f);
                performLineSlash(level, player, forward, 10.0, 15.0f, ParticleTypes.SNOWFLAKE, StatusEffectType.FROSTBITE, 55.0f, 40.0f);
            }
            case WATERFOWL_DANCE -> {
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.8f, 1.4f);
                player.setDeltaMovement(forward.x * 1.1, 0.35, forward.z * 1.1);
                player.hurtMarked = true;
                AABB box = player.getBoundingBox().inflate(4.5, 2.0, 4.5);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, e -> e != player && e.isAlive())) {
                    target.hurt(player.damageSources().playerAttack(player), 16.0f);
                    StatusBuildupManager.addBuildup(target, player, StatusEffectType.BLEED, 40.0f);
                    if (target instanceof EldenBossEntity boss) {
                        boss.applyPoiseDamage(30.0f);
                    }
                }
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.1, player.getZ(), 16, 2.5, 0.6, 2.5, 0.1);
            }
            case TAKERS_FLAMES -> {
                executeTakersFlames(level, player, forward);
            }
            case STARCALLER_CRY -> {
                executeStarcallerCry(level, player);
            }
            case DESTINED_DEATH -> {
                executeDestinedDeath(level, player, forward);
            }
            case BLOODHOUNDS_FINESSE -> {
                executeBloodhoundsFinesse(level, player, forward);
            }
            case GOLDEN_VOW -> {
                executeGoldenVow(level, player);
            }
            case ANCIENT_LIGHTNING_SPEAR -> {
                executeAncientLightningSpear(level, player, forward);
            }
            default -> {
            }
        }
        return getAshCooldownTicks();
    }

    /**
     * Corpse Piler (Rivers of Blood): 3-stage blood-blade slash combo.
     * Stage 0: Quick double blood slash (opens 1.6s combo window).
     * Stage 1: Extended double cross blood slash (opens 1.6s combo window).
     * Stage 2: Heavy wide-arc blood finisher (resets combo and applies full cooldown).
     */
    private int executeCorpsePiler(ServerLevel level, Player player) {
        int stage = AshOfWarComboManager.getActiveComboStage(player, AshOfWarType.CORPSE_PILER);
        Vec3 forward = player.getLookAngle().normalize();

        if (stage == 0) {
            lungeForward(player, forward, 0.25);
            performBloodArcSlash(level, player, forward, 6.5, 1.4, 7.5f, 28.0f, 12.0f, 0.75f);
            AshOfWarComboManager.scheduleDelayedAction(level, 4, () -> {
                if (player.isAlive() && player.level() == level) {
                    Vec3 followDir = player.getLookAngle().normalize();
                    performBloodArcSlash(level, player, followDir, 6.5, 1.5, 7.5f, 28.0f, 12.0f, 0.90f);
                }
            });
            AshOfWarComboManager.setComboStage(player, AshOfWarType.CORPSE_PILER, 1, 32);
            return 12;
        } else if (stage == 1) {
            lungeForward(player, forward, 0.30);
            performBloodArcSlash(level, player, forward, 7.5, 1.7, 9.0f, 34.0f, 15.0f, 0.85f);
            AshOfWarComboManager.scheduleDelayedAction(level, 4, () -> {
                if (player.isAlive() && player.level() == level) {
                    Vec3 followDir = player.getLookAngle().normalize();
                    performBloodArcSlash(level, player, followDir, 7.5, 1.8, 9.0f, 34.0f, 15.0f, 1.05f);
                }
            });
            AshOfWarComboManager.setComboStage(player, AshOfWarType.CORPSE_PILER, 2, 32);
            return 12;
        } else {
            lungeForward(player, forward, 0.42);
            performBloodArcSlash(level, player, forward, 8.5, 2.2, 14.0f, 50.0f, 28.0f, 0.60f);
            level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                    player.getX() + forward.x * 3.0, player.getY() + 1.2, player.getZ() + forward.z * 3.0,
                    12, 1.2, 0.4, 1.2, 0.08);
            AshOfWarComboManager.resetCombo(player);
            return getAshCooldownTicks();
        }
    }

    private void lungeForward(Player player, Vec3 forward, double strength) {
        Vec3 horizontal = new Vec3(forward.x, 0.0, forward.z);
        if (horizontal.lengthSqr() > 1.0E-4) {
            Vec3 dash = horizontal.normalize().scale(strength);
            player.setDeltaMovement(player.getDeltaMovement().add(dash.x, 0.05, dash.z));
            player.hurtMarked = true;
        }
    }

    private void performBloodArcSlash(ServerLevel level, Player player, Vec3 forward, double range, double halfWidth,
                                      float damage, float bleedBuildup, float poise, float pitch) {
        player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5f, pitch);
        Vec3 start = player.getEyePosition();
        Vec3 horizontalForward = new Vec3(forward.x, 0.0, forward.z);
        Vec3 right = horizontalForward.lengthSqr() > 1.0E-4
                ? horizontalForward.normalize().cross(new Vec3(0.0, 1.0, 0.0)).normalize()
                : new Vec3(1.0, 0.0, 0.0);

        Set<UUID> hitEntities = new HashSet<>();
        for (double d = 1.2; d <= range; d += 0.85) {
            Vec3 center = start.add(forward.scale(d));
            for (double side = -halfWidth; side <= halfWidth; side += 0.75) {
                Vec3 p = center.add(right.scale(side));
                level.sendParticles(ParticleTypes.CRIMSON_SPORE, p.x, p.y - 0.35, p.z, 3, 0.15, 0.12, 0.15, 0.02);
            }
            if (d >= range - 1.5) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y - 0.3, center.z, 2, 0.6, 0.2, 0.6, 0.0);
            }
            AABB hitBox = new AABB(
                    center.x - halfWidth, center.y - 1.2, center.z - halfWidth,
                    center.x + halfWidth, center.y + 1.2, center.z + halfWidth);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())) {
                if (hitEntities.add(target.getUUID())) {
                    target.invulnerableTime = 0;
                    target.hurt(player.damageSources().playerAttack(player), damage);
                    StatusBuildupManager.addBuildup(target, player, StatusEffectType.BLEED, bleedBuildup);
                    if (target instanceof EldenBossEntity boss) {
                        boss.applyPoiseDamage(poise);
                    }
                }
            }
        }
    }

    /**
     * Transient Moonlight (Moonveil):
     * - Shift + Right-Click: Long-range heavy vertical moonlight beam (high damage & poise break).
     * - Normal Right-Click: Wide horizontal moonlight slash wave (crowd control).
     */
    private int executeTransientMoonlight(ServerLevel level, Player player, Vec3 forward) {
        Vec3 start = player.getEyePosition();
        Set<UUID> hitEntities = new HashSet<>();

        if (player.isShiftKeyDown()) {
            // Heavy Vertical Wave (long range, tall narrow beam, heavy stance damage)
            player.playSound(SoundEvents.TRIDENT_RIPTIDE_3, 1.6f, 1.15f);
            for (double d = 1.2; d <= 13.0; d += 0.75) {
                Vec3 center = start.add(forward.scale(d));
                for (double yOff = -0.8; yOff <= 1.4; yOff += 0.55) {
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + yOff, center.z, 2, 0.08, 0.08, 0.08, 0.01);
                }
                level.sendParticles(ParticleTypes.END_ROD, center.x, center.y + 0.2, center.z, 1, 0.05, 0.35, 0.05, 0.01);

                AABB hitBox = new AABB(
                        center.x - 0.85, center.y - 1.6, center.z - 0.85,
                        center.x + 0.85, center.y + 1.8, center.z + 0.85);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())) {
                    if (hitEntities.add(target.getUUID())) {
                        target.hurt(player.damageSources().playerAttack(player), 17.0f);
                        StatusBuildupManager.addBuildup(target, player, StatusEffectType.BLEED, 30.0f);
                        if (target instanceof EldenBossEntity boss) {
                            boss.applyPoiseDamage(48.0f);
                        }
                    }
                }
            }
            return 45;
        } else {
            // Light Horizontal Wave (wide crescent arc)
            player.playSound(SoundEvents.TRIDENT_RIPTIDE_2, 1.4f, 1.45f);
            Vec3 horizontalForward = new Vec3(forward.x, 0.0, forward.z);
            Vec3 right = horizontalForward.lengthSqr() > 1.0E-4
                    ? horizontalForward.normalize().cross(new Vec3(0.0, 1.0, 0.0)).normalize()
                    : new Vec3(1.0, 0.0, 0.0);

            for (double d = 1.2; d <= 8.5; d += 0.8) {
                Vec3 center = start.add(forward.scale(d));
                for (double side = -2.4; side <= 2.4; side += 0.6) {
                    Vec3 p = center.add(right.scale(side));
                    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y - 0.25, p.z, 2, 0.1, 0.08, 0.1, 0.01);
                }
                if (d >= 7.0) {
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y - 0.25, center.z, 3, 1.2, 0.15, 1.2, 0.0);
                }

                AABB hitBox = new AABB(
                        center.x - 2.6, center.y - 1.0, center.z - 2.6,
                        center.x + 2.6, center.y + 1.0, center.z + 2.6);
                for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())) {
                    if (hitEntities.add(target.getUUID())) {
                        target.hurt(player.damageSources().playerAttack(player), 12.0f);
                        StatusBuildupManager.addBuildup(target, player, StatusEffectType.BLEED, 22.0f);
                        if (target instanceof EldenBossEntity boss) {
                            boss.applyPoiseDamage(28.0f);
                        }
                    }
                }
            }
            return 32;
        }
    }

    /**
     * Taker's Flames (Blasphemous Blade): Blazing flame trail + lifesteal on hit.
     */
    private void executeTakersFlames(ServerLevel level, Player player, Vec3 forward) {
        player.playSound(SoundEvents.BLAZE_SHOOT, 1.5f, 0.75f);
        Vec3 horizontalForward = new Vec3(forward.x, 0.0, forward.z);
        Vec3 dir = horizontalForward.lengthSqr() > 1.0E-4 ? horizontalForward.normalize() : new Vec3(0.0, 0.0, 1.0);
        Vec3 start = player.position().add(0.0, 0.3, 0.0);

        Set<UUID> hitEntities = new HashSet<>();
        float totalHeal = 0.0f;
        for (double d = 1.2; d <= 12.0; d += 0.85) {
            Vec3 pos = start.add(dir.scale(d));
            level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.3, pos.z, 8, 0.45, 0.35, 0.45, 0.04);
            level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 0.2, pos.z, 2, 0.3, 0.2, 0.3, 0.02);

            AABB hitBox = new AABB(pos.x - 1.6, pos.y - 0.8, pos.z - 1.6, pos.x + 1.6, pos.y + 2.2, pos.z + 1.6);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())) {
                if (hitEntities.add(target.getUUID())) {
                    target.hurt(player.damageSources().playerAttack(player), 15.0f);
                    target.setSecondsOnFire(4);
                    if (target instanceof EldenBossEntity boss) {
                        boss.applyPoiseDamage(32.0f);
                    }
                    totalHeal = Math.min(10.0f, totalHeal + 2.5f);
                }
            }
        }
        if (totalHeal > 0.0f) {
            player.heal(totalHeal);
            level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 1.8, player.getZ(), 5, 0.4, 0.3, 0.4, 0.02);
        }
    }

    /**
     * Starcaller Cry (Starscourge Greatsword): Pulls nearby enemies in with gravity,
     * followed by a heavy gravity slam explosion after 14 ticks.
     */
    private void executeStarcallerCry(ServerLevel level, Player player) {
        player.playSound(SoundEvents.ENDERMAN_SCREAM, 1.5f, 0.55f);
        level.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 45, 3.0, 1.0, 3.0, 0.2);

        AABB pullBox = player.getBoundingBox().inflate(9.0, 3.0, 9.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, pullBox, e -> e != player && e.isAlive())) {
            Vec3 pullVec = player.position().subtract(target.position());
            double dist = pullVec.length();
            if (dist > 1.5) {
                Vec3 velocity = pullVec.normalize().scale(Math.min(1.4, dist * 0.22));
                target.setDeltaMovement(velocity.x, 0.28, velocity.z);
                target.hurtMarked = true;
            }
            target.hurt(player.damageSources().playerAttack(player), 6.0f);
            if (target instanceof EldenBossEntity boss) {
                boss.applyPoiseDamage(18.0f);
            }
        }

        AshOfWarComboManager.scheduleDelayedAction(level, 14, () -> {
            if (!player.isAlive() || player.level() != level) {
                return;
            }
            player.playSound(SoundEvents.GENERIC_EXPLODE, 1.6f, 0.7f);
            level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 0.8, player.getZ(), 2, 1.0, 0.5, 1.0, 0.0);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 0.6, player.getZ(), 35, 3.2, 0.6, 3.2, 0.06);

            AABB slamBox = player.getBoundingBox().inflate(6.0, 2.5, 6.0);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, slamBox, e -> e != player && e.isAlive())) {
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().playerAttack(player), 18.0f);
                Vec3 push = target.position().subtract(player.position()).normalize();
                target.setDeltaMovement(push.x * 0.6, 0.45, push.z * 0.6);
                target.hurtMarked = true;
                if (target instanceof EldenBossEntity boss) {
                    boss.applyPoiseDamage(45.0f);
                }
            }
        });
    }

    /**
     * Destined Death (Maliketh's Black Blade): 3-tick swirling dome of crimson-black slashes
     * dealing % max HP burst damage and Wither.
     */
    private void executeDestinedDeath(ServerLevel level, Player player, Vec3 forward) {
        Vec3 center = player.position().add(forward.x * 3.2, 1.0, forward.z * 3.2);
        Runnable pulse = () -> {
            if (!player.isAlive() || player.level() != level) {
                return;
            }
            player.playSound(SoundEvents.WITHER_SHOOT, 1.2f, 0.65f);
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, center.x, center.y, center.z, 30, 2.5, 1.4, 2.5, 0.05);
            level.sendParticles(ParticleTypes.SQUID_INK, center.x, center.y, center.z, 20, 2.2, 1.2, 2.2, 0.06);
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 6, 2.0, 0.8, 2.0, 0.0);

            AABB dome = new AABB(center.x - 4.2, center.y - 2.0, center.z - 4.2, center.x + 4.2, center.y + 2.5, center.z + 4.2);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, dome, e -> e != player && e.isAlive())) {
                float pctDamage = Math.min(18.0f, target.getMaxHealth() * 0.03f);
                target.invulnerableTime = 0;
                target.hurt(player.damageSources().playerAttack(player), 7.0f + pctDamage);
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                if (target instanceof EldenBossEntity boss) {
                    boss.applyPoiseDamage(16.0f);
                }
            }
        };

        pulse.run();
        AshOfWarComboManager.scheduleDelayedAction(level, 5, pulse);
        AshOfWarComboManager.scheduleDelayedAction(level, 10, pulse);
    }

    /**
     * Bloodhound's Finesse (Bloodhound's Fang): Heavy bleed slash followed by an evasive backflip.
     */
    private void executeBloodhoundsFinesse(ServerLevel level, Player player, Vec3 forward) {
        performBloodArcSlash(level, player, forward, 5.2, 1.8, 14.0f, 35.0f, 32.0f, 0.8f);
        Vec3 horizontalForward = new Vec3(forward.x, 0.0, forward.z);
        if (horizontalForward.lengthSqr() > 1.0E-4) {
            Vec3 back = horizontalForward.normalize().scale(-1.15);
            player.setDeltaMovement(back.x, 0.42, back.z);
            player.hurtMarked = true;
        }
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 12, 2, false, false));
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 12, 0.4, 0.2, 0.4, 0.06);
    }

    /**
     * Golden Vow (Golden Halberd): Radiant golden aura buffing Attack Damage and Defense for 20s.
     */
    private void executeGoldenVow(ServerLevel level, Player player) {
        player.playSound(SoundEvents.BEACON_ACTIVATE, 1.4f, 1.15f);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 32, 2.0, 0.8, 2.0, 0.06);
        level.sendParticles(ParticleTypes.WAX_ON, player.getX(), player.getY() + 1.2, player.getZ(), 24, 1.8, 0.8, 1.8, 0.05);

        AABB auraBox = player.getBoundingBox().inflate(8.0, 4.0, 8.0);
        for (Player ally : level.getEntitiesOfClass(Player.class, auraBox, Player::isAlive)) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 0));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 0));
        }
    }

    /**
     * Ancient Lightning Spear (Bolt of Gransax): Long-range piercing red lightning bolt.
     */
    private void executeAncientLightningSpear(ServerLevel level, Player player, Vec3 forward) {
        player.playSound(SoundEvents.TRIDENT_THROW, 1.6f, 0.8f);
        player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 0.8f, 1.4f);

        Vec3 start = player.getEyePosition();
        Set<UUID> hitEntities = new HashSet<>();
        boolean spawnedBolt = false;

        for (double d = 1.5; d <= 22.0; d += 0.75) {
            Vec3 pos = start.add(forward.scale(d));
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y - 0.1, pos.z, 4, 0.15, 0.15, 0.15, 0.03);
            level.sendParticles(ParticleTypes.CRIMSON_SPORE, pos.x, pos.y - 0.1, pos.z, 3, 0.12, 0.12, 0.12, 0.02);

            AABB hitBox = new AABB(pos.x - 1.0, pos.y - 1.0, pos.z - 1.0, pos.x + 1.0, pos.y + 1.0, pos.z + 1.0);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())) {
                if (hitEntities.add(target.getUUID())) {
                    target.hurt(player.damageSources().lightningBolt(), 19.0f);
                    if (target instanceof EldenBossEntity boss) {
                        boss.applyPoiseDamage(40.0f);
                    }
                    if (!spawnedBolt) {
                        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                        if (bolt != null) {
                            bolt.moveTo(target.getX(), target.getY(), target.getZ());
                            bolt.setVisualOnly(true);
                            level.addFreshEntity(bolt);
                        }
                        spawnedBolt = true;
                    }
                }
            }
        }
    }

    private void performLineSlash(ServerLevel level, Player player, Vec3 forward, double range, float damage,
                                  ParticleOptions particle, StatusEffectType status, float buildup, float poise) {
        Vec3 start = player.getEyePosition();
        Set<UUID> hitEntities = new HashSet<>();
        for (double d = 1.0; d <= range; d += 0.8) {
            Vec3 pos = start.add(forward.scale(d));
            level.sendParticles(particle, pos.x, pos.y - 0.3, pos.z, 4, 0.25, 0.2, 0.25, 0.02);
            AABB hitBox = new AABB(pos.x - 1.1, pos.y - 1.2, pos.z - 1.1, pos.x + 1.1, pos.y + 1.2, pos.z + 1.1);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())) {
                if (hitEntities.add(target.getUUID())) {
                    target.hurt(player.damageSources().playerAttack(player), damage);
                    StatusBuildupManager.addBuildup(target, player, status, buildup);
                    if (target instanceof EldenBossEntity boss) {
                        boss.applyPoiseDamage(poise);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (this.ashOfWar != AshOfWarType.NONE) {
            tooltip.add(Component.literal("Ash of War: " + this.ashOfWar.name().replace('_', ' '))
                    .withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.literal("FP Cost: " + (int) this.ashOfWar.getBaseFpCost())
                    .withStyle(ChatFormatting.AQUA));
            if (this.ashOfWar == AshOfWarType.CORPSE_PILER) {
                tooltip.add(Component.literal("Right-Click repeatedly: 3-stage blood slash combo")
                        .withStyle(ChatFormatting.DARK_RED));
            } else if (this.ashOfWar == AshOfWarType.TRANSIENT_MOONLIGHT) {
                tooltip.add(Component.literal("Right-Click: Wide horizontal wave (15 FP)")
                        .withStyle(ChatFormatting.BLUE));
                tooltip.add(Component.literal("Shift + Right-Click: Long-range vertical wave (20 FP)")
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        if (this.statusType != StatusEffectType.NONE) {
            tooltip.add(Component.literal("Status Build-up: " + this.statusType.name() + " (" + (int) this.statusBuildup + ")")
                    .withStyle(ChatFormatting.RED));
        }
        tooltip.add(Component.literal("Stance Damage: " + (int) this.poiseDamage)
                .withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
