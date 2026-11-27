package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.mechanic.BossArenaBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, EldenRingCore.MOD_ID);

    public static final RegistryObject<BlockEntityType<BossArenaBlockEntity>> BOSS_ARENA =
            BLOCK_ENTITIES.register("boss_arena_controller",
                    () -> BlockEntityType.Builder.of(
                            BossArenaBlockEntity::new,
                            ModBlocks.BOSS_ARENA_CONTROLLER.get()
                    ).build(null));
}
