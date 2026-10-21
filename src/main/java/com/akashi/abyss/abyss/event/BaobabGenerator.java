package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.ModFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public final class BaobabGenerator {
    private BaobabGenerator() {}

    public static final int HEIGHT = 16;       // высота ствола
    public static final int WATER_DEPTH = 3;   // глубина бассейна
    private static final double OUTER = 3.5;   // радиус ствола
    private static final double INNER = 1.6;   // радиус полости (3x3)
    private static final int[] CANOPY_RADII = {7, 5, 3};

    public static void generate(ServerLevel level, BlockPos origin, Random rnd) {
        BlockState log = Abyss.BAOBAB_LOG.get().defaultBlockState();
        BlockState water = ModFluids.HEALING_WATER_BLOCK.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState leaves = Abyss.YELLOW_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        int baseY = origin.getY();
        int topY = baseY + HEIGHT;
        int bottomY = baseY - WATER_DEPTH - 1;

        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > OUTER) continue;
                boolean inner = d <= INNER;

                for (int y = bottomY; y <= topY; y++) {
                    BlockPos p = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    BlockState state;
                    if (y == topY || y == bottomY) state = log;        // крышка и дно
                    else if (inner) state = y < baseY ? water : air;   // бассейн и полость
                    else state = log;                                  // сплошная стенка
                    level.setBlock(p, state, 2);
                }
            }
        }

        // Сплошная крона-зонтик: радиус 7 вместе со стволом целиком внутри чанка
        for (int i = 0; i < CANOPY_RADII.length; i++) {
            int r = CANOPY_RADII[i];
            int y = topY + i;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dz * dz > r * r) continue;
                    BlockPos p = new BlockPos(origin.getX() + dx, y, origin.getZ() + dz);
                    if (level.getBlockState(p).isAir()) level.setBlock(p, leaves, 2);
                }
            }
        }
    }
}