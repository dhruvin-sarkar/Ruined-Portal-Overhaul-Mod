package com.ruinedportaloverhaul.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NetherConduitBlockEntity extends BlockEntity {
    private static final int ACTIVATION_SCAN_INTERVAL_TICKS = 20;
    private static final int ACTIVATION_REQUIRED_FRAME_BLOCKS = 12;

    private boolean active;
    private int frameBlockCount;

    public NetherConduitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NETHER_CONDUIT, pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, NetherConduitBlockEntity blockEntity) {
        if (level.getGameTime() % ACTIVATION_SCAN_INTERVAL_TICKS != 0) {
            return;
        }

        int frameBlocks = countFrameBlocks(level, pos);
        boolean active = frameBlocks >= ACTIVATION_REQUIRED_FRAME_BLOCKS;
        if (blockEntity.active != active || blockEntity.frameBlockCount != frameBlocks) {
            blockEntity.active = active;
            blockEntity.frameBlockCount = frameBlocks;
            blockEntity.setChanged();
        }
    }

    public boolean isActive() {
        return this.active;
    }

    public int frameBlockCount() {
        return this.frameBlockCount;
    }

    private static int countFrameBlocks(Level level, BlockPos pos) {
        int count = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (!isFrameEdge(dx, dy, dz)) {
                        continue;
                    }
                    cursor.set(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (level.getBlockState(cursor).is(Blocks.NETHER_BRICKS)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    private static boolean isFrameEdge(int dx, int dy, int dz) {
        int outerAxes = 0;
        if (Math.abs(dx) == 2) {
            outerAxes++;
        }
        if (Math.abs(dy) == 2) {
            outerAxes++;
        }
        if (Math.abs(dz) == 2) {
            outerAxes++;
        }
        return outerAxes == 2;
    }
}
