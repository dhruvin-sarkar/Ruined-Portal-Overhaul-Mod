/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.permissions;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;

public interface Permission {
    public static final Codec<Permission> FULL_CODEC = BuiltInRegistries.PERMISSION_TYPE.byNameCodec().dispatch(Permission::codec, mapCodec -> mapCodec);
    public static final Codec<Permission> CODEC = Codec.either(FULL_CODEC, Identifier.CODEC).xmap(either -> (Permission)either.map(permission -> permission, Atom::create), permission -> {
        Either either;
        if (permission instanceof Atom) {
            Atom atom = (Atom)permission;
            either = Either.right((Object)atom.id());
        } else {
            either = Either.left((Object)permission);
        }
        return either;
    });

    public MapCodec<? extends Permission> codec();

    public record Atom(Identifier id) implements Permission
    {
        public static final MapCodec<Atom> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("id").forGetter(Atom::id)).apply((Applicative)instance, Atom::new));

        public MapCodec<Atom> codec() {
            return MAP_CODEC;
        }

        public static Atom create(String string) {
            return Atom.create(Identifier.withDefaultNamespace(string));
        }

        public static Atom create(Identifier identifier) {
            return new Atom(identifier);
        }
    }

    public record HasCommandLevel(PermissionLevel level) implements Permission
    {
        public static final MapCodec<HasCommandLevel> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)PermissionLevel.CODEC.fieldOf("level").forGetter(HasCommandLevel::level)).apply((Applicative)instance, HasCommandLevel::new));

        public MapCodec<HasCommandLevel> codec() {
            return MAP_CODEC;
        }
    }
}

