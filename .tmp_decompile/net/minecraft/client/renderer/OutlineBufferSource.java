/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

@Environment(value=EnvType.CLIENT)
public class OutlineBufferSource
implements MultiBufferSource {
    private final MultiBufferSource.BufferSource outlineBufferSource = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
    private int outlineColor = -1;

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        if (renderType.isOutline()) {
            VertexConsumer vertexConsumer = this.outlineBufferSource.getBuffer(renderType);
            return new EntityOutlineGenerator(vertexConsumer, this.outlineColor);
        }
        Optional<RenderType> optional = renderType.outline();
        if (optional.isPresent()) {
            VertexConsumer vertexConsumer2 = this.outlineBufferSource.getBuffer(optional.get());
            return new EntityOutlineGenerator(vertexConsumer2, this.outlineColor);
        }
        throw new IllegalStateException("Can't render an outline for this rendertype!");
    }

    public void setColor(int i) {
        this.outlineColor = i;
    }

    public void endOutlineBatch() {
        this.outlineBufferSource.endBatch();
    }

    @Environment(value=EnvType.CLIENT)
    record EntityOutlineGenerator(VertexConsumer delegate, int color) implements VertexConsumer
    {
        @Override
        public VertexConsumer addVertex(float f, float g, float h) {
            this.delegate.addVertex(f, g, h).setColor(this.color);
            return this;
        }

        @Override
        public VertexConsumer setColor(int i, int j, int k, int l) {
            return this;
        }

        @Override
        public VertexConsumer setColor(int i) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float f, float g) {
            this.delegate.setUv(f, g);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int i, int j) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int i, int j) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float f, float g, float h) {
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float f) {
            return this;
        }
    }
}

