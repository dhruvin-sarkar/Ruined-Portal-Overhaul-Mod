/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.Short2BooleanMap
 *  it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
 */
package net.minecraft.world.level.material;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class FlowingFluid
extends Fluid {
    public static final BooleanProperty FALLING = BlockStateProperties.FALLING;
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_FLOWING;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<BlockStatePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<BlockStatePairKey>(200){

            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });
    private final Map<FluidState, VoxelShape> shapes = Maps.newIdentityHashMap();

    @Override
    protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING);
    }

    @Override
    public Vec3 getFlow(BlockGetter blockGetter, BlockPos blockPos, FluidState fluidState) {
        double d = 0.0;
        double e = 0.0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            FluidState fluidState2 = blockGetter.getFluidState(mutableBlockPos);
            if (!this.affectsFlow(fluidState2)) continue;
            float f = fluidState2.getOwnHeight();
            float g = 0.0f;
            if (f == 0.0f) {
                Vec3i blockPos2;
                FluidState fluidState3;
                if (!blockGetter.getBlockState(mutableBlockPos).blocksMotion() && this.affectsFlow(fluidState3 = blockGetter.getFluidState((BlockPos)(blockPos2 = mutableBlockPos.below()))) && (f = fluidState3.getOwnHeight()) > 0.0f) {
                    g = fluidState.getOwnHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                g = fluidState.getOwnHeight() - f;
            }
            if (g == 0.0f) continue;
            d += (double)((float)direction.getStepX() * g);
            e += (double)((float)direction.getStepZ() * g);
        }
        Vec3 vec3 = new Vec3(d, 0.0, e);
        if (fluidState.getValue(FALLING).booleanValue()) {
            for (Direction direction2 : Direction.Plane.HORIZONTAL) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos, direction2);
                if (!this.isSolidFace(blockGetter, mutableBlockPos, direction2) && !this.isSolidFace(blockGetter, (BlockPos)mutableBlockPos.above(), direction2)) continue;
                vec3 = vec3.normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return vec3.normalize();
    }

    private boolean affectsFlow(FluidState fluidState) {
        return fluidState.isEmpty() || fluidState.getType().isSame(this);
    }

    protected boolean isSolidFace(BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        BlockState blockState = blockGetter.getBlockState(blockPos);
        FluidState fluidState = blockGetter.getFluidState(blockPos);
        if (fluidState.getType().isSame(this)) {
            return false;
        }
        if (direction == Direction.UP) {
            return true;
        }
        if (blockState.getBlock() instanceof IceBlock) {
            return false;
        }
        return blockState.isFaceSturdy(blockGetter, blockPos, direction);
    }

    protected void spread(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        FluidState fluidState3;
        Fluid fluid;
        FluidState fluidState2;
        BlockState blockState2;
        if (fluidState.isEmpty()) {
            return;
        }
        BlockPos blockPos2 = blockPos.below();
        if (this.canMaybePassThrough(serverLevel, blockPos, blockState, Direction.DOWN, blockPos2, blockState2 = serverLevel.getBlockState(blockPos2), fluidState2 = blockState2.getFluidState()) && fluidState2.canBeReplacedWith(serverLevel, blockPos2, fluid = (fluidState3 = this.getNewLiquid(serverLevel, blockPos2, blockState2)).getType(), Direction.DOWN) && FlowingFluid.canHoldSpecificFluid(serverLevel, blockPos2, blockState2, fluid)) {
            this.spreadTo(serverLevel, blockPos2, blockState2, Direction.DOWN, fluidState3);
            if (this.sourceNeighborCount(serverLevel, blockPos) >= 3) {
                this.spreadToSides(serverLevel, blockPos, fluidState, blockState);
            }
            return;
        }
        if (fluidState.isSource() || !this.isWaterHole(serverLevel, blockPos, blockState, blockPos2, blockState2)) {
            this.spreadToSides(serverLevel, blockPos, fluidState, blockState);
        }
    }

    private void spreadToSides(ServerLevel serverLevel, BlockPos blockPos, FluidState fluidState, BlockState blockState) {
        int i = fluidState.getAmount() - this.getDropOff(serverLevel);
        if (fluidState.getValue(FALLING).booleanValue()) {
            i = 7;
        }
        if (i <= 0) {
            return;
        }
        Map<Direction, FluidState> map = this.getSpread(serverLevel, blockPos, blockState);
        for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
            Direction direction = entry.getKey();
            FluidState fluidState2 = entry.getValue();
            BlockPos blockPos2 = blockPos.relative(direction);
            this.spreadTo(serverLevel, blockPos2, serverLevel.getBlockState(blockPos2), direction, fluidState2);
        }
    }

    protected FluidState getNewLiquid(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        BlockPos.MutableBlockPos blockPos3;
        BlockState blockState4;
        FluidState fluidState3;
        int i = 0;
        int j = 0;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos.MutableBlockPos blockPos2 = mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            BlockState blockState2 = serverLevel.getBlockState(blockPos2);
            FluidState fluidState = blockState2.getFluidState();
            if (!fluidState.getType().isSame(this) || !FlowingFluid.canPassThroughWall(direction, serverLevel, blockPos, blockState, blockPos2, blockState2)) continue;
            if (fluidState.isSource()) {
                ++j;
            }
            i = Math.max(i, fluidState.getAmount());
        }
        if (j >= 2 && this.canConvertToSource(serverLevel)) {
            BlockState blockState3 = serverLevel.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos, Direction.DOWN));
            FluidState fluidState2 = blockState3.getFluidState();
            if (blockState3.isSolid() || this.isSourceBlockOfThisType(fluidState2)) {
                return this.getSource(false);
            }
        }
        if (!(fluidState3 = (blockState4 = serverLevel.getBlockState(blockPos3 = mutableBlockPos.setWithOffset((Vec3i)blockPos, Direction.UP))).getFluidState()).isEmpty() && fluidState3.getType().isSame(this) && FlowingFluid.canPassThroughWall(Direction.UP, serverLevel, blockPos, blockState, blockPos3, blockState4)) {
            return this.getFlowing(8, true);
        }
        int k = i - this.getDropOff(serverLevel);
        if (k <= 0) {
            return Fluids.EMPTY.defaultFluidState();
        }
        return this.getFlowing(k, false);
    }

    private static boolean canPassThroughWall(Direction direction, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        boolean bl;
        BlockStatePairKey blockStatePairKey;
        if (SharedConstants.DEBUG_DISABLE_LIQUID_SPREADING || SharedConstants.DEBUG_ONLY_GENERATE_HALF_THE_WORLD && blockPos2.getZ() < 0) {
            return false;
        }
        VoxelShape voxelShape = blockState2.getCollisionShape(blockGetter, blockPos2);
        if (voxelShape == Shapes.block()) {
            return false;
        }
        VoxelShape voxelShape2 = blockState.getCollisionShape(blockGetter, blockPos);
        if (voxelShape2 == Shapes.block()) {
            return false;
        }
        if (voxelShape2 == Shapes.empty() && voxelShape == Shapes.empty()) {
            return true;
        }
        Object2ByteLinkedOpenHashMap<BlockStatePairKey> object2ByteLinkedOpenHashMap = blockState.getBlock().hasDynamicShape() || blockState2.getBlock().hasDynamicShape() ? null : OCCLUSION_CACHE.get();
        if (object2ByteLinkedOpenHashMap != null) {
            blockStatePairKey = new BlockStatePairKey(blockState, blockState2, direction);
            byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst((Object)blockStatePairKey);
            if (b != 127) {
                return b != 0;
            }
        } else {
            blockStatePairKey = null;
        }
        boolean bl2 = bl = !Shapes.mergedFaceOccludes(voxelShape2, voxelShape, direction);
        if (object2ByteLinkedOpenHashMap != null) {
            if (object2ByteLinkedOpenHashMap.size() == 200) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst((Object)blockStatePairKey, (byte)(bl ? 1 : 0));
        }
        return bl;
    }

    public abstract Fluid getFlowing();

    public FluidState getFlowing(int i, boolean bl) {
        return (FluidState)((FluidState)this.getFlowing().defaultFluidState().setValue(LEVEL, i)).setValue(FALLING, bl);
    }

    public abstract Fluid getSource();

    public FluidState getSource(boolean bl) {
        return (FluidState)this.getSource().defaultFluidState().setValue(FALLING, bl);
    }

    protected abstract boolean canConvertToSource(ServerLevel var1);

    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            LiquidBlockContainer liquidBlockContainer = (LiquidBlockContainer)((Object)block);
            liquidBlockContainer.placeLiquid(levelAccessor, blockPos, blockState, fluidState);
        } else {
            if (!blockState.isAir()) {
                this.beforeDestroyingBlock(levelAccessor, blockPos, blockState);
            }
            levelAccessor.setBlock(blockPos, fluidState.createLegacyBlock(), 3);
        }
    }

    protected abstract void beforeDestroyingBlock(LevelAccessor var1, BlockPos var2, BlockState var3);

    protected int getSlopeDistance(LevelReader levelReader, BlockPos blockPos, int i, Direction direction, BlockState blockState, SpreadContext spreadContext) {
        int j = 1000;
        for (Direction direction2 : Direction.Plane.HORIZONTAL) {
            int k;
            if (direction2 == direction) continue;
            BlockPos blockPos2 = blockPos.relative(direction2);
            BlockState blockState2 = spreadContext.getBlockState(blockPos2);
            FluidState fluidState = blockState2.getFluidState();
            if (!this.canPassThrough(levelReader, this.getFlowing(), blockPos, blockState, direction2, blockPos2, blockState2, fluidState)) continue;
            if (spreadContext.isHole(blockPos2)) {
                return i;
            }
            if (i >= this.getSlopeFindDistance(levelReader) || (k = this.getSlopeDistance(levelReader, blockPos2, i + 1, direction2.getOpposite(), blockState2, spreadContext)) >= j) continue;
            j = k;
        }
        return j;
    }

    boolean isWaterHole(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, BlockPos blockPos2, BlockState blockState2) {
        if (!FlowingFluid.canPassThroughWall(Direction.DOWN, blockGetter, blockPos, blockState, blockPos2, blockState2)) {
            return false;
        }
        if (blockState2.getFluidState().getType().isSame(this)) {
            return true;
        }
        return FlowingFluid.canHoldFluid(blockGetter, blockPos2, blockState2, this.getFlowing());
    }

    private boolean canPassThrough(BlockGetter blockGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState) {
        return this.canMaybePassThrough(blockGetter, blockPos, blockState, direction, blockPos2, blockState2, fluidState) && FlowingFluid.canHoldSpecificFluid(blockGetter, blockPos2, blockState2, fluid);
    }

    private boolean canMaybePassThrough(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction, BlockPos blockPos2, BlockState blockState2, FluidState fluidState) {
        return !this.isSourceBlockOfThisType(fluidState) && FlowingFluid.canHoldAnyFluid(blockState2) && FlowingFluid.canPassThroughWall(direction, blockGetter, blockPos, blockState, blockPos2, blockState2);
    }

    private boolean isSourceBlockOfThisType(FluidState fluidState) {
        return fluidState.getType().isSame(this) && fluidState.isSource();
    }

    protected abstract int getSlopeFindDistance(LevelReader var1);

    private int sourceNeighborCount(LevelReader levelReader, BlockPos blockPos) {
        int i = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative(direction);
            FluidState fluidState = levelReader.getFluidState(blockPos2);
            if (!this.isSourceBlockOfThisType(fluidState)) continue;
            ++i;
        }
        return i;
    }

    protected Map<Direction, FluidState> getSpread(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        int i = 1000;
        EnumMap map = Maps.newEnumMap(Direction.class);
        SpreadContext spreadContext = null;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            int j;
            FluidState fluidState2;
            FluidState fluidState;
            BlockState blockState2;
            BlockPos blockPos2;
            if (!this.canMaybePassThrough(serverLevel, blockPos, blockState, direction, blockPos2 = blockPos.relative(direction), blockState2 = serverLevel.getBlockState(blockPos2), fluidState = blockState2.getFluidState()) || !FlowingFluid.canHoldSpecificFluid(serverLevel, blockPos2, blockState2, (fluidState2 = this.getNewLiquid(serverLevel, blockPos2, blockState2)).getType())) continue;
            if (spreadContext == null) {
                spreadContext = new SpreadContext(serverLevel, blockPos);
            }
            if ((j = spreadContext.isHole(blockPos2) ? 0 : this.getSlopeDistance(serverLevel, blockPos2, 1, direction.getOpposite(), blockState2, spreadContext)) < i) {
                map.clear();
            }
            if (j > i) continue;
            if (fluidState.canBeReplacedWith(serverLevel, blockPos2, fluidState2.getType(), direction)) {
                map.put(direction, fluidState2);
            }
            i = j;
        }
        return map;
    }

    private static boolean canHoldAnyFluid(BlockState blockState) {
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            return true;
        }
        if (blockState.blocksMotion()) {
            return false;
        }
        return !(block instanceof DoorBlock) && !blockState.is(BlockTags.SIGNS) && !blockState.is(Blocks.LADDER) && !blockState.is(Blocks.SUGAR_CANE) && !blockState.is(Blocks.BUBBLE_COLUMN) && !blockState.is(Blocks.NETHER_PORTAL) && !blockState.is(Blocks.END_PORTAL) && !blockState.is(Blocks.END_GATEWAY) && !blockState.is(Blocks.STRUCTURE_VOID);
    }

    private static boolean canHoldFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        return FlowingFluid.canHoldAnyFluid(blockState) && FlowingFluid.canHoldSpecificFluid(blockGetter, blockPos, blockState, fluid);
    }

    private static boolean canHoldSpecificFluid(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Fluid fluid) {
        Block block = blockState.getBlock();
        if (block instanceof LiquidBlockContainer) {
            LiquidBlockContainer liquidBlockContainer = (LiquidBlockContainer)((Object)block);
            return liquidBlockContainer.canPlaceLiquid(null, blockGetter, blockPos, blockState, fluid);
        }
        return true;
    }

    protected abstract int getDropOff(LevelReader var1);

    protected int getSpreadDelay(Level level, BlockPos blockPos, FluidState fluidState, FluidState fluidState2) {
        return this.getTickDelay(level);
    }

    @Override
    public void tick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!fluidState.isSource()) {
            FluidState fluidState2 = this.getNewLiquid(serverLevel, blockPos, serverLevel.getBlockState(blockPos));
            int i = this.getSpreadDelay(serverLevel, blockPos, fluidState, fluidState2);
            if (fluidState2.isEmpty()) {
                fluidState = fluidState2;
                blockState = Blocks.AIR.defaultBlockState();
                serverLevel.setBlock(blockPos, blockState, 3);
            } else if (fluidState2 != fluidState) {
                fluidState = fluidState2;
                blockState = fluidState.createLegacyBlock();
                serverLevel.setBlock(blockPos, blockState, 3);
                serverLevel.scheduleTick(blockPos, fluidState.getType(), i);
            }
        }
        this.spread(serverLevel, blockPos, blockState, fluidState);
    }

    protected static int getLegacyLevel(FluidState fluidState) {
        if (fluidState.isSource()) {
            return 0;
        }
        return 8 - Math.min(fluidState.getAmount(), 8) + (fluidState.getValue(FALLING) != false ? 8 : 0);
    }

    private static boolean hasSameAbove(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        return fluidState.getType().isSame(blockGetter.getFluidState(blockPos.above()).getType());
    }

    @Override
    public float getHeight(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos) {
        if (FlowingFluid.hasSameAbove(fluidState, blockGetter, blockPos)) {
            return 1.0f;
        }
        return fluidState.getOwnHeight();
    }

    @Override
    public float getOwnHeight(FluidState fluidState) {
        return (float)fluidState.getAmount() / 9.0f;
    }

    @Override
    public abstract int getAmount(FluidState var1);

    @Override
    public VoxelShape getShape(FluidState fluidState2, BlockGetter blockGetter, BlockPos blockPos) {
        if (fluidState2.getAmount() == 9 && FlowingFluid.hasSameAbove(fluidState2, blockGetter, blockPos)) {
            return Shapes.block();
        }
        return this.shapes.computeIfAbsent(fluidState2, fluidState -> Shapes.box(0.0, 0.0, 0.0, 1.0, fluidState.getHeight(blockGetter, blockPos), 1.0));
    }

    record BlockStatePairKey(BlockState first, BlockState second, Direction direction) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (!(object instanceof BlockStatePairKey)) return false;
            BlockStatePairKey blockStatePairKey = (BlockStatePairKey)((Object)object);
            if (this.first != blockStatePairKey.first) return false;
            if (this.second != blockStatePairKey.second) return false;
            if (this.direction != blockStatePairKey.direction) return false;
            return true;
        }

        public int hashCode() {
            int i = System.identityHashCode(this.first);
            i = 31 * i + System.identityHashCode(this.second);
            i = 31 * i + this.direction.hashCode();
            return i;
        }
    }

    protected class SpreadContext {
        private final BlockGetter level;
        private final BlockPos origin;
        private final Short2ObjectMap<BlockState> stateCache = new Short2ObjectOpenHashMap();
        private final Short2BooleanMap holeCache = new Short2BooleanOpenHashMap();

        SpreadContext(BlockGetter blockGetter, BlockPos blockPos) {
            this.level = blockGetter;
            this.origin = blockPos;
        }

        public BlockState getBlockState(BlockPos blockPos) {
            return this.getBlockState(blockPos, this.getCacheKey(blockPos));
        }

        private BlockState getBlockState(BlockPos blockPos, short s2) {
            return (BlockState)this.stateCache.computeIfAbsent(s2, s -> this.level.getBlockState(blockPos));
        }

        public boolean isHole(BlockPos blockPos) {
            return this.holeCache.computeIfAbsent(this.getCacheKey(blockPos), s -> {
                BlockState blockState = this.getBlockState(blockPos, s);
                BlockPos blockPos2 = blockPos.below();
                BlockState blockState2 = this.level.getBlockState(blockPos2);
                return FlowingFluid.this.isWaterHole(this.level, blockPos, blockState, blockPos2, blockState2);
            });
        }

        private short getCacheKey(BlockPos blockPos) {
            int i = blockPos.getX() - this.origin.getX();
            int j = blockPos.getZ() - this.origin.getZ();
            return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
        }
    }
}

