/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlTextureView
extends GpuTextureView {
    private static final int EMPTY = -1;
    private boolean closed;
    private int firstFboId = -1;
    private int firstFboDepthId = -1;
    private @Nullable Int2IntMap fboCache;

    protected GlTextureView(GlTexture glTexture, int i, int j) {
        super(glTexture, i, j);
        glTexture.addViews();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.texture().removeViews();
            if (this.firstFboId != -1) {
                GlStateManager._glDeleteFramebuffers(this.firstFboId);
            }
            if (this.fboCache != null) {
                IntIterator intIterator = this.fboCache.values().iterator();
                while (intIterator.hasNext()) {
                    int i = (Integer)intIterator.next();
                    GlStateManager._glDeleteFramebuffers(i);
                }
            }
        }
    }

    public int getFbo(DirectStateAccess directStateAccess, @Nullable GpuTexture gpuTexture) {
        int i2;
        int n = i2 = gpuTexture == null ? 0 : ((GlTexture)gpuTexture).id;
        if (this.firstFboDepthId == i2) {
            return this.firstFboId;
        }
        if (this.firstFboId == -1) {
            this.firstFboId = this.createFbo(directStateAccess, i2);
            this.firstFboDepthId = i2;
            return this.firstFboId;
        }
        if (this.fboCache == null) {
            this.fboCache = new Int2IntArrayMap();
        }
        return this.fboCache.computeIfAbsent(i2, i -> this.createFbo(directStateAccess, i));
    }

    private int createFbo(DirectStateAccess directStateAccess, int i) {
        int j = directStateAccess.createFrameBufferObject();
        directStateAccess.bindFrameBufferTextures(j, this.texture().id, i, this.baseMipLevel(), 0);
        return j;
    }

    @Override
    public GlTexture texture() {
        return (GlTexture)super.texture();
    }

    @Override
    public /* synthetic */ GpuTexture texture() {
        return this.texture();
    }
}

