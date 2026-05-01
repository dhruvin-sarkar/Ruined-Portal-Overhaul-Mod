package com.ruinedportaloverhaul.network;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NetherFireballPayload(double lookX, double lookY, double lookZ) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<NetherFireballPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "nether_fireball")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NetherFireballPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.DOUBLE,
        NetherFireballPayload::lookX,
        ByteBufCodecs.DOUBLE,
        NetherFireballPayload::lookY,
        ByteBufCodecs.DOUBLE,
        NetherFireballPayload::lookZ,
        NetherFireballPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
