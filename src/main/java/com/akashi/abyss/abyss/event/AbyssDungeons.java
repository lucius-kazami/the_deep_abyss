package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class AbyssDungeons {
    private AbyssDungeons() {}

    // ---------- Настройки ----------
    private static final long WORLD_SEED = 7731905521L;
    private static final int CELL = 224;
    private static final int MARGIN = 70;
    private static final double VILLAGE_CHANCE = 0.34;
    private static final double CITADEL_CHANCE = 0.15;
    private static final double TOWER_CHANCE = 0.18;
    private static final int UG_FLOOR = -470;

    private static final int VILLAGE = 0;
    private static final int CITADEL = 1;
    private static final int TOWER = 2;
    private static final String[] NAMES = { "село древних", "цитадель с подземельем", "башня древних" };

    private static final int TOWER_RADIUS = 14;

    private static final int TOWER_CLEARANCE = 10;
    private static final int TOWER_MAX_UPPER_FLOORS = 70;

    // Верхние этажи
    private static final int TOWER_UPPER_FLOORS = 4;
    private static final int TOWER_FLOOR_HEIGHT = 8;

    // Подземные этажи: -1, -2 и финальный -3
    private static final int TOWER_BASEMENT_LEVELS = 3;

    private static final int TOWER_MIN_HEIGHT = 45;
    private static final int TOWER_MAX_HEIGHT = 80;

    private static final ResourceKey<LootTable> LOOT_RUINS = lootKey("chests/ruins");
    private static final ResourceKey<LootTable> LOOT_DUNGEON = lootKey("chests/dungeon");

    private static final EntityType<?>[] MOBS = { EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER };

    private record Site(int type, int ox, int oz, int baseY, long seed) {}

    private static ResourceKey<LootTable> lootKey(String path) {
        return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath(Abyss.MODID, path));
    }

    // ================= Точка входа =================
    public static void generate(ServerLevel level, ChunkPos pos) {
        int minX = pos.getMinBlockX(), minZ = pos.getMinBlockZ();
        int cx0 = Math.floorDiv(minX - MARGIN, CELL), cx1 = Math.floorDiv(minX + 15 + MARGIN, CELL);
        int cz0 = Math.floorDiv(minZ - MARGIN, CELL), cz1 = Math.floorDiv(minZ + 15 + MARGIN, CELL);
        Clip clip = new Clip(level, minX, minZ);

        for (int cx = cx0; cx <= cx1; cx++) {
            for (int cz = cz0; cz <= cz1; cz++) {
                Site s = siteForCell(cx, cz);
                if (s == null) continue;
                if (s.ox() + MARGIN < minX || s.ox() - MARGIN > minX + 15) continue;
                if (s.oz() + MARGIN < minZ || s.oz() - MARGIN > minZ + 15) continue;
                if (clip.in(s.ox(), s.oz())) {
                    Abyss.LOGGER.info(">>> Данж ({}) центр: {} {}", NAMES[s.type()], s.ox(), s.oz());
                }
                switch (s.type()) {
                    case VILLAGE -> village(clip, s);
                    case CITADEL -> citadel(clip, s);
                    default -> tower(clip, s);
                }
            }
        }
    }

    // ================= Выбор места =================
    private static Site siteForCell(int cx, int cz) {
        Random r = new Random(WORLD_SEED ^ ((long) cx * 341873128712L) ^ ((long) cz * 132897987541L));
        double roll = r.nextDouble();
        int type;
        if (roll < VILLAGE_CHANCE) type = VILLAGE;
        else if (roll < VILLAGE_CHANCE + CITADEL_CHANCE) type = CITADEL;
        else if (roll < VILLAGE_CHANCE + CITADEL_CHANCE + TOWER_CHANCE) type = TOWER;
        else return null;

        for (int attempt = 0; attempt < 5; attempt++) {
            int ox = cx * CELL + MARGIN + r.nextInt(CELL - 2 * MARGIN);
            int oz = cz * CELL + MARGIN + r.nextInt(CELL - 2 * MARGIN);
            int base = evaluate(ox, oz, type);
            if (base != Integer.MIN_VALUE) return new Site(type, ox, oz, base, r.nextLong());
        }
        return null;
    }

    private static int evaluate(int ox, int oz, int type) {
        for (int[] ch : AbyssGreatChasms.CENTERS) {
            if (Math.hypot(ox - ch[0], oz - ch[1]) < 100) return Integer.MIN_VALUE;
        }
        int rad = type == VILLAGE ? 22 : (type == TOWER ? TOWER_RADIUS + 3 : 20);
        int maxVar = type == VILLAGE ? 18 : (type == CITADEL ? 12 : 8);
        int[][] offs = { {0, 0}, {rad, 0}, {-rad, 0}, {0, rad}, {0, -rad}, {rad, rad}, {-rad, rad}, {rad, -rad}, {-rad, -rad} };
        int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE, minCeiling = Integer.MAX_VALUE;
        long sum = 0;
        for (int[] o : offs) {
            AbyssTerrain.Sample t = AbyssTerrain.sample(ox + o[0], oz + o[1]);
            if (t.water() || t.lava()) return Integer.MIN_VALUE;
            min = Math.min(min, t.floorTop());
            max = Math.max(max, t.floorTop());
            sum += t.floorTop();
            if (type == TOWER) minCeiling = Math.min(minCeiling, AbyssTerrain.ceilingBottom(ox + o[0], oz + o[1]));
        }
        if (max - min > maxVar) return Integer.MIN_VALUE;
        int avg = (int) Math.round(sum / (double) offs.length);
        if (type == TOWER && (minCeiling - avg) < TOWER_MIN_HEIGHT + TOWER_CLEARANCE) return Integer.MIN_VALUE;
        return avg;
    }

    // ================= Клип: пишем только в текущий чанк =================
    private static final class Clip {
        final ServerLevel level;
        final int minX, minZ, maxX, maxZ;

        Clip(ServerLevel level, int minX, int minZ) {
            this.level = level;
            this.minX = minX;
            this.minZ = minZ;
            this.maxX = minX + 15;
            this.maxZ = minZ + 15;
        }

        boolean in(int x, int z) { return x >= minX && x <= maxX && z >= minZ && z <= maxZ; }

        void set(int x, int y, int z, BlockState state) {
            if (!in(x, z)) return;
            if (x == minX && z == minZ && y == AbyssTerrain.FLOOR_Y) return; // метка "чанк сгенерирован"
            level.setBlock(new BlockPos(x, y, z), state, 2);
        }

        void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
            int ax = Math.max(Math.min(x0, x1), minX), bx = Math.min(Math.max(x0, x1), maxX);
            int az = Math.max(Math.min(z0, z1), minZ), bz = Math.min(Math.max(z0, z1), maxZ);
            int ay = Math.min(y0, y1), by = Math.max(y0, y1);
            for (int x = ax; x <= bx; x++)
                for (int z = az; z <= bz; z++)
                    for (int y = ay; y <= by; y++)
                        set(x, y, z, state);
        }

        void chest(int x, int y, int z, Direction facing, ResourceKey<LootTable> table, long seed) {
            if (!in(x, z)) return;
            BlockPos p = new BlockPos(x, y, z);
            level.setBlock(p, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
            if (level.getBlockEntity(p) instanceof ChestBlockEntity chestEntity) {
                chestEntity.setLootTable(table, seed);
            }
        }

        void spawner(int x, int y, int z, EntityType<?> type) {
            if (!in(x, z)) return;
            BlockPos p = new BlockPos(x, y, z);
            level.setBlock(p, Blocks.SPAWNER.defaultBlockState(), 2);
            if (level.getBlockEntity(p) instanceof SpawnerBlockEntity sp) {
                sp.setEntityId(type, level.getRandom());
            }
        }
        void door(int x, int y, int z, Direction facing) {
            if (!in(x, z)) return;
            BlockState lower = Blocks.OAK_DOOR.defaultBlockState()
                    .setValue(DoorBlock.FACING, facing)
                    .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
            BlockState upper = lower.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            set(x, y, z, lower);
            set(x, y + 1, z, upper);
        }
    }

    // ================= Палитры (разные "цивилизации") =================
    private record Palette(BlockState[] wall, BlockState[] floor) {}

    private static Palette palette(int index) {
        return switch (index) {
            case 1 -> new Palette(
                    new BlockState[] { Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Blocks.DEEPSLATE_TILES.defaultBlockState(),
                            Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState(), Blocks.COBBLED_DEEPSLATE.defaultBlockState() },
                    new BlockState[] { Blocks.COBBLED_DEEPSLATE.defaultBlockState(), Blocks.POLISHED_DEEPSLATE.defaultBlockState(),
                            Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState() });
            case 2 -> new Palette(
                    new BlockState[] { Blocks.SANDSTONE.defaultBlockState(), Blocks.CHISELED_SANDSTONE.defaultBlockState(),
                            Blocks.CUT_SANDSTONE.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState() },
                    new BlockState[] { Blocks.SANDSTONE.defaultBlockState(), Blocks.SMOOTH_SANDSTONE.defaultBlockState(),
                            Blocks.CHISELED_SANDSTONE.defaultBlockState(), Blocks.SAND.defaultBlockState(), Blocks.RED_SAND.defaultBlockState() });
            default -> new Palette(
                    new BlockState[] { Blocks.STONE_BRICKS.defaultBlockState(), Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                            Blocks.CRACKED_STONE_BRICKS.defaultBlockState(), Blocks.COBBLESTONE.defaultBlockState() },
                    new BlockState[] { Blocks.COBBLESTONE.defaultBlockState(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                            Blocks.STONE_BRICKS.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), Blocks.CRACKED_STONE_BRICKS.defaultBlockState() });
        };
    }

    private static BlockState wallMix(Palette p, double r) {
        if (r < 0.55) return p.wall()[0];
        if (r < 0.80) return p.wall()[1];
        if (r < 0.95) return p.wall()[2];
        return p.wall()[3];
    }

    private static BlockState floorMix(Palette p, double r) {
        if (r < 0.30) return p.floor()[0];
        if (r < 0.50) return p.floor()[1];
        if (r < 0.70) return p.floor()[2];
        if (r < 0.85) return p.floor()[3];
        return p.floor()[4];
    }

    // ---- материалы по умолчанию для цитадели/башни (свой отдельный стиль) ----
    private static BlockState air() { return Blocks.AIR.defaultBlockState(); }
    private static BlockState cobweb() { return Blocks.COBWEB.defaultBlockState(); }
    private static BlockState plainBrick() { return Blocks.STONE_BRICKS.defaultBlockState(); }
    private static BlockState lightStone() { return Abyss.ABYSS_LIGHT_STONE.get().defaultBlockState(); }

    private static BlockState brick(double r) {
        if (r < 0.55) return Blocks.STONE_BRICKS.defaultBlockState();
        if (r < 0.80) return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        if (r < 0.95) return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        return Blocks.COBBLESTONE.defaultBlockState();
    }

    private static double decayProb(int h) {
        return switch (h) {
            case 1 -> 0.08;
            case 2 -> 0.30;
            case 3 -> 0.55;
            case 4 -> 0.70;
            default -> 0.82;
        };
    }

    private static double h01(int x, int y, int z, int salt) {
        long h = (long) x * 341873128712L + (long) y * 668265263L + (long) z * 132897987541L + salt * 7919L;
        h = (h ^ (h >>> 33)) * 0xff51afd7ed558ccdL;
        h = (h ^ (h >>> 33)) * 0xc4ceb9fe1a85ec53L;
        h = h ^ (h >>> 33);
        return (h & 0xFFFFFF) / (double) 0xFFFFFF;
    }

    // ================= Выравнивание площадки =================
    private static void pad(Clip c, int x0, int z0, int x1, int z1, int baseY, boolean pave) {
        int ax = Math.max(x0, c.minX), bx = Math.min(x1, c.maxX);
        int az = Math.max(z0, c.minZ), bz = Math.min(z1, c.maxZ);
        for (int x = ax; x <= bx; x++)
            for (int z = az; z <= bz; z++)
                padColumn(c, x, z, baseY, pave);
    }

    private static void padColumn(Clip c, int x, int z, int baseY, boolean pave) {
        int t = AbyssTerrain.sample(x, z).floorTop();
        for (int y = t + 1; y <= baseY; y++) {
            c.set(x, y, z, y >= baseY - 1 ? floorMix(palette(0), h01(x, y, z, 11)) : Abyss.ABYSS_SOIL.get().defaultBlockState());
        }
        int top = Math.max(t, baseY) + 9;
        for (int y = baseY + 1; y <= top; y++) c.set(x, y, z, air());
        if (pave) c.set(x, baseY, z, floorMix(palette(0), h01(x, baseY, z, 12)));
    }

    // ================= 1. Село древних =================
    private static void village(Clip c, Site s) {
        Random r = new Random(s.seed());
        int ox = s.ox(), oz = s.oz();
        int salt = (int) (s.seed() ^ (s.seed() >>> 32));
        int plazaY = AbyssTerrain.sample(ox, oz).floorTop();
        Palette pal = palette(r.nextInt(3)); // разная "цивилизация" каждый раз

        // площадь
        int pax = Math.max(ox - 6, c.minX), pbx = Math.min(ox + 6, c.maxX);
        int paz = Math.max(oz - 6, c.minZ), pbz = Math.min(oz + 6, c.maxZ);
        for (int x = pax; x <= pbx; x++)
            for (int z = paz; z <= pbz; z++)
                if (Math.hypot(x - ox, z - oz) <= 5.7) padVillageColumn(c, x, z, plazaY, pal);

        int pillar = 3 + r.nextInt(3);
        for (int i = 1; i <= pillar; i++) {
            c.set(ox, plazaY + i, oz,
                    i == pillar ? Blocks.CHISELED_STONE_BRICKS.defaultBlockState() : wallMix(pal, h01(ox, plazaY + i, oz, salt)));
        }
        c.set(ox, plazaY + pillar + 1, oz, lightStone());

        // поваленная колонна (обломок цивилизации на краю площади)
        int pdx = r.nextBoolean() ? 1 : -1, pdz = r.nextBoolean() ? 1 : -1;
        int fx = ox + pdx * 5, fz = oz + pdz * 4;
        for (int i = 0; i < 4; i++) {
            int lx = fx + (Math.abs(pdx) == 1 ? i * -pdx : 0);
            int lz = fz + (Math.abs(pdz) == 1 && Math.abs(pdx) != 1 ? i * -pdz : 0);
            c.set(lx, plazaY + 1, lz, wallMix(pal, h01(lx, plazaY + 1, lz, salt + 40)));
        }
        c.set(fx, plazaY + 1, fz, Blocks.CHISELED_STONE_BRICKS.defaultBlockState());

        // колодец
        int wx = ox - pdx * 5, wz = oz - pdz * 4;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean rim = Math.abs(dx) == 1 || Math.abs(dz) == 1;
                if (rim) {
                    c.set(wx + dx, plazaY + 1, wz + dz, wallMix(pal, h01(wx + dx, plazaY + 1, wz + dz, salt + 50)));
                } else {
                    c.set(wx + dx, plazaY, wz + dz, Blocks.WATER.defaultBlockState());
                    c.set(wx + dx, plazaY - 1, wz + dz, floorMix(pal, h01(wx + dx, plazaY - 1, wz + dz, salt + 51)));
                }
            }
        }

        // мусор во дворе
        for (int i = 0; i < 10; i++) {
            double ang = r.nextDouble() * Math.PI * 2;
            int dist = 8 + r.nextInt(10);
            int rx = ox + (int) Math.round(Math.cos(ang) * dist);
            int rz = oz + (int) Math.round(Math.sin(ang) * dist);
            if (h01(rx, 0, rz, salt + 60) < 0.5) {
                int t = AbyssTerrain.sample(rx, rz).floorTop();
                c.set(rx, t + 1, rz, h01(rx, t + 1, rz, salt + 61) < 0.5 ? Blocks.GRAVEL.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
            }
        }

        int[][] lamps = { {5, 0}, {-5, 0}, {0, 5}, {0, -5} };
        for (int[] l : lamps) {
            int lx = ox + l[0], lz = oz + l[1];
            if (h01(lx, plazaY, lz, salt + 3) < 0.7) {
                c.set(lx, plazaY + 1, lz, Blocks.OAK_FENCE.defaultBlockState());
                c.set(lx, plazaY + 2, lz, Blocks.OAK_FENCE.defaultBlockState());
                c.set(lx, plazaY + 3, lz, lightStone());
            }
        }

        // дома по кольцу
        int n = 5 + r.nextInt(3);
        double a0 = r.nextDouble() * Math.PI * 2;
        List<int[]> rects = new ArrayList<>();
        List<int[]> starts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double ang = a0 + i * Math.PI * 2 / n + (r.nextDouble() - 0.5) * 0.2;
            int dist = 17 + r.nextInt(8);
            int bx0 = ox + (int) Math.round(Math.cos(ang) * dist);
            int bz0 = oz + (int) Math.round(Math.sin(ang) * dist);
            boolean hall = i == 0;
            int w = hall ? 9 : 5 + r.nextInt(3);
            int d = hall ? 9 : 5 + r.nextInt(3);
            int wallH = hall ? 6 : 3 + r.nextInt(2);
            boolean withChest = hall || r.nextDouble() < 0.5;
            boolean collapsed = !hall && r.nextDouble() < 0.3;
            long bseed = r.nextLong();

            int x0 = bx0 - w / 2, z0 = bz0 - d / 2, x1 = x0 + w - 1, z1 = z0 + d - 1;
            if (!buildable(x0, z0, x1, z1, bx0, bz0)) continue;

            int by = AbyssTerrain.sample(bx0, bz0).floorTop();
            int dx = ox - bx0, dz = oz - bz0;
            Direction door = Math.abs(dx) > Math.abs(dz)
                    ? (dx > 0 ? Direction.EAST : Direction.WEST)
                    : (dz > 0 ? Direction.SOUTH : Direction.NORTH);

            if (collapsed) {
                ruinFoundation(c, x0, z0, x1, z1, by, pal, bseed);
            } else {
                house(c, x0, z0, x1, z1, by, wallH, door, withChest, hall, pal, bseed);
            }
            rects.add(new int[] { x0, z0, x1, z1 });

            int cxm = (x0 + x1) / 2, czm = (z0 + z1) / 2;
            int sx = door == Direction.EAST ? x1 + 1 : (door == Direction.WEST ? x0 - 1 : cxm);
            int sz = door == Direction.SOUTH ? z1 + 1 : (door == Direction.NORTH ? z0 - 1 : czm);
            starts.add(new int[] { sx, sz });
        }

        for (int[] st : starts) path(c, ox, oz, st[0], st[1], rects, salt);
    }

    private static void padVillageColumn(Clip c, int x, int z, int baseY, Palette pal) {
        int t = AbyssTerrain.sample(x, z).floorTop();
        for (int y = t + 1; y <= baseY; y++) {
            c.set(x, y, z, y >= baseY - 1 ? floorMix(pal, h01(x, y, z, 11)) : Abyss.ABYSS_SOIL.get().defaultBlockState());
        }
        int top = Math.max(t, baseY) + 9;
        for (int y = baseY + 1; y <= top; y++) c.set(x, y, z, air());
        c.set(x, baseY, z, floorMix(pal, h01(x, baseY, z, 12)));
    }

    private static boolean buildable(int x0, int z0, int x1, int z1, int cx, int cz) {
        int[][] pts = { {cx, cz}, {x0, z0}, {x1, z0}, {x0, z1}, {x1, z1} };
        for (int[] p : pts) {
            AbyssTerrain.Sample t = AbyssTerrain.sample(p[0], p[1]);
            if (t.water() || t.lava()) return false;
        }
        return true;
    }

    /** Полностью разрушенный дом: только фундамент и обломки, без стен. */
    private static void ruinFoundation(Clip c, int x0, int z0, int x1, int z1, int by, Palette pal, long seed) {
        if (x1 + 1 < c.minX || x0 - 1 > c.maxX || z1 + 1 < c.minZ || z0 - 1 > c.maxZ) return;
        int salt = (int) (seed ^ (seed >>> 32));
        pad(c, x0, z0, x1, z1, by, true);
        int ax = Math.max(x0, c.minX), bx = Math.min(x1, c.maxX);
        int az = Math.max(z0, c.minZ), bz = Math.min(z1, c.maxZ);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (edge && h01(x, by + 1, z, salt) < 0.55) {
                    c.set(x, by + 1, z, wallMix(pal, h01(x, by + 1, z, salt + 1)));
                } else if (!edge && h01(x, by + 1, z, salt + 2) < 0.18) {
                    c.set(x, by + 1, z, floorMix(pal, h01(x, by + 1, z, salt + 3)));
                }
            }
        }
    }

    private static void house(Clip c, int x0, int z0, int x1, int z1, int by, int wallH,
                              Direction door, boolean withChest, boolean hall, Palette pal, long seed) {
        if (x1 + 1 < c.minX || x0 - 1 > c.maxX || z1 + 1 < c.minZ || z0 - 1 > c.maxZ) return;
        int salt = (int) (seed ^ (seed >>> 32));
        int cxm = (x0 + x1) / 2, czm = (z0 + z1) / 2;
        pad(c, x0, z0, x1, z1, by, true);

        int ax = Math.max(x0, c.minX), bx = Math.min(x1, c.maxX);
        int az = Math.max(z0, c.minZ), bz = Math.min(z1, c.maxZ);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                boolean edge = x == x0 || x == x1 || z == z0 || z == z1;
                if (edge) {
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    boolean isDoor = (door == Direction.EAST && x == x1 && z == czm)
                            || (door == Direction.WEST && x == x0 && z == czm)
                            || (door == Direction.SOUTH && z == z1 && x == cxm)
                            || (door == Direction.NORTH && z == z0 && x == cxm);
                    int top = wallH + (corner ? 1 : 0);
                    for (int h = 1; h <= top; h++) {
                        if (isDoor && h <= 2) continue;
                        double decay = decayProb(h) * (corner ? 0.5 : 1.0);
                        if (h01(x, by + h, z, salt) < decay) continue;
                        c.set(x, by + h, z, wallMix(pal, h01(x, by + h, z, salt + 1)));
                    }
                } else if (h01(x, by + wallH, z, salt + 9) < 0.10) {
                    c.set(x, by + wallH, z, cobweb());
                } else if (h01(x, by + wallH + 1, z, salt + 15) < 0.45) {
                    c.set(x, by + wallH + 1, z, wallMix(pal, h01(x, by + wallH + 1, z, salt + 16))); // остатки крыши
                }
            }
        }

        if (hall) {
            int[][] pil = { {x0 + 2, z0 + 2}, {x1 - 2, z0 + 2}, {x0 + 2, z1 - 2}, {x1 - 2, z1 - 2} };
            for (int[] p : pil) {
                for (int h = 1; h < wallH; h++) {
                    if (h01(p[0], by + h, p[1], salt + 4) >= decayProb(h) * 0.6) {
                        c.set(p[0], by + h, p[1], plainBrick());
                    }
                }
            }
        }
        if (withChest) {
            c.chest(x0 + 1, by + 1, z0 + 1, Direction.SOUTH, LOOT_RUINS, seed);
            if (hall) c.chest(x1 - 1, by + 1, z1 - 1, Direction.NORTH, LOOT_RUINS, seed + 1);
        }
    }

    private static void path(Clip c, int ox, int oz, int tx, int tz, List<int[]> rects, int salt) {
        int x = ox;
        while (true) {
            pathBlock(c, x, oz, ox, oz, rects, salt);
            if (x == tx) break;
            x += Integer.signum(tx - x);
        }
        int z = oz;
        while (true) {
            pathBlock(c, tx, z, ox, oz, rects, salt);
            if (z == tz) break;
            z += Integer.signum(tz - z);
        }
    }

    private static void pathBlock(Clip c, int x, int z, int ox, int oz, List<int[]> rects, int salt) {
        if (!c.in(x, z)) return;
        if (Math.hypot(x - ox, z - oz) <= 5.7) return;
        for (int[] rc : rects) {
            if (x >= rc[0] && x <= rc[2] && z >= rc[1] && z <= rc[3]) return;
        }
        if (h01(x, 0, z, salt + 7) < 0.22) return;
        AbyssTerrain.Sample t = AbyssTerrain.sample(x, z);
        if (t.water() || t.lava()) return;
        BlockState mix = h01(x, 1, z, salt + 8) < 0.55 ? Blocks.COBBLESTONE.defaultBlockState()
                : (h01(x, 1, z, salt + 8) < 0.8 ? Blocks.MOSSY_COBBLESTONE.defaultBlockState() : Blocks.GRAVEL.defaultBlockState());
        c.set(x, t.floorTop(), z, mix);
    }

    // ================= 2. Цитадель с подземельем =================
    private static void citadel(Clip c, Site s) {
        Random r = new Random(s.seed());
        int ox = s.ox(), oz = s.oz(), by = s.baseY();
        int salt = (int) (s.seed() ^ (s.seed() >>> 32));
        int mx0 = ox - 18, mz0 = oz - 18;

        Maze m = Maze.generate(9, 4, r);
        m.addGap(0, 0, 17, 19);
        m.addGap(36, 36, 17, 19);
        m.addGap(17, 19, 0, 0);
        m.addGap(17, 19, 36, 36);

        List<Integer> cells = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                boolean corner = (i == 0 || i == 8) && (j == 0 || j == 8);
                if (!corner && !(i == 4 && j == 4)) cells.add(i * 9 + j);
            }
        }
        Collections.shuffle(cells, r);
        EntityType<?>[] cellMob = new EntityType<?>[4];
        for (int k = 0; k < 4; k++) cellMob[k] = MOBS[r.nextInt(MOBS.length)];

        int[] dirX = { -1, 1, 0, 0 };
        int[] dirZ = { 0, 0, -1, 1 };
        int[] len1 = new int[4], len2 = new int[4], side = new int[4];
        boolean[] roomSpawner = new boolean[4];
        EntityType<?>[] roomMob = new EntityType<?>[4];
        for (int k = 0; k < 4; k++) {
            len1[k] = 10 + r.nextInt(7);
            len2[k] = 10 + r.nextInt(9);
            side[k] = r.nextBoolean() ? 1 : -1;
            roomSpawner[k] = r.nextBoolean();
            roomMob[k] = MOBS[r.nextInt(MOBS.length)];
        }

        citadelSurface(c, ox, oz, by, salt);

        int size = 9 * 4 + 1;
        int ax = Math.max(mx0, c.minX), bx = Math.min(mx0 + size - 1, c.maxX);
        int az = Math.max(mz0, c.minZ), bz = Math.min(mz0 + size - 1, c.maxZ);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int u = x - mx0, v = z - mz0;
                c.set(x, UG_FLOOR, z, brick(h01(x, UG_FLOOR, z, salt + 5)));
                c.set(x, UG_FLOOR + 4, z, brick(h01(x, UG_FLOOR + 4, z, salt + 6)));
                boolean wall = m.wall(u, v);
                for (int h = 1; h <= 3; h++) {
                    c.set(x, UG_FLOOR + h, z, wall ? brick(h01(x, UG_FLOOR + h, z, salt + 7 + h)) : air());
                }
                if (!wall && h01(x, UG_FLOOR + 3, z, salt + 20) < 0.04) c.set(x, UG_FLOOR + 3, z, cobweb());
            }
        }

        int[][] corners = { {0, 0}, {8, 0}, {0, 8}, {8, 8} };
        for (int k = 0; k < 4; k++) {
            int wx = mx0 + 2 + 4 * corners[k][0], wz = mz0 + 2 + 4 * corners[k][1];
            c.chest(wx, UG_FLOOR + 1, wz, Direction.SOUTH, LOOT_DUNGEON, (long) salt + k);
            c.set(wx, UG_FLOOR + 4, wz, lightStone());
        }
        for (int k = 0; k < 4; k++) {
            int cell = cells.get(k);
            int wx = mx0 + 2 + 4 * (cell / 9), wz = mz0 + 2 + 4 * (cell % 9);
            c.spawner(wx, UG_FLOOR + 1, wz, cellMob[k]);
        }

        for (int k = 0; k < 4; k++) {
            int px = Math.abs(dirZ[k]) * side[k], pz = Math.abs(dirX[k]) * side[k];
            int sx0 = ox + dirX[k] * 19, sz0 = oz + dirZ[k] * 19;
            int ex1 = sx0 + dirX[k] * len1[k], ez1 = sz0 + dirZ[k] * len1[k];
            int ex2 = ex1 + px * len2[k], ez2 = ez1 + pz * len2[k];

            c.fill(ex2 - 5, UG_FLOOR, ez2 - 5, ex2 + 5, UG_FLOOR + 5, ez2 + 5, plainBrick());
            c.fill(ex2 - 4, UG_FLOOR + 1, ez2 - 4, ex2 + 4, UG_FLOOR + 4, ez2 + 4, air());
            carveLine(c, sx0, sz0, ex1, ez1);
            carveLine(c, ex1, ez1, ex2, ez2);
            c.chest(ex2 - 3, UG_FLOOR + 1, ez2 - 3, Direction.SOUTH, LOOT_DUNGEON, (long) salt + 10 + k);
            c.chest(ex2 + 3, UG_FLOOR + 1, ez2 - 3, Direction.SOUTH, LOOT_DUNGEON, (long) salt + 20 + k);
            if (roomSpawner[k]) c.spawner(ex2, UG_FLOOR + 1, ez2, roomMob[k]);
        }

        int sx = ox - 1, sz = oz;
        if (sx + 1 >= c.minX && sx - 1 <= c.maxX && sz + 1 >= c.minZ && sz - 1 <= c.maxZ) {
            for (int y = UG_FLOOR + 5; y < by; y++) {
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        if (dx != 0 || dz != 0) c.set(sx + dx, y, sz + dz, plainBrick());
            }
            c.set(sx, UG_FLOOR + 4, sz, air());
            for (int y = UG_FLOOR + 1; y <= UG_FLOOR + 3; y++) c.set(sx - 1, y, sz, plainBrick());
            BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST);
            for (int y = UG_FLOOR + 1; y <= by; y++) c.set(sx, y, sz, ladder);
        }
    }

    private static void carveLine(Clip c, int x0, int z0, int x1, int z1) {
        int x = x0, z = z0;
        while (true) {
            c.fill(x - 1, UG_FLOOR + 1, z - 1, x + 1, UG_FLOOR + 3, z + 1, air());
            if (x == x1 && z == z1) break;
            if (x != x1) x += Integer.signum(x1 - x);
            else z += Integer.signum(z1 - z);
        }
    }

    private static void citadelSurface(Clip c, int ox, int oz, int by, int salt) {
        pad(c, ox - 20, oz - 20, ox + 20, oz + 20, by, true);
        int ax = Math.max(ox - 20, c.minX), bx = Math.min(ox + 20, c.maxX);
        int az = Math.max(oz - 20, c.minZ), bz = Math.min(oz + 20, c.maxZ);
        for (int x = ax; x <= bx; x++) {
            for (int z = az; z <= bz; z++) {
                int adx = Math.abs(x - ox), adz = Math.abs(z - oz);
                int hgt = 0;
                if (adx >= 16 && adz >= 16) {
                    int tdx = Math.abs(adx - 18), tdz = Math.abs(adz - 18);
                    if (Math.max(tdx, tdz) == 2) hgt = 11;
                } else if ((adx == 18 && adz <= 18) || (adz == 18 && adx <= 18)) {
                    hgt = 7;
                } else if (adx <= 6 && adz <= 6 && (adx == 6 || adz == 6)) {
                    hgt = 9;
                }

                if (adx == 20 && adz == 20) c.set(x, by + 12, z, lightStone());

                if (hgt > 0) {
                    boolean gate = (z - oz == 18 && adx <= 1);
                    boolean keepDoor = (z - oz == 6 && adx <= 1);
                    for (int h = 1; h <= hgt; h++) {
                        if (gate && h <= 4) continue;
                        if (keepDoor && h <= 3) continue;
                        if (h01(x, by + h, z, salt) < decayProb(Math.min(h, 5)) * 0.6) continue;
                        c.set(x, by + h, z, brick(h01(x, by + h, z, salt + 1)));
                    }
                } else if (adx < 18 && adz < 18 && !(adx <= 7 && adz <= 7)
                        && h01(x, by + 1, z, salt + 2) < 0.02) {
                    c.set(x, by + 1, z, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
                }
            }
        }
        c.chest(ox - 4, by + 1, oz - 4, Direction.SOUTH, LOOT_DUNGEON, (long) salt);
        c.chest(ox + 4, by + 1, oz - 4, Direction.SOUTH, LOOT_DUNGEON, (long) salt + 1);
    }

// ============================================================
// 3. БАШНЯ ДРЕВНИХ V3
// ============================================================

    private static final int ROOM_TRAP = 0, ROOM_MOBS = 1, ROOM_TREASURE = 2;
    private static final int FINAL_ROOM_RADIUS = 17;

    private static void tower(Clip c, Site s) {
        Random r = new Random(s.seed());
        int ox = s.ox(), oz = s.oz(), by = s.baseY();
        int salt = (int) (s.seed() ^ (s.seed() >>> 32));

        // самая низкая точка потолка над всей площадью башни
        int ceiling = Integer.MAX_VALUE;
        int rr = TOWER_RADIUS + 2;
        int[][] offs = { {0, 0}, {rr, 0}, {-rr, 0}, {0, rr}, {0, -rr}, {rr, rr}, {-rr, rr}, {rr, -rr}, {-rr, -rr} };
        for (int[] o : offs) ceiling = Math.min(ceiling, AbyssTerrain.ceilingBottom(ox + o[0], oz + o[1]));

        // сколько верхних этажей помещается до потолка
        int available = ceiling - by - TOWER_CLEARANCE;
        int upperFloors = Math.max(1, Math.min(TOWER_MAX_UPPER_FLOORS, available / TOWER_FLOOR_HEIGHT - 1));
        int height = (upperFloors + 1) * TOWER_FLOOR_HEIGHT; // крыша = потолок последнего этажа

        pad(c, ox - TOWER_RADIUS - 3, oz - TOWER_RADIUS - 3, ox + TOWER_RADIUS + 3, oz + TOWER_RADIUS + 3, by, true);
        buildTowerShell(c, ox, oz, by, height, salt);

        // Этажи с тремя комнатами: от -2 до верхнего
        for (int level = 1 - TOWER_BASEMENT_LEVELS; level <= upperFloors; level++) {
            buildTowerFloor(c, ox, oz, by + level * TOWER_FLOOR_HEIGHT, level, r, salt + 1000 + (level + 10) * 137);
        }
        // Последний подземный этаж: большой зал
        buildTowerFinalRoom(c, ox, oz, by - TOWER_BASEMENT_LEVELS * TOWER_FLOOR_HEIGHT, r, salt + 9000);

        // Лестницы между всеми этажами
        for (int level = -TOWER_BASEMENT_LEVELS; level < upperFloors; level++) {
            int lowerY = by + level * TOWER_FLOOR_HEIGHT;
            buildTowerStairs(c, ox, oz, lowerY, lowerY + TOWER_FLOOR_HEIGHT, level);
        }
        c.set(ox, by + height + 1, oz, lightStone());
    }
    private static void buildTowerShell(Clip c, int ox, int oz, int by, int height, int salt) {
        int radius = TOWER_RADIUS;
        for (int x = ox - radius; x <= ox + radius; x++) {
            for (int z = oz - radius; z <= oz + radius; z++) {
                double distance = Math.hypot(x - ox, z - oz);
                if (distance > radius + 0.5D) continue;
                boolean wall = distance >= radius - 1.5D;
                for (int y = by + 1; y <= by + height; y++) {
                    int localY = y - by;
                    if (wall) {
                        boolean entrance = z >= oz + radius - 1 && Math.abs(x - ox) <= 2 && localY <= 5;
                        if (entrance) { c.set(x, y, z, air()); continue; }
                        int floorIndex = localY / TOWER_FLOOR_HEIGHT;
                        int floorLocal = localY % TOWER_FLOOR_HEIGHT;
                        boolean windowHeight = floorIndex > 0 && (floorLocal == 4 || floorLocal == 5);
                        boolean southWindow = z > oz + 8 && Math.abs(x - ox) <= 1; // окна только на южной стороне
                        if (windowHeight && southWindow) { c.set(x, y, z, air()); continue; }
                        c.set(x, y, z, brick(h01(x, y, z, salt + 10)));
                    } else c.set(x, y, z, air());
                }
            }
        }
        int roofY = by + height;
        for (int x = ox - radius; x <= ox + radius; x++)
            for (int z = oz - radius; z <= oz + radius; z++)
                if (Math.hypot(x - ox, z - oz) <= radius)
                    c.set(x, roofY, z, brick(h01(x, roofY, z, salt + 20)));
    }

    /** Этаж: плита, холл и три комнаты за дубовыми дверями. */
    private static void buildTowerFloor(Clip c, int ox, int oz, int floorY, int level, Random r, int salt) {
        boolean basement = level < 0;
        for (int x = ox - 15; x <= ox + 15; x++) {
            for (int z = oz - 15; z <= oz + 15; z++) {
                double d = Math.hypot(x - ox, z - oz);
                if (d < 12.5D) {
                    c.set(x, floorY, z, floorMix(palette(0), h01(x, floorY, z, salt)));
                    if (basement)
                        for (int y = floorY + 1; y <= floorY + 7; y++) c.set(x, y, z, air());
                } else if (basement && d <= 14.5D) {
                    for (int y = floorY; y <= floorY + 8; y++)
                        c.set(x, y, z, brick(h01(x, y, z, salt + 1)));
                }
            }
        }

        // три комнаты: ловушка / мобы / сундуки в случайном порядке
        List<Integer> kinds = new ArrayList<>();
        kinds.add(ROOM_TRAP); kinds.add(ROOM_MOBS); kinds.add(ROOM_TREASURE);
        Collections.shuffle(kinds, r);
        buildTowerRoom(c, ox - 9, oz, floorY, Direction.EAST,  kinds.get(0), r, salt + 100);
        buildTowerRoom(c, ox + 9, oz, floorY, Direction.WEST,  kinds.get(1), r, salt + 200);
        buildTowerRoom(c, ox, oz - 9, floorY, Direction.SOUTH, kinds.get(2), r, salt + 300);

        // освещение холла
        c.set(ox, floorY + 7, oz, lightStone());
        c.set(ox, floorY + 7, oz + 9, lightStone());
    }

    private static void buildTowerRoom(Clip c, int cx, int cz, int floorY, Direction doorSide,
                                       int kind, Random r, int salt) {
        int rad = 4;
        for (int x = cx - rad; x <= cx + rad; x++) {
            for (int z = cz - rad; z <= cz + rad; z++) {
                boolean wall = x == cx - rad || x == cx + rad || z == cz - rad || z == cz + rad;
                c.set(x, floorY, z, floorMix(palette(0), h01(x, floorY, z, salt)));
                for (int y = floorY + 1; y <= floorY + 7; y++)
                    c.set(x, y, z, wall ? brick(h01(x, y, z, salt + 1)) : air());
                c.set(x, floorY + 8, z, brick(h01(x, floorY + 8, z, salt + 2)));
            }
        }

        // дверь в сторону холла
        int doorX = cx, doorZ = cz;
        switch (doorSide) {
            case EAST -> doorX = cx + rad;
            case WEST -> doorX = cx - rad;
            case SOUTH -> doorZ = cz + rad;
            default -> doorZ = cz - rad;
        }
        c.set(doorX, floorY + 1, doorZ, air());
        c.set(doorX, floorY + 2, doorZ, air());
        c.door(doorX, floorY + 1, doorZ, doorSide);

        switch (kind) {
            case ROOM_TRAP -> {
                for (int x = cx - 3; x <= cx + 3; x++)
                    for (int z = cz - 3; z <= cz + 3; z++)
                        if (h01(x, floorY, z, salt + 50) < 0.30D)
                            c.set(x, floorY, z, Blocks.MAGMA_BLOCK.defaultBlockState());
                int[][] plates = { {-2, -2}, {2, -2}, {-2, 2}, {2, 2} };
                for (int[] p : plates) {
                    c.set(cx + p[0], floorY, cz + p[1], Blocks.TNT.defaultBlockState());
                    c.set(cx + p[0], floorY + 1, cz + p[1], Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
                }
                for (int x = cx - 3; x <= cx + 3; x++)
                    for (int z = cz - 3; z <= cz + 3; z++)
                        if (h01(x, floorY + 2, z, salt + 55) < 0.08D)
                            c.set(x, floorY + 2, z, cobweb());
                c.chest(cx, floorY + 1, cz, Direction.SOUTH, LOOT_DUNGEON, salt + 51L); // приманка
                c.set(cx, floorY + 7, cz, lightStone());
            }
            case ROOM_MOBS -> {
                // свет не ставим: спавнерам нужна темнота
                c.spawner(cx - 2, floorY + 1, cz - 2, MOBS[r.nextInt(MOBS.length)]);
                c.spawner(cx + 2, floorY + 1, cz + 2, MOBS[r.nextInt(MOBS.length)]);
                for (int x = cx - 3; x <= cx + 3; x++)
                    for (int z = cz - 3; z <= cz + 3; z++)
                        if (h01(x, floorY + 3, z, salt + 60) < 0.10D)
                            c.set(x, floorY + 3, z, cobweb());
            }
            default -> {
                c.chest(cx - 3, floorY + 1, cz - 3, Direction.SOUTH, LOOT_DUNGEON, salt + 70L);
                c.chest(cx + 3, floorY + 1, cz - 3, Direction.SOUTH, LOOT_DUNGEON, salt + 71L);
                c.chest(cx + 3, floorY + 1, cz + 3, Direction.NORTH, LOOT_DUNGEON, salt + 72L);
                c.set(cx, floorY + 7, cz, lightStone());
            }
        }
    }

    /** Лестница между плитами lowerY и upperY (шаг по этажам, стороны чередуются). */
    private static void buildTowerStairs(Clip c, int ox, int oz, int lowerY, int upperY, int transition) {
        int x0 = Math.floorMod(transition, 2) == 0 ? ox + 3 : ox - 4;
        int z0 = oz + 10;
        BlockState stair = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
        int h = upperY - lowerY;
        for (int i = 0; i < h; i++) {
            int z = z0 - i, y = lowerY + 1 + i;
            for (int dx = 0; dx <= 1; dx++) {
                int x = x0 + dx;
                for (int yy = lowerY + 1; yy < y; yy++) c.set(x, yy, z, plainBrick()); // опора под ступенью
                c.set(x, y, z, stair);
                for (int k = 1; k <= 3; k++) c.set(x, y + k, z, air());               // проём в плите сверху
            }
        }
    }

    /** Последний подземный этаж: один большой зал с мобами и сундуками. */
    private static void buildTowerFinalRoom(Clip c, int ox, int oz, int floorY, Random r, int salt) {
        double inner = FINAL_ROOM_RADIUS - 0.5D, outer = FINAL_ROOM_RADIUS + 1.5D;
        for (int x = ox - 19; x <= ox + 19; x++) {
            for (int z = oz - 19; z <= oz + 19; z++) {
                double d = Math.hypot(x - ox, z - oz);
                if (d < inner) {
                    c.set(x, floorY, z, floorMix(palette(0), h01(x, floorY, z, salt)));
                    for (int y = floorY + 1; y <= floorY + 7; y++) c.set(x, y, z, air());
                    if (d >= 12.5D) c.set(x, floorY + 8, z, brick(h01(x, floorY + 8, z, salt + 1)));
                } else if (d <= outer) {
                    for (int y = floorY; y <= floorY + 8; y++)
                        c.set(x, y, z, brick(h01(x, y, z, salt + 2)));
                }
            }
        }

        int[][] pillars = { {-10, -10}, {10, -10}, {-10, 10}, {10, 10}, {-14, 0}, {14, 0} };
        for (int[] p : pillars)
            for (int y = floorY + 1; y <= floorY + 7; y++)
                c.set(ox + p[0], y, oz + p[1], plainBrick());

        c.spawner(ox - 8, floorY + 1, oz - 8, EntityType.ZOMBIE);
        c.spawner(ox + 8, floorY + 1, oz - 8, EntityType.SKELETON);
        c.spawner(ox - 8, floorY + 1, oz + 8, EntityType.SPIDER);
        c.spawner(ox + 8, floorY + 1, oz + 8, MOBS[r.nextInt(MOBS.length)]);
        c.spawner(ox, floorY + 1, oz - 12, MOBS[r.nextInt(MOBS.length)]);

        // центральный постамент с главными сундуками
        c.fill(ox - 3, floorY, oz - 3, ox + 3, floorY + 1, oz + 3, Blocks.POLISHED_DEEPSLATE.defaultBlockState());
        c.fill(ox - 2, floorY + 2, oz - 2, ox + 2, floorY + 2, oz + 2, Blocks.DEEPSLATE_TILES.defaultBlockState());
        c.chest(ox - 2, floorY + 3, oz, Direction.EAST, LOOT_DUNGEON, salt + 500L);
        c.chest(ox + 2, floorY + 3, oz, Direction.WEST, LOOT_DUNGEON, salt + 501L);
        c.chest(ox, floorY + 3, oz - 2, Direction.SOUTH, LOOT_DUNGEON, salt + 502L);

        // сундуки по краям зала
        c.chest(ox - 14, floorY + 1, oz + 3, Direction.EAST, LOOT_DUNGEON, salt + 510L);
        c.chest(ox + 14, floorY + 1, oz + 3, Direction.WEST, LOOT_DUNGEON, salt + 511L);
        c.chest(ox, floorY + 1, oz - 15, Direction.SOUTH, LOOT_DUNGEON, salt + 512L);
        c.chest(ox, floorY + 1, oz + 15, Direction.NORTH, LOOT_DUNGEON, salt + 513L);

        // один источник света над постаментом, чтобы спавнеры по краям работали
        c.set(ox, floorY + 7, oz, lightStone());
        for (int x = ox - 11; x <= ox + 11; x += 4)
            if (h01(x, floorY, oz, salt + 600) < 0.5D) c.set(x, floorY + 1, oz - 11, cobweb());
    }

    // ================= Генератор лабиринта (подземелье цитадели) =================
    private static final class Maze {
        final int n, p;
        final boolean[][] east, south;
        final List<int[]> gaps = new ArrayList<>();

        Maze(int n, int p) {
            this.n = n;
            this.p = p;
            this.east = new boolean[n][n];
            this.south = new boolean[n][n];
        }

        static Maze generate(int n, int p, Random r) {
            Maze m = new Maze(n, p);
            boolean[][] vis = new boolean[n][n];
            int[] stack = new int[n * n];
            int sp = 0;
            stack[sp++] = 0;
            vis[0][0] = true;
            int[] dxs = { 1, -1, 0, 0 };
            int[] dzs = { 0, 0, 1, -1 };
            while (sp > 0) {
                int cur = stack[sp - 1];
                int ci = cur / n, cj = cur % n;
                int[] opts = new int[4];
                int oc = 0;
                for (int d = 0; d < 4; d++) {
                    int ni = ci + dxs[d], nj = cj + dzs[d];
                    if (ni >= 0 && nj >= 0 && ni < n && nj < n && !vis[ni][nj]) opts[oc++] = d;
                }
                if (oc == 0) { sp--; continue; }
                int d = opts[r.nextInt(oc)];
                int ni = ci + dxs[d], nj = cj + dzs[d];
                if (d == 0) m.east[ci][cj] = true;
                else if (d == 1) m.east[ni][nj] = true;
                else if (d == 2) m.south[ci][cj] = true;
                else m.south[ni][nj] = true;
                vis[ni][nj] = true;
                stack[sp++] = ni * n + nj;
            }
            return m;
        }

        void addGap(int u0, int u1, int v0, int v1) { gaps.add(new int[] { u0, u1, v0, v1 }); }

        boolean wall(int u, int v) {
            int size = n * p + 1;
            for (int[] g : gaps) {
                if (u >= g[0] && u <= g[1] && v >= g[2] && v <= g[3]) return false;
            }
            if (u <= 0 || v <= 0 || u >= size - 1 || v >= size - 1) return true;
            boolean uw = u % p == 0, vw = v % p == 0;
            if (uw && vw) return true;
            if (!uw && !vw) return false;
            if (uw) {
                int i = u / p - 1, j = v / p;
                return !east[i][j];
            }
            int i = u / p, j = v / p - 1;
            return !south[i][j];
        }
    }
}