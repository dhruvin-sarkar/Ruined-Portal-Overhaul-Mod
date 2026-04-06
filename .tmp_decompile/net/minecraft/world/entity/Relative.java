/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.entity;

import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum Relative {
    X(0),
    Y(1),
    Z(2),
    Y_ROT(3),
    X_ROT(4),
    DELTA_X(5),
    DELTA_Y(6),
    DELTA_Z(7),
    ROTATE_DELTA(8);

    public static final Set<Relative> ALL;
    public static final Set<Relative> ROTATION;
    public static final Set<Relative> DELTA;
    public static final StreamCodec<ByteBuf, Set<Relative>> SET_STREAM_CODEC;
    private final int bit;

    @SafeVarargs
    public static Set<Relative> union(Set<Relative> ... sets) {
        HashSet<Relative> hashSet = new HashSet<Relative>();
        for (Set<Relative> set : sets) {
            hashSet.addAll(set);
        }
        return hashSet;
    }

    public static Set<Relative> rotation(boolean bl, boolean bl2) {
        EnumSet<Relative> set = EnumSet.noneOf(Relative.class);
        if (bl) {
            set.add(Y_ROT);
        }
        if (bl2) {
            set.add(X_ROT);
        }
        return set;
    }

    public static Set<Relative> position(boolean bl, boolean bl2, boolean bl3) {
        EnumSet<Relative> set = EnumSet.noneOf(Relative.class);
        if (bl) {
            set.add(X);
        }
        if (bl2) {
            set.add(Y);
        }
        if (bl3) {
            set.add(Z);
        }
        return set;
    }

    public static Set<Relative> direction(boolean bl, boolean bl2, boolean bl3) {
        EnumSet<Relative> set = EnumSet.noneOf(Relative.class);
        if (bl) {
            set.add(DELTA_X);
        }
        if (bl2) {
            set.add(DELTA_Y);
        }
        if (bl3) {
            set.add(DELTA_Z);
        }
        return set;
    }

    private Relative(int j) {
        this.bit = j;
    }

    private int getMask() {
        return 1 << this.bit;
    }

    private boolean isSet(int i) {
        return (i & this.getMask()) == this.getMask();
    }

    public static Set<Relative> unpack(int i) {
        EnumSet<Relative> set = EnumSet.noneOf(Relative.class);
        for (Relative relative : Relative.values()) {
            if (!relative.isSet(i)) continue;
            set.add(relative);
        }
        return set;
    }

    public static int pack(Set<Relative> set) {
        int i = 0;
        for (Relative relative : set) {
            i |= relative.getMask();
        }
        return i;
    }

    static {
        ALL = Set.of((Object[])Relative.values());
        ROTATION = Set.of((Object)((Object)X_ROT), (Object)((Object)Y_ROT));
        DELTA = Set.of((Object)((Object)DELTA_X), (Object)((Object)DELTA_Y), (Object)((Object)DELTA_Z), (Object)((Object)ROTATE_DELTA));
        SET_STREAM_CODEC = ByteBufCodecs.INT.map(Relative::unpack, Relative::pack);
    }
}

