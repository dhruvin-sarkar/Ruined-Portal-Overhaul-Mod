/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  org.apache.commons.io.FilenameUtils
 */
package net.minecraft.commands.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.apache.commons.io.FilenameUtils;

public class ResourceSelectorArgument<T>
implements ArgumentType<Collection<Holder.Reference<T>>> {
    private static final Collection<String> EXAMPLES = List.of((Object)"minecraft:*", (Object)"*:asset", (Object)"*");
    public static final Dynamic2CommandExceptionType ERROR_NO_MATCHES = new Dynamic2CommandExceptionType((object, object2) -> Component.translatableEscape("argument.resource_selector.not_found", object, object2));
    final ResourceKey<? extends Registry<T>> registryKey;
    private final HolderLookup<T> registryLookup;

    ResourceSelectorArgument(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
        this.registryKey = resourceKey;
        this.registryLookup = commandBuildContext.lookupOrThrow(resourceKey);
    }

    public Collection<Holder.Reference<T>> parse(StringReader stringReader) throws CommandSyntaxException {
        String string = ResourceSelectorArgument.ensureNamespaced(ResourceSelectorArgument.readPattern(stringReader));
        List list = this.registryLookup.listElements().filter(reference -> ResourceSelectorArgument.matches(string, reference.key().identifier())).toList();
        if (list.isEmpty()) {
            throw ERROR_NO_MATCHES.createWithContext((ImmutableStringReader)stringReader, (Object)string, (Object)this.registryKey.identifier());
        }
        return list;
    }

    public static <T> Collection<Holder.Reference<T>> parse(StringReader stringReader, HolderLookup<T> holderLookup) {
        String string = ResourceSelectorArgument.ensureNamespaced(ResourceSelectorArgument.readPattern(stringReader));
        return holderLookup.listElements().filter(reference -> ResourceSelectorArgument.matches(string, reference.key().identifier())).toList();
    }

    private static String readPattern(StringReader stringReader) {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && ResourceSelectorArgument.isAllowedPatternCharacter(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(i, stringReader.getCursor());
    }

    private static boolean isAllowedPatternCharacter(char c) {
        return Identifier.isAllowedInIdentifier(c) || c == '*' || c == '?';
    }

    private static String ensureNamespaced(String string) {
        if (!string.contains(":")) {
            return "minecraft:" + string;
        }
        return string;
    }

    private static boolean matches(String string, Identifier identifier) {
        return FilenameUtils.wildcardMatch((String)identifier.toString(), (String)string);
    }

    public static <T> ResourceSelectorArgument<T> resourceSelector(CommandBuildContext commandBuildContext, ResourceKey<? extends Registry<T>> resourceKey) {
        return new ResourceSelectorArgument<T>(commandBuildContext, resourceKey);
    }

    public static <T> Collection<Holder.Reference<T>> getSelectedResources(CommandContext<CommandSourceStack> commandContext, String string) {
        return (Collection)commandContext.getArgument(string, Collection.class);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.listSuggestions(commandContext, suggestionsBuilder, this.registryKey, SharedSuggestionProvider.ElementSuggestionType.ELEMENTS);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }

    public static class Info<T>
    implements ArgumentTypeInfo<ResourceSelectorArgument<T>, Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeResourceKey(template.registryKey);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return new Template(friendlyByteBuf.readRegistryKey());
        }

        @Override
        public void serializeToJson(Template template, JsonObject jsonObject) {
            jsonObject.addProperty("registry", template.registryKey.identifier().toString());
        }

        @Override
        public Template unpack(ResourceSelectorArgument<T> resourceSelectorArgument) {
            return new Template(resourceSelectorArgument.registryKey);
        }

        @Override
        public /* synthetic */ ArgumentTypeInfo.Template deserializeFromNetwork(FriendlyByteBuf friendlyByteBuf) {
            return this.deserializeFromNetwork(friendlyByteBuf);
        }

        public final class Template
        implements ArgumentTypeInfo.Template<ResourceSelectorArgument<T>> {
            final ResourceKey<? extends Registry<T>> registryKey;

            Template(ResourceKey<? extends Registry<T>> resourceKey) {
                this.registryKey = resourceKey;
            }

            @Override
            public ResourceSelectorArgument<T> instantiate(CommandBuildContext commandBuildContext) {
                return new ResourceSelectorArgument(commandBuildContext, this.registryKey);
            }

            @Override
            public ArgumentTypeInfo<ResourceSelectorArgument<T>, ?> type() {
                return Info.this;
            }

            @Override
            public /* synthetic */ ArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return this.instantiate(commandBuildContext);
            }
        }
    }
}

