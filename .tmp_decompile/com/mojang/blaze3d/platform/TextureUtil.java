/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.ARGB;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
@DontObfuscate
public class TextureUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int MIN_MIPMAP_LEVEL = 0;
    private static final int DEFAULT_IMAGE_BUFFER_SIZE = 8192;
    private static final int[][] DIRECTIONS = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

    public static ByteBuffer readResource(InputStream inputStream) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
        if (readableByteChannel instanceof SeekableByteChannel) {
            SeekableByteChannel seekableByteChannel = (SeekableByteChannel)readableByteChannel;
            return TextureUtil.readResource(readableByteChannel, (int)seekableByteChannel.size() + 1);
        }
        return TextureUtil.readResource(readableByteChannel, 8192);
    }

    private static ByteBuffer readResource(ReadableByteChannel readableByteChannel, int i) throws IOException {
        ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)i);
        try {
            while (readableByteChannel.read(byteBuffer) != -1) {
                if (byteBuffer.hasRemaining()) continue;
                byteBuffer = MemoryUtil.memRealloc((ByteBuffer)byteBuffer, (int)(byteBuffer.capacity() * 2));
            }
            byteBuffer.flip();
            return byteBuffer;
        }
        catch (IOException iOException) {
            MemoryUtil.memFree((Buffer)byteBuffer);
            throw iOException;
        }
    }

    public static void writeAsPNG(Path path, String string, GpuTexture gpuTexture, int i, IntUnaryOperator intUnaryOperator) {
        RenderSystem.assertOnRenderThread();
        long l = 0L;
        for (int j = 0; j <= i; ++j) {
            l += (long)gpuTexture.getFormat().pixelSize() * (long)gpuTexture.getWidth(j) * (long)gpuTexture.getHeight(j);
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Exporting textures larger than 2GB is not supported");
        }
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Texture output buffer", 9, l);
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        Runnable runnable = () -> {
            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false);){
                int j = 0;
                for (int k = 0; k <= i; ++k) {
                    int l = gpuTexture.getWidth(k);
                    int m = gpuTexture.getHeight(k);
                    try (NativeImage nativeImage = new NativeImage(l, m, false);){
                        for (int n = 0; n < m; ++n) {
                            for (int o = 0; o < l; ++o) {
                                int p = mappedView.data().getInt(j + (o + n * l) * gpuTexture.getFormat().pixelSize());
                                nativeImage.setPixelABGR(o, n, intUnaryOperator.applyAsInt(p));
                            }
                        }
                        Path path2 = path.resolve(string + "_" + k + ".png");
                        nativeImage.writeToFile(path2);
                        LOGGER.debug("Exported png to: {}", (Object)path2.toAbsolutePath());
                    }
                    catch (IOException iOException) {
                        LOGGER.debug("Unable to write: ", (Throwable)iOException);
                    }
                    j += gpuTexture.getFormat().pixelSize() * l * m;
                }
            }
            gpuBuffer.close();
        };
        AtomicInteger atomicInteger = new AtomicInteger();
        int k = 0;
        for (int m = 0; m <= i; ++m) {
            commandEncoder.copyTextureToBuffer(gpuTexture, gpuBuffer, k, () -> {
                if (atomicInteger.getAndIncrement() == i) {
                    runnable.run();
                }
            }, m);
            k += gpuTexture.getFormat().pixelSize() * gpuTexture.getWidth(m) * gpuTexture.getHeight(m);
        }
    }

    public static Path getDebugTexturePath(Path path) {
        return path.resolve("screenshots").resolve("debug");
    }

    public static Path getDebugTexturePath() {
        return TextureUtil.getDebugTexturePath(Path.of((String)".", (String[])new String[0]));
    }

    public static void solidify(NativeImage nativeImage) {
        int m;
        int l;
        int k;
        int i = nativeImage.getWidth();
        int j = nativeImage.getHeight();
        int[] is = new int[i * j];
        int[] js = new int[i * j];
        Arrays.fill(js, Integer.MAX_VALUE);
        IntArrayFIFOQueue intArrayFIFOQueue = new IntArrayFIFOQueue();
        for (k = 0; k < i; ++k) {
            for (l = 0; l < j; ++l) {
                m = nativeImage.getPixel(k, l);
                if (ARGB.alpha(m) == 0) continue;
                int n = TextureUtil.pack(k, l, i);
                js[n] = 0;
                is[n] = m;
                intArrayFIFOQueue.enqueue(n);
            }
        }
        while (!intArrayFIFOQueue.isEmpty()) {
            k = intArrayFIFOQueue.dequeueInt();
            l = TextureUtil.x(k, i);
            m = TextureUtil.y(k, i);
            for (int[] ks : DIRECTIONS) {
                int o = l + ks[0];
                int p = m + ks[1];
                int q = TextureUtil.pack(o, p, i);
                if (o < 0 || p < 0 || o >= i || p >= j || js[q] <= js[k] + 1) continue;
                js[q] = js[k] + 1;
                is[q] = is[k];
                intArrayFIFOQueue.enqueue(q);
            }
        }
        for (k = 0; k < i; ++k) {
            for (l = 0; l < j; ++l) {
                m = nativeImage.getPixel(k, l);
                if (ARGB.alpha(m) == 0) {
                    nativeImage.setPixel(k, l, ARGB.color(0, is[TextureUtil.pack(k, l, i)]));
                    continue;
                }
                nativeImage.setPixel(k, l, m);
            }
        }
    }

    public static void fillEmptyAreasWithDarkColor(NativeImage nativeImage) {
        int s;
        int r;
        int q;
        int p;
        int o;
        int n;
        int m;
        int i = nativeImage.getWidth();
        int j = nativeImage.getHeight();
        int k = -1;
        int l = Integer.MAX_VALUE;
        for (m = 0; m < i; ++m) {
            for (n = 0; n < j; ++n) {
                int t;
                o = nativeImage.getPixel(m, n);
                p = ARGB.alpha(o);
                if (p == 0 || (t = (q = ARGB.red(o)) + (r = ARGB.green(o)) + (s = ARGB.blue(o))) >= l) continue;
                l = t;
                k = o;
            }
        }
        m = 3 * ARGB.red(k) / 4;
        n = 3 * ARGB.green(k) / 4;
        o = 3 * ARGB.blue(k) / 4;
        p = ARGB.color(0, m, n, o);
        for (q = 0; q < i; ++q) {
            for (r = 0; r < j; ++r) {
                s = nativeImage.getPixel(q, r);
                if (ARGB.alpha(s) != 0) continue;
                nativeImage.setPixel(q, r, p);
            }
        }
    }

    private static int pack(int i, int j, int k) {
        return i + j * k;
    }

    private static int x(int i, int j) {
        return i % j;
    }

    private static int y(int i, int j) {
        return i / j;
    }
}

