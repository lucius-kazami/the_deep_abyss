package com.akashi.abyss.abyss.client.renderer;

import com.akashi.abyss.abyss.Abyss;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;


@EventBusSubscriber(
        modid = Abyss.MODID
)
public class ClientRenderEvents {

    // ============================================================
    // РЕГИСТРАЦИЯ ENTITY RENDERERS
    // ============================================================

    @SubscribeEvent
    public static void registerRenderers(
            EntityRenderersEvent.RegisterRenderers event
    ) {

        // ========================================================
        // ENERGY BLAST
        // ========================================================

        event.registerEntityRenderer(
                Abyss.ENERGY_BLAST.get(),
                EnergyBlastRenderer::new
        );
    }
}