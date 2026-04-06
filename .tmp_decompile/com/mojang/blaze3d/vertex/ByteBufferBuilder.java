/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.MemoryPool
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.system.MemoryUtil$MemoryAllocator
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.vertex;

import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class ByteBufferBuilder
implements AutoCloseable {
    private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool((String)"ByteBufferBuilder");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator((boolean)false);
    private static final long DEFAULT_MAX_CAPACITY = 0xFFFFFFFFL;
    private static final int MAX_GROWTH_SIZE = 0x200000;
    private static final int BUFFER_FREED_GENERATION = -1;
    long pointer;
    private long capacity;
    private final long maxCapacity;
    private long writeOffset;
    private long nextResultOffset;
    private int resultCount;
    private int generation;

    public ByteBufferBuilder(int i, long l) {
        this.capacity = i;
        this.maxCapacity = l;
        this.pointer = ALLOCATOR.malloc((long)i);
        MEMORY_POOL.malloc(this.pointer, i);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + i + " bytes");
        }
    }

    public ByteBufferBuilder(int i) {
        this(i, 0xFFFFFFFFL);
    }

    public static ByteBufferBuilder exactlySized(int i) {
        return new ByteBufferBuilder(i, i);
    }

    public long reserve(int i) {
        long l = this.writeOffset;
        long m = Math.addExact(l, (long)i);
        this.ensureCapacity(m);
        this.writeOffset = m;
        return Math.addExact(this.pointer, l);
    }

    private void ensureCapacity(long l) {
        if (l > this.capacity) {
            if (l > this.maxCapacity) {
                throw new IllegalArgumentException("Maximum capacity of ByteBufferBuilder (" + this.maxCapacity + ") exceeded, required " + l);
            }
            long m = Math.min(this.capacity, 0x200000L);
            long n = Mth.clamp(this.capacity + m, l, this.maxCapacity);
            this.resize(n);
        }
    }

    private void resize(long l) {
        MEMORY_POOL.free(this.pointer);
        this.pointer = ALLOCATOR.realloc(this.pointer, l);
        MEMORY_POOL.malloc(this.pointer, (int)Math.min(l, Integer.MAX_VALUE));
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)this.capacity, (Object)l);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.capacity + " bytes to " + l + " bytes");
        }
        this.capacity = l;
    }

    public @Nullable Result build() {
        this.checkOpen();
        long l = this.nextResultOffset;
        long m = this.writeOffset - l;
        if (m == 0L) {
            return null;
        }
        if (m > Integer.MAX_VALUE) {
            throw new IllegalStateException("Cannot build buffer larger than 2147483647 bytes (was " + m + ")");
        }
        this.nextResultOffset = this.writeOffset;
        ++this.resultCount;
        return new Result(l, (int)m, this.generation);
    }

    public void clear() {
        if (this.resultCount > 0) {
            LOGGER.warn("Clearing BufferBuilder with unused batches");
        }
        this.discard();
    }

    public void discard() {
        this.checkOpen();
        if (this.resultCount > 0) {
            this.discardResults();
            this.resultCount = 0;
        }
    }

    boolean isValid(int i) {
        return i == this.generation;
    }

    void freeResult() {
        if (--this.resultCount <= 0) {
            this.discardResults();
        }
    }

    private void discardResults() {
        long l = this.writeOffset - this.nextResultOffset;
        if (l > 0L) {
            MemoryUtil.memCopy((long)(this.pointer + this.nextResultOffset), (long)this.pointer, (long)l);
        }
        this.writeOffset = l;
        this.nextResultOffset = 0L;
        ++this.generation;
    }

    @Override
    public void close() {
        if (this.pointer != 0L) {
            MEMORY_POOL.free(this.pointer);
            ALLOCATOR.free(this.pointer);
            this.pointer = 0L;
            this.generation = -1;
        }
    }

    private void checkOpen() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    @Environment(value=EnvType.CLIENT)
    public class Result
    implements AutoCloseable {
        private final long offset;
        private final int capacity;
        private final int generation;
        private boolean closed;

        Result(long l, int i, int j) {
            this.offset = l;
            this.capacity = i;
            this.generation = j;
        }

        public ByteBuffer byteBuffer() {
            if (!ByteBufferBuilder.this.isValid(this.generation)) {
                throw new IllegalStateException("Buffer is no longer valid");
            }
            return MemoryUtil.memByteBuffer((long)(ByteBufferBuilder.this.pointer + this.offset), (int)this.capacity);
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (ByteBufferBuilder.this.isValid(this.generation)) {
                ByteBufferBuilder.this.freeResult();
            }
        }
    }
}

