/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.MemoryPool
 *  com.mojang.jtracy.TracyClient
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlBuffer
extends GpuBuffer {
    protected static final MemoryPool MEMORY_POOl = TracyClient.createMemoryPool((String)"GPU Buffers");
    protected boolean closed;
    protected final @Nullable Supplier<String> label;
    private final DirectStateAccess dsa;
    protected final int handle;
    protected @Nullable ByteBuffer persistentBuffer;

    protected GlBuffer(@Nullable Supplier<String> supplier, DirectStateAccess directStateAccess, @GpuBuffer.Usage int i, long l, int j, @Nullable ByteBuffer byteBuffer) {
        super(i, l);
        this.label = supplier;
        this.dsa = directStateAccess;
        this.handle = j;
        this.persistentBuffer = byteBuffer;
        int k = (int)Math.min(l, Integer.MAX_VALUE);
        MEMORY_POOl.malloc((long)j, k);
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.persistentBuffer != null) {
            this.dsa.unmapBuffer(this.handle, this.usage());
            this.persistentBuffer = null;
        }
        GlStateManager._glDeleteBuffers(this.handle);
        MEMORY_POOl.free((long)this.handle);
    }

    @Environment(value=EnvType.CLIENT)
    public static class GlMappedView
    implements GpuBuffer.MappedView {
        private final Runnable unmap;
        private final GlBuffer buffer;
        private final ByteBuffer data;
        private boolean closed;

        protected GlMappedView(Runnable runnable, GlBuffer glBuffer, ByteBuffer byteBuffer) {
            this.unmap = runnable;
            this.buffer = glBuffer;
            this.data = byteBuffer;
        }

        @Override
        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.unmap.run();
        }
    }
}

