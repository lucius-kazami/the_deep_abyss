package com.akashi.abyss.abyss;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

public class WaterMaterials {

    public static final TagKey<Item> REPAIRS_WATER = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("abyss", "repairs_water"));

    // Инструменты как алмазные. Какие руды стихий кирка воды не даёт, см. OreHierarchy.
    public static final ToolMaterial WATER_TOOL = new ToolMaterial(
            OreHierarchy.INCORRECT_FOR_WATER,
            1561,
            8.0F,
            3.0F,
            10,
            REPAIRS_WATER);

    public static final ResourceKey<EquipmentAsset> WATER_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("abyss", "water"));

    // Чуть сильнее незеритовой (20 очков, твёрдость 3), слабее земляной (24, твёрдость 8)
    public static final ArmorMaterial WATER_ARMOR = new ArmorMaterial(
            42,
            Util.make(new EnumMap<>(ArmorType.class), m -> {
                m.put(ArmorType.BOOTS, 4);
                m.put(ArmorType.LEGGINGS, 6);
                m.put(ArmorType.CHESTPLATE, 8);
                m.put(ArmorType.HELMET, 4);
                m.put(ArmorType.BODY, 8);
            }),
            15,
            SoundEvents.ARMOR_EQUIP_TURTLE,
            4.0F,
            0.1F,
            REPAIRS_WATER,
            WATER_ASSET);
}
