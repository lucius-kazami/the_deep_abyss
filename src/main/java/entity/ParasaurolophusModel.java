
package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ParasaurolophusModel
        extends DefaultedEntityGeoModel<ParasaurolophusEntity> {


    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/parasaurolophus_green.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/parasaurolophus_yellow.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/parasaurolophus_blue.png"),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID, "textures/entity/parasaurolophus_red.png")
    };


    public ParasaurolophusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID, "parasaurolophus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        int variant = renderState instanceof
                ParasaurolophusRenderer.ParasaurolophusRenderState s
                ? s.variant : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
