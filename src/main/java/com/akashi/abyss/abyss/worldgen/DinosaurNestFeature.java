package com.akashi.abyss.abyss.worldgen;

import com.akashi.abyss.abyss.Abyss;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.tags.FluidTags;

public class DinosaurNestFeature extends Feature<NoneFeatureConfiguration> {

    public DinosaurNestFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();

        BlockPos ground = origin;

        while (ground.getY() > level.getMinY()
                && level.isEmptyBlock(ground)) {
            ground = ground.below();
        }

        if (!level.getBlockState(ground).isSolidRender()) {
            return false;
        }

        BlockPos center = ground.above();

        // Создаём основание гнезда.
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x * x + z * z > 5) {
                    continue;
                }

                BlockPos pos = center.offset(x, -1, z);

                level.setBlock(
                        pos,
                        random.nextBoolean()
                                ? Blocks.COARSE_DIRT.defaultBlockState()
                                : Blocks.PODZOL.defaultBlockState(),
                        3
                );
            }
        }

        // Выбираем один из пяти видов яиц.
        BlockState egg = switch (random.nextInt(5)) {
            case 0 -> Abyss.TRICERATOPS_EGG.get().defaultBlockState();
            case 1 -> Abyss.STEGOSAURUS_EGG.get().defaultBlockState();
            case 2 -> Abyss.PARASAUROLOPHUS_EGG.get().defaultBlockState();
            case 3 -> Abyss.ANKYLOSAURUS_EGG.get().defaultBlockState();
            default -> Abyss.BRACHIOSAURUS_EGG.get().defaultBlockState();
        };

        // Размещаем яйца.
        int eggCount = 1 + random.nextInt(4);
        int placed = 0;

        for (int attempt = 0; attempt < 12 && placed < eggCount; attempt++) {
            int x = random.nextInt(3) - 1;
            int z = random.nextInt(3) - 1;

            BlockPos eggPos = center.offset(x, 0, z);

            if (level.isEmptyBlock(eggPos)
                    && level.getBlockState(eggPos.below()).isSolidRender()) {

                level.setBlock(eggPos, egg, 3);
                placed++;
            }
        }

        return placed > 0;
    }
    private boolean placeElasmosaurusNest(
            WorldGenLevel level,
            RandomSource random,
            BlockPos center
    ) {
        // Проверяем, что место находится под водой
        if (!level.getFluidState(center).is(FluidTags.WATER)) {
            return false;
        }

        // Проверяем наличие твёрдого дна
        if (!level.getBlockState(center.below()).isSolid()) {
            return false;
        }

        BlockState egg = Abyss.ELASMOSAURUS_EGG.get()
                .defaultBlockState();

        int placed = 0;
        int eggCount = 1 + random.nextInt(3);

        for (int attempt = 0; attempt < 12 && placed < eggCount; attempt++) {

            BlockPos eggPos = center.offset(
                    random.nextInt(3) - 1,
                    0,
                    random.nextInt(3) - 1
            );

            if (!level.getFluidState(eggPos).is(FluidTags.WATER)) {
                continue;
            }

            if (!level.getBlockState(eggPos.below()).isSolid()) {
                continue;
            }

            level.setBlock(eggPos, egg, 3);
            placed++;
        }

        return placed > 0;
    }
}