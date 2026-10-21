package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleTowers {

    private CastleTowers() {}

    private static final int TOWER_RADIUS = 10;
    private static final int TOWER_HEIGHT = 52;

    private static final int OFFSET_X = 56;
    private static final int OFFSET_Z = 70;

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        buildTower(
                level,
                centerX - OFFSET_X,
                baseY,
                centerZ - OFFSET_Z,
                chunkMinX,
                chunkMinZ
        );

        buildTower(
                level,
                centerX + OFFSET_X,
                baseY,
                centerZ - OFFSET_Z,
                chunkMinX,
                chunkMinZ
        );

        buildTower(
                level,
                centerX - OFFSET_X,
                baseY,
                centerZ + OFFSET_Z,
                chunkMinX,
                chunkMinZ
        );

        buildTower(
                level,
                centerX + OFFSET_X,
                baseY,
                centerZ + OFFSET_Z,
                chunkMinX,
                chunkMinZ
        );
    }

    private static void buildTower(
            ServerLevel level,
            int cx,
            int baseY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int dx = -TOWER_RADIUS; dx <= TOWER_RADIUS; dx++) {
            for (int dz = -TOWER_RADIUS; dz <= TOWER_RADIUS; dz++) {

                int x = cx + dx;
                int z = cz + dz;

                if (!insideChunk(x, z, chunkMinX, chunkMinZ)) {
                    continue;
                }

                double distance =
                        Math.sqrt(dx * dx + dz * dz);

                /*
                 * Стена башни толщиной примерно 3 блока.
                 */
                if (distance < TOWER_RADIUS - 3
                        || distance > TOWER_RADIUS) {
                    continue;
                }

                for (int dy = 1; dy <= TOWER_HEIGHT; dy++) {

                    BlockState state;

                    long hash = hash(x, baseY + dy, z);

                    if (dy > 43) {
                        state = Blocks.PACKED_ICE.defaultBlockState();

                    } else if (Math.floorMod(hash, 31) == 0) {
                        state = Blocks.CRACKED_DEEPSLATE_BRICKS
                                .defaultBlockState();

                    } else if (dy % 11 == 0) {
                        state = Blocks.BLUE_ICE.defaultBlockState();

                    } else {
                        state = Blocks.DEEPSLATE_BRICKS
                                .defaultBlockState();
                    }

                    level.setBlock(
                            new BlockPos(x, baseY + dy, z),
                            state,
                            2
                    );
                }
            }
        }

        buildRoof(
                level,
                cx,
                baseY + TOWER_HEIGHT,
                cz,
                chunkMinX,
                chunkMinZ
        );
    }

    /*
     * Высокая ледяная крыша.
     */
    private static void buildRoof(
            ServerLevel level,
            int cx,
            int roofY,
            int cz,
            int chunkMinX,
            int chunkMinZ
    ) {

        for (int layer = 0; layer <= 24; layer++) {

            double progress = layer / 24.0;

            int radius =
                    Math.max(
                            0,
                            (int) Math.round(
                                    10 * (1.0 - progress)
                            )
                    );

            int y = roofY + layer + 1;

            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {

                    if (dx * dx + dz * dz > radius * radius) {
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

                    /*
                     * Только оболочка конуса.
                     */
                    int innerRadius =
                            Math.max(0, radius - 2);

                    if (radius > 2
                            && dx * dx + dz * dz
                            < innerRadius * innerRadius) {
                        continue;
                    }

                    BlockState state;

                    if (layer > 16) {
                        state = Blocks.BLUE_ICE.defaultBlockState();
                    } else {
                        state = Blocks.PACKED_ICE.defaultBlockState();
                    }

                    level.setBlock(
                            new BlockPos(x, y, z),
                            state,
                            2
                    );
                }
            }
        }

        /*
         * Самый верхний шпиль.
         */
        if (insideChunk(cx, cz, chunkMinX, chunkMinZ)) {

            for (int dy = 25; dy <= 32; dy++) {

                level.setBlock(
                        new BlockPos(
                                cx,
                                roofY + dy,
                                cz
                        ),
                        Blocks.BLUE_ICE.defaultBlockState(),
                        2
                );
            }
        }
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