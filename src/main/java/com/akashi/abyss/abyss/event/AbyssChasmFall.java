package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.world.AbyssDimensions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Abyss.MODID)
public class AbyssChasmFall {

    private static final int TOP_Y = -460;
    private static final int BOTTOM_Y = -610;
    private static final double MARGIN = 2.0; // запас у стены разлома

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        if (level.dimension() != AbyssDimensions.BEZDNA) return;

        double y = player.getY();
        if (y > TOP_Y || y < BOTTOM_Y) return;
        if (!insideChasm(player.getX(), player.getZ())) return;

        // Обновляем эффект, только когда он почти закончился (без лишних пакетов)
        MobEffectInstance current = player.getEffect(MobEffects.SLOW_FALLING);
        if (current == null || current.getDuration() < 20) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, true, false, false));
        }
    }

    private static boolean insideChasm(double x, double z) {
        for (int[] c : AbyssGreatChasms.CENTERS) {
            double dx = x - (c[0] + 0.5);
            double dz = z - (c[1] + 0.5);
            if (Math.hypot(dx, dz) <= AbyssGreatChasms.RADIUS + MARGIN) return true;
        }
        return false;
    }
}