/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.layouts;

import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.network.chat.Component;

@Environment(value=EnvType.CLIENT)
public class CommonLayouts {
    private static final int LABEL_SPACING = 4;

    private CommonLayouts() {
    }

    public static Layout labeledElement(Font font, LayoutElement layoutElement, Component component) {
        return CommonLayouts.labeledElement(font, layoutElement, component, layoutSettings -> {});
    }

    public static Layout labeledElement(Font font, LayoutElement layoutElement, Component component, Consumer<LayoutSettings> consumer) {
        LinearLayout linearLayout = LinearLayout.vertical().spacing(4);
        linearLayout.addChild(new StringWidget(component, font));
        linearLayout.addChild(layoutElement, consumer);
        return linearLayout;
    }
}

