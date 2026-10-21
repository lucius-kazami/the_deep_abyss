package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import com.akashi.abyss.abyss.entity.SpinosaurusRenderer;

@EventBusSubscriber(modid = Abyss.MODID, value = Dist.CLIENT)
public final class ClientEntityEvents {
    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TRICERATOPS.get(), TriceratopsRenderer::new);
        event.registerEntityRenderer(ModEntities.TYRANNOSAURUS.get(), TyrannosaurusRenderer::new);
        event.registerEntityRenderer(ModEntities.STEGOSAURUS.get(), StegosaurusRenderer::new);
        event.registerEntityRenderer(ModEntities.PARASAUROLOPHUS.get(), ParasaurolophusRenderer::new);
        event.registerEntityRenderer(ModEntities.ANKYLOSAURUS.get(), AnkylosaurusRenderer::new);
        event.registerEntityRenderer(ModEntities.BRACHIOSAURUS.get(), BrachiosaurusRenderer::new);
        event.registerEntityRenderer(ModEntities.SPINOSAURUS.get(), SpinosaurusRenderer::new);
    }
}