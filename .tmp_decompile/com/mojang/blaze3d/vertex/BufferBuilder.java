/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteOrder;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class BufferBuilder
implements VertexConsumer {
    private static final int MAX_VERTEX_COUNT = 0xFFFFFF;
    private static final long NOT_BUILDING = -1L;
    private static final long UNKNOWN_ELEMENT = -1L;
    private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private final ByteBufferBuilder buffer;
    private long vertexPointer = -1L;
    private int vertices;
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final boolean fastFormat;
    private final boolean fullFormat;
    private final int vertexSize;
    private final int initialElementsToFill;
    private final int[] offsetsByElement;
    private int elementsToFill;
    private boolean building = true;

    public BufferBuilder(ByteBufferBuilder byteBufferBuilder, VertexFormat.Mode mode, VertexFormat vertexFormat) {
        if (!vertexFormat.contains(VertexFormatElement.POSITION)) {
            throw new IllegalArgumentException("Cannot build mesh with no position element");
        }
        this.buffer = byteBufferBuilder;
        this.mode = mode;
        this.format = vertexFormat;
        this.vertexSize = vertexFormat.getVertexSize();
        this.initialElementsToFill = vertexFormat.getElementsMask() & ~VertexFormatElement.POSITION.mask();
        this.offsetsByElement = vertexFormat.getOffsetsByElement();
        boolean bl = vertexFormat == DefaultVertexFormat.NEW_ENTITY;
        boolean bl2 = vertexFormat == DefaultVertexFormat.BLOCK;
        this.fastFormat = bl || bl2;
        this.fullFormat = bl;
    }

    public @Nullable MeshData build() {
        this.ensureBuilding();
        this.endLastVertex();
        MeshData meshData = this.storeMesh();
        this.building = false;
        this.vertexPointer = -1L;
        return meshData;
    }

    public MeshData buildOrThrow() {
        MeshData meshData = this.build();
        if (meshData == null) {
            throw new IllegalStateException("BufferBuilder was empty");
        }
        return meshData;
    }

    private void ensureBuilding() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    private @Nullable MeshData storeMesh() {
        if (this.vertices == 0) {
            return null;
        }
        ByteBufferBuilder.Result result = this.buffer.build();
        if (result == null) {
            return null;
        }
        int i = this.mode.indexCount(this.vertices);
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(this.vertices);
        return new MeshData(result, new MeshData.DrawState(this.format, this.vertices, i, this.mode, indexType));
    }

    private long beginVertex() {
        long l;
        this.ensureBuilding();
        this.endLastVertex();
        if (this.vertices >= 0xFFFFFF) {
            throw new IllegalStateException("Trying to write too many vertices (>16777215) into BufferBuilder");
        }
        ++this.vertices;
        this.vertexPointer = l = this.buffer.reserve(this.vertexSize);
        return l;
    }

    private long beginElement(VertexFormatElement vertexFormatElement) {
        int i = this.elementsToFill;
        int j = i & ~vertexFormatElement.mask();
        if (j == i) {
            return -1L;
        }
        this.elementsToFill = j;
        long l = this.vertexPointer;
        if (l == -1L) {
            throw new IllegalArgumentException("Not currently building vertex");
        }
        return l + (long)this.offsetsByElement[vertexFormatElement.id()];
    }

    private void endLastVertex() {
        if (this.vertices == 0) {
            return;
        }
        if (this.elementsToFill != 0) {
            String string = VertexFormatElement.elementsFromMask(this.elementsToFill).map(this.format::getElementName).collect(Collectors.joining(", "));
            throw new IllegalStateException("Missing elements in vertex: " + string);
        }
        if (this.mode == VertexFormat.Mode.LINES) {
            long l = this.buffer.reserve(this.vertexSize);
            MemoryUtil.memCopy((long)(l - (long)this.vertexSize), (long)l, (long)this.vertexSize);
            ++this.vertices;
        }
    }

    private static void putRgba(long l, int i) {
        int j = ARGB.toABGR(i);
        MemoryUtil.memPutInt((long)l, (int)(IS_LITTLE_ENDIAN ? j : Integer.reverseBytes(j)));
    }

    private static void putPackedUv(long l, int i) {
        if (IS_LITTLE_ENDIAN) {
            MemoryUtil.memPutInt((long)l, (int)i);
        } else {
            MemoryUtil.memPutShort((long)l, (short)((short)(i & 0xFFFF)));
            MemoryUtil.memPutShort((long)(l + 2L), (short)((short)(i >> 16 & 0xFFFF)));
        }
    }

    @Override
    public VertexConsumer addVertex(float f, float g, float h) {
        long l = this.beginVertex() + (long)this.offsetsByElement[VertexFormatElement.POSITION.id()];
        this.elementsToFill = this.initialElementsToFill;
        MemoryUtil.memPutFloat((long)l, (float)f);
        MemoryUtil.memPutFloat((long)(l + 4L), (float)g);
        MemoryUtil.memPutFloat((long)(l + 8L), (float)h);
        return this;
    }

    @Override
    public VertexConsumer setColor(int i, int j, int k, int l) {
        long m = this.beginElement(VertexFormatElement.COLOR);
        if (m != -1L) {
            MemoryUtil.memPutByte((long)m, (byte)((byte)i));
            MemoryUtil.memPutByte((long)(m + 1L), (byte)((byte)j));
            MemoryUtil.memPutByte((long)(m + 2L), (byte)((byte)k));
            MemoryUtil.memPutByte((long)(m + 3L), (byte)((byte)l));
        }
        return this;
    }

    @Override
    public VertexConsumer setColor(int i) {
        long l = this.beginElement(VertexFormatElement.COLOR);
        if (l != -1L) {
            BufferBuilder.putRgba(l, i);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv(float f, float g) {
        long l = this.beginElement(VertexFormatElement.UV0);
        if (l != -1L) {
            MemoryUtil.memPutFloat((long)l, (float)f);
            MemoryUtil.memPutFloat((long)(l + 4L), (float)g);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv1(int i, int j) {
        return this.uvShort((short)i, (short)j, VertexFormatElement.UV1);
    }

    @Override
    public VertexConsumer setOverlay(int i) {
        long l = this.beginElement(VertexFormatElement.UV1);
        if (l != -1L) {
            BufferBuilder.putPackedUv(l, i);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv2(int i, int j) {
        return this.uvShort((short)i, (short)j, VertexFormatElement.UV2);
    }

    @Override
    public VertexConsumer setLight(int i) {
        long l = this.beginElement(VertexFormatElement.UV2);
        if (l != -1L) {
            BufferBuilder.putPackedUv(l, i);
        }
        return this;
    }

    private VertexConsumer uvShort(short s, short t, VertexFormatElement vertexFormatElement) {
        long l = this.beginElement(vertexFormatElement);
        if (l != -1L) {
            MemoryUtil.memPutShort((long)l, (short)s);
            MemoryUtil.memPutShort((long)(l + 2L), (short)t);
        }
        return this;
    }

    @Override
    public VertexConsumer setNormal(float f, float g, float h) {
        long l = this.beginElement(VertexFormatElement.NORMAL);
        if (l != -1L) {
            MemoryUtil.memPutByte((long)l, (byte)BufferBuilder.normalIntValue(f));
            MemoryUtil.memPutByte((long)(l + 1L), (byte)BufferBuilder.normalIntValue(g));
            MemoryUtil.memPutByte((long)(l + 2L), (byte)BufferBuilder.normalIntValue(h));
        }
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float f) {
        long l = this.beginElement(VertexFormatElement.LINE_WIDTH);
        if (l != -1L) {
            MemoryUtil.memPutFloat((long)l, (float)f);
        }
        return this;
    }

    private static byte normalIntValue(float f) {
        return (byte)((int)(Mth.clamp(f, -1.0f, 1.0f) * 127.0f) & 0xFF);
    }

    @Override
    public void addVertex(float f, float g, float h, int i, float j, float k, int l, int m, float n, float o, float p) {
        if (this.fastFormat) {
            long r;
            long q = this.beginVertex();
            MemoryUtil.memPutFloat((long)(q + 0L), (float)f);
            MemoryUtil.memPutFloat((long)(q + 4L), (float)g);
            MemoryUtil.memPutFloat((long)(q + 8L), (float)h);
            BufferBuilder.putRgba(q + 12L, i);
            MemoryUtil.memPutFloat((long)(q + 16L), (float)j);
            MemoryUtil.memPutFloat((long)(q + 20L), (float)k);
            if (this.fullFormat) {
                BufferBuilder.putPackedUv(q + 24L, l);
                r = q + 28L;
            } else {
                r = q + 24L;
            }
            BufferBuilder.putPackedUv(r + 0L, m);
            MemoryUtil.memPutByte((long)(r + 4L), (byte)BufferBuilder.normalIntValue(n));
            MemoryUtil.memPutByte((long)(r + 5L), (byte)BufferBuilder.normalIntValue(o));
            MemoryUtil.memPutByte((long)(r + 6L), (byte)BufferBuilder.normalIntValue(p));
            return;
        }
        VertexConsumer.super.addVertex(f, g, h, i, j, k, l, m, n, o, p);
    }
}

