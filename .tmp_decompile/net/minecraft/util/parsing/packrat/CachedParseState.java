/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import org.jspecify.annotations.Nullable;

public abstract class CachedParseState<S>
implements ParseState<S> {
    private @Nullable PositionCache[] positionCache = new PositionCache[256];
    private final ErrorCollector<S> errorCollector;
    private final Scope scope = new Scope();
    private @Nullable SimpleControl[] controlCache = new SimpleControl[16];
    private int nextControlToReturn;
    private final Silent silent = new Silent();

    protected CachedParseState(ErrorCollector<S> errorCollector) {
        this.errorCollector = errorCollector;
    }

    @Override
    public Scope scope() {
        return this.scope;
    }

    @Override
    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    @Override
    public <T> @Nullable T parse(NamedRule<S, T> namedRule) {
        CacheEntry cacheEntry2;
        T object;
        int i = this.mark();
        PositionCache positionCache = this.getCacheForPosition(i);
        int j = positionCache.findKeyIndex(namedRule.name());
        if (j != -1) {
            CacheEntry cacheEntry = positionCache.getValue(j);
            if (cacheEntry != null) {
                if (cacheEntry == CacheEntry.NEGATIVE) {
                    return null;
                }
                this.restore(cacheEntry.markAfterParse);
                return cacheEntry.value;
            }
        } else {
            j = positionCache.allocateNewEntry(namedRule.name());
        }
        if ((object = namedRule.value().parse(this)) == null) {
            cacheEntry2 = CacheEntry.negativeEntry();
        } else {
            int k = this.mark();
            cacheEntry2 = new CacheEntry<T>(object, k);
        }
        positionCache.setValue(j, cacheEntry2);
        return object;
    }

    private PositionCache getCacheForPosition(int i) {
        PositionCache positionCache;
        int j = this.positionCache.length;
        if (i >= j) {
            int k = Util.growByHalf(j, i + 1);
            PositionCache[] positionCaches = new PositionCache[k];
            System.arraycopy(this.positionCache, 0, positionCaches, 0, j);
            this.positionCache = positionCaches;
        }
        if ((positionCache = this.positionCache[i]) == null) {
            this.positionCache[i] = positionCache = new PositionCache();
        }
        return positionCache;
    }

    @Override
    public Control acquireControl() {
        SimpleControl simpleControl;
        int j;
        int i = this.controlCache.length;
        if (this.nextControlToReturn >= i) {
            j = Util.growByHalf(i, this.nextControlToReturn + 1);
            SimpleControl[] simpleControls = new SimpleControl[j];
            System.arraycopy(this.controlCache, 0, simpleControls, 0, i);
            this.controlCache = simpleControls;
        }
        if ((simpleControl = this.controlCache[j = this.nextControlToReturn++]) == null) {
            this.controlCache[j] = simpleControl = new SimpleControl();
        } else {
            simpleControl.reset();
        }
        return simpleControl;
    }

    @Override
    public void releaseControl() {
        --this.nextControlToReturn;
    }

    @Override
    public ParseState<S> silent() {
        return this.silent;
    }

    static class PositionCache {
        public static final int ENTRY_STRIDE = 2;
        private static final int NOT_FOUND = -1;
        private Object[] atomCache = new Object[16];
        private int nextKey;

        PositionCache() {
        }

        public int findKeyIndex(Atom<?> atom) {
            for (int i = 0; i < this.nextKey; i += 2) {
                if (this.atomCache[i] != atom) continue;
                return i;
            }
            return -1;
        }

        public int allocateNewEntry(Atom<?> atom) {
            int i = this.nextKey;
            this.nextKey += 2;
            int j = i + 1;
            int k = this.atomCache.length;
            if (j >= k) {
                int l = Util.growByHalf(k, j + 1);
                Object[] objects = new Object[l];
                System.arraycopy(this.atomCache, 0, objects, 0, k);
                this.atomCache = objects;
            }
            this.atomCache[i] = atom;
            return i;
        }

        public <T> @Nullable CacheEntry<T> getValue(int i) {
            return (CacheEntry)((Object)this.atomCache[i + 1]);
        }

        public void setValue(int i, CacheEntry<?> cacheEntry) {
            this.atomCache[i + 1] = cacheEntry;
        }
    }

    static class SimpleControl
    implements Control {
        private boolean hasCut;

        SimpleControl() {
        }

        @Override
        public void cut() {
            this.hasCut = true;
        }

        @Override
        public boolean hasCut() {
            return this.hasCut;
        }

        public void reset() {
            this.hasCut = false;
        }
    }

    class Silent
    implements ParseState<S> {
        private final ErrorCollector<S> silentCollector = new ErrorCollector.Nop();

        Silent() {
        }

        @Override
        public ErrorCollector<S> errorCollector() {
            return this.silentCollector;
        }

        @Override
        public Scope scope() {
            return CachedParseState.this.scope();
        }

        @Override
        public <T> @Nullable T parse(NamedRule<S, T> namedRule) {
            return CachedParseState.this.parse(namedRule);
        }

        @Override
        public S input() {
            return CachedParseState.this.input();
        }

        @Override
        public int mark() {
            return CachedParseState.this.mark();
        }

        @Override
        public void restore(int i) {
            CachedParseState.this.restore(i);
        }

        @Override
        public Control acquireControl() {
            return CachedParseState.this.acquireControl();
        }

        @Override
        public void releaseControl() {
            CachedParseState.this.releaseControl();
        }

        @Override
        public ParseState<S> silent() {
            return this;
        }
    }

    static final class CacheEntry<T>
    extends Record {
        final @Nullable T value;
        final int markAfterParse;
        public static final CacheEntry<?> NEGATIVE = new CacheEntry<Object>(null, -1);

        CacheEntry(@Nullable T object, int i) {
            this.value = object;
            this.markAfterParse = i;
        }

        public static <T> CacheEntry<T> negativeEntry() {
            return NEGATIVE;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this, object);
        }

        public @Nullable T value() {
            return this.value;
        }

        public int markAfterParse() {
            return this.markAfterParse;
        }
    }
}

