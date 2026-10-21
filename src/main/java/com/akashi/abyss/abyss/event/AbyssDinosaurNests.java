
package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.akashi.abyss.abyss.block.MosasaurusEggBlock;
import com.akashi.abyss.abyss.block.ElasmosaurusEggBlock;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;

import java.util.Random;

public final class AbyssDinosaurNests {

    // В среднем одна попытка генерации на 30 чанков.
    private static final int NEST_RARITY = 50;
    private static final int MOSASAURUS_NEST_RARITY = 80;
    // В среднем одна попытка генерации на 40 чанков
    private static final int ELASMOSAURUS_NEST_RARITY = 40;
    private static final int ARCHELON_NEST_RARITY = 30;

    // Отступ от границ чанка.
    private static final int MARGIN = 3;

    private static final long NEST_SEED = 84629173L;

    private AbyssDinosaurNests() {
    }

    public static void generate(ServerLevel level, ChunkPos chunkPos) {

        // Только измерение Бездны.
        if (!level.dimension().equals(AbyssDimensions.BEZDNA)) {
            return;
        }

        Random random = new Random(
                level.getSeed()
                        ^ NEST_SEED
                        ^ ((long) chunkPos.x * 341873128712L)
                        ^ ((long) chunkPos.z * 132897987541L)
        );

        // Подводные гнёзда генерируются независимо от наземных
        generateMosasaurusNest(level, chunkPos);
        generateElasmosaurusNest(level, chunkPos);
        generateArchelonNest(level, chunkPos);

        if (random.nextInt(NEST_RARITY) != 0) {
            return;
        }

        int centerX = chunkPos.getMinBlockX()
                + MARGIN
                + random.nextInt(16 - MARGIN * 2);

        int centerZ = chunkPos.getMinBlockZ()
                + MARGIN
                + random.nextInt(16 - MARGIN * 2);

        BlockPos center = findSurface(level, centerX, centerZ);

        if (center == null) {
            return;
        }

        // Проверяем ровную площадку.
        if (!isSuitableArea(level, center)) {
            return;
        }

        Block eggBlock = switch (random.nextInt(24)) {
            case 0, 1, 2, 3 -> Abyss.TRICERATOPS_EGG.get();
            case 4, 5, 6 -> Abyss.STEGOSAURUS_EGG.get();
            case 7, 8, 9 -> Abyss.PARASAUROLOPHUS_EGG.get();
            case 10, 11, 12 -> Abyss.ANKYLOSAURUS_EGG.get();
            case 13, 14, 15 -> Abyss.BRACHIOSAURUS_EGG.get();
            case 16, 17 -> Abyss.TYRANNOSAURUS_EGG.get();
            case 18, 19 -> Abyss.SPINOSAURUS_EGG.get();
            case 20 -> Abyss.GIGANOTOSAURUS_EGG.get();
            default -> Abyss.THERIZINOSAURUS_EGG.get();
        };

        // Подготавливаем небольшую площадку из почвы Бездны.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                if (dx * dx + dz * dz > 5) {
                    continue;
                }

                BlockPos groundPos = center.offset(dx, -1, dz);

                // Не меняем рельеф по высоте.
                if (isGround(level.getBlockState(groundPos))) {
                    level.setBlock(
                            groundPos,
                            Abyss.ABYSS_SOIL.get().defaultBlockState(),
                            2
                    );
                }
            }
        }

        // Сначала центральное яйцо, затем дополнительные.
        int targetEggs = 1 + random.nextInt(4);
        int placedEggs = 0;

        int[][] offsets = {
                {0, 0},
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1},
                {1, 1},
                {-1, -1},
                {1, -1},
                {-1, 1}
        };

        for (int i = 0; i < offsets.length && placedEggs < targetEggs; i++) {

            int[] offset = offsets[i];

            BlockPos eggPos = center.offset(offset[0], 0, offset[1]);

            if (!level.getBlockState(eggPos).isAir()) {
                continue;
            }

            if (!isGround(level.getBlockState(eggPos.below()))) {
                continue;
            }

            level.setBlock(
                    eggPos,
                    eggBlock.defaultBlockState(),
                    3
            );

            placedEggs++;
        }

        if (placedEggs > 0) {
            Abyss.LOGGER.info(
                    "Сгенерировано гнездо динозавра: {} яиц, позиция {}",
                    placedEggs,
                    center
            );
        }
    }

    private static BlockPos findSurface(
            ServerLevel level,
            int x,
            int z
    ) {

        // Ищем поверхность только внутри первого биома.
        for (int y = AbyssBiome1ChunkHandler.CEILING_Y - 1;
             y > AbyssBiome1ChunkHandler.FLOOR_Y;
             y--) {

            BlockPos ground = new BlockPos(x, y, z);

            if (!isGround(level.getBlockState(ground))) {
                continue;
            }

            BlockPos above = ground.above();

            if (!level.getBlockState(above).isAir()) {
                continue;
            }

            if (!level.getBlockState(above.above()).isAir()) {
                continue;
            }

            return above;
        }

        return null;
    }

    private static boolean isSuitableArea(
            ServerLevel level,
            BlockPos center
    ) {

        int centerGroundY = center.getY() - 1;

        // Проверяем площадку 5x5.
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {

                BlockPos ground = center.offset(dx, -1, dz);

                if (!isGround(level.getBlockState(ground))) {
                    return false;
                }

                // Не строим гнёзда на склонах и возле препятствий.
                BlockPos space = ground.above();

                if (!level.getBlockState(space).isAir()) {
                    return false;
                }

                if (!level.getBlockState(space.above()).isAir()) {
                    return false;
                }

                if (ground.getY() != centerGroundY) {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isGround(BlockState state) {

        return state.is(Abyss.ABYSS_GRASS.get())
                || state.is(Abyss.ABYSS_GRASS_WET.get())
                || state.is(Abyss.ABYSS_SOIL.get());
    }


    private static void generateMosasaurusNest(
            ServerLevel level,
            ChunkPos chunkPos
    ) {
        Random random = new Random(
                level.getSeed()
                        ^ 918273645L
                        ^ ((long) chunkPos.x * 341873128712L)
                        ^ ((long) chunkPos.z * 132897987541L)
        );

        if (random.nextInt(MOSASAURUS_NEST_RARITY) != 0) {
            return;
        }

        int x = chunkPos.getMinBlockX() + 4 + random.nextInt(8);
        int z = chunkPos.getMinBlockZ() + 4 + random.nextInt(8);

        // Ищем дно водоёма первого биома.
        int seaLevel = AbyssBiome1ChunkHandler.getSeaLevel();

        for (int y = seaLevel - 1;
             y > AbyssBiome1ChunkHandler.FLOOR_Y - 60;
             y--) {

            BlockPos ground = new BlockPos(x, y, z);
            BlockPos eggPos = ground.above();

            // Под яйцом должен быть твёрдый грунт.
            if (!level.getBlockState(ground).isSolid()) {
                continue;
            }

            // Яйцо должно находиться под водой.
            if (!level.getFluidState(eggPos)
                    .is(net.minecraft.tags.FluidTags.WATER)) {
                continue;
            }

            if (!level.getFluidState(eggPos.above())
                    .is(net.minecraft.tags.FluidTags.WATER)) {
                continue;
            }

            BlockState eggState = Abyss.MOSASAURUS_EGG.get()
                    .defaultBlockState()
                    .setValue(MosasaurusEggBlock.WATERLOGGED, true);

            level.setBlock(eggPos, eggState, 3);

            Abyss.LOGGER.info(
                    "Сгенерировано подводное гнездо Мозазавра: {}",
                    eggPos
            );

            return;
        }
    }

    private static void generateElasmosaurusNest(
            ServerLevel level,
            ChunkPos chunkPos
    ) {
        Random random = new Random(
                level.getSeed()
                        ^ 729384651L
                        ^ ((long) chunkPos.x * 341873128712L)
                        ^ ((long) chunkPos.z * 132897987541L)
        );

        if (random.nextInt(ELASMOSAURUS_NEST_RARITY) != 0) {
            return;
        }

        // Несколько попыток найти подходящее дно
        for (int attempt = 0; attempt < 8; attempt++) {

            int x = chunkPos.getMinBlockX() + 4 + random.nextInt(8);
            int z = chunkPos.getMinBlockZ() + 4 + random.nextInt(8);

            int seaLevel = AbyssBiome1ChunkHandler.getSeaLevel();

            // Ищем дно под водой
            for (int y = seaLevel - 1;
                 y > AbyssBiome1ChunkHandler.FLOOR_Y - 60;
                 y--) {

                BlockPos ground = new BlockPos(x, y, z);
                BlockPos eggPos = ground.above();

                // Яйца появляются на песчаном дне
                if (!level.getBlockState(ground).is(Blocks.SAND)) {
                    continue;
                }

                // Минимальная глубина воды — 8 блоков
                if (seaLevel - eggPos.getY() < 8) {
                    continue;
                }

                // Проверяем наличие воды
                if (!level.getFluidState(eggPos).is(FluidTags.WATER)) {
                    continue;
                }

                if (!level.getFluidState(eggPos.above()).is(FluidTags.WATER)) {
                    continue;
                }

                // Проверяем допустимую глубину подводного гнезда
                if (eggPos.getY() <= AbyssBiome1ChunkHandler.FLOOR_Y - 60
                        || eggPos.getY() >= AbyssBiome1ChunkHandler.getSeaLevel()) {
                    continue;
                }

                // От 1 до 3 яиц в гнезде
                int eggCount = 1 + random.nextInt(3);
                int placed = 0;

                for (int dx = -1; dx <= 1 && placed < eggCount; dx++) {
                    for (int dz = -1; dz <= 1 && placed < eggCount; dz++) {

                        BlockPos nearbyEggPos = eggPos.offset(dx, 0, dz);

                        if (!level.getBlockState(
                                nearbyEggPos.below()).is(Blocks.SAND)) {
                            continue;
                        }

                        if (!level.getFluidState(
                                nearbyEggPos).is(FluidTags.WATER)) {
                            continue;
                        }

                        if (!level.getFluidState(
                                nearbyEggPos.above()).is(FluidTags.WATER)) {
                            continue;
                        }

                        BlockState eggState =
                                Abyss.ELASMOSAURUS_EGG.get()
                                        .defaultBlockState()
                                        .setValue(
                                                ElasmosaurusEggBlock.WATERLOGGED,
                                                true
                                        );

                        level.setBlock(nearbyEggPos, eggState, 3);
                        placed++;
                    }
                }

                if (placed > 0) {
                    Abyss.LOGGER.info(
                            ">>> Сгенерировано гнездо Эласмозавра: {} яиц, позиция {}",
                            placed,
                            eggPos
                    );
                    return;
                }
            }
        }
    }
    private static void generateArchelonNest(
            ServerLevel level,
            ChunkPos chunkPos
    ) {
        RandomSource random = RandomSource.create(
                level.getSeed()
                        ^ 573829104L
                        ^ ((long) chunkPos.x * 341873128712L)
                        ^ ((long) chunkPos.z * 132897987541L)
        );

        if (random.nextInt(ARCHELON_NEST_RARITY) != 0) {
            return;
        }

        int seaLevel = AbyssBiome1ChunkHandler.getSeaLevel();

        int x = chunkPos.getMinBlockX() + random.nextInt(16);
        int z = chunkPos.getMinBlockZ() + random.nextInt(16);

        // Ищем место у поверхности воды
        for (int y = seaLevel + 3; y >= seaLevel - 5; y--) {

            BlockPos pos = new BlockPos(x, y, z);
            BlockPos below = pos.below();

            if (!AbyssBiome1ChunkHandler.isInsideBiome1(pos.getY())) {
                continue;
            }

            if (!level.getBlockState(pos).isAir()) {
                continue;
            }

            if (!level.getBlockState(below).isFaceSturdy(
                    level, below, Direction.UP)) {
                continue;
            }

            // Рядом обязательно должна быть вода
            boolean nearWater = false;

            for (BlockPos nearby : BlockPos.betweenClosed(
                    pos.offset(-4, -2, -4),
                    pos.offset(4, 0, 4))) {

                if (level.getFluidState(nearby).is(FluidTags.WATER)) {
                    nearWater = true;
                    break;
                }
            }

            if (!nearWater) {
                continue;
            }

            int count = 2 + random.nextInt(3);

            for (int i = 0; i < count; i++) {
                BlockPos eggPos = pos.offset(
                        random.nextInt(3) - 1,
                        0,
                        random.nextInt(3) - 1
                );

                BlockPos eggBelow = eggPos.below();

                if (level.getBlockState(eggPos).isAir()
                        && level.getBlockState(eggBelow).isFaceSturdy(
                        level, eggBelow, Direction.UP)) {

                    level.setBlock(
                            eggPos,
                            Abyss.ARCHELON_EGG.get().defaultBlockState(),
                            3
                    );
                }
            }

            return;
        }
    }
}
