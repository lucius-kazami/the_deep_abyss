package com.akashi.abyss.abyss.entity;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.state.CameraRenderState;
import software.bernie.geckolib.cache.object.BakedGeoModel;

import java.util.Map;

public class StegosaurusRenderer extends GeoEntityRenderer<
        StegosaurusEntity,
        StegosaurusRenderer.StegosaurusRenderState> {

    public StegosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new StegosaurusModel());
        this.withScale(1.5F);
    }

    @Override
    public StegosaurusRenderState createRenderState(
            StegosaurusEntity entity,
            Void relatedObject
    ) {
        return new StegosaurusRenderState();
    }

    @Override
    public void extractRenderState(
            StegosaurusEntity entity,
            StegosaurusRenderState state,
            float partialTick
    ) {
        super.extractRenderState(entity, state, partialTick);
        state.variant = entity.getVariant();
        state.baby = entity.isBaby();
    }

    public static class StegosaurusRenderState
            extends LivingEntityRenderState
            implements GeoRenderState {

        public int variant;
        public boolean baby;

        private final Map<DataTicket<?>, Object> geckolibData =
                new Reference2ObjectOpenHashMap<>();

        @Override
        public <D> void addGeckolibData(DataTicket<D> dataTicket, @Nullable D data) {
            this.geckolibData.put(dataTicket, data);
        }

        @Override
        public boolean hasGeckolibData(DataTicket<?> dataTicket) {
            return this.geckolibData.containsKey(dataTicket);
        }

        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <D> D getGeckolibData(DataTicket<D> dataTicket) {
            Object data = this.geckolibData.get(dataTicket);

            if (data == null && !hasGeckolibData(dataTicket)) {
                throw new IllegalArgumentException(
                        "Attempted to retrieve GeckoLib data that does not exist: " + dataTicket
                );
            }

            return (D) data;
        }

        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return this.geckolibData;
        }
    }
    @Override
    public void scaleModelForRender(
            StegosaurusRenderState state,
            float widthScale,
            float heightScale,
            PoseStack poseStack,
            BakedGeoModel model,
            CameraRenderState cameraRenderState
    ) {
        float babyScale = state.baby ? 0.35F : 1.0F;

        super.scaleModelForRender(
                state,
                widthScale * babyScale,
                heightScale * babyScale,
                poseStack,
                model,
                cameraRenderState
        );
    }
}