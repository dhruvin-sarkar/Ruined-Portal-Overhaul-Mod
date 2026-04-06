/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity.trialspawner;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.TrialSpawnerBlock;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerConfig;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.slf4j.Logger;

public final class TrialSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02f;
    private final TrialSpawnerStateData data = new TrialSpawnerStateData();
    private FullConfig config;
    private final StateAccessor stateAccessor;
    private PlayerDetector playerDetector;
    private final PlayerDetector.EntitySelector entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;

    public TrialSpawner(FullConfig fullConfig, StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this.config = fullConfig;
        this.stateAccessor = stateAccessor;
        this.playerDetector = playerDetector;
        this.entitySelector = entitySelector;
    }

    public TrialSpawnerConfig activeConfig() {
        return this.isOminous ? this.config.ominous().value() : this.config.normal.value();
    }

    public TrialSpawnerConfig normalConfig() {
        return this.config.normal.value();
    }

    public TrialSpawnerConfig ominousConfig() {
        return this.config.ominous.value();
    }

    public void load(ValueInput valueInput) {
        valueInput.read(TrialSpawnerStateData.Packed.MAP_CODEC).ifPresent(this.data::apply);
        this.config = valueInput.read(FullConfig.MAP_CODEC).orElse(FullConfig.DEFAULT);
    }

    public void store(ValueOutput valueOutput) {
        valueOutput.store(TrialSpawnerStateData.Packed.MAP_CODEC, this.data.pack());
        valueOutput.store(FullConfig.MAP_CODEC, this.config);
    }

    public void applyOminous(ServerLevel serverLevel, BlockPos blockPos) {
        serverLevel.setBlock(blockPos, (BlockState)serverLevel.getBlockState(blockPos).setValue(TrialSpawnerBlock.OMINOUS, true), 3);
        serverLevel.levelEvent(3020, blockPos, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, serverLevel);
    }

    public void removeOminous(ServerLevel serverLevel, BlockPos blockPos) {
        serverLevel.setBlock(blockPos, (BlockState)serverLevel.getBlockState(blockPos).setValue(TrialSpawnerBlock.OMINOUS, false), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public int getTargetCooldownLength() {
        return this.config.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.config.requiredPlayerRange;
    }

    public TrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public TrialSpawnerStateData getStateData() {
        return this.data;
    }

    public void setState(Level level, TrialSpawnerState trialSpawnerState) {
        this.stateAccessor.setState(level, trialSpawnerState);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.EntitySelector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(ServerLevel serverLevel) {
        if (!serverLevel.getGameRules().get(GameRules.SPAWNER_BLOCKS_WORK).booleanValue()) {
            return false;
        }
        if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        }
        if (serverLevel.getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return serverLevel.getGameRules().get(GameRules.SPAWN_MOBS);
    }

    public Optional<UUID> spawnMob(ServerLevel serverLevel, BlockPos blockPos) {
        RandomSource randomSource = serverLevel.getRandom();
        SpawnData spawnData = this.data.getOrCreateNextSpawnData(this, serverLevel.getRandom());
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(() -> "spawner@" + String.valueOf(blockPos), LOGGER);){
            Object mob;
            SpawnData.CustomSpawnRules customSpawnRules;
            ValueInput valueInput = TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)serverLevel.registryAccess(), spawnData.entityToSpawn());
            Optional<EntityType<?>> optional = EntityType.by(valueInput);
            if (optional.isEmpty()) {
                Optional<UUID> optional2 = Optional.empty();
                return optional2;
            }
            Vec3 vec3 = valueInput.read("Pos", Vec3.CODEC).orElseGet(() -> {
                TrialSpawnerConfig trialSpawnerConfig = this.activeConfig();
                return new Vec3((double)blockPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)trialSpawnerConfig.spawnRange() + 0.5, blockPos.getY() + randomSource.nextInt(3) - 1, (double)blockPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)trialSpawnerConfig.spawnRange() + 0.5);
            });
            if (!serverLevel.noCollision(optional.get().getSpawnAABB(vec3.x, vec3.y, vec3.z))) {
                Optional<UUID> optional3 = Optional.empty();
                return optional3;
            }
            if (!TrialSpawner.inLineOfSight(serverLevel, blockPos.getCenter(), vec3)) {
                Optional<UUID> optional4 = Optional.empty();
                return optional4;
            }
            BlockPos blockPos2 = BlockPos.containing(vec3);
            if (!SpawnPlacements.checkSpawnRules(optional.get(), serverLevel, EntitySpawnReason.TRIAL_SPAWNER, blockPos2, serverLevel.getRandom())) {
                Optional<UUID> optional5 = Optional.empty();
                return optional5;
            }
            if (spawnData.getCustomSpawnRules().isPresent() && !(customSpawnRules = spawnData.getCustomSpawnRules().get()).isValidPosition(blockPos2, serverLevel)) {
                Optional<UUID> optional6 = Optional.empty();
                return optional6;
            }
            Entity entity2 = EntityType.loadEntityRecursive(valueInput, (Level)serverLevel, EntitySpawnReason.TRIAL_SPAWNER, entity -> {
                entity.snapTo(vec3.x, vec3.y, vec3.z, randomSource.nextFloat() * 360.0f, 0.0f);
                return entity;
            });
            if (entity2 == null) {
                Optional<UUID> optional7 = Optional.empty();
                return optional7;
            }
            if (entity2 instanceof Mob) {
                boolean bl;
                mob = (Mob)entity2;
                if (!((Mob)mob).checkSpawnObstruction(serverLevel)) {
                    Optional<UUID> optional8 = Optional.empty();
                    return optional8;
                }
                boolean bl2 = bl = spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().getString("id").isPresent();
                if (bl) {
                    ((Mob)mob).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(((Entity)mob).blockPosition()), EntitySpawnReason.TRIAL_SPAWNER, null);
                }
                ((Mob)mob).setPersistenceRequired();
                spawnData.getEquipment().ifPresent(((Mob)mob)::equip);
            }
            if (!serverLevel.tryAddFreshEntityWithPassengers(entity2)) {
                mob = Optional.empty();
                return mob;
            }
            FlameParticle flameParticle = this.isOminous ? FlameParticle.OMINOUS : FlameParticle.NORMAL;
            serverLevel.levelEvent(3011, blockPos, flameParticle.encode());
            serverLevel.levelEvent(3012, blockPos2, flameParticle.encode());
            serverLevel.gameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos2);
            Optional<UUID> optional9 = Optional.of(entity2.getUUID());
            return optional9;
        }
    }

    public void ejectReward(ServerLevel serverLevel, BlockPos blockPos, ResourceKey<LootTable> resourceKey) {
        LootParams lootParams;
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
        ObjectArrayList<ItemStack> objectArrayList = lootTable.getRandomItems(lootParams = new LootParams.Builder(serverLevel).create(LootContextParamSets.EMPTY));
        if (!objectArrayList.isEmpty()) {
            for (ItemStack itemStack : objectArrayList) {
                DefaultDispenseItemBehavior.spawnItem(serverLevel, itemStack, 2, Direction.UP, Vec3.atBottomCenterOf(blockPos).relative(Direction.UP, 1.2));
            }
            serverLevel.levelEvent(3014, blockPos, 0);
        }
    }

    public void tickClient(Level level, BlockPos blockPos, boolean bl) {
        RandomSource randomSource;
        TrialSpawnerState trialSpawnerState = this.getState();
        trialSpawnerState.emitParticles(level, blockPos, bl);
        if (trialSpawnerState.hasSpinningMob()) {
            double d = Math.max(0L, this.data.nextMobSpawnsAt - level.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + trialSpawnerState.spinningMobSpeed() / (d + 200.0)) % 360.0;
        }
        if (trialSpawnerState.isCapableOfSpawning() && (randomSource = level.getRandom()).nextFloat() <= 0.02f) {
            SoundEvent soundEvent = bl ? SoundEvents.TRIAL_SPAWNER_AMBIENT_OMINOUS : SoundEvents.TRIAL_SPAWNER_AMBIENT;
            level.playLocalSound(blockPos, soundEvent, SoundSource.BLOCKS, randomSource.nextFloat() * 0.25f + 0.75f, randomSource.nextFloat() + 0.5f, false);
        }
    }

    public void tickServer(ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        TrialSpawnerState trialSpawnerState2;
        this.isOminous = bl;
        TrialSpawnerState trialSpawnerState = this.getState();
        if (this.data.currentMobs.removeIf(uUID -> TrialSpawner.shouldMobBeUntracked(serverLevel, blockPos, uUID))) {
            this.data.nextMobSpawnsAt = serverLevel.getGameTime() + (long)this.activeConfig().ticksBetweenSpawn();
        }
        if ((trialSpawnerState2 = trialSpawnerState.tickAndGetNext(blockPos, this, serverLevel)) != trialSpawnerState) {
            this.setState(serverLevel, trialSpawnerState2);
        }
    }

    private static boolean shouldMobBeUntracked(ServerLevel serverLevel, BlockPos blockPos, UUID uUID) {
        Entity entity = serverLevel.getEntity(uUID);
        return entity == null || !entity.isAlive() || !entity.level().dimension().equals(serverLevel.dimension()) || entity.blockPosition().distSqr(blockPos) > (double)MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level level, Vec3 vec3, Vec3 vec32) {
        BlockHitResult blockHitResult = level.clip(new ClipContext(vec32, vec3, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return blockHitResult.getBlockPos().equals(BlockPos.containing(vec3)) || blockHitResult.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level level, BlockPos blockPos, RandomSource randomSource, SimpleParticleType simpleParticleType) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double e = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double f = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            level.addParticle(simpleParticleType, d, e, f, 0.0, 0.0, 0.0);
        }
    }

    public static void addBecomeOminousParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double e = (double)blockPos.getY() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double f = (double)blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 2.0;
            double g = randomSource.nextGaussian() * 0.02;
            double h = randomSource.nextGaussian() * 0.02;
            double j = randomSource.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.TRIAL_OMEN, d, e, f, g, h, j);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d, e, f, g, h, j);
        }
    }

    public static void addDetectPlayerParticles(Level level, BlockPos blockPos, RandomSource randomSource, int i, ParticleOptions particleOptions) {
        for (int j = 0; j < 30 + Math.min(i, 10) * 5; ++j) {
            double d = (double)(2.0f * randomSource.nextFloat() - 1.0f) * 0.65;
            double e = (double)(2.0f * randomSource.nextFloat() - 1.0f) * 0.65;
            double f = (double)blockPos.getX() + 0.5 + d;
            double g = (double)blockPos.getY() + 0.1 + (double)randomSource.nextFloat() * 0.8;
            double h = (double)blockPos.getZ() + 0.5 + e;
            level.addParticle(particleOptions, f, g, h, 0.0, 0.0, 0.0);
        }
    }

    public static void addEjectItemParticles(Level level, BlockPos blockPos, RandomSource randomSource) {
        for (int i = 0; i < 20; ++i) {
            double d = (double)blockPos.getX() + 0.4 + randomSource.nextDouble() * 0.2;
            double e = (double)blockPos.getY() + 0.4 + randomSource.nextDouble() * 0.2;
            double f = (double)blockPos.getZ() + 0.4 + randomSource.nextDouble() * 0.2;
            double g = randomSource.nextGaussian() * 0.02;
            double h = randomSource.nextGaussian() * 0.02;
            double j = randomSource.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.SMALL_FLAME, d, e, f, g, h, j * 0.25);
            level.addParticle(ParticleTypes.SMOKE, d, e, f, g, h, j);
        }
    }

    public void overrideEntityToSpawn(EntityType<?> entityType, Level level) {
        this.data.reset();
        this.config = this.config.overrideEntity(entityType);
        this.setState(level, TrialSpawnerState.INACTIVE);
    }

    @Deprecated(forRemoval=true)
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector playerDetector) {
        this.playerDetector = playerDetector;
    }

    @Deprecated(forRemoval=true)
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public static final class FullConfig
    extends Record {
        final Holder<TrialSpawnerConfig> normal;
        final Holder<TrialSpawnerConfig> ominous;
        final int targetCooldownLength;
        final int requiredPlayerRange;
        public static final MapCodec<FullConfig> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TrialSpawnerConfig.CODEC.optionalFieldOf("normal_config", Holder.direct(TrialSpawnerConfig.DEFAULT)).forGetter(FullConfig::normal), (App)TrialSpawnerConfig.CODEC.optionalFieldOf("ominous_config", Holder.direct(TrialSpawnerConfig.DEFAULT)).forGetter(FullConfig::ominous), (App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("target_cooldown_length", (Object)36000).forGetter(FullConfig::targetCooldownLength), (App)Codec.intRange((int)1, (int)128).optionalFieldOf("required_player_range", (Object)14).forGetter(FullConfig::requiredPlayerRange)).apply((Applicative)instance, FullConfig::new));
        public static final FullConfig DEFAULT = new FullConfig(Holder.direct(TrialSpawnerConfig.DEFAULT), Holder.direct(TrialSpawnerConfig.DEFAULT), 36000, 14);

        public FullConfig(Holder<TrialSpawnerConfig> holder, Holder<TrialSpawnerConfig> holder2, int i, int j) {
            this.normal = holder;
            this.ominous = holder2;
            this.targetCooldownLength = i;
            this.requiredPlayerRange = j;
        }

        public FullConfig overrideEntity(EntityType<?> entityType) {
            return new FullConfig(Holder.direct(this.normal.value().withSpawning(entityType)), Holder.direct(this.ominous.value().withSpawning(entityType)), this.targetCooldownLength, this.requiredPlayerRange);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FullConfig.class, "normal;ominous;targetCooldownLength;requiredPlayerRange", "normal", "ominous", "targetCooldownLength", "requiredPlayerRange"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FullConfig.class, "normal;ominous;targetCooldownLength;requiredPlayerRange", "normal", "ominous", "targetCooldownLength", "requiredPlayerRange"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FullConfig.class, "normal;ominous;targetCooldownLength;requiredPlayerRange", "normal", "ominous", "targetCooldownLength", "requiredPlayerRange"}, this, object);
        }

        public Holder<TrialSpawnerConfig> normal() {
            return this.normal;
        }

        public Holder<TrialSpawnerConfig> ominous() {
            return this.ominous;
        }

        public int targetCooldownLength() {
            return this.targetCooldownLength;
        }

        public int requiredPlayerRange() {
            return this.requiredPlayerRange;
        }
    }

    public static interface StateAccessor {
        public void setState(Level var1, TrialSpawnerState var2);

        public TrialSpawnerState getState();

        public void markUpdated();
    }

    public static enum FlameParticle {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particleType;

        private FlameParticle(SimpleParticleType simpleParticleType) {
            this.particleType = simpleParticleType;
        }

        public static FlameParticle decode(int i) {
            FlameParticle[] flameParticles = FlameParticle.values();
            if (i > flameParticles.length || i < 0) {
                return NORMAL;
            }
            return flameParticles[i];
        }

        public int encode() {
            return this.ordinal();
        }
    }
}

