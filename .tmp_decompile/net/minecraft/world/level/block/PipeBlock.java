/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class PipeBlock
extends Block {
    public static final BooleanProperty NORTH = BlockStateProperties.NORTH;
    public static final BooleanProperty EAST = BlockStateProperties.EAST;
    public static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
    public static final BooleanProperty WEST = BlockStateProperties.WEST;
    public static final BooleanProperty UP = BlockStateProperties.UP;
    public static final BooleanProperty DOWN = BlockStateProperties.DOWN;
    public static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = ImmutableMap.copyOf((Map)Maps.newEnumMap((Map)Map.of((Object)Direction.NORTH, (Object)NORTH, (Object)Direction.EAST, (Object)EAST, (Object)Direction.SOUTH, (Object)SOUTH, (Object)Direction.WEST, (Object)WEST, (Object)Direction.UP, (Object)UP, (Object)Direction.DOWN, (Object)DOWN)));
    private final Function<BlockState, VoxelShape> shapes;

    protected PipeBlock(float f, BlockBehaviour.Properties properties) {
        super(properties);
        this.shapes = this.makeShapes(f);
    }

    protected abstract MapCodec<? extends PipeBlock> codec();

    private Function<BlockState, VoxelShape> makeShapes(float f) {
        VoxelShape voxelShape = Block.cube(f);
        Map<Direction, VoxelShape> map = Shapes.rotateAll(Block.boxZ(f, 0.0, 8.0));
        return this.getShapeForEachState(blockState -> {
            VoxelShape voxelShape2 = voxelShape;
            for (Map.Entry<Direction, BooleanProperty> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                if (!((Boolean)blockState.getValue(entry.getValue())).booleanValue()) continue;
                voxelShape2 = Shapes.or((VoxelShape)map.get(entry.getKey()), voxelShape2);
            }
            return voxelShape2;
        });
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        return false;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return this.shapes.apply(blockState);
    }
}

