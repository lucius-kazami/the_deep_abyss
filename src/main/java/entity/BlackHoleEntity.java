package com.akashi.abyss.abyss.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.particles.BlockParticleOption;



import java.util.List;

public class BlackHoleEntity extends Entity {

    // ============================================================
    // SYNCHED DATA
    // ============================================================

    private static final EntityDataAccessor<Boolean> SUPER_BLACK_HOLE =
            SynchedEntityData.defineId(
                    BlackHoleEntity.class,
                    EntityDataSerializers.BOOLEAN
            );


    // ============================================================
    // ОБЫЧНАЯ ЧЁРНАЯ ДЫРА
    // ============================================================

    private static final int NORMAL_LIFE_TIME = 20 * 5;

    private static final double NORMAL_PULL_RADIUS = 10.0D;

    private static final double NORMAL_PULL_STRENGTH = 0.18D;

    private static final float NORMAL_DAMAGE = 10.0F;


    // ============================================================
    // ВЕЛИКАЯ ЧЁРНАЯ ДЫРА
    // ============================================================

    private static final int SUPER_LIFE_TIME = 20 * 8;

    private static final double SUPER_PULL_RADIUS = 20.0D;

    private static final double SUPER_PULL_STRENGTH = 0.40D;

    private static final float SUPER_DAMAGE = 15.0F;


    // ============================================================
    // ДАННЫЕ
    // ============================================================

    private int life = 0;

    private Entity owner;


    // ============================================================
    // КОНСТРУКТОР
    // ============================================================

    public BlackHoleEntity(
            EntityType<? extends BlackHoleEntity> type,
            Level level
    ) {

        super(type, level);

        setNoGravity(true);
    }


    // ============================================================
    // SYNCHED DATA
    // ============================================================

    @Override
    protected void defineSynchedData(
            SynchedEntityData.Builder builder
    ) {

        builder.define(
                SUPER_BLACK_HOLE,
                false
        );
    }


    // ============================================================
    // ТИП ЧЁРНОЙ ДЫРЫ
    // ============================================================

    public void setSuperBlackHole(boolean value) {

        entityData.set(
                SUPER_BLACK_HOLE,
                value
        );
    }


    public boolean isSuperBlackHole() {

        return entityData.get(
                SUPER_BLACK_HOLE
        );
    }


    // ============================================================
    // OWNER
    // ============================================================

    public void setOwner(Entity owner) {

        this.owner = owner;
    }


    public Entity getOwner() {

        return owner;
    }


    // ============================================================
    // LIFE
    // ============================================================

    public int getBlackHoleLife() {

        return life;
    }


    public int getMaxLife() {

        return isSuperBlackHole()
                ? SUPER_LIFE_TIME
                : NORMAL_LIFE_TIME;
    }


    // ============================================================
    // VISUAL SCALE
    // ============================================================

    public float getVisualScale() {

        float maxScale =
                isSuperBlackHole()
                        ? 6.0F
                        : 3.0F;


        int formationTime =
                isSuperBlackHole()
                        ? 30
                        : 20;


        // --------------------------------------------------------
        // ПОЯВЛЕНИЕ
        // --------------------------------------------------------

        if (life < formationTime) {

            float progress =
                    life / (float) formationTime;

            // Плавное ускорение роста
            progress =
                    progress * progress;

            return Math.max(
                    0.05F,
                    maxScale * progress
            );
        }


        // --------------------------------------------------------
        // КОЛЛАПС
        // --------------------------------------------------------

        int remaining =
                getMaxLife() - life;


        if (remaining < 20) {

            float progress =
                    remaining / 20.0F;

            return Math.max(
                    0.05F,
                    maxScale * progress
            );
        }


        return maxScale;
    }


    // ============================================================
    // TICK
    // ============================================================

    @Override
    public void tick() {

        super.tick();

        life++;


        // ========================================================
        // КОНЕЦ ЖИЗНИ
        // ========================================================

        if (life >= getMaxLife()) {

            if (level() instanceof ServerLevel serverLevel) {

                spawnCollapseEffect(
                        serverLevel
                );
            }

            discard();

            return;
        }


        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }


        // ========================================================
        // АНИМАЦИЯ
        // ========================================================

        spawnBlackHoleAnimation(
                serverLevel
        );


        // ========================================================
        // ВРЕМЯ ФОРМИРОВАНИЯ
        // ========================================================

        int formationTime =
                isSuperBlackHole()
                        ? 30
                        : 20;


        // Пока формируется — ещё никого не тянем
        if (life < formationTime) {
            return;
        }


        // ========================================================
        // ПРИТЯЖЕНИЕ
        // ========================================================

        pullLivingEntities(
                serverLevel
        );
        if (isSuperBlackHole()) {
            pullBlocks(serverLevel);
        }
    }
    private void pullBlocks(ServerLevel level) {

        // Только большая чёрная дыра
        if (!isSuperBlackHole()) {
            return;
        }

        // ============================================================
        // НАСТРОЙКИ
        // ============================================================

        // Радиус разрушения
        int radius = 20;

        // Сколько блоков уничтожается за один проход
        int blocksPerTick = 80;

        int destroyed = 0;

        BlockPos center = blockPosition();


        // ============================================================
        // РАЗРУШЕНИЕ
        // ============================================================

        for (int x = -radius; x <= radius; x++) {

            for (int y = -radius; y <= radius; y++) {

                for (int z = -radius; z <= radius; z++) {

                    // Достигли лимита
                    if (destroyed >= blocksPerTick) {
                        return;
                    }


                    // =================================================
                    // СФЕРИЧЕСКАЯ ОБЛАСТЬ
                    // =================================================

                    double distanceSq =
                            x * x
                                    + y * y
                                    + z * z;

                    if (distanceSq > radius * radius) {
                        continue;
                    }


                    BlockPos pos =
                            center.offset(
                                    x,
                                    y,
                                    z
                            );


                    BlockState state =
                            level.getBlockState(pos);


                    // Воздух
                    if (state.isAir()) {
                        continue;
                    }


                    // Bedrock не уничтожаем
                    if (state.is(Blocks.BEDROCK)) {
                        continue;
                    }


                    // Неразрушаемые блоки не трогаем
                    if (state.getDestroySpeed(level, pos) < 0.0F) {
                        continue;
                    }


                    // BlockEntity пока не трогаем
                    // Сундуки, печки и т.д.
                    if (level.getBlockEntity(pos) != null) {
                        continue;
                    }


                    // =================================================
                    // УНИЧТОЖАЕМ БЛОК НАВСЕГДА
                    // =================================================

                    level.removeBlock(
                            pos,
                            false
                    );


                    // =================================================
                    // ЧАСТИЦЫ РАЗРУШЕНИЯ
                    // =================================================

                    level.sendParticles(
                            new BlockParticleOption(
                                    ParticleTypes.BLOCK,
                                    state
                            ),

                            pos.getX() + 0.5D,
                            pos.getY() + 0.5D,
                            pos.getZ() + 0.5D,

                            4,

                            0.25D,
                            0.25D,
                            0.25D,

                            0.05D
                    );


                    destroyed++;
                }
            }
        }
    }

        // ============================================================
    // ПРИТЯГИВАНИЕ СУЩЕСТВ
    // ============================================================

    private void pullLivingEntities(
            ServerLevel serverLevel
    ) {

        double radius =
                isSuperBlackHole()
                        ? SUPER_PULL_RADIUS
                        : NORMAL_PULL_RADIUS;


        float damage =
                isSuperBlackHole()
                        ? SUPER_DAMAGE
                        : NORMAL_DAMAGE;


        AABB area =
                getBoundingBox()
                        .inflate(radius);


        List<LivingEntity> entities =
                serverLevel.getEntitiesOfClass(
                        LivingEntity.class,
                        area,
                        target ->
                                target.isAlive()
                                        && target != owner
                );


        for (LivingEntity target : entities) {

            pullEntity(
                    target,
                    radius
            );


            // Урон один раз в секунду
            if (life % 20 == 0) {

                target.hurtServer(
                        serverLevel,
                        serverLevel.damageSources().magic(),
                        damage
                );
            }
        }
    }


    // ============================================================
    // ФИЗИКА ПРИТЯЖЕНИЯ
    // ============================================================

    private void pullEntity(
            LivingEntity target,
            double radius
    ) {

        Vec3 center =
                position();


        Vec3 targetCenter =
                target.position()
                        .add(
                                0.0D,
                                target.getBbHeight() / 2.0D,
                                0.0D
                        );


        Vec3 direction =
                center.subtract(
                        targetCenter
                );


        double distance =
                direction.length();


        if (distance <= 0.1D) {
            return;
        }


        double baseStrength =
                isSuperBlackHole()
                        ? SUPER_PULL_STRENGTH
                        : NORMAL_PULL_STRENGTH;


        // Чем ближе к центру, тем сильнее притяжение
        double closeness =
                1.0D
                        - Math.min(
                        distance / radius,
                        1.0D
                );


        double strength =
                baseStrength
                        * (
                        1.0D
                                + closeness * 1.5D
                );


        // Очень близко к центру
        if (distance < 3.0D) {

            strength *=
                    isSuperBlackHole()
                            ? 2.3D
                            : 1.6D;
        }


        Vec3 pull =
                direction
                        .normalize()
                        .scale(strength);


        target.setDeltaMovement(
                target.getDeltaMovement()
                        .add(pull)
        );


        target.hurtMarked = true;
    }


    // ============================================================
    // ОСНОВНАЯ АНИМАЦИЯ ЧЁРНОЙ ДЫРЫ
    // ============================================================

    private void spawnBlackHoleAnimation(
            ServerLevel level
    ) {

        boolean superHole =
                isSuperBlackHole();


        double scale =
                getVisualScale();


        // ========================================================
        // 1. ЧЁРНОЕ ЯДРО
        // ========================================================

        spawnCore(
                level,
                scale,
                superHole
        );


        // ========================================================
        // 2. ВНУТРЕННЕЕ ВРАЩАЮЩЕЕСЯ КОЛЬЦО
        // ========================================================

        spawnRotatingRing(
                level,

                scale * 0.65D,

                superHole
                        ? 20
                        : 14,

                0.16D,

                0.0D
        );


        // ========================================================
        // 3. СРЕДНЕЕ КОЛЬЦО
        // ========================================================

        spawnRotatingRing(
                level,

                scale,

                superHole
                        ? 28
                        : 18,

                -0.10D,

                1.5D
        );


        // ========================================================
        // 4. ВНЕШНЕЕ КОЛЬЦО
        // ========================================================

        spawnRotatingRing(
                level,

                scale * 1.45D,

                superHole
                        ? 36
                        : 22,

                0.07D,

                3.0D
        );


        // ========================================================
        // 5. ВТЯГИВАЮЩАЯСЯ МАТЕРИЯ
        // ========================================================

        spawnIncomingMatter(
                level,
                scale,
                superHole
        );


        // ========================================================
        // 6. ДОПОЛНИТЕЛЬНЫЕ ЧАСТИЦЫ ДЛЯ БОЛЬШОЙ ДЫРЫ
        // ========================================================

        if (superHole) {

            spawnSuperBlackHoleEffects(
                    level,
                    scale
            );
        }
    }


    // ============================================================
    // ЧЁРНОЕ ЯДРО
    // ============================================================

    private void spawnCore(
            ServerLevel level,
            double scale,
            boolean superHole
    ) {

        double coreSize =
                Math.max(
                        0.1D,
                        scale * 0.15D
                );


        level.sendParticles(
                ParticleTypes.SQUID_INK,

                getX(),
                getY(),
                getZ(),

                superHole
                        ? 14
                        : 7,

                coreSize,
                coreSize,
                coreSize,

                0.005D
        );


        // Фиолетовая энергия непосредственно вокруг ядра
        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,

                getX(),
                getY(),
                getZ(),

                superHole
                        ? 12
                        : 6,

                scale * 0.25D,
                scale * 0.25D,
                scale * 0.25D,

                0.01D
        );
    }


    // ============================================================
    // ВРАЩАЮЩЕЕСЯ КОЛЬЦО
    // ============================================================

    private void spawnRotatingRing(
            ServerLevel level,
            double radius,
            int particleCount,
            double rotationSpeed,
            double phase
    ) {

        double rotation =
                life * rotationSpeed
                        + phase;


        for (int i = 0; i < particleCount; i++) {

            double angle =
                    rotation
                            + (
                            Math.PI
                                    * 2.0D
                                    * i
                                    / particleCount
                    );


            double x =
                    getX()
                            + Math.cos(angle)
                            * radius;


            double z =
                    getZ()
                            + Math.sin(angle)
                            * radius;


            // Небольшая волна по высоте
            double y =
                    getY()
                            + Math.sin(
                            angle * 2.0D
                                    + life * 0.08D
                    ) * radius * 0.12D;


            level.sendParticles(
                    ParticleTypes.PORTAL,

                    x,
                    y,
                    z,

                    1,

                    0.02D,
                    0.02D,
                    0.02D,

                    0.0D
            );
        }
    }


    // ============================================================
    // МАТЕРИЯ, ЛЕТЯЩАЯ К ЧЁРНОЙ ДЫРЕ
    // ============================================================

    private void spawnIncomingMatter(
            ServerLevel level,
            double scale,
            boolean superHole
    ) {

        int streams =
                superHole
                        ? 14
                        : 7;


        double outerRadius =
                scale
                        * (
                        superHole
                                ? 3.5D
                                : 2.8D
                );


        for (int i = 0; i < streams; i++) {

            double angle =
                    life * 0.08D
                            + (
                            Math.PI
                                    * 2.0D
                                    * i
                                    / streams
                    );


            // Радиус немного меняется,
            // создавая эффект движения внутрь
            double animation =
                    (
                            (life + i * 5)
                                    % 30
                    ) / 30.0D;


            double radius =
                    outerRadius
                            * (
                            1.0D
                                    - animation * 0.75D
                    );


            double x =
                    getX()
                            + Math.cos(angle)
                            * radius;


            double z =
                    getZ()
                            + Math.sin(angle)
                            * radius;


            double y =
                    getY()
                            + Math.sin(
                            angle * 1.7D
                    ) * scale * 0.5D;


            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,

                    x,
                    y,
                    z,

                    2,

                    0.08D,
                    0.08D,
                    0.08D,

                    0.02D
            );
        }
    }


    // ============================================================
    // ВЕЛИКАЯ СИНГУЛЯРНОСТЬ
    // ============================================================

    private void spawnSuperBlackHoleEffects(
            ServerLevel level,
            double scale
    ) {

        // ========================================================
        // ВЕРТИКАЛЬНОЕ КОЛЬЦО №1
        // ========================================================

        int particles = 24;


        double rotation =
                life * 0.08D;


        for (int i = 0; i < particles; i++) {

            double angle =
                    rotation
                            + (
                            Math.PI
                                    * 2.0D
                                    * i
                                    / particles
                    );


            double x =
                    getX()
                            + Math.cos(angle)
                            * scale * 1.2D;


            double y =
                    getY()
                            + Math.sin(angle)
                            * scale * 1.2D;


            level.sendParticles(
                    ParticleTypes.REVERSE_PORTAL,

                    x,
                    y,
                    getZ(),

                    1,

                    0.03D,
                    0.03D,
                    0.03D,

                    0.0D
            );
        }


        // ========================================================
        // ВЕРТИКАЛЬНОЕ КОЛЬЦО №2
        // ========================================================

        for (int i = 0; i < particles; i++) {

            double angle =
                    -rotation
                            + (
                            Math.PI
                                    * 2.0D
                                    * i
                                    / particles
                    );


            double z =
                    getZ()
                            + Math.cos(angle)
                            * scale;


            double y =
                    getY()
                            + Math.sin(angle)
                            * scale;


            level.sendParticles(
                    ParticleTypes.PORTAL,

                    getX(),
                    y,
                    z,

                    1,

                    0.03D,
                    0.03D,
                    0.03D,

                    0.0D
            );
        }
    }


    // ============================================================
    // КОЛЛАПС
    // ============================================================

    private void spawnCollapseEffect(
            ServerLevel level
    ) {

        boolean superHole =
                isSuperBlackHole();


        double radius =
                superHole
                        ? 4.0D
                        : 2.0D;


        // ========================================================
        // ФИОЛЕТОВАЯ ВСПЫШКА
        // ========================================================

        level.sendParticles(
                ParticleTypes.REVERSE_PORTAL,

                getX(),
                getY(),
                getZ(),

                superHole
                        ? 200
                        : 80,

                radius,
                radius,
                radius,

                0.25D
        );


        // ========================================================
        // ТЁМНАЯ ВСПЫШКА
        // ========================================================

        level.sendParticles(
                ParticleTypes.SQUID_INK,

                getX(),
                getY(),
                getZ(),

                superHole
                        ? 70
                        : 30,

                radius * 0.4D,
                radius * 0.4D,
                radius * 0.4D,

                0.08D
        );


        // ========================================================
        // ПОРТАЛЬНЫЙ ВЗРЫВ
        // ========================================================

        level.sendParticles(
                ParticleTypes.PORTAL,

                getX(),
                getY(),
                getZ(),

                superHole
                        ? 150
                        : 60,

                radius,
                radius,
                radius,

                0.20D
        );
    }


    // ============================================================
    // ЧЁРНАЯ ДЫРА НЕ ПОЛУЧАЕТ УРОН
    // ============================================================

    @Override
    public boolean hurtServer(
            ServerLevel serverLevel,
            DamageSource damageSource,
            float amount
    ) {

        return false;
    }


    // ============================================================
    // СОХРАНЕНИЕ
    // ============================================================

    @Override
    protected void readAdditionalSaveData(
            net.minecraft.world.level.storage.ValueInput input
    ) {
    }


    @Override
    protected void addAdditionalSaveData(
            net.minecraft.world.level.storage.ValueOutput output
    ) {
    }
}