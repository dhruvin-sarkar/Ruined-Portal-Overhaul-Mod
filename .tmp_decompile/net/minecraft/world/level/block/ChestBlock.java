/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.floats.Float2FloatFunction
 *  java.lang.MatchException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ChestBlock
extends AbstractChestBlock<ChestBlockEntity>
implements SimpleWaterloggedBlock {
    public static final MapCodec<ChestBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound").forGetter(ChestBlock::getOpenChestSound), (App)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound").forGetter(ChestBlock::getCloseChestSound), ChestBlock.propertiesCodec()).apply((Applicative)instance, (soundEvent, soundEvent2, properties) -> new ChestBlock(() -> BlockEntityType.CHEST, (SoundEvent)((Object)((Object)soundEvent)), (SoundEvent)((Object)((Object)soundEvent2)), (BlockBehaviour.Properties)properties)));
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final int EVENT_SET_OPEN_COUNT = 1;
    private static final VoxelShape SHAPE = Block.column(14.0, 0.0, 14.0);
    private static final Map<Direction, VoxelShape> HALF_SHAPES = Shapes.rotateHorizontal(Block.boxZ(14.0, 0.0, 14.0, 0.0, 15.0));
    private final SoundEvent openSound;
    private final SoundEvent closeSound;
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>> CHEST_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Container>>(){

        @Override
        public Optional<Container> acceptDouble(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
            return Optional.of(new CompoundContainer(chestBlockEntity, chestBlockEntity2));
        }

        @Override
        public Optional<Container> acceptSingle(ChestBlockEntity chestBlockEntity) {
            return Optional.of(chestBlockEntity);
        }

        @Override
        public Optional<Container> acceptNone() {
            return Optional.empty();
        }

        @Override
        public /* synthetic */ Object acceptNone() {
            return this.acceptNone();
        }
    };
    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<MenuProvider>>(){

        @Override
        public Optional<MenuProvider> acceptDouble(final ChestBlockEntity chestBlockEntity, final ChestBlockEntity chestBlockEntity2) {
            final CompoundContainer container = new CompoundContainer(chestBlockEntity, chestBlockEntity2);
            return Optional.of(new MenuProvider(){

                @Override
                public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                    if (chestBlockEntity.canOpen(player) && chestBlockEntity2.canOpen(player)) {
                        chestBlockEntity.unpackLootTable(inventory.player);
                        chestBlockEntity2.unpackLootTable(inventory.player);
                        return ChestMenu.sixRows(i, inventory, container);
                    }
                    Direction direction = ChestBlock.getConnectedDirection(chestBlockEntity.getBlockState());
                    Vec3 vec3 = chestBlockEntity.getBlockPos().getCenter();
                    Vec3 vec32 = vec3.add((double)direction.getStepX() / 2.0, 0.0, (double)direction.getStepZ() / 2.0);
                    BaseContainerBlockEntity.sendChestLockedNotifications(vec32, player, this.getDisplayName());
                    return null;
                }

                @Override
                public Component getDisplayName() {
                    if (chestBlockEntity.hasCustomName()) {
                        return chestBlockEntity.getDisplayName();
                    }
                    if (chestBlockEntity2.hasCustomName()) {
                        return chestBlockEntity2.getDisplayName();
                    }
                    return Component.translatable("container.chestDouble");
                }
            });
        }

        @Override
        public Optional<MenuProvider> acceptSingle(ChestBlockEntity chestBlockEntity) {
            return Optional.of(chestBlockEntity);
        }

        @Override
        public Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }

        @Override
        public /* synthetic */ Object acceptNone() {
            return this.acceptNone();
        }
    };

    @Override
    public MapCodec<? extends ChestBlock> codec() {
        return CODEC;
    }

    protected ChestBlock(Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, SoundEvent soundEvent, SoundEvent soundEvent2, BlockBehaviour.Properties properties) {
        super(properties, supplier);
        this.openSound = soundEvent;
        this.closeSound = soundEvent2;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(TYPE, ChestType.SINGLE)).setValue(WATERLOGGED, false));
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState blockState) {
        ChestType chestType = blockState.getValue(TYPE);
        if (chestType == ChestType.SINGLE) {
            return DoubleBlockCombiner.BlockType.SINGLE;
        }
        if (chestType == ChestType.RIGHT) {
            return DoubleBlockCombiner.BlockType.FIRST;
        }
        return DoubleBlockCombiner.BlockType.SECOND;
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        if (this.chestCanConnectTo(blockState2) && direction.getAxis().isHorizontal()) {
            ChestType chestType = blockState2.getValue(TYPE);
            if (blockState.getValue(TYPE) == ChestType.SINGLE && chestType != ChestType.SINGLE && blockState.getValue(FACING) == blockState2.getValue(FACING) && ChestBlock.getConnectedDirection(blockState2) == direction.getOpposite()) {
                return (BlockState)blockState.setValue(TYPE, chestType.getOpposite());
            }
        } else if (ChestBlock.getConnectedDirection(blockState) == direction) {
            return (BlockState)blockState.setValue(TYPE, ChestType.SINGLE);
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    public boolean chestCanConnectTo(BlockState blockState) {
        return blockState.is(this);
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return switch (blockState.getValue(TYPE)) {
            default -> throw new MatchException(null, null);
            case ChestType.SINGLE -> SHAPE;
            case ChestType.LEFT, ChestType.RIGHT -> HALF_SHAPES.get(ChestBlock.getConnectedDirection(blockState));
        };
    }

    public static Direction getConnectedDirection(BlockState blockState) {
        Direction direction = blockState.getValue(FACING);
        return blockState.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
    }

    public static BlockPos getConnectedBlockPos(BlockPos blockPos, BlockState blockState) {
        Direction direction = ChestBlock.getConnectedDirection(blockState);
        return blockPos.relative(direction);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction3;
        ChestType chestType = ChestType.SINGLE;
        Direction direction = blockPlaceContext.getHorizontalDirection().getOpposite();
        FluidState fluidState = blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos());
        boolean bl = blockPlaceContext.isSecondaryUseActive();
        Direction direction2 = blockPlaceContext.getClickedFace();
        if (direction2.getAxis().isHorizontal() && bl && (direction3 = this.candidatePartnerFacing(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), direction2.getOpposite())) != null && direction3.getAxis() != direction2.getAxis()) {
            direction = direction3;
            ChestType chestType2 = chestType = direction.getCounterClockWise() == direction2.getOpposite() ? ChestType.RIGHT : ChestType.LEFT;
        }
        if (chestType == ChestType.SINGLE && !bl) {
            chestType = this.getChestType(blockPlaceContext.getLevel(), blockPlaceContext.getClickedPos(), direction);
        }
        return (BlockState)((BlockState)((BlockState)this.defaultBlockState().setValue(FACING, direction)).setValue(TYPE, chestType)).setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    protected ChestType getChestType(Level level, BlockPos blockPos, Direction direction) {
        if (direction == this.candidatePartnerFacing(level, blockPos, direction.getClockWise())) {
            return ChestType.LEFT;
        }
        if (direction == this.candidatePartnerFacing(level, blockPos, direction.getCounterClockWise())) {
            return ChestType.RIGHT;
        }
        return ChestType.SINGLE;
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    private @Nullable Direction candidatePartnerFacing(Level level, BlockPos blockPos, Direction direction) {
        BlockState blockState = level.getBlockState(blockPos.relative(direction));
        return this.chestCanConnectTo(blockState) && blockState.getValue(TYPE) == ChestType.SINGLE ? blockState.getValue(FACING) : null;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, boolean bl) {
        Containers.updateNeighboursAfterDestroy(blockState, serverLevel, blockPos);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            MenuProvider menuProvider = this.getMenuProvider(blockState, level, blockPos);
            if (menuProvider != null) {
                player.openMenu(menuProvider);
                player.awardStat(this.getOpenChestStat());
                PiglinAi.angerNearbyPiglins(serverLevel, player, true);
            }
        }
        return InteractionResult.SUCCESS;
    }

    protected Stat<Identifier> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    public BlockEntityType<? extends ChestBlockEntity> blockEntityType() {
        return (BlockEntityType)this.blockEntityType.get();
    }

    public static @Nullable Container getContainer(ChestBlock chestBlock, BlockState blockState, Level level, BlockPos blockPos, boolean bl) {
        return chestBlock.combine(blockState, level, blockPos, bl).apply(CHEST_COMBINER).orElse(null);
    }

    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState blockState, Level level, BlockPos blockPos2, boolean bl) {
        BiPredicate<LevelAccessor, BlockPos> biPredicate = bl ? (levelAccessor, blockPos) -> false : ChestBlock::isChestBlockedAt;
        return DoubleBlockCombiner.combineWithNeigbour((BlockEntityType)this.blockEntityType.get(), ChestBlock::getBlockType, ChestBlock::getConnectedDirection, FACING, blockState, level, blockPos2, biPredicate);
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return this.combine(blockState, level, blockPos, false).apply(MENU_PROVIDER_COMBINER).orElse(null);
    }

    public static DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction> opennessCombiner(final LidBlockEntity lidBlockEntity) {
        return new DoubleBlockCombiner.Combiner<ChestBlockEntity, Float2FloatFunction>(){

            @Override
            public Float2FloatFunction acceptDouble(ChestBlockEntity chestBlockEntity, ChestBlockEntity chestBlockEntity2) {
                return f -> Math.max(chestBlockEntity.getOpenNess(f), chestBlockEntity2.getOpenNess(f));
            }

            @Override
            public Float2FloatFunction acceptSingle(ChestBlockEntity chestBlockEntity) {
                return chestBlockEntity::getOpenNess;
            }

            @Override
            public Float2FloatFunction acceptNone() {
                return lidBlockEntity::getOpenNess;
            }

            @Override
            public /* synthetic */ Object acceptNone() {
                return this.acceptNone();
            }
        };
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new ChestBlockEntity(blockPos, blockState);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return level.isClientSide() ? ChestBlock.createTickerHelper(blockEntityType, this.blockEntityType(), ChestBlockEntity::lidAnimateTick) : null;
    }

    public static boolean isChestBlockedAt(LevelAccessor levelAccessor, BlockPos blockPos) {
        return ChestBlock.isBlockedChestByBlock(levelAccessor, blockPos) || ChestBlock.isCatSittingOnChest(levelAccessor, blockPos);
    }

    private static boolean isBlockedChestByBlock(BlockGetter blockGetter, BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.above();
        return blockGetter.getBlockState(blockPos2).isRedstoneConductor(blockGetter, blockPos2);
    }

    private static boolean isCatSittingOnChest(LevelAccessor levelAccessor, BlockPos blockPos) {
        List<Cat> list = levelAccessor.getEntitiesOfClass(Cat.class, new AABB(blockPos.getX(), blockPos.getY() + 1, blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 2, blockPos.getZ() + 1));
        if (!list.isEmpty()) {
            for (Cat cat : list) {
                if (!cat.isInSittingPose()) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(ChestBlock.getContainer(this, blockState, level, blockPos, false));
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
        builder.add(FACING, TYPE, WATERLOGGED);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockEntity).recheckOpen();
        }
    }

    public SoundEvent getOpenChestSound() {
        return this.openSound;
    }

    public SoundEvent getCloseChestSound() {
        return this.closeSound;
    }
}

