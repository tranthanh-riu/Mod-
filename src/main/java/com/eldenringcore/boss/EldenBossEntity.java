package com.eldenringcore.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable Elden Ring Boss Framework:
 * - State Machine AI (Telegraph/Delayed Attack -> Active Hitbox -> Recovery)
 * - Phase 2 Transition at configurable HP threshold
 * - Poise / Stance Break with Critical Hit bonus window
 * - Frame-accurate Arc & AoE hitboxes
 * - Elden Ring Boss Bar & Victory Banner ("DEMIGOD FELLED" / "GREAT ENEMY FELLED")
 */
public abstract class EldenBossEntity extends Monster implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_BOSS_STATE =
            SynchedEntityData.defineId(EldenBossEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<String> DATA_CURRENT_ANIM =
            SynchedEntityData.defineId(EldenBossEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> DATA_PHASE_TWO =
            SynchedEntityData.defineId(EldenBossEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);
    protected final ServerBossEvent bossEvent;
    protected final List<BossAttackMove> moveset = new ArrayList<>();

    protected BossState state = BossState.IDLE;
    protected BossAttackMove currentMove = null;
    protected int stateTick = 0;

    protected boolean phaseTwo = false;
    protected float maxPoise = 100.0f;
    protected float currentPoise = 100.0f;
    protected int ticksSinceLastHit = 0;

    protected EldenBossEntity(EntityType<? extends Monster> type, Level level, Component bossTitle, BossEvent.BossBarColor barColor) {
        super(type, level);
        this.bossEvent = new ServerBossEvent(bossTitle, barColor, BossEvent.BossBarOverlay.PROGRESS);
        this.xpReward = 250;
        this.initMoveset();
    }

    protected abstract void initMoveset();

    protected float getPhaseTwoThreshold() {
        return 0.5f;
    }

    protected String getVictoryBannerText() {
        return "GREAT ENEMY FELLED";
    }

    protected void onPhaseTwoStart() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1.5, getZ(), 2, 0.5, 0.5, 0.5, 0.0);
            this.playSound(SoundEvents.WITHER_SPAWN, 2.0f, 0.8f);
        }
        this.bossEvent.setColor(BossEvent.BossBarColor.RED);
    }

    public boolean isPhaseTwo() {
        return this.phaseTwo;
    }

    public BossState getBossState() {
        return this.state;
    }

    public int getStateTick() {
        return this.stateTick;
    }

    public void applyPoiseDamage(float amount) {
        if (this.state == BossState.STAGGERED || this.state == BossState.PHASE_TRANSITION) {
            return;
        }
        this.currentPoise -= amount;
        this.ticksSinceLastHit = 0;
        if (this.currentPoise <= 0.0f) {
            this.currentPoise = this.maxPoise;
            this.enterState(BossState.STAGGERED);
            this.currentMove = null;
            this.playSound(SoundEvents.ANVIL_LAND, 1.8f, 0.6f);
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT, getX(), getY() + 1.8, getZ(), 25, 0.6, 0.6, 0.6, 0.2);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_BOSS_STATE, BossState.IDLE.ordinal());
        this.entityData.define(DATA_CURRENT_ANIM, "");
        this.entityData.define(DATA_PHASE_TWO, false);
    }

    public boolean isSyncedPhaseTwo() {
        return this.entityData.get(DATA_PHASE_TWO);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false; // Bosses with aerial attacks (Radahn, Malenia, Margit) never take fall damage
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false; // Demigod Bosses never randomly despawn
    }

    public BossState getSyncedBossState() {
        int ord = this.entityData.get(DATA_BOSS_STATE);
        BossState[] values = BossState.values();
        return (ord >= 0 && ord < values.length) ? values[ord] : BossState.IDLE;
    }

    public String getSyncedAnimName() {
        return this.entityData.get(DATA_CURRENT_ANIM);
    }

    protected void enterState(BossState next) {
        this.state = next;
        this.stateTick = 0;
        this.entityData.set(DATA_BOSS_STATE, next.ordinal());
        if ((next == BossState.TELEGRAPH || next == BossState.ATTACKING) && this.currentMove != null) {
            this.entityData.set(DATA_CURRENT_ANIM, this.currentMove.animationName());
        } else {
            this.entityData.set(DATA_CURRENT_ANIM, "");
        }
    }

    @Override
    public void customServerAiStep() {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        this.ticksSinceLastHit++;
        if (this.ticksSinceLastHit > 120 && this.currentPoise < this.maxPoise) {
            this.currentPoise = Math.min(this.maxPoise, this.currentPoise + 1.5f);
        }

        // Trigger Phase 2 transition
        if (!this.phaseTwo && (this.getHealth() / this.getMaxHealth()) <= this.getPhaseTwoThreshold()) {
            this.phaseTwo = true;
            this.entityData.set(DATA_PHASE_TWO, true);
            this.currentMove = null;
            this.enterState(BossState.PHASE_TRANSITION);
            this.onPhaseTwoStart();
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            Player nearest = this.level().getNearestPlayer(this, 32.0);
            if (nearest != null && !nearest.isCreative() && !nearest.isSpectator()) {
                this.setTarget(nearest);
                target = nearest;
            }
        }

        this.stateTick++;

        switch (this.state) {
            case IDLE -> {
                if (target != null && target.isAlive()) {
                    this.enterState(BossState.APPROACH);
                }
            }
            case PHASE_TRANSITION -> {
                this.getNavigation().stop();
                if (this.stateTick >= 40) {
                    this.enterState(BossState.APPROACH);
                }
            }
            case STAGGERED -> {
                this.getNavigation().stop();
                if (this.stateTick >= 60) {
                    this.enterState(BossState.APPROACH);
                }
            }
            case APPROACH -> {
                if (target == null || !target.isAlive()) {
                    this.enterState(BossState.IDLE);
                    return;
                }
                this.getLookControl().setLookAt(target, 30.0f, 30.0f);
                this.getNavigation().moveTo(target, this.phaseTwo ? 1.25 : 1.05);

                double distSqr = this.distanceToSqr(target);
                BossAttackMove chosen = this.pickMove(distSqr);
                if (chosen != null && this.stateTick >= 12) {
                    this.currentMove = chosen;
                    this.getNavigation().stop();
                    this.enterState(BossState.TELEGRAPH);
                }
            }
            case TELEGRAPH -> {
                if (this.currentMove == null) {
                    this.enterState(BossState.APPROACH);
                    return;
                }
                if (target != null) {
                    // Track player during early telegraph, lock direction near strike for fair dodging
                    if (this.stateTick < this.currentMove.windupTicks() * 0.75) {
                        this.getLookControl().setLookAt(target, 45.0f, 45.0f);
                    }
                }
                this.spawnTelegraphParticles();
                if (this.stateTick >= this.currentMove.windupTicks()) {
                    this.enterState(BossState.ATTACKING);
                }
            }
            case ATTACKING -> {
                if (this.currentMove == null) {
                    this.enterState(BossState.APPROACH);
                    return;
                }
                this.currentMove.onActiveTick().accept(this, target);
                if (this.stateTick >= this.currentMove.activeTicks()) {
                    this.enterState(BossState.RECOVERY);
                }
            }
            case RECOVERY -> {
                this.getNavigation().stop();
                int recovery = this.currentMove != null ? this.currentMove.recoveryTicks() : 15;
                if (this.stateTick >= recovery) {
                    this.currentMove = null;
                    this.enterState(BossState.APPROACH);
                }
            }
        }
    }

    protected BossAttackMove pickMove(double distSqr) {
        List<BossAttackMove> valid = new ArrayList<>();
        int totalWeight = 0;
        for (BossAttackMove move : this.moveset) {
            if (move.canUse(this, distSqr)) {
                valid.add(move);
                totalWeight += move.weight();
            }
        }
        if (valid.isEmpty() || totalWeight <= 0) {
            return null;
        }
        int roll = this.random.nextInt(totalWeight);
        int cumulative = 0;
        for (BossAttackMove move : valid) {
            cumulative += move.weight();
            if (roll < cumulative) {
                return move;
            }
        }
        return valid.get(0);
    }

    protected void spawnTelegraphParticles() {
        if (this.level() instanceof ServerLevel serverLevel && this.stateTick % 4 == 0) {
            serverLevel.sendParticles(ParticleTypes.FLAME, getX(), getY() + 2.2, getZ(), 2, 0.3, 0.2, 0.3, 0.01);
        }
    }

    /**
     * Frame-accurate directional cone hitbox in front of the boss.
     */
    public void performArcHitbox(double reach, double arcDegrees, float damage, double knockback, ParticleOptions particle) {
        Vec3 forward = this.getLookAngle().multiply(1, 0, 1).normalize();
        double minDot = Math.cos(Math.toRadians(arcDegrees * 0.5));
        AABB box = this.getBoundingBox().inflate(reach, 1.5, reach);

        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            Vec3 toTarget = entity.position().subtract(this.position()).multiply(1, 0, 1);
            double dist = toTarget.length();
            if (dist <= reach && dist > 0.001) {
                double dot = forward.dot(toTarget.normalize());
                if (dot >= minDot) {
                    entity.hurt(this.damageSources().mobAttack(this), damage);
                    entity.push(forward.x * knockback, 0.25, forward.z * knockback);
                }
            }
        }

        if (this.level() instanceof ServerLevel serverLevel && particle != null) {
            Vec3 hitPos = this.position().add(forward.scale(reach * 0.65)).add(0, 1.2, 0);
            serverLevel.sendParticles(particle, hitPos.x, hitPos.y, hitPos.z, 12, 0.6, 0.3, 0.6, 0.05);
        }
    }

    /**
     * Radial AoE slam hitbox around the boss.
     */
    public void performAoeHitbox(double radius, float damage, double knockback, ParticleOptions particle) {
        AABB box = this.getBoundingBox().inflate(radius, 2.0, radius);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> e != this && e.isAlive() && !(e instanceof Player p && (p.isCreative() || p.isSpectator())))) {
            if (this.distanceToSqr(entity) <= radius * radius) {
                entity.hurt(this.damageSources().mobAttack(this), damage);
                Vec3 dir = entity.position().subtract(this.position()).normalize();
                entity.push(dir.x * knockback, 0.45, dir.z * knockback);
            }
        }
        if (this.level() instanceof ServerLevel serverLevel && particle != null) {
            serverLevel.sendParticles(particle, getX(), getY() + 0.3, getZ(), 35, radius * 0.5, 0.2, radius * 0.5, 0.08);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.state == BossState.PHASE_TRANSITION) {
            return false; // Brief invulnerability during Phase 2 cinematic transition
        }
        float finalDamage = amount;
        if (this.state == BossState.STAGGERED) {
            finalDamage *= 1.75f; // Critical Riposte bonus while staggered
            this.playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.5f, 0.8f);
        }
        boolean damaged = super.hurt(source, finalDamage);
        if (damaged && source.getEntity() instanceof LivingEntity) {
            this.applyPoiseDamage(Math.max(8.0f, amount * 1.2f));
        }
        return damaged;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (this.level() instanceof ServerLevel serverLevel) {
            Component banner = Component.literal(this.getVictoryBannerText())
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            for (ServerPlayer player : serverLevel.getPlayers(p -> p.distanceToSqr(this) <= 64.0 * 64.0)) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(banner));
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 0.8f);
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "main_controller", 4, animState -> {
            BossState syncedState = this.getSyncedBossState();
            String syncedAnim = this.getSyncedAnimName();
            if (syncedState == BossState.STAGGERED) {
                return animState.setAndContinue(RawAnimation.begin().thenLoop("animation.boss.staggered"));
            }
            if (syncedState == BossState.PHASE_TRANSITION) {
                return animState.setAndContinue(RawAnimation.begin().thenPlay("animation.boss.phase2"));
            }
            if ((syncedState == BossState.TELEGRAPH || syncedState == BossState.ATTACKING) && !syncedAnim.isEmpty()) {
                return animState.setAndContinue(RawAnimation.begin().thenPlay(syncedAnim));
            }
            if (animState.isMoving()) {
                return animState.setAndContinue(RawAnimation.begin().thenLoop("animation.boss.walk"));
            }
            return animState.setAndContinue(RawAnimation.begin().thenLoop("animation.boss.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.animCache;
    }
}
