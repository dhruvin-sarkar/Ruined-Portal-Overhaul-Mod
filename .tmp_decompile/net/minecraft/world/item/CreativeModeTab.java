/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class CreativeModeTab {
    static final Identifier DEFAULT_BACKGROUND = CreativeModeTab.createTextureLocation("items");
    private final Component displayName;
    Identifier backgroundTexture = DEFAULT_BACKGROUND;
    boolean canScroll = true;
    boolean showTitle = true;
    boolean alignedRight = false;
    private final Row row;
    private final int column;
    private final Type type;
    private @Nullable ItemStack iconItemStack;
    private Collection<ItemStack> displayItems = ItemStackLinkedSet.createTypeAndComponentsSet();
    private Set<ItemStack> displayItemsSearchTab = ItemStackLinkedSet.createTypeAndComponentsSet();
    private final Supplier<ItemStack> iconGenerator;
    private final DisplayItemsGenerator displayItemsGenerator;

    CreativeModeTab(Row row, int i, Type type, Component component, Supplier<ItemStack> supplier, DisplayItemsGenerator displayItemsGenerator) {
        this.row = row;
        this.column = i;
        this.displayName = component;
        this.iconGenerator = supplier;
        this.displayItemsGenerator = displayItemsGenerator;
        this.type = type;
    }

    public static Identifier createTextureLocation(String string) {
        return Identifier.withDefaultNamespace("textures/gui/container/creative_inventory/tab_" + string + ".png");
    }

    public static Builder builder(Row row, int i) {
        return new Builder(row, i);
    }

    public Component getDisplayName() {
        return this.displayName;
    }

    public ItemStack getIconItem() {
        if (this.iconItemStack == null) {
            this.iconItemStack = this.iconGenerator.get();
        }
        return this.iconItemStack;
    }

    public Identifier getBackgroundTexture() {
        return this.backgroundTexture;
    }

    public boolean showTitle() {
        return this.showTitle;
    }

    public boolean canScroll() {
        return this.canScroll;
    }

    public int column() {
        return this.column;
    }

    public Row row() {
        return this.row;
    }

    public boolean hasAnyItems() {
        return !this.displayItems.isEmpty();
    }

    public boolean shouldDisplay() {
        return this.type != Type.CATEGORY || this.hasAnyItems();
    }

    public boolean isAlignedRight() {
        return this.alignedRight;
    }

    public Type getType() {
        return this.type;
    }

    public void buildContents(ItemDisplayParameters itemDisplayParameters) {
        ItemDisplayBuilder itemDisplayBuilder = new ItemDisplayBuilder(this, itemDisplayParameters.enabledFeatures);
        ResourceKey<CreativeModeTab> resourceKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(this).orElseThrow(() -> new IllegalStateException("Unregistered creative tab: " + String.valueOf(this)));
        this.displayItemsGenerator.accept(itemDisplayParameters, itemDisplayBuilder);
        this.displayItems = itemDisplayBuilder.tabContents;
        this.displayItemsSearchTab = itemDisplayBuilder.searchTabContents;
    }

    public Collection<ItemStack> getDisplayItems() {
        return this.displayItems;
    }

    public Collection<ItemStack> getSearchTabDisplayItems() {
        return this.displayItemsSearchTab;
    }

    public boolean contains(ItemStack itemStack) {
        return this.displayItemsSearchTab.contains(itemStack);
    }

    public static enum Row {
        TOP,
        BOTTOM;

    }

    @FunctionalInterface
    public static interface DisplayItemsGenerator {
        public void accept(ItemDisplayParameters var1, Output var2);
    }

    public static enum Type {
        CATEGORY,
        INVENTORY,
        HOTBAR,
        SEARCH;

    }

    public static class Builder {
        private static final DisplayItemsGenerator EMPTY_GENERATOR = (itemDisplayParameters, output) -> {};
        private final Row row;
        private final int column;
        private Component displayName = Component.empty();
        private Supplier<ItemStack> iconGenerator = () -> ItemStack.EMPTY;
        private DisplayItemsGenerator displayItemsGenerator = EMPTY_GENERATOR;
        private boolean canScroll = true;
        private boolean showTitle = true;
        private boolean alignedRight = false;
        private Type type = Type.CATEGORY;
        private Identifier backgroundTexture = DEFAULT_BACKGROUND;

        public Builder(Row row, int i) {
            this.row = row;
            this.column = i;
        }

        public Builder title(Component component) {
            this.displayName = component;
            return this;
        }

        public Builder icon(Supplier<ItemStack> supplier) {
            this.iconGenerator = supplier;
            return this;
        }

        public Builder displayItems(DisplayItemsGenerator displayItemsGenerator) {
            this.displayItemsGenerator = displayItemsGenerator;
            return this;
        }

        public Builder alignedRight() {
            this.alignedRight = true;
            return this;
        }

        public Builder hideTitle() {
            this.showTitle = false;
            return this;
        }

        public Builder noScrollBar() {
            this.canScroll = false;
            return this;
        }

        protected Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder backgroundTexture(Identifier identifier) {
            this.backgroundTexture = identifier;
            return this;
        }

        public CreativeModeTab build() {
            if ((this.type == Type.HOTBAR || this.type == Type.INVENTORY) && this.displayItemsGenerator != EMPTY_GENERATOR) {
                throw new IllegalStateException("Special tabs can't have display items");
            }
            CreativeModeTab creativeModeTab = new CreativeModeTab(this.row, this.column, this.type, this.displayName, this.iconGenerator, this.displayItemsGenerator);
            creativeModeTab.alignedRight = this.alignedRight;
            creativeModeTab.showTitle = this.showTitle;
            creativeModeTab.canScroll = this.canScroll;
            creativeModeTab.backgroundTexture = this.backgroundTexture;
            return creativeModeTab;
        }
    }

    static class ItemDisplayBuilder
    implements Output {
        public final Collection<ItemStack> tabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        public final Set<ItemStack> searchTabContents = ItemStackLinkedSet.createTypeAndComponentsSet();
        private final CreativeModeTab tab;
        private final FeatureFlagSet featureFlagSet;

        public ItemDisplayBuilder(CreativeModeTab creativeModeTab, FeatureFlagSet featureFlagSet) {
            this.tab = creativeModeTab;
            this.featureFlagSet = featureFlagSet;
        }

        @Override
        public void accept(ItemStack itemStack, TabVisibility tabVisibility) {
            boolean bl;
            if (itemStack.getCount() != 1) {
                throw new IllegalArgumentException("Stack size must be exactly 1");
            }
            boolean bl2 = bl = this.tabContents.contains(itemStack) && tabVisibility != TabVisibility.SEARCH_TAB_ONLY;
            if (bl) {
                throw new IllegalStateException("Accidentally adding the same item stack twice " + itemStack.getDisplayName().getString() + " to a Creative Mode Tab: " + this.tab.getDisplayName().getString());
            }
            if (itemStack.getItem().isEnabled(this.featureFlagSet)) {
                switch (tabVisibility.ordinal()) {
                    case 0: {
                        this.tabContents.add(itemStack);
                        this.searchTabContents.add(itemStack);
                        break;
                    }
                    case 1: {
                        this.tabContents.add(itemStack);
                        break;
                    }
                    case 2: {
                        this.searchTabContents.add(itemStack);
                    }
                }
            }
        }
    }

    public static final class ItemDisplayParameters
    extends Record {
        final FeatureFlagSet enabledFeatures;
        private final boolean hasPermissions;
        private final HolderLookup.Provider holders;

        public ItemDisplayParameters(FeatureFlagSet featureFlagSet, boolean bl, HolderLookup.Provider provider) {
            this.enabledFeatures = featureFlagSet;
            this.hasPermissions = bl;
            this.holders = provider;
        }

        public boolean needsUpdate(FeatureFlagSet featureFlagSet, boolean bl, HolderLookup.Provider provider) {
            return !this.enabledFeatures.equals(featureFlagSet) || this.hasPermissions != bl || this.holders != provider;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ItemDisplayParameters.class, "enabledFeatures;hasPermissions;holders", "enabledFeatures", "hasPermissions", "holders"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ItemDisplayParameters.class, "enabledFeatures;hasPermissions;holders", "enabledFeatures", "hasPermissions", "holders"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ItemDisplayParameters.class, "enabledFeatures;hasPermissions;holders", "enabledFeatures", "hasPermissions", "holders"}, this, object);
        }

        public FeatureFlagSet enabledFeatures() {
            return this.enabledFeatures;
        }

        public boolean hasPermissions() {
            return this.hasPermissions;
        }

        public HolderLookup.Provider holders() {
            return this.holders;
        }
    }

    public static interface Output {
        public void accept(ItemStack var1, TabVisibility var2);

        default public void accept(ItemStack itemStack) {
            this.accept(itemStack, TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default public void accept(ItemLike itemLike, TabVisibility tabVisibility) {
            this.accept(new ItemStack(itemLike), tabVisibility);
        }

        default public void accept(ItemLike itemLike) {
            this.accept(new ItemStack(itemLike), TabVisibility.PARENT_AND_SEARCH_TABS);
        }

        default public void acceptAll(Collection<ItemStack> collection, TabVisibility tabVisibility) {
            collection.forEach(itemStack -> this.accept((ItemStack)itemStack, tabVisibility));
        }

        default public void acceptAll(Collection<ItemStack> collection) {
            this.acceptAll(collection, TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    protected static enum TabVisibility {
        PARENT_AND_SEARCH_TABS,
        PARENT_TAB_ONLY,
        SEARCH_TAB_ONLY;

    }
}

