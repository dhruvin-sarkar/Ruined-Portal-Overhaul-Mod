/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.server.level;

import com.mojang.serialization.Codec;
import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

public enum ParticleStatus {
    ALL(0, "options.particles.all"),
    DECREASED(1, "options.particles.decreased"),
    MINIMAL(2, "options.particles.minimal");

    private static final IntFunction<ParticleStatus> BY_ID;
    public static final Codec<ParticleStatus> LEGACY_CODEC;
    private final int id;
    private final Component caption;

    private ParticleStatus(int j, String string2) {
        this.id = j;
        this.caption = Component.translatable(string2);
    }

    public Component caption() {
        return this.caption;
    }

    static {
        BY_ID = ByIdMap.continuous(particleStatus -> particleStatus.id, ParticleStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        LEGACY_CODEC = Codec.INT.xmap(BY_ID::apply, particleStatus -> particleStatus.id);
    }
}

