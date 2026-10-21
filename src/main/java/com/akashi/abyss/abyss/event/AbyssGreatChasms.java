package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssGreatChasms {

    public static final double RADIUS = 15.0; // диаметр 30 блоков

    // Центры пропастей: центр мира и 1500 блоков по оси X
    public static final int[][] CENTERS = {
            {0, 0},
            {1500, 0}
    };

    private static final int TOP_Y = -350;    // выше любых холмов
    private static final int BOTTOM_Y = -625; // ниже верха Биома 2 (-601)
    private static final int MARKER_Y = -450; // дно Биома 1: твёрдый блок, пока разлом не вырезан

    @SubscribeEvent(priority = EventPriority.LOW) // после рельефа, деревьев, шахт и пещер
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos pos = chunk.getPos();
        int chunkMinX = pos.getMinBlockX();
        int chunkMinZ = pos.getMinBlockZ();
        int chunkMaxX = chunkMinX + 15;
        int chunkMaxZ = chunkMinZ + 15;

        for (int[] center : CENTERS) {
            double cx = center[0] + 0.5;
            double cz = center[1] + 0.5;

            // Ближайшая к центру точка чанка: если она вне круга, чанк разлома не касается
            int nearX = (int) Math.max(chunkMinX, Math.min(chunkMaxX, center[0]));
            int nearZ = (int) Math.max(chunkMinZ, Math.min(chunkMaxZ, center[1]));
            double nd = Math.hypot(nearX + 0.5 - cx, nearZ + 0.5 - cz);
            if (nd > RADIUS) continue;

            // Уже вырезано? В этой точке на MARKER_Y без разлома всегда твёрдый блок
            if (level.getBlockState(new BlockPos(nearX, MARKER_Y, nearZ)).isAir()) continue;

            for (int x = chunkMinX; x <= chunkMaxX; x++) {
                for (int z = chunkMinZ; z <= chunkMaxZ; z++) {
                    double dist = Math.hypot(x + 0.5 - cx, z + 0.5 - cz);
                    if (dist > RADIUS) continue;
                    for (int y = BOTTOM_Y; y <= TOP_Y; y++) {
                        level.setBlock(new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(), 2);
                    }
                }
            }
            Abyss.LOGGER.info(">>> Разлом вырезан в чанке {}", pos);
        }
    }
}