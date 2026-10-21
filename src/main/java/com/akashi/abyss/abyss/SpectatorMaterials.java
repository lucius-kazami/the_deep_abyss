package com.akashi.abyss.abyss;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;

public class SpectatorMaterials {

    // ============================================================
    // РЕМОНТ
    // ============================================================

    public static final TagKey<Item> REPAIRS_SPECTATOR =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            Abyss.MODID,
                            "repairs_spectator"
                    )
            );


    // ============================================================
    // EQUIPMENT ASSET
    // ============================================================

    public static final ResourceKey<EquipmentAsset> SPECTATOR_ASSET =
            ResourceKey.create(
                    EquipmentAssets.ROOT_ID,
                    ResourceLocation.fromNamespaceAndPath(
                            Abyss.MODID,
                            "spectator"
                    )
            );


    // ============================================================
    // БРОНЯ СПЕКТАТОРА
    // ============================================================

    public static final ArmorMaterial SPECTATOR_ARMOR =
            new ArmorMaterial(

                    // Прочность.
                    // Фактически нагрудник всё равно будет UNBREAKABLE.
                    50,

                    // Защита
                    Util.make(
                            new EnumMap<>(ArmorType.class),
                            map -> {

                                map.put(ArmorType.BOOTS, 4);
                                map.put(ArmorType.LEGGINGS, 7);

                                // Нас интересует именно нагрудник.
                                map.put(ArmorType.CHESTPLATE, 10);

                                map.put(ArmorType.HELMET, 4);
                                map.put(ArmorType.BODY, 12);
                            }
                    ),

                    // Enchantability
                    20,

                    // Звук надевания
                    SoundEvents.ARMOR_EQUIP_NETHERITE,

                    // Toughness
                    4.0F,

                    // Knockback resistance
                    0.15F,

                    // Ремонт
                    REPAIRS_SPECTATOR,

                    // Внешний вид брони
                    SPECTATOR_ASSET
            );


    private SpectatorMaterials() {
    }
}