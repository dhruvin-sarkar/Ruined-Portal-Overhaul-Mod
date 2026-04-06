/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class MinecartCollisionContext
extends EntityCollisionContext {
    private @Nullable BlockPos ingoreBelow;
    private @Nullable BlockPos slopeIgnore;

    protected MinecartCollisionContext(AbstractMinecart abstractMinecart, boolean bl) {
        super(abstractMinecart, bl, false);
        this.setupContext(abstractMinecart);
    }

    private void setupContext(AbstractMinecart abstractMinecart) {
        BlockPos blockPos = abstractMinecart.getCurrentBlockPosOrRailBelow();
        BlockState blockState = abstractMinecart.level().getBlockState(blockPos);
        boolean bl = BaseRailBlock.isRail(blockState);
        if (bl) {
            this.ingoreBelow = blockPos.below();
            RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
            if (railShape.isSlope()) {
                this.slopeIgnore = switch (railShape) {
                    case RailShape.ASCENDING_EAST -> blockPos.east();
                    case RailShape.ASCENDING_WEST -> blockPos.west();
                    case RailShape.ASCENDING_NORTH -> blockPos.north();
                    case RailShape.ASCENDING_SOUTH -> blockPos.south();
                    default -> null;
                };
            }
        }
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, CollisionGetter collisionGetter, BlockPos blockPos) {
        if (blockPos.equals(this.ingoreBelow) || blockPos.equals(this.slopeIgnore)) {
            return Shapes.empty();
        }
        return super.getCollisionShape(blockState, collisionGetter, blockPos);
    }
}

