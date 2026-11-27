package com.eldenringcore.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

/**
 * Geographic BiomeSource for The Lands Between Dimension (~4800 x 5600 blocks):
 * Powered by 27 curated Terralith biomes — 7 iconic Elden Ring Regions, each with
 * 1 main signature biome, 2 scenic sub-biomes, 1 authentic 3D underground cave biome,
 * plus dramatic white-cliff coastlines and deep ocean surround!
 *
 * Map coordinates (in Blocks, North = -Z, East = +X):
 * - Limgrave      : South-West  (center X=-500,  Z=1200)
 * - Caelid        : East        (center X=1150,  Z=950)
 * - Liurnia       : West        (center X=-1300, Z=-250)
 * - Altus/Leyndell: Center-North(center X=100,   Z=-1150)
 * - Mt. Gelmir    : North-West  (center X=-1300, Z=-1600)
 * - Mountaintops  : North-East  (center X=1400,  Z=-1750)
 * - Haligtree     : Far North   (center X=0,     Z=-2450)
 */
public class LandsBetweenBiomeSource extends BiomeSource {

    public record RegionBiomes(
            Holder<Biome> main,
            Holder<Biome> sub1,
            Holder<Biome> sub2,
            Holder<Biome> cave
    ) {
        public static final Codec<RegionBiomes> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Biome.CODEC.fieldOf("main").forGetter(RegionBiomes::main),
                Biome.CODEC.fieldOf("sub1").forGetter(RegionBiomes::sub1),
                Biome.CODEC.fieldOf("sub2").forGetter(RegionBiomes::sub2),
                Biome.CODEC.fieldOf("cave").forGetter(RegionBiomes::cave)
        ).apply(inst, RegionBiomes::new));

        public Stream<Holder<Biome>> stream() {
            return Stream.of(main, sub1, sub2, cave);
        }
    }

    public static final Codec<LandsBetweenBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegionBiomes.CODEC.fieldOf("limgrave").forGetter(s -> s.limgrave),
            RegionBiomes.CODEC.fieldOf("liurnia").forGetter(s -> s.liurnia),
            RegionBiomes.CODEC.fieldOf("caelid").forGetter(s -> s.caelid),
            RegionBiomes.CODEC.fieldOf("altus_plateau").forGetter(s -> s.altusPlateau),
            RegionBiomes.CODEC.fieldOf("mt_gelmir").forGetter(s -> s.mtGelmir),
            RegionBiomes.CODEC.fieldOf("mountaintops").forGetter(s -> s.mountaintops),
            RegionBiomes.CODEC.fieldOf("haligtree").forGetter(s -> s.haligtree),
            Biome.CODEC.fieldOf("coast").forGetter(s -> s.coast),
            Biome.CODEC.fieldOf("ocean").forGetter(s -> s.ocean)
    ).apply(instance, LandsBetweenBiomeSource::new));

    private final RegionBiomes limgrave;
    private final RegionBiomes liurnia;
    private final RegionBiomes caelid;
    private final RegionBiomes altusPlateau;
    private final RegionBiomes mtGelmir;
    private final RegionBiomes mountaintops;
    private final RegionBiomes haligtree;
    private final Holder<Biome> coast;
    private final Holder<Biome> ocean;

    public LandsBetweenBiomeSource(RegionBiomes limgrave, RegionBiomes liurnia, RegionBiomes caelid,
                                   RegionBiomes altusPlateau, RegionBiomes mtGelmir, RegionBiomes mountaintops,
                                   RegionBiomes haligtree, Holder<Biome> coast, Holder<Biome> ocean) {
        this.limgrave = limgrave;
        this.liurnia = liurnia;
        this.caelid = caelid;
        this.altusPlateau = altusPlateau;
        this.mtGelmir = mtGelmir;
        this.mountaintops = mountaintops;
        this.haligtree = haligtree;
        this.coast = coast;
        this.ocean = ocean;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(
                limgrave.stream(),
                liurnia.stream(),
                caelid.stream(),
                altusPlateau.stream(),
                mtGelmir.stream(),
                mountaintops.stream(),
                haligtree.stream(),
                Stream.of(coast, ocean)
        ).flatMap(s -> s);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        // Regions come from the painted map (tools/mapgen/MapGen.java), so biomes follow the terrain.
        double bx = quartX * 4.0;
        double bz = quartZ * 4.0;
        int blockY = quartY * 4;

        LandsBetweenMap map = LandsBetweenMap.get();
        int id = map.region(bx, bz);
        if (id == LandsBetweenMap.OCEAN) {
            return this.ocean;
        }
        if (id == LandsBetweenMap.COAST) {
            return blockY < 32 ? this.limgrave.cave() : this.coast;
        }
        RegionBiomes chosen = switch (id) {
            case LandsBetweenMap.LIURNIA -> this.liurnia;
            case LandsBetweenMap.CAELID -> this.caelid;
            case LandsBetweenMap.ALTUS -> this.altusPlateau;
            case LandsBetweenMap.GELMIR -> this.mtGelmir;
            case LandsBetweenMap.MOUNTAINTOPS -> this.mountaintops;
            case LandsBetweenMap.HALIGTREE -> this.haligtree;
            default -> this.limgrave;
        };

        // Underground realms: well below the painted surface (and under Y 32).
        if (blockY < 32 && blockY < map.surfaceY(bx, bz) - 24) {
            return chosen.cave();
        }

        // The area around each region's Site of Grace stays the signature biome.
        double[] c = CENTRES[id];
        if (Math.hypot(bx - c[0], bz - c[1]) < 320) {
            return chosen.main();
        }

        // Organic low-frequency sub-biome patches (~300-450 block pockets, smooth & natural)
        double subNoise = Math.sin(bx * 0.0055 + Math.cos(bz * 0.0042) * 1.4)
                        * Math.cos(bz * 0.0051 - Math.sin(bx * 0.0039) * 1.2);
        if (subNoise > 0.28) {
            return chosen.sub1();
        } else if (subNoise < -0.28) {
            return chosen.sub2();
        }
        return chosen.main();
    }

    /** Site of Grace centre of each region, indexed by LandsBetweenMap region id. */
    private static final double[][] CENTRES = {
            {0, 0}, {-500, 1200}, {-1300, -250}, {1150, 950}, {100, -1150},
            {-1300, -1600}, {1400, -1750}, {0, -2450}, {0, 0}
    };
}
