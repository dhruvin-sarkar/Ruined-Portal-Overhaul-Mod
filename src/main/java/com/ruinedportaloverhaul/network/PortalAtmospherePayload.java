package com.ruinedportaloverhaul.network;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record PortalAtmospherePayload(float intensity, float descent) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<PortalAtmospherePayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "portal_atmosphere")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, PortalAtmospherePayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        PortalAtmospherePayload::intensity,
        ByteBufCodecs.FLOAT,
        PortalAtmospherePayload::descent,
        PortalAtmospherePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
