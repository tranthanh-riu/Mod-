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
        // Wrap coordinates across an 8192x8192 ocean-bordered archipelago tile:
        // - Inside [-4096, +4096]: full ~4800x5600 Lands Between continent + wide ocean border.
        // - Beyond +-4096: invisible ocean-seam repeat so /locate & Cataclysm Eyes never hang in infinite ocean.
        double bx = wrapTile(quartX * 4.0, 8192.0);
        double bz = wrapTile(quartZ * 4.0, 8192.0);
        int blockY = quartY * 4;

        // Smooth organic coastline warp
        double warpX = Math.sin(bz * 0.0028) * 140.0 + Math.cos(bz * 0.0072) * 55.0;
        double warpZ = Math.cos(bx * 0.0028) * 140.0 + Math.sin(bx * 0.0072) * 55.0;
        double x = bx + warpX;
        double z = bz + warpZ;

        double bestScore = 0.0;
        RegionBiomes chosen = null;

        double s;
        if ((s = ellipseScore(x, z, -500,  1200, 1050, 850)) > bestScore) { bestScore = s; chosen = this.limgrave; }
        if ((s = ellipseScore(x, z, 1150,   950,  950, 800)) > bestScore) { bestScore = s; chosen = this.caelid; }
        if ((s = ellipseScore(x, z,-1300,  -250,  950, 950)) > bestScore) { bestScore = s; chosen = this.liurnia; }
        if ((s = ellipseScore(x, z,  100, -1150,  950, 800)) > bestScore) { bestScore = s; chosen = this.altusPlateau; }
        if ((s = ellipseScore(x, z,-1300, -1600,  780, 680)) > bestScore) { bestScore = s; chosen = this.mtGelmir; }
        if ((s = ellipseScore(x, z, 1400, -1750,  950, 820)) > bestScore) { bestScore = s; chosen = this.mountaintops; }
        if ((s = ellipseScore(x, z,    0, -2450,  680, 580)) > bestScore) { bestScore = s; chosen = this.haligtree; }

        if (chosen == null) {
            return this.ocean;
        }

        // Authentic 3D Underground Realms (Siofra River, Lake of Rot, Deeproot Depths, Volcano Depths, Giants' Catacombs)
        if (blockY < 32) {
            return chosen.cave();
        }

        // Scenic coastal cliff rim along the ocean edge of Limgrave / Caelid / Liurnia
        if (bestScore < 0.065 && (chosen == this.limgrave || chosen == this.liurnia)) {
            return this.coast;
        }

        // Core center around each region's Site of Grace stays the iconic signature biome
        if (bestScore > 0.72) {
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

    private static double wrapTile(double coord, double size) {
        double half = size * 0.5;
        double shifted = (coord + half) % size;
        if (shifted < 0) shifted += size;
        return shifted - half;
    }

    private static double ellipseScore(double x, double z, double cx, double cz, double rx, double rz) {
        double dx = (x - cx) / rx;
        double dz = (z - cz) / rz;
        return 1.0 - Math.sqrt(dx * dx + dz * dz);
    }
}
