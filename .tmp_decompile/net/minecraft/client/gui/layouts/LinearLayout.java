/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class LinearLayout
implements Layout {
    private final GridLayout wrapped;
    private final Orientation orientation;
    private int nextChildIndex = 0;

    private LinearLayout(Orientation orientation) {
        this(0, 0, orientation);
    }

    public LinearLayout(int i, int j, Orientation orientation) {
        this.wrapped = new GridLayout(i, j);
        this.orientation = orientation;
    }

    public LinearLayout spacing(int i) {
        this.orientation.setSpacing(this.wrapped, i);
        return this;
    }

    public LayoutSettings newCellSettings() {
        return this.wrapped.newCellSettings();
    }

    public LayoutSettings defaultCellSetting() {
        return this.wrapped.defaultCellSetting();
    }

    public <T extends LayoutElement> T addChild(T layoutElement, LayoutSettings layoutSettings) {
        return this.orientation.addChild(this.wrapped, layoutElement, this.nextChildIndex++, layoutSettings);
    }

    public <T extends LayoutElement> T addChild(T layoutElement) {
        return this.addChild(layoutElement, this.newCellSettings());
    }

    public <T extends LayoutElement> T addChild(T layoutElement, Consumer<LayoutSettings> consumer) {
        return this.orientation.addChild(this.wrapped, layoutElement, this.nextChildIndex++, Util.make(this.newCellSettings(), consumer));
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.wrapped.visitChildren(consumer);
    }

    @Override
    public void arrangeElements() {
        this.wrapped.arrangeElements();
    }

    @Override
    public int getWidth() {
        return this.wrapped.getWidth();
    }

    @Override
    public int getHeight() {
        return this.wrapped.getHeight();
    }

    @Override
    public void setX(int i) {
        this.wrapped.setX(i);
    }

    @Override
    public void setY(int i) {
        this.wrapped.setY(i);
    }

    @Override
    public int getX() {
        return this.wrapped.getX();
    }

    @Override
    public int getY() {
        return this.wrapped.getY();
    }

    public static LinearLayout vertical() {
        return new LinearLayout(Orientation.VERTICAL);
    }

    public static LinearLayout horizontal() {
        return new LinearLayout(Orientation.HORIZONTAL);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;


        void setSpacing(GridLayout gridLayout, int i) {
            switch (this.ordinal()) {
                case 0: {
                    gridLayout.columnSpacing(i);
                    break;
                }
                case 1: {
                    gridLayout.rowSpacing(i);
                }
            }
        }

        public <T extends LayoutElement> T addChild(GridLayout gridLayout, T layoutElement, int i, LayoutSettings layoutSettings) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> gridLayout.addChild(layoutElement, 0, i, layoutSettings);
                case 1 -> gridLayout.addChild(layoutElement, i, 0, layoutSettings);
            };
        }
    }
}

