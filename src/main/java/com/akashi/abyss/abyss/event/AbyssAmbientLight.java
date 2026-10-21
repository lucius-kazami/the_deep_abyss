package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssAmbientLight {

    public static final int GRID_STEP = 3;           // было 4 — плотнее сетка
    public static final int HEIGHT_ABOVE_FLOOR = 5;

    private static final Map<ChunkPos, List<BlockPos>> POINTS = new ConcurrentHashMap<>();
    private static int lastLevel = -1;
    private static int tickCounter = 0;

    public static void registerLightPoint(ChunkPos pos, BlockPos p) {
        POINTS.computeIfAbsent(pos, k -> new CopyOnWriteArrayList<>()).add(p);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;

        if (++tickCounter < 40) return;
        tickCounter = 0;

        int newLevel = computeLightLevel(level.getDayTime());
        if (newLevel == lastLevel) return;
        lastLevel = newLevel;

        BlockState state = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, newLevel);
        for (Map.Entry<ChunkPos, List<BlockPos>> entry : POINTS.entrySet()) {
            if (!level.hasChunk(entry.getKey().x, entry.getKey().z)) continue;
            for (BlockPos p : entry.getValue()) {
                if (level.getBlockState(p).is(Blocks.LIGHT)) {
                    level.setBlock(p, state, 2);
                }
            }
        }
    }

    public static int computeLightLevel(long dayTime) {
        long t = dayTime % 24000;
        if (t < 12000) return 15;                                 // день — максимум
        if (t < 13000) return 15 - (int) ((t - 12000) * 8 / 1000); // закат: 15 -> 7
        if (t < 23000) return 7;                                  // ночь — светлее, чем раньше
        return 7 + (int) ((t - 23000) * 8 / 1000);                // рассвет: 7 -> 15
    }
}