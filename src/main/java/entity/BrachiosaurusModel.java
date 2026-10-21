
package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class BrachiosaurusModel
        extends DefaultedEntityGeoModel<BrachiosaurusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/brachiosaurus.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/brachiosaurus_brown.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/brachiosaurus_ash.png")
    };

    public BrachiosaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID, "brachiosaurus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        int variant = renderState instanceof
                BrachiosaurusRenderer.BrachiosaurusRenderState state
                ? state.variant : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
