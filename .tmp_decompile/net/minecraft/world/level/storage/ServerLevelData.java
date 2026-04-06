/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.level.timers.TimerQueue;
import org.jspecify.annotations.Nullable;

public interface ServerLevelData
extends WritableLevelData {
    public String getLevelName();

    public void setThundering(boolean var1);

    public int getRainTime();

    public void setRainTime(int var1);

    public void setThunderTime(int var1);

    public int getThunderTime();

    @Override
    default public void fillCrashReportCategory(CrashReportCategory crashReportCategory, LevelHeightAccessor levelHeightAccessor) {
        WritableLevelData.super.fillCrashReportCategory(crashReportCategory, levelHeightAccessor);
        crashReportCategory.setDetail("Level name", this::getLevelName);
        crashReportCategory.setDetail("Level game mode", () -> String.format(Locale.ROOT, "Game mode: %s (ID %d). Hardcore: %b. Commands: %b", this.getGameType().getName(), this.getGameType().getId(), this.isHardcore(), this.isAllowCommands()));
        crashReportCategory.setDetail("Level weather", () -> String.format(Locale.ROOT, "Rain time: %d (now: %b), thunder time: %d (now: %b)", this.getRainTime(), this.isRaining(), this.getThunderTime(), this.isThundering()));
    }

    public int getClearWeatherTime();

    public void setClearWeatherTime(int var1);

    public int getWanderingTraderSpawnDelay();

    public void setWanderingTraderSpawnDelay(int var1);

    public int getWanderingTraderSpawnChance();

    public void setWanderingTraderSpawnChance(int var1);

    public @Nullable UUID getWanderingTraderId();

    public void setWanderingTraderId(UUID var1);

    public GameType getGameType();

    @Deprecated
    public Optional<WorldBorder.Settings> getLegacyWorldBorderSettings();

    @Deprecated
    public void setLegacyWorldBorderSettings(Optional<WorldBorder.Settings> var1);

    public boolean isInitialized();

    public void setInitialized(boolean var1);

    public boolean isAllowCommands();

    public void setGameType(GameType var1);

    public TimerQueue<MinecraftServer> getScheduledEvents();

    public void setGameTime(long var1);

    public void setDayTime(long var1);

    public GameRules getGameRules();
}

