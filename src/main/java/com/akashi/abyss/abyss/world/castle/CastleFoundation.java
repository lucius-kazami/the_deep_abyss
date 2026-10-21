package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleFoundation {

    private CastleFoundation() {}

    /*
     * Размер основного острова-крепости.
     *
     * Общий размер примерно:
     * 141 x 171 блок.
     */
    public static final int HALF_WIDTH = 70;
    public static final int HALF_LENGTH = 85;

    /*
     * Строим фундамент только в указанном чанке.
     */
    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        int startX = Math.max(
                centerX - HALF_WIDTH,
                chunkMinX
        );

        int endX = Math.min(
                centerX + HALF_WIDTH,
                chunkMinX + 15
        );

        int startZ = Math.max(
                centerZ - HALF_LENGTH,
                chunkMinZ
        );

        int endZ = Math.min(
                centerZ + HALF_LENGTH,
                chunkMinZ + 15
        );

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {

                int dx = x - centerX;
                int dz = z - centerZ;

                if (!insideIsland(dx, dz)) {
                    continue;
                }

                buildSurface(
                        level,
                        x,
                        baseY,
                        z,
                        dx,
                        dz
                );

                buildUnderwaterFoundation(
                        level,
                        x,
                        baseY,
                        z,
                        dx,
                        dz
                );
            }
        }
    }

    /*
     * Форма острова.
     *
     * Не обычный прямоугольник.
     * Углы срезаются и форма становится массивнее.
     */
    private static boolean insideIsland(
            int dx,
            int dz
    ) {

        double nx =
                dx / (double) HALF_WIDTH;

        double nz =
                dz / (double) HALF_LENGTH;

        double ellipse =
                nx * nx + nz * nz;

        /*
         * Основной овал.
         */
        if (ellipse <= 1.0) {
            return true;
        }

        /*
         * Центральная часть немного расширена,
         * чтобы стены замка имели место.
         */
        return Math.abs(dx) <= 55
                && Math.abs(dz) <= 78;
    }

    /*
     * Верхняя поверхность крепости.
     */
    private static void buildSurface(
            ServerLevel level,
            int x,
            int y,
            int z,
            int dx,
            int dz
    ) {

        BlockState surface = chooseSurfaceBlock(
                x,
                z
        );

        /*
         * Основная площадка.
         */
        level.setBlock(
                new BlockPos(x, y, z),
                surface,
                2
        );

        /*
         * Второй слой.
         */
        level.setBlock(
                new BlockPos(x, y - 1, z),
                chooseFoundationBlock(x, y - 1, z),
                2
        );

        /*
         * Третий слой.
         */
        level.setBlock(
                new BlockPos(x, y - 2, z),
                chooseFoundationBlock(x, y - 2, z),
                2
        );

        /*
         * Ледяные трещины.
         *
         * Они не случайные, поэтому после перезапуска
         * рисунок остаётся одинаковым.
         */
        long hash = hash(x, z, 7712);

        if (Math.floorMod(hash, 47) == 0) {
            level.setBlock(
                    new BlockPos(x, y, z),
                    Blocks.BLUE_ICE.defaultBlockState(),
                    2
            );
        }

        /*
         * Больше льда ближе к краям острова.
         */
        double edgeX =
                Math.abs(dx) / (double) HALF_WIDTH;

        double edgeZ =
                Math.abs(dz) / (double) HALF_LENGTH;

        double edge =
                Math.max(edgeX, edgeZ);

        if (edge > 0.82) {

            if (Math.floorMod(hash, 4) != 0) {
                level.setBlock(
                        new BlockPos(x, y, z),
                        Blocks.PACKED_ICE.defaultBlockState(),
                        2
                );
            }
        }
    }

    /*
     * Подводное основание.
     */
    private static void buildUnderwaterFoundation(
            ServerLevel level,
            int x,
            int baseY,
            int z,
            int dx,
            int dz
    ) {

        /*
         * Не заполняем всё пространство сплошным кубом.
         *
         * Делаем массивные опоры.
         */

        boolean mainSupport =
                Math.floorMod(x, 12) <= 4
                        &&
                        Math.floorMod(z, 12) <= 4;

        /*
         * По краю фундамент более массивный.
         */
        double nx =
                Math.abs(dx) / (double) HALF_WIDTH;

        double nz =
                Math.abs(dz) / (double) HALF_LENGTH;

        boolean outerSupport =
                Math.max(nx, nz) > 0.78;

        if (!mainSupport && !outerSupport) {
            return;
        }

        /*
         * В твоём первом биоме глубина воды максимум
         * около 16 блоков.
         *
         * Поэтому 20 блоков достаточно, чтобы опоры
         * гарантированно дошли до грунта.
         */
        for (int depth = 3; depth <= 20; depth++) {

            int y = baseY - depth;

            BlockPos pos =
                    new BlockPos(x, y, z);

            /*
             * Если дошли до твёрдого дна,
             * дальше не копаем.
             */
            if (!level.getBlockState(pos).is(Blocks.WATER)
                    && !level.getBlockState(pos).isAir()) {
                break;
            }

            BlockState state;

            long h = hash(x, z, depth);

            if (Math.floorMod(h, 13) == 0) {
                state =
                        Blocks.CRACKED_DEEPSLATE_BRICKS
                                .defaultBlockState();

            } else if (Math.floorMod(h, 9) == 0) {
                state =
                        Blocks.PACKED_ICE
                                .defaultBlockState();

            } else {
                state =
                        Blocks.DEEPSLATE_BRICKS
                                .defaultBlockState();
            }

            level.setBlock(
                    pos,
                    state,
                    2
            );
        }
    }

    private static BlockState chooseSurfaceBlock(
            int x,
            int z
    ) {

        long h = hash(x, z, 991);

        int value =
                Math.floorMod(h, 100);

        if (value < 6) {
            return Blocks.BLUE_ICE
                    .defaultBlockState();
        }

        if (value < 18) {
            return Blocks.PACKED_ICE
                    .defaultBlockState();
        }

        if (value < 28) {
            return Blocks.CRACKED_DEEPSLATE_BRICKS
                    .defaultBlockState();
        }

        if (value < 45) {
            return Blocks.POLISHED_DEEPSLATE
                    .defaultBlockState();
        }

        return Blocks.DEEPSLATE_BRICKS
                .defaultBlockState();
    }

    private static BlockState chooseFoundationBlock(
            int x,
            int y,
            int z
    ) {

        long h =
                hash(x + y, z, 8127);

        if (Math.floorMod(h, 11) == 0) {
            return Blocks.CRACKED_DEEPSLATE_BRICKS
                    .defaultBlockState();
        }

        return Blocks.DEEPSLATE_BRICKS
                .defaultBlockState();
    }

    private static long hash(
            int x,
            int z,
            int seed
    ) {

        long h =
                (long) x * 341873128712L
                        +
                        (long) z * 132897987541L
                        +
                        seed * 7919L;

        h = (h ^ (h >>> 33))
                * 0xff51afd7ed558ccdL;

        h = (h ^ (h >>> 33))
                * 0xc4ceb9fe1a85ec53L;

        return h ^ (h >>> 33);
    }
}