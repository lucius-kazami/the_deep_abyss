
package com.akashi.abyss.abyss.entity;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class TyrannosaurusModel
        extends DefaultedEntityGeoModel<TyrannosaurusEntity> {

    private static final ResourceLocation[] TEXTURES = {
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/tyrannosaurus.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/tyrannosaurus_brown.png"
            ),
            ResourceLocation.fromNamespaceAndPath(
                    Abyss.MODID,
                    "textures/entity/tyrannosaurus_dark.png"
            )
    };

    public TyrannosaurusModel() {
        super(ResourceLocation.fromNamespaceAndPath(
                Abyss.MODID,
                "tyrannosaurus"
        ));
    }

    @Override
    public ResourceLocation getTextureResource(GeoRenderState renderState) {
        int variant =
                renderState instanceof TyrannosaurusRenderer.TyrannosaurusRenderState s
                        ? s.variant
                        : 0;

        return TEXTURES[Math.floorMod(variant, TEXTURES.length)];
    }
}
