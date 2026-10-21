package com.akashi.abyss.abyss.event;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

// Видимость в лаве: убираем густой туман лавы, пока на игроке полный огненный набор.
// Если этот файл не компилируется (API тумана в 1.21.10 другое), просто удали его:
// всё остальное работает, а лава будет видна на ~3 блока (из-за сопротивления огню).
@EventBusSubscriber(modid = Abyss.MODID, value = Dist.CLIENT)
public class FireLavaVisionClient {

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getType() != FogType.LAVA) return;
        if (!(event.getCamera().getEntity() instanceof Player player)) return;
        if (!FireGearEvents.hasFullSet(player)) return;

        event.setNearPlaneDistance(-8.0F);
        event.setFarPlaneDistance(160.0F);
    }
}
