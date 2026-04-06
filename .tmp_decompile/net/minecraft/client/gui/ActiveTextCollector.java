/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 *  org.joml.Vector2f
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui;

import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.font.ActiveArea;
import net.minecraft.client.gui.font.EmptyArea;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.joml.Vector2f;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ActiveTextCollector {
    public static final double PERIOD_PER_SCROLLED_PIXEL = 0.5;
    public static final double MIN_SCROLL_PERIOD = 3.0;

    public Parameters defaultParameters();

    public void defaultParameters(Parameters var1);

    default public void accept(int i, int j, FormattedCharSequence formattedCharSequence) {
        this.accept(TextAlignment.LEFT, i, j, this.defaultParameters(), formattedCharSequence);
    }

    default public void accept(int i, int j, Component component) {
        this.accept(TextAlignment.LEFT, i, j, this.defaultParameters(), component.getVisualOrderText());
    }

    default public void accept(TextAlignment textAlignment, int i, int j, Parameters parameters, Component component) {
        this.accept(textAlignment, i, j, parameters, component.getVisualOrderText());
    }

    public void accept(TextAlignment var1, int var2, int var3, Parameters var4, FormattedCharSequence var5);

    default public void accept(TextAlignment textAlignment, int i, int j, Component component) {
        this.accept(textAlignment, i, j, component.getVisualOrderText());
    }

    default public void accept(TextAlignment textAlignment, int i, int j, FormattedCharSequence formattedCharSequence) {
        this.accept(textAlignment, i, j, this.defaultParameters(), formattedCharSequence);
    }

    public void acceptScrolling(Component var1, int var2, int var3, int var4, int var5, int var6, Parameters var7);

    default public void acceptScrolling(Component component, int i, int j, int k, int l, int m) {
        this.acceptScrolling(component, i, j, k, l, m, this.defaultParameters());
    }

    default public void acceptScrollingWithDefaultCenter(Component component, int i, int j, int k, int l) {
        this.acceptScrolling(component, (i + j) / 2, i, j, k, l);
    }

    default public void defaultScrollingHelper(Component component, int i, int j, int k, int l, int m, int n, int o, Parameters parameters) {
        int p = (l + m - o) / 2 + 1;
        int q = k - j;
        if (n > q) {
            int r = n - q;
            double d = (double)Util.getMillis() / 1000.0;
            double e = Math.max((double)r * 0.5, 3.0);
            double f = Math.sin(1.5707963267948966 * Math.cos(Math.PI * 2 * d / e)) / 2.0 + 0.5;
            double g = Mth.lerp(f, 0.0, (double)r);
            Parameters parameters2 = parameters.withScissor(j, k, l, m);
            this.accept(TextAlignment.LEFT, j - (int)g, p, parameters2, component.getVisualOrderText());
        } else {
            int r = Mth.clamp(i, j + n / 2, k - n / 2);
            this.accept(TextAlignment.CENTER, r, p, component);
        }
    }

    public static void findElementUnderCursor(GuiTextRenderState guiTextRenderState, float f, float g, final Consumer<Style> consumer) {
        ScreenRectangle screenRectangle = guiTextRenderState.bounds();
        if (screenRectangle == null || !screenRectangle.containsPoint((int)f, (int)g)) {
            return;
        }
        Vector2f vector2fc = guiTextRenderState.pose.invert(new Matrix3x2f()).transformPosition(new Vector2f(f, g));
        final float h = vector2fc.x();
        final float i = vector2fc.y();
        guiTextRenderState.ensurePrepared().visit(new Font.GlyphVisitor(){

            @Override
            public void acceptGlyph(TextRenderable.Styled styled) {
                this.acceptActiveArea(styled);
            }

            @Override
            public void acceptEmptyArea(EmptyArea emptyArea) {
                this.acceptActiveArea(emptyArea);
            }

            private void acceptActiveArea(ActiveArea activeArea) {
                if (ActiveTextCollector.isPointInRectangle(h, i, activeArea.activeLeft(), activeArea.activeTop(), activeArea.activeRight(), activeArea.activeBottom())) {
                    consumer.accept(activeArea.style());
                }
            }
        });
    }

    public static boolean isPointInRectangle(float f, float g, float h, float i, float j, float k) {
        return f >= h && f < j && g >= i && g < k;
    }

    @Environment(value=EnvType.CLIENT)
    public record Parameters(Matrix3x2fc pose, float opacity, @Nullable ScreenRectangle scissor) {
        public Parameters(Matrix3x2fc matrix3x2fc) {
            this(matrix3x2fc, 1.0f, null);
        }

        public Parameters withPose(Matrix3x2fc matrix3x2fc) {
            return new Parameters(matrix3x2fc, this.opacity, this.scissor);
        }

        public Parameters withScale(float f) {
            return this.withPose((Matrix3x2fc)this.pose.scale(f, f, new Matrix3x2f()));
        }

        public Parameters withOpacity(float f) {
            if (this.opacity == f) {
                return this;
            }
            return new Parameters(this.pose, f, this.scissor);
        }

        public Parameters withScissor(ScreenRectangle screenRectangle) {
            if (screenRectangle.equals((Object)this.scissor)) {
                return this;
            }
            return new Parameters(this.pose, this.opacity, screenRectangle);
        }

        public Parameters withScissor(int i, int j, int k, int l) {
            ScreenRectangle screenRectangle = new ScreenRectangle(i, k, j - i, l - k).transformAxisAligned(this.pose);
            if (this.scissor != null) {
                screenRectangle = (ScreenRectangle)((Object)Objects.requireNonNullElse((Object)((Object)this.scissor.intersection(screenRectangle)), (Object)((Object)ScreenRectangle.empty())));
            }
            return this.withScissor(screenRectangle);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ClickableStyleFinder
    implements ActiveTextCollector {
        private static final Parameters INITIAL = new Parameters((Matrix3x2fc)new Matrix3x2f());
        private final Font font;
        private final int testX;
        private final int testY;
        private Parameters defaultParameters = INITIAL;
        private boolean includeInsertions;
        private @Nullable Style result;
        private final Consumer<Style> styleScanner = style -> {
            if (style.getClickEvent() != null || this.includeInsertions && style.getInsertion() != null) {
                this.result = style;
            }
        };

        public ClickableStyleFinder(Font font, int i, int j) {
            this.font = font;
            this.testX = i;
            this.testY = j;
        }

        @Override
        public Parameters defaultParameters() {
            return this.defaultParameters;
        }

        @Override
        public void defaultParameters(Parameters parameters) {
            this.defaultParameters = parameters;
        }

        @Override
        public void accept(TextAlignment textAlignment, int i, int j, Parameters parameters, FormattedCharSequence formattedCharSequence) {
            int k = textAlignment.calculateLeft(i, this.font, formattedCharSequence);
            GuiTextRenderState guiTextRenderState = new GuiTextRenderState(this.font, formattedCharSequence, parameters.pose(), k, j, ARGB.white(parameters.opacity()), 0, true, true, parameters.scissor());
            ActiveTextCollector.findElementUnderCursor(guiTextRenderState, this.testX, this.testY, this.styleScanner);
        }

        @Override
        public void acceptScrolling(Component component, int i, int j, int k, int l, int m, Parameters parameters) {
            int n = this.font.width(component);
            int o = this.font.lineHeight;
            this.defaultScrollingHelper(component, i, j, k, l, m, n, o, parameters);
        }

        public ClickableStyleFinder includeInsertions(boolean bl) {
            this.includeInsertions = bl;
            return this;
        }

        public @Nullable Style result() {
            return this.result;
        }
    }
}

