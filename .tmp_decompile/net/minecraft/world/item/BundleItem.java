/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  org.apache.commons.lang3.math.Fraction
 */
package net.minecraft.world.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem
extends Item {
    public static final int MAX_SHOWN_GRID_ITEMS_X = 4;
    public static final int MAX_SHOWN_GRID_ITEMS_Y = 3;
    public static final int MAX_SHOWN_GRID_ITEMS = 12;
    public static final int OVERFLOWING_MAX_SHOWN_GRID_ITEMS = 11;
    private static final int FULL_BAR_COLOR = ARGB.colorFromFloat(1.0f, 1.0f, 0.33f, 0.33f);
    private static final int BAR_COLOR = ARGB.colorFromFloat(1.0f, 0.44f, 0.53f, 1.0f);
    private static final int TICKS_AFTER_FIRST_THROW = 10;
    private static final int TICKS_BETWEEN_THROWS = 2;
    private static final int TICKS_MAX_THROW_DURATION = 200;

    public BundleItem(Item.Properties properties) {
        super(properties);
    }

    public static float getFullnessDisplay(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return bundleContents.weight().floatValue();
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction clickAction, Player player) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return false;
        }
        ItemStack itemStack2 = slot.getItem();
        BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
        if (clickAction == ClickAction.PRIMARY && !itemStack2.isEmpty()) {
            if (mutable.tryTransfer(slot, player) > 0) {
                BundleItem.playInsertSound(player);
            } else {
                BundleItem.playInsertFailSound(player);
            }
            itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        }
        if (clickAction == ClickAction.SECONDARY && itemStack2.isEmpty()) {
            ItemStack itemStack3 = mutable.removeOne();
            if (itemStack3 != null) {
                ItemStack itemStack4 = slot.safeInsert(itemStack3);
                if (itemStack4.getCount() > 0) {
                    mutable.tryInsert(itemStack4);
                } else {
                    BundleItem.playRemoveOneSound(player);
                }
            }
            itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack itemStack, ItemStack itemStack2, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (clickAction == ClickAction.PRIMARY && itemStack2.isEmpty()) {
            BundleItem.toggleSelectedItem(itemStack, -1);
            return false;
        }
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return false;
        }
        BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
        if (clickAction == ClickAction.PRIMARY && !itemStack2.isEmpty()) {
            if (slot.allowModification(player) && mutable.tryInsert(itemStack2) > 0) {
                BundleItem.playInsertSound(player);
            } else {
                BundleItem.playInsertFailSound(player);
            }
            itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        }
        if (clickAction == ClickAction.SECONDARY && itemStack2.isEmpty()) {
            ItemStack itemStack3;
            if (slot.allowModification(player) && (itemStack3 = mutable.removeOne()) != null) {
                BundleItem.playRemoveOneSound(player);
                slotAccess.set(itemStack3);
            }
            itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
            this.broadcastChangesOnContainerMenu(player);
            return true;
        }
        BundleItem.toggleSelectedItem(itemStack, -1);
        return false;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand interactionHand) {
        player.startUsingItem(interactionHand);
        return InteractionResult.SUCCESS;
    }

    private void dropContent(Level level, Player player, ItemStack itemStack) {
        if (this.dropContent(itemStack, player)) {
            BundleItem.playDropContentsSound(level, player);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return bundleContents.weight().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getBarWidth(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return Math.min(1 + Mth.mulAndTruncate(bundleContents.weight(), 12), 13);
    }

    @Override
    public int getBarColor(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return bundleContents.weight().compareTo(Fraction.ONE) >= 0 ? FULL_BAR_COLOR : BAR_COLOR;
    }

    public static void toggleSelectedItem(ItemStack itemStack, int i) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return;
        }
        BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
        mutable.toggleSelectedItem(i);
        itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
    }

    public static boolean hasSelectedItem(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        return bundleContents != null && bundleContents.getSelectedItem() != -1;
    }

    public static int getSelectedItem(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return bundleContents.getSelectedItem();
    }

    public static ItemStack getSelectedItemStack(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents != null && bundleContents.getSelectedItem() != -1) {
            return bundleContents.getItemUnsafe(bundleContents.getSelectedItem());
        }
        return ItemStack.EMPTY;
    }

    public static int getNumberOfItemsToShow(ItemStack itemStack) {
        BundleContents bundleContents = itemStack.getOrDefault(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        return bundleContents.getNumberOfItemsToShow();
    }

    private boolean dropContent(ItemStack itemStack, Player player) {
        BundleContents bundleContents = itemStack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null || bundleContents.isEmpty()) {
            return false;
        }
        Optional<ItemStack> optional = BundleItem.removeOneItemFromBundle(itemStack, player, bundleContents);
        if (optional.isPresent()) {
            player.drop(optional.get(), true);
            return true;
        }
        return false;
    }

    private static Optional<ItemStack> removeOneItemFromBundle(ItemStack itemStack, Player player, BundleContents bundleContents) {
        BundleContents.Mutable mutable = new BundleContents.Mutable(bundleContents);
        ItemStack itemStack2 = mutable.removeOne();
        if (itemStack2 != null) {
            BundleItem.playRemoveOneSound(player);
            itemStack.set(DataComponents.BUNDLE_CONTENTS, mutable.toImmutable());
            return Optional.of(itemStack2);
        }
        return Optional.empty();
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int i) {
        if (livingEntity instanceof Player) {
            boolean bl;
            Player player = (Player)livingEntity;
            int j = this.getUseDuration(itemStack, livingEntity);
            boolean bl2 = bl = i == j;
            if (bl || i < j - 10 && i % 2 == 0) {
                this.dropContent(level, player, itemStack);
            }
        }
    }

    @Override
    public int getUseDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return 200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
        return ItemUseAnimation.BUNDLE;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        TooltipDisplay tooltipDisplay = itemStack.getOrDefault(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT);
        if (!tooltipDisplay.shows(DataComponents.BUNDLE_CONTENTS)) {
            return Optional.empty();
        }
        return Optional.ofNullable(itemStack.get(DataComponents.BUNDLE_CONTENTS)).map(BundleTooltip::new);
    }

    @Override
    public void onDestroyed(ItemEntity itemEntity) {
        BundleContents bundleContents = itemEntity.getItem().get(DataComponents.BUNDLE_CONTENTS);
        if (bundleContents == null) {
            return;
        }
        itemEntity.getItem().set(DataComponents.BUNDLE_CONTENTS, BundleContents.EMPTY);
        ItemUtils.onContainerDestroyed(itemEntity, bundleContents.itemsCopy());
    }

    public static List<BundleItem> getAllBundleItemColors() {
        return Stream.of(Items.BUNDLE, Items.WHITE_BUNDLE, Items.ORANGE_BUNDLE, Items.MAGENTA_BUNDLE, Items.LIGHT_BLUE_BUNDLE, Items.YELLOW_BUNDLE, Items.LIME_BUNDLE, Items.PINK_BUNDLE, Items.GRAY_BUNDLE, Items.LIGHT_GRAY_BUNDLE, Items.CYAN_BUNDLE, Items.BLACK_BUNDLE, Items.BROWN_BUNDLE, Items.GREEN_BUNDLE, Items.RED_BUNDLE, Items.BLUE_BUNDLE, Items.PURPLE_BUNDLE).map(item -> (BundleItem)item).toList();
    }

    public static Item getByColor(DyeColor dyeColor) {
        return switch (dyeColor) {
            default -> throw new MatchException(null, null);
            case DyeColor.WHITE -> Items.WHITE_BUNDLE;
            case DyeColor.ORANGE -> Items.ORANGE_BUNDLE;
            case DyeColor.MAGENTA -> Items.MAGENTA_BUNDLE;
            case DyeColor.LIGHT_BLUE -> Items.LIGHT_BLUE_BUNDLE;
            case DyeColor.YELLOW -> Items.YELLOW_BUNDLE;
            case DyeColor.LIME -> Items.LIME_BUNDLE;
            case DyeColor.PINK -> Items.PINK_BUNDLE;
            case DyeColor.GRAY -> Items.GRAY_BUNDLE;
            case DyeColor.LIGHT_GRAY -> Items.LIGHT_GRAY_BUNDLE;
            case DyeColor.CYAN -> Items.CYAN_BUNDLE;
            case DyeColor.BLUE -> Items.BLUE_BUNDLE;
            case DyeColor.BROWN -> Items.BROWN_BUNDLE;
            case DyeColor.GREEN -> Items.GREEN_BUNDLE;
            case DyeColor.RED -> Items.RED_BUNDLE;
            case DyeColor.BLACK -> Items.BLACK_BUNDLE;
            case DyeColor.PURPLE -> Items.PURPLE_BUNDLE;
        };
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + entity.level().getRandom().nextFloat() * 0.4f);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + entity.level().getRandom().nextFloat() * 0.4f);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1.0f, 1.0f);
    }

    private static void playDropContentsSound(Level level, Entity entity) {
        level.playSound(null, entity.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 0.8f, 0.8f + entity.level().getRandom().nextFloat() * 0.4f);
    }

    private void broadcastChangesOnContainerMenu(Player player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (abstractContainerMenu != null) {
            abstractContainerMenu.slotsChanged(player.getInventory());
        }
    }
}

