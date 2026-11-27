package com.eldenringcore.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;

import java.util.stream.Stream;

/**
 * Geographic BiomeSource for The Lands Between Dimension (~2800 x 3400 blocks):
 * Places all 7 iconic Elden Ring regions in their authentic world positions with full native
 * Minecraft 3D terrain, deep caves (Y=-64), rivers, trees, When Dungeons Arise fortresses & YUNG's Catacombs!
 *
 * Map coordinates (in Blocks, North = -Z, East = +X):
 * - Limgrave      : South-West  (center X=-300, Z=700)
 * - Caelid        : East        (center X=650,  Z=550)
 * - Liurnia       : West        (center X=-750, Z=-150)
 * - Mt. Gelmir    : North-West  (center X=-750, Z=-950)
 * - Altus/Leyndell: Center-North(center X=50,   Z=-700)
 * - Mountaintops  : North-East  (center X=800,  Z=-1050)
 * - Haligtree     : Far North   (center X=0,    Z=-1450)
 */
public class LandsBetweenBiomeSource extends BiomeSource {
    public static final Codec<LandsBetweenBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.CODEC.fieldOf("limgrave").forGetter(s -> s.limgrave),
            Biome.CODEC.fieldOf("liurnia").forGetter(s -> s.liurnia),
            Biome.CODEC.fieldOf("caelid").forGetter(s -> s.caelid),
            Biome.CODEC.fieldOf("altus_plateau").forGetter(s -> s.altusPlateau),
            Biome.CODEC.fieldOf("mt_gelmir").forGetter(s -> s.mtGelmir),
            Biome.CODEC.fieldOf("mountaintops").forGetter(s -> s.mountaintops),
            Biome.CODEC.fieldOf("haligtree").forGetter(s -> s.haligtree),
            Biome.CODEC.fieldOf("ocean").forGetter(s -> s.ocean)
    ).apply(instance, LandsBetweenBiomeSource::new));

    private final Holder<Biome> limgrave;
    private final Holder<Biome> liurnia;
    private final Holder<Biome> caelid;
    private final Holder<Biome> altusPlateau;
    private final Holder<Biome> mtGelmir;
    private final Holder<Biome> mountaintops;
    private final Holder<Biome> haligtree;
    private final Holder<Biome> ocean;

    public LandsBetweenBiomeSource(Holder<Biome> limgrave, Holder<Biome> liurnia, Holder<Biome> caelid,
                                   Holder<Biome> altusPlateau, Holder<Biome> mtGelmir, Holder<Biome> mountaintops,
                                   Holder<Biome> haligtree, Holder<Biome> ocean) {
        this.limgrave = limgrave;
        this.liurnia = liurnia;
        this.caelid = caelid;
        this.altusPlateau = altusPlateau;
        this.mtGelmir = mtGelmir;
        this.mountaintops = mountaintops;
        this.haligtree = haligtree;
        this.ocean = ocean;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    protected Stream<Holder<Biome>> collectPossibleBiomes() {
        return Stream.of(limgrave, liurnia, caelid, altusPlateau, mtGelmir, mountaintops, haligtree, ocean);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int quartX, int quartY, int quartZ, Climate.Sampler sampler) {
        double bx = quartX * 4.0;
        double bz = quartZ * 4.0;

        // Subtle organic coastline warp so borders look natural, not straight lines
        double warpX = Math.sin(bz * 0.0045) * 95.0 + Math.cos(bz * 0.011) * 35.0;
        double warpZ = Math.cos(bx * 0.0045) * 95.0 + Math.sin(bx * 0.011) * 35.0;
        double x = bx + warpX;
        double z = bz + warpZ;

        // Check each of the 7 regions by closest normalized ellipse score
        double bestScore = 0.0;
        Holder<Biome> chosen = this.ocean;

        double s;
        if ((s = ellipseScore(x, z, -300,  700, 620, 520)) > bestScore) { bestScore = s; chosen = this.limgrave; }
        if ((s = ellipseScore(x, z,  650,  550, 560, 480)) > bestScore) { bestScore = s; chosen = this.caelid; }
        if ((s = ellipseScore(x, z, -750, -150, 540, 560)) > bestScore) { bestScore = s; chosen = this.liurnia; }
        if ((s = ellipseScore(x, z, -750, -950, 440, 380)) > bestScore) { bestScore = s; chosen = this.mtGelmir; }
        if ((s = ellipseScore(x, z,   50, -700, 540, 460)) > bestScore) { bestScore = s; chosen = this.altusPlateau; }
        if ((s = ellipseScore(x, z,  800,-1050, 560, 480)) > bestScore) { bestScore = s; chosen = this.mountaintops; }
        if ((s = ellipseScore(x, z,    0,-1450, 380, 320)) > bestScore) { bestScore = s; chosen = this.haligtree; }

        return chosen;
    }

    private static double ellipseScore(double x, double z, double cx, double cz, double rx, double rz) {
        double dx = (x - cx) / rx;
        double dz = (z - cz) / rz;
        return 1.0 - Math.sqrt(dx * dx + dz * dz);
    }
}
