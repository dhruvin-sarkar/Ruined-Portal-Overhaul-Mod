package com.ruinedportaloverhaul.raid;

import com.ruinedportaloverhaul.entity.ModEntities;
import com.ruinedportaloverhaul.entity.ExiledPiglinTraderEntity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

public final class GoldRaidManager {
    private static final int PORTAL_TRIGGER_RANGE = 12;
    private static final int AMBIENT_PARTICLE_RANGE = 24;
    private static final int BOSS_BAR_PLAYER_RANGE = 48;
    private static final int AMBIENT_PARTICLE_INTERVAL_TICKS = 40;
    private static final int INTER_WAVE_PULSE_INTERVAL_TICKS = 60;
    private static final int WAVE_DELAY_TICKS = 300;
    private static final double BOSS_BAR_PLAYER_RANGE_SQUARED = BOSS_BAR_PLAYER_RANGE * BOSS_BAR_PLAYER_RANGE;

    private static final ResourceKey<LootTable> BOSS_REWARD_LOOT = ResourceKey.create(
        Registries.LOOT_TABLE,
        com.ruinedportaloverhaul.world.ModStructures.id("chests/portal_boss_reward")
    );

    private static final String[] WAVE_LABELS = {
        "The Tribute Begins...",
        "They Grow Bolder",
        "The Brutes Arrive",
        "Chaos Unleashed",
        "The Evoker Awakens"
    };

    private static final Map<Long, RaidState> ACTIVE_RAIDS = new HashMap<>();

    private GoldRaidManager() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(GoldRaidManager::tick);
    }

    private static void tick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            tickLevel(level);
        }
    }

    private static void tickLevel(ServerLevel level) {
        if (level != level.getServer().overworld()) {
            return;
        }

        long gameTime = level.getGameTime();
        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        restorePersistedRaids(level, portalRaidState);

        if (gameTime % AMBIENT_PARTICLE_INTERVAL_TICKS == 0) {
            spawnAmbientPortalParticles(level);
        }

        if (gameTime % 20 == 0) {
            for (ServerPlayer player : level.players()) {
                if (!hasFullGoldArmor(player)) {
                    continue;
                }
                BlockPos portal = findNearbyPortalFrame(level, player, PORTAL_TRIGGER_RANGE);
                if (portal == null) {
                    continue;
                }
                if (portalRaidState.isCompleted(portal)) {
                    continue;
                }
                long key = raidKey(level, portal);
                if (portalRaidState.isRaidActive(portal)) {
                    continue;
                }
                if (!ACTIVE_RAIDS.containsKey(key)) {
                    startRaid(level, portal, key, portalRaidState);
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

    private static boolean tickRaid(RaidState state) {
        syncBossBarPlayers(state);

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
        double centerX = state.origin.getX() + 0.5;
        double centerY = state.origin.getY() + 0.5;
        double centerZ = state.origin.getZ() + 0.5;

        for (ServerPlayer player : state.level.players()) {
            if (player.distanceToSqr(centerX, centerY, centerZ) <= BOSS_BAR_PLAYER_RANGE_SQUARED) {
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

    private static void startRaid(ServerLevel level, BlockPos origin, long key, PortalRaidState portalRaidState) {
        if (!portalRaidState.beginRaid(origin)) {
            return;
        }
        ServerBossEvent bossBar = new ServerBossEvent(
            Component.literal(WAVE_LABELS[0]),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.setDarkenScreen(true);
        bossBar.setCreateWorldFog(false);
        RaidState state = new RaidState(level, origin, bossBar, portalRaidState);
        ACTIVE_RAIDS.put(key, state);
        spawnWave(state);
        playRaidStartEffects(level, origin);
        state.delayTicks = WAVE_DELAY_TICKS;
        state.persistWaveState();
    }

    private static void spawnWave(RaidState state) {
        state.activeMobs.clear();
        state.waveSize = 0;
        switch (state.waveIndex) {
            case 0 -> spawnWave(state, new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 6));
            case 1 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 6),
                new SpawnEntry(ModEntities.PIGLIN_VINDICATOR, 3)
            );
            case 2 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 4),
                new SpawnEntry(ModEntities.PIGLIN_VINDICATOR, 3),
                new SpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 2)
            );
            case 3 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_BRUTE_PILLAGER, 3),
                new SpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 2),
                new SpawnEntry(ModEntities.PIGLIN_RAVAGER, 1)
            );
            case 4 -> spawnWave(
                state,
                new SpawnEntry(ModEntities.PIGLIN_PILLAGER, 4),
                new SpawnEntry(ModEntities.PIGLIN_ILLUSIONER, 2),
                new SpawnEntry(ModEntities.PIGLIN_RAVAGER, 1),
                new SpawnEntry(ModEntities.PIGLIN_EVOKER, 1)
            );
            default -> {
            }
        }
        state.bossBar.setProgress(1.0f);
        state.persistWaveState();
    }

    @SafeVarargs
    private static void spawnWave(RaidState state, SpawnEntry... entries) {
        for (SpawnEntry entry : entries) {
            for (int i = 0; i < entry.count; i++) {
                LivingEntity entity = spawnMob(state, entry.type, i);
                if (entity != null) {
                    state.activeMobs.add(entity.getUUID());
                    state.waveSize++;
                }
            }
        }
    }

    private static LivingEntity spawnMob(RaidState state, EntityType<? extends LivingEntity> type, int offsetIndex) {
        double angle = (Math.PI * 2.0) * ((offsetIndex + state.waveIndex * 3) / 12.0);
        double radius = 6.0 + (offsetIndex % 3);
        BlockPos spawnPos = state.origin.offset(
            (int) Math.round(Math.cos(angle) * radius),
            1,
            (int) Math.round(Math.sin(angle) * radius)
        );
        LivingEntity entity = type.spawn(state.level, spawnPos, EntitySpawnReason.MOB_SUMMONED);
        if (entity instanceof Mob mob) {
            mob.setTarget(state.level.getNearestPlayer(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 24.0, false));
        }
        if (entity != null) {
            playHighThreatSpawnSound(state.level, type, spawnPos);
        }
        return entity;
    }

    private static void finishRaid(RaidState state) {
        state.bossBar.removeAllPlayers();
        state.trackedPlayers.clear();
        state.bossBar.setVisible(false);
        playCompletionFanfare(state.level, state.origin);
        ignitePortal(state.level, state.origin);
        spawnBossChest(state.level, state.origin);
        spawnExiledTrader(state.level, state.origin);
        state.portalRaidState.markCompleted(state.origin);

        Component message = Component.literal("The portal accepts the tribute.");
        for (ServerPlayer player : state.level.getPlayers(player -> player.distanceToSqr(state.origin.getX() + 0.5, state.origin.getY() + 0.5, state.origin.getZ() + 0.5) < 1600.0)) {
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
        }
    }

    private static void spawnExiledTrader(ServerLevel level, BlockPos origin) {
        level.setBlock(origin.offset(2, 1, 1), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), 3);
        ExiledPiglinTraderEntity trader = ModEntities.EXILED_PIGLIN.spawn(
            level,
            origin.offset(2, 1, 0),
            EntitySpawnReason.MOB_SUMMONED
        );
        if (trader != null) {
            trader.setCustomName(Component.literal("Exiled Piglin"));
            trader.setCustomNameVisible(true);
            trader.rememberSpawnTime(level.getGameTime());
            playExiledPiglinSpawnEffects(level, trader.blockPosition());
        }
    }

    private static void spawnAmbientPortalParticles(ServerLevel level) {
        Set<BlockPos> emitted = new HashSet<>();
        for (ServerPlayer player : level.players()) {
            BlockPos portal = findNearbyPortalFrame(level, player, AMBIENT_PARTICLE_RANGE);
            if (portal != null && emitted.add(portal.immutable())) {
                spawnAmbientPortalParticles(level, portal);
            }
        }
    }

    private static void spawnAmbientPortalParticles(ServerLevel level, BlockPos origin) {
        PortalInterior interior = findPortalInterior(level, origin);
        if (interior != null) {
            for (BlockPos framePos : portalFrameBlocks(origin, interior.axis())) {
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

    private static void playRaidStartEffects(ServerLevel level, BlockPos origin) {
        level.playSound(null, origin, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1.0f, 1.2f);
        spawnRandomizedParticleBurst(level, origin, ParticleTypes.LARGE_SMOKE, 40, 3.0, 0.01);
        spawnRandomizedParticleBurst(level, origin, ParticleTypes.FLAME, 20, 3.0, 0.01);
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
            level.playSound(null, spawnPos, SoundEvents.RAVAGER_ROAR, SoundSource.HOSTILE, 1.5f, 0.8f);
        } else if (type == ModEntities.PIGLIN_EVOKER) {
            level.playSound(null, spawnPos, SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.0f, 1.0f);
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
        List<BlockPos> frameBlocks = portalFrameBlocks(origin, axis);
        for (BlockPos frameBlock : frameBlocks) {
            if (!isPortalFrame(level, frameBlock.getX(), frameBlock.getY(), frameBlock.getZ())) {
                return null;
            }
        }

        List<BlockPos> interiorBlocks = new ArrayList<>();
        int baseY = origin.getY() + 1;
        if (axis == Direction.Axis.X) {
            for (int x = origin.getX() - 1; x <= origin.getX(); x++) {
                for (int y = baseY + 1; y <= baseY + 3; y++) {
                    interiorBlocks.add(new BlockPos(x, y, origin.getZ()));
                }
            }
        } else {
            for (int z = origin.getZ() - 1; z <= origin.getZ(); z++) {
                for (int y = baseY + 1; y <= baseY + 3; y++) {
                    interiorBlocks.add(new BlockPos(origin.getX(), y, z));
                }
            }
        }
        return new PortalInterior(axis, interiorBlocks);
    }

    private static List<BlockPos> portalFrameBlocks(BlockPos origin, Direction.Axis axis) {
        List<BlockPos> blocks = new ArrayList<>();
        int baseY = origin.getY() + 1;
        int topY = baseY + 4;

        if (axis == Direction.Axis.X) {
            int leftX = origin.getX() - 2;
            int rightX = origin.getX() + 1;
            for (int y = baseY; y <= topY; y++) {
                blocks.add(new BlockPos(leftX, y, origin.getZ()));
                blocks.add(new BlockPos(rightX, y, origin.getZ()));
            }
            for (int x = leftX; x <= rightX; x++) {
                blocks.add(new BlockPos(x, baseY, origin.getZ()));
                blocks.add(new BlockPos(x, topY, origin.getZ()));
            }
        } else {
            int leftZ = origin.getZ() - 2;
            int rightZ = origin.getZ() + 1;
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

    private static boolean hasFullGoldArmor(Player player) {
        return isGold(player, EquipmentSlot.HEAD)
            && isGold(player, EquipmentSlot.CHEST)
            && isGold(player, EquipmentSlot.LEGS)
            && isGold(player, EquipmentSlot.FEET);
    }

    private static boolean isGold(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        return switch (slot) {
            case HEAD -> stack.is(Items.GOLDEN_HELMET);
            case CHEST -> stack.is(Items.GOLDEN_CHESTPLATE);
            case LEGS -> stack.is(Items.GOLDEN_LEGGINGS);
            case FEET -> stack.is(Items.GOLDEN_BOOTS);
            default -> false;
        };
    }

    private static BlockPos findNearbyPortalFrame(ServerLevel level, ServerPlayer player, int range) {
        BlockPos origin = player.blockPosition();
        double rangeSquared = (double) range * (double) range;
        for (int y = origin.getY() - 4; y <= origin.getY() + 4; y++) {
            for (int x = origin.getX() - range; x <= origin.getX() + range; x++) {
                for (int z = origin.getZ() - range; z <= origin.getZ() + range; z++) {
                    BlockPos portalOrigin = findPortalOriginAt(level, x, y, z);
                    if (portalOrigin != null
                        && player.distanceToSqr(
                            portalOrigin.getX() + 0.5,
                            portalOrigin.getY() + 1.0,
                            portalOrigin.getZ() + 0.5
                        ) <= rangeSquared) {
                        return portalOrigin;
                    }
                }
            }
        }
        return null;
    }

    private static BlockPos findPortalOriginAt(ServerLevel level, int centerX, int baseY, int centerZ) {
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

    private static int expectedWaveSize(int waveIndex) {
        return switch (waveIndex) {
            case 0 -> 6;
            case 1 -> 9;
            case 2 -> 9;
            case 3 -> 6;
            case 4 -> 8;
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

    private record PortalInterior(Direction.Axis axis, List<BlockPos> blocks) {
    }
}
