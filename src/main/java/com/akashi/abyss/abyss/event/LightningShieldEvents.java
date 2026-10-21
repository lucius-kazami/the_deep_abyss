package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class LightningShieldEvents {

    /*
     * Дополнительный электрический урон
     * атакующему при блокировании.
     */
    private static final float COUNTER_DAMAGE = 8.0F;


    // ============================================================
    // БЛОКИРОВАНИЕ УДАРА ЩИТОМ
    // ============================================================

    @SubscribeEvent
    public static void onShieldBlock(
            LivingShieldBlockEvent event
    ) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        /*
         * Событие должно действительно быть заблокировано щитом.
         */
        if (!event.getBlocked()) {
            return;
        }

        /*
         * Предмет, который игрок сейчас использует.
         */
        ItemStack shield = player.getUseItem();

        if (!shield.is(Abyss.LIGHTNING_SHIELD.get())) {
            return;
        }

        /*
         * Кто нанёс удар.
         */
        Entity sourceEntity =
                event.getDamageSource().getEntity();

        if (!(sourceEntity instanceof LivingEntity attacker)) {
            return;
        }

        if (attacker == player) {
            return;
        }


        // ========================================================
        // ЭЛЕКТРИЧЕСКИЙ КОНТР-УДАР
        // ========================================================

        attacker.hurtServer(
                level,
                level.damageSources().lightningBolt(),
                COUNTER_DAMAGE
        );


        /*
         * Визуальная молния на атакующем.
         */
        createVisualLightning(
                level,
                attacker.getX(),
                attacker.getY(),
                attacker.getZ()
        );


        /*
         * Электрические частицы.
         */
        level.sendParticles(
                ParticleTypes.ELECTRIC_SPARK,

                attacker.getX(),
                attacker.getY() + attacker.getBbHeight() / 2.0,
                attacker.getZ(),

                35,

                0.5,
                0.8,
                0.5,

                0.15
        );
    }


    // ============================================================
    // ЗАЩИТА ОТ МОЛНИИ ПРИ ИСПОЛЬЗОВАНИИ ЩИТА
    // ============================================================

    @SubscribeEvent
    public static void onIncomingDamage(
            LivingIncomingDamageEvent event
    ) {

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!player.isUsingItem()) {
            return;
        }

        ItemStack shield =
                player.getUseItem();

        if (!shield.is(Abyss.LIGHTNING_SHIELD.get())) {
            return;
        }


        /*
         * Если урон относится к молнии —
         * полностью отменяем его.
         */
        if (event.getSource().is(
                net.minecraft.tags.DamageTypeTags.IS_LIGHTNING
        )) {

            event.setCanceled(true);


            /*
             * Маленький визуальный эффект поглощения.
             */
            if (player.level() instanceof ServerLevel level) {

                level.sendParticles(
                        ParticleTypes.ELECTRIC_SPARK,

                        player.getX(),
                        player.getY() + 1.0,
                        player.getZ(),

                        40,

                        0.7,
                        1.0,
                        0.7,

                        0.15
                );
            }
        }
    }


    // ============================================================
    // ВИЗУАЛЬНАЯ МОЛНИЯ
    // ============================================================

    private static void createVisualLightning(
            ServerLevel level,
            double x,
            double y,
            double z
    ) {

        LightningBolt lightning =
                new LightningBolt(
                        EntityType.LIGHTNING_BOLT,
                        level
                );

        lightning.setPos(
                x,
                y,
                z
        );

        /*
         * Только визуальный эффект.
         *
         * Урон выше наносится вручную.
         */
        lightning.setVisualOnly(true);

        level.addFreshEntity(lightning);
    }
}