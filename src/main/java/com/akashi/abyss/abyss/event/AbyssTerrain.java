package com.akashi.abyss.abyss.event;

/** Чистая функция рельефа Биома 1. Формулы скопированы из AbyssBiome1ChunkHandler. */
public final class AbyssTerrain {
    private AbyssTerrain() {}

    public static final int FLOOR_Y = -450;
    public static final int CEILING_Y = -301;
    public static final int SEA_LEVEL = FLOOR_Y + 20;
    private static final int MAX_HILL_HEIGHT = 40;
    private static final double SEA_THRESHOLD = 0.30;
    private static final double LAKE_THRESHOLD = 0.24;
    private static final double RIVER_HALF_WIDTH = 0.03;
    private static final double SHORE_MARGIN = 0.08;
    private static final double RIVER_MARGIN = 0.04;
    private static final int MAX_WATER_DEPTH = 60;
    private static final double VOLCANIC_THRESHOLD = 0.66;
    private static final double POISON_THRESHOLD = 0.60;
    private static final double LAVA_THRESHOLD = 0.62;
    private static final int MAX_CEILING_ROUGHNESS = 26;
    private static final int CEILING_DETAIL_ROUGHNESS = 8;
    private static final double STALACTITE_CHANCE = 0.03;

    public record Sample(int floorTop, boolean water, boolean lava) {}

    public static Sample sample(int x, int z) {
        double sea = noise01(x, z, 5000, 220);
        double lake = noise01(x, z, 6000, 64);
        double river = Math.abs(noise01(x, z, 7000, 110) - 0.5);

        int waterDepth = 0;

        // Моря: от 20 до 60 блоков
        if (sea < SEA_THRESHOLD) {
            double depthFactor = clamp(
                    (SEA_THRESHOLD - sea) / SEA_THRESHOLD,
                    0.0, 1.0
            );

            waterDepth = Math.max(
                    waterDepth,
                    20 + (int) Math.round(depthFactor * 40)
            );
        }

        // Озёра: от 10 до 50 блоков
        if (lake < LAKE_THRESHOLD) {
            double depthFactor = clamp(
                    (LAKE_THRESHOLD - lake) / LAKE_THRESHOLD,
                    0.0, 1.0
            );

            waterDepth = Math.max(
                    waterDepth,
                    10 + (int) Math.round(depthFactor * 40)
            );
        }

        // Реки: от 5 до 30 блоков
        if (river < RIVER_HALF_WIDTH) {
            double depthFactor = clamp(
                    (RIVER_HALF_WIDTH - river) / RIVER_HALF_WIDTH,
                    0.0, 1.0
            );

            waterDepth = Math.max(
                    waterDepth,
                    5 + (int) Math.round(depthFactor * 25)
            );
        }

        waterDepth = Math.min(waterDepth, MAX_WATER_DEPTH);
        boolean isWater = waterDepth > 0;

        double landness = Math.min(
                Math.min(
                        (sea - SEA_THRESHOLD) / SHORE_MARGIN,
                        (lake - LAKE_THRESHOLD) / SHORE_MARGIN
                ),
                (river - RIVER_HALF_WIDTH) / RIVER_MARGIN
        );

        landness = clamp(landness, 0.0, 1.0);

        double region = noise01(x, z, 3000, 80);
        double hillFactor = clamp((region - 0.35) / 0.45, 0.0, 1.0);
        double baseNoise = noise01(x, z, 0, 12);

        int hillHeight = (int) Math.round(
                baseNoise * MAX_HILL_HEIGHT * hillFactor * landness
        );

        boolean volcanic =
                !isWater
                        && landness >= 0.5
                        && noise01(x, z, 9000, 150) > VOLCANIC_THRESHOLD;

        boolean lava =
                volcanic
                        && noise01(x, z, 15000, 18) > LAVA_THRESHOLD;

        int landTop = SEA_LEVEL + 1 + hillHeight;

        int floorTop;

        if (isWater) {
            floorTop = SEA_LEVEL - waterDepth;
        } else if (lava) {
            floorTop = landTop - 3;
        } else {
            floorTop = landTop;
        }

        return new Sample(floorTop, isWater, lava);
    }

    /** Нижняя граница потолка (та же формула, что и в AbyssBiome1ChunkHandler). */
    public static int ceilingBottom(int x, int z) {
        double ceilingBase = noise01(x, z, 1000, 40);
        double ceilingDetail = noise01(x, z, 1500, 7);
        int dip = (int) Math.round(ceilingBase * MAX_CEILING_ROUGHNESS + ceilingDetail * CEILING_DETAIL_ROUGHNESS);
        if (noise01(x, z, 1800, 1) < STALACTITE_CHANCE) {
            dip += 4 + (int) (noise01(x, z, 1900, 1) * 8);
        }
        return CEILING_Y - dip;
    }

    public static boolean isPoisonZone(int x, int z) {
        double sea = noise01(x, z, 5000, 220);
        double lake = noise01(x, z, 6000, 64);
        double river = Math.abs(noise01(x, z, 7000, 110) - 0.5);
        boolean isWater = sea < SEA_THRESHOLD || lake < LAKE_THRESHOLD || river < RIVER_HALF_WIDTH;
        if (isWater) return false;

        double landness = Math.min(
                Math.min((sea - SEA_THRESHOLD) / SHORE_MARGIN, (lake - LAKE_THRESHOLD) / SHORE_MARGIN),
                (river - RIVER_HALF_WIDTH) / RIVER_MARGIN);
        landness = clamp(landness, 0.0, 1.0);

        double volcanicNoise = noise01(x, z, 9000, 150);
        double poisonNoise = noise01(x, z, 12000, 90);
        boolean volcanic = landness >= 0.5 && volcanicNoise > VOLCANIC_THRESHOLD;
        return !volcanic && poisonNoise > POISON_THRESHOLD;
    }

    private static double noise01(int worldX, int worldZ, int seedOffset, int scale) {
        double fx = worldX / (double) scale, fz = worldZ / (double) scale;
        int gx0 = (int) Math.floor(fx), gz0 = (int) Math.floor(fz);
        double tx = fx - gx0, tz = fz - gz0;
        double v00 = latticeNoise(gx0, gz0, seedOffset);
        double v10 = latticeNoise(gx0 + 1, gz0, seedOffset);
        double v01 = latticeNoise(gx0, gz0 + 1, seedOffset);
        double v11 = latticeNoise(gx0 + 1, gz0 + 1, seedOffset);
        return lerp(tz, lerp(tx, v00, v10), lerp(tx, v01, v11));
    }

    private static double latticeNoise(int gx, int gz, int seedOffset) {
        long hash = (long) gx * 341873128712L + (long) gz * 132897987541L + seedOffset * 7919L;
        hash = (hash ^ (hash >>> 33)) * 0xff51afd7ed558ccdL;
        hash = (hash ^ (hash >>> 33)) * 0xc4ceb9fe1a85ec53L;
        hash = hash ^ (hash >>> 33);
        return (hash & 0xFFFFFF) / (double) 0xFFFFFF;
    }

    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}