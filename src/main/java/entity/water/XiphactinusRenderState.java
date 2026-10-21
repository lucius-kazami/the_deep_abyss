
package com.akashi.abyss.abyss.entity.water;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

public class XiphactinusRenderState
        extends LivingEntityRenderState
        implements GeoRenderState {

    // Вариант текстуры: 0, 1 или 2
    public int variant = 0;

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
