/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface SubmitNodeCollector
extends OrderedSubmitNodeCollector {
    public OrderedSubmitNodeCollector order(int var1);

    @Environment(value=EnvType.CLIENT)
    public static interface ParticleGroupRenderer {
        public  @Nullable QuadParticleRenderState.PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache var1);

        public void render(QuadParticleRenderState.PreparedBuffers var1, ParticleFeatureRenderer.ParticleBufferCache var2, RenderPass var3, TextureManager var4, boolean var5);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface CustomGeometryRenderer {
        public void render(PoseStack.Pose var1, VertexConsumer var2);
    }
}

