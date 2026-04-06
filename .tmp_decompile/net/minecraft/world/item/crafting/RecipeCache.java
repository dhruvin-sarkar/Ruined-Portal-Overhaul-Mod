/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.crafting;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

public class RecipeCache {
    private final @Nullable Entry[] entries;
    private WeakReference<@Nullable RecipeManager> cachedRecipeManager = new WeakReference<Object>(null);

    public RecipeCache(int i) {
        this.entries = new Entry[i];
    }

    public Optional<RecipeHolder<CraftingRecipe>> get(ServerLevel serverLevel, CraftingInput craftingInput) {
        if (craftingInput.isEmpty()) {
            return Optional.empty();
        }
        this.validateRecipeManager(serverLevel);
        for (int i = 0; i < this.entries.length; ++i) {
            Entry entry = this.entries[i];
            if (entry == null || !entry.matches(craftingInput)) continue;
            this.moveEntryToFront(i);
            return Optional.ofNullable(entry.value());
        }
        return this.compute(craftingInput, serverLevel);
    }

    private void validateRecipeManager(ServerLevel serverLevel) {
        RecipeManager recipeManager = serverLevel.recipeAccess();
        if (recipeManager != this.cachedRecipeManager.get()) {
            this.cachedRecipeManager = new WeakReference<RecipeManager>(recipeManager);
            Arrays.fill((Object[])this.entries, null);
        }
    }

    private Optional<RecipeHolder<CraftingRecipe>> compute(CraftingInput craftingInput, ServerLevel serverLevel) {
        Optional<RecipeHolder<CraftingRecipe>> optional = serverLevel.recipeAccess().getRecipeFor(RecipeType.CRAFTING, craftingInput, serverLevel);
        this.insert(craftingInput, optional.orElse(null));
        return optional;
    }

    private void moveEntryToFront(int i) {
        if (i > 0) {
            Entry entry = this.entries[i];
            System.arraycopy(this.entries, 0, this.entries, 1, i);
            this.entries[0] = entry;
        }
    }

    private void insert(CraftingInput craftingInput, @Nullable RecipeHolder<CraftingRecipe> recipeHolder) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);
        for (int i = 0; i < craftingInput.size(); ++i) {
            nonNullList.set(i, craftingInput.getItem(i).copyWithCount(1));
        }
        System.arraycopy(this.entries, 0, this.entries, 1, this.entries.length - 1);
        this.entries[0] = new Entry(nonNullList, craftingInput.width(), craftingInput.height(), recipeHolder);
    }

    record Entry(NonNullList<ItemStack> key, int width, int height, @Nullable RecipeHolder<CraftingRecipe> value) {
        public boolean matches(CraftingInput craftingInput) {
            if (this.width != craftingInput.width() || this.height != craftingInput.height()) {
                return false;
            }
            for (int i = 0; i < this.key.size(); ++i) {
                if (ItemStack.isSameItemSameComponents(this.key.get(i), craftingInput.getItem(i))) continue;
                return false;
            }
            return true;
        }
    }
}

