package com.akashi.abyss.abyss.client.renderer;

import com.akashi.abyss.abyss.entity.EnergyBlastProjectile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;

import org.joml.Matrix4f;


public class EnergyBlastRenderer
        extends EntityRenderer<EnergyBlastProjectile, EntityRenderState> {

    // ============================================================
    // ТЕКСТУРА
    // ============================================================

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "abyss",
                    "textures/entity/energy_blast.png"
            );


    public EnergyBlastRenderer(
            EntityRendererProvider.Context context
    ) {
        super(context);
    }


    // ============================================================
    // CREATE RENDER STATE
    // ============================================================

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }


    // ============================================================
    // SUBMIT
    // ============================================================

    @Override
    public void submit(
            EntityRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState cameraState
    ) {

        super.submit(
                state,
                poseStack,
                collector,
                cameraState
        );


        poseStack.pushPose();


        // ========================================================
        // ПОВОРАЧИВАЕМ К КАМЕРЕ
        // ========================================================

        poseStack.mulPose(
                cameraState.orientation
        );


        // ========================================================
        // РАЗМЕР СНАРЯДА
        // ========================================================

        poseStack.scale(
                0.75F,
                0.75F,
                0.75F
        );


        // ========================================================
        // GEOMETRY
        // ========================================================

        collector.submitCustomGeometry(
                poseStack,

                RenderType.entityTranslucent(TEXTURE),

                (pose, vertexConsumer) -> {

                    drawEnergyQuad(
                            pose,
                            vertexConsumer
                    );
                }
        );


        poseStack.popPose();
    }


    // ============================================================
    // ENERGY QUAD
    // ============================================================

    private static void drawEnergyQuad(
            PoseStack.Pose pose,
            VertexConsumer consumer
    ) {

        Matrix4f matrix =
                pose.pose();


        float size = 0.5F;


        // ========================================================
        // BOTTOM LEFT
        // ========================================================

        consumer.addVertex(
                        matrix,
                        -size,
                        -size,
                        0.0F
                )
                .setColor(
                        255,
                        255,
                        255,
                        255
                )
                .setUv(
                        0.0F,
                        1.0F
                )
                .setOverlay(
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
                )
                .setLight(
                        0x00F000F0
                )
                .setNormal(
                        pose,
                        0.0F,
                        0.0F,
                        1.0F
                );


        // ========================================================
        // BOTTOM RIGHT
        // ========================================================

        consumer.addVertex(
                        matrix,
                        size,
                        -size,
                        0.0F
                )
                .setColor(
                        255,
                        255,
                        255,
                        255
                )
                .setUv(
                        1.0F,
                        1.0F
                )
                .setOverlay(
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
                )
                .setLight(
                        0x00F000F0
                )
                .setNormal(
                        pose,
                        0.0F,
                        0.0F,
                        1.0F
                );


        // ========================================================
        // TOP RIGHT
        // ========================================================

        consumer.addVertex(
                        matrix,
                        size,
                        size,
                        0.0F
                )
                .setColor(
                        255,
                        255,
                        255,
                        255
                )
                .setUv(
                        1.0F,
                        0.0F
                )
                .setOverlay(
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
                )
                .setLight(
                        0x00F000F0
                )
                .setNormal(
                        pose,
                        0.0F,
                        0.0F,
                        1.0F
                );


        // ========================================================
        // TOP LEFT
        // ========================================================

        consumer.addVertex(
                        matrix,
                        -size,
                        size,
                        0.0F
                )
                .setColor(
                        255,
                        255,
                        255,
                        255
                )
                .setUv(
                        0.0F,
                        0.0F
                )
                .setOverlay(
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY
                )
                .setLight(
                        0x00F000F0
                )
                .setNormal(
                        pose,
                        0.0F,
                        0.0F,
                        1.0F
                );
    }
}