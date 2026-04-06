/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class ItemParser {
    static final DynamicCommandExceptionType ERROR_UNKNOWN_ITEM = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.item.id.invalid", object));
    static final DynamicCommandExceptionType ERROR_UNKNOWN_COMPONENT = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.item.component.unknown", object));
    static final Dynamic2CommandExceptionType ERROR_MALFORMED_COMPONENT = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("arguments.item.component.malformed", object, object2));
    static final SimpleCommandExceptionType ERROR_EXPECTED_COMPONENT = new SimpleCommandExceptionType((Message)Component.translatable("arguments.item.component.expected"));
    static final DynamicCommandExceptionType ERROR_REPEATED_COMPONENT = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.item.component.repeated", object));
    private static final DynamicCommandExceptionType ERROR_MALFORMED_ITEM = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.item.malformed", object));
    public static final char SYNTAX_START_COMPONENTS = '[';
    public static final char SYNTAX_END_COMPONENTS = ']';
    public static final char SYNTAX_COMPONENT_SEPARATOR = ',';
    public static final char SYNTAX_COMPONENT_ASSIGNMENT = '=';
    public static final char SYNTAX_REMOVED_COMPONENT = '!';
    static final Function<SuggestionsBuilder, CompletableFuture<Suggestions>> SUGGEST_NOTHING = SuggestionsBuilder::buildFuture;
    final HolderLookup.RegistryLookup<Item> items;
    final RegistryOps<Tag> registryOps;
    final TagParser<Tag> tagParser;

    public ItemParser(HolderLookup.Provider provider) {
        this.items = provider.lookupOrThrow(Registries.ITEM);
        this.registryOps = provider.createSerializationContext(NbtOps.INSTANCE);
        this.tagParser = TagParser.create(this.registryOps);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public ItemResult parse(StringReader stringReader) throws CommandSyntaxException {
        final @Nullable MutableObject mutableObject = new MutableObject();
        final DataComponentPatch.Builder builder = DataComponentPatch.builder();
        this.parse(stringReader, new Visitor(){

            @Override
            public void visitItem(Holder<Item> holder) {
                mutableObject.setValue(holder);
            }

            @Override
            public <T> void visitComponent(DataComponentType<T> dataComponentType, T object) {
                builder.set(dataComponentType, object);
            }

            @Override
            public <T> void visitRemovedComponent(DataComponentType<T> dataComponentType) {
                builder.remove(dataComponentType);
            }
        });
        Holder holder = Objects.requireNonNull((Holder)mutableObject.get(), "Parser gave no item");
        DataComponentPatch dataComponentPatch = builder.build();
        ItemParser.validateComponents(stringReader, holder, dataComponentPatch);
        return new ItemResult(holder, dataComponentPatch);
    }

    private static void validateComponents(StringReader stringReader, Holder<Item> holder, DataComponentPatch dataComponentPatch) throws CommandSyntaxException {
        PatchedDataComponentMap dataComponentMap = PatchedDataComponentMap.fromPatch(holder.value().components(), dataComponentPatch);
        DataResult<Unit> dataResult = ItemStack.validateComponents(dataComponentMap);
        dataResult.getOrThrow(string -> ERROR_MALFORMED_ITEM.createWithContext((ImmutableStringReader)stringReader, string));
    }

    public void parse(StringReader stringReader, Visitor visitor) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        try {
            new State(stringReader, visitor).parse();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            stringReader.setCursor(i);
            throw commandSyntaxException;
        }
    }

    public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder) {
        StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
        stringReader.setCursor(suggestionsBuilder.getStart());
        SuggestionsVisitor suggestionsVisitor = new SuggestionsVisitor();
        State state = new State(stringReader, suggestionsVisitor);
        try {
            state.parse();
        }
        catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        return suggestionsVisitor.resolveSuggestions(suggestionsBuilder, stringReader);
    }

    public static interface Visitor {
        default public void visitItem(Holder<Item> holder) {
        }

        default public <T> void visitComponent(DataComponentType<T> dataComponentType, T object) {
        }

        default public <T> void visitRemovedComponent(DataComponentType<T> dataComponentType) {
        }

        default public void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {
        }
    }

    public record ItemResult(Holder<Item> item, DataComponentPatch components) {
    }

    class State {
        private final StringReader reader;
        private final Visitor visitor;

        State(StringReader stringReader, Visitor visitor) {
            this.reader = stringReader;
            this.visitor = visitor;
        }

        public void parse() throws CommandSyntaxException {
            this.visitor.visitSuggestions(this::suggestItem);
            this.readItem();
            this.visitor.visitSuggestions(this::suggestStartComponents);
            if (this.reader.canRead() && this.reader.peek() == '[') {
                this.visitor.visitSuggestions(SUGGEST_NOTHING);
                this.readComponents();
            }
        }

        private void readItem() throws CommandSyntaxException {
            int i = this.reader.getCursor();
            Identifier identifier = Identifier.read(this.reader);
            this.visitor.visitItem((Holder<Item>)ItemParser.this.items.get(ResourceKey.create(Registries.ITEM, identifier)).orElseThrow(() -> {
                this.reader.setCursor(i);
                return ERROR_UNKNOWN_ITEM.createWithContext((ImmutableStringReader)this.reader, (Object)identifier);
            }));
        }

        private void readComponents() throws CommandSyntaxException {
            this.reader.expect('[');
            this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
            ReferenceArraySet set = new ReferenceArraySet();
            while (this.reader.canRead() && this.reader.peek() != ']') {
                this.reader.skipWhitespace();
                if (this.reader.canRead() && this.reader.peek() == '!') {
                    this.reader.skip();
                    this.visitor.visitSuggestions(this::suggestComponent);
                    dataComponentType = State.readComponentType(this.reader);
                    if (!set.add(dataComponentType)) {
                        throw ERROR_REPEATED_COMPONENT.create(dataComponentType);
                    }
                    this.visitor.visitRemovedComponent(dataComponentType);
                    this.visitor.visitSuggestions(SUGGEST_NOTHING);
                    this.reader.skipWhitespace();
                } else {
                    dataComponentType = State.readComponentType(this.reader);
                    if (!set.add(dataComponentType)) {
                        throw ERROR_REPEATED_COMPONENT.create(dataComponentType);
                    }
                    this.visitor.visitSuggestions(this::suggestAssignment);
                    this.reader.skipWhitespace();
                    this.reader.expect('=');
                    this.visitor.visitSuggestions(SUGGEST_NOTHING);
                    this.reader.skipWhitespace();
                    this.readComponent(ItemParser.this.tagParser, ItemParser.this.registryOps, dataComponentType);
                    this.reader.skipWhitespace();
                }
                this.visitor.visitSuggestions(this::suggestNextOrEndComponents);
                if (!this.reader.canRead() || this.reader.peek() != ',') break;
                this.reader.skip();
                this.reader.skipWhitespace();
                this.visitor.visitSuggestions(this::suggestComponentAssignmentOrRemoval);
                if (this.reader.canRead()) continue;
                throw ERROR_EXPECTED_COMPONENT.createWithContext((ImmutableStringReader)this.reader);
            }
            this.reader.expect(']');
            this.visitor.visitSuggestions(SUGGEST_NOTHING);
        }

        public static DataComponentType<?> readComponentType(StringReader stringReader) throws CommandSyntaxException {
            if (!stringReader.canRead()) {
                throw ERROR_EXPECTED_COMPONENT.createWithContext((ImmutableStringReader)stringReader);
            }
            int i = stringReader.getCursor();
            Identifier identifier = Identifier.read(stringReader);
            DataComponentType<?> dataComponentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(identifier);
            if (dataComponentType == null || dataComponentType.isTransient()) {
                stringReader.setCursor(i);
                throw ERROR_UNKNOWN_COMPONENT.createWithContext((ImmutableStringReader)stringReader, (Object)identifier);
            }
            return dataComponentType;
        }

        private <T, O> void readComponent(TagParser<O> tagParser, RegistryOps<O> registryOps, DataComponentType<T> dataComponentType) throws CommandSyntaxException {
            int i = this.reader.getCursor();
            O object = tagParser.parseAsArgument(this.reader);
            DataResult dataResult = dataComponentType.codecOrThrow().parse(registryOps, object);
            this.visitor.visitComponent(dataComponentType, dataResult.getOrThrow(string -> {
                this.reader.setCursor(i);
                return ERROR_MALFORMED_COMPONENT.createWithContext((ImmutableStringReader)this.reader, (Object)dataComponentType.toString(), string);
            }));
        }

        private CompletableFuture<Suggestions> suggestStartComponents(SuggestionsBuilder suggestionsBuilder) {
            if (suggestionsBuilder.getRemaining().isEmpty()) {
                suggestionsBuilder.suggest(String.valueOf('['));
            }
            return suggestionsBuilder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestNextOrEndComponents(SuggestionsBuilder suggestionsBuilder) {
            if (suggestionsBuilder.getRemaining().isEmpty()) {
                suggestionsBuilder.suggest(String.valueOf(','));
                suggestionsBuilder.suggest(String.valueOf(']'));
            }
            return suggestionsBuilder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestAssignment(SuggestionsBuilder suggestionsBuilder) {
            if (suggestionsBuilder.getRemaining().isEmpty()) {
                suggestionsBuilder.suggest(String.valueOf('='));
            }
            return suggestionsBuilder.buildFuture();
        }

        private CompletableFuture<Suggestions> suggestItem(SuggestionsBuilder suggestionsBuilder) {
            return SharedSuggestionProvider.suggestResource(ItemParser.this.items.listElementIds().map(ResourceKey::identifier), suggestionsBuilder);
        }

        private CompletableFuture<Suggestions> suggestComponentAssignmentOrRemoval(SuggestionsBuilder suggestionsBuilder) {
            suggestionsBuilder.suggest(String.valueOf('!'));
            return this.suggestComponent(suggestionsBuilder, String.valueOf('='));
        }

        private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder suggestionsBuilder) {
            return this.suggestComponent(suggestionsBuilder, "");
        }

        private CompletableFuture<Suggestions> suggestComponent(SuggestionsBuilder suggestionsBuilder, String string) {
            String string2 = suggestionsBuilder.getRemaining().toLowerCase(Locale.ROOT);
            SharedSuggestionProvider.filterResources(BuiltInRegistries.DATA_COMPONENT_TYPE.entrySet(), string2, entry -> ((ResourceKey)entry.getKey()).identifier(), entry -> {
                DataComponentType dataComponentType = (DataComponentType)entry.getValue();
                if (dataComponentType.codec() != null) {
                    Identifier identifier = ((ResourceKey)entry.getKey()).identifier();
                    suggestionsBuilder.suggest(String.valueOf(identifier) + string);
                }
            });
            return suggestionsBuilder.buildFuture();
        }
    }

    static class SuggestionsVisitor
    implements Visitor {
        private Function<SuggestionsBuilder, CompletableFuture<Suggestions>> suggestions = SUGGEST_NOTHING;

        SuggestionsVisitor() {
        }

        @Override
        public void visitSuggestions(Function<SuggestionsBuilder, CompletableFuture<Suggestions>> function) {
            this.suggestions = function;
        }

        public CompletableFuture<Suggestions> resolveSuggestions(SuggestionsBuilder suggestionsBuilder, StringReader stringReader) {
            return this.suggestions.apply(suggestionsBuilder.createOffset(stringReader.getCursor()));
        }
    }
}

