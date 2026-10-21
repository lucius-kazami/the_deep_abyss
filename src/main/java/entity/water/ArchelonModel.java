
package com.akashi.abyss.abyss.entity.water;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;

import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ArchelonModel
        extends DefaultedEntityGeoModel<ArchelonEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/archelon_0.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/archelon_1.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/archelon_2.png"
            )
    };

    public ArchelonModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "archelon"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant =
                state instanceof ArchelonRenderer.ArchelonRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
