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
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GlTexture
extends GpuTexture {
    private static final int EMPTY = -1;
    protected final int id;
    private int firstFboId = -1;
    private int firstFboDepthId = -1;
    private @Nullable Int2IntMap fboCache;
    protected boolean closed;
    private int views;

    protected GlTexture(@GpuTexture.Usage int i, String string, TextureFormat textureFormat, int j, int k, int l, int m, int n) {
        super(i, string, textureFormat, j, k, l, m);
        this.id = n;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.views == 0) {
            this.destroyImmediately();
        }
    }

    private void destroyImmediately() {
        GlStateManager._deleteTexture(this.id);
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

    @Override
    public boolean isClosed() {
        return this.closed;
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
        directStateAccess.bindFrameBufferTextures(j, this.id, i, 0, 0);
        return j;
    }

    public int glId() {
        return this.id;
    }

    public void addViews() {
        ++this.views;
    }

    public void removeViews() {
        --this.views;
        if (this.closed && this.views == 0) {
            this.destroyImmediately();
        }
    }
}

