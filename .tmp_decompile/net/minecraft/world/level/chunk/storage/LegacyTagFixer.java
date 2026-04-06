/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk.storage;

import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;

@FunctionalInterface
public interface LegacyTagFixer {
    public static final Supplier<LegacyTagFixer> EMPTY = () -> compoundTag -> compoundTag;

    public CompoundTag applyFix(CompoundTag var1);

    default public void markChunkDone(ChunkPos chunkPos) {
    }

    default public int targetDataVersion() {
        return -1;
    }
}

