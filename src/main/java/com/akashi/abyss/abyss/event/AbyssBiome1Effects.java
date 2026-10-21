package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssBiome1Effects {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().dimension() != AbyssDimensions.BEZDNA) {
            return;
        }

        if (!AbyssBiome1ChunkHandler.isInsideBiome1(player.getY())) {
            return;
        }

        // Один запуск каждые 10 тиков.
        if (player.tickCount % 3 != 0) {
            return;
        }

        ServerLevel level = (ServerLevel) player.level();

        spawnAmbientParticles(level, player);
        spawnGroundParticles(level, player);
    }

    private static void spawnAmbientParticles(
            ServerLevel level,
            ServerPlayer player
    ) {

        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        // Светящиеся огоньки.
        level.sendParticles(
                ParticleTypes.GLOW,
                x,
                y,
                z,
                6,
                8.0,
                4.0,
                8.0,
                0.015
        );

        // Лёгкие споры.
        if (level.random.nextFloat() < 0.75F) {

            level.sendParticles(
                    ParticleTypes.SPORE_BLOSSOM_AIR,
                    x,
                    y + 3,
                    z,
                    4,
                    7.0,
                    3.0,
                    7.0,
                    0.01
            );
        }
    }

    private static void spawnGroundParticles(
            ServerLevel level,
            ServerPlayer player
    ) {

        BlockPos playerPos = player.blockPosition();

        // Проверяем случайные места вокруг игрока.
        for (int i = 0; i < 4; i++) {

            int dx = level.random.nextInt(17) - 8;
            int dz = level.random.nextInt(17) - 8;

            BlockPos pos = playerPos.offset(dx, 0, dz);

            // Ищем землю.
            for (int dy = 5; dy >= -5; dy--) {

                BlockPos check = pos.offset(0, dy, 0);

                if (level.getBlockState(check).isAir()) {
                    continue;
                }

                BlockPos above = check.above();

                if (!level.getBlockState(above).isAir()) {
                    break;
                }

                // Вулканическая территория.
                if (level.getBlockState(check).is(
                        Abyss.ABYSS_CHARRED_GROUND.get()
                )) {

                    level.sendParticles(
                            ParticleTypes.FLAME,
                            above.getX() + 0.5,
                            above.getY() + 0.2,
                            above.getZ() + 0.5,
                            1,
                            0.2,
                            0.1,
                            0.2,
                            0.005
                    );

                    if (level.random.nextFloat() < 0.3F) {

                        level.sendParticles(
                                ParticleTypes.SMOKE,
                                above.getX() + 0.5,
                                above.getY() + 0.3,
                                above.getZ() + 0.5,
                                1,
                                0.2,
                                0.2,
                                0.2,
                                0.005
                        );
                    }

                    break;
                }

                // Влажная / заражённая территория.
                if (level.getBlockState(check).is(
                        Abyss.ABYSS_GRASS_WET.get()
                )) {

                    level.sendParticles(
                            ParticleTypes.WITCH,
                            above.getX() + 0.5,
                            above.getY() + 0.5,
                            above.getZ() + 0.5,
                            1,
                            0.4,
                            0.3,
                            0.4,
                            0.001
                    );

                    break;
                }

                break;
            }
        }
    }
}