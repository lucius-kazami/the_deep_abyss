package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;

public final class CastleDecorations {

    private CastleDecorations() {}

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildEntranceChains(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildMainHallLights(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildFrozenRemains(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildSecretRooms(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildIceGrowths(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ЦЕПИ У ГЛАВНЫХ ВОРОТ
    // =========================================================

    private static void buildEntranceChains(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int gateZ =
                centerZ
                        + CastleFoundation.HALF_LENGTH
                        - 10;

        int[] offsets = {
                -7,
                -4,
                4,
                7
        };

        for (int offset : offsets) {

            int x =
                    centerX + offset;

            /*
             * Цепи свисают с верхней части ворот.
             */
            int length =
                    8
                            + Math.floorMod(
                            offset * offset,
                            6
                    );

            for (int dy = 0; dy < length; dy++) {

                setIfInside(
                        level,
                        x,
                        baseY + 27 - dy,
                        gateZ - 1,
                        Blocks.IRON_BARS.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }
    }


    // =========================================================
    // ОСВЕЩЕНИЕ
    // =========================================================

    private static void buildMainHallLights(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Свет вдоль главного коридора.
         */
        for (int z = centerZ + 5;
             z >= centerZ - 55;
             z -= 10) {

            buildHangingLight(
                    level,
                    centerX - 5,
                    baseY + 10,
                    z,
                    chunkMinX,
                    chunkMinZ
            );

            buildHangingLight(
                    level,
                    centerX + 5,
                    baseY + 10,
                    z,
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    private static void buildHangingLight(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Цепь.
         */
        for (int dy = 0; dy < 3; dy++) {

            setIfInside(
                    level,
                    x,
                    y - dy,
                    z,
                    Blocks.IRON_BARS.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }

        /*
         * Источник света.
         *
         * Пока используем sea lantern.
         * Позже можем заменить на собственный
         * ледяной светильник Abyss.
         */
        setIfInside(
                level,
                x,
                y - 3,
                z,
                Blocks.SEA_LANTERN.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        /*
         * Ледяная оболочка.
         */
        setIfInside(
                level,
                x - 1,
                y - 3,
                z,
                Blocks.PACKED_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x + 1,
                y - 3,
                z,
                Blocks.PACKED_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ЗАМЁРЗШИЕ ОСТАНКИ
    // =========================================================

    private static void buildFrozenRemains(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Несколько маленьких "останков" древних жителей.
         *
         * Пока без сущностей/черепов — только окружение.
         */
        buildFrozenRemain(
                level,
                centerX - 18,
                baseY + 3,
                centerZ + 25,
                chunkMinX,
                chunkMinZ
        );

        buildFrozenRemain(
                level,
                centerX + 32,
                baseY + 3,
                centerZ + 35,
                chunkMinX,
                chunkMinZ
        );

        buildFrozenRemain(
                level,
                centerX - 40,
                baseY + 3,
                centerZ - 20,
                chunkMinX,
                chunkMinZ
        );

        buildFrozenRemain(
                level,
                centerX + 15,
                baseY + 3,
                centerZ - 48,
                chunkMinX,
                chunkMinZ
        );
    }


    private static void buildFrozenRemain(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        setIfInside(
                level,
                x,
                y,
                z,
                Blocks.BONE_BLOCK.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x,
                y + 1,
                z,
                Blocks.PACKED_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x + 1,
                y,
                z,
                Blocks.BLUE_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x - 1,
                y,
                z,
                Blocks.PACKED_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // СЕКРЕТНЫЕ КОМНАТЫ
    // =========================================================

    private static void buildSecretRooms(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildSecretRoom(
                level,
                centerX - 48,
                baseY,
                centerZ - 52,
                chunkMinX,
                chunkMinZ
        );

        buildSecretRoom(
                level,
                centerX + 48,
                baseY,
                centerZ + 42,
                chunkMinX,
                chunkMinZ
        );
    }


    private static void buildSecretRoom(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        int halfWidth = 5;
        int halfLength = 5;

        for (int dx = -halfWidth;
             dx <= halfWidth;
             dx++) {

            for (int dz = -halfLength;
                 dz <= halfLength;
                 dz++) {

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

                /*
                 * Пол.
                 */
                place(
                        level,
                        x,
                        baseY + 3,
                        z,
                        Blocks.DEEPSLATE_TILES.defaultBlockState()
                );

                boolean wall =
                        Math.abs(dx) == halfWidth
                                || Math.abs(dz) == halfLength;

                for (int dy = 4; dy <= 10; dy++) {

                    if (wall) {

                        place(
                                level,
                                x,
                                baseY + dy,
                                z,
                                secretStone(
                                        x,
                                        baseY + dy,
                                        z
                                )
                        );

                    } else {

                        place(
                                level,
                                x,
                                baseY + dy,
                                z,
                                Blocks.AIR.defaultBlockState()
                        );
                    }
                }
            }
        }

        /*
         * Тайный проход.
         *
         * Оставляем небольшой проём, скрытый льдом.
         */
        for (int dy = 4; dy <= 7; dy++) {

            setIfInside(
                    level,
                    cx,
                    baseY + dy,
                    cz + halfLength,
                    dy <= 6
                            ? Blocks.PACKED_ICE.defaultBlockState()
                            : Blocks.BLUE_ICE.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }

        /*
         * Свет внутри.
         */
        setIfInside(
                level,
                cx,
                baseY + 9,
                cz,
                Blocks.SEA_LANTERN.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ЛЕДЯНЫЕ НАРОСТЫ
    // =========================================================

    private static void buildIceGrowths(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int[][] positions = {

                {-52, 25},
                {-50, -25},

                {52, 12},
                {50, -35},

                {-30, 55},
                {30, 52},

                {-20, -55},
                {20, -57}
        };

        for (int[] p : positions) {

            int x =
                    centerX + p[0];

            int z =
                    centerZ + p[1];

            int height =
                    5
                            + Math.floorMod(
                            p[0] * 3 + p[1],
                            8
                    );

            buildIceGrowth(
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


    private static void buildIceGrowth(
            ServerLevel level,
            int x,
            int y,
            int z,
            int height,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Центральный кристалл.
         */
        for (int dy = 0; dy < height; dy++) {

            setIfInside(
                    level,
                    x,
                    y + dy,
                    z,
                    dy > height / 2
                            ? Blocks.BLUE_ICE.defaultBlockState()
                            : Blocks.PACKED_ICE.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }

        /*
         * Маленькие соседние кристаллы.
         */
        for (int dy = 0;
             dy < Math.max(2, height - 3);
             dy++) {

            setIfInside(
                    level,
                    x + 1,
                    y + dy,
                    z,
                    Blocks.BLUE_ICE.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }

        for (int dy = 0;
             dy < Math.max(2, height - 4);
             dy++) {

            setIfInside(
                    level,
                    x - 1,
                    y + dy,
                    z + 1,
                    Blocks.PACKED_ICE.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    // =========================================================
    // БЛОКИ
    // =========================================================

    private static net.minecraft.world.level.block.state.BlockState secretStone(
            int x,
            int y,
            int z
    ) {

        int value =
                Math.floorMod(
                        hash(x, y, z),
                        100
                );

        if (value < 15) {

            return Blocks.CRACKED_DEEPSLATE_BRICKS
                    .defaultBlockState();
        }

        if (value < 22) {

            return Blocks.PACKED_ICE
                    .defaultBlockState();
        }

        return Blocks.DEEPSLATE_BRICKS
                .defaultBlockState();
    }


    // =========================================================
    // UTILS
    // =========================================================

    private static void setIfInside(
            ServerLevel level,
            int x,
            int y,
            int z,
            net.minecraft.world.level.block.state.BlockState state,
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

        place(
                level,
                x,
                y,
                z,
                state
        );
    }


    private static void place(
            ServerLevel level,
            int x,
            int y,
            int z,
            net.minecraft.world.level.block.state.BlockState state
    ) {

        level.setBlock(
                new BlockPos(
                        x,
                        y,
                        z
                ),
                state,
                2
        );
    }


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
        h *= 0xff51afd7ed558ccdL;

        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;

        return h ^ (h >>> 33);
    }
}