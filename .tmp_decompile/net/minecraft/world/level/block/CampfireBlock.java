/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class CampfireBlock
extends BaseEntityBlock
implements SimpleWaterloggedBlock {
    public static final MapCodec<CampfireBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("spawn_particles").forGetter(campfireBlock -> campfireBlock.spawnParticles), (App)Codec.intRange((int)0, (int)1000).fieldOf("fire_damage").forGetter(campfireBlock -> campfireBlock.fireDamage), CampfireBlock.propertiesCodec()).apply((Applicative)instance, CampfireBlock::new));
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty SIGNAL_FIRE = BlockStateProperties.SIGNAL_FIRE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 7.0);
    private static final VoxelShape SHAPE_VIRTUAL_POST = Block.column(4.0, 0.0, 16.0);
    private static final int SMOKE_DISTANCE = 5;
    private final boolean spawnParticles;
    private final int fireDamage;

    public MapCodec<CampfireBlock> codec() {
        return CODEC;
    }

    public CampfireBlock(boolean bl, int i, BlockBehaviour.Properties properties) {
        super(properties);
        this.spawnParticles = bl;
        this.fireDamage = i;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(LIT, true)).setValue(SIGNAL_FIRE, false)).setValue(WATERLOGGED, false)).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CampfireBlockEntity) {
            CampfireBlockEntity campfireBlockEntity = (CampfireBlockEntity)blockEntity;
            ItemStack itemStack2 = player.getItemInHand(interactionHand);
            if (level.recipeAccess().propertySet(RecipePropertySet.CAMPFIRE_INPUT).test(itemStack2)) {
                ServerLevel serverLevel;
                if (level instanceof ServerLevel && campfireBlockEntity.placeFood(serverLevel = (ServerLevel)level, player, itemStack2)) {
                    player.awardStat(Stats.INTERACT_WITH_CAMPFIRE);
                    return InteractionResult.SUCCESS_SERVER;
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl) {
        if (blockState.getValue(LIT).booleanValue() && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().campfire(), this.fireDamage);
        }
        super.entityInside(blockState, level, blockPos, entity, insideBlockEffectApplier, bl);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        BlockPos blockPos;
        Level levelAccessor = blockPlaceContext.getLevel();
        boolean bl = levelAccessor.getFluidState(blockPos = blockPlaceContext.getClickedPos()).getType() == Fluids.WATER;
        return (BlockState)((BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(WATERLOGGED, bl)).setValue(SIGNAL_FIRE, this.isSmokeSource(levelAccessor.getBlockState(blockPos.below())))).setValue(LIT, !bl)).setValue(FACING, blockPlaceContext.getHorizontalDirection());
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (direction == Direction.DOWN) {
            return (BlockState)blockState.setValue(SIGNAL_FIRE, this.isSmokeSource(blockState2));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    private boolean isSmokeSource(BlockState blockState) {
        return blockState.is(Blocks.HAY_BLOCK);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (!blockState.getValue(LIT).booleanValue()) {
            return;
        }
        if (randomSource.nextInt(10) == 0) {
            level.playLocalSound((double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5f + randomSource.nextFloat(), randomSource.nextFloat() * 0.7f + 0.6f, false);
        }
        if (this.spawnParticles && randomSource.nextInt(5) == 0) {
            for (int i = 0; i < randomSource.nextInt(1) + 1; ++i) {
                level.addParticle(ParticleTypes.LAVA, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5, randomSource.nextFloat() / 2.0f, 5.0E-5, randomSource.nextFloat() / 2.0f);
            }
        }
    }

    public static void dowse(@Nullable Entity entity, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (levelAccessor.isClientSide()) {
            for (int i = 0; i < 20; ++i) {
                CampfireBlock.makeParticles((Level)levelAccessor, blockPos, blockState.getValue(SIGNAL_FIRE), true);
            }
        }
        levelAccessor.gameEvent(entity, GameEvent.BLOCK_CHANGE, blockPos);
    }

    @Override
    public boolean placeLiquid(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!blockState.getValue(BlockStateProperties.WATERLOGGED).booleanValue() && fluidState.getType() == Fluids.WATER) {
            boolean bl = blockState.getValue(LIT);
            if (bl) {
                if (!levelAccessor.isClientSide()) {
                    levelAccessor.playSound(null, blockPos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                CampfireBlock.dowse(null, levelAccessor, blockPos, blockState);
            }
            levelAccessor.setBlock(blockPos, (BlockState)((BlockState)blockState.setValue(WATERLOGGED, true)).setValue(LIT, false), 3);
            levelAccessor.scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(levelAccessor));
            return true;
        }
        return false;
    }

    @Override
    protected void onProjectileHit(Level level, BlockState blockState, BlockHitResult blockHitResult, Projectile projectile) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (projectile.isOnFire() && projectile.mayInteract(serverLevel, blockPos) && !blockState.getValue(LIT).booleanValue() && !blockState.getValue(WATERLOGGED).booleanValue()) {
                level.setBlock(blockPos, (BlockState)blockState.setValue(BlockStateProperties.LIT, true), 11);
            }
        }
    }

    public static void makeParticles(Level level, BlockPos blockPos, boolean bl, boolean bl2) {
        RandomSource randomSource = level.getRandom();
        SimpleParticleType simpleParticleType = bl ? ParticleTypes.CAMPFIRE_SIGNAL_SMOKE : ParticleTypes.CAMPFIRE_COSY_SMOKE;
        level.addAlwaysVisibleParticle(simpleParticleType, true, (double)blockPos.getX() + 0.5 + randomSource.nextDouble() / 3.0 * (double)(randomSource.nextBoolean() ? 1 : -1), (double)blockPos.getY() + randomSource.nextDouble() + randomSource.nextDouble(), (double)blockPos.getZ() + 0.5 + randomSource.nextDouble() / 3.0 * (double)(randomSource.nextBoolean() ? 1 : -1), 0.0, 0.07, 0.0);
        if (bl2) {
            level.addParticle(ParticleTypes.SMOKE, (double)blockPos.getX() + 0.5 + randomSource.nextDouble() / 4.0 * (double)(randomSource.nextBoolean() ? 1 : -1), (double)blockPos.getY() + 0.4, (double)blockPos.getZ() + 0.5 + randomSource.nextDouble() / 4.0 * (double)(randomSource.nextBoolean() ? 1 : -1), 0.0, 0.005, 0.0);
        }
    }

    public static boolean isSmokeyPos(Level level, BlockPos blockPos) {
        for (int i = 1; i <= 5; ++i) {
            BlockPos blockPos2 = blockPos.below(i);
            BlockState blockState = level.getBlockState(blockPos2);
            if (CampfireBlock.isLitCampfire(blockState)) {
                return true;
            }
            boolean bl = Shapes.joinIsNotEmpty(SHAPE_VIRTUAL_POST, blockState.getCollisionShape(level, blockPos, CollisionContext.empty()), BooleanOp.AND);
            if (!bl) continue;
            BlockState blockState2 = level.getBlockState(blockPos2.below());
            return CampfireBlock.isLitCampfire(blockState2);
        }
        return false;
    }

    public static boolean isLitCampfire(BlockState blockState) {
        return blockState.hasProperty(LIT) && blockState.is(BlockTags.CAMPFIRES) && blockState.getValue(LIT) != false;
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT, SIGNAL_FIRE, WATERLOGGED, FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CampfireBlockEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level2, BlockState blockState2, BlockEntityType<T> blockEntityType) {
        if (level2 instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level2;
            if (blockState2.getValue(LIT).booleanValue()) {
                RecipeManager.CachedCheck cachedCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
                return CampfireBlock.createTickerHelper(blockEntityType, BlockEntityType.CAMPFIRE, (level, blockPos, blockState, campfireBlockEntity) -> CampfireBlockEntity.cookTick(serverLevel, blockPos, blockState, campfireBlockEntity, cachedCheck));
            }
            return CampfireBlock.createTickerHelper(blockEntityType, BlockEntityType.CAMPFIRE, CampfireBlockEntity::cooldownTick);
        }
        if (blockState2.getValue(LIT).booleanValue()) {
            return CampfireBlock.createTickerHelper(blockEntityType, BlockEntityType.CAMPFIRE, CampfireBlockEntity::particleTick);
        }
        return null;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    public static boolean canLight(BlockState blockState) {
        return blockState.is(BlockTags.CAMPFIRES, blockStateBase -> blockStateBase.hasProperty(WATERLOGGED) && blockStateBase.hasProperty(LIT)) && blockState.getValue(WATERLOGGED) == false && blockState.getValue(LIT) == false;
    }
}

