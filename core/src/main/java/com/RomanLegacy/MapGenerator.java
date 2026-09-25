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
                double nx = (double) x / width;
                double ny = (double) y / height;
                double dx = (nx - 0.5) * 2.0;
                double dy = (ny - 0.5) * 2.0;
                double distMask = 1.0 - Math.sqrt(dx * dx * 1.1 + dy * dy * 1.1);
                double n = fbm(nx * baseFreq, ny * baseFreq, 6, 2.0, 0.5);
                double value = n * 0.9 + distMask * 0.7;
                int terrain;
                if (value < 0.10)
                    terrain = WATER;
                else if (value < 0.18)
                    terrain = SHORE;
                else if (value < 0.35)
                    terrain = PLAIN;
                else if (value < 0.52)
                    terrain = FOREST;
                else if (value < 0.64)
                    terrain = HILL;
                else if (value < 0.76)
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
        int h = hash & 7;
        double u = h < 4 ? x : y, v = h < 4 ? y : x;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
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