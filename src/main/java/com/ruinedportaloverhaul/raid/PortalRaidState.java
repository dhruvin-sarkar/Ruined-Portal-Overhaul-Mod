package com.ruinedportaloverhaul.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class PortalRaidState extends SavedData {
    private static final String COMPLETED_PORTALS_KEY = "completed_portals";
    private static final String ACTIVE_RAIDS_KEY = "active_raids";

    public static final Codec<PortalRaidState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.listOf()
            .fieldOf(COMPLETED_PORTALS_KEY)
            .forGetter(state -> new ArrayList<>(state.completedPortals)),
        ActiveRaidData.CODEC.listOf()
            .optionalFieldOf(ACTIVE_RAIDS_KEY, List.of())
            .forGetter(PortalRaidState::snapshotActiveRaids)
    ).apply(instance, PortalRaidState::new));

    private static final SavedDataType<PortalRaidState> TYPE = new SavedDataType<>(
        RuinedPortalOverhaul.MOD_ID + "_raid_state",
        PortalRaidState::new,
        CODEC,
        null
    );

    private final Set<BlockPos> completedPortals = new HashSet<>();
    private final Set<BlockPos> activeRaidLocations = new HashSet<>();
    private final Map<BlockPos, Set<java.util.UUID>> waveTrackerUuids = new HashMap<>();
    private final Map<BlockPos, Integer> currentWaveNumbers = new HashMap<>();
    private final Map<BlockPos, Long> waveEndTimeTicks = new HashMap<>();

    public PortalRaidState() {
    }

    private PortalRaidState(List<BlockPos> completedPortals, List<ActiveRaidData> activeRaids) {
        this.completedPortals.addAll(completedPortals);
        for (ActiveRaidData activeRaid : activeRaids) {
            BlockPos portalOrigin = activeRaid.portalOrigin().immutable();
            this.activeRaidLocations.add(portalOrigin);
            this.currentWaveNumbers.put(portalOrigin, activeRaid.currentWaveNumber());
            this.waveEndTimeTicks.put(portalOrigin, activeRaid.waveEndTimeTicks());
            this.waveTrackerUuids.put(portalOrigin, new HashSet<>(activeRaid.waveMobs()));
        }
    }

    public static PortalRaidState get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isCompleted(BlockPos portalOrigin) {
        return this.completedPortals.contains(portalOrigin.immutable());
    }

    public boolean isRaidActive(BlockPos portalOrigin) {
        return this.activeRaidLocations.contains(portalOrigin.immutable());
    }

    public boolean beginRaid(BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        if (!this.activeRaidLocations.add(origin)) {
            return false;
        }
        this.currentWaveNumbers.put(origin, 0);
        this.waveEndTimeTicks.put(origin, 0L);
        this.waveTrackerUuids.put(origin, new HashSet<>());
        this.setDirty();
        return true;
    }

    public void updateWave(BlockPos portalOrigin, int currentWaveNumber, Set<java.util.UUID> waveMobs, long waveEndTimeTick) {
        BlockPos origin = portalOrigin.immutable();
        this.activeRaidLocations.add(origin);
        this.currentWaveNumbers.put(origin, currentWaveNumber);
        this.waveEndTimeTicks.put(origin, waveEndTimeTick);
        this.waveTrackerUuids.put(origin, new HashSet<>(waveMobs));
        this.setDirty();
    }

    public void clearActiveRaid(BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        boolean changed = this.activeRaidLocations.remove(origin);
        changed |= this.currentWaveNumbers.remove(origin) != null;
        changed |= this.waveEndTimeTicks.remove(origin) != null;
        changed |= this.waveTrackerUuids.remove(origin) != null;
        if (changed) {
            this.setDirty();
        }
    }

    public void markCompleted(BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        this.clearActiveRaid(origin);
        if (this.completedPortals.add(origin)) {
            this.setDirty();
        }
    }

    public List<ActiveRaidSnapshot> activeRaidSnapshots() {
        List<ActiveRaidSnapshot> snapshots = new ArrayList<>();
        for (BlockPos portalOrigin : this.activeRaidLocations) {
            snapshots.add(new ActiveRaidSnapshot(
                portalOrigin,
                this.currentWaveNumbers.getOrDefault(portalOrigin, 0),
                this.waveEndTimeTicks.getOrDefault(portalOrigin, 0L),
                new HashSet<>(this.waveTrackerUuids.getOrDefault(portalOrigin, Set.of()))
            ));
        }
        return snapshots;
    }

    private List<ActiveRaidData> snapshotActiveRaids() {
        List<ActiveRaidData> activeRaids = new ArrayList<>();
        for (BlockPos portalOrigin : this.activeRaidLocations) {
            activeRaids.add(new ActiveRaidData(
                portalOrigin,
                this.currentWaveNumbers.getOrDefault(portalOrigin, 0),
                this.waveEndTimeTicks.getOrDefault(portalOrigin, 0L),
                this.waveTrackerUuids.getOrDefault(portalOrigin, Set.of())
            ));
        }
        return activeRaids;
    }

    private record ActiveRaidData(
        BlockPos portalOrigin,
        int currentWaveNumber,
        long waveEndTimeTicks,
        Set<java.util.UUID> waveMobs
    ) {
        private static final Codec<ActiveRaidData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("portal_origin").forGetter(ActiveRaidData::portalOrigin),
            Codec.INT.fieldOf("current_wave_number").forGetter(ActiveRaidData::currentWaveNumber),
            Codec.LONG.fieldOf("wave_end_time_ticks").forGetter(ActiveRaidData::waveEndTimeTicks),
            UUIDUtil.CODEC_SET.fieldOf("wave_mobs").forGetter(ActiveRaidData::waveMobs)
        ).apply(instance, ActiveRaidData::new));
    }

    public record ActiveRaidSnapshot(
        BlockPos portalOrigin,
        int currentWaveNumber,
        long waveEndTimeTicks,
        Set<java.util.UUID> waveMobs
    ) {
    }
}
