/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.collect.Sets
 */
package net.minecraft.util.context;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import net.minecraft.util.context.ContextKey;

public class ContextKeySet {
    private final Set<ContextKey<?>> required;
    private final Set<ContextKey<?>> allowed;

    ContextKeySet(Set<ContextKey<?>> set, Set<ContextKey<?>> set2) {
        this.required = Set.copyOf(set);
        this.allowed = Set.copyOf((Collection)Sets.union(set, set2));
    }

    public Set<ContextKey<?>> required() {
        return this.required;
    }

    public Set<ContextKey<?>> allowed() {
        return this.allowed;
    }

    public String toString() {
        return "[" + Joiner.on((String)", ").join(this.allowed.stream().map(contextKey -> (this.required.contains(contextKey) ? "!" : "") + String.valueOf(contextKey.name())).iterator()) + "]";
    }

    public static class Builder {
        private final Set<ContextKey<?>> required = Sets.newIdentityHashSet();
        private final Set<ContextKey<?>> optional = Sets.newIdentityHashSet();

        public Builder required(ContextKey<?> contextKey) {
            if (this.optional.contains(contextKey)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(contextKey.name()) + " is already optional");
            }
            this.required.add(contextKey);
            return this;
        }

        public Builder optional(ContextKey<?> contextKey) {
            if (this.required.contains(contextKey)) {
                throw new IllegalArgumentException("Parameter " + String.valueOf(contextKey.name()) + " is already required");
            }
            this.optional.add(contextKey);
            return this;
        }

        public ContextKeySet build() {
            return new ContextKeySet(this.required, this.optional);
        }
    }
}

