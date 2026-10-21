package com.akashi.abyss.abyss.client;

import com.akashi.abyss.abyss.Abyss;
import com.akashi.abyss.abyss.block.EternalFrostSwordPedestalBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import org.jetbrains.annotations.Nullable;

public class EternalFrostSwordPedestalRenderer
        implements BlockEntityRenderer<
        EternalFrostSwordPedestalBlockEntity,
        EternalFrostSwordPedestalRenderState
        > {

    public EternalFrostSwordPedestalRenderer(
            BlockEntityRendererProvider.Context context
    ) {
    }

    @Override
    public EternalFrostSwordPedestalRenderState createRenderState() {
        return new EternalFrostSwordPedestalRenderState();
    }

    @Override
    public void extractRenderState(
            EternalFrostSwordPedestalBlockEntity blockEntity,
            EternalFrostSwordPedestalRenderState state,
            float partialTick,
            Vec3 cameraPos,
            @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {

        BlockEntityRenderer.super.extractRenderState(
                blockEntity,
                state,
                partialTick,
                cameraPos,
                crumblingOverlay
        );

        ItemStack sword =
                new ItemStack(
                        Abyss.ETERNAL_FROST_SWORD.get()
                );

        Minecraft.getInstance()
                .getItemModelResolver()
                .updateForTopItem(
                        state.sword,
                        sword,
                        ItemDisplayContext.FIXED,
                        blockEntity.getLevel(),
                        null,
                        0
                );
    }

    @Override
    public void submit(
            EternalFrostSwordPedestalRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {

        if (state.sword.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        poseStack.translate(
                0.5D,
                1.00D,
                0.5D
        );

// Ставим меч вертикально:
// рукоять сверху, клинок вниз
        poseStack.mulPose(
                new Quaternionf()
                        .rotationZ(
                                (float) Math.toRadians(135.0F)
                        )
        );

        poseStack.scale(
                1.8F,
                1.8F,
                1.8F
        );

        state.sword.submit(
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                0
        );

        poseStack.popPose();
    }
}