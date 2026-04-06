/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public sealed interface InteractionResult {
    public static final Success SUCCESS = new Success(SwingSource.CLIENT, ItemContext.DEFAULT);
    public static final Success SUCCESS_SERVER = new Success(SwingSource.SERVER, ItemContext.DEFAULT);
    public static final Success CONSUME = new Success(SwingSource.NONE, ItemContext.DEFAULT);
    public static final Fail FAIL = new Fail();
    public static final Pass PASS = new Pass();
    public static final TryEmptyHandInteraction TRY_WITH_EMPTY_HAND = new TryEmptyHandInteraction();

    default public boolean consumesAction() {
        return false;
    }

    public record Success(SwingSource swingSource, ItemContext itemContext) implements InteractionResult
    {
        @Override
        public boolean consumesAction() {
            return true;
        }

        public Success heldItemTransformedTo(ItemStack itemStack) {
            return new Success(this.swingSource, new ItemContext(true, itemStack));
        }

        public Success withoutItem() {
            return new Success(this.swingSource, ItemContext.NONE);
        }

        public boolean wasItemInteraction() {
            return this.itemContext.wasItemInteraction;
        }

        public @Nullable ItemStack heldItemTransformedTo() {
            return this.itemContext.heldItemTransformedTo;
        }
    }

    public static enum SwingSource {
        NONE,
        CLIENT,
        SERVER;

    }

    public static final class ItemContext
    extends Record {
        final boolean wasItemInteraction;
        final @Nullable ItemStack heldItemTransformedTo;
        static ItemContext NONE = new ItemContext(false, null);
        static ItemContext DEFAULT = new ItemContext(true, null);

        public ItemContext(boolean bl, @Nullable ItemStack itemStack) {
            this.wasItemInteraction = bl;
            this.heldItemTransformedTo = itemStack;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ItemContext.class, "wasItemInteraction;heldItemTransformedTo", "wasItemInteraction", "heldItemTransformedTo"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ItemContext.class, "wasItemInteraction;heldItemTransformedTo", "wasItemInteraction", "heldItemTransformedTo"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ItemContext.class, "wasItemInteraction;heldItemTransformedTo", "wasItemInteraction", "heldItemTransformedTo"}, this, object);
        }

        public boolean wasItemInteraction() {
            return this.wasItemInteraction;
        }

        public @Nullable ItemStack heldItemTransformedTo() {
            return this.heldItemTransformedTo;
        }
    }

    public record Fail() implements InteractionResult
    {
    }

    public record Pass() implements InteractionResult
    {
    }

    public record TryEmptyHandInteraction() implements InteractionResult
    {
    }
}

