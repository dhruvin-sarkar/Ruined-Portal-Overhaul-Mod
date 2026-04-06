/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.npc.villager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.VillagerDataHolder;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider;
import net.minecraft.world.item.enchantment.providers.TradeRebalanceEnchantmentProviders;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

public class VillagerTrades {
    private static final int DEFAULT_SUPPLY = 12;
    private static final int COMMON_ITEMS_SUPPLY = 16;
    private static final int UNCOMMON_ITEMS_SUPPLY = 3;
    private static final int XP_LEVEL_1_SELL = 1;
    private static final int XP_LEVEL_1_BUY = 2;
    private static final int XP_LEVEL_2_SELL = 5;
    private static final int XP_LEVEL_2_BUY = 10;
    private static final int XP_LEVEL_3_SELL = 10;
    private static final int XP_LEVEL_3_BUY = 20;
    private static final int XP_LEVEL_4_SELL = 15;
    private static final int XP_LEVEL_4_BUY = 30;
    private static final int XP_LEVEL_5_TRADE = 30;
    private static final float LOW_TIER_PRICE_MULTIPLIER = 0.05f;
    private static final float HIGH_TIER_PRICE_MULTIPLIER = 0.2f;
    public static final Map<ResourceKey<VillagerProfession>, Int2ObjectMap<ItemListing[]>> TRADES = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(VillagerProfession.FARMER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.WHEAT, 20, 16, 2), new EmeraldForItems(Items.POTATO, 26, 16, 2), new EmeraldForItems(Items.CARROT, 22, 16, 2), new EmeraldForItems(Items.BEETROOT, 15, 16, 2), new ItemsForEmeralds(Items.BREAD, 1, 6, 16, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Blocks.PUMPKIN, 6, 12, 10), new ItemsForEmeralds(Items.PUMPKIN_PIE, 1, 4, 5), new ItemsForEmeralds(Items.APPLE, 1, 4, 16, 5)}, (Object)3, (Object)new ItemListing[]{new ItemsForEmeralds(Items.COOKIE, 3, 18, 10), new EmeraldForItems(Blocks.MELON, 4, 12, 20)}, (Object)4, (Object)new ItemListing[]{new ItemsForEmeralds(Blocks.CAKE, 1, 1, 12, 15), new SuspiciousStewForEmerald(MobEffects.NIGHT_VISION, 100, 15), new SuspiciousStewForEmerald(MobEffects.JUMP_BOOST, 160, 15), new SuspiciousStewForEmerald(MobEffects.WEAKNESS, 140, 15), new SuspiciousStewForEmerald(MobEffects.BLINDNESS, 120, 15), new SuspiciousStewForEmerald(MobEffects.POISON, 280, 15), new SuspiciousStewForEmerald(MobEffects.SATURATION, 7, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.GOLDEN_CARROT, 3, 3, 30), new ItemsForEmeralds(Items.GLISTERING_MELON_SLICE, 4, 3, 30)})));
        hashMap.put(VillagerProfession.FISHERMAN, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.STRING, 20, 16, 2), new EmeraldForItems(Items.COAL, 10, 16, 2), new ItemsAndEmeraldsToItems((ItemLike)Items.COD, 6, 1, Items.COOKED_COD, 6, 16, 1, 0.05f), new ItemsForEmeralds(Items.COD_BUCKET, 3, 1, 16, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.COD, 15, 16, 10), new ItemsAndEmeraldsToItems((ItemLike)Items.SALMON, 6, 1, Items.COOKED_SALMON, 6, 16, 5, 0.05f), new ItemsForEmeralds(Items.CAMPFIRE, 2, 1, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.SALMON, 13, 16, 20), new EnchantedItemForEmeralds(Items.FISHING_ROD, 3, 3, 10, 0.2f)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.TROPICAL_FISH, 6, 12, 30)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.PUFFERFISH, 4, 12, 30), new EmeraldsForVillagerTypeItem(1, 12, 30, (Map<ResourceKey<VillagerType>, Item>)ImmutableMap.builder().put(VillagerType.PLAINS, (Object)Items.OAK_BOAT).put(VillagerType.TAIGA, (Object)Items.SPRUCE_BOAT).put(VillagerType.SNOW, (Object)Items.SPRUCE_BOAT).put(VillagerType.DESERT, (Object)Items.JUNGLE_BOAT).put(VillagerType.JUNGLE, (Object)Items.JUNGLE_BOAT).put(VillagerType.SAVANNA, (Object)Items.ACACIA_BOAT).put(VillagerType.SWAMP, (Object)Items.DARK_OAK_BOAT).build())})));
        hashMap.put(VillagerProfession.SHEPHERD, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Blocks.WHITE_WOOL, 18, 16, 2), new EmeraldForItems(Blocks.BROWN_WOOL, 18, 16, 2), new EmeraldForItems(Blocks.BLACK_WOOL, 18, 16, 2), new EmeraldForItems(Blocks.GRAY_WOOL, 18, 16, 2), new ItemsForEmeralds(Items.SHEARS, 2, 1, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.WHITE_DYE, 12, 16, 10), new EmeraldForItems(Items.GRAY_DYE, 12, 16, 10), new EmeraldForItems(Items.BLACK_DYE, 12, 16, 10), new EmeraldForItems(Items.LIGHT_BLUE_DYE, 12, 16, 10), new EmeraldForItems(Items.LIME_DYE, 12, 16, 10), new ItemsForEmeralds(Blocks.WHITE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.ORANGE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.MAGENTA_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_BLUE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.YELLOW_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.LIME_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.PINK_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.GRAY_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_GRAY_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.CYAN_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.PURPLE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.BLUE_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.BROWN_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.GREEN_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.RED_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.BLACK_WOOL, 1, 1, 16, 5), new ItemsForEmeralds(Blocks.WHITE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.ORANGE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.MAGENTA_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_BLUE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.YELLOW_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.LIME_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.PINK_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.GRAY_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.LIGHT_GRAY_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.CYAN_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.PURPLE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.BLUE_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.BROWN_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.GREEN_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.RED_CARPET, 1, 4, 16, 5), new ItemsForEmeralds(Blocks.BLACK_CARPET, 1, 4, 16, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.YELLOW_DYE, 12, 16, 20), new EmeraldForItems(Items.LIGHT_GRAY_DYE, 12, 16, 20), new EmeraldForItems(Items.ORANGE_DYE, 12, 16, 20), new EmeraldForItems(Items.RED_DYE, 12, 16, 20), new EmeraldForItems(Items.PINK_DYE, 12, 16, 20), new ItemsForEmeralds(Blocks.WHITE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.YELLOW_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.RED_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.BLACK_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.BLUE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.BROWN_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.CYAN_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.GRAY_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.GREEN_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.LIGHT_BLUE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.LIGHT_GRAY_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.LIME_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.MAGENTA_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.ORANGE_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.PINK_BED, 3, 1, 12, 10), new ItemsForEmeralds(Blocks.PURPLE_BED, 3, 1, 12, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.BROWN_DYE, 12, 16, 30), new EmeraldForItems(Items.PURPLE_DYE, 12, 16, 30), new EmeraldForItems(Items.BLUE_DYE, 12, 16, 30), new EmeraldForItems(Items.GREEN_DYE, 12, 16, 30), new EmeraldForItems(Items.MAGENTA_DYE, 12, 16, 30), new EmeraldForItems(Items.CYAN_DYE, 12, 16, 30), new ItemsForEmeralds(Items.WHITE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.BLUE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.RED_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.PINK_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.GREEN_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.LIME_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.GRAY_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.BLACK_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.PURPLE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.MAGENTA_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.CYAN_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.BROWN_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.YELLOW_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.ORANGE_BANNER, 3, 1, 12, 15), new ItemsForEmeralds(Items.LIGHT_GRAY_BANNER, 3, 1, 12, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.PAINTING, 2, 3, 30)})));
        hashMap.put(VillagerProfession.FLETCHER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.STICK, 32, 16, 2), new ItemsForEmeralds(Items.ARROW, 1, 16, 1), new ItemsAndEmeraldsToItems((ItemLike)Blocks.GRAVEL, 10, 1, Items.FLINT, 10, 12, 1, 0.05f)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 26, 12, 10), new ItemsForEmeralds(Items.BOW, 2, 1, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.STRING, 14, 16, 20), new ItemsForEmeralds(Items.CROSSBOW, 3, 1, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.FEATHER, 24, 16, 30), new EnchantedItemForEmeralds(Items.BOW, 2, 3, 15)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.TRIPWIRE_HOOK, 8, 12, 30), new EnchantedItemForEmeralds(Items.CROSSBOW, 3, 3, 15), new TippedArrowForItemsAndEmeralds(Items.ARROW, 5, Items.TIPPED_ARROW, 5, 2, 12, 30)})));
        hashMap.put(VillagerProfession.LIBRARIAN, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.builder().put((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.PAPER, 24, 16, 2), new EnchantBookForEmeralds(1, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put((Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.BOOK, 4, 12, 10), new EnchantBookForEmeralds(5, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Items.LANTERN, 1, 1, 5)}).put((Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.INK_SAC, 5, 12, 20), new EnchantBookForEmeralds(10, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Items.GLASS, 1, 4, 10)}).put((Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.WRITABLE_BOOK, 2, 12, 30), new EnchantBookForEmeralds(15, EnchantmentTags.TRADEABLE), new ItemsForEmeralds(Items.CLOCK, 5, 1, 15), new ItemsForEmeralds(Items.COMPASS, 4, 1, 15)}).put((Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.NAME_TAG, 20, 1, 30)}).build()));
        hashMap.put(VillagerProfession.CARTOGRAPHER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.PAPER, 24, 12, 2), new ItemsForEmeralds(Items.MAP, 7, 1, 12, 1, 0.05f)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.GLASS_PANE, 11, 12, 10), TypeSpecificTrade.oneTradeInBiomes(new TreasureMapForEmeralds(8, StructureTags.ON_TAIGA_VILLAGE_MAPS, "filled_map.village_taiga", MapDecorationTypes.TAIGA_VILLAGE, 12, 5), VillagerType.SWAMP, VillagerType.SNOW, VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new TreasureMapForEmeralds(8, StructureTags.ON_SWAMP_EXPLORER_MAPS, "filled_map.explorer_swamp", MapDecorationTypes.SWAMP_HUT, 12, 5), VillagerType.TAIGA, VillagerType.SNOW, VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new TreasureMapForEmeralds(8, StructureTags.ON_SNOWY_VILLAGE_MAPS, "filled_map.village_snowy", MapDecorationTypes.SNOWY_VILLAGE, 12, 5), VillagerType.TAIGA, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new TreasureMapForEmeralds(8, StructureTags.ON_SAVANNA_VILLAGE_MAPS, "filled_map.village_savanna", MapDecorationTypes.SAVANNA_VILLAGE, 12, 5), VillagerType.PLAINS, VillagerType.JUNGLE, VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new TreasureMapForEmeralds(8, StructureTags.ON_PLAINS_VILLAGE_MAPS, "filled_map.village_plains", MapDecorationTypes.PLAINS_VILLAGE, 12, 5), VillagerType.TAIGA, VillagerType.SNOW, VillagerType.SAVANNA, VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new TreasureMapForEmeralds(8, StructureTags.ON_JUNGLE_EXPLORER_MAPS, "filled_map.explorer_jungle", MapDecorationTypes.JUNGLE_TEMPLE, 12, 5), VillagerType.SWAMP, VillagerType.SAVANNA, VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new TreasureMapForEmeralds(8, StructureTags.ON_DESERT_VILLAGE_MAPS, "filled_map.village_desert", MapDecorationTypes.DESERT_VILLAGE, 12, 5), VillagerType.SAVANNA, VillagerType.JUNGLE)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.COMPASS, 1, 12, 20), new TreasureMapForEmeralds(13, StructureTags.ON_OCEAN_EXPLORER_MAPS, "filled_map.monument", MapDecorationTypes.OCEAN_MONUMENT, 12, 10), new TreasureMapForEmeralds(12, StructureTags.ON_TRIAL_CHAMBERS_MAPS, "filled_map.trial_chambers", MapDecorationTypes.TRIAL_CHAMBERS, 12, 10)}, (Object)4, (Object)new ItemListing[]{new ItemsForEmeralds(Items.ITEM_FRAME, 7, 1, 12, 15, 0.05f), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.BLUE_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.WHITE_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.SNOW, VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.RED_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.SNOW, VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.GREEN_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.DESERT, VillagerType.SAVANNA, VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.LIME_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.DESERT, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.PURPLE_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.TAIGA, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CYAN_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.DESERT, VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.YELLOW_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.PLAINS, VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.ORANGE_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.SAVANNA, VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.BROWN_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.PLAINS, VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.MAGENTA_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.LIGHT_BLUE_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.SNOW, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.PINK_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.TAIGA, VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.GRAY_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.BLACK_BANNER, 2, 1, 12, 15, 0.05f), VillagerType.SWAMP)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Items.GLOBE_BANNER_PATTERN, 8, 1, 12, 30, 0.05f), new TreasureMapForEmeralds(14, StructureTags.ON_WOODLAND_EXPLORER_MAPS, "filled_map.mansion", MapDecorationTypes.WOODLAND_MANSION, 12, 30)})));
        hashMap.put(VillagerProfession.CLERIC, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.ROTTEN_FLESH, 32, 16, 2), new ItemsForEmeralds(Items.REDSTONE, 1, 2, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.GOLD_INGOT, 3, 12, 10), new ItemsForEmeralds(Items.LAPIS_LAZULI, 1, 1, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.RABBIT_FOOT, 2, 12, 20), new ItemsForEmeralds(Blocks.GLOWSTONE, 4, 1, 12, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.TURTLE_SCUTE, 4, 12, 30), new EmeraldForItems(Items.GLASS_BOTTLE, 9, 12, 30), new ItemsForEmeralds(Items.ENDER_PEARL, 5, 1, 15)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.NETHER_WART, 22, 12, 30), new ItemsForEmeralds(Items.EXPERIENCE_BOTTLE, 3, 1, 30)})));
        hashMap.put(VillagerProfession.ARMORER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(new ItemStack(Items.IRON_LEGGINGS), 7, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.IRON_BOOTS), 4, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.IRON_HELMET), 5, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.IRON_CHESTPLATE), 9, 1, 12, 1, 0.2f)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_BOOTS), 1, 1, 12, 5, 0.2f), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 1, 12, 5, 0.2f)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.LAVA_BUCKET, 1, 12, 20), new EmeraldForItems(Items.DIAMOND, 1, 12, 20), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_HELMET), 1, 1, 12, 10, 0.2f), new ItemsForEmeralds(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 4, 1, 12, 10, 0.2f), new ItemsForEmeralds(new ItemStack(Items.SHIELD), 5, 1, 12, 10, 0.2f)}, (Object)4, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_LEGGINGS, 14, 3, 15, 0.2f), new EnchantedItemForEmeralds(Items.DIAMOND_BOOTS, 8, 3, 15, 0.2f)}, (Object)5, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_HELMET, 8, 3, 30, 0.2f), new EnchantedItemForEmeralds(Items.DIAMOND_CHESTPLATE, 16, 3, 30, 0.2f)})));
        hashMap.put(VillagerProfession.WEAPONSMITH, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(new ItemStack(Items.IRON_AXE), 3, 1, 12, 1, 0.2f), new EnchantedItemForEmeralds(Items.IRON_SWORD, 2, 3, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 24, 12, 20)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.DIAMOND, 1, 12, 30), new EnchantedItemForEmeralds(Items.DIAMOND_AXE, 12, 3, 15, 0.2f)}, (Object)5, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_SWORD, 8, 3, 30, 0.2f)})));
        hashMap.put(VillagerProfession.TOOLSMITH, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(new ItemStack(Items.STONE_AXE), 1, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.STONE_SHOVEL), 1, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.STONE_PICKAXE), 1, 1, 12, 1, 0.2f), new ItemsForEmeralds(new ItemStack(Items.STONE_HOE), 1, 1, 12, 1, 0.2f)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.IRON_INGOT, 4, 12, 10), new ItemsForEmeralds(new ItemStack(Items.BELL), 36, 1, 12, 5, 0.2f)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 30, 12, 20), new EnchantedItemForEmeralds(Items.IRON_AXE, 1, 3, 10, 0.2f), new EnchantedItemForEmeralds(Items.IRON_SHOVEL, 2, 3, 10, 0.2f), new EnchantedItemForEmeralds(Items.IRON_PICKAXE, 3, 3, 10, 0.2f), new ItemsForEmeralds(new ItemStack(Items.DIAMOND_HOE), 4, 1, 3, 10, 0.2f)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.DIAMOND, 1, 12, 30), new EnchantedItemForEmeralds(Items.DIAMOND_AXE, 12, 3, 15, 0.2f), new EnchantedItemForEmeralds(Items.DIAMOND_SHOVEL, 5, 3, 15, 0.2f)}, (Object)5, (Object)new ItemListing[]{new EnchantedItemForEmeralds(Items.DIAMOND_PICKAXE, 13, 3, 30, 0.2f)})));
        hashMap.put(VillagerProfession.BUTCHER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.CHICKEN, 14, 16, 2), new EmeraldForItems(Items.PORKCHOP, 7, 16, 2), new EmeraldForItems(Items.RABBIT, 4, 16, 2), new ItemsForEmeralds(Items.RABBIT_STEW, 1, 1, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 16, 2), new ItemsForEmeralds(Items.COOKED_PORKCHOP, 1, 5, 16, 5), new ItemsForEmeralds(Items.COOKED_CHICKEN, 1, 8, 16, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.MUTTON, 7, 16, 20), new EmeraldForItems(Items.BEEF, 10, 16, 20)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.DRIED_KELP_BLOCK, 10, 12, 30)}, (Object)5, (Object)new ItemListing[]{new EmeraldForItems(Items.SWEET_BERRIES, 10, 12, 30)})));
        hashMap.put(VillagerProfession.LEATHERWORKER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.LEATHER, 6, 16, 2), new DyedArmorForEmeralds(Items.LEATHER_LEGGINGS, 3), new DyedArmorForEmeralds(Items.LEATHER_CHESTPLATE, 7)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.FLINT, 26, 12, 10), new DyedArmorForEmeralds(Items.LEATHER_HELMET, 5, 12, 5), new DyedArmorForEmeralds(Items.LEATHER_BOOTS, 4, 12, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.RABBIT_HIDE, 9, 12, 20), new DyedArmorForEmeralds(Items.LEATHER_CHESTPLATE, 7)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.TURTLE_SCUTE, 4, 12, 30), new DyedArmorForEmeralds(Items.LEATHER_HORSE_ARMOR, 6, 12, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(new ItemStack(Items.SADDLE), 6, 1, 12, 30, 0.2f), new DyedArmorForEmeralds(Items.LEATHER_HELMET, 5, 12, 30)})));
        hashMap.put(VillagerProfession.MASON, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.of((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.CLAY_BALL, 10, 16, 2), new ItemsForEmeralds(Items.BRICK, 1, 10, 16, 1)}, (Object)2, (Object)new ItemListing[]{new EmeraldForItems(Blocks.STONE, 20, 16, 10), new ItemsForEmeralds(Blocks.CHISELED_STONE_BRICKS, 1, 4, 16, 5)}, (Object)3, (Object)new ItemListing[]{new EmeraldForItems(Blocks.GRANITE, 16, 16, 20), new EmeraldForItems(Blocks.ANDESITE, 16, 16, 20), new EmeraldForItems(Blocks.DIORITE, 16, 16, 20), new ItemsForEmeralds(Blocks.DRIPSTONE_BLOCK, 1, 4, 16, 10), new ItemsForEmeralds(Blocks.POLISHED_ANDESITE, 1, 4, 16, 10), new ItemsForEmeralds(Blocks.POLISHED_DIORITE, 1, 4, 16, 10), new ItemsForEmeralds(Blocks.POLISHED_GRANITE, 1, 4, 16, 10)}, (Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.QUARTZ, 12, 12, 30), new ItemsForEmeralds(Blocks.ORANGE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.WHITE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLUE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_BLUE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GRAY_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_GRAY_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLACK_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.RED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PINK_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.MAGENTA_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIME_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GREEN_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.CYAN_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PURPLE_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.YELLOW_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BROWN_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.ORANGE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.WHITE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BLACK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.RED_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PINK_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.MAGENTA_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.LIME_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.GREEN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.CYAN_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.PURPLE_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.YELLOW_GLAZED_TERRACOTTA, 1, 1, 12, 15), new ItemsForEmeralds(Blocks.BROWN_GLAZED_TERRACOTTA, 1, 1, 12, 15)}, (Object)5, (Object)new ItemListing[]{new ItemsForEmeralds(Blocks.QUARTZ_PILLAR, 1, 1, 12, 30), new ItemsForEmeralds(Blocks.QUARTZ_BLOCK, 1, 1, 12, 30)})));
    });
    public static final List<Pair<ItemListing[], Integer>> WANDERING_TRADER_TRADES = ImmutableList.builder().add((Object)Pair.of((Object)new ItemListing[]{new EmeraldForItems(VillagerTrades.potionCost(Potions.WATER), 2, 1, 1), new EmeraldForItems(Items.WATER_BUCKET, 1, 2, 1, 2), new EmeraldForItems(Items.MILK_BUCKET, 1, 2, 1, 2), new EmeraldForItems(Items.FERMENTED_SPIDER_EYE, 1, 2, 1, 3), new EmeraldForItems(Items.BAKED_POTATO, 4, 2, 1), new EmeraldForItems(Items.HAY_BLOCK, 1, 2, 1)}, (Object)2)).add((Object)Pair.of((Object)new ItemListing[]{new ItemsForEmeralds(Items.PACKED_ICE, 1, 1, 6, 1), new ItemsForEmeralds(Items.BLUE_ICE, 6, 1, 6, 1), new ItemsForEmeralds(Items.GUNPOWDER, 1, 4, 2, 1), new ItemsForEmeralds(Items.PODZOL, 3, 3, 6, 1), new ItemsForEmeralds(Blocks.ACACIA_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.BIRCH_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.DARK_OAK_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.JUNGLE_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.OAK_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.SPRUCE_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.CHERRY_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.MANGROVE_LOG, 1, 8, 4, 1), new ItemsForEmeralds(Blocks.PALE_OAK_LOG, 1, 8, 4, 1), new EnchantedItemForEmeralds(Items.IRON_PICKAXE, 1, 1, 1, 0.2f), new ItemsForEmeralds(VillagerTrades.potion(Potions.LONG_INVISIBILITY), 5, 1, 1, 1)}, (Object)2)).add((Object)Pair.of((Object)new ItemListing[]{new ItemsForEmeralds(Items.TROPICAL_FISH_BUCKET, 3, 1, 4, 1), new ItemsForEmeralds(Items.PUFFERFISH_BUCKET, 3, 1, 4, 1), new ItemsForEmeralds(Items.SEA_PICKLE, 2, 1, 5, 1), new ItemsForEmeralds(Items.SLIME_BALL, 4, 1, 5, 1), new ItemsForEmeralds(Items.GLOWSTONE, 2, 1, 5, 1), new ItemsForEmeralds(Items.NAUTILUS_SHELL, 5, 1, 5, 1), new ItemsForEmeralds(Items.FERN, 1, 1, 12, 1), new ItemsForEmeralds(Items.SUGAR_CANE, 1, 1, 8, 1), new ItemsForEmeralds(Items.PUMPKIN, 1, 1, 4, 1), new ItemsForEmeralds(Items.KELP, 3, 1, 12, 1), new ItemsForEmeralds(Items.CACTUS, 3, 1, 8, 1), new ItemsForEmeralds(Items.DANDELION, 1, 1, 12, 1), new ItemsForEmeralds(Items.POPPY, 1, 1, 12, 1), new ItemsForEmeralds(Items.BLUE_ORCHID, 1, 1, 8, 1), new ItemsForEmeralds(Items.ALLIUM, 1, 1, 12, 1), new ItemsForEmeralds(Items.AZURE_BLUET, 1, 1, 12, 1), new ItemsForEmeralds(Items.RED_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.ORANGE_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.WHITE_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.PINK_TULIP, 1, 1, 12, 1), new ItemsForEmeralds(Items.OXEYE_DAISY, 1, 1, 12, 1), new ItemsForEmeralds(Items.CORNFLOWER, 1, 1, 12, 1), new ItemsForEmeralds(Items.LILY_OF_THE_VALLEY, 1, 1, 7, 1), new ItemsForEmeralds(Items.OPEN_EYEBLOSSOM, 1, 1, 7, 1), new ItemsForEmeralds(Items.WHEAT_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.BEETROOT_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.PUMPKIN_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.MELON_SEEDS, 1, 1, 12, 1), new ItemsForEmeralds(Items.ACACIA_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.BIRCH_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.DARK_OAK_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.JUNGLE_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.OAK_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.SPRUCE_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.CHERRY_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.PALE_OAK_SAPLING, 5, 1, 8, 1), new ItemsForEmeralds(Items.MANGROVE_PROPAGULE, 5, 1, 8, 1), new ItemsForEmeralds(Items.RED_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.WHITE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BLUE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.PINK_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BLACK_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.GREEN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIGHT_GRAY_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.MAGENTA_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.YELLOW_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.GRAY_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.PURPLE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIGHT_BLUE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.LIME_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.ORANGE_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BROWN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.CYAN_DYE, 1, 3, 12, 1), new ItemsForEmeralds(Items.BRAIN_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.BUBBLE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.FIRE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.HORN_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.TUBE_CORAL_BLOCK, 3, 1, 8, 1), new ItemsForEmeralds(Items.VINE, 1, 3, 4, 1), new ItemsForEmeralds(Items.PALE_HANGING_MOSS, 1, 3, 4, 1), new ItemsForEmeralds(Items.BROWN_MUSHROOM, 1, 3, 4, 1), new ItemsForEmeralds(Items.RED_MUSHROOM, 1, 3, 4, 1), new ItemsForEmeralds(Items.LILY_PAD, 1, 5, 2, 1), new ItemsForEmeralds(Items.SMALL_DRIPLEAF, 1, 2, 5, 1), new ItemsForEmeralds(Items.SAND, 1, 8, 8, 1), new ItemsForEmeralds(Items.RED_SAND, 1, 4, 6, 1), new ItemsForEmeralds(Items.POINTED_DRIPSTONE, 1, 2, 5, 1), new ItemsForEmeralds(Items.ROOTED_DIRT, 1, 2, 5, 1), new ItemsForEmeralds(Items.MOSS_BLOCK, 1, 2, 5, 1), new ItemsForEmeralds(Items.PALE_MOSS_BLOCK, 1, 2, 5, 1), new ItemsForEmeralds(Items.WILDFLOWERS, 1, 1, 12, 1), new ItemsForEmeralds(Items.DRY_TALL_GRASS, 1, 1, 12, 1), new ItemsForEmeralds(Items.FIREFLY_BUSH, 3, 1, 12, 1)}, (Object)5)).build();
    public static final Map<ResourceKey<VillagerProfession>, Int2ObjectMap<ItemListing[]>> EXPERIMENTAL_TRADES = Map.of(VillagerProfession.LIBRARIAN, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.builder().put((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.PAPER, 24, 16, 2), VillagerTrades.commonBooks(1), new ItemsForEmeralds(Blocks.BOOKSHELF, 9, 1, 12, 1)}).put((Object)2, (Object)new ItemListing[]{new EmeraldForItems(Items.BOOK, 4, 12, 10), VillagerTrades.commonBooks(5), new ItemsForEmeralds(Items.LANTERN, 1, 1, 5)}).put((Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.INK_SAC, 5, 12, 20), VillagerTrades.commonBooks(10), new ItemsForEmeralds(Items.GLASS, 1, 4, 10)}).put((Object)4, (Object)new ItemListing[]{new EmeraldForItems(Items.WRITABLE_BOOK, 2, 12, 30), new ItemsForEmeralds(Items.CLOCK, 5, 1, 15), new ItemsForEmeralds(Items.COMPASS, 4, 1, 15)}).put((Object)5, (Object)new ItemListing[]{VillagerTrades.specialBooks(), new ItemsForEmeralds(Items.NAME_TAG, 20, 1, 30)}).build()), VillagerProfession.ARMORER, VillagerTrades.toIntMap((ImmutableMap<Integer, ItemListing[]>)ImmutableMap.builder().put((Object)1, (Object)new ItemListing[]{new EmeraldForItems(Items.COAL, 15, 12, 2), new EmeraldForItems(Items.IRON_INGOT, 5, 12, 2)}).put((Object)2, (Object)new ItemListing[]{TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 4, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 4, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 5, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 5, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 7, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_LEGGINGS, 7, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 9, 1, 12, 5, 0.05f), VillagerType.DESERT, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_CHESTPLATE, 9, 1, 12, 5, 0.05f), VillagerType.JUNGLE, VillagerType.SWAMP)}).put((Object)3, (Object)new ItemListing[]{new EmeraldForItems(Items.LAVA_BUCKET, 1, 12, 20), new ItemsForEmeralds(Items.SHIELD, 5, 1, 12, 10, 0.05f), new ItemsForEmeralds(Items.BELL, 36, 1, 12, 10, 0.2f)}).put((Object)4, (Object)new ItemListing[]{TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_BOOTS_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_HELMET_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_LEGGINGS_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_CHESTPLATE_4), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_BOOTS_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_HELMET_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_LEGGINGS_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_CHESTPLATE_4), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 2, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_BOOTS_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 3, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_HELMET_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_LEGGINGS, 5, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_LEGGINGS_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_CHESTPLATE, 7, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_CHESTPLATE_4), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_BOOTS_4), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.IRON_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_HELMET_4), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_BOOTS_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_HELMET_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_LEGGINGS_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_CHESTPLATE_4), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_BOOTS_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_HELMET_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_LEGGINGS, 11, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_LEGGINGS_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_CHESTPLATE, 13, 1, 3, 15, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_CHESTPLATE_4), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_BOOTS, 1, 4, Items.DIAMOND_LEGGINGS, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_LEGGINGS, 1, 4, Items.DIAMOND_CHESTPLATE, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_HELMET, 1, 4, Items.DIAMOND_BOOTS, 1, 3, 15, 0.05f), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems((ItemLike)Items.DIAMOND_CHESTPLATE, 1, 2, Items.DIAMOND_HELMET, 1, 3, 15, 0.05f), VillagerType.TAIGA)}).put((Object)5, (Object)new ItemListing[]{TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 4, 16, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_CHESTPLATE_5), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 16, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_DESERT_ARMORER_LEGGINGS_5), VillagerType.DESERT), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 16, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_LEGGINGS_5), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 12, Items.DIAMOND_BOOTS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_PLAINS_ARMORER_BOOTS_5), VillagerType.PLAINS), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 6, Items.DIAMOND_HELMET, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_HELMET_5), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 8, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SAVANNA_ARMORER_CHESTPLATE_5), VillagerType.SAVANNA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 2, 12, Items.DIAMOND_BOOTS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_BOOTS_5), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 12, Items.DIAMOND_HELMET, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SNOW_ARMORER_HELMET_5), VillagerType.SNOW), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_HELMET_5), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_JUNGLE_ARMORER_BOOTS_5), VillagerType.JUNGLE), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_HELMET, 9, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_HELMET_5), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsForEmeralds(Items.CHAINMAIL_BOOTS, 8, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_SWAMP_ARMORER_BOOTS_5), VillagerType.SWAMP), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 4, 18, Items.DIAMOND_CHESTPLATE, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_TAIGA_ARMORER_CHESTPLATE_5), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new ItemsAndEmeraldsToItems(Items.DIAMOND, 3, 18, Items.DIAMOND_LEGGINGS, 1, 3, 30, 0.05f, TradeRebalanceEnchantmentProviders.TRADES_TAIGA_ARMORER_LEGGINGS_5), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new EmeraldForItems(Items.DIAMOND_BLOCK, 1, 12, 30, 42), VillagerType.TAIGA), TypeSpecificTrade.oneTradeInBiomes(new EmeraldForItems(Items.IRON_BLOCK, 1, 12, 30, 4), VillagerType.DESERT, VillagerType.JUNGLE, VillagerType.PLAINS, VillagerType.SAVANNA, VillagerType.SNOW, VillagerType.SWAMP)}).build()));

    private static ItemListing commonBooks(int i) {
        return new TypeSpecificTrade((Map<ResourceKey<VillagerType>, ItemListing>)ImmutableMap.builder().put(VillagerType.DESERT, (Object)new EnchantBookForEmeralds(i, EnchantmentTags.TRADES_DESERT_COMMON)).put(VillagerType.JUNGLE, (Object)new EnchantBookForEmeralds(i, EnchantmentTags.TRADES_JUNGLE_COMMON)).put(VillagerType.PLAINS, (Object)new EnchantBookForEmeralds(i, EnchantmentTags.TRADES_PLAINS_COMMON)).put(VillagerType.SAVANNA, (Object)new EnchantBookForEmeralds(i, EnchantmentTags.TRADES_SAVANNA_COMMON)).put(VillagerType.SNOW, (Object)new EnchantBookForEmeralds(i, EnchantmentTags.TRADES_SNOW_COMMON)).put(VillagerType.SWAMP, (Object)new EnchantBookForEmeralds(i, EnchantmentTags.TRADES_SWAMP_COMMON)).put(VillagerType.TAIGA, (Object)new EnchantBookForEmeralds(i, EnchantmentTags.TRADES_TAIGA_COMMON)).build());
    }

    private static ItemListing specialBooks() {
        return new TypeSpecificTrade((Map<ResourceKey<VillagerType>, ItemListing>)ImmutableMap.builder().put(VillagerType.DESERT, (Object)new EnchantBookForEmeralds(30, 3, 3, EnchantmentTags.TRADES_DESERT_SPECIAL)).put(VillagerType.JUNGLE, (Object)new EnchantBookForEmeralds(30, 2, 2, EnchantmentTags.TRADES_JUNGLE_SPECIAL)).put(VillagerType.PLAINS, (Object)new EnchantBookForEmeralds(30, 3, 3, EnchantmentTags.TRADES_PLAINS_SPECIAL)).put(VillagerType.SAVANNA, (Object)new EnchantBookForEmeralds(30, 3, 3, EnchantmentTags.TRADES_SAVANNA_SPECIAL)).put(VillagerType.SNOW, (Object)new EnchantBookForEmeralds(30, EnchantmentTags.TRADES_SNOW_SPECIAL)).put(VillagerType.SWAMP, (Object)new EnchantBookForEmeralds(30, EnchantmentTags.TRADES_SWAMP_SPECIAL)).put(VillagerType.TAIGA, (Object)new EnchantBookForEmeralds(30, 2, 2, EnchantmentTags.TRADES_TAIGA_SPECIAL)).build());
    }

    private static Int2ObjectMap<ItemListing[]> toIntMap(ImmutableMap<Integer, ItemListing[]> immutableMap) {
        return new Int2ObjectOpenHashMap(immutableMap);
    }

    private static ItemCost potionCost(Holder<Potion> holder) {
        return new ItemCost(Items.POTION).withComponents(builder -> builder.expect(DataComponents.POTION_CONTENTS, new PotionContents(holder)));
    }

    private static ItemStack potion(Holder<Potion> holder) {
        return PotionContents.createItemStack(Items.POTION, holder);
    }

    record TypeSpecificTrade(Map<ResourceKey<VillagerType>, ItemListing> trades) implements ItemListing
    {
        @SafeVarargs
        public static TypeSpecificTrade oneTradeInBiomes(ItemListing itemListing, ResourceKey<VillagerType> ... resourceKeys) {
            return new TypeSpecificTrade(Arrays.stream(resourceKeys).collect(Collectors.toMap(resourceKey -> resourceKey, resourceKey -> itemListing)));
        }

        @Override
        public @Nullable MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            if (entity instanceof VillagerDataHolder) {
                VillagerDataHolder villagerDataHolder = (VillagerDataHolder)((Object)entity);
                ResourceKey resourceKey = villagerDataHolder.getVillagerData().type().unwrapKey().orElse(null);
                if (resourceKey == null) {
                    return null;
                }
                ItemListing itemListing = this.trades.get(resourceKey);
                if (itemListing == null) {
                    return null;
                }
                return itemListing.getOffer(serverLevel, entity, randomSource);
            }
            return null;
        }
    }

    static class EnchantBookForEmeralds
    implements ItemListing {
        private final int villagerXp;
        private final TagKey<Enchantment> tradeableEnchantments;
        private final int minLevel;
        private final int maxLevel;

        public EnchantBookForEmeralds(int i, TagKey<Enchantment> tagKey) {
            this(i, 0, Integer.MAX_VALUE, tagKey);
        }

        public EnchantBookForEmeralds(int i, int j, int k, TagKey<Enchantment> tagKey) {
            this.minLevel = j;
            this.maxLevel = k;
            this.villagerXp = i;
            this.tradeableEnchantments = tagKey;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            int l;
            ItemStack itemStack;
            Optional optional = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getRandomElementOf(this.tradeableEnchantments, randomSource);
            if (!optional.isEmpty()) {
                Holder holder = (Holder)optional.get();
                Enchantment enchantment = (Enchantment)((Object)holder.value());
                int i = Math.max(enchantment.getMinLevel(), this.minLevel);
                int j = Math.min(enchantment.getMaxLevel(), this.maxLevel);
                int k = Mth.nextInt(randomSource, i, j);
                itemStack = EnchantmentHelper.createBook(new EnchantmentInstance(holder, k));
                l = 2 + randomSource.nextInt(5 + k * 10) + 3 * k;
                if (holder.is(EnchantmentTags.DOUBLE_TRADE_PRICE)) {
                    l *= 2;
                }
                if (l > 64) {
                    l = 64;
                }
            } else {
                l = 1;
                itemStack = new ItemStack(Items.BOOK);
            }
            return new MerchantOffer(new ItemCost(Items.EMERALD, l), Optional.of(new ItemCost(Items.BOOK)), itemStack, 12, this.villagerXp, 0.2f);
        }
    }

    public static interface ItemListing {
        public @Nullable MerchantOffer getOffer(ServerLevel var1, Entity var2, RandomSource var3);
    }

    static class EmeraldForItems
    implements ItemListing {
        private final ItemCost itemStack;
        private final int maxUses;
        private final int villagerXp;
        private final int emeraldAmount;
        private final float priceMultiplier;

        public EmeraldForItems(ItemLike itemLike, int i, int j, int k) {
            this(itemLike, i, j, k, 1);
        }

        public EmeraldForItems(ItemLike itemLike, int i, int j, int k, int l) {
            this(new ItemCost(itemLike.asItem(), i), j, k, l);
        }

        public EmeraldForItems(ItemCost itemCost, int i, int j, int k) {
            this.itemStack = itemCost;
            this.maxUses = i;
            this.villagerXp = j;
            this.emeraldAmount = k;
            this.priceMultiplier = 0.05f;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            return new MerchantOffer(this.itemStack, new ItemStack(Items.EMERALD, this.emeraldAmount), this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class ItemsForEmeralds
    implements ItemListing {
        private final ItemStack itemStack;
        private final int emeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;
        private final Optional<ResourceKey<EnchantmentProvider>> enchantmentProvider;

        public ItemsForEmeralds(Block block, int i, int j, int k, int l) {
            this(new ItemStack(block), i, j, k, l);
        }

        public ItemsForEmeralds(Item item, int i, int j, int k) {
            this(new ItemStack(item), i, j, 12, k);
        }

        public ItemsForEmeralds(Item item, int i, int j, int k, int l) {
            this(new ItemStack(item), i, j, k, l);
        }

        public ItemsForEmeralds(ItemStack itemStack, int i, int j, int k, int l) {
            this(itemStack, i, j, k, l, 0.05f);
        }

        public ItemsForEmeralds(Item item, int i, int j, int k, int l, float f) {
            this(new ItemStack(item), i, j, k, l, f);
        }

        public ItemsForEmeralds(Item item, int i, int j, int k, int l, float f, ResourceKey<EnchantmentProvider> resourceKey) {
            this(new ItemStack(item), i, j, k, l, f, Optional.of(resourceKey));
        }

        public ItemsForEmeralds(ItemStack itemStack, int i, int j, int k, int l, float f) {
            this(itemStack, i, j, k, l, f, Optional.empty());
        }

        public ItemsForEmeralds(ItemStack itemStack, int i, int j, int k, int l, float f, Optional<ResourceKey<EnchantmentProvider>> optional) {
            this.itemStack = itemStack;
            this.emeraldCost = i;
            this.itemStack.setCount(j);
            this.maxUses = k;
            this.villagerXp = l;
            this.priceMultiplier = f;
            this.enchantmentProvider = optional;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            ItemStack itemStack = this.itemStack.copy();
            this.enchantmentProvider.ifPresent(resourceKey -> EnchantmentHelper.enchantItemFromProvider(itemStack, serverLevel.registryAccess(), resourceKey, serverLevel.getCurrentDifficultyAt(entity.blockPosition()), randomSource));
            return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class SuspiciousStewForEmerald
    implements ItemListing {
        private final SuspiciousStewEffects effects;
        private final int xp;
        private final float priceMultiplier;

        public SuspiciousStewForEmerald(Holder<MobEffect> holder, int i, int j) {
            this(new SuspiciousStewEffects(List.of((Object)((Object)new SuspiciousStewEffects.Entry(holder, i)))), j, 0.05f);
        }

        public SuspiciousStewForEmerald(SuspiciousStewEffects suspiciousStewEffects, int i, float f) {
            this.effects = suspiciousStewEffects;
            this.xp = i;
            this.priceMultiplier = f;
        }

        @Override
        public @Nullable MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            ItemStack itemStack = new ItemStack(Items.SUSPICIOUS_STEW, 1);
            itemStack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.effects);
            return new MerchantOffer(new ItemCost(Items.EMERALD), itemStack, 12, this.xp, this.priceMultiplier);
        }
    }

    static class ItemsAndEmeraldsToItems
    implements ItemListing {
        private final ItemCost fromItem;
        private final int emeraldCost;
        private final ItemStack toItem;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;
        private final Optional<ResourceKey<EnchantmentProvider>> enchantmentProvider;

        public ItemsAndEmeraldsToItems(ItemLike itemLike, int i, int j, Item item, int k, int l, int m, float f) {
            this(itemLike, i, j, new ItemStack(item), k, l, m, f);
        }

        private ItemsAndEmeraldsToItems(ItemLike itemLike, int i, int j, ItemStack itemStack, int k, int l, int m, float f) {
            this(new ItemCost(itemLike, i), j, itemStack.copyWithCount(k), l, m, f, Optional.empty());
        }

        ItemsAndEmeraldsToItems(ItemLike itemLike, int i, int j, ItemLike itemLike2, int k, int l, int m, float f, ResourceKey<EnchantmentProvider> resourceKey) {
            this(new ItemCost(itemLike, i), j, new ItemStack(itemLike2, k), l, m, f, Optional.of(resourceKey));
        }

        public ItemsAndEmeraldsToItems(ItemCost itemCost, int i, ItemStack itemStack, int j, int k, float f, Optional<ResourceKey<EnchantmentProvider>> optional) {
            this.fromItem = itemCost;
            this.emeraldCost = i;
            this.toItem = itemStack;
            this.maxUses = j;
            this.villagerXp = k;
            this.priceMultiplier = f;
            this.enchantmentProvider = optional;
        }

        @Override
        public @Nullable MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            ItemStack itemStack = this.toItem.copy();
            this.enchantmentProvider.ifPresent(resourceKey -> EnchantmentHelper.enchantItemFromProvider(itemStack, serverLevel.registryAccess(), resourceKey, serverLevel.getCurrentDifficultyAt(entity.blockPosition()), randomSource));
            return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(this.fromItem), itemStack, 0, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class EnchantedItemForEmeralds
    implements ItemListing {
        private final ItemStack itemStack;
        private final int baseEmeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final float priceMultiplier;

        public EnchantedItemForEmeralds(Item item, int i, int j, int k) {
            this(item, i, j, k, 0.05f);
        }

        public EnchantedItemForEmeralds(Item item, int i, int j, int k, float f) {
            this.itemStack = new ItemStack(item);
            this.baseEmeraldCost = i;
            this.maxUses = j;
            this.villagerXp = k;
            this.priceMultiplier = f;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            int i = 5 + randomSource.nextInt(15);
            RegistryAccess registryAccess = serverLevel.registryAccess();
            Optional optional = registryAccess.lookupOrThrow(Registries.ENCHANTMENT).get(EnchantmentTags.ON_TRADED_EQUIPMENT);
            ItemStack itemStack = EnchantmentHelper.enchantItem(randomSource, new ItemStack(this.itemStack.getItem()), i, registryAccess, optional);
            int j = Math.min(this.baseEmeraldCost + i, 64);
            ItemCost itemCost = new ItemCost(Items.EMERALD, j);
            return new MerchantOffer(itemCost, itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class EmeraldsForVillagerTypeItem
    implements ItemListing {
        private final Map<ResourceKey<VillagerType>, Item> trades;
        private final int cost;
        private final int maxUses;
        private final int villagerXp;

        public EmeraldsForVillagerTypeItem(int i, int j, int k, Map<ResourceKey<VillagerType>, Item> map) {
            BuiltInRegistries.VILLAGER_TYPE.registryKeySet().stream().filter(resourceKey -> !map.containsKey(resourceKey)).findAny().ifPresent(resourceKey -> {
                throw new IllegalStateException("Missing trade for villager type: " + String.valueOf(resourceKey));
            });
            this.trades = map;
            this.cost = i;
            this.maxUses = j;
            this.villagerXp = k;
        }

        @Override
        public @Nullable MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            if (entity instanceof VillagerDataHolder) {
                VillagerDataHolder villagerDataHolder = (VillagerDataHolder)((Object)entity);
                ResourceKey resourceKey = villagerDataHolder.getVillagerData().type().unwrapKey().orElse(null);
                if (resourceKey == null) {
                    return null;
                }
                ItemCost itemCost = new ItemCost(this.trades.get(resourceKey), this.cost);
                return new MerchantOffer(itemCost, new ItemStack(Items.EMERALD), this.maxUses, this.villagerXp, 0.05f);
            }
            return null;
        }
    }

    static class TippedArrowForItemsAndEmeralds
    implements ItemListing {
        private final ItemStack toItem;
        private final int toCount;
        private final int emeraldCost;
        private final int maxUses;
        private final int villagerXp;
        private final Item fromItem;
        private final int fromCount;
        private final float priceMultiplier;

        public TippedArrowForItemsAndEmeralds(Item item, int i, Item item2, int j, int k, int l, int m) {
            this.toItem = new ItemStack(item2);
            this.emeraldCost = k;
            this.maxUses = l;
            this.villagerXp = m;
            this.fromItem = item;
            this.fromCount = i;
            this.toCount = j;
            this.priceMultiplier = 0.05f;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            ItemCost itemCost = new ItemCost(Items.EMERALD, this.emeraldCost);
            List list = BuiltInRegistries.POTION.listElements().filter(reference -> !((Potion)reference.value()).getEffects().isEmpty() && serverLevel.potionBrewing().isBrewablePotion((Holder<Potion>)reference)).collect(Collectors.toList());
            Holder holder = (Holder)Util.getRandom(list, randomSource);
            ItemStack itemStack = new ItemStack(this.toItem.getItem(), this.toCount);
            itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
            return new MerchantOffer(itemCost, Optional.of(new ItemCost(this.fromItem, this.fromCount)), itemStack, this.maxUses, this.villagerXp, this.priceMultiplier);
        }
    }

    static class TreasureMapForEmeralds
    implements ItemListing {
        private final int emeraldCost;
        private final TagKey<Structure> destination;
        private final String displayName;
        private final Holder<MapDecorationType> destinationType;
        private final int maxUses;
        private final int villagerXp;

        public TreasureMapForEmeralds(int i, TagKey<Structure> tagKey, String string, Holder<MapDecorationType> holder, int j, int k) {
            this.emeraldCost = i;
            this.destination = tagKey;
            this.displayName = string;
            this.destinationType = holder;
            this.maxUses = j;
            this.villagerXp = k;
        }

        @Override
        public @Nullable MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            BlockPos blockPos = serverLevel.findNearestMapStructure(this.destination, entity.blockPosition(), 100, true);
            if (blockPos != null) {
                ItemStack itemStack = MapItem.create(serverLevel, blockPos.getX(), blockPos.getZ(), (byte)2, true, true);
                MapItem.renderBiomePreviewMap(serverLevel, itemStack);
                MapItemSavedData.addTargetDecoration(itemStack, blockPos, "+", this.destinationType);
                itemStack.set(DataComponents.ITEM_NAME, Component.translatable(this.displayName));
                return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(new ItemCost(Items.COMPASS)), itemStack, this.maxUses, this.villagerXp, 0.2f);
            }
            return null;
        }
    }

    static class DyedArmorForEmeralds
    implements ItemListing {
        private final Item item;
        private final int value;
        private final int maxUses;
        private final int villagerXp;

        public DyedArmorForEmeralds(Item item, int i) {
            this(item, i, 12, 1);
        }

        public DyedArmorForEmeralds(Item item, int i, int j, int k) {
            this.item = item;
            this.value = i;
            this.maxUses = j;
            this.villagerXp = k;
        }

        @Override
        public MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            ItemCost itemCost = new ItemCost(Items.EMERALD, this.value);
            ItemStack itemStack = new ItemStack(this.item);
            if (itemStack.is(ItemTags.DYEABLE)) {
                ArrayList list = Lists.newArrayList();
                list.add(DyedArmorForEmeralds.getRandomDye(randomSource));
                if (randomSource.nextFloat() > 0.7f) {
                    list.add(DyedArmorForEmeralds.getRandomDye(randomSource));
                }
                if (randomSource.nextFloat() > 0.8f) {
                    list.add(DyedArmorForEmeralds.getRandomDye(randomSource));
                }
                itemStack = DyedItemColor.applyDyes(itemStack, list);
            }
            return new MerchantOffer(itemCost, itemStack, this.maxUses, this.villagerXp, 0.2f);
        }

        private static DyeItem getRandomDye(RandomSource randomSource) {
            return DyeItem.byColor(DyeColor.byId(randomSource.nextInt(16)));
        }
    }

    static class FailureItemListing
    implements ItemListing {
        private FailureItemListing() {
        }

        @Override
        public MerchantOffer getOffer(ServerLevel serverLevel, Entity entity, RandomSource randomSource) {
            return null;
        }
    }
}

