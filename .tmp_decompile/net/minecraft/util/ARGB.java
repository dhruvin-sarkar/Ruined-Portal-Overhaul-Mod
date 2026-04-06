/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package net.minecraft.util;

import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ARGB {
    private static final int LINEAR_CHANNEL_DEPTH = 1024;
    private static final short[] SRGB_TO_LINEAR = Util.make(new short[256], ss -> {
        for (int i = 0; i < ((short[])ss).length; ++i) {
            float f = (float)i / 255.0f;
            ss[i] = (short)Math.round(ARGB.computeSrgbToLinear(f) * 1023.0f);
        }
    });
    private static final byte[] LINEAR_TO_SRGB = Util.make(new byte[1024], bs -> {
        for (int i = 0; i < ((byte[])bs).length; ++i) {
            float f = (float)i / 1023.0f;
            bs[i] = (byte)Math.round(ARGB.computeLinearToSrgb(f) * 255.0f);
        }
    });

    private static float computeSrgbToLinear(float f) {
        if (f >= 0.04045f) {
            return (float)Math.pow(((double)f + 0.055) / 1.055, 2.4);
        }
        return f / 12.92f;
    }

    private static float computeLinearToSrgb(float f) {
        if (f >= 0.0031308f) {
            return (float)(1.055 * Math.pow(f, 0.4166666666666667) - 0.055);
        }
        return 12.92f * f;
    }

    public static float srgbToLinearChannel(int i) {
        return (float)SRGB_TO_LINEAR[i] / 1023.0f;
    }

    public static int linearToSrgbChannel(float f) {
        return LINEAR_TO_SRGB[Mth.floor(f * 1023.0f)] & 0xFF;
    }

    public static int meanLinear(int i, int j, int k, int l) {
        return ARGB.color((ARGB.alpha(i) + ARGB.alpha(j) + ARGB.alpha(k) + ARGB.alpha(l)) / 4, ARGB.linearChannelMean(ARGB.red(i), ARGB.red(j), ARGB.red(k), ARGB.red(l)), ARGB.linearChannelMean(ARGB.green(i), ARGB.green(j), ARGB.green(k), ARGB.green(l)), ARGB.linearChannelMean(ARGB.blue(i), ARGB.blue(j), ARGB.blue(k), ARGB.blue(l)));
    }

    private static int linearChannelMean(int i, int j, int k, int l) {
        int m = (SRGB_TO_LINEAR[i] + SRGB_TO_LINEAR[j] + SRGB_TO_LINEAR[k] + SRGB_TO_LINEAR[l]) / 4;
        return LINEAR_TO_SRGB[m] & 0xFF;
    }

    public static int alpha(int i) {
        return i >>> 24;
    }

    public static int red(int i) {
        return i >> 16 & 0xFF;
    }

    public static int green(int i) {
        return i >> 8 & 0xFF;
    }

    public static int blue(int i) {
        return i & 0xFF;
    }

    public static int color(int i, int j, int k, int l) {
        return (i & 0xFF) << 24 | (j & 0xFF) << 16 | (k & 0xFF) << 8 | l & 0xFF;
    }

    public static int color(int i, int j, int k) {
        return ARGB.color(255, i, j, k);
    }

    public static int color(Vec3 vec3) {
        return ARGB.color(ARGB.as8BitChannel((float)vec3.x()), ARGB.as8BitChannel((float)vec3.y()), ARGB.as8BitChannel((float)vec3.z()));
    }

    public static int multiply(int i, int j) {
        if (i == -1) {
            return j;
        }
        if (j == -1) {
            return i;
        }
        return ARGB.color(ARGB.alpha(i) * ARGB.alpha(j) / 255, ARGB.red(i) * ARGB.red(j) / 255, ARGB.green(i) * ARGB.green(j) / 255, ARGB.blue(i) * ARGB.blue(j) / 255);
    }

    public static int addRgb(int i, int j) {
        return ARGB.color(ARGB.alpha(i), Math.min(ARGB.red(i) + ARGB.red(j), 255), Math.min(ARGB.green(i) + ARGB.green(j), 255), Math.min(ARGB.blue(i) + ARGB.blue(j), 255));
    }

    public static int subtractRgb(int i, int j) {
        return ARGB.color(ARGB.alpha(i), Math.max(ARGB.red(i) - ARGB.red(j), 0), Math.max(ARGB.green(i) - ARGB.green(j), 0), Math.max(ARGB.blue(i) - ARGB.blue(j), 0));
    }

    public static int multiplyAlpha(int i, float f) {
        if (i == 0 || f <= 0.0f) {
            return 0;
        }
        if (f >= 1.0f) {
            return i;
        }
        return ARGB.color(ARGB.alphaFloat(i) * f, i);
    }

    public static int scaleRGB(int i, float f) {
        return ARGB.scaleRGB(i, f, f, f);
    }

    public static int scaleRGB(int i, float f, float g, float h) {
        return ARGB.color(ARGB.alpha(i), Math.clamp((long)((int)((float)ARGB.red(i) * f)), (int)0, (int)255), Math.clamp((long)((int)((float)ARGB.green(i) * g)), (int)0, (int)255), Math.clamp((long)((int)((float)ARGB.blue(i) * h)), (int)0, (int)255));
    }

    public static int scaleRGB(int i, int j) {
        return ARGB.color(ARGB.alpha(i), Math.clamp((long)((long)ARGB.red(i) * (long)j / 255L), (int)0, (int)255), Math.clamp((long)((long)ARGB.green(i) * (long)j / 255L), (int)0, (int)255), Math.clamp((long)((long)ARGB.blue(i) * (long)j / 255L), (int)0, (int)255));
    }

    public static int greyscale(int i) {
        int j = (int)((float)ARGB.red(i) * 0.3f + (float)ARGB.green(i) * 0.59f + (float)ARGB.blue(i) * 0.11f);
        return ARGB.color(ARGB.alpha(i), j, j, j);
    }

    public static int alphaBlend(int i, int j) {
        int k = ARGB.alpha(i);
        int l = ARGB.alpha(j);
        if (l == 255) {
            return j;
        }
        if (l == 0) {
            return i;
        }
        int m = l + k * (255 - l) / 255;
        return ARGB.color(m, ARGB.alphaBlendChannel(m, l, ARGB.red(i), ARGB.red(j)), ARGB.alphaBlendChannel(m, l, ARGB.green(i), ARGB.green(j)), ARGB.alphaBlendChannel(m, l, ARGB.blue(i), ARGB.blue(j)));
    }

    private static int alphaBlendChannel(int i, int j, int k, int l) {
        return (l * j + k * (i - j)) / i;
    }

    public static int srgbLerp(float f, int i, int j) {
        int k = Mth.lerpInt(f, ARGB.alpha(i), ARGB.alpha(j));
        int l = Mth.lerpInt(f, ARGB.red(i), ARGB.red(j));
        int m = Mth.lerpInt(f, ARGB.green(i), ARGB.green(j));
        int n = Mth.lerpInt(f, ARGB.blue(i), ARGB.blue(j));
        return ARGB.color(k, l, m, n);
    }

    public static int linearLerp(float f, int i, int j) {
        return ARGB.color(Mth.lerpInt(f, ARGB.alpha(i), ARGB.alpha(j)), LINEAR_TO_SRGB[Mth.lerpInt(f, SRGB_TO_LINEAR[ARGB.red(i)], SRGB_TO_LINEAR[ARGB.red(j)])] & 0xFF, LINEAR_TO_SRGB[Mth.lerpInt(f, SRGB_TO_LINEAR[ARGB.green(i)], SRGB_TO_LINEAR[ARGB.green(j)])] & 0xFF, LINEAR_TO_SRGB[Mth.lerpInt(f, SRGB_TO_LINEAR[ARGB.blue(i)], SRGB_TO_LINEAR[ARGB.blue(j)])] & 0xFF);
    }

    public static int opaque(int i) {
        return i | 0xFF000000;
    }

    public static int transparent(int i) {
        return i & 0xFFFFFF;
    }

    public static int color(int i, int j) {
        return i << 24 | j & 0xFFFFFF;
    }

    public static int color(float f, int i) {
        return ARGB.as8BitChannel(f) << 24 | i & 0xFFFFFF;
    }

    public static int white(float f) {
        return ARGB.as8BitChannel(f) << 24 | 0xFFFFFF;
    }

    public static int white(int i) {
        return i << 24 | 0xFFFFFF;
    }

    public static int black(float f) {
        return ARGB.as8BitChannel(f) << 24;
    }

    public static int black(int i) {
        return i << 24;
    }

    public static int colorFromFloat(float f, float g, float h, float i) {
        return ARGB.color(ARGB.as8BitChannel(f), ARGB.as8BitChannel(g), ARGB.as8BitChannel(h), ARGB.as8BitChannel(i));
    }

    public static Vector3f vector3fFromRGB24(int i) {
        return new Vector3f(ARGB.redFloat(i), ARGB.greenFloat(i), ARGB.blueFloat(i));
    }

    public static Vector4f vector4fFromARGB32(int i) {
        return new Vector4f(ARGB.redFloat(i), ARGB.greenFloat(i), ARGB.blueFloat(i), ARGB.alphaFloat(i));
    }

    public static int average(int i, int j) {
        return ARGB.color((ARGB.alpha(i) + ARGB.alpha(j)) / 2, (ARGB.red(i) + ARGB.red(j)) / 2, (ARGB.green(i) + ARGB.green(j)) / 2, (ARGB.blue(i) + ARGB.blue(j)) / 2);
    }

    public static int as8BitChannel(float f) {
        return Mth.floor(f * 255.0f);
    }

    public static float alphaFloat(int i) {
        return ARGB.from8BitChannel(ARGB.alpha(i));
    }

    public static float redFloat(int i) {
        return ARGB.from8BitChannel(ARGB.red(i));
    }

    public static float greenFloat(int i) {
        return ARGB.from8BitChannel(ARGB.green(i));
    }

    public static float blueFloat(int i) {
        return ARGB.from8BitChannel(ARGB.blue(i));
    }

    private static float from8BitChannel(int i) {
        return (float)i / 255.0f;
    }

    public static int toABGR(int i) {
        return i & 0xFF00FF00 | (i & 0xFF0000) >> 16 | (i & 0xFF) << 16;
    }

    public static int fromABGR(int i) {
        return ARGB.toABGR(i);
    }

    public static int setBrightness(int i, float f) {
        float s;
        float r;
        float q;
        float p;
        int j = ARGB.red(i);
        int k = ARGB.green(i);
        int l = ARGB.blue(i);
        int m = ARGB.alpha(i);
        int n = Math.max(Math.max(j, k), l);
        int o = Math.min(Math.min(j, k), l);
        float g = n - o;
        float h = n != 0 ? g / (float)n : 0.0f;
        if (h == 0.0f) {
            p = 0.0f;
        } else {
            q = (float)(n - j) / g;
            r = (float)(n - k) / g;
            s = (float)(n - l) / g;
            p = j == n ? s - r : (k == n ? 2.0f + q - s : 4.0f + r - q);
            if ((p /= 6.0f) < 0.0f) {
                p += 1.0f;
            }
        }
        if (h == 0.0f) {
            k = l = Math.round(f * 255.0f);
            j = l;
            return ARGB.color(m, j, k, l);
        }
        q = (p - (float)Math.floor(p)) * 6.0f;
        r = q - (float)Math.floor(q);
        s = f * (1.0f - h);
        float t = f * (1.0f - h * r);
        float u = f * (1.0f - h * (1.0f - r));
        switch ((int)q) {
            case 0: {
                j = Math.round(f * 255.0f);
                k = Math.round(u * 255.0f);
                l = Math.round(s * 255.0f);
                break;
            }
            case 1: {
                j = Math.round(t * 255.0f);
                k = Math.round(f * 255.0f);
                l = Math.round(s * 255.0f);
                break;
            }
            case 2: {
                j = Math.round(s * 255.0f);
                k = Math.round(f * 255.0f);
                l = Math.round(u * 255.0f);
                break;
            }
            case 3: {
                j = Math.round(s * 255.0f);
                k = Math.round(t * 255.0f);
                l = Math.round(f * 255.0f);
                break;
            }
            case 4: {
                j = Math.round(u * 255.0f);
                k = Math.round(s * 255.0f);
                l = Math.round(f * 255.0f);
                break;
            }
            case 5: {
                j = Math.round(f * 255.0f);
                k = Math.round(s * 255.0f);
                l = Math.round(t * 255.0f);
            }
        }
        return ARGB.color(m, j, k, l);
    }
}

