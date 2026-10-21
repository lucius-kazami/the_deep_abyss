
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

public class AnkylosaurusRenderer extends GeoEntityRenderer<
        AnkylosaurusEntity,
        AnkylosaurusRenderer.AnkylosaurusRenderState> {

    public AnkylosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new AnkylosaurusModel());
        this.withScale(1.5F);
    }

    @Override
    public AnkylosaurusRenderState createRenderState(
            AnkylosaurusEntity entity,
            Void relatedObject
    ) {
        return new AnkylosaurusRenderState();
    }

    @Override
    public void extractRenderState(
            AnkylosaurusEntity entity,
            AnkylosaurusRenderState state,
            float partialTick
    ) {
        super.extractRenderState(entity, state, partialTick);
        state.variant = entity.getVariant();
        state.baby = entity.isBaby();
    }

    public static class AnkylosaurusRenderState
            extends LivingEntityRenderState
            implements GeoRenderState {

        public int variant;
        public boolean baby;

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
                        "Attempted to retrieve GeckoLib data that does not exist: "
                                + ticket
                );
            }

            return (D) data;
        }

        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return geckolibData;
        }
    }
    @Override
    public void scaleModelForRender(
            AnkylosaurusRenderState state,
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
