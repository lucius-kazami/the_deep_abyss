
package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class MosasaurusModel extends DefaultedEntityGeoModel<MosasaurusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/mosasaurus_0.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/mosasaurus_1.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/mosasaurus_2.png")
    };

    public MosasaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "mosasaurus"));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant = state instanceof MosasaurusRenderer.MosasaurusRenderState s
                ? s.variant
                : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
