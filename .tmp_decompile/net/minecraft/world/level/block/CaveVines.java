/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CaveVines {
    public static final VoxelShape SHAPE = Block.column(14.0, 0.0, 16.0);
    public static final BooleanProperty BERRIES = BlockStateProperties.BERRIES;

    public static InteractionResult use(Entity entity, BlockState blockState, Level level, BlockPos blockPos) {
        if (blockState.getValue(BERRIES).booleanValue()) {
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel2 = (ServerLevel)level;
                Block.dropFromBlockInteractLootTable(serverLevel2, BuiltInLootTables.HARVEST_CAVE_VINE, blockState, level.getBlockEntity(blockPos), null, entity, (serverLevel, itemStack) -> Block.popResource((Level)serverLevel, blockPos, itemStack));
                float f = Mth.randomBetween(serverLevel2.random, 0.8f, 1.2f);
                serverLevel2.playSound(null, blockPos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, f);
                BlockState blockState2 = (BlockState)blockState.setValue(BERRIES, false);
                serverLevel2.setBlock(blockPos, blockState2, 2);
                serverLevel2.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(entity, blockState2));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static boolean hasGlowBerries(BlockState blockState) {
        return blockState.hasProperty(BERRIES) && blockState.getValue(BERRIES) != false;
    }

    public static ToIntFunction<BlockState> emission(int i) {
        return blockState -> blockState.getValue(BlockStateProperties.BERRIES) != false ? i : 0;
    }
}

