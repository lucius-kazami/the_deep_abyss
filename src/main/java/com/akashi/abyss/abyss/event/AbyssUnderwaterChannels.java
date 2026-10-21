
package com.akashi.abyss.abyss.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AbyssUnderwaterChannels {

    // Расстояние между основными тоннелями
    private static final int SPACING = 48;

    // Ширина тоннеля: 7 блоков
    private static final int HALF_WIDTH = 3;

    // Высота тоннеля: 7 блоков
    private static final int TUNNEL_BOTTOM = -502;
    private static final int TUNNEL_TOP = -496;

    // Защитная оболочка вокруг тоннелей
    private static final int SHELL_BOTTOM = -504;
    private static final int SHELL_TOP = -494;

    private AbyssUnderwaterChannels() {}

    /**
     * Генерирует вертикальный срез подводной сети.
     * Вызывается для каждой колонки x/z первого биома.
     */
    public static void generateColumn(
            ServerLevel level,
            int x,
            int z,
            int floorTop,
            boolean isWater
    ) {
        int distanceX = distanceToGrid(x);
        int distanceZ = distanceToGrid(z);

        // Два направления образуют связанную сеть.
        boolean insideTunnel =
                distanceX <= HALF_WIDTH ||
                        distanceZ <= HALF_WIDTH;

        BlockState solid = Blocks.STONE.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();

        // Сначала создаём герметичную каменную оболочку.
        for (int y = SHELL_BOTTOM; y <= SHELL_TOP; y++) {
            level.setBlock(new BlockPos(x, y, z), solid, 2);
        }

        if (!insideTunnel) return;

        // Внутри тоннеля исключительно вода.
        for (int y = TUNNEL_BOTTOM; y <= TUNNEL_TOP; y++) {
            level.setBlock(new BlockPos(x, y, z), water, 2);
        }

        // Входы создаются только под настоящими водоёмами.
        if (!isWater) return;

        // В первой версии подключаем только глубокие участки.
        // Под дном должно быть достаточно грунта для стенок входа.
        if (floorTop > -482) return;

        // Защита от случайного выхода выше уровня воды.
        int seaLevel = AbyssBiome1ChunkHandler.getSeaLevel();
        if (floorTop >= seaLevel) return;

        // Соединяем тоннель с водой над дном.
        // Проход полностью затоплен.
        for (int y = TUNNEL_TOP + 1; y <= floorTop; y++) {
            level.setBlock(new BlockPos(x, y, z), water, 2);
        }
    }

    private static int distanceToGrid(int coordinate) {
        int remainder = Math.floorMod(coordinate, SPACING);
        return Math.min(remainder, SPACING - remainder);
    }
}
