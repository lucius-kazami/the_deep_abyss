package com.akashi.abyss.abyss.block;

import com.akashi.abyss.abyss.Abyss;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;


public class EmptyOreBlock extends Block {

    // =========================================================
    // НАСТРОЙКИ
    // =========================================================

    // Minecraft = 20 тиков в секунду
    // 20 * 5 = 5 секунд
    private static final int TRANSFORM_TIME = 20 * 60;


    // =========================================================
    // КОНСТРУКТОР
    // =========================================================

    public EmptyOreBlock(Properties properties) {
        super(properties);
    }


    // =========================================================
    // УСТАНОВКА БЛОКА
    // =========================================================

    @Override
    public void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {

        super.onPlace(
                state,
                level,
                pos,
                oldState,
                movedByPiston
        );

        if (!level.isClientSide()
                && level instanceof ServerLevel serverLevel) {

            // Через 5 секунд проверяем окружение
            serverLevel.scheduleTick(
                    pos,
                    this,
                    TRANSFORM_TIME
            );
        }
    }


    // =========================================================
    // ПРОВЕРКА ЧЕРЕЗ 5 СЕКУНД
    // =========================================================

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {

        Element element = detectElement(level, pos);


        // -----------------------------------------------------
        // ВОДА
        // -----------------------------------------------------

        if (element == Element.WATER) {

            level.setBlock(
                    pos,
                    Abyss.WATER_ORE.get().defaultBlockState(),
                    3
            );

            return;
        }


        // -----------------------------------------------------
        // ОГОНЬ
        // -----------------------------------------------------

        if (element == Element.FIRE) {

            level.setBlock(
                    pos,
                    Abyss.FIRE_ORE.get().defaultBlockState(),
                    3
            );

            return;
        }


        // -----------------------------------------------------
        // ЗЕМЛЯ
        // -----------------------------------------------------

        if (element == Element.EARTH) {

            level.setBlock(
                    pos,
                    Abyss.EARTH_ORE.get().defaultBlockState(),
                    3
            );

            return;
        }


        // -----------------------------------------------------
        // ВЕТЕР
        // -----------------------------------------------------

        if (element == Element.WIND) {

            level.setBlock(
                    pos,
                    Abyss.WIND_ORE.get().defaultBlockState(),
                    3
            );

            return;
        }


        // -----------------------------------------------------
        // НИЧЕГО НЕ НАЙДЕНО
        // -----------------------------------------------------

        // Если сейчас подходящей стихии нет,
        // проверяем снова через 5 секунд.

        level.scheduleTick(
                pos,
                this,
                TRANSFORM_TIME
        );
    }

    // =========================================================
    // ОПРЕДЕЛЕНИЕ СТИХИИ
    // =========================================================

    private Element detectElement(
            ServerLevel level,
            BlockPos pos
    ) {

        // Приоритет:
        //
        // 1. Огонь
        // 2. Вода
        // 3. Воздух
        // 4. Земля


        // -----------------------------------------------------
        // ОГОНЬ
        // -----------------------------------------------------

        if (hasFireNearby(level, pos)) {
            return Element.FIRE;
        }


        // -----------------------------------------------------
        // ВОДА
        // -----------------------------------------------------

        if (hasWaterNearby(level, pos)) {
            return Element.WATER;
        }


        // -----------------------------------------------------
        // ВОЗДУХ
        // -----------------------------------------------------

        if (isFloating(level, pos)) {
            return Element.WIND;
        }


        // -----------------------------------------------------
        // ЗЕМЛЯ
        // -----------------------------------------------------

        if (hasStoneNearby(level, pos)) {
            return Element.EARTH;
        }


        return Element.NONE;
    }


    // =========================================================
    // 🔥 ОГОНЬ
    // =========================================================

    private boolean hasFireNearby(
            ServerLevel level,
            BlockPos pos
    ) {

        // Радиус поиска огня/лавы
        int radius = 2;


        for (BlockPos check : BlockPos.betweenClosed(

                pos.offset(
                        -radius,
                        -radius,
                        -radius
                ),

                pos.offset(
                        radius,
                        radius,
                        radius
                )

        )) {

            BlockState state =
                    level.getBlockState(check);


            // Проверяем:
            // - лаву
            // - обычный огонь
            // - огонь душ

            if (
                    state.is(Blocks.LAVA)
                            || state.is(Blocks.FIRE)
                            || state.is(Blocks.SOUL_FIRE)
            ) {

                return true;
            }
        }


        return false;
    }


    // =========================================================
    // 💧 ВОДА
    // =========================================================

    private boolean hasWaterNearby(
            ServerLevel level,
            BlockPos pos
    ) {

        // Радиус поиска воды
        int radius = 2;


        for (BlockPos check : BlockPos.betweenClosed(

                pos.offset(
                        -radius,
                        -radius,
                        -radius
                ),

                pos.offset(
                        radius,
                        radius,
                        radius
                )

        )) {

            // FluidTags.WATER определяет
            // как источник воды,
            // так и текущую воду.

            if (
                    level
                            .getFluidState(check)
                            .is(FluidTags.WATER)
            ) {

                return true;
            }
        }


        return false;
    }


    // =========================================================
    // 🌍 ЗЕМЛЯ
    // =========================================================

    private boolean hasStoneNearby(
            ServerLevel level,
            BlockPos pos
    ) {

        int stoneCount = 0;

        // Проверяем блоки в радиусе 1
        int radius = 1;


        for (BlockPos check : BlockPos.betweenClosed(

                pos.offset(
                        -radius,
                        -radius,
                        -radius
                ),

                pos.offset(
                        radius,
                        radius,
                        radius
                )

        )) {


            // Саму пустую руду не считаем

            if (check.equals(pos)) {
                continue;
            }


            BlockState state =
                    level.getBlockState(check);


            // Какие блоки относятся к земле

            if (
                    state.is(Blocks.STONE)
                            || state.is(Blocks.DEEPSLATE)
                            || state.is(Blocks.COBBLESTONE)
                            || state.is(Blocks.TUFF)
                            || state.is(Blocks.DIRT)
                            || state.is(Blocks.GRASS_BLOCK)
            ) {

                stoneCount++;
            }
        }


        // Нужно минимум 5 земляных/каменных
        // блоков вокруг пустой руды.

        return stoneCount >= 5;
    }


    // =========================================================
    // 🌪 ВЕТЕР
    // =========================================================

    private boolean isFloating(
            ServerLevel level,
            BlockPos pos
    ) {

        /*
         * Для получения руды ветра
         * прямо под пустой рудой
         * должен находиться воздух.
         *
         * Например:
         *
         *     [EMPTY ORE]
         *        AIR
         *        AIR
         *
         */


        return level
                .getBlockState(pos.below())
                .isAir();
    }


    // =========================================================
    // ВИДЫ СТИХИЙ
    // =========================================================

    private enum Element {

        NONE,

        FIRE,

        WATER,

        EARTH,

        WIND
    }
}