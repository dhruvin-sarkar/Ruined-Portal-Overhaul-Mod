/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.ticks;

import com.mojang.serialization.Codec;

public enum TickPriority {
    EXTREMELY_HIGH(-3),
    VERY_HIGH(-2),
    HIGH(-1),
    NORMAL(0),
    LOW(1),
    VERY_LOW(2),
    EXTREMELY_LOW(3);

    public static final Codec<TickPriority> CODEC;
    private final int value;

    private TickPriority(int j) {
        this.value = j;
    }

    public static TickPriority byValue(int i) {
        for (TickPriority tickPriority : TickPriority.values()) {
            if (tickPriority.value != i) continue;
            return tickPriority;
        }
        if (i < TickPriority.EXTREMELY_HIGH.value) {
            return EXTREMELY_HIGH;
        }
        return EXTREMELY_LOW;
    }

    public int getValue() {
        return this.value;
    }

    static {
        CODEC = Codec.INT.xmap(TickPriority::byValue, TickPriority::getValue);
    }
}

