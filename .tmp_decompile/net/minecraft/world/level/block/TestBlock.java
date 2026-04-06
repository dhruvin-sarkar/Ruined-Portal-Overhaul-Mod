/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.TestBlockMode;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class TestBlock
extends BaseEntityBlock
implements GameMasterBlock {
    public static final MapCodec<TestBlock> CODEC = TestBlock.simpleCodec(TestBlock::new);
    public static final EnumProperty<TestBlockMode> MODE = BlockStateProperties.TEST_BLOCK_MODE;

    public TestBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TestBlockEntity(blockPos, blockState);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        TestBlockMode testBlockMode;
        BlockItemStateProperties blockItemStateProperties = blockPlaceContext.getItemInHand().get(DataComponents.BLOCK_STATE);
        BlockState blockState = this.defaultBlockState();
        if (blockItemStateProperties != null && (testBlockMode = blockItemStateProperties.get(MODE)) != null) {
            blockState = (BlockState)blockState.setValue(MODE, testBlockMode);
        }
        return blockState;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MODE);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TestBlockEntity)) {
            return InteractionResult.PASS;
        }
        TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
        if (!player.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            player.openTestBlock(testBlockEntity);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        TestBlockEntity testBlockEntity = TestBlock.getServerTestBlockEntity(serverLevel, blockPos);
        if (testBlockEntity == null) {
            return;
        }
        testBlockEntity.reset();
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        TestBlockEntity testBlockEntity = TestBlock.getServerTestBlockEntity(level, blockPos);
        if (testBlockEntity == null) {
            return;
        }
        if (testBlockEntity.getMode() == TestBlockMode.START) {
            return;
        }
        boolean bl2 = level.hasNeighborSignal(blockPos);
        boolean bl3 = testBlockEntity.isPowered();
        if (bl2 && !bl3) {
            testBlockEntity.setPowered(true);
            testBlockEntity.trigger();
        } else if (!bl2 && bl3) {
            testBlockEntity.setPowered(false);
        }
    }

    private static @Nullable TestBlockEntity getServerTestBlockEntity(Level level, BlockPos blockPos) {
        ServerLevel serverLevel;
        BlockEntity blockEntity;
        if (level instanceof ServerLevel && (blockEntity = (serverLevel = (ServerLevel)level).getBlockEntity(blockPos)) instanceof TestBlockEntity) {
            TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
            return testBlockEntity;
        }
        return null;
    }

    @Override
    public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (blockState.getValue(MODE) != TestBlockMode.START) {
            return 0;
        }
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity instanceof TestBlockEntity) {
            TestBlockEntity testBlockEntity = (TestBlockEntity)blockEntity;
            return testBlockEntity.isPowered() ? 15 : 0;
        }
        return 0;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        ItemStack itemStack = super.getCloneItemStack(levelReader, blockPos, blockState, bl);
        return TestBlock.setModeOnStack(itemStack, blockState.getValue(MODE));
    }

    public static ItemStack setModeOnStack(ItemStack itemStack, TestBlockMode testBlockMode) {
        itemStack.set(DataComponents.BLOCK_STATE, itemStack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY).with(MODE, testBlockMode));
        return itemStack;
    }

    protected MapCodec<TestBlock> codec() {
        return CODEC;
    }
}

