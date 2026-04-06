/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.components;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class MultiLineTextWidget
extends AbstractStringWidget {
    private OptionalInt maxWidth = OptionalInt.empty();
    private OptionalInt maxRows = OptionalInt.empty();
    private final SingleKeyCache<CacheKey, MultiLineLabel> cache = Util.singleKeyCache(cacheKey -> {
        if (cacheKey.maxRows.isPresent()) {
            return MultiLineLabel.create(font, cacheKey.maxWidth, cacheKey.maxRows.getAsInt(), cacheKey.message);
        }
        return MultiLineLabel.create(font, cacheKey.message, cacheKey.maxWidth);
    });
    private boolean centered = false;

    public MultiLineTextWidget(Component component, Font font) {
        this(0, 0, component, font);
    }

    public MultiLineTextWidget(int i, int j, Component component, Font font) {
        super(i, j, 0, 0, component, font);
        this.active = false;
    }

    public MultiLineTextWidget setMaxWidth(int i) {
        this.maxWidth = OptionalInt.of(i);
        return this;
    }

    public MultiLineTextWidget setMaxRows(int i) {
        this.maxRows = OptionalInt.of(i);
        return this;
    }

    public MultiLineTextWidget setCentered(boolean bl) {
        this.centered = bl;
        return this;
    }

    @Override
    public int getWidth() {
        return this.cache.getValue(this.getFreshCacheKey()).getWidth();
    }

    @Override
    public int getHeight() {
        return this.cache.getValue(this.getFreshCacheKey()).getLineCount() * this.getFont().lineHeight;
    }

    @Override
    public void visitLines(ActiveTextCollector activeTextCollector) {
        MultiLineLabel multiLineLabel = this.cache.getValue(this.getFreshCacheKey());
        int i = this.getTextX();
        int j = this.getTextY();
        int k = this.getFont().lineHeight;
        if (this.centered) {
            int l = this.getX() + this.getWidth() / 2;
            multiLineLabel.visitLines(TextAlignment.CENTER, l, j, k, activeTextCollector);
        } else {
            multiLineLabel.visitLines(TextAlignment.LEFT, i, j, k, activeTextCollector);
        }
    }

    protected int getTextX() {
        return this.getX();
    }

    protected int getTextY() {
        return this.getY();
    }

    private CacheKey getFreshCacheKey() {
        return new CacheKey(this.getMessage(), this.maxWidth.orElse(Integer.MAX_VALUE), this.maxRows);
    }

    @Environment(value=EnvType.CLIENT)
    static final class CacheKey
    extends Record {
        final Component message;
        final int maxWidth;
        final OptionalInt maxRows;

        CacheKey(Component component, int i, OptionalInt optionalInt) {
            this.message = component;
            this.maxWidth = i;
            this.maxRows = optionalInt;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheKey.class, "message;maxWidth;maxRows", "message", "maxWidth", "maxRows"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheKey.class, "message;maxWidth;maxRows", "message", "maxWidth", "maxRows"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheKey.class, "message;maxWidth;maxRows", "message", "maxWidth", "maxRows"}, this, object);
        }

        public Component message() {
            return this.message;
        }

        public int maxWidth() {
            return this.maxWidth;
        }

        public OptionalInt maxRows() {
            return this.maxRows;
        }
    }
}

