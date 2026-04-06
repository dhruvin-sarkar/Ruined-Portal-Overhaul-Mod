/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.ARBBufferStorage
 *  org.lwjgl.opengl.ARBDirectStateAccess
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.opengl.GL31
 *  org.lwjgl.opengl.GLCapabilities
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.GraphicsWorkarounds;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;

@Environment(value=EnvType.CLIENT)
public abstract class DirectStateAccess {
    public static DirectStateAccess create(GLCapabilities gLCapabilities, Set<String> set, GraphicsWorkarounds graphicsWorkarounds) {
        if (gLCapabilities.GL_ARB_direct_state_access && GlDevice.USE_GL_ARB_direct_state_access && !graphicsWorkarounds.isGlOnDx12()) {
            set.add("GL_ARB_direct_state_access");
            return new Core();
        }
        return new Emulated();
    }

    abstract int createBuffer();

    abstract void bufferData(int var1, long var2, @GpuBuffer.Usage int var4);

    abstract void bufferData(int var1, ByteBuffer var2, @GpuBuffer.Usage int var3);

    abstract void bufferSubData(int var1, long var2, ByteBuffer var4, @GpuBuffer.Usage int var5);

    abstract void bufferStorage(int var1, long var2, @GpuBuffer.Usage int var4);

    abstract void bufferStorage(int var1, ByteBuffer var2, @GpuBuffer.Usage int var3);

    abstract @Nullable ByteBuffer mapBufferRange(int var1, long var2, long var4, int var6, @GpuBuffer.Usage int var7);

    abstract void unmapBuffer(int var1, @GpuBuffer.Usage int var2);

    abstract int createFrameBufferObject();

    abstract void bindFrameBufferTextures(int var1, int var2, int var3, int var4, int var5);

    abstract void blitFrameBuffers(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12);

    abstract void flushMappedBufferRange(int var1, long var2, long var4, @GpuBuffer.Usage int var6);

    abstract void copyBufferSubData(int var1, int var2, long var3, long var5, long var7);

    @Environment(value=EnvType.CLIENT)
    static class Core
    extends DirectStateAccess {
        Core() {
        }

        @Override
        int createBuffer() {
            GlStateManager.incrementTrackedBuffers();
            return ARBDirectStateAccess.glCreateBuffers();
        }

        @Override
        void bufferData(int i, long l, @GpuBuffer.Usage int j) {
            ARBDirectStateAccess.glNamedBufferData((int)i, (long)l, (int)GlConst.bufferUsageToGlEnum(j));
        }

        @Override
        void bufferData(int i, ByteBuffer byteBuffer, @GpuBuffer.Usage int j) {
            ARBDirectStateAccess.glNamedBufferData((int)i, (ByteBuffer)byteBuffer, (int)GlConst.bufferUsageToGlEnum(j));
        }

        @Override
        void bufferSubData(int i, long l, ByteBuffer byteBuffer, @GpuBuffer.Usage int j) {
            ARBDirectStateAccess.glNamedBufferSubData((int)i, (long)l, (ByteBuffer)byteBuffer);
        }

        @Override
        void bufferStorage(int i, long l, @GpuBuffer.Usage int j) {
            ARBDirectStateAccess.glNamedBufferStorage((int)i, (long)l, (int)GlConst.bufferUsageToGlFlag(j));
        }

        @Override
        void bufferStorage(int i, ByteBuffer byteBuffer, @GpuBuffer.Usage int j) {
            ARBDirectStateAccess.glNamedBufferStorage((int)i, (ByteBuffer)byteBuffer, (int)GlConst.bufferUsageToGlFlag(j));
        }

        @Override
        @Nullable ByteBuffer mapBufferRange(int i, long l, long m, int j, @GpuBuffer.Usage int k) {
            return ARBDirectStateAccess.glMapNamedBufferRange((int)i, (long)l, (long)m, (int)j);
        }

        @Override
        void unmapBuffer(int i, int j) {
            ARBDirectStateAccess.glUnmapNamedBuffer((int)i);
        }

        @Override
        public int createFrameBufferObject() {
            return ARBDirectStateAccess.glCreateFramebuffers();
        }

        @Override
        public void bindFrameBufferTextures(int i, int j, int k, int l, @GpuBuffer.Usage int m) {
            ARBDirectStateAccess.glNamedFramebufferTexture((int)i, (int)36064, (int)j, (int)l);
            ARBDirectStateAccess.glNamedFramebufferTexture((int)i, (int)36096, (int)k, (int)l);
            if (m != 0) {
                GlStateManager._glBindFramebuffer(m, i);
            }
        }

        @Override
        public void blitFrameBuffers(int i, int j, int k, int l, int m, int n, int o, int p, int q, int r, int s, int t) {
            ARBDirectStateAccess.glBlitNamedFramebuffer((int)i, (int)j, (int)k, (int)l, (int)m, (int)n, (int)o, (int)p, (int)q, (int)r, (int)s, (int)t);
        }

        @Override
        void flushMappedBufferRange(int i, long l, long m, @GpuBuffer.Usage int j) {
            ARBDirectStateAccess.glFlushMappedNamedBufferRange((int)i, (long)l, (long)m);
        }

        @Override
        void copyBufferSubData(int i, int j, long l, long m, long n) {
            ARBDirectStateAccess.glCopyNamedBufferSubData((int)i, (int)j, (long)l, (long)m, (long)n);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Emulated
    extends DirectStateAccess {
        Emulated() {
        }

        private int selectBufferBindTarget(@GpuBuffer.Usage int i) {
            if ((i & 0x20) != 0) {
                return 34962;
            }
            if ((i & 0x40) != 0) {
                return 34963;
            }
            if ((i & 0x80) != 0) {
                return 35345;
            }
            return 36663;
        }

        @Override
        int createBuffer() {
            return GlStateManager._glGenBuffers();
        }

        @Override
        void bufferData(int i, long l, @GpuBuffer.Usage int j) {
            int k = this.selectBufferBindTarget(j);
            GlStateManager._glBindBuffer(k, i);
            GlStateManager._glBufferData(k, l, GlConst.bufferUsageToGlEnum(j));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void bufferData(int i, ByteBuffer byteBuffer, @GpuBuffer.Usage int j) {
            int k = this.selectBufferBindTarget(j);
            GlStateManager._glBindBuffer(k, i);
            GlStateManager._glBufferData(k, byteBuffer, GlConst.bufferUsageToGlEnum(j));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void bufferSubData(int i, long l, ByteBuffer byteBuffer, @GpuBuffer.Usage int j) {
            int k = this.selectBufferBindTarget(j);
            GlStateManager._glBindBuffer(k, i);
            GlStateManager._glBufferSubData(k, l, byteBuffer);
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void bufferStorage(int i, long l, @GpuBuffer.Usage int j) {
            int k = this.selectBufferBindTarget(j);
            GlStateManager._glBindBuffer(k, i);
            ARBBufferStorage.glBufferStorage((int)k, (long)l, (int)GlConst.bufferUsageToGlFlag(j));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void bufferStorage(int i, ByteBuffer byteBuffer, @GpuBuffer.Usage int j) {
            int k = this.selectBufferBindTarget(j);
            GlStateManager._glBindBuffer(k, i);
            ARBBufferStorage.glBufferStorage((int)k, (ByteBuffer)byteBuffer, (int)GlConst.bufferUsageToGlFlag(j));
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        @Nullable ByteBuffer mapBufferRange(int i, long l, long m, int j, @GpuBuffer.Usage int k) {
            int n = this.selectBufferBindTarget(k);
            GlStateManager._glBindBuffer(n, i);
            ByteBuffer byteBuffer = GlStateManager._glMapBufferRange(n, l, m, j);
            GlStateManager._glBindBuffer(n, 0);
            return byteBuffer;
        }

        @Override
        void unmapBuffer(int i, @GpuBuffer.Usage int j) {
            int k = this.selectBufferBindTarget(j);
            GlStateManager._glBindBuffer(k, i);
            GlStateManager._glUnmapBuffer(k);
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void flushMappedBufferRange(int i, long l, long m, @GpuBuffer.Usage int j) {
            int k = this.selectBufferBindTarget(j);
            GlStateManager._glBindBuffer(k, i);
            GL30.glFlushMappedBufferRange((int)k, (long)l, (long)m);
            GlStateManager._glBindBuffer(k, 0);
        }

        @Override
        void copyBufferSubData(int i, int j, long l, long m, long n) {
            GlStateManager._glBindBuffer(36662, i);
            GlStateManager._glBindBuffer(36663, j);
            GL31.glCopyBufferSubData((int)36662, (int)36663, (long)l, (long)m, (long)n);
            GlStateManager._glBindBuffer(36662, 0);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        public int createFrameBufferObject() {
            return GlStateManager.glGenFramebuffers();
        }

        @Override
        public void bindFrameBufferTextures(int i, int j, int k, int l, int m) {
            int n = m == 0 ? 36009 : m;
            int o = GlStateManager.getFrameBuffer(n);
            GlStateManager._glBindFramebuffer(n, i);
            GlStateManager._glFramebufferTexture2D(n, 36064, 3553, j, l);
            GlStateManager._glFramebufferTexture2D(n, 36096, 3553, k, l);
            if (m == 0) {
                GlStateManager._glBindFramebuffer(n, o);
            }
        }

        @Override
        public void blitFrameBuffers(int i, int j, int k, int l, int m, int n, int o, int p, int q, int r, int s, int t) {
            int u = GlStateManager.getFrameBuffer(36008);
            int v = GlStateManager.getFrameBuffer(36009);
            GlStateManager._glBindFramebuffer(36008, i);
            GlStateManager._glBindFramebuffer(36009, j);
            GlStateManager._glBlitFrameBuffer(k, l, m, n, o, p, q, r, s, t);
            GlStateManager._glBindFramebuffer(36008, u);
            GlStateManager._glBindFramebuffer(36009, v);
        }
    }
}

