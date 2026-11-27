package com.eldenringcore.combat;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.boss.EldenBossEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/**
 * Hemorrhage (Xuất Huyết): after the bleed bar fills, health drains in pulses instead of all at once.
 * Every PULSE_TICKS it removes a share of max health that ignores armour, with dripping blood.
 * Total over the default duration: 15% max HP (8% for bosses).
 */
public class HemorrhageEffect extends MobEffect {
    public static final ResourceKey<DamageType> DAMAGE_TYPE =
            ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(EldenRingCore.MOD_ID, "hemorrhage"));
    public static final int PULSE_TICKS = 10;
    public static final int DURATION_TICKS = 60;          // 6 pulses over 3 seconds
    private static final float TOTAL_SHARE = 0.15f;
    private static final float TOTAL_SHARE_BOSS = 0.08f;
    static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.55f, 0.02f, 0.02f), 1.3f);

    public HemorrhageEffect() {
        super(MobEffectCategory.HARMFUL, 0x8A0303);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % PULSE_TICKS == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level) || !entity.isAlive()) return;
        float share = entity instanceof EldenBossEntity ? TOTAL_SHARE_BOSS : TOTAL_SHARE;
        int pulses = DURATION_TICKS / PULSE_TICKS;
        float damage = Math.max(0.5f, entity.getMaxHealth() * share / pulses) * (1 + amplifier);
        entity.hurt(source(entity), damage);
        level.sendParticles(BLOOD, entity.getX(), entity.getY() + entity.getBbHeight() * 0.6, entity.getZ(),
                8, entity.getBbWidth() * 0.35, entity.getBbHeight() * 0.25, entity.getBbWidth() * 0.35, 0.0);
    }

    static DamageSource source(LivingEntity entity) {
        return new DamageSource(entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE_TYPE));
    }
}
