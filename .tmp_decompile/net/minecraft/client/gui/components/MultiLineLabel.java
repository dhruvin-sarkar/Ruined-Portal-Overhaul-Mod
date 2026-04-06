/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface MultiLineLabel {
    public static final MultiLineLabel EMPTY = new MultiLineLabel(){

        @Override
        public int visitLines(TextAlignment textAlignment, int i, int j, int k, ActiveTextCollector activeTextCollector) {
            return j;
        }

        @Override
        public int getLineCount() {
            return 0;
        }

        @Override
        public int getWidth() {
            return 0;
        }
    };

    public static MultiLineLabel create(Font font, Component ... components) {
        return MultiLineLabel.create(font, Integer.MAX_VALUE, Integer.MAX_VALUE, components);
    }

    public static MultiLineLabel create(Font font, int i, Component ... components) {
        return MultiLineLabel.create(font, i, Integer.MAX_VALUE, components);
    }

    public static MultiLineLabel create(Font font, Component component, int i) {
        return MultiLineLabel.create(font, i, Integer.MAX_VALUE, component);
    }

    public static MultiLineLabel create(final Font font, final int i, final int j, final Component ... components) {
        if (components.length == 0) {
            return EMPTY;
        }
        return new MultiLineLabel(){
            private @Nullable List<TextAndWidth> cachedTextAndWidth;
            private @Nullable Language splitWithLanguage;

            @Override
            public int visitLines(TextAlignment textAlignment, int i2, int j2, int k, ActiveTextCollector activeTextCollector) {
                int l = j2;
                for (TextAndWidth textAndWidth : this.getSplitMessage()) {
                    int m = textAlignment.calculateLeft(i2, textAndWidth.width);
                    activeTextCollector.accept(m, l, textAndWidth.text);
                    l += k;
                }
                return l;
            }

            private List<TextAndWidth> getSplitMessage() {
                Language language = Language.getInstance();
                if (this.cachedTextAndWidth != null && language == this.splitWithLanguage) {
                    return this.cachedTextAndWidth;
                }
                this.splitWithLanguage = language;
                ArrayList<FormattedText> list = new ArrayList<FormattedText>();
                for (Component component : components) {
                    list.addAll(font.splitIgnoringLanguage(component, i));
                }
                this.cachedTextAndWidth = new ArrayList<TextAndWidth>();
                int i2 = Math.min(list.size(), j);
                List list2 = list.subList(0, i2);
                for (int j2 = 0; j2 < list2.size(); ++j2) {
                    FormattedText formattedText = (FormattedText)list2.get(j2);
                    FormattedCharSequence formattedCharSequence = Language.getInstance().getVisualOrder(formattedText);
                    if (j2 == list2.size() - 1 && i2 == j && i2 != list.size()) {
                        FormattedText formattedText2 = font.substrByWidth(formattedText, font.width(formattedText) - font.width(CommonComponents.ELLIPSIS));
                        FormattedText formattedText3 = FormattedText.composite(formattedText2, CommonComponents.ELLIPSIS.copy().withStyle(components[components.length - 1].getStyle()));
                        this.cachedTextAndWidth.add(new TextAndWidth(Language.getInstance().getVisualOrder(formattedText3), font.width(formattedText3)));
                        continue;
                    }
                    this.cachedTextAndWidth.add(new TextAndWidth(formattedCharSequence, font.width(formattedCharSequence)));
                }
                return this.cachedTextAndWidth;
            }

            @Override
            public int getLineCount() {
                return this.getSplitMessage().size();
            }

            @Override
            public int getWidth() {
                return Math.min(i, this.getSplitMessage().stream().mapToInt(TextAndWidth::width).max().orElse(0));
            }
        };
    }

    public int visitLines(TextAlignment var1, int var2, int var3, int var4, ActiveTextCollector var5);

    public int getLineCount();

    public int getWidth();

    @Environment(value=EnvType.CLIENT)
    public static final class TextAndWidth
    extends Record {
        final FormattedCharSequence text;
        final int width;

        public TextAndWidth(FormattedCharSequence formattedCharSequence, int i) {
            this.text = formattedCharSequence;
            this.width = i;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TextAndWidth.class, "text;width", "text", "width"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TextAndWidth.class, "text;width", "text", "width"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TextAndWidth.class, "text;width", "text", "width"}, this, object);
        }

        public FormattedCharSequence text() {
            return this.text;
        }

        public int width() {
            return this.width;
        }
    }
}

