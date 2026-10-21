package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleInterior {

    private CastleInterior() {}

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildMainCorridor(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildCrossHall(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildLibrary(
                level,
                centerX - 25,
                baseY,
                centerZ - 5,
                chunkMinX,
                chunkMinZ
        );

        buildArmory(
                level,
                centerX + 25,
                baseY,
                centerZ - 5,
                chunkMinX,
                chunkMinZ
        );

        buildBarracks(
                level,
                centerX - 25,
                baseY,
                centerZ - 35,
                chunkMinX,
                chunkMinZ
        );

        buildFrozenChapel(
                level,
                centerX + 25,
                baseY,
                centerZ - 35,
                chunkMinX,
                chunkMinZ
        );

        buildGrandStaircases(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );

        buildUpperGallery(
                level,
                centerX,
                baseY,
                centerZ,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ГЛАВНЫЙ КОРИДОР
    // =========================================================

    private static void buildMainCorridor(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Коридор от главного входа дворца
         * до тронного зала.
         */
        for (int z = centerZ + 12;
             z >= centerZ - 58;
             z--) {

            for (int dx = -5; dx <= 5; dx++) {

                int x = centerX + dx;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                BlockState floor;

                if (Math.abs(dx) <= 1) {
                    floor = Blocks.BLUE_ICE.defaultBlockState();

                } else if (Math.abs(dx) <= 3) {
                    floor = Blocks.POLISHED_DEEPSLATE.defaultBlockState();

                } else {
                    floor = Blocks.DEEPSLATE_TILES.defaultBlockState();
                }

                place(
                        level,
                        x,
                        baseY + 2,
                        z,
                        floor
                );

                /*
                 * Очищаем пространство коридора.
                 */
                for (int dy = 3; dy <= 10; dy++) {

                    place(
                            level,
                            x,
                            baseY + dy,
                            z,
                            Blocks.AIR.defaultBlockState()
                    );
                }
            }

            /*
             * Декоративные арки каждые 10 блоков.
             */
            if (Math.floorMod(z - centerZ, 10) == 0) {

                buildCorridorArch(
                        level,
                        centerX,
                        baseY,
                        z,
                        chunkMinX,
                        chunkMinZ
                );
            }
        }
    }


    // =========================================================
    // АРКИ
    // =========================================================

    private static void buildCorridorArch(
            ServerLevel level,
            int centerX,
            int baseY,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Боковые колонны.
         */
        for (int side : new int[]{-5, 5}) {

            int x = centerX + side;

            if (!insideChunk(
                    x,
                    z,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            for (int dy = 3; dy <= 11; dy++) {

                place(
                        level,
                        x,
                        baseY + dy,
                        z,
                        dy == 6 || dy == 10
                                ? Blocks.PACKED_ICE.defaultBlockState()
                                : Blocks.DEEPSLATE_BRICKS.defaultBlockState()
                );
            }
        }

        /*
         * Верх арки.
         */
        for (int dx = -5; dx <= 5; dx++) {

            int x = centerX + dx;

            if (!insideChunk(
                    x,
                    z,
                    chunkMinX,
                    chunkMinZ
            )) {
                continue;
            }

            int drop =
                    Math.abs(dx) >= 4
                            ? 0
                            : Math.abs(dx) >= 2
                            ? 1
                            : 2;

            place(
                    level,
                    x,
                    baseY + 11 + drop,
                    z,
                    Math.abs(dx) <= 1
                            ? Blocks.BLUE_ICE.defaultBlockState()
                            : Blocks.DEEPSLATE_BRICKS.defaultBlockState()
            );
        }
    }


    // =========================================================
    // ЦЕНТРАЛЬНЫЙ ПОПЕРЕЧНЫЙ ЗАЛ
    // =========================================================

    private static void buildCrossHall(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int hallZ = centerZ - 15;

        for (int dx = -34; dx <= 34; dx++) {
            for (int dz = -4; dz <= 4; dz++) {

                int x = centerX + dx;
                int z = hallZ + dz;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                place(
                        level,
                        x,
                        baseY + 2,
                        z,
                        Math.abs(dz) <= 1
                                ? Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                                : Blocks.DEEPSLATE_TILES.defaultBlockState()
                );

                for (int dy = 3; dy <= 10; dy++) {

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


    // =========================================================
    // БИБЛИОТЕКА
    // =========================================================

    private static void buildLibrary(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildRoom(
                level,
                cx,
                baseY,
                cz,
                12,
                13,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Книжные стеллажи.
         */
        for (int zOffset = -8;
             zOffset <= 8;
             zOffset += 4) {

            for (int side : new int[]{-9, 9}) {

                for (int dy = 3; dy <= 7; dy++) {

                    setIfInside(
                            level,
                            cx + side,
                            baseY + dy,
                            cz + zOffset,
                            Blocks.BOOKSHELF.defaultBlockState(),
                            chunkMinX,
                            chunkMinZ
                    );
                }
            }
        }

        /*
         * Центральные книжные ряды.
         */
        for (int zOffset = -7;
             zOffset <= 7;
             zOffset += 7) {

            for (int xOffset = -5;
                 xOffset <= 5;
                 xOffset++) {

                /*
                 * Проход посередине.
                 */
                if (Math.abs(xOffset) <= 1) {
                    continue;
                }

                for (int dy = 3; dy <= 6; dy++) {

                    setIfInside(
                            level,
                            cx + xOffset,
                            baseY + dy,
                            cz + zOffset,
                            Blocks.BOOKSHELF.defaultBlockState(),
                            chunkMinX,
                            chunkMinZ
                    );
                }
            }
        }

        /*
         * Замёрзший центральный стол.
         */
        for (int dx = -3; dx <= 3; dx++) {

            setIfInside(
                    level,
                    cx + dx,
                    baseY + 3,
                    cz,
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }

        setIfInside(
                level,
                cx,
                baseY + 4,
                cz,
                Blocks.BLUE_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ОРУЖЕЙНАЯ
    // =========================================================

    private static void buildArmory(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildRoom(
                level,
                cx,
                baseY,
                cz,
                12,
                13,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Стойки/постаменты вдоль стен.
         */
        for (int zOffset = -8;
             zOffset <= 8;
             zOffset += 4) {

            buildWeaponPedestal(
                    level,
                    cx - 8,
                    baseY,
                    cz + zOffset,
                    chunkMinX,
                    chunkMinZ
            );

            buildWeaponPedestal(
                    level,
                    cx + 8,
                    baseY,
                    cz + zOffset,
                    chunkMinX,
                    chunkMinZ
            );
        }

        /*
         * Центральная платформа.
         */
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -3; dz <= 3; dz++) {

                if (Math.abs(dx) + Math.abs(dz) > 6) {
                    continue;
                }

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 3,
                        cz + dz,
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Ледяной артефакт-заглушка.
         * Позже здесь можно поставить loot/декорацию.
         */
        setIfInside(
                level,
                cx,
                baseY + 4,
                cz,
                Blocks.PACKED_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                cx,
                baseY + 5,
                cz,
                Blocks.BLUE_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    private static void buildWeaponPedestal(
            ServerLevel level,
            int x,
            int baseY,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        setIfInside(
                level,
                x,
                baseY + 3,
                z,
                Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x,
                baseY + 4,
                z,
                Blocks.IRON_BARS.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x,
                baseY + 5,
                z,
                Blocks.IRON_BARS.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // КАЗАРМЫ
    // =========================================================

    private static void buildBarracks(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildRoom(
                level,
                cx,
                baseY,
                cz,
                12,
                12,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Каменные основания бывших кроватей.
         */
        for (int zOffset = -7;
             zOffset <= 7;
             zOffset += 5) {

            buildBedPlatform(
                    level,
                    cx - 7,
                    baseY,
                    cz + zOffset,
                    chunkMinX,
                    chunkMinZ
            );

            buildBedPlatform(
                    level,
                    cx + 7,
                    baseY,
                    cz + zOffset,
                    chunkMinX,
                    chunkMinZ
            );
        }

        /*
         * Центральный очаг давно замёрз.
         */
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 3,
                        cz + dz,
                        Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        setIfInside(
                level,
                cx,
                baseY + 4,
                cz,
                Blocks.BLUE_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    private static void buildBedPlatform(
            ServerLevel level,
            int x,
            int baseY,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dz = -1; dz <= 1; dz++) {

            setIfInside(
                    level,
                    x,
                    baseY + 3,
                    z + dz,
                    Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }

        setIfInside(
                level,
                x,
                baseY + 4,
                z - 1,
                Blocks.PACKED_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ЛЕДЯНАЯ ЧАСОВНЯ
    // =========================================================

    private static void buildFrozenChapel(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildRoom(
                level,
                cx,
                baseY,
                cz,
                12,
                12,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Центральная дорожка.
         */
        for (int dz = -8; dz <= 8; dz++) {

            for (int dx = -1; dx <= 1; dx++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 3,
                        cz + dz,
                        Blocks.BLUE_ICE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Алтарь.
         */
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 4,
                        cz - 8 + dz,
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Большой ледяной кристалл.
         */
        buildCrystal(
                level,
                cx,
                baseY + 5,
                cz - 8,
                10,
                chunkMinX,
                chunkMinZ
        );

        buildCrystal(
                level,
                cx - 2,
                baseY + 5,
                cz - 7,
                6,
                chunkMinX,
                chunkMinZ
        );

        buildCrystal(
                level,
                cx + 2,
                baseY + 5,
                cz - 7,
                7,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // БОЛЬШИЕ ЛЕСТНИЦЫ
    // =========================================================

    private static void buildGrandStaircases(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Две симметричные лестницы.
         */
        buildStaircase(
                level,
                centerX - 13,
                baseY,
                centerZ - 18,
                -1,
                chunkMinX,
                chunkMinZ
        );

        buildStaircase(
                level,
                centerX + 13,
                baseY,
                centerZ - 18,
                1,
                chunkMinX,
                chunkMinZ
        );
    }


    private static void buildStaircase(
            ServerLevel level,
            int startX,
            int baseY,
            int startZ,
            int xDirection,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * 15 ступеней.
         */
        for (int step = 0; step < 15; step++) {

            int x =
                    startX
                            + xDirection
                            * (step / 2);

            int z =
                    startZ - step;

            int y =
                    baseY + 3 + step;

            for (int width = -2; width <= 2; width++) {

                int worldX = x + width;

                if (!insideChunk(
                        worldX,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                place(
                        level,
                        worldX,
                        y,
                        z,
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                );

                /*
                 * Очищаем пространство над ступенью.
                 */
                for (int air = 1; air <= 4; air++) {

                    place(
                            level,
                            worldX,
                            y + air,
                            z,
                            Blocks.AIR.defaultBlockState()
                    );
                }
            }
        }
    }


    // =========================================================
    // ГАЛЕРЕЯ ВТОРОГО ЭТАЖА
    // =========================================================

    private static void buildUpperGallery(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int galleryY =
                baseY + 18;

        /*
         * Левая и правая галерея.
         */
        for (int z = centerZ - 48;
             z <= centerZ + 7;
             z++) {

            for (int side : new int[]{-14, 14}) {

                for (int width = -2; width <= 2; width++) {

                    int x =
                            centerX
                                    + side
                                    + width;

                    if (!insideChunk(
                            x,
                            z,
                            chunkMinX,
                            chunkMinZ
                    )) {
                        continue;
                    }

                    place(
                            level,
                            x,
                            galleryY,
                            z,
                            Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                    );

                    /*
                     * Перила со стороны центрального зала.
                     */
                    if (width ==
                            (side < 0 ? 2 : -2)) {

                        if (Math.floorMod(z, 3) != 0) {

                            place(
                                    level,
                                    x,
                                    galleryY + 1,
                                    z,
                                    Blocks.IRON_BARS.defaultBlockState()
                            );
                        }
                    }
                }
            }
        }

        /*
         * Поперечный мост.
         */
        int bridgeZ =
                centerZ - 32;

        for (int dx = -14; dx <= 14; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                int x = centerX + dx;
                int z = bridgeZ + dz;

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                place(
                        level,
                        x,
                        galleryY,
                        z,
                        Math.abs(dx) <= 2
                                ? Blocks.BLUE_ICE.defaultBlockState()
                                : Blocks.POLISHED_DEEPSLATE.defaultBlockState()
                );
            }
        }
    }


    // =========================================================
    // БАЗОВАЯ КОМНАТА
    // =========================================================

    private static void buildRoom(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int halfWidth,
            int halfLength,
            int chunkMinX,
            int chunkMinZ
    ) {

        int minX = cx - halfWidth;
        int maxX = cx + halfWidth;

        int minZ = cz - halfLength;
        int maxZ = cz + halfLength;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {

                if (!insideChunk(
                        x,
                        z,
                        chunkMinX,
                        chunkMinZ
                )) {
                    continue;
                }

                /*
                 * Пол комнаты.
                 */
                place(
                        level,
                        x,
                        baseY + 2,
                        z,
                        roomFloor(x, z)
                );

                /*
                 * Освобождаем внутреннее пространство.
                 */
                boolean wall =
                        x <= minX + 1
                                || x >= maxX - 1
                                || z <= minZ + 1
                                || z >= maxZ - 1;

                if (!wall) {

                    for (int dy = 3; dy <= 11; dy++) {

                        place(
                                level,
                                x,
                                baseY + dy,
                                z,
                                Blocks.AIR.defaultBlockState()
                        );
                    }

                    continue;
                }

                /*
                 * Проём двери в сторону центра замка.
                 */
                boolean door =
                        Math.abs(z - cz) <= 2
                                &&
                                (
                                        x <= minX + 1
                                                || x >= maxX - 1
                                );

                for (int dy = 3; dy <= 11; dy++) {

                    if (door && dy <= 8) {

                        place(
                                level,
                                x,
                                baseY + dy,
                                z,
                                Blocks.AIR.defaultBlockState()
                        );

                        continue;
                    }

                    BlockState state;

                    if (dy >= 7
                            && dy <= 9
                            && Math.floorMod(x + z, 9) <= 2) {

                        state =
                                Blocks.BLUE_STAINED_GLASS
                                        .defaultBlockState();

                    } else {

                        state =
                                interiorStone(
                                        x,
                                        baseY + dy,
                                        z
                                );
                    }

                    place(
                            level,
                            x,
                            baseY + dy,
                            z,
                            state
                    );
                }
            }
        }
    }


    // =========================================================
    // КРИСТАЛЛ
    // =========================================================

    private static void buildCrystal(
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

            place(
                    level,
                    x,
                    startY + dy,
                    z,
                    dy > height / 2
                            ? Blocks.BLUE_ICE.defaultBlockState()
                            : Blocks.PACKED_ICE.defaultBlockState()
            );
        }
    }


    // =========================================================
    // БЛОКИ
    // =========================================================

    private static BlockState roomFloor(
            int x,
            int z
    ) {

        long h =
                hash(
                        x,
                        0,
                        z
                );

        int value =
                Math.floorMod(
                        h,
                        100
                );

        if (value < 8) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }

        if (value < 18) {
            return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        }

        return Blocks.DEEPSLATE_TILES.defaultBlockState();
    }


    private static BlockState interiorStone(
            int x,
            int y,
            int z
    ) {

        int value =
                Math.floorMod(
                        hash(x, y, z),
                        100
                );

        if (value < 7) {
            return Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        }

        if (value < 11) {
            return Blocks.PACKED_ICE.defaultBlockState();
        }

        if (value < 20) {
            return Blocks.POLISHED_DEEPSLATE.defaultBlockState();
        }

        return Blocks.DEEPSLATE_BRICKS.defaultBlockState();
    }


    // =========================================================
    // UTILS
    // =========================================================

    private static void setIfInside(
            ServerLevel level,
            int x,
            int y,
            int z,
            BlockState state,
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
            BlockState state
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