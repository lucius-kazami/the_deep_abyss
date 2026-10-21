package com.akashi.abyss.abyss.entity.water;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class TrilobiteModel
        extends DefaultedEntityGeoModel<TrilobiteEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/trilobite_0.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/trilobite_1.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/trilobite_2.png"
            )
    };

    public TrilobiteModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "trilobite"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant =
                state instanceof TrilobiteRenderer.TrilobiteRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
