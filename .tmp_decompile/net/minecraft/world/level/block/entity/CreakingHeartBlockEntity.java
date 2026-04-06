/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  org.apache.commons.lang3.mutable.Mutable
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SpawnUtil;
import net.minecraft.util.Util;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class CreakingHeartBlockEntity
extends BlockEntity {
    private static final int PLAYER_DETECTION_RANGE = 32;
    public static final int CREAKING_ROAMING_RADIUS = 32;
    private static final int DISTANCE_CREAKING_TOO_FAR = 34;
    private static final int SPAWN_RANGE_XZ = 16;
    private static final int SPAWN_RANGE_Y = 8;
    private static final int ATTEMPTS_PER_SPAWN = 5;
    private static final int UPDATE_TICKS = 20;
    private static final int UPDATE_TICKS_VARIANCE = 5;
    private static final int HURT_CALL_TOTAL_TICKS = 100;
    private static final int NUMBER_OF_HURT_CALLS = 10;
    private static final int HURT_CALL_INTERVAL = 10;
    private static final int HURT_CALL_PARTICLE_TICKS = 50;
    private static final int MAX_DEPTH = 2;
    private static final int MAX_COUNT = 64;
    private static final int TICKS_GRACE_PERIOD = 30;
    private static final Optional<Creaking> NO_CREAKING = Optional.empty();
    private @Nullable Either<Creaking, UUID> creakingInfo;
    private long ticksExisted;
    private int ticker;
    private int emitter;
    private @Nullable Vec3 emitterTarget;
    private int outputSignal;

    public CreakingHeartBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.CREAKING_HEART, blockPos, blockState);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, CreakingHeartBlockEntity creakingHeartBlockEntity) {
        Creaking creaking2;
        ++creakingHeartBlockEntity.ticksExisted;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        int i = creakingHeartBlockEntity.computeAnalogOutputSignal();
        if (creakingHeartBlockEntity.outputSignal != i) {
            creakingHeartBlockEntity.outputSignal = i;
            level.updateNeighbourForOutputSignal(blockPos, Blocks.CREAKING_HEART);
        }
        if (creakingHeartBlockEntity.emitter > 0) {
            if (creakingHeartBlockEntity.emitter > 50) {
                creakingHeartBlockEntity.emitParticles(serverLevel, 1, true);
                creakingHeartBlockEntity.emitParticles(serverLevel, 1, false);
            }
            if (creakingHeartBlockEntity.emitter % 10 == 0 && creakingHeartBlockEntity.emitterTarget != null) {
                creakingHeartBlockEntity.getCreakingProtector().ifPresent(creaking -> {
                    creakingHeartBlockEntity.emitterTarget = creaking.getBoundingBox().getCenter();
                });
                Vec3 vec3 = Vec3.atCenterOf(blockPos);
                float f = 0.2f + 0.8f * (float)(100 - creakingHeartBlockEntity.emitter) / 100.0f;
                Vec3 vec32 = vec3.subtract(creakingHeartBlockEntity.emitterTarget).scale(f).add(creakingHeartBlockEntity.emitterTarget);
                BlockPos blockPos2 = BlockPos.containing(vec32);
                float g = (float)creakingHeartBlockEntity.emitter / 2.0f / 100.0f + 0.5f;
                serverLevel.playSound(null, blockPos2, SoundEvents.CREAKING_HEART_HURT, SoundSource.BLOCKS, g, 1.0f);
            }
            --creakingHeartBlockEntity.emitter;
        }
        if (creakingHeartBlockEntity.ticker-- >= 0) {
            return;
        }
        creakingHeartBlockEntity.ticker = creakingHeartBlockEntity.level == null ? 20 : creakingHeartBlockEntity.level.random.nextInt(5) + 20;
        BlockState blockState2 = CreakingHeartBlockEntity.updateCreakingState(level, blockState, blockPos, creakingHeartBlockEntity);
        if (blockState2 != blockState) {
            level.setBlock(blockPos, blockState2, 3);
            if (blockState2.getValue(CreakingHeartBlock.STATE) == CreakingHeartState.UPROOTED) {
                return;
            }
        }
        if (creakingHeartBlockEntity.creakingInfo != null) {
            Optional<Creaking> optional = creakingHeartBlockEntity.getCreakingProtector();
            if (optional.isPresent()) {
                creaking2 = optional.get();
                if (level.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, blockPos) == false && !creaking2.isPersistenceRequired() || creakingHeartBlockEntity.distanceToCreaking() > 34.0 || creaking2.playerIsStuckInYou()) {
                    creakingHeartBlockEntity.removeProtector(null);
                }
            }
            return;
        }
        if (blockState2.getValue(CreakingHeartBlock.STATE) != CreakingHeartState.AWAKE) {
            return;
        }
        if (!serverLevel.isSpawningMonsters()) {
            return;
        }
        Player player = level.getNearestPlayer((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ(), 32.0, false);
        if (player != null && (creaking2 = CreakingHeartBlockEntity.spawnProtector(serverLevel, creakingHeartBlockEntity)) != null) {
            creakingHeartBlockEntity.setCreakingInfo(creaking2);
            creaking2.makeSound(SoundEvents.CREAKING_SPAWN);
            level.playSound(null, creakingHeartBlockEntity.getBlockPos(), SoundEvents.CREAKING_HEART_SPAWN, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    private static BlockState updateCreakingState(Level level, BlockState blockState, BlockPos blockPos, CreakingHeartBlockEntity creakingHeartBlockEntity) {
        if (!CreakingHeartBlock.hasRequiredLogs(blockState, level, blockPos) && creakingHeartBlockEntity.creakingInfo == null) {
            return (BlockState)blockState.setValue(CreakingHeartBlock.STATE, CreakingHeartState.UPROOTED);
        }
        CreakingHeartState creakingHeartState = level.environmentAttributes().getValue(EnvironmentAttributes.CREAKING_ACTIVE, blockPos) != false ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT;
        return (BlockState)blockState.setValue(CreakingHeartBlock.STATE, creakingHeartState);
    }

    private double distanceToCreaking() {
        return this.getCreakingProtector().map(creaking -> Math.sqrt(creaking.distanceToSqr(Vec3.atBottomCenterOf(this.getBlockPos())))).orElse(0.0);
    }

    private void clearCreakingInfo() {
        this.creakingInfo = null;
        this.setChanged();
    }

    public void setCreakingInfo(Creaking creaking) {
        this.creakingInfo = Either.left((Object)creaking);
        this.setChanged();
    }

    public void setCreakingInfo(UUID uUID) {
        this.creakingInfo = Either.right((Object)uUID);
        this.ticksExisted = 0L;
        this.setChanged();
    }

    private Optional<Creaking> getCreakingProtector() {
        Level level;
        if (this.creakingInfo == null) {
            return NO_CREAKING;
        }
        if (this.creakingInfo.left().isPresent()) {
            Creaking creaking = (Creaking)this.creakingInfo.left().get();
            if (!creaking.isRemoved()) {
                return Optional.of(creaking);
            }
            this.setCreakingInfo(creaking.getUUID());
        }
        if ((level = this.level) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.creakingInfo.right().isPresent()) {
                UUID uUID = (UUID)this.creakingInfo.right().get();
                Entity entity = serverLevel.getEntity(uUID);
                if (entity instanceof Creaking) {
                    Creaking creaking2 = (Creaking)entity;
                    this.setCreakingInfo(creaking2);
                    return Optional.of(creaking2);
                }
                if (this.ticksExisted >= 30L) {
                    this.clearCreakingInfo();
                }
                return NO_CREAKING;
            }
        }
        return NO_CREAKING;
    }

    private static @Nullable Creaking spawnProtector(ServerLevel serverLevel, CreakingHeartBlockEntity creakingHeartBlockEntity) {
        BlockPos blockPos = creakingHeartBlockEntity.getBlockPos();
        Optional<Creaking> optional = SpawnUtil.trySpawnMob(EntityType.CREAKING, EntitySpawnReason.SPAWNER, serverLevel, blockPos, 5, 16, 8, SpawnUtil.Strategy.ON_TOP_OF_COLLIDER_NO_LEAVES, true);
        if (optional.isEmpty()) {
            return null;
        }
        Creaking creaking = optional.get();
        serverLevel.gameEvent((Entity)creaking, GameEvent.ENTITY_PLACE, creaking.position());
        serverLevel.broadcastEntityEvent(creaking, (byte)60);
        creaking.setTransient(blockPos);
        return creaking;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public void creakingHurt() {
        Object var2_1 = this.getCreakingProtector().orElse(null);
        if (!(var2_1 instanceof Creaking)) {
            return;
        }
        Creaking creaking = var2_1;
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        if (this.emitter > 0) {
            return;
        }
        this.emitParticles(serverLevel, 20, false);
        if (this.getBlockState().getValue(CreakingHeartBlock.STATE) == CreakingHeartState.AWAKE) {
            int i = this.level.getRandom().nextIntBetweenInclusive(2, 3);
            for (int j = 0; j < i; ++j) {
                this.spreadResin(serverLevel).ifPresent(blockPos -> {
                    this.level.playSound(null, (BlockPos)blockPos, SoundEvents.RESIN_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                    this.level.gameEvent((Holder<GameEvent>)GameEvent.BLOCK_PLACE, (BlockPos)blockPos, GameEvent.Context.of(this.getBlockState()));
                });
            }
        }
        this.emitter = 100;
        this.emitterTarget = creaking.getBoundingBox().getCenter();
    }

    private Optional<BlockPos> spreadResin(ServerLevel serverLevel) {
        MutableObject mutable = new MutableObject(null);
        BlockPos.breadthFirstTraversal(this.worldPosition, 2, 64, (blockPos, consumer) -> {
            for (Direction direction : Util.shuffledCopy(Direction.values(), serverLevel.random)) {
                BlockPos blockPos2 = blockPos.relative(direction);
                if (!serverLevel.getBlockState(blockPos2).is(BlockTags.PALE_OAK_LOGS)) continue;
                consumer.accept(blockPos2);
            }
        }, arg_0 -> CreakingHeartBlockEntity.method_65169(serverLevel, (Mutable)mutable, arg_0));
        return Optional.ofNullable((BlockPos)mutable.get());
    }

    private void emitParticles(ServerLevel serverLevel, int i, boolean bl) {
        Object var5_4 = this.getCreakingProtector().orElse(null);
        if (!(var5_4 instanceof Creaking)) {
            return;
        }
        Creaking creaking = var5_4;
        int j = bl ? 16545810 : 0x5F5F5F;
        RandomSource randomSource = serverLevel.random;
        for (double d = 0.0; d < (double)i; d += 1.0) {
            AABB aABB = creaking.getBoundingBox();
            Vec3 vec3 = aABB.getMinPosition().add(randomSource.nextDouble() * aABB.getXsize(), randomSource.nextDouble() * aABB.getYsize(), randomSource.nextDouble() * aABB.getZsize());
            Vec3 vec32 = Vec3.atLowerCornerOf(this.getBlockPos()).add(randomSource.nextDouble(), randomSource.nextDouble(), randomSource.nextDouble());
            if (bl) {
                Vec3 vec33 = vec3;
                vec3 = vec32;
                vec32 = vec33;
            }
            TrailParticleOption trailParticleOption = new TrailParticleOption(vec32, j, randomSource.nextInt(40) + 10);
            serverLevel.sendParticles(trailParticleOption, true, true, vec3.x, vec3.y, vec3.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
        this.removeProtector(null);
    }

    public void removeProtector(@Nullable DamageSource damageSource) {
        Object var3_2 = this.getCreakingProtector().orElse(null);
        if (var3_2 instanceof Creaking) {
            Creaking creaking = var3_2;
            if (damageSource == null) {
                creaking.tearDown();
            } else {
                creaking.creakingDeathEffects(damageSource);
                creaking.setTearingDown();
                creaking.setHealth(0.0f);
            }
            this.clearCreakingInfo();
        }
    }

    public boolean isProtector(Creaking creaking) {
        return this.getCreakingProtector().map(creaking2 -> creaking2 == creaking).orElse(false);
    }

    public int getAnalogOutputSignal() {
        return this.outputSignal;
    }

    public int computeAnalogOutputSignal() {
        if (this.creakingInfo == null || this.getCreakingProtector().isEmpty()) {
            return 0;
        }
        double d = this.distanceToCreaking();
        double e = Math.clamp((double)d, (double)0.0, (double)32.0) / 32.0;
        return 15 - (int)Math.floor(e * 15.0);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        valueInput.read("creaking", UUIDUtil.CODEC).ifPresentOrElse(this::setCreakingInfo, this::clearCreakingInfo);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (this.creakingInfo != null) {
            valueOutput.store("creaking", UUIDUtil.CODEC, (UUID)this.creakingInfo.map(Entity::getUUID, uUID -> uUID));
        }
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    private static /* synthetic */ BlockPos.TraversalNodeStatus method_65169(ServerLevel serverLevel, Mutable mutable, BlockPos blockPos) {
        if (!serverLevel.getBlockState(blockPos).is(BlockTags.PALE_OAK_LOGS)) {
            return BlockPos.TraversalNodeStatus.ACCEPT;
        }
        for (Direction direction : Util.shuffledCopy(Direction.values(), serverLevel.random)) {
            BlockPos blockPos2 = blockPos.relative(direction);
            BlockState blockState = serverLevel.getBlockState(blockPos2);
            Direction direction2 = direction.getOpposite();
            if (blockState.isAir()) {
                blockState = Blocks.RESIN_CLUMP.defaultBlockState();
            } else if (blockState.is(Blocks.WATER) && blockState.getFluidState().isSource()) {
                blockState = (BlockState)Blocks.RESIN_CLUMP.defaultBlockState().setValue(MultifaceBlock.WATERLOGGED, true);
            }
            if (!blockState.is(Blocks.RESIN_CLUMP) || MultifaceBlock.hasFace(blockState, direction2)) continue;
            serverLevel.setBlock(blockPos2, (BlockState)blockState.setValue(MultifaceBlock.getFaceProperty(direction2), true), 3);
            mutable.setValue((Object)blockPos2);
            return BlockPos.TraversalNodeStatus.STOP;
        }
        return BlockPos.TraversalNodeStatus.ACCEPT;
    }
}

