/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.primitives.UnsignedBytes
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.chars.CharList
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  java.lang.runtime.SwitchBootstraps
 *  java.util.HexFormat
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.google.common.collect.ImmutableMap;
import com.google.common.primitives.UnsignedBytes;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.chars.CharList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.nbt.SnbtOperations;
import net.minecraft.network.chat.Component;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.GreedyPatternParseRule;
import net.minecraft.util.parsing.packrat.commands.GreedyPredicateParseRule;
import net.minecraft.util.parsing.packrat.commands.NumberRunParseRule;
import net.minecraft.util.parsing.packrat.commands.StringReaderTerms;
import net.minecraft.util.parsing.packrat.commands.UnquotedStringParseRule;
import org.jspecify.annotations.Nullable;

public class SnbtGrammar {
    private static final DynamicCommandExceptionType ERROR_NUMBER_PARSE_FAILURE = new DynamicCommandExceptionType(object -> Component.translatableEscape("snbt.parser.number_parse_failure", object));
    static final DynamicCommandExceptionType ERROR_EXPECTED_HEX_ESCAPE = new DynamicCommandExceptionType(object -> Component.translatableEscape("snbt.parser.expected_hex_escape", object));
    private static final DynamicCommandExceptionType ERROR_INVALID_CODEPOINT = new DynamicCommandExceptionType(object -> Component.translatableEscape("snbt.parser.invalid_codepoint", object));
    private static final DynamicCommandExceptionType ERROR_NO_SUCH_OPERATION = new DynamicCommandExceptionType(object -> Component.translatableEscape("snbt.parser.no_such_operation", object));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_INTEGER_TYPE = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_integer_type")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_FLOAT_TYPE = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_float_type")));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NON_NEGATIVE_NUMBER = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_non_negative_number")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_CHARACTER_NAME = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_character_name")));
    static final DelayedException<CommandSyntaxException> ERROR_INVALID_ARRAY_ELEMENT_TYPE = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_array_element_type")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_UNQUOTED_START = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_unquoted_start")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_UNQUOTED_STRING = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_unquoted_string")));
    private static final DelayedException<CommandSyntaxException> ERROR_INVALID_STRING_CONTENTS = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.invalid_string_contents")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_BINARY_NUMERAL = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_binary_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_UNDESCORE_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.underscore_not_allowed")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_DECIMAL_NUMERAL = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_decimal_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_HEX_NUMERAL = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_hex_numeral")));
    private static final DelayedException<CommandSyntaxException> ERROR_EMPTY_KEY = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.empty_key")));
    private static final DelayedException<CommandSyntaxException> ERROR_LEADING_ZERO_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.leading_zero_not_allowed")));
    private static final DelayedException<CommandSyntaxException> ERROR_INFINITY_NOT_ALLOWED = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.infinity_not_allowed")));
    private static final HexFormat HEX_ESCAPE = HexFormat.of().withUpperCase();
    private static final NumberRunParseRule BINARY_NUMERAL = new NumberRunParseRule((DelayedException)ERROR_EXPECTED_BINARY_NUMERAL, (DelayedException)ERROR_UNDESCORE_NOT_ALLOWED){

        @Override
        protected boolean isAccepted(char c) {
            return switch (c) {
                case '0', '1', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumberRunParseRule DECIMAL_NUMERAL = new NumberRunParseRule((DelayedException)ERROR_EXPECTED_DECIMAL_NUMERAL, (DelayedException)ERROR_UNDESCORE_NOT_ALLOWED){

        @Override
        protected boolean isAccepted(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '_' -> true;
                default -> false;
            };
        }
    };
    private static final NumberRunParseRule HEX_NUMERAL = new NumberRunParseRule((DelayedException)ERROR_EXPECTED_HEX_NUMERAL, (DelayedException)ERROR_UNDESCORE_NOT_ALLOWED){

        @Override
        protected boolean isAccepted(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', '_', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    };
    private static final GreedyPredicateParseRule PLAIN_STRING_CHUNK = new GreedyPredicateParseRule(1, (DelayedException)ERROR_INVALID_STRING_CONTENTS){

        @Override
        protected boolean isAccepted(char c) {
            return switch (c) {
                case '\"', '\'', '\\' -> false;
                default -> true;
            };
        }
    };
    private static final StringReaderTerms.TerminalCharacters NUMBER_LOOKEAHEAD = new StringReaderTerms.TerminalCharacters(CharList.of()){

        @Override
        protected boolean isAccepted(char c) {
            return SnbtGrammar.canStartNumber(c);
        }
    };
    private static final Pattern UNICODE_NAME = Pattern.compile("[-a-zA-Z0-9 ]+");

    static DelayedException<CommandSyntaxException> createNumberParseError(NumberFormatException numberFormatException) {
        return DelayedException.create(ERROR_NUMBER_PARSE_FAILURE, numberFormatException.getMessage());
    }

    public static @Nullable String escapeControlCharacters(char c) {
        return switch (c) {
            case '\b' -> "b";
            case '\t' -> "t";
            case '\n' -> "n";
            case '\f' -> "f";
            case '\r' -> "r";
            default -> c < ' ' ? "x" + HEX_ESCAPE.toHexDigits((byte)c) : null;
        };
    }

    private static boolean isAllowedToStartUnquotedString(char c) {
        return !SnbtGrammar.canStartNumber(c);
    }

    static boolean canStartNumber(char c) {
        return switch (c) {
            case '+', '-', '.', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> true;
            default -> false;
        };
    }

    static boolean needsUnderscoreRemoval(String string) {
        return string.indexOf(95) != -1;
    }

    private static void cleanAndAppend(StringBuilder stringBuilder, String string) {
        SnbtGrammar.cleanAndAppend(stringBuilder, string, SnbtGrammar.needsUnderscoreRemoval(string));
    }

    static void cleanAndAppend(StringBuilder stringBuilder, String string, boolean bl) {
        if (bl) {
            for (char c : string.toCharArray()) {
                if (c == '_') continue;
                stringBuilder.append(c);
            }
        } else {
            stringBuilder.append(string);
        }
    }

    static short parseUnsignedShort(String string, int i) {
        int j = Integer.parseInt(string, i);
        if (j >> 16 == 0) {
            return (short)j;
        }
        throw new NumberFormatException("out of range: " + j);
    }

    private static <T> @Nullable T createFloat(DynamicOps<T> dynamicOps, Sign sign, @Nullable String string, @Nullable String string2, @Nullable Signed<String> signed, @Nullable TypeSuffix typeSuffix, ParseState<?> parseState) {
        StringBuilder stringBuilder = new StringBuilder();
        sign.append(stringBuilder);
        if (string != null) {
            SnbtGrammar.cleanAndAppend(stringBuilder, string);
        }
        if (string2 != null) {
            stringBuilder.append('.');
            SnbtGrammar.cleanAndAppend(stringBuilder, string2);
        }
        if (signed != null) {
            stringBuilder.append('e');
            signed.sign().append(stringBuilder);
            SnbtGrammar.cleanAndAppend(stringBuilder, (String)signed.value);
        }
        try {
            String string3 = stringBuilder.toString();
            TypeSuffix typeSuffix2 = typeSuffix;
            int n = 0;
            return switch (SwitchBootstraps.enumSwitch("enumSwitch", new Object[]{"FLOAT", "DOUBLE"}, (TypeSuffix)typeSuffix2, (int)n)) {
                case 0 -> SnbtGrammar.convertFloat(dynamicOps, parseState, string3);
                case 1 -> SnbtGrammar.convertDouble(dynamicOps, parseState, string3);
                case -1 -> SnbtGrammar.convertDouble(dynamicOps, parseState, string3);
                default -> {
                    parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_FLOAT_TYPE);
                    yield null;
                }
            };
        }
        catch (NumberFormatException numberFormatException) {
            parseState.errorCollector().store(parseState.mark(), SnbtGrammar.createNumberParseError(numberFormatException));
            return null;
        }
    }

    private static <T> @Nullable T convertFloat(DynamicOps<T> dynamicOps, ParseState<?> parseState, String string) {
        float f = Float.parseFloat(string);
        if (!Float.isFinite(f)) {
            parseState.errorCollector().store(parseState.mark(), ERROR_INFINITY_NOT_ALLOWED);
            return null;
        }
        return (T)dynamicOps.createFloat(f);
    }

    private static <T> @Nullable T convertDouble(DynamicOps<T> dynamicOps, ParseState<?> parseState, String string) {
        double d = Double.parseDouble(string);
        if (!Double.isFinite(d)) {
            parseState.errorCollector().store(parseState.mark(), ERROR_INFINITY_NOT_ALLOWED);
            return null;
        }
        return (T)dynamicOps.createDouble(d);
    }

    private static String joinList(List<String> list) {
        return switch (list.size()) {
            case 0 -> "";
            case 1 -> (String)list.getFirst();
            default -> String.join((CharSequence)"", list);
        };
    }

    public static <T> Grammar<T> createParser(DynamicOps<T> dynamicOps) {
        Object object = dynamicOps.createBoolean(true);
        Object object2 = dynamicOps.createBoolean(false);
        Object object3 = dynamicOps.emptyMap();
        Object object4 = dynamicOps.emptyList();
        Dictionary<StringReader> dictionary = new Dictionary<StringReader>();
        Atom atom = Atom.of("sign");
        dictionary.put(atom, Term.alternative(Term.sequence(StringReaderTerms.character('+'), Term.marker(atom, Sign.PLUS)), Term.sequence(StringReaderTerms.character('-'), Term.marker(atom, Sign.MINUS))), scope -> (Sign)((Object)((Object)scope.getOrThrow(atom))));
        Atom atom2 = Atom.of("integer_suffix");
        dictionary.put(atom2, Term.alternative(Term.sequence(StringReaderTerms.characters('u', 'U'), Term.alternative(Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.UNSIGNED, TypeSuffix.LONG))))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.alternative(Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker(atom2, new IntegerSuffix(SignedPrefix.SIGNED, TypeSuffix.LONG))))), Term.sequence(StringReaderTerms.characters('b', 'B'), Term.marker(atom2, new IntegerSuffix(null, TypeSuffix.BYTE))), Term.sequence(StringReaderTerms.characters('s', 'S'), Term.marker(atom2, new IntegerSuffix(null, TypeSuffix.SHORT))), Term.sequence(StringReaderTerms.characters('i', 'I'), Term.marker(atom2, new IntegerSuffix(null, TypeSuffix.INT))), Term.sequence(StringReaderTerms.characters('l', 'L'), Term.marker(atom2, new IntegerSuffix(null, TypeSuffix.LONG)))), scope -> (IntegerSuffix)((Object)((Object)scope.getOrThrow(atom2))));
        Atom atom3 = Atom.of("binary_numeral");
        dictionary.put(atom3, BINARY_NUMERAL);
        Atom atom4 = Atom.of("decimal_numeral");
        dictionary.put(atom4, DECIMAL_NUMERAL);
        Atom atom5 = Atom.of("hex_numeral");
        dictionary.put(atom5, HEX_NUMERAL);
        Atom atom6 = Atom.of("integer_literal");
        NamedRule namedRule = dictionary.put(atom6, Term.sequence(Term.optional(dictionary.named(atom)), Term.alternative(Term.sequence(StringReaderTerms.character('0'), Term.cut(), Term.alternative(Term.sequence(StringReaderTerms.characters('x', 'X'), Term.cut(), dictionary.named(atom5)), Term.sequence(StringReaderTerms.characters('b', 'B'), dictionary.named(atom3)), Term.sequence(dictionary.named(atom4), Term.cut(), Term.fail(ERROR_LEADING_ZERO_NOT_ALLOWED)), Term.marker(atom4, "0"))), dictionary.named(atom4)), Term.optional(dictionary.named(atom2))), scope -> {
            IntegerSuffix integerSuffix = scope.getOrDefault(atom2, IntegerSuffix.EMPTY);
            Sign sign = scope.getOrDefault(atom, Sign.PLUS);
            String string = (String)scope.get(atom4);
            if (string != null) {
                return new IntegerLiteral(sign, Base.DECIMAL, string, integerSuffix);
            }
            String string2 = (String)scope.get(atom5);
            if (string2 != null) {
                return new IntegerLiteral(sign, Base.HEX, string2, integerSuffix);
            }
            String string3 = (String)scope.getOrThrow(atom3);
            return new IntegerLiteral(sign, Base.BINARY, string3, integerSuffix);
        });
        Atom atom7 = Atom.of("float_type_suffix");
        dictionary.put(atom7, Term.alternative(Term.sequence(StringReaderTerms.characters('f', 'F'), Term.marker(atom7, TypeSuffix.FLOAT)), Term.sequence(StringReaderTerms.characters('d', 'D'), Term.marker(atom7, TypeSuffix.DOUBLE))), scope -> (TypeSuffix)((Object)((Object)scope.getOrThrow(atom7))));
        Atom atom8 = Atom.of("float_exponent_part");
        dictionary.put(atom8, Term.sequence(StringReaderTerms.characters('e', 'E'), Term.optional(dictionary.named(atom)), dictionary.named(atom4)), scope -> new Signed<String>(scope.getOrDefault(atom, Sign.PLUS), (String)scope.getOrThrow(atom4)));
        Atom atom9 = Atom.of("float_whole_part");
        Atom atom10 = Atom.of("float_fraction_part");
        Atom atom11 = Atom.of("float_literal");
        dictionary.putComplex(atom11, Term.sequence(Term.optional(dictionary.named(atom)), Term.alternative(Term.sequence(dictionary.namedWithAlias(atom4, atom9), StringReaderTerms.character('.'), Term.cut(), Term.optional(dictionary.namedWithAlias(atom4, atom10)), Term.optional(dictionary.named(atom8)), Term.optional(dictionary.named(atom7))), Term.sequence(StringReaderTerms.character('.'), Term.cut(), dictionary.namedWithAlias(atom4, atom10), Term.optional(dictionary.named(atom8)), Term.optional(dictionary.named(atom7))), Term.sequence(dictionary.namedWithAlias(atom4, atom9), dictionary.named(atom8), Term.cut(), Term.optional(dictionary.named(atom7))), Term.sequence(dictionary.namedWithAlias(atom4, atom9), Term.optional(dictionary.named(atom8)), dictionary.named(atom7)))), parseState -> {
            Scope scope = parseState.scope();
            Sign sign = scope.getOrDefault(atom, Sign.PLUS);
            String string = (String)scope.get(atom9);
            String string2 = (String)scope.get(atom10);
            Signed signed = (Signed)((Object)((Object)scope.get(atom8)));
            TypeSuffix typeSuffix = (TypeSuffix)((Object)((Object)scope.get(atom7)));
            return SnbtGrammar.createFloat(dynamicOps, sign, string, string2, signed, typeSuffix, parseState);
        });
        Atom atom12 = Atom.of("string_hex_2");
        dictionary.put(atom12, new SimpleHexLiteralParseRule(2));
        Atom atom13 = Atom.of("string_hex_4");
        dictionary.put(atom13, new SimpleHexLiteralParseRule(4));
        Atom atom14 = Atom.of("string_hex_8");
        dictionary.put(atom14, new SimpleHexLiteralParseRule(8));
        Atom atom15 = Atom.of("string_unicode_name");
        dictionary.put(atom15, new GreedyPatternParseRule(UNICODE_NAME, ERROR_INVALID_CHARACTER_NAME));
        Atom atom16 = Atom.of("string_escape_sequence");
        dictionary.putComplex(atom16, Term.alternative(Term.sequence(StringReaderTerms.character('b'), Term.marker(atom16, "\b")), Term.sequence(StringReaderTerms.character('s'), Term.marker(atom16, " ")), Term.sequence(StringReaderTerms.character('t'), Term.marker(atom16, "\t")), Term.sequence(StringReaderTerms.character('n'), Term.marker(atom16, "\n")), Term.sequence(StringReaderTerms.character('f'), Term.marker(atom16, "\f")), Term.sequence(StringReaderTerms.character('r'), Term.marker(atom16, "\r")), Term.sequence(StringReaderTerms.character('\\'), Term.marker(atom16, "\\")), Term.sequence(StringReaderTerms.character('\''), Term.marker(atom16, "'")), Term.sequence(StringReaderTerms.character('\"'), Term.marker(atom16, "\"")), Term.sequence(StringReaderTerms.character('x'), dictionary.named(atom12)), Term.sequence(StringReaderTerms.character('u'), dictionary.named(atom13)), Term.sequence(StringReaderTerms.character('U'), dictionary.named(atom14)), Term.sequence(StringReaderTerms.character('N'), StringReaderTerms.character('{'), dictionary.named(atom15), StringReaderTerms.character('}'))), parseState -> {
            int j;
            Scope scope = parseState.scope();
            String string = (String)scope.getAny(atom16);
            if (string != null) {
                return string;
            }
            String string2 = (String)scope.getAny(atom12, atom13, atom14);
            if (string2 != null) {
                int i = HexFormat.fromHexDigits((CharSequence)string2);
                if (!Character.isValidCodePoint(i)) {
                    parseState.errorCollector().store(parseState.mark(), DelayedException.create(ERROR_INVALID_CODEPOINT, String.format(Locale.ROOT, "U+%08X", i)));
                    return null;
                }
                return Character.toString((int)i);
            }
            String string3 = (String)scope.getOrThrow(atom15);
            try {
                j = Character.codePointOf((String)string3);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                parseState.errorCollector().store(parseState.mark(), ERROR_INVALID_CHARACTER_NAME);
                return null;
            }
            return Character.toString((int)j);
        });
        Atom atom17 = Atom.of("string_plain_contents");
        dictionary.put(atom17, PLAIN_STRING_CHUNK);
        Atom atom18 = Atom.of("string_chunks");
        Atom atom19 = Atom.of("string_contents");
        Atom atom20 = Atom.of("single_quoted_string_chunk");
        NamedRule namedRule2 = dictionary.put(atom20, Term.alternative(dictionary.namedWithAlias(atom17, atom19), Term.sequence(StringReaderTerms.character('\\'), dictionary.namedWithAlias(atom16, atom19)), Term.sequence(StringReaderTerms.character('\"'), Term.marker(atom19, "\""))), scope -> (String)scope.getOrThrow(atom19));
        Atom atom21 = Atom.of("single_quoted_string_contents");
        dictionary.put(atom21, Term.repeated(namedRule2, atom18), scope -> SnbtGrammar.joinList((List)scope.getOrThrow(atom18)));
        Atom atom22 = Atom.of("double_quoted_string_chunk");
        NamedRule namedRule3 = dictionary.put(atom22, Term.alternative(dictionary.namedWithAlias(atom17, atom19), Term.sequence(StringReaderTerms.character('\\'), dictionary.namedWithAlias(atom16, atom19)), Term.sequence(StringReaderTerms.character('\''), Term.marker(atom19, "'"))), scope -> (String)scope.getOrThrow(atom19));
        Atom atom23 = Atom.of("double_quoted_string_contents");
        dictionary.put(atom23, Term.repeated(namedRule3, atom18), scope -> SnbtGrammar.joinList((List)scope.getOrThrow(atom18)));
        Atom atom24 = Atom.of("quoted_string_literal");
        dictionary.put(atom24, Term.alternative(Term.sequence(StringReaderTerms.character('\"'), Term.cut(), Term.optional(dictionary.namedWithAlias(atom23, atom19)), StringReaderTerms.character('\"')), Term.sequence(StringReaderTerms.character('\''), Term.optional(dictionary.namedWithAlias(atom21, atom19)), StringReaderTerms.character('\''))), scope -> (String)scope.getOrThrow(atom19));
        Atom atom25 = Atom.of("unquoted_string");
        dictionary.put(atom25, new UnquotedStringParseRule(1, ERROR_EXPECTED_UNQUOTED_STRING));
        Atom atom26 = Atom.of("literal");
        Atom atom27 = Atom.of("arguments");
        dictionary.put(atom27, Term.repeatedWithTrailingSeparator(dictionary.forward(atom26), atom27, StringReaderTerms.character(',')), scope -> (List)scope.getOrThrow(atom27));
        Atom atom28 = Atom.of("unquoted_string_or_builtin");
        dictionary.putComplex(atom28, Term.sequence(dictionary.named(atom25), Term.optional(Term.sequence(StringReaderTerms.character('('), dictionary.named(atom27), StringReaderTerms.character(')')))), parseState -> {
            Scope scope = parseState.scope();
            String string = (String)scope.getOrThrow(atom25);
            if (string.isEmpty() || !SnbtGrammar.isAllowedToStartUnquotedString(string.charAt(0))) {
                parseState.errorCollector().store(parseState.mark(), SnbtOperations.BUILTIN_IDS, ERROR_INVALID_UNQUOTED_START);
                return null;
            }
            List list = (List)scope.get(atom27);
            if (list != null) {
                SnbtOperations.BuiltinKey builtinKey = new SnbtOperations.BuiltinKey(string, list.size());
                SnbtOperations.BuiltinOperation builtinOperation = SnbtOperations.BUILTIN_OPERATIONS.get((Object)builtinKey);
                if (builtinOperation != null) {
                    return builtinOperation.run(dynamicOps, list, parseState);
                }
                parseState.errorCollector().store(parseState.mark(), DelayedException.create(ERROR_NO_SUCH_OPERATION, builtinKey.toString()));
                return null;
            }
            if (string.equalsIgnoreCase("true")) {
                return object;
            }
            if (string.equalsIgnoreCase("false")) {
                return object2;
            }
            return dynamicOps.createString(string);
        });
        Atom atom29 = Atom.of("map_key");
        dictionary.put(atom29, Term.alternative(dictionary.named(atom24), dictionary.named(atom25)), scope -> (String)scope.getAnyOrThrow(atom24, atom25));
        Atom atom30 = Atom.of("map_entry");
        NamedRule namedRule4 = dictionary.putComplex(atom30, Term.sequence(dictionary.named(atom29), StringReaderTerms.character(':'), dictionary.named(atom26)), parseState -> {
            Scope scope = parseState.scope();
            String string = (String)scope.getOrThrow(atom29);
            if (string.isEmpty()) {
                parseState.errorCollector().store(parseState.mark(), ERROR_EMPTY_KEY);
                return null;
            }
            Object object = scope.getOrThrow(atom26);
            return Map.entry((Object)string, object);
        });
        Atom atom31 = Atom.of("map_entries");
        dictionary.put(atom31, Term.repeatedWithTrailingSeparator(namedRule4, atom31, StringReaderTerms.character(',')), scope -> (List)scope.getOrThrow(atom31));
        Atom atom32 = Atom.of("map_literal");
        dictionary.put(atom32, Term.sequence(StringReaderTerms.character('{'), dictionary.named(atom31), StringReaderTerms.character('}')), scope -> {
            List list = (List)scope.getOrThrow(atom31);
            if (list.isEmpty()) {
                return object3;
            }
            ImmutableMap.Builder builder = ImmutableMap.builderWithExpectedSize((int)list.size());
            for (Map.Entry entry : list) {
                builder.put(dynamicOps.createString((String)entry.getKey()), entry.getValue());
            }
            return dynamicOps.createMap((Map)builder.buildKeepingLast());
        });
        Atom atom33 = Atom.of("list_entries");
        dictionary.put(atom33, Term.repeatedWithTrailingSeparator(dictionary.forward(atom26), atom33, StringReaderTerms.character(',')), scope -> (List)scope.getOrThrow(atom33));
        Atom atom34 = Atom.of("array_prefix");
        dictionary.put(atom34, Term.alternative(Term.sequence(StringReaderTerms.character('B'), Term.marker(atom34, ArrayPrefix.BYTE)), Term.sequence(StringReaderTerms.character('L'), Term.marker(atom34, ArrayPrefix.LONG)), Term.sequence(StringReaderTerms.character('I'), Term.marker(atom34, ArrayPrefix.INT))), scope -> (ArrayPrefix)((Object)((Object)scope.getOrThrow(atom34))));
        Atom atom35 = Atom.of("int_array_entries");
        dictionary.put(atom35, Term.repeatedWithTrailingSeparator(namedRule, atom35, StringReaderTerms.character(',')), scope -> (List)scope.getOrThrow(atom35));
        Atom atom36 = Atom.of("list_literal");
        dictionary.putComplex(atom36, Term.sequence(StringReaderTerms.character('['), Term.alternative(Term.sequence(dictionary.named(atom34), StringReaderTerms.character(';'), dictionary.named(atom35)), dictionary.named(atom33)), StringReaderTerms.character(']')), parseState -> {
            Scope scope = parseState.scope();
            ArrayPrefix arrayPrefix = (ArrayPrefix)((Object)((Object)scope.get(atom34)));
            if (arrayPrefix != null) {
                List list = (List)scope.getOrThrow(atom35);
                return list.isEmpty() ? arrayPrefix.create(dynamicOps) : arrayPrefix.create(dynamicOps, list, parseState);
            }
            List list = (List)scope.getOrThrow(atom33);
            return list.isEmpty() ? object4 : dynamicOps.createList(list.stream());
        });
        NamedRule namedRule5 = dictionary.putComplex(atom26, Term.alternative(Term.sequence(Term.positiveLookahead(NUMBER_LOOKEAHEAD), Term.alternative(dictionary.namedWithAlias(atom11, atom26), dictionary.named(atom6))), Term.sequence(Term.positiveLookahead(StringReaderTerms.characters('\"', '\'')), Term.cut(), dictionary.named(atom24)), Term.sequence(Term.positiveLookahead(StringReaderTerms.character('{')), Term.cut(), dictionary.namedWithAlias(atom32, atom26)), Term.sequence(Term.positiveLookahead(StringReaderTerms.character('[')), Term.cut(), dictionary.namedWithAlias(atom36, atom26)), dictionary.namedWithAlias(atom28, atom26)), parseState -> {
            Scope scope = parseState.scope();
            String string = (String)scope.get(atom24);
            if (string != null) {
                return dynamicOps.createString(string);
            }
            IntegerLiteral integerLiteral = (IntegerLiteral)((Object)((Object)scope.get(atom6)));
            if (integerLiteral != null) {
                return integerLiteral.create(dynamicOps, parseState);
            }
            return scope.getOrThrow(atom26);
        });
        return new Grammar<Object>(dictionary, namedRule5);
    }

    static enum Sign {
        PLUS,
        MINUS;


        public void append(StringBuilder stringBuilder) {
            if (this == MINUS) {
                stringBuilder.append("-");
            }
        }
    }

    static final class Signed<T>
    extends Record {
        private final Sign sign;
        final T value;

        Signed(Sign sign, T object) {
            this.sign = sign;
            this.value = object;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Signed.class, "sign;value", "sign", "value"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Signed.class, "sign;value", "sign", "value"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Signed.class, "sign;value", "sign", "value"}, this, object);
        }

        public Sign sign() {
            return this.sign;
        }

        public T value() {
            return this.value;
        }
    }

    static enum TypeSuffix {
        FLOAT,
        DOUBLE,
        BYTE,
        SHORT,
        INT,
        LONG;

    }

    static final class IntegerSuffix
    extends Record {
        final @Nullable SignedPrefix signed;
        final @Nullable TypeSuffix type;
        public static final IntegerSuffix EMPTY = new IntegerSuffix(null, null);

        IntegerSuffix(@Nullable SignedPrefix signedPrefix, @Nullable TypeSuffix typeSuffix) {
            this.signed = signedPrefix;
            this.type = typeSuffix;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IntegerSuffix.class, "signed;type", "signed", "type"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IntegerSuffix.class, "signed;type", "signed", "type"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IntegerSuffix.class, "signed;type", "signed", "type"}, this, object);
        }

        public @Nullable SignedPrefix signed() {
            return this.signed;
        }

        public @Nullable TypeSuffix type() {
            return this.type;
        }
    }

    static enum SignedPrefix {
        SIGNED,
        UNSIGNED;

    }

    static class SimpleHexLiteralParseRule
    extends GreedyPredicateParseRule {
        public SimpleHexLiteralParseRule(int i) {
            super(i, i, DelayedException.create(ERROR_EXPECTED_HEX_ESCAPE, String.valueOf(i)));
        }

        @Override
        protected boolean isAccepted(char c) {
            return switch (c) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f' -> true;
                default -> false;
            };
        }
    }

    static enum ArrayPrefix {
        BYTE(TypeSuffix.BYTE, new TypeSuffix[0]){
            private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(new byte[0]);

            @Override
            public <T> T create(DynamicOps<T> dynamicOps) {
                return (T)dynamicOps.createByteList(EMPTY_BUFFER);
            }

            @Override
            public <T> @Nullable T create(DynamicOps<T> dynamicOps, List<IntegerLiteral> list, ParseState<?> parseState) {
                ByteArrayList byteList = new ByteArrayList();
                for (IntegerLiteral integerLiteral : list) {
                    Number number = this.buildNumber(integerLiteral, parseState);
                    if (number == null) {
                        return null;
                    }
                    byteList.add(number.byteValue());
                }
                return (T)dynamicOps.createByteList(ByteBuffer.wrap(byteList.toByteArray()));
            }
        }
        ,
        INT(TypeSuffix.INT, new TypeSuffix[]{TypeSuffix.BYTE, TypeSuffix.SHORT}){

            @Override
            public <T> T create(DynamicOps<T> dynamicOps) {
                return (T)dynamicOps.createIntList(IntStream.empty());
            }

            @Override
            public <T> @Nullable T create(DynamicOps<T> dynamicOps, List<IntegerLiteral> list, ParseState<?> parseState) {
                IntStream.Builder builder = IntStream.builder();
                for (IntegerLiteral integerLiteral : list) {
                    Number number = this.buildNumber(integerLiteral, parseState);
                    if (number == null) {
                        return null;
                    }
                    builder.add(number.intValue());
                }
                return (T)dynamicOps.createIntList(builder.build());
            }
        }
        ,
        LONG(TypeSuffix.LONG, new TypeSuffix[]{TypeSuffix.BYTE, TypeSuffix.SHORT, TypeSuffix.INT}){

            @Override
            public <T> T create(DynamicOps<T> dynamicOps) {
                return (T)dynamicOps.createLongList(LongStream.empty());
            }

            @Override
            public <T> @Nullable T create(DynamicOps<T> dynamicOps, List<IntegerLiteral> list, ParseState<?> parseState) {
                LongStream.Builder builder = LongStream.builder();
                for (IntegerLiteral integerLiteral : list) {
                    Number number = this.buildNumber(integerLiteral, parseState);
                    if (number == null) {
                        return null;
                    }
                    builder.add(number.longValue());
                }
                return (T)dynamicOps.createLongList(builder.build());
            }
        };

        private final TypeSuffix defaultType;
        private final Set<TypeSuffix> additionalTypes;

        ArrayPrefix(TypeSuffix typeSuffix, TypeSuffix ... typeSuffixs) {
            this.additionalTypes = Set.of((Object[])typeSuffixs);
            this.defaultType = typeSuffix;
        }

        public boolean isAllowed(TypeSuffix typeSuffix) {
            return typeSuffix == this.defaultType || this.additionalTypes.contains((Object)typeSuffix);
        }

        public abstract <T> T create(DynamicOps<T> var1);

        public abstract <T> @Nullable T create(DynamicOps<T> var1, List<IntegerLiteral> var2, ParseState<?> var3);

        protected @Nullable Number buildNumber(IntegerLiteral integerLiteral, ParseState<?> parseState) {
            TypeSuffix typeSuffix = this.computeType(integerLiteral.suffix);
            if (typeSuffix == null) {
                parseState.errorCollector().store(parseState.mark(), ERROR_INVALID_ARRAY_ELEMENT_TYPE);
                return null;
            }
            return (Number)integerLiteral.create(JavaOps.INSTANCE, typeSuffix, parseState);
        }

        private @Nullable TypeSuffix computeType(IntegerSuffix integerSuffix) {
            TypeSuffix typeSuffix = integerSuffix.type();
            if (typeSuffix == null) {
                return this.defaultType;
            }
            if (!this.isAllowed(typeSuffix)) {
                return null;
            }
            return typeSuffix;
        }
    }

    static final class IntegerLiteral
    extends Record {
        private final Sign sign;
        private final Base base;
        private final String digits;
        final IntegerSuffix suffix;

        IntegerLiteral(Sign sign, Base base, String string, IntegerSuffix integerSuffix) {
            this.sign = sign;
            this.base = base;
            this.digits = string;
            this.suffix = integerSuffix;
        }

        private SignedPrefix signedOrDefault() {
            if (this.suffix.signed != null) {
                return this.suffix.signed;
            }
            return switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0, 2 -> SignedPrefix.UNSIGNED;
                case 1 -> SignedPrefix.SIGNED;
            };
        }

        private String cleanupDigits(Sign sign) {
            boolean bl = SnbtGrammar.needsUnderscoreRemoval(this.digits);
            if (sign == Sign.MINUS || bl) {
                StringBuilder stringBuilder = new StringBuilder();
                sign.append(stringBuilder);
                SnbtGrammar.cleanAndAppend(stringBuilder, this.digits, bl);
                return stringBuilder.toString();
            }
            return this.digits;
        }

        public <T> @Nullable T create(DynamicOps<T> dynamicOps, ParseState<?> parseState) {
            return this.create(dynamicOps, (TypeSuffix)((Object)Objects.requireNonNullElse((Object)((Object)this.suffix.type), (Object)((Object)TypeSuffix.INT))), parseState);
        }

        public <T> @Nullable T create(DynamicOps<T> dynamicOps, TypeSuffix typeSuffix, ParseState<?> parseState) {
            boolean bl;
            boolean bl2 = bl = this.signedOrDefault() == SignedPrefix.SIGNED;
            if (!bl && this.sign == Sign.MINUS) {
                parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_NON_NEGATIVE_NUMBER);
                return null;
            }
            String string = this.cleanupDigits(this.sign);
            int i = switch (this.base.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> 2;
                case 1 -> 10;
                case 2 -> 16;
            };
            try {
                if (bl) {
                    return (T)(switch (typeSuffix.ordinal()) {
                        case 2 -> dynamicOps.createByte(Byte.parseByte(string, i));
                        case 3 -> dynamicOps.createShort(Short.parseShort(string, i));
                        case 4 -> dynamicOps.createInt(Integer.parseInt(string, i));
                        case 5 -> dynamicOps.createLong(Long.parseLong(string, i));
                        default -> {
                            parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_INTEGER_TYPE);
                            yield null;
                        }
                    });
                }
                return (T)(switch (typeSuffix.ordinal()) {
                    case 2 -> dynamicOps.createByte(UnsignedBytes.parseUnsignedByte((String)string, (int)i));
                    case 3 -> dynamicOps.createShort(SnbtGrammar.parseUnsignedShort(string, i));
                    case 4 -> dynamicOps.createInt(Integer.parseUnsignedInt(string, i));
                    case 5 -> dynamicOps.createLong(Long.parseUnsignedLong(string, i));
                    default -> {
                        parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_INTEGER_TYPE);
                        yield null;
                    }
                });
            }
            catch (NumberFormatException numberFormatException) {
                parseState.errorCollector().store(parseState.mark(), SnbtGrammar.createNumberParseError(numberFormatException));
                return null;
            }
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IntegerLiteral.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IntegerLiteral.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IntegerLiteral.class, "sign;base;digits;suffix", "sign", "base", "digits", "suffix"}, this, object);
        }

        public Sign sign() {
            return this.sign;
        }

        public Base base() {
            return this.base;
        }

        public String digits() {
            return this.digits;
        }

        public IntegerSuffix suffix() {
            return this.suffix;
        }
    }

    static enum Base {
        BINARY,
        DECIMAL,
        HEX;

    }
}

