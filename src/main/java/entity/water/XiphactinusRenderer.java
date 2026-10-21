
package com.akashi.abyss.abyss.entity.water;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class XiphactinusRenderer
        extends GeoEntityRenderer<XiphactinusEntity, XiphactinusRenderState> {

    public XiphactinusRenderer(EntityRendererProvider.Context context) {
        super(context, new XiphactinusModel());
        this.shadowRadius = 0.8F;
    }

    @Override
    public XiphactinusRenderState createRenderState(
            XiphactinusEntity entity,
            Void relatedObject
    ) {
        return new XiphactinusRenderState();
    }

    @Override
    public void extractRenderState(
            XiphactinusEntity entity,
            XiphactinusRenderState state,
            float partialTick
    ) {
        super.extractRenderState(entity, state, partialTick);

        // Передаём случайный вариант окраски модели
        state.variant = entity.getVariant();
    }
}
