package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleMainPalace {

    private CastleMainPalace() {}

    private static final int HALF_WIDTH = 38;

    // Дворец смещён к северной части крепости
    private static final int FRONT_Z = 15;
    private static final int BACK_Z = -58;

    private static final int SIDE_HEIGHT = 30;
    private static final int CENTER_HEIGHT = 45;

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int frontZ = centerZ + FRONT_Z;
        int backZ = centerZ + BACK_Z;

        buildGrandStairs(
                level,
                centerX,
                baseY,
                frontZ,
                chunkMinX,
                chunkMinZ
        );

        buildPalaceFloor(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildOuterShell(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildColumns(
                level,
                centerX,
                baseY,
                frontZ,
                chunkMinX,
                chunkMinZ
        );

        buildCentralHall(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildCentralTower(
                level,
                centerX,
                baseY,
                centerZ - 25,
                chunkMinX,
                chunkMinZ
        );
    }

    // =========================================================
    // БОЛЬШАЯ ЛЕСТНИЦА
    // =========================================================

    private static void buildGrandStairs(
            ServerLevel level,
            int centerX,
            int baseY,
            int frontZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * 8 ступеней.
         * Чем выше ступень, тем ближе она к дворцу.
         */
        for (int step = 0; step < 8; step++) {

            int z = frontZ + 8 - step;

            int halfWidth = 18 - step;

            for (int x = centerX - halfWidth;
                 x <= centerX + halfWidth;
                 x++) {

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                for (int fillZ = z;
                     fillZ <= frontZ + 8;
                     fillZ++) {

                    if (!insideChunk(
                            x,
                            fillZ,
                            chunkMinX,
                            chunkMinZ
                    )) {
                        continue;
                    }

                    level.setBlock(
                            new BlockPos(
                                    x,
                                    baseY + 1 + step,
                                    fillZ
                            ),
                            Blocks.POLISHED_DEEPSLATE
                                    .defaultBlockState(),
                            2
                    );
                }
            }
        }
    }

    // =========================================================
    // ПОЛ
    // =========================================================

    private static void buildPalaceFloor(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int minX = Math.max(
                centerX - HALF_WIDTH,
                chunkMinX
        );

        int maxX = Math.min(
                centerX + HALF_WIDTH,
                chunkMinX + 15
        );

        int minZ = Math.max(
                centerZ + BACK_Z,
                chunkMinZ
        );

        int maxZ = Math.min(
                centerZ + FRONT_Z,
                chunkMinZ + 15
        );

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                BlockState state;

                /*
                 * Центральная ледяная дорожка.
                 */
                if (Math.abs(x - centerX) <= 3) {

                    state =
                            Blocks.BLUE_ICE
                                    .defaultBlockState();

                } else if (Math.floorMod(x + z, 11) == 0) {

                    state =
                            Blocks.PACKED_ICE
                                    .defaultBlockState();

                } else {

                    state =
                            Blocks.POLISHED_DEEPSLATE
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

    // =========================================================
    // ВНЕШНЯЯ ОБОЛОЧКА
    // =========================================================

    private static void buildOuterShell(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int minX = centerX - HALF_WIDTH;
        int maxX = centerX + HALF_WIDTH;

        int minZ = centerZ + BACK_Z;
        int maxZ = centerZ + FRONT_Z;

        for (int x = Math.max(minX, chunkMinX);
             x <= Math.min(maxX, chunkMinX + 15);
             x++) {

            for (int z = Math.max(minZ, chunkMinZ);
                 z <= Math.min(maxZ, chunkMinZ + 15);
                 z++) {

                boolean sideWall =
                        x <= minX + 2
                                || x >= maxX - 2;

                boolean backWall =
                        z <= minZ + 2;

                boolean frontWall =
                        z >= maxZ - 2;

                if (!sideWall
                        && !backWall
                        && !frontWall) {
                    continue;
                }

                /*
                 * Главный вход.
                 */
                if (frontWall
                        && Math.abs(x - centerX) <= 6) {
                    continue;
                }

                /*
                 * Боковые окна.
                 */
                boolean window =
                        sideWall
                                && ((z - minZ) % 14 >= 4)
                                && ((z - minZ) % 14 <= 8);

                for (int dy = 2;
                     dy <= SIDE_HEIGHT;
                     dy++) {

                    BlockState state;

                    if (window
                            && dy >= 9
                            && dy <= 18) {

                        state =
                                Blocks.BLUE_STAINED_GLASS
                                        .defaultBlockState();

                    } else {

                        state =
                                wallBlock(
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
    }

    // =========================================================
    // КОЛОННЫ У ВХОДА
    // =========================================================

    private static void buildColumns(
            ServerLevel level,
            int centerX,
            int baseY,
            int frontZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int[] positions = {
                -25, -17, -9,
                9, 17, 25
        };

        for (int offset : positions) {

            int x = centerX + offset;
            int z = frontZ + 1;

            if (!insideChunk(
                    x,
                    z,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            for (int dy = 2; dy <= 27; dy++) {

                BlockState state;

                if (dy <= 3 || dy >= 25) {
                    state =
                            Blocks.POLISHED_DEEPSLATE
                                    .defaultBlockState();
                } else {
                    state =
                            Blocks.DEEPSLATE_BRICKS
                                    .defaultBlockState();
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

                /*
                 * Колонна 2x2.
                 */
                if (insideChunk(
                        x + 1,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {

                    level.setBlock(
                            new BlockPos(
                                    x + 1,
                                    baseY + dy,
                                    z
                            ),
                            state,
                            2
                    );
                }
            }
        }
    }

    // =========================================================
    // ЦЕНТРАЛЬНЫЙ ЗАЛ
    // =========================================================

    private static void buildCentralHall(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int halfWidth = 18;

        int minZ = centerZ - 50;
        int maxZ = centerZ + 10;

        for (int x = Math.max(
                centerX - halfWidth,
                chunkMinX
        );
             x <= Math.min(
                     centerX + halfWidth,
                     chunkMinX + 15
             );
             x++) {

            for (int z = Math.max(minZ, chunkMinZ);
                 z <= Math.min(maxZ, chunkMinZ + 15);
                 z++) {

                boolean shell =
                        Math.abs(x - centerX) >= halfWidth - 2
                                || z <= minZ + 2
                                || z >= maxZ - 2;

                if (!shell) {
                    continue;
                }

                /*
                 * Не закрываем вход.
                 */
                if (z >= maxZ - 2
                        && Math.abs(x - centerX) <= 5) {
                    continue;
                }

                for (int dy = 2;
                     dy <= CENTER_HEIGHT;
                     dy++) {

                    BlockState state;

                    /*
                     * Огромные вертикальные ледяные окна.
                     */
                    boolean iceWindow =
                            dy >= 14
                                    && dy <= 32
                                    && Math.floorMod(z, 13) <= 4;

                    if (iceWindow
                            && Math.abs(x - centerX)
                            >= halfWidth - 2) {

                        state =
                                Blocks.BLUE_STAINED_GLASS
                                        .defaultBlockState();

                    } else {

                        state =
                                wallBlock(
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
    }

    // =========================================================
    // ЦЕНТРАЛЬНАЯ ЛЕДЯНАЯ БАШНЯ
    // =========================================================

    private static void buildCentralTower(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        int radius = 12;
        int height = 58;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {

                int distance =
                        dx * dx + dz * dz;

                int outer =
                        radius * radius;

                int inner =
                        (radius - 3)
                                * (radius - 3);

                if (distance > outer
                        || distance < inner) {
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

                for (int dy = 2;
                     dy <= height;
                     dy++) {

                    BlockState state;

                    if (dy > 48) {
                        state =
                                Blocks.PACKED_ICE
                                        .defaultBlockState();

                    } else if (dy % 12 == 0) {
                        state =
                                Blocks.BLUE_ICE
                                        .defaultBlockState();

                    } else {
                        state =
                                wallBlock(
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

        buildIceSpire(
                level,
                cx,
                baseY + height,
                cz,
                chunkMinX,
                chunkMinZ
        );
    }

    // =========================================================
    // ШПИЛЬ
    // =========================================================

    private static void buildIceSpire(
            ServerLevel level,
            int cx,
            int startY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * 22 уровня постепенно сужающегося шпиля.
         */
        for (int layer = 0; layer < 22; layer++) {

            int radius =
                    Math.max(
                            1,
                            10 - layer / 2
                    );

            int y = startY + layer + 1;

            for (int dx = -radius;
                 dx <= radius;
                 dx++) {

                for (int dz = -radius;
                     dz <= radius;
                     dz++) {

                    if (dx * dx + dz * dz
                            > radius * radius) {
                        continue;
                    }

                    /*
                     * Не делаем шпиль полностью сплошным.
                     */
                    int inner =
                            Math.max(
                                    0,
                                    radius - 2
                            );

                    if (radius > 2
                            && dx * dx + dz * dz
                            < inner * inner) {
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
                            layer > 13
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

            for (int dy = 23; dy <= 28; dy++) {

                level.setBlock(
                        new BlockPos(
                                cx,
                                startY + dy,
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
    // БЛОКИ ДРЕВНЕЙ КЛАДКИ
    // =========================================================

    private static BlockState wallBlock(
            int x,
            int y,
            int z
    ) {

        long h = hash(x, y, z);

        int value =
                Math.floorMod(h, 100);

        if (value < 7) {
            return Blocks.CRACKED_DEEPSLATE_BRICKS
                    .defaultBlockState();
        }

        if (value < 11) {
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