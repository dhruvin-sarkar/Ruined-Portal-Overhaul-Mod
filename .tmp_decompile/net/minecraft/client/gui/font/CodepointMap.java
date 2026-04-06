/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.function.IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CodepointMap<T> {
    private static final int BLOCK_BITS = 8;
    private static final int BLOCK_SIZE = 256;
    private static final int IN_BLOCK_MASK = 255;
    private static final int MAX_BLOCK = 4351;
    private static final int BLOCK_COUNT = 4352;
    private final T[] empty;
    private final @Nullable T[][] blockMap;
    private final IntFunction<T[]> blockConstructor;

    public CodepointMap(IntFunction<T[]> intFunction, IntFunction<T[][]> intFunction2) {
        this.empty = intFunction.apply(256);
        this.blockMap = intFunction2.apply(4352);
        Arrays.fill(this.blockMap, this.empty);
        this.blockConstructor = intFunction;
    }

    public void clear() {
        Arrays.fill(this.blockMap, this.empty);
    }

    public @Nullable T get(int i) {
        int j = i >> 8;
        int k = i & 0xFF;
        return this.blockMap[j][k];
    }

    public @Nullable T put(int i, T object) {
        int j = i >> 8;
        int k = i & 0xFF;
        T[] objects = this.blockMap[j];
        if (objects == this.empty) {
            objects = this.blockConstructor.apply(256);
            this.blockMap[j] = objects;
            objects[k] = object;
            return null;
        }
        T object2 = objects[k];
        objects[k] = object;
        return object2;
    }

    public T computeIfAbsent(int i, IntFunction<T> intFunction) {
        int j = i >> 8;
        T[] objects = this.blockMap[j];
        int k = i & 0xFF;
        T object = objects[k];
        if (object != null) {
            return object;
        }
        if (objects == this.empty) {
            objects = this.blockConstructor.apply(256);
            this.blockMap[j] = objects;
        }
        T object2 = intFunction.apply(i);
        objects[k] = object2;
        return object2;
    }

    public @Nullable T remove(int i) {
        int j = i >> 8;
        int k = i & 0xFF;
        T[] objects = this.blockMap[j];
        if (objects == this.empty) {
            return null;
        }
        T object = objects[k];
        objects[k] = null;
        return object;
    }

    public void forEach(Output<T> output) {
        for (int i = 0; i < this.blockMap.length; ++i) {
            T[] objects = this.blockMap[i];
            if (objects == this.empty) continue;
            for (int j = 0; j < objects.length; ++j) {
                T object = objects[j];
                if (object == null) continue;
                int k = i << 8 | j;
                output.accept(k, object);
            }
        }
    }

    public IntSet keySet() {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        this.forEach((i, object) -> intOpenHashSet.add(i));
        return intOpenHashSet;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface Output<T> {
        public void accept(int var1, T var2);
    }
}

