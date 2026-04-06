/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps
 *  java.util.SequencedMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.Object2ObjectSortedMaps;
import java.util.HashMap;
import java.util.Map;
import java.util.SequencedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface MultiBufferSource {
    public static BufferSource immediate(ByteBufferBuilder byteBufferBuilder) {
        return MultiBufferSource.immediateWithBuffers((SequencedMap<RenderType, ByteBufferBuilder>)Object2ObjectSortedMaps.emptyMap(), byteBufferBuilder);
    }

    public static BufferSource immediateWithBuffers(SequencedMap<RenderType, ByteBufferBuilder> sequencedMap, ByteBufferBuilder byteBufferBuilder) {
        return new BufferSource(byteBufferBuilder, sequencedMap);
    }

    public VertexConsumer getBuffer(RenderType var1);

    @Environment(value=EnvType.CLIENT)
    public static class BufferSource
    implements MultiBufferSource {
        protected final ByteBufferBuilder sharedBuffer;
        protected final SequencedMap<RenderType, ByteBufferBuilder> fixedBuffers;
        protected final Map<RenderType, BufferBuilder> startedBuilders = new HashMap<RenderType, BufferBuilder>();
        protected @Nullable RenderType lastSharedType;

        protected BufferSource(ByteBufferBuilder byteBufferBuilder, SequencedMap<RenderType, ByteBufferBuilder> sequencedMap) {
            this.sharedBuffer = byteBufferBuilder;
            this.fixedBuffers = sequencedMap;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            BufferBuilder bufferBuilder = this.startedBuilders.get(renderType);
            if (bufferBuilder != null && !renderType.canConsolidateConsecutiveGeometry()) {
                this.endBatch(renderType, bufferBuilder);
                bufferBuilder = null;
            }
            if (bufferBuilder != null) {
                return bufferBuilder;
            }
            ByteBufferBuilder byteBufferBuilder = (ByteBufferBuilder)this.fixedBuffers.get((Object)renderType);
            if (byteBufferBuilder != null) {
                bufferBuilder = new BufferBuilder(byteBufferBuilder, renderType.mode(), renderType.format());
            } else {
                if (this.lastSharedType != null) {
                    this.endBatch(this.lastSharedType);
                }
                bufferBuilder = new BufferBuilder(this.sharedBuffer, renderType.mode(), renderType.format());
                this.lastSharedType = renderType;
            }
            this.startedBuilders.put(renderType, bufferBuilder);
            return bufferBuilder;
        }

        public void endLastBatch() {
            if (this.lastSharedType != null) {
                this.endBatch(this.lastSharedType);
                this.lastSharedType = null;
            }
        }

        public void endBatch() {
            this.endLastBatch();
            for (RenderType renderType : this.fixedBuffers.keySet()) {
                this.endBatch(renderType);
            }
        }

        public void endBatch(RenderType renderType) {
            BufferBuilder bufferBuilder = this.startedBuilders.remove(renderType);
            if (bufferBuilder != null) {
                this.endBatch(renderType, bufferBuilder);
            }
        }

        private void endBatch(RenderType renderType, BufferBuilder bufferBuilder) {
            MeshData meshData = bufferBuilder.build();
            if (meshData != null) {
                if (renderType.sortOnUpload()) {
                    ByteBufferBuilder byteBufferBuilder = (ByteBufferBuilder)this.fixedBuffers.getOrDefault((Object)renderType, (Object)this.sharedBuffer);
                    meshData.sortQuads(byteBufferBuilder, RenderSystem.getProjectionType().vertexSorting());
                }
                renderType.draw(meshData);
            }
            if (renderType.equals(this.lastSharedType)) {
                this.lastSharedType = null;
            }
        }
    }
}

