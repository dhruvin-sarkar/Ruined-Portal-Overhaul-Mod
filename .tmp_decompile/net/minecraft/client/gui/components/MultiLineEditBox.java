/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.components;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MultiLineEditBox
extends AbstractTextAreaWidget {
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int PLACEHOLDER_TEXT_COLOR = ARGB.color(204, -2039584);
    private static final int CURSOR_BLINK_INTERVAL_MS = 300;
    private final Font font;
    private final Component placeholder;
    private final MultilineTextField textField;
    private final int textColor;
    private final boolean textShadow;
    private final int cursorColor;
    private long focusedTime = Util.getMillis();

    MultiLineEditBox(Font font, int i, int j, int k, int l, Component component, Component component2, int m, boolean bl, int n, boolean bl2, boolean bl3) {
        super(i, j, k, l, component2, bl2, bl3);
        this.font = font;
        this.textShadow = bl;
        this.textColor = m;
        this.cursorColor = n;
        this.placeholder = component;
        this.textField = new MultilineTextField(font, k - this.totalInnerPadding());
        this.textField.setCursorListener(this::scrollToCursor);
    }

    public void setCharacterLimit(int i) {
        this.textField.setCharacterLimit(i);
    }

    public void setLineLimit(int i) {
        this.textField.setLineLimit(i);
    }

    public void setValueListener(Consumer<String> consumer) {
        this.textField.setValueListener(consumer);
    }

    public void setValue(String string) {
        this.setValue(string, false);
    }

    public void setValue(String string, boolean bl) {
        this.textField.setValue(string, bl);
    }

    public String getValue() {
        return this.textField.value();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bl) {
        if (bl) {
            this.textField.selectWordAtCursor();
        } else {
            this.textField.setSelecting(mouseButtonEvent.hasShiftDown());
            this.seekCursorScreen(mouseButtonEvent.x(), mouseButtonEvent.y());
        }
    }

    @Override
    protected void onDrag(MouseButtonEvent mouseButtonEvent, double d, double e) {
        this.textField.setSelecting(true);
        this.seekCursorScreen(mouseButtonEvent.x(), mouseButtonEvent.y());
        this.textField.setSelecting(mouseButtonEvent.hasShiftDown());
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return this.textField.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        if (!(this.visible && this.isFocused() && characterEvent.isAllowedChatCharacter())) {
            return false;
        }
        this.textField.insertText(characterEvent.codepointAsString());
        return true;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        String string = this.textField.value();
        if (string.isEmpty() && !this.isFocused()) {
            guiGraphics.drawWordWrap(this.font, this.placeholder, this.getInnerLeft(), this.getInnerTop(), this.width - this.totalInnerPadding(), PLACEHOLDER_TEXT_COLOR);
            return;
        }
        int k = this.textField.cursor();
        boolean bl = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L;
        boolean bl2 = k < string.length();
        int l = 0;
        int m = 0;
        int n = this.getInnerTop();
        boolean bl3 = false;
        for (MultilineTextField.StringView stringView : this.textField.iterateLines()) {
            boolean bl4 = this.withinContentAreaTopBottom(n, n + this.font.lineHeight);
            int o = this.getInnerLeft();
            if (bl && bl2 && k >= stringView.beginIndex() && k <= stringView.endIndex()) {
                if (bl4) {
                    string2 = string.substring(stringView.beginIndex(), k);
                    guiGraphics.drawString(this.font, string2, o, n, this.textColor, this.textShadow);
                    l = o + this.font.width(string2);
                    if (!bl3) {
                        guiGraphics.fill(l, n - 1, l + 1, n + 1 + this.font.lineHeight, this.cursorColor);
                        bl3 = true;
                    }
                    guiGraphics.drawString(this.font, string.substring(k, stringView.endIndex()), l, n, this.textColor, this.textShadow);
                }
            } else {
                if (bl4) {
                    string2 = string.substring(stringView.beginIndex(), stringView.endIndex());
                    guiGraphics.drawString(this.font, string2, o, n, this.textColor, this.textShadow);
                    l = o + this.font.width(string2) - 1;
                }
                m = n;
            }
            n += this.font.lineHeight;
        }
        if (bl && !bl2 && this.withinContentAreaTopBottom(m, m + this.font.lineHeight)) {
            guiGraphics.drawString(this.font, CURSOR_APPEND_CHARACTER, l + 1, m, this.cursorColor, this.textShadow);
        }
        if (this.textField.hasSelection()) {
            MultilineTextField.StringView stringView2 = this.textField.getSelected();
            int p = this.getInnerLeft();
            n = this.getInnerTop();
            for (MultilineTextField.StringView stringView3 : this.textField.iterateLines()) {
                if (stringView2.beginIndex() > stringView3.endIndex()) {
                    n += this.font.lineHeight;
                    continue;
                }
                if (stringView3.beginIndex() > stringView2.endIndex()) break;
                if (this.withinContentAreaTopBottom(n, n + this.font.lineHeight)) {
                    int q = this.font.width(string.substring(stringView3.beginIndex(), Math.max(stringView2.beginIndex(), stringView3.beginIndex())));
                    int r = stringView2.endIndex() > stringView3.endIndex() ? this.width - this.innerPadding() : this.font.width(string.substring(stringView3.beginIndex(), stringView2.endIndex()));
                    guiGraphics.textHighlight(p + q, n, p + r, n + this.font.lineHeight, true);
                }
                n += this.font.lineHeight;
            }
        }
        if (this.isHovered()) {
            guiGraphics.requestCursor(CursorTypes.IBEAM);
        }
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics) {
        super.renderDecorations(guiGraphics);
        if (this.textField.hasCharacterLimit()) {
            int i = this.textField.characterLimit();
            MutableComponent component = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), i);
            guiGraphics.drawString(this.font, component, this.getX() + this.width - this.font.width(component), this.getY() + this.height + 4, -6250336);
        }
    }

    @Override
    public int getInnerHeight() {
        return this.font.lineHeight * this.textField.getLineCount();
    }

    @Override
    protected double scrollRate() {
        return (double)this.font.lineHeight / 2.0;
    }

    private void scrollToCursor() {
        double d = this.scrollAmount();
        MultilineTextField.StringView stringView = this.textField.getLineView((int)(d / (double)this.font.lineHeight));
        if (this.textField.cursor() <= stringView.beginIndex()) {
            d = this.textField.getLineAtCursor() * this.font.lineHeight;
        } else {
            MultilineTextField.StringView stringView2 = this.textField.getLineView((int)((d + (double)this.height) / (double)this.font.lineHeight) - 1);
            if (this.textField.cursor() > stringView2.endIndex()) {
                d = this.textField.getLineAtCursor() * this.font.lineHeight - this.height + this.font.lineHeight + this.totalInnerPadding();
            }
        }
        this.setScrollAmount(d);
    }

    private void seekCursorScreen(double d, double e) {
        double f = d - (double)this.getX() - (double)this.innerPadding();
        double g = e - (double)this.getY() - (double)this.innerPadding() + this.scrollAmount();
        this.textField.seekCursorToPoint(f, g);
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        if (bl) {
            this.focusedTime = Util.getMillis();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private int x;
        private int y;
        private Component placeholder = CommonComponents.EMPTY;
        private int textColor = -2039584;
        private boolean textShadow = true;
        private int cursorColor = -3092272;
        private boolean showBackground = true;
        private boolean showDecorations = true;

        public Builder setX(int i) {
            this.x = i;
            return this;
        }

        public Builder setY(int i) {
            this.y = i;
            return this;
        }

        public Builder setPlaceholder(Component component) {
            this.placeholder = component;
            return this;
        }

        public Builder setTextColor(int i) {
            this.textColor = i;
            return this;
        }

        public Builder setTextShadow(boolean bl) {
            this.textShadow = bl;
            return this;
        }

        public Builder setCursorColor(int i) {
            this.cursorColor = i;
            return this;
        }

        public Builder setShowBackground(boolean bl) {
            this.showBackground = bl;
            return this;
        }

        public Builder setShowDecorations(boolean bl) {
            this.showDecorations = bl;
            return this;
        }

        public MultiLineEditBox build(Font font, int i, int j, Component component) {
            return new MultiLineEditBox(font, this.x, this.y, i, j, this.placeholder, component, this.textColor, this.textShadow, this.cursorColor, this.showBackground, this.showDecorations);
        }
    }
}

