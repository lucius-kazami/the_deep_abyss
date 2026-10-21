package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleEntrance {

    private CastleEntrance() {}

    private static final int GATE_HALF_WIDTH = 8;

    private static final int GATE_HEIGHT = 18;

    private static final int TOWER_RADIUS = 8;

    private static final int TOWER_HEIGHT = 42;

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Вход находится на южной стороне.
         */
        int entranceZ =
                centerZ
                        + CastleFoundation.HALF_LENGTH
                        - 10;

        buildGate(
                level,
                centerX,
                baseY,
                entranceZ,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Левая башня.
         */
        buildTower(
                level,
                centerX - 17,
                baseY,
                entranceZ,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Правая башня.
         */
        buildTower(
                level,
                centerX + 17,
                baseY,
                entranceZ,
                chunkMinX,
                chunkMinZ
        );

        buildBridgePath(
                level,
                centerX,
                baseY,
                entranceZ,
                chunkMinX,
                chunkMinZ
        );
    }

    // ==========================================================
    // ВОРОТА
    // ==========================================================

    private static void buildGate(
            ServerLevel level,
            int centerX,
            int baseY,
            int gateZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dx = -14; dx <= 14; dx++) {

            int worldX =
                    centerX + dx;

            if (!insideChunk(
                    worldX,
                    gateZ,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            for (int dy = 1; dy <= 30; dy++) {

                /*
                 * Центральный проход.
                 */
                boolean opening =
                        Math.abs(dx) <= GATE_HALF_WIDTH
                                &&
                                dy <= GATE_HEIGHT;

                if (opening) {
                    continue;
                }

                BlockState block;

                if (dy >= 25) {
                    block =
                            Blocks.BLUE_ICE
                                    .defaultBlockState();

                } else if (dy % 7 == 0) {
                    block =
                            Blocks.PACKED_ICE
                                    .defaultBlockState();

                } else {
                    block =
                            Blocks.DEEPSLATE_BRICKS
                                    .defaultBlockState();
                }

                level.setBlock(
                        new BlockPos(
                                worldX,
                                baseY + dy,
                                gateZ
                        ),
                        block,
                        2
                );
            }
        }

        /*
         * Ледяные зубья наверху.
         */
        for (int dx = -13; dx <= 13; dx += 4) {

            int worldX =
                    centerX + dx;

            if (!insideChunk(
                    worldX,
                    gateZ,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            int height =
                    4 + Math.abs(dx) % 4;

            for (int dy = 31;
                 dy <= 31 + height;
                 dy++) {

                level.setBlock(
                        new BlockPos(
                                worldX,
                                baseY + dy,
                                gateZ
                        ),
                        Blocks.BLUE_ICE
                                .defaultBlockState(),
                        2
                );
            }
        }

        /*
         * Решётка ворот.
         */
        for (int dx = -GATE_HALF_WIDTH;
             dx <= GATE_HALF_WIDTH;
             dx += 2) {

            int worldX =
                    centerX + dx;

            if (!insideChunk(
                    worldX,
                    gateZ,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            for (int dy = 1;
                 dy <= GATE_HEIGHT - 1;
                 dy++) {

                level.setBlock(
                        new BlockPos(
                                worldX,
                                baseY + dy,
                                gateZ
                        ),
                        Blocks.IRON_BARS
                                .defaultBlockState(),
                        2
                );
            }
        }
    }

    // ==========================================================
    // БАШНИ У ВОРОТ
    // ==========================================================

    private static void buildTower(
            ServerLevel level,
            int towerX,
            int baseY,
            int towerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dx = -TOWER_RADIUS;
             dx <= TOWER_RADIUS;
             dx++) {

            for (int dz = -TOWER_RADIUS;
                 dz <= TOWER_RADIUS;
                 dz++) {

                int distanceSquared =
                        dx * dx + dz * dz;

                /*
                 * Круглая башня.
                 */
                if (distanceSquared
                        > TOWER_RADIUS * TOWER_RADIUS) {
                    continue;
                }

                int worldX =
                        towerX + dx;

                int worldZ =
                        towerZ + dz;

                if (!insideChunk(
                        worldX,
                        worldZ,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                /*
                 * Строим только внешнюю оболочку.
                 */
                int innerRadius =
                        TOWER_RADIUS - 2;

                boolean shell =
                        distanceSquared
                                >= innerRadius * innerRadius;

                if (!shell) {
                    continue;
                }

                for (int dy = 1;
                     dy <= TOWER_HEIGHT;
                     dy++) {

                    BlockState state;

                    if (dy > 34) {
                        state =
                                Blocks.BLUE_ICE
                                        .defaultBlockState();

                    } else if (dy % 8 == 0) {
                        state =
                                Blocks.PACKED_ICE
                                        .defaultBlockState();

                    } else {
                        state =
                                Blocks.DEEPSLATE_BRICKS
                                        .defaultBlockState();
                    }

                    level.setBlock(
                            new BlockPos(
                                    worldX,
                                    baseY + dy,
                                    worldZ
                            ),
                            state,
                            2
                    );
                }
            }
        }

        buildTowerCrown(
                level,
                towerX,
                baseY + TOWER_HEIGHT,
                towerZ,
                chunkMinX,
                chunkMinZ
        );
    }

    // ==========================================================
    // ЛЕДЯНАЯ КОРОНА БАШНИ
    // ==========================================================

    private static void buildTowerCrown(
            ServerLevel level,
            int centerX,
            int y,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dx = -7; dx <= 7; dx++) {

            for (int dz = -7; dz <= 7; dz++) {

                int distance =
                        dx * dx + dz * dz;

                if (distance < 30
                        || distance > 55) {
                    continue;
                }

                int worldX =
                        centerX + dx;

                int worldZ =
                        centerZ + dz;

                if (!insideChunk(
                        worldX,
                        worldZ,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                int spikeHeight =
                        3
                                + Math.floorMod(
                                Math.abs(dx * 3 + dz * 5),
                                7
                        );

                for (int dy = 1;
                     dy <= spikeHeight;
                     dy++) {

                    level.setBlock(
                            new BlockPos(
                                    worldX,
                                    y + dy,
                                    worldZ
                            ),
                            Blocks.BLUE_ICE
                                    .defaultBlockState(),
                            2
                    );
                }
            }
        }
    }

    // ==========================================================
    // ДОРОГА ОТ ВОРОТ
    // ==========================================================

    private static void buildBridgePath(
            ServerLevel level,
            int centerX,
            int baseY,
            int entranceZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Дорога идёт от ворот к центральному двору.
         */
        for (int z = entranceZ - 55;
             z <= entranceZ;
             z++) {

            for (int dx = -5; dx <= 5; dx++) {

                int x =
                        centerX + dx;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                BlockState state;

                if (Math.abs(dx) == 5) {
                    state =
                            Blocks.POLISHED_DEEPSLATE
                                    .defaultBlockState();

                } else if (Math.floorMod(z, 9) == 0) {
                    state =
                            Blocks.BLUE_ICE
                                    .defaultBlockState();

                } else {
                    state =
                            Blocks.DEEPSLATE_TILES
                                    .defaultBlockState();
                }

                level.setBlock(
                        new BlockPos(
                                x,
                                baseY + 1,
                                z
                        ),
                        state,
                        2
                );
            }
        }
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
}