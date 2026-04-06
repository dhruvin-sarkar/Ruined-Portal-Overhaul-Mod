/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.gui.screens.recipebook.SearchRecipeBookCategory;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.item.crafting.ExtendedRecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

@Environment(value=EnvType.CLIENT)
public class ClientRecipeBook
extends RecipeBook {
    private final Map<RecipeDisplayId, RecipeDisplayEntry> known = new HashMap<RecipeDisplayId, RecipeDisplayEntry>();
    private final Set<RecipeDisplayId> highlight = new HashSet<RecipeDisplayId>();
    private Map<ExtendedRecipeBookCategory, List<RecipeCollection>> collectionsByTab = Map.of();
    private List<RecipeCollection> allCollections = List.of();

    public void add(RecipeDisplayEntry recipeDisplayEntry) {
        this.known.put(recipeDisplayEntry.id(), recipeDisplayEntry);
    }

    public void remove(RecipeDisplayId recipeDisplayId) {
        this.known.remove((Object)recipeDisplayId);
        this.highlight.remove((Object)recipeDisplayId);
    }

    public void clear() {
        this.known.clear();
        this.highlight.clear();
    }

    public boolean willHighlight(RecipeDisplayId recipeDisplayId) {
        return this.highlight.contains((Object)recipeDisplayId);
    }

    public void removeHighlight(RecipeDisplayId recipeDisplayId) {
        this.highlight.remove((Object)recipeDisplayId);
    }

    public void addHighlight(RecipeDisplayId recipeDisplayId) {
        this.highlight.add(recipeDisplayId);
    }

    public void rebuildCollections() {
        Map<RecipeBookCategory, List<List<RecipeDisplayEntry>>> map = ClientRecipeBook.categorizeAndGroupRecipes(this.known.values());
        HashMap<SearchRecipeBookCategory, List> map2 = new HashMap<SearchRecipeBookCategory, List>();
        ImmutableList.Builder builder = ImmutableList.builder();
        map.forEach((recipeBookCategory, list) -> map2.put((SearchRecipeBookCategory)recipeBookCategory, (List)list.stream().map(RecipeCollection::new).peek(arg_0 -> ((ImmutableList.Builder)builder).add(arg_0)).collect(ImmutableList.toImmutableList())));
        for (SearchRecipeBookCategory searchRecipeBookCategory : SearchRecipeBookCategory.values()) {
            map2.put(searchRecipeBookCategory, (List)searchRecipeBookCategory.includedCategories().stream().flatMap(recipeBookCategory -> map2.getOrDefault(recipeBookCategory, List.of()).stream()).collect(ImmutableList.toImmutableList()));
        }
        this.collectionsByTab = Map.copyOf(map2);
        this.allCollections = builder.build();
    }

    private static Map<RecipeBookCategory, List<List<RecipeDisplayEntry>>> categorizeAndGroupRecipes(Iterable<RecipeDisplayEntry> iterable) {
        HashMap<RecipeBookCategory, List<List<RecipeDisplayEntry>>> map = new HashMap<RecipeBookCategory, List<List<RecipeDisplayEntry>>>();
        HashBasedTable table = HashBasedTable.create();
        for (RecipeDisplayEntry recipeDisplayEntry : iterable) {
            RecipeBookCategory recipeBookCategory2 = recipeDisplayEntry.category();
            OptionalInt optionalInt = recipeDisplayEntry.group();
            if (optionalInt.isEmpty()) {
                map.computeIfAbsent(recipeBookCategory2, recipeBookCategory -> new ArrayList()).add(List.of((Object)((Object)recipeDisplayEntry)));
                continue;
            }
            ArrayList<RecipeDisplayEntry> list = (ArrayList<RecipeDisplayEntry>)table.get((Object)recipeBookCategory2, (Object)optionalInt.getAsInt());
            if (list == null) {
                list = new ArrayList<RecipeDisplayEntry>();
                table.put((Object)recipeBookCategory2, (Object)optionalInt.getAsInt(), list);
                map.computeIfAbsent(recipeBookCategory2, recipeBookCategory -> new ArrayList()).add(list);
            }
            list.add(recipeDisplayEntry);
        }
        return map;
    }

    public List<RecipeCollection> getCollections() {
        return this.allCollections;
    }

    public List<RecipeCollection> getCollection(ExtendedRecipeBookCategory extendedRecipeBookCategory) {
        return this.collectionsByTab.getOrDefault(extendedRecipeBookCategory, Collections.emptyList());
    }
}

