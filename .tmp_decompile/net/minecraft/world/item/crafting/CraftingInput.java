/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public class CraftingInput
implements RecipeInput {
    public static final CraftingInput EMPTY = new CraftingInput(0, 0, List.of());
    private final int width;
    private final int height;
    private final List<ItemStack> items;
    private final StackedItemContents stackedContents = new StackedItemContents();
    private final int ingredientCount;

    private CraftingInput(int i, int j, List<ItemStack> list) {
        this.width = i;
        this.height = j;
        this.items = list;
        int k = 0;
        for (ItemStack itemStack : list) {
            if (itemStack.isEmpty()) continue;
            ++k;
            this.stackedContents.accountStack(itemStack, 1);
        }
        this.ingredientCount = k;
    }

    public static CraftingInput of(int i, int j, List<ItemStack> list) {
        return CraftingInput.ofPositioned(i, j, list).input();
    }

    public static Positioned ofPositioned(int i, int j, List<ItemStack> list) {
        int o;
        if (i == 0 || j == 0) {
            return Positioned.EMPTY;
        }
        int k = i - 1;
        int l = 0;
        int m = j - 1;
        int n = 0;
        for (o = 0; o < j; ++o) {
            boolean bl = true;
            for (int p = 0; p < i; ++p) {
                ItemStack itemStack = list.get(p + o * i);
                if (itemStack.isEmpty()) continue;
                k = Math.min(k, p);
                l = Math.max(l, p);
                bl = false;
            }
            if (bl) continue;
            m = Math.min(m, o);
            n = Math.max(n, o);
        }
        o = l - k + 1;
        int q = n - m + 1;
        if (o <= 0 || q <= 0) {
            return Positioned.EMPTY;
        }
        if (o == i && q == j) {
            return new Positioned(new CraftingInput(i, j, list), k, m);
        }
        ArrayList<ItemStack> list2 = new ArrayList<ItemStack>(o * q);
        for (int r = 0; r < q; ++r) {
            for (int s = 0; s < o; ++s) {
                int t = s + k + (r + m) * i;
                list2.add(list.get(t));
            }
        }
        return new Positioned(new CraftingInput(o, q, list2), k, m);
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    public ItemStack getItem(int i, int j) {
        return this.items.get(i + j * this.width);
    }

    @Override
    public int size() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.ingredientCount == 0;
    }

    public StackedItemContents stackedContents() {
        return this.stackedContents;
    }

    public List<ItemStack> items() {
        return this.items;
    }

    public int ingredientCount() {
        return this.ingredientCount;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof CraftingInput) {
            CraftingInput craftingInput = (CraftingInput)object;
            return this.width == craftingInput.width && this.height == craftingInput.height && this.ingredientCount == craftingInput.ingredientCount && ItemStack.listMatches(this.items, craftingInput.items);
        }
        return false;
    }

    public int hashCode() {
        int i = ItemStack.hashStackList(this.items);
        i = 31 * i + this.width;
        i = 31 * i + this.height;
        return i;
    }

    public record Positioned(CraftingInput input, int left, int top) {
        public static final Positioned EMPTY = new Positioned(EMPTY, 0, 0);
    }
}

