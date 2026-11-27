package com.eldenringcore.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;

/**
 * Noise generator whose land shape comes from the painted Lands Between heightmap instead of
 * vanilla's random continents. Everything else (surface rules, aquifers, ore veins, biome
 * features, carvers) is taken unchanged from the referenced noise settings, e.g. minecraft:overworld
 * (which Terralith extends when it is installed).
 */
public class LandsBetweenChunkGenerator extends NoiseBasedChunkGenerator {
    public static final Codec<LandsBetweenChunkGenerator> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(g -> g.biomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(g -> g.baseSettings)
    ).apply(inst, LandsBetweenChunkGenerator::new));

    private final Holder<NoiseGeneratorSettings> baseSettings;

    public LandsBetweenChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, Holder.direct(withHeightmap(settings.value())));
        this.baseSettings = settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static NoiseGeneratorSettings withHeightmap(NoiseGeneratorSettings s) {
        NoiseRouter r = s.noiseRouter();
        // Density > 0 is solid. A gentle gradient keeps cell interpolation smooth; the initial
        // density is offset so the preliminary surface (used by surface rules and aquifers) sits
        // right at the painted height.
        DensityFunction terrain = DensityFunctions.interpolated(new TerrainDensity(0.0));
        DensityFunction initial = new TerrainDensity(0.390625);
        NoiseRouter router = new NoiseRouter(
                r.barrierNoise(), r.fluidLevelFloodednessNoise(), r.fluidLevelSpreadNoise(), r.lavaNoise(),
                r.temperature(), r.vegetation(), r.continents(), r.erosion(), r.depth(), r.ridges(),
                initial, terrain, r.veinToggle(), r.veinRidged(), r.veinGap());
        return new NoiseGeneratorSettings(
                s.noiseSettings(), s.defaultBlock(), s.defaultFluid(), router, s.surfaceRule(),
                s.spawnTarget(), s.seaLevel(), s.disableMobGeneration(), s.aquifersEnabled(),
                s.oreVeinsEnabled(), s.useLegacyRandomSource());
    }

    /** (surfaceY - y) / 24, plus an offset. Built in code only, never written to disk. */
    record TerrainDensity(double offset) implements DensityFunction.SimpleFunction {
        private static final KeyDispatchDataCodec<TerrainDensity> CODEC =
                KeyDispatchDataCodec.of(MapCodec.unit(new TerrainDensity(0.0)));

        @Override
        public double compute(FunctionContext ctx) {
            double surface = LandsBetweenMap.get().surfaceY(ctx.blockX(), ctx.blockZ());
            return (surface - ctx.blockY()) / 24.0 + offset;
        }

        @Override
        public double minValue() {
            return -20.0;
        }

        @Override
        public double maxValue() {
            return 20.0;
        }

        @Override
        public KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }
    }
}
