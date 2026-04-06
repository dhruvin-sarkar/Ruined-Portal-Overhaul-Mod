/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.entity;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class CommandBlockEntity
extends BlockEntity {
    private static final boolean DEFAULT_POWERED = false;
    private static final boolean DEFAULT_CONDITION_MET = false;
    private static final boolean DEFAULT_AUTOMATIC = false;
    private boolean powered = false;
    private boolean auto = false;
    private boolean conditionMet = false;
    private final BaseCommandBlock commandBlock = new BaseCommandBlock(){

        @Override
        public void setCommand(String string) {
            super.setCommand(string);
            CommandBlockEntity.this.setChanged();
        }

        @Override
        public void onUpdated(ServerLevel serverLevel) {
            BlockState blockState = serverLevel.getBlockState(CommandBlockEntity.this.worldPosition);
            serverLevel.sendBlockUpdated(CommandBlockEntity.this.worldPosition, blockState, blockState, 3);
        }

        @Override
        public CommandSourceStack createCommandSourceStack(ServerLevel serverLevel, CommandSource commandSource) {
            Direction direction = CommandBlockEntity.this.getBlockState().getValue(CommandBlock.FACING);
            return new CommandSourceStack(commandSource, Vec3.atCenterOf(CommandBlockEntity.this.worldPosition), new Vec2(0.0f, direction.toYRot()), serverLevel, LevelBasedPermissionSet.GAMEMASTER, this.getName().getString(), this.getName(), serverLevel.getServer(), null);
        }

        @Override
        public boolean isValid() {
            return !CommandBlockEntity.this.isRemoved();
        }
    };

    public CommandBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.COMMAND_BLOCK, blockPos, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        this.commandBlock.save(valueOutput);
        valueOutput.putBoolean("powered", this.isPowered());
        valueOutput.putBoolean("conditionMet", this.wasConditionMet());
        valueOutput.putBoolean("auto", this.isAutomatic());
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.commandBlock.load(valueInput);
        this.powered = valueInput.getBooleanOr("powered", false);
        this.conditionMet = valueInput.getBooleanOr("conditionMet", false);
        this.setAutomatic(valueInput.getBooleanOr("auto", false));
    }

    public BaseCommandBlock getCommandBlock() {
        return this.commandBlock;
    }

    public void setPowered(boolean bl) {
        this.powered = bl;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public boolean isAutomatic() {
        return this.auto;
    }

    public void setAutomatic(boolean bl) {
        boolean bl2 = this.auto;
        this.auto = bl;
        if (!bl2 && bl && !this.powered && this.level != null && this.getMode() != Mode.SEQUENCE) {
            this.scheduleTick();
        }
    }

    public void onModeSwitch() {
        Mode mode = this.getMode();
        if (mode == Mode.AUTO && (this.powered || this.auto) && this.level != null) {
            this.scheduleTick();
        }
    }

    private void scheduleTick() {
        Block block = this.getBlockState().getBlock();
        if (block instanceof CommandBlock) {
            this.markConditionMet();
            this.level.scheduleTick(this.worldPosition, block, 1);
        }
    }

    public boolean wasConditionMet() {
        return this.conditionMet;
    }

    public boolean markConditionMet() {
        this.conditionMet = true;
        if (this.isConditional()) {
            BlockEntity blockEntity;
            BlockPos blockPos = this.worldPosition.relative(this.level.getBlockState(this.worldPosition).getValue(CommandBlock.FACING).getOpposite());
            this.conditionMet = this.level.getBlockState(blockPos).getBlock() instanceof CommandBlock ? (blockEntity = this.level.getBlockEntity(blockPos)) instanceof CommandBlockEntity && ((CommandBlockEntity)blockEntity).getCommandBlock().getSuccessCount() > 0 : false;
        }
        return this.conditionMet;
    }

    public Mode getMode() {
        BlockState blockState = this.getBlockState();
        if (blockState.is(Blocks.COMMAND_BLOCK)) {
            return Mode.REDSTONE;
        }
        if (blockState.is(Blocks.REPEATING_COMMAND_BLOCK)) {
            return Mode.AUTO;
        }
        if (blockState.is(Blocks.CHAIN_COMMAND_BLOCK)) {
            return Mode.SEQUENCE;
        }
        return Mode.REDSTONE;
    }

    public boolean isConditional() {
        BlockState blockState = this.level.getBlockState(this.getBlockPos());
        if (blockState.getBlock() instanceof CommandBlock) {
            return blockState.getValue(CommandBlock.CONDITIONAL);
        }
        return false;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        super.applyImplicitComponents(dataComponentGetter);
        this.commandBlock.setCustomName(dataComponentGetter.get(DataComponents.CUSTOM_NAME));
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        builder.set(DataComponents.CUSTOM_NAME, this.commandBlock.getCustomName());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput valueOutput) {
        super.removeComponentsFromTag(valueOutput);
        valueOutput.discard("CustomName");
        valueOutput.discard("conditionMet");
        valueOutput.discard("powered");
    }

    public static enum Mode {
        SEQUENCE,
        AUTO,
        REDSTONE;

    }
}

