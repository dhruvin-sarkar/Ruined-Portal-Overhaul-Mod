/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity.trialspawner;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.jspecify.annotations.Nullable;

public class TrialSpawnerStateData {
    private static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    final Set<UUID> detectedPlayers = new HashSet<UUID>();
    final Set<UUID> currentMobs = new HashSet<UUID>();
    long cooldownEndsAt;
    long nextMobSpawnsAt;
    int totalMobsSpawned;
    Optional<SpawnData> nextSpawnData = Optional.empty();
    Optional<ResourceKey<LootTable>> ejectingLootTable = Optional.empty();
    private @Nullable Entity displayEntity;
    private @Nullable WeightedList<ItemStack> dispensing;
    double spin;
    double oSpin;

    public Packed pack() {
        return new Packed(Set.copyOf(this.detectedPlayers), Set.copyOf(this.currentMobs), this.cooldownEndsAt, this.nextMobSpawnsAt, this.totalMobsSpawned, this.nextSpawnData, this.ejectingLootTable);
    }

    public void apply(Packed packed) {
        this.detectedPlayers.clear();
        this.detectedPlayers.addAll(packed.detectedPlayers);
        this.currentMobs.clear();
        this.currentMobs.addAll(packed.currentMobs);
        this.cooldownEndsAt = packed.cooldownEndsAt;
        this.nextMobSpawnsAt = packed.nextMobSpawnsAt;
        this.totalMobsSpawned = packed.totalMobsSpawned;
        this.nextSpawnData = packed.nextSpawnData;
        this.ejectingLootTable = packed.ejectingLootTable;
    }

    public void reset() {
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
        this.resetStatistics();
    }

    public void resetStatistics() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
    }

    public boolean hasMobToSpawn(TrialSpawner trialSpawner, RandomSource randomSource) {
        boolean bl = this.getOrCreateNextSpawnData(trialSpawner, randomSource).getEntityToSpawn().getString("id").isPresent();
        return bl || !trialSpawner.activeConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig trialSpawnerConfig, int i) {
        return this.totalMobsSpawned >= trialSpawnerConfig.calculateTargetTotalMobs(i);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, int i) {
        return serverLevel.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < trialSpawnerConfig.calculateTargetSimultaneousMobs(i);
    }

    public int countAdditionalPlayers(BlockPos blockPos) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Trial Spawner at " + String.valueOf(blockPos) + " has no detected players");
        }
        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel serverLevel, BlockPos blockPos, TrialSpawner trialSpawner) {
        List<UUID> list2;
        boolean bl2;
        boolean bl;
        boolean bl3 = bl = (blockPos.asLong() + serverLevel.getGameTime()) % 20L != 0L;
        if (bl) {
            return;
        }
        if (trialSpawner.getState().equals(TrialSpawnerState.COOLDOWN) && trialSpawner.isOminous()) {
            return;
        }
        List<UUID> list = trialSpawner.getPlayerDetector().detect(serverLevel, trialSpawner.getEntitySelector(), blockPos, trialSpawner.getRequiredPlayerRange(), true);
        if (trialSpawner.isOminous() || list.isEmpty()) {
            bl2 = false;
        } else {
            Optional<Pair<Player, Holder<MobEffect>>> optional = TrialSpawnerStateData.findPlayerWithOminousEffect(serverLevel, list);
            optional.ifPresent(pair -> {
                Player player = (Player)pair.getFirst();
                if (pair.getSecond() == MobEffects.BAD_OMEN) {
                    TrialSpawnerStateData.transformBadOmenIntoTrialOmen(player);
                }
                serverLevel.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
                trialSpawner.applyOminous(serverLevel, blockPos);
            });
            bl2 = optional.isPresent();
        }
        if (trialSpawner.getState().equals(TrialSpawnerState.COOLDOWN) && !bl2) {
            return;
        }
        boolean bl32 = trialSpawner.getStateData().detectedPlayers.isEmpty();
        List<UUID> list3 = list2 = bl32 ? list : trialSpawner.getPlayerDetector().detect(serverLevel, trialSpawner.getEntitySelector(), blockPos, trialSpawner.getRequiredPlayerRange(), false);
        if (this.detectedPlayers.addAll(list2)) {
            this.nextMobSpawnsAt = Math.max(serverLevel.getGameTime() + 40L, this.nextMobSpawnsAt);
            if (!bl2) {
                int i = trialSpawner.isOminous() ? 3019 : 3013;
                serverLevel.levelEvent(i, blockPos, this.detectedPlayers.size());
            }
        }
    }

    private static Optional<Pair<Player, Holder<MobEffect>>> findPlayerWithOminousEffect(ServerLevel serverLevel, List<UUID> list) {
        Player player2 = null;
        for (UUID uUID : list) {
            Player player22 = serverLevel.getPlayerByUUID(uUID);
            if (player22 == null) continue;
            Holder<MobEffect> holder = MobEffects.TRIAL_OMEN;
            if (player22.hasEffect(holder)) {
                return Optional.of(Pair.of((Object)player22, holder));
            }
            if (!player22.hasEffect(MobEffects.BAD_OMEN)) continue;
            player2 = player22;
        }
        return Optional.ofNullable(player2).map(player -> Pair.of((Object)player, MobEffects.BAD_OMEN));
    }

    public void resetAfterBecomingOminous(TrialSpawner trialSpawner, ServerLevel serverLevel) {
        this.currentMobs.stream().map(serverLevel::getEntity).forEach(entity -> {
            if (entity == null) {
                return;
            }
            serverLevel.levelEvent(3012, entity.blockPosition(), TrialSpawner.FlameParticle.NORMAL.encode());
            if (entity instanceof Mob) {
                Mob mob = (Mob)entity;
                mob.dropPreservedEquipment(serverLevel);
            }
            entity.remove(Entity.RemovalReason.DISCARDED);
        });
        if (!trialSpawner.ominousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }
        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = serverLevel.getGameTime() + (long)trialSpawner.ominousConfig().ticksBetweenSpawn();
        trialSpawner.markUpdated();
        this.cooldownEndsAt = serverLevel.getGameTime() + trialSpawner.ominousConfig().ticksBetweenItemSpawners();
    }

    private static void transformBadOmenIntoTrialOmen(Player player) {
        MobEffectInstance mobEffectInstance = player.getEffect(MobEffects.BAD_OMEN);
        if (mobEffectInstance == null) {
            return;
        }
        int i = mobEffectInstance.getAmplifier() + 1;
        int j = 18000 * i;
        player.removeEffect(MobEffects.BAD_OMEN);
        player.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, j, 0));
    }

    public boolean isReadyToOpenShutter(ServerLevel serverLevel, float f, int i) {
        long l = this.cooldownEndsAt - (long)i;
        return (float)serverLevel.getGameTime() >= (float)l + f;
    }

    public boolean isReadyToEjectItems(ServerLevel serverLevel, float f, int i) {
        long l = this.cooldownEndsAt - (long)i;
        return (float)(serverLevel.getGameTime() - l) % f == 0.0f;
    }

    public boolean isCooldownFinished(ServerLevel serverLevel) {
        return serverLevel.getGameTime() >= this.cooldownEndsAt;
    }

    protected SpawnData getOrCreateNextSpawnData(TrialSpawner trialSpawner, RandomSource randomSource) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        }
        WeightedList<SpawnData> weightedList = trialSpawner.activeConfig().spawnPotentialsDefinition();
        Optional<SpawnData> optional = weightedList.isEmpty() ? this.nextSpawnData : weightedList.getRandom(randomSource);
        this.nextSpawnData = Optional.of(optional.orElseGet(SpawnData::new));
        trialSpawner.markUpdated();
        return this.nextSpawnData.get();
    }

    public @Nullable Entity getOrCreateDisplayEntity(TrialSpawner trialSpawner, Level level, TrialSpawnerState trialSpawnerState) {
        CompoundTag compoundTag;
        if (!trialSpawnerState.hasSpinningMob()) {
            return null;
        }
        if (this.displayEntity == null && (compoundTag = this.getOrCreateNextSpawnData(trialSpawner, level.getRandom()).getEntityToSpawn()).getString("id").isPresent()) {
            this.displayEntity = EntityType.loadEntityRecursive(compoundTag, level, EntitySpawnReason.TRIAL_SPAWNER, EntityProcessor.NOP);
        }
        return this.displayEntity;
    }

    public CompoundTag getUpdateTag(TrialSpawnerState trialSpawnerState) {
        CompoundTag compoundTag = new CompoundTag();
        if (trialSpawnerState == TrialSpawnerState.ACTIVE) {
            compoundTag.putLong(TAG_NEXT_MOB_SPAWNS_AT, this.nextMobSpawnsAt);
        }
        this.nextSpawnData.ifPresent(spawnData -> compoundTag.store(TAG_SPAWN_DATA, SpawnData.CODEC, spawnData));
        return compoundTag;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }

    WeightedList<ItemStack> getDispensingItems(ServerLevel serverLevel, TrialSpawnerConfig trialSpawnerConfig, BlockPos blockPos) {
        long l;
        LootParams lootParams;
        if (this.dispensing != null) {
            return this.dispensing;
        }
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(trialSpawnerConfig.itemsToDropWhenOminous());
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY), l = TrialSpawnerStateData.lowResolutionPosition(serverLevel, blockPos));
        if (objectArrayList.isEmpty()) {
            return WeightedList.of();
        }
        WeightedList.Builder<ItemStack> builder = WeightedList.builder();
        for (ItemStack itemStack : objectArrayList) {
            builder.add(itemStack.copyWithCount(1), itemStack.getCount());
        }
        this.dispensing = builder.build();
        return this.dispensing;
    }

    private static long lowResolutionPosition(ServerLevel serverLevel, BlockPos blockPos) {
        BlockPos blockPos2 = new BlockPos(Mth.floor((float)blockPos.getX() / 30.0f), Mth.floor((float)blockPos.getY() / 20.0f), Mth.floor((float)blockPos.getZ() / 30.0f));
        return serverLevel.getSeed() + blockPos2.asLong();
    }

    public static final class Packed
    extends Record {
        final Set<UUID> detectedPlayers;
        final Set<UUID> currentMobs;
        final long cooldownEndsAt;
        final long nextMobSpawnsAt;
        final int totalMobsSpawned;
        final Optional<SpawnData> nextSpawnData;
        final Optional<ResourceKey<LootTable>> ejectingLootTable;
        public static final MapCodec<Packed> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)UUIDUtil.CODEC_SET.lenientOptionalFieldOf("registered_players", (Object)Set.of()).forGetter(Packed::detectedPlayers), (App)UUIDUtil.CODEC_SET.lenientOptionalFieldOf("current_mobs", (Object)Set.of()).forGetter(Packed::currentMobs), (App)Codec.LONG.lenientOptionalFieldOf("cooldown_ends_at", (Object)0L).forGetter(Packed::cooldownEndsAt), (App)Codec.LONG.lenientOptionalFieldOf(TrialSpawnerStateData.TAG_NEXT_MOB_SPAWNS_AT, (Object)0L).forGetter(Packed::nextMobSpawnsAt), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).lenientOptionalFieldOf("total_mobs_spawned", (Object)0).forGetter(Packed::totalMobsSpawned), (App)SpawnData.CODEC.lenientOptionalFieldOf(TrialSpawnerStateData.TAG_SPAWN_DATA).forGetter(Packed::nextSpawnData), (App)LootTable.KEY_CODEC.lenientOptionalFieldOf("ejecting_loot_table").forGetter(Packed::ejectingLootTable)).apply((Applicative)instance, Packed::new));

        public Packed(Set<UUID> set, Set<UUID> set2, long l, long m, int i, Optional<SpawnData> optional, Optional<ResourceKey<LootTable>> optional2) {
            this.detectedPlayers = set;
            this.currentMobs = set2;
            this.cooldownEndsAt = l;
            this.nextMobSpawnsAt = m;
            this.totalMobsSpawned = i;
            this.nextSpawnData = optional;
            this.ejectingLootTable = optional2;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "detectedPlayers;currentMobs;cooldownEndsAt;nextMobSpawnsAt;totalMobsSpawned;nextSpawnData;ejectingLootTable", "detectedPlayers", "currentMobs", "cooldownEndsAt", "nextMobSpawnsAt", "totalMobsSpawned", "nextSpawnData", "ejectingLootTable"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "detectedPlayers;currentMobs;cooldownEndsAt;nextMobSpawnsAt;totalMobsSpawned;nextSpawnData;ejectingLootTable", "detectedPlayers", "currentMobs", "cooldownEndsAt", "nextMobSpawnsAt", "totalMobsSpawned", "nextSpawnData", "ejectingLootTable"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "detectedPlayers;currentMobs;cooldownEndsAt;nextMobSpawnsAt;totalMobsSpawned;nextSpawnData;ejectingLootTable", "detectedPlayers", "currentMobs", "cooldownEndsAt", "nextMobSpawnsAt", "totalMobsSpawned", "nextSpawnData", "ejectingLootTable"}, this, object);
        }

        public Set<UUID> detectedPlayers() {
            return this.detectedPlayers;
        }

        public Set<UUID> currentMobs() {
            return this.currentMobs;
        }

        public long cooldownEndsAt() {
            return this.cooldownEndsAt;
        }

        public long nextMobSpawnsAt() {
            return this.nextMobSpawnsAt;
        }

        public int totalMobsSpawned() {
            return this.totalMobsSpawned;
        }

        public Optional<SpawnData> nextSpawnData() {
            return this.nextSpawnData;
        }

        public Optional<ResourceKey<LootTable>> ejectingLootTable() {
            return this.ejectingLootTable;
        }
    }
}

