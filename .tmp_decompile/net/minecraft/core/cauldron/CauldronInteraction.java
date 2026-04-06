/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.core.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;

public interface CauldronInteraction {
    public static final Map<String, InteractionMap> INTERACTIONS = new Object2ObjectArrayMap();
    public static final Codec<InteractionMap> CODEC = Codec.stringResolver(InteractionMap::name, INTERACTIONS::get);
    public static final InteractionMap EMPTY = CauldronInteraction.newInteractionMap("empty");
    public static final InteractionMap WATER = CauldronInteraction.newInteractionMap("water");
    public static final InteractionMap LAVA = CauldronInteraction.newInteractionMap("lava");
    public static final InteractionMap POWDER_SNOW = CauldronInteraction.newInteractionMap("powder_snow");

    public static InteractionMap newInteractionMap(String string) {
        Object2ObjectOpenHashMap object2ObjectOpenHashMap = new Object2ObjectOpenHashMap();
        object2ObjectOpenHashMap.defaultReturnValue((blockState, level, blockPos, player, interactionHand, itemStack) -> InteractionResult.TRY_WITH_EMPTY_HAND);
        InteractionMap interactionMap = new InteractionMap(string, (Map<Item, CauldronInteraction>)object2ObjectOpenHashMap);
        INTERACTIONS.put(string, interactionMap);
        return interactionMap;
    }

    public InteractionResult interact(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, ItemStack var6);

    public static void bootStrap() {
        Map<Item, CauldronInteraction> map = EMPTY.map();
        CauldronInteraction.addDefaultInteractions(map);
        map.put(Items.POTION, (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null || !potionContents.is(Potions.WATER)) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(blockPos, Blocks.WATER_CAULDRON.defaultBlockState());
                level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
            }
            return InteractionResult.SUCCESS;
        });
        Map<Item, CauldronInteraction> map2 = WATER.map();
        CauldronInteraction.addDefaultInteractions(map2);
        map2.put(Items.BUCKET, (blockState2, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.fillBucket(blockState2, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.WATER_BUCKET), blockState -> blockState.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        map2.put(Items.GLASS_BOTTLE, (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            if (!level.isClientSide()) {
                Item item = itemStack.getItem();
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, PotionContents.createItemStack(Items.POTION, Potions.WATER)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
                level.playSound(null, blockPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
            }
            return InteractionResult.SUCCESS;
        });
        map2.put(Items.POTION, (blockState, level, blockPos, player, interactionHand, itemStack) -> {
            if (blockState.getValue(LayeredCauldronBlock.LEVEL) == 3) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            PotionContents potionContents = itemStack.get(DataComponents.POTION_CONTENTS);
            if (potionContents == null || !potionContents.is(Potions.WATER)) {
                return InteractionResult.TRY_WITH_EMPTY_HAND;
            }
            if (!level.isClientSide()) {
                player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                level.setBlockAndUpdate(blockPos, (BlockState)blockState.cycle(LayeredCauldronBlock.LEVEL));
                level.playSound(null, blockPos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
            }
            return InteractionResult.SUCCESS;
        });
        map2.put(Items.LEATHER_BOOTS, CauldronInteraction::dyedItemIteration);
        map2.put(Items.LEATHER_LEGGINGS, CauldronInteraction::dyedItemIteration);
        map2.put(Items.LEATHER_CHESTPLATE, CauldronInteraction::dyedItemIteration);
        map2.put(Items.LEATHER_HELMET, CauldronInteraction::dyedItemIteration);
        map2.put(Items.LEATHER_HORSE_ARMOR, CauldronInteraction::dyedItemIteration);
        map2.put(Items.WOLF_ARMOR, CauldronInteraction::dyedItemIteration);
        map2.put(Items.WHITE_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.GRAY_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.BLACK_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.BLUE_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.BROWN_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.CYAN_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.GREEN_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.LIGHT_BLUE_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.LIGHT_GRAY_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.LIME_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.MAGENTA_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.ORANGE_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.PINK_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.PURPLE_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.RED_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.YELLOW_BANNER, CauldronInteraction::bannerInteraction);
        map2.put(Items.WHITE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.GRAY_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.BLACK_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.BLUE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.BROWN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.CYAN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.GREEN_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.LIGHT_BLUE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.LIGHT_GRAY_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.LIME_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.MAGENTA_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.ORANGE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.PINK_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.PURPLE_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.RED_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        map2.put(Items.YELLOW_SHULKER_BOX, CauldronInteraction::shulkerBoxInteraction);
        Map<Item, CauldronInteraction> map3 = LAVA.map();
        map3.put(Items.BUCKET, (blockState2, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.fillBucket(blockState2, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.LAVA_BUCKET), blockState -> true, SoundEvents.BUCKET_FILL_LAVA));
        CauldronInteraction.addDefaultInteractions(map3);
        Map<Item, CauldronInteraction> map4 = POWDER_SNOW.map();
        map4.put(Items.BUCKET, (blockState2, level, blockPos, player, interactionHand, itemStack) -> CauldronInteraction.fillBucket(blockState2, level, blockPos, player, interactionHand, itemStack, new ItemStack(Items.POWDER_SNOW_BUCKET), blockState -> blockState.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL_POWDER_SNOW));
        CauldronInteraction.addDefaultInteractions(map4);
    }

    public static void addDefaultInteractions(Map<Item, CauldronInteraction> map) {
        map.put(Items.LAVA_BUCKET, CauldronInteraction::fillLavaInteraction);
        map.put(Items.WATER_BUCKET, CauldronInteraction::fillWaterInteraction);
        map.put(Items.POWDER_SNOW_BUCKET, CauldronInteraction::fillPowderSnowInteraction);
    }

    public static InteractionResult fillBucket(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack, ItemStack itemStack2, Predicate<BlockState> predicate, SoundEvent soundEvent) {
        if (!predicate.test(blockState)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            Item item = itemStack.getItem();
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2));
            player.awardStat(Stats.USE_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(blockPos, Blocks.CAULDRON.defaultBlockState());
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(null, GameEvent.FLUID_PICKUP, blockPos);
        }
        return InteractionResult.SUCCESS;
    }

    public static InteractionResult emptyBucket(Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack, BlockState blockState, SoundEvent soundEvent) {
        if (!level.isClientSide()) {
            Item item = itemStack.getItem();
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, new ItemStack(Items.BUCKET)));
            player.awardStat(Stats.FILL_CAULDRON);
            player.awardStat(Stats.ITEM_USED.get(item));
            level.setBlockAndUpdate(blockPos, blockState);
            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, 1.0f, 1.0f);
            level.gameEvent(null, GameEvent.FLUID_PLACE, blockPos);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult fillWaterInteraction(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        return CauldronInteraction.emptyBucket(level, blockPos, player, interactionHand, itemStack, (BlockState)Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY);
    }

    private static InteractionResult fillLavaInteraction(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        return CauldronInteraction.isUnderWater(level, blockPos) ? InteractionResult.CONSUME : CauldronInteraction.emptyBucket(level, blockPos, player, interactionHand, itemStack, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
    }

    private static InteractionResult fillPowderSnowInteraction(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        return CauldronInteraction.isUnderWater(level, blockPos) ? InteractionResult.CONSUME : CauldronInteraction.emptyBucket(level, blockPos, player, interactionHand, itemStack, (BlockState)Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY_POWDER_SNOW);
    }

    private static InteractionResult shulkerBoxInteraction(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        Block block = Block.byItem(itemStack.getItem());
        if (!(block instanceof ShulkerBoxBlock)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            ItemStack itemStack2 = itemStack.transmuteCopy(Blocks.SHULKER_BOX, 1);
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2, false));
            player.awardStat(Stats.CLEAN_SHULKER_BOX);
            LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult bannerInteraction(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        BannerPatternLayers bannerPatternLayers = itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        if (bannerPatternLayers.layers().isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            ItemStack itemStack2 = itemStack.copyWithCount(1);
            itemStack2.set(DataComponents.BANNER_PATTERNS, bannerPatternLayers.removeLast());
            player.setItemInHand(interactionHand, ItemUtils.createFilledResult(itemStack, player, itemStack2, false));
            player.awardStat(Stats.CLEAN_BANNER);
            LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
        }
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult dyedItemIteration(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, ItemStack itemStack) {
        if (!itemStack.is(ItemTags.DYEABLE)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!itemStack.has(DataComponents.DYED_COLOR)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!level.isClientSide()) {
            itemStack.remove(DataComponents.DYED_COLOR);
            player.awardStat(Stats.CLEAN_ARMOR);
            LayeredCauldronBlock.lowerFillLevel(blockState, level, blockPos);
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean isUnderWater(Level level, BlockPos blockPos) {
        FluidState fluidState = level.getFluidState(blockPos.above());
        return fluidState.is(FluidTags.WATER);
    }

    public record InteractionMap(String name, Map<Item, CauldronInteraction> map) {
    }
}

