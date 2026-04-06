/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.systems.GpuDevice;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GraphicsWorkarounds {
    private static final List<String> INTEL_GEN11_CORE = List.of((Object[])new String[]{"i3-1000g1", "i3-1000g4", "i3-1000ng4", "i3-1005g1", "i3-l13g4", "i5-1030g4", "i5-1030g7", "i5-1030ng7", "i5-1034g1", "i5-1035g1", "i5-1035g4", "i5-1035g7", "i5-1038ng7", "i5-l16g7", "i7-1060g7", "i7-1060ng7", "i7-1065g7", "i7-1068g7", "i7-1068ng7"});
    private static final List<String> INTEL_GEN11_ATOM = List.of((Object)"x6211e", (Object)"x6212re", (Object)"x6214re", (Object)"x6413e", (Object)"x6414re", (Object)"x6416re", (Object)"x6425e", (Object)"x6425re", (Object)"x6427fe");
    private static final List<String> INTEL_GEN11_CELERON = List.of((Object)"j6412", (Object)"j6413", (Object)"n4500", (Object)"n4505", (Object)"n5095", (Object)"n5095a", (Object)"n5100", (Object)"n5105", (Object)"n6210", (Object)"n6211");
    private static final List<String> INTEL_GEN11_PENTIUM = List.of((Object)"6805", (Object)"j6426", (Object)"n6415", (Object)"n6000", (Object)"n6005");
    private static @Nullable GraphicsWorkarounds instance;
    private final WeakReference<GpuDevice> gpuDevice;
    private final boolean alwaysCreateFreshImmediateBuffer;
    private final boolean isGlOnDx12;
    private final boolean isAmd;

    private GraphicsWorkarounds(GpuDevice gpuDevice) {
        this.gpuDevice = new WeakReference<GpuDevice>(gpuDevice);
        this.alwaysCreateFreshImmediateBuffer = GraphicsWorkarounds.isIntelGen11(gpuDevice);
        this.isGlOnDx12 = GraphicsWorkarounds.isGlOnDx12(gpuDevice);
        this.isAmd = GraphicsWorkarounds.isAmd(gpuDevice);
    }

    public static GraphicsWorkarounds get(GpuDevice gpuDevice) {
        GraphicsWorkarounds graphicsWorkarounds = instance;
        if (graphicsWorkarounds == null || graphicsWorkarounds.gpuDevice.get() != gpuDevice) {
            instance = graphicsWorkarounds = new GraphicsWorkarounds(gpuDevice);
        }
        return graphicsWorkarounds;
    }

    public boolean alwaysCreateFreshImmediateBuffer() {
        return this.alwaysCreateFreshImmediateBuffer;
    }

    public boolean isGlOnDx12() {
        return this.isGlOnDx12;
    }

    public boolean isAmd() {
        return this.isAmd;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean isIntelGen11(GpuDevice gpuDevice) {
        String string = GLX._getCpuInfo().toLowerCase(Locale.ROOT);
        String string2 = gpuDevice.getRenderer().toLowerCase(Locale.ROOT);
        if (!string.contains("intel")) return false;
        if (!string2.contains("intel")) return false;
        if (string2.contains("mesa")) {
            return false;
        }
        if (string2.endsWith("gen11")) {
            return true;
        }
        if (!string2.contains("uhd graphics") && !string2.contains("iris")) {
            return false;
        }
        if (string.contains("atom")) {
            if (INTEL_GEN11_ATOM.stream().anyMatch(string::contains)) return true;
        }
        if (string.contains("celeron")) {
            if (INTEL_GEN11_CELERON.stream().anyMatch(string::contains)) return true;
        }
        if (string.contains("pentium")) {
            if (INTEL_GEN11_PENTIUM.stream().anyMatch(string::contains)) return true;
        }
        if (!INTEL_GEN11_CORE.stream().anyMatch(string::contains)) return false;
        return true;
    }

    private static boolean isGlOnDx12(GpuDevice gpuDevice) {
        boolean bl = Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64();
        return bl || gpuDevice.getRenderer().startsWith("D3D12");
    }

    private static boolean isAmd(GpuDevice gpuDevice) {
        return gpuDevice.getRenderer().contains("AMD");
    }
}

