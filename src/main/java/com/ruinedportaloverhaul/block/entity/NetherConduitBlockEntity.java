package com.ruinedportaloverhaul.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NetherConduitBlockEntity extends BlockEntity {
    public NetherConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETHER_CONDUIT, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NetherConduitBlockEntity blockEntity) {
        // Behavior is added in the dedicated conduit feature commits.
    }
}
