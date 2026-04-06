/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.context;

import net.minecraft.resources.Identifier;

public class ContextKey<T> {
    private final Identifier name;

    public ContextKey(Identifier identifier) {
        this.name = identifier;
    }

    public static <T> ContextKey<T> vanilla(String string) {
        return new ContextKey<T>(Identifier.withDefaultNamespace(string));
    }

    public Identifier name() {
        return this.name;
    }

    public String toString() {
        return "<parameter " + String.valueOf(this.name) + ">";
    }
}

