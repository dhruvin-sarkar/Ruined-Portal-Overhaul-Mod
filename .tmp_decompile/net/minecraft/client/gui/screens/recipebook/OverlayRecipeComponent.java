/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.recipebook;

import com.google.common.collect.Lists;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OverlayRecipeComponent
implements Renderable,
GuiEventListener {
    private static final Identifier OVERLAY_RECIPE_SPRITE = Identifier.withDefaultNamespace("recipe_book/overlay_recipe");
    private static final int MAX_ROW = 4;
    private static final int MAX_ROW_LARGE = 5;
    private static final float ITEM_RENDER_SCALE = 0.375f;
    public static final int BUTTON_SIZE = 25;
    private final List<OverlayRecipeButton> recipeButtons = Lists.newArrayList();
    private boolean isVisible;
    private int x;
    private int y;
    private RecipeCollection collection = RecipeCollection.EMPTY;
    private @Nullable RecipeDisplayId lastRecipeClicked;
    final SlotSelectTime slotSelectTime;
    private final boolean isFurnaceMenu;

    public OverlayRecipeComponent(SlotSelectTime slotSelectTime, boolean bl) {
        this.slotSelectTime = slotSelectTime;
        this.isFurnaceMenu = bl;
    }

    public void init(RecipeCollection recipeCollection, ContextMap contextMap, boolean bl, int i, int j, int k, int l, float f) {
        float t;
        float s;
        float r;
        float q;
        float h;
        this.collection = recipeCollection;
        List<RecipeDisplayEntry> list = recipeCollection.getSelectedRecipes(RecipeCollection.CraftableStatus.CRAFTABLE);
        List list2 = bl ? Collections.emptyList() : recipeCollection.getSelectedRecipes(RecipeCollection.CraftableStatus.NOT_CRAFTABLE);
        int m = list.size();
        int n = m + list2.size();
        int o = n <= 16 ? 4 : 5;
        int p = (int)Math.ceil((float)n / (float)o);
        this.x = i;
        this.y = j;
        float g = this.x + Math.min(n, o) * 25;
        if (g > (h = (float)(k + 50))) {
            this.x = (int)((float)this.x - f * (float)((int)((g - h) / f)));
        }
        if ((q = (float)(this.y + p * 25)) > (r = (float)(l + 50))) {
            this.y = (int)((float)this.y - f * (float)Mth.ceil((q - r) / f));
        }
        if ((s = (float)this.y) < (t = (float)(l - 100))) {
            this.y = (int)((float)this.y - f * (float)Mth.ceil((s - t) / f));
        }
        this.isVisible = true;
        this.recipeButtons.clear();
        for (int u = 0; u < n; ++u) {
            boolean bl2 = u < m;
            RecipeDisplayEntry recipeDisplayEntry = bl2 ? list.get(u) : (RecipeDisplayEntry)((Object)list2.get(u - m));
            int v = this.x + 4 + 25 * (u % o);
            int w = this.y + 5 + 25 * (u / o);
            if (this.isFurnaceMenu) {
                this.recipeButtons.add(new OverlaySmeltingRecipeButton(this, v, w, recipeDisplayEntry.id(), recipeDisplayEntry.display(), contextMap, bl2));
                continue;
            }
            this.recipeButtons.add(new OverlayCraftingRecipeButton(this, v, w, recipeDisplayEntry.id(), recipeDisplayEntry.display(), contextMap, bl2));
        }
        this.lastRecipeClicked = null;
    }

    public RecipeCollection getRecipeCollection() {
        return this.collection;
    }

    public @Nullable RecipeDisplayId getLastRecipeClicked() {
        return this.lastRecipeClicked;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (mouseButtonEvent.button() != 0) {
            return false;
        }
        for (OverlayRecipeButton overlayRecipeButton : this.recipeButtons) {
            if (!overlayRecipeButton.mouseClicked(mouseButtonEvent, bl)) continue;
            this.lastRecipeClicked = overlayRecipeButton.recipe;
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double d, double e) {
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (!this.isVisible) {
            return;
        }
        int k = this.recipeButtons.size() <= 16 ? 4 : 5;
        int l = Math.min(this.recipeButtons.size(), k);
        int m = Mth.ceil((float)this.recipeButtons.size() / (float)k);
        int n = 4;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, OVERLAY_RECIPE_SPRITE, this.x, this.y, l * 25 + 8, m * 25 + 8);
        for (OverlayRecipeButton overlayRecipeButton : this.recipeButtons) {
            overlayRecipeButton.render(guiGraphics, i, j, f);
        }
    }

    public void setVisible(boolean bl) {
        this.isVisible = bl;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    @Override
    public void setFocused(boolean bl) {
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    class OverlaySmeltingRecipeButton
    extends OverlayRecipeButton {
        private static final Identifier ENABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay");
        private static final Identifier HIGHLIGHTED_ENABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay_highlighted");
        private static final Identifier DISABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay_disabled");
        private static final Identifier HIGHLIGHTED_DISABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/furnace_overlay_disabled_highlighted");

        public OverlaySmeltingRecipeButton(OverlayRecipeComponent overlayRecipeComponent, int i, int j, RecipeDisplayId recipeDisplayId, RecipeDisplay recipeDisplay, ContextMap contextMap, boolean bl) {
            super(i, j, recipeDisplayId, bl, OverlaySmeltingRecipeButton.calculateIngredientsPositions(recipeDisplay, contextMap));
        }

        private static List<OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeDisplay recipeDisplay, ContextMap contextMap) {
            FurnaceRecipeDisplay furnaceRecipeDisplay;
            List<ItemStack> list;
            if (recipeDisplay instanceof FurnaceRecipeDisplay && !(list = (furnaceRecipeDisplay = (FurnaceRecipeDisplay)recipeDisplay).ingredient().resolveForStacks(contextMap)).isEmpty()) {
                return List.of((Object)((Object)OverlaySmeltingRecipeButton.createGridPos(1, 1, list)));
            }
            return List.of();
        }

        @Override
        protected Identifier getSprite(boolean bl) {
            if (bl) {
                return this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
            }
            return this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
        }
    }

    @Environment(value=EnvType.CLIENT)
    class OverlayCraftingRecipeButton
    extends OverlayRecipeButton {
        private static final Identifier ENABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/crafting_overlay");
        private static final Identifier HIGHLIGHTED_ENABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/crafting_overlay_highlighted");
        private static final Identifier DISABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/crafting_overlay_disabled");
        private static final Identifier HIGHLIGHTED_DISABLED_SPRITE = Identifier.withDefaultNamespace("recipe_book/crafting_overlay_disabled_highlighted");
        private static final int GRID_WIDTH = 3;
        private static final int GRID_HEIGHT = 3;

        public OverlayCraftingRecipeButton(OverlayRecipeComponent overlayRecipeComponent, int i, int j, RecipeDisplayId recipeDisplayId, RecipeDisplay recipeDisplay, ContextMap contextMap, boolean bl) {
            super(i, j, recipeDisplayId, bl, OverlayCraftingRecipeButton.calculateIngredientsPositions(recipeDisplay, contextMap));
        }

        private static List<OverlayRecipeButton.Pos> calculateIngredientsPositions(RecipeDisplay recipeDisplay, ContextMap contextMap) {
            ArrayList<OverlayRecipeButton.Pos> list = new ArrayList<OverlayRecipeButton.Pos>();
            RecipeDisplay recipeDisplay2 = recipeDisplay;
            Objects.requireNonNull(recipeDisplay2);
            RecipeDisplay recipeDisplay3 = recipeDisplay2;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ShapedCraftingRecipeDisplay.class, ShapelessCraftingRecipeDisplay.class}, (Object)recipeDisplay3, (int)n)) {
                case 0: {
                    ShapedCraftingRecipeDisplay shapedCraftingRecipeDisplay = (ShapedCraftingRecipeDisplay)recipeDisplay3;
                    PlaceRecipeHelper.placeRecipe(3, 3, shapedCraftingRecipeDisplay.width(), shapedCraftingRecipeDisplay.height(), shapedCraftingRecipeDisplay.ingredients(), (slotDisplay, i, j, k) -> {
                        List<ItemStack> list2 = slotDisplay.resolveForStacks(contextMap);
                        if (!list2.isEmpty()) {
                            list.add(OverlayCraftingRecipeButton.createGridPos(j, k, list2));
                        }
                    });
                    break;
                }
                case 1: {
                    ShapelessCraftingRecipeDisplay shapelessCraftingRecipeDisplay = (ShapelessCraftingRecipeDisplay)recipeDisplay3;
                    List<SlotDisplay> list2 = shapelessCraftingRecipeDisplay.ingredients();
                    for (int i2 = 0; i2 < list2.size(); ++i2) {
                        List<ItemStack> list3 = list2.get(i2).resolveForStacks(contextMap);
                        if (list3.isEmpty()) continue;
                        list.add(OverlayCraftingRecipeButton.createGridPos(i2 % 3, i2 / 3, list3));
                    }
                    break;
                }
            }
            return list;
        }

        @Override
        protected Identifier getSprite(boolean bl) {
            if (bl) {
                return this.isHoveredOrFocused() ? HIGHLIGHTED_ENABLED_SPRITE : ENABLED_SPRITE;
            }
            return this.isHoveredOrFocused() ? HIGHLIGHTED_DISABLED_SPRITE : DISABLED_SPRITE;
        }
    }

    @Environment(value=EnvType.CLIENT)
    abstract class OverlayRecipeButton
    extends AbstractWidget {
        final RecipeDisplayId recipe;
        private final boolean isCraftable;
        private final List<Pos> slots;

        public OverlayRecipeButton(int i, int j, RecipeDisplayId recipeDisplayId, boolean bl, List<Pos> list) {
            super(i, j, 24, 24, CommonComponents.EMPTY);
            this.slots = list;
            this.recipe = recipeDisplayId;
            this.isCraftable = bl;
        }

        protected static Pos createGridPos(int i, int j, List<ItemStack> list) {
            return new Pos(3 + i * 7, 3 + j * 7, list);
        }

        protected abstract Identifier getSprite(boolean var1);

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(this.isCraftable), this.getX(), this.getY(), this.width, this.height);
            float g = this.getX() + 2;
            float h = this.getY() + 2;
            for (Pos pos : this.slots) {
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().translate(g + (float)pos.x, h + (float)pos.y);
                guiGraphics.pose().scale(0.375f, 0.375f);
                guiGraphics.pose().translate(-8.0f, -8.0f);
                guiGraphics.renderItem(pos.selectIngredient(OverlayRecipeComponent.this.slotSelectTime.currentIndex()), 0, 0);
                guiGraphics.pose().popMatrix();
            }
        }

        @Environment(value=EnvType.CLIENT)
        protected static final class Pos
        extends Record {
            final int x;
            final int y;
            private final List<ItemStack> ingredients;

            public Pos(int i, int j, List<ItemStack> list) {
                if (list.isEmpty()) {
                    throw new IllegalArgumentException("Ingredient list must be non-empty");
                }
                this.x = i;
                this.y = j;
                this.ingredients = list;
            }

            public ItemStack selectIngredient(int i) {
                return this.ingredients.get(i % this.ingredients.size());
            }

            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Pos.class, "x;y;ingredients", "x", "y", "ingredients"}, this);
            }

            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Pos.class, "x;y;ingredients", "x", "y", "ingredients"}, this);
            }

            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Pos.class, "x;y;ingredients", "x", "y", "ingredients"}, this, object);
            }

            public int x() {
                return this.x;
            }

            public int y() {
                return this.y;
            }

            public List<ItemStack> ingredients() {
                return this.ingredients;
            }
        }
    }
}

