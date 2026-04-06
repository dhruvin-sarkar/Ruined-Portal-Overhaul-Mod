/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.opengl.EXTDebugLabel
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.opengl.KHRDebug
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.logging.LogUtils;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringUtil;
import org.lwjgl.opengl.EXTDebugLabel;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public abstract class GlDebugLabel {
    private static final Logger LOGGER = LogUtils.getLogger();

    public void applyLabel(GlBuffer glBuffer) {
    }

    public void applyLabel(GlTexture glTexture) {
    }

    public void applyLabel(GlShaderModule glShaderModule) {
    }

    public void applyLabel(GlProgram glProgram) {
    }

    public void applyLabel(VertexArrayCache.VertexArray vertexArray) {
    }

    public void pushDebugGroup(Supplier<String> supplier) {
    }

    public void popDebugGroup() {
    }

    public static GlDebugLabel create(GLCapabilities gLCapabilities, boolean bl, Set<String> set) {
        if (bl) {
            if (gLCapabilities.GL_KHR_debug && GlDevice.USE_GL_KHR_debug) {
                set.add("GL_KHR_debug");
                return new Core();
            }
            if (gLCapabilities.GL_EXT_debug_label && GlDevice.USE_GL_EXT_debug_label) {
                set.add("GL_EXT_debug_label");
                return new Ext();
            }
            LOGGER.warn("Debug labels unavailable: neither KHR_debug nor EXT_debug_label are supported");
        }
        return new Empty();
    }

    public boolean exists() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    static class Core
    extends GlDebugLabel {
        private final int maxLabelLength = GL11.glGetInteger((int)33512);

        Core() {
        }

        @Override
        public void applyLabel(GlBuffer glBuffer) {
            Supplier<String> supplier = glBuffer.label;
            if (supplier != null) {
                KHRDebug.glObjectLabel((int)33504, (int)glBuffer.handle, (CharSequence)StringUtil.truncateStringIfNecessary(supplier.get(), this.maxLabelLength, true));
            }
        }

        @Override
        public void applyLabel(GlTexture glTexture) {
            KHRDebug.glObjectLabel((int)5890, (int)glTexture.id, (CharSequence)StringUtil.truncateStringIfNecessary(glTexture.getLabel(), this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(GlShaderModule glShaderModule) {
            KHRDebug.glObjectLabel((int)33505, (int)glShaderModule.getShaderId(), (CharSequence)StringUtil.truncateStringIfNecessary(glShaderModule.getDebugLabel(), this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(GlProgram glProgram) {
            KHRDebug.glObjectLabel((int)33506, (int)glProgram.getProgramId(), (CharSequence)StringUtil.truncateStringIfNecessary(glProgram.getDebugLabel(), this.maxLabelLength, true));
        }

        @Override
        public void applyLabel(VertexArrayCache.VertexArray vertexArray) {
            KHRDebug.glObjectLabel((int)32884, (int)vertexArray.id, (CharSequence)StringUtil.truncateStringIfNecessary(vertexArray.format.toString(), this.maxLabelLength, true));
        }

        @Override
        public void pushDebugGroup(Supplier<String> supplier) {
            KHRDebug.glPushDebugGroup((int)33354, (int)0, (CharSequence)supplier.get());
        }

        @Override
        public void popDebugGroup() {
            KHRDebug.glPopDebugGroup();
        }

        @Override
        public boolean exists() {
            return true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Ext
    extends GlDebugLabel {
        Ext() {
        }

        @Override
        public void applyLabel(GlBuffer glBuffer) {
            Supplier<String> supplier = glBuffer.label;
            if (supplier != null) {
                EXTDebugLabel.glLabelObjectEXT((int)37201, (int)glBuffer.handle, (CharSequence)StringUtil.truncateStringIfNecessary(supplier.get(), 256, true));
            }
        }

        @Override
        public void applyLabel(GlTexture glTexture) {
            EXTDebugLabel.glLabelObjectEXT((int)5890, (int)glTexture.id, (CharSequence)StringUtil.truncateStringIfNecessary(glTexture.getLabel(), 256, true));
        }

        @Override
        public void applyLabel(GlShaderModule glShaderModule) {
            EXTDebugLabel.glLabelObjectEXT((int)35656, (int)glShaderModule.getShaderId(), (CharSequence)StringUtil.truncateStringIfNecessary(glShaderModule.getDebugLabel(), 256, true));
        }

        @Override
        public void applyLabel(GlProgram glProgram) {
            EXTDebugLabel.glLabelObjectEXT((int)35648, (int)glProgram.getProgramId(), (CharSequence)StringUtil.truncateStringIfNecessary(glProgram.getDebugLabel(), 256, true));
        }

        @Override
        public void applyLabel(VertexArrayCache.VertexArray vertexArray) {
            EXTDebugLabel.glLabelObjectEXT((int)32884, (int)vertexArray.id, (CharSequence)StringUtil.truncateStringIfNecessary(vertexArray.format.toString(), 256, true));
        }

        @Override
        public boolean exists() {
            return true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Empty
    extends GlDebugLabel {
        Empty() {
        }
    }
}

