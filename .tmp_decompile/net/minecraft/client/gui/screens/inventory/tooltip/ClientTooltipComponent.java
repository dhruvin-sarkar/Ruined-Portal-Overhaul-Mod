/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientActivePlayersTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientBundleTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.BundleTooltip;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

@Environment(value=EnvType.CLIENT)
public interface ClientTooltipComponent {
    public static ClientTooltipComponent create(FormattedCharSequence formattedCharSequence) {
        return new ClientTextTooltip(formattedCharSequence);
    }

    public static ClientTooltipComponent create(TooltipComponent tooltipComponent) {
        TooltipComponent tooltipComponent2 = tooltipComponent;
        Objects.requireNonNull(tooltipComponent2);
        TooltipComponent tooltipComponent3 = tooltipComponent2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{BundleTooltip.class, ClientActivePlayersTooltip.ActivePlayersTooltip.class}, (Object)tooltipComponent3, (int)n)) {
            case 0 -> {
                BundleTooltip bundleTooltip = (BundleTooltip)tooltipComponent3;
                yield new ClientBundleTooltip(bundleTooltip.contents());
            }
            case 1 -> {
                ClientActivePlayersTooltip.ActivePlayersTooltip activePlayersTooltip = (ClientActivePlayersTooltip.ActivePlayersTooltip)tooltipComponent3;
                yield new ClientActivePlayersTooltip(activePlayersTooltip);
            }
            default -> throw new IllegalArgumentException("Unknown TooltipComponent");
        };
    }

    public int getHeight(Font var1);

    public int getWidth(Font var1);

    default public boolean showTooltipWithItemInHand() {
        return false;
    }

    default public void renderText(GuiGraphics guiGraphics, Font font, int i, int j) {
    }

    default public void renderImage(Font font, int i, int j, int k, int l, GuiGraphics guiGraphics) {
    }
}

