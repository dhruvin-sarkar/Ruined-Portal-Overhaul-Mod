/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class CommandBlock
extends BaseEntityBlock
implements GameMasterBlock {
    public static final MapCodec<CommandBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("automatic").forGetter(commandBlock -> commandBlock.automatic), CommandBlock.propertiesCodec()).apply((Applicative)instance, CommandBlock::new));
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final EnumProperty<Direction> FACING = DirectionalBlock.FACING;
    public static final BooleanProperty CONDITIONAL = BlockStateProperties.CONDITIONAL;
    private final boolean automatic;

    public MapCodec<CommandBlock> codec() {
        return CODEC;
    }

    public CommandBlock(boolean bl, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(CONDITIONAL, false));
        this.automatic = bl;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        CommandBlockEntity commandBlockEntity = new CommandBlockEntity(blockPos, blockState);
        commandBlockEntity.setAutomatic(this.automatic);
        return commandBlockEntity;
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (level.isClientSide()) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity) {
            CommandBlockEntity commandBlockEntity = (CommandBlockEntity)blockEntity;
            this.setPoweredAndUpdate(level, blockPos, commandBlockEntity, level.hasNeighborSignal(blockPos));
        }
    }

    private void setPoweredAndUpdate(Level level, BlockPos blockPos, CommandBlockEntity commandBlockEntity, boolean bl) {
        boolean bl2 = commandBlockEntity.isPowered();
        if (bl == bl2) {
            return;
        }
        commandBlockEntity.setPowered(bl);
        if (bl) {
            if (commandBlockEntity.isAutomatic() || commandBlockEntity.getMode() == CommandBlockEntity.Mode.SEQUENCE) {
                return;
            }
            commandBlockEntity.markConditionMet();
            level.scheduleTick(blockPos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        BlockEntity blockEntity = serverLevel.getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity) {
            CommandBlockEntity commandBlockEntity = (CommandBlockEntity)blockEntity;
            BaseCommandBlock baseCommandBlock = commandBlockEntity.getCommandBlock();
            boolean bl = !StringUtil.isNullOrEmpty(baseCommandBlock.getCommand());
            CommandBlockEntity.Mode mode = commandBlockEntity.getMode();
            boolean bl2 = commandBlockEntity.wasConditionMet();
            if (mode == CommandBlockEntity.Mode.AUTO) {
                commandBlockEntity.markConditionMet();
                if (bl2) {
                    this.execute(blockState, serverLevel, blockPos, baseCommandBlock, bl);
                } else if (commandBlockEntity.isConditional()) {
                    baseCommandBlock.setSuccessCount(0);
                }
                if (commandBlockEntity.isPowered() || commandBlockEntity.isAutomatic()) {
                    serverLevel.scheduleTick(blockPos, this, 1);
                }
            } else if (mode == CommandBlockEntity.Mode.REDSTONE) {
                if (bl2) {
                    this.execute(blockState, serverLevel, blockPos, baseCommandBlock, bl);
                } else if (commandBlockEntity.isConditional()) {
                    baseCommandBlock.setSuccessCount(0);
                }
            }
            serverLevel.updateNeighbourForOutputSignal(blockPos, this);
        }
    }

    private void execute(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, BaseCommandBlock baseCommandBlock, boolean bl) {
        if (bl) {
            baseCommandBlock.performCommand(serverLevel);
        } else {
            baseCommandBlock.setSuccessCount(0);
        }
        CommandBlock.executeChain(serverLevel, blockPos, blockState.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity && player.canUseGameMasterBlocks()) {
            player.openCommandBlock((CommandBlockEntity)blockEntity);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof CommandBlockEntity) {
            return ((CommandBlockEntity)blockEntity).getCommandBlock().getSuccessCount();
        }
        return 0;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof CommandBlockEntity)) {
            return;
        }
        CommandBlockEntity commandBlockEntity = (CommandBlockEntity)blockEntity;
        BaseCommandBlock baseCommandBlock = commandBlockEntity.getCommandBlock();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (!itemStack.has(DataComponents.BLOCK_ENTITY_DATA)) {
                baseCommandBlock.setTrackOutput(serverLevel.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK));
                commandBlockEntity.setAutomatic(this.automatic);
            }
            boolean bl = level.hasNeighborSignal(blockPos);
            this.setPoweredAndUpdate(level, blockPos, commandBlockEntity, bl);
        }
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
        builder.add(FACING, CONDITIONAL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite());
    }

    private static void executeChain(ServerLevel serverLevel, BlockPos blockPos, Direction direction) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        GameRules gameRules = serverLevel.getGameRules();
        int i = gameRules.get(GameRules.MAX_COMMAND_SEQUENCE_LENGTH);
        while (i-- > 0) {
            CommandBlockEntity commandBlockEntity;
            BlockEntity blockEntity;
            mutableBlockPos.move(direction);
            BlockState blockState = serverLevel.getBlockState(mutableBlockPos);
            Block block = blockState.getBlock();
            if (!blockState.is(Blocks.CHAIN_COMMAND_BLOCK) || !((blockEntity = serverLevel.getBlockEntity(mutableBlockPos)) instanceof CommandBlockEntity) || (commandBlockEntity = (CommandBlockEntity)blockEntity).getMode() != CommandBlockEntity.Mode.SEQUENCE) break;
            if (commandBlockEntity.isPowered() || commandBlockEntity.isAutomatic()) {
                BaseCommandBlock baseCommandBlock = commandBlockEntity.getCommandBlock();
                if (commandBlockEntity.markConditionMet()) {
                    if (!baseCommandBlock.performCommand(serverLevel)) break;
                    serverLevel.updateNeighbourForOutputSignal(mutableBlockPos, block);
                } else if (commandBlockEntity.isConditional()) {
                    baseCommandBlock.setSuccessCount(0);
                }
            }
            direction = blockState.getValue(FACING);
        }
        if (i <= 0) {
            int j = Math.max(gameRules.get(GameRules.MAX_COMMAND_SEQUENCE_LENGTH), 0);
            LOGGER.warn("Command Block chain tried to execute more than {} steps!", (Object)j);
        }
    }
}

