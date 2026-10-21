package com.akashi.abyss.abyss.entity.water;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class OrthocerasModel
        extends DefaultedEntityGeoModel<OrthocerasEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/orthoceras_0.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/orthoceras_1.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/orthoceras_2.png"
            )
    };

    public OrthocerasModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "orthoceras"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant =
                state instanceof OrthocerasRenderer.OrthocerasRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
