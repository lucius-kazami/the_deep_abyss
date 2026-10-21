
package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class SpinosaurusModel
        extends DefaultedEntityGeoModel<SpinosaurusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/spinosaurus.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/spinosaurus_green.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/spinosaurus_dark.png"
            )
    };

    public SpinosaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "spinosaurus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        int variant =
                renderState instanceof SpinosaurusRenderer.SpinosaurusRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
