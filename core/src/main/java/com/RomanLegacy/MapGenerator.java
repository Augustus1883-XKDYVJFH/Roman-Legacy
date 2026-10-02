package com.RomanLegacy;

import java.util.Random;

public class MapGenerator {

    public static final int WATER = 0;
    public static final int SHORE = 1;
    public static final int PLAIN = 2;
    public static final int FOREST = 3;
    public static final int HILL = 4;
    public static final int MOUNTAIN = 5;
    public static final int PEAK = 6;

    private final int[] perm = new int[512];

    public MapGenerator() {
        this(new Random().nextLong());
    }

    public MapGenerator(long seed) {
        initPerlin(seed);
    }

    public int[][] generate(int width, int height, double baseFreq) {
        int[][] map = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double nx = (double) x / Math.max(1, width - 1);
                double ny = (double) y / Math.max(1, height - 1);

                // Domain warping rompe i bordi concentrici e la simmetria del
                // vecchio mascheramento radiale. Le coordinate restano corrette
                // anche quando la mappa non è quadrata.
                double warpScale = Math.max(0.1, baseFreq * 0.65);
                double warpX = perlin(nx * warpScale + 17.3, ny * warpScale - 8.1) * 0.16;
                double warpY = perlin(nx * warpScale - 31.7, ny * warpScale + 22.4) * 0.16;
                double sx = nx + warpX;
                double sy = ny + warpY;

                double dx = (sx - 0.5) * 2.0;
                double dy = (sy - 0.5) * 2.0;
                // Un bacino ovale e deformato produce coste meno circolari e
                // meno simili ai bordi della griglia.
                double edgeNoise = perlin(sx * warpScale + 53.2, sy * warpScale - 11.8) * 0.18;
                double landMask = 1.0 - Math.sqrt(dx * dx * 0.82 + dy * dy * 1.12) + edgeNoise;

                double n = fbm(sx * baseFreq, sy * baseFreq, 6, 2.0, 0.5);
                // Un secondo fBm, a scala più fine e con offset indipendente,
                // varia le altitudini interne senza intaccare la linea costiera.
                // Evita le fasce allungate prodotte da una singola cresta Perlin.
                double detail = fbm(sx * baseFreq * 1.7 + 13.4,
                        sy * baseFreq * 1.7 - 7.2, 4, 2.0, 0.5);
                double value = n * 0.82 + landMask * 0.70 + detail * 0.20;
                int terrain;
                if (value < 0.15)
                    terrain = WATER;
                else if (value < 0.23)
                    terrain = SHORE;
                else if (value < 0.42)
                    terrain = PLAIN;
                else if (value < 0.52)
                    terrain = FOREST;
                else if (value < 0.62)
                    terrain = HILL;
                else if (value < 0.79)
                    terrain = MOUNTAIN;
                else
                    terrain = PEAK;
                map[y][x] = terrain;
            }
        }
        return map;
    }

    private void initPerlin(long seed) {
        Random r = new Random(seed);
        int[] p = new int[256];
        for (int i = 0; i < 256; i++)
            p[i] = i;
        for (int i = 255; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int tmp = p[i];
            p[i] = p[j];
            p[j] = tmp;
        }
        for (int i = 0; i < 512; i++)
            perm[i] = p[i & 255];
    }

    private double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double a, double b, double t) {
        return a + t * (b - a);
    }

    private double grad(int hash, double x, double y) {
        switch (hash & 7) {
            case 0:
                return x;
            case 1:
                return -x;
            case 2:
                return y;
            case 3:
                return -y;
            case 4:
                return (x + y) * 0.70710678118;
            case 5:
                return (x - y) * 0.70710678118;
            case 6:
                return (-x + y) * 0.70710678118;
            default:
                return (-x - y) * 0.70710678118;
        }
    }

    private double perlin(double x, double y) {
        int xi = (int) Math.floor(x) & 255, yi = (int) Math.floor(y) & 255;
        double xf = x - Math.floor(x), yf = y - Math.floor(y);
        double u = fade(xf), v = fade(yf);
        int aa = perm[perm[xi] + yi], ab = perm[perm[xi] + yi + 1];
        int ba = perm[perm[xi + 1] + yi], bb = perm[perm[xi + 1] + yi + 1];
        return lerp(
                lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u),
                lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u), v);
    }

    private double fbm(double x, double y, int oct, double lac, double gain) {
        double val = 0, amp = 0.5, freq = 1, max = 0;
        for (int i = 0; i < oct; i++) {
            val += perlin(x * freq, y * freq) * amp;
            max += amp;
            amp *= gain;
            freq *= lac;
        }
        return val / max;
    }
}
