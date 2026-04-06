/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.GraphicsWorkarounds;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
@DontObfuscate
public class VertexFormat {
    public static final int UNKNOWN_ELEMENT = -1;
    private final List<VertexFormatElement> elements;
    private final List<String> names;
    private final int vertexSize;
    private final int elementsMask;
    private final int[] offsetsByElement = new int[32];
    private @Nullable GpuBuffer immediateDrawVertexBuffer;
    private @Nullable GpuBuffer immediateDrawIndexBuffer;

    VertexFormat(List<VertexFormatElement> list, List<String> list2, IntList intList, int i2) {
        this.elements = list;
        this.names = list2;
        this.vertexSize = i2;
        this.elementsMask = list.stream().mapToInt(VertexFormatElement::mask).reduce(0, (i, j) -> i | j);
        for (int j2 = 0; j2 < this.offsetsByElement.length; ++j2) {
            VertexFormatElement vertexFormatElement = VertexFormatElement.byId(j2);
            int k = vertexFormatElement != null ? list.indexOf((Object)vertexFormatElement) : -1;
            this.offsetsByElement[j2] = k != -1 ? intList.getInt(k) : -1;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toString() {
        return "VertexFormat" + String.valueOf(this.names);
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public List<String> getElementAttributeNames() {
        return this.names;
    }

    public int[] getOffsetsByElement() {
        return this.offsetsByElement;
    }

    public int getOffset(VertexFormatElement vertexFormatElement) {
        return this.offsetsByElement[vertexFormatElement.id()];
    }

    public boolean contains(VertexFormatElement vertexFormatElement) {
        return (this.elementsMask & vertexFormatElement.mask()) != 0;
    }

    public int getElementsMask() {
        return this.elementsMask;
    }

    public String getElementName(VertexFormatElement vertexFormatElement) {
        int i = this.elements.indexOf((Object)vertexFormatElement);
        if (i == -1) {
            throw new IllegalArgumentException(String.valueOf((Object)vertexFormatElement) + " is not contained in format");
        }
        return this.names.get(i);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof VertexFormat)) return false;
        VertexFormat vertexFormat = (VertexFormat)object;
        if (this.elementsMask != vertexFormat.elementsMask) return false;
        if (this.vertexSize != vertexFormat.vertexSize) return false;
        if (!this.names.equals(vertexFormat.names)) return false;
        if (!Arrays.equals(this.offsetsByElement, vertexFormat.offsetsByElement)) return false;
        return true;
    }

    public int hashCode() {
        return this.elementsMask * 31 + Arrays.hashCode(this.offsetsByElement);
    }

    private static GpuBuffer uploadToBuffer(@Nullable GpuBuffer gpuBuffer, ByteBuffer byteBuffer, @GpuBuffer.Usage int i, Supplier<String> supplier) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        if (GraphicsWorkarounds.get(gpuDevice).alwaysCreateFreshImmediateBuffer()) {
            if (gpuBuffer != null) {
                gpuBuffer.close();
            }
            return gpuDevice.createBuffer(supplier, i, byteBuffer);
        }
        if (gpuBuffer == null) {
            gpuBuffer = gpuDevice.createBuffer(supplier, i, byteBuffer);
        } else {
            CommandEncoder commandEncoder = gpuDevice.createCommandEncoder();
            if (gpuBuffer.size() < (long)byteBuffer.remaining()) {
                gpuBuffer.close();
                gpuBuffer = gpuDevice.createBuffer(supplier, i, byteBuffer);
            } else {
                commandEncoder.writeToBuffer(gpuBuffer.slice(), byteBuffer);
            }
        }
        return gpuBuffer;
    }

    public GpuBuffer uploadImmediateVertexBuffer(ByteBuffer byteBuffer) {
        this.immediateDrawVertexBuffer = VertexFormat.uploadToBuffer(this.immediateDrawVertexBuffer, byteBuffer, 40, () -> "Immediate vertex buffer for " + String.valueOf(this));
        return this.immediateDrawVertexBuffer;
    }

    public GpuBuffer uploadImmediateIndexBuffer(ByteBuffer byteBuffer) {
        this.immediateDrawIndexBuffer = VertexFormat.uploadToBuffer(this.immediateDrawIndexBuffer, byteBuffer, 72, () -> "Immediate index buffer for " + String.valueOf(this));
        return this.immediateDrawIndexBuffer;
    }

    @Environment(value=EnvType.CLIENT)
    @DontObfuscate
    public static class Builder {
        private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
        private final IntList offsets = new IntArrayList();
        private int offset;

        Builder() {
        }

        public Builder add(String string, VertexFormatElement vertexFormatElement) {
            this.elements.put((Object)string, (Object)vertexFormatElement);
            this.offsets.add(this.offset);
            this.offset += vertexFormatElement.byteSize();
            return this;
        }

        public Builder padding(int i) {
            this.offset += i;
            return this;
        }

        public VertexFormat build() {
            ImmutableMap immutableMap = this.elements.buildOrThrow();
            ImmutableList immutableList = immutableMap.values().asList();
            ImmutableList immutableList2 = immutableMap.keySet().asList();
            return new VertexFormat((List<VertexFormatElement>)immutableList, (List<String>)immutableList2, this.offsets, this.offset);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Mode {
        LINES(2, 2, false),
        DEBUG_LINES(2, 2, false),
        DEBUG_LINE_STRIP(2, 1, true),
        POINTS(1, 1, false),
        TRIANGLES(3, 3, false),
        TRIANGLE_STRIP(3, 1, true),
        TRIANGLE_FAN(3, 1, true),
        QUADS(4, 4, false);

        public final int primitiveLength;
        public final int primitiveStride;
        public final boolean connectedPrimitives;

        private Mode(int j, int k, boolean bl) {
            this.primitiveLength = j;
            this.primitiveStride = k;
            this.connectedPrimitives = bl;
        }

        public int indexCount(int i) {
            return switch (this.ordinal()) {
                case 1, 2, 3, 4, 5, 6 -> i;
                case 0, 7 -> i / 4 * 6;
                default -> 0;
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum IndexType {
        SHORT(2),
        INT(4);

        public final int bytes;

        private IndexType(int j) {
            this.bytes = j;
        }

        public static IndexType least(int i) {
            if ((i & 0xFFFF0000) != 0) {
                return INT;
            }
            return SHORT;
        }
    }
}

