/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.recipebook;

import java.util.Iterator;
import net.minecraft.util.Mth;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

public interface PlaceRecipeHelper {
    public static <T> void placeRecipe(int i, int j, Recipe<?> recipe, Iterable<T> iterable, Output<T> output) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe)recipe;
            PlaceRecipeHelper.placeRecipe(i, j, shapedRecipe.getWidth(), shapedRecipe.getHeight(), iterable, output);
        } else {
            PlaceRecipeHelper.placeRecipe(i, j, i, j, iterable, output);
        }
    }

    public static <T> void placeRecipe(int i, int j, int k, int l, Iterable<T> iterable, Output<T> output) {
        Iterator<T> iterator = iterable.iterator();
        int m = 0;
        block0: for (int n = 0; n < j; ++n) {
            boolean bl = (float)l < (float)j / 2.0f;
            int o = Mth.floor((float)j / 2.0f - (float)l / 2.0f);
            if (bl && o > n) {
                m += i;
                ++n;
            }
            for (int p = 0; p < i; ++p) {
                boolean bl2;
                if (!iterator.hasNext()) {
                    return;
                }
                bl = (float)k < (float)i / 2.0f;
                o = Mth.floor((float)i / 2.0f - (float)k / 2.0f);
                int q = k;
                boolean bl3 = bl2 = p < k;
                if (bl) {
                    q = o + k;
                    boolean bl4 = bl2 = o <= p && p < o + k;
                }
                if (bl2) {
                    output.addItemToSlot(iterator.next(), m, p, n);
                } else if (q == p) {
                    m += i - p;
                    continue block0;
                }
                ++m;
            }
        }
    }

    @FunctionalInterface
    public static interface Output<T> {
        public void addItemToSlot(T var1, int var2, int var3, int var4);
    }
}

