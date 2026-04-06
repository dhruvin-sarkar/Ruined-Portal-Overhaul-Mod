/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;

public record SelectorPattern(String pattern, EntitySelector resolved) {
    public static final Codec<SelectorPattern> CODEC = Codec.STRING.comapFlatMap(SelectorPattern::parse, SelectorPattern::pattern);

    public static DataResult<SelectorPattern> parse(String string) {
        try {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string), true);
            return DataResult.success((Object)((Object)new SelectorPattern(string, entitySelectorParser.parse())));
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return DataResult.error(() -> "Invalid selector component: " + string + ": " + commandSyntaxException.getMessage());
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (!(object instanceof SelectorPattern)) return false;
        SelectorPattern selectorPattern = (SelectorPattern)((Object)object);
        if (!this.pattern.equals(selectorPattern.pattern)) return false;
        return true;
    }

    public int hashCode() {
        return this.pattern.hashCode();
    }

    public String toString() {
        return this.pattern;
    }
}

