package com.akashi.abyss.abyss.client;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class BlackHoleRenderState extends EntityRenderState {

    public boolean superBlackHole = false;

    // Основной размер
    public float scale = 1.0F;

    // Вращение основного слоя
    public float rotation = 0.0F;

    // Вращение второго слоя
    public float secondRotation = 0.0F;

    // Пульсация
    public float pulse = 1.0F;

    // Прозрачность второго слоя
    public int secondLayerAlpha = 150;
}