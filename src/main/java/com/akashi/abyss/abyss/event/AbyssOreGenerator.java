package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssOreGenerator {

    // Y-диапазоны биомов/переходных зон
    private static final int BIOME2_TOP = -601, BIOME2_BOTTOM = -750;
    private static final int BIOME3_TOP = -841, BIOME3_BOTTOM = -960;
    private static final int BIOME4_TOP = -1056, BIOME4_BOTTOM = -1206;

    private static final double EARTH_CHANCE = 0.015;   // во всех переходных зонах
    private static final double FIRE_CHANCE = 0.020;    // биом 2 — "много"
    private static final double LIGHTNING_CHANCE = 0.020; // биом 2 и 4
    private static final double WIND_CHANCE = 0.018;    // биом 4
    private static final double WATER_CHANCE = 0.02;   // рядом с биомом 3

    private static final double RUBY_CHANCE = 0.006;
    private static final double SAPPHIRE_CHANCE = 0.006;
    private static final double AMBER_CHANCE = 0.008;

    private static final Set<ChunkPos> PROCESSED = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos pos = chunk.getPos();
        // Не заходим в диапазон Биома 1 — там своя логика в AbyssBiome1ChunkHandler
        if (!PROCESSED.add(pos)) return;

        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                processColumn(level, minX + x, minZ + z);
            }
        }
    }

    private static void processColumn(ServerLevel level, int x, int z) {
        // от бедрока (-1312) до дна Биома 1 (-451), не трогая сам Биом 1
        for (int y = -1312; y <= -451; y++) {
            BlockState current = level.getBlockState(new BlockPos(x, y, z));
            if (!current.is(Blocks.STONE) && !current.is(Blocks.DEEPSLATE) && !current.is(Blocks.SAND) && !current.is(Blocks.SANDSTONE)) {
                continue; // не трогаем воду/воздух/уже поставленные блоки
            }

            BlockState newState = null;

            if (y <= BIOME2_TOP && y >= BIOME2_BOTTOM) {
                // Биом 2 (когда появится рельеф) — земля/огонь/молния, много
                if (oreRoll(x, y, z, 50001, EARTH_CHANCE)) newState = Abyss.EARTH_ORE.get().defaultBlockState();
                else if (oreRoll(x, y, z, 50002, FIRE_CHANCE)) newState = Abyss.FIRE_ORE.get().defaultBlockState();
                else if (oreRoll(x, y, z, 50003, LIGHTNING_CHANCE)) newState = Abyss.LIGHTNING_ORE.get().defaultBlockState();
            } else if (y <= BIOME3_TOP && y >= BIOME3_BOTTOM) {
                // Биом 3 (подводный) — только вода и земля
                if (current.is(Blocks.SAND) || current.is(Blocks.SANDSTONE)) {
                    if (oreRoll(x, y, z, 50004, WATER_CHANCE)) newState = Abyss.WATER_ORE.get().defaultBlockState();
                } else if (oreRoll(x, y, z, 50005, EARTH_CHANCE)) {
                    newState = Abyss.EARTH_ORE.get().defaultBlockState();
                }
            } else if (y <= BIOME4_TOP && y >= BIOME4_BOTTOM) {
                // Биом 4 — ветер/земля/молния
                if (oreRoll(x, y, z, 50006, WIND_CHANCE)) newState = Abyss.WIND_ORE.get().defaultBlockState();
                else if (oreRoll(x, y, z, 50007, EARTH_CHANCE)) newState = Abyss.EARTH_ORE.get().defaultBlockState();
                else if (oreRoll(x, y, z, 50008, LIGHTNING_CHANCE)) newState = Abyss.LIGHTNING_ORE.get().defaultBlockState();
            } else {
                if (current.is(Blocks.STONE) || current.is(Blocks.DEEPSLATE)) {
                    if (oreRoll(x, y, z, 50009, EARTH_CHANCE)) newState = Abyss.EARTH_ORE.get().defaultBlockState();
                    else if (oreRoll(x, y, z, 50010, RUBY_CHANCE)) newState = Abyss.RUBY_ORE.get().defaultBlockState();
                    else if (oreRoll(x, y, z, 50011, SAPPHIRE_CHANCE)) newState = Abyss.SAPPHIRE_ORE.get().defaultBlockState();
                    else if (oreRoll(x, y, z, 50012, AMBER_CHANCE)) newState = Abyss.AMBER_ORE.get().defaultBlockState();
                }
            }

            if (newState != null) {
                level.setBlock(new BlockPos(x, y, z), newState, 2);
            }
        }
    }

    private static long hash3(int x, int y, int z, int seed) {
        long h = (long) x * 341873128712L + (long) y * 668265263L + (long) z * 132897987541L + seed * 7919L;
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        return h ^ (h >>> 33);
    }

    private static boolean oreRoll(int x, int y, int z, int seed, double chance) {
        long h = hash3(x, y, z, seed);
        double v = (h & 0xFFFFFF) / (double) 0xFFFFFF;
        return v < chance;
    }
}