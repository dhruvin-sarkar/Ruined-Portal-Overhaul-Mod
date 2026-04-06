/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class IdentifierPattern {
    public static final Codec<IdentifierPattern> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.PATTERN.optionalFieldOf("namespace").forGetter(identifierPattern -> identifierPattern.namespacePattern), (App)ExtraCodecs.PATTERN.optionalFieldOf("path").forGetter(identifierPattern -> identifierPattern.pathPattern)).apply((Applicative)instance, IdentifierPattern::new));
    private final Optional<Pattern> namespacePattern;
    private final Predicate<String> namespacePredicate;
    private final Optional<Pattern> pathPattern;
    private final Predicate<String> pathPredicate;
    private final Predicate<Identifier> locationPredicate;

    private IdentifierPattern(Optional<Pattern> optional, Optional<Pattern> optional2) {
        this.namespacePattern = optional;
        this.namespacePredicate = optional.map(Pattern::asPredicate).orElse(string -> true);
        this.pathPattern = optional2;
        this.pathPredicate = optional2.map(Pattern::asPredicate).orElse(string -> true);
        this.locationPredicate = identifier -> this.namespacePredicate.test(identifier.getNamespace()) && this.pathPredicate.test(identifier.getPath());
    }

    public Predicate<String> namespacePredicate() {
        return this.namespacePredicate;
    }

    public Predicate<String> pathPredicate() {
        return this.pathPredicate;
    }

    public Predicate<Identifier> locationPredicate() {
        return this.locationPredicate;
    }
}

