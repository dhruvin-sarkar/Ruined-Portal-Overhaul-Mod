/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SignBlockEntity
extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_TEXT_LINE_WIDTH = 90;
    private static final int TEXT_LINE_HEIGHT = 10;
    private static final boolean DEFAULT_IS_WAXED = false;
    private @Nullable UUID playerWhoMayEdit;
    private SignText frontText = this.createDefaultSignText();
    private SignText backText = this.createDefaultSignText();
    private boolean isWaxed = false;

    public SignBlockEntity(BlockPos blockPos, BlockState blockState) {
        this((BlockEntityType)BlockEntityType.SIGN, blockPos, blockState);
    }

    public SignBlockEntity(BlockEntityType blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    protected SignText createDefaultSignText() {
        return new SignText();
    }

    public boolean isFacingFrontText(Player player) {
        Block block = this.getBlockState().getBlock();
        if (block instanceof SignBlock) {
            float g;
            SignBlock signBlock = (SignBlock)block;
            Vec3 vec3 = signBlock.getSignHitboxCenterPosition(this.getBlockState());
            double d = player.getX() - ((double)this.getBlockPos().getX() + vec3.x);
            double e = player.getZ() - ((double)this.getBlockPos().getZ() + vec3.z);
            float f = signBlock.getYRotationDegrees(this.getBlockState());
            return Mth.degreesDifferenceAbs(f, g = (float)(Mth.atan2(e, d) * 57.2957763671875) - 90.0f) <= 90.0f;
        }
        return false;
    }

    public SignText getText(boolean bl) {
        return bl ? this.frontText : this.backText;
    }

    public SignText getFrontText() {
        return this.frontText;
    }

    public SignText getBackText() {
        return this.backText;
    }

    public int getTextLineHeight() {
        return 10;
    }

    public int getMaxTextLineWidth() {
        return 90;
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.store("front_text", SignText.DIRECT_CODEC, this.frontText);
        valueOutput.store("back_text", SignText.DIRECT_CODEC, this.backText);
        valueOutput.putBoolean("is_waxed", this.isWaxed);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.frontText = valueInput.read("front_text", SignText.DIRECT_CODEC).map(this::loadLines).orElseGet(SignText::new);
        this.backText = valueInput.read("back_text", SignText.DIRECT_CODEC).map(this::loadLines).orElseGet(SignText::new);
        this.isWaxed = valueInput.getBooleanOr("is_waxed", false);
    }

    private SignText loadLines(SignText signText) {
        for (int i = 0; i < 4; ++i) {
            Component component = this.loadLine(signText.getMessage(i, false));
            Component component2 = this.loadLine(signText.getMessage(i, true));
            signText = signText.setMessage(i, component, component2);
        }
        return signText;
    }

    private Component loadLine(Component component) {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            try {
                return ComponentUtils.updateForEntity(SignBlockEntity.createCommandSourceStack(null, serverLevel, this.worldPosition), component, null, 0);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }
        return component;
    }

    public void updateSignText(Player player, boolean bl, List<FilteredText> list) {
        if (this.isWaxed() || !player.getUUID().equals(this.getPlayerWhoMayEdit()) || this.level == null) {
            LOGGER.warn("Player {} just tried to change non-editable sign", (Object)player.getPlainTextName());
            return;
        }
        this.updateText(signText -> this.setMessages(player, list, (SignText)signText), bl);
        this.setAllowedPlayerEditor(null);
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean updateText(UnaryOperator<SignText> unaryOperator, boolean bl) {
        SignText signText = this.getText(bl);
        return this.setText((SignText)unaryOperator.apply(signText), bl);
    }

    private SignText setMessages(Player player, List<FilteredText> list, SignText signText) {
        for (int i = 0; i < list.size(); ++i) {
            FilteredText filteredText = list.get(i);
            Style style = signText.getMessage(i, player.isTextFilteringEnabled()).getStyle();
            signText = player.isTextFilteringEnabled() ? signText.setMessage(i, Component.literal(filteredText.filteredOrEmpty()).setStyle(style)) : signText.setMessage(i, Component.literal(filteredText.raw()).setStyle(style), Component.literal(filteredText.filteredOrEmpty()).setStyle(style));
        }
        return signText;
    }

    public boolean setText(SignText signText, boolean bl) {
        return bl ? this.setFrontText(signText) : this.setBackText(signText);
    }

    private boolean setBackText(SignText signText) {
        if (signText != this.backText) {
            this.backText = signText;
            this.markUpdated();
            return true;
        }
        return false;
    }

    private boolean setFrontText(SignText signText) {
        if (signText != this.frontText) {
            this.frontText = signText;
            this.markUpdated();
            return true;
        }
        return false;
    }

    public boolean canExecuteClickCommands(boolean bl, Player player) {
        return this.isWaxed() && this.getText(bl).hasAnyClickCommands(player);
    }

    public boolean executeClickCommandsIfPresent(ServerLevel serverLevel, Player player, BlockPos blockPos, boolean bl) {
        boolean bl2 = false;
        block5: for (Component component : this.getText(bl).getMessages(player.isTextFilteringEnabled())) {
            ClickEvent clickEvent;
            Style style = component.getStyle();
            ClickEvent clickEvent2 = clickEvent = style.getClickEvent();
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.RunCommand.class, ClickEvent.ShowDialog.class, ClickEvent.Custom.class}, (Object)clickEvent2, (int)n)) {
                case 0: {
                    ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent2;
                    serverLevel.getServer().getCommands().performPrefixedCommand(SignBlockEntity.createCommandSourceStack(player, serverLevel, blockPos), runCommand.command());
                    bl2 = true;
                    continue block5;
                }
                case 1: {
                    ClickEvent.ShowDialog showDialog = (ClickEvent.ShowDialog)clickEvent2;
                    player.openDialog(showDialog.dialog());
                    bl2 = true;
                    continue block5;
                }
                case 2: {
                    ClickEvent.Custom custom = (ClickEvent.Custom)clickEvent2;
                    serverLevel.getServer().handleCustomClickAction(custom.id(), custom.payload());
                    bl2 = true;
                    continue block5;
                }
            }
        }
        return bl2;
    }

    private static CommandSourceStack createCommandSourceStack(@Nullable Player player, ServerLevel serverLevel, BlockPos blockPos) {
        String string = player == null ? "Sign" : player.getPlainTextName();
        Component component = player == null ? Component.literal("Sign") : player.getDisplayName();
        return new CommandSourceStack(CommandSource.NULL, Vec3.atCenterOf(blockPos), Vec2.ZERO, serverLevel, LevelBasedPermissionSet.GAMEMASTER, string, component, serverLevel.getServer(), player);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public void setAllowedPlayerEditor(@Nullable UUID uUID) {
        this.playerWhoMayEdit = uUID;
    }

    public @Nullable UUID getPlayerWhoMayEdit() {
        return this.playerWhoMayEdit;
    }

    private void markUpdated() {
        this.setChanged();
        this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    public boolean isWaxed() {
        return this.isWaxed;
    }

    public boolean setWaxed(boolean bl) {
        if (this.isWaxed != bl) {
            this.isWaxed = bl;
            this.markUpdated();
            return true;
        }
        return false;
    }

    public boolean playerIsTooFarAwayToEdit(UUID uUID) {
        Player player = this.level.getPlayerByUUID(uUID);
        return player == null || !player.isWithinBlockInteractionRange(this.getBlockPos(), 4.0);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SignBlockEntity signBlockEntity) {
        UUID uUID = signBlockEntity.getPlayerWhoMayEdit();
        if (uUID != null) {
            signBlockEntity.clearInvalidPlayerWhoMayEdit(signBlockEntity, level, uUID);
        }
    }

    private void clearInvalidPlayerWhoMayEdit(SignBlockEntity signBlockEntity, Level level, UUID uUID) {
        if (signBlockEntity.playerIsTooFarAwayToEdit(uUID)) {
            signBlockEntity.setAllowedPlayerEditor(null);
        }
    }

    public SoundEvent getSignInteractionFailedSoundEvent() {
        return SoundEvents.WAXED_SIGN_INTERACT_FAIL;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }
}

