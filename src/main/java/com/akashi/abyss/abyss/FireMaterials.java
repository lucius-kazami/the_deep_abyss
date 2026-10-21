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

public class FireMaterials {

    public static final TagKey<Item> REPAIRS_FIRE = TagKey.create(
            Registries.ITEM, ResourceLocation.fromNamespaceAndPath("abyss", "repairs_fire"));

    // Инструменты как незеритовые
    public static final ToolMaterial FIRE_TOOL = new ToolMaterial(
            OreHierarchy.INCORRECT_FOR_FIRE,
            2031,
            9.0F,
            4.0F,
            15,
            REPAIRS_FIRE);

    public static final ResourceKey<EquipmentAsset> FIRE_ASSET = ResourceKey.create(
            EquipmentAssets.ROOT_ID, ResourceLocation.fromNamespaceAndPath("abyss", "fire"));

    // Броня как незеритовая (20 очков, твёрдость 3, сопротивление отбрасыванию 0.1)
    public static final ArmorMaterial FIRE_ARMOR = new ArmorMaterial(
            37,
            Util.make(new EnumMap<>(ArmorType.class), m -> {
                m.put(ArmorType.BOOTS, 3);
                m.put(ArmorType.LEGGINGS, 6);
                m.put(ArmorType.CHESTPLATE, 8);
                m.put(ArmorType.HELMET, 3);
                m.put(ArmorType.BODY, 11);
            }),
            15,
            SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0F,
            0.1F,
            REPAIRS_FIRE,
            FIRE_ASSET);
}
