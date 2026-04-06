/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.SnbtGrammar;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Dictionary;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.Term;
import net.minecraft.util.parsing.packrat.commands.Grammar;
import net.minecraft.util.parsing.packrat.commands.IdentifierParseRule;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jspecify.annotations.Nullable;

public class ResourceOrIdArgument<T>
implements ArgumentType<Holder<T>> {
    private static final Collection<String> EXAMPLES = List.of((Object)"foo", (Object)"foo:bar", (Object)"012", (Object)"{}", (Object)"true");
    public static final DynamicCommandExceptionType ERROR_FAILED_TO_PARSE = new DynamicCommandExceptionType(object -> Component.translatableEscape("argument.resource_or_id.failed_to_parse", object));
    public static final Dynamic2CommandExceptionType ERROR_NO_SUCH_ELEMENT = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("argument.resource_or_id.no_such_element", object, object2));
    public static final DynamicOps<Tag> OPS = NbtOps.INSTANCE;
    private final HolderLookup.Provider registryLookup;
    private final Optional<? extends HolderLookup.RegistryLookup<T>> elementLookup;
    private final Codec<T> codec;
    private final Grammar<Result<T, Tag>> grammar;
    private final ResourceKey<? extends Registry<T>> registryKey;

    protected ResourceOrIdArgument(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey, Codec<T> codec) {
        this.registryLookup = commandBuildContext;
        this.elementLookup = commandBuildContext.lookup(resourceKey);
        this.registryKey = resourceKey;
        this.codec = codec;
        this.grammar = ResourceOrIdArgument.createGrammar(resourceKey, OPS);
    }

    public static <T, O> Grammar<Result<T, O>> createGrammar(ResourceKey<? extends Registry<T>> resourceKey, DynamicOps<O> dynamicOps) {
        Grammar<O> grammar = SnbtGrammar.createParser(dynamicOps);
        Dictionary<StringReader> dictionary = new Dictionary<StringReader>();
        Atom atom = Atom.of("result");
        Atom atom2 = Atom.of("id");
        Atom atom3 = Atom.of("value");
        dictionary.put(atom2, IdentifierParseRule.INSTANCE);
        dictionary.put(atom3, grammar.top().value());
        NamedRule namedRule = dictionary.put(atom, Term.alternative(dictionary.named(atom2), dictionary.named(atom3)), scope -> {
            Identifier identifier = (Identifier)scope.get(atom2);
            if (identifier != null) {
                return new ReferenceResult(ResourceKey.create(resourceKey, identifier));
            }
            Object object = scope.getOrThrow(atom3);
            return new InlineResult(object);
        });
        return new Grammar<Result<T, O>>(dictionary, namedRule);
    }

    public static LootTableArgument lootTable(CommandBuildContext commandBuildContext) {
        return new LootTableArgument(commandBuildContext);
    }

    public static Holder<LootTable> getLootTable(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        return ResourceOrIdArgument.getResource(commandContext, string);
    }

    public static LootModifierArgument lootModifier(CommandBuildContext commandBuildContext) {
        return new LootModifierArgument(commandBuildContext);
    }

    public static Holder<LootItemFunction> getLootModifier(CommandContext<CommandSourceStack> commandContext, String string) {
        return ResourceOrIdArgument.getResource(commandContext, string);
    }

    public static LootPredicateArgument lootPredicate(CommandBuildContext commandBuildContext) {
        return new LootPredicateArgument(commandBuildContext);
    }

    public static Holder<LootItemCondition> getLootPredicate(CommandContext<CommandSourceStack> commandContext, String string) {
        return ResourceOrIdArgument.getResource(commandContext, string);
    }

    public static DialogArgument dialog(CommandBuildContext commandBuildContext) {
        return new DialogArgument(commandBuildContext);
    }

    public static Holder<Dialog> getDialog(CommandContext<CommandSourceStack> commandContext, String string) {
        return ResourceOrIdArgument.getResource(commandContext, string);
    }

    private static <T> Holder<T> getResource(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Holder)commandContext.getArgument(string, Holder.class);
    }

    public @Nullable Holder<T> parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader, this.grammar, OPS);
    }

    private <O> @Nullable Holder<T> parse(StringReader stringReader, Grammar<Result<T, O>> grammar, DynamicOps<O> dynamicOps) throws CommandSyntaxException {
        Result<T, O> result = grammar.parseForCommands(stringReader);
        if (this.elementLookup.isEmpty()) {
            return null;
        }
        return result.parse((ImmutableStringReader)stringReader, this.registryLookup, dynamicOps, this.codec, this.elementLookup.get());
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.listSuggestions(commandContext, suggestionsBuilder, this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class LootTableArgument
    extends ResourceOrIdArgument<LootTable> {
        protected LootTableArgument(CommandBuildContext commandBuildContext) {
            super(commandBuildContext, Registries.LOOT_TABLE, LootTable.DIRECT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootModifierArgument
    extends ResourceOrIdArgument<LootItemFunction> {
        protected LootModifierArgument(CommandBuildContext commandBuildContext) {
            super(commandBuildContext, Registries.ITEM_MODIFIER, LootItemFunctions.ROOT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class LootPredicateArgument
    extends ResourceOrIdArgument<LootItemCondition> {
        protected LootPredicateArgument(CommandBuildContext commandBuildContext) {
            super(commandBuildContext, Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static class DialogArgument
    extends ResourceOrIdArgument<Dialog> {
        protected DialogArgument(CommandBuildContext commandBuildContext) {
            super(commandBuildContext, Registries.DIALOG, Dialog.DIRECT_CODEC);
        }

        @Override
        public /* synthetic */ @Nullable Object parse(StringReader stringReader) throws CommandSyntaxException {
            return super.parse(stringReader);
        }
    }

    public static sealed interface Result<T, O>
    permits InlineResult, ReferenceResult {
        public Holder<T> parse(ImmutableStringReader var1, HolderLookup.Provider var2, DynamicOps<O> var3, Codec<T> var4, HolderLookup.RegistryLookup<T> var5) throws CommandSyntaxException;
    }

    public record ReferenceResult<T, O>(ResourceKey<T> key) implements Result<T, O>
    {
        @Override
        public Holder<T> parse(ImmutableStringReader immutableStringReader, HolderLookup.Provider provider, DynamicOps<O> dynamicOps, Codec<T> codec, HolderLookup.RegistryLookup<T> registryLookup) throws CommandSyntaxException {
            return registryLookup.get(this.key).orElseThrow(() -> ERROR_NO_SUCH_ELEMENT.createWithContext(immutableStringReader, (Object)this.key.identifier(), (Object)this.key.registry()));
        }
    }

    public record InlineResult<T, O>(O value) implements Result<T, O>
    {
        @Override
        public Holder<T> parse(ImmutableStringReader immutableStringReader, HolderLookup.Provider provider, DynamicOps<O> dynamicOps, Codec<T> codec, HolderLookup.RegistryLookup<T> registryLookup) throws CommandSyntaxException {
            return Holder.direct(codec.parse(provider.createSerializationContext(dynamicOps), this.value).getOrThrow(string -> ERROR_FAILED_TO_PARSE.createWithContext(immutableStringReader, string)));
        }
    }
}

