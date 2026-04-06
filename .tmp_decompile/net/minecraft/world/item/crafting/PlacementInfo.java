/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.world.item.crafting;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.crafting.Ingredient;

public class PlacementInfo {
    public static final int EMPTY_SLOT = -1;
    public static final PlacementInfo NOT_PLACEABLE = new PlacementInfo(List.of(), IntList.of());
    private final List<Ingredient> ingredients;
    private final IntList slotsToIngredientIndex;

    private PlacementInfo(List<Ingredient> list, IntList intList) {
        this.ingredients = list;
        this.slotsToIngredientIndex = intList;
    }

    public static PlacementInfo create(Ingredient ingredient) {
        if (ingredient.isEmpty()) {
            return NOT_PLACEABLE;
        }
        return new PlacementInfo(List.of((Object)ingredient), IntList.of((int)0));
    }

    public static PlacementInfo createFromOptionals(List<Optional<Ingredient>> list) {
        int i = list.size();
        ArrayList<Ingredient> list2 = new ArrayList<Ingredient>(i);
        IntArrayList intList = new IntArrayList(i);
        int j = 0;
        for (Optional<Ingredient> optional : list) {
            if (optional.isPresent()) {
                Ingredient ingredient = optional.get();
                if (ingredient.isEmpty()) {
                    return NOT_PLACEABLE;
                }
                list2.add(ingredient);
                intList.add(j++);
                continue;
            }
            intList.add(-1);
        }
        return new PlacementInfo(list2, (IntList)intList);
    }

    public static PlacementInfo create(List<Ingredient> list) {
        int i = list.size();
        IntArrayList intList = new IntArrayList(i);
        for (int j = 0; j < i; ++j) {
            Ingredient ingredient = list.get(j);
            if (ingredient.isEmpty()) {
                return NOT_PLACEABLE;
            }
            intList.add(j);
        }
        return new PlacementInfo(list, (IntList)intList);
    }

    public IntList slotsToIngredientIndex() {
        return this.slotsToIngredientIndex;
    }

    public List<Ingredient> ingredients() {
        return this.ingredients;
    }

    public boolean isImpossibleToPlace() {
        return this.slotsToIngredientIndex.isEmpty();
    }
}

