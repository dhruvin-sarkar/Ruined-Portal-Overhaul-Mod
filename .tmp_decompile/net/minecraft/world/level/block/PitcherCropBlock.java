/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  java.lang.MatchException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class PitcherCropBlock
extends DoublePlantBlock
implements BonemealableBlock {
    public static final MapCodec<PitcherCropBlock> CODEC = PitcherCropBlock.simpleCodec(PitcherCropBlock::new);
    public static final int MAX_AGE = 4;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    public static final EnumProperty<DoubleBlockHalf> HALF = DoublePlantBlock.HALF;
    private static final int DOUBLE_PLANT_AGE_INTERSECTION = 3;
    private static final int BONEMEAL_INCREASE = 1;
    private static final VoxelShape SHAPE_BULB = Block.column(6.0, -1.0, 3.0);
    private static final VoxelShape SHAPE_CROP = Block.column(10.0, -1.0, 5.0);
    private final Function<BlockState, VoxelShape> shapes = this.makeShapes();

    public MapCodec<PitcherCropBlock> codec() {
        return CODEC;
    }

    public PitcherCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    private Function<BlockState, VoxelShape> makeShapes() {
        int[] is = new int[]{0, 9, 11, 22, 26};
        return this.getShapeForEachState(blockState -> {
            int i = (blockState.getValue(AGE) == 0 ? 4 : 6) + is[blockState.getValue(AGE)];
            int j = blockState.getValue(AGE) == 0 ? 6 : 10;
            return switch (blockState.getValue(HALF)) {
                default -> throw new MatchException(null, null);
                case DoubleBlockHalf.LOWER -> Block.column(j, -1.0, Math.min(16, -1 + i));
                case DoubleBlockHalf.UPPER -> Block.column(j, 0.0, Math.max(0, -1 + i - 16));
            };
        });
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState();
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapes.apply(blockState);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return blockState.getValue(AGE) == 0 ? SHAPE_BULB : SHAPE_CROP;
        }
        return Shapes.empty();
    }

    @Override
    public BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (PitcherCropBlock.isDouble(blockState.getValue(AGE))) {
            return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
        }
        return blockState.canSurvive(levelReader, blockPos) ? blockState : Blocks.AIR.defaultBlockState();
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        if (PitcherCropBlock.isLower(blockState) && !PitcherCropBlock.sufficientLight(levelReader, blockPos)) {
            return false;
        }
        return super.canSurvive(blockState, levelReader, blockPos);
    }

    @Override
    protected boolean mayPlaceOn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return blockState.is(Blocks.FARMLAND);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (entity instanceof Ravager && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                serverLevel.destroyBlock(blockPos, true, entity);
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext blockPlaceContext) {
        return false;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
    }

    @Override
    public boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(blockState);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        boolean bl;
        float f = CropBlock.getGrowthSpeed(this, serverLevel, blockPos);
        boolean bl2 = bl = randomSource.nextInt((int)(25.0f / f) + 1) == 0;
        if (bl) {
            this.grow(serverLevel, blockState, blockPos, 1);
        }
    }

    private void grow(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, int i) {
        int j = Math.min(blockState.getValue(AGE) + i, 4);
        if (!this.canGrow(serverLevel, blockPos, blockState, j)) {
            return;
        }
        BlockState blockState2 = (BlockState)blockState.setValue(AGE, j);
        serverLevel.setBlock(blockPos, blockState2, 2);
        if (PitcherCropBlock.isDouble(j)) {
            serverLevel.setBlock(blockPos.above(), (BlockState)blockState2.setValue(HALF, DoubleBlockHalf.UPPER), 3);
        }
    }

    private static boolean canGrowInto(LevelReader levelReader, BlockPos blockPos) {
        BlockState blockState = levelReader.getBlockState(blockPos);
        return blockState.isAir() || blockState.is(Blocks.PITCHER_CROP);
    }

    private static boolean sufficientLight(LevelReader levelReader, BlockPos blockPos) {
        return CropBlock.hasSufficientLight(levelReader, blockPos);
    }

    private static boolean isLower(BlockState blockState) {
        return blockState.is(Blocks.PITCHER_CROP) && blockState.getValue(HALF) == DoubleBlockHalf.LOWER;
    }

    private static boolean isDouble(int i) {
        return i >= 3;
    }

    private boolean canGrow(LevelReader levelReader, BlockPos blockPos, BlockState blockState, int i) {
        return !this.isMaxAge(blockState) && PitcherCropBlock.sufficientLight(levelReader, blockPos) && (!PitcherCropBlock.isDouble(i) || PitcherCropBlock.canGrowInto(levelReader, blockPos.above()));
    }

    private boolean isMaxAge(BlockState blockState) {
        return blockState.getValue(AGE) >= 4;
    }

    private @Nullable PosAndState getLowerHalf(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        if (PitcherCropBlock.isLower(blockState)) {
            return new PosAndState(blockPos, blockState);
        }
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState2 = levelReader.getBlockState(blockPos2);
        if (PitcherCropBlock.isLower(blockState2)) {
            return new PosAndState(blockPos2, blockState2);
        }
        return null;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        PosAndState posAndState = this.getLowerHalf(levelReader, blockPos, blockState);
        if (posAndState == null) {
            return false;
        }
        return this.canGrow(levelReader, posAndState.pos, posAndState.state, posAndState.state.getValue(AGE) + 1);
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        PosAndState posAndState = this.getLowerHalf(serverLevel, blockPos, blockState);
        if (posAndState == null) {
            return;
        }
        this.grow(serverLevel, posAndState.state, posAndState.pos, 1);
    }

    static final class PosAndState
    extends Record {
        final BlockPos pos;
        final BlockState state;

        PosAndState(BlockPos blockPos, BlockState blockState) {
            this.pos = blockPos;
            this.state = blockState;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PosAndState.class, "pos;state", "pos", "state"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PosAndState.class, "pos;state", "pos", "state"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PosAndState.class, "pos;state", "pos", "state"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }
    }
}

