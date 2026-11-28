package com.eldenringcore.nativecombat;

import com.eldenringcore.boss.EldenBossEntity;
import com.eldenringcore.combat.FocusPointsManager;
import com.eldenringcore.combat.StatusBuildupManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-authoritative Native Elden Ring Combat Engine (ZERO Epic Fight dependency):
 * - Stamina pool + smooth regeneration
 * - 3-hit directional Katana combo with real 3D arc hitboxes & Bleed buildup
 * - Heavy Thrust / Jump Strike with Poise break
 * - Ash of War: Unsheathe (FP + Stamina, luminous crescent wave)
 * - 8-way Dodge Roll with authentic i-frames (invulnerability window)
 */
public class NativeCombatManager {
    private static final float MAX_STAMINA = 100.0f;
    private static final float STAMINA_REGEN_PER_TICK = 1.65f;
    private static final int REGEN_DELAY_TICKS = 14;
    private static final int COMBO_WINDOW_TICKS = 26;
    private static final int ROLL_IFRAME_TICKS = 10; // 0.5s invulnerability
    private static final int ROLL_COOLDOWN_TICKS = 16;
    private static final float ROLL_STAMINA_COST = 20.0f;

    private static class PlayerCombatState {
        float stamina = MAX_STAMINA;
        int regenDelay = 0;
        int actionCooldown = 0;
        int rollCooldown = 0;
        int iframeTicks = 0;
        int comboStep = 0;
        int comboResetTimer = 0;
        int lastSyncedInt = 100;
    }

    private static final Map<UUID, PlayerCombatState> STATES = new ConcurrentHashMap<>();

    private static PlayerCombatState state(ServerPlayer p) {
        return STATES.computeIfAbsent(p.getUUID(), k -> new PlayerCombatState());
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        STATES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PlayerCombatState st = STATES.get(player.getUUID());
        if (st != null && st.iframeTicks > 0 && !event.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            // Authentic Dodge Roll i-frame dodge!
            event.setCanceled(true);
            ServerLevel level = player.serverLevel();
            level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 6, 0.3, 0.3, 0.3, 0.1);
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.55f, 1.8f);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        PlayerCombatState st = state(player);
        if (st.actionCooldown > 0) st.actionCooldown--;
        if (st.rollCooldown > 0) st.rollCooldown--;
        if (st.iframeTicks > 0) st.iframeTicks--;

        if (st.comboResetTimer > 0) {
            st.comboResetTimer--;
            if (st.comboResetTimer == 0) {
                st.comboStep = 0;
                NativeCombatNetworking.syncStamina(player, st.stamina, MAX_STAMINA, 0);
            }
        }

        if (st.regenDelay > 0) {
            st.regenDelay--;
        } else if (st.stamina < MAX_STAMINA) {
            st.stamina = Math.min(MAX_STAMINA, st.stamina + STAMINA_REGEN_PER_TICK);
        }

        int rounded = Math.round(st.stamina);
        if (rounded != st.lastSyncedInt || player.tickCount % 20 == 0) {
            st.lastSyncedInt = rounded;
            NativeCombatNetworking.syncStamina(player, st.stamina, MAX_STAMINA, st.comboStep);
        }
    }

    public static void handleAction(ServerPlayer player, NativeKatanaItem.C2SAction action, float moveX, float moveZ) {
        if (player.isSpectator() || !player.isAlive()) return;
        PlayerCombatState st = state(player);

        if (action == NativeKatanaItem.C2SAction.DODGE_ROLL) {
            performDodgeRoll(player, st, moveX, moveZ);
            return;
        }

        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof NativeKatanaItem katana)) return;
        if (st.actionCooldown > 0) return;

        EldenWeaponStyle style = katana.getStyle();
        switch (action) {
            case LIGHT_COMBO -> performLightCombo(player, st, katana, style);
            case HEAVY_ATTACK -> performHeavyThrust(player, st, katana, style);
            case UNSHEATHE -> performUnsheathe(player, st, katana, style);
            default -> {}
        }
    }

    private static boolean consumeStamina(ServerPlayer player, PlayerCombatState st, float cost) {
        if (player.isCreative()) return true;
        if (st.stamina < cost * 0.4f) {
            player.displayClientMessage(Component.literal("Hết Stamina!").withStyle(ChatFormatting.YELLOW), true);
            return false;
        }
        st.stamina = Math.max(0.0f, st.stamina - cost);
        st.regenDelay = REGEN_DELAY_TICKS;
        NativeCombatNetworking.syncStamina(player, st.stamina, MAX_STAMINA, st.comboStep);
        return true;
    }

    private static void performLightCombo(ServerPlayer player, PlayerCombatState st, NativeKatanaItem katana, EldenWeaponStyle style) {
        if (!consumeStamina(player, st, style.lightStamina)) return;

        int step = st.comboStep % style.comboAnims.length;
        String anim = style.comboAnims[step];
        boolean isFinisher = (step == style.comboAnims.length - 1);

        st.comboStep = (step + 1) % style.comboAnims.length;
        st.comboResetTimer = COMBO_WINDOW_TICKS;
        st.actionCooldown = isFinisher ? style.lightCooldownTicks + 4 : style.lightCooldownTicks;

        NativeCombatNetworking.broadcastAnimation(player, anim, 1.0f);
        NativeCombatNetworking.syncStamina(player, st.stamina, MAX_STAMINA, step + 1);

        // Slight forward combat step
        Vec3 look = flatLook(player);
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(isFinisher ? 0.34 : 0.16)));
        player.hurtMarked = true;

        float baseDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float dmgMult = isFinisher ? 1.38f : (step == 1 ? 1.08f : 1.0f);
        float buildup = isFinisher ? katana.getStatusBuildup() * 1.45f : katana.getStatusBuildup();

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                0.9f, isFinisher ? 0.85f : (1.15f + step * 0.1f));

        spawnSlashArcParticles(level, player, style.reach * 0.72, isFinisher ? 140 : 105, ParticleTypes.SWEEP_ATTACK, 3);
        performArcHitbox(player, level, style.reach, style.arcDegrees, baseDmg * dmgMult,
                katana, buildup, isFinisher ? 0.55f : 0.25f, false);
    }

    private static void performHeavyThrust(ServerPlayer player, PlayerCombatState st, NativeKatanaItem katana, EldenWeaponStyle style) {
        if (!consumeStamina(player, st, style.heavyStamina)) return;

        st.comboStep = 0;
        st.comboResetTimer = 0;
        st.actionCooldown = style.heavyCooldownTicks;

        NativeCombatNetworking.broadcastAnimation(player, style.heavyAnim, 1.0f);

        Vec3 look = flatLook(player);
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(0.55)));
        player.hurtMarked = true;

        float baseDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS, 1.0f, 0.9f);
        level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THROW, SoundSource.PLAYERS, 0.65f, 1.35f);

        // Piercing thrust trail
        Vec3 eye = player.getEyePosition().subtract(0, 0.25, 0);
        for (double d = 1.0; d <= style.reach + 0.7; d += 0.45) {
            Vec3 p = eye.add(look.scale(d));
            level.sendParticles(ParticleTypes.CRIT, p.x, p.y, p.z, 2, 0.08, 0.08, 0.08, 0.02);
        }

        performArcHitbox(player, level, style.reach + 0.7, 65.0, baseDmg * 1.6f,
                katana, katana.getStatusBuildup() * 1.5f, 0.75f, true);
    }

    private static void performUnsheathe(ServerPlayer player, PlayerCombatState st, NativeKatanaItem katana, EldenWeaponStyle style) {
        if (!FocusPointsManager.tryConsumeFp(player, style.ashFpCost)) {
            return;
        }
        if (!consumeStamina(player, st, style.ashStamina)) return;

        st.comboStep = 0;
        st.comboResetTimer = 0;
        st.actionCooldown = 18;

        NativeCombatNetworking.broadcastAnimation(player, style.ashOfWarAnim, 1.0f);

        Vec3 look = flatLook(player);
        player.setDeltaMovement(player.getDeltaMovement().add(look.scale(0.75)));
        player.hurtMarked = true;

        float baseDmg = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.45f, 1.9f);
        level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.72f);

        // Iconic luminous Unsheathe crescent wave!
        spawnSlashArcParticles(level, player, style.reach + 0.8, 150, ParticleTypes.SOUL_FIRE_FLAME, 18);
        spawnSlashArcParticles(level, player, style.reach * 0.8, 140, ParticleTypes.SWEEP_ATTACK, 5);

        performArcHitbox(player, level, style.reach + 1.0, 150.0, baseDmg * 2.15f,
                katana, katana.getStatusBuildup() * 1.85f, 0.9f, true);
    }

    private static void performDodgeRoll(ServerPlayer player, PlayerCombatState st, float moveX, float moveZ) {
        if (st.rollCooldown > 0 || !player.onGround()) return;
        if (!consumeStamina(player, st, ROLL_STAMINA_COST)) return;

        st.rollCooldown = ROLL_COOLDOWN_TICKS;
        st.iframeTicks = ROLL_IFRAME_TICKS;
        st.comboStep = 0;

        // Compute 8-way roll direction from player yaw + WASD input
        float yawRad = (float) Math.toRadians(player.getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad));
        Vec3 right = new Vec3(-Math.cos(yawRad), 0, -Math.sin(yawRad));
        Vec3 dir;
        if (Math.abs(moveX) > 0.01f || Math.abs(moveZ) > 0.01f) {
            dir = forward.scale(moveZ).add(right.scale(-moveX)).normalize();
        } else {
            dir = forward;
        }

        player.setDeltaMovement(dir.x * 0.88, 0.16, dir.z * 0.88);
        player.hurtMarked = true;

        NativeCombatNetworking.broadcastAnimation(player, "dodge_roll_forward", 1.0f);

        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 0.8f, 1.25f);
        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.15, player.getZ(), 8, 0.25, 0.05, 0.25, 0.04);
    }

    private static void performArcHitbox(ServerPlayer player, ServerLevel level, double reach, double arcDeg,
                                         float damage, NativeKatanaItem katana, float statusBuildup,
                                         float knockback, boolean heavyPoise) {
        Vec3 origin = player.position().add(0, 1.0, 0);
        Vec3 look = flatLook(player);
        double minDot = Math.cos(Math.toRadians(arcDeg * 0.5));

        AABB box = player.getBoundingBox().inflate(reach + 1.0, 2.0, reach + 1.0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !e.isAlliedTo(player));

        boolean hitAny = false;
        for (LivingEntity target : targets) {
            Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(origin);
            double dist = toTarget.length();
            if (dist > reach + target.getBbWidth() * 0.5) continue;

            Vec3 flatDir = new Vec3(toTarget.x, 0, toTarget.z);
            if (flatDir.lengthSqr() > 1e-4 && flatDir.normalize().dot(look) < minDot) continue;

            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                hitAny = true;
                StatusBuildupManager.addBuildup(target, player, katana.getStatusType(), statusBuildup);

                Vec3 kb = flatDir.lengthSqr() > 1e-4 ? flatDir.normalize() : look;
                target.push(kb.x * knockback, 0.15, kb.z * knockback);
                if (heavyPoise) {
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 22, 2));
                    if (target instanceof EldenBossEntity) {
                        level.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(), 10, 0.4, 0.4, 0.4, 0.12);
                    }
                }
                level.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                        target.getX(), target.getY() + target.getBbHeight() * 0.6, target.getZ(),
                        4, 0.25, 0.25, 0.25, 0.05);
            }
        }
        if (hitAny) {
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.PLAYERS, 0.85f, 1.1f);
        }
    }

    private static void spawnSlashArcParticles(ServerLevel level, ServerPlayer player, double radius, double arcDeg,
                                               net.minecraft.core.particles.ParticleOptions particle, int steps) {
        Vec3 center = player.position().add(0, 1.15, 0);
        float baseYaw = (float) Math.toRadians(player.getYRot());
        double halfRad = Math.toRadians(arcDeg * 0.5);
        for (int i = 0; i < steps; i++) {
            double t = steps == 1 ? 0.5 : (double) i / (steps - 1);
            double angle = baseYaw - halfRad + t * halfRad * 2.0;
            double px = center.x - Math.sin(angle) * radius;
            double pz = center.z + Math.cos(angle) * radius;
            level.sendParticles(particle, px, center.y, pz, 1, 0.05, 0.05, 0.05, 0.01);
        }
    }

    private static Vec3 flatLook(ServerPlayer player) {
        float yawRad = (float) Math.toRadians(player.getYRot());
        return new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();
    }
}
