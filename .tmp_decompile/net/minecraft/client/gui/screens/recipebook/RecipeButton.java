/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.recipebook;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

@Environment(value=EnvType.CLIENT)
public class RecipeButton
extends AbstractWidget {
    private static final Identifier SLOT_MANY_CRAFTABLE_SPRITE = Identifier.withDefaultNamespace("recipe_book/slot_many_craftable");
    private static final Identifier SLOT_CRAFTABLE_SPRITE = Identifier.withDefaultNamespace("recipe_book/slot_craftable");
    private static final Identifier SLOT_MANY_UNCRAFTABLE_SPRITE = Identifier.withDefaultNamespace("recipe_book/slot_many_uncraftable");
    private static final Identifier SLOT_UNCRAFTABLE_SPRITE = Identifier.withDefaultNamespace("recipe_book/slot_uncraftable");
    private static final float ANIMATION_TIME = 15.0f;
    private static final int BACKGROUND_SIZE = 25;
    private static final Component MORE_RECIPES_TOOLTIP = Component.translatable("gui.recipebook.moreRecipes");
    private RecipeCollection collection = RecipeCollection.EMPTY;
    private List<ResolvedEntry> selectedEntries = List.of();
    private boolean allRecipesHaveSameResultDisplay;
    private final SlotSelectTime slotSelectTime;
    private float animationTime;

    public RecipeButton(SlotSelectTime slotSelectTime) {
        super(0, 0, 25, 25, CommonComponents.EMPTY);
        this.slotSelectTime = slotSelectTime;
    }

    public void init(RecipeCollection recipeCollection, boolean bl, RecipeBookPage recipeBookPage, ContextMap contextMap) {
        this.collection = recipeCollection;
        List<RecipeDisplayEntry> list = recipeCollection.getSelectedRecipes(bl ? RecipeCollection.CraftableStatus.CRAFTABLE : RecipeCollection.CraftableStatus.ANY);
        this.selectedEntries = list.stream().map(recipeDisplayEntry -> new ResolvedEntry(recipeDisplayEntry.id(), recipeDisplayEntry.resultItems(contextMap))).toList();
        this.allRecipesHaveSameResultDisplay = RecipeButton.allRecipesHaveSameResultDisplay(this.selectedEntries);
        List list2 = list.stream().map(RecipeDisplayEntry::id).filter(recipeBookPage.getRecipeBook()::willHighlight).toList();
        if (!list2.isEmpty()) {
            list2.forEach(recipeBookPage::recipeShown);
            this.animationTime = 15.0f;
        }
    }

    private static boolean allRecipesHaveSameResultDisplay(List<ResolvedEntry> list) {
        Iterator iterator = list.stream().flatMap(resolvedEntry -> resolvedEntry.displayItems().stream()).iterator();
        if (!iterator.hasNext()) {
            return true;
        }
        ItemStack itemStack = (ItemStack)iterator.next();
        while (iterator.hasNext()) {
            ItemStack itemStack2 = (ItemStack)iterator.next();
            if (ItemStack.isSameItemSameComponents(itemStack, itemStack2)) continue;
            return false;
        }
        return true;
    }

    public RecipeCollection getCollection() {
        return this.collection;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        boolean bl;
        Identifier identifier = this.collection.hasCraftable() ? (this.hasMultipleRecipes() ? SLOT_MANY_CRAFTABLE_SPRITE : SLOT_CRAFTABLE_SPRITE) : (this.hasMultipleRecipes() ? SLOT_MANY_UNCRAFTABLE_SPRITE : SLOT_UNCRAFTABLE_SPRITE);
        boolean bl2 = bl = this.animationTime > 0.0f;
        if (bl) {
            float g = 1.0f + 0.1f * (float)Math.sin(this.animationTime / 15.0f * (float)Math.PI);
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(this.getX() + 8), (float)(this.getY() + 12));
            guiGraphics.pose().scale(g, g);
            guiGraphics.pose().translate((float)(-(this.getX() + 8)), (float)(-(this.getY() + 12)));
            this.animationTime -= f;
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.width, this.height);
        ItemStack itemStack = this.getDisplayStack();
        int k = 4;
        if (this.hasMultipleRecipes() && this.allRecipesHaveSameResultDisplay) {
            guiGraphics.renderItem(itemStack, this.getX() + k + 1, this.getY() + k + 1, 0);
            --k;
        }
        guiGraphics.renderFakeItem(itemStack, this.getX() + k, this.getY() + k);
        if (bl) {
            guiGraphics.pose().popMatrix();
        }
    }

    private boolean hasMultipleRecipes() {
        return this.selectedEntries.size() > 1;
    }

    public boolean isOnlyOption() {
        return this.selectedEntries.size() == 1;
    }

    public RecipeDisplayId getCurrentRecipe() {
        int i = this.slotSelectTime.currentIndex() % this.selectedEntries.size();
        return this.selectedEntries.get((int)i).id;
    }

    public ItemStack getDisplayStack() {
        int i = this.slotSelectTime.currentIndex();
        int j = this.selectedEntries.size();
        int k = i / j;
        int l = i - j * k;
        return this.selectedEntries.get(l).selectItem(k);
    }

    public List<Component> getTooltipText(ItemStack itemStack) {
        ArrayList<Component> list = new ArrayList<Component>(Screen.getTooltipFromItem(Minecraft.getInstance(), itemStack));
        if (this.hasMultipleRecipes()) {
            list.add(MORE_RECIPES_TOOLTIP);
        }
        return list;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narration.recipe", this.getDisplayStack().getHoverName()));
        if (this.hasMultipleRecipes()) {
            narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"), Component.translatable("narration.recipe.usage.more"));
        } else {
            narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
        }
    }

    @Override
    public int getWidth() {
        return 25;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo mouseButtonInfo) {
        return mouseButtonInfo.button() == 0 || mouseButtonInfo.button() == 1;
    }

    @Environment(value=EnvType.CLIENT)
    static final class ResolvedEntry
    extends Record {
        final RecipeDisplayId id;
        private final List<ItemStack> displayItems;

        ResolvedEntry(RecipeDisplayId recipeDisplayId, List<ItemStack> list) {
            this.id = recipeDisplayId;
            this.displayItems = list;
        }

        public ItemStack selectItem(int i) {
            if (this.displayItems.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int j = i % this.displayItems.size();
            return this.displayItems.get(j);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResolvedEntry.class, "id;displayItems", "id", "displayItems"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResolvedEntry.class, "id;displayItems", "id", "displayItems"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResolvedEntry.class, "id;displayItems", "id", "displayItems"}, this, object);
        }

        public RecipeDisplayId id() {
            return this.id;
        }

        public List<ItemStack> displayItems() {
            return this.displayItems;
        }
    }
}

