package com.eldenringcore.mechanic;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-dimension Site of Grace Fast Travel network (supports both Overworld and eldenring_core:lands_between).
 */
public class GraceFastTravelManager extends SavedData {
    private static final String DATA_NAME = "eldenring_core_graces";

    public record GracePoint(String name, BlockPos pos, String dimensionId, boolean autoSurfaceY) {}

    private final List<GracePoint> graces = new ArrayList<>();

    public static GraceFastTravelManager get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(
                GraceFastTravelManager::load,
                GraceFastTravelManager::new,
                DATA_NAME
        );
    }

    public static GraceFastTravelManager load(CompoundTag tag) {
        GraceFastTravelManager mgr = new GraceFastTravelManager();
        ListTag list = tag.getList("Graces", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag g = list.getCompound(i);
            String dim = g.contains("Dim") ? g.getString("Dim") : "minecraft:overworld";
            mgr.graces.add(new GracePoint(
                    g.getString("Name"),
                    new BlockPos(g.getInt("X"), g.getInt("Y"), g.getInt("Z")),
                    dim,
                    g.getBoolean("AutoY")
            ));
        }
        return mgr;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (GracePoint gp : this.graces) {
            CompoundTag g = new CompoundTag();
            g.putString("Name", gp.name());
            g.putInt("X", gp.pos().getX());
            g.putInt("Y", gp.pos().getY());
            g.putInt("Z", gp.pos().getZ());
            g.putString("Dim", gp.dimensionId());
            g.putBoolean("AutoY", gp.autoSurfaceY());
            list.add(g);
        }
        tag.put("Graces", list);
        return tag;
    }

    public boolean registerGrace(BlockPos pos, String customName) {
        return registerGrace(pos, customName, "minecraft:overworld", false);
    }

    public boolean registerGrace(BlockPos pos, String customName, String dimensionId, boolean autoSurfaceY) {
        for (GracePoint gp : this.graces) {
            if (gp.dimensionId().equals(dimensionId) && gp.pos().distManhattan(pos) <= 4) {
                return false;
            }
        }
        String label = (customName != null && !customName.isBlank())
                ? customName
                : ("Site of Grace #" + (this.graces.size() + 1) + " (" + pos.getX() + ", " + pos.getZ() + ")");
        this.graces.add(new GracePoint(label, pos.immutable(), dimensionId, autoSurfaceY));
        this.setDirty();
        return true;
    }

    public void sendFastTravelMenu(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("--- SITE OF GRACE FAST TRAVEL ---").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        if (this.graces.isEmpty()) {
            player.sendSystemMessage(Component.literal("Chưa khám phá điểm Site of Grace nào.").withStyle(ChatFormatting.GRAY));
            return;
        }
        for (int i = 0; i < this.graces.size(); i++) {
            GracePoint gp = this.graces.get(i);
            int idx = i + 1;
            Component line = Component.literal(" [" + idx + "] " + gp.name() + " ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal("[Dịch chuyển]")
                            .withStyle(style -> style
                                    .withColor(ChatFormatting.GOLD)
                                    .withBold(true)
                                    .withUnderlined(true)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/eldenstory tp " + idx))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                            Component.literal("Bấm để dịch chuyển tới " + gp.name())))));
            player.sendSystemMessage(line);
        }
    }

    public boolean teleportPlayer(ServerPlayer player, int oneBasedIndex) {
        int idx = oneBasedIndex - 1;
        if (idx < 0 || idx >= this.graces.size()) {
            return false;
        }
        GracePoint gp = this.graces.get(idx);
        ResourceLocation dimLoc = ResourceLocation.tryParse(gp.dimensionId());
        ResourceKey<Level> dimKey = dimLoc != null ? ResourceKey.create(Registries.DIMENSION, dimLoc) : Level.OVERWORLD;
        ServerLevel targetLevel = player.server.getLevel(dimKey);
        if (targetLevel == null) {
            targetLevel = player.server.overworld();
        }

        double y = gp.pos().getY() + 1.0;
        if (gp.autoSurfaceY()) {
            targetLevel.getChunk(gp.pos());
            int surfaceY = targetLevel.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, gp.pos().getX(), gp.pos().getZ());
            y = Math.max(surfaceY + 2, 70);
        }

        player.teleportTo(targetLevel, gp.pos().getX() + 0.5, y, gp.pos().getZ() + 0.5, player.getYRot(), player.getXRot());
        targetLevel.sendParticles(ParticleTypes.END_ROD, gp.pos().getX() + 0.5, y + 0.5, gp.pos().getZ() + 0.5, 35, 0.5, 1.0, 0.5, 0.05);
        player.playSound(SoundEvents.BEACON_ACTIVATE, 1.2f, 1.3f);
        player.sendSystemMessage(Component.literal("Đã dịch chuyển tới " + gp.name()).withStyle(ChatFormatting.GOLD));
        return true;
    }
}
