package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class LightningMiningEvents {

    /*
     * Не позволяет дополнительным разрушениям
     * снова запускать способность кирки.
     */
    private static boolean breakingArea = false;

    /*
     * 6 слоёв в глубину.
     */
    private static final int DEPTH = 6;

    /*
     * Максимум дополнительных блоков:
     * 3x3x6 = 54
     * Центральный блок ломается обычным способом,
     * поэтому максимум дополнительных = 53.
     */
    private static final int MAX_EXTRA_BLOCKS = 53;


    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {

        if (breakingArea) {
            return;
        }

        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack pickaxe = player.getMainHandItem();

        if (!pickaxe.is(Abyss.LIGHTNING_PICKAXE.get())) {
            return;
        }

        BlockPos origin = event.getPos();

        BlockState originState =
                level.getBlockState(origin);

        /*
         * Способность запускается только если исходный блок
         * действительно предназначен для кирки.
         */
        if (!originState.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return;
        }

        Direction direction =
                getMiningDirection(player);

        breakingArea = true;

        try {

            breakTunnel(
                    level,
                    player,
                    pickaxe,
                    origin,
                    direction
            );

        } finally {

            breakingArea = false;
        }
    }


    // ============================================================
    // ТОННЕЛЬ 3x3x6
    // ============================================================

    private static void breakTunnel(
            ServerLevel level,
            ServerPlayer player,
            ItemStack pickaxe,
            BlockPos origin,
            Direction direction
    ) {

        int brokenBlocks = 0;


        for (int depth = 0;
             depth < DEPTH;
             depth++) {

            BlockPos center =
                    origin.relative(
                            direction,
                            depth
                    );


            /*
             * Небольшая электрическая вспышка
             * на каждом новом слое.
             */
            spawnLayerEffect(
                    level,
                    center
            );


            for (int a = -1; a <= 1; a++) {

                for (int b = -1; b <= 1; b++) {

                    BlockPos target =
                            getOffsetPosition(
                                    center,
                                    direction,
                                    a,
                                    b
                            );


                    /*
                     * Исходный блок Minecraft уже ломает сам.
                     */
                    if (target.equals(origin)) {
                        continue;
                    }


                    if (brokenBlocks >= MAX_EXTRA_BLOCKS) {
                        return;
                    }


                    if (!canBreak(
                            level,
                            target
                    )) {

                        continue;
                    }


                    /*
                     * Если кирка уже почти уничтожена,
                     * прекращаем массовое разрушение.
                     */
                    if (pickaxe.isEmpty()) {
                        return;
                    }


                    spawnBlockEffect(
                            level,
                            target
                    );


                    /*
                     * Ломаем от имени игрока.
                     */
                    boolean destroyed =
                            player.gameMode
                                    .destroyBlock(target);


                    if (!destroyed) {
                        continue;
                    }


                    brokenBlocks++;


                    /*
                     * Каждый дополнительный блок расходует
                     * 1 единицу прочности.
                     */
                    pickaxe.hurtAndBreak(
                            1,
                            player,
                            EquipmentSlot.MAINHAND
                    );


                    /*
                     * Если предмет сломался —
                     * прекращаем способность.
                     */
                    if (pickaxe.isEmpty()) {
                        return;
                    }
                }
            }
        }


        /*
         * Финальная электрическая вспышка,
         * если удалось что-нибудь дополнительно сломать.
         */
        if (brokenBlocks > 0) {

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,

                    origin.getX() + 0.5,
                    origin.getY() + 0.5,
                    origin.getZ() + 0.5,

                    45,

                    1.5,
                    1.5,
                    1.5,

                    0.2
            );
        }
    }


    // ============================================================
    // МОЖНО ЛИ ЛОМАТЬ БЛОК
    // ============================================================

    private static boolean canBreak(
            ServerLevel level,
            BlockPos pos
    ) {

        BlockState state =
                level.getBlockState(pos);


        /*
         * Воздух.
         */
        if (state.isAir()) {
            return false;
        }


        /*
         * Кирка молнии массово ломает только блоки,
         * предназначенные для кирки.
         *
         * Камень, руды, металлические блоки и т.д.
         */
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }


        /*
         * Bedrock и другие неразрушимые блоки.
         */
        float hardness =
                state.getDestroySpeed(
                        level,
                        pos
                );

        if (hardness < 0.0F) {
            return false;
        }


        /*
         * ========================================================
         * ЗАЩИТА BLOCK ENTITY
         * ========================================================
         *
         * Не уничтожаем массовой способностью:
         *
         * сундуки
         * печки
         * шалкеры
         * спавнеры/машины модов
         * различные контейнеры
         * и другие BlockEntity.
         */

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (blockEntity != null) {
            return false;
        }


        return true;
    }


    // ============================================================
    // СОЗДАНИЕ СЛОЯ 3x3
    // ============================================================

    private static BlockPos getOffsetPosition(
            BlockPos center,
            Direction direction,
            int a,
            int b
    ) {

        /*
         * Копаем вверх или вниз.
         *
         * Площадь располагается по X/Z.
         */

        if (direction.getAxis()
                == Direction.Axis.Y) {

            return center.offset(
                    a,
                    0,
                    b
            );
        }


        /*
         * Север / юг.
         *
         * Площадь располагается по X/Y.
         */

        if (direction.getAxis()
                == Direction.Axis.Z) {

            return center.offset(
                    a,
                    b,
                    0
            );
        }


        /*
         * Восток / запад.
         *
         * Площадь располагается по Z/Y.
         */

        return center.offset(
                0,
                b,
                a
        );
    }


    // ============================================================
    // НАПРАВЛЕНИЕ КОПАНИЯ
    // ============================================================

    private static Direction getMiningDirection(
            ServerPlayer player
    ) {

        float pitch =
                player.getXRot();


        /*
         * Игрок смотрит вниз.
         */
        if (pitch > 55.0F) {
            return Direction.DOWN;
        }


        /*
         * Игрок смотрит вверх.
         */
        if (pitch < -55.0F) {
            return Direction.UP;
        }


        /*
         * Горизонтальное направление.
         */
        return player.getDirection();
    }


    // ============================================================
    // ЭФФЕКТ ОДНОГО БЛОКА
    // ============================================================

    private static void spawnBlockEffect(
            ServerLevel level,
            BlockPos pos
    ) {

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,

                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,

                8,

                0.3,
                0.3,
                0.3,

                0.1
        );
    }


    // ============================================================
    // ЭФФЕКТ СЛОЯ
    // ============================================================

    private static void spawnLayerEffect(
            ServerLevel level,
            BlockPos center
    ) {

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,

                center.getX() + 0.5,
                center.getY() + 0.5,
                center.getZ() + 0.5,

                15,

                1.0,
                1.0,
                1.0,

                0.15
        );
    }
}