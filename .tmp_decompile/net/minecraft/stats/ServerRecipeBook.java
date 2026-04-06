/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.slf4j.Logger
 */
package net.minecraft.stats;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRecipeBookSettingsPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.RecipeBookSettings;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import org.slf4j.Logger;

public class ServerRecipeBook
extends RecipeBook {
    public static final String RECIPE_BOOK_TAG = "recipeBook";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DisplayResolver displayResolver;
    @VisibleForTesting
    protected final Set<ResourceKey<Recipe<?>>> known = Sets.newIdentityHashSet();
    @VisibleForTesting
    protected final Set<ResourceKey<Recipe<?>>> highlight = Sets.newIdentityHashSet();

    public ServerRecipeBook(DisplayResolver displayResolver) {
        this.displayResolver = displayResolver;
    }

    public void add(ResourceKey<Recipe<?>> resourceKey) {
        this.known.add(resourceKey);
    }

    public boolean contains(ResourceKey<Recipe<?>> resourceKey) {
        return this.known.contains(resourceKey);
    }

    public void remove(ResourceKey<Recipe<?>> resourceKey) {
        this.known.remove(resourceKey);
        this.highlight.remove(resourceKey);
    }

    public void removeHighlight(ResourceKey<Recipe<?>> resourceKey) {
        this.highlight.remove(resourceKey);
    }

    private void addHighlight(ResourceKey<Recipe<?>> resourceKey) {
        this.highlight.add(resourceKey);
    }

    public int addRecipes(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
        ArrayList<ClientboundRecipeBookAddPacket.Entry> list = new ArrayList<ClientboundRecipeBookAddPacket.Entry>();
        for (RecipeHolder<?> recipeHolder : collection) {
            ResourceKey<Recipe<?>> resourceKey = recipeHolder.id();
            if (this.known.contains(resourceKey) || recipeHolder.value().isSpecial()) continue;
            this.add(resourceKey);
            this.addHighlight(resourceKey);
            this.displayResolver.displaysForRecipe(resourceKey, recipeDisplayEntry -> list.add(new ClientboundRecipeBookAddPacket.Entry((RecipeDisplayEntry)((Object)recipeDisplayEntry), recipeHolder.value().showNotification(), true)));
            CriteriaTriggers.RECIPE_UNLOCKED.trigger(serverPlayer, recipeHolder);
        }
        if (!list.isEmpty()) {
            serverPlayer.connection.send(new ClientboundRecipeBookAddPacket(list, false));
        }
        return list.size();
    }

    public int removeRecipes(Collection<RecipeHolder<?>> collection, ServerPlayer serverPlayer) {
        ArrayList list = Lists.newArrayList();
        for (RecipeHolder<?> recipeHolder : collection) {
            ResourceKey<Recipe<?>> resourceKey = recipeHolder.id();
            if (!this.known.contains(resourceKey)) continue;
            this.remove(resourceKey);
            this.displayResolver.displaysForRecipe(resourceKey, recipeDisplayEntry -> list.add(recipeDisplayEntry.id()));
        }
        if (!list.isEmpty()) {
            serverPlayer.connection.send(new ClientboundRecipeBookRemovePacket(list));
        }
        return list.size();
    }

    private void loadRecipes(List<ResourceKey<Recipe<?>>> list, Consumer<ResourceKey<Recipe<?>>> consumer, Predicate<ResourceKey<Recipe<?>>> predicate) {
        for (ResourceKey<Recipe<?>> resourceKey : list) {
            if (!predicate.test(resourceKey)) {
                LOGGER.error("Tried to load unrecognized recipe: {} removed now.", resourceKey);
                continue;
            }
            consumer.accept(resourceKey);
        }
    }

    public void sendInitialRecipeBook(ServerPlayer serverPlayer) {
        serverPlayer.connection.send(new ClientboundRecipeBookSettingsPacket(this.getBookSettings().copy()));
        ArrayList<ClientboundRecipeBookAddPacket.Entry> list = new ArrayList<ClientboundRecipeBookAddPacket.Entry>(this.known.size());
        for (ResourceKey<Recipe<?>> resourceKey : this.known) {
            this.displayResolver.displaysForRecipe(resourceKey, recipeDisplayEntry -> list.add(new ClientboundRecipeBookAddPacket.Entry((RecipeDisplayEntry)((Object)recipeDisplayEntry), false, this.highlight.contains(resourceKey))));
        }
        serverPlayer.connection.send(new ClientboundRecipeBookAddPacket(list, true));
    }

    public void copyOverData(ServerRecipeBook serverRecipeBook) {
        this.apply(serverRecipeBook.pack());
    }

    public Packed pack() {
        return new Packed(this.bookSettings.copy(), List.copyOf(this.known), List.copyOf(this.highlight));
    }

    private void apply(Packed packed) {
        this.known.clear();
        this.highlight.clear();
        this.bookSettings.replaceFrom(packed.settings);
        this.known.addAll(packed.known);
        this.highlight.addAll(packed.highlight);
    }

    public void loadUntrusted(Packed packed, Predicate<ResourceKey<Recipe<?>>> predicate) {
        this.bookSettings.replaceFrom(packed.settings);
        this.loadRecipes(packed.known, this.known::add, predicate);
        this.loadRecipes(packed.highlight, this.highlight::add, predicate);
    }

    @FunctionalInterface
    public static interface DisplayResolver {
        public void displaysForRecipe(ResourceKey<Recipe<?>> var1, Consumer<RecipeDisplayEntry> var2);
    }

    public static final class Packed
    extends Record {
        final RecipeBookSettings settings;
        final List<ResourceKey<Recipe<?>>> known;
        final List<ResourceKey<Recipe<?>>> highlight;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RecipeBookSettings.MAP_CODEC.forGetter(Packed::settings), (App)Recipe.KEY_CODEC.listOf().fieldOf("recipes").forGetter(Packed::known), (App)Recipe.KEY_CODEC.listOf().fieldOf("toBeDisplayed").forGetter(Packed::highlight)).apply((Applicative)instance, Packed::new));

        public Packed(RecipeBookSettings recipeBookSettings, List<ResourceKey<Recipe<?>>> list, List<ResourceKey<Recipe<?>>> list2) {
            this.settings = recipeBookSettings;
            this.known = list;
            this.highlight = list2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "settings;known;highlight", "settings", "known", "highlight"}, this, object);
        }

        public RecipeBookSettings settings() {
            return this.settings;
        }

        public List<ResourceKey<Recipe<?>>> known() {
            return this.known;
        }

        public List<ResourceKey<Recipe<?>>> highlight() {
            return this.highlight;
        }
    }
}

