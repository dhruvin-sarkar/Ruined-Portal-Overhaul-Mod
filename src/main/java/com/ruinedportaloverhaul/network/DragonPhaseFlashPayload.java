package com.ruinedportaloverhaul.network;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record DragonPhaseFlashPayload(int ticks) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<DragonPhaseFlashPayload> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "dragon_phase_flash")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DragonPhaseFlashPayload> CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_INT,
        DragonPhaseFlashPayload::ticks,
        DragonPhaseFlashPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
