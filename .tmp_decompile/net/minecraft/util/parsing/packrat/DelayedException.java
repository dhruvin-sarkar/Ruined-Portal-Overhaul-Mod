/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.util.parsing.packrat;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;

public interface DelayedException<T extends Exception> {
    public T create(String var1, int var2);

    public static DelayedException<CommandSyntaxException> create(SimpleCommandExceptionType simpleCommandExceptionType) {
        return (string, i) -> simpleCommandExceptionType.createWithContext((ImmutableStringReader)StringReaderTerms.createReader(string, i));
    }

    public static DelayedException<CommandSyntaxException> create(DynamicCommandExceptionType dynamicCommandExceptionType, String string) {
        return (string2, i) -> dynamicCommandExceptionType.createWithContext((ImmutableStringReader)StringReaderTerms.createReader(string2, i), (Object)string);
    }
}

