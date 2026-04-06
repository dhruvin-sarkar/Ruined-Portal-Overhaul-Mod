/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMaps
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.core.component;

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import org.jspecify.annotations.Nullable;

public final class PatchedDataComponentMap
implements DataComponentMap {
    private final DataComponentMap prototype;
    private Reference2ObjectMap<DataComponentType<?>, Optional<?>> patch;
    private boolean copyOnWrite;

    public PatchedDataComponentMap(DataComponentMap dataComponentMap) {
        this(dataComponentMap, Reference2ObjectMaps.emptyMap(), true);
    }

    private PatchedDataComponentMap(DataComponentMap dataComponentMap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap, boolean bl) {
        this.prototype = dataComponentMap;
        this.patch = reference2ObjectMap;
        this.copyOnWrite = bl;
    }

    public static PatchedDataComponentMap fromPatch(DataComponentMap dataComponentMap, DataComponentPatch dataComponentPatch) {
        if (PatchedDataComponentMap.isPatchSanitized(dataComponentMap, dataComponentPatch.map)) {
            return new PatchedDataComponentMap(dataComponentMap, dataComponentPatch.map, true);
        }
        PatchedDataComponentMap patchedDataComponentMap = new PatchedDataComponentMap(dataComponentMap);
        patchedDataComponentMap.applyPatch(dataComponentPatch);
        return patchedDataComponentMap;
    }

    private static boolean isPatchSanitized(DataComponentMap dataComponentMap, Reference2ObjectMap<DataComponentType<?>, Optional<?>> reference2ObjectMap) {
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(reference2ObjectMap)) {
            Object object = dataComponentMap.get((DataComponentType)entry.getKey());
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent() && optional.get().equals(object)) {
                return false;
            }
            if (!optional.isEmpty() || object != null) continue;
            return false;
        }
        return true;
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        Optional optional = (Optional)this.patch.get(dataComponentType);
        if (optional != null) {
            return optional.orElse(null);
        }
        return this.prototype.get(dataComponentType);
    }

    public boolean hasNonDefault(DataComponentType<?> dataComponentType) {
        return this.patch.containsKey(dataComponentType);
    }

    public <T> @Nullable T set(DataComponentType<T> dataComponentType, @Nullable T object) {
        this.ensureMapOwnership();
        T object2 = this.prototype.get(dataComponentType);
        Optional optional = Objects.equals(object, object2) ? (Optional)this.patch.remove(dataComponentType) : (Optional)this.patch.put(dataComponentType, Optional.ofNullable(object));
        if (optional != null) {
            return optional.orElse(object2);
        }
        return object2;
    }

    public <T> @Nullable T set(TypedDataComponent<T> typedDataComponent) {
        return this.set(typedDataComponent.type(), typedDataComponent.value());
    }

    public <T> @Nullable T remove(DataComponentType<? extends T> dataComponentType) {
        this.ensureMapOwnership();
        T object = this.prototype.get(dataComponentType);
        Optional optional = object != null ? (Optional)this.patch.put(dataComponentType, Optional.empty()) : (Optional)this.patch.remove(dataComponentType);
        if (optional != null) {
            return optional.orElse(null);
        }
        return object;
    }

    public void applyPatch(DataComponentPatch dataComponentPatch) {
        this.ensureMapOwnership();
        for (Map.Entry entry : Reference2ObjectMaps.fastIterable(dataComponentPatch.map)) {
            this.applyPatch((DataComponentType)entry.getKey(), (Optional)entry.getValue());
        }
    }

    private void applyPatch(DataComponentType<?> dataComponentType, Optional<?> optional) {
        Object object = this.prototype.get(dataComponentType);
        if (optional.isPresent()) {
            if (optional.get().equals(object)) {
                this.patch.remove(dataComponentType);
            } else {
                this.patch.put(dataComponentType, optional);
            }
        } else if (object != null) {
            this.patch.put(dataComponentType, Optional.empty());
        } else {
            this.patch.remove(dataComponentType);
        }
    }

    public void restorePatch(DataComponentPatch dataComponentPatch) {
        this.ensureMapOwnership();
        this.patch.clear();
        this.patch.putAll(dataComponentPatch.map);
    }

    public void clearPatch() {
        this.ensureMapOwnership();
        this.patch.clear();
    }

    public void setAll(DataComponentMap dataComponentMap) {
        for (TypedDataComponent<?> typedDataComponent : dataComponentMap) {
            typedDataComponent.applyTo(this);
        }
    }

    private void ensureMapOwnership() {
        if (this.copyOnWrite) {
            this.patch = new Reference2ObjectArrayMap(this.patch);
            this.copyOnWrite = false;
        }
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        if (this.patch.isEmpty()) {
            return this.prototype.keySet();
        }
        ReferenceArraySet set = new ReferenceArraySet(this.prototype.keySet());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            Optional optional = (Optional)entry.getValue();
            if (optional.isPresent()) {
                set.add((DataComponentType)entry.getKey());
                continue;
            }
            set.remove(entry.getKey());
        }
        return set;
    }

    @Override
    public Iterator<TypedDataComponent<?>> iterator() {
        if (this.patch.isEmpty()) {
            return this.prototype.iterator();
        }
        ArrayList<TypedDataComponent> list = new ArrayList<TypedDataComponent>(this.patch.size() + this.prototype.size());
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            if (!((Optional)entry.getValue()).isPresent()) continue;
            list.add(TypedDataComponent.createUnchecked((DataComponentType)entry.getKey(), ((Optional)entry.getValue()).get()));
        }
        for (TypedDataComponent typedDataComponent : this.prototype) {
            if (this.patch.containsKey(typedDataComponent.type())) continue;
            list.add(typedDataComponent);
        }
        return list.iterator();
    }

    @Override
    public int size() {
        int i = this.prototype.size();
        for (Reference2ObjectMap.Entry entry : Reference2ObjectMaps.fastIterable(this.patch)) {
            boolean bl2;
            boolean bl = ((Optional)entry.getValue()).isPresent();
            if (bl == (bl2 = this.prototype.has((DataComponentType)entry.getKey()))) continue;
            i += bl ? 1 : -1;
        }
        return i;
    }

    public DataComponentPatch asPatch() {
        if (this.patch.isEmpty()) {
            return DataComponentPatch.EMPTY;
        }
        this.copyOnWrite = true;
        return new DataComponentPatch(this.patch);
    }

    public PatchedDataComponentMap copy() {
        this.copyOnWrite = true;
        return new PatchedDataComponentMap(this.prototype, this.patch, true);
    }

    public DataComponentMap toImmutableMap() {
        if (this.patch.isEmpty()) {
            return this.prototype;
        }
        return this.copy();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PatchedDataComponentMap)) return false;
        PatchedDataComponentMap patchedDataComponentMap = (PatchedDataComponentMap)object;
        if (!this.prototype.equals(patchedDataComponentMap.prototype)) return false;
        if (!this.patch.equals(patchedDataComponentMap.patch)) return false;
        return true;
    }

    public int hashCode() {
        return this.prototype.hashCode() + this.patch.hashCode() * 31;
    }

    public String toString() {
        return "{" + this.stream().map(TypedDataComponent::toString).collect(Collectors.joining(", ")) + "}";
    }
}

