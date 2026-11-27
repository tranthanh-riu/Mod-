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

import java.util.List;

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
            executeAshOfWar(serverLevel, player);
            player.getCooldowns().addCooldown(this, getAshCooldownTicks());
        }
        player.swing(hand);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    protected float getAshFpCost(Player player) {
        return this.ashOfWar.getBaseFpCost();
    }

    private int getAshCooldownTicks() {
        return switch (this.ashOfWar) {
            case CORPSE_PILER -> 50;
            case TRANSIENT_MOONLIGHT -> 40;
            case MOONLIGHT_WAVE -> 60;
            case WATERFOWL_DANCE -> 80;
            default -> 20;
        };
    }

    private void executeAshOfWar(ServerLevel level, Player player) {
        Vec3 forward = player.getLookAngle().normalize();
        switch (this.ashOfWar) {
            case CORPSE_PILER -> {
                player.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.5f, 0.7f);
                performLineSlash(level, player, forward, 6.0, 10.0f, ParticleTypes.CRIMSON_SPORE, StatusEffectType.BLEED, 45.0f, 20.0f);
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
    }

    private void performLineSlash(ServerLevel level, Player player, Vec3 forward, double range, float damage,
                                  ParticleOptions particle, StatusEffectType status, float buildup, float poise) {
        Vec3 start = player.getEyePosition();
        for (double d = 1.0; d <= range; d += 0.8) {
            Vec3 pos = start.add(forward.scale(d));
            level.sendParticles(particle, pos.x, pos.y - 0.3, pos.z, 4, 0.25, 0.2, 0.25, 0.02);
            AABB hitBox = new AABB(pos.x - 1.1, pos.y - 1.2, pos.z - 1.1, pos.x + 1.1, pos.y + 1.2, pos.z + 1.1);
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, hitBox, e -> e != player && e.isAlive())) {
                target.hurt(player.damageSources().playerAttack(player), damage);
                StatusBuildupManager.addBuildup(target, player, status, buildup);
                if (target instanceof EldenBossEntity boss) {
                    boss.applyPoiseDamage(poise);
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
