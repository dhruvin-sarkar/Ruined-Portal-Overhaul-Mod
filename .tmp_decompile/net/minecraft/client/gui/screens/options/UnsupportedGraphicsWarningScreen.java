/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.options;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;

@Environment(value=EnvType.CLIENT)
public class UnsupportedGraphicsWarningScreen
extends Screen {
    private static final int BUTTON_PADDING = 20;
    private static final int BUTTON_MARGIN = 5;
    private static final int BUTTON_HEIGHT = 20;
    private final Component narrationMessage;
    private final List<Component> message;
    private final ImmutableList<ButtonOption> buttonOptions;
    private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
    private int contentTop;
    private int buttonWidth;

    protected UnsupportedGraphicsWarningScreen(Component component, List<Component> list, ImmutableList<ButtonOption> immutableList) {
        super(component);
        this.message = list;
        this.narrationMessage = CommonComponents.joinForNarration(component, ComponentUtils.formatList(list, CommonComponents.EMPTY));
        this.buttonOptions = immutableList;
    }

    @Override
    public Component getNarrationMessage() {
        return this.narrationMessage;
    }

    @Override
    public void init() {
        for (ButtonOption buttonOption : this.buttonOptions) {
            this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(buttonOption.message) + 20);
        }
        int i = 5 + this.buttonWidth + 5;
        int j = i * this.buttonOptions.size();
        this.messageLines = MultiLineLabel.create(this.font, j, this.message.toArray(new Component[0]));
        int k = this.messageLines.getLineCount() * this.font.lineHeight;
        this.contentTop = (int)((double)this.height / 2.0 - (double)k / 2.0);
        int l = this.contentTop + k + this.font.lineHeight * 2;
        int m = (int)((double)this.width / 2.0 - (double)j / 2.0);
        for (ButtonOption buttonOption2 : this.buttonOptions) {
            this.addRenderableWidget(Button.builder(buttonOption2.message, buttonOption2.onPress).bounds(m, l, this.buttonWidth, 20).build());
            m += i;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        ActiveTextCollector activeTextCollector = guiGraphics.textRenderer();
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.contentTop - this.font.lineHeight * 2, -1);
        this.messageLines.visitLines(TextAlignment.CENTER, this.width / 2, this.contentTop, this.font.lineHeight, activeTextCollector);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    public static final class ButtonOption {
        final Component message;
        final Button.OnPress onPress;

        public ButtonOption(Component component, Button.OnPress onPress) {
            this.message = component;
            this.onPress = onPress;
        }
    }
}

