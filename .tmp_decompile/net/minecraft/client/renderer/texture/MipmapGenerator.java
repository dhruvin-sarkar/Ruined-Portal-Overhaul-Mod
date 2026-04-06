/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

@Environment(value=EnvType.CLIENT)
public class MipmapGenerator {
    private static final String ITEM_PREFIX = "item/";
    private static final float ALPHA_CUTOFF = 0.5f;
    private static final float STRICT_ALPHA_CUTOFF = 0.3f;

    private MipmapGenerator() {
    }

    private static float alphaTestCoverage(NativeImage nativeImage, float f, float g) {
        int i = nativeImage.getWidth();
        int j = nativeImage.getHeight();
        float h = 0.0f;
        int k = 4;
        for (int l = 0; l < j - 1; ++l) {
            for (int m = 0; m < i - 1; ++m) {
                float n = Math.clamp((float)(ARGB.alphaFloat(nativeImage.getPixel(m, l)) * g), (float)0.0f, (float)1.0f);
                float o = Math.clamp((float)(ARGB.alphaFloat(nativeImage.getPixel(m + 1, l)) * g), (float)0.0f, (float)1.0f);
                float p = Math.clamp((float)(ARGB.alphaFloat(nativeImage.getPixel(m, l + 1)) * g), (float)0.0f, (float)1.0f);
                float q = Math.clamp((float)(ARGB.alphaFloat(nativeImage.getPixel(m + 1, l + 1)) * g), (float)0.0f, (float)1.0f);
                float r = 0.0f;
                for (int s = 0; s < 4; ++s) {
                    float t = ((float)s + 0.5f) / 4.0f;
                    for (int u = 0; u < 4; ++u) {
                        float v = ((float)u + 0.5f) / 4.0f;
                        float w = n * (1.0f - v) * (1.0f - t) + o * v * (1.0f - t) + p * (1.0f - v) * t + q * v * t;
                        if (!(w > f)) continue;
                        r += 1.0f;
                    }
                }
                h += r / 16.0f;
            }
        }
        return h / (float)((i - 1) * (j - 1));
    }

    private static void scaleAlphaToCoverage(NativeImage nativeImage, float f, float g, float h) {
        int p;
        float i = 0.0f;
        float j = 4.0f;
        float k = 1.0f;
        float l = 1.0f;
        float m = Float.MAX_VALUE;
        int n = nativeImage.getWidth();
        int o = nativeImage.getHeight();
        for (p = 0; p < 5; ++p) {
            float q = MipmapGenerator.alphaTestCoverage(nativeImage, g, k);
            float r = Math.abs(q - f);
            if (r < m) {
                m = r;
                l = k;
            }
            if (q < f) {
                i = k;
            } else {
                if (!(q > f)) break;
                j = k;
            }
            k = (i + j) * 0.5f;
        }
        for (p = 0; p < o; ++p) {
            for (int s = 0; s < n; ++s) {
                int t = nativeImage.getPixel(s, p);
                float u = ARGB.alphaFloat(t);
                u = u * l + h + 0.025f;
                u = Math.clamp((float)u, (float)0.0f, (float)1.0f);
                nativeImage.setPixel(s, p, ARGB.color(u, t));
            }
        }
    }

    public static NativeImage[] generateMipLevels(Identifier identifier, NativeImage[] nativeImages, int i, MipmapStrategy mipmapStrategy, float f) {
        if (mipmapStrategy == MipmapStrategy.AUTO) {
            MipmapStrategy mipmapStrategy2 = mipmapStrategy = MipmapGenerator.hasTransparentPixel(nativeImages[0]) ? MipmapStrategy.CUTOUT : MipmapStrategy.MEAN;
        }
        if (nativeImages.length == 1 && !identifier.getPath().startsWith(ITEM_PREFIX)) {
            if (mipmapStrategy == MipmapStrategy.CUTOUT || mipmapStrategy == MipmapStrategy.STRICT_CUTOUT) {
                TextureUtil.solidify(nativeImages[0]);
            } else if (mipmapStrategy == MipmapStrategy.DARK_CUTOUT) {
                TextureUtil.fillEmptyAreasWithDarkColor(nativeImages[0]);
            }
        }
        if (i + 1 <= nativeImages.length) {
            return nativeImages;
        }
        NativeImage[] nativeImages2 = new NativeImage[i + 1];
        nativeImages2[0] = nativeImages[0];
        boolean bl = mipmapStrategy == MipmapStrategy.CUTOUT || mipmapStrategy == MipmapStrategy.STRICT_CUTOUT || mipmapStrategy == MipmapStrategy.DARK_CUTOUT;
        float g = mipmapStrategy == MipmapStrategy.STRICT_CUTOUT ? 0.3f : 0.5f;
        float h = bl ? MipmapGenerator.alphaTestCoverage(nativeImages[0], g, 1.0f) : 0.0f;
        for (int j = 1; j <= i; ++j) {
            if (j < nativeImages.length) {
                nativeImages2[j] = nativeImages[j];
            } else {
                NativeImage nativeImage = nativeImages2[j - 1];
                NativeImage nativeImage2 = new NativeImage(nativeImage.getWidth() >> 1, nativeImage.getHeight() >> 1, false);
                int k = nativeImage2.getWidth();
                int l = nativeImage2.getHeight();
                for (int m = 0; m < k; ++m) {
                    for (int n = 0; n < l; ++n) {
                        int o = nativeImage.getPixel(m * 2 + 0, n * 2 + 0);
                        int p = nativeImage.getPixel(m * 2 + 1, n * 2 + 0);
                        int q = nativeImage.getPixel(m * 2 + 0, n * 2 + 1);
                        int r = nativeImage.getPixel(m * 2 + 1, n * 2 + 1);
                        int s = mipmapStrategy == MipmapStrategy.DARK_CUTOUT ? MipmapGenerator.darkenedAlphaBlend(o, p, q, r) : ARGB.meanLinear(o, p, q, r);
                        nativeImage2.setPixel(m, n, s);
                    }
                }
                nativeImages2[j] = nativeImage2;
            }
            if (!bl) continue;
            MipmapGenerator.scaleAlphaToCoverage(nativeImages2[j], h, g, f);
        }
        return nativeImages2;
    }

    private static boolean hasTransparentPixel(NativeImage nativeImage) {
        for (int i = 0; i < nativeImage.getWidth(); ++i) {
            for (int j = 0; j < nativeImage.getHeight(); ++j) {
                if (ARGB.alpha(nativeImage.getPixel(i, j)) != 0) continue;
                return true;
            }
        }
        return false;
    }

    private static int darkenedAlphaBlend(int i, int j, int k, int l) {
        float f = 0.0f;
        float g = 0.0f;
        float h = 0.0f;
        float m = 0.0f;
        if (ARGB.alpha(i) != 0) {
            f += ARGB.srgbToLinearChannel(ARGB.alpha(i));
            g += ARGB.srgbToLinearChannel(ARGB.red(i));
            h += ARGB.srgbToLinearChannel(ARGB.green(i));
            m += ARGB.srgbToLinearChannel(ARGB.blue(i));
        }
        if (ARGB.alpha(j) != 0) {
            f += ARGB.srgbToLinearChannel(ARGB.alpha(j));
            g += ARGB.srgbToLinearChannel(ARGB.red(j));
            h += ARGB.srgbToLinearChannel(ARGB.green(j));
            m += ARGB.srgbToLinearChannel(ARGB.blue(j));
        }
        if (ARGB.alpha(k) != 0) {
            f += ARGB.srgbToLinearChannel(ARGB.alpha(k));
            g += ARGB.srgbToLinearChannel(ARGB.red(k));
            h += ARGB.srgbToLinearChannel(ARGB.green(k));
            m += ARGB.srgbToLinearChannel(ARGB.blue(k));
        }
        if (ARGB.alpha(l) != 0) {
            f += ARGB.srgbToLinearChannel(ARGB.alpha(l));
            g += ARGB.srgbToLinearChannel(ARGB.red(l));
            h += ARGB.srgbToLinearChannel(ARGB.green(l));
            m += ARGB.srgbToLinearChannel(ARGB.blue(l));
        }
        return ARGB.color(ARGB.linearToSrgbChannel(f /= 4.0f), ARGB.linearToSrgbChannel(g /= 4.0f), ARGB.linearToSrgbChannel(h /= 4.0f), ARGB.linearToSrgbChannel(m /= 4.0f));
    }
}

