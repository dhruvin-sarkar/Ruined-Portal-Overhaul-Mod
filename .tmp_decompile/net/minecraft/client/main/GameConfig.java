/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.main;

import com.mojang.blaze3d.platform.DisplayData;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.User;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraft.util.StringUtil;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GameConfig {
    public final UserData user;
    public final DisplayData display;
    public final FolderData location;
    public final GameData game;
    public final QuickPlayData quickPlay;

    public GameConfig(UserData userData, DisplayData displayData, FolderData folderData, GameData gameData, QuickPlayData quickPlayData) {
        this.user = userData;
        this.display = displayData;
        this.location = folderData;
        this.game = gameData;
        this.quickPlay = quickPlayData;
    }

    @Environment(value=EnvType.CLIENT)
    public static class UserData {
        public final User user;
        public final Proxy proxy;

        public UserData(User user, Proxy proxy) {
            this.user = user;
            this.proxy = proxy;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class FolderData {
        public final File gameDirectory;
        public final File resourcePackDirectory;
        public final File assetDirectory;
        public final @Nullable String assetIndex;

        public FolderData(File file, File file2, File file3, @Nullable String string) {
            this.gameDirectory = file;
            this.resourcePackDirectory = file2;
            this.assetDirectory = file3;
            this.assetIndex = string;
        }

        public Path getExternalAssetSource() {
            return this.assetIndex == null ? this.assetDirectory.toPath() : IndexedAssetSource.createIndexFs(this.assetDirectory.toPath(), this.assetIndex);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class GameData {
        public final boolean demo;
        public final String launchVersion;
        public final String versionType;
        public final boolean disableMultiplayer;
        public final boolean disableChat;
        public final boolean captureTracyImages;
        public final boolean renderDebugLabels;
        public final boolean offlineDeveloperMode;

        public GameData(boolean bl, String string, String string2, boolean bl2, boolean bl3, boolean bl4, boolean bl5, boolean bl6) {
            this.demo = bl;
            this.launchVersion = string;
            this.versionType = string2;
            this.disableMultiplayer = bl2;
            this.disableChat = bl3;
            this.captureTracyImages = bl4;
            this.renderDebugLabels = bl5;
            this.offlineDeveloperMode = bl6;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record QuickPlayData(@Nullable String logPath, QuickPlayVariant variant) {
        public boolean isEnabled() {
            return this.variant.isEnabled();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record QuickPlayDisabled() implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record QuickPlayRealmsData(String realmId) implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return !StringUtil.isBlank(this.realmId);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record QuickPlayMultiplayerData(String serverAddress) implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return !StringUtil.isBlank(this.serverAddress);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record QuickPlaySinglePlayerData(@Nullable String worldId) implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static sealed interface QuickPlayVariant
    permits QuickPlaySinglePlayerData, QuickPlayMultiplayerData, QuickPlayRealmsData, QuickPlayDisabled {
        public static final QuickPlayVariant DISABLED = new QuickPlayDisabled();

        public boolean isEnabled();
    }
}

