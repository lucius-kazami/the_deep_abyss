package com.akashi.abyss.abyss.entity.heaven;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class QuetzalcoatlusModel
        extends DefaultedEntityGeoModel<QuetzalcoatlusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/heaven/quetzalcoatlus_0.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/heaven/quetzalcoatlus_1.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/heaven/quetzalcoatlus_2.png"
            )
    };

    public QuetzalcoatlusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "quetzalcoatlus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant =
                state instanceof QuetzalcoatlusRenderer.QuetzalcoatlusRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
