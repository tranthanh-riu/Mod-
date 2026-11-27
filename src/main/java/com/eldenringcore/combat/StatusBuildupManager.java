package com.eldenringcore.combat;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Elden Ring Status Build-up system:
 * Hits accumulate hidden build-up toward a threshold (default 100).
 * Once full, triggers Blood Loss (% max HP burst), Frostbite (burst + slow/fragility), or Scarlet Rot.
 */
public class StatusBuildupManager {
    private static final float PROC_THRESHOLD = 100.0f;
    private static final float DECAY_PER_SECOND = 4.0f;

    private static final Map<UUID, Map<StatusEffectType, Float>> BUILDUP = new ConcurrentHashMap<>();

    public static void addBuildup(LivingEntity target, LivingEntity attacker, StatusEffectType type, float amount) {
        if (type == StatusEffectType.NONE || amount <= 0 || !target.isAlive()) {
            return;
        }
        Map<StatusEffectType, Float> map = BUILDUP.computeIfAbsent(target.getUUID(), id -> new EnumMap<>(StatusEffectType.class));
        float next = map.getOrDefault(type, 0.0f) + amount;

        if (next >= PROC_THRESHOLD) {
            map.put(type, 0.0f);
            triggerProc(target, attacker, type);
        } else {
            map.put(type, next);
        }
    }

    private static void triggerProc(LivingEntity target, LivingEntity attacker, StatusEffectType type) {
        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        switch (type) {
            case BLEED -> {
                float burst = Math.max(8.0f, target.getMaxHealth() * 0.12f);
                target.hurt(attacker != null ? target.damageSources().mobAttack(attacker) : target.damageSources().magic(), burst);
                serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY() + 1.2, target.getZ(), 20, 0.5, 0.5, 0.5, 0.1);
                target.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.8f, 0.6f);
            }
            case FROSTBITE -> {
                float burst = Math.max(6.0f, target.getMaxHealth() * 0.09f);
                target.hurt(attacker != null ? target.damageSources().mobAttack(attacker) : target.damageSources().freeze(), burst);
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 140, 0));
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + 1.2, target.getZ(), 25, 0.6, 0.6, 0.6, 0.05);
                target.playSound(SoundEvents.GLASS_BREAK, 1.5f, 1.2f);
            }
            case SCARLET_ROT -> {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, 180, 1));
                serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getY() + 1.2, target.getZ(), 25, 0.6, 0.6, 0.6, 0.05);
                target.playSound(SoundEvents.SPIDER_HURT, 1.4f, 0.5f);
            }
            default -> {
            }
        }
    }

    @SubscribeEvent
    public void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide() || entity.tickCount % 20 != 0) {
            return;
        }
        Map<StatusEffectType, Float> map = BUILDUP.get(entity.getUUID());
        if (map == null || map.isEmpty()) {
            return;
        }
        map.replaceAll((k, v) -> Math.max(0.0f, v - DECAY_PER_SECOND));
        map.entrySet().removeIf(e -> e.getValue() <= 0.0f);
        if (map.isEmpty()) {
            BUILDUP.remove(entity.getUUID());
        }
    }
}
