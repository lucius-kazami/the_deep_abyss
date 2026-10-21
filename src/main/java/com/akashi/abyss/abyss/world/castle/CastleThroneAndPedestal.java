package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import com.akashi.abyss.abyss.Abyss;

public final class CastleThroneAndPedestal {

    private CastleThroneAndPedestal() {}

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Центр арены соответствует нашему тронному залу.
         */
        int arenaX = centerX;
        int arenaZ = centerZ - 45;

        /*
         * Сам трон находится ещё севернее.
         */
        int throneX = centerX;
        int throneZ = centerZ - 57;

        buildSwordPedestal(
                level,
                arenaX,
                baseY,
                arenaZ,
                chunkMinX,
                chunkMinZ
        );

        buildThrone(
                level,
                throneX,
                baseY,
                throneZ,
                chunkMinX,
                chunkMinZ
        );

        buildThronePlatform(
                level,
                throneX,
                baseY,
                throneZ,
                chunkMinX,
                chunkMinZ
        );

        buildFrozenCrown(
                level,
                throneX,
                baseY,
                throneZ,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ПОСТАМЕНТ МЕЧА
    // =========================================================

    private static void buildSwordPedestal(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Большая нижняя платформа 11x11.
         */
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {

                if (Math.abs(dx) + Math.abs(dz) > 8) {
                    continue;
                }

                /*
                 * Запечатанный Меч Вечного Мороза.
                 */
                setIfInside(
                        level,
                        cx,
                        baseY + 8,
                        cz,
                        Abyss.ETERNAL_FROST_SWORD_PEDESTAL
                                .get()
                                .defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Второй уровень 9x9.
         */
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {

                if (Math.abs(dx) + Math.abs(dz) > 6) {
                    continue;
                }

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 5,
                        cz + dz,
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Ледяное кольцо.
         */
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {

                double distance =
                        Math.sqrt(dx * dx + dz * dz);

                if (distance < 2.0 || distance > 3.4) {
                    continue;
                }

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 6,
                        cz + dz,
                        Blocks.BLUE_ICE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Камень, в который позже будет воткнут
         * Меч Вечного Льда.
         *
         * Делаем его достаточно большим.
         */
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                if (Math.abs(dx) == 2
                        && Math.abs(dz) == 2) {
                    continue;
                }

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 6,
                        cz + dz,
                        Blocks.DEEPSLATE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Неровная верхушка камня.
         */
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 7,
                        cz + dz,
                        Blocks.DEEPSLATE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Самая верхняя точка.
         * Именно сюда позже поставим модель меча.
         */
        setIfInside(
                level,
                cx,
                baseY + 8,
                cz,
                Abyss.ETERNAL_FROST_SWORD_PEDESTAL
                        .get()
                        .defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        /*
         * Четыре кристалла вокруг постамента.
         */
        buildCrystal(
                level,
                cx - 7,
                baseY + 5,
                cz,
                7,
                chunkMinX,
                chunkMinZ
        );

        buildCrystal(
                level,
                cx + 7,
                baseY + 5,
                cz,
                7,
                chunkMinX,
                chunkMinZ
        );

        buildCrystal(
                level,
                cx,
                baseY + 5,
                cz - 7,
                9,
                chunkMinX,
                chunkMinZ
        );

        buildCrystal(
                level,
                cx,
                baseY + 5,
                cz + 7,
                6,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ПЛАТФОРМА ТРОНА
    // =========================================================

    private static void buildThronePlatform(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Первая ступень.
         */
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -4; dz <= 4; dz++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 3,
                        cz + dz,
                        Blocks.DEEPSLATE_TILES.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Вторая ступень.
         */
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -3; dz <= 3; dz++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 4,
                        cz + dz,
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Третья ступень.
         */
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 5,
                        cz + dz,
                        Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Ледяная дорожка от арены к трону.
         */
        for (int z = cz + 1; z <= cz + 9; z++) {

            for (int dx = -1; dx <= 1; dx++) {

                setIfInside(
                        level,
                        cx + dx,
                        baseY + 3,
                        z,
                        Blocks.BLUE_ICE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }
    }


    // =========================================================
    // САМ ТРОН
    // =========================================================

    private static void buildThrone(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        int throneBaseY = baseY + 6;

        /*
         * Сиденье.
         */
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -2; dz <= 1; dz++) {

                setIfInside(
                        level,
                        cx + dx,
                        throneBaseY,
                        cz + dz,
                        Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Спинка огромного трона.
         */
        for (int dy = 1; dy <= 13; dy++) {

            int width;

            if (dy <= 5) {
                width = 4;
            } else if (dy <= 9) {
                width = 3;
            } else {
                width = 2;
            }

            for (int dx = -width; dx <= width; dx++) {

                BlockState state;

                if (Math.abs(dx) == width) {

                    state =
                            Blocks.PACKED_ICE.defaultBlockState();

                } else if (dy >= 10) {

                    state =
                            Blocks.BLUE_ICE.defaultBlockState();

                } else {

                    state =
                            Blocks.DEEPSLATE_BRICKS.defaultBlockState();
                }

                setIfInside(
                        level,
                        cx + dx,
                        throneBaseY + dy,
                        cz - 2,
                        state,
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Подлокотники.
         */
        for (int side : new int[]{-4, 4}) {

            for (int dy = 0; dy <= 4; dy++) {

                setIfInside(
                        level,
                        cx + side,
                        throneBaseY + dy,
                        cz,
                        dy >= 3
                                ? Blocks.BLUE_ICE.defaultBlockState()
                                : Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }

        /*
         * Два шпиля трона.
         */
        buildCrystal(
                level,
                cx - 5,
                throneBaseY,
                cz - 2,
                15,
                chunkMinX,
                chunkMinZ
        );

        buildCrystal(
                level,
                cx + 5,
                throneBaseY,
                cz - 2,
                15,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // ЛЕДЯНАЯ КОРОНА НАД ТРОНОМ
    // =========================================================

    private static void buildFrozenCrown(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        int crownY = baseY + 21;

        for (int dx = -7; dx <= 7; dx++) {

            int height =
                    2 + Math.max(
                            0,
                            7 - Math.abs(dx)
                    );

            for (int dy = 0; dy < height; dy++) {

                setIfInside(
                        level,
                        cx + dx,
                        crownY + dy,
                        cz - 2,
                        dy > height / 2
                                ? Blocks.BLUE_ICE.defaultBlockState()
                                : Blocks.PACKED_ICE.defaultBlockState(),
                        chunkMinX,
                        chunkMinZ
                );
            }
        }
    }


    // =========================================================
    // КРИСТАЛЛ
    // =========================================================

    private static void buildCrystal(
            ServerLevel level,
            int x,
            int y,
            int z,
            int height,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dy = 0; dy < height; dy++) {

            setIfInside(
                    level,
                    x,
                    y + dy,
                    z,
                    dy >= height / 2
                            ? Blocks.BLUE_ICE.defaultBlockState()
                            : Blocks.PACKED_ICE.defaultBlockState(),
                    chunkMinX,
                    chunkMinZ
            );
        }

        /*
         * Основание кристалла.
         */
        setIfInside(
                level,
                x - 1,
                y,
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
                Blocks.PACKED_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x,
                y,
                z - 1,
                Blocks.BLUE_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );

        setIfInside(
                level,
                x,
                y,
                z + 1,
                Blocks.BLUE_ICE.defaultBlockState(),
                chunkMinX,
                chunkMinZ
        );
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

        level.setBlock(
                new BlockPos(x, y, z),
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
}