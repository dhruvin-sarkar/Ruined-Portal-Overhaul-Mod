/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.Validate
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.chunk;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.IdMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PaletteResize;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

public class SingleValuePalette<T>
implements Palette<T> {
    private @Nullable T value;

    public SingleValuePalette(List<T> list) {
        if (!list.isEmpty()) {
            Validate.isTrue((list.size() <= 1 ? 1 : 0) != 0, (String)"Can't initialize SingleValuePalette with %d values.", (long)list.size());
            this.value = list.getFirst();
        }
    }

    public static <A> Palette<A> create(int i, List<A> list) {
        return new SingleValuePalette<A>(list);
    }

    @Override
    public int idFor(T object, PaletteResize<T> paletteResize) {
        if (this.value == null || this.value == object) {
            this.value = object;
            return 0;
        }
        return paletteResize.onResize(1, object);
    }

    @Override
    public boolean maybeHas(Predicate<T> predicate) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return predicate.test(this.value);
    }

    @Override
    public T valueFor(int i) {
        if (this.value == null || i != 0) {
            throw new IllegalStateException("Missing Palette entry for id " + i + ".");
        }
        return this.value;
    }

    @Override
    public void read(FriendlyByteBuf friendlyByteBuf, IdMap<T> idMap) {
        this.value = idMap.byIdOrThrow(friendlyByteBuf.readVarInt());
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf, IdMap<T> idMap) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        friendlyByteBuf.writeVarInt(idMap.getId(this.value));
    }

    @Override
    public int getSerializedSize(IdMap<T> idMap) {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return VarInt.getByteSize(idMap.getId(this.value));
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public Palette<T> copy() {
        if (this.value == null) {
            throw new IllegalStateException("Use of an uninitialized palette");
        }
        return this;
    }
}

