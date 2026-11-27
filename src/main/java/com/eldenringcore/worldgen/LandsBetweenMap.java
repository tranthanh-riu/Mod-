package com.eldenringcore.worldgen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.io.InputStream;

/**
 * The painted Lands Between continent: a heightmap and a region map made by tools/mapgen/MapGen.java.
 * One pixel covers 4x4 blocks; the images cover X/Z -4096..4095 and repeat as open ocean beyond.
 */
public final class LandsBetweenMap {
    public static final int OCEAN = 0, LIMGRAVE = 1, LIURNIA = 2, CAELID = 3, ALTUS = 4,
            GELMIR = 5, MOUNTAINTOPS = 6, HALIGTREE = 7, COAST = 8;

    private static final String DIR = "/eldenring_core_worldgen/";
    private static final int SCALE = 4;
    private static volatile LandsBetweenMap instance;

    private final int size;
    private final int half;
    private final short[] height; // (surfaceY + 64) * 64
    private final byte[] region;

    public static LandsBetweenMap get() {
        LandsBetweenMap m = instance;
        if (m == null) {
            synchronized (LandsBetweenMap.class) {
                if (instance == null) instance = load();
                m = instance;
            }
        }
        return m;
    }

    private LandsBetweenMap(int size, short[] height, byte[] region) {
        this.size = size;
        this.half = size * SCALE / 2;
        this.height = height;
        this.region = region;
    }

    private static LandsBetweenMap load() {
        BufferedImage h = read("height.png");
        BufferedImage r = read("regions.png");
        int size = h.getWidth();
        if (h.getHeight() != size || r.getWidth() != size || r.getHeight() != size) {
            throw new IllegalStateException("Lands Between height.png and regions.png must be the same square size");
        }
        Raster hr = h.getRaster(), rr = r.getRaster();
        short[] height = new short[size * size];
        byte[] region = new byte[size * size];
        for (int z = 0; z < size; z++) {
            for (int x = 0; x < size; x++) {
                height[z * size + x] = (short) hr.getSample(x, z, 0);
                region[z * size + x] = (byte) rr.getSample(x, z, 0);
            }
        }
        return new LandsBetweenMap(size, height, region);
    }

    private static BufferedImage read(String name) {
        try (InputStream in = LandsBetweenMap.class.getResourceAsStream(DIR + name)) {
            if (in == null) throw new IllegalStateException("Missing " + DIR + name + " in the eldenring_core jar");
            BufferedImage img = ImageIO.read(in);
            if (img == null) throw new IllegalStateException("Unreadable image " + DIR + name);
            return img;
        } catch (IOException e) {
            throw new IllegalStateException("Could not read " + DIR + name, e);
        }
    }

    /** Surface height in blocks, bilinearly interpolated between pixel centres. */
    public double surfaceY(double blockX, double blockZ) {
        double px = (wrap(blockX) + half) / SCALE - 0.5;
        double pz = (wrap(blockZ) + half) / SCALE - 0.5;
        int x0 = (int) Math.floor(px), z0 = (int) Math.floor(pz);
        double fx = px - x0, fz = pz - z0;
        double a = raw(x0, z0), b = raw(x0 + 1, z0), c = raw(x0, z0 + 1), d = raw(x0 + 1, z0 + 1);
        double top = a + (b - a) * fx, bottom = c + (d - c) * fx;
        return (top + (bottom - top) * fz) / 64.0 - 64.0;
    }

    /** Region id at a block position (nearest pixel). */
    public int region(double blockX, double blockZ) {
        int px = clamp((int) Math.floor((wrap(blockX) + half) / SCALE));
        int pz = clamp((int) Math.floor((wrap(blockZ) + half) / SCALE));
        return region[pz * size + px];
    }

    private double raw(int px, int pz) {
        return height[clamp(pz) * size + clamp(px)] & 0xFFFF;
    }

    private int clamp(int p) {
        return p < 0 ? 0 : (p >= size ? size - 1 : p);
    }

    /** Repeats the tile beyond +-4096 so far-away searches (/locate) meet ocean, not an endless void. */
    private double wrap(double coord) {
        double tile = size * SCALE;
        double shifted = (coord + half) % tile;
        if (shifted < 0) shifted += tile;
        return shifted - half;
    }
}
