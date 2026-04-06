/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.blaze3d.resource;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.resource.ResourceDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record RenderTargetDescriptor(int width, int height, boolean useDepth, int clearColor) implements ResourceDescriptor<RenderTarget>
{
    @Override
    public RenderTarget allocate() {
        return new TextureTarget(null, this.width, this.height, this.useDepth);
    }

    @Override
    public void prepare(RenderTarget renderTarget) {
        if (this.useDepth) {
            RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(renderTarget.getColorTexture(), this.clearColor, renderTarget.getDepthTexture(), 1.0);
        } else {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(renderTarget.getColorTexture(), this.clearColor);
        }
    }

    @Override
    public void free(RenderTarget renderTarget) {
        renderTarget.destroyBuffers();
    }

    @Override
    public boolean canUsePhysicalResource(ResourceDescriptor<?> resourceDescriptor) {
        if (resourceDescriptor instanceof RenderTargetDescriptor) {
            RenderTargetDescriptor renderTargetDescriptor = (RenderTargetDescriptor)resourceDescriptor;
            return this.width == renderTargetDescriptor.width && this.height == renderTargetDescriptor.height && this.useDepth == renderTargetDescriptor.useDepth;
        }
        return false;
    }

    @Override
    public /* synthetic */ void free(Object object) {
        this.free((RenderTarget)object);
    }

    @Override
    public /* synthetic */ void prepare(Object object) {
        this.prepare((RenderTarget)object);
    }

    @Override
    public /* synthetic */ Object allocate() {
        return this.allocate();
    }
}

