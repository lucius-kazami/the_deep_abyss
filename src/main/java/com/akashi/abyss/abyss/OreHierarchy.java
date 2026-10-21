package com.akashi.abyss.abyss;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Иерархия кирок стихий. В каждом теге перечислены руды, с которых эта кирка НЕ даёт дроп
 * (руда ломается, но ничего не выпадает). Теги лежат в data/abyss/tags/block/.
 * Руду своей стихии каждая кирка добывает.
 *
 *   земля:  земля, ветер, вода        (не даёт: огонь, молнию)
 *   вода:   вода, земля, огонь        (не даёт: ветер, молнию)
 *   огонь:  огонь, земля, ветер       (не даёт: воду, молнию)
 *   ветер:  ветер, земля, вода, молния (не даёт: огонь)
 *   молния: всё
 */
public class OreHierarchy {

    private static TagKey<Block> tag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("abyss", name));
    }

    public static final TagKey<Block> INCORRECT_FOR_EARTH = tag("incorrect_for_earth_tool");
    public static final TagKey<Block> INCORRECT_FOR_WATER = tag("incorrect_for_water_tool");
    public static final TagKey<Block> INCORRECT_FOR_FIRE = tag("incorrect_for_fire_tool");
    public static final TagKey<Block> INCORRECT_FOR_WIND = tag("incorrect_for_wind_tool");
    public static final TagKey<Block> INCORRECT_FOR_LIGHTNING = tag("incorrect_for_lightning_tool");
}
