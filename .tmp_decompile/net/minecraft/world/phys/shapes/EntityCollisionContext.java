/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.phys.shapes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class EntityCollisionContext
implements CollisionContext {
    private final boolean descending;
    private final double entityBottom;
    private final boolean placement;
    private final ItemStack heldItem;
    private final boolean alwaysCollideWithFluid;
    private final @Nullable Entity entity;

    protected EntityCollisionContext(boolean bl, boolean bl2, double d, ItemStack itemStack, boolean bl3, @Nullable Entity entity) {
        this.descending = bl;
        this.placement = bl2;
        this.entityBottom = d;
        this.heldItem = itemStack;
        this.alwaysCollideWithFluid = bl3;
        this.entity = entity;
    }

    @Deprecated
    protected EntityCollisionContext(Entity entity, boolean bl, boolean bl2) {
        ItemStack itemStack;
        boolean bl3 = entity.isDescending();
        double d = entity.getY();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            itemStack = livingEntity.getMainHandItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        this(bl3, bl2, d, itemStack, bl, entity);
    }

    @Override
    public boolean isHoldingItem(Item item) {
        return this.heldItem.is(item);
    }

    @Override
    public boolean alwaysCollideWithFluid() {
        return this.alwaysCollideWithFluid;
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState, FluidState fluidState2) {
        Entity entity = this.entity;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity.canStandOnFluid(fluidState2) && !fluidState.getType().isSame(fluidState2.getType());
        }
        return false;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, CollisionGetter collisionGetter, BlockPos blockPos) {
        return blockState.getCollisionShape(collisionGetter, blockPos, this);
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
        return this.entityBottom > (double)blockPos.getY() + voxelShape.max(Direction.Axis.Y) - (double)1.0E-5f;
    }

    public @Nullable Entity getEntity() {
        return this.entity;
    }

    @Override
    public boolean isPlacement() {
        return this.placement;
    }

    protected static class Empty
    extends EntityCollisionContext {
        protected static final CollisionContext WITHOUT_FLUID_COLLISIONS = new Empty(false);
        protected static final CollisionContext WITH_FLUID_COLLISIONS = new Empty(true);

        public Empty(boolean bl) {
            super(false, false, -1.7976931348623157E308, ItemStack.EMPTY, bl, null);
        }

        @Override
        public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
            return bl;
        }
    }
}

