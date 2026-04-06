/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CloudRenderer
extends SimplePreparableReloadListener<Optional<TextureData>>
implements AutoCloseable {
    private static final int FLAG_INSIDE_FACE = 16;
    private static final int FLAG_USE_TOP_COLOR = 32;
    private static final float CELL_SIZE_IN_BLOCKS = 12.0f;
    private static final int TICKS_PER_CELL = 400;
    private static final float BLOCKS_PER_SECOND = 0.6f;
    private static final int UBO_SIZE = new Std140SizeCalculator().putVec4().putVec3().putVec3().get();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/environment/clouds.png");
    private static final long EMPTY_CELL = 0L;
    private static final int COLOR_OFFSET = 4;
    private static final int NORTH_OFFSET = 3;
    private static final int EAST_OFFSET = 2;
    private static final int SOUTH_OFFSET = 1;
    private static final int WEST_OFFSET = 0;
    private boolean needsRebuild = true;
    private int prevCellX = Integer.MIN_VALUE;
    private int prevCellZ = Integer.MIN_VALUE;
    private RelativeCameraPos prevRelativeCameraPos = RelativeCameraPos.INSIDE_CLOUDS;
    private @Nullable CloudStatus prevType;
    private @Nullable TextureData texture;
    private int quadCount = 0;
    private final MappableRingBuffer ubo = new MappableRingBuffer(() -> "Cloud UBO", 130, UBO_SIZE);
    private @Nullable MappableRingBuffer utb;

    /*
     * Enabled aggressive exception aggregation
     */
    @Override
    protected Optional<TextureData> prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        try (InputStream inputStream = resourceManager.open(TEXTURE_LOCATION);){
            NativeImage nativeImage = NativeImage.read(inputStream);
            try {
                int i = nativeImage.getWidth();
                int j = nativeImage.getHeight();
                long[] ls = new long[i * j];
                for (int k = 0; k < j; ++k) {
                    for (int l = 0; l < i; ++l) {
                        int m = nativeImage.getPixel(l, k);
                        if (CloudRenderer.isCellEmpty(m)) {
                            ls[l + k * i] = 0L;
                            continue;
                        }
                        boolean bl = CloudRenderer.isCellEmpty(nativeImage.getPixel(l, Math.floorMod(k - 1, j)));
                        boolean bl2 = CloudRenderer.isCellEmpty(nativeImage.getPixel(Math.floorMod(l + 1, j), k));
                        boolean bl3 = CloudRenderer.isCellEmpty(nativeImage.getPixel(l, Math.floorMod(k + 1, j)));
                        boolean bl4 = CloudRenderer.isCellEmpty(nativeImage.getPixel(Math.floorMod(l - 1, j), k));
                        ls[l + k * i] = CloudRenderer.packCellData(m, bl, bl2, bl3, bl4);
                    }
                }
                Optional<TextureData> optional = Optional.of(new TextureData(ls, i, j));
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return optional;
            }
            catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to load cloud texture", (Throwable)iOException);
            return Optional.empty();
        }
    }

    private static int getSizeForCloudDistance(int i) {
        int j = 4;
        int k = (i + 1) * 2 * ((i + 1) * 2) / 2;
        int l = k * 4 + 54;
        return l * 3;
    }

    @Override
    protected void apply(Optional<TextureData> optional, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.texture = optional.orElse(null);
        this.needsRebuild = true;
    }

    private static boolean isCellEmpty(int i) {
        return ARGB.alpha(i) < 10;
    }

    private static long packCellData(int i, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        return (long)i << 4 | (long)((bl ? 1 : 0) << 3) | (long)((bl2 ? 1 : 0) << 2) | (long)((bl3 ? 1 : 0) << 1) | (long)((bl4 ? 1 : 0) << 0);
    }

    private static boolean isNorthEmpty(long l) {
        return (l >> 3 & 1L) != 0L;
    }

    private static boolean isEastEmpty(long l) {
        return (l >> 2 & 1L) != 0L;
    }

    private static boolean isSouthEmpty(long l) {
        return (l >> 1 & 1L) != 0L;
    }

    private static boolean isWestEmpty(long l) {
        return (l >> 0 & 1L) != 0L;
    }

    public void render(int i, CloudStatus cloudStatus, float f, Vec3 vec3, long l, float g) {
        GpuTextureView gpuTextureView2;
        GpuTextureView gpuTextureView;
        GpuBuffer.MappedView mappedView;
        RenderPipeline renderPipeline;
        float h;
        float n;
        if (this.texture == null) {
            return;
        }
        int j = Minecraft.getInstance().options.cloudRange().get() * 16;
        int k = Mth.ceil((float)j / 12.0f);
        int m = CloudRenderer.getSizeForCloudDistance(k);
        if (this.utb == null || this.utb.currentBuffer().size() != (long)m) {
            if (this.utb != null) {
                this.utb.close();
            }
            this.utb = new MappableRingBuffer(() -> "Cloud UTB", 258, m);
        }
        RelativeCameraPos relativeCameraPos = (n = (h = (float)((double)f - vec3.y)) + 4.0f) < 0.0f ? RelativeCameraPos.ABOVE_CLOUDS : (h > 0.0f ? RelativeCameraPos.BELOW_CLOUDS : RelativeCameraPos.INSIDE_CLOUDS);
        float o = (float)(l % ((long)this.texture.width * 400L)) + g;
        double d = vec3.x + (double)(o * 0.030000001f);
        double e = vec3.z + (double)3.96f;
        double p = (double)this.texture.width * 12.0;
        double q = (double)this.texture.height * 12.0;
        d -= (double)Mth.floor(d / p) * p;
        e -= (double)Mth.floor(e / q) * q;
        int r = Mth.floor(d / 12.0);
        int s = Mth.floor(e / 12.0);
        float t = (float)(d - (double)((float)r * 12.0f));
        float u = (float)(e - (double)((float)s * 12.0f));
        boolean bl = cloudStatus == CloudStatus.FANCY;
        RenderPipeline renderPipeline2 = renderPipeline = bl ? RenderPipelines.CLOUDS : RenderPipelines.FLAT_CLOUDS;
        if (this.needsRebuild || r != this.prevCellX || s != this.prevCellZ || relativeCameraPos != this.prevRelativeCameraPos || cloudStatus != this.prevType) {
            this.needsRebuild = false;
            this.prevCellX = r;
            this.prevCellZ = s;
            this.prevRelativeCameraPos = relativeCameraPos;
            this.prevType = cloudStatus;
            this.utb.rotate();
            mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.utb.currentBuffer(), false, true);
            try {
                this.buildMesh(relativeCameraPos, mappedView.data(), r, s, bl, k);
                this.quadCount = mappedView.data().position() / 3;
            }
            finally {
                if (mappedView != null) {
                    mappedView.close();
                }
            }
        }
        if (this.quadCount == 0) {
            return;
        }
        mappedView = RenderSystem.getDevice().createCommandEncoder().mapBuffer(this.ubo.currentBuffer(), false, true);
        try {
            Std140Builder.intoBuffer(mappedView.data()).putVec4((Vector4fc)ARGB.vector4fFromARGB32(i)).putVec3(-t, h, -u).putVec3(12.0f, 4.0f, 12.0f);
        }
        finally {
            if (mappedView != null) {
                mappedView.close();
            }
        }
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        RenderTarget renderTarget2 = Minecraft.getInstance().levelRenderer.getCloudsTarget();
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(6 * this.quadCount);
        if (renderTarget2 != null) {
            gpuTextureView = renderTarget2.getColorTextureView();
            gpuTextureView2 = renderTarget2.getDepthTextureView();
        } else {
            gpuTextureView = renderTarget.getColorTextureView();
            gpuTextureView2 = renderTarget.getDepthTextureView();
        }
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Clouds", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            renderPass.setUniform("CloudInfo", this.ubo.currentBuffer());
            renderPass.setUniform("CloudFaces", this.utb.currentBuffer());
            renderPass.drawIndexed(0, 0, 6 * this.quadCount, 1);
        }
    }

    private void buildMesh(RelativeCameraPos relativeCameraPos, ByteBuffer byteBuffer, int i, int j, boolean bl, int k) {
        if (this.texture == null) {
            return;
        }
        long[] ls = this.texture.cells;
        int l = this.texture.width;
        int m = this.texture.height;
        for (int n = 0; n <= 2 * k; ++n) {
            for (int o = -n; o <= n; ++o) {
                int p = n - Math.abs(o);
                if (p < 0 || p > k || o * o + p * p > k * k) continue;
                if (p != 0) {
                    this.tryBuildCell(relativeCameraPos, byteBuffer, i, j, bl, o, l, -p, m, ls);
                }
                this.tryBuildCell(relativeCameraPos, byteBuffer, i, j, bl, o, l, p, m, ls);
            }
        }
    }

    private void tryBuildCell(RelativeCameraPos relativeCameraPos, ByteBuffer byteBuffer, int i, int j, boolean bl, int k, int l, int m, int n, long[] ls) {
        int p;
        int o = Math.floorMod(i + k, l);
        long q = ls[o + (p = Math.floorMod(j + m, n)) * l];
        if (q == 0L) {
            return;
        }
        if (bl) {
            this.buildExtrudedCell(relativeCameraPos, byteBuffer, k, m, q);
        } else {
            this.buildFlatCell(byteBuffer, k, m);
        }
    }

    private void buildFlatCell(ByteBuffer byteBuffer, int i, int j) {
        this.encodeFace(byteBuffer, i, j, Direction.DOWN, 32);
    }

    private void encodeFace(ByteBuffer byteBuffer, int i, int j, Direction direction, int k) {
        int l = direction.get3DDataValue() | k;
        l |= (i & 1) << 7;
        byteBuffer.put((byte)(i >> 1)).put((byte)(j >> 1)).put((byte)(l |= (j & 1) << 6));
    }

    private void buildExtrudedCell(RelativeCameraPos relativeCameraPos, ByteBuffer byteBuffer, int i, int j, long l) {
        boolean bl;
        if (relativeCameraPos != RelativeCameraPos.BELOW_CLOUDS) {
            this.encodeFace(byteBuffer, i, j, Direction.UP, 0);
        }
        if (relativeCameraPos != RelativeCameraPos.ABOVE_CLOUDS) {
            this.encodeFace(byteBuffer, i, j, Direction.DOWN, 0);
        }
        if (CloudRenderer.isNorthEmpty(l) && j > 0) {
            this.encodeFace(byteBuffer, i, j, Direction.NORTH, 0);
        }
        if (CloudRenderer.isSouthEmpty(l) && j < 0) {
            this.encodeFace(byteBuffer, i, j, Direction.SOUTH, 0);
        }
        if (CloudRenderer.isWestEmpty(l) && i > 0) {
            this.encodeFace(byteBuffer, i, j, Direction.WEST, 0);
        }
        if (CloudRenderer.isEastEmpty(l) && i < 0) {
            this.encodeFace(byteBuffer, i, j, Direction.EAST, 0);
        }
        boolean bl2 = bl = Math.abs(i) <= 1 && Math.abs(j) <= 1;
        if (bl) {
            for (Direction direction : Direction.values()) {
                this.encodeFace(byteBuffer, i, j, direction, 16);
            }
        }
    }

    public void markForRebuild() {
        this.needsRebuild = true;
    }

    public void endFrame() {
        this.ubo.rotate();
    }

    @Override
    public void close() {
        this.ubo.close();
        if (this.utb != null) {
            this.utb.close();
        }
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    @Environment(value=EnvType.CLIENT)
    static enum RelativeCameraPos {
        ABOVE_CLOUDS,
        INSIDE_CLOUDS,
        BELOW_CLOUDS;

    }

    @Environment(value=EnvType.CLIENT)
    public static final class TextureData
    extends Record {
        final long[] cells;
        final int width;
        final int height;

        public TextureData(long[] ls, int i, int j) {
            this.cells = ls;
            this.width = i;
            this.height = j;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TextureData.class, "cells;width;height", "cells", "width", "height"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TextureData.class, "cells;width;height", "cells", "width", "height"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TextureData.class, "cells;width;height", "cells", "width", "height"}, this, object);
        }

        public long[] cells() {
            return this.cells;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }
    }
}

