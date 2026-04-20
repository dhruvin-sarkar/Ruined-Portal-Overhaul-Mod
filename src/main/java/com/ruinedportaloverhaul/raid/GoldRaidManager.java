package com.ruinedportaloverhaul.raid;

import com.ruinedportaloverhaul.entity.ModEntities;
import com.ruinedportaloverhaul.entity.ExiledPiglinTraderEntity;
import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.block.NetherConduitChestPlacement;
import com.ruinedportaloverhaul.network.PortalAtmospherePayload;
import com.ruinedportaloverhaul.structure.PortalDungeonPiece;
import com.ruinedportaloverhaul.structure.PortalStructureHelper;
import com.ruinedportaloverhaul.world.ModStructures;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;

public final class GoldRaidManager {
    private static final int APPROACH_TRIGGER_RANGE = PortalStructureHelper.OUTER_RADIUS;
    private static final int RAID_TRIGGER_RANGE = PortalStructureHelper.INNER_RADIUS + 13;
    private static final int RAID_TITLE_PLAYER_RANGE = 64;
    private static final int AMBIENT_PARTICLE_RANGE = PortalStructureHelper.OUTER_RADIUS;
    private static final int BOSS_BAR_PLAYER_RANGE = 48;
    private static final int PRE_RAID_SPAWNER_SCAN_RADIUS = Math.max(80, PortalStructureHelper.MIDDLE_RADIUS + 28);
    private static final int RAID_SCAN_INTERVAL_TICKS = 10;
    private static final int ATMOSPHERE_PACKET_INTERVAL_TICKS = 10;
    private static final int AMBIENT_PARTICLE_INTERVAL_TICKS = 40;
    private static final int AMBIENT_SPAWN_INTERVAL_TICKS = 10;
    private static final int AMBIENT_MOB_CAP = 180;
    private static final int AMBIENT_BURST_MIN = 8;
    private static final int AMBIENT_BURST_RANDOM = 5;
    private static final int ANCHORED_GHAST_CAP = 16;
    private static final int GHAST_SPAWN_INTERVAL_TICKS = 25;
    private static final int GHAST_ANCHOR_RADIUS = PortalStructureHelper.OUTER_RADIUS + 32;
    private static final int GHAST_ANCHOR_TICKS = 20 * 180;
    private static final int INTER_WAVE_PULSE_INTERVAL_TICKS = 60;
    private static final int WAVE_DELAY_TICKS = 180;
    private static final int TERRITORY_BOON_DURATION_TICKS = 260;
    private static final float MIN_PORTAL_ATMOSPHERE_INTENSITY = 0.22f;
    private static final double BOSS_BAR_PLAYER_RANGE_SQUARED = BOSS_BAR_PLAYER_RANGE * BOSS_BAR_PLAYER_RANGE;
    private static final String PORTAL_AMBIENT_TAG = "rpo_portal_ambient";
    private static final String PORTAL_AMBIENT_ORIGIN_TAG_PREFIX = "rpo_ambient_origin_";
    private static final String PORTAL_GHAST_TAG = "rpo_portal_ghast";
    private static final String PORTAL_GHAST_ORIGIN_TAG_PREFIX = "rpo_origin_";
    private static final String PORTAL_TOTEM_TAG_PREFIX = "rpo_totem_granted_";

    private static final ResourceKey<LootTable> BOSS_REWARD_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        com.ruinedportaloverhaul.world.ModStructures.id("chests/portal_boss_reward")
    );

    private static final String[] WAVE_LABELS = {
        "The Red Storm Breaks",
        "They Grow Bolder",
        "The Brutes Arrive",
        "Chaos Unleashed",
        "The Evoker Awakens"
    };

    private static final Map<Long, RaidState> ACTIVE_RAIDS = new HashMap<>();
    private static final Map<Long, Long> NEXT_AMBIENT_SPAWN_TICK = new HashMap<>();
    private static final Map<Long, Long> NEXT_GHAST_SPAWN_TICK = new HashMap<>();
    private static final Map<UUID, PortalGhastAnchor> ANCHORED_GHASTS = new HashMap<>();

    private static final List<AmbientSpawnEntry> OUTER_AMBIENT_SPAWNS = List.of(
        new AmbientSpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 8),
        new AmbientSpawnEntry(ModEntities.PIGLIN_PILLAGER, 6),
        new AmbientSpawnEntry(ModEntities.PIGLIN_VINDICATOR, 4),
        new AmbientSpawnEntry(EntityType.MAGMA_CUBE, 4),
        new AmbientSpawnEntry(EntityType.BLAZE, 1)
    );
    private static final List<AmbientSpawnEntry> MIDDLE_AMBIENT_SPAWNS = List.of(
        new AmbientSpawnEntry(ModEntities.PIGLIN_PILLAGER, 7),
        new AmbientSpawnEntry(ModEntities.PIGLIN_VINDICATOR, 6),
        new AmbientSpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 4),
        new AmbientSpawnEntry(EntityType.BLAZE, 5),
        new AmbientSpawnEntry(EntityType.WITHER_SKELETON, 4),
        new AmbientSpawnEntry(EntityType.MAGMA_CUBE, 4),
        new AmbientSpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 3)
    );
    private static final List<AmbientSpawnEntry> INNER_AMBIENT_SPAWNS = List.of(
        new AmbientSpawnEntry(EntityType.BLAZE, 7),
        new AmbientSpawnEntry(EntityType.WITHER_SKELETON, 6),
        new AmbientSpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 5),
        new AmbientSpawnEntry(ModEntities.PIGLIN_VINDICATOR, 4),
        new AmbientSpawnEntry(EntityType.MAGMA_CUBE, 4),
        new AmbientSpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 2),
        new AmbientSpawnEntry(ModEntities.PIGLIN_PILLAGER, 3)
    );
    private static final List<AmbientSpawnEntry> LOWER_AMBIENT_SPAWNS = List.of(
        new AmbientSpawnEntry(EntityType.BLAZE, 8),
        new AmbientSpawnEntry(EntityType.WITHER_SKELETON, 7),
        new AmbientSpawnEntry(EntityType.MAGMA_CUBE, 6),
        new AmbientSpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 5),
        new AmbientSpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 3),
        new AmbientSpawnEntry(ModEntities.PIGLIN_VINDICATOR, 4),
        new AmbientSpawnEntry(ModEntities.PIGLIN_PILLAGER, 2)
    );
    private static final List<AmbientSpawnEntry> DEEP_AMBIENT_SPAWNS = List.of(
        new AmbientSpawnEntry(EntityType.WITHER_SKELETON, 8),
        new AmbientSpawnEntry(EntityType.BLAZE, 8),
        new AmbientSpawnEntry(EntityType.MAGMA_CUBE, 7),
        new AmbientSpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 8),
        new AmbientSpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 5),
        new AmbientSpawnEntry(ModEntities.PIGLIN_EVOKER, 2),
        new AmbientSpawnEntry(ModEntities.PIGLIN_VINDICATOR, 4)
    );

    private GoldRaidManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(GoldRaidManager::tick);
        ServerEntityEvents.ENTITY_LOAD.register(GoldRaidManager::suppressCompletedPortalMobLoad);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearRuntimeState());
    }

    private static void suppressCompletedPortalMobLoad(Entity entity, ServerLevel level) {
        if (!(entity instanceof Mob mob) || entity.getType() == ModEntities.EXILED_PIGLIN || mob.isPersistenceRequired()) {
            return;
        }

        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        BlockPos entityPos = entity.blockPosition();
        for (BlockPos portalOrigin : portalRaidState.completedPortalOrigins()) {
            if (isInsideCompletedPortalMobSuppressionArea(entityPos, portalOrigin)) {
                entity.discard();
                return;
            }
        }
    }

    private static boolean isInsideCompletedPortalMobSuppressionArea(BlockPos entityPos, BlockPos portalOrigin) {
        int verticalRange = PortalStructureHelper.PIT_DEPTH + 80;
        return Math.abs(entityPos.getY() - portalOrigin.getY()) <= verticalRange
            && horizontalDistanceSqr(entityPos, portalOrigin) <= PortalStructureHelper.OUTER_RADIUS * PortalStructureHelper.OUTER_RADIUS;
    }

    private static void tick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            tickLevel(level);
        }
    }

    private static void tickLevel(ServerLevel level) {
        if (!level.getServer().isRunning()) {
            return;
        }
        if (level != level.getServer().overworld()) {
            return;
        }

        long gameTime = level.getGameTime();
        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        restorePersistedRaids(level, portalRaidState);

        if (gameTime % ATMOSPHERE_PACKET_INTERVAL_TICKS == 0) {
            tickPortalZoneStormPayloads(level, portalRaidState);
        }

        if (gameTime % AMBIENT_PARTICLE_INTERVAL_TICKS == 0) {
            tickPortalZoneAtmosphere(level, portalRaidState, gameTime);
        }

        if (gameTime % AMBIENT_SPAWN_INTERVAL_TICKS == 0) {
            tickPortalZoneNaturalSpawns(level, portalRaidState, gameTime);
        }

        if (gameTime % RAID_SCAN_INTERVAL_TICKS == 0) {
            cleanupAnchoredGhasts(level, gameTime);
            for (ServerPlayer player : level.players()) {
                BlockPos portal = findNearbyGeneratedPortal(level, player, APPROACH_TRIGGER_RANGE);
                if (portal == null || portalRaidState.isCompleted(portal)) {
                    continue;
                }

                if (portalRaidState.markApproachActivated(portal)) {
                    playApproachActivation(level, player, portal);
                    persistKnownSpawners(level, portalRaidState, portal);
                }

                long key = raidKey(level, portal);
                if (!portalRaidState.isRaidActive(portal)
                    && !ACTIVE_RAIDS.containsKey(key)
                    && horizontalDistanceSqr(player.blockPosition(), portal) <= RAID_TRIGGER_RANGE * RAID_TRIGGER_RANGE) {
                    startRaid(level, portal, key, portalRaidState, player);
                }
            }
        }

        List<Long> finished = new ArrayList<>();
        for (Map.Entry<Long, RaidState> entry : ACTIVE_RAIDS.entrySet()) {
            RaidState state = entry.getValue();
            if (state.level != level) {
                continue;
            }
            if (tickRaid(state)) {
                finished.add(entry.getKey());
            }
        }
        for (Long key : finished) {
            ACTIVE_RAIDS.remove(key);
        }
    }

    private static void restorePersistedRaids(ServerLevel level, PortalRaidState portalRaidState) {
        for (PortalRaidState.ActiveRaidSnapshot snapshot : portalRaidState.activeRaidSnapshots()) {
            BlockPos origin = snapshot.portalOrigin().immutable();
            long key = raidKey(level, origin);
            if (ACTIVE_RAIDS.containsKey(key)) {
                continue;
            }
            if (portalRaidState.isCompleted(origin)) {
                portalRaidState.clearActiveRaid(origin);
                continue;
            }
            if (!snapshot.waveMobs().isEmpty() && !level.isPositionEntityTicking(origin)) {
                // Wait for the portal entity section before resolving persisted wave UUIDs after a restart.
                continue;
            }

            int waveIndex = Math.max(0, Math.min(WAVE_LABELS.length - 1, snapshot.currentWaveNumber() - 1));
            ServerBossEvent bossBar = new ServerBossEvent(
                Component.literal(WAVE_LABELS[waveIndex]),
                BossEvent.BossBarColor.YELLOW,
                BossEvent.BossBarOverlay.PROGRESS
            );
            bossBar.setDarkenScreen(true);
            bossBar.setCreateWorldFog(false);

            RaidState state = new RaidState(level, origin, bossBar, portalRaidState, waveIndex);
            for (UUID uuid : snapshot.waveMobs()) {
                Entity entity = level.getEntity(uuid);
                if (entity instanceof LivingEntity living && living.isAlive()) {
                    state.activeMobs.add(uuid);
                }
            }
            state.waveSize = Math.max(expectedWaveSize(waveIndex), state.activeMobs.size());
            if (snapshot.waveEndTimeTicks() > level.getGameTime()) {
                state.delayTicks = (int) Math.min(Integer.MAX_VALUE, snapshot.waveEndTimeTicks() - level.getGameTime());
            }
            ACTIVE_RAIDS.put(key, state);
        }
    }

    private static void playApproachActivation(ServerLevel level, ServerPlayer player, BlockPos origin) {
        ModAdvancementTriggers.trigger(ModAdvancementTriggers.PORTAL_APPROACH, player);
        player.displayClientMessage(Component.literal("...something stirs.").withStyle(ChatFormatting.DARK_PURPLE), true);
        level.playSound(null, origin, SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 0.4f, 0.6f);
    }

    private static void persistKnownSpawners(ServerLevel level, PortalRaidState portalRaidState, BlockPos origin) {
        if (!portalRaidState.portalSpawners(origin).isEmpty()) {
            return;
        }
        List<BlockPos> spawners = scanPreRaidSpawners(level, origin);
        if (!spawners.isEmpty()) {
            portalRaidState.setPortalSpawners(origin, spawners);
        }
    }

    private static void disablePreRaidSpawners(ServerLevel level, PortalRaidState portalRaidState, BlockPos origin) {
        persistKnownSpawners(level, portalRaidState, origin);
        List<BlockPos> spawners = portalRaidState.portalSpawners(origin);
        if (spawners.isEmpty()) {
            spawners = scanPreRaidSpawners(level, origin);
        }
        for (BlockPos spawner : spawners) {
            if (level.isLoaded(spawner) && level.getBlockState(spawner).is(Blocks.SPAWNER)) {
                level.setBlock(spawner, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static List<BlockPos> scanPreRaidSpawners(ServerLevel level, BlockPos origin) {
        List<BlockPos> spawners = new ArrayList<>();
        int minY = Math.max(level.getMinY(), origin.getY() - 60);
        int maxY = Math.min(level.getMaxY(), origin.getY() + 20);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int x = origin.getX() - PRE_RAID_SPAWNER_SCAN_RADIUS; x <= origin.getX() + PRE_RAID_SPAWNER_SCAN_RADIUS; x++) {
            for (int z = origin.getZ() - PRE_RAID_SPAWNER_SCAN_RADIUS; z <= origin.getZ() + PRE_RAID_SPAWNER_SCAN_RADIUS; z++) {
                for (int y = minY; y <= maxY; y++) {
                    cursor.set(x, y, z);
                    if (level.isLoaded(cursor) && level.getBlockState(cursor).is(Blocks.SPAWNER)) {
                        spawners.add(cursor.immutable());
                    }
                }
            }
        }
        return spawners;
    }

    private static boolean tickRaid(RaidState state) {
        syncBossBarPlayers(state);

        if (!state.activeMobs.isEmpty() && !state.level.isPositionEntityTicking(state.origin)) {
            // Do not count unloaded wave mobs as dead; pause until the portal area is ticking entities again.
            return false;
        }

        boolean removedDeadMobs = state.activeMobs.removeIf(uuid -> {
            Entity entity = state.level.getEntity(uuid);
            return !(entity instanceof LivingEntity living) || !living.isAlive();
        });
        if (removedDeadMobs) {
            state.persistWaveState();
        }

        if (!state.activeMobs.isEmpty()) {
            float progress = Math.max(0.05f, state.activeMobs.size() / (float) state.waveSize);
            state.bossBar.setProgress(progress);
            return false;
        }

        if (state.waveIndex >= 4) {
            finishRaid(state);
            return true;
        }

        if (state.delayTicks > 0) {
            state.delayTicks--;
            if (state.delayTicks % INTER_WAVE_PULSE_INTERVAL_TICKS == 0) {
                spawnInterWavePulse(state.level, state.origin);
            }
            int seconds = Math.max(1, (state.delayTicks + 19) / 20);
            Component message = Component.literal("Next wave in " + seconds + "s");
            for (ServerPlayer player : state.bossBar.getPlayers()) {
                player.displayClientMessage(message, true);
            }
            return false;
        }

        state.waveIndex++;
        state.bossBar.setName(Component.literal(WAVE_LABELS[state.waveIndex]));
        spawnWave(state);
        state.delayTicks = WAVE_DELAY_TICKS;
        state.persistWaveState();
        return false;
    }

    private static void syncBossBarPlayers(RaidState state) {
        Set<UUID> inRange = new HashSet<>();
        for (ServerPlayer player : state.level.players()) {
            if (horizontalDistanceSqr(player.blockPosition(), state.origin) <= BOSS_BAR_PLAYER_RANGE_SQUARED) {
                inRange.add(player.getUUID());
                state.trackedPlayers.add(player.getUUID());
                state.bossBar.addPlayer(player);
            }
        }

        state.trackedPlayers.removeIf(uuid -> {
            if (inRange.contains(uuid)) {
                return false;
            }
            ServerPlayer player = state.level.getServer().getPlayerList().getPlayer(uuid);
            if (player != null) {
                state.bossBar.removePlayer(player);
            }
            return true;
        });
    }

    private static void startRaid(ServerLevel level, BlockPos origin, long key, PortalRaidState portalRaidState, ServerPlayer triggeringPlayer) {
        if (!portalRaidState.beginRaid(origin)) {
            return;
        }
        ModAdvancementTriggers.trigger(ModAdvancementTriggers.RAID_STARTED, triggeringPlayer);
        ServerBossEvent bossBar = new ServerBossEvent(
            Component.literal(WAVE_LABELS[0]),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.setDarkenScreen(true);
        bossBar.setCreateWorldFog(false);
        RaidState state = new RaidState(level, origin, bossBar, portalRaidState);
        ACTIVE_RAIDS.put(key, state);
        playRaidStartEffects(level, origin);
        broadcastRaidStartTitle(level, origin);
        disablePreRaidSpawners(level, portalRaidState, origin);
        spawnWave(state);
        state.delayTicks = WAVE_DELAY_TICKS;
        state.persistWaveState();
    }

    private static void spawnWave(RaidState state) {
        state.activeMobs.clear();
        state.waveSize = 0;
        switch (state.waveIndex) {
            case 0 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 12),
                new SpawnEntry(ModEntities.PIGLIN_VINDICATOR, 8)
            );
            case 1 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 14),
                new SpawnEntry(ModEntities.PIGLIN_VINDICATOR, 8),
                new SpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 5)
            );
            case 2 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 12),
                new SpawnEntry(ModEntities.PIGLIN_VINDICATOR, 9),
                new SpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 8),
                new SpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 5)
            );
            case 3 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 6),
                new SpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 8),
                new SpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 6),
                new SpawnEntry(ModEntities.PIGLIN_VINDICATOR, 8),
                new SpawnEntry(ModEntities.PIGLIN_RAVAGER, 1),
                new SpawnEntry(ModEntities.PIGLIN_EVOKER, 1)
            );
            case 4 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 10),
                new SpawnEntry(ModEntities.PIGLIN_VINDICATOR, 9),
                new SpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 8),
                new SpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 5),
                new SpawnEntry(ModEntities.PIGLIN_RAVAGER, 2),
                new SpawnEntry(ModEntities.PIGLIN_EVOKER, 3)
            );
            default -> {
            }
        }
        state.bossBar.setProgress(1.0f);
        state.persistWaveState();
    }

    @SafeVarargs
    private static void spawnWave(RaidState state, SpawnEntry... entries) {
        int totalMobs = 0;
        for (SpawnEntry entry : entries) {
            totalMobs += entry.count;
        }
        int spawnSlot = 0;
        for (SpawnEntry entry : entries) {
            for (int i = 0; i < entry.count; i++) {
                LivingEntity entity = spawnMob(state, entry.type, spawnSlot++, totalMobs);
                if (entity != null) {
                    trackWaveMob(state, entity);
                    if (entry.type == ModEntities.PIGLIN_RAVAGER && state.waveIndex != 4) {
                        LivingEntity rider = spawnRavagerRider(state, entity);
                        if (rider != null) {
                            trackWaveMob(state, rider);
                        }
                    }
                }
            }
        }
    }

    private static LivingEntity spawnMob(RaidState state, EntityType<? extends LivingEntity> type, int offsetIndex, int totalMobs) {
        // Resolve a collision-free surface point before spawning so wave mobs do not start inside scar or tunnel blocks.
        BlockPos spawnPos = findWaveSpawnPosition(state, type, offsetIndex, totalMobs);
        LivingEntity entity = type.spawn(state.level, spawnPos, EntitySpawnReason.EVENT);
        if (entity instanceof Mob mob) {
            mob.setTarget(state.level.getNearestPlayer(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 24.0, false));
        }
        if (entity != null) {
            playHighThreatSpawnSound(state.level, type, spawnPos);
        }
        return entity;
    }

    private static void trackWaveMob(RaidState state, LivingEntity entity) {
        state.activeMobs.add(entity.getUUID());
        state.waveSize++;
    }

    private static LivingEntity spawnRavagerRider(RaidState state, LivingEntity ravager) {
        // Spawn the rider after the ravager exists, then track it so raid completion waits for the whole threat.
        BlockPos riderPos = ravager.blockPosition().above();
        LivingEntity rider = ModEntities.PIGLIN_VINDICATOR.spawn(state.level, riderPos, EntitySpawnReason.EVENT);
        if (rider instanceof Mob mob) {
            mob.setTarget(state.level.getNearestPlayer(riderPos.getX(), riderPos.getY(), riderPos.getZ(), 24.0, false));
        }
        if (rider != null) {
            rider.startRiding(ravager, true, true);
        }
        return rider;
    }

    private static BlockPos findWaveSpawnPosition(RaidState state, EntityType<? extends LivingEntity> type, int offsetIndex, int totalMobs) {
        double radius = 14.0 + Math.min(10.0, totalMobs * 0.45);
        for (int attempt = 0; attempt < 18; attempt++) {
            int slot = offsetIndex + attempt;
            double angle = (Math.PI * 2.0) * (slot / (double) Math.max(1, totalMobs));
            BlockPos horizontal = state.origin.offset(
                (int) Math.round(Math.cos(angle) * radius),
                0,
                (int) Math.round(Math.sin(angle) * radius)
            );
            BlockPos surface = state.level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, horizontal);
            for (int yOffset = 0; yOffset <= 2; yOffset++) {
                BlockPos candidate = surface.above(yOffset);
                if (canSpawnWaveMobAt(state.level, type, candidate)) {
                    return candidate;
                }
            }
        }
        return state.origin.above(2);
    }

    private static boolean canSpawnWaveMobAt(ServerLevel level, EntityType<? extends LivingEntity> type, BlockPos pos) {
        // 1.21.11 footing check: use face support instead of deprecated blocksMotion for wave spawn safety.
        return level.getWorldBorder().isWithinBounds(pos)
            && level.hasChunkAt(pos)
            && level.hasChunkAt(pos.above())
            && level.hasChunkAt(pos.below())
            && level.getFluidState(pos).isEmpty()
            && level.getFluidState(pos.above()).isEmpty()
            && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
            && level.noCollision(type.getSpawnAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
    }

    private static boolean isNetherSpawnFloor(BlockState state) {
        return state.is(Blocks.NETHERRACK)
            || state.is(Blocks.BLACKSTONE)
            || state.is(Blocks.BASALT)
            || state.is(Blocks.SMOOTH_BASALT)
            || state.is(Blocks.SOUL_SAND)
            || state.is(Blocks.SOUL_SOIL)
            || state.is(Blocks.MAGMA_BLOCK)
            || state.is(Blocks.POLISHED_BLACKSTONE)
            || state.is(Blocks.POLISHED_BLACKSTONE_BRICKS)
            || state.is(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double clamp01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static void finishRaid(RaidState state) {
        List<ServerPlayer> nearbyPlayers = state.level.getPlayers(player -> horizontalDistanceSqr(player.blockPosition(), state.origin) < 1600.0);
        for (ServerPlayer player : nearbyPlayers) {
            ModAdvancementTriggers.trigger(ModAdvancementTriggers.RAID_COMPLETED, player);
        }
        state.bossBar.removeAllPlayers();
        state.trackedPlayers.clear();
        state.bossBar.setVisible(false);
        playCompletionFanfare(state.level, state.origin);
        ignitePortal(state.level, state.origin);
        spawnBossChest(state.level, state.origin);
        spawnExiledTrader(state.level, state.origin);
        disablePreRaidSpawners(state.level, state.portalRaidState, state.origin);
        state.portalRaidState.markCompleted(state.origin);

        Component message = Component.literal("The portal falls silent.");
        for (ServerPlayer player : nearbyPlayers) {
            player.displayClientMessage(message, true);
        }
    }

    private static void ignitePortal(ServerLevel level, BlockPos origin) {
        PortalInterior interior = findPortalInterior(level, origin);
        if (interior == null) {
            return;
        }

        BlockState portalState = Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, interior.axis());
        for (BlockPos pos : interior.blocks()) {
            if (level.getBlockState(pos).isAir()) {
                level.setBlock(pos, portalState, 2);
            }
        }
    }

    private static void spawnBossChest(ServerLevel level, BlockPos origin) {
        BlockPos chestPos = origin.offset(3, 1, 0);
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2);
        if (level.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest) {
            chest.setLootTable(BOSS_REWARD_LOOT);
            chest.setLootTableSeed(level.getRandom().nextLong());
            if (NetherConduitChestPlacement.useBossChest(origin)) {
                NetherConduitChestPlacement.addNetherConduit(chest);
            }
        }
    }

    private static void spawnExiledTrader(ServerLevel level, BlockPos origin) {
        // Treat the trader as the raid completion event spawn so its lifecycle matches the encounter state.
        level.setBlock(origin.offset(4, 1, 0), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), 3);
        level.setBlock(origin.offset(4, 1, 1), Blocks.CRIMSON_FENCE_GATE.defaultBlockState(), 3);
        ExiledPiglinTraderEntity trader = ModEntities.EXILED_PIGLIN.spawn(
            level,
            origin.offset(4, 1, -1),
            EntitySpawnReason.EVENT
        );
        if (trader != null) {
            trader.setCustomName(Component.literal("Exiled Piglin"));
            trader.setCustomNameVisible(true);
            trader.rememberSpawnTime(level.getGameTime());
            playExiledPiglinSpawnEffects(level, trader.blockPosition());
        }
    }

    private static void tickPortalZoneAtmosphere(ServerLevel level, PortalRaidState portalRaidState, long gameTime) {
        Set<BlockPos> emitted = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            BlockPos portal = findPortalDungeonOrigin(level, player.blockPosition(), AMBIENT_PARTICLE_RANGE);
            if (portal == null || portalRaidState.isCompleted(portal)) {
                continue;
            }
            spawnPlayerAtmosphere(level, player, portal, gameTime);
            if (emitted.add(portal.immutable())) {
                spawnAmbientPortalFrameParticles(level, portal);
            }
        }
    }

    private static void tickPortalZoneStormPayloads(ServerLevel level, PortalRaidState portalRaidState) {
        for (ServerPlayer player : level.players()) {
            BlockPos portal = findNearbyGeneratedPortal(level, player, PortalStructureHelper.OUTER_RADIUS);
            if (portal == null || portalRaidState.isCompleted(portal)) {
                continue;
            }
            sendPortalAtmosphere(player, portal);
            applyPortalTerritoryBoon(player, portal);
        }
    }

    private static void applyPortalTerritoryBoon(ServerPlayer player, BlockPos origin) {
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, TERRITORY_BOON_DURATION_TICKS, 1, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, TERRITORY_BOON_DURATION_TICKS, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, TERRITORY_BOON_DURATION_TICKS, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, TERRITORY_BOON_DURATION_TICKS, 3, true, false, true));
        ModAdvancementTriggers.trigger(ModAdvancementTriggers.AETHER_BOON, player);
        grantTerritoryTotem(player, origin);
    }

    private static void grantTerritoryTotem(ServerPlayer player, BlockPos origin) {
        String tag = PORTAL_TOTEM_TAG_PREFIX + origin.asLong();
        if (player.getTags().contains(tag)) {
            return;
        }

        player.addTag(tag);
        ItemStack totem = new ItemStack(Items.TOTEM_OF_UNDYING);
        if (!player.addItem(totem)) {
            player.drop(totem, false);
        }
        ModAdvancementTriggers.trigger(ModAdvancementTriggers.TERRITORY_TOTEM, player);
        player.displayClientMessage(Component.literal("The red air presses a totem into your hand.").withStyle(ChatFormatting.GOLD), true);
    }

    private static void sendPortalAtmosphere(ServerPlayer player, BlockPos origin) {
        if (!ServerPlayNetworking.canSend(player, PortalAtmospherePayload.TYPE)) {
            return;
        }
        double distance = horizontalDistance(player.blockPosition(), origin);
        float baseIntensity = (float) clamp01(1.0 - distance / PortalStructureHelper.OUTER_RADIUS);
        float intensity = MIN_PORTAL_ATMOSPHERE_INTENSITY + (1.0f - MIN_PORTAL_ATMOSPHERE_INTENSITY) * baseIntensity;
        float descent = (float) clamp01((origin.getY() - player.getY()) / PortalStructureHelper.PIT_DEPTH);
        double belowPortal = origin.getY() - player.getY();
        if (distance <= PortalStructureHelper.INNER_RADIUS + 10 && belowPortal >= 8.0) {
            ModAdvancementTriggers.trigger(ModAdvancementTriggers.PIT_DESCENT, player);
        }
        if (distance <= PortalStructureHelper.INNER_RADIUS + 10 && belowPortal >= 24.0) {
            ModAdvancementTriggers.trigger(ModAdvancementTriggers.DEEP_STORM, player);
        }
        ServerPlayNetworking.send(player, new PortalAtmospherePayload(intensity, descent));
    }

    private static void spawnPlayerAtmosphere(ServerLevel level, ServerPlayer player, BlockPos origin, long gameTime) {
        double distance = horizontalDistance(player.blockPosition(), origin);
        if (distance > PortalStructureHelper.OUTER_RADIUS) {
            return;
        }
        double intensity = 1.0 - Math.min(1.0, distance / PortalStructureHelper.OUTER_RADIUS);
        double y = player.getY() + 5.0 + intensity * 4.0;
        int ashCount = 10 + (int) Math.round(intensity * 32.0);
        int sporeCount = 4 + (int) Math.round(intensity * 18.0);
        int smokeCount = 2 + (int) Math.round(intensity * 9.0);
        spawnParticle(level, ParticleTypes.ASH, player.getX(), y, player.getZ(), ashCount, 20.0, 8.0, 20.0, 0.01);
        spawnParticle(level, ParticleTypes.CRIMSON_SPORE, player.getX(), y - 1.5, player.getZ(), sporeCount, 18.0, 6.0, 18.0, 0.01);
        spawnParticle(level, ParticleTypes.LARGE_SMOKE, player.getX(), y - 2.5, player.getZ(), smokeCount, 14.0, 5.0, 14.0, 0.01);
        if (intensity > 0.45 && gameTime % 120 == 0) {
            level.playSound(null, player.blockPosition(), SoundEvents.LAVA_AMBIENT, SoundSource.AMBIENT, 0.35f, 0.7f);
        }
        if (intensity > 0.65) {
            spawnParticle(level, ParticleTypes.DRIPPING_LAVA, player.getX(), player.getY() + 8.0, player.getZ(), 2, 12.0, 5.0, 12.0, 0.01);
        }
    }

    private static void spawnAmbientPortalFrameParticles(ServerLevel level, BlockPos origin) {
        PortalInterior interior = findPortalInterior(level, origin);
        if (interior != null) {
            for (BlockPos framePos : interior.frameBlocks()) {
                level.sendParticles(
                    ParticleTypes.PORTAL,
                    framePos.getX() + 0.5,
                    framePos.getY() + 0.5,
                    framePos.getZ() + 0.5,
                    2,
                    0.3,
                    0.3,
                    0.3,
                    0.05
                );
            }
        }

        int surfaceY = origin.getY() + 1;
        int radius = 15;
        spawnParticle(level, ParticleTypes.FLAME, origin.getX() - radius + 0.5, surfaceY + 0.5, origin.getZ() - radius + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
        spawnParticle(level, ParticleTypes.FLAME, origin.getX() + radius + 0.5, surfaceY + 0.5, origin.getZ() - radius + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
        spawnParticle(level, ParticleTypes.FLAME, origin.getX() - radius + 0.5, surfaceY + 0.5, origin.getZ() + radius + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
        spawnParticle(level, ParticleTypes.FLAME, origin.getX() + radius + 0.5, surfaceY + 0.5, origin.getZ() + radius + 0.5, 1, 0.1, 0.1, 0.1, 0.01);
    }

    private static void tickPortalZoneNaturalSpawns(ServerLevel level, PortalRaidState portalRaidState, long gameTime) {
        Set<BlockPos> attempted = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            BlockPos portal = findPortalDungeonOrigin(level, player.blockPosition(), PortalStructureHelper.OUTER_RADIUS);
            if (portal == null
                || !attempted.add(portal.immutable())
                || portalRaidState.isCompleted(portal)
                || level.getDifficulty() == Difficulty.PEACEFUL) {
                continue;
            }

            long key = raidKey(level, portal);
            trySpawnAmbientGroundMob(level, player, portal, key, gameTime);
            trySpawnAnchoredGhast(level, player, portal, key, gameTime);
        }
    }

    private static void trySpawnAmbientGroundMob(ServerLevel level, ServerPlayer player, BlockPos origin, long key, long gameTime) {
        long nextSpawnTick = NEXT_AMBIENT_SPAWN_TICK.getOrDefault(key, 0L);
        int existingMobs = countPortalAmbientMobs(level, origin);
        if (gameTime < nextSpawnTick || existingMobs >= AMBIENT_MOB_CAP) {
            return;
        }

        double distance = horizontalDistance(player.blockPosition(), origin);
        List<AmbientSpawnEntry> entries = ambientSpawnEntries(player, origin, distance);
        int depthBonus = player.getY() <= origin.getY() - 38.0 ? 4 : player.getY() <= origin.getY() - 16.0 ? 2 : 0;
        int burstSize = Math.min(AMBIENT_MOB_CAP - existingMobs, AMBIENT_BURST_MIN + depthBonus + level.getRandom().nextInt(AMBIENT_BURST_RANDOM));
        int spawned = 0;
        for (int i = 0; i < burstSize; i++) {
            EntityType<? extends LivingEntity> type = pickAmbientType(level.getRandom(), entries);
            BlockPos spawnPos = findAmbientSpawnPosition(level, player, origin, type, distance);
            if (spawnPos == null) {
                continue;
            }

            LivingEntity entity = type.spawn(level, spawnPos, EntitySpawnReason.EVENT);
            if (entity instanceof Mob mob) {
                mob.addTag(PORTAL_AMBIENT_TAG);
                mob.addTag(PORTAL_AMBIENT_ORIGIN_TAG_PREFIX + origin.asLong());
                mob.setTarget(player);
            }
            if (entity != null) {
                spawned++;
                spawnParticle(level, ParticleTypes.LARGE_SMOKE, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.02);
            }
        }

        long cooldown = spawned > 0 ? 8L + level.getRandom().nextInt(11) : 12L + level.getRandom().nextInt(10);
        NEXT_AMBIENT_SPAWN_TICK.put(key, gameTime + cooldown);
    }

    private static void trySpawnAnchoredGhast(ServerLevel level, ServerPlayer player, BlockPos origin, long key, long gameTime) {
        if (horizontalDistance(player.blockPosition(), origin) > PortalStructureHelper.OUTER_RADIUS
            || gameTime < NEXT_GHAST_SPAWN_TICK.getOrDefault(key, 0L)
            || countAnchoredGhasts(level, origin) >= ANCHORED_GHAST_CAP) {
            return;
        }

        int remaining = ANCHORED_GHAST_CAP - countAnchoredGhasts(level, origin);
        int burst = Math.min(remaining, 1 + level.getRandom().nextInt(3));
        int spawned = 0;
        for (int i = 0; i < burst; i++) {
            BlockPos spawnPos = findGhastSpawnPosition(level, player, origin);
            if (spawnPos == null) {
                continue;
            }

            Ghast ghast = EntityType.GHAST.spawn(level, spawnPos, EntitySpawnReason.EVENT);
            if (ghast != null) {
                ghast.addTag(PORTAL_GHAST_TAG);
                ghast.addTag(PORTAL_GHAST_ORIGIN_TAG_PREFIX + origin.asLong());
                ANCHORED_GHASTS.put(ghast.getUUID(), new PortalGhastAnchor(origin.immutable(), gameTime + GHAST_ANCHOR_TICKS));
                spawnParticle(level, ParticleTypes.CRIMSON_SPORE, spawnPos.getX() + 0.5, spawnPos.getY() + 2.0, spawnPos.getZ() + 0.5, 30, 3.0, 2.0, 3.0, 0.02);
                level.playSound(null, spawnPos, SoundEvents.GHAST_AMBIENT, SoundSource.HOSTILE, 2.0f, 0.65f);
                spawned++;
            }
        }

        long cooldown = spawned > 0 ? GHAST_SPAWN_INTERVAL_TICKS + level.getRandom().nextInt(31) : 18L + level.getRandom().nextInt(16);
        NEXT_GHAST_SPAWN_TICK.put(key, gameTime + cooldown);
    }

    private static List<AmbientSpawnEntry> ambientSpawnEntries(ServerPlayer player, BlockPos origin, double portalDistance) {
        if (player.getY() <= origin.getY() - 38.0) {
            return DEEP_AMBIENT_SPAWNS;
        }
        if (player.getY() <= origin.getY() - 16.0) {
            return LOWER_AMBIENT_SPAWNS;
        }
        if (portalDistance <= PortalStructureHelper.INNER_RADIUS + 8) {
            return INNER_AMBIENT_SPAWNS;
        }
        if (portalDistance <= PortalStructureHelper.MIDDLE_RADIUS) {
            return MIDDLE_AMBIENT_SPAWNS;
        }
        return OUTER_AMBIENT_SPAWNS;
    }

    private static EntityType<? extends LivingEntity> pickAmbientType(RandomSource random, List<AmbientSpawnEntry> entries) {
        int totalWeight = 0;
        for (AmbientSpawnEntry entry : entries) {
            totalWeight += entry.weight();
        }
        int roll = random.nextInt(Math.max(1, totalWeight));
        for (AmbientSpawnEntry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return entry.type();
            }
        }
        return entries.get(entries.size() - 1).type();
    }

    private static BlockPos findAmbientSpawnPosition(
        ServerLevel level,
        ServerPlayer player,
        BlockPos origin,
        EntityType<? extends LivingEntity> type,
        double playerPortalDistance
    ) {
        if (player.getY() <= origin.getY() - 8.0) {
            BlockPos underground = findAmbientUndergroundSpawnPosition(level, player, origin, type);
            if (underground != null) {
                return underground;
            }
        }
        return findAmbientGroundSpawnPosition(level, player, origin, type, playerPortalDistance);
    }

    private static BlockPos findAmbientUndergroundSpawnPosition(
        ServerLevel level,
        ServerPlayer player,
        BlockPos origin,
        EntityType<? extends LivingEntity> type
    ) {
        RandomSource random = level.getRandom();
        int minY = Math.max(level.getMinY() + 4, origin.getY() - PortalStructureHelper.PIT_DEPTH - 8);
        int maxY = origin.getY() - 3;
        for (int attempt = 0; attempt < 44; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 7.0 + random.nextDouble() * 24.0;
            int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * radius);
            if (horizontalDistance(x, z, origin) > PortalStructureHelper.MIDDLE_RADIUS + 36.0) {
                continue;
            }

            int baseY = clamp((int) Math.round(player.getY()) + random.nextInt(13) - 6, minY, maxY);
            for (int yOffset = 5; yOffset >= -6; yOffset--) {
                BlockPos candidate = new BlockPos(x, clamp(baseY + yOffset, minY, maxY), z);
                if (canSpawnWaveMobAt(level, type, candidate) && isNetherSpawnFloor(level.getBlockState(candidate.below()))) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static BlockPos findAmbientGroundSpawnPosition(
        ServerLevel level,
        ServerPlayer player,
        BlockPos origin,
        EntityType<? extends LivingEntity> type,
        double playerPortalDistance
    ) {
        RandomSource random = level.getRandom();
        double minPortalRadius = playerPortalDistance <= PortalStructureHelper.MIDDLE_RADIUS
            ? PortalStructureHelper.INNER_RADIUS + 3.0
            : PortalStructureHelper.MIDDLE_RADIUS + 2.0;
        double maxPortalRadius = playerPortalDistance <= PortalStructureHelper.MIDDLE_RADIUS
            ? PortalStructureHelper.MIDDLE_RADIUS + 8.0
            : PortalStructureHelper.OUTER_RADIUS - 4.0;

        for (int attempt = 0; attempt < 40; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double playerRadius = 18.0 + random.nextDouble() * 28.0;
            int x = player.blockPosition().getX() + (int) Math.round(Math.cos(angle) * playerRadius);
            int z = player.blockPosition().getZ() + (int) Math.round(Math.sin(angle) * playerRadius);
            double portalRadius = horizontalDistance(x, z, origin);
            if (portalRadius < minPortalRadius || portalRadius > maxPortalRadius) {
                continue;
            }
            if (!level.hasChunk(x >> 4, z >> 4)) {
                continue;
            }
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z));
            for (int yOffset = 0; yOffset <= 2; yOffset++) {
                BlockPos candidate = surface.above(yOffset);
                if (canSpawnWaveMobAt(level, type, candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private static BlockPos findGhastSpawnPosition(ServerLevel level, ServerPlayer player, BlockPos origin) {
        RandomSource random = level.getRandom();
        for (int attempt = 0; attempt < 44; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = 18.0 + random.nextDouble() * (PortalStructureHelper.OUTER_RADIUS - 18.0);
            int x = origin.getX() + (int) Math.round(Math.cos(angle) * radius);
            int z = origin.getZ() + (int) Math.round(Math.sin(angle) * radius);
            if (horizontalDistance(x, z, origin) > PortalStructureHelper.OUTER_RADIUS - 12) {
                continue;
            }
            if (!level.hasChunk(x >> 4, z >> 4)) {
                continue;
            }
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, origin.getY(), z));
            int y = Math.min(level.getMaxY() - 6, Math.max(surface.getY() + 12, player.blockPosition().getY() + 18 + random.nextInt(18)));
            BlockPos candidate = new BlockPos(x, y, z);
            if (hasClearGhastVolume(level, candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean hasClearGhastVolume(ServerLevel level, BlockPos base) {
        if (!level.getWorldBorder().isWithinBounds(base)
            || !level.hasChunksAt(base.offset(-2, 0, -2), base.offset(2, 3, 2))
            || !level.noCollision(EntityType.GHAST.getSpawnAABB(base.getX() + 0.5, base.getY(), base.getZ() + 0.5))) {
            return false;
        }
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos pos = base.offset(dx, dy, dz);
                    if (!level.getWorldBorder().isWithinBounds(pos)
                        || !level.getFluidState(pos).isEmpty()
                        || !level.getBlockState(pos).isAir()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static int countPortalAmbientMobs(ServerLevel level, BlockPos origin) {
        AABB zone = new AABB(origin).inflate(PortalStructureHelper.OUTER_RADIUS, 96.0, PortalStructureHelper.OUTER_RADIUS);
        return level.getEntities((Entity) null, zone, entity -> entity instanceof Mob mob
            && mob.isAlive()
            && (hasTaggedAmbientOrigin(mob, origin) || isPortalAmbientType(entity.getType()))).size();
    }

    private static boolean isPortalAmbientType(EntityType<?> type) {
        return type == EntityType.ZOMBIFIED_PIGLIN
            || type == EntityType.MAGMA_CUBE
            || type == EntityType.BLAZE
            || type == EntityType.WITHER_SKELETON
            || type == ModEntities.PIGLIN_PILLAGER
            || type == ModEntities.PIGLIN_VINDICATOR
            || type == ModEntities.PIGLIN_BRUTE_PILLAGER
            || type == ModEntities.PIGLIN_ILLUSIONER
            || type == ModEntities.PIGLIN_EVOKER;
    }

    private static boolean hasTaggedAmbientOrigin(Mob mob, BlockPos origin) {
        return mob.getTags().contains(PORTAL_AMBIENT_TAG)
            && taggedOrigin(mob, PORTAL_AMBIENT_ORIGIN_TAG_PREFIX)
                .map(taggedOrigin -> horizontalDistance(taggedOrigin, origin) <= 1.0)
                .orElse(false);
    }

    private static int countAnchoredGhasts(ServerLevel level, BlockPos origin) {
        AABB zone = new AABB(origin).inflate(GHAST_ANCHOR_RADIUS, 96.0, GHAST_ANCHOR_RADIUS);
        return level.getEntities(EntityType.GHAST, zone, ghast -> ghast.isAlive()
            && ghast.getTags().contains(PORTAL_GHAST_TAG)
            && taggedGhastOrigin(ghast).map(taggedOrigin -> horizontalDistance(taggedOrigin, origin) <= 1.0).orElse(false)).size();
    }

    private static void cleanupAnchoredGhasts(ServerLevel level, long gameTime) {
        Iterator<Map.Entry<UUID, PortalGhastAnchor>> iterator = ANCHORED_GHASTS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, PortalGhastAnchor> entry = iterator.next();
            Entity entity = level.getEntity(entry.getKey());
            PortalGhastAnchor anchor = entry.getValue();
            if (!(entity instanceof Ghast ghast) || !ghast.isAlive()) {
                iterator.remove();
                continue;
            }
            if (gameTime > anchor.expireTick()
                || horizontalDistance(ghast.blockPosition(), anchor.origin()) > GHAST_ANCHOR_RADIUS) {
                spawnParticle(level, ParticleTypes.LARGE_SMOKE, ghast.getX(), ghast.getY() + 1.5, ghast.getZ(), 20, 2.0, 2.0, 2.0, 0.02);
                ghast.discard();
                iterator.remove();
            }
        }

        for (ServerPlayer player : level.players()) {
            AABB scan = new AABB(player.blockPosition()).inflate(GHAST_ANCHOR_RADIUS, 96.0, GHAST_ANCHOR_RADIUS);
            for (Ghast ghast : level.getEntities(EntityType.GHAST, scan, ghast -> ghast.getTags().contains(PORTAL_GHAST_TAG))) {
                taggedGhastOrigin(ghast).ifPresent(origin -> {
                    if (horizontalDistance(ghast.blockPosition(), origin) > GHAST_ANCHOR_RADIUS) {
                        ghast.discard();
                    }
                });
            }
        }
    }

    private static java.util.Optional<BlockPos> taggedGhastOrigin(Ghast ghast) {
        return taggedOrigin(ghast, PORTAL_GHAST_ORIGIN_TAG_PREFIX);
    }

    private static java.util.Optional<BlockPos> taggedOrigin(Entity entity, String prefix) {
        for (String tag : entity.getTags()) {
            if (!tag.startsWith(prefix)) {
                continue;
            }
            try {
                return java.util.Optional.of(BlockPos.of(Long.parseLong(tag.substring(prefix.length()))));
            } catch (NumberFormatException ignored) {
                return java.util.Optional.empty();
            }
        }
        return java.util.Optional.empty();
    }

    private static void playRaidStartEffects(ServerLevel level, BlockPos origin) {
        level.playSound(null, origin, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.5f, 1.0f);
        spawnRandomizedParticleBurst(level, origin, ParticleTypes.LARGE_SMOKE, 40, 3.0, 0.01);
        spawnRandomizedParticleBurst(level, origin, ParticleTypes.FLAME, 20, 3.0, 0.01);
    }

    private static void broadcastRaidStartTitle(ServerLevel level, BlockPos origin) {
        double rangeSquared = RAID_TITLE_PLAYER_RANGE * RAID_TITLE_PLAYER_RANGE;
        for (ServerPlayer player : level.getPlayers(player -> horizontalDistanceSqr(player.blockPosition(), origin) <= rangeSquared)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 40, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("The Red Storm Breaks").withStyle(ChatFormatting.DARK_RED)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("Survive the waves...").withStyle(ChatFormatting.RED)));
        }
    }

    private static void spawnInterWavePulse(ServerLevel level, BlockPos origin) {
        double centerX = origin.getX() + 0.5;
        double centerY = origin.getY() + 1.0;
        double centerZ = origin.getZ() + 0.5;
        for (int i = 0; i < 12; i++) {
            double x = centerX + 4.0 * Math.cos(i * Math.PI / 6.0);
            double z = centerZ + 4.0 * Math.sin(i * Math.PI / 6.0);
            spawnParticle(level, ParticleTypes.SOUL, x, centerY, z, 1, 0.0, 0.0, 0.0, 0.01);
        }
        level.playSound(null, origin, SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 0.6f, 0.8f);
    }

    private static void playHighThreatSpawnSound(ServerLevel level, EntityType<? extends LivingEntity> type, BlockPos spawnPos) {
        if (type == ModEntities.PIGLIN_RAVAGER) {
            level.playSound(null, spawnPos, SoundEvents.HOGLIN_ANGRY, SoundSource.HOSTILE, 1.5f, 0.7f);
        } else if (type == ModEntities.PIGLIN_EVOKER) {
            level.playSound(null, spawnPos, SoundEvents.PIGLIN_BRUTE_ANGRY, SoundSource.HOSTILE, 1.1f, 0.85f);
        }
    }

    private static void playCompletionFanfare(ServerLevel level, BlockPos origin) {
        level.playSound(null, origin, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
        spawnRandomizedParticleBurst(level, origin, ParticleTypes.FIREWORK, 60, 10.0, 0.02);
        spawnRandomizedParticleBurst(level, origin, ParticleTypes.TOTEM_OF_UNDYING, 30, 4.0, 0.02);
    }

    private static void playExiledPiglinSpawnEffects(ServerLevel level, BlockPos spawnPos) {
        level.playSound(null, spawnPos, SoundEvents.PIGLIN_AMBIENT, SoundSource.NEUTRAL, 1.0f, 0.7f);
        spawnParticle(level, ParticleTypes.SMOKE, spawnPos.getX() + 0.5, spawnPos.getY() + 0.5, spawnPos.getZ() + 0.5, 15, 0.5, 0.5, 0.5, 0.02);
    }

    private static void spawnRandomizedParticleBurst(
        ServerLevel level,
        BlockPos origin,
        ParticleOptions particle,
        int count,
        double radius,
        double speed
    ) {
        RandomSource random = level.getRandom();
        double centerX = origin.getX() + 0.5;
        double centerY = origin.getY() + 2.0;
        double centerZ = origin.getZ() + 0.5;
        for (int i = 0; i < count; i++) {
            double x = centerX + random.nextDouble() * radius - radius / 2.0;
            double y = centerY + random.nextDouble() * radius - radius / 2.0;
            double z = centerZ + random.nextDouble() * radius - radius / 2.0;
            spawnParticle(level, particle, x, y, z, 1, 0.0, 0.0, 0.0, speed);
        }
    }

    private static void spawnParticle(
        ServerLevel level,
        ParticleOptions particle,
        double x,
        double y,
        double z,
        int count,
        double xSpread,
        double ySpread,
        double zSpread,
        double speed
    ) {
        level.sendParticles(particle, x, y, z, count, xSpread, ySpread, zSpread, speed);
    }

    private static PortalInterior findPortalInterior(ServerLevel level, BlockPos origin) {
        PortalInterior xAxisInterior = findPortalInterior(level, origin, Direction.Axis.X);
        if (xAxisInterior != null) {
            return xAxisInterior;
        }
        return findPortalInterior(level, origin, Direction.Axis.Z);
    }

    private static PortalInterior findPortalInterior(ServerLevel level, BlockPos origin, Direction.Axis axis) {
        PortalInterior compact = findPortalInterior(level, origin, axis, 4, 5);
        if (compact != null) {
            return compact;
        }
        return findPortalInterior(level, origin, axis, 6, 7);
    }

    private static PortalInterior findPortalInterior(ServerLevel level, BlockPos origin, Direction.Axis axis, int width, int height) {
        List<BlockPos> frameBlocks = portalFrameBlocks(origin, axis, width, height);
        for (BlockPos frameBlock : frameBlocks) {
            if (!isPortalFrame(level, frameBlock.getX(), frameBlock.getY(), frameBlock.getZ())) {
                return null;
            }
        }

        List<BlockPos> interiorBlocks = new ArrayList<>();
        int baseY = origin.getY() + 1;
        int left = -width / 2;
        int right = left + width - 1;
        int topY = baseY + height - 1;
        if (axis == Direction.Axis.X) {
            for (int x = origin.getX() + left + 1; x < origin.getX() + right; x++) {
                for (int y = baseY + 1; y < topY; y++) {
                    interiorBlocks.add(new BlockPos(x, y, origin.getZ()));
                }
            }
        } else {
            for (int z = origin.getZ() + left + 1; z < origin.getZ() + right; z++) {
                for (int y = baseY + 1; y < topY; y++) {
                    interiorBlocks.add(new BlockPos(origin.getX(), y, z));
                }
            }
        }
        return new PortalInterior(axis, interiorBlocks, frameBlocks);
    }

    private static List<BlockPos> portalFrameBlocks(BlockPos origin, Direction.Axis axis, int width, int height) {
        List<BlockPos> blocks = new ArrayList<>();
        int baseY = origin.getY() + 1;
        int topY = baseY + height - 1;
        int left = -width / 2;
        int right = left + width - 1;

        if (axis == Direction.Axis.X) {
            int leftX = origin.getX() + left;
            int rightX = origin.getX() + right;
            for (int y = baseY; y <= topY; y++) {
                blocks.add(new BlockPos(leftX, y, origin.getZ()));
                blocks.add(new BlockPos(rightX, y, origin.getZ()));
            }
            for (int x = leftX; x <= rightX; x++) {
                blocks.add(new BlockPos(x, baseY, origin.getZ()));
                blocks.add(new BlockPos(x, topY, origin.getZ()));
            }
        } else {
            int leftZ = origin.getZ() + left;
            int rightZ = origin.getZ() + right;
            for (int y = baseY; y <= topY; y++) {
                blocks.add(new BlockPos(origin.getX(), y, leftZ));
                blocks.add(new BlockPos(origin.getX(), y, rightZ));
            }
            for (int z = leftZ; z <= rightZ; z++) {
                blocks.add(new BlockPos(origin.getX(), baseY, z));
                blocks.add(new BlockPos(origin.getX(), topY, z));
            }
        }

        return blocks;
    }

    private static BlockPos findNearbyPortalFrame(ServerLevel level, ServerPlayer player, int range) {
        BlockPos origin = player.blockPosition();
        double rangeSquared = (double) range * (double) range;
        for (int y = origin.getY() - 8; y <= origin.getY() + 8; y++) {
            for (int x = origin.getX() - range; x <= origin.getX() + range; x++) {
                for (int z = origin.getZ() - range; z <= origin.getZ() + range; z++) {
                    BlockPos portalOrigin = findPortalOriginAt(level, x, y, z);
                    if (portalOrigin != null
                        && horizontalDistanceSqr(player.blockPosition(), portalOrigin) <= rangeSquared) {
                        return portalOrigin;
                    }
                }
            }
        }
        return null;
    }

    private static BlockPos findNearbyGeneratedPortal(ServerLevel level, ServerPlayer player, int range) {
        BlockPos portal = findPortalDungeonOrigin(level, player.blockPosition(), range);
        if (portal != null) {
            return portal;
        }
        if (range <= PortalStructureHelper.MIDDLE_RADIUS) {
            return findNearbyPortalFrame(level, player, range);
        }
        return null;
    }

    private static BlockPos findPortalDungeonOrigin(ServerLevel level, BlockPos pos, int range) {
        double rangeSquared = (double) range * (double) range;
        int chunkRadius = Math.max(1, (range + 15) / 16 + 1);
        ChunkPos centerChunk = new ChunkPos(pos);
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        Set<BlockPos> seenOrigins = new HashSet<>();

        for (int chunkX = centerChunk.x - chunkRadius; chunkX <= centerChunk.x + chunkRadius; chunkX++) {
            for (int chunkZ = centerChunk.z - chunkRadius; chunkZ <= centerChunk.z + chunkRadius; chunkZ++) {
                if (!level.hasChunk(chunkX, chunkZ)) {
                    continue;
                }
                for (StructureStart start : level.structureManager().startsForStructure(
                    new ChunkPos(chunkX, chunkZ),
                    structure -> structure.type() == ModStructures.PORTAL_DUNGEON_TYPE
                )) {
                    for (StructurePiece piece : start.getPieces()) {
                        if (piece instanceof PortalDungeonPiece dungeonPiece) {
                            BlockPos origin = dungeonPiece.portalOrigin().immutable();
                            if (!seenOrigins.add(origin)) {
                                continue;
                            }
                            double distance = horizontalDistanceSqr(pos, origin);
                            if (distance <= rangeSquared && distance < nearestDistance) {
                                nearest = origin;
                                nearestDistance = distance;
                            }
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private static BlockPos findPortalOriginAt(ServerLevel level, int centerX, int baseY, int centerZ) {
        if (!isPortalFrame(level, centerX, baseY, centerZ)) {
            return null;
        }
        BlockPos origin = new BlockPos(centerX, baseY - 1, centerZ);
        return findPortalInterior(level, origin) == null ? null : origin;
    }

    private static boolean isPortalFrame(ServerLevel level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN) || state.is(BlockTags.PORTALS);
    }

    private static long raidKey(ServerLevel level, BlockPos portalPos) {
        return ((long) level.dimension().hashCode() << 32) ^ portalPos.asLong();
    }

    private static double horizontalDistance(BlockPos first, BlockPos second) {
        return Math.sqrt(horizontalDistanceSqr(first, second));
    }

    private static double horizontalDistance(int x, int z, BlockPos origin) {
        double dx = x - origin.getX();
        double dz = z - origin.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    private static double horizontalDistanceSqr(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    private static void clearRuntimeState() {
        for (RaidState state : ACTIVE_RAIDS.values()) {
            state.bossBar.removeAllPlayers();
        }
        ACTIVE_RAIDS.clear();
        NEXT_AMBIENT_SPAWN_TICK.clear();
        NEXT_GHAST_SPAWN_TICK.clear();
        ANCHORED_GHASTS.clear();
    }

    private static int expectedWaveSize(int waveIndex) {
        return switch (waveIndex) {
            case 0 -> 20;
            case 1 -> 27;
            case 2 -> 34;
            case 3 -> 30;
            case 4 -> 37;
            default -> 0;
        };
    }

    private static final class RaidState {
        private final ServerLevel level;
        private final BlockPos origin;
        private final ServerBossEvent bossBar;
        private final PortalRaidState portalRaidState;
        private final List<UUID> activeMobs = new ArrayList<>();
        private final Set<UUID> trackedPlayers = new HashSet<>();
        private int waveIndex;
        private int delayTicks;
        private int waveSize;

        private RaidState(ServerLevel level, BlockPos origin, ServerBossEvent bossBar, PortalRaidState portalRaidState) {
            this(level, origin, bossBar, portalRaidState, 0);
        }

        private RaidState(ServerLevel level, BlockPos origin, ServerBossEvent bossBar, PortalRaidState portalRaidState, int waveIndex) {
            this.level = level;
            this.origin = origin;
            this.bossBar = bossBar;
            this.portalRaidState = portalRaidState;
            this.waveIndex = waveIndex;
            this.delayTicks = 0;
        }

        private void persistWaveState() {
            long waveEndTick = this.activeMobs.isEmpty() && this.delayTicks > 0
                ? this.level.getGameTime() + this.delayTicks
                : 0L;
            this.portalRaidState.updateWave(this.origin, this.waveIndex + 1, new HashSet<>(this.activeMobs), waveEndTick);
        }
    }

    private record SpawnEntry(EntityType<? extends LivingEntity> type, int count) {
    }

    private record AmbientSpawnEntry(EntityType<? extends LivingEntity> type, int weight) {
    }

    private record PortalGhastAnchor(BlockPos origin, long expireTick) {
    }

    private record PortalInterior(Direction.Axis axis, List<BlockPos> blocks, List<BlockPos> frameBlocks) {
    }
}
