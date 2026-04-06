/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.resources;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import java.util.function.UnaryOperator;
import net.minecraft.IdentifierException;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

public final class Identifier
implements Comparable<Identifier> {
    public static final Codec<Identifier> CODEC = Codec.STRING.comapFlatMap(Identifier::read, Identifier::toString).stable();
    public static final StreamCodec<ByteBuf, Identifier> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Identifier::parse, Identifier::toString);
    public static final SimpleCommandExceptionType ERROR_INVALID = new SimpleCommandExceptionType((Message)Component.translatable("argument.id.invalid"));
    public static final char NAMESPACE_SEPARATOR = ':';
    public static final String DEFAULT_NAMESPACE = "minecraft";
    public static final String REALMS_NAMESPACE = "realms";
    private final String namespace;
    private final String path;

    private Identifier(String string, String string2) {
        assert (Identifier.isValidNamespace(string));
        assert (Identifier.isValidPath(string2));
        this.namespace = string;
        this.path = string2;
    }

    private static Identifier createUntrusted(String string, String string2) {
        return new Identifier(Identifier.assertValidNamespace(string, string2), Identifier.assertValidPath(string, string2));
    }

    public static Identifier fromNamespaceAndPath(String string, String string2) {
        return Identifier.createUntrusted(string, string2);
    }

    public static Identifier parse(String string) {
        return Identifier.bySeparator(string, ':');
    }

    public static Identifier withDefaultNamespace(String string) {
        return new Identifier(DEFAULT_NAMESPACE, Identifier.assertValidPath(DEFAULT_NAMESPACE, string));
    }

    public static @Nullable Identifier tryParse(String string) {
        return Identifier.tryBySeparator(string, ':');
    }

    public static @Nullable Identifier tryBuild(String string, String string2) {
        if (Identifier.isValidNamespace(string) && Identifier.isValidPath(string2)) {
            return new Identifier(string, string2);
        }
        return null;
    }

    public static Identifier bySeparator(String string, char c) {
        int i = string.indexOf(c);
        if (i >= 0) {
            String string2 = string.substring(i + 1);
            if (i != 0) {
                String string3 = string.substring(0, i);
                return Identifier.createUntrusted(string3, string2);
            }
            return Identifier.withDefaultNamespace(string2);
        }
        return Identifier.withDefaultNamespace(string);
    }

    public static @Nullable Identifier tryBySeparator(String string, char c) {
        int i = string.indexOf(c);
        if (i >= 0) {
            String string2 = string.substring(i + 1);
            if (!Identifier.isValidPath(string2)) {
                return null;
            }
            if (i != 0) {
                String string3 = string.substring(0, i);
                return Identifier.isValidNamespace(string3) ? new Identifier(string3, string2) : null;
            }
            return new Identifier(DEFAULT_NAMESPACE, string2);
        }
        return Identifier.isValidPath(string) ? new Identifier(DEFAULT_NAMESPACE, string) : null;
    }

    public static DataResult<Identifier> read(String string) {
        try {
            return DataResult.success((Object)Identifier.parse(string));
        }
        catch (IdentifierException identifierException) {
            return DataResult.error(() -> "Not a valid resource location: " + string + " " + identifierException.getMessage());
        }
    }

    public String getPath() {
        return this.path;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public Identifier withPath(String string) {
        return new Identifier(this.namespace, Identifier.assertValidPath(this.namespace, string));
    }

    public Identifier withPath(UnaryOperator<String> unaryOperator) {
        return this.withPath((String)unaryOperator.apply(this.path));
    }

    public Identifier withPrefix(String string) {
        return this.withPath(string + this.path);
    }

    public Identifier withSuffix(String string) {
        return this.withPath(this.path + string);
    }

    public String toString() {
        return this.namespace + ":" + this.path;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Identifier) {
            Identifier identifier = (Identifier)object;
            return this.namespace.equals(identifier.namespace) && this.path.equals(identifier.path);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.namespace.hashCode() + this.path.hashCode();
    }

    @Override
    public int compareTo(Identifier identifier) {
        int i = this.path.compareTo(identifier.path);
        if (i == 0) {
            i = this.namespace.compareTo(identifier.namespace);
        }
        return i;
    }

    public String toDebugFileName() {
        return this.toString().replace('/', '_').replace(':', '_');
    }

    public String toLanguageKey() {
        return this.namespace + "." + this.path;
    }

    public String toShortLanguageKey() {
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toLanguageKey();
    }

    public String toShortString() {
        return this.namespace.equals(DEFAULT_NAMESPACE) ? this.path : this.toString();
    }

    public String toLanguageKey(String string) {
        return string + "." + this.toLanguageKey();
    }

    public String toLanguageKey(String string, String string2) {
        return string + "." + this.toLanguageKey() + "." + string2;
    }

    private static String readGreedy(StringReader stringReader) {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && Identifier.isAllowedInIdentifier(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(i, stringReader.getCursor());
    }

    public static Identifier read(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        String string = Identifier.readGreedy(stringReader);
        try {
            return Identifier.parse(string);
        }
        catch (IdentifierException identifierException) {
            stringReader.setCursor(i);
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)stringReader);
        }
    }

    public static Identifier readNonEmpty(StringReader stringReader) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        String string = Identifier.readGreedy(stringReader);
        if (string.isEmpty()) {
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)stringReader);
        }
        try {
            return Identifier.parse(string);
        }
        catch (IdentifierException identifierException) {
            stringReader.setCursor(i);
            throw ERROR_INVALID.createWithContext((ImmutableStringReader)stringReader);
        }
    }

    public static boolean isAllowedInIdentifier(char c) {
        return c >= '0' && c <= '9' || c >= 'a' && c <= 'z' || c == '_' || c == ':' || c == '/' || c == '.' || c == '-';
    }

    public static boolean isValidPath(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (Identifier.validPathChar(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    public static boolean isValidNamespace(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (Identifier.validNamespaceChar(string.charAt(i))) continue;
            return false;
        }
        return true;
    }

    private static String assertValidNamespace(String string, String string2) {
        if (!Identifier.isValidNamespace(string)) {
            throw new IdentifierException("Non [a-z0-9_.-] character in namespace of location: " + string + ":" + string2);
        }
        return string;
    }

    public static boolean validPathChar(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '/' || c == '.';
    }

    private static boolean validNamespaceChar(char c) {
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    private static String assertValidPath(String string, String string2) {
        if (!Identifier.isValidPath(string2)) {
            throw new IdentifierException("Non [a-z0-9/._-] character in path of location: " + string + ":" + string2);
        }
        return string2;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((Identifier)object);
    }
}

