package com.akashi.abyss.abyss.event;

import java.util.Random;
import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.Block;
import com.akashi.abyss.abyss.ModPlants;
import com.akashi.abyss.abyss.world.AbyssVanillaOres;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.akashi.abyss.abyss.world.castle.AncientFrozenCastleGenerator;
import com.akashi.abyss.abyss.world.QuetzalcoatlusMountainGenerator;


@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssBiome1ChunkHandler {

    // ---------- Вертикаль ----------
    public static final int FLOOR_Y = -450;
    public static final int CEILING_Y = -301;
    private static final int SEA_LEVEL = FLOOR_Y + 20;   // верх воды (-430)
    private static final int MAX_HILL_HEIGHT = 40;
    private static final int QUETZAL_MOUNTAIN_MAX_HEIGHT = 65;

    // ---------- Вода ----------
    private static final double SEA_THRESHOLD = 0.30;     // море/океан (большие, глубокие)
    private static final double LAKE_THRESHOLD = 0.24;    // озёра (поменьше)
    private static final double RIVER_HALF_WIDTH = 0.03;  // реки (узкие)
    private static final double SHORE_MARGIN = 0.08;      // ширина плавного берега
    private static final double RIVER_MARGIN = 0.04;
    private static final int MAX_WATER_DEPTH = 60;
    private static final int SAND_LAYERS = 3;             // песок на дне

    // ---------- Потолок ----------
    private static final int MAX_CEILING_ROUGHNESS = 26;
    private static final int CEILING_DETAIL_ROUGHNESS = 8;
    private static final double STALACTITE_CHANCE = 0.03;

    // ---------- Зоны ----------
    private static final double VOLCANIC_THRESHOLD = 0.66; // серые блоки, лава, мёртвые деревья
    private static final double POISON_THRESHOLD = 0.64;   // синяя земля, белые деревья
    private static final double LAVA_THRESHOLD = 0.62;

    // ---------- Деревья ----------
    private static final int TREE_MIN_TRUNK = 10;
    private static final int TREE_MAX_TRUNK = 15;

    private static final Set<ChunkPos> PROCESSED = ConcurrentHashMap.newKeySet();

    private enum Zone { NORMAL, POISON, VOLCANIC }

    private record Column(int floorTop, Zone zone, boolean water, boolean lava, int ceilingBottom) {}

    private static final double EARTH_ORE_CHANCE = 0.020;
    private static final double FIRE_ORE_CHANCE = 0.015;
    private static final double WATER_ORE_CHANCE = 0.0003; // редко, как и просили

    private static final double SHAFT_CHANCE = 0.0006;
    private static final int SHAFT_DEPTH = 78;
    private static final int SHAFT_MIN_Y = -535;

    private static final double CEILING_LIGHT_CHANCE = 0.03;

    private static final long CAVE_LOCAL_SEED = 442139L;
    private static final double LOCAL_CAVE_CHANCE = 0.6; // высокая — пещеры почти в каждом чанке
    private static final int LOCAL_CAVE_BOTTOM = -530;

    private static final long BAOBAB_SEED = 771993L;
    private static final int BAOBAB_RARITY = 250; // примерно 1 баобаб на 250 чанков

    // ---------- Драгоценные руды ----------
    private static final double RUBY_ORE_CHANCE = 0.006;

    // Янтарь значительно реже рубина
    private static final double AMBER_ORE_CHANCE = 0.0005;

    private static final double LAND_PLANT_CHANCE = 0.05;
    private static final double VOLCANIC_PLANT_CHANCE = 0.015;
    private static final double WATER_PLANT_CHANCE = 0.06;
    private static final double LILY_CHANCE = 0.03;
    private static final double VINE_CHANCE = 0.02;

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

    public static boolean isInsideBiome1(double y) {
        return y >= FLOOR_Y && y < CEILING_Y;
    }

    public static int getSeaLevel() {
        return SEA_LEVEL;
    }

    public static boolean isWaterPosition(int x, int z) {
        double sea = noise01(x, z, 5000, 220);
        double lake = noise01(x, z, 6000, 64);
        double river = Math.abs(noise01(x, z, 7000, 110) - 0.5);

        return sea < SEA_THRESHOLD
                || lake < LAKE_THRESHOLD
                || river < RIVER_HALF_WIDTH;
    }

    public static boolean isLargeWaterArea(
            int centerX,
            int centerZ,
            int radius
    ) {
        /*
         * Проверяем сетку вокруг точки.
         * Не каждый блок — это было бы слишком дорого.
         */
        for (int x = centerX - radius; x <= centerX + radius; x += 16) {
            for (int z = centerZ - radius; z <= centerZ + radius; z += 16) {

                if (!isWaterPosition(x, z)) {
                    return false;
                }
            }
        }

        return true;
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;

        ChunkPos pos = chunk.getPos();
        int minX = pos.getMinBlockX();
        int minZ = pos.getMinBlockZ();

        // Метка "чанк уже сгенерирован": нижний блок всегда почва.
        // Работает и после перезапуска игры, так что мир не затрётся.
        if (chunk.getBlockState(new BlockPos(minX, FLOOR_Y, minZ)).is(Abyss.ABYSS_SOIL.get())) return;
        if (!PROCESSED.add(pos)) return;

        // Проход 1: рельеф, вода, потолок
        Column[][] columns = new Column[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                columns[x][z] = generateColumn(level, minX + x, minZ + z);
            }
        }

        // Проход 1.5: локальные пещеры
        carveLocalCaves(level, pos, minX, minZ, columns);
        QuetzalcoatlusMountainGenerator.carve(level, pos);

        // Проход 1.6: баобаб
        boolean hasBaobab = maybeSpawnBaobab(level, pos, columns);

        // Проход 2: деревья
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                maybeSpawnTree(level, minX + x, minZ + z, columns[x][z], hasBaobab);
            }
        }

        // Проход 2.5: растения, водоросли, лозы
        spawnPlants(level, pos, columns);

        // Проход 3: сетка искусственного света (солнце/луна)
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (x % AbyssAmbientLight.GRID_STEP != 0 || z % AbyssAmbientLight.GRID_STEP != 0) continue;
                Column c = columns[x][z];
                int worldX = minX + x, worldZ = minZ + z;
                int baseY = c.water() ? SEA_LEVEL : c.floorTop();
                int ly = baseY + AbyssAmbientLight.HEIGHT_ABOVE_FLOOR;
                BlockPos lp = new BlockPos(worldX, ly, worldZ);
                if (level.getBlockState(lp).isAir()) {
                    int startLevel = AbyssAmbientLight.computeLightLevel(level.getDayTime());
                    level.setBlock(lp, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, startLevel), 2);
                    AbyssAmbientLight.registerLightPoint(pos, lp);
                }
            }
        }

        // Проход 3.5: данжи
        AbyssDungeons.generate(level, pos);

        // Проход 3.52: гнёзда динозавров
        AbyssDinosaurNests.generate(level, pos);

        // Проход 3.55: динозавры
        AbyssFauna.populate(level, pos);

        // Проход 3.6: единственный Замок Древней Цивилизации
        AncientFrozenCastleGenerator.generate(level, pos);

        // Проход 4: шахтные стволы
        for (int x = 2; x < 13; x++) {
            for (int z = 2; z < 13; z++) {
                Column c = columns[x][z];
                if (c.water() || c.lava()) continue;
                if (hasBaobab && (x - 8) * (x - 8) + (z - 8) * (z - 8) <= 36) continue;
                int worldX = minX + x, worldZ = minZ + z;
                if (!oreRoll(worldX, c.floorTop(), worldZ, 70001, SHAFT_CHANCE)) continue;
                if (isTreeFootprint(level, worldX, c.floorTop(), worldZ)) continue;
                carveEntranceShaft(level, pos, worldX, c.floorTop(), worldZ);
            }
        }

        // Ставим яйцо и создаём взрослую особь после всех остальных декораторов.
        QuetzalcoatlusMountainGenerator.populate(level, pos);

        Abyss.LOGGER.info(">>> Biome1 сгенерирован для чанка {}", pos);
    }

    private static Column generateColumn(ServerLevel level, int x, int z) {
        // ----- Вода: море, озёра, реки -----
        double sea = noise01(x, z, 5000, 220);
        double lake = noise01(x, z, 6000, 64);
        double river = Math.abs(noise01(x, z, 7000, 110) - 0.5);

        int waterDepth = 0;

// Моря: до 60 блоков
        if (sea < SEA_THRESHOLD) {
            double factor = (SEA_THRESHOLD - sea) / SEA_THRESHOLD;
            waterDepth = Math.max(
                    waterDepth,
                    5 + (int) (factor * 55)
            );
        }

// Озёра: до 50 блоков
        if (lake < LAKE_THRESHOLD) {
            double factor = (LAKE_THRESHOLD - lake) / LAKE_THRESHOLD;
            waterDepth = Math.max(
                    waterDepth,
                    4 + (int) (factor * 46)
            );
        }

// Реки: до 30 блоков
        if (river < RIVER_HALF_WIDTH) {
            double factor =
                    (RIVER_HALF_WIDTH - river) / RIVER_HALF_WIDTH;

            waterDepth = Math.max(
                    waterDepth,
                    3 + (int) (factor * 27)
            );
        }

        waterDepth = Math.min(waterDepth, MAX_WATER_DEPTH);
        boolean isWater = waterDepth > 0;

        // 0 = у самой воды, 1 = далеко от воды
        double landness = Math.min(
                Math.min((sea - SEA_THRESHOLD) / SHORE_MARGIN, (lake - LAKE_THRESHOLD) / SHORE_MARGIN),
                (river - RIVER_HALF_WIDTH) / RIVER_MARGIN);
        landness = clamp(landness, 0.0, 1.0);
        boolean shore = !isWater && landness < 0.6;

        // ----- Холмы (у воды плавно сходят на нет) -----
        double region = noise01(x, z, 3000, 80);
        double hillFactor = clamp((region - 0.35) / 0.45, 0.0, 1.0);
        double baseNoise = noise01(x, z, 0, 12);
        int hillHeight = (int) Math.round(baseNoise * MAX_HILL_HEIGHT * hillFactor * landness);

        // ----- Зона -----
        double volcanicNoise = noise01(x, z, 9000, 150);
        double poisonNoise = noise01(x, z, 12000, 90);
        Zone zone = Zone.NORMAL;
        if (!isWater) {
            if (landness >= 0.5 && volcanicNoise > VOLCANIC_THRESHOLD) zone = Zone.VOLCANIC;
            else if (poisonNoise > POISON_THRESHOLD) zone = Zone.POISON;
        }
        boolean lava = zone == Zone.VOLCANIC && noise01(x, z, 15000, 18) > LAVA_THRESHOLD;

        // Суша всегда МИНИМУМ на 1 выше воды
        // Calculate the ceiling before filling terrain, so peaks never pierce it.
        double ceilingBaseNoise = noise01(x, z, 1000, 40);
        double ceilingDetailNoise = noise01(x, z, 1500, 7);
        int ceilingDip = (int) Math.round(ceilingBaseNoise * MAX_CEILING_ROUGHNESS
                + ceilingDetailNoise * CEILING_DETAIL_ROUGHNESS);
        if (noise01(x, z, 1800, 1) < STALACTITE_CHANCE) {
            ceilingDip += 4 + (int) (noise01(x, z, 1900, 1) * 8);
        }
        int ceilingBottom = CEILING_Y - ceilingDip;

        double mountainNoise = noise01(x, z, 84520, 180);
        double mountainFactor = clamp((mountainNoise - 0.73D) / 0.27D, 0.0D, 1.0D);
        int mountainHeight = (!isWater && !lava && landness > 0.7D)
                ? (int) Math.round(mountainFactor * mountainFactor * QUETZAL_MOUNTAIN_MAX_HEIGHT)
                : 0;
        int landTop = SEA_LEVEL + 1 + hillHeight + mountainHeight;
        landTop = Math.min(landTop, ceilingBottom - 22);
        int floorTop;
        if (isWater) floorTop = SEA_LEVEL - waterDepth;
        else if (lava) floorTop = landTop - 3;
        else floorTop = landTop;

        // ----- Заливка земли -----
        // Для глубоких водоёмов создаём грунт ниже обычного FLOOR_Y
        int groundStartY = isWater
                ? Math.min(FLOOR_Y, floorTop - 12)
                : FLOOR_Y;
        for (int y = groundStartY; y <= floorTop; y++) {
            BlockState state;
            if (isWater && y > floorTop - SAND_LAYERS) {
                state = oreRoll(x, y, z, 40003, WATER_ORE_CHANCE)
                        ? Abyss.WATER_ORE.get().defaultBlockState()
                        : Blocks.SAND.defaultBlockState();
            } else if (!isWater && y == floorTop) {
                state = topBlock(zone, shore);
            } else {
                state = Abyss.ABYSS_SOIL.get().defaultBlockState();
                if (y < floorTop - 1) {

                    if (oreRoll(x, y, z, 40001, EARTH_ORE_CHANCE)) {

                        state = Abyss.EARTH_ORE.get().defaultBlockState();

                    } else if (
                            zone == Zone.VOLCANIC &&
                                    oreRoll(x, y, z, 40002, FIRE_ORE_CHANCE)
                    ) {

                        state = Abyss.FIRE_ORE.get().defaultBlockState();

                    } else if (oreRoll(x, y, z, 40100, RUBY_ORE_CHANCE)) {

                        state = Abyss.RUBY_ORE.get().defaultBlockState();

                    } else if (oreRoll(x, y, z, 40101, AMBER_ORE_CHANCE)) {

                        state = Abyss.AMBER_ORE.get().defaultBlockState();

                    } else {

                        state = AbyssVanillaOres.tryGenerate(
                                x,
                                y,
                                z,
                                state
                        );
                    }
                }
            }
            level.setBlock(new BlockPos(x, y, z), state, 2);
        }

        // Вода: ровно до SEA_LEVEL
        if (isWater) {
            for (int y = floorTop + 1; y <= SEA_LEVEL; y++) {
                level.setBlock(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState(), 2);
            }
        }

        // Подводные туннели первого биома (восстановлено).
        // Выполняем после формирования грунта и воды, как в исходном генераторе.
        AbyssUnderwaterTunnels.generateColumn(
                level,
                x,
                z,
                floorTop,
                isWater
        );

        // Лава: на 1 ниже окружающей земли
        if (lava) {
            for (int y = floorTop + 1; y < landTop; y++) {
                level.setBlock(new BlockPos(x, y, z), Blocks.LAVA.defaultBlockState(), 2);
            }
        }

        // ----- Неровный потолок: камень свисает вниз -----
        // ceilingBottom was calculated above, before the mountain terrain.
        BlockState ceilingBlock = Blocks.STONE.defaultBlockState();
        for (int y = ceilingBottom; y < CEILING_Y; y++) {
            level.setBlock(new BlockPos(x, y, z), ceilingBlock, 2);
        }

        if (oreRoll(x, ceilingBottom, z, 80001, CEILING_LIGHT_CHANCE)) {
            level.setBlock(new BlockPos(x, ceilingBottom, z), Abyss.ABYSS_LIGHT_STONE.get().defaultBlockState(), 2);
        }

        return new Column(floorTop, zone, isWater, lava, ceilingBottom);
    }

    private static BlockState topBlock(Zone zone, boolean shore) {
        if (zone == Zone.VOLCANIC) return Abyss.ABYSS_CHARRED_GROUND.get().defaultBlockState();
        if (zone == Zone.POISON || shore) return Abyss.ABYSS_GRASS_WET.get().defaultBlockState(); // синяя земля
        return Abyss.ABYSS_GRASS.get().defaultBlockState();
    }

    private static void maybeSpawnTree(ServerLevel level, int x, int z, Column c, boolean baobabHere) {
        if (c.water() || c.lava()) return;

        // крона радиусом 2 не должна выходить за границу чанка
        int lx = x & 15, lz = z & 15;
        if (lx < 3 || lx > 12 || lz < 3 || lz > 12) return;
        if (baobabHere && (lx - 8) * (lx - 8) + (lz - 8) * (lz - 8) <= 36) return;

        double density = switch (c.zone()) {
            case NORMAL -> 0.05;
            case POISON -> 0.08;
            case VOLCANIC -> 0.015;
        };
        if (noise01(x, z, 20000, 1) > density) return;

        int range = TREE_MAX_TRUNK - TREE_MIN_TRUNK;
        int trunkHeight = TREE_MIN_TRUNK + Math.min(range, (int) (noise01(x, z, 21000, 1) * (range + 1)));
        int trunkTop = c.floorTop() + trunkHeight;
        if (trunkTop + 3 >= c.ceilingBottom()) return; // упирается в потолок

        RotatedPillarBlock log = switch (c.zone()) {
            case VOLCANIC -> Abyss.ABYSS_CHARRED_LOG.get();  // серые
            case POISON -> Abyss.ABYSS_WHITE_LOG.get();      // белые
            case NORMAL -> Abyss.ABYSS_LOG.get();            // коричневые
        };
        BlockState logState = log.defaultBlockState();
        for (int y = c.floorTop() + 1; y <= trunkTop; y++) {
            level.setBlock(new BlockPos(x, y, z), logState, 2);
        }

        if (c.zone() == Zone.VOLCANIC) return; // мёртвые деревья без листьев

        BlockState leaves = pickLeaves(c.zone(), x, z);
        placeLeafLayer(level, x, z, trunkTop - 2, leaves, true);
        placeLeafLayer(level, x, z, trunkTop - 1, leaves, true);
        placeLeafLayer(level, x, z, trunkTop, leaves, false);
        level.setBlock(new BlockPos(x, trunkTop + 1, z), leaves, 2);
    }

    private static void spawnPlants(ServerLevel level, ChunkPos pos, Column[][] columns) {
        int minX = pos.getMinBlockX(), minZ = pos.getMinBlockZ();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                Column c = columns[x][z];
                int wx = minX + x, wz = minZ + z;

                // Лозы свисают с потолка (только если сверху есть сплошной блок)
                if (!c.lava() && oreRoll(wx, c.ceilingBottom(), wz, 63001, VINE_CHANCE)) {
                    BlockPos top = new BlockPos(wx, c.ceilingBottom() - 1, wz);
                    Block vine = pickVine(c.zone(), wx, top.getY(), wz);
                    int len = 1 + (int) ((hash3(wx, top.getY(), wz, 63002) >>> 8) % 4);
                    for (int i = 0; i < len; i++) {
                        BlockPos vp = top.below(i);
                        BlockState vs = vine.defaultBlockState();
                        if (!level.getBlockState(vp).isAir() || !vs.canSurvive(level, vp)) break;
                        level.setBlock(vp, vs, 2);
                    }
                }

                // Вода: подводные растения и кувшинки
                if (c.water()) {
                    BlockPos wp = new BlockPos(wx, c.floorTop() + 1, wz);
                    if (oreRoll(wx, wp.getY(), wz, 61001, WATER_PLANT_CHANCE) && level.getBlockState(wp).is(Blocks.WATER)) {
                        BlockState ws = pickWaterPlant(wx, wp.getY(), wz).defaultBlockState();
                        if (ws.canSurvive(level, wp)) level.setBlock(wp, ws, 2);
                    }
                    if (SEA_LEVEL - c.floorTop() <= 6 && oreRoll(wx, SEA_LEVEL, wz, 61002, LILY_CHANCE)) {
                        BlockPos lp = new BlockPos(wx, SEA_LEVEL + 1, wz);
                        if (level.getBlockState(lp).isAir()) {
                            level.setBlock(lp, ModPlants.LILY_PAD_FLOWER.get().defaultBlockState(), 2);
                        }
                    }
                    continue;
                }
                if (c.lava()) continue;

                // Суша
                BlockPos gp = new BlockPos(wx, c.floorTop() + 1, wz);
                if (!level.getBlockState(gp).isAir()) continue; // ствол, крона или что-то ещё
                double chance = c.zone() == Zone.VOLCANIC ? VOLCANIC_PLANT_CHANCE : LAND_PLANT_CHANCE;
                if (!oreRoll(wx, gp.getY(), wz, 62001, chance)) continue;

                Block plant;
                if (c.zone() != Zone.VOLCANIC && nearWater(columns, x, z) && oreRoll(wx, gp.getY(), wz, 62003, 0.5)) {
                    plant = ModPlants.CATTAIL.get();
                } else {
                    plant = pickLandPlant(c.zone(), wx, gp.getY(), wz);
                }
                BlockState ps = plant.defaultBlockState();
                if (ps.canSurvive(level, gp)) level.setBlock(gp, ps, 2);
            }
        }
    }

    private static boolean nearWater(Column[][] cols, int x, int z) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                int nx = x + dx, nz = z + dz;
                if (nx < 0 || nx > 15 || nz < 0 || nz > 15) continue;
                if (cols[nx][nz].water()) return true;
            }
        }
        return false;
    }

    private static Block pick(Block[] options, int x, int y, int z, int seed) {
        return options[(int) ((hash3(x, y, z, seed) >>> 8) % options.length)];
    }

    private static Block pickLandPlant(Zone zone, int x, int y, int z) {
        Block[] options = switch (zone) {
            case NORMAL -> new Block[]{ModPlants.BELL_FLOWER.get(), ModPlants.PINK_FLOWER.get(),
                    ModPlants.STAR_FLOWER.get(), ModPlants.TALL_GRASS.get(), ModPlants.TALL_GRASS.get(),
                    ModPlants.GOLDEN_GRASS.get(), ModPlants.DARK_MUSHROOM.get()};
            case POISON -> new Block[]{ModPlants.PURPLE_GRASS.get(), ModPlants.PURPLE_GRASS.get(),
                    ModPlants.CYAN_MUSHROOM.get(), ModPlants.DARK_MUSHROOM.get(), ModPlants.STAR_FLOWER.get()};
            case VOLCANIC -> new Block[]{ModPlants.FIRE_FLOWER.get(), ModPlants.FIRE_MUSHROOM.get(),
                    ModPlants.FIRE_FLOWER.get()};
        };
        return pick(options, x, y, z, 62002);
    }

    private static Block pickVine(Zone zone, int x, int y, int z) {
        Block[] options = switch (zone) {
            case NORMAL -> new Block[]{ModPlants.WHITE_VINE.get(), ModPlants.PINK_VINE.get()};
            case POISON -> new Block[]{ModPlants.PURPLE_VINE.get(), ModPlants.WHITE_VINE.get()};
            case VOLCANIC -> new Block[]{ModPlants.FIRE_VINE.get()};
        };
        return pick(options, x, y, z, 63003);
    }

    private static Block pickWaterPlant(int x, int y, int z) {
        Block[] options = {
                ModPlants.GREEN_SEAGRASS.get(), ModPlants.GREEN_SEAGRASS.get(), ModPlants.GREEN_SEAGRASS.get(),
                ModPlants.CYAN_SEAGRASS.get(), ModPlants.CYAN_SEAGRASS.get(),
                ModPlants.RED_SEAGRASS.get(), ModPlants.RED_SEAGRASS.get(),
                ModPlants.BLUE_SEAGRASS.get(), ModPlants.BLUE_SEAGRASS.get(),
                ModPlants.RED_CORAL.get(), ModPlants.PURPLE_CORAL.get(),
                ModPlants.BLUE_AQUA_FLOWER.get(), ModPlants.PINK_AQUA_FLOWER.get()};
        return pick(options, x, y, z, 61003);
    }

    private static boolean maybeSpawnBaobab(ServerLevel level, ChunkPos pos, Column[][] columns) {
        Random rnd = new Random(BAOBAB_SEED ^ ((long) pos.x * 341873128712L) ^ ((long) pos.z * 132897987541L));
        if (rnd.nextInt(BAOBAB_RARITY) != 0) return false;

        // площадка вокруг центра чанка должна быть сухой, ровной и не вулканической
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, minCeiling = Integer.MAX_VALUE;
        for (int x = 4; x <= 12; x += 2) {
            for (int z = 4; z <= 12; z += 2) {
                Column c = columns[x][z];
                if (c.water() || c.lava() || c.zone() == Zone.VOLCANIC) return false;
                min = Math.min(min, c.floorTop());
                max = Math.max(max, c.floorTop());
                minCeiling = Math.min(minCeiling, c.ceilingBottom());
            }
        }
        if (max - min > 3) return false;

        int baseY = max + 1;
        if (baseY + BaobabGenerator.HEIGHT + 5 >= minCeiling) return false; // не помещается под потолок

        BaobabGenerator.generate(level, new BlockPos(pos.getMinBlockX() + 8, baseY, pos.getMinBlockZ() + 8), rnd);
        return true;
    }

    private static void carveLocalCaves(ServerLevel level, ChunkPos pos, int minX, int minZ, Column[][] columns) {
        long seed = CAVE_LOCAL_SEED ^ ((long) pos.x * 341873128712L) ^ ((long) pos.z * 132897987541L);
        Random rnd = new Random(seed);
        if (rnd.nextDouble() > LOCAL_CAVE_CHANCE) return;

        int caveCount = 1 + rnd.nextInt(2);
        for (int c = 0; c < caveCount; c++) {
            int lx = rnd.nextInt(16), lz = rnd.nextInt(16);
            Column col = columns[lx][lz];
            if (col.water() || col.lava()) continue;

            double x = minX + lx;
            double z = minZ + lz;
            double y = col.floorTop() - 1; // старт прямо под поверхностью — гарантированный выход наружу
            double yaw = rnd.nextDouble() * Math.PI * 2;
            double pitch = -0.25 - rnd.nextDouble() * 0.2; // сразу уходим вниз
            int steps = 18 + rnd.nextInt(16);

            for (int i = 0; i < steps; i++) {
                yaw += (rnd.nextDouble() - 0.5) * 0.6;
                pitch += (rnd.nextDouble() - 0.5) * 0.15;
                pitch = Math.max(-0.45, Math.min(0.25, pitch));

                x += Math.cos(yaw) * 1.4;
                z += Math.sin(yaw) * 1.4;
                y += Math.sin(pitch) * 1.4;
                if (y < LOCAL_CAVE_BOTTOM) break;

                double radius = (i < 3) ? (1.2 + rnd.nextDouble()) : (1.6 + rnd.nextDouble() * 1.4);
                carveLocalSphere(level, pos, x, y, z, radius);
            }
        }
    }

    private static void carveLocalSphere(ServerLevel level, ChunkPos target, double cx, double cy, double cz, double radius) {
        int minXi = (int) Math.floor(cx - radius), maxXi = (int) Math.ceil(cx + radius);
        int minYi = (int) Math.floor(cy - radius), maxYi = (int) Math.ceil(cy + radius);
        int minZi = (int) Math.floor(cz - radius), maxZi = (int) Math.ceil(cz + radius);
        for (int x = minXi; x <= maxXi; x++) {
            if ((x >> 4) != target.x) continue;
            for (int z = minZi; z <= maxZi; z++) {
                if ((z >> 4) != target.z) continue;
                for (int y = minYi; y <= maxYi; y++) {
                    double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy) * 1.3 + (z - cz) * (z - cz));
                    if (dist <= radius) {
                        BlockState cur = level.getBlockState(new BlockPos(x, y, z));
                        if (cur.is(Blocks.WATER) || cur.is(Blocks.LAVA)) continue;
                        level.setBlock(new BlockPos(x, y, z), Blocks.CAVE_AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static boolean isTreeFootprint(ServerLevel level, int x, int floorTop, int z) {
        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockState s = level.getBlockState(new BlockPos(x + dx, floorTop + 1, z + dz));
                if (s.is(Abyss.ABYSS_LOG.get()) || s.is(Abyss.ABYSS_CHARRED_LOG.get()) || s.is(Abyss.ABYSS_WHITE_LOG.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void carveEntranceShaft(ServerLevel level, ChunkPos pos, int x, int topY, int z) {
        int bottomY = Math.max(topY - SHAFT_DEPTH, SHAFT_MIN_Y);
        for (int y = topY; y >= bottomY; y--) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockPos p = new BlockPos(x + dx, y, z + dz);
                    BlockState cur = level.getBlockState(p);
                    if (cur.is(Blocks.WATER) || cur.is(Blocks.LAVA)) continue;
                    level.setBlock(p, Blocks.CAVE_AIR.defaultBlockState(), 2);
                }
            }
            level.setBlock(new BlockPos(x, y, z),
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST), 2);
            if ((topY - y) % 8 == 0) {
                level.setBlock(new BlockPos(x + 1, y, z + 1), Abyss.ABYSS_LIGHT_STONE.get().defaultBlockState(), 2);
            }
        }

        // Гарантированный боковой ход — чтобы шахта не была тупиком
        Random tunnelRnd = new Random(hash3(x, bottomY, z, 90001));
        double yaw = tunnelRnd.nextDouble() * Math.PI * 2;
        double tx = x + 0.5, tz = z + 0.5, ty = bottomY;
        int length = 18 + tunnelRnd.nextInt(18);
        for (int i = 0; i < length; i++) {
            yaw += (tunnelRnd.nextDouble() - 0.5) * 0.5;
            tx += Math.cos(yaw) * 1.4;
            tz += Math.sin(yaw) * 1.4;
            ty += (tunnelRnd.nextDouble() - 0.5) * 1.0;
            carveLocalSphere(level, pos, tx, ty, tz, 2.0 + tunnelRnd.nextDouble());
        }

        for (int dx = -1; dx <= 2; dx++) {
            for (int dz = -1; dz <= 2; dz++) {
                if (dx == -1 || dx == 2 || dz == -1 || dz == 2) {
                    BlockPos rim = new BlockPos(x + dx, topY + 1, z + dz);
                    if (level.getBlockState(rim).isAir()) {
                        level.setBlock(rim, Blocks.STONE_BRICK_WALL.defaultBlockState(), 2);
                    }
                }
            }
        }
    }

    private static void placeLeafLayer(ServerLevel level, int cx, int cz, int y, BlockState leaves, boolean wide) {
        int radius = wide ? 3 : 2;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (wide && Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                BlockPos p = new BlockPos(cx + dx, y, cz + dz);
                if (level.getBlockState(p).isAir()) {
                    level.setBlock(p, leaves, 2);
                }
            }
        }
    }

    // Три леса: цвет зависит от БОЛЬШОЙ области, а не от каждого дерева
    private static BlockState pickLeaves(Zone zone, int x, int z) {
        LeavesBlock block;
        if (zone == Zone.POISON) {
            block = Abyss.PURPLE_LEAVES.get();
        } else {
            double forest = noise01(x, z, 30000, 100);
            if (forest < 0.44) block = Abyss.RED_LEAVES.get();          // красный лес
            else if (forest < 0.56) block = Abyss.YELLOW_LEAVES.get();  // жёлтый лес
            else block = Abyss.GREEN_LEAVES.get();                      // зелёный лес
        }
        // PERSISTENT = true: листья не опадают
        return block.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
    }

    public static double noise01(int worldX, int worldZ, int seedOffset, int scale) {
        double fx = worldX / (double) scale, fz = worldZ / (double) scale;
        int gx0 = (int) Math.floor(fx), gz0 = (int) Math.floor(fz);
        double tx = fx - gx0, tz = fz - gz0;
        double v00 = latticeNoise(gx0, gz0, seedOffset);
        double v10 = latticeNoise(gx0 + 1, gz0, seedOffset);
        double v01 = latticeNoise(gx0, gz0 + 1, seedOffset);
        double v11 = latticeNoise(gx0 + 1, gz0 + 1, seedOffset);
        return lerp(tz, lerp(tx, v00, v10), lerp(tx, v01, v11));
    }

    private static double latticeNoise(int gx, int gz, int seedOffset) {
        long hash = (long) gx * 341873128712L + (long) gz * 132897987541L + seedOffset * 7919L;
        hash = (hash ^ (hash >>> 33)) * 0xff51afd7ed558ccdL;
        hash = (hash ^ (hash >>> 33)) * 0xc4ceb9fe1a85ec53L;
        hash = hash ^ (hash >>> 33);
        return (hash & 0xFFFFFF) / (double) 0xFFFFFF;
    }

    private static double lerp(double t, double a, double b) { return a + t * (b - a); }
    private static double clamp(double v, double min, double max) { return Math.max(min, Math.min(max, v)); }
}