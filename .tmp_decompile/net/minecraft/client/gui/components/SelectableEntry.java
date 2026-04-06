/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface SelectableEntry {
    default public boolean mouseOverIcon(int i, int j, int k) {
        return i >= 0 && i < k && j >= 0 && j < k;
    }

    default public boolean mouseOverLeftHalf(int i, int j, int k) {
        return i >= 0 && i < k / 2 && j >= 0 && j < k;
    }

    default public boolean mouseOverRightHalf(int i, int j, int k) {
        return i >= k / 2 && i < k && j >= 0 && j < k;
    }

    default public boolean mouseOverTopRightQuarter(int i, int j, int k) {
        return i >= k / 2 && i < k && j >= 0 && j < k / 2;
    }

    default public boolean mouseOverBottomRightQuarter(int i, int j, int k) {
        return i >= k / 2 && i < k && j >= k / 2 && j < k;
    }

    default public boolean mouseOverTopLeftQuarter(int i, int j, int k) {
        return i >= 0 && i < k / 2 && j >= 0 && j < k / 2;
    }

    default public boolean mouseOverBottomLeftQuarter(int i, int j, int k) {
        return i >= 0 && i < k / 2 && j >= k / 2 && j < k;
    }
}

