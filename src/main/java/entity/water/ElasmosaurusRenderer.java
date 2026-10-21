package com.akashi.abyss.abyss.entity.water;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;

import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

public class ElasmosaurusRenderer extends GeoEntityRenderer<
        ElasmosaurusEntity,
        ElasmosaurusRenderer.ElasmosaurusRenderState> {

    private static final float ADULT_SCALE = 2.4F;
    private static final float BABY_SCALE = 0.35F;

    public ElasmosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new ElasmosaurusModel());
        this.withScale(ADULT_SCALE);
    }

    @Override
    public ElasmosaurusRenderState createRenderState(
            ElasmosaurusEntity entity,
            Void relatedObject
    ) {
        return new ElasmosaurusRenderState();
    }

    @Override
    public void extractRenderState(
            ElasmosaurusEntity entity,
            ElasmosaurusRenderState state,
            float partialTick
    ) {
        super.extractRenderState(entity, state, partialTick);

        state.variant = entity.getVariant();
        state.baby = entity.isElasmosaurusBaby();
    }

    @Override
    public void scaleModelForRender(
            ElasmosaurusRenderState state,
            float widthScale,
            float heightScale,
            PoseStack poseStack,
            BakedGeoModel model,
            CameraRenderState cameraRenderState
    ) {
        float scale = state.baby ? BABY_SCALE : 1.0F;

        super.scaleModelForRender(
                state,
                widthScale * scale,
                heightScale * scale,
                poseStack,
                model,
                cameraRenderState
        );
    }

    public static class ElasmosaurusRenderState
            extends LivingEntityRenderState
            implements GeoRenderState {

        public int variant = 0;
        public boolean baby = false;

        private final Map<DataTicket<?>, Object> geckolibData =
                new Reference2ObjectOpenHashMap<>();

        @Override
        public <D> void addGeckolibData(
                DataTicket<D> ticket,
                @Nullable D data
        ) {
            geckolibData.put(ticket, data);
        }

        @Override
        public boolean hasGeckolibData(DataTicket<?> ticket) {
            return geckolibData.containsKey(ticket);
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <D> D getGeckolibData(DataTicket<D> ticket) {
            Object data = geckolibData.get(ticket);

            if (data == null && !hasGeckolibData(ticket)) {
                throw new IllegalArgumentException(
                        "Missing GeckoLib data: " + ticket
                );
            }

            return (D) data;
        }

        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return geckolibData;
        }
    }
}