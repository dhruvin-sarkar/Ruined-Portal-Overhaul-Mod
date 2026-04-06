/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GLCapabilities
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.GraphicsWorkarounds;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.BufferStorage;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDebug;
import com.mojang.blaze3d.opengl.GlDebugLabel;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderSource;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GlDevice
implements GpuDevice {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static boolean USE_GL_ARB_vertex_attrib_binding = true;
    protected static boolean USE_GL_KHR_debug = true;
    protected static boolean USE_GL_EXT_debug_label = true;
    protected static boolean USE_GL_ARB_debug_output = true;
    protected static boolean USE_GL_ARB_direct_state_access = true;
    protected static boolean USE_GL_ARB_buffer_storage = true;
    private final CommandEncoder encoder;
    private final @Nullable GlDebug debugLog;
    private final GlDebugLabel debugLabels;
    private final int maxSupportedTextureSize;
    private final DirectStateAccess directStateAccess;
    private final ShaderSource defaultShaderSource;
    private final Map<RenderPipeline, GlRenderPipeline> pipelineCache = new IdentityHashMap<RenderPipeline, GlRenderPipeline>();
    private final Map<ShaderCompilationKey, GlShaderModule> shaderCache = new HashMap<ShaderCompilationKey, GlShaderModule>();
    private final VertexArrayCache vertexArrayCache;
    private final BufferStorage bufferStorage;
    private final Set<String> enabledExtensions = new HashSet<String>();
    private final int uniformOffsetAlignment;
    private final int maxSupportedAnisotropy;

    public GlDevice(long l, int i, boolean bl, ShaderSource shaderSource, boolean bl2) {
        GLFW.glfwMakeContextCurrent((long)l);
        GLCapabilities gLCapabilities = GL.createCapabilities();
        int j = GlDevice.getMaxSupportedTextureSize();
        GLFW.glfwSetWindowSizeLimits((long)l, (int)-1, (int)-1, (int)j, (int)j);
        GraphicsWorkarounds graphicsWorkarounds = GraphicsWorkarounds.get(this);
        this.debugLog = GlDebug.enableDebugCallback(i, bl, this.enabledExtensions);
        this.debugLabels = GlDebugLabel.create(gLCapabilities, bl2, this.enabledExtensions);
        this.vertexArrayCache = VertexArrayCache.create(gLCapabilities, this.debugLabels, this.enabledExtensions);
        this.bufferStorage = BufferStorage.create(gLCapabilities, this.enabledExtensions);
        this.directStateAccess = DirectStateAccess.create(gLCapabilities, this.enabledExtensions, graphicsWorkarounds);
        this.maxSupportedTextureSize = j;
        this.defaultShaderSource = shaderSource;
        this.encoder = new GlCommandEncoder(this);
        this.uniformOffsetAlignment = GL11.glGetInteger((int)35380);
        GL11.glEnable((int)34895);
        GL11.glEnable((int)34370);
        if (gLCapabilities.GL_EXT_texture_filter_anisotropic) {
            this.maxSupportedAnisotropy = Mth.floor(GL11.glGetFloat((int)34047));
            this.enabledExtensions.add("GL_EXT_texture_filter_anisotropic");
        } else {
            this.maxSupportedAnisotropy = 1;
        }
    }

    public GlDebugLabel debugLabels() {
        return this.debugLabels;
    }

    @Override
    public CommandEncoder createCommandEncoder() {
        return this.encoder;
    }

    @Override
    public int getMaxSupportedAnisotropy() {
        return this.maxSupportedAnisotropy;
    }

    @Override
    public GpuSampler createSampler(AddressMode addressMode, AddressMode addressMode2, FilterMode filterMode, FilterMode filterMode2, int i, OptionalDouble optionalDouble) {
        if (i < 1 || i > this.maxSupportedAnisotropy) {
            throw new IllegalArgumentException("maxAnisotropy out of range; must be >= 1 and <= " + this.getMaxSupportedAnisotropy() + ", but was " + i);
        }
        return new GlSampler(addressMode, addressMode2, filterMode, filterMode2, i, optionalDouble);
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> supplier, @GpuTexture.Usage int i, TextureFormat textureFormat, int j, int k, int l, int m) {
        return this.createTexture(this.debugLabels.exists() && supplier != null ? supplier.get() : null, i, textureFormat, j, k, l, m);
    }

    @Override
    public GpuTexture createTexture(@Nullable String string, @GpuTexture.Usage int i, TextureFormat textureFormat, int j, int k, int l, int m) {
        int r;
        int o;
        boolean bl;
        if (m < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        }
        if (l < 1) {
            throw new IllegalArgumentException("depthOrLayers must be at least 1");
        }
        boolean bl2 = bl = (i & 0x10) != 0;
        if (bl) {
            if (j != k) {
                throw new IllegalArgumentException("Cubemap compatible textures must be square, but size is " + j + "x" + k);
            }
            if (l % 6 != 0) {
                throw new IllegalArgumentException("Cubemap compatible textures must have a layer count with a multiple of 6, was " + l);
            }
            if (l > 6) {
                throw new UnsupportedOperationException("Array textures are not yet supported");
            }
        } else if (l > 1) {
            throw new UnsupportedOperationException("Array or 3D textures are not yet supported");
        }
        GlStateManager.clearGlErrors();
        int n = GlStateManager._genTexture();
        if (string == null) {
            string = String.valueOf(n);
        }
        if (bl) {
            GL11.glBindTexture((int)34067, (int)n);
            o = 34067;
        } else {
            GlStateManager._bindTexture(n);
            o = 3553;
        }
        GlStateManager._texParameter(o, 33085, m - 1);
        GlStateManager._texParameter(o, 33082, 0);
        GlStateManager._texParameter(o, 33083, m - 1);
        if (textureFormat.hasDepthAspect()) {
            GlStateManager._texParameter(o, 34892, 0);
        }
        if (bl) {
            for (int p : GlConst.CUBEMAP_TARGETS) {
                for (int q = 0; q < m; ++q) {
                    GlStateManager._texImage2D(p, q, GlConst.toGlInternalId(textureFormat), j >> q, k >> q, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
                }
            }
        } else {
            for (int r2 = 0; r2 < m; ++r2) {
                GlStateManager._texImage2D(o, r2, GlConst.toGlInternalId(textureFormat), j >> r2, k >> r2, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
            }
        }
        if ((r = GlStateManager._getError()) == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate texture of " + j + "x" + k + " for " + string);
        }
        if (r != 0) {
            throw new IllegalStateException("OpenGL error " + r);
        }
        GlTexture glTexture = new GlTexture(i, string, textureFormat, j, k, l, m, n);
        this.debugLabels.applyLabel(glTexture);
        return glTexture;
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture) {
        return this.createTextureView(gpuTexture, 0, gpuTexture.getMipLevels());
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture, int i, int j) {
        if (gpuTexture.isClosed()) {
            throw new IllegalArgumentException("Can't create texture view with closed texture");
        }
        if (i < 0 || i + j > gpuTexture.getMipLevels()) {
            throw new IllegalArgumentException(j + " mip levels starting from " + i + " would be out of range for texture with only " + gpuTexture.getMipLevels() + " mip levels");
        }
        return new GlTextureView((GlTexture)gpuTexture, i, j);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, @GpuBuffer.Usage int i, long l) {
        if (l <= 0L) {
            throw new IllegalArgumentException("Buffer size must be greater than zero");
        }
        GlStateManager.clearGlErrors();
        GlBuffer glBuffer = this.bufferStorage.createBuffer(this.directStateAccess, supplier, i, l);
        int j = GlStateManager._getError();
        if (j == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate buffer of " + l + " for " + String.valueOf(supplier));
        }
        if (j != 0) {
            throw new IllegalStateException("OpenGL error " + j);
        }
        this.debugLabels.applyLabel(glBuffer);
        return glBuffer;
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, @GpuBuffer.Usage int i, ByteBuffer byteBuffer) {
        if (!byteBuffer.hasRemaining()) {
            throw new IllegalArgumentException("Buffer source must not be empty");
        }
        GlStateManager.clearGlErrors();
        long l = byteBuffer.remaining();
        GlBuffer glBuffer = this.bufferStorage.createBuffer(this.directStateAccess, supplier, i, byteBuffer);
        int j = GlStateManager._getError();
        if (j == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate buffer of " + l + " for " + String.valueOf(supplier));
        }
        if (j != 0) {
            throw new IllegalStateException("OpenGL error " + j);
        }
        this.debugLabels.applyLabel(glBuffer);
        return glBuffer;
    }

    @Override
    public String getImplementationInformation() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return "NO CONTEXT";
        }
        return GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    @Override
    public List<String> getLastDebugMessages() {
        return this.debugLog == null ? Collections.emptyList() : this.debugLog.getLastOpenGlDebugMessages();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return this.debugLog != null;
    }

    @Override
    public String getRenderer() {
        return GlStateManager._getString(7937);
    }

    @Override
    public String getVendor() {
        return GlStateManager._getString(7936);
    }

    @Override
    public String getBackendName() {
        return "OpenGL";
    }

    @Override
    public String getVersion() {
        return GlStateManager._getString(7938);
    }

    private static int getMaxSupportedTextureSize() {
        int j;
        int i = GlStateManager._getInteger(3379);
        for (j = Math.max(32768, i); j >= 1024; j >>= 1) {
            GlStateManager._texImage2D(32868, 0, 6408, j, j, 0, 6408, 5121, null);
            int k = GlStateManager._getTexLevelParameter(32868, 0, 4096);
            if (k == 0) continue;
            return j;
        }
        j = Math.max(i, 1024);
        LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (Object)j);
        return j;
    }

    @Override
    public int getMaxTextureSize() {
        return this.maxSupportedTextureSize;
    }

    @Override
    public int getUniformOffsetAlignment() {
        return this.uniformOffsetAlignment;
    }

    @Override
    public void clearPipelineCache() {
        for (GlRenderPipeline glRenderPipeline : this.pipelineCache.values()) {
            if (glRenderPipeline.program() == GlProgram.INVALID_PROGRAM) continue;
            glRenderPipeline.program().close();
        }
        this.pipelineCache.clear();
        for (GlShaderModule glShaderModule : this.shaderCache.values()) {
            if (glShaderModule == GlShaderModule.INVALID_SHADER) continue;
            glShaderModule.close();
        }
        this.shaderCache.clear();
        String string = GlStateManager._getString(7937);
        if (string.contains("AMD")) {
            GlDevice.sacrificeShaderToOpenGlAndAmd();
        }
    }

    private static void sacrificeShaderToOpenGlAndAmd() {
        int i = GlStateManager.glCreateShader(35633);
        int j = GlStateManager.glCreateProgram();
        GlStateManager.glAttachShader(j, i);
        GlStateManager.glDeleteShader(i);
        GlStateManager.glDeleteProgram(j);
    }

    @Override
    public List<String> getEnabledExtensions() {
        return new ArrayList<String>(this.enabledExtensions);
    }

    @Override
    public void close() {
        this.clearPipelineCache();
    }

    public DirectStateAccess directStateAccess() {
        return this.directStateAccess;
    }

    protected GlRenderPipeline getOrCompilePipeline(RenderPipeline renderPipeline2) {
        return this.pipelineCache.computeIfAbsent(renderPipeline2, renderPipeline -> this.compilePipeline((RenderPipeline)renderPipeline, this.defaultShaderSource));
    }

    protected GlShaderModule getOrCompileShader(Identifier identifier, ShaderType shaderType, ShaderDefines shaderDefines, ShaderSource shaderSource) {
        ShaderCompilationKey shaderCompilationKey2 = new ShaderCompilationKey(identifier, shaderType, shaderDefines);
        return this.shaderCache.computeIfAbsent(shaderCompilationKey2, shaderCompilationKey -> this.compileShader((ShaderCompilationKey)((Object)shaderCompilationKey), shaderSource));
    }

    @Override
    public GlRenderPipeline precompilePipeline(RenderPipeline renderPipeline2, @Nullable ShaderSource shaderSource) {
        ShaderSource shaderSource2 = shaderSource == null ? this.defaultShaderSource : shaderSource;
        return this.pipelineCache.computeIfAbsent(renderPipeline2, renderPipeline -> this.compilePipeline((RenderPipeline)renderPipeline, shaderSource2));
    }

    private GlShaderModule compileShader(ShaderCompilationKey shaderCompilationKey, ShaderSource shaderSource) {
        String string = shaderSource.get(shaderCompilationKey.id, shaderCompilationKey.type);
        if (string == null) {
            LOGGER.error("Couldn't find source for {} shader ({})", (Object)shaderCompilationKey.type, (Object)shaderCompilationKey.id);
            return GlShaderModule.INVALID_SHADER;
        }
        String string2 = GlslPreprocessor.injectDefines(string, shaderCompilationKey.defines);
        int i = GlStateManager.glCreateShader(GlConst.toGl(shaderCompilationKey.type));
        GlStateManager.glShaderSource(i, string2);
        GlStateManager.glCompileShader(i);
        if (GlStateManager.glGetShaderi(i, 35713) == 0) {
            String string3 = StringUtils.trim((String)GlStateManager.glGetShaderInfoLog(i, 32768));
            LOGGER.error("Couldn't compile {} shader ({}): {}", new Object[]{shaderCompilationKey.type.getName(), shaderCompilationKey.id, string3});
            return GlShaderModule.INVALID_SHADER;
        }
        GlShaderModule glShaderModule = new GlShaderModule(i, shaderCompilationKey.id, shaderCompilationKey.type);
        this.debugLabels.applyLabel(glShaderModule);
        return glShaderModule;
    }

    private GlProgram compileProgram(RenderPipeline renderPipeline, ShaderSource shaderSource) {
        GlShaderModule glShaderModule = this.getOrCompileShader(renderPipeline.getVertexShader(), ShaderType.VERTEX, renderPipeline.getShaderDefines(), shaderSource);
        GlShaderModule glShaderModule2 = this.getOrCompileShader(renderPipeline.getFragmentShader(), ShaderType.FRAGMENT, renderPipeline.getShaderDefines(), shaderSource);
        if (glShaderModule == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: vertex shader {} was invalid", (Object)renderPipeline.getLocation(), (Object)renderPipeline.getVertexShader());
            return GlProgram.INVALID_PROGRAM;
        }
        if (glShaderModule2 == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: fragment shader {} was invalid", (Object)renderPipeline.getLocation(), (Object)renderPipeline.getFragmentShader());
            return GlProgram.INVALID_PROGRAM;
        }
        try {
            GlProgram glProgram = GlProgram.link(glShaderModule, glShaderModule2, renderPipeline.getVertexFormat(), renderPipeline.getLocation().toString());
            glProgram.setupUniforms(renderPipeline.getUniforms(), renderPipeline.getSamplers());
            this.debugLabels.applyLabel(glProgram);
            return glProgram;
        }
        catch (ShaderManager.CompilationException compilationException) {
            LOGGER.error("Couldn't compile program for pipeline {}: {}", (Object)renderPipeline.getLocation(), (Object)compilationException);
            return GlProgram.INVALID_PROGRAM;
        }
    }

    private GlRenderPipeline compilePipeline(RenderPipeline renderPipeline, ShaderSource shaderSource) {
        return new GlRenderPipeline(renderPipeline, this.compileProgram(renderPipeline, shaderSource));
    }

    public VertexArrayCache vertexArrayCache() {
        return this.vertexArrayCache;
    }

    public BufferStorage getBufferStorage() {
        return this.bufferStorage;
    }

    @Override
    public /* synthetic */ CompiledRenderPipeline precompilePipeline(RenderPipeline renderPipeline, @Nullable ShaderSource shaderSource) {
        return this.precompilePipeline(renderPipeline, shaderSource);
    }

    @Environment(value=EnvType.CLIENT)
    static final class ShaderCompilationKey
    extends Record {
        final Identifier id;
        final ShaderType type;
        final ShaderDefines defines;

        ShaderCompilationKey(Identifier identifier, ShaderType shaderType, ShaderDefines shaderDefines) {
            this.id = identifier;
            this.type = shaderType;
            this.defines = shaderDefines;
        }

        public String toString() {
            String string = String.valueOf(this.id) + " (" + String.valueOf((Object)this.type) + ")";
            if (!this.defines.isEmpty()) {
                return string + " with " + String.valueOf((Object)this.defines);
            }
            return string;
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ShaderCompilationKey.class, "id;type;defines", "id", "type", "defines"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ShaderCompilationKey.class, "id;type;defines", "id", "type", "defines"}, this, object);
        }

        public Identifier id() {
            return this.id;
        }

        public ShaderType type() {
            return this.type;
        }

        public ShaderDefines defines() {
            return this.defines;
        }
    }
}

