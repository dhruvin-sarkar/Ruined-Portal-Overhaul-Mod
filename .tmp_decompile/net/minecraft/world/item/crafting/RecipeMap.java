/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableMultimap
 *  com.google.common.collect.ImmutableMultimap$Builder
 *  com.google.common.collect.Multimap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.crafting;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public class RecipeMap {
    public static final RecipeMap EMPTY = new RecipeMap((Multimap<RecipeType<?>, RecipeHolder<?>>)ImmutableMultimap.of(), Map.of());
    private final Multimap<RecipeType<?>, RecipeHolder<?>> byType;
    private final Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> byKey;

    private RecipeMap(Multimap<RecipeType<?>, RecipeHolder<?>> multimap, Map<ResourceKey<Recipe<?>>, RecipeHolder<?>> map) {
        this.byType = multimap;
        this.byKey = map;
    }

    public static RecipeMap create(Iterable<RecipeHolder<?>> iterable) {
        ImmutableMultimap.Builder builder = ImmutableMultimap.builder();
        ImmutableMap.Builder builder2 = ImmutableMap.builder();
        for (RecipeHolder<?> recipeHolder : iterable) {
            builder.put(recipeHolder.value().getType(), recipeHolder);
            builder2.put(recipeHolder.id(), recipeHolder);
        }
        return new RecipeMap((Multimap<RecipeType<?>, RecipeHolder<?>>)builder.build(), (Map<ResourceKey<Recipe<?>>, RecipeHolder<?>>)builder2.build());
    }

    public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> byType(RecipeType<T> recipeType) {
        return this.byType.get(recipeType);
    }

    public Collection<RecipeHolder<?>> values() {
        return this.byKey.values();
    }

    public @Nullable RecipeHolder<?> byKey(ResourceKey<Recipe<?>> resourceKey) {
        return this.byKey.get(resourceKey);
    }

    public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getRecipesFor(RecipeType<T> recipeType, I recipeInput, Level level) {
        if (recipeInput.isEmpty()) {
            return Stream.empty();
        }
        return this.byType(recipeType).stream().filter(recipeHolder -> recipeHolder.value().matches((RecipeInput)recipeInput, level));
    }
}

