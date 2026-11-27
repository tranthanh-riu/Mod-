package com.eldenringcore.registry;

import com.eldenringcore.EldenRingCore;
import com.eldenringcore.worldgen.LandsBetweenBiomeSource;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModWorldgen {
    public static final ResourceKey<Level> LANDS_BETWEEN_LEVEL = ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(EldenRingCore.MOD_ID, "lands_between")
    );

    public static final DeferredRegister<Codec<? extends BiomeSource>> BIOME_SOURCES =
            DeferredRegister.create(Registries.BIOME_SOURCE, EldenRingCore.MOD_ID);

    public static final RegistryObject<Codec<LandsBetweenBiomeSource>> LANDS_BETWEEN_BIOME_SOURCE =
            BIOME_SOURCES.register("lands_between", () -> LandsBetweenBiomeSource.CODEC);
}
