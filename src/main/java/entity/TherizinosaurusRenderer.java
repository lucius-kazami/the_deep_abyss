package com.akashi.abyss.abyss.entity;
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

public class TherizinosaurusRenderer extends GeoEntityRenderer<TherizinosaurusEntity, TherizinosaurusRenderer.TherizinosaurusRenderState> {
    public TherizinosaurusRenderer(EntityRendererProvider.Context context) {
        super(context, new TherizinosaurusModel());
        this.withScale(1.2F);
    }

    @Override
    public TherizinosaurusRenderState createRenderState(TherizinosaurusEntity entity, Void relatedObject) {
        return new TherizinosaurusRenderState();
    }

    @Override
    public void extractRenderState(TherizinosaurusEntity entity, TherizinosaurusRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.variant = entity.getVariant();
        state.baby = entity.isBaby();
    }

    @Override
    public void scaleModelForRender(TherizinosaurusRenderState state, float widthScale, float heightScale,
                                    PoseStack poseStack, BakedGeoModel model, CameraRenderState cameraRenderState) {
        float babyScale = state.baby ? 0.35F : 1.0F;
        super.scaleModelForRender(state, widthScale * babyScale, heightScale * babyScale,
                poseStack, model, cameraRenderState);
    }

    public static class TherizinosaurusRenderState extends LivingEntityRenderState implements GeoRenderState {
        public int variant;
        public boolean baby;
        private final Map<DataTicket<?>, Object> geckolibData = new Reference2ObjectOpenHashMap<>();
        @Override
        public <D> void addGeckolibData(DataTicket<D> ticket, @Nullable D data) { geckolibData.put(ticket, data); }
        @Override
        public boolean hasGeckolibData(DataTicket<?> ticket) { return geckolibData.containsKey(ticket); }
        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public <D> D getGeckolibData(DataTicket<D> ticket) {
            Object data = geckolibData.get(ticket);
            if (data == null && !hasGeckolibData(ticket))
                throw new IllegalArgumentException("Missing GeckoLib data: " + ticket);
            return (D) data;
        }
        @Override
        public Map<DataTicket<?>, Object> getDataMap() { return geckolibData; }
    }
}
