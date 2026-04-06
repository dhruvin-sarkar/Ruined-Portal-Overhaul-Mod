/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector2i
 *  org.joml.Vector2ic
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import org.joml.Vector2i;
import org.joml.Vector2ic;

@Environment(value=EnvType.CLIENT)
public class DefaultTooltipPositioner
implements ClientTooltipPositioner {
    public static final ClientTooltipPositioner INSTANCE = new DefaultTooltipPositioner();

    private DefaultTooltipPositioner() {
    }

    @Override
    public Vector2ic positionTooltip(int i, int j, int k, int l, int m, int n) {
        Vector2i vector2i = new Vector2i(k, l).add(12, -12);
        this.positionTooltip(i, j, vector2i, m, n);
        return vector2i;
    }

    private void positionTooltip(int i, int j, Vector2i vector2i, int k, int l) {
        int m;
        if (vector2i.x + k > i) {
            vector2i.x = Math.max(vector2i.x - 24 - k, 4);
        }
        if (vector2i.y + (m = l + 3) > j) {
            vector2i.y = j - m;
        }
    }
}

