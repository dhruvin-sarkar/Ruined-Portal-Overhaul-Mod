/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DiscardedPayload(Identifier id) implements CustomPacketPayload
{
    public static <T extends FriendlyByteBuf> StreamCodec<T, DiscardedPayload> codec(Identifier identifier, int i) {
        return CustomPacketPayload.codec((T discardedPayload, B friendlyByteBuf) -> {}, (B friendlyByteBuf) -> {
            int j = friendlyByteBuf.readableBytes();
            if (j < 0 || j > i) {
                throw new IllegalArgumentException("Payload may not be larger than " + i + " bytes");
            }
            friendlyByteBuf.skipBytes(j);
            return new DiscardedPayload(identifier);
        });
    }

    public CustomPacketPayload.Type<DiscardedPayload> type() {
        return new CustomPacketPayload.Type<DiscardedPayload>(this.id);
    }
}

