/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiProfilerChartRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ResultField;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

@Environment(value=EnvType.CLIENT)
public class GuiProfilerChartRenderer
extends PictureInPictureRenderer<GuiProfilerChartRenderState> {
    public GuiProfilerChartRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiProfilerChartRenderState> getRenderStateClass() {
        return GuiProfilerChartRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiProfilerChartRenderState guiProfilerChartRenderState, PoseStack poseStack) {
        double d = 0.0;
        poseStack.translate(0.0f, -5.0f, 0.0f);
        Matrix4f matrix4f = poseStack.last().pose();
        for (ResultField resultField : guiProfilerChartRenderState.chartData()) {
            float h;
            float g;
            float f;
            int l;
            int i = Mth.floor(resultField.percentage / 4.0) + 1;
            VertexConsumer vertexConsumer = this.bufferSource.getBuffer(RenderTypes.debugTriangleFan());
            int j = ARGB.opaque(resultField.getColor());
            int k = ARGB.multiply(j, -8355712);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, 0.0f, 0.0f, 0.0f).setColor(j);
            for (l = i; l >= 0; --l) {
                f = (float)((d + resultField.percentage * (double)l / (double)i) * 6.2831854820251465 / 100.0);
                g = Mth.sin(f) * 105.0f;
                h = Mth.cos(f) * 105.0f * 0.5f;
                vertexConsumer.addVertex((Matrix4fc)matrix4f, g, h, 0.0f).setColor(j);
            }
            vertexConsumer = this.bufferSource.getBuffer(RenderTypes.debugQuads());
            for (l = i; l > 0; --l) {
                f = (float)((d + resultField.percentage * (double)l / (double)i) * 6.2831854820251465 / 100.0);
                g = Mth.sin(f) * 105.0f;
                h = Mth.cos(f) * 105.0f * 0.5f;
                float m = (float)((d + resultField.percentage * (double)(l - 1) / (double)i) * 6.2831854820251465 / 100.0);
                float n = Mth.sin(m) * 105.0f;
                float o = Mth.cos(m) * 105.0f * 0.5f;
                if ((h + o) / 2.0f < 0.0f) continue;
                vertexConsumer.addVertex((Matrix4fc)matrix4f, g, h, 0.0f).setColor(k);
                vertexConsumer.addVertex((Matrix4fc)matrix4f, g, h + 10.0f, 0.0f).setColor(k);
                vertexConsumer.addVertex((Matrix4fc)matrix4f, n, o + 10.0f, 0.0f).setColor(k);
                vertexConsumer.addVertex((Matrix4fc)matrix4f, n, o, 0.0f).setColor(k);
            }
            d += resultField.percentage;
        }
    }

    @Override
    protected float getTranslateY(int i, int j) {
        return (float)i / 2.0f;
    }

    @Override
    protected String getTextureLabel() {
        return "profiler chart";
    }
}

