package com.akashi.abyss.abyss.world;

import com.akashi.abyss.abyss.Abyss;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class AbyssDimensions {
    public static final ResourceKey<Level> BEZDNA = ResourceKey.create(
            Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(Abyss.MODID, "bezdna")
    );
}