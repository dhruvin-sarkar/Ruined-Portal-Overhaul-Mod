/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.server.dialog.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.functions.StringTemplate;

public class ParsedTemplate {
    public static final Codec<ParsedTemplate> CODEC = Codec.STRING.comapFlatMap(ParsedTemplate::parse, parsedTemplate -> parsedTemplate.raw);
    public static final Codec<String> VARIABLE_CODEC = Codec.STRING.validate(string -> StringTemplate.isValidVariableName(string) ? DataResult.success((Object)string) : DataResult.error(() -> string + " is not a valid input name"));
    private final String raw;
    private final StringTemplate parsed;

    private ParsedTemplate(String string, StringTemplate stringTemplate) {
        this.raw = string;
        this.parsed = stringTemplate;
    }

    private static DataResult<ParsedTemplate> parse(String string) {
        StringTemplate stringTemplate;
        try {
            stringTemplate = StringTemplate.fromString(string);
        }
        catch (Exception exception) {
            return DataResult.error(() -> "Failed to parse template " + string + ": " + exception.getMessage());
        }
        return DataResult.success((Object)new ParsedTemplate(string, stringTemplate));
    }

    public String instantiate(Map<String, String> map) {
        List list = this.parsed.variables().stream().map(string -> map.getOrDefault(string, "")).toList();
        return this.parsed.substitute(list);
    }
}

