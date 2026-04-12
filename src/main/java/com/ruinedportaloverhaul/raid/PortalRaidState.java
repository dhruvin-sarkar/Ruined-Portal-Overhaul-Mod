package com.ruinedportaloverhaul.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class PortalRaidState extends SavedData {
    private static final String COMPLETED_PORTALS_KEY = "completed_portals";

    public static final Codec<PortalRaidState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.listOf()
            .fieldOf(COMPLETED_PORTALS_KEY)
            .forGetter(state -> new ArrayList<>(state.completedPortals))
    ).apply(instance, PortalRaidState::new));

    private static final SavedDataType<PortalRaidState> TYPE = new SavedDataType<>(
        RuinedPortalOverhaul.MOD_ID + "_raid_state",
        PortalRaidState::new,
        CODEC,
        null
    );

    private final Set<BlockPos> completedPortals = new HashSet<>();

    public PortalRaidState() {
    }

    private PortalRaidState(List<BlockPos> completedPortals) {
        this.completedPortals.addAll(completedPortals);
    }

    public static PortalRaidState get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isCompleted(BlockPos portalOrigin) {
        return this.completedPortals.contains(portalOrigin.immutable());
    }

    public void markCompleted(BlockPos portalOrigin) {
        if (this.completedPortals.add(portalOrigin.immutable())) {
            this.setDirty();
        }
    }
}
