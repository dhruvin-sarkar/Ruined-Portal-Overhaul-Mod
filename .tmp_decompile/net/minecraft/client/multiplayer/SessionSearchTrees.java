/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.multiplayer;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.FullTextSearchTree;
import net.minecraft.client.searchtree.IdSearchTree;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class SessionSearchTrees {
    private static final Key RECIPE_COLLECTIONS = new Key();
    private static final Key CREATIVE_NAMES = new Key();
    private static final Key CREATIVE_TAGS = new Key();
    private CompletableFuture<SearchTree<ItemStack>> creativeByNameSearch = CompletableFuture.completedFuture(SearchTree.empty());
    private CompletableFuture<SearchTree<ItemStack>> creativeByTagSearch = CompletableFuture.completedFuture(SearchTree.empty());
    private CompletableFuture<SearchTree<RecipeCollection>> recipeSearch = CompletableFuture.completedFuture(SearchTree.empty());
    private final Map<Key, Runnable> reloaders = new IdentityHashMap<Key, Runnable>();

    private void register(Key key, Runnable runnable) {
        runnable.run();
        this.reloaders.put(key, runnable);
    }

    public void rebuildAfterLanguageChange() {
        for (Runnable runnable : this.reloaders.values()) {
            runnable.run();
        }
    }

    private static Stream<String> getTooltipLines(Stream<ItemStack> stream, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag) {
        return stream.flatMap(itemStack -> itemStack.getTooltipLines(tooltipContext, null, tooltipFlag).stream()).map(component -> ChatFormatting.stripFormatting(component.getString()).trim()).filter(string -> !string.isEmpty());
    }

    public void updateRecipes(ClientRecipeBook clientRecipeBook, Level level) {
        this.register(RECIPE_COLLECTIONS, () -> {
            List<RecipeCollection> list = clientRecipeBook.getCollections();
            RegistryAccess registryAccess = level.registryAccess();
            HolderLookup.RegistryLookup registry = registryAccess.lookupOrThrow(Registries.ITEM);
            Item.TooltipContext tooltipContext = Item.TooltipContext.of(registryAccess);
            ContextMap contextMap = SlotDisplayContext.fromLevel(level);
            TooltipFlag.Default tooltipFlag = TooltipFlag.Default.NORMAL;
            CompletableFuture<SearchTree<RecipeCollection>> completableFuture = this.recipeSearch;
            this.recipeSearch = CompletableFuture.supplyAsync(() -> SessionSearchTrees.method_60361(contextMap, tooltipContext, tooltipFlag, (Registry)registry, list), Util.backgroundExecutor());
            completableFuture.cancel(true);
        });
    }

    public SearchTree<RecipeCollection> recipes() {
        return this.recipeSearch.join();
    }

    public void updateCreativeTags(List<ItemStack> list) {
        this.register(CREATIVE_TAGS, () -> {
            CompletableFuture<SearchTree<ItemStack>> completableFuture = this.creativeByTagSearch;
            this.creativeByTagSearch = CompletableFuture.supplyAsync(() -> new IdSearchTree<ItemStack>(itemStack -> itemStack.getTags().map(TagKey::location), list), Util.backgroundExecutor());
            completableFuture.cancel(true);
        });
    }

    public SearchTree<ItemStack> creativeTagSearch() {
        return this.creativeByTagSearch.join();
    }

    public void updateCreativeTooltips(HolderLookup.Provider provider, List<ItemStack> list) {
        this.register(CREATIVE_NAMES, () -> {
            Item.TooltipContext tooltipContext = Item.TooltipContext.of(provider);
            TooltipFlag.Default tooltipFlag = TooltipFlag.Default.NORMAL.asCreative();
            CompletableFuture<SearchTree<ItemStack>> completableFuture = this.creativeByNameSearch;
            this.creativeByNameSearch = CompletableFuture.supplyAsync(() -> new FullTextSearchTree<ItemStack>(itemStack -> SessionSearchTrees.getTooltipLines(Stream.of(itemStack), tooltipContext, tooltipFlag), itemStack -> itemStack.getItemHolder().unwrapKey().map(ResourceKey::identifier).stream(), list), Util.backgroundExecutor());
            completableFuture.cancel(true);
        });
    }

    public SearchTree<ItemStack> creativeNameSearch() {
        return this.creativeByNameSearch.join();
    }

    private static /* synthetic */ SearchTree method_60361(ContextMap contextMap, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, Registry registry, List list) {
        return new FullTextSearchTree<RecipeCollection>(recipeCollection -> SessionSearchTrees.getTooltipLines(recipeCollection.getRecipes().stream().flatMap(recipeDisplayEntry -> recipeDisplayEntry.resultItems(contextMap).stream()), tooltipContext, tooltipFlag), recipeCollection -> recipeCollection.getRecipes().stream().flatMap(recipeDisplayEntry -> recipeDisplayEntry.resultItems(contextMap).stream()).map(itemStack -> registry.getKey(itemStack.getItem())), list);
    }

    @Environment(value=EnvType.CLIENT)
    static class Key {
        Key() {
        }
    }
}

