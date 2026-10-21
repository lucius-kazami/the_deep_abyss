
package com.akashi.abyss.abyss.entity.water;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class XiphactinusModel
        extends DefaultedEntityGeoModel<XiphactinusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/xiphactinus_0.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/xiphactinus_1.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/xiphactinus_2.png")
    };

    public XiphactinusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID, "xiphactinus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant = state instanceof XiphactinusRenderState s
                ? s.variant
                : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
