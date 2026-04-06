/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class BaseSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String SPAWN_DATA_TAG = "SpawnData";
    private static final int EVENT_SPAWN = 1;
    private static final int DEFAULT_SPAWN_DELAY = 20;
    private static final int DEFAULT_MIN_SPAWN_DELAY = 200;
    private static final int DEFAULT_MAX_SPAWN_DELAY = 800;
    private static final int DEFAULT_SPAWN_COUNT = 4;
    private static final int DEFAULT_MAX_NEARBY_ENTITIES = 6;
    private static final int DEFAULT_REQUIRED_PLAYER_RANGE = 16;
    private static final int DEFAULT_SPAWN_RANGE = 4;
    private int spawnDelay = 20;
    private WeightedList<SpawnData> spawnPotentials = WeightedList.of();
    private @Nullable SpawnData nextSpawnData;
    private double spin;
    private double oSpin;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    private @Nullable Entity displayEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public void setEntityId(EntityType<?> entityType, @Nullable Level level, RandomSource randomSource, BlockPos blockPos) {
        this.getOrCreateNextSpawnData(level, randomSource, blockPos).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    private boolean isNearPlayer(Level level, BlockPos blockPos) {
        return level.hasNearbyAlivePlayer((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(Level level, BlockPos blockPos) {
        if (!this.isNearPlayer(level, blockPos)) {
            this.oSpin = this.spin;
        } else if (this.displayEntity != null) {
            RandomSource randomSource = level.getRandom();
            double d = (double)blockPos.getX() + randomSource.nextDouble();
            double e = (double)blockPos.getY() + randomSource.nextDouble();
            double f = (double)blockPos.getZ() + randomSource.nextDouble();
            level.addParticle(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            level.addParticle(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.oSpin = this.spin;
            this.spin = (this.spin + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerLevel serverLevel, BlockPos blockPos) {
        if (!this.isNearPlayer(serverLevel, blockPos) || !serverLevel.isSpawnerBlockEnabled()) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.delay(serverLevel, blockPos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }
        boolean bl = false;
        RandomSource randomSource = serverLevel.getRandom();
        SpawnData spawnData = this.getOrCreateNextSpawnData(serverLevel, randomSource, blockPos);
        for (int i = 0; i < this.spawnCount; ++i) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(this::toString, LOGGER);){
                ValueInput valueInput = TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)serverLevel.registryAccess(), spawnData.getEntityToSpawn());
                Optional<EntityType<?>> optional = EntityType.by(valueInput);
                if (optional.isEmpty()) {
                    this.delay(serverLevel, blockPos);
                    return;
                }
                Vec3 vec3 = valueInput.read("Pos", Vec3.CODEC).orElseGet(() -> new Vec3((double)blockPos.getX() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5, blockPos.getY() + randomSource.nextInt(3) - 1, (double)blockPos.getZ() + (randomSource.nextDouble() - randomSource.nextDouble()) * (double)this.spawnRange + 0.5));
                if (!serverLevel.noCollision(optional.get().getSpawnAABB(vec3.x, vec3.y, vec3.z))) continue;
                BlockPos blockPos2 = BlockPos.containing(vec3);
                if (spawnData.getCustomSpawnRules().isPresent()) {
                    SpawnData.CustomSpawnRules customSpawnRules;
                    if (!optional.get().getCategory().isFriendly() && serverLevel.getDifficulty() == Difficulty.PEACEFUL || !(customSpawnRules = spawnData.getCustomSpawnRules().get()).isValidPosition(blockPos2, serverLevel)) continue;
                } else if (!SpawnPlacements.checkSpawnRules(optional.get(), serverLevel, EntitySpawnReason.SPAWNER, blockPos2, serverLevel.getRandom())) continue;
                Entity entity2 = EntityType.loadEntityRecursive(valueInput, (Level)serverLevel, EntitySpawnReason.SPAWNER, entity -> {
                    entity.snapTo(vec3.x, vec3.y, vec3.z, entity.getYRot(), entity.getXRot());
                    return entity;
                });
                if (entity2 == null) {
                    this.delay(serverLevel, blockPos);
                    return;
                }
                int j = serverLevel.getEntities(EntityTypeTest.forExactClass(entity2.getClass()), new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1).inflate(this.spawnRange), EntitySelector.NO_SPECTATORS).size();
                if (j >= this.maxNearbyEntities) {
                    this.delay(serverLevel, blockPos);
                    return;
                }
                entity2.snapTo(entity2.getX(), entity2.getY(), entity2.getZ(), randomSource.nextFloat() * 360.0f, 0.0f);
                if (entity2 instanceof Mob) {
                    boolean bl2;
                    Mob mob = (Mob)entity2;
                    if (spawnData.getCustomSpawnRules().isEmpty() && !mob.checkSpawnRules(serverLevel, EntitySpawnReason.SPAWNER) || !mob.checkSpawnObstruction(serverLevel)) continue;
                    boolean bl3 = bl2 = spawnData.getEntityToSpawn().size() == 1 && spawnData.getEntityToSpawn().getString("id").isPresent();
                    if (bl2) {
                        ((Mob)entity2).finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(entity2.blockPosition()), EntitySpawnReason.SPAWNER, null);
                    }
                    spawnData.getEquipment().ifPresent(mob::equip);
                }
                if (!serverLevel.tryAddFreshEntityWithPassengers(entity2)) {
                    this.delay(serverLevel, blockPos);
                    return;
                }
                serverLevel.levelEvent(2004, blockPos, 0);
                serverLevel.gameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos2);
                if (entity2 instanceof Mob) {
                    ((Mob)entity2).spawnAnim();
                }
                bl = true;
                continue;
            }
        }
        if (bl) {
            this.delay(serverLevel, blockPos);
        }
    }

    private void delay(Level level, BlockPos blockPos) {
        RandomSource randomSource = level.random;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + randomSource.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getRandom(randomSource).ifPresent(spawnData -> this.setNextSpawnData(level, blockPos, (SpawnData)((Object)spawnData)));
        this.broadcastEvent(level, blockPos, 1);
    }

    public void load(@Nullable Level level, BlockPos blockPos, ValueInput valueInput) {
        this.spawnDelay = valueInput.getShortOr("Delay", (short)20);
        valueInput.read(SPAWN_DATA_TAG, SpawnData.CODEC).ifPresent(spawnData -> this.setNextSpawnData(level, blockPos, (SpawnData)((Object)spawnData)));
        this.spawnPotentials = valueInput.read("SpawnPotentials", SpawnData.LIST_CODEC).orElseGet(() -> WeightedList.of(this.nextSpawnData != null ? this.nextSpawnData : new SpawnData()));
        this.minSpawnDelay = valueInput.getIntOr("MinSpawnDelay", 200);
        this.maxSpawnDelay = valueInput.getIntOr("MaxSpawnDelay", 800);
        this.spawnCount = valueInput.getIntOr("SpawnCount", 4);
        this.maxNearbyEntities = valueInput.getIntOr("MaxNearbyEntities", 6);
        this.requiredPlayerRange = valueInput.getIntOr("RequiredPlayerRange", 16);
        this.spawnRange = valueInput.getIntOr("SpawnRange", 4);
        this.displayEntity = null;
    }

    public void save(ValueOutput valueOutput) {
        valueOutput.putShort("Delay", (short)this.spawnDelay);
        valueOutput.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        valueOutput.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        valueOutput.putShort("SpawnCount", (short)this.spawnCount);
        valueOutput.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        valueOutput.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        valueOutput.putShort("SpawnRange", (short)this.spawnRange);
        valueOutput.storeNullable(SPAWN_DATA_TAG, SpawnData.CODEC, this.nextSpawnData);
        valueOutput.store("SpawnPotentials", SpawnData.LIST_CODEC, this.spawnPotentials);
    }

    public @Nullable Entity getOrCreateDisplayEntity(Level level, BlockPos blockPos) {
        if (this.displayEntity == null) {
            CompoundTag compoundTag = this.getOrCreateNextSpawnData(level, level.getRandom(), blockPos).getEntityToSpawn();
            if (compoundTag.getString("id").isEmpty()) {
                return null;
            }
            this.displayEntity = EntityType.loadEntityRecursive(compoundTag, level, EntitySpawnReason.SPAWNER, EntityProcessor.NOP);
            if (compoundTag.size() != 1 || this.displayEntity instanceof Mob) {
                // empty if block
            }
        }
        return this.displayEntity;
    }

    public boolean onEventTriggered(Level level, int i) {
        if (i == 1) {
            if (level.isClientSide()) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    protected void setNextSpawnData(@Nullable Level level, BlockPos blockPos, SpawnData spawnData) {
        this.nextSpawnData = spawnData;
    }

    private SpawnData getOrCreateNextSpawnData(@Nullable Level level, RandomSource randomSource, BlockPos blockPos) {
        if (this.nextSpawnData != null) {
            return this.nextSpawnData;
        }
        this.setNextSpawnData(level, blockPos, this.spawnPotentials.getRandom(randomSource).orElseGet(SpawnData::new));
        return this.nextSpawnData;
    }

    public abstract void broadcastEvent(Level var1, BlockPos var2, int var3);

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }
}

