package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.entity.AnkylosaurusEntity;
import com.akashi.abyss.abyss.entity.ModEntities;
import com.akashi.abyss.abyss.entity.ParasaurolophusEntity;
import com.akashi.abyss.abyss.entity.StegosaurusEntity;
import com.akashi.abyss.abyss.entity.TriceratopsEntity;
import com.akashi.abyss.abyss.entity.water.XiphactinusEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import java.util.Random;
import com.akashi.abyss.abyss.entity.MosasaurusEntity;
import net.minecraft.tags.FluidTags;
import com.akashi.abyss.abyss.entity.water.ElasmosaurusEntity;
import com.akashi.abyss.abyss.entity.water.ArchelonEntity;
import com.akashi.abyss.abyss.entity.water.TrilobiteEntity;
import com.akashi.abyss.abyss.entity.water.OrthocerasEntity;
import com.akashi.abyss.abyss.entity.heaven.QuetzalcoatlusEntity;

public final class AbyssFauna {

    private static final long FAUNA_SEED = 884211L;

    private static final double HERD_CHANCE = 0.05;
    private static final double PARASAUROLOPHUS_CHANCE = 0.05;
    private static final double ANKYLOSAURUS_CHANCE = 0.05;
    private static final double BRACHIOSAURUS_CHANCE = 0.005;
    private static final double TYRANNOSAURUS_CHANCE = 0.005;
    private static final double GIGANOTOSAURUS_CHANCE = 0.005;
    private static final double THERIZINOSAURUS_CHANCE = 0.005;
    private static final double MOSASAURUS_CHANCE = 0.005D;
    private static final double XIPHACTINUS_CHANCE = 0.05D;
    private static final double ELASMOSAURUS_CHANCE = 0.005D;
    private static final double ARCHELON_CHANCE = 0.08D;
    private static final double TRILOBITE_CHANCE = 0.08D;
    private static final double ORTHOCERAS_CHANCE = 0.12D;
    private static final double NAUTILUS_CHANCE = 1.0D;
    private static final double QUETZALCOATLUS_CHANCE = 0.01D;

    private AbyssFauna() {}

    public static void populate(ServerLevel level, ChunkPos pos) {
        Random r = new Random(
                FAUNA_SEED
                        ^ ((long) pos.x * 341873128712L)
                        ^ ((long) pos.z * 132897987541L)
        );

        // Мозазавр — редкий морской хищник первого биома
        if (r.nextDouble() < MOSASAURUS_CHANCE) {
            spawnMosasaurus(level, pos, r);
        }

        // Ксифактин — водный хищник первого биома
        if (r.nextDouble() < XIPHACTINUS_CHANCE) {
            spawnXiphactinus(level, pos, r);
        }

        // Эласмозавры — мирные водные рептилии первого биома
        if (r.nextDouble() < ELASMOSAURUS_CHANCE) {
            spawnElasmosaurus(level, pos, r);
        }

        // Архелоны — мирные морские черепахи первого биома
        if (r.nextDouble() < ARCHELON_CHANCE) {
            spawnArchelon(level, pos, r);
        }
        // Трилобиты — небольшие группы на дне водоёмов первого биома
        if (r.nextDouble() < TRILOBITE_CHANCE) {
            spawnTrilobites(level, pos, r);
        }

        // Мирные ортокерасы первого биома
        if (r.nextDouble() < ORTHOCERAS_CHANCE) {
            spawnOrthoceras(level, pos, r);
        }

        // Рыбы первого биома
        if (r.nextDouble() < 0.35D) {
            spawnAbyssFish(level, pos, r);
        }

        // Ванильные наутилусы первого биома (если доступны в версии Minecraft)
        if (r.nextDouble() < NAUTILUS_CHANCE) {
            spawnVanillaNautilus(level, pos, r);
        }

        // Анкилозавры — вулканический лес
        if (r.nextDouble() < ANKYLOSAURUS_CHANCE) {
            spawnAnkylosaurusHerd(level, pos, r);
        }

        // Паразауролофы — Белый ядовитый лес
        if (r.nextDouble() < PARASAUROLOPHUS_CHANCE) {
            spawnParasaurolophusHerd(level, pos, r);
        }
        // Тираннозавры — редко, по одному, в обычных лесах
        if (r.nextDouble() < TYRANNOSAURUS_CHANCE) {
            spawnTyrannosaurus(level, pos, r);
        }

        // Спинозавры — редко, возле водоёмов первого биома
        if (r.nextDouble() < 0.03D) {
            spawnSpinosaurus(level, pos, r);
        }

        // Гиганотозавр — редкий одиночный хищник Обугленного леса
        if (r.nextDouble() < GIGANOTOSAURUS_CHANCE) {
            spawnGiganotosaurus(level, pos, r);
        }

        // Теризинозавр — одиночный травоядный обычного леса
        if (r.nextDouble() < THERIZINOSAURUS_CHANCE) {
            spawnTherizinosaurus(level, pos, r);
        }

        // Кетцалькоатль — редкий обитатель первого биома
        if (r.nextDouble() < QUETZALCOATLUS_CHANCE) {
            spawnQuetzalcoatlus(level, pos, r);
        }

        // Трицератопсы и стегозавры — обычный лес
        if (r.nextDouble() >= HERD_CHANCE) return;

        // Брахиозавры — обычные леса первого биома
        if (r.nextDouble() < BRACHIOSAURUS_CHANCE) {
            spawnBrachiosaurusHerd(level, pos, r);
        }

        boolean steg = r.nextBoolean();

        EntityType<? extends Mob> type = steg
                ? ModEntities.STEGOSAURUS.get()
                : ModEntities.TRICERATOPS.get();

        int herd = 2 + r.nextInt(2);

        for (int i = 0; i < herd; i++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample t = AbyssTerrain.sample(x, z);
            if (t.water() || t.lava()) continue;

            BlockPos ground = new BlockPos(x, t.floorTop(), z);

            if (!level.getBlockState(ground).is(Abyss.ABYSS_GRASS.get())) {
                continue;
            }

            Mob dino = type.create(level, EntitySpawnReason.CHUNK_GENERATION);
            if (dino == null) continue;

            dino.snapTo(
                    x + 0.5,
                    t.floorTop() + 1,
                    z + 0.5,
                    r.nextFloat() * 360F,
                    0F
            );

            if (dino instanceof StegosaurusEntity s) {
                s.setVariant(r.nextInt(3));
            } else if (dino instanceof TriceratopsEntity tr) {
                tr.setVariant(r.nextInt(3));
            }

            dino.setPersistenceRequired();
            level.addFreshEntity(dino);
        }
    }

    private static void spawnParasaurolophusHerd(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        int herd = 2 + r.nextInt(3);

        for (int i = 0; i < herd; i++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            if (!AbyssTerrain.isPoisonZone(x, z)) continue;

            AbyssTerrain.Sample t = AbyssTerrain.sample(x, z);
            if (t.water() || t.lava()) continue;

            BlockPos ground = new BlockPos(x, t.floorTop(), z);

            if (!level.getBlockState(ground).is(
                    Abyss.ABYSS_GRASS_WET.get()
            )) {
                continue;
            }

            ParasaurolophusEntity dino =
                    ModEntities.PARASAUROLOPHUS.get()
                            .create(level, EntitySpawnReason.CHUNK_GENERATION);

            if (dino == null) continue;

            dino.snapTo(
                    x + 0.5,
                    t.floorTop() + 1,
                    z + 0.5,
                    r.nextFloat() * 360F,
                    0F
            );

            dino.setVariant(r.nextInt(4));
            dino.setPersistenceRequired();
            level.addFreshEntity(dino);
        }
    }

    private static void spawnAnkylosaurusHerd(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        int herd = 1 + r.nextInt(3);

        for (int i = 0; i < herd; i++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample t = AbyssTerrain.sample(x, z);

            if (t.water() || t.lava()) continue;

            BlockPos ground = new BlockPos(x, t.floorTop(), z);

            // Только обугленная земля вулканического леса
            if (!level.getBlockState(ground).is(
                    Abyss.ABYSS_CHARRED_GROUND.get()
            )) {
                continue;
            }

            AnkylosaurusEntity dino =
                    ModEntities.ANKYLOSAURUS.get()
                            .create(level, EntitySpawnReason.CHUNK_GENERATION);

            if (dino == null) continue;

            dino.snapTo(
                    x + 0.5,
                    t.floorTop() + 1,
                    z + 0.5,
                    r.nextFloat() * 360F,
                    0F
            );

            dino.setVariant(r.nextInt(3));
            dino.setPersistenceRequired();
            level.addFreshEntity(dino);

            Abyss.LOGGER.info(
                    ">>> Ankylosaurus spawned in Volcanic Forest at {} {}",
                    x, z
            );
        }
    }


    private static void spawnBrachiosaurusHerd(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        int herd = 1 + r.nextInt(2);

        for (int i = 0; i < herd; i++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample t = AbyssTerrain.sample(x, z);

            if (t.water() || t.lava()) continue;

            // Исключаем ядовитый лес
            if (AbyssTerrain.isPoisonZone(x, z)) continue;

            BlockPos ground = new BlockPos(x, t.floorTop(), z);

            // Только обычная трава первого биома
            if (!level.getBlockState(ground)
                    .is(Abyss.ABYSS_GRASS.get())) {
                continue;
            }

            // Проверяем свободное пространство для гиганта
            boolean spaceAvailable = true;

            for (int dy = 1; dy <= 12; dy++) {
                if (!level.getBlockState(ground.above(dy))
                        .isAir()) {
                    spaceAvailable = false;
                    break;
                }
            }

            if (!spaceAvailable) continue;

            var dino = ModEntities.BRACHIOSAURUS.get()
                    .create(level, EntitySpawnReason.CHUNK_GENERATION);

            if (dino == null) continue;

            dino.snapTo(
                    x + 0.5,
                    t.floorTop() + 1,
                    z + 0.5,
                    r.nextFloat() * 360F,
                    0F
            );

            // При прямом создании сущности finalizeSpawn
            // не вызывается автоматически.
            dino.setVariant(r.nextInt(3));

            dino.setPersistenceRequired();
            level.addFreshEntity(dino);

            Abyss.LOGGER.info(
                    ">>> Brachiosaurus spawned in Green Forest at {} {}",
                    x, z
            );
        }
    }
    private static void spawnTyrannosaurus(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Несколько попыток найти подходящее место
        for (int attempt = 0; attempt < 5; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample t = AbyssTerrain.sample(x, z);

            // Не появляется в воде или лаве
            if (t.water() || t.lava()) continue;

            // Исключаем ядовитый лес
            if (AbyssTerrain.isPoisonZone(x, z)) continue;

            BlockPos ground = new BlockPos(x, t.floorTop(), z);

            // Только обычная трава первого биома.
            // Это также исключает вулканическую землю.
            if (!level.getBlockState(ground)
                    .is(Abyss.ABYSS_GRASS.get())) {
                continue;
            }

            var dino = ModEntities.TYRANNOSAURUS.get()
                    .create(level, EntitySpawnReason.CHUNK_GENERATION);

            if (dino == null) return;

            dino.snapTo(
                    x + 0.5,
                    t.floorTop() + 1,
                    z + 0.5,
                    r.nextFloat() * 360F,
                    0F
            );

            // Проверяем, что хитбокс тираннозавра
            // не пересекается с деревьями и другими блоками
            if (!level.noCollision(dino)) {
                dino.discard();
                continue;
            }

            // Одна из трёх текстур
            dino.setVariant(r.nextInt(3));

            // Не исчезает при удалении игрока
            dino.setPersistenceRequired();

            level.addFreshEntity(dino);

            Abyss.LOGGER.info(
                    ">>> Tyrannosaurus spawned in Normal Forest at {} {}",
                    x, z
            );

            // Только один тираннозавр на успешную попытку
            return;
        }
    }
    private static void spawnSpinosaurus(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Несколько попыток найти берег
        for (int attempt = 0; attempt < 12; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

            // Только суша, без лавы
            if (terrain.water() || terrain.lava()) continue;

            // Не появляется в ядовитом лесу
            if (AbyssTerrain.isPoisonZone(x, z)) continue;

            BlockPos ground = new BlockPos(x, terrain.floorTop(), z);

            // Только обычная трава первого биома
            if (!level.getBlockState(ground)
                    .is(Abyss.ABYSS_GRASS.get())) {
                continue;
            }

            // Ищем воду в радиусе 12 блоков.
            // Проверяем именно рельеф первого биома.
            boolean nearWater = false;

            for (int dx = -12; dx <= 12 && !nearWater; dx += 3) {
                for (int dz = -12; dz <= 12; dz += 3) {

                    if (dx * dx + dz * dz > 144) continue;

                    AbyssTerrain.Sample nearby =
                            AbyssTerrain.sample(x + dx, z + dz);

                    if (nearby.water()) {
                        nearWater = true;
                        break;
                    }
                }
            }

            if (!nearWater) continue;

            var dino = ModEntities.SPINOSAURUS.get()
                    .create(level, EntitySpawnReason.CHUNK_GENERATION);

            if (dino == null) return;

            dino.snapTo(
                    x + 0.5,
                    terrain.floorTop() + 1,
                    z + 0.5,
                    r.nextFloat() * 360F,
                    0F
            );

            // Проверяем столкновения с блоками и деревьями
            if (!level.noCollision(dino)) {
                dino.discard();
                continue;
            }

            // Три варианта окраски
            dino.setVariant(r.nextInt(3));

            // Не исчезает при удалении игрока
            dino.setPersistenceRequired();

            level.addFreshEntity(dino);

            Abyss.LOGGER.info(
                    ">>> Spinosaurus spawned near water at {} {}",
                    x, z
            );

            // Только один спинозавр
            return;
        }
    }
    /**
     * Редкий гиганотозавр на обугленной земле первого биома.
     * Не появляется в воде, лаве и других типах леса.
     */
    private static void spawnGiganotosaurus(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);
            if (terrain.water() || terrain.lava()) continue;
            if (AbyssTerrain.isPoisonZone(x, z)) continue;

            BlockPos ground = new BlockPos(x, terrain.floorTop(), z);

            // Только Обугленный лес: поверхность из обугленной земли.
            if (!level.getBlockState(ground).is(Abyss.ABYSS_CHARRED_GROUND.get())) {
                continue;
            }

            var dino = ModEntities.GIGANOTOSAURUS.get()
                    .create(level, EntitySpawnReason.CHUNK_GENERATION);
            if (dino == null) return;

            dino.snapTo(
                    x + 0.5,
                    terrain.floorTop() + 1,
                    z + 0.5,
                    r.nextFloat() * 360F,
                    0F
            );

            // Хитбокс гиганотозавра не должен пересекаться с деревьями.
            if (!level.noCollision(dino)) {
                dino.discard();
                continue;
            }

            dino.setVariant(r.nextInt(3));
            dino.setPersistenceRequired();
            level.addFreshEntity(dino);

            Abyss.LOGGER.info(
                    ">>> Giganotosaurus spawned in Charred Forest at {} {}",
                    x, z
            );
            return;
        }
    }

    private static void spawnTherizinosaurus(ServerLevel level, ChunkPos pos, Random r) {
        for (int attempt = 0; attempt < 5; attempt++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);
            AbyssTerrain.Sample t = AbyssTerrain.sample(x, z);
            if (t.water() || t.lava() || AbyssTerrain.isPoisonZone(x, z)) continue;
            BlockPos ground = new BlockPos(x, t.floorTop(), z);
            if (!level.getBlockState(ground).is(Abyss.ABYSS_GRASS.get())) continue;
            var dino = ModEntities.THERIZINOSAURUS.get().create(level, EntitySpawnReason.CHUNK_GENERATION);
            if (dino == null) return;
            dino.snapTo(x + 0.5D, t.floorTop() + 1, z + 0.5D, r.nextFloat() * 360F, 0F);
            if (!level.noCollision(dino)) { dino.discard(); continue; }
            dino.setVariant(r.nextInt(3));
            dino.setPersistenceRequired();
            level.addFreshEntity(dino);
            return;
        }
    }

    private static void spawnAbyssFish(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        int x = pos.getMinBlockX() + 2 + r.nextInt(12);
        int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

        AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

        if (!terrain.water()) return;

        int depth = AbyssTerrain.SEA_LEVEL - terrain.floorTop();

        if (depth < 4) return;

        EntityType<?> fishType;

        int choice = r.nextInt(3);

        if (choice == 0) {
            fishType = EntityType.COD;
        } else if (choice == 1) {
            fishType = EntityType.SALMON;
        } else {
            fishType = EntityType.TROPICAL_FISH;
        }

        int count = 2 + r.nextInt(4);

        for (int i = 0; i < count; i++) {
            int fishY = terrain.floorTop()
                    + 2 + r.nextInt(Math.max(1, depth - 3));

            BlockPos fishPos = new BlockPos(x, fishY, z);

            if (!level.getFluidState(fishPos)
                    .is(net.minecraft.tags.FluidTags.WATER)) {
                continue;
            }

            var fish = fishType.create(
                    level,
                    EntitySpawnReason.CHUNK_GENERATION
            );

            if (fish == null) continue;

            fish.snapTo(
                    x + 0.5 + r.nextDouble() * 2 - 1,
                    fishY + 0.2,
                    z + 0.5 + r.nextDouble() * 2 - 1,
                    r.nextFloat() * 360F,
                    0F
            );

            if (!level.noCollision(fish)) {
                fish.discard();
                continue;
            }

            level.addFreshEntity(fish);
        }
    }

    private static void spawnXiphactinus(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Несколько попыток найти подходящий водоём
        for (int attempt = 0; attempt < 12; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

            // Только вода первого биома
            if (!terrain.water() || terrain.lava()) continue;

            int waterDepth =
                    AbyssTerrain.SEA_LEVEL - terrain.floorTop();

            // Минимальная глубина — 8 блоков
            if (waterDepth < 8) continue;

            // Появляется примерно посередине толщи воды
            int spawnY = terrain.floorTop() + waterDepth / 2;

            BlockPos spawnPos = new BlockPos(x, spawnY, z);

            // Не допускаем появления за пределами первого биома
            if (!AbyssBiome1ChunkHandler.isInsideBiome1(spawnY)) {
                continue;
            }

            // Проверяем настоящую воду
            if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) {
                continue;
            }

            XiphactinusEntity fish =
                    ModEntities.XIPHACTINUS.get().create(
                            level,
                            EntitySpawnReason.CHUNK_GENERATION
                    );

            if (fish == null) return;

            fish.snapTo(
                    x + 0.5D,
                    spawnY + 0.5D,
                    z + 0.5D,
                    r.nextFloat() * 360.0F,
                    0.0F
            );

            // Проверяем свободное место
            if (!level.noCollision(fish)) {
                fish.discard();
                continue;
            }

            // Одна из трёх окрасок
            fish.setVariant(r.nextInt(3));

            // Не исчезает при удалении игрока
            fish.setPersistenceRequired();

            level.addFreshEntity(fish);

            Abyss.LOGGER.info(
                    ">>> Xiphactinus spawned underwater at {} {} {}",
                    x, spawnY, z
            );

            return;
        }
    }

    private static void spawnMosasaurus(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Несколько попыток найти глубокую воду
        for (int attempt = 0; attempt < 12; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

            // Только водоёмы
            if (!terrain.water() || terrain.lava()) {
                continue;
            }

            int waterDepth =
                    AbyssTerrain.SEA_LEVEL - terrain.floorTop();

            // Только глубокие водоёмы
            if (waterDepth < 20) {
                continue;
            }

            // Проверяем, что вокруг достаточно воды
            if (!AbyssBiome1ChunkHandler.isLargeWaterArea(
                    x, z, 16
            )) {
                continue;
            }

            // Появляется в толще воды, не на самом дне
            int spawnY = terrain.floorTop()
                    + waterDepth / 2;

            BlockPos spawnPos = new BlockPos(x, spawnY, z);

            if (!AbyssBiome1ChunkHandler.isInsideBiome1(spawnY)) {
                continue;
            }

            if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) {
                continue;
            }

            MosasaurusEntity mosasaurus =
                    ModEntities.MOSASAURUS.get().create(
                            level,
                            EntitySpawnReason.CHUNK_GENERATION
                    );

            if (mosasaurus == null) {
                return;
            }

            mosasaurus.snapTo(
                    x + 0.5D,
                    spawnY + 0.5D,
                    z + 0.5D,
                    r.nextFloat() * 360.0F,
                    0.0F
            );

            // Проверяем, что хитбокс не пересекает блоки
            if (!level.noCollision(mosasaurus)) {
                mosasaurus.discard();
                continue;
            }

            // Только взрослый Мозазавр
            mosasaurus.setBaby(false);

            // Случайная окраска
            mosasaurus.setVariant(r.nextInt(3));

            // Не исчезает при удалении игрока
            mosasaurus.setPersistenceRequired();

            level.addFreshEntity(mosasaurus);

            Abyss.LOGGER.info(
                    ">>> Mosasaurus spawned underwater at {} {} {}",
                    x, spawnY, z
            );

            return;
        }
    }

    private static void spawnElasmosaurus(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Несколько попыток найти подходящий водоём
        for (int attempt = 0; attempt < 12; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

            // Только водоёмы первого биома
            if (!terrain.water() || terrain.lava()) continue;

            int waterDepth =
                    AbyssTerrain.SEA_LEVEL - terrain.floorTop();

            // Минимальная глубина воды
            if (waterDepth < 12) continue;

            // Эласмозавры появляются в достаточно больших водоёмах
            if (!AbyssBiome1ChunkHandler.isLargeWaterArea(x, z, 10)) {
                continue;
            }

            // Группа из 1–3 особей
            int groupSize = 1 + r.nextInt(3);

            for (int i = 0; i < groupSize; i++) {

                int spawnX = x + r.nextInt(7) - 3;
                int spawnZ = z + r.nextInt(7) - 3;

                AbyssTerrain.Sample nearby =
                        AbyssTerrain.sample(spawnX, spawnZ);

                if (!nearby.water() || nearby.lava()) continue;

                int nearbyDepth =
                        AbyssTerrain.SEA_LEVEL - nearby.floorTop();

                if (nearbyDepth < 12) continue;

                int spawnY = nearby.floorTop() + nearbyDepth / 2;

                BlockPos spawnPos =
                        new BlockPos(spawnX, spawnY, spawnZ);

                // Только первый биом
                if (!AbyssBiome1ChunkHandler.isInsideBiome1(spawnY)) {
                    continue;
                }

                // Проверяем наличие воды
                if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) {
                    continue;
                }

                ElasmosaurusEntity elasmosaurus =
                        ModEntities.ELASMOSAURUS.get().create(
                                level,
                                EntitySpawnReason.CHUNK_GENERATION
                        );

                if (elasmosaurus == null) continue;

                elasmosaurus.snapTo(
                        spawnX + 0.5D,
                        spawnY + 0.5D,
                        spawnZ + 0.5D,
                        r.nextFloat() * 360.0F,
                        0.0F
                );

                // Не допускаем появления внутри блоков
                if (!level.noCollision(elasmosaurus)) {
                    elasmosaurus.discard();
                    continue;
                }

                // Только взрослые особи
                elasmosaurus.setElasmosaurusBaby(false);

                // Одна из трёх окрасок
                elasmosaurus.setVariant(r.nextInt(3));

                // Сохраняем после удаления игрока
                elasmosaurus.setPersistenceRequired();

                level.addFreshEntity(elasmosaurus);

                Abyss.LOGGER.info(
                        ">>> Elasmosaurus spawned underwater at {} {} {}",
                        spawnX, spawnY, spawnZ
                );
            }

            return;
        }
    }
    private static void spawnTrilobites(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Ищем подходящий водоём первого биома
        for (int attempt = 0; attempt < 20; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

            if (!terrain.water() || terrain.lava()) {
                continue;
            }

            int waterDepth =
                    AbyssTerrain.SEA_LEVEL - terrain.floorTop();

            // Минимальная глубина воды
            if (waterDepth < 4) {
                continue;
            }

            // Группа из 2–5 трилобитов
            int groupSize = 2 + r.nextInt(4);
            int spawned = 0;

            for (int i = 0; i < groupSize; i++) {

                int spawnX = x + r.nextInt(7) - 3;
                int spawnZ = z + r.nextInt(7) - 3;

                AbyssTerrain.Sample nearby =
                        AbyssTerrain.sample(spawnX, spawnZ);

                if (!nearby.water() || nearby.lava()) {
                    continue;
                }

                int depth =
                        AbyssTerrain.SEA_LEVEL - nearby.floorTop();

                if (depth < 4) {
                    continue;
                }

                // Случайная высота внутри толщи воды:
                // от одного блока над дном до одного блока под поверхностью
                int spawnY = nearby.floorTop()
                        + 1 + r.nextInt(depth - 2);

                // Только первый биом
                if (!AbyssBiome1ChunkHandler.isInsideBiome1(spawnY)) {
                    continue;
                }

                BlockPos spawnPos =
                        new BlockPos(spawnX, spawnY, spawnZ);

                // Обязательно настоящая вода
                if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) {
                    continue;
                }

                TrilobiteEntity trilobite =
                        ModEntities.TRILOBITE.get().create(
                                level,
                                EntitySpawnReason.CHUNK_GENERATION
                        );

                if (trilobite == null) {
                    continue;
                }

                trilobite.snapTo(
                        spawnX + 0.5D,
                        spawnY + 0.5D,
                        spawnZ + 0.5D,
                        r.nextFloat() * 360.0F,
                        0.0F
                );

                // Проверка столкновений
                if (!level.noCollision(trilobite)) {
                    trilobite.discard();
                    continue;
                }

                // Три варианта окраски
                trilobite.setVariant(r.nextInt(3));

                // Сохраняем существо после удаления игрока
                trilobite.setPersistenceRequired();

                if (level.addFreshEntity(trilobite)) {
                    spawned++;

                    Abyss.LOGGER.info(
                            ">>> Trilobite spawned underwater at {} {} {}",
                            spawnX, spawnY, spawnZ
                    );
                }
            }

            // Завершаем поиск только после успешного появления
            if (spawned > 0) {
                Abyss.LOGGER.info(
                        ">>> Trilobite group spawned: {} entities",
                        spawned
                );
                return;
            }
        }
    }

    private static void spawnArchelon(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Ищем подходящий водоём
        for (int attempt = 0; attempt < 12; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

            if (!terrain.water() || terrain.lava()) {
                continue;
            }

            int waterDepth =
                    AbyssTerrain.SEA_LEVEL - terrain.floorTop();

            // Архелону достаточно 6 блоков глубины
            if (waterDepth < 6) {
                continue;
            }

            // Группа из 1–2 взрослых особей
            int groupSize = 1 + r.nextInt(2);

            for (int i = 0; i < groupSize; i++) {

                int spawnX = x + r.nextInt(5) - 2;
                int spawnZ = z + r.nextInt(5) - 2;

                AbyssTerrain.Sample nearby =
                        AbyssTerrain.sample(spawnX, spawnZ);

                if (!nearby.water() || nearby.lava()) {
                    continue;
                }

                int nearbyDepth =
                        AbyssTerrain.SEA_LEVEL - nearby.floorTop();

                if (nearbyDepth < 6) {
                    continue;
                }

                int spawnY = nearby.floorTop() + nearbyDepth / 2;

                if (!AbyssBiome1ChunkHandler.isInsideBiome1(spawnY)) {
                    continue;
                }

                BlockPos spawnPos =
                        new BlockPos(spawnX, spawnY, spawnZ);

                if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) {
                    continue;
                }

                ArchelonEntity archelon =
                        ModEntities.ARCHELON.get().create(
                                level,
                                EntitySpawnReason.CHUNK_GENERATION
                        );

                if (archelon == null) {
                    continue;
                }

                archelon.snapTo(
                        spawnX + 0.5D,
                        spawnY + 0.5D,
                        spawnZ + 0.5D,
                        r.nextFloat() * 360.0F,
                        0.0F
                );

                if (!level.noCollision(archelon)) {
                    archelon.discard();
                    continue;
                }

                archelon.setArchelonBaby(false);
                archelon.setVariant(r.nextInt(3));
                archelon.setPersistenceRequired();

                level.addFreshEntity(archelon);

                Abyss.LOGGER.info(
                        ">>> Archelon spawned underwater at {} {} {}",
                        spawnX, spawnY, spawnZ
                );
            }

            return;
        }
    }
    /**
     * Ванильные наутилусы в воде первого биома.
     * В Minecraft 1.21.10 тип отсутствует; в таком случае ничего не создаём.
     */
    private static void spawnVanillaNautilus(ServerLevel level, ChunkPos pos, Random r) {
        var typeOptional = BuiltInRegistries.ENTITY_TYPE.getOptional(
                ResourceLocation.parse("minecraft:nautilus")
        );
        if (typeOptional.isEmpty()) return;

        EntityType<?> nautilusType = typeOptional.get();

        for (int attempt = 0; attempt < 12; attempt++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);
            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);
            if (!terrain.water() || terrain.lava()) continue;

            int depth = AbyssTerrain.SEA_LEVEL - terrain.floorTop();
            if (depth < 5) continue;

            int spawned = 0;
            int count = 1 + r.nextInt(3);
            for (int i = 0; i < count; i++) {
                int spawnX = x + r.nextInt(5) - 2;
                int spawnZ = z + r.nextInt(5) - 2;
                AbyssTerrain.Sample nearby = AbyssTerrain.sample(spawnX, spawnZ);
                if (!nearby.water() || nearby.lava()) continue;

                int nearbyDepth = AbyssTerrain.SEA_LEVEL - nearby.floorTop();
                if (nearbyDepth < 5) continue;

                int spawnY = nearby.floorTop() + 2 + r.nextInt(nearbyDepth - 3);
                if (!AbyssBiome1ChunkHandler.isInsideBiome1(spawnY)) continue;

                BlockPos spawnPos = new BlockPos(spawnX, spawnY, spawnZ);
                if (!level.getFluidState(spawnPos).is(FluidTags.WATER)) continue;

                var nautilus = nautilusType.create(level, EntitySpawnReason.CHUNK_GENERATION);
                if (nautilus == null) continue;

                nautilus.snapTo(spawnX + 0.5D, spawnY + 0.5D, spawnZ + 0.5D,
                        r.nextFloat() * 360.0F, 0.0F);
                if (!level.noCollision(nautilus)) {
                    nautilus.discard();
                    continue;
                }

                if (nautilus instanceof Mob mob) mob.setPersistenceRequired();
                if (level.addFreshEntity(nautilus)) {
                    spawned++;
                    Abyss.LOGGER.info(">>> Vanilla Nautilus spawned at {} {} {}",
                            spawnX, spawnY, spawnZ);
                }
            }
            if (spawned > 0) return;
        }
    }


    private static void spawnOrthoceras(ServerLevel level, ChunkPos pos, Random r) {
        for (int attempt = 0; attempt < 16; attempt++) {
            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);
            if (!terrain.water() || terrain.lava()) continue;

            int depth = AbyssTerrain.SEA_LEVEL - terrain.floorTop();
            if (depth < 8) continue;

            int count = 2 + r.nextInt(3);
            int spawned = 0;

            for (int i = 0; i < count; i++) {
                int sx = x + r.nextInt(7) - 3;
                int sz = z + r.nextInt(7) - 3;

                AbyssTerrain.Sample nearby = AbyssTerrain.sample(sx, sz);
                if (!nearby.water() || nearby.lava()) continue;

                int d = AbyssTerrain.SEA_LEVEL - nearby.floorTop();
                if (d < 8) continue;

                // Нижние две трети водоёма, не вплотную ко дну
                int minY = nearby.floorTop() + 2;
                int maxY = nearby.floorTop() + Math.max(2, (d * 2) / 3);
                int sy = minY + r.nextInt(maxY - minY + 1);

                if (!AbyssBiome1ChunkHandler.isInsideBiome1(sy)) continue;

                BlockPos at = new BlockPos(sx, sy, sz);
                if (!level.getFluidState(at).is(FluidTags.WATER)) continue;

                OrthocerasEntity mob = ModEntities.ORTHOCERAS.get()
                        .create(level, EntitySpawnReason.CHUNK_GENERATION);

                if (mob == null) continue;

                mob.snapTo(
                        sx + 0.5D,
                        sy + 0.5D,
                        sz + 0.5D,
                        r.nextFloat() * 360F,
                        0F
                );

                if (!level.noCollision(mob)) {
                    mob.discard();
                    continue;
                }

                mob.setVariant(r.nextInt(3));
                mob.setPersistenceRequired();

                if (level.addFreshEntity(mob)) {
                    spawned++;
                    Abyss.LOGGER.info(
                            ">>> Orthoceras spawned at {} {} {}",
                            sx, sy, sz
                    );
                }
            }

            if (spawned > 0) return;
        }
    }
    private static void spawnQuetzalcoatlus(
            ServerLevel level,
            ChunkPos pos,
            Random r
    ) {
        // Ищем подходящую открытую площадку.
        for (int attempt = 0; attempt < 12; attempt++) {

            int x = pos.getMinBlockX() + 2 + r.nextInt(12);
            int z = pos.getMinBlockZ() + 2 + r.nextInt(12);

            AbyssTerrain.Sample terrain = AbyssTerrain.sample(x, z);

            // Только суша.
            if (terrain.water() || terrain.lava()) continue;

            // Не появляется в ядовитом лесу.
            if (AbyssTerrain.isPoisonZone(x, z)) continue;

            int groundY = terrain.floorTop();
            int spawnY = groundY + 1;

            // Только первый биом.
            if (!AbyssBiome1ChunkHandler.isInsideBiome1(spawnY)) {
                continue;
            }

            BlockPos ground = new BlockPos(x, groundY, z);

            // Появляется на обычной траве Бездны.
            if (!level.getBlockState(ground).is(Abyss.ABYSS_GRASS.get())) {
                continue;
            }

            QuetzalcoatlusEntity quetzalcoatlus =
                    ModEntities.QUETZALCOATLUS.get().create(
                            level,
                            EntitySpawnReason.CHUNK_GENERATION
                    );

            if (quetzalcoatlus == null) return;

            quetzalcoatlus.snapTo(
                    x + 0.5D,
                    spawnY,
                    z + 0.5D,
                    r.nextFloat() * 360.0F,
                    0.0F
            );

            // Проверяем свободное пространство для большого тела.
            if (!level.noCollision(quetzalcoatlus)) {
                quetzalcoatlus.discard();
                continue;
            }

            // Оставляем естественный вариант окраски,
            // назначенный при создании существа.

            quetzalcoatlus.setPersistenceRequired();

            if (level.addFreshEntity(quetzalcoatlus)) {
                Abyss.LOGGER.info(
                        ">>> Quetzalcoatlus spawned at {} {} {}",
                        x, spawnY, z
                );
            }

            return;
        }
    }
}