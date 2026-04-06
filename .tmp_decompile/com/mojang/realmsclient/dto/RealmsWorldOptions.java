/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.annotations.SerializedName;
import com.mojang.realmsclient.dto.Exclude;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.ValueObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldOptions
extends ValueObject
implements ReflectionBasedSerialization {
    @SerializedName(value="spawnProtection")
    public int spawnProtection = 0;
    @SerializedName(value="forceGameMode")
    public boolean forceGameMode = false;
    @SerializedName(value="difficulty")
    public int difficulty = 2;
    @SerializedName(value="gameMode")
    public int gameMode = 0;
    @SerializedName(value="slotName")
    private String slotName = "";
    @SerializedName(value="version")
    public String version = "";
    @SerializedName(value="compatibility")
    public RealmsServer.Compatibility compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
    @SerializedName(value="worldTemplateId")
    public long templateId = -1L;
    @SerializedName(value="worldTemplateImage")
    public @Nullable String templateImage = null;
    @Exclude
    public boolean empty;

    private RealmsWorldOptions() {
    }

    public RealmsWorldOptions(int i, int j, int k, boolean bl, String string, String string2, RealmsServer.Compatibility compatibility) {
        this.spawnProtection = i;
        this.difficulty = j;
        this.gameMode = k;
        this.forceGameMode = bl;
        this.slotName = string;
        this.version = string2;
        this.compatibility = compatibility;
    }

    public static RealmsWorldOptions createDefaults() {
        return new RealmsWorldOptions();
    }

    public static RealmsWorldOptions createDefaultsWith(GameType gameType, Difficulty difficulty, boolean bl, String string, String string2) {
        RealmsWorldOptions realmsWorldOptions = RealmsWorldOptions.createDefaults();
        realmsWorldOptions.difficulty = difficulty.getId();
        realmsWorldOptions.gameMode = gameType.getId();
        realmsWorldOptions.slotName = string2;
        realmsWorldOptions.version = string;
        return realmsWorldOptions;
    }

    public static RealmsWorldOptions createFromSettings(LevelSettings levelSettings, String string) {
        return RealmsWorldOptions.createDefaultsWith(levelSettings.gameType(), levelSettings.difficulty(), levelSettings.hardcore(), string, levelSettings.levelName());
    }

    public static RealmsWorldOptions createEmptyDefaults() {
        RealmsWorldOptions realmsWorldOptions = RealmsWorldOptions.createDefaults();
        realmsWorldOptions.setEmpty(true);
        return realmsWorldOptions;
    }

    public void setEmpty(boolean bl) {
        this.empty = bl;
    }

    public static RealmsWorldOptions parse(GuardedSerializer guardedSerializer, String string) {
        RealmsWorldOptions realmsWorldOptions = guardedSerializer.fromJson(string, RealmsWorldOptions.class);
        if (realmsWorldOptions == null) {
            return RealmsWorldOptions.createDefaults();
        }
        RealmsWorldOptions.finalize(realmsWorldOptions);
        return realmsWorldOptions;
    }

    private static void finalize(RealmsWorldOptions realmsWorldOptions) {
        if (realmsWorldOptions.slotName == null) {
            realmsWorldOptions.slotName = "";
        }
        if (realmsWorldOptions.version == null) {
            realmsWorldOptions.version = "";
        }
        if (realmsWorldOptions.compatibility == null) {
            realmsWorldOptions.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
        }
    }

    public String getSlotName(int i) {
        if (StringUtil.isBlank(this.slotName)) {
            if (this.empty) {
                return I18n.get("mco.configure.world.slot.empty", new Object[0]);
            }
            return this.getDefaultSlotName(i);
        }
        return this.slotName;
    }

    public String getDefaultSlotName(int i) {
        return I18n.get("mco.configure.world.slot", i);
    }

    public RealmsWorldOptions copy() {
        return new RealmsWorldOptions(this.spawnProtection, this.difficulty, this.gameMode, this.forceGameMode, this.slotName, this.version, this.compatibility);
    }
}

