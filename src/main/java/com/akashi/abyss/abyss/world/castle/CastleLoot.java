package com.akashi.abyss.abyss.world.castle;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CastleLoot {

    private CastleLoot() {}

    public static void buildChunk(
            ServerLevel level,
            int centerX,
            int baseY,
            int centerZ,
            int chunkMinX,
            int chunkMinZ
    ) {

        /*
         * Библиотека.
         */
        createLibraryChest(
                level,
                centerX - 25,
                baseY + 3,
                centerZ - 5,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Оружейная.
         */
        createArmoryChest(
                level,
                centerX + 25,
                baseY + 3,
                centerZ - 5,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Казармы.
         */
        createBarracksChest(
                level,
                centerX - 25,
                baseY + 3,
                centerZ - 35,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Часовня.
         */
        createChapelChest(
                level,
                centerX + 25,
                baseY + 3,
                centerZ - 35,
                chunkMinX,
                chunkMinZ
        );

        /*
         * Два секретных тайника.
         */
        createSecretChest(
                level,
                centerX - 48,
                baseY + 4,
                centerZ - 52,
                chunkMinX,
                chunkMinZ
        );

        createSecretChest(
                level,
                centerX + 48,
                baseY + 4,
                centerZ + 42,
                chunkMinX,
                chunkMinZ
        );
    }


    // =========================================================
    // БИБЛИОТЕКА
    // =========================================================

    private static void createLibraryChest(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        createChest(
                level,
                x,
                y,
                z,
                chunkMinX,
                chunkMinZ,

                new ItemStack(Items.BOOK, 8),
                new ItemStack(Items.PAPER, 12),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 3),
                new ItemStack(Items.LAPIS_LAZULI, 7),
                new ItemStack(Items.AMETHYST_SHARD, 5)
        );
    }


    // =========================================================
    // ОРУЖЕЙНАЯ
    // =========================================================

    private static void createArmoryChest(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        createChest(
                level,
                x,
                y,
                z,
                chunkMinX,
                chunkMinZ,

                new ItemStack(Items.IRON_INGOT, 8),
                new ItemStack(Items.GOLD_INGOT, 3),
                new ItemStack(Items.DIAMOND, 1),
                new ItemStack(Items.IRON_SWORD, 1),
                new ItemStack(Items.IRON_AXE, 1),
                new ItemStack(Items.SHIELD, 1)
        );
    }


    // =========================================================
    // КАЗАРМЫ
    // =========================================================

    private static void createBarracksChest(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        createChest(
                level,
                x,
                y,
                z,
                chunkMinX,
                chunkMinZ,

                new ItemStack(Items.BREAD, 8),
                new ItemStack(Items.COOKED_BEEF, 5),
                new ItemStack(Items.IRON_INGOT, 4),
                new ItemStack(Items.ARROW, 16),
                new ItemStack(Items.BOW, 1)
        );
    }


    // =========================================================
    // ЧАСОВНЯ
    // =========================================================

    private static void createChapelChest(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        createChest(
                level,
                x,
                y,
                z,
                chunkMinX,
                chunkMinZ,

                new ItemStack(Items.GOLD_INGOT, 5),
                new ItemStack(Items.AMETHYST_SHARD, 8),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 5),
                new ItemStack(Items.GOLDEN_APPLE, 1)
        );
    }


    // =========================================================
    // СЕКРЕТНЫЙ СУНДУК
    // =========================================================

    private static void createSecretChest(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ
    ) {

        createChest(
                level,
                x,
                y,
                z,
                chunkMinX,
                chunkMinZ,

                new ItemStack(Items.DIAMOND, 3),
                new ItemStack(Items.EMERALD, 6),
                new ItemStack(Items.GOLD_INGOT, 8),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 8),
                new ItemStack(Items.GOLDEN_APPLE, 1)
        );
    }


    // =========================================================
    // СОЗДАНИЕ СУНДУКА
    // =========================================================

    private static void createChest(
            ServerLevel level,
            int x,
            int y,
            int z,
            int chunkMinX,
            int chunkMinZ,
            ItemStack... loot
    ) {

        if (!insideChunk(
                x,
                z,
                chunkMinX,
                chunkMinZ
        )) {
            return;
        }

        BlockPos pos =
                new BlockPos(x, y, z);

        /*
         * Не перезаписываем уже существующий сундук.
         *
         * Это важно, иначе при повторном вызове генератора
         * игроку снова восстановится весь лут.
         */
        BlockEntity oldEntity =
                level.getBlockEntity(pos);

        if (oldEntity instanceof Container) {
            return;
        }

        level.setBlock(
                pos,
                Blocks.CHEST.defaultBlockState(),
                2
        );

        BlockEntity blockEntity =
                level.getBlockEntity(pos);

        if (!(blockEntity instanceof Container container)) {
            return;
        }

        /*
         * Начинаем не всегда с первого слота,
         * чтобы сундук выглядел менее искусственно.
         */
        int slotOffset =
                Math.floorMod(
                        x * 31 + z * 17,
                        5
                );

        for (int i = 0; i < loot.length; i++) {

            int slot =
                    slotOffset + i * 2;

            if (slot >= container.getContainerSize()) {
                break;
            }

            container.setItem(
                    slot,
                    loot[i].copy()
            );
        }

        blockEntity.setChanged();
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
}