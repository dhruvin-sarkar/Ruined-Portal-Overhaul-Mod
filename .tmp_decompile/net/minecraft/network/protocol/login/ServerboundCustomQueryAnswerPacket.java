/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.login;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.login.LoginPacketTypes;
import net.minecraft.network.protocol.login.ServerLoginPacketListener;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;
import net.minecraft.network.protocol.login.custom.DiscardedQueryAnswerPayload;
import org.jspecify.annotations.Nullable;

public record ServerboundCustomQueryAnswerPacket(int transactionId, @Nullable CustomQueryAnswerPayload payload) implements Packet<ServerLoginPacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundCustomQueryAnswerPacket> STREAM_CODEC = Packet.codec(ServerboundCustomQueryAnswerPacket::write, ServerboundCustomQueryAnswerPacket::read);
    private static final int MAX_PAYLOAD_SIZE = 0x100000;

    private static ServerboundCustomQueryAnswerPacket read(FriendlyByteBuf friendlyByteBuf) {
        int i = friendlyByteBuf.readVarInt();
        return new ServerboundCustomQueryAnswerPacket(i, ServerboundCustomQueryAnswerPacket.readPayload(i, friendlyByteBuf));
    }

    private static CustomQueryAnswerPayload readPayload(int i, FriendlyByteBuf friendlyByteBuf) {
        return ServerboundCustomQueryAnswerPacket.readUnknownPayload(friendlyByteBuf);
    }

    private static CustomQueryAnswerPayload readUnknownPayload(FriendlyByteBuf friendlyByteBuf) {
        int i = friendlyByteBuf.readableBytes();
        if (i < 0 || i > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        friendlyByteBuf.skipBytes(i);
        return DiscardedQueryAnswerPayload.INSTANCE;
    }

    private void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeVarInt(this.transactionId);
        friendlyByteBuf2.writeNullable(this.payload, (friendlyByteBuf, customQueryAnswerPayload) -> customQueryAnswerPayload.write((FriendlyByteBuf)((Object)friendlyByteBuf)));
    }

    @Override
    public PacketType<ServerboundCustomQueryAnswerPacket> type() {
        return LoginPacketTypes.SERVERBOUND_CUSTOM_QUERY_ANSWER;
    }

    @Override
    public void handle(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.handleCustomQueryPacket(this);
    }
}

