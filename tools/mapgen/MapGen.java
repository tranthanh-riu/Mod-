import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;

/**
 * Generates the Lands Between continent for eldenring_core: our own layout, painted procedurally.
 *
 *   java tools/mapgen/MapGen.java src/main/resources/eldenring_core_worldgen
 *
 * Writes (1 pixel = 4x4 blocks, covering X/Z -4096..4095, north = -Z = top of image):
 *   height.png   16-bit gray, value = (surfaceY + 64) * 64
 *   regions.png  8-bit gray, value = region id (0 ocean, 1 limgrave, 2 liurnia, 3 caelid,
 *                4 altus, 5 gelmir, 6 mountaintops, 7 haligtree, 8 coast cliffs)
 *   preview.png  shaded colour map for people to look at (not loaded by the game)
 *
 * Re-run after editing; the output is deterministic (fixed SEED).
 */
public class MapGen {
    static final int SIZE = 2048;          // pixels
    static final int SCALE = 4;            // blocks per pixel
    static final int HALF = SIZE * SCALE / 2;
    static final long SEED = 0x45_4C_44_45_4EL; // "ELDEN"
    static final int SEA = 63;

    // Region centres (match Sites of Grace and RegionDifficultyManager), radii in blocks.
    static final String[] NAMES = {"ocean", "limgrave", "liurnia", "caelid", "altus", "gelmir", "mountaintops", "haligtree", "coast"};
    static final double[][] ELLIPSES = {
            {},
            {-500, 1200, 1050, 850},   // 1 Limgrave
            {-1300, -250, 950, 950},   // 2 Liurnia
            {1150, 950, 950, 800},     // 3 Caelid
            {100, -1150, 950, 800},    // 4 Altus Plateau / Leyndell
            {-1300, -1600, 780, 680},  // 5 Mt. Gelmir
            {1400, -1750, 950, 820},   // 6 Mountaintops of the Giants
            {0, -2450, 680, 580},      // 7 Haligtree
    };

    // Hidden ellipses that only add land (no region of their own) to close the gaps.
    static final double[][] BRIDGES = {
            {-150, 250, 700, 520},    // heart of the continent between Limgrave, Liurnia, Caelid, Altus
            {-850, 520, 420, 330},    // Limgrave <-> Liurnia
            {600, -250, 420, 380},    // Caelid <-> Altus
            {100, -380, 520, 320},    // southern foot of Altus
            {520, 160, 460, 360},     // Caelid <-> Limgrave north
    };

    static Noise n1, n2, n3, n4;

    public static void main(String[] args) throws Exception {
        File out = new File(args.length > 0 ? args[0] : "mapgen-out");
        out.mkdirs();
        n1 = new Noise(SEED);
        n2 = new Noise(SEED * 31 + 7);
        n3 = new Noise(SEED * 131 + 3);
        n4 = new Noise(SEED * 977 + 11);

        double[][] h = new double[SIZE][SIZE];
        int[][] reg = new int[SIZE][SIZE];
        for (int pz = 0; pz < SIZE; pz++) {
            for (int px = 0; px < SIZE; px++) {
                double x = px * SCALE - HALF + SCALE / 2.0;
                double z = pz * SCALE - HALF + SCALE / 2.0;
                double[] r = sample(x, z);
                h[pz][px] = r[0];
                reg[pz][px] = (int) r[1];
            }
        }
        h = blur(h, 1);

        BufferedImage hi = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_USHORT_GRAY);
        BufferedImage ri = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster hr = hi.getRaster(), rr = ri.getRaster();
        for (int z = 0; z < SIZE; z++) {
            for (int x = 0; x < SIZE; x++) {
                double y = Math.max(-60, Math.min(316, h[z][x]));
                hr.setSample(x, z, 0, (int) Math.round((y + 64) * 64));
                rr.setSample(x, z, 0, reg[z][x]);
            }
        }
        ImageIO.write(hi, "png", new File(out, "height.png"));
        ImageIO.write(ri, "png", new File(out, "regions.png"));
        ImageIO.write(preview(h, reg), "png", new File(out, "preview.png"));

        int[] count = new int[9];
        double max = -999;
        for (int z = 0; z < SIZE; z++) for (int x = 0; x < SIZE; x++) { count[reg[z][x]]++; max = Math.max(max, h[z][x]); }
        for (int i = 0; i < 9; i++) System.out.printf("%-13s %6.1f km2%n", NAMES[i], count[i] * SCALE * SCALE / 1e6);
        System.out.printf("highest point Y=%.0f%n", max);
    }

    /** Returns {surfaceY, regionId} for a block position. */
    static double[] sample(double bx, double bz) {
        // Same organic coastline warp as the biome source used before, plus finer noise.
        double x = bx + Math.sin(bz * 0.0028) * 140.0 + Math.cos(bz * 0.0072) * 55.0 + n4.fbm(bx / 220, bz / 220, 3) * 45;
        double z = bz + Math.cos(bx * 0.0028) * 140.0 + Math.sin(bx * 0.0072) * 55.0 + n4.fbm(bx / 220 + 50, bz / 220, 3) * 45;

        double[] s = new double[8];
        double best = -9;
        int bestR = 0;
        for (int r = 1; r <= 7; r++) {
            double[] e = ELLIPSES[r];
            double dx = (x - e[0]) / e[2], dz = (z - e[1]) / e[3];
            s[r] = 1.0 - Math.sqrt(dx * dx + dz * dz);
            if (s[r] > best) { best = s[r]; bestR = r; }
        }
        // Smooth union of all regions (plus land bridges) so the regions form one continent
        // instead of separate islands; the Haligtree stays joined by a narrow neck.
        double k = 9, acc = 0;
        for (int r = 1; r <= 7; r++) acc += Math.exp(k * s[r]);
        for (double[] f : BRIDGES) {
            double dx = (x - f[0]) / f[2], dz = (z - f[1]) / f[3];
            acc += Math.exp(k * (1.0 - Math.sqrt(dx * dx + dz * dz)));
        }
        double land = Math.log(acc) / k + n1.fbm(bx / 350, bz / 350, 4) * 0.06;

        // Blend region heights: weights fall off over a narrow band so borders become slopes or cliffs.
        double sumW = 0, landH = 0;
        for (int r = 1; r <= 7; r++) {
            double t = s[r] - (best - 0.16);
            if (t <= 0) continue;
            double w = smooth(0, 0.16, t);
            w = w * w * (r == 4 ? w * w : 1); // Altus rises as a sharp cliff wall
            // Outside its own ellipse a region eases down to rolling foothills (~Y 80).
            double rh = regionHeight(r, bx, bz);
            double inside = smooth(-0.18, 0.0, s[r]);
            double foot = 80 + n2.fbm(bx / 180, bz / 180, 3) * 8;
            landH += w * (foot + (rh - foot) * inside);
            sumW += w;
        }
        landH /= sumW;
        landH = ramps(bx, bz, landH);

        double oceanH = 28 + 26 * smooth(-0.30, 0.0, land) + n2.fbm(bx / 120, bz / 120, 3) * 3;
        boolean cliffCoast = bestR == 1 || bestR == 2;
        double c = smooth(0.0, cliffCoast ? 0.025 : 0.07, land);
        double y = oceanH + (landH - oceanH) * c;

        int region = land > 0 ? bestR : 0;
        if (region != 0 && cliffCoast && land < 0.05 && y > SEA) region = 8;
        if (region != 0 && y < SEA - 6 && land < 0.02) region = 0;
        return new double[]{y, region};
    }

    static double regionHeight(int r, double x, double z) {
        switch (r) {
            case 1: { // Limgrave: green rolling hills, the Stormhill ridge in the north-west
                double y = 72 + n1.fbm(x / 260, z / 260, 5) * 14 + n2.fbm(x / 70, z / 70, 3) * 4;
                y += 38 * bump(x, z, -760, 640, 420, 170); // Stormhill
                y += 22 * bump(x, z, -430, 560, 140, 140); // castle bluff
                return y;
            }
            case 2: { // Liurnia: flooded basin, shallow lakes, academy island
                double y = 61 + n1.fbm(x / 210, z / 210, 5) * 9 + n2.fbm(x / 55, z / 55, 3) * 3;
                y += 32 * bump(x, z, -1300, -320, 190, 190);  // academy island
                y += 18 * bump(x, z, -1720, 120, 260, 220);   // western highland
                return y;
            }
            case 3: { // Caelid: scorched plain cut by canyons, an impact crater
                double y = 82 + n1.fbm(x / 240, z / 240, 4) * 8 + n2.fbm(x / 60, z / 60, 3) * 4;
                double rid = 1 - Math.abs(n3.fbm(x / 520, z / 520, 3));
                if (rid > 0.92) y -= 15 * Math.pow((rid - 0.92) / 0.08, 1.5); // dry canyons
                double d = dist(x, z, 1420, 1180);
                if (d < 230) y -= 34 * (1 - Math.pow(d / 230, 2));          // crater bowl
                else if (d < 300) y += 9 * (1 - Math.abs(d - 255) / 45);    // crater rim
                return y;
            }
            case 4: { // Altus Plateau: high tableland, the capital on a terrace
                double y = 152 + n1.fbm(x / 230, z / 230, 5) * 9 + n2.fbm(x / 60, z / 60, 3) * 3;
                y += 14 * bump(x, z, 140, -1180, 360, 300); // capital terrace (Erdtree site)
                return y;
            }
            case 5: { // Mt. Gelmir: volcano with a crater lake
                double d = dist(x, z, -1300, -1620);
                double cone = 92 + 168 * Math.pow(Math.max(0, 1 - d / 700), 1.4);
                cone += (1 - Math.abs(n3.fbm(x / 160, z / 160, 4))) * 18 - 9;
                if (d < 110) cone = Math.min(cone, 205 + d * 0.3);
                return cone;
            }
            case 6: { // Mountaintops: jagged snowy peaks
                double rid = 1 - Math.abs(n3.fbm(x / 320, z / 320, 5));
                return 175 + rid * rid * 105 + n2.fbm(x / 70, z / 70, 3) * 6;
            }
            case 7: { // Haligtree: frozen highland around the great tree
                double y = 128 + n1.fbm(x / 200, z / 200, 4) * 12;
                y += 16 * bump(x, z, 0, -2500, 200, 200);
                return y;
            }
            default:
                return SEA;
        }
    }

    /** Walkable ramps through the Altus cliffs. */
    static double ramps(double x, double z, double y) {
        y = ramp(x, z, y, -820, -620, -430, -880, 9);   // Liurnia -> Altus (lift-like ascent)
        y = ramp(x, z, y, 520, 120, 420, -420, 9);      // Caelid / Limgrave side -> Altus
        y = ramp(x, z, y, -280, -1980, -120, -1640, 7); // Altus -> Haligtree pass
        return y;
    }

    static double ramp(double x, double z, double y, double ax, double az, double bx, double bz, double halfW) {
        double vx = bx - ax, vz = bz - az, len2 = vx * vx + vz * vz;
        double t = Math.max(0, Math.min(1, ((x - ax) * vx + (z - az) * vz) / len2));
        double px = ax + t * vx, pz = az + t * vz;
        double d = dist(x, z, px, pz);
        if (d > halfW * 3) return y;
        double ya = sampleLandAt(ax, az), yb = sampleLandAt(bx, bz);
        double target = ya + (yb - ya) * t;
        double w = 1 - smooth(halfW, halfW * 3, d);
        return y + (target - y) * w;
    }

    static double sampleLandAt(double x, double z) {
        // Height of the nearest region's terrain at a ramp end (no recursion into ramps).
        double best = -9; int bestR = 1;
        for (int r = 1; r <= 7; r++) {
            double[] e = ELLIPSES[r];
            double dx = (x - e[0]) / e[2], dz = (z - e[1]) / e[3];
            double s = 1.0 - Math.sqrt(dx * dx + dz * dz);
            if (s > best) { best = s; bestR = r; }
        }
        return regionHeight(bestR, x, z);
    }

    static double bump(double x, double z, double cx, double cz, double rx, double rz) {
        double dx = (x - cx) / rx, dz = (z - cz) / rz;
        double d = Math.sqrt(dx * dx + dz * dz);
        return d >= 1 ? 0 : smooth(0, 1, 1 - d);
    }

    static double dist(double x, double z, double cx, double cz) { return Math.hypot(x - cx, z - cz); }

    static double smooth(double a, double b, double v) {
        double t = Math.max(0, Math.min(1, (v - a) / (b - a)));
        return t * t * (3 - 2 * t);
    }

    static double[][] blur(double[][] h, int r) {
        double[][] o = new double[SIZE][SIZE];
        for (int z = 0; z < SIZE; z++) for (int x = 0; x < SIZE; x++) {
            double s = 0; int n = 0;
            for (int dz = -r; dz <= r; dz++) for (int dx = -r; dx <= r; dx++) {
                int xx = x + dx, zz = z + dz;
                if (xx < 0 || zz < 0 || xx >= SIZE || zz >= SIZE) continue;
                s += h[zz][xx]; n++;
            }
            o[z][x] = s / n;
        }
        return o;
    }

    static BufferedImage preview(double[][] h, int[][] reg) {
        int[][] col = {
                {40, 80, 150}, {110, 160, 70}, {90, 120, 170}, {170, 70, 50}, {205, 175, 70},
                {110, 70, 60}, {225, 230, 240}, {200, 200, 215}, {225, 220, 200}};
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
        for (int z = 0; z < SIZE; z++) for (int x = 0; x < SIZE; x++) {
            double y = h[z][x];
            int[] c = col[reg[z][x]];
            double r, g, b;
            if (y < SEA) {
                double d = Math.min(1, (SEA - y) / 35.0);
                r = 70 - 40 * d; g = 130 - 60 * d; b = 200 - 50 * d;
            } else {
                double hx = h[z][Math.min(SIZE - 1, x + 1)] - h[z][Math.max(0, x - 1)];
                double hz = h[Math.min(SIZE - 1, z + 1)][x] - h[Math.max(0, z - 1)][x];
                double shade = Math.max(0.35, Math.min(1.35, 1.0 - (hx + hz) * 0.06));
                double lift = 0.75 + Math.min(0.45, (y - SEA) / 400.0);
                r = c[0] * shade * lift; g = c[1] * shade * lift; b = c[2] * shade * lift;
            }
            img.setRGB(x, z, (clamp(r) << 16) | (clamp(g) << 8) | clamp(b));
        }
        return img;
    }

    static int clamp(double v) { return (int) Math.max(0, Math.min(255, v)); }

    /** Small seeded Perlin noise with fBm. */
    static final class Noise {
        final int[] p = new int[512];

        Noise(long seed) {
            int[] perm = new int[256];
            for (int i = 0; i < 256; i++) perm[i] = i;
            java.util.Random rnd = new java.util.Random(seed);
            for (int i = 255; i > 0; i--) { int j = rnd.nextInt(i + 1); int t = perm[i]; perm[i] = perm[j]; perm[j] = t; }
            for (int i = 0; i < 512; i++) p[i] = perm[i & 255];
        }

        double fbm(double x, double z, int oct) {
            double s = 0, a = 1, f = 1, norm = 0;
            for (int i = 0; i < oct; i++) { s += a * noise(x * f, z * f); norm += a; a *= 0.5; f *= 2.03; }
            return s / norm;
        }

        double noise(double x, double z) {
            int xi = (int) Math.floor(x), zi = (int) Math.floor(z);
            double xf = x - xi, zf = z - zi;
            xi &= 255; zi &= 255;
            double u = fade(xf), v = fade(zf);
            int aa = p[p[xi] + zi], ab = p[p[xi] + zi + 1], ba = p[p[xi + 1] + zi], bb = p[p[xi + 1] + zi + 1];
            double x1 = lerp(grad(aa, xf, zf), grad(ba, xf - 1, zf), u);
            double x2 = lerp(grad(ab, xf, zf - 1), grad(bb, xf - 1, zf - 1), u);
            return lerp(x1, x2, v) * 1.4;
        }

        static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
        static double lerp(double a, double b, double t) { return a + (b - a) * t; }
        static double grad(int h, double x, double z) {
            switch (h & 7) {
                case 0: return x + z; case 1: return -x + z; case 2: return x - z; case 3: return -x - z;
                case 4: return x; case 5: return -x; case 6: return z; default: return -z;
            }
        }
    }
}
