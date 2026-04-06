/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public interface SingleComponentItemPredicate<T>
extends DataComponentPredicate {
    @Override
    default public boolean matches(DataComponentGetter dataComponentGetter) {
        T object = dataComponentGetter.get(this.componentType());
        return object != null && this.matches(object);
    }

    public DataComponentType<T> componentType();

    public boolean matches(T var1);
}

