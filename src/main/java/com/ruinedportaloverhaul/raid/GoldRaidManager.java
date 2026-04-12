package com.ruinedportaloverhaul.raid;

import com.ruinedportaloverhaul.entity.ModEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
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
    private static final int WAVE_DELAY_TICKS = 300;

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
        if (gameTime % 20 == 0) {
            PortalRaidState portalRaidState = PortalRaidState.get(level.getServer());
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
                    if (!ACTIVE_RAIDS.containsKey(key)) {
                        portalRaidState.clearActiveRaid(portal);
                    } else {
                        continue;
                    }
                }
                if (!ACTIVE_RAIDS.containsKey(key)) {
                    startRaid(level, player, portal, key, portalRaidState);
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

    private static boolean tickRaid(RaidState state) {
        state.bossBar.removeAllPlayers();
        for (ServerPlayer player : state.level.getPlayers(player -> player.distanceToSqr(state.origin.getX() + 0.5, state.origin.getY() + 0.5, state.origin.getZ() + 0.5) < 1600.0)) {
            state.bossBar.addPlayer(player);
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
            int seconds = Math.max(1, state.delayTicks / 20);
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

    private static void startRaid(ServerLevel level, ServerPlayer player, BlockPos origin, long key, PortalRaidState portalRaidState) {
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
        return entity;
    }

    private static void finishRaid(RaidState state) {
        state.bossBar.removeAllPlayers();
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
        BlockPos base = origin.offset(0, 1, 0);
        for (int y = 0; y < 4; y++) {
            BlockPos pos = base.above(y);
            level.setBlock(pos, Blocks.NETHER_PORTAL.defaultBlockState().setValue(NetherPortalBlock.AXIS, net.minecraft.core.Direction.Axis.X), 2);
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
        com.ruinedportaloverhaul.entity.ExiledPiglinTraderEntity trader = ModEntities.EXILED_PIGLIN.spawn(
            level,
            origin.offset(2, 1, 0),
            EntitySpawnReason.MOB_SUMMONED
        );
        if (trader != null) {
            trader.setCustomName(Component.literal("Exiled Piglin"));
            trader.setCustomNameVisible(true);
            trader.rememberSpawnTime(level.getGameTime());
        }
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
        boolean hasFrame = isPortalFrame(level, centerX - 1, baseY, centerZ)
            && isPortalFrame(level, centerX + 1, baseY, centerZ)
            && isPortalFrame(level, centerX - 1, baseY + 1, centerZ)
            && isPortalFrame(level, centerX + 1, baseY + 1, centerZ)
            && isPortalFrame(level, centerX - 1, baseY + 2, centerZ)
            && isPortalFrame(level, centerX + 1, baseY + 2, centerZ);

        if (!hasFrame) {
            return null;
        }

        return new BlockPos(centerX, baseY - 1, centerZ);
    }

    private static boolean isPortalFrame(ServerLevel level, int x, int y, int z) {
        BlockState state = level.getBlockState(new BlockPos(x, y, z));
        return state.is(Blocks.OBSIDIAN) || state.is(Blocks.CRYING_OBSIDIAN) || state.is(BlockTags.PORTALS);
    }

    private static long raidKey(ServerLevel level, BlockPos portalPos) {
        return ((long) level.dimension().hashCode() << 32) ^ portalPos.asLong();
    }

    private static final class RaidState {
        private final ServerLevel level;
        private final BlockPos origin;
        private final ServerBossEvent bossBar;
        private final PortalRaidState portalRaidState;
        private final List<UUID> activeMobs = new ArrayList<>();
        private int waveIndex;
        private int delayTicks;
        private int waveSize;

        private RaidState(ServerLevel level, BlockPos origin, ServerBossEvent bossBar, PortalRaidState portalRaidState) {
            this.level = level;
            this.origin = origin;
            this.bossBar = bossBar;
            this.portalRaidState = portalRaidState;
            this.waveIndex = 0;
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
}
