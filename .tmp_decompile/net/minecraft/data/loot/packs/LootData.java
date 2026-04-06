/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.data.loot.packs;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

public interface LootData {
    public static final Map<DyeColor, ItemLike> WOOL_ITEM_BY_DYE = Maps.newEnumMap((Map)Map.ofEntries((Map.Entry[])new Map.Entry[]{Map.entry((Object)DyeColor.WHITE, (Object)Blocks.WHITE_WOOL), Map.entry((Object)DyeColor.ORANGE, (Object)Blocks.ORANGE_WOOL), Map.entry((Object)DyeColor.MAGENTA, (Object)Blocks.MAGENTA_WOOL), Map.entry((Object)DyeColor.LIGHT_BLUE, (Object)Blocks.LIGHT_BLUE_WOOL), Map.entry((Object)DyeColor.YELLOW, (Object)Blocks.YELLOW_WOOL), Map.entry((Object)DyeColor.LIME, (Object)Blocks.LIME_WOOL), Map.entry((Object)DyeColor.PINK, (Object)Blocks.PINK_WOOL), Map.entry((Object)DyeColor.GRAY, (Object)Blocks.GRAY_WOOL), Map.entry((Object)DyeColor.LIGHT_GRAY, (Object)Blocks.LIGHT_GRAY_WOOL), Map.entry((Object)DyeColor.CYAN, (Object)Blocks.CYAN_WOOL), Map.entry((Object)DyeColor.PURPLE, (Object)Blocks.PURPLE_WOOL), Map.entry((Object)DyeColor.BLUE, (Object)Blocks.BLUE_WOOL), Map.entry((Object)DyeColor.BROWN, (Object)Blocks.BROWN_WOOL), Map.entry((Object)DyeColor.GREEN, (Object)Blocks.GREEN_WOOL), Map.entry((Object)DyeColor.RED, (Object)Blocks.RED_WOOL), Map.entry((Object)DyeColor.BLACK, (Object)Blocks.BLACK_WOOL)}));
}

