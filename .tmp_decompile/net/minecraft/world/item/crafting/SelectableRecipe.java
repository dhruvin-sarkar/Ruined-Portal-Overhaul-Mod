/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.item.crafting;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record SelectableRecipe<T extends Recipe<?>>(SlotDisplay optionDisplay, Optional<RecipeHolder<T>> recipe) {
    public static <T extends Recipe<?>> StreamCodec<RegistryFriendlyByteBuf, SelectableRecipe<T>> noRecipeCodec() {
        return StreamCodec.composite(SlotDisplay.STREAM_CODEC, SelectableRecipe::optionDisplay, slotDisplay -> new SelectableRecipe((SlotDisplay)slotDisplay, Optional.empty()));
    }

    public record SingleInputSet<T extends Recipe<?>>(List<SingleInputEntry<T>> entries) {
        public static <T extends Recipe<?>> SingleInputSet<T> empty() {
            return new SingleInputSet<T>(List.of());
        }

        public static <T extends Recipe<?>> StreamCodec<RegistryFriendlyByteBuf, SingleInputSet<T>> noRecipeCodec() {
            return StreamCodec.composite(SingleInputEntry.noRecipeCodec().apply(ByteBufCodecs.list()), SingleInputSet::entries, SingleInputSet::new);
        }

        public boolean acceptsInput(ItemStack itemStack) {
            return this.entries.stream().anyMatch(singleInputEntry -> singleInputEntry.input.test(itemStack));
        }

        public SingleInputSet<T> selectByInput(ItemStack itemStack) {
            return new SingleInputSet<T>(this.entries.stream().filter(singleInputEntry -> singleInputEntry.input.test(itemStack)).toList());
        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public int size() {
            return this.entries.size();
        }
    }

    public static final class SingleInputEntry<T extends Recipe<?>>
    extends Record {
        final Ingredient input;
        private final SelectableRecipe<T> recipe;

        public SingleInputEntry(Ingredient ingredient, SelectableRecipe<T> selectableRecipe) {
            this.input = ingredient;
            this.recipe = selectableRecipe;
        }

        public static <T extends Recipe<?>> StreamCodec<RegistryFriendlyByteBuf, SingleInputEntry<T>> noRecipeCodec() {
            return StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, SingleInputEntry::input, SelectableRecipe.noRecipeCodec(), SingleInputEntry::recipe, SingleInputEntry::new);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SingleInputEntry.class, "input;recipe", "input", "recipe"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SingleInputEntry.class, "input;recipe", "input", "recipe"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SingleInputEntry.class, "input;recipe", "input", "recipe"}, this, object);
        }

        public Ingredient input() {
            return this.input;
        }

        public SelectableRecipe<T> recipe() {
            return this.recipe;
        }
    }
}

