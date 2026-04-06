/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SweetBerryBushBlock
extends VegetationBlock
implements BonemealableBlock {
    public static final MapCodec<SweetBerryBushBlock> CODEC = SweetBerryBushBlock.simpleCodec(SweetBerryBushBlock::new);
    private static final float HURT_SPEED_THRESHOLD = 0.003f;
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    private static final VoxelShape SHAPE_SAPLING = Block.column(10.0, 0.0, 8.0);
    private static final VoxelShape SHAPE_GROWING = Block.column(14.0, 0.0, 16.0);

    public MapCodec<SweetBerryBushBlock> codec() {
        return CODEC;
    }

    public SweetBerryBushBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(AGE, 0));
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState, boolean bl) {
        return new ItemStack(Items.SWEET_BERRIES);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return switch (blockState.getValue(AGE)) {
            case 0 -> SHAPE_SAPLING;
            case 3 -> Shapes.block();
            default -> SHAPE_GROWING;
        };
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(AGE) < 3;
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        int i = blockState.getValue(AGE);
        if (i < 3 && randomSource.nextInt(5) == 0 && serverLevel.getRawBrightness(blockPos.above(), 0) >= 9) {
            BlockState blockState2 = (BlockState)blockState.setValue(AGE, i + 1);
            serverLevel.setBlock(blockPos, blockState2, 2);
            serverLevel.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(blockState2));
        }
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        Vec3 vec3;
        ServerLevel serverLevel;
        block7: {
            block6: {
                if (!(entity instanceof LivingEntity) || entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) {
                    return;
                }
                entity.makeStuckInBlock(blockState, new Vec3(0.8f, 0.75, 0.8f));
                if (!(level instanceof ServerLevel)) break block6;
                serverLevel = (ServerLevel)level;
                if (blockState.getValue(AGE) != 0) break block7;
            }
            return;
        }
        Vec3 vec32 = vec3 = entity.isClientAuthoritative() ? entity.getKnownMovement() : entity.oldPosition().subtract(entity.position());
        if (vec3.horizontalDistanceSqr() > 0.0) {
            double d = Math.abs(vec3.x());
            double e = Math.abs(vec3.z());
            if (d >= (double)0.003f || e >= (double)0.003f) {
                entity.hurtServer(serverLevel, level.damageSources().sweetBerryBush(), 1.0f);
            }
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        boolean bl;
        int i = blockState.getValue(AGE);
        boolean bl2 = bl = i == 3;
        if (!bl && itemStack.is(Items.BONE_MEAL)) {
            return InteractionResult.PASS;
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (blockState.getValue(AGE) > 1) {
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel2 = (ServerLevel)level;
                Block.dropFromBlockInteractLootTable(serverLevel2, BuiltInLootTables.HARVEST_SWEET_BERRY_BUSH, blockState, level.getBlockEntity(blockPos), null, player, (serverLevel, itemStack) -> Block.popResource((Level)serverLevel, blockPos, itemStack));
                serverLevel2.playSound(null, blockPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 0.8f + serverLevel2.random.nextFloat() * 0.4f);
                BlockState blockState2 = (BlockState)blockState.setValue(AGE, 1);
                serverLevel2.setBlock(blockPos, blockState2, 2);
                serverLevel2.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Context.of(player, blockState2));
            }
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(blockState, level, blockPos, player, blockHitResult);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return blockState.getValue(AGE) < 3;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel serverLevel, RandomSource randomSource, BlockPos blockPos, BlockState blockState) {
        int i = Math.min(3, blockState.getValue(AGE) + 1);
        serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(AGE, i), 2);
    }
}

