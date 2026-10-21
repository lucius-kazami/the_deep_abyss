package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public final class EmptyOreLightningHandler {

    private EmptyOreLightningHandler() {
    }

    @SubscribeEvent
    public static void onLightningTick(EntityTickEvent.Post event) {

        // Нас интересует только молния
        if (!(event.getEntity() instanceof LightningBolt lightning)) {
            return;
        }

        // Только серверная сторона
        if (!(lightning.level() instanceof ServerLevel level)) {
            return;
        }

        // Проверяем только первые тики жизни молнии,
        // чтобы одна молния не проверяла блок много раз
        if (lightning.tickCount > 2) {
            return;
        }

        BlockPos center = lightning.blockPosition();

        /*
         * Проверяем небольшой участок вокруг точки молнии.
         *
         * Это надёжнее, чем проверять только один блок,
         * потому что координата LightningBolt может оказаться
         * немного выше блока, в который визуально ударила молния.
         */
        for (int x = -1; x <= 1; x++) {
            for (int y = -2; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {

                    BlockPos pos = center.offset(x, y, z);

                    BlockState state = level.getBlockState(pos);

                    // Нашли пустую руду
                    if (state.is(Abyss.EMPTY_ORE.get())) {

                        // Мгновенно превращаем в руду молнии
                        level.setBlock(
                                pos,
                                Abyss.LIGHTNING_ORE.get().defaultBlockState(),
                                3
                        );

                        return;
                    }
                }
            }
        }
    }
}