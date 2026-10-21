package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleWalls {

    private CastleWalls() {}

    private static final int WALL_X = 58;
    private static final int WALL_Z = 72;

    private static final int WALL_HEIGHT = 27;
    private static final int WALL_THICKNESS = 3;

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int startX = Math.max(centerX - WALL_X, chunkMinX);
        int endX = Math.min(centerX + WALL_X, chunkMinX + 15);

        int startZ = Math.max(centerZ - WALL_Z, chunkMinZ);
        int endZ = Math.min(centerZ + WALL_Z, chunkMinZ + 15);

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {

                int dx = Math.abs(x - centerX);
                int dz = Math.abs(z - centerZ);

                boolean northSouthWall =
                        dz >= WALL_Z - WALL_THICKNESS
                                && dz <= WALL_Z;

                boolean eastWestWall =
                        dx >= WALL_X - WALL_THICKNESS
                                && dx <= WALL_X;

                if (!northSouthWall && !eastWestWall) {
                    continue;
                }

                /*
                 * Не перекрываем центральные главные ворота.
                 */
                boolean mainGate =
                        z >= centerZ + WALL_Z - WALL_THICKNESS
                                && Math.abs(x - centerX) <= 9;

                if (mainGate) {
                    continue;
                }

                buildWallColumn(level, x, baseY, z);
            }
        }
    }

    private static void buildWallColumn(
            ServerLevel level,
            int x,
            int baseY,
            int z
    ) {

        for (int dy = 1; dy <= WALL_HEIGHT; dy++) {

            BlockState state;

            long hash = hash(x, baseY + dy, z);

            if (dy >= WALL_HEIGHT - 2) {
                state = Blocks.POLISHED_DEEPSLATE.defaultBlockState();

            } else if (Math.floorMod(hash, 29) == 0) {
                state = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();

            } else if (Math.floorMod(hash, 17) == 0) {
                state = Blocks.PACKED_ICE.defaultBlockState();

            } else {
                state = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
            }

            level.setBlock(
                    new BlockPos(x, baseY + dy, z),
                    state,
                    2
            );
        }

        /*
         * Зубцы.
         */
        if (Math.floorMod(x + z, 4) < 2) {

            for (int dy = WALL_HEIGHT + 1;
                 dy <= WALL_HEIGHT + 4;
                 dy++) {

                level.setBlock(
                        new BlockPos(x, baseY + dy, z),
                        Blocks.DEEPSLATE_BRICKS.defaultBlockState(),
                        2
                );
            }
        }

        /*
         * Иногда ледяной шпиль.
         */
        if (Math.floorMod(hash(x, baseY, z), 31) == 0) {

            for (int dy = WALL_HEIGHT + 1;
                 dy <= WALL_HEIGHT + 8;
                 dy++) {

                level.setBlock(
                        new BlockPos(x, baseY + dy, z),
                        Blocks.BLUE_ICE.defaultBlockState(),
                        2
                );
            }
        }
    }

    private static long hash(int x, int y, int z) {

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