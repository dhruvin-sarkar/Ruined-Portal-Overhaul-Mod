/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.player;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import java.util.function.Function;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum PlayerModelType implements StringRepresentable
{
    SLIM("slim", "slim"),
    WIDE("wide", "default");

    public static final Codec<PlayerModelType> CODEC;
    private static final Function<String, PlayerModelType> NAME_LOOKUP;
    public static final StreamCodec<ByteBuf, PlayerModelType> STREAM_CODEC;
    private final String id;
    private final String legacyServicesId;

    private PlayerModelType(String string2, String string3) {
        this.id = string2;
        this.legacyServicesId = string3;
    }

    public static PlayerModelType byLegacyServicesName(@Nullable String string) {
        return (PlayerModelType)Objects.requireNonNullElse((Object)NAME_LOOKUP.apply(string), (Object)WIDE);
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    static {
        CODEC = StringRepresentable.fromEnum(PlayerModelType::values);
        NAME_LOOKUP = StringRepresentable.createNameLookup(PlayerModelType.values(), playerModelType -> playerModelType.legacyServicesId);
        STREAM_CODEC = ByteBufCodecs.BOOL.map(boolean_ -> boolean_ != false ? SLIM : WIDE, playerModelType -> playerModelType == SLIM);
    }
}

