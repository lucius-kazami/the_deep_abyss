package com.akashi.abyss.abyss.world;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class AbyssVanillaOres {

    private AbyssVanillaOres() {}

    public static BlockState tryGenerate(
            int x,
            int y,
            int z,
            BlockState original
    ) {

        // Уголь
        if (roll(x, y, z, 10001, 0.012)) {
            return Blocks.COAL_ORE.defaultBlockState();
        }

        // Железо
        if (roll(x, y, z, 10002, 0.014)) {
            return Blocks.IRON_ORE.defaultBlockState();
        }

        // Медь
        if (roll(x, y, z, 10003, 0.010)) {
            return Blocks.COPPER_ORE.defaultBlockState();
        }

        // Золото
        if (roll(x, y, z, 10004, 0.004)) {
            return Blocks.GOLD_ORE.defaultBlockState();
        }

        // Красная пыль
        if (roll(x, y, z, 10005, 0.006)) {
            return Blocks.REDSTONE_ORE.defaultBlockState();
        }

        // Лазурит
        if (roll(x, y, z, 10006, 0.003)) {
            return Blocks.LAPIS_ORE.defaultBlockState();
        }

        // Алмаз
        if (roll(x, y, z, 10007, 0.0015)) {
            return Blocks.DIAMOND_ORE.defaultBlockState();
        }

        // Изумруд — очень редко
        if (roll(x, y, z, 10008, 0.0007)) {
            return Blocks.EMERALD_ORE.defaultBlockState();
        }

        return original;
    }

    private static boolean roll(
            int x,
            int y,
            int z,
            int seed,
            double chance
    ) {

        long h =
                (long) x * 341873128712L +
                        (long) y * 668265263L +
                        (long) z * 132897987541L +
                        seed * 7919L;

        h = (h ^ (h >>> 33))
                * 0xff51afd7ed558ccdL;

        h = (h ^ (h >>> 33))
                * 0xc4ceb9fe1a85ec53L;

        h ^= h >>> 33;

        double value =
                (h & 0xFFFFFF) /
                        (double) 0xFFFFFF;

        return value < chance;
    }
}