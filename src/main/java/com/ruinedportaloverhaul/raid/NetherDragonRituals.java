package com.ruinedportaloverhaul.raid;

import com.ruinedportaloverhaul.advancement.ModAdvancementTriggers;
import com.ruinedportaloverhaul.advancement.PortalEventTrigger;
import com.ruinedportaloverhaul.entity.NetherDragonEntity;
import com.ruinedportaloverhaul.entity.NetherCrystalEntity;
import com.ruinedportaloverhaul.sound.ModSounds;
import com.ruinedportaloverhaul.structure.PortalStructureHelper;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public final class NetherDragonRituals {
    private static final double RITUAL_MESSAGE_RANGE_SQUARED = 64.0 * 64.0;
    private static final double BOSS_BAR_RANGE_SQUARED = 96.0 * 96.0;
    private static final int TITLE_TICK = 40;
    private static final int SPAWN_TICK = 80;

    private static final Map<Long, SummoningSequence> SUMMONING_SEQUENCES = new HashMap<>();
    private static final Map<UUID, ServerBossEvent> BOSS_BARS = new HashMap<>();

    private NetherDragonRituals() {
    }

    public static void initialize() {
        ServerTickEvents.END_SERVER_TICK.register(NetherDragonRituals::tick);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> clearRuntimeState());
    }

    public static void onNetherCrystalPlaced(ServerLevel level, BlockPos pedestalPos, NetherCrystalEntity crystal) {
        PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
        portalRaidState.completedPortalForPedestal(pedestalPos).ifPresent(portalOrigin -> {
            PortalRaidState.RitualProgress progress = portalRaidState.markRitualCrystalPlaced(portalOrigin, pedestalPos);
            if (progress.allFilled() && !portalRaidState.isDragonActive(portalOrigin) && !SUMMONING_SEQUENCES.containsKey(portalOrigin.asLong())) {
                beginSummoning(level, portalOrigin);
            }
        });
    }

    public static void onNetherDragonDeath(ServerLevel level, NetherDragonEntity dragon) {
        BlockPos portalOrigin = dragon.portalOrigin();
        dragon.spawnAtLocation(level, new ItemStack(Items.NETHER_STAR, 2));
        dragon.spawnAtLocation(level, new ItemStack(Items.ANCIENT_DEBRIS, 1 + level.getRandom().nextInt(3)));
        triggerNearby(level, portalOrigin, ModAdvancementTriggers.NETHER_DRAGON_DEFEATED);
        shatterPedestals(level, portalOrigin);
        PortalRaidState.get(level.getServer()).clearRitual(portalOrigin);

        ServerBossEvent bossBar = BOSS_BARS.remove(dragon.getUUID());
        if (bossBar != null) {
            bossBar.removeAllPlayers();
        }
    }

    private static void beginSummoning(ServerLevel level, BlockPos portalOrigin) {
        BlockPos origin = portalOrigin.immutable();
        triggerNearby(level, origin, ModAdvancementTriggers.NETHER_CRYSTAL_RITUAL_COMPLETE);
        SUMMONING_SEQUENCES.put(origin.asLong(), new SummoningSequence(level, origin, level.getGameTime()));
        playOpeningPulse(level, origin);
    }

    private static void triggerNearby(ServerLevel level, BlockPos origin, PortalEventTrigger trigger) {
        for (ServerPlayer player : level.getPlayers(player -> player.blockPosition().distSqr(origin) <= RITUAL_MESSAGE_RANGE_SQUARED)) {
            ModAdvancementTriggers.trigger(trigger, player);
        }
    }

    private static void tick(MinecraftServer server) {
        reconcilePortalState(server);
        tickSummoningSequences();
        tickBossBars(server);
    }

    private static void reconcilePortalState(MinecraftServer server) {
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

            portalRaidState.setDragonActive(portalOrigin, false);
            if (sequence == null && progress.allFilled()) {
                beginSummoning(level, portalOrigin);
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

    private static void tickBossBars(MinecraftServer server) {
        Iterator<Map.Entry<UUID, ServerBossEvent>> iterator = BOSS_BARS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, ServerBossEvent> entry = iterator.next();
            ServerBossEvent bossBar = entry.getValue();
            NetherDragonEntity dragon = findDragonByUuid(server, entry.getKey());

            if (dragon == null || !dragon.isAlive()) {
                bossBar.removeAllPlayers();
                iterator.remove();
                continue;
            }

            bossBar.setProgress(Math.max(0.0f, dragon.getHealth() / dragon.getMaxHealth()));
            ServerLevel dragonLevel = (ServerLevel) dragon.level();
            for (ServerPlayer player : dragonLevel.players()) {
                if (player.blockPosition().distSqr(dragon.portalOrigin()) <= BOSS_BAR_RANGE_SQUARED) {
                    bossBar.addPlayer(player);
                } else {
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
        for (ServerPlayer player : level.getPlayers(player -> player.blockPosition().distSqr(origin) <= RITUAL_MESSAGE_RANGE_SQUARED)) {
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(Component.literal("The Nether Dragon Awakens").withStyle(ChatFormatting.DARK_RED)));
            player.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("Flee or fight.").withStyle(ChatFormatting.RED)));
        }
    }

    private static void spawnDragon(ServerLevel level, BlockPos origin) {
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
            Component.literal("The Nether Dragon").withStyle(ChatFormatting.DARK_RED),
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
        AABB searchBox = new AABB(portalOrigin).inflate(256.0, 192.0, 256.0);
        for (ServerLevel level : server.getAllLevels()) {
            for (NetherDragonEntity dragon : level.getEntitiesOfClass(
                NetherDragonEntity.class,
                searchBox,
                candidate -> candidate.isAlive() && candidate.portalOrigin().equals(portalOrigin)
            )) {
                return dragon;
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
        }
        level.playSound(null, origin, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.4f, 0.7f);
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
