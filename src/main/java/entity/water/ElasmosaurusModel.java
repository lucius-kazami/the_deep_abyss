
package com.akashi.abyss.abyss.entity.water;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ElasmosaurusModel
        extends DefaultedEntityGeoModel<ElasmosaurusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/elasmosaurus_0.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/elasmosaurus_1.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/elasmosaurus_2.png"
            )
    };

    public ElasmosaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "elasmosaurus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant =
                state instanceof ElasmosaurusRenderer.ElasmosaurusRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
