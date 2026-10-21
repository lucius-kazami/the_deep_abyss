
package com.akashi.abyss.abyss.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AbyssUnderwaterTunnels {

    private static final int SPACING = 64;
    private static final int RADIUS = 4;

    // Основной тоннель находится ниже поверхности суши.
    private static final int BOTTOM_Y = -472;
    private static final int TOP_Y = -466;

    // Герметичный грунт вокруг тоннелей.
    private static final int SHELL_BOTTOM = -475;
    private static final int SHELL_TOP = -451;

    private AbyssUnderwaterTunnels() {}

    public static void generateColumn(
            ServerLevel level,
            int x,
            int z,
            int floorTop,
            boolean isWater
    ) {
        // Плавные изгибы тоннелей.
        double bendX =
                (AbyssBiome1ChunkHandler.noise01(
                        x, z, 81001, 48) - 0.5D) * 12.0D;

        double bendZ =
                (AbyssBiome1ChunkHandler.noise01(
                        x, z, 81002, 48) - 0.5D) * 12.0D;

        double distanceX = distanceToGrid(x + bendX);
        double distanceZ = distanceToGrid(z + bendZ);

        // Пересекающиеся тоннели образуют сеть.
        boolean tunnel =
                distanceX <= RADIUS ||
                        distanceZ <= RADIUS;

        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();

        // Уплотняем грунт вокруг тоннелей.
        // Не трогаем уже существующую толщу воды.
        for (int y = SHELL_BOTTOM; y <= SHELL_TOP; y++) {

            BlockPos p = new BlockPos(x, y, z);

            if (y > floorTop) continue;

            level.setBlock(p, stone, 2);
        }

        if (!tunnel) return;

        // Создаём полностью заполненный водой тоннель.
        for (int y = BOTTOM_Y; y <= TOP_Y; y++) {

            BlockPos p = new BlockPos(x, y, z);

            level.setBlock(p, water, 2);
        }

        // Подводный вход в озеро, реку или море.
        if (isWater) {

            int seaLevel =
                    AbyssBiome1ChunkHandler.getSeaLevel();

            if (floorTop >= seaLevel) return;

            // Соединяем тоннель с существующей водой.
            int entranceTop = Math.max(TOP_Y, floorTop);

            for (int y = TOP_Y + 1; y <= entranceTop; y++) {

                BlockPos p = new BlockPos(x, y, z);

                level.setBlock(p, water, 2);
            }
        }
    }

    private static double distanceToGrid(double value) {
        double remainder = value
                - Math.floor(value / SPACING) * SPACING;

        return Math.min(
                remainder,
                SPACING - remainder
        );
    }
}