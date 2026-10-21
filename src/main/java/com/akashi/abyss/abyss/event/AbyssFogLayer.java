package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssFogLayer {

    public static final int FOG_TOP = -500;
    public static final int FOG_BOTTOM = -580;

    private static final int RADIUS = 20;    // по горизонтали (сервер шлёт частицы только в 32 блоках)
    private static final int VERTICAL = 14;  // по вертикали вокруг игрока
    private static final int ATTEMPTS = 40;  // попыток за такт (больше — гуще туман)
    private static final int MARGIN = 14;    // туман виден чуть выше и ниже слоя

    private static boolean loggedOnce = false;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;

        int py = player.getBlockY();
        if (py > FOG_TOP + MARGIN || py < FOG_BOTTOM - MARGIN) return;

        if (!loggedOnce) {
            loggedOnce = true;
            Abyss.LOGGER.info(">>> Туман: игрок вошёл в слой {}..{}", FOG_BOTTOM, FOG_TOP);
        }

        int yMin = Math.max(FOG_BOTTOM, py - VERTICAL);
        int yMax = Math.min(FOG_TOP, py + VERTICAL);
        if (yMin > yMax) return;

        RandomSource rnd = level.getRandom();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < ATTEMPTS; i++) {
            int x = player.getBlockX() + rnd.nextInt(RADIUS * 2 + 1) - RADIUS;
            int z = player.getBlockZ() + rnd.nextInt(RADIUS * 2 + 1) - RADIUS;
            int y = yMin + rnd.nextInt(yMax - yMin + 1);
            pos.set(x, y, z);

            if (!level.hasChunkAt(pos)) continue;
            if (!level.getBlockState(pos).isAir()) continue; // туман только в пустоте

            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x + 0.5, y + 0.5, z + 0.5, 1, 0.5, 0.5, 0.5, 0.0);
        }
    }
}