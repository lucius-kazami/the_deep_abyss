package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


public class SpectatorChestplateEvents {

    // ============================================================
    // SPECTATOR STATE
    // ============================================================

    private static final String SPECTRAL_ACTIVE =
            "abyss_spectral_active";


    // ============================================================
    // PLAYER TICK
    // ============================================================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();

        // ========================================================
        // ПРОВЕРЯЕМ НАГРУДНИК
        // ========================================================

        ItemStack chest =
                player.getItemBySlot(EquipmentSlot.CHEST);

        boolean wearingSpectatorChestplate =
                chest.is(Abyss.SPECTATOR_CHESTPLATE.get());


        // ========================================================
        // CLIENT
        // ========================================================

        if (player.level().isClientSide()) {

            /*
             * На клиенте PersistentData с сервера может не содержать
             * наше состояние, поэтому для локальной физики просто
             * проверяем сам нагрудник.
             */

            if (wearingSpectatorChestplate) {

                // Отключаем столкновения
                player.noPhysics = true;

                // Не падаем
                player.fallDistance = 0.0F;
            }

            return;
        }


        // ========================================================
        // SERVER
        // ========================================================

        boolean spectral =
                isSpectral(player);


        // ========================================================
        // НАГРУДНИК НАДЕТ
        // ========================================================

        if (wearingSpectatorChestplate) {

            if (!spectral) {

                startSpectralMode(player);
            }


            // Поддерживаем способности каждый тик
            applySpectralAbilities(player);

            return;
        }


        // ========================================================
        // НАГРУДНИК СНЯТ
        // ========================================================

        if (spectral) {

            stopSpectralMode(player);
        }
    }


    // ============================================================
    // START SPECTRAL MODE
    // ============================================================

    private static void startSpectralMode(Player player) {

        player.getPersistentData()
                .putBoolean(
                        SPECTRAL_ACTIVE,
                        true
                );


        // ========================================================
        // РАЗРЕШАЕМ ПОЛЁТ
        // ========================================================

        player.getAbilities().mayfly = true;

        // Сразу включаем полёт
        player.getAbilities().flying = true;

        player.onUpdateAbilities();


        // ========================================================
        // NOCLIP
        // ========================================================

        player.noPhysics = true;

        player.fallDistance = 0.0F;
    }


    // ============================================================
    // SPECTRAL ABILITIES
    // ============================================================

    private static void applySpectralAbilities(Player player) {

        // ========================================================
        // ПОЛЁТ
        // ========================================================

        player.getAbilities().mayfly = true;

        player.getAbilities().flying = true;


        // ========================================================
        // NOCLIP
        // ========================================================

        player.noPhysics = true;

        player.fallDistance = 0.0F;


        // ========================================================
        // НЕВИДИМОСТЬ
        // ========================================================

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.INVISIBILITY,
                        10,
                        0,
                        false,
                        false,
                        false
                )
        );


        // ========================================================
        // НОЧНОЕ ЗРЕНИЕ
        // ========================================================

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.NIGHT_VISION,
                        220,
                        0,
                        false,
                        false,
                        false
                )
        );


        // ========================================================
        // СКОРОСТЬ
        // ========================================================

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.SPEED,
                        10,
                        1,
                        false,
                        false,
                        false
                )
        );


        // ========================================================
        // FALL PROTECTION
        // ========================================================

        player.addEffect(
                new MobEffectInstance(
                        MobEffects.SLOW_FALLING,
                        10,
                        0,
                        false,
                        false,
                        false
                )
        );
    }


    // ============================================================
    // STOP SPECTRAL MODE
    // ============================================================

    private static void stopSpectralMode(Player player) {

        player.getPersistentData()
                .putBoolean(
                        SPECTRAL_ACTIVE,
                        false
                );


        // ========================================================
        // ВОЗВРАЩАЕМ ФИЗИКУ
        // ========================================================

        player.noPhysics = false;

        player.fallDistance = 0.0F;


        // ========================================================
        // ВОЗВРАЩАЕМ ПОЛЁТ
        // ========================================================

        if (!player.isCreative() && !player.isSpectator()) {

            player.getAbilities().flying = false;

            player.getAbilities().mayfly = false;

            player.onUpdateAbilities();
        }


        // ========================================================
        // УБИРАЕМ ЭФФЕКТЫ
        // ========================================================

        player.removeEffect(MobEffects.INVISIBILITY);

        player.removeEffect(MobEffects.NIGHT_VISION);

        player.removeEffect(MobEffects.SPEED);

        player.removeEffect(MobEffects.SLOW_FALLING);
    }


    // ============================================================
    // БЕССМЕРТИЕ
    // ============================================================

    @SubscribeEvent
    public static void onSpectatorDamage(
            LivingIncomingDamageEvent event
    ) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }


        if (!isSpectral(player)) {
            return;
        }


        // ========================================================
        // ПОЛНОСТЬЮ ОТМЕНЯЕМ ВХОДЯЩИЙ УРОН
        // ========================================================

        event.setCanceled(true);
    }


    // ============================================================
    // МОБЫ ИГНОРИРУЮТ СПЕКТАТОРА
    // ============================================================

    @SubscribeEvent
    public static void onMobTick(
            EntityTickEvent.Post event
    ) {

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }


        if (mob.level().isClientSide()) {
            return;
        }


        // ========================================================
        // ТЕКУЩАЯ ЦЕЛЬ МОБА
        // ========================================================

        if (!(mob.getTarget() instanceof Player player)) {
            return;
        }


        // ========================================================
        // ОБЫЧНЫХ ИГРОКОВ НЕ ТРОГАЕМ
        // ========================================================

        if (!isSpectral(player)) {
            return;
        }


        // ========================================================
        // УБИРАЕМ ЦЕЛЬ
        // ========================================================

        mob.setTarget(null);


        // ========================================================
        // ОСТАНАВЛИВАЕМ ПРЕСЛЕДОВАНИЕ
        // ========================================================

        mob.getNavigation().stop();
    }


    // ============================================================
    // IS SPECTRAL
    // ============================================================

    public static boolean isSpectral(Player player) {

        return player
                .getPersistentData()
                .getBoolean(SPECTRAL_ACTIVE)
                .orElse(false);
    }
}