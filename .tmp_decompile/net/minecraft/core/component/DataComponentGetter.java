/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public interface DataComponentGetter {
    public <T> @Nullable T get(DataComponentType<? extends T> var1);

    default public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T object) {
        T object2 = this.get(dataComponentType);
        return object2 != null ? object2 : object;
    }

    default public <T> @Nullable TypedDataComponent<T> getTyped(DataComponentType<T> dataComponentType) {
        T object = this.get(dataComponentType);
        return object != null ? new TypedDataComponent<T>(dataComponentType, object) : null;
    }
}

