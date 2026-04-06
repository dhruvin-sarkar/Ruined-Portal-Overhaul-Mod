/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

public final class MutableComponent
implements Component {
    private final ComponentContents contents;
    private final List<Component> siblings;
    private Style style;
    private FormattedCharSequence visualOrderText = FormattedCharSequence.EMPTY;
    private @Nullable Language decomposedWith;

    MutableComponent(ComponentContents componentContents, List<Component> list, Style style) {
        this.contents = componentContents;
        this.siblings = list;
        this.style = style;
    }

    public static MutableComponent create(ComponentContents componentContents) {
        return new MutableComponent(componentContents, Lists.newArrayList(), Style.EMPTY);
    }

    @Override
    public ComponentContents getContents() {
        return this.contents;
    }

    @Override
    public List<Component> getSiblings() {
        return this.siblings;
    }

    public MutableComponent setStyle(Style style) {
        this.style = style;
        return this;
    }

    @Override
    public Style getStyle() {
        return this.style;
    }

    public MutableComponent append(String string) {
        if (string.isEmpty()) {
            return this;
        }
        return this.append(Component.literal(string));
    }

    public MutableComponent append(Component component) {
        this.siblings.add(component);
        return this;
    }

    public MutableComponent withStyle(UnaryOperator<Style> unaryOperator) {
        this.setStyle((Style)unaryOperator.apply(this.getStyle()));
        return this;
    }

    public MutableComponent withStyle(Style style) {
        this.setStyle(style.applyTo(this.getStyle()));
        return this;
    }

    public MutableComponent withStyle(ChatFormatting ... chatFormattings) {
        this.setStyle(this.getStyle().applyFormats(chatFormattings));
        return this;
    }

    public MutableComponent withStyle(ChatFormatting chatFormatting) {
        this.setStyle(this.getStyle().applyFormat(chatFormatting));
        return this;
    }

    public MutableComponent withColor(int i) {
        this.setStyle(this.getStyle().withColor(i));
        return this;
    }

    public MutableComponent withoutShadow() {
        this.setStyle(this.getStyle().withoutShadow());
        return this;
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        Language language = Language.getInstance();
        if (this.decomposedWith != language) {
            this.visualOrderText = language.getVisualOrder(this);
            this.decomposedWith = language;
        }
        return this.visualOrderText;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MutableComponent)) return false;
        MutableComponent mutableComponent = (MutableComponent)object;
        if (!this.contents.equals(mutableComponent.contents)) return false;
        if (!this.style.equals(mutableComponent.style)) return false;
        if (!this.siblings.equals(mutableComponent.siblings)) return false;
        return true;
    }

    public int hashCode() {
        int i = 1;
        i = 31 * i + this.contents.hashCode();
        i = 31 * i + this.style.hashCode();
        i = 31 * i + this.siblings.hashCode();
        return i;
    }

    public String toString() {
        boolean bl2;
        StringBuilder stringBuilder = new StringBuilder(this.contents.toString());
        boolean bl = !this.style.isEmpty();
        boolean bl3 = bl2 = !this.siblings.isEmpty();
        if (bl || bl2) {
            stringBuilder.append('[');
            if (bl) {
                stringBuilder.append("style=");
                stringBuilder.append(this.style);
            }
            if (bl && bl2) {
                stringBuilder.append(", ");
            }
            if (bl2) {
                stringBuilder.append("siblings=");
                stringBuilder.append(this.siblings);
            }
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }
}

