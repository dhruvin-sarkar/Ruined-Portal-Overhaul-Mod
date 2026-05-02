package com.ruinedportaloverhaul.raid;

import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.advancement.PortalEventTrigger;
import com.ruinedportaloverhaul.config.ModConfigManager;
import com.ruinedportaloverhaul.entity.NetherDragonEntity;
import com.ruinedportaloverhaul.entity.NetherCrystalEntity;
import com.ruinedportaloverhaul.item.ModItems;
import com.ruinedportaloverhaul.sound.ModSounds;
import com.ruinedportaloverhaul.structure.PortalStructureHelper;
import com.ruinedportaloverhaul.world.ModParticles;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.AABB;

public final class NetherDragonRituals {
    private static final double BOSS_BAR_RANGE_SQUARED = 96.0 * 96.0;
    private static final double RITUAL_MESSAGE_RANGE_SQUARED = BOSS_BAR_RANGE_SQUARED;
    private static final int TITLE_TICK = 40;
    private static final int SPAWN_TICK = 80;
    private static final int RITUAL_RECONCILE_INTERVAL_TICKS = 20;
    private static final int BOSS_BAR_PLAYER_SYNC_INTERVAL_TICKS = 20;

    private static final Map<Long, SummoningSequence> SUMMONING_SEQUENCES = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new HashMap<>();

    private NetherDragonRituals() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(NetherDragonRituals::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearRuntimeState());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> detachDisconnectedPlayer(handler.getPlayer()));
    }

    private static void detachDisconnectedPlayer(ServerPlayer player) {
        // Fix: dragon boss bar player tracking rebuilt from the live player list each tick, but never explicitly removed disconnecting players, so their ServerPlayer references leaked in the bar's internal set. Disconnect hook now sweeps every active dragon bar.
        if (player == null) {
            return;
        }
        for (ServerBossEvent bossBar : BOSS_BARS.values()) {
            bossBar.removePlayer(player);
        }
    }

    public static void onNetherCrystalPlaced(ServerLevel level, BlockPos pedestalPos, NetherCrystalEntity crystal) {
        // Fix: ritual placements used to summon unconditionally, so new crystals now respect the live dragon toggle while still preserving ritual progress.
        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        portalRaidState.completedPortalForPedestal(pedestalPos).ifPresent(portalOrigin -> {
            PortalRaidState.RitualProgress progress = portalRaidState.markRitualCrystalPlaced(portalOrigin, pedestalPos);
            if (ModConfigManager.enableNetherDragon()
                && progress.allFilled()
                && !portalRaidState.isDragonActive(portalOrigin)
                && !SUMMONING_SEQUENCES.containsKey(portalOrigin.asLong())) {
                beginSummoning(level, portalOrigin);
            }
        });
    }

    public static void onPortalCompleted(ServerLevel level, BlockPos portalOrigin) {
        // Fix: crystals staged on the pedestals before raid completion never entered ritual state because the tracker only learned about completed portals after a later placement. Portal completion now reconciles any already-present crystals immediately and starts the summoning sequence if the full set is waiting.
        if (!arePortalChunksLoaded(level, portalOrigin)) {
            return;
        }

        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        PortalRaidState.RitualProgress progress = portalRaidState.syncRitualCrystals(portalOrigin, findFilledPedestals(level, portalOrigin));
        if (ModConfigManager.enableNetherDragon()
            && progress.allFilled()
            && !portalRaidState.isDragonActive(portalOrigin)
            && !SUMMONING_SEQUENCES.containsKey(portalOrigin.asLong())) {
            beginSummoning(level, portalOrigin);
        }
    }

    public static void onNetherDragonFinaleStart(ServerLevel level, NetherDragonEntity dragon) {
        BlockPos portalOrigin = dragon.portalOrigin();
        shatterPedestals(level, portalOrigin);
        level.playSound(null, portalOrigin, ModSounds.RITUAL_VICTORY, SoundSource.HOSTILE, 1.2f, 0.9f);
    }

    public static void onNetherDragonDeath(ServerLevel level, NetherDragonEntity dragon) {
        // Rewards land after the pedestal shatter beat so the boss death reads as a finale instead of one overloaded tick.
        BlockPos portalOrigin = dragon.portalOrigin();
        if (level.getGameRules().get(GameRules.MOB_DROPS)) {
            // Fix: spawnAtLocation(dragon) dropped loot at the dragon's body position, which is ~5 blocks above the portal by end of the 60-tick death rise and often scattered outside the arena. Loot now lands at the pedestal ring so players fighting from the ritual floor can actually pick it up.
            dropRitualLoot(level, portalOrigin, new ItemStack(Items.NETHER_STAR, 2));
            dropRitualLoot(level, portalOrigin, new ItemStack(Items.ANCIENT_DEBRIS, 1 + level.getRandom().nextInt(3)));
            dropRitualLoot(level, portalOrigin, new ItemStack(ModItems.CORRUPTED_NETHERITE_INGOT, 1 + level.getRandom().nextInt(2)));
            if (level.getRandom().nextFloat() < 0.3f) {
                dropRitualLoot(level, portalOrigin, new ItemStack(ModItems.NETHER_DRAGON_SCALE));
            }
            PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
            if (portalRaidState.tryMarkNetherTideDiscRolled() && level.getRandom().nextFloat() < 0.15f) {
                dropRitualLoot(level, portalOrigin, new ItemStack(ModItems.MUSIC_DISC_NETHER_TIDE));
            }
        }
        triggerNearby(level, portalOrigin, ModAdvancementTriggers.NETHER_DRAGON_DEFEATED);
        PortalRaidState.get(level.getServer()).clearRitual(portalOrigin);

        ServerBossEvent bossBar = BOSS_BARS.remove(dragon.getUUID());
        if (bossBar != null) {
            bossBar.removeAllPlayers();
        }
    }

    private static void dropRitualLoot(ServerLevel level, BlockPos portalOrigin, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        double x = portalOrigin.getX() + 0.5;
        double y = portalOrigin.getY() + 1.0;
        double z = portalOrigin.getZ() + 0.5;
        ItemEntity item = new ItemEntity(level, x, y, z, stack);
        item.setDefaultPickUpDelay();
        level.addFreshEntity(item);
    }

    private static void beginSummoning(ServerLevel level, BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        triggerNearby(level, origin, ModAdvancementTriggers.NETHER_CRYSTAL_RITUAL_COMPLETE);
        SUMMONING_SEQUENCES.put(origin.asLong(), new SummoningSequence(level, origin, level.getGameTime()));
        playOpeningPulse(level, origin);
    }

    private static void triggerNearby(ServerLevel level, BlockPos origin, PortalEventTrigger trigger) {
        // Fix: ritual progression checks used full 3D distance, so players directly below the portal in the pit or caves could miss summon/death progression despite staying inside the dungeon footprint. Portal-centered ritual cues now follow horizontal X/Z distance like the raid and storm systems.
        for (ServerPlayer player : level.getPlayers(player -> horizontalDistanceSqr(player.blockPosition(), origin) <= RITUAL_MESSAGE_RANGE_SQUARED)) {
            ModAdvancementTriggers.trigger(trigger, player);
        }
    }

    private static void tick(MinecraftServer server) {
        // Fix: ritual recovery and dragon boss-bar membership were doing portal/chunk/entity scans every server tick. Recovery now runs once per second while summoning timing and boss-bar progress stay tick-responsive.
        long gameTime = server.overworld().getGameTime();
        if (gameTime % RITUAL_RECONCILE_INTERVAL_TICKS == 0) {
            reconcilePortalState(server);
        }
        tickSummoningSequences();
        tickBossBars(server, gameTime);
    }

    private static void reconcilePortalState(MinecraftServer server) {
        // Fix: ritual reconciliation now keeps existing dragons intact, preserves saved active-dragon locks while dragons are unloaded, and refuses new summoning sequences when the dragon feature is disabled.
        ServerLevel level = server.overworld();
        PortalRaidState portalRaidState = PortalRaidState.get(server);
        Set<BlockPos> trackedPortals = new HashSet<>(portalRaidState.ritualPortalOrigins());
        trackedPortals.addAll(portalRaidState.activeDragonPortalOrigins());

        for (BlockPos portalOrigin : trackedPortals) {
            if (!arePortalChunksLoaded(level, portalOrigin)) {
                continue;
            }

            PortalRaidState.RitualProgress progress = portalRaidState.syncRitualCrystals(portalOrigin, findFilledPedestals(level, portalOrigin));
            SummoningSequence sequence = SUMMONING_SEQUENCES.get(portalOrigin.asLong());
            if (sequence != null && !progress.allFilled()) {
                SUMMONING_SEQUENCES.remove(portalOrigin.asLong());
                sequence = null;
            }

            NetherDragonEntity dragon = findLoadedDragon(server, portalOrigin);
            if (dragon != null) {
                portalRaidState.setDragonActive(portalOrigin, true);
                ensureBossBar(dragon);
                continue;
            }

            if (portalRaidState.isDragonActive(portalOrigin)) {
                continue;
            }
            if (ModConfigManager.enableNetherDragon() && sequence == null && progress.allFilled()) {
                beginSummoning(level, portalOrigin);
            } else if (!ModConfigManager.enableNetherDragon() && sequence != null) {
                SUMMONING_SEQUENCES.remove(portalOrigin.asLong());
            }
        }
    }

    private static void tickSummoningSequences() {
        Iterator<SummoningSequence> iterator = SUMMONING_SEQUENCES.values().iterator();
        while (iterator.hasNext()) {
            SummoningSequence sequence = iterator.next();
            if (!arePortalChunksLoaded(sequence.level(), sequence.portalOrigin())) {
                continue;
            }
            long elapsed = sequence.level().getGameTime() - sequence.startTick();
            if (!sequence.titleSent() && elapsed >= TITLE_TICK) {
                broadcastTitle(sequence.level(), sequence.portalOrigin());
                sequence.markTitleSent();
            }
            if (elapsed >= SPAWN_TICK) {
                spawnDragon(sequence.level(), sequence.portalOrigin());
                iterator.remove();
            }
        }
    }

    private static void tickBossBars(MinecraftServer server, long gameTime) {
        // Fix: dragon boss bars used vertical-aware distance and rebuilt viewer sets every tick. Progress/name still update every tick, but player membership now syncs on the same 20-tick cadence as raid boss bars using horizontal portal distance.
        Iterator<Map.Entry<UUID, ServerBossEvent>> iterator = BOSS_BARS.entrySet().iterator();
        boolean syncPlayers = gameTime % BOSS_BAR_PLAYER_SYNC_INTERVAL_TICKS == 0;
        while (iterator.hasNext()) {
            Map.Entry<UUID, ServerBossEvent> entry = iterator.next();
            ServerBossEvent bossBar = entry.getValue();
            NetherDragonEntity dragon = findDragonByUuid(server, entry.getKey());

            if (dragon == null || !dragon.isAlive()) {
                bossBar.removeAllPlayers();
                iterator.remove();
                continue;
            }

            bossBar.setName(dragon.bossBarTitle());
            bossBar.setProgress(Math.max(0.0f, dragon.getHealth() / dragon.getMaxHealth()));
            if (!syncPlayers) {
                continue;
            }
            ServerLevel dragonLevel = (ServerLevel) dragon.level();
            Set<ServerPlayer> inRangePlayers = new HashSet<>();
            for (ServerPlayer player : dragonLevel.players()) {
                if (horizontalDistanceSqr(player.blockPosition(), dragon.portalOrigin()) <= BOSS_BAR_RANGE_SQUARED) {
                    inRangePlayers.add(player);
                    bossBar.addPlayer(player);
                }
            }
            for (ServerPlayer player : new java.util.ArrayList<>(bossBar.getPlayers())) {
                if (!inRangePlayers.contains(player)) {
                    bossBar.removePlayer(player);
                }
            }
        }
    }

    private static void playOpeningPulse(ServerLevel level, BlockPos origin) {
        level.playSound(null, origin, ModSounds.RITUAL_DRAGON_SUMMON, SoundSource.HOSTILE, 2.0f, 0.85f);
        spawnSphericalBurst(level, origin, ParticleTypes.FLAME, 80, 5.0, 0.02);
        spawnSphericalBurst(level, origin, ParticleTypes.LARGE_SMOKE, 40, 5.5, 0.01);
    }

    private static void broadcastTitle(ServerLevel level, BlockPos origin) {
        // Fix: the ritual summon title used full 3D distance, so nearby players directly beneath the portal could miss the warning. The localized title card now respects the same horizontal portal footprint as the rest of the dungeon flow.
        for (ServerPlayer player : level.getPlayers(player -> horizontalDistanceSqr(player.blockPosition(), origin) <= RITUAL_MESSAGE_RANGE_SQUARED)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("title.ruined_portal_overhaul.ritual.dragon_awakens").withStyle(ChatFormatting.DARK_RED)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("subtitle.ruined_portal_overhaul.ritual.dragon_awakens").withStyle(ChatFormatting.RED)));
        }
    }

    private static void spawnDragon(ServerLevel level, BlockPos origin) {
        // Fix: the summon stage now rechecks the dragon toggle so disabling the feature mid-ritual cannot still spawn a boss from a queued sequence.
        if (!ModConfigManager.enableNetherDragon()) {
            return;
        }
        NetherDragonEntity dragon = new NetherDragonEntity(level, origin);
        dragon.setPos(origin.getX() + 0.5, origin.getY() + 10.0, origin.getZ() + 0.5);
        dragon.setYRot(level.getRandom().nextFloat() * 360.0f);
        dragon.setXRot(0.0f);
        level.addFreshEntity(dragon);
        PortalRaidState.get(level.getServer()).setDragonActive(origin, true);
        level.playSound(null, origin, ModSounds.RITUAL_DRAGON_SUMMON, SoundSource.HOSTILE, 5.0f, 0.85f);

        ensureBossBar(dragon);
    }

    private static void ensureBossBar(NetherDragonEntity dragon) {
        if (BOSS_BARS.containsKey(dragon.getUUID())) {
            return;
        }

        ServerBossEvent bossBar = new ServerBossEvent(
            dragon.bossBarTitle(),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.PROGRESS
        );
        bossBar.setDarkenScreen(true);
        bossBar.setCreateWorldFog(true);
        BOSS_BARS.put(dragon.getUUID(), bossBar);
    }

    private static NetherDragonEntity findDragonByUuid(MinecraftServer server, UUID dragonUuid) {
        for (ServerLevel level : server.getAllLevels()) {
            if (level.getEntity(dragonUuid) instanceof NetherDragonEntity foundDragon) {
                return foundDragon;
            }
        }
        return null;
    }

    private static NetherDragonEntity findLoadedDragon(MinecraftServer server, BlockPos portalOrigin) {
        // Fix: active dragon recovery used to search only near the portal, so a wide-roaming dragon could be missed and a duplicate ritual could begin. Scan all currently loaded entities and let the saved active flag cover temporarily unloaded dragons.
        for (ServerLevel level : server.getAllLevels()) {
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (entity instanceof NetherDragonEntity dragon
                    && dragon.isAlive()
                    && dragon.portalOrigin().equals(portalOrigin)) {
                    return dragon;
                }
            }
        }
        return null;
    }

    private static Set<BlockPos> findFilledPedestals(ServerLevel level, BlockPos portalOrigin) {
        Set<BlockPos> filledPedestals = new HashSet<>();
        for (BlockPos pedestal : PortalStructureHelper.ritualPedestalPositions(portalOrigin)) {
            if (hasCrystalAt(level, pedestal)) {
                filledPedestals.add(pedestal.immutable());
            }
        }
        return filledPedestals;
    }

    private static boolean hasCrystalAt(ServerLevel level, BlockPos pedestal) {
        AABB crystalBox = new AABB(pedestal.above()).inflate(0.75, 1.5, 0.75);
        for (EndCrystal crystal : level.getEntitiesOfClass(EndCrystal.class, crystalBox)) {
            if (crystal instanceof NetherCrystalEntity && crystal.isAlive()) {
                return true;
            }
        }
        return false;
    }

    private static boolean arePortalChunksLoaded(ServerLevel level, BlockPos portalOrigin) {
        if (!isChunkLoaded(level, portalOrigin)) {
            return false;
        }
        for (BlockPos pedestal : PortalStructureHelper.ritualPedestalPositions(portalOrigin)) {
            if (!isChunkLoaded(level, pedestal)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isChunkLoaded(ServerLevel level, BlockPos pos) {
        return level.hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static void shatterPedestals(ServerLevel level, BlockPos origin) {
        // Fix: pedestal shattering previously played a raw explode event, which left the ritual finale partly outside the mod sound registry. The break pulse now uses a replaceable ritual sound id.
        for (BlockPos pedestal : PortalStructureHelper.ritualPedestalPositions(origin)) {
            AABB crystalBox = new AABB(pedestal.above()).inflate(0.75, 1.5, 0.75);
            for (EndCrystal crystal : level.getEntitiesOfClass(EndCrystal.class, crystalBox)) {
                if (crystal instanceof NetherCrystalEntity) {
                    crystal.discard();
                }
            }

            level.setBlock(pedestal, Blocks.AIR.defaultBlockState(), 3);
            level.sendParticles(ParticleTypes.EXPLOSION, pedestal.getX() + 0.5, pedestal.getY() + 0.5, pedestal.getZ() + 0.5, 8, 0.35, 0.35, 0.35, 0.02);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, pedestal.getX() + 0.5, pedestal.getY() + 0.5, pedestal.getZ() + 0.5, 12, 0.45, 0.45, 0.45, 0.01);
            level.sendParticles(ModParticles.CORRUPTION_RUNE, pedestal.getX() + 0.5, pedestal.getY() + 1.0, pedestal.getZ() + 0.5, 10, 0.35, 0.35, 0.35, 0.02);
        }
        level.playSound(null, origin, ModSounds.RITUAL_PEDESTAL_SHATTER, SoundSource.BLOCKS, 1.4f, 0.7f);
    }

    private static double horizontalDistanceSqr(BlockPos first, BlockPos second) {
        double dx = first.getX() - second.getX();
        double dz = first.getZ() - second.getZ();
        return dx * dx + dz * dz;
    }

    private static void spawnSphericalBurst(
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
            double theta = random.nextDouble() * Math.PI * 2.0;
            double yOffset = random.nextDouble() * 2.0 - 1.0;
            double horizontal = Math.sqrt(Math.max(0.0, 1.0 - yOffset * yOffset));
            double distance = radius * Math.cbrt(random.nextDouble());
            double x = centerX + Math.cos(theta) * horizontal * distance;
            double y = centerY + yOffset * distance;
            double z = centerZ + Math.sin(theta) * horizontal * distance;
            level.sendParticles(particle, x, y, z, 1, 0.0, 0.0, 0.0, speed);
        }
    }

    private static void clearRuntimeState() {
        SUMMONING_SEQUENCES.clear();
        for (ServerBossEvent bossBar : BOSS_BARS.values()) {
            bossBar.removeAllPlayers();
        }
        BOSS_BARS.clear();
    }

    private static final class SummoningSequence {
        private final ServerLevel level;
        private final BlockPos portalOrigin;
        private final long startTick;
        private boolean titleSent;

        private SummoningSequence(ServerLevel level, BlockPos portalOrigin, long startTick) {
            this.level = level;
            this.portalOrigin = portalOrigin;
            this.startTick = startTick;
        }

        private ServerLevel level() {
            return this.level;
        }

        private BlockPos portalOrigin() {
            return this.portalOrigin;
        }

        private long startTick() {
            return this.startTick;
        }

        private boolean titleSent() {
            return this.titleSent;
        }

        private void markTitleSent() {
            this.titleSent = true;
        }
    }
}
