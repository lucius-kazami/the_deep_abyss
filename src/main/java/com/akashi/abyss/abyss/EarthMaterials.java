package com.akashi.abyss.abyss;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;

import java.util.EnumMap;

public class EarthMaterials {

    public static final TagKey<Item> REPAIRS_EARTH = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("abyss", "repairs_earth"));

    // Блоки, которые инструменты стихий земли, ветра и молнии НЕ могут добыть.
    // Тег пустой, поэтому они добывают всё (в том числе водную руду).
    public static final TagKey<Block> INCORRECT_FOR_ELEMENTAL = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("abyss", "incorrect_for_elemental_tool"));

    public static final ToolMaterial EARTH_TOOL = new ToolMaterial(
            OreHierarchy.INCORRECT_FOR_EARTH,
            10000,
            8.0F,
            2.0F,
            14,
            REPAIRS_EARTH);

    public static final ResourceKey<EquipmentAsset> EARTH_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("abyss", "earth"));

    public static final ArmorMaterial EARTH_ARMOR = new ArmorMaterial(
            60,
            Util.make(new EnumMap<>(ArmorType.class), m -> {
                m.put(ArmorType.BOOTS, 4);
                m.put(ArmorType.LEGGINGS, 7);
                m.put(ArmorType.CHESTPLATE, 9);
                m.put(ArmorType.HELMET, 4);
                m.put(ArmorType.BODY, 9);
            }),
            12,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            8.0F,
            0.1F,
            REPAIRS_EARTH,
            EARTH_ASSET);
}
