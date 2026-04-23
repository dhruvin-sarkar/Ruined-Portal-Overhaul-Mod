package com.ruinedportaloverhaul.raid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.structure.PortalDungeonVariant;
import com.ruinedportaloverhaul.structure.PortalStructureHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private static final String PORTAL_VARIANTS_KEY = "portal_variants";
    private static final String RITUAL_CRYSTALS_KEY = "ritual_crystals";
    private static final String ACTIVE_DRAGON_PORTALS_KEY = "active_dragon_portals";

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
            .forGetter(PortalRaidState::snapshotPortalSpawners),
        PortalVariantData.CODEC.listOf()
            .optionalFieldOf(PORTAL_VARIANTS_KEY, List.of())
            .forGetter(PortalRaidState::snapshotPortalVariants),
        RitualData.CODEC.listOf()
            .optionalFieldOf(RITUAL_CRYSTALS_KEY, List.of())
            .forGetter(PortalRaidState::snapshotRitualCrystals),
        BlockPos.CODEC.listOf()
            .optionalFieldOf(ACTIVE_DRAGON_PORTALS_KEY, List.of())
            .forGetter(state -> new ArrayList<>(state.activeDragonPortals))
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
    private final Map<BlockPos, Integer> portalVariants = new HashMap<>();
    private final Map<BlockPos, Set<java.util.UUID>> waveTrackerUuids = new HashMap<>();
    private final Map<BlockPos, Integer> currentWaveNumbers = new HashMap<>();
    private final Map<BlockPos, Long> waveEndTimeTicks = new HashMap<>();
    private final Map<BlockPos, Set<BlockPos>> ritualCrystals = new HashMap<>();
    private final Set<BlockPos> activeDragonPortals = new HashSet<>();

    public PortalRaidState() {
    }

    private PortalRaidState(
        List<BlockPos> completedPortals,
        List<ActiveRaidData> activeRaids,
        List<BlockPos> activatedPortals,
        List<PortalSpawnerData> portalSpawners,
        List<PortalVariantData> portalVariants,
        List<RitualData> ritualCrystals,
        List<BlockPos> activeDragonPortals
    ) {
        this.completedPortals.addAll(completedPortals);
        this.activatedPortals.addAll(activatedPortals);
        this.activeDragonPortals.addAll(activeDragonPortals);
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
        for (PortalVariantData variantData : portalVariants) {
            this.portalVariants.put(variantData.portalOrigin().immutable(), variantData.variantId());
        }
        for (RitualData ritualData : ritualCrystals) {
            this.ritualCrystals.put(ritualData.portalOrigin().immutable(), new HashSet<>(immutablePositions(ritualData.filledPedestals())));
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

    public void rememberPortalVariant(BlockPos portalOrigin, PortalDungeonVariant variant) {
        BlockPos origin = portalOrigin.immutable();
        Integer previous = this.portalVariants.put(origin, variant.id());
        if (previous == null || previous.intValue() != variant.id()) {
            this.setDirty();
        }
    }

    public PortalDungeonVariant portalVariant(BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        int variantId = this.portalVariants.getOrDefault(origin, PortalDungeonVariant.selectForOrigin(origin).id());
        return PortalDungeonVariant.byId(variantId);
    }

    public Optional<BlockPos> completedPortalForPedestal(BlockPos pedestalPos) {
        BlockPos pedestal = pedestalPos.immutable();
        for (BlockPos portalOrigin : this.completedPortals) {
            if (PortalStructureHelper.ritualPedestalPositions(portalOrigin).contains(pedestal)) {
                return Optional.of(portalOrigin);
            }
        }
        return Optional.empty();
    }

    public RitualProgress markRitualCrystalPlaced(BlockPos portalOrigin, BlockPos pedestalPos) {
        BlockPos origin = portalOrigin.immutable();
        BlockPos pedestal = pedestalPos.immutable();
        List<BlockPos> expectedPedestals = PortalStructureHelper.ritualPedestalPositions(origin);
        Set<BlockPos> filledPedestals = this.ritualCrystals.computeIfAbsent(origin, ignored -> new HashSet<>());
        boolean wasComplete = filledPedestals.containsAll(expectedPedestals);

        if (expectedPedestals.contains(pedestal) && filledPedestals.add(pedestal)) {
            this.setDirty();
        }

        boolean isComplete = filledPedestals.containsAll(expectedPedestals);
        return new RitualProgress(origin, Set.copyOf(filledPedestals), isComplete, isComplete && !wasComplete);
    }

    public Set<BlockPos> filledRitualPedestals(BlockPos portalOrigin) {
        return Set.copyOf(this.ritualCrystals.getOrDefault(portalOrigin.immutable(), Set.of()));
    }

    public Set<BlockPos> ritualPortalOrigins() {
        return Set.copyOf(this.ritualCrystals.keySet());
    }

    public boolean isRitualComplete(BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        return this.ritualCrystals.getOrDefault(origin, Set.of()).containsAll(PortalStructureHelper.ritualPedestalPositions(origin));
    }

    public RitualProgress syncRitualCrystals(BlockPos portalOrigin, Set<BlockPos> filledPedestals) {
        BlockPos origin = portalOrigin.immutable();
        List<BlockPos> expectedPedestals = PortalStructureHelper.ritualPedestalPositions(origin);
        Set<BlockPos> sanitizedPedestals = new HashSet<>();
        for (BlockPos pedestal : filledPedestals) {
            BlockPos immutablePedestal = pedestal.immutable();
            if (expectedPedestals.contains(immutablePedestal)) {
                sanitizedPedestals.add(immutablePedestal);
            }
        }

        Set<BlockPos> existingPedestals = this.ritualCrystals.get(origin);
        boolean wasComplete = existingPedestals != null && existingPedestals.containsAll(expectedPedestals);
        boolean changed = false;

        if (sanitizedPedestals.isEmpty()) {
            changed = this.ritualCrystals.remove(origin) != null;
        } else if (existingPedestals == null || !existingPedestals.equals(sanitizedPedestals)) {
            this.ritualCrystals.put(origin, sanitizedPedestals);
            changed = true;
        }

        if (changed) {
            this.setDirty();
        }

        boolean isComplete = sanitizedPedestals.containsAll(expectedPedestals);
        return new RitualProgress(origin, Set.copyOf(sanitizedPedestals), isComplete, isComplete && !wasComplete);
    }

    public boolean isDragonActive(BlockPos portalOrigin) {
        return this.activeDragonPortals.contains(portalOrigin.immutable());
    }

    public Set<BlockPos> activeDragonPortalOrigins() {
        return Set.copyOf(this.activeDragonPortals);
    }

    public void setDragonActive(BlockPos portalOrigin, boolean active) {
        BlockPos origin = portalOrigin.immutable();
        boolean changed = active ? this.activeDragonPortals.add(origin) : this.activeDragonPortals.remove(origin);
        if (changed) {
            this.setDirty();
        }
    }

    public void clearRitual(BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        boolean changed = this.ritualCrystals.remove(origin) != null;
        changed |= this.activeDragonPortals.remove(origin);
        if (changed) {
            this.setDirty();
        }
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

    private List<PortalVariantData> snapshotPortalVariants() {
        List<PortalVariantData> variants = new ArrayList<>();
        for (Map.Entry<BlockPos, Integer> entry : this.portalVariants.entrySet()) {
            variants.add(new PortalVariantData(entry.getKey(), entry.getValue()));
        }
        return variants;
    }

    private List<RitualData> snapshotRitualCrystals() {
        List<RitualData> crystals = new ArrayList<>();
        for (Map.Entry<BlockPos, Set<BlockPos>> entry : this.ritualCrystals.entrySet()) {
            crystals.add(new RitualData(entry.getKey(), new ArrayList<>(entry.getValue())));
        }
        return crystals;
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

    private record PortalVariantData(BlockPos portalOrigin, int variantId) {
        private static final Codec<PortalVariantData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("portal_origin").forGetter(PortalVariantData::portalOrigin),
            Codec.INT.fieldOf("variant_id").forGetter(PortalVariantData::variantId)
        ).apply(instance, PortalVariantData::new));
    }

    private record RitualData(BlockPos portalOrigin, List<BlockPos> filledPedestals) {
        private static final Codec<RitualData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("portal_origin").forGetter(RitualData::portalOrigin),
            BlockPos.CODEC.listOf().fieldOf("filled_pedestals").forGetter(RitualData::filledPedestals)
        ).apply(instance, RitualData::new));
    }

    public record ActiveRaidSnapshot(
        BlockPos portalOrigin,
        int currentWaveNumber,
        long waveEndTimeTicks,
        Set<java.util.UUID> waveMobs
    ) {
    }

    public record RitualProgress(
        BlockPos portalOrigin,
        Set<BlockPos> filledPedestals,
        boolean allFilled,
        boolean newlyCompleted
    ) {
    }
}
