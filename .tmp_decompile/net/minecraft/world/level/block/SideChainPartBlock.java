/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.lang.invoke.LambdaMetafactory;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SideChainPart;

public interface SideChainPartBlock {
    public SideChainPart getSideChainPart(BlockState var1);

    public BlockState setSideChainPart(BlockState var1, SideChainPart var2);

    public Direction getFacing(BlockState var1);

    public boolean isConnectable(BlockState var1);

    public int getMaxChainLength();

    default public List<BlockPos> getAllBlocksConnectedTo(LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (!this.isConnectable(blockState)) {
            return List.of();
        }
        Neighbors neighbors = this.getNeighbors(levelAccessor, blockPos, this.getFacing(blockState));
        LinkedList<BlockPos> list = new LinkedList<BlockPos>();
        list.add(blockPos);
        this.addBlocksConnectingTowards(neighbors::left, SideChainPart.LEFT, (Consumer<BlockPos>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)V, addFirst(java.lang.Object ), (Lnet/minecraft/core/BlockPos;)V)(list));
        this.addBlocksConnectingTowards(neighbors::right, SideChainPart.RIGHT, (Consumer<BlockPos>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)V, addLast(java.lang.Object ), (Lnet/minecraft/core/BlockPos;)V)(list));
        return list;
    }

    private void addBlocksConnectingTowards(IntFunction<Neighbor> intFunction, SideChainPart sideChainPart, Consumer<BlockPos> consumer) {
        for (int i = 1; i < this.getMaxChainLength(); ++i) {
            Neighbor neighbor = intFunction.apply(i);
            if (neighbor.connectsTowards(sideChainPart)) {
                consumer.accept(neighbor.pos());
            }
            if (neighbor.isUnconnectableOrChainEnd()) break;
        }
    }

    default public void updateNeighborsAfterPoweringDown(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        Neighbors neighbors = this.getNeighbors(levelAccessor, blockPos, this.getFacing(blockState));
        neighbors.left().disconnectFromRight();
        neighbors.right().disconnectFromLeft();
    }

    default public void updateSelfAndNeighborsOnPoweringUp(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, BlockState blockState2) {
        if (!this.isConnectable(blockState)) {
            return;
        }
        if (this.isBeingUpdatedByNeighbor(blockState, blockState2)) {
            return;
        }
        Neighbors neighbors = this.getNeighbors(levelAccessor, blockPos, this.getFacing(blockState));
        SideChainPart sideChainPart = SideChainPart.UNCONNECTED;
        int i = neighbors.left().isConnectable() ? this.getAllBlocksConnectedTo(levelAccessor, neighbors.left().pos()).size() : 0;
        int j = neighbors.right().isConnectable() ? this.getAllBlocksConnectedTo(levelAccessor, neighbors.right().pos()).size() : 0;
        int k = 1;
        if (this.canConnect(i, k)) {
            sideChainPart = sideChainPart.whenConnectedToTheLeft();
            neighbors.left().connectToTheRight();
            k += i;
        }
        if (this.canConnect(j, k)) {
            sideChainPart = sideChainPart.whenConnectedToTheRight();
            neighbors.right().connectToTheLeft();
        }
        this.setPart(levelAccessor, blockPos, sideChainPart);
    }

    private boolean canConnect(int i, int j) {
        return i > 0 && j + i <= this.getMaxChainLength();
    }

    private boolean isBeingUpdatedByNeighbor(BlockState blockState, BlockState blockState2) {
        boolean bl = this.getSideChainPart(blockState).isConnected();
        boolean bl2 = this.isConnectable(blockState2) && this.getSideChainPart(blockState2).isConnected();
        return bl || bl2;
    }

    private Neighbors getNeighbors(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction) {
        return new Neighbors(this, levelAccessor, direction, blockPos, new HashMap<BlockPos, Neighbor>());
    }

    default public void setPart(LevelAccessor levelAccessor, BlockPos blockPos, SideChainPart sideChainPart) {
        BlockState blockState = levelAccessor.getBlockState(blockPos);
        if (this.getSideChainPart(blockState) != sideChainPart) {
            levelAccessor.setBlock(blockPos, this.setSideChainPart(blockState, sideChainPart), 3);
        }
    }

    public record Neighbors(SideChainPartBlock block, LevelAccessor level, Direction facing, BlockPos center, Map<BlockPos, Neighbor> cache) {
        private boolean isConnectableToThisBlock(BlockState blockState) {
            return this.block.isConnectable(blockState) && this.block.getFacing(blockState) == this.facing;
        }

        private Neighbor createNewNeighbor(BlockPos blockPos) {
            BlockState blockState = this.level.getBlockState(blockPos);
            SideChainPart sideChainPart = this.isConnectableToThisBlock(blockState) ? this.block.getSideChainPart(blockState) : null;
            return sideChainPart == null ? new EmptyNeighbor(blockPos) : new SideChainNeighbor(this.level, this.block, blockPos, sideChainPart);
        }

        private Neighbor getOrCreateNeighbor(Direction direction, Integer integer) {
            return this.cache.computeIfAbsent(this.center.relative(direction, (int)integer), this::createNewNeighbor);
        }

        public Neighbor left(int i) {
            return this.getOrCreateNeighbor(this.facing.getClockWise(), i);
        }

        public Neighbor right(int i) {
            return this.getOrCreateNeighbor(this.facing.getCounterClockWise(), i);
        }

        public Neighbor left() {
            return this.left(1);
        }

        public Neighbor right() {
            return this.right(1);
        }
    }

    public static sealed interface Neighbor
    permits EmptyNeighbor, SideChainNeighbor {
        public BlockPos pos();

        public boolean isConnectable();

        public boolean isUnconnectableOrChainEnd();

        public boolean connectsTowards(SideChainPart var1);

        default public void connectToTheRight() {
        }

        default public void connectToTheLeft() {
        }

        default public void disconnectFromRight() {
        }

        default public void disconnectFromLeft() {
        }
    }

    public record SideChainNeighbor(LevelAccessor level, SideChainPartBlock block, BlockPos pos, SideChainPart part) implements Neighbor
    {
        @Override
        public boolean isConnectable() {
            return true;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return this.part.isChainEnd();
        }

        @Override
        public boolean connectsTowards(SideChainPart sideChainPart) {
            return this.part.isConnectionTowards(sideChainPart);
        }

        @Override
        public void connectToTheRight() {
            this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheRight());
        }

        @Override
        public void connectToTheLeft() {
            this.block.setPart(this.level, this.pos, this.part.whenConnectedToTheLeft());
        }

        @Override
        public void disconnectFromRight() {
            this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheRight());
        }

        @Override
        public void disconnectFromLeft() {
            this.block.setPart(this.level, this.pos, this.part.whenDisconnectedFromTheLeft());
        }
    }

    public record EmptyNeighbor(BlockPos pos) implements Neighbor
    {
        @Override
        public boolean isConnectable() {
            return false;
        }

        @Override
        public boolean isUnconnectableOrChainEnd() {
            return true;
        }

        @Override
        public boolean connectsTowards(SideChainPart sideChainPart) {
            return false;
        }
    }
}

