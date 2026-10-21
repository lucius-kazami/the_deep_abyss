package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssCaveGenerator {

    // Диапазон по Y: от дна Биома 1 (с запасом) почти до бедрока
    private static final int CAVE_TOP = -455;
    private static final int CAVE_BOTTOM = -535;

    private static final long WORLD_SEED = 918273645L; // фиксированный сид пещер

    private static final int SEARCH_RADIUS = 4; // в чанках
    private static final double CAVE_CHANCE = 0.04;
    private static final double RAVINE_CHANCE = 0.025;
    private static final double MINESHAFT_CHANCE = 0.015;

    private static final int CAVE_STEPS = 55;
    private static final double CAVE_STEP_LEN = 1.6;
    private static final double CAVE_MIN_RADIUS = 1.6;
    private static final double CAVE_MAX_RADIUS = 3.4;

    private static final Set<ChunkPos> PROCESSED = ConcurrentHashMap.newKeySet();

    @SubscribeEvent(priority = EventPriority.HIGH) // раньше руды, чтобы руда не появлялась в вырезанном воздухе
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos pos = chunk.getPos();
        if (!PROCESSED.add(pos)) return;

        for (int ox = pos.x - SEARCH_RADIUS; ox <= pos.x + SEARCH_RADIUS; ox++) {
            for (int oz = pos.z - SEARCH_RADIUS; oz <= pos.z + SEARCH_RADIUS; oz++) {
                long originSeed = originSeed(ox, oz);
                Random rnd = new Random(originSeed);

                if (rnd.nextDouble() < CAVE_CHANCE) {
                    carveCave(level, pos, ox, oz, new Random(originSeed ^ 0xC4EL));
                }
                if (rnd.nextDouble() < RAVINE_CHANCE) {
                    carveRavine(level, pos, ox, oz, new Random(originSeed ^ 0x9A1L));
                }
                if (rnd.nextDouble() < MINESHAFT_CHANCE) {
                    carveMineshaft(level, pos, ox, oz, new Random(originSeed ^ 0x5E3L));
                }
            }
        }
    }

    private static long originSeed(int cx, int cz) {
        return WORLD_SEED ^ ((long) cx * 341873128712L) ^ ((long) cz * 132897987541L);
    }

    // ---------- Пещеры: цепочка сфер вдоль случайного 3D-пути ----------
    private static void carveCave(ServerLevel level, ChunkPos target, int ox, int oz, Random rnd) {
        double x = ox * 16 + 8;
        double z = oz * 16 + 8;
        double y = CAVE_BOTTOM + rnd.nextInt(Math.max(1, CAVE_TOP - CAVE_BOTTOM - 40)) + 20;

        double yaw = rnd.nextDouble() * Math.PI * 2;
        double pitch = (rnd.nextDouble() - 0.5) * 0.3;

        for (int step = 0; step < CAVE_STEPS; step++) {
            yaw += (rnd.nextDouble() - 0.5) * 0.5;
            pitch += (rnd.nextDouble() - 0.5) * 0.2;
            pitch = Math.max(-0.6, Math.min(0.6, pitch));

            x += Math.cos(yaw) * Math.cos(pitch) * CAVE_STEP_LEN;
            z += Math.sin(yaw) * Math.cos(pitch) * CAVE_STEP_LEN;
            y += Math.sin(pitch) * CAVE_STEP_LEN;

            if (y > CAVE_TOP || y < CAVE_BOTTOM) break;

            double radius = CAVE_MIN_RADIUS + (CAVE_MAX_RADIUS - CAVE_MIN_RADIUS)
                    * (0.5 + 0.5 * Math.sin(step * 0.3));
            if (rnd.nextInt(12) == 0) radius += 2.5; // редкие залы

            carveSphere(level, target, x, y, z, radius);
        }
    }

    // ---------- Разломы: узкая, но высокая трещина ----------
    private static void carveRavine(ServerLevel level, ChunkPos target, int ox, int oz, Random rnd) {
        double x = ox * 16 + 8;
        double z = oz * 16 + 8;
        double yaw = rnd.nextDouble() * Math.PI * 2;
        int topY = CAVE_TOP - 10 - rnd.nextInt(40);
        int bottomY = Math.max(CAVE_BOTTOM, topY - 80 - rnd.nextInt(40));

        int length = 40 + rnd.nextInt(30);
        double dx = Math.cos(yaw), dz = Math.sin(yaw);

        for (int i = 0; i < length; i++) {
            double px = x + dx * i;
            double pz = z + dz * i;
            double wobble = Math.sin(i * 0.2) * 1.5;
            double width = 1.2 + Math.abs(Math.sin(i * 0.15)) * 1.8;

            for (int y = bottomY; y <= topY; y++) {
                carveDisc(level, target, px + wobble, y, pz, width);
            }
        }
    }

    // ---------- Шахты: упрощённый коридор с настилом ----------
    private static void carveMineshaft(ServerLevel level, ChunkPos target, int ox, int oz, Random rnd) {
        double x = ox * 16 + 8;
        double z = oz * 16 + 8;
        int range = Math.max(10, CAVE_TOP - CAVE_BOTTOM - 20);
        int y = CAVE_BOTTOM + 10 + rnd.nextInt(range);
        double yaw = rnd.nextDouble() * Math.PI * 2;
        double dx = Math.cos(yaw), dz = Math.sin(yaw);
        int length = 30 + rnd.nextInt(40);

        for (int i = 0; i < length; i++) {
            int px = (int) Math.round(x + dx * i);
            int pz = (int) Math.round(z + dz * i);
            if (!inChunk(target, px, pz)) continue;

            for (int w = -1; w <= 1; w++) {
                int wx = (int) Math.round(px - dz * w);
                int wz = (int) Math.round(pz + dx * w);
                if (!inChunk(target, wx, wz)) continue;
                for (int h = 0; h < 4; h++) {
                    setAir(level, wx, y + h, wz);
                }
                level.setBlock(new BlockPos(wx, y - 1, wz), Blocks.OAK_PLANKS.defaultBlockState(), 2);
            }
            if (i % 4 == 0) {
                int lx = (int) Math.round(px - dz * -2);
                int lz = (int) Math.round(pz + dx * -2);
                int rx = (int) Math.round(px - dz * 2);
                int rz = (int) Math.round(pz + dx * 2);
                for (int h = 0; h < 4; h++) {
                    if (inChunk(target, lx, lz)) level.setBlock(new BlockPos(lx, y + h, lz), Blocks.OAK_FENCE.defaultBlockState(), 2);
                    if (inChunk(target, rx, rz)) level.setBlock(new BlockPos(rx, y + h, rz), Blocks.OAK_FENCE.defaultBlockState(), 2);
                }
            }
        }
    }

    // ---------- Вспомогательное ----------
    private static boolean inChunk(ChunkPos target, int x, int z) {
        return (x >> 4) == target.x && (z >> 4) == target.z;
    }

    private static void setAir(ServerLevel level, int x, int y, int z) {
        BlockState current = level.getBlockState(new BlockPos(x, y, z));
        if (current.is(Blocks.WATER) || current.is(Blocks.LAVA)) return;
        level.setBlock(new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(), 2);
    }

    private static void carveSphere(ServerLevel level, ChunkPos target, double cx, double cy, double cz, double radius) {
        int minX = (int) Math.floor(cx - radius), maxX = (int) Math.ceil(cx + radius);
        int minY = (int) Math.floor(cy - radius), maxY = (int) Math.ceil(cy + radius);
        int minZ = (int) Math.floor(cz - radius), maxZ = (int) Math.ceil(cz + radius);

        for (int x = minX; x <= maxX; x++) {
            if ((x >> 4) != target.x) continue;
            for (int z = minZ; z <= maxZ; z++) {
                if ((z >> 4) != target.z) continue;
                for (int y = minY; y <= maxY; y++) {
                    double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy) * 1.3 + (z - cz) * (z - cz));
                    if (dist <= radius) setAir(level, x, y, z);
                }
            }
        }
    }

    private static void carveDisc(ServerLevel level, ChunkPos target, double cx, int y, double cz, double radius) {
        int minX = (int) Math.floor(cx - radius), maxX = (int) Math.ceil(cx + radius);
        int minZ = (int) Math.floor(cz - radius), maxZ = (int) Math.ceil(cz + radius);
        for (int x = minX; x <= maxX; x++) {
            if ((x >> 4) != target.x) continue;
            for (int z = minZ; z <= maxZ; z++) {
                if ((z >> 4) != target.z) continue;
                double dist = Math.sqrt((x - cx) * (x - cx) + (z - cz) * (z - cz));
                if (dist <= radius) setAir(level, x, y, z);
            }
        }
    }
}