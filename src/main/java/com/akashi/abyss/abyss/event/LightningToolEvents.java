package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

@EventBusSubscriber(modid = Abyss.MODID)
public class LightningToolEvents {

    /*
     * Защита от повторного вызова BreakEvent.
     */
    private static boolean processingBlocks = false;

    /*
     * Ограничение количества блоков дерева.
     * Нужно, чтобы случайно не снести огромную конструкцию из брёвен.
     */
    private static final int MAX_TREE_BLOCKS = 256;


    // ============================================================
    // ТОПОР МОЛНИИ
    // ============================================================

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {

        if (processingBlocks) {
            return;
        }

        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        ItemStack tool = player.getMainHandItem();

        if (!tool.is(Abyss.LIGHTNING_AXE.get())) {
            return;
        }

        BlockPos origin = event.getPos();

        BlockState originState =
                level.getBlockState(origin);

        /*
         * Способность работает только на брёвнах.
         */
        if (!originState.is(BlockTags.LOGS)) {
            return;
        }

        processingBlocks = true;

        try {

            destroyTree(
                    level,
                    player,
                    origin
            );

        } finally {

            processingBlocks = false;
        }
    }


    private static void destroyTree(
            ServerLevel level,
            ServerPlayer player,
            BlockPos origin
    ) {

        Queue<BlockPos> queue =
                new ArrayDeque<>();

        Set<BlockPos> visited =
                new HashSet<>();

        queue.add(origin);
        visited.add(origin);


        int destroyed = 0;


        while (!queue.isEmpty()
                && destroyed < MAX_TREE_BLOCKS) {

            BlockPos current =
                    queue.poll();


            /*
             * Проверяем соседние блоки вокруг текущего бревна.
             *
             * Используем область 3x3x3.
             */
            for (int x = -1; x <= 1; x++) {

                for (int y = -1; y <= 1; y++) {

                    for (int z = -1; z <= 1; z++) {

                        if (x == 0
                                && y == 0
                                && z == 0) {
                            continue;
                        }


                        BlockPos next =
                                current.offset(
                                        x,
                                        y,
                                        z
                                );


                        if (!visited.add(next)) {
                            continue;
                        }


                        BlockState state =
                                level.getBlockState(next);


                        /*
                         * Ищем только брёвна.
                         */
                        if (!state.is(BlockTags.LOGS)) {
                            continue;
                        }


                        queue.add(next);


                        /*
                         * Электричество на блоке.
                         */
                        spawnElectricParticles(
                                level,
                                next,
                                12
                        );


                        /*
                         * Ломаем от имени игрока.
                         */
                        player.gameMode.destroyBlock(next);

                        destroyed++;


                        if (destroyed >= MAX_TREE_BLOCKS) {
                            break;
                        }
                    }

                    if (destroyed >= MAX_TREE_BLOCKS) {
                        break;
                    }
                }

                if (destroyed >= MAX_TREE_BLOCKS) {
                    break;
                }
            }
        }
    }


    // ============================================================
    // ЛОПАТА МОЛНИИ — 3x3
    // ============================================================

    @SubscribeEvent
    public static void onShovelBreak(
            BlockEvent.BreakEvent event
    ) {

        if (processingBlocks) {
            return;
        }

        if (!(event.getPlayer()
                instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.level()
                instanceof ServerLevel level)) {
            return;
        }


        ItemStack tool =
                player.getMainHandItem();


        if (!tool.is(Abyss.LIGHTNING_SHOVEL.get())) {
            return;
        }


        BlockPos origin =
                event.getPos();


        Direction direction =
                getMiningDirection(player);


        processingBlocks = true;

        try {

            /*
             * Только один слой 3x3.
             */
            for (int a = -1; a <= 1; a++) {

                for (int b = -1; b <= 1; b++) {

                    BlockPos target =
                            getOffsetPosition(
                                    origin,
                                    direction,
                                    a,
                                    b
                            );


                    if (target.equals(origin)) {
                        continue;
                    }


                    BlockState state =
                            level.getBlockState(target);


                    if (state.isAir()) {
                        continue;
                    }


                    /*
                     * Лопата ломает только блоки,
                     * для которых подходит shovel.
                     */
                    if (!state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
                        continue;
                    }


                    float hardness =
                            state.getDestroySpeed(
                                    level,
                                    target
                            );


                    if (hardness < 0.0F) {
                        continue;
                    }


                    spawnElectricParticles(
                            level,
                            target,
                            7
                    );


                    player.gameMode.destroyBlock(target);
                }
            }

        } finally {

            processingBlocks = false;
        }
    }


    // ============================================================
    // МОТЫГА МОЛНИИ — ОБРАБОТКА 3x3
    // ============================================================

    @SubscribeEvent
    public static void onHoeUse(
            UseItemOnBlockEvent event
    ) {

        /*
         * Только сервер.
         */
        if (!(event.getLevel()
                instanceof ServerLevel level)) {
            return;
        }


        if (!(event.getPlayer()
                instanceof ServerPlayer player)) {
            return;
        }


        /*
         * Проверяем руку.
         */
        InteractionHand hand =
                event.getHand();


        ItemStack tool =
                player.getItemInHand(hand);


        if (!tool.is(Abyss.LIGHTNING_HOE.get())) {
            return;
        }


        BlockPos center =
                event.getPos();


        /*
         * Обрабатываем землю вокруг центрального блока.
         *
         * Центральный блок оставляем обычной механике мотыги.
         */
        for (int x = -1; x <= 1; x++) {

            for (int z = -1; z <= 1; z++) {

                if (x == 0 && z == 0) {
                    continue;
                }


                BlockPos target =
                        center.offset(
                                x,
                                0,
                                z
                        );


                tillBlock(
                        level,
                        player,
                        target
                );
            }
        }
    }


    private static void tillBlock(
            ServerLevel level,
            ServerPlayer player,
            BlockPos pos
    ) {

        BlockState state =
                level.getBlockState(pos);


        /*
         * Над землёй должно быть свободное место.
         */
        if (!level.getBlockState(pos.above()).isAir()) {
            return;
        }


        /*
         * Пока поддерживаем основные блоки.
         */
        if (state.is(
                net.minecraft.world.level.block.Blocks.DIRT
        )
                ||
                state.is(
                        net.minecraft.world.level.block.Blocks.GRASS_BLOCK
                )
                ||
                state.is(
                        net.minecraft.world.level.block.Blocks.DIRT_PATH
                )) {


            level.setBlockAndUpdate(
                    pos,
                    net.minecraft.world.level.block.Blocks.FARMLAND
                            .defaultBlockState()
            );


            spawnElectricParticles(
                    level,
                    pos,
                    8
            );
        }
    }


    // ============================================================
    // 3x3 ОТНОСИТЕЛЬНО НАПРАВЛЕНИЯ
    // ============================================================

    private static BlockPos getOffsetPosition(
            BlockPos center,
            Direction direction,
            int a,
            int b
    ) {

        if (direction.getAxis()
                == Direction.Axis.Y) {

            return center.offset(
                    a,
                    0,
                    b
            );
        }


        if (direction.getAxis()
                == Direction.Axis.Z) {

            return center.offset(
                    a,
                    b,
                    0
            );
        }


        return center.offset(
                0,
                b,
                a
        );
    }


    // ============================================================
    // НАПРАВЛЕНИЕ ВЗГЛЯДА
    // ============================================================

    private static Direction getMiningDirection(
            ServerPlayer player
    ) {

        float pitch =
                player.getXRot();


        if (pitch > 55.0F) {
            return Direction.DOWN;
        }


        if (pitch < -55.0F) {
            return Direction.UP;
        }


        return player.getDirection();
    }


    // ============================================================
    // ЭЛЕКТРИЧЕСКИЕ ЧАСТИЦЫ
    // ============================================================

    private static void spawnElectricParticles(
            ServerLevel level,
            BlockPos pos,
            int amount
    ) {

        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,

                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,

                amount,

                0.35,
                0.35,
                0.35,

                0.08
        );
    }
}