/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.nbt;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import org.jspecify.annotations.Nullable;

public class SnbtOperations {
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_STRING_UUID = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_string_uuid")));
    static final DelayedException<CommandSyntaxException> ERROR_EXPECTED_NUMBER_OR_BOOLEAN = DelayedException.create(new SimpleCommandExceptionType((Message)Component.translatable("snbt.parser.expected_number_or_boolean")));
    public static final String BUILTIN_TRUE = "true";
    public static final String BUILTIN_FALSE = "false";
    public static final Map<BuiltinKey, BuiltinOperation> BUILTIN_OPERATIONS = Map.of((Object)((Object)new BuiltinKey("bool", 1)), (Object)new BuiltinOperation(){

        @Override
        public <T> T run(DynamicOps<T> dynamicOps, List<T> list, ParseState<StringReader> parseState) {
            Boolean boolean_ = 1.convert(dynamicOps, list.getFirst());
            if (boolean_ == null) {
                parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_NUMBER_OR_BOOLEAN);
                return null;
            }
            return (T)dynamicOps.createBoolean(boolean_.booleanValue());
        }

        private static <T> @Nullable Boolean convert(DynamicOps<T> dynamicOps, T object) {
            Optional optional = dynamicOps.getBooleanValue(object).result();
            if (optional.isPresent()) {
                return (Boolean)optional.get();
            }
            Optional optional2 = dynamicOps.getNumberValue(object).result();
            if (optional2.isPresent()) {
                return ((Number)optional2.get()).doubleValue() != 0.0;
            }
            return null;
        }
    }, (Object)((Object)new BuiltinKey("uuid", 1)), (Object)new BuiltinOperation(){

        @Override
        public <T> T run(DynamicOps<T> dynamicOps, List<T> list, ParseState<StringReader> parseState) {
            UUID uUID;
            Optional optional = dynamicOps.getStringValue(list.getFirst()).result();
            if (optional.isEmpty()) {
                parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_STRING_UUID);
                return null;
            }
            try {
                uUID = UUID.fromString((String)optional.get());
            }
            catch (IllegalArgumentException illegalArgumentException) {
                parseState.errorCollector().store(parseState.mark(), ERROR_EXPECTED_STRING_UUID);
                return null;
            }
            return (T)dynamicOps.createIntList(IntStream.of(UUIDUtil.uuidToIntArray(uUID)));
        }
    });
    public static final SuggestionSupplier<StringReader> BUILTIN_IDS = new SuggestionSupplier<StringReader>(){
        private final Set<String> keys = Stream.concat(Stream.of("false", "true"), BUILTIN_OPERATIONS.keySet().stream().map(BuiltinKey::id)).collect(Collectors.toSet());

        @Override
        public Stream<String> possibleValues(ParseState<StringReader> parseState) {
            return this.keys.stream();
        }
    };

    public record BuiltinKey(String id, int argCount) {
        public String toString() {
            return this.id + "/" + this.argCount;
        }
    }

    public static interface BuiltinOperation {
        public <T> @Nullable T run(DynamicOps<T> var1, List<T> var2, ParseState<StringReader> var3);
    }
}

