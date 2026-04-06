/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntComparator
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.navigation;

import it.unimi.dsi.fastutil.ints.IntComparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.navigation.ScreenAxis;

@Environment(value=EnvType.CLIENT)
public enum ScreenDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    private final IntComparator coordinateValueComparator = (i, j) -> i == j ? 0 : (this.isBefore(i, j) ? -1 : 1);

    public ScreenAxis getAxis() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1 -> ScreenAxis.VERTICAL;
            case 2, 3 -> ScreenAxis.HORIZONTAL;
        };
    }

    public ScreenDirection getOpposite() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> DOWN;
            case 1 -> UP;
            case 2 -> RIGHT;
            case 3 -> LEFT;
        };
    }

    public boolean isPositive() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 2 -> false;
            case 1, 3 -> true;
        };
    }

    public boolean isAfter(int i, int j) {
        if (this.isPositive()) {
            return i > j;
        }
        return j > i;
    }

    public boolean isBefore(int i, int j) {
        if (this.isPositive()) {
            return i < j;
        }
        return j < i;
    }

    public IntComparator coordinateValueComparator() {
        return this.coordinateValueComparator;
    }
}

