
package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class GiganotosaurusModel
        extends DefaultedEntityGeoModel<GiganotosaurusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/giganotosaurus_0.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/giganotosaurus_1.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/giganotosaurus_2.png"
            )
    };

    public GiganotosaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "giganotosaurus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {

        int variant =
                renderState instanceof GiganotosaurusRenderer.GiganotosaurusRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
