package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleThroneRoom {

    private CastleThroneRoom() {}

    // =========================================================
    // РАЗМЕРЫ
    // =========================================================

    private static final int HALF_WIDTH = 24;

    /*
     * Тронный зал находится в северной части дворца.
     */
    private static final int FRONT_OFFSET_Z = -22;
    private static final int BACK_OFFSET_Z = -62;

    private static final int WALL_HEIGHT = 38;

    /*
     * Центр будущей арены босса.
     */
    private static final int ARENA_OFFSET_Z = -45;

    private static final int ARENA_RADIUS = 13;


    // =========================================================
    // ГЛАВНЫЙ МЕТОД
    // =========================================================

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildFloor(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildWalls(
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
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildArena(
                level,
                centerX,
                baseY,
                centerZ + ARENA_OFFSET_Z,
                chunkMinX,
                chunkMinZ
        );

        buildThronePlatform(
                level,
                centerX,
                baseY,
                centerZ + BACK_OFFSET_Z + 7,
                chunkMinX,
                chunkMinZ
        );

        buildSwordPedestal(
                level,
                centerX,
                baseY,
                centerZ + ARENA_OFFSET_Z,
                chunkMinX,
                chunkMinZ
        );

        buildCeilingArches(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ПОЛ
    // =========================================================

    private static void buildFloor(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int minZ = centerZ + BACK_OFFSET_Z;
        int maxZ = centerZ + FRONT_OFFSET_Z;

        for (int x = centerX - HALF_WIDTH;
             x <= centerX + HALF_WIDTH;
             x++) {

            for (int z = minZ; z <= maxZ; z++) {

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                int dx = x - centerX;

                BlockState state;

                /*
                 * Центральная ледяная дорога к мечу.
                 */
                if (Math.abs(dx) <= 2) {

                    state =
                            Blocks.BLUE_ICE
                                    .defaultBlockState();

                } else if (Math.abs(dx) <= 5) {

                    state =
                            Blocks.POLISHED_DEEPSLATE
                                    .defaultBlockState();

                } else {

                    long h = hash(
                            x,
                            baseY,
                            z
                    );

                    if (Math.floorMod(h, 19) == 0) {
                        state =
                                Blocks.PACKED_ICE
                                        .defaultBlockState();
                    } else {
                        state =
                                Blocks.DEEPSLATE_TILES
                                        .defaultBlockState();
                    }
                }

                level.setBlock(
                        new BlockPos(
                                x,
                                baseY + 2,
                                z
                        ),
                        state,
                        2
                );
            }
        }
    }


    // =========================================================
    // СТЕНЫ ТРОННОГО ЗАЛА
    // =========================================================

    private static void buildWalls(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int minZ = centerZ + BACK_OFFSET_Z;
        int maxZ = centerZ + FRONT_OFFSET_Z;

        for (int x = centerX - HALF_WIDTH;
             x <= centerX + HALF_WIDTH;
             x++) {

            for (int z = minZ; z <= maxZ; z++) {

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                boolean leftRight =
                        Math.abs(x - centerX)
                                >= HALF_WIDTH - 2;

                boolean back =
                        z <= minZ + 2;

                boolean front =
                        z >= maxZ - 2;

                if (!leftRight && !back && !front) {
                    continue;
                }

                /*
                 * Открытый вход.
                 */
                if (front
                        && Math.abs(x - centerX) <= 5) {
                    continue;
                }

                for (int dy = 3;
                     dy <= WALL_HEIGHT;
                     dy++) {

                    BlockState state;

                    /*
                     * Огромные ледяные окна по бокам.
                     */
                    boolean window =
                            leftRight
                                    && dy >= 12
                                    && dy <= 27
                                    && Math.floorMod(
                                    z - minZ,
                                    13
                            ) >= 4
                                    && Math.floorMod(
                                    z - minZ,
                                    13
                            ) <= 8;

                    if (window) {

                        state =
                                Blocks.BLUE_STAINED_GLASS
                                        .defaultBlockState();

                    } else {

                        state =
                                ancientStone(
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
    // КОЛОННЫ
    // =========================================================

    private static void buildColumns(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int minZ = centerZ + BACK_OFFSET_Z + 7;
        int maxZ = centerZ + FRONT_OFFSET_Z - 5;

        /*
         * Два ряда колонн.
         */
        int[] xOffsets = {
                -17,
                17
        };

        for (int xOffset : xOffsets) {

            for (int z = minZ;
                 z <= maxZ;
                 z += 11) {

                buildColumn(
                        level,
                        centerX + xOffset,
                        baseY,
                        z,
                        chunkMinX,
                        chunkMinZ
                );
            }
        }
    }


    private static void buildColumn(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

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
                 * Основание.
                 */
                for (int dy = 3; dy <= 5; dy++) {

                    level.setBlock(
                            new BlockPos(
                                    x,
                                    baseY + dy,
                                    z
                            ),
                            Blocks.POLISHED_DEEPSLATE
                                    .defaultBlockState(),
                            2
                    );
                }

                /*
                 * Верхняя капитель.
                 */
                for (int dy = 30; dy <= 33; dy++) {

                    level.setBlock(
                            new BlockPos(
                                    x,
                                    baseY + dy,
                                    z
                            ),
                            Blocks.POLISHED_DEEPSLATE
                                    .defaultBlockState(),
                            2
                    );
                }
            }
        }

        /*
         * Центральное тело колонны.
         */
        if (insideChunk(
                cx,
                cz,
                chunkMinX,
                chunkMinZ
        )) {

            for (int dy = 6; dy <= 29; dy++) {

                BlockState state;

                if (dy % 8 == 0) {
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
                                cx,
                                baseY + dy,
                                cz
                        ),
                        state,
                        2
                );
            }
        }
    }


    // =========================================================
    // АРЕНА БОССА
    // =========================================================

    private static void buildArena(
            ServerLevel level,
            int centerX,
            int baseY,
            int arenaZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dx = -ARENA_RADIUS;
             dx <= ARENA_RADIUS;
             dx++) {

            for (int dz = -ARENA_RADIUS;
                 dz <= ARENA_RADIUS;
                 dz++) {

                double distance =
                        Math.sqrt(
                                dx * dx
                                        + dz * dz
                        );

                if (distance > ARENA_RADIUS) {
                    continue;
                }

                int x = centerX + dx;
                int z = arenaZ + dz;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                BlockState state;

                /*
                 * Внешнее кольцо.
                 */
                if (distance >= ARENA_RADIUS - 2) {

                    state =
                            Blocks.BLUE_ICE
                                    .defaultBlockState();

                    /*
                     * Второе кольцо.
                     */
                } else if (distance >= ARENA_RADIUS - 5) {

                    state =
                            Blocks.POLISHED_DEEPSLATE
                                    .defaultBlockState();

                    /*
                     * Центр.
                     */
                } else {

                    long h = hash(
                            x,
                            baseY,
                            z
                    );

                    if (Math.floorMod(h, 13) == 0) {

                        state =
                                Blocks.PACKED_ICE
                                        .defaultBlockState();

                    } else {

                        state =
                                Blocks.DEEPSLATE_TILES
                                        .defaultBlockState();
                    }
                }

                level.setBlock(
                        new BlockPos(
                                x,
                                baseY + 3,
                                z
                        ),
                        state,
                        2
                );
            }
        }

        /*
         * 8 ледяных кристаллов вокруг арены.
         */
        for (int i = 0; i < 8; i++) {

            double angle =
                    Math.PI * 2.0
                            * i / 8.0;

            int x =
                    centerX
                            + (int) Math.round(
                            Math.cos(angle) * 11
                    );

            int z =
                    arenaZ
                            + (int) Math.round(
                            Math.sin(angle) * 11
                    );

            buildCrystal(
                    level,
                    x,
                    baseY + 4,
                    z,
                    chunkMinX,
                    chunkMinZ
            );
        }
    }


    // =========================================================
    // ЛЕДЯНОЙ КРИСТАЛЛ
    // =========================================================

    private static void buildCrystal(
            ServerLevel level,
            int x,
            int y,
            int z,
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

        /*
         * Основной шпиль.
         */
        for (int dy = 0; dy < 8; dy++) {

            level.setBlock(
                    new BlockPos(
                            x,
                            y + dy,
                            z
                    ),
                    dy > 4
                            ? Blocks.BLUE_ICE.defaultBlockState()
                            : Blocks.PACKED_ICE.defaultBlockState(),
                    2
            );
        }

        /*
         * Небольшие боковые кристаллы.
         */
        if (insideChunk(
                x + 1,
                z,
                chunkMinX,
                chunkMinZ
        )) {

            for (int dy = 0; dy < 4; dy++) {

                level.setBlock(
                        new BlockPos(
                                x + 1,
                                y + dy,
                                z
                        ),
                        Blocks.BLUE_ICE
                                .defaultBlockState(),
                        2
                );
            }
        }
    }


    // =========================================================
    // ТРОН
    // =========================================================

    private static void buildThronePlatform(
            ServerLevel level,
            int centerX,
            int baseY,
            int throneZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Три уровня возвышения.
         */
        for (int layer = 0; layer < 3; layer++) {

            int halfWidth =
                    8 - layer * 2;

            int halfLength =
                    5 - layer;

            for (int dx = -halfWidth;
                 dx <= halfWidth;
                 dx++) {

                for (int dz = -halfLength;
                     dz <= halfLength;
                     dz++) {

                    int x = centerX + dx;
                    int z = throneZ + dz;

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
                                    baseY + 3 + layer,
                                    z
                            ),
                            layer == 2
                                    ? Blocks.BLUE_ICE.defaultBlockState()
                                    : Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                            2
                    );
                }
            }
        }

        /*
         * Сам трон.
         */
        if (insideChunk(
                centerX,
                throneZ,
                chunkMinX,
                chunkMinZ
        )) {

            for (int dy = 6; dy <= 13; dy++) {

                int width =
                        dy <= 8
                                ? 2
                                : 3;

                for (int dx = -width;
                     dx <= width;
                     dx++) {

                    int x =
                            centerX + dx;

                    if (!insideChunk(
                            x,
                            throneZ,
                            chunkMinX,
                            chunkMinZ
                    )) {
                        continue;
                    }

                    level.setBlock(
                            new BlockPos(
                                    x,
                                    baseY + dy,
                                    throneZ
                            ),
                            dy >= 11
                                    ? Blocks.BLUE_ICE.defaultBlockState()
                                    : Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                            2
                    );
                }
            }
        }
    }


    // =========================================================
    // КАМЕНЬ МЕЧА ВЕЧНОГО ЛЬДА
    // =========================================================

    private static void buildSwordPedestal(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Основание 7x7.
         */
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {

                /*
                 * Срезаем углы.
                 */
                if (Math.abs(dx)
                        + Math.abs(dz) > 5) {
                    continue;
                }

                int x = centerX + dx;
                int z = centerZ + dz;

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
                                baseY + 4,
                                z
                        ),
                        Blocks.POLISHED_DEEPSLATE
                                .defaultBlockState(),
                        2
                );
            }
        }

        /*
         * Второй уровень.
         */
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                int x = centerX + dx;
                int z = centerZ + dz;

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
                                baseY + 5,
                                z
                        ),
                        Blocks.BLUE_ICE
                                .defaultBlockState(),
                        2
                );
            }
        }

        /*
         * Камень, в котором позже будет торчать меч.
         */
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                int x = centerX + dx;
                int z = centerZ + dz;

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
                                baseY + 6,
                                z
                        ),
                        Blocks.PACKED_ICE
                                .defaultBlockState(),
                        2
                );
            }
        }

        /*
         * Центральный блок.
         *
         * ПОЗЖЕ:
         *
         * Blocks.BLUE_ICE
         *
         * заменим на:
         *
         * Abyss.ETERNAL_ICE_SWORD_PEDESTAL
         */
        if (insideChunk(
                centerX,
                centerZ,
                chunkMinX,
                chunkMinZ
        )) {

            level.setBlock(
                    new BlockPos(
                            centerX,
                            baseY + 7,
                            centerZ
                    ),
                    Blocks.BLUE_ICE
                            .defaultBlockState(),
                    2
            );
        }
    }


    // =========================================================
    // СВОДЫ
    // =========================================================

    private static void buildCeilingArches(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int minZ = centerZ + BACK_OFFSET_Z + 5;
        int maxZ = centerZ + FRONT_OFFSET_Z - 4;

        /*
         * Несколько поперечных арок.
         */
        for (int z = minZ;
             z <= maxZ;
             z += 10) {

            for (int dx = -HALF_WIDTH;
                 dx <= HALF_WIDTH;
                 dx++) {

                int x = centerX + dx;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                /*
                 * Чем ближе к центру, тем выше свод.
                 */
                double normalized =
                        Math.abs(dx)
                                / (double) HALF_WIDTH;

                int archY =
                        baseY
                                + 32
                                + (int) Math.round(
                                (1.0 - normalized)
                                        * 10.0
                        );

                BlockState state;

                if (Math.abs(dx) <= 4) {
                    state =
                            Blocks.BLUE_ICE
                                    .defaultBlockState();
                } else {
                    state =
                            Blocks.DEEPSLATE_BRICKS
                                    .defaultBlockState();
                }

                /*
                 * Арка толщиной два блока.
                 */
                level.setBlock(
                        new BlockPos(
                                x,
                                archY,
                                z
                        ),
                        state,
                        2
                );

                level.setBlock(
                        new BlockPos(
                                x,
                                archY - 1,
                                z
                        ),
                        state,
                        2
                );
            }
        }
    }


    // =========================================================
    // ДРЕВНИЙ КАМЕНЬ
    // =========================================================

    private static BlockState ancientStone(
            int x,
            int y,
            int z
    ) {

        long h = hash(x, y, z);

        int value =
                Math.floorMod(h, 100);

        if (value < 8) {
            return Blocks.CRACKED_DEEPSLATE_BRICKS
                    .defaultBlockState();
        }

        if (value < 13) {
            return Blocks.PACKED_ICE
                    .defaultBlockState();
        }

        if (value < 23) {
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
        h *= 0xff51afd7ed558ccdL;

        h ^= h >>> 33;
        h *= 0xc4ceb9fe1a85ec53L;

        return h ^ (h >>> 33);
    }
}