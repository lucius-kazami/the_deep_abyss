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

public class LightningMaterials {

    /*
     * Предметы, которыми ремонтируется комплект молнии.
     *
     * data/abyss/tags/item/repairs_lightning.json
     */
    public static final TagKey<Item> REPAIRS_LIGHTNING =
            TagKey.create(
                    Registries.ITEM,
                    ResourceLocation.fromNamespaceAndPath(
                            Abyss.MODID,
                            "repairs_lightning"
                    )
            );

    /*
     * Инструменты молнии добывают всё.
     */
    public static final TagKey<Block> INCORRECT_FOR_LIGHTNING =
            OreHierarchy.INCORRECT_FOR_LIGHTNING;

    /*
     * =========================================================
     * ИНСТРУМЕНТЫ МОЛНИИ
     * =========================================================
     *
     * durability = 2031
     * mining speed = 18
     * damage bonus = 5
     * enchantment = 15
     */

    public static final ToolMaterial LIGHTNING_TOOL =
            new ToolMaterial(
                    INCORRECT_FOR_LIGHTNING,
                    2031,
                    18.0F,
                    5.0F,
                    15,
                    REPAIRS_LIGHTNING
            );

    /*
     * =========================================================
     * БРОНЯ
     * =========================================================
     */

    public static final ResourceKey<EquipmentAsset> LIGHTNING_ASSET =
            ResourceKey.create(
                    EquipmentAssets.ROOT_ID,
                    ResourceLocation.fromNamespaceAndPath(
                            Abyss.MODID,
                            "lightning"
                    )
            );

    public static final ArmorMaterial LIGHTNING_ARMOR =
            new ArmorMaterial(
                    37,

                    Util.make(
                            new EnumMap<>(ArmorType.class),
                            map -> {
                                map.put(ArmorType.BOOTS, 3);
                                map.put(ArmorType.LEGGINGS, 6);
                                map.put(ArmorType.CHESTPLATE, 8);
                                map.put(ArmorType.HELMET, 3);
                                map.put(ArmorType.BODY, 11);
                            }
                    ),

                    15,

                    SoundEvents.ARMOR_EQUIP_NETHERITE,

                    // toughness как у незерита
                    3.0F,

                    // сопротивление отбрасыванию
                    0.1F,

                    REPAIRS_LIGHTNING,

                    LIGHTNING_ASSET
            );

    private LightningMaterials() {
    }
}