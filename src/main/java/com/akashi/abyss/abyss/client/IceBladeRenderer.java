package com.akashi.abyss.abyss.client;

import com.akashi.abyss.abyss.entity.IceBladeProjectile;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class IceBladeRenderer
        extends ThrownItemRenderer<IceBladeProjectile> {

    public IceBladeRenderer(
            EntityRendererProvider.Context context
    ) {

        super(
                context,
                1.5F,
                true
        );
    }
}