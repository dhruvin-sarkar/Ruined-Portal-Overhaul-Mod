/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.ARBVertexAttribBinding
 *  org.lwjgl.opengl.GLCapabilities
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDebugLabel;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBVertexAttribBinding;
import org.lwjgl.opengl.GLCapabilities;

@Environment(value=EnvType.CLIENT)
public abstract class VertexArrayCache {
    public static VertexArrayCache create(GLCapabilities gLCapabilities, GlDebugLabel glDebugLabel, Set<String> set) {
        if (gLCapabilities.GL_ARB_vertex_attrib_binding && GlDevice.USE_GL_ARB_vertex_attrib_binding) {
            set.add("GL_ARB_vertex_attrib_binding");
            return new Separate(glDebugLabel);
        }
        return new Emulated(glDebugLabel);
    }

    public abstract void bindVertexArray(VertexFormat var1, @Nullable GlBuffer var2);

    @Environment(value=EnvType.CLIENT)
    static class Separate
    extends VertexArrayCache {
        private final Map<VertexFormat, VertexArray> cache = new HashMap<VertexFormat, VertexArray>();
        private final GlDebugLabel debugLabels;
        private final boolean needsMesaWorkaround;

        public Separate(GlDebugLabel glDebugLabel) {
            String string;
            this.debugLabels = glDebugLabel;
            this.needsMesaWorkaround = "Mesa".equals(GlStateManager._getString(7936)) ? (string = GlStateManager._getString(7938)).contains("25.0.0") || string.contains("25.0.1") || string.contains("25.0.2") : false;
        }

        @Override
        public void bindVertexArray(VertexFormat vertexFormat, @Nullable GlBuffer glBuffer) {
            VertexArray vertexArray = this.cache.get(vertexFormat);
            if (vertexArray == null) {
                int i = GlStateManager._glGenVertexArrays();
                GlStateManager._glBindVertexArray(i);
                if (glBuffer != null) {
                    List<VertexFormatElement> list = vertexFormat.getElements();
                    for (int j = 0; j < list.size(); ++j) {
                        VertexFormatElement vertexFormatElement = list.get(j);
                        GlStateManager._enableVertexAttribArray(j);
                        switch (vertexFormatElement.usage()) {
                            case POSITION: 
                            case GENERIC: 
                            case UV: {
                                if (vertexFormatElement.type() == VertexFormatElement.Type.FLOAT) {
                                    ARBVertexAttribBinding.glVertexAttribFormat((int)j, (int)vertexFormatElement.count(), (int)GlConst.toGl(vertexFormatElement.type()), (boolean)false, (int)vertexFormat.getOffset(vertexFormatElement));
                                    break;
                                }
                                ARBVertexAttribBinding.glVertexAttribIFormat((int)j, (int)vertexFormatElement.count(), (int)GlConst.toGl(vertexFormatElement.type()), (int)vertexFormat.getOffset(vertexFormatElement));
                                break;
                            }
                            case NORMAL: 
                            case COLOR: {
                                ARBVertexAttribBinding.glVertexAttribFormat((int)j, (int)vertexFormatElement.count(), (int)GlConst.toGl(vertexFormatElement.type()), (boolean)true, (int)vertexFormat.getOffset(vertexFormatElement));
                            }
                        }
                        ARBVertexAttribBinding.glVertexAttribBinding((int)j, (int)0);
                    }
                }
                if (glBuffer != null) {
                    ARBVertexAttribBinding.glBindVertexBuffer((int)0, (int)glBuffer.handle, (long)0L, (int)vertexFormat.getVertexSize());
                }
                VertexArray vertexArray2 = new VertexArray(i, vertexFormat, glBuffer);
                this.debugLabels.applyLabel(vertexArray2);
                this.cache.put(vertexFormat, vertexArray2);
                return;
            }
            GlStateManager._glBindVertexArray(vertexArray.id);
            if (glBuffer != null && vertexArray.lastVertexBuffer != glBuffer) {
                if (this.needsMesaWorkaround && vertexArray.lastVertexBuffer != null && vertexArray.lastVertexBuffer.handle == glBuffer.handle) {
                    ARBVertexAttribBinding.glBindVertexBuffer((int)0, (int)0, (long)0L, (int)0);
                }
                ARBVertexAttribBinding.glBindVertexBuffer((int)0, (int)glBuffer.handle, (long)0L, (int)vertexFormat.getVertexSize());
                vertexArray.lastVertexBuffer = glBuffer;
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Emulated
    extends VertexArrayCache {
        private final Map<VertexFormat, VertexArray> cache = new HashMap<VertexFormat, VertexArray>();
        private final GlDebugLabel debugLabels;

        public Emulated(GlDebugLabel glDebugLabel) {
            this.debugLabels = glDebugLabel;
        }

        @Override
        public void bindVertexArray(VertexFormat vertexFormat, @Nullable GlBuffer glBuffer) {
            VertexArray vertexArray = this.cache.get(vertexFormat);
            if (vertexArray == null) {
                int i = GlStateManager._glGenVertexArrays();
                GlStateManager._glBindVertexArray(i);
                if (glBuffer != null) {
                    GlStateManager._glBindBuffer(34962, glBuffer.handle);
                    Emulated.setupCombinedAttributes(vertexFormat, true);
                }
                VertexArray vertexArray2 = new VertexArray(i, vertexFormat, glBuffer);
                this.debugLabels.applyLabel(vertexArray2);
                this.cache.put(vertexFormat, vertexArray2);
                return;
            }
            GlStateManager._glBindVertexArray(vertexArray.id);
            if (glBuffer != null && vertexArray.lastVertexBuffer != glBuffer) {
                GlStateManager._glBindBuffer(34962, glBuffer.handle);
                vertexArray.lastVertexBuffer = glBuffer;
                Emulated.setupCombinedAttributes(vertexFormat, false);
            }
        }

        private static void setupCombinedAttributes(VertexFormat vertexFormat, boolean bl) {
            int i = vertexFormat.getVertexSize();
            List<VertexFormatElement> list = vertexFormat.getElements();
            block4: for (int j = 0; j < list.size(); ++j) {
                VertexFormatElement vertexFormatElement = list.get(j);
                if (bl) {
                    GlStateManager._enableVertexAttribArray(j);
                }
                switch (vertexFormatElement.usage()) {
                    case POSITION: 
                    case GENERIC: 
                    case UV: {
                        if (vertexFormatElement.type() == VertexFormatElement.Type.FLOAT) {
                            GlStateManager._vertexAttribPointer(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), false, i, vertexFormat.getOffset(vertexFormatElement));
                            continue block4;
                        }
                        GlStateManager._vertexAttribIPointer(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), i, vertexFormat.getOffset(vertexFormatElement));
                        continue block4;
                    }
                    case NORMAL: 
                    case COLOR: {
                        GlStateManager._vertexAttribPointer(j, vertexFormatElement.count(), GlConst.toGl(vertexFormatElement.type()), true, i, vertexFormat.getOffset(vertexFormatElement));
                    }
                }
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class VertexArray {
        final int id;
        final VertexFormat format;
        @Nullable GlBuffer lastVertexBuffer;

        VertexArray(int i, VertexFormat vertexFormat, @Nullable GlBuffer glBuffer) {
            this.id = i;
            this.format = vertexFormat;
            this.lastVertexBuffer = glBuffer;
        }
    }
}

