/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public interface DataComponentHolder
extends DataComponentGetter {
    public DataComponentMap getComponents();

    @Override
    default public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        return this.getComponents().get(dataComponentType);
    }

    default public <T> Stream<T> getAllOfType(Class<? extends T> class_) {
        return this.getComponents().stream().map(TypedDataComponent::value).filter(object -> class_.isAssignableFrom(object.getClass())).map(object -> object);
    }

    @Override
    default public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T object) {
        return this.getComponents().getOrDefault(dataComponentType, object);
    }

    default public boolean has(DataComponentType<?> dataComponentType) {
        return this.getComponents().has(dataComponentType);
    }
}

