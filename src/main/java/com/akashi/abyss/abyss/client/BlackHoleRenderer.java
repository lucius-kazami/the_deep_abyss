package com.akashi.abyss.abyss.client;

import com.akashi.abyss.abyss.entity.BlackHoleEntity;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Quaternionf;

public class BlackHoleRenderer
        extends EntityRenderer<BlackHoleEntity, BlackHoleRenderState> {

    // ============================================================
    // TEXTURES
    // ============================================================

    private static final ResourceLocation NORMAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "abyss",
                    "textures/entity/black_hole_normal.png"
            );

    private static final ResourceLocation SUPER_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "abyss",
                    "textures/entity/black_hole_super.png"
            );

    private static final ResourceLocation CORE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    "abyss",
                    "textures/entity/black_hole_core.png"
            );


    // ============================================================
    // CONSTRUCTOR
    // ============================================================

    public BlackHoleRenderer(EntityRendererProvider.Context context) {
        super(context);
    }


    // ============================================================
    // CREATE STATE
    // ============================================================

    @Override
    public BlackHoleRenderState createRenderState() {
        return new BlackHoleRenderState();
    }


    // ============================================================
    // ENTITY -> STATE
    // ============================================================

    @Override
    public void extractRenderState(
            BlackHoleEntity entity,
            BlackHoleRenderState state,
            float partialTick
    ) {

        super.extractRenderState(
                entity,
                state,
                partialTick
        );

        state.superBlackHole =
                entity.isSuperBlackHole();

        float age =
                entity.getBlackHoleLife()
                        + partialTick;

        state.scale =
                entity.getVisualScale();

        // Основное вращение
        state.rotation =
                age * (
                        state.superBlackHole
                                ? 1.8F
                                : 2.8F
                );

        // Вращение в другую сторону
        state.secondRotation =
                -age * (
                        state.superBlackHole
                                ? 3.0F
                                : 4.5F
                );

        // Пульсация
        state.pulse =
                1.0F
                        + (float) Math.sin(age * 0.15F)
                        * (
                        state.superBlackHole
                                ? 0.08F
                                : 0.05F
                );

        state.secondLayerAlpha =
                state.superBlackHole
                        ? 180
                        : 145;
    }


    // ============================================================
    // MAIN RENDER
    // ============================================================

    @Override
    public void submit(
            BlackHoleRenderState state,
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


        // ========================================================
        // TEXTURE
        // ========================================================

        ResourceLocation texture =
                state.superBlackHole
                        ? SUPER_TEXTURE
                        : NORMAL_TEXTURE;


        RenderType renderType =
                RenderType.entityTranslucent(
                        texture
                );


        RenderType coreRenderType =
                RenderType.entityTranslucent(
                        CORE_TEXTURE
                );


        // ========================================================
        // ОБЩИЙ РАЗМЕР
        // ========================================================

        float scale =
                state.scale
                        * state.pulse;


        // ========================================================
        // РАЗМЕР 3D-ЯДРА
        // ========================================================

        float coreRadius =
                state.superBlackHole
                        ? scale * 0.34F
                        : scale * 0.30F;


        // ========================================================
        // 3D-ЯДРО
        // ========================================================

        submitSphere(
                poseStack,
                collector,
                coreRenderType,
                coreRadius,
                255
        );


        // ========================================================
        // ГОРИЗОНТ СОБЫТИЙ
        // ========================================================

        submitEventHorizon(
                poseStack,
                collector,
                renderType,
                coreRadius,
                state.rotation,
                state.superBlackHole
        );


        // ========================================================
        // КУСКИ МАТЕРИИ НА ОРБИТЕ
        // ========================================================

        int matterCount =
                state.superBlackHole
                        ? 14
                        : 7;


        float matterOrbit =
                state.superBlackHole
                        ? coreRadius * 3.5F
                        : coreRadius * 2.8F;


        for (int i = 0; i < matterCount; i++) {

            float angle =
                    (float) Math.toRadians(
                            state.rotation * 1.4F
                                    + i * (360.0F / matterCount)
                    );


            float radiusVariation =
                    0.82F
                            + 0.18F
                            * (float) Math.sin(
                            i * 2.37F
                    );


            float radius =
                    matterOrbit
                            * radiusVariation;


            float x =
                    (float) Math.cos(angle)
                            * radius;


            float z =
                    (float) Math.sin(angle)
                            * radius;


            float y =
                    (float) Math.sin(
                            angle * 2.0F
                                    + i
                    )
                            * coreRadius
                            * 0.55F;


            float chunkSize =
                    coreRadius
                            * (
                            0.09F
                                    + (i % 4) * 0.025F
                    );


            submitMatterChunk(
                    poseStack,
                    collector,
                    renderType,

                    x,
                    y,
                    z,

                    chunkSize,

                    angle * 2.0F,

                    state.superBlackHole
                            ? 220
                            : 180
            );
        }
        // ========================================================
// НАСТОЯЩИЙ АККРЕЦИОННЫЙ ДИСК
// ========================================================

        float coreSize =
                state.superBlackHole
                        ? scale * 0.34F
                        : scale * 0.30F;


// Главное кольцо
        submitAccretionRing(
                poseStack,
                collector,
                renderType,

                coreSize * 1.20F,
                coreSize * 2.50F,

                state.rotation,

                68.0F,
                0.0F,

                state.superBlackHole
                        ? 230
                        : 210
        );


// Второе кольцо
        submitAccretionRing(
                poseStack,
                collector,
                renderType,

                coreSize * 1.35F,
                coreSize * 2.15F,

                state.secondRotation,

                -45.0F,
                35.0F,

                state.superBlackHole
                        ? 190
                        : 150
        );

        if (state.superBlackHole) {

            // ====================================================
            // ТРЕТЬЯ ОРБИТА
            // ====================================================

            submitAccretionRing(
                    poseStack,
                    collector,
                    renderType,

                    coreSize * 1.55F,
                    coreSize * 3.00F,

                    -state.rotation * 0.65F,

                    25.0F,
                    70.0F,

                    145
            );


            // ====================================================
            // ДАЛЬНЯЯ ОРБИТА
            // ====================================================

            submitAccretionRing(
                    poseStack,
                    collector,
                    renderType,

                    coreSize * 2.20F,
                    coreSize * 3.65F,

                    state.secondRotation * 0.40F,

                    80.0F,
                    -30.0F,

                    100
            );
        }
        // ========================================================
// ОБЪЁМНЫЙ АККРЕЦИОННЫЙ ДИСК
// ========================================================

        submitThickRing(
                poseStack,
                collector,
                renderType,

                coreSize * 1.72F,   // радиус орбиты
                coreSize * 0.20F,   // толщина

                -state.rotation * 1.35F,

                68.0F,
                0.0F,

                state.superBlackHole
                        ? 220
                        : 195
        );

        // ========================================================
        // 1. ЦЕНТРАЛЬНЫЙ ОБЪЁМ
        //
        // Несколько плоскостей пересекаются в центре.
        // Поэтому изображение больше не исчезает при просмотре сбоку.
        // ========================================================

        submitLayer(
                poseStack,
                collector,
                renderType,

                scale,

                state.rotation,

                0.0F,
                0.0F,

                255
        );


        submitLayer(
                poseStack,
                collector,
                renderType,

                scale * 0.98F,

                -state.rotation,

                0.0F,
                90.0F,

                245
        );


        submitLayer(
                poseStack,
                collector,
                renderType,

                scale * 0.94F,

                state.secondRotation,

                90.0F,
                0.0F,

                220
        );


        // ========================================================
        // 2. НАКЛОНЁННОЕ АККРЕЦИОННОЕ КОЛЬЦО
        // ========================================================

        submitLayer(
                poseStack,
                collector,
                renderType,

                scale * 1.15F,

                state.secondRotation,

                65.0F,
                25.0F,

                state.secondLayerAlpha
        );


        // ========================================================
        // 3. ВТОРОЕ НАКЛОНЁННОЕ КОЛЬЦО
        // ========================================================

        submitLayer(
                poseStack,
                collector,
                renderType,

                scale * 1.08F,

                -state.secondRotation,

                -55.0F,
                -35.0F,

                state.superBlackHole
                        ? 155
                        : 120
        );


        // ========================================================
        // SUPER BLACK HOLE
        //
        // Большая сингулярность получает ещё несколько слоёв.
        // ========================================================

        if (state.superBlackHole) {

            // Большое внешнее кольцо

            submitLayer(
                    poseStack,
                    collector,
                    renderType,

                    scale * 1.32F,

                    state.rotation * 0.55F,

                    75.0F,
                    45.0F,

                    120
            );

            // ====================================================
// ОБЪЁМНАЯ ОРБИТА №2
// ====================================================

            submitThickRing(
                    poseStack,
                    collector,
                    renderType,

                    coreSize * 2.25F,
                    coreSize * 0.13F,

                    state.secondRotation * 0.75F,

                    -40.0F,
                    38.0F,

                    155
            );


// ====================================================
// ОБЪЁМНАЯ ОРБИТА №3
// ====================================================

            submitThickRing(
                    poseStack,
                    collector,
                    renderType,

                    coreSize * 2.75F,
                    coreSize * 0.09F,

                    -state.rotation * 0.45F,

                    35.0F,
                    72.0F,

                    115
            );


            // Вертикальное кольцо

            submitLayer(
                    poseStack,
                    collector,
                    renderType,

                    scale * 1.20F,

                    -state.rotation * 0.7F,

                    0.0F,
                    90.0F,

                    115
            );


            // Диагональное кольцо

            submitLayer(
                    poseStack,
                    collector,
                    renderType,

                    scale * 1.26F,

                    state.secondRotation * 0.6F,

                    45.0F,
                    45.0F,

                    105
            );
        }
    }


    // ============================================================
    // ОДИН 3D-СЛОЙ
    // ============================================================

    private void submitLayer(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType renderType,

            float scale,

            float rotationZ,

            float rotationX,
            float rotationY,

            int alpha
    ) {

        poseStack.pushPose();


        // ========================================================
        // НАКЛОН ПО X
        // ========================================================

        poseStack.mulPose(
                new Quaternionf()
                        .rotationX(
                                (float) Math.toRadians(rotationX)
                        )
        );


        // ========================================================
        // НАКЛОН ПО Y
        // ========================================================

        poseStack.mulPose(
                new Quaternionf()
                        .rotationY(
                                (float) Math.toRadians(rotationY)
                        )
        );


        // ========================================================
        // АНИМАЦИЯ ВРАЩЕНИЯ
        // ========================================================

        poseStack.mulPose(
                new Quaternionf()
                        .rotationZ(
                                (float) Math.toRadians(rotationZ)
                        )
        );


        // ========================================================
        // SCALE
        // ========================================================

        poseStack.scale(
                scale,
                scale,
                scale
        );


        // ========================================================
        // GEOMETRY
        // ========================================================

        collector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {

                    float min = -0.5F;
                    float max = 0.5F;

                    // Максимальная яркость
                    int fullBright =
                            0x00F000F0;


                    // =================================================
                    // FRONT
                    // =================================================

                    buffer.addVertex(
                                    pose,
                                    min,
                                    min,
                                    0.0F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    0.0F,
                                    1.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    1.0F
                            );


                    buffer.addVertex(
                                    pose,
                                    max,
                                    min,
                                    0.0F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    1.0F,
                                    1.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    1.0F
                            );


                    buffer.addVertex(
                                    pose,
                                    max,
                                    max,
                                    0.0F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    1.0F,
                                    0.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    1.0F
                            );


                    buffer.addVertex(
                                    pose,
                                    min,
                                    max,
                                    0.0F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    0.0F,
                                    0.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    1.0F
                            );


                    // =================================================
                    // BACK
                    //
                    // Рисуем обратную сторону тоже.
                    // Поэтому чёрная дыра видна с обеих сторон.
                    // =================================================

                    buffer.addVertex(
                                    pose,
                                    min,
                                    max,
                                    -0.002F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    0.0F,
                                    0.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    -1.0F
                            );


                    buffer.addVertex(
                                    pose,
                                    max,
                                    max,
                                    -0.002F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    1.0F,
                                    0.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    -1.0F
                            );


                    buffer.addVertex(
                                    pose,
                                    max,
                                    min,
                                    -0.002F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    1.0F,
                                    1.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    -1.0F
                            );


                    buffer.addVertex(
                                    pose,
                                    min,
                                    min,
                                    -0.002F
                            )
                            .setColor(
                                    255,
                                    255,
                                    255,
                                    alpha
                            )
                            .setUv(
                                    0.0F,
                                    1.0F
                            )
                            .setOverlay(
                                    OverlayTexture.NO_OVERLAY
                            )
                            .setLight(
                                    fullBright
                            )
                            .setNormal(
                                    pose,
                                    0.0F,
                                    0.0F,
                                    -1.0F
                            );
                }

        );

        poseStack.popPose();
    }
    // ============================================================
// GLOW SPHERE
// Светящаяся фиолетовая оболочка вокруг чёрной дыры
// ============================================================

    private void submitGlowSphere(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType renderType,
            float radius,
            int alpha
    ) {

        final int latitudeSegments = 12;
        final int longitudeSegments = 20;

        collector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {

                    for (int lat = 0; lat < latitudeSegments; lat++) {

                        float theta1 =
                                (float) Math.PI
                                        * lat
                                        / latitudeSegments;

                        float theta2 =
                                (float) Math.PI
                                        * (lat + 1)
                                        / latitudeSegments;


                        for (int lon = 0; lon < longitudeSegments; lon++) {

                            float phi1 =
                                    (float) (Math.PI * 2.0)
                                            * lon
                                            / longitudeSegments;

                            float phi2 =
                                    (float) (Math.PI * 2.0)
                                            * (lon + 1)
                                            / longitudeSegments;


                            // -----------------------------------------
                            // VERTEX 1
                            // -----------------------------------------

                            float x1 =
                                    radius
                                            * (float) Math.sin(theta1)
                                            * (float) Math.cos(phi1);

                            float y1 =
                                    radius
                                            * (float) Math.cos(theta1);

                            float z1 =
                                    radius
                                            * (float) Math.sin(theta1)
                                            * (float) Math.sin(phi1);


                            // -----------------------------------------
                            // VERTEX 2
                            // -----------------------------------------

                            float x2 =
                                    radius
                                            * (float) Math.sin(theta2)
                                            * (float) Math.cos(phi1);

                            float y2 =
                                    radius
                                            * (float) Math.cos(theta2);

                            float z2 =
                                    radius
                                            * (float) Math.sin(theta2)
                                            * (float) Math.sin(phi1);


                            // -----------------------------------------
                            // VERTEX 3
                            // -----------------------------------------

                            float x3 =
                                    radius
                                            * (float) Math.sin(theta2)
                                            * (float) Math.cos(phi2);

                            float y3 =
                                    radius
                                            * (float) Math.cos(theta2);

                            float z3 =
                                    radius
                                            * (float) Math.sin(theta2)
                                            * (float) Math.sin(phi2);


                            // -----------------------------------------
                            // VERTEX 4
                            // -----------------------------------------

                            float x4 =
                                    radius
                                            * (float) Math.sin(theta1)
                                            * (float) Math.cos(phi2);

                            float y4 =
                                    radius
                                            * (float) Math.cos(theta1);

                            float z4 =
                                    radius
                                            * (float) Math.sin(theta1)
                                            * (float) Math.sin(phi2);


                            // -----------------------------------------
                            // ФИОЛЕТОВОЕ СВЕЧЕНИЕ
                            // -----------------------------------------

                            int red = 125;
                            int green = 35;
                            int blue = 255;


                            buffer.addVertex(
                                            pose,
                                            x1,
                                            y1,
                                            z1
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(0.0F, 0.0F)
                                    .setOverlay(OverlayTexture.NO_OVERLAY)
                                    .setLight(0x00F000F0)
                                    .setNormal(
                                            pose,
                                            x1 / radius,
                                            y1 / radius,
                                            z1 / radius
                                    );


                            buffer.addVertex(
                                            pose,
                                            x2,
                                            y2,
                                            z2
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(0.0F, 1.0F)
                                    .setOverlay(OverlayTexture.NO_OVERLAY)
                                    .setLight(0x00F000F0)
                                    .setNormal(
                                            pose,
                                            x2 / radius,
                                            y2 / radius,
                                            z2 / radius
                                    );


                            buffer.addVertex(
                                            pose,
                                            x3,
                                            y3,
                                            z3
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(1.0F, 1.0F)
                                    .setOverlay(OverlayTexture.NO_OVERLAY)
                                    .setLight(0x00F000F0)
                                    .setNormal(
                                            pose,
                                            x3 / radius,
                                            y3 / radius,
                                            z3 / radius
                                    );


                            buffer.addVertex(
                                            pose,
                                            x4,
                                            y4,
                                            z4
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(1.0F, 0.0F)
                                    .setOverlay(OverlayTexture.NO_OVERLAY)
                                    .setLight(0x00F000F0)
                                    .setNormal(
                                            pose,
                                            x4 / radius,
                                            y4 / radius,
                                            z4 / radius
                                    );
                        }
                    }
                }
        );
    }
    private void submitSphere(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType renderType,
            float radius,
            int alpha
    ) {

        poseStack.pushPose();

        collector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {

                    int latitudeSegments = 16;
                    int longitudeSegments = 24;

                    int fullBright = 0x00F000F0;

                    for (int lat = 0; lat < latitudeSegments; lat++) {

                        float theta1 =
                                (float) Math.PI
                                        * lat
                                        / latitudeSegments;

                        float theta2 =
                                (float) Math.PI
                                        * (lat + 1)
                                        / latitudeSegments;


                        for (int lon = 0; lon < longitudeSegments; lon++) {

                            float phi1 =
                                    (float) (Math.PI * 2.0)
                                            * lon
                                            / longitudeSegments;

                            float phi2 =
                                    (float) (Math.PI * 2.0)
                                            * (lon + 1)
                                            / longitudeSegments;


                            // ============================================
                            // ТОЧКА 1
                            // ============================================

                            float x1 =
                                    (float) (
                                            Math.sin(theta1)
                                                    * Math.cos(phi1)
                                    );

                            float y1 =
                                    (float) Math.cos(theta1);

                            float z1 =
                                    (float) (
                                            Math.sin(theta1)
                                                    * Math.sin(phi1)
                                    );


                            // ============================================
                            // ТОЧКА 2
                            // ============================================

                            float x2 =
                                    (float) (
                                            Math.sin(theta2)
                                                    * Math.cos(phi1)
                                    );

                            float y2 =
                                    (float) Math.cos(theta2);

                            float z2 =
                                    (float) (
                                            Math.sin(theta2)
                                                    * Math.sin(phi1)
                                    );


                            // ============================================
                            // ТОЧКА 3
                            // ============================================

                            float x3 =
                                    (float) (
                                            Math.sin(theta2)
                                                    * Math.cos(phi2)
                                    );

                            float y3 =
                                    (float) Math.cos(theta2);

                            float z3 =
                                    (float) (
                                            Math.sin(theta2)
                                                    * Math.sin(phi2)
                                    );


                            // ============================================
                            // ТОЧКА 4
                            // ============================================

                            float x4 =
                                    (float) (
                                            Math.sin(theta1)
                                                    * Math.cos(phi2)
                                    );

                            float y4 =
                                    (float) Math.cos(theta1);

                            float z4 =
                                    (float) (
                                            Math.sin(theta1)
                                                    * Math.sin(phi2)
                                    );


                            // ============================================
                            // UV
                            // ============================================

                            float u1 =
                                    (float) lon
                                            / longitudeSegments;

                            float u2 =
                                    (float) (lon + 1)
                                            / longitudeSegments;

                            float v1 =
                                    (float) lat
                                            / latitudeSegments;

                            float v2 =
                                    (float) (lat + 1)
                                            / latitudeSegments;


                            // ============================================
                            // VERTEX 1
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x1 * radius,
                                            y1 * radius,
                                            z1 * radius
                                    )
                                    .setColor(
                                            255,
                                            255,
                                            255,
                                            alpha
                                    )
                                    .setUv(u1, v1)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            x1,
                                            y1,
                                            z1
                                    );


                            // ============================================
                            // VERTEX 2
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x2 * radius,
                                            y2 * radius,
                                            z2 * radius
                                    )
                                    .setColor(
                                            255,
                                            255,
                                            255,
                                            alpha
                                    )
                                    .setUv(u1, v2)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            x2,
                                            y2,
                                            z2
                                    );


                            // ============================================
                            // VERTEX 3
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x3 * radius,
                                            y3 * radius,
                                            z3 * radius
                                    )
                                    .setColor(
                                            255,
                                            255,
                                            255,
                                            alpha
                                    )
                                    .setUv(u2, v2)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            x3,
                                            y3,
                                            z3
                                    );


                            // ============================================
                            // VERTEX 4
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x4 * radius,
                                            y4 * radius,
                                            z4 * radius
                                    )
                                    .setColor(
                                            255,
                                            255,
                                            255,
                                            alpha
                                    )
                                    .setUv(u2, v1)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            x4,
                                            y4,
                                            z4
                                    );
                        }
                    }
                }
        );

        poseStack.popPose();
    }
    private void submitAccretionRing(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType renderType,

            float innerRadius,
            float outerRadius,

            float rotation,
            float rotationX,
            float rotationY,

            int alpha
    ) {

        poseStack.pushPose();

        // Наклон кольца
        poseStack.mulPose(
                new Quaternionf().rotationX(
                        (float) Math.toRadians(rotationX)
                )
        );

        poseStack.mulPose(
                new Quaternionf().rotationY(
                        (float) Math.toRadians(rotationY)
                )
        );

        // Постоянное вращение
        poseStack.mulPose(
                new Quaternionf().rotationZ(
                        (float) Math.toRadians(rotation)
                )
        );


        collector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {

                    // Чем больше число — тем круглее кольцо.
                    int segments = 64;

                    int fullBright = 0x00F000F0;

                    for (int i = 0; i < segments; i++) {

                        float angle1 =
                                (float) (Math.PI * 2.0)
                                        * i
                                        / segments;

                        float angle2 =
                                (float) (Math.PI * 2.0)
                                        * (i + 1)
                                        / segments;


                        // ============================================
                        // INNER
                        // ============================================

                        float innerX1 =
                                (float) Math.cos(angle1)
                                        * innerRadius;

                        float innerY1 =
                                (float) Math.sin(angle1)
                                        * innerRadius;


                        float innerX2 =
                                (float) Math.cos(angle2)
                                        * innerRadius;

                        float innerY2 =
                                (float) Math.sin(angle2)
                                        * innerRadius;


                        // ============================================
                        // OUTER
                        // ============================================

                        float outerX1 =
                                (float) Math.cos(angle1)
                                        * outerRadius;

                        float outerY1 =
                                (float) Math.sin(angle1)
                                        * outerRadius;


                        float outerX2 =
                                (float) Math.cos(angle2)
                                        * outerRadius;

                        float outerY2 =
                                (float) Math.sin(angle2)
                                        * outerRadius;


                        // ============================================
                        // UV
                        // ============================================

                        float u1 =
                                (float) i
                                        / segments;

                        float u2 =
                                (float) (i + 1)
                                        / segments;


                        // ============================================
                        // QUAD
                        // ============================================

                        buffer.addVertex(
                                        pose,
                                        innerX1,
                                        innerY1,
                                        0.0F
                                )
                                .setColor(
                                        210,
                                        120,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u1,
                                        0.25F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        1.0F
                                );


                        buffer.addVertex(
                                        pose,
                                        outerX1,
                                        outerY1,
                                        0.0F
                                )
                                .setColor(
                                        150,
                                        40,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u1,
                                        0.0F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        1.0F
                                );


                        buffer.addVertex(
                                        pose,
                                        outerX2,
                                        outerY2,
                                        0.0F
                                )
                                .setColor(
                                        150,
                                        40,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u2,
                                        0.0F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        1.0F
                                );


                        buffer.addVertex(
                                        pose,
                                        innerX2,
                                        innerY2,
                                        0.0F
                                )
                                .setColor(
                                        210,
                                        120,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u2,
                                        0.25F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        1.0F
                                );


                        // ============================================
                        // ОБРАТНАЯ СТОРОНА
                        // ============================================

                        buffer.addVertex(
                                        pose,
                                        innerX2,
                                        innerY2,
                                        -0.003F
                                )
                                .setColor(
                                        210,
                                        120,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u2,
                                        0.25F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        -1.0F
                                );


                        buffer.addVertex(
                                        pose,
                                        outerX2,
                                        outerY2,
                                        -0.003F
                                )
                                .setColor(
                                        150,
                                        40,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u2,
                                        0.0F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        -1.0F
                                );


                        buffer.addVertex(
                                        pose,
                                        outerX1,
                                        outerY1,
                                        -0.003F
                                )
                                .setColor(
                                        150,
                                        40,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u1,
                                        0.0F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        -1.0F
                                );


                        buffer.addVertex(
                                        pose,
                                        innerX1,
                                        innerY1,
                                        -0.003F
                                )
                                .setColor(
                                        210,
                                        120,
                                        255,
                                        alpha
                                )
                                .setUv(
                                        u1,
                                        0.25F
                                )
                                .setOverlay(
                                        OverlayTexture.NO_OVERLAY
                                )
                                .setLight(fullBright)
                                .setNormal(
                                        pose,
                                        0.0F,
                                        0.0F,
                                        -1.0F
                                );
                    }
                }
        );

        poseStack.popPose();
    }
    private void submitThickRing(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType renderType,

            float ringRadius,
            float thickness,

            float rotation,
            float rotationX,
            float rotationY,

            int alpha
    ) {

        poseStack.pushPose();

        // ============================================================
        // НАКЛОН ВСЕГО ТОРА
        // ============================================================

        poseStack.mulPose(
                new Quaternionf().rotationX(
                        (float) Math.toRadians(rotationX)
                )
        );

        poseStack.mulPose(
                new Quaternionf().rotationY(
                        (float) Math.toRadians(rotationY)
                )
        );

        // Вращение вокруг центра
        poseStack.mulPose(
                new Quaternionf().rotationZ(
                        (float) Math.toRadians(rotation)
                )
        );


        collector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {

                    // Количество сегментов по большой окружности.
                    int majorSegments = 48;

                    // Количество сегментов толщины кольца.
                    int minorSegments = 10;

                    int fullBright = 0x00F000F0;


                    for (int i = 0; i < majorSegments; i++) {

                        float major1 =
                                (float) (Math.PI * 2.0)
                                        * i
                                        / majorSegments;

                        float major2 =
                                (float) (Math.PI * 2.0)
                                        * (i + 1)
                                        / majorSegments;


                        for (int j = 0; j < minorSegments; j++) {

                            float minor1 =
                                    (float) (Math.PI * 2.0)
                                            * j
                                            / minorSegments;

                            float minor2 =
                                    (float) (Math.PI * 2.0)
                                            * (j + 1)
                                            / minorSegments;


                            // ============================================
                            // VERTEX 1
                            // ============================================

                            float radial1 =
                                    ringRadius
                                            + thickness
                                            * (float) Math.cos(minor1);

                            float x1 =
                                    radial1
                                            * (float) Math.cos(major1);

                            float y1 =
                                    radial1
                                            * (float) Math.sin(major1);

                            float z1 =
                                    thickness
                                            * (float) Math.sin(minor1);


                            // ============================================
                            // VERTEX 2
                            // ============================================

                            float radial2 =
                                    ringRadius
                                            + thickness
                                            * (float) Math.cos(minor1);

                            float x2 =
                                    radial2
                                            * (float) Math.cos(major2);

                            float y2 =
                                    radial2
                                            * (float) Math.sin(major2);

                            float z2 =
                                    thickness
                                            * (float) Math.sin(minor1);


                            // ============================================
                            // VERTEX 3
                            // ============================================

                            float radial3 =
                                    ringRadius
                                            + thickness
                                            * (float) Math.cos(minor2);

                            float x3 =
                                    radial3
                                            * (float) Math.cos(major2);

                            float y3 =
                                    radial3
                                            * (float) Math.sin(major2);

                            float z3 =
                                    thickness
                                            * (float) Math.sin(minor2);


                            // ============================================
                            // VERTEX 4
                            // ============================================

                            float radial4 =
                                    ringRadius
                                            + thickness
                                            * (float) Math.cos(minor2);

                            float x4 =
                                    radial4
                                            * (float) Math.cos(major1);

                            float y4 =
                                    radial4
                                            * (float) Math.sin(major1);

                            float z4 =
                                    thickness
                                            * (float) Math.sin(minor2);


                            // ============================================
                            // NORMALS
                            // ============================================

                            float nx1 =
                                    (float) Math.cos(major1)
                                            * (float) Math.cos(minor1);

                            float ny1 =
                                    (float) Math.sin(major1)
                                            * (float) Math.cos(minor1);

                            float nz1 =
                                    (float) Math.sin(minor1);


                            float nx2 =
                                    (float) Math.cos(major2)
                                            * (float) Math.cos(minor1);

                            float ny2 =
                                    (float) Math.sin(major2)
                                            * (float) Math.cos(minor1);

                            float nz2 =
                                    (float) Math.sin(minor1);


                            float nx3 =
                                    (float) Math.cos(major2)
                                            * (float) Math.cos(minor2);

                            float ny3 =
                                    (float) Math.sin(major2)
                                            * (float) Math.cos(minor2);

                            float nz3 =
                                    (float) Math.sin(minor2);


                            float nx4 =
                                    (float) Math.cos(major1)
                                            * (float) Math.cos(minor2);

                            float ny4 =
                                    (float) Math.sin(major1)
                                            * (float) Math.cos(minor2);

                            float nz4 =
                                    (float) Math.sin(minor2);


                            // ============================================
                            // UV
                            // ============================================

                            float u1 =
                                    i / (float) majorSegments;

                            float u2 =
                                    (i + 1) / (float) majorSegments;

                            float v1 =
                                    j / (float) minorSegments;

                            float v2 =
                                    (j + 1) / (float) minorSegments;


                            // ============================================
                            // ЦВЕТ
                            //
                            // Внутренняя часть тора немного ярче.
                            // ============================================

                            float energy =
                                    0.5F
                                            + 0.5F
                                            * (float) Math.sin(
                                            major1 * 5.0F
                                                    + minor1 * 2.0F
                                    );

                            int red =
                                    (int) (
                                            145
                                                    + 80 * energy
                                    );

                            int green =
                                    (int) (
                                            30
                                                    + 55 * energy
                                    );

                            int blue = 255;


                            // ============================================
                            // VERTEX 1
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x1,
                                            y1,
                                            z1
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(u1, v1)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            nx1,
                                            ny1,
                                            nz1
                                    );


                            // ============================================
                            // VERTEX 2
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x2,
                                            y2,
                                            z2
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(u2, v1)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            nx2,
                                            ny2,
                                            nz2
                                    );


                            // ============================================
                            // VERTEX 3
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x3,
                                            y3,
                                            z3
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(u2, v2)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            nx3,
                                            ny3,
                                            nz3
                                    );


                            // ============================================
                            // VERTEX 4
                            // ============================================

                            buffer.addVertex(
                                            pose,
                                            x4,
                                            y4,
                                            z4
                                    )
                                    .setColor(
                                            red,
                                            green,
                                            blue,
                                            alpha
                                    )
                                    .setUv(u1, v2)
                                    .setOverlay(
                                            OverlayTexture.NO_OVERLAY
                                    )
                                    .setLight(fullBright)
                                    .setNormal(
                                            pose,
                                            nx4,
                                            ny4,
                                            nz4
                                    );
                        }
                    }
                }
        );

        poseStack.popPose();
    }
    private void submitEventHorizon(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType renderType,

            float coreRadius,
            float rotation,
            boolean superHole
    ) {

        // ============================================================
        // ВНУТРЕННЕЕ ЯРКОЕ КОЛЬЦО
        // ============================================================

        submitThickRing(
                poseStack,
                collector,
                renderType,

                coreRadius * 1.08F,

                coreRadius * 0.055F,

                rotation * 1.7F,

                90.0F,
                0.0F,

                superHole
                        ? 255
                        : 235
        );


        // ============================================================
        // ВТОРОЙ СЛОЙ СВЕЧЕНИЯ
        // ============================================================

        submitThickRing(
                poseStack,
                collector,
                renderType,

                coreRadius * 1.14F,

                coreRadius * 0.035F,

                -rotation * 1.15F,

                90.0F,
                0.0F,

                superHole
                        ? 180
                        : 140
        );
    }
    private void submitMatterChunk(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            RenderType renderType,

            float x,
            float y,
            float z,

            float size,

            float rotation,

            int alpha
    ) {

        poseStack.pushPose();

        poseStack.translate(
                x,
                y,
                z
        );

        poseStack.mulPose(
                new Quaternionf()
                        .rotationXYZ(
                                rotation,
                                rotation * 0.7F,
                                rotation * 1.3F
                        )
        );

        poseStack.scale(
                size,
                size,
                size
        );


        collector.submitCustomGeometry(
                poseStack,
                renderType,
                (pose, buffer) -> {

                    float min = -0.5F;
                    float max = 0.5F;

                    int light = 0x00F000F0;


                    // FRONT
                    addMatterFace(
                            pose,
                            buffer,

                            min, min, max,
                            max, min, max,
                            max, max, max,
                            min, max, max,

                            0.0F,
                            0.0F,
                            1.0F,

                            light,
                            alpha
                    );


                    // BACK
                    addMatterFace(
                            pose,
                            buffer,

                            max, min, min,
                            min, min, min,
                            min, max, min,
                            max, max, min,

                            0.0F,
                            0.0F,
                            -1.0F,

                            light,
                            alpha
                    );


                    // LEFT
                    addMatterFace(
                            pose,
                            buffer,

                            min, min, min,
                            min, min, max,
                            min, max, max,
                            min, max, min,

                            -1.0F,
                            0.0F,
                            0.0F,

                            light,
                            alpha
                    );


                    // RIGHT
                    addMatterFace(
                            pose,
                            buffer,

                            max, min, max,
                            max, min, min,
                            max, max, min,
                            max, max, max,

                            1.0F,
                            0.0F,
                            0.0F,

                            light,
                            alpha
                    );


                    // TOP
                    addMatterFace(
                            pose,
                            buffer,

                            min, max, max,
                            max, max, max,
                            max, max, min,
                            min, max, min,

                            0.0F,
                            1.0F,
                            0.0F,

                            light,
                            alpha
                    );


                    // BOTTOM
                    addMatterFace(
                            pose,
                            buffer,

                            min, min, min,
                            max, min, min,
                            max, min, max,
                            min, min, max,

                            0.0F,
                            -1.0F,
                            0.0F,

                            light,
                            alpha
                    );
                }
        );

        poseStack.popPose();
    }
    private void addMatterFace(
            PoseStack.Pose pose,
            com.mojang.blaze3d.vertex.VertexConsumer buffer,

            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3,
            float x4, float y4, float z4,

            float nx,
            float ny,
            float nz,

            int light,
            int alpha
    ) {

        buffer.addVertex(pose, x1, y1, z1)
                .setColor(180, 70, 255, alpha)
                .setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        buffer.addVertex(pose, x2, y2, z2)
                .setColor(220, 120, 255, alpha)
                .setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        buffer.addVertex(pose, x3, y3, z3)
                .setColor(150, 35, 255, alpha)
                .setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);

        buffer.addVertex(pose, x4, y4, z4)
                .setColor(190, 65, 255, alpha)
                .setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }
}