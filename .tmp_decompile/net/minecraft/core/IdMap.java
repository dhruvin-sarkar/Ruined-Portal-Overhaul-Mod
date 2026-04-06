/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core;

import org.jspecify.annotations.Nullable;

public interface IdMap<T>
extends Iterable<T> {
    public static final int DEFAULT = -1;

    public int getId(T var1);

    public @Nullable T byId(int var1);

    default public T byIdOrThrow(int i) {
        T object = this.byId(i);
        if (object == null) {
            throw new IllegalArgumentException("No value with id " + i);
        }
        return object;
    }

    default public int getIdOrThrow(T object) {
        int i = this.getId(object);
        if (i == -1) {
            throw new IllegalArgumentException("Can't find id for '" + String.valueOf(object) + "' in map " + String.valueOf(this));
        }
        return i;
    }

    public int size();
}

