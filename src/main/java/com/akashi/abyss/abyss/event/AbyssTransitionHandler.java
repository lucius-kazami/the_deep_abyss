package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssTransitionHandler {

    private static final int TRIGGER_Y = -64; // глубина входа в Bezdna

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;

        ServerLevel currentLevel = player.level();
        if (currentLevel.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;

        BlockPos pos = event.getPos();
        if (pos.getY() > TRIGGER_Y) return;

        MinecraftServer server = currentLevel.getServer();
        ServerLevel targetLevel = server.getLevel(AbyssDimensions.BEZDNA);
        if (targetLevel == null) return;

        // сохраняем реальную глубину — бесшовный переход, а не прыжок на фикс. высоту
        double targetY = pos.getY();

        player.teleportTo(targetLevel, player.getX(), targetY, player.getZ(),
                java.util.Set.of(), player.getYRot(), player.getXRot(), true);
    }
}