
package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class AnkylosaurusModel
        extends DefaultedEntityGeoModel<AnkylosaurusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/ankylosaurus.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/ankylosaurus_ash.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/ankylosaurus_fire.png")
    };

    public AnkylosaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID, "ankylosaurus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        int variant = renderState instanceof
                AnkylosaurusRenderer.AnkylosaurusRenderState state
                ? state.variant : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
