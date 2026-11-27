package com.eldenringcore.mechanic;

import com.eldenringcore.registry.ModBlockEntities;
import com.eldenringcore.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Controls an Elden Ring Boss Arena on a hand-crafted Adventure Map:
 * - Detects Survival/Adventure players entering triggerRadius -> spawns Boss.
 * - If all players die/leave resetRadius -> despawns Boss so the next attempt starts fresh.
 * - When Boss is defeated -> marks arena cleared and spawns a Site of Grace at pos.above().
 */
public class BossArenaBlockEntity extends BlockEntity {
    private static final int MAX_GATE_OFFSET = 64;
    private static final int MAX_GATE_BLOCKS = 256;
    private final Set<BlockPos> fogGates = new LinkedHashSet<>();
    private String bossId = "eldenring_core:margit";
    private double triggerRadius = 14.0;
    private double resetRadius = 34.0;
    private boolean defeated = false;
    private UUID activeBossUuid = null;
    private long bossChunk;
    private int tickCounter = 0;

    public BossArenaBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOSS_ARENA.get(), pos, state);
    }

    public String getBossId() {
        return this.bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
        this.setChanged();
    }

    public void resetArena() {
        if (this.level instanceof ServerLevel serverLevel && this.activeBossUuid != null) {
            Entity existing = serverLevel.getEntity(this.activeBossUuid);
            if (existing != null) {
                existing.discard();
            }
        }
        this.activeBossUuid = null;
        this.defeated = false;
        if (this.level instanceof ServerLevel serverLevel) {
            updateFogGates(serverLevel, true);
        }
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BossArenaBlockEntity arena) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        arena.tickCounter++;
        if (arena.tickCounter % 20 != 0) {
            return; // Check once per second
        }
        if (!arena.defeated && arena.activeBossUuid == null) {
            List<Player> challengers = findActiveChallengers(serverLevel, pos, arena.triggerRadius);
            if (!challengers.isEmpty()) {
                arena.spawnBoss(serverLevel, pos);
            }
        } else if (!arena.defeated) {
            Entity bossEntity = serverLevel.getEntity(arena.activeBossUuid);
            if (bossEntity == null) {
                if (serverLevel.areEntitiesLoaded(arena.bossChunk)) {
                    arena.resetArena();
                }
            } else if (!bossEntity.isAlive()) {
                arena.onBossDefeated(serverLevel, pos);
            } else if (findActiveChallengers(serverLevel, pos, arena.resetRadius).isEmpty()) {
                arena.resetArena();
            } else if (arena.bossChunk != bossEntity.chunkPosition().toLong()) {
                arena.bossChunk = bossEntity.chunkPosition().toLong();
                arena.setChanged();
            }
        }
        // Reconcile gates whose chunks were unloaded when the fight changed state.
        arena.updateFogGates(serverLevel, arena.activeBossUuid == null);
    }

    void updateFogGates(ServerLevel level, boolean open) {
        for (BlockPos offset : this.fogGates) {
            BlockPos gatePos = this.worldPosition.offset(offset);
            if (!level.hasChunkAt(gatePos)) {
                continue;
            }
            BlockState gate = level.getBlockState(gatePos);
            if (gate.is(ModBlocks.FOG_GATE.get()) && gate.getValue(FogGateBlock.OPEN) != open) {
                level.setBlock(gatePos, gate.setValue(FogGateBlock.OPEN, open), Block.UPDATE_ALL);
            }
        }
    }

    private static List<Player> findActiveChallengers(ServerLevel level, BlockPos pos, double radius) {
        AABB box = new AABB(pos).inflate(radius);
        return level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isCreative() && !p.isSpectator());
    }

    private void spawnBoss(ServerLevel level, BlockPos pos) {
        ResourceLocation id = ResourceLocation.tryParse(this.bossId);
        if (id == null) {
            return;
        }
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        if (type == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
            return;
        }
        Entity entity = type.create(level);
        if (entity instanceof LivingEntity living) {
            living.moveTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0f, 0.0f);
            if (living instanceof Mob mob) {
                mob.setPersistenceRequired();
            }
            if (!level.addFreshEntity(living)) {
                return;
            }
            this.activeBossUuid = living.getUUID();
            this.bossChunk = living.chunkPosition().toLong();
            this.setChanged();

            level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.4f, 0.7f);
            level.sendParticles(ParticleTypes.WAX_ON, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 30, 1.0, 1.0, 1.0, 0.05);
        }
    }

    private void onBossDefeated(ServerLevel level, BlockPos pos) {
        this.defeated = true;
        this.activeBossUuid = null;
        this.setChanged();

        BlockPos gracePos = pos.above();
        if (level.getBlockState(gracePos).isAir()) {
            level.setBlockAndUpdate(gracePos, ModBlocks.SITE_OF_GRACE.get().defaultBlockState());
            level.sendParticles(ParticleTypes.END_ROD, gracePos.getX() + 0.5, gracePos.getY() + 0.8, gracePos.getZ() + 0.5, 40, 0.5, 1.0, 0.5, 0.06);
            level.playSound(null, gracePos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 1.1f);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("BossId", this.bossId);
        tag.putDouble("TriggerRadius", this.triggerRadius);
        tag.putDouble("ResetRadius", this.resetRadius);
        tag.putBoolean("Defeated", this.defeated);
        ListTag gates = new ListTag();
        for (BlockPos offset : this.fogGates) {
            gates.add(NbtUtils.writeBlockPos(offset));
        }
        tag.put("FogGates", gates);
        if (this.activeBossUuid != null) {
            tag.putUUID("ActiveBoss", this.activeBossUuid);
            tag.putLong("BossChunk", this.bossChunk);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("BossId")) this.bossId = tag.getString("BossId");
        if (tag.contains("TriggerRadius")) this.triggerRadius = tag.getDouble("TriggerRadius");
        if (tag.contains("ResetRadius")) this.resetRadius = tag.getDouble("ResetRadius");
        this.defeated = tag.getBoolean("Defeated");
        this.activeBossUuid = tag.hasUUID("ActiveBoss") ? tag.getUUID("ActiveBoss") : null;
        this.bossChunk = tag.contains("BossChunk", Tag.TAG_LONG) ? tag.getLong("BossChunk") : new ChunkPos(worldPosition).toLong();
        this.fogGates.clear();
        ListTag gates = tag.getList("FogGates", Tag.TAG_COMPOUND);
        for (int i = 0; i < Math.min(gates.size(), MAX_GATE_BLOCKS); i++) {
            CompoundTag gate = gates.getCompound(i);
            if (gate.contains("X", Tag.TAG_INT) && gate.contains("Y", Tag.TAG_INT) && gate.contains("Z", Tag.TAG_INT)) {
                BlockPos offset = NbtUtils.readBlockPos(gate);
                if (withinGateRange(offset.getX()) && withinGateRange(offset.getY()) && withinGateRange(offset.getZ())) {
                    this.fogGates.add(offset);
                }
            }
        }
    }

    private static boolean withinGateRange(int offset) {
        return offset >= -MAX_GATE_OFFSET && offset <= MAX_GATE_OFFSET;
    }
}
