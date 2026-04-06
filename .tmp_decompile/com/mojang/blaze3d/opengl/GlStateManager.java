/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.Plot
 *  com.mojang.jtracy.TracyClient
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL13
 *  org.lwjgl.opengl.GL14
 *  org.lwjgl.opengl.GL15
 *  org.lwjgl.opengl.GL20
 *  org.lwjgl.opengl.GL20C
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.opengl.GL32
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
@DontObfuscate
public class GlStateManager {
    private static final Plot PLOT_TEXTURES = TracyClient.createPlot((String)"GPU Textures");
    private static int numTextures = 0;
    private static final Plot PLOT_BUFFERS = TracyClient.createPlot((String)"GPU Buffers");
    private static int numBuffers = 0;
    private static final BlendState BLEND = new BlendState();
    private static final DepthState DEPTH = new DepthState();
    private static final CullState CULL = new CullState();
    private static final PolygonOffsetState POLY_OFFSET = new PolygonOffsetState();
    private static final ColorLogicState COLOR_LOGIC = new ColorLogicState();
    private static final ScissorState SCISSOR = new ScissorState();
    private static int activeTexture;
    private static final int TEXTURE_COUNT = 12;
    private static final TextureState[] TEXTURES;
    private static final ColorMask COLOR_MASK;
    private static int readFbo;
    private static int writeFbo;

    public static void _disableScissorTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.SCISSOR.mode.disable();
    }

    public static void _enableScissorTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.SCISSOR.mode.enable();
    }

    public static void _scissorBox(int i, int j, int k, int l) {
        RenderSystem.assertOnRenderThread();
        GL20.glScissor((int)i, (int)j, (int)k, (int)l);
    }

    public static void _disableDepthTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.DEPTH.mode.disable();
    }

    public static void _enableDepthTest() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.DEPTH.mode.enable();
    }

    public static void _depthFunc(int i) {
        RenderSystem.assertOnRenderThread();
        if (i != GlStateManager.DEPTH.func) {
            GlStateManager.DEPTH.func = i;
            GL11.glDepthFunc((int)i);
        }
    }

    public static void _depthMask(boolean bl) {
        RenderSystem.assertOnRenderThread();
        if (bl != GlStateManager.DEPTH.mask) {
            GlStateManager.DEPTH.mask = bl;
            GL11.glDepthMask((boolean)bl);
        }
    }

    public static void _disableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.mode.disable();
    }

    public static void _enableBlend() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.BLEND.mode.enable();
    }

    public static void _blendFuncSeparate(int i, int j, int k, int l) {
        RenderSystem.assertOnRenderThread();
        if (i != GlStateManager.BLEND.srcRgb || j != GlStateManager.BLEND.dstRgb || k != GlStateManager.BLEND.srcAlpha || l != GlStateManager.BLEND.dstAlpha) {
            GlStateManager.BLEND.srcRgb = i;
            GlStateManager.BLEND.dstRgb = j;
            GlStateManager.BLEND.srcAlpha = k;
            GlStateManager.BLEND.dstAlpha = l;
            GlStateManager.glBlendFuncSeparate(i, j, k, l);
        }
    }

    public static int glGetProgrami(int i, int j) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgrami((int)i, (int)j);
    }

    public static void glAttachShader(int i, int j) {
        RenderSystem.assertOnRenderThread();
        GL20.glAttachShader((int)i, (int)j);
    }

    public static void glDeleteShader(int i) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteShader((int)i);
    }

    public static int glCreateShader(int i) {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateShader((int)i);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void glShaderSource(int i, String string) {
        RenderSystem.assertOnRenderThread();
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)(bs.length + 1));
        byteBuffer.put(bs);
        byteBuffer.put((byte)0);
        byteBuffer.flip();
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            pointerBuffer.put(byteBuffer);
            GL20C.nglShaderSource((int)i, (int)1, (long)pointerBuffer.address0(), (long)0L);
        }
        finally {
            MemoryUtil.memFree((Buffer)byteBuffer);
        }
    }

    public static void glCompileShader(int i) {
        RenderSystem.assertOnRenderThread();
        GL20.glCompileShader((int)i);
    }

    public static int glGetShaderi(int i, int j) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderi((int)i, (int)j);
    }

    public static void _glUseProgram(int i) {
        RenderSystem.assertOnRenderThread();
        GL20.glUseProgram((int)i);
    }

    public static int glCreateProgram() {
        RenderSystem.assertOnRenderThread();
        return GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int i) {
        RenderSystem.assertOnRenderThread();
        GL20.glDeleteProgram((int)i);
    }

    public static void glLinkProgram(int i) {
        RenderSystem.assertOnRenderThread();
        GL20.glLinkProgram((int)i);
    }

    public static int _glGetUniformLocation(int i, CharSequence charSequence) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetUniformLocation((int)i, (CharSequence)charSequence);
    }

    public static void _glUniform1i(int i, int j) {
        RenderSystem.assertOnRenderThread();
        GL20.glUniform1i((int)i, (int)j);
    }

    public static void _glBindAttribLocation(int i, int j, CharSequence charSequence) {
        RenderSystem.assertOnRenderThread();
        GL20.glBindAttribLocation((int)i, (int)j, (CharSequence)charSequence);
    }

    public static void incrementTrackedBuffers() {
        PLOT_BUFFERS.setValue((double)(++numBuffers));
    }

    public static int _glGenBuffers() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.incrementTrackedBuffers();
        return GL15.glGenBuffers();
    }

    public static int _glGenVertexArrays() {
        RenderSystem.assertOnRenderThread();
        return GL30.glGenVertexArrays();
    }

    public static void _glBindBuffer(int i, int j) {
        RenderSystem.assertOnRenderThread();
        GL15.glBindBuffer((int)i, (int)j);
    }

    public static void _glBindVertexArray(int i) {
        RenderSystem.assertOnRenderThread();
        GL30.glBindVertexArray((int)i);
    }

    public static void _glBufferData(int i, ByteBuffer byteBuffer, int j) {
        RenderSystem.assertOnRenderThread();
        GL15.glBufferData((int)i, (ByteBuffer)byteBuffer, (int)j);
    }

    public static void _glBufferSubData(int i, long l, ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThread();
        GL15.glBufferSubData((int)i, (long)l, (ByteBuffer)byteBuffer);
    }

    public static void _glBufferData(int i, long l, int j) {
        RenderSystem.assertOnRenderThread();
        GL15.glBufferData((int)i, (long)l, (int)j);
    }

    public static @Nullable ByteBuffer _glMapBufferRange(int i, long l, long m, int j) {
        RenderSystem.assertOnRenderThread();
        return GL30.glMapBufferRange((int)i, (long)l, (long)m, (int)j);
    }

    public static void _glUnmapBuffer(int i) {
        RenderSystem.assertOnRenderThread();
        GL15.glUnmapBuffer((int)i);
    }

    public static void _glDeleteBuffers(int i) {
        RenderSystem.assertOnRenderThread();
        PLOT_BUFFERS.setValue((double)(--numBuffers));
        GL15.glDeleteBuffers((int)i);
    }

    public static void _glBindFramebuffer(int i, int j) {
        if ((i == 36008 || i == 36160) && readFbo != j) {
            GL30.glBindFramebuffer((int)36008, (int)j);
            readFbo = j;
        }
        if ((i == 36009 || i == 36160) && writeFbo != j) {
            GL30.glBindFramebuffer((int)36009, (int)j);
            writeFbo = j;
        }
    }

    public static int getFrameBuffer(int i) {
        if (i == 36008) {
            return readFbo;
        }
        if (i == 36009) {
            return writeFbo;
        }
        return 0;
    }

    public static void _glBlitFrameBuffer(int i, int j, int k, int l, int m, int n, int o, int p, int q, int r) {
        RenderSystem.assertOnRenderThread();
        GL30.glBlitFramebuffer((int)i, (int)j, (int)k, (int)l, (int)m, (int)n, (int)o, (int)p, (int)q, (int)r);
    }

    public static void _glDeleteFramebuffers(int i) {
        RenderSystem.assertOnRenderThread();
        GL30.glDeleteFramebuffers((int)i);
        if (readFbo == i) {
            readFbo = 0;
        }
        if (writeFbo == i) {
            writeFbo = 0;
        }
    }

    public static int glGenFramebuffers() {
        RenderSystem.assertOnRenderThread();
        return GL30.glGenFramebuffers();
    }

    public static void _glFramebufferTexture2D(int i, int j, int k, int l, int m) {
        RenderSystem.assertOnRenderThread();
        GL30.glFramebufferTexture2D((int)i, (int)j, (int)k, (int)l, (int)m);
    }

    public static void glBlendFuncSeparate(int i, int j, int k, int l) {
        RenderSystem.assertOnRenderThread();
        GL14.glBlendFuncSeparate((int)i, (int)j, (int)k, (int)l);
    }

    public static String glGetShaderInfoLog(int i, int j) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetShaderInfoLog((int)i, (int)j);
    }

    public static String glGetProgramInfoLog(int i, int j) {
        RenderSystem.assertOnRenderThread();
        return GL20.glGetProgramInfoLog((int)i, (int)j);
    }

    public static void _enableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.enable.enable();
    }

    public static void _disableCull() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.CULL.enable.disable();
    }

    public static void _polygonMode(int i, int j) {
        RenderSystem.assertOnRenderThread();
        GL11.glPolygonMode((int)i, (int)j);
    }

    public static void _enablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.fill.enable();
    }

    public static void _disablePolygonOffset() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.POLY_OFFSET.fill.disable();
    }

    public static void _polygonOffset(float f, float g) {
        RenderSystem.assertOnRenderThread();
        if (f != GlStateManager.POLY_OFFSET.factor || g != GlStateManager.POLY_OFFSET.units) {
            GlStateManager.POLY_OFFSET.factor = f;
            GlStateManager.POLY_OFFSET.units = g;
            GL11.glPolygonOffset((float)f, (float)g);
        }
    }

    public static void _enableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.enable.enable();
    }

    public static void _disableColorLogicOp() {
        RenderSystem.assertOnRenderThread();
        GlStateManager.COLOR_LOGIC.enable.disable();
    }

    public static void _logicOp(int i) {
        RenderSystem.assertOnRenderThread();
        if (i != GlStateManager.COLOR_LOGIC.op) {
            GlStateManager.COLOR_LOGIC.op = i;
            GL11.glLogicOp((int)i);
        }
    }

    public static void _activeTexture(int i) {
        RenderSystem.assertOnRenderThread();
        if (activeTexture != i - 33984) {
            activeTexture = i - 33984;
            GL13.glActiveTexture((int)i);
        }
    }

    public static void _texParameter(int i, int j, int k) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexParameteri((int)i, (int)j, (int)k);
    }

    public static int _getTexLevelParameter(int i, int j, int k) {
        return GL11.glGetTexLevelParameteri((int)i, (int)j, (int)k);
    }

    public static int _genTexture() {
        RenderSystem.assertOnRenderThread();
        PLOT_TEXTURES.setValue((double)(++numTextures));
        return GL11.glGenTextures();
    }

    public static void _deleteTexture(int i) {
        RenderSystem.assertOnRenderThread();
        GL11.glDeleteTextures((int)i);
        for (TextureState textureState : TEXTURES) {
            if (textureState.binding != i) continue;
            textureState.binding = -1;
        }
        PLOT_TEXTURES.setValue((double)(--numTextures));
    }

    public static void _bindTexture(int i) {
        RenderSystem.assertOnRenderThread();
        if (i != GlStateManager.TEXTURES[GlStateManager.activeTexture].binding) {
            GlStateManager.TEXTURES[GlStateManager.activeTexture].binding = i;
            GL11.glBindTexture((int)3553, (int)i);
        }
    }

    public static void _texImage2D(int i, int j, int k, int l, int m, int n, int o, int p, @Nullable ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexImage2D((int)i, (int)j, (int)k, (int)l, (int)m, (int)n, (int)o, (int)p, (ByteBuffer)byteBuffer);
    }

    public static void _texSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p, long q) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexSubImage2D((int)i, (int)j, (int)k, (int)l, (int)m, (int)n, (int)o, (int)p, (long)q);
    }

    public static void _texSubImage2D(int i, int j, int k, int l, int m, int n, int o, int p, ByteBuffer byteBuffer) {
        RenderSystem.assertOnRenderThread();
        GL11.glTexSubImage2D((int)i, (int)j, (int)k, (int)l, (int)m, (int)n, (int)o, (int)p, (ByteBuffer)byteBuffer);
    }

    public static void _viewport(int i, int j, int k, int l) {
        GL11.glViewport((int)i, (int)j, (int)k, (int)l);
    }

    public static void _colorMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        RenderSystem.assertOnRenderThread();
        if (bl != GlStateManager.COLOR_MASK.red || bl2 != GlStateManager.COLOR_MASK.green || bl3 != GlStateManager.COLOR_MASK.blue || bl4 != GlStateManager.COLOR_MASK.alpha) {
            GlStateManager.COLOR_MASK.red = bl;
            GlStateManager.COLOR_MASK.green = bl2;
            GlStateManager.COLOR_MASK.blue = bl3;
            GlStateManager.COLOR_MASK.alpha = bl4;
            GL11.glColorMask((boolean)bl, (boolean)bl2, (boolean)bl3, (boolean)bl4);
        }
    }

    public static void _clear(int i) {
        RenderSystem.assertOnRenderThread();
        GL11.glClear((int)i);
        if (MacosUtil.IS_MACOS) {
            GlStateManager._getError();
        }
    }

    public static void _vertexAttribPointer(int i, int j, int k, boolean bl, int l, long m) {
        RenderSystem.assertOnRenderThread();
        GL20.glVertexAttribPointer((int)i, (int)j, (int)k, (boolean)bl, (int)l, (long)m);
    }

    public static void _vertexAttribIPointer(int i, int j, int k, int l, long m) {
        RenderSystem.assertOnRenderThread();
        GL30.glVertexAttribIPointer((int)i, (int)j, (int)k, (int)l, (long)m);
    }

    public static void _enableVertexAttribArray(int i) {
        RenderSystem.assertOnRenderThread();
        GL20.glEnableVertexAttribArray((int)i);
    }

    public static void _drawElements(int i, int j, int k, long l) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawElements((int)i, (int)j, (int)k, (long)l);
    }

    public static void _drawArrays(int i, int j, int k) {
        RenderSystem.assertOnRenderThread();
        GL11.glDrawArrays((int)i, (int)j, (int)k);
    }

    public static void _pixelStore(int i, int j) {
        RenderSystem.assertOnRenderThread();
        GL11.glPixelStorei((int)i, (int)j);
    }

    public static void _readPixels(int i, int j, int k, int l, int m, int n, long o) {
        RenderSystem.assertOnRenderThread();
        GL11.glReadPixels((int)i, (int)j, (int)k, (int)l, (int)m, (int)n, (long)o);
    }

    public static int _getError() {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetError();
    }

    public static void clearGlErrors() {
        RenderSystem.assertOnRenderThread();
        while (GL11.glGetError() != 0) {
        }
    }

    public static String _getString(int i) {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetString((int)i);
    }

    public static int _getInteger(int i) {
        RenderSystem.assertOnRenderThread();
        return GL11.glGetInteger((int)i);
    }

    public static long _glFenceSync(int i, int j) {
        RenderSystem.assertOnRenderThread();
        return GL32.glFenceSync((int)i, (int)j);
    }

    public static int _glClientWaitSync(long l, int i, long m) {
        RenderSystem.assertOnRenderThread();
        return GL32.glClientWaitSync((long)l, (int)i, (long)m);
    }

    public static void _glDeleteSync(long l) {
        RenderSystem.assertOnRenderThread();
        GL32.glDeleteSync((long)l);
    }

    static {
        TEXTURES = (TextureState[])IntStream.range(0, 12).mapToObj(i -> new TextureState()).toArray(TextureState[]::new);
        COLOR_MASK = new ColorMask();
    }

    @Environment(value=EnvType.CLIENT)
    static class ScissorState {
        public final BooleanState mode = new BooleanState(3089);

        ScissorState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BooleanState {
        private final int state;
        private boolean enabled;

        public BooleanState(int i) {
            this.state = i;
        }

        public void disable() {
            this.setEnabled(false);
        }

        public void enable() {
            this.setEnabled(true);
        }

        public void setEnabled(boolean bl) {
            RenderSystem.assertOnRenderThread();
            if (bl != this.enabled) {
                this.enabled = bl;
                if (bl) {
                    GL11.glEnable((int)this.state);
                } else {
                    GL11.glDisable((int)this.state);
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class DepthState {
        public final BooleanState mode = new BooleanState(2929);
        public boolean mask = true;
        public int func = 513;

        DepthState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class BlendState {
        public final BooleanState mode = new BooleanState(3042);
        public int srcRgb = 1;
        public int dstRgb = 0;
        public int srcAlpha = 1;
        public int dstAlpha = 0;

        BlendState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CullState {
        public final BooleanState enable = new BooleanState(2884);

        CullState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class PolygonOffsetState {
        public final BooleanState fill = new BooleanState(32823);
        public float factor;
        public float units;

        PolygonOffsetState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ColorLogicState {
        public final BooleanState enable = new BooleanState(3058);
        public int op = 5379;

        ColorLogicState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class TextureState {
        public int binding;

        TextureState() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ColorMask {
        public boolean red = true;
        public boolean green = true;
        public boolean blue = true;
        public boolean alpha = true;

        ColorMask() {
        }
    }
}

