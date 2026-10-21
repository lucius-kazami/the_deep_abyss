package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class TriceratopsModel extends DefaultedEntityGeoModel<TriceratopsEntity> {
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "textures/entity/triceratops.png"),
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "textures/entity/triceratops_brown.png"),
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "textures/entity/triceratops_blue.png")
    };

    public TriceratopsModel() {
        super(ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "triceratops"));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        int variant = renderState instanceof TriceratopsRenderer.TriceratopsRenderState s ? s.variant : 0;
        return TEXTURES[Math.floorMod(variant, 3)];
    }
}