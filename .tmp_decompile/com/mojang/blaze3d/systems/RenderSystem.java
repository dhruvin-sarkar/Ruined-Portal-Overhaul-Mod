/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntConsumer
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWErrorCallbackI
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.SamplerCache;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import net.minecraft.util.Util;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
@DontObfuscate
public class RenderSystem {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    public static final int PROJECTION_MATRIX_UBO_SIZE = new Std140SizeCalculator().putMat4f().get();
    private static @Nullable Thread renderThread;
    private static @Nullable GpuDevice DEVICE;
    private static double lastDrawTime;
    private static final AutoStorageIndexBuffer sharedSequential;
    private static final AutoStorageIndexBuffer sharedSequentialQuad;
    private static final AutoStorageIndexBuffer sharedSequentialLines;
    private static ProjectionType projectionType;
    private static ProjectionType savedProjectionType;
    private static final Matrix4fStack modelViewStack;
    private static @Nullable GpuBufferSlice shaderFog;
    private static @Nullable GpuBufferSlice shaderLightDirections;
    private static @Nullable GpuBufferSlice projectionMatrixBuffer;
    private static @Nullable GpuBufferSlice savedProjectionMatrixBuffer;
    private static String apiDescription;
    private static final AtomicLong pollEventsWaitStart;
    private static final AtomicBoolean pollingEvents;
    private static final ArrayListDeque<GpuAsyncTask> PENDING_FENCES;
    public static @Nullable GpuTextureView outputColorTextureOverride;
    public static @Nullable GpuTextureView outputDepthTextureOverride;
    private static @Nullable GpuBuffer globalSettingsUniform;
    private static @Nullable DynamicUniforms dynamicUniforms;
    private static final ScissorState scissorStateForRenderTypeDraws;
    private static SamplerCache samplerCache;

    public static SamplerCache getSamplerCache() {
        return samplerCache;
    }

    public static void initRenderThread() {
        if (renderThread != null) {
            throw new IllegalStateException("Could not initialize render thread");
        }
        renderThread = Thread.currentThread();
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static void assertOnRenderThread() {
        if (!RenderSystem.isOnRenderThread()) {
            throw RenderSystem.constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    private static void pollEvents() {
        pollEventsWaitStart.set(Util.getMillis());
        pollingEvents.set(true);
        GLFW.glfwPollEvents();
        pollingEvents.set(false);
    }

    public static boolean isFrozenAtPollEvents() {
        return pollingEvents.get() && Util.getMillis() - pollEventsWaitStart.get() > 200L;
    }

    public static void flipFrame(Window window, @Nullable TracyFrameCapture tracyFrameCapture) {
        RenderSystem.pollEvents();
        Tesselator.getInstance().clear();
        GLFW.glfwSwapBuffers((long)window.handle());
        if (tracyFrameCapture != null) {
            tracyFrameCapture.endFrame();
        }
        dynamicUniforms.reset();
        Minecraft.getInstance().levelRenderer.endFrame();
        RenderSystem.pollEvents();
    }

    public static void limitDisplayFPS(int i) {
        double d = lastDrawTime + 1.0 / (double)i;
        double e = GLFW.glfwGetTime();
        while (e < d) {
            GLFW.glfwWaitEventsTimeout((double)(d - e));
            e = GLFW.glfwGetTime();
        }
        lastDrawTime = e;
    }

    public static void setShaderFog(GpuBufferSlice gpuBufferSlice) {
        shaderFog = gpuBufferSlice;
    }

    public static @Nullable GpuBufferSlice getShaderFog() {
        return shaderFog;
    }

    public static void setShaderLights(GpuBufferSlice gpuBufferSlice) {
        shaderLightDirections = gpuBufferSlice;
    }

    public static @Nullable GpuBufferSlice getShaderLights() {
        return shaderLightDirections;
    }

    public static void enableScissorForRenderTypeDraws(int i, int j, int k, int l) {
        scissorStateForRenderTypeDraws.enable(i, j, k, l);
    }

    public static void disableScissorForRenderTypeDraws() {
        scissorStateForRenderTypeDraws.disable();
    }

    public static ScissorState getScissorStateForRenderTypeDraws() {
        return scissorStateForRenderTypeDraws;
    }

    public static String getBackendDescription() {
        return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        return apiDescription;
    }

    public static TimeSource.NanoTimeSource initBackendSystem() {
        return GLX._initGlfw()::getAsLong;
    }

    public static void initRenderer(long l, int i, boolean bl, ShaderSource shaderSource, boolean bl2) {
        DEVICE = new GlDevice(l, i, bl, shaderSource, bl2);
        apiDescription = RenderSystem.getDevice().getImplementationInformation();
        dynamicUniforms = new DynamicUniforms();
        samplerCache.initialize();
    }

    public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
        GLX._setGlfwErrorCallback(gLFWErrorCallbackI);
    }

    public static void setupDefaultState() {
        modelViewStack.clear();
    }

    public static void setProjectionMatrix(GpuBufferSlice gpuBufferSlice, ProjectionType projectionType) {
        RenderSystem.assertOnRenderThread();
        projectionMatrixBuffer = gpuBufferSlice;
        RenderSystem.projectionType = projectionType;
    }

    public static void backupProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        savedProjectionMatrixBuffer = projectionMatrixBuffer;
        savedProjectionType = projectionType;
    }

    public static void restoreProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        projectionMatrixBuffer = savedProjectionMatrixBuffer;
        projectionType = savedProjectionType;
    }

    public static @Nullable GpuBufferSlice getProjectionMatrixBuffer() {
        RenderSystem.assertOnRenderThread();
        return projectionMatrixBuffer;
    }

    public static Matrix4f getModelViewMatrix() {
        RenderSystem.assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4fStack getModelViewStack() {
        RenderSystem.assertOnRenderThread();
        return modelViewStack;
    }

    public static AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode mode) {
        RenderSystem.assertOnRenderThread();
        return switch (mode) {
            case VertexFormat.Mode.QUADS -> sharedSequentialQuad;
            case VertexFormat.Mode.LINES -> sharedSequentialLines;
            default -> sharedSequential;
        };
    }

    public static void setGlobalSettingsUniform(GpuBuffer gpuBuffer) {
        globalSettingsUniform = gpuBuffer;
    }

    public static @Nullable GpuBuffer getGlobalSettingsUniform() {
        return globalSettingsUniform;
    }

    public static ProjectionType getProjectionType() {
        RenderSystem.assertOnRenderThread();
        return projectionType;
    }

    public static void queueFencedTask(Runnable runnable) {
        PENDING_FENCES.addLast(new GpuAsyncTask(runnable, RenderSystem.getDevice().createCommandEncoder().createFence()));
    }

    public static void executePendingTasks() {
        GpuAsyncTask gpuAsyncTask = PENDING_FENCES.peekFirst();
        while (gpuAsyncTask != null) {
            if (gpuAsyncTask.fence.awaitCompletion(0L)) {
                try {
                    gpuAsyncTask.callback.run();
                }
                finally {
                    gpuAsyncTask.fence.close();
                }
                PENDING_FENCES.removeFirst();
                gpuAsyncTask = PENDING_FENCES.peekFirst();
                continue;
            }
            return;
        }
    }

    public static GpuDevice getDevice() {
        if (DEVICE == null) {
            throw new IllegalStateException("Can't getDevice() before it was initialized");
        }
        return DEVICE;
    }

    public static @Nullable GpuDevice tryGetDevice() {
        return DEVICE;
    }

    public static DynamicUniforms getDynamicUniforms() {
        if (dynamicUniforms == null) {
            throw new IllegalStateException("Can't getDynamicUniforms() before device was initialized");
        }
        return dynamicUniforms;
    }

    public static void bindDefaultUniforms(RenderPass renderPass) {
        GpuBufferSlice gpuBufferSlice3;
        GpuBuffer gpuBuffer;
        GpuBufferSlice gpuBufferSlice2;
        GpuBufferSlice gpuBufferSlice = RenderSystem.getProjectionMatrixBuffer();
        if (gpuBufferSlice != null) {
            renderPass.setUniform("Projection", gpuBufferSlice);
        }
        if ((gpuBufferSlice2 = RenderSystem.getShaderFog()) != null) {
            renderPass.setUniform("Fog", gpuBufferSlice2);
        }
        if ((gpuBuffer = RenderSystem.getGlobalSettingsUniform()) != null) {
            renderPass.setUniform("Globals", gpuBuffer);
        }
        if ((gpuBufferSlice3 = RenderSystem.getShaderLights()) != null) {
            renderPass.setUniform("Lighting", gpuBufferSlice3);
        }
    }

    static {
        lastDrawTime = Double.MIN_VALUE;
        sharedSequential = new AutoStorageIndexBuffer(1, 1, java.util.function.IntConsumer::accept);
        sharedSequentialQuad = new AutoStorageIndexBuffer(4, 6, (intConsumer, i) -> {
            intConsumer.accept(i);
            intConsumer.accept(i + 1);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 3);
            intConsumer.accept(i);
        });
        sharedSequentialLines = new AutoStorageIndexBuffer(4, 6, (intConsumer, i) -> {
            intConsumer.accept(i);
            intConsumer.accept(i + 1);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 3);
            intConsumer.accept(i + 2);
            intConsumer.accept(i + 1);
        });
        projectionType = ProjectionType.PERSPECTIVE;
        savedProjectionType = ProjectionType.PERSPECTIVE;
        modelViewStack = new Matrix4fStack(16);
        shaderFog = null;
        apiDescription = "Unknown";
        pollEventsWaitStart = new AtomicLong();
        pollingEvents = new AtomicBoolean(false);
        PENDING_FENCES = new ArrayListDeque();
        scissorStateForRenderTypeDraws = new ScissorState();
        samplerCache = new SamplerCache();
    }

    @Environment(value=EnvType.CLIENT)
    public static final class AutoStorageIndexBuffer {
        private final int vertexStride;
        private final int indexStride;
        private final IndexGenerator generator;
        private @Nullable GpuBuffer buffer;
        private VertexFormat.IndexType type = VertexFormat.IndexType.SHORT;
        private int indexCount;

        AutoStorageIndexBuffer(int i, int j, IndexGenerator indexGenerator) {
            this.vertexStride = i;
            this.indexStride = j;
            this.generator = indexGenerator;
        }

        public boolean hasStorage(int i) {
            return i <= this.indexCount;
        }

        public GpuBuffer getBuffer(int i) {
            this.ensureStorage(i);
            return this.buffer;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void ensureStorage(int i) {
            if (this.hasStorage(i)) {
                return;
            }
            i = Mth.roundToward(i * 2, this.indexStride);
            LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", (Object)this.indexCount, (Object)i);
            int j = i / this.indexStride;
            int k = j * this.vertexStride;
            VertexFormat.IndexType indexType = VertexFormat.IndexType.least(k);
            int l = Mth.roundToward(i * indexType.bytes, 4);
            ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)l);
            try {
                this.type = indexType;
                IntConsumer intConsumer = this.intConsumer(byteBuffer);
                for (int m = 0; m < i; m += this.indexStride) {
                    this.generator.accept(intConsumer, m * this.vertexStride / this.indexStride);
                }
                byteBuffer.flip();
                if (this.buffer != null) {
                    this.buffer.close();
                }
                this.buffer = RenderSystem.getDevice().createBuffer(() -> "Auto Storage index buffer", 64, byteBuffer);
            }
            finally {
                MemoryUtil.memFree((Buffer)byteBuffer);
            }
            this.indexCount = i;
        }

        private IntConsumer intConsumer(ByteBuffer byteBuffer) {
            switch (this.type) {
                case SHORT: {
                    return i -> byteBuffer.putShort((short)i);
                }
            }
            return byteBuffer::putInt;
        }

        public VertexFormat.IndexType type() {
            return this.type;
        }

        @Environment(value=EnvType.CLIENT)
        static interface IndexGenerator {
            public void accept(IntConsumer var1, int var2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class GpuAsyncTask
    extends Record {
        final Runnable callback;
        final GpuFence fence;

        GpuAsyncTask(Runnable runnable, GpuFence gpuFence) {
            this.callback = runnable;
            this.fence = gpuFence;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GpuAsyncTask.class, "callback;fence", "callback", "fence"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GpuAsyncTask.class, "callback;fence", "callback", "fence"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GpuAsyncTask.class, "callback;fence", "callback", "fence"}, this, object);
        }

        public Runnable callback() {
            return this.callback;
        }

        public GpuFence fence() {
            return this.fence;
        }
    }
}

