/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.jspecify.annotations.Nullable;

public class ClientboundStopSoundPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundStopSoundPacket> STREAM_CODEC = Packet.codec(ClientboundStopSoundPacket::write, ClientboundStopSoundPacket::new);
    private static final int HAS_SOURCE = 1;
    private static final int HAS_SOUND = 2;
    private final @Nullable Identifier name;
    private final @Nullable SoundSource source;

    public ClientboundStopSoundPacket(@Nullable Identifier identifier, @Nullable SoundSource soundSource) {
        this.name = identifier;
        this.source = soundSource;
    }

    private ClientboundStopSoundPacket(FriendlyByteBuf friendlyByteBuf) {
        byte i = friendlyByteBuf.readByte();
        this.source = (i & 1) > 0 ? friendlyByteBuf.readEnum(SoundSource.class) : null;
        this.name = (i & 2) > 0 ? friendlyByteBuf.readIdentifier() : null;
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        if (this.source != null) {
            if (this.name != null) {
                friendlyByteBuf.writeByte(3);
                friendlyByteBuf.writeEnum(this.source);
                friendlyByteBuf.writeIdentifier(this.name);
            } else {
                friendlyByteBuf.writeByte(1);
                friendlyByteBuf.writeEnum(this.source);
            }
        } else if (this.name != null) {
            friendlyByteBuf.writeByte(2);
            friendlyByteBuf.writeIdentifier(this.name);
        } else {
            friendlyByteBuf.writeByte(0);
        }
    }

    @Override
    public PacketType<ClientboundStopSoundPacket> type() {
        return GamePacketTypes.CLIENTBOUND_STOP_SOUND;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleStopSoundEvent(this);
    }

    public @Nullable Identifier getName() {
        return this.name;
    }

    public @Nullable SoundSource getSource() {
        return this.source;
    }
}

