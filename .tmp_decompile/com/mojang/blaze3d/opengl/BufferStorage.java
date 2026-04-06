/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public abstract class BufferStorage {
    public static BufferStorage create(GLCapabilities gLCapabilities, Set<String> set) {
        if (gLCapabilities.GL_ARB_buffer_storage && GlDevice.USE_GL_ARB_buffer_storage) {
            set.add("GL_ARB_buffer_storage");
            return new Immutable();
        }
        return new Mutable();
    }

    public abstract GlBuffer createBuffer(DirectStateAccess var1, @Nullable Supplier<String> var2, @GpuBuffer.Usage int var3, long var4);

    public abstract GlBuffer createBuffer(DirectStateAccess var1, @Nullable Supplier<String> var2, @GpuBuffer.Usage int var3, ByteBuffer var4);

    public abstract GlBuffer.GlMappedView mapBuffer(DirectStateAccess var1, GlBuffer var2, long var3, long var5, int var7);

    @Environment(value=EnvType.CLIENT)
    static class Immutable
    extends BufferStorage {
        Immutable() {
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, @GpuBuffer.Usage int i, long l) {
            int j = directStateAccess.createBuffer();
            directStateAccess.bufferStorage(j, l, i);
            ByteBuffer byteBuffer = this.tryMapBufferPersistent(directStateAccess, i, j, l);
            return new GlBuffer(supplier, directStateAccess, i, l, j, byteBuffer);
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, @GpuBuffer.Usage int i, ByteBuffer byteBuffer) {
            int j = directStateAccess.createBuffer();
            int k = byteBuffer.remaining();
            directStateAccess.bufferStorage(j, byteBuffer, i);
            ByteBuffer byteBuffer2 = this.tryMapBufferPersistent(directStateAccess, i, j, k);
            return new GlBuffer(supplier, directStateAccess, i, k, j, byteBuffer2);
        }

        private @Nullable ByteBuffer tryMapBufferPersistent(DirectStateAccess directStateAccess, @GpuBuffer.Usage int i, int j, long l) {
            ByteBuffer byteBuffer;
            int k = 0;
            if ((i & 1) != 0) {
                k |= 1;
            }
            if ((i & 2) != 0) {
                k |= 0x12;
            }
            if (k != 0) {
                GlStateManager.clearGlErrors();
                byteBuffer = directStateAccess.mapBufferRange(j, 0L, l, k | 0x40, i);
                if (byteBuffer == null) {
                    throw new IllegalStateException("Can't persistently map buffer, opengl error " + GlStateManager._getError());
                }
            } else {
                byteBuffer = null;
            }
            return byteBuffer;
        }

        @Override
        public GlBuffer.GlMappedView mapBuffer(DirectStateAccess directStateAccess, GlBuffer glBuffer, long l, long m, int i) {
            if (glBuffer.persistentBuffer == null) {
                throw new IllegalStateException("Somehow trying to map an unmappable buffer");
            }
            if (l > Integer.MAX_VALUE || m > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Mapping buffers larger than 2GB is not supported");
            }
            if (l < 0L || m < 0L) {
                throw new IllegalArgumentException("Offset or length must be positive integer values");
            }
            return new GlBuffer.GlMappedView(() -> {
                if ((i & 2) != 0) {
                    directStateAccess.flushMappedBufferRange(glBuffer.handle, l, m, glBuffer.usage());
                }
            }, glBuffer, MemoryUtil.memSlice((ByteBuffer)glBuffer.persistentBuffer, (int)((int)l), (int)((int)m)));
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Mutable
    extends BufferStorage {
        Mutable() {
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, @GpuBuffer.Usage int i, long l) {
            int j = directStateAccess.createBuffer();
            directStateAccess.bufferData(j, l, i);
            return new GlBuffer(supplier, directStateAccess, i, l, j, null);
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, @GpuBuffer.Usage int i, ByteBuffer byteBuffer) {
            int j = directStateAccess.createBuffer();
            int k = byteBuffer.remaining();
            directStateAccess.bufferData(j, byteBuffer, i);
            return new GlBuffer(supplier, directStateAccess, i, k, j, null);
        }

        @Override
        public GlBuffer.GlMappedView mapBuffer(DirectStateAccess directStateAccess, GlBuffer glBuffer, long l, long m, int i) {
            GlStateManager.clearGlErrors();
            ByteBuffer byteBuffer = directStateAccess.mapBufferRange(glBuffer.handle, l, m, i, glBuffer.usage());
            if (byteBuffer == null) {
                throw new IllegalStateException("Can't map buffer, opengl error " + GlStateManager._getError());
            }
            return new GlBuffer.GlMappedView(() -> directStateAccess.unmapBuffer(glBuffer.handle, glBuffer.usage()), glBuffer, byteBuffer);
        }
    }
}

