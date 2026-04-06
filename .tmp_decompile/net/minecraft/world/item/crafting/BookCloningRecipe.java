/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class BookCloningRecipe
extends CustomRecipe {
    public BookCloningRecipe(CraftingBookCategory craftingBookCategory) {
        super(craftingBookCategory);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() < 2) {
            return false;
        }
        boolean bl = false;
        boolean bl2 = false;
        for (int i = 0; i < craftingInput.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            if (itemStack.isEmpty()) continue;
            if (itemStack.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
                if (bl2) {
                    return false;
                }
                bl2 = true;
                continue;
            }
            if (itemStack.is(ItemTags.BOOK_CLONING_TARGET)) {
                bl = true;
                continue;
            }
            return false;
        }
        return bl2 && bl;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        int i = 0;
        ItemStack itemStack = ItemStack.EMPTY;
        for (int j = 0; j < craftingInput.size(); ++j) {
            ItemStack itemStack2 = craftingInput.getItem(j);
            if (itemStack2.isEmpty()) continue;
            if (itemStack2.has(DataComponents.WRITTEN_BOOK_CONTENT)) {
                if (!itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                itemStack = itemStack2;
                continue;
            }
            if (itemStack2.is(ItemTags.BOOK_CLONING_TARGET)) {
                ++i;
                continue;
            }
            return ItemStack.EMPTY;
        }
        WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (itemStack.isEmpty() || i < 1 || writtenBookContent == null) {
            return ItemStack.EMPTY;
        }
        WrittenBookContent writtenBookContent2 = writtenBookContent.tryCraftCopy();
        if (writtenBookContent2 == null) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack3 = itemStack.copyWithCount(i);
        itemStack3.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContent2);
        return itemStack3;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput craftingInput) {
        NonNullList<ItemStack> nonNullList = NonNullList.withSize(craftingInput.size(), ItemStack.EMPTY);
        for (int i = 0; i < nonNullList.size(); ++i) {
            ItemStack itemStack = craftingInput.getItem(i);
            ItemStack itemStack2 = itemStack.getItem().getCraftingRemainder();
            if (!itemStack2.isEmpty()) {
                nonNullList.set(i, itemStack2);
                continue;
            }
            if (!itemStack.has(DataComponents.WRITTEN_BOOK_CONTENT)) continue;
            nonNullList.set(i, itemStack.copyWithCount(1));
            break;
        }
        return nonNullList;
    }

    @Override
    public RecipeSerializer<BookCloningRecipe> getSerializer() {
        return RecipeSerializer.BOOK_CLONING;
    }
}

