/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EyeblossomBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FlowerPotBlock
extends Block {
    public static final MapCodec<FlowerPotBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("potted").forGetter(flowerPotBlock -> flowerPotBlock.potted), FlowerPotBlock.propertiesCodec()).apply((Applicative)instance, FlowerPotBlock::new));
    private static final Map<Block, Block> POTTED_BY_CONTENT = Maps.newHashMap();
    private static final VoxelShape SHAPE = Block.column(6.0, 0.0, 6.0);
    private final Block potted;

    public MapCodec<FlowerPotBlock> codec() {
        return CODEC;
    }

    public FlowerPotBlock(Block block, BlockBehaviour.Properties properties) {
        super(properties);
        this.potted = block;
        POTTED_BY_CONTENT.put(block, this);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        Block block;
        Item item = itemStack.getItem();
        if (item instanceof BlockItem) {
            BlockItem blockItem = (BlockItem)item;
            block = POTTED_BY_CONTENT.getOrDefault(blockItem.getBlock(), Blocks.AIR);
        } else {
            block = Blocks.AIR;
        }
        BlockState blockState2 = block.defaultBlockState();
        if (blockState2.isAir()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (!this.isEmpty()) {
            return InteractionResult.CONSUME;
        }
        level.setBlock(blockPos, blockState2, 3);
        level.gameEvent((Entity)player, GameEvent.BLOCK_CHANGE, blockPos);
        player.awardStat(Stats.POT_FLOWER);
        itemStack.consume(1, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (this.isEmpty()) {
            return InteractionResult.CONSUME;
        }
        ItemStack itemStack = new ItemStack(this.potted);
        if (!player.addItem(itemStack)) {
            player.drop(itemStack, false);
        }
        level.setBlock(blockPos, Blocks.FLOWER_POT.defaultBlockState(), 3);
        level.gameEvent((Entity)player, GameEvent.BLOCK_CHANGE, blockPos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        if (this.isEmpty()) {
            return super.getCloneItemStack(levelReader, blockPos, blockState, bl);
        }
        return new ItemStack(this.potted);
    }

    private boolean isEmpty() {
        return this.potted == Blocks.AIR;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelReader, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    public Block getPotted() {
        return this.potted;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.is(Blocks.POTTED_OPEN_EYEBLOSSOM) || blockState.is(Blocks.POTTED_CLOSED_EYEBLOSSOM);
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        boolean bl2;
        boolean bl;
        if (this.isRandomlyTicking(blockState) && (bl = this.potted == Blocks.OPEN_EYEBLOSSOM) != (bl2 = serverLevel.environmentAttributes().getValue(EnvironmentAttributes.EYEBLOSSOM_OPEN, blockPos).toBoolean(bl))) {
            serverLevel.setBlock(blockPos, this.opposite(blockState), 3);
            EyeblossomBlock.Type type = EyeblossomBlock.Type.fromBoolean(bl).transform();
            type.spawnTransformParticle(serverLevel, blockPos, randomSource);
            serverLevel.playSound(null, blockPos, type.longSwitchSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        super.randomTick(blockState, serverLevel, blockPos, randomSource);
    }

    public BlockState opposite(BlockState blockState) {
        if (blockState.is(Blocks.POTTED_OPEN_EYEBLOSSOM)) {
            return Blocks.POTTED_CLOSED_EYEBLOSSOM.defaultBlockState();
        }
        if (blockState.is(Blocks.POTTED_CLOSED_EYEBLOSSOM)) {
            return Blocks.POTTED_OPEN_EYEBLOSSOM.defaultBlockState();
        }
        return blockState;
    }
}

