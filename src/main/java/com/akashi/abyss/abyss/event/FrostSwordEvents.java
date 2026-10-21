package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class FrostSwordEvents {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level =
                (ServerLevel) player.level();


        // ========================================================
        // ПРОВЕРЯЕМ, ДЕРЖИТ ЛИ ИГРОК МЕЧ
        // ========================================================

        ItemStack mainHand =
                player.getMainHandItem();

        ItemStack offHand =
                player.getOffhandItem();


        boolean holdingSword =
                mainHand.is(Abyss.ETERNAL_FROST_SWORD.get())
                        ||
                        offHand.is(Abyss.ETERNAL_FROST_SWORD.get());


        if (!holdingSword) {
            return;
        }


        // ========================================================
        // ЛЁГКИЙ МОРОЗ ВОКРУГ МЕЧА
        // ========================================================

        if (player.tickCount % 3 == 0) {

            level.sendParticles(
                    ParticleTypes.SNOWFLAKE,

                    player.getX(),
                    player.getY() + 0.15,
                    player.getZ(),

                    3,

                    0.25,
                    0.05,
                    0.25,

                    0.01
            );
        }


        // ========================================================
        // ЕСЛИ ИГРОК ДВИГАЕТСЯ
        // ========================================================

        double horizontalSpeed =
                player.getDeltaMovement()
                        .horizontalDistanceSqr();


        if (horizontalSpeed < 0.001D) {
            return;
        }


        // ========================================================
        // ЛЕДЯНОЙ СЛЕД
        // ========================================================

        level.sendParticles(
                ParticleTypes.SNOWFLAKE,

                player.getX(),
                player.getY() + 0.05,
                player.getZ(),

                7,

                0.35,
                0.03,
                0.35,

                0.01
        );


        level.sendParticles(
                ParticleTypes.ITEM_SNOWBALL,

                player.getX(),
                player.getY() + 0.05,
                player.getZ(),

                2,

                0.25,
                0.02,
                0.25,

                0.01
        );
    }
}