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
        return this.ashOfWar.getBaseFpCost();
    }

    private int getAshCooldownTicks() {
        return switch (this.ashOfWar) {
            case CORPSE_PILER -> 45;
            case TRANSIENT_MOONLIGHT -> 40;
            case MOONLIGHT_WAVE -> 60;
            case WATERFOWL_DANCE -> 80;
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
                player.playSound(SoundEvents.TRIDENT_RIPTIDE_2, 1.4f, 1.4f);
                performLineSlash(level, player, forward, 9.0, 13.0f, ParticleTypes.SOUL_FIRE_FLAME, StatusEffectType.BLEED, 25.0f, 35.0f);
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
