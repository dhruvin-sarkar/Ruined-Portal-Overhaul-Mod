/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.debugchart;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ResultField;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ProfilerPieChart {
    public static final int RADIUS = 105;
    public static final int PIE_CHART_THICKNESS = 10;
    private static final int MARGIN = 5;
    private final Font font;
    private @Nullable ProfileResults profilerPieChartResults;
    private String profilerTreePath = "root";
    private int bottomOffset = 0;

    public ProfilerPieChart(Font font) {
        this.font = font;
    }

    public void setPieChartResults(@Nullable ProfileResults profileResults) {
        this.profilerPieChartResults = profileResults;
    }

    public void setBottomOffset(int i) {
        this.bottomOffset = i;
    }

    public void render(GuiGraphics guiGraphics) {
        if (this.profilerPieChartResults == null) {
            return;
        }
        List<ResultField> list = this.profilerPieChartResults.getTimes(this.profilerTreePath);
        ResultField resultField = (ResultField)list.removeFirst();
        int i = guiGraphics.guiWidth() - 105 - 10;
        int j = i - 105;
        int k = i + 105;
        int l = list.size() * this.font.lineHeight;
        int m = guiGraphics.guiHeight() - this.bottomOffset - 5;
        int n = m - l;
        int o = 62;
        int p = n - 62 - 5;
        guiGraphics.fill(j - 5, p - 62 - 5, k + 5, m + 5, -1873784752);
        guiGraphics.submitProfilerChartRenderState(list, j, p - 62 + 10, k, p + 62);
        DecimalFormat decimalFormat = new DecimalFormat("##0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
        String string = ProfileResults.demanglePath(resultField.name);
        Object string2 = "";
        if (!"unspecified".equals(string)) {
            string2 = (String)string2 + "[0] ";
        }
        string2 = string.isEmpty() ? (String)string2 + "ROOT " : (String)string2 + string + " ";
        int q = -1;
        int r = p - 62;
        guiGraphics.drawString(this.font, (String)string2, j, r, -1);
        string2 = decimalFormat.format(resultField.globalPercentage) + "%";
        guiGraphics.drawString(this.font, (String)string2, k - this.font.width((String)string2), r, -1);
        for (int s = 0; s < list.size(); ++s) {
            ResultField resultField2 = list.get(s);
            StringBuilder stringBuilder = new StringBuilder();
            if ("unspecified".equals(resultField2.name)) {
                stringBuilder.append("[?] ");
            } else {
                stringBuilder.append("[").append(s + 1).append("] ");
            }
            Object string3 = stringBuilder.append(resultField2.name).toString();
            int t = n + s * this.font.lineHeight;
            guiGraphics.drawString(this.font, (String)string3, j, t, resultField2.getColor());
            string3 = decimalFormat.format(resultField2.percentage) + "%";
            guiGraphics.drawString(this.font, (String)string3, k - 50 - this.font.width((String)string3), t, resultField2.getColor());
            string3 = decimalFormat.format(resultField2.globalPercentage) + "%";
            guiGraphics.drawString(this.font, (String)string3, k - this.font.width((String)string3), t, resultField2.getColor());
        }
    }

    public void profilerPieChartKeyPress(int i) {
        if (this.profilerPieChartResults == null) {
            return;
        }
        List<ResultField> list = this.profilerPieChartResults.getTimes(this.profilerTreePath);
        if (list.isEmpty()) {
            return;
        }
        ResultField resultField = list.remove(0);
        if (i == 0) {
            int j;
            if (!resultField.name.isEmpty() && (j = this.profilerTreePath.lastIndexOf(30)) >= 0) {
                this.profilerTreePath = this.profilerTreePath.substring(0, j);
            }
        } else if (--i < list.size() && !"unspecified".equals(list.get((int)i).name)) {
            if (!this.profilerTreePath.isEmpty()) {
                this.profilerTreePath = this.profilerTreePath + "\u001e";
            }
            this.profilerTreePath = this.profilerTreePath + list.get((int)i).name;
        }
    }
}

