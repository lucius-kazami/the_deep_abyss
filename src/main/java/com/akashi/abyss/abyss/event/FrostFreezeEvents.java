package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


// ============================================================
// СИСТЕМА ЗАМОРОЗКИ
// ============================================================

@EventBusSubscriber(modid = Abyss.MODID)
public class FrostFreezeEvents {

    // ========================================================
    // НАСТРОЙКИ
    // ========================================================

    /**
     * 20 тиков = 1 секунда.
     * 200 тиков = 10 секунд.
     */
    public static final int FREEZE_DURATION = 200;


    /**
     * UUID существа -> оставшееся время заморозки.
     */
    private static final Map<UUID, Integer> FROZEN =
            new HashMap<>();


    // ========================================================
    // ЗАМОРОЗИТЬ СУЩЕСТВО
    // ========================================================

    public static void freeze(LivingEntity entity) {

        if (entity == null) {
            return;
        }


        // ----------------------------------------------------
        // СБРАСЫВАЕМ ТАЙМЕР НА 10 СЕКУНД
        // ----------------------------------------------------

        FROZEN.put(
                entity.getUUID(),
                FREEZE_DURATION
        );


        // ----------------------------------------------------
        // ВАНИЛЬНЫЙ ЭФФЕКТ ЗАМЕРЗАНИЯ
        // ----------------------------------------------------

        entity.setTicksFrozen(
                Math.max(
                        entity.getTicksFrozen(),
                        FREEZE_DURATION
                )
        );


        // ----------------------------------------------------
        // ЕСЛИ ЭТО МОБ
        // ----------------------------------------------------

        if (entity instanceof Mob mob) {

            // Останавливаем навигацию
            mob.getNavigation().stop();

            // Убираем текущую цель
            mob.setTarget(null);
        }


        // ----------------------------------------------------
        // ОСТАНАВЛИВАЕМ ДВИЖЕНИЕ
        // ----------------------------------------------------

        entity.setDeltaMovement(
                Vec3.ZERO
        );

        entity.hurtMarked = true;


        // ----------------------------------------------------
        // МОЩНАЯ ВСПЫШКА ПРИ ЗАМОРОЗКЕ
        // ----------------------------------------------------

        if (entity.level() instanceof ServerLevel serverLevel) {

            double centerY =
                    entity.getY()
                            + entity.getBbHeight() / 2.0D;


            // Снежная вспышка
            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,

                    entity.getX(),
                    centerY,
                    entity.getZ(),

                    60,

                    entity.getBbWidth() * 0.8D,
                    entity.getBbHeight() * 0.55D,
                    entity.getBbWidth() * 0.8D,

                    0.08D
            );


            // Ледяные осколки
            serverLevel.sendParticles(
                    ParticleTypes.ITEM_SNOWBALL,

                    entity.getX(),
                    centerY,
                    entity.getZ(),

                    25,

                    entity.getBbWidth() * 0.7D,
                    entity.getBbHeight() * 0.5D,
                    entity.getBbWidth() * 0.7D,

                    0.05D
            );
        }
    }


    // ========================================================
    // TICK ЗАМОРОЖЕННОГО СУЩЕСТВА
    // ========================================================

    @SubscribeEvent
    public static void onEntityTick(
            EntityTickEvent.Post event
    ) {

        // ----------------------------------------------------
        // НАМ НУЖНЫ ТОЛЬКО ЖИВЫЕ СУЩЕСТВА
        // ----------------------------------------------------

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }


        // ----------------------------------------------------
        // РАБОТАЕМ ТОЛЬКО НА СЕРВЕРЕ
        // ----------------------------------------------------

        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }


        UUID uuid =
                entity.getUUID();


        Integer remaining =
                FROZEN.get(uuid);


        // Сущность не заморожена
        if (remaining == null) {
            return;
        }


        // ----------------------------------------------------
        // ЕСЛИ СУЩЕСТВО УМЕРЛО
        // ----------------------------------------------------

        if (!entity.isAlive()) {

            FROZEN.remove(uuid);

            return;
        }


        // ====================================================
        // ПОЛНАЯ ОСТАНОВКА ДВИЖЕНИЯ
        // ====================================================

        entity.setDeltaMovement(
                Vec3.ZERO
        );

        entity.hurtMarked = true;


        // ====================================================
        // ОСТАНАВЛИВАЕМ AI МОБА
        // ====================================================

        if (entity instanceof Mob mob) {

            mob.getNavigation().stop();

            mob.setTarget(null);
        }


        // ====================================================
        // ПОДДЕРЖИВАЕМ ВАНИЛЬНУЮ ЗАМОРОЗКУ
        // ====================================================

        entity.setTicksFrozen(
                Math.max(
                        entity.getTicksFrozen(),
                        140
                )
        );


        // ====================================================
        // ЛЕДЯНОЙ ЭФФЕКТ ВОКРУГ ТЕЛА
        // ====================================================

        spawnFrozenParticles(
                serverLevel,
                entity
        );


        // ====================================================
        // УМЕНЬШАЕМ ТАЙМЕР
        // ====================================================

        remaining--;


        // ====================================================
        // КОНЕЦ ЗАМОРОЗКИ
        // ====================================================

        if (remaining <= 0) {

            FROZEN.remove(uuid);


            // Убираем ванильную заморозку
            entity.setTicksFrozen(0);


            // Финальные частицы
            spawnUnfreezeParticles(
                    serverLevel,
                    entity
            );


            return;
        }


        // Сохраняем новое время
        FROZEN.put(
                uuid,
                remaining
        );
    }


    // ========================================================
    // ПОСТОЯННЫЙ ЛЕДЯНОЙ ЭФФЕКТ
    // ========================================================

    private static void spawnFrozenParticles(
            ServerLevel serverLevel,
            LivingEntity entity
    ) {

        double centerY =
                entity.getY()
                        + entity.getBbHeight() / 2.0D;


        double radius =
                Math.max(
                        0.55D,
                        entity.getBbWidth() * 0.85D
                );


        // ====================================================
        // НИЖНЕЕ ЛЕДЯНОЕ КОЛЬЦО
        // ====================================================

        spawnIceRing(
                serverLevel,
                entity,
                entity.getY()
                        + entity.getBbHeight() * 0.15D,
                radius
        );


        // ====================================================
        // СРЕДНЕЕ ЛЕДЯНОЕ КОЛЬЦО
        // ====================================================

        spawnIceRing(
                serverLevel,
                entity,
                entity.getY()
                        + entity.getBbHeight() * 0.50D,
                radius
        );


        // ====================================================
        // ВЕРХНЕЕ ЛЕДЯНОЕ КОЛЬЦО
        // ====================================================

        spawnIceRing(
                serverLevel,
                entity,
                entity.getY()
                        + entity.getBbHeight() * 0.85D,
                radius
        );


        // ====================================================
        // СНЕГ ВНУТРИ КОКОНА
        // ====================================================

        if (entity.tickCount % 2 == 0) {

            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,

                    entity.getX(),
                    centerY,
                    entity.getZ(),

                    6,

                    entity.getBbWidth() * 0.55D,
                    entity.getBbHeight() * 0.45D,
                    entity.getBbWidth() * 0.55D,

                    0.01D
            );
        }


        // ====================================================
        // ЛЕДЯНЫЕ ОСКОЛКИ
        // ====================================================

        if (entity.tickCount % 4 == 0) {

            serverLevel.sendParticles(
                    ParticleTypes.ITEM_SNOWBALL,

                    entity.getX(),
                    centerY,
                    entity.getZ(),

                    4,

                    entity.getBbWidth() * 0.55D,
                    entity.getBbHeight() * 0.45D,
                    entity.getBbWidth() * 0.55D,

                    0.01D
            );
        }


        // ====================================================
        // ЧАСТИЦЫ У НОГ
        // ====================================================

        if (entity.tickCount % 3 == 0) {

            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,

                    entity.getX(),
                    entity.getY() + 0.1D,
                    entity.getZ(),

                    8,

                    radius,
                    0.05D,
                    radius,

                    0.01D
            );
        }
    }


    // ========================================================
    // ЛЕДЯНОЕ КОЛЬЦО
    // ========================================================

    private static void spawnIceRing(
            ServerLevel serverLevel,
            LivingEntity entity,
            double y,
            double radius
    ) {

        int particles = 10;


        for (int i = 0; i < particles; i++) {

            double angle =
                    (Math.PI * 2.0D / particles) * i;


            double x =
                    entity.getX()
                            + Math.cos(angle) * radius;


            double z =
                    entity.getZ()
                            + Math.sin(angle) * radius;


            serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,

                    x,
                    y,
                    z,

                    1,

                    0.015D,
                    0.015D,
                    0.015D,

                    0.0D
            );
        }
    }


    // ========================================================
    // ЭФФЕКТ РАЗМОРОЗКИ
    // ========================================================

    private static void spawnUnfreezeParticles(
            ServerLevel serverLevel,
            LivingEntity entity
    ) {

        double centerY =
                entity.getY()
                        + entity.getBbHeight() / 2.0D;


        // ----------------------------------------------------
        // СНЕГ
        // ----------------------------------------------------

        serverLevel.sendParticles(
                ParticleTypes.SNOWFLAKE,

                entity.getX(),
                centerY,
                entity.getZ(),

                40,

                entity.getBbWidth() * 0.8D,
                entity.getBbHeight() * 0.55D,
                entity.getBbWidth() * 0.8D,

                0.08D
        );


        // ----------------------------------------------------
        // РАЗЛЕТАЮЩИЕСЯ ОСКОЛКИ
        // ----------------------------------------------------

        serverLevel.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,

                entity.getX(),
                centerY,
                entity.getZ(),

                20,

                entity.getBbWidth() * 0.8D,
                entity.getBbHeight() * 0.55D,
                entity.getBbWidth() * 0.8D,

                0.08D
        );
    }


    // ========================================================
    // ПРОВЕРКА — ЗАМОРОЖЕНА ЛИ СУЩНОСТЬ
    // ========================================================

    public static boolean isFrozen(
            LivingEntity entity
    ) {

        return FROZEN.containsKey(
                entity.getUUID()
        );
    }


    // ========================================================
    // ПОЛУЧИТЬ ОСТАВШЕЕСЯ ВРЕМЯ
    // ========================================================

    public static int getRemainingFreezeTicks(
            LivingEntity entity
    ) {

        return FROZEN.getOrDefault(
                entity.getUUID(),
                0
        );
    }
}