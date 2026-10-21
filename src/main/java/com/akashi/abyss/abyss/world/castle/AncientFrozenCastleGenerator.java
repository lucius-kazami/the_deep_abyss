package com.akashi.abyss.abyss.world.castle;

import com.akashi.abyss.abyss.event.AbyssBiome1ChunkHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

public final class AncientFrozenCastleGenerator {

    private AncientFrozenCastleGenerator() {}

    // ============================================================
    // НАСТРОЙКИ
    // ============================================================

    // Центр огромной дыры.
    // Пока считаем, что она находится около 0 / 0.
    private static final int ABYSS_CENTER_X = 0;
    private static final int ABYSS_CENTER_Z = 0;

    private static final int MIN_DISTANCE = 900;
    private static final int MAX_DISTANCE = 1100;

    // Для первой проверки ищем водную область радиусом 35 блоков.
    // Позже увеличим под настоящий огромный замок.
    private static final int WATER_CHECK_RADIUS = 35;

    // Через сколько градусов проверять следующее направление.
    private static final int ANGLE_STEP = 10;

    // ============================================================
    // ОСНОВНОЙ МЕТОД
    // ============================================================

    public static void generate(
            ServerLevel level,
            ChunkPos loadedChunk
    ) {

        AncientFrozenCastleSavedData data =
                level.getDataStorage().computeIfAbsent(
                        AncientFrozenCastleSavedData.TYPE
                );

        /*
         * Если координаты замка ещё не выбраны,
         * пытаемся найти подходящее море.
         */
        if (!data.isGenerated()) {

            BlockPos castlePos = findCastlePosition();

            if (castlePos == null) {
                return;
            }

            data.markGenerated(castlePos);

            System.out.println(
                    "[ABYSS] Ancient Frozen Castle position: "
                            + castlePos
            );
        }

        BlockPos castlePos = data.getPosition();

        if (!chunkIntersectsCastle(
                loadedChunk,
                castlePos
        )) {
            return;
        }

        AncientFrozenCastleBuilder.buildChunk(
                level,
                castlePos,
                loadedChunk
        );

        System.out.println(
                "[ABYSS] Ancient Frozen Castle generated at "
                        + castlePos
        );
    }

    // ============================================================
    // ПОИСК МЕСТА
    // ============================================================

    private static BlockPos findCastlePosition() {

        /*
         * Ищем кольцами:
         *
         * 900
         * 950
         * 1000
         * 1050
         * 1100
         */

        for (int distance = MIN_DISTANCE;
             distance <= MAX_DISTANCE;
             distance += 50) {

            for (int angle = 0;
                 angle < 360;
                 angle += ANGLE_STEP) {

                double radians =
                        Math.toRadians(angle);

                int x =
                        ABYSS_CENTER_X
                                + (int) Math.round(
                                Math.cos(radians) * distance
                        );

                int z =
                        ABYSS_CENTER_Z
                                + (int) Math.round(
                                Math.sin(radians) * distance
                        );

                if (!AbyssBiome1ChunkHandler
                        .isLargeWaterArea(
                                x,
                                z,
                                WATER_CHECK_RADIUS
                        )) {
                    continue;
                }

                return new BlockPos(
                        x,
                        AbyssBiome1ChunkHandler.getSeaLevel() + 1,
                        z
                );
            }
        }

        return null;
    }

    private static boolean chunkIntersectsCastle(
            ChunkPos chunk,
            BlockPos center
    ) {

        int marginX =
                CastleFoundation.HALF_WIDTH + 16;

        int marginZ =
                CastleFoundation.HALF_LENGTH + 16;

        int chunkMinX =
                chunk.getMinBlockX();

        int chunkMaxX =
                chunkMinX + 15;

        int chunkMinZ =
                chunk.getMinBlockZ();

        int chunkMaxZ =
                chunkMinZ + 15;

        int castleMinX =
                center.getX() - marginX;

        int castleMaxX =
                center.getX() + marginX;

        int castleMinZ =
                center.getZ() - marginZ;

        int castleMaxZ =
                center.getZ() + marginZ;

        return chunkMaxX >= castleMinX
                && chunkMinX <= castleMaxX
                && chunkMaxZ >= castleMinZ
                && chunkMinZ <= castleMaxZ;
    }
}