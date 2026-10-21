package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class TherizinosaurusModel extends DefaultedEntityGeoModel<TherizinosaurusEntity> {
    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "textures/entity/therizinosaurus_0.png"),
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "textures/entity/therizinosaurus_1.png"),
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "textures/entity/therizinosaurus_2.png")
    };

    public TherizinosaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "therizinosaurus"));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState state) {
        int variant = state instanceof TherizinosaurusRenderer.TherizinosaurusRenderState s ? s.variant : 0;
        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
