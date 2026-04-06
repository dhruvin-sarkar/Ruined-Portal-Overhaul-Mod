/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.parsing.packrat.ErrorEntry;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import org.jspecify.annotations.Nullable;

public interface ErrorCollector<S> {
    public void store(int var1, SuggestionSupplier<S> var2, Object var3);

    default public void store(int i, Object object) {
        this.store(i, SuggestionSupplier.empty(), object);
    }

    public void finish(int var1);

    public static class LongestOnly<S>
    implements ErrorCollector<S> {
        private @Nullable MutableErrorEntry<S>[] entries = new MutableErrorEntry[16];
        private int nextErrorEntry;
        private int lastCursor = -1;

        private void discardErrorsFromShorterParse(int i) {
            if (i > this.lastCursor) {
                this.lastCursor = i;
                this.nextErrorEntry = 0;
            }
        }

        @Override
        public void finish(int i) {
            this.discardErrorsFromShorterParse(i);
        }

        @Override
        public void store(int i, SuggestionSupplier<S> suggestionSupplier, Object object) {
            this.discardErrorsFromShorterParse(i);
            if (i == this.lastCursor) {
                this.addErrorEntry(suggestionSupplier, object);
            }
        }

        private void addErrorEntry(SuggestionSupplier<S> suggestionSupplier, Object object) {
            MutableErrorEntry<S> mutableErrorEntry;
            int j;
            int i = this.entries.length;
            if (this.nextErrorEntry >= i) {
                j = Util.growByHalf(i, this.nextErrorEntry + 1);
                MutableErrorEntry[] mutableErrorEntrys = new MutableErrorEntry[j];
                System.arraycopy(this.entries, 0, mutableErrorEntrys, 0, i);
                this.entries = mutableErrorEntrys;
            }
            if ((mutableErrorEntry = this.entries[j = this.nextErrorEntry++]) == null) {
                this.entries[j] = mutableErrorEntry = new MutableErrorEntry();
            }
            mutableErrorEntry.suggestions = suggestionSupplier;
            mutableErrorEntry.reason = object;
        }

        public List<ErrorEntry<S>> entries() {
            int i = this.nextErrorEntry;
            if (i == 0) {
                return List.of();
            }
            ArrayList<ErrorEntry<S>> list = new ArrayList<ErrorEntry<S>>(i);
            for (int j = 0; j < i; ++j) {
                MutableErrorEntry<S> mutableErrorEntry = this.entries[j];
                list.add(new ErrorEntry(this.lastCursor, mutableErrorEntry.suggestions, mutableErrorEntry.reason));
            }
            return list;
        }

        public int cursor() {
            return this.lastCursor;
        }

        static class MutableErrorEntry<S> {
            SuggestionSupplier<S> suggestions = SuggestionSupplier.empty();
            Object reason = "empty";

            MutableErrorEntry() {
            }
        }
    }

    public static class Nop<S>
    implements ErrorCollector<S> {
        @Override
        public void store(int i, SuggestionSupplier<S> suggestionSupplier, Object object) {
        }

        @Override
        public void finish(int i) {
        }
    }
}

