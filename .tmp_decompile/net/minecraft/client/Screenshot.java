/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class Screenshot {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String SCREENSHOT_DIR = "screenshots";

    public static void grab(File file, RenderTarget renderTarget, Consumer<Component> consumer) {
        Screenshot.grab(file, null, renderTarget, 1, consumer);
    }

    public static void grab(File file, @Nullable String string, RenderTarget renderTarget, int i, Consumer<Component> consumer) {
        Screenshot.takeScreenshot(renderTarget, i, nativeImage -> {
            File file2 = new File(file, SCREENSHOT_DIR);
            file2.mkdir();
            File file3 = string == null ? Screenshot.getFile(file2) : new File(file2, string);
            Util.ioPool().execute(() -> {
                try (NativeImage nativeImage2 = nativeImage;){
                    nativeImage.writeToFile(file3);
                    MutableComponent component = Component.literal(file3.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(file3.getAbsoluteFile())));
                    consumer.accept(Component.translatable("screenshot.success", component));
                }
                catch (Exception exception) {
                    LOGGER.warn("Couldn't save screenshot", (Throwable)exception);
                    consumer.accept(Component.translatable("screenshot.failure", exception.getMessage()));
                }
            });
        });
    }

    public static void takeScreenshot(RenderTarget renderTarget, Consumer<NativeImage> consumer) {
        Screenshot.takeScreenshot(renderTarget, 1, consumer);
    }

    public static void takeScreenshot(RenderTarget renderTarget, int i, Consumer<NativeImage> consumer) {
        int j = renderTarget.width;
        int k = renderTarget.height;
        GpuTexture gpuTexture = renderTarget.getColorTexture();
        if (gpuTexture == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        }
        if (j % i != 0 || k % i != 0) {
            throw new IllegalArgumentException("Image size is not divisible by downscale factor");
        }
        GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Screenshot buffer", 9, (long)j * (long)k * (long)gpuTexture.getFormat().pixelSize());
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(gpuTexture, gpuBuffer, 0L, () -> {
            try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(gpuBuffer, true, false);){
                int l = k / i;
                int m = j / i;
                NativeImage nativeImage = new NativeImage(m, l, false);
                for (int n = 0; n < l; ++n) {
                    for (int o = 0; o < m; ++o) {
                        int s;
                        int p;
                        if (i == 1) {
                            p = mappedView.data().getInt((o + n * j) * gpuTexture.getFormat().pixelSize());
                            nativeImage.setPixelABGR(o, k - n - 1, p | 0xFF000000);
                            continue;
                        }
                        p = 0;
                        int q = 0;
                        int r = 0;
                        for (s = 0; s < i; ++s) {
                            for (int t = 0; t < i; ++t) {
                                int u = mappedView.data().getInt((o * i + s + (n * i + t) * j) * gpuTexture.getFormat().pixelSize());
                                p += ARGB.red(u);
                                q += ARGB.green(u);
                                r += ARGB.blue(u);
                            }
                        }
                        s = i * i;
                        nativeImage.setPixelABGR(o, l - n - 1, ARGB.color(255, p / s, q / s, r / s));
                    }
                }
                consumer.accept(nativeImage);
            }
            gpuBuffer.close();
        }, 0);
    }

    private static File getFile(File file) {
        String string = Util.getFilenameFormattedDateTime();
        int i = 1;
        File file2;
        while ((file2 = new File(file, string + (String)(i == 1 ? "" : "_" + i) + ".png")).exists()) {
            ++i;
        }
        return file2;
    }
}

