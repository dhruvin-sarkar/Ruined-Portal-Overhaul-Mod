package com.ruinedportaloverhaul.block;

import com.mojang.serialization.MapCodec;
import com.ruinedportaloverhaul.block.entity.ModBlockEntities;
import com.ruinedportaloverhaul.block.entity.NetherConduitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NetherConduitBlock extends BaseEntityBlock {
    public static final MapCodec<NetherConduitBlock> CODEC = BlockBehaviour.simpleCodec(NetherConduitBlock::new);
    private static final VoxelShape SHAPE = box(5.0, 5.0, 5.0, 11.0, 11.0, 11.0);

    public NetherConduitBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<NetherConduitBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NetherConduitBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide()
            ? null
            : createTickerHelper(blockEntityType, ModBlockEntities.NETHER_CONDUIT, NetherConduitBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
