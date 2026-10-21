package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleDetails {

    private CastleDetails() {}

    private static final int WALL_X = 58;
    private static final int WALL_Z = 72;

    private static final int WALL_HEIGHT = 27;

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildWallButtresses(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildWallTurrets(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildIceSpikes(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildRuinedSections(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildFrozenWindows(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildCourtyardCrystals(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // КОНТРФОРСЫ
    // =========================================================

    private static void buildWallButtresses(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Северная и южная стены.
         */
        for (int offset = -45; offset <= 45; offset += 15) {

            buildButtressZ(
                    level,
                    centerX + offset,
                    baseY,
                    centerZ - WALL_Z,
                    -1,
                    chunkMinX,
                    chunkMinZ
            );

            /*
             * Около главных ворот оставляем пространство.
             */
            if (Math.abs(offset) > 12) {

                buildButtressZ(
                        level,
                        centerX + offset,
                        baseY,
                        centerZ + WALL_Z,
                        1,
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Западная и восточная стены.
         */
        for (int offset = -55; offset <= 55; offset += 15) {

            buildButtressX(
                    level,
                    centerX - WALL_X,
                    baseY,
                    centerZ + offset,
                    -1,
                    chunkMinX,
                    chunkMinZ
            );

            buildButtressX(
                    level,
                    centerX + WALL_X,
                    baseY,
                    centerZ + offset,
                    1,
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    private static void buildButtressZ(
            ServerLevel level,
            int x,
            int baseY,
            int wallZ,
            int direction,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Внизу контрфорс выступает на 7 блоков,
         * наверху постепенно сужается.
         */
        for (int dy = 1; dy <= WALL_HEIGHT + 5; dy++) {

            int depth;

            if (dy <= 8) {
                depth = 7;
            } else if (dy <= 16) {
                depth = 5;
            } else if (dy <= 24) {
                depth = 3;
            } else {
                depth = 2;
            }

            for (int d = 0; d <= depth; d++) {
                for (int width = -1; width <= 1; width++) {

                    int worldX = x + width;
                    int worldZ = wallZ + direction * d;

                    if (!insideChunk(
                            worldX,
                            worldZ,
                            chunkMinX,
                            chunkMinZ
                    )) {
                        continue;
                    }

                    level.setBlock(
                            new BlockPos(
                                    worldX,
                                    baseY + dy,
                                    worldZ
                            ),
                            detailStone(
                                    worldX,
                                    baseY + dy,
                                    worldZ
                            ),
                            2
                    );
                }
            }
        }
    }


    private static void buildButtressX(
            ServerLevel level,
            int wallX,
            int baseY,
            int z,
            int direction,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dy = 1; dy <= WALL_HEIGHT + 5; dy++) {

            int depth;

            if (dy <= 8) {
                depth = 7;
            } else if (dy <= 16) {
                depth = 5;
            } else if (dy <= 24) {
                depth = 3;
            } else {
                depth = 2;
            }

            for (int d = 0; d <= depth; d++) {
                for (int width = -1; width <= 1; width++) {

                    int worldX = wallX + direction * d;
                    int worldZ = z + width;

                    if (!insideChunk(
                            worldX,
                            worldZ,
                            chunkMinX,
                            chunkMinZ
                    )) {
                        continue;
                    }

                    level.setBlock(
                            new BlockPos(
                                    worldX,
                                    baseY + dy,
                                    worldZ
                            ),
                            detailStone(
                                    worldX,
                                    baseY + dy,
                                    worldZ
                            ),
                            2
                    );
                }
            }
        }
    }


    // =========================================================
    // МАЛЕНЬКИЕ БАШНИ НА СТЕНАХ
    // =========================================================

    private static void buildWallTurrets(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Северная стена.
         */
        buildTurret(
                level,
                centerX - 28,
                baseY,
                centerZ - WALL_Z,
                chunkMinX,
                chunkMinZ
        );

        buildTurret(
                level,
                centerX + 28,
                baseY,
                centerZ - WALL_Z,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Южная стена около ворот.
         */
        buildTurret(
                level,
                centerX - 35,
                baseY,
                centerZ + WALL_Z,
                chunkMinX,
                chunkMinZ
        );

        buildTurret(
                level,
                centerX + 35,
                baseY,
                centerZ + WALL_Z,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Боковые стены.
         */
        buildTurret(
                level,
                centerX - WALL_X,
                baseY,
                centerZ - 30,
                chunkMinX,
                chunkMinZ
        );

        buildTurret(
                level,
                centerX - WALL_X,
                baseY,
                centerZ + 30,
                chunkMinX,
                chunkMinZ
        );

        buildTurret(
                level,
                centerX + WALL_X,
                baseY,
                centerZ - 30,
                chunkMinX,
                chunkMinZ
        );

        buildTurret(
                level,
                centerX + WALL_X,
                baseY,
                centerZ + 30,
                chunkMinX,
                chunkMinZ
        );
    }


    private static void buildTurret(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        int radius = 4;
        int height = 36;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {

                double distance =
                        Math.sqrt(dx * dx + dz * dz);

                /*
                 * Полая круглая башенка.
                 */
                if (distance > radius
                        || distance < radius - 1.7) {
                    continue;
                }

                int x = cx + dx;
                int z = cz + dz;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                for (int dy = 1; dy <= height; dy++) {

                    BlockState state;

                    if (dy > 29) {
                        state =
                                Blocks.PACKED_ICE
                                        .defaultBlockState();
                    } else {
                        state =
                                detailStone(
                                        x,
                                        baseY + dy,
                                        z
                                );
                    }

                    level.setBlock(
                            new BlockPos(
                                    x,
                                    baseY + dy,
                                    z
                            ),
                            state,
                            2
                    );
                }
            }
        }

        /*
         * Острая ледяная крыша.
         */
        for (int layer = 0; layer < 14; layer++) {

            int radiusAtLayer =
                    Math.max(
                            0,
                            5 - layer / 3
                    );

            int y =
                    baseY + height + layer + 1;

            for (int dx = -radiusAtLayer;
                 dx <= radiusAtLayer;
                 dx++) {

                for (int dz = -radiusAtLayer;
                     dz <= radiusAtLayer;
                     dz++) {

                    if (dx * dx + dz * dz
                            > radiusAtLayer * radiusAtLayer) {
                        continue;
                    }

                    int x = cx + dx;
                    int z = cz + dz;

                    if (!insideChunk(
                            x,
                            z,
                            chunkMinX,
                            chunkMinZ
                    )) {
                        continue;
                    }

                    level.setBlock(
                            new BlockPos(
                                    x,
                                    y,
                                    z
                            ),
                            layer > 8
                                    ? Blocks.BLUE_ICE.defaultBlockState()
                                    : Blocks.PACKED_ICE.defaultBlockState(),
                            2
                    );
                }
            }
        }

        /*
         * Самый кончик.
         */
        if (insideChunk(
                cx,
                cz,
                chunkMinX,
                chunkMinZ
        )) {

            for (int dy = 15; dy <= 19; dy++) {

                level.setBlock(
                        new BlockPos(
                                cx,
                                baseY + height + dy,
                                cz
                        ),
                        Blocks.BLUE_ICE
                                .defaultBlockState(),
                        2
                );
            }
        }
    }


    // =========================================================
    // ЛЕДЯНЫЕ ШПИЛИ НА СТЕНАХ
    // =========================================================

    private static void buildIceSpikes(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Север / юг.
         */
        for (int offset = -50; offset <= 50; offset += 10) {

            buildSpike(
                    level,
                    centerX + offset,
                    baseY + WALL_HEIGHT + 2,
                    centerZ - WALL_Z,
                    4 + Math.floorMod(offset, 5),
                    chunkMinX,
                    chunkMinZ
            );

            if (Math.abs(offset) > 12) {

                buildSpike(
                        level,
                        centerX + offset,
                        baseY + WALL_HEIGHT + 2,
                        centerZ + WALL_Z,
                        4 + Math.floorMod(offset + 3, 6),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Боковые стены.
         */
        for (int offset = -60; offset <= 60; offset += 12) {

            buildSpike(
                    level,
                    centerX - WALL_X,
                    baseY + WALL_HEIGHT + 2,
                    centerZ + offset,
                    5 + Math.floorMod(offset, 5),
                    chunkMinX,
                    chunkMinZ
            );

            buildSpike(
                    level,
                    centerX + WALL_X,
                    baseY + WALL_HEIGHT + 2,
                    centerZ + offset,
                    5 + Math.floorMod(offset + 2, 5),
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    private static void buildSpike(
            ServerLevel level,
            int x,
            int startY,
            int z,
            int height,
            int chunkMinX,
            int chunkMinZ
    ) {

        if (!insideChunk(
                x,
                z,
                chunkMinX,
                chunkMinZ
        )) {
            return;
        }

        for (int dy = 0; dy < height; dy++) {

            BlockState state =
                    dy >= height / 2
                            ? Blocks.BLUE_ICE.defaultBlockState()
                            : Blocks.PACKED_ICE.defaultBlockState();

            level.setBlock(
                    new BlockPos(
                            x,
                            startY + dy,
                            z
                    ),
                    state,
                    2
            );
        }
    }


    // =========================================================
    // РАЗРУШЕННЫЕ УЧАСТКИ
    // =========================================================

    private static void buildRuinedSections(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Один большой пролом на западной стене.
         */
        int wallX = centerX - WALL_X;

        int ruinCenterZ =
                centerZ + 8;

        for (int dz = -7; dz <= 7; dz++) {

            for (int depth = 0; depth <= 3; depth++) {

                int x =
                        wallX + depth;

                int z =
                        ruinCenterZ + dz;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                /*
                 * Пролом имеет неровную форму.
                 */
                int maxHeight =
                        16
                                - Math.abs(dz)
                                + Math.floorMod(
                                dz * dz + 7,
                                5
                        );

                for (int dy = 8;
                     dy <= maxHeight;
                     dy++) {

                    level.setBlock(
                            new BlockPos(
                                    x,
                                    baseY + dy,
                                    z
                            ),
                            Blocks.AIR
                                    .defaultBlockState(),
                            2
                    );
                }
            }
        }

        /*
         * Лёд внутри пролома.
         */
        for (int dz = -5; dz <= 5; dz += 2) {

            int z =
                    ruinCenterZ + dz;

            buildSpike(
                    level,
                    wallX + 1,
                    baseY + 4,
                    z,
                    4 + Math.floorMod(dz + 9, 7),
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    // =========================================================
    // АРОЧНЫЕ ЛЕДЯНЫЕ ОКНА
    // =========================================================

    private static void buildFrozenWindows(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Окна по северной стене.
         */
        for (int offset = -42; offset <= 42; offset += 14) {

            buildWindowZ(
                    level,
                    centerX + offset,
                    baseY,
                    centerZ - WALL_Z + 1,
                    chunkMinX,
                    chunkMinZ
            );
        }

        /*
         * Боковые стены.
         */
        for (int offset = -48; offset <= 48; offset += 16) {

            buildWindowX(
                    level,
                    centerX - WALL_X + 1,
                    baseY,
                    centerZ + offset,
                    chunkMinX,
                    chunkMinZ
            );

            buildWindowX(
                    level,
                    centerX + WALL_X - 1,
                    baseY,
                    centerZ + offset,
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    private static void buildWindowZ(
            ServerLevel level,
            int cx,
            int baseY,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dx = -2; dx <= 2; dx++) {

            int x = cx + dx;

            if (!insideChunk(
                    x,
                    z,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            for (int dy = 10; dy <= 20; dy++) {

                /*
                 * Сужение арки наверху.
                 */
                if (dy >= 18
                        && Math.abs(dx) >= 2) {
                    continue;
                }

                if (dy >= 20
                        && Math.abs(dx) >= 1) {
                    continue;
                }

                level.setBlock(
                        new BlockPos(
                                x,
                                baseY + dy,
                                z
                        ),
                        Blocks.BLUE_STAINED_GLASS
                                .defaultBlockState(),
                        2
                );
            }
        }
    }


    private static void buildWindowX(
            ServerLevel level,
            int x,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dz = -2; dz <= 2; dz++) {

            int z = cz + dz;

            if (!insideChunk(
                    x,
                    z,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            for (int dy = 10; dy <= 20; dy++) {

                if (dy >= 18
                        && Math.abs(dz) >= 2) {
                    continue;
                }

                if (dy >= 20
                        && Math.abs(dz) >= 1) {
                    continue;
                }

                level.setBlock(
                        new BlockPos(
                                x,
                                baseY + dy,
                                z
                        ),
                        Blocks.BLUE_STAINED_GLASS
                                .defaultBlockState(),
                        2
                );
            }
        }
    }


    // =========================================================
    // КРИСТАЛЛЫ ВО ДВОРЕ
    // =========================================================

    private static void buildCourtyardCrystals(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int[][] positions = {

                {-45, 45},
                {45, 45},

                {-40, 15},
                {40, 15},

                {-48, -15},
                {48, -15},

                {-32, 35},
                {32, 35}
        };

        for (int[] pos : positions) {

            int x =
                    centerX + pos[0];

            int z =
                    centerZ + pos[1];

            int height =
                    5
                            + Math.floorMod(
                            pos[0] + pos[1],
                            7
                    );

            buildCrystalCluster(
                    level,
                    x,
                    baseY + 2,
                    z,
                    height,
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    private static void buildCrystalCluster(
            ServerLevel level,
            int x,
            int startY,
            int z,
            int height,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildSpike(
                level,
                x,
                startY,
                z,
                height,
                chunkMinX,
                chunkMinZ
        );

        buildSpike(
                level,
                x + 1,
                startY,
                z,
                Math.max(
                        3,
                        height - 3
                ),
                chunkMinX,
                chunkMinZ
        );

        buildSpike(
                level,
                x - 1,
                startY,
                z + 1,
                Math.max(
                        2,
                        height - 4
                ),
                chunkMinX,
                chunkMinZ
        );

        buildSpike(
                level,
                x,
                startY,
                z - 1,
                Math.max(
                        3,
                        height - 2
                ),
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // БЛОКИ
    // =========================================================

    private static BlockState detailStone(
            int x,
            int y,
            int z
    ) {

        long h =
                hash(
                        x,
                        y,
                        z
                );

        int value =
                Math.floorMod(
                        h,
                        100
                );

        if (value < 8) {

            return Blocks.CRACKED_DEEPSLATE_BRICKS
                    .defaultBlockState();
        }

        if (value < 13) {

            return Blocks.PACKED_ICE
                    .defaultBlockState();
        }

        if (value < 20) {

            return Blocks.POLISHED_DEEPSLATE
                    .defaultBlockState();
        }

        return Blocks.DEEPSLATE_BRICKS
                .defaultBlockState();
    }


    // =========================================================
    // UTILS
    // =========================================================

    private static boolean insideChunk(
            int x,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        return x >= chunkMinX
                && x <= chunkMinX + 15
                && z >= chunkMinZ
                && z <= chunkMinZ + 15;
    }


    private static long hash(
            int x,
            int y,
            int z
    ) {

        long h =
                (long) x * 341873128712L
                        + (long) y * 668265263L
                        + (long) z * 132897987541L;

        h ^= h >>> 33;

        h *=
                0xff51afd7ed558ccdL;

        h ^= h >>> 33;

        h *=
                0xc4ceb9fe1a85ec53L;

        return h ^ (h >>> 33);
    }
}