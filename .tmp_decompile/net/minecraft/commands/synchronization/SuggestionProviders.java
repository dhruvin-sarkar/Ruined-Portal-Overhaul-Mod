/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.commands.synchronization;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class SuggestionProviders {
    private static final Map<Identifier, SuggestionProvider<SharedSuggestionProvider>> PROVIDERS_BY_NAME = new HashMap<Identifier, SuggestionProvider<SharedSuggestionProvider>>();
    private static final Identifier ID_ASK_SERVER = Identifier.withDefaultNamespace("ask_server");
    public static final SuggestionProvider<SharedSuggestionProvider> ASK_SERVER = SuggestionProviders.register(ID_ASK_SERVER, (SuggestionProvider<SharedSuggestionProvider>)((SuggestionProvider)(commandContext, suggestionsBuilder) -> ((SharedSuggestionProvider)commandContext.getSource()).customSuggestion(commandContext)));
    public static final SuggestionProvider<SharedSuggestionProvider> AVAILABLE_SOUNDS = SuggestionProviders.register(Identifier.withDefaultNamespace("available_sounds"), (SuggestionProvider<SharedSuggestionProvider>)((SuggestionProvider)(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(((SharedSuggestionProvider)commandContext.getSource()).getAvailableSounds(), suggestionsBuilder)));
    public static final SuggestionProvider<SharedSuggestionProvider> SUMMONABLE_ENTITIES = SuggestionProviders.register(Identifier.withDefaultNamespace("summonable_entities"), (SuggestionProvider<SharedSuggestionProvider>)((SuggestionProvider)(commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggestResource(BuiltInRegistries.ENTITY_TYPE.stream().filter(entityType -> entityType.isEnabled(((SharedSuggestionProvider)commandContext.getSource()).enabledFeatures()) && entityType.canSummon()), suggestionsBuilder, EntityType::getKey, EntityType::getDescription)));

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> register(Identifier identifier, SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
        SuggestionProvider<SharedSuggestionProvider> suggestionProvider2 = PROVIDERS_BY_NAME.putIfAbsent(identifier, suggestionProvider);
        if (suggestionProvider2 != null) {
            throw new IllegalArgumentException("A command suggestion provider is already registered with the name '" + String.valueOf(identifier) + "'");
        }
        return new RegisteredSuggestion(identifier, suggestionProvider);
    }

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> cast(SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
        return suggestionProvider;
    }

    public static <S extends SharedSuggestionProvider> SuggestionProvider<S> getProvider(Identifier identifier) {
        return SuggestionProviders.cast(PROVIDERS_BY_NAME.getOrDefault(identifier, ASK_SERVER));
    }

    public static Identifier getName(SuggestionProvider<?> suggestionProvider) {
        Identifier identifier;
        if (suggestionProvider instanceof RegisteredSuggestion) {
            RegisteredSuggestion registeredSuggestion = (RegisteredSuggestion)suggestionProvider;
            identifier = registeredSuggestion.name;
        } else {
            identifier = ID_ASK_SERVER;
        }
        return identifier;
    }

    static final class RegisteredSuggestion
    extends Record
    implements SuggestionProvider<SharedSuggestionProvider> {
        final Identifier name;
        private final SuggestionProvider<SharedSuggestionProvider> delegate;

        RegisteredSuggestion(Identifier identifier, SuggestionProvider<SharedSuggestionProvider> suggestionProvider) {
            this.name = identifier;
            this.delegate = suggestionProvider;
        }

        public CompletableFuture<Suggestions> getSuggestions(CommandContext<SharedSuggestionProvider> commandContext, SuggestionsBuilder suggestionsBuilder) throws CommandSyntaxException {
            return this.delegate.getSuggestions(commandContext, suggestionsBuilder);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegisteredSuggestion.class, "name;delegate", "name", "delegate"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegisteredSuggestion.class, "name;delegate", "name", "delegate"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegisteredSuggestion.class, "name;delegate", "name", "delegate"}, this, object);
        }

        public Identifier name() {
            return this.name;
        }

        public SuggestionProvider<SharedSuggestionProvider> delegate() {
            return this.delegate;
        }
    }
}

