/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.apache.commons.lang3.function.TriConsumer
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.function.TriConsumer;
import org.jspecify.annotations.Nullable;

public class TransportItemsBetweenContainers
extends Behavior<PathfinderMob> {
    public static final int TARGET_INTERACTION_TIME = 60;
    private static final int VISITED_POSITIONS_MEMORY_TIME = 6000;
    private static final int TRANSPORTED_ITEM_MAX_STACK_SIZE = 16;
    private static final int MAX_VISITED_POSITIONS = 10;
    private static final int MAX_UNREACHABLE_POSITIONS = 50;
    private static final int PASSENGER_MOB_TARGET_SEARCH_DISTANCE = 1;
    private static final int IDLE_COOLDOWN = 140;
    private static final double CLOSE_ENOUGH_TO_START_QUEUING_DISTANCE = 3.0;
    private static final double CLOSE_ENOUGH_TO_START_INTERACTING_WITH_TARGET_DISTANCE = 0.5;
    private static final double CLOSE_ENOUGH_TO_START_INTERACTING_WITH_TARGET_PATH_END_DISTANCE = 1.0;
    private static final double CLOSE_ENOUGH_TO_CONTINUE_INTERACTING_WITH_TARGET = 2.0;
    private final float speedModifier;
    private final int horizontalSearchDistance;
    private final int verticalSearchDistance;
    private final Predicate<BlockState> sourceBlockType;
    private final Predicate<BlockState> destinationBlockType;
    private final Predicate<TransportItemTarget> shouldQueueForTarget;
    private final Consumer<PathfinderMob> onStartTravelling;
    private final Map<ContainerInteractionState, OnTargetReachedInteraction> onTargetInteractionActions;
    private @Nullable TransportItemTarget target = null;
    private TransportItemState state;
    private @Nullable ContainerInteractionState interactionState;
    private int ticksSinceReachingTarget;

    public TransportItemsBetweenContainers(float f, Predicate<BlockState> predicate, Predicate<BlockState> predicate2, int i, int j, Map<ContainerInteractionState, OnTargetReachedInteraction> map, Consumer<PathfinderMob> consumer, Predicate<TransportItemTarget> predicate3) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.VISITED_BLOCK_POSITIONS, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.IS_PANICKING, (Object)((Object)MemoryStatus.VALUE_ABSENT)));
        this.speedModifier = f;
        this.sourceBlockType = predicate;
        this.destinationBlockType = predicate2;
        this.horizontalSearchDistance = i;
        this.verticalSearchDistance = j;
        this.onStartTravelling = consumer;
        this.shouldQueueForTarget = predicate3;
        this.onTargetInteractionActions = map;
        this.state = TransportItemState.TRAVELLING;
    }

    @Override
    protected void start(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        PathNavigation pathNavigation = pathfinderMob.getNavigation();
        if (pathNavigation instanceof GroundPathNavigation) {
            GroundPathNavigation groundPathNavigation = (GroundPathNavigation)pathNavigation;
            groundPathNavigation.setCanPathToTargetsBelowSurface(true);
        }
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        return !pathfinderMob.isLeashed();
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        return pathfinderMob.getBrain().getMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS).isEmpty() && !pathfinderMob.isPanicking() && !pathfinderMob.isLeashed();
    }

    @Override
    protected boolean timedOut(long l) {
        return false;
    }

    @Override
    protected void tick(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        boolean bl = this.updateInvalidTarget(serverLevel, pathfinderMob);
        if (this.target == null) {
            this.stop(serverLevel, pathfinderMob, l);
            return;
        }
        if (bl) {
            return;
        }
        if (this.state.equals((Object)TransportItemState.QUEUING)) {
            this.onQueuingForTarget(this.target, serverLevel, pathfinderMob);
        }
        if (this.state.equals((Object)TransportItemState.TRAVELLING)) {
            this.onTravelToTarget(this.target, serverLevel, pathfinderMob);
        }
        if (this.state.equals((Object)TransportItemState.INTERACTING)) {
            this.onReachedTarget(this.target, serverLevel, pathfinderMob);
        }
    }

    private boolean updateInvalidTarget(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        if (!this.hasValidTarget(serverLevel, pathfinderMob)) {
            this.stopTargetingCurrentTarget(pathfinderMob);
            Optional<TransportItemTarget> optional = this.getTransportTarget(serverLevel, pathfinderMob);
            if (optional.isPresent()) {
                this.target = optional.get();
                this.onStartTravelling(pathfinderMob);
                this.setVisitedBlockPos(pathfinderMob, serverLevel, this.target.pos);
                return true;
            }
            this.enterCooldownAfterNoMatchingTargetFound(pathfinderMob);
            return true;
        }
        return false;
    }

    private void onQueuingForTarget(TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob) {
        if (!this.isAnotherMobInteractingWithTarget(transportItemTarget, level)) {
            this.resumeTravelling(pathfinderMob);
        }
    }

    protected void onTravelToTarget(TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob) {
        if (this.isWithinTargetDistance(3.0, transportItemTarget, level, pathfinderMob, this.getCenterPos(pathfinderMob)) && this.isAnotherMobInteractingWithTarget(transportItemTarget, level)) {
            this.startQueuing(pathfinderMob);
        } else if (this.isWithinTargetDistance(TransportItemsBetweenContainers.getInteractionRange(pathfinderMob), transportItemTarget, level, pathfinderMob, this.getCenterPos(pathfinderMob))) {
            this.startOnReachedTargetInteraction(transportItemTarget, pathfinderMob);
        } else {
            this.walkTowardsTarget(pathfinderMob);
        }
    }

    private Vec3 getCenterPos(PathfinderMob pathfinderMob) {
        return this.setMiddleYPosition(pathfinderMob, pathfinderMob.position());
    }

    protected void onReachedTarget(TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob) {
        if (!this.isWithinTargetDistance(2.0, transportItemTarget, level, pathfinderMob, this.getCenterPos(pathfinderMob))) {
            this.onStartTravelling(pathfinderMob);
        } else {
            ++this.ticksSinceReachingTarget;
            this.onTargetInteraction(transportItemTarget, pathfinderMob);
            if (this.ticksSinceReachingTarget >= 60) {
                this.doReachedTargetInteraction(pathfinderMob, transportItemTarget.container, this::pickUpItems, (pathfinderMob2, container) -> this.stopTargetingCurrentTarget(pathfinderMob), this::putDownItem, (pathfinderMob2, container) -> this.stopTargetingCurrentTarget(pathfinderMob));
                this.onStartTravelling(pathfinderMob);
            }
        }
    }

    private void startQueuing(PathfinderMob pathfinderMob) {
        this.stopInPlace(pathfinderMob);
        this.setTransportingState(TransportItemState.QUEUING);
    }

    private void resumeTravelling(PathfinderMob pathfinderMob) {
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.walkTowardsTarget(pathfinderMob);
    }

    private void walkTowardsTarget(PathfinderMob pathfinderMob) {
        if (this.target != null) {
            BehaviorUtils.setWalkAndLookTargetMemories((LivingEntity)pathfinderMob, this.target.pos, this.speedModifier, 0);
        }
    }

    private void startOnReachedTargetInteraction(TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob) {
        this.doReachedTargetInteraction(pathfinderMob, transportItemTarget.container, this.onReachedInteraction(ContainerInteractionState.PICKUP_ITEM), this.onReachedInteraction(ContainerInteractionState.PICKUP_NO_ITEM), this.onReachedInteraction(ContainerInteractionState.PLACE_ITEM), this.onReachedInteraction(ContainerInteractionState.PLACE_NO_ITEM));
        this.setTransportingState(TransportItemState.INTERACTING);
    }

    private void onStartTravelling(PathfinderMob pathfinderMob) {
        this.onStartTravelling.accept(pathfinderMob);
        this.setTransportingState(TransportItemState.TRAVELLING);
        this.interactionState = null;
        this.ticksSinceReachingTarget = 0;
    }

    private BiConsumer<PathfinderMob, Container> onReachedInteraction(ContainerInteractionState containerInteractionState) {
        return (pathfinderMob, container) -> this.setInteractionState(containerInteractionState);
    }

    private void setTransportingState(TransportItemState transportItemState) {
        this.state = transportItemState;
    }

    private void setInteractionState(ContainerInteractionState containerInteractionState) {
        this.interactionState = containerInteractionState;
    }

    private void onTargetInteraction(TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob) {
        pathfinderMob.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(transportItemTarget.pos));
        this.stopInPlace(pathfinderMob);
        if (this.interactionState != null) {
            Optional.ofNullable(this.onTargetInteractionActions.get((Object)this.interactionState)).ifPresent(onTargetReachedInteraction -> onTargetReachedInteraction.accept(pathfinderMob, (Object)transportItemTarget, this.ticksSinceReachingTarget));
        }
    }

    private void doReachedTargetInteraction(PathfinderMob pathfinderMob, Container container, BiConsumer<PathfinderMob, Container> biConsumer, BiConsumer<PathfinderMob, Container> biConsumer2, BiConsumer<PathfinderMob, Container> biConsumer3, BiConsumer<PathfinderMob, Container> biConsumer4) {
        if (TransportItemsBetweenContainers.isPickingUpItems(pathfinderMob)) {
            if (TransportItemsBetweenContainers.matchesGettingItemsRequirement(container)) {
                biConsumer.accept(pathfinderMob, container);
            } else {
                biConsumer2.accept(pathfinderMob, container);
            }
        } else if (TransportItemsBetweenContainers.matchesLeavingItemsRequirement(pathfinderMob, container)) {
            biConsumer3.accept(pathfinderMob, container);
        } else {
            biConsumer4.accept(pathfinderMob, container);
        }
    }

    private Optional<TransportItemTarget> getTransportTarget(ServerLevel serverLevel, PathfinderMob pathfinderMob) {
        AABB aABB = this.getTargetSearchArea(pathfinderMob);
        Set<GlobalPos> set = TransportItemsBetweenContainers.getVisitedPositions(pathfinderMob);
        Set<GlobalPos> set2 = TransportItemsBetweenContainers.getUnreachablePositions(pathfinderMob);
        List list = ChunkPos.rangeClosed(new ChunkPos(pathfinderMob.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance(pathfinderMob), 16) + 1).toList();
        TransportItemTarget transportItemTarget = null;
        double d = 3.4028234663852886E38;
        for (ChunkPos chunkPos : list) {
            LevelChunk levelChunk = serverLevel.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (levelChunk == null) continue;
            for (BlockEntity blockEntity : levelChunk.getBlockEntities().values()) {
                TransportItemTarget transportItemTarget2;
                ChestBlockEntity chestBlockEntity;
                double e;
                if (!(blockEntity instanceof ChestBlockEntity) || !((e = (chestBlockEntity = (ChestBlockEntity)blockEntity).getBlockPos().distToCenterSqr(pathfinderMob.position())) < d) || (transportItemTarget2 = this.isTargetValidToPick(pathfinderMob, serverLevel, chestBlockEntity, set, set2, aABB)) == null) continue;
                transportItemTarget = transportItemTarget2;
                d = e;
            }
        }
        return transportItemTarget == null ? Optional.empty() : Optional.of(transportItemTarget);
    }

    private @Nullable TransportItemTarget isTargetValidToPick(PathfinderMob pathfinderMob, Level level, BlockEntity blockEntity, Set<GlobalPos> set, Set<GlobalPos> set2, AABB aABB) {
        BlockPos blockPos = blockEntity.getBlockPos();
        boolean bl = aABB.contains(blockPos.getX(), blockPos.getY(), blockPos.getZ());
        if (!bl) {
            return null;
        }
        TransportItemTarget transportItemTarget = TransportItemTarget.tryCreatePossibleTarget(blockEntity, level);
        if (transportItemTarget == null) {
            return null;
        }
        boolean bl2 = this.isWantedBlock(pathfinderMob, transportItemTarget.state) && !this.isPositionAlreadyVisited(set, set2, transportItemTarget, level) && !this.isContainerLocked(transportItemTarget);
        return bl2 ? transportItemTarget : null;
    }

    private boolean isContainerLocked(TransportItemTarget transportItemTarget) {
        BaseContainerBlockEntity baseContainerBlockEntity;
        BlockEntity blockEntity = transportItemTarget.blockEntity;
        return blockEntity instanceof BaseContainerBlockEntity && (baseContainerBlockEntity = (BaseContainerBlockEntity)blockEntity).isLocked();
    }

    private boolean hasValidTarget(Level level, PathfinderMob pathfinderMob) {
        boolean bl;
        boolean bl2 = bl = this.target != null && this.isWantedBlock(pathfinderMob, this.target.state) && this.targetHasNotChanged(level, this.target);
        if (bl && !this.isTargetBlocked(level, this.target)) {
            if (!this.state.equals((Object)TransportItemState.TRAVELLING)) {
                return true;
            }
            if (this.hasValidTravellingPath(level, this.target, pathfinderMob)) {
                return true;
            }
            this.markVisitedBlockPosAsUnreachable(pathfinderMob, level, this.target.pos);
        }
        return false;
    }

    private boolean hasValidTravellingPath(Level level, TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob) {
        Path path = pathfinderMob.getNavigation().getPath() == null ? pathfinderMob.getNavigation().createPath(transportItemTarget.pos, 0) : pathfinderMob.getNavigation().getPath();
        Vec3 vec3 = this.getPositionToReachTargetFrom(path, pathfinderMob);
        boolean bl = this.isWithinTargetDistance(TransportItemsBetweenContainers.getInteractionRange(pathfinderMob), transportItemTarget, level, pathfinderMob, vec3);
        boolean bl2 = path == null && !bl;
        return bl2 || this.targetIsReachableFromPosition(level, bl, vec3, transportItemTarget, pathfinderMob);
    }

    private Vec3 getPositionToReachTargetFrom(@Nullable Path path, PathfinderMob pathfinderMob) {
        boolean bl = path == null || path.getEndNode() == null;
        Vec3 vec3 = bl ? pathfinderMob.position() : path.getEndNode().asBlockPos().getBottomCenter();
        return this.setMiddleYPosition(pathfinderMob, vec3);
    }

    private Vec3 setMiddleYPosition(PathfinderMob pathfinderMob, Vec3 vec3) {
        return vec3.add(0.0, pathfinderMob.getBoundingBox().getYsize() / 2.0, 0.0);
    }

    private boolean isTargetBlocked(Level level, TransportItemTarget transportItemTarget) {
        return ChestBlock.isChestBlockedAt(level, transportItemTarget.pos);
    }

    private boolean targetHasNotChanged(Level level, TransportItemTarget transportItemTarget) {
        return transportItemTarget.blockEntity.equals(level.getBlockEntity(transportItemTarget.pos));
    }

    private Stream<TransportItemTarget> getConnectedTargets(TransportItemTarget transportItemTarget, Level level) {
        if (transportItemTarget.state.getValueOrElse(ChestBlock.TYPE, ChestType.SINGLE) != ChestType.SINGLE) {
            TransportItemTarget transportItemTarget2 = TransportItemTarget.tryCreatePossibleTarget(ChestBlock.getConnectedBlockPos(transportItemTarget.pos, transportItemTarget.state), level);
            return transportItemTarget2 != null ? Stream.of(transportItemTarget, transportItemTarget2) : Stream.of(transportItemTarget);
        }
        return Stream.of(transportItemTarget);
    }

    private AABB getTargetSearchArea(PathfinderMob pathfinderMob) {
        int i = this.getHorizontalSearchDistance(pathfinderMob);
        return new AABB(pathfinderMob.blockPosition()).inflate(i, this.getVerticalSearchDistance(pathfinderMob), i);
    }

    private int getHorizontalSearchDistance(PathfinderMob pathfinderMob) {
        return pathfinderMob.isPassenger() ? 1 : this.horizontalSearchDistance;
    }

    private int getVerticalSearchDistance(PathfinderMob pathfinderMob) {
        return pathfinderMob.isPassenger() ? 1 : this.verticalSearchDistance;
    }

    private static Set<GlobalPos> getVisitedPositions(PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().getMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS).orElse(Set.of());
    }

    private static Set<GlobalPos> getUnreachablePositions(PathfinderMob pathfinderMob) {
        return pathfinderMob.getBrain().getMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS).orElse(Set.of());
    }

    private boolean isPositionAlreadyVisited(Set<GlobalPos> set, Set<GlobalPos> set2, TransportItemTarget transportItemTarget2, Level level) {
        return this.getConnectedTargets(transportItemTarget2, level).map(transportItemTarget -> new GlobalPos(level.dimension(), transportItemTarget.pos)).anyMatch(globalPos -> set.contains(globalPos) || set2.contains(globalPos));
    }

    private static boolean hasFinishedPath(PathfinderMob pathfinderMob) {
        return pathfinderMob.getNavigation().getPath() != null && pathfinderMob.getNavigation().getPath().isDone();
    }

    protected void setVisitedBlockPos(PathfinderMob pathfinderMob, Level level, BlockPos blockPos) {
        HashSet<GlobalPos> set = new HashSet<GlobalPos>(TransportItemsBetweenContainers.getVisitedPositions(pathfinderMob));
        set.add(new GlobalPos(level.dimension(), blockPos));
        if (set.size() > 10) {
            this.enterCooldownAfterNoMatchingTargetFound(pathfinderMob);
        } else {
            pathfinderMob.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
        }
    }

    protected void markVisitedBlockPosAsUnreachable(PathfinderMob pathfinderMob, Level level, BlockPos blockPos) {
        HashSet<GlobalPos> set = new HashSet<GlobalPos>(TransportItemsBetweenContainers.getVisitedPositions(pathfinderMob));
        set.remove((Object)new GlobalPos(level.dimension(), blockPos));
        HashSet<GlobalPos> set2 = new HashSet<GlobalPos>(TransportItemsBetweenContainers.getUnreachablePositions(pathfinderMob));
        set2.add(new GlobalPos(level.dimension(), blockPos));
        if (set2.size() > 50) {
            this.enterCooldownAfterNoMatchingTargetFound(pathfinderMob);
        } else {
            pathfinderMob.getBrain().setMemoryWithExpiry(MemoryModuleType.VISITED_BLOCK_POSITIONS, set, 6000L);
            pathfinderMob.getBrain().setMemoryWithExpiry(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, set2, 6000L);
        }
    }

    private boolean isWantedBlock(PathfinderMob pathfinderMob, BlockState blockState) {
        return TransportItemsBetweenContainers.isPickingUpItems(pathfinderMob) ? this.sourceBlockType.test(blockState) : this.destinationBlockType.test(blockState);
    }

    private static double getInteractionRange(PathfinderMob pathfinderMob) {
        return TransportItemsBetweenContainers.hasFinishedPath(pathfinderMob) ? 1.0 : 0.5;
    }

    private boolean isWithinTargetDistance(double d, TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob, Vec3 vec3) {
        AABB aABB = pathfinderMob.getBoundingBox();
        AABB aABB2 = AABB.ofSize(vec3, aABB.getXsize(), aABB.getYsize(), aABB.getZsize());
        return transportItemTarget.state.getCollisionShape(level, transportItemTarget.pos).bounds().inflate(d, 0.5, d).move(transportItemTarget.pos).intersects(aABB2);
    }

    private boolean targetIsReachableFromPosition(Level level, boolean bl, Vec3 vec3, TransportItemTarget transportItemTarget, PathfinderMob pathfinderMob) {
        return bl && this.canSeeAnyTargetSide(transportItemTarget, level, pathfinderMob, vec3);
    }

    private boolean canSeeAnyTargetSide(TransportItemTarget transportItemTarget, Level level, PathfinderMob pathfinderMob, Vec3 vec3) {
        Vec3 vec322 = transportItemTarget.pos.getCenter();
        return Direction.stream().map(direction -> vec322.add(0.5 * (double)direction.getStepX(), 0.5 * (double)direction.getStepY(), 0.5 * (double)direction.getStepZ())).map(vec32 -> level.clip(new ClipContext(vec3, (Vec3)vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, pathfinderMob))).anyMatch(blockHitResult -> blockHitResult.getType() == HitResult.Type.BLOCK && blockHitResult.getBlockPos().equals(transportItemTarget.pos));
    }

    private boolean isAnotherMobInteractingWithTarget(TransportItemTarget transportItemTarget, Level level) {
        return this.getConnectedTargets(transportItemTarget, level).anyMatch(this.shouldQueueForTarget);
    }

    private static boolean isPickingUpItems(PathfinderMob pathfinderMob) {
        return pathfinderMob.getMainHandItem().isEmpty();
    }

    private static boolean matchesGettingItemsRequirement(Container container) {
        return !container.isEmpty();
    }

    private static boolean matchesLeavingItemsRequirement(PathfinderMob pathfinderMob, Container container) {
        return container.isEmpty() || TransportItemsBetweenContainers.hasItemMatchingHandItem(pathfinderMob, container);
    }

    private static boolean hasItemMatchingHandItem(PathfinderMob pathfinderMob, Container container) {
        ItemStack itemStack = pathfinderMob.getMainHandItem();
        for (ItemStack itemStack2 : container) {
            if (!ItemStack.isSameItem(itemStack2, itemStack)) continue;
            return true;
        }
        return false;
    }

    private void pickUpItems(PathfinderMob pathfinderMob, Container container) {
        pathfinderMob.setItemSlot(EquipmentSlot.MAINHAND, TransportItemsBetweenContainers.pickupItemFromContainer(container));
        pathfinderMob.setGuaranteedDrop(EquipmentSlot.MAINHAND);
        container.setChanged();
        this.clearMemoriesAfterMatchingTargetFound(pathfinderMob);
    }

    private void putDownItem(PathfinderMob pathfinderMob, Container container) {
        ItemStack itemStack = TransportItemsBetweenContainers.addItemsToContainer(pathfinderMob, container);
        container.setChanged();
        pathfinderMob.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
        if (itemStack.isEmpty()) {
            this.clearMemoriesAfterMatchingTargetFound(pathfinderMob);
        } else {
            this.stopTargetingCurrentTarget(pathfinderMob);
        }
    }

    private static ItemStack pickupItemFromContainer(Container container) {
        int i = 0;
        for (ItemStack itemStack : container) {
            if (!itemStack.isEmpty()) {
                int j = Math.min(itemStack.getCount(), 16);
                return container.removeItem(i, j);
            }
            ++i;
        }
        return ItemStack.EMPTY;
    }

    private static ItemStack addItemsToContainer(PathfinderMob pathfinderMob, Container container) {
        int i = 0;
        ItemStack itemStack = pathfinderMob.getMainHandItem();
        for (ItemStack itemStack2 : container) {
            if (itemStack2.isEmpty()) {
                container.setItem(i, itemStack);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(itemStack2, itemStack) && itemStack2.getCount() < itemStack2.getMaxStackSize()) {
                int j = itemStack2.getMaxStackSize() - itemStack2.getCount();
                int k = Math.min(j, itemStack.getCount());
                itemStack2.setCount(itemStack2.getCount() + k);
                itemStack.setCount(itemStack.getCount() - j);
                container.setItem(i, itemStack2);
                if (itemStack.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            }
            ++i;
        }
        return itemStack;
    }

    protected void stopTargetingCurrentTarget(PathfinderMob pathfinderMob) {
        this.ticksSinceReachingTarget = 0;
        this.target = null;
        pathfinderMob.getNavigation().stop();
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    protected void clearMemoriesAfterMatchingTargetFound(PathfinderMob pathfinderMob) {
        this.stopTargetingCurrentTarget(pathfinderMob);
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    private void enterCooldownAfterNoMatchingTargetFound(PathfinderMob pathfinderMob) {
        this.stopTargetingCurrentTarget(pathfinderMob);
        pathfinderMob.getBrain().setMemory(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, 140);
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.VISITED_BLOCK_POSITIONS);
        pathfinderMob.getBrain().eraseMemory(MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS);
    }

    @Override
    protected void stop(ServerLevel serverLevel, PathfinderMob pathfinderMob, long l) {
        this.onStartTravelling(pathfinderMob);
        PathNavigation pathNavigation = pathfinderMob.getNavigation();
        if (pathNavigation instanceof GroundPathNavigation) {
            GroundPathNavigation groundPathNavigation = (GroundPathNavigation)pathNavigation;
            groundPathNavigation.setCanPathToTargetsBelowSurface(false);
        }
    }

    private void stopInPlace(PathfinderMob pathfinderMob) {
        pathfinderMob.getNavigation().stop();
        pathfinderMob.setXxa(0.0f);
        pathfinderMob.setYya(0.0f);
        pathfinderMob.setSpeed(0.0f);
        pathfinderMob.setDeltaMovement(0.0, pathfinderMob.getDeltaMovement().y, 0.0);
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void stop(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.stop(serverLevel, (PathfinderMob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (PathfinderMob)livingEntity, l);
    }

    public static final class TransportItemTarget
    extends Record {
        final BlockPos pos;
        final Container container;
        final BlockEntity blockEntity;
        final BlockState state;

        public TransportItemTarget(BlockPos blockPos, Container container, BlockEntity blockEntity, BlockState blockState) {
            this.pos = blockPos;
            this.container = container;
            this.blockEntity = blockEntity;
            this.state = blockState;
        }

        public static @Nullable TransportItemTarget tryCreatePossibleTarget(BlockEntity blockEntity, Level level) {
            BlockPos blockPos = blockEntity.getBlockPos();
            BlockState blockState = blockEntity.getBlockState();
            Container container = TransportItemTarget.getBlockEntityContainer(blockEntity, blockState, level, blockPos);
            if (container != null) {
                return new TransportItemTarget(blockPos, container, blockEntity, blockState);
            }
            return null;
        }

        public static @Nullable TransportItemTarget tryCreatePossibleTarget(BlockPos blockPos, Level level) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            return blockEntity == null ? null : TransportItemTarget.tryCreatePossibleTarget(blockEntity, level);
        }

        private static @Nullable Container getBlockEntityContainer(BlockEntity blockEntity, BlockState blockState, Level level, BlockPos blockPos) {
            Block block = blockState.getBlock();
            if (block instanceof ChestBlock) {
                ChestBlock chestBlock = (ChestBlock)block;
                return ChestBlock.getContainer(chestBlock, blockState, level, blockPos, false);
            }
            if (blockEntity instanceof Container) {
                Container container = (Container)((Object)blockEntity);
                return container;
            }
            return null;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TransportItemTarget.class, "pos;container;blockEntity;state", "pos", "container", "blockEntity", "state"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TransportItemTarget.class, "pos;container;blockEntity;state", "pos", "container", "blockEntity", "state"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TransportItemTarget.class, "pos;container;blockEntity;state", "pos", "container", "blockEntity", "state"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Container container() {
            return this.container;
        }

        public BlockEntity blockEntity() {
            return this.blockEntity;
        }

        public BlockState state() {
            return this.state;
        }
    }

    public static enum TransportItemState {
        TRAVELLING,
        QUEUING,
        INTERACTING;

    }

    public static enum ContainerInteractionState {
        PICKUP_ITEM,
        PICKUP_NO_ITEM,
        PLACE_ITEM,
        PLACE_NO_ITEM;

    }

    @FunctionalInterface
    public static interface OnTargetReachedInteraction
    extends TriConsumer<PathfinderMob, TransportItemTarget, Integer> {
    }
}

