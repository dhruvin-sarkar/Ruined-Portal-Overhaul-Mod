/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class BaseCommandBlock {
    private static final Component DEFAULT_NAME = Component.literal("@");
    private static final int NO_LAST_EXECUTION = -1;
    private long lastExecution = -1L;
    private boolean updateLastExecution = true;
    private int successCount;
    private boolean trackOutput = true;
    @Nullable Component lastOutput;
    private String command = "";
    private @Nullable Component customName;

    public int getSuccessCount() {
        return this.successCount;
    }

    public void setSuccessCount(int i) {
        this.successCount = i;
    }

    public Component getLastOutput() {
        return this.lastOutput == null ? CommonComponents.EMPTY : this.lastOutput;
    }

    public void save(ValueOutput valueOutput) {
        valueOutput.putString("Command", this.command);
        valueOutput.putInt("SuccessCount", this.successCount);
        valueOutput.storeNullable("CustomName", ComponentSerialization.CODEC, this.customName);
        valueOutput.putBoolean("TrackOutput", this.trackOutput);
        if (this.trackOutput) {
            valueOutput.storeNullable("LastOutput", ComponentSerialization.CODEC, this.lastOutput);
        }
        valueOutput.putBoolean("UpdateLastExecution", this.updateLastExecution);
        if (this.updateLastExecution && this.lastExecution != -1L) {
            valueOutput.putLong("LastExecution", this.lastExecution);
        }
    }

    public void load(ValueInput valueInput) {
        this.command = valueInput.getStringOr("Command", "");
        this.successCount = valueInput.getIntOr("SuccessCount", 0);
        this.setCustomName(BlockEntity.parseCustomNameSafe(valueInput, "CustomName"));
        this.trackOutput = valueInput.getBooleanOr("TrackOutput", true);
        this.lastOutput = this.trackOutput ? BlockEntity.parseCustomNameSafe(valueInput, "LastOutput") : null;
        this.updateLastExecution = valueInput.getBooleanOr("UpdateLastExecution", true);
        this.lastExecution = this.updateLastExecution ? valueInput.getLongOr("LastExecution", -1L) : -1L;
    }

    public void setCommand(String string) {
        this.command = string;
        this.successCount = 0;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean performCommand(ServerLevel serverLevel) {
        if (serverLevel.getGameTime() == this.lastExecution) {
            return false;
        }
        if ("Searge".equalsIgnoreCase(this.command)) {
            this.lastOutput = Component.literal("#itzlipofutzli");
            this.successCount = 1;
            return true;
        }
        this.successCount = 0;
        if (serverLevel.isCommandBlockEnabled() && !StringUtil.isNullOrEmpty(this.command)) {
            try {
                this.lastOutput = null;
                try (CloseableCommandBlockSource closeableCommandBlockSource = this.createSource(serverLevel);){
                    CommandSource commandSource = (CommandSource)Objects.requireNonNullElse((Object)closeableCommandBlockSource, (Object)CommandSource.NULL);
                    CommandSourceStack commandSourceStack = this.createCommandSourceStack(serverLevel, commandSource).withCallback((bl, i) -> {
                        if (bl) {
                            ++this.successCount;
                        }
                    });
                    serverLevel.getServer().getCommands().performPrefixedCommand(commandSourceStack, this.command);
                }
            }
            catch (Throwable throwable) {
                CrashReport crashReport = CrashReport.forThrowable(throwable, "Executing command block");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Command to be executed");
                crashReportCategory.setDetail("Command", this::getCommand);
                crashReportCategory.setDetail("Name", () -> this.getName().getString());
                throw new ReportedException(crashReport);
            }
        }
        this.lastExecution = this.updateLastExecution ? serverLevel.getGameTime() : -1L;
        return true;
    }

    private @Nullable CloseableCommandBlockSource createSource(ServerLevel serverLevel) {
        return this.trackOutput ? new CloseableCommandBlockSource(serverLevel) : null;
    }

    public Component getName() {
        return this.customName != null ? this.customName : DEFAULT_NAME;
    }

    public @Nullable Component getCustomName() {
        return this.customName;
    }

    public void setCustomName(@Nullable Component component) {
        this.customName = component;
    }

    public abstract void onUpdated(ServerLevel var1);

    public void setLastOutput(@Nullable Component component) {
        this.lastOutput = component;
    }

    public void setTrackOutput(boolean bl) {
        this.trackOutput = bl;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public abstract CommandSourceStack createCommandSourceStack(ServerLevel var1, CommandSource var2);

    public abstract boolean isValid();

    protected class CloseableCommandBlockSource
    implements CommandSource,
    AutoCloseable {
        private final ServerLevel level;
        private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);
        private boolean closed;

        protected CloseableCommandBlockSource(ServerLevel serverLevel) {
            this.level = serverLevel;
        }

        @Override
        public boolean acceptsSuccess() {
            return !this.closed && this.level.getGameRules().get(GameRules.SEND_COMMAND_FEEDBACK) != false;
        }

        @Override
        public boolean acceptsFailure() {
            return !this.closed;
        }

        @Override
        public boolean shouldInformAdmins() {
            return !this.closed && this.level.getGameRules().get(GameRules.COMMAND_BLOCK_OUTPUT) != false;
        }

        @Override
        public void sendSystemMessage(Component component) {
            if (!this.closed) {
                BaseCommandBlock.this.lastOutput = Component.literal("[" + TIME_FORMAT.format(ZonedDateTime.now()) + "] ").append(component);
                BaseCommandBlock.this.onUpdated(this.level);
            }
        }

        @Override
        public void close() throws Exception {
            this.closed = true;
        }
    }
}

