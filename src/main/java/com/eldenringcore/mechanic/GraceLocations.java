package com.eldenringcore.mechanic;

import com.eldenringcore.EldenRingCore;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = EldenRingCore.MOD_ID)
public final class GraceLocations extends SavedData {
    private static final String DISCOVERED = "eldenring_core_discovered_graces";
    private final Map<BlockPos, String> names = new HashMap<>();

    private static GraceLocations get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(GraceLocations::load, GraceLocations::new, "eldenring_core_graces");
    }

    static boolean discover(ServerPlayer player, BlockPos position) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag dimensions = persisted.getCompound(DISCOVERED);
        String dimension = player.level().dimension().location().toString();
        long[] discovered = dimensions.getLongArray(dimension);
        long packed = position.asLong();
        for (long previous : discovered) {
            if (previous == packed) {
                return false;
            }
        }
        long[] updated = Arrays.copyOf(discovered, discovered.length + 1);
        updated[discovered.length] = packed;
        dimensions.putLongArray(dimension, updated);
        persisted.put(DISCOVERED, dimensions);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
        return true;
    }

    static List<GlobalPos> destinations(ServerPlayer player, GlobalPos origin) {
        CompoundTag dimensions = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(DISCOVERED);
        List<GlobalPos> destinations = new ArrayList<>();
        for (String key : dimensions.getAllKeys()) {
            ResourceLocation dimensionId = ResourceLocation.tryParse(key);
            if (dimensionId == null) {
                continue;
            }
            ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
            ServerLevel level = player.serverLevel().getServer().getLevel(dimension);
            if (level == null) {
                continue;
            }
            for (long packed : dimensions.getLongArray(key)) {
                BlockPos position = BlockPos.of(packed);
                GlobalPos destination = GlobalPos.of(dimension, position);
                if (!destination.equals(origin) && (!level.hasChunkAt(position) || isGrace(level, position))) {
                    destinations.add(destination);
                }
            }
        }
        destinations.sort(Comparator.comparing((GlobalPos destination) -> destination.dimension().location())
                .thenComparing(GlobalPos::pos));
        return destinations;
    }

    static Component title(ServerLevel level, BlockPos position) {
        String name = get(level).names.get(position);
        return name != null ? Component.literal(name)
                : Component.translatable("menu.eldenring_core.grace.unnamed", position.getX(), position.getY(), position.getZ());
    }

    static boolean isGrace(ServerLevel level, BlockPos position) {
        return level.getBlockState(position).getBlock() instanceof SiteOfGraceBlock;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("grace")
                .then(Commands.literal("name").requires(source -> source.hasPermission(2))
                        .then(Commands.argument("position", BlockPosArgument.blockPos())
                                .then(Commands.argument("name", StringArgumentType.greedyString()).executes(context -> {
                                    ServerLevel level = context.getSource().getLevel();
                                    BlockPos position = BlockPosArgument.getLoadedBlockPos(context, "position");
                                    String name = StringArgumentType.getString(context, "name").trim();
                                    if (!isGrace(level, position) || name.isEmpty() || name.length() > 64) {
                                        context.getSource().sendFailure(Component.translatable("message.eldenring_core.grace.invalid_name"));
                                        return 0;
                                    }
                                    GraceLocations data = get(level);
                                    data.names.put(position.immutable(), name);
                                    data.setDirty();
                                    context.getSource().sendSuccess(() -> Component.translatable("message.eldenring_core.grace.named", name), true);
                                    return 1;
                                })))));
    }

    private static GraceLocations load(CompoundTag tag) {
        GraceLocations locations = new GraceLocations();
        ListTag entries = tag.getList("Names", Tag.TAG_COMPOUND);
        for (int index = 0; index < entries.size(); index++) {
            CompoundTag entry = entries.getCompound(index);
            locations.names.put(BlockPos.of(entry.getLong("Position")), entry.getString("Name"));
        }
        return locations;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag entries = new ListTag();
        names.forEach((position, name) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("Position", position.asLong());
            entry.putString("Name", name);
            entries.add(entry);
        });
        tag.put("Names", entries);
        return tag;
    }
}
