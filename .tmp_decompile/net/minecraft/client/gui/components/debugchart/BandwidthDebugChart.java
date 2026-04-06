/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.SampleStorage;

@Environment(value=EnvType.CLIENT)
public class BandwidthDebugChart
extends AbstractDebugChart {
    private static final int MIN_COLOR = -16711681;
    private static final int MID_COLOR = -6250241;
    private static final int MAX_COLOR = -65536;
    private static final int KILOBYTE = 1024;
    private static final int MEGABYTE = 0x100000;
    private static final int CHART_TOP_VALUE = 0x100000;

    public BandwidthDebugChart(Font font, SampleStorage sampleStorage) {
        super(font, sampleStorage);
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int i, int j, int k) {
        this.drawLabeledLineAtValue(guiGraphics, i, j, k, 64);
        this.drawLabeledLineAtValue(guiGraphics, i, j, k, 1024);
        this.drawLabeledLineAtValue(guiGraphics, i, j, k, 16384);
        this.drawStringWithShade(guiGraphics, BandwidthDebugChart.toDisplayStringInternal(1048576.0), i + 1, k - BandwidthDebugChart.getSampleHeightInternal(1048576.0) + 1);
    }

    private void drawLabeledLineAtValue(GuiGraphics guiGraphics, int i, int j, int k, int l) {
        this.drawLineWithLabel(guiGraphics, i, j, k - BandwidthDebugChart.getSampleHeightInternal(l), BandwidthDebugChart.toDisplayStringInternal(l));
    }

    private void drawLineWithLabel(GuiGraphics guiGraphics, int i, int j, int k, String string) {
        this.drawStringWithShade(guiGraphics, string, i + 1, k + 1);
        guiGraphics.hLine(i, i + j - 1, k, -1);
    }

    @Override
    protected String toDisplayString(double d) {
        return BandwidthDebugChart.toDisplayStringInternal(BandwidthDebugChart.toBytesPerSecond(d));
    }

    private static String toDisplayStringInternal(double d) {
        if (d >= 1048576.0) {
            return String.format(Locale.ROOT, "%.1f MiB/s", d / 1048576.0);
        }
        if (d >= 1024.0) {
            return String.format(Locale.ROOT, "%.1f KiB/s", d / 1024.0);
        }
        return String.format(Locale.ROOT, "%d B/s", Mth.floor(d));
    }

    @Override
    protected int getSampleHeight(double d) {
        return BandwidthDebugChart.getSampleHeightInternal(BandwidthDebugChart.toBytesPerSecond(d));
    }

    private static int getSampleHeightInternal(double d) {
        return (int)Math.round(Math.log(d + 1.0) * 60.0 / Math.log(1048576.0));
    }

    @Override
    protected int getSampleColor(long l) {
        return this.getSampleColor(BandwidthDebugChart.toBytesPerSecond(l), 0.0, -16711681, 8192.0, -6250241, 1.048576E7, -65536);
    }

    private static double toBytesPerSecond(double d) {
        return d * 20.0;
    }
}

