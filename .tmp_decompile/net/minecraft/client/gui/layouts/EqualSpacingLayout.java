/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.layouts;

import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class EqualSpacingLayout
extends AbstractLayout {
    private final Orientation orientation;
    private final List<ChildContainer> children = new ArrayList<ChildContainer>();
    private final LayoutSettings defaultChildLayoutSettings = LayoutSettings.defaults();

    public EqualSpacingLayout(int i, int j, Orientation orientation) {
        this(0, 0, i, j, orientation);
    }

    public EqualSpacingLayout(int i, int j, int k, int l, Orientation orientation) {
        super(i, j, k, l);
        this.orientation = orientation;
    }

    @Override
    public void arrangeElements() {
        super.arrangeElements();
        if (this.children.isEmpty()) {
            return;
        }
        int i = 0;
        int j = this.orientation.getSecondaryLength(this);
        for (ChildContainer childContainer : this.children) {
            i += this.orientation.getPrimaryLength(childContainer);
            j = Math.max(j, this.orientation.getSecondaryLength(childContainer));
        }
        int k = this.orientation.getPrimaryLength(this) - i;
        int l = this.orientation.getPrimaryPosition(this);
        Iterator<ChildContainer> iterator = this.children.iterator();
        ChildContainer childContainer2 = iterator.next();
        this.orientation.setPrimaryPosition(childContainer2, l);
        l += this.orientation.getPrimaryLength(childContainer2);
        if (this.children.size() >= 2) {
            Divisor divisor = new Divisor(k, this.children.size() - 1);
            while (divisor.hasNext()) {
                ChildContainer childContainer3 = iterator.next();
                this.orientation.setPrimaryPosition(childContainer3, l += divisor.nextInt());
                l += this.orientation.getPrimaryLength(childContainer3);
            }
        }
        int m = this.orientation.getSecondaryPosition(this);
        for (ChildContainer childContainer4 : this.children) {
            this.orientation.setSecondaryPosition(childContainer4, m, j);
        }
        switch (this.orientation.ordinal()) {
            case 0: {
                this.height = j;
                break;
            }
            case 1: {
                this.width = j;
            }
        }
    }

    @Override
    public void visitChildren(Consumer<LayoutElement> consumer) {
        this.children.forEach(childContainer -> consumer.accept(childContainer.child));
    }

    public LayoutSettings newChildLayoutSettings() {
        return this.defaultChildLayoutSettings.copy();
    }

    public LayoutSettings defaultChildLayoutSetting() {
        return this.defaultChildLayoutSettings;
    }

    public <T extends LayoutElement> T addChild(T layoutElement) {
        return this.addChild(layoutElement, this.newChildLayoutSettings());
    }

    public <T extends LayoutElement> T addChild(T layoutElement, LayoutSettings layoutSettings) {
        this.children.add(new ChildContainer(layoutElement, layoutSettings));
        return layoutElement;
    }

    public <T extends LayoutElement> T addChild(T layoutElement, Consumer<LayoutSettings> consumer) {
        return this.addChild(layoutElement, Util.make(this.newChildLayoutSettings(), consumer));
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Orientation {
        HORIZONTAL,
        VERTICAL;


        int getPrimaryLength(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getWidth();
                case 1 -> layoutElement.getHeight();
            };
        }

        int getPrimaryLength(ChildContainer childContainer) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> childContainer.getWidth();
                case 1 -> childContainer.getHeight();
            };
        }

        int getSecondaryLength(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getHeight();
                case 1 -> layoutElement.getWidth();
            };
        }

        int getSecondaryLength(ChildContainer childContainer) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> childContainer.getHeight();
                case 1 -> childContainer.getWidth();
            };
        }

        void setPrimaryPosition(ChildContainer childContainer, int i) {
            switch (this.ordinal()) {
                case 0: {
                    childContainer.setX(i, childContainer.getWidth());
                    break;
                }
                case 1: {
                    childContainer.setY(i, childContainer.getHeight());
                }
            }
        }

        void setSecondaryPosition(ChildContainer childContainer, int i, int j) {
            switch (this.ordinal()) {
                case 0: {
                    childContainer.setY(i, j);
                    break;
                }
                case 1: {
                    childContainer.setX(i, j);
                }
            }
        }

        int getPrimaryPosition(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getX();
                case 1 -> layoutElement.getY();
            };
        }

        int getSecondaryPosition(LayoutElement layoutElement) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> layoutElement.getY();
                case 1 -> layoutElement.getX();
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class ChildContainer
    extends AbstractLayout.AbstractChildWrapper {
        protected ChildContainer(LayoutElement layoutElement, LayoutSettings layoutSettings) {
            super(layoutElement, layoutSettings);
        }
    }
}

