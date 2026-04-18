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
    private static final String ACTIVATED_PORTALS_KEY = "activated_portals";
    private static final String PORTAL_SPAWNERS_KEY = "portal_spawners";

    public static final Codec<PortalRaidState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.listOf()
            .fieldOf(COMPLETED_PORTALS_KEY)
            .forGetter(state -> new ArrayList<>(state.completedPortals)),
        ActiveRaidData.CODEC.listOf()
            .optionalFieldOf(ACTIVE_RAIDS_KEY, List.of())
            .forGetter(PortalRaidState::snapshotActiveRaids),
        BlockPos.CODEC.listOf()
            .optionalFieldOf(ACTIVATED_PORTALS_KEY, List.of())
            .forGetter(state -> new ArrayList<>(state.activatedPortals)),
        PortalSpawnerData.CODEC.listOf()
            .optionalFieldOf(PORTAL_SPAWNERS_KEY, List.of())
            .forGetter(PortalRaidState::snapshotPortalSpawners)
    ).apply(instance, PortalRaidState::new));

    private static final SavedDataType<PortalRaidState> TYPE = new SavedDataType<>(
        RuinedPortalOverhaul.MOD_ID + "_raid_state",
        PortalRaidState::new,
        CODEC,
        null
    );

    private final Set<BlockPos> completedPortals = new HashSet<>();
    private final Set<BlockPos> activatedPortals = new HashSet<>();
    private final Set<BlockPos> activeRaidLocations = new HashSet<>();
    private final Map<BlockPos, List<BlockPos>> portalSpawners = new HashMap<>();
    private final Map<BlockPos, Set<java.util.UUID>> waveTrackerUuids = new HashMap<>();
    private final Map<BlockPos, Integer> currentWaveNumbers = new HashMap<>();
    private final Map<BlockPos, Long> waveEndTimeTicks = new HashMap<>();

    public PortalRaidState() {
    }

    private PortalRaidState(
        List<BlockPos> completedPortals,
        List<ActiveRaidData> activeRaids,
        List<BlockPos> activatedPortals,
        List<PortalSpawnerData> portalSpawners
    ) {
        this.completedPortals.addAll(completedPortals);
        this.activatedPortals.addAll(activatedPortals);
        for (ActiveRaidData activeRaid : activeRaids) {
            BlockPos portalOrigin = activeRaid.portalOrigin().immutable();
            this.activeRaidLocations.add(portalOrigin);
            this.currentWaveNumbers.put(portalOrigin, activeRaid.currentWaveNumber());
            this.waveEndTimeTicks.put(portalOrigin, activeRaid.waveEndTimeTicks());
            this.waveTrackerUuids.put(portalOrigin, new HashSet<>(activeRaid.waveMobs()));
        }
        for (PortalSpawnerData spawnerData : portalSpawners) {
            this.portalSpawners.put(spawnerData.portalOrigin().immutable(), immutablePositions(spawnerData.spawners()));
        }
    }

    public static PortalRaidState get(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        return overworld.getDataStorage().computeIfAbsent(TYPE);
    }

    public boolean isCompleted(BlockPos portalOrigin) {
        return this.completedPortals.contains(portalOrigin.immutable());
    }

    public Set<BlockPos> completedPortalOrigins() {
        return Set.copyOf(this.completedPortals);
    }

    public boolean isRaidActive(BlockPos portalOrigin) {
        return this.activeRaidLocations.contains(portalOrigin.immutable());
    }

    public boolean isApproachActivated(BlockPos portalOrigin) {
        return this.activatedPortals.contains(portalOrigin.immutable());
    }

    public boolean markApproachActivated(BlockPos portalOrigin) {
        if (this.activatedPortals.add(portalOrigin.immutable())) {
            this.setDirty();
            return true;
        }
        return false;
    }

    public void setPortalSpawners(BlockPos portalOrigin, List<BlockPos> spawners) {
        BlockPos origin = portalOrigin.immutable();
        List<BlockPos> immutableSpawners = immutablePositions(spawners);
        if (!immutableSpawners.equals(this.portalSpawners.get(origin))) {
            this.portalSpawners.put(origin, immutableSpawners);
            this.setDirty();
        }
    }

    public List<BlockPos> portalSpawners(BlockPos portalOrigin) {
        return new ArrayList<>(this.portalSpawners.getOrDefault(portalOrigin.immutable(), List.of()));
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
        boolean changed = this.completedPortals.add(origin);
        changed |= this.portalSpawners.remove(origin) != null;
        if (changed) {
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

    private List<PortalSpawnerData> snapshotPortalSpawners() {
        List<PortalSpawnerData> spawners = new ArrayList<>();
        for (Map.Entry<BlockPos, List<BlockPos>> entry : this.portalSpawners.entrySet()) {
            spawners.add(new PortalSpawnerData(entry.getKey(), entry.getValue()));
        }
        return spawners;
    }

    private static List<BlockPos> immutablePositions(List<BlockPos> positions) {
        List<BlockPos> immutable = new ArrayList<>();
        for (BlockPos pos : positions) {
            immutable.add(pos.immutable());
        }
        return immutable;
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

    private record PortalSpawnerData(BlockPos portalOrigin, List<BlockPos> spawners) {
        private static final Codec<PortalSpawnerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("portal_origin").forGetter(PortalSpawnerData::portalOrigin),
            BlockPos.CODEC.listOf().fieldOf("spawners").forGetter(PortalSpawnerData::spawners)
        ).apply(instance, PortalSpawnerData::new));
    }

    public record ActiveRaidSnapshot(
        BlockPos portalOrigin,
        int currentWaveNumber,
        long waveEndTimeTicks,
        Set<java.util.UUID> waveMobs
    ) {
    }
}
