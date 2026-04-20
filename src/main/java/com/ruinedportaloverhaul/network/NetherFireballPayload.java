package com.ruinedportaloverhaul.network;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record NetherFireballPayload() implements CustomPacketPayload {
    public static final NetherFireballPayload INSTANCE = new NetherFireballPayload();
    public static final CustomPacketPayload.Type<NetherFireballPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "nether_fireball")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, NetherFireballPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
