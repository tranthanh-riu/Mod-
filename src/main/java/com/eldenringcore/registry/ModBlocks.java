package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.mechanic.BossArenaBlock;
import com.eldenringcore.mechanic.FogGateBlock;
import com.eldenringcore.mechanic.SiteOfGraceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, EldenRingCore.MOD_ID);

    public static final RegistryObject<Block> SITE_OF_GRACE = BLOCKS.register("site_of_grace",
            () -> new SiteOfGraceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.GOLD)
                    .strength(-1.0f, 3600000.0f)
                    .lightLevel(state -> 14)
                    .noOcclusion()
                    .sound(SoundType.AMETHYST)));

    public static final RegistryObject<Block> FOG_GATE = BLOCKS.register("fog_gate",
            () -> new FogGateBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(-1.0f, 3600000.0f)
                    .lightLevel(state -> 10)
                    .noOcclusion()
                    .sound(SoundType.WOOL)));

    public static final RegistryObject<Block> BOSS_ARENA_CONTROLLER = BLOCKS.register("boss_arena_controller",
            () -> new BossArenaBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(-1.0f, 3600000.0f)
                    .sound(SoundType.STONE)));
}
