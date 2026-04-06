/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.telemetry.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.TelemetryEventSender;
import net.minecraft.client.telemetry.TelemetryEventType;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.TelemetryPropertyMap;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class WorldLoadEvent {
    private boolean eventSent;
    private @Nullable TelemetryProperty.GameMode gameMode;
    private @Nullable String serverBrand;
    private final @Nullable String minigameName;

    public WorldLoadEvent(@Nullable String string) {
        this.minigameName = string;
    }

    public void addProperties(TelemetryPropertyMap.Builder builder) {
        if (this.serverBrand != null) {
            builder.put(TelemetryProperty.SERVER_MODDED, !this.serverBrand.equals("vanilla"));
        }
        builder.put(TelemetryProperty.SERVER_TYPE, this.getServerType());
    }

    private TelemetryProperty.ServerType getServerType() {
        ServerData serverData = Minecraft.getInstance().getCurrentServer();
        if (serverData != null && serverData.isRealm()) {
            return TelemetryProperty.ServerType.REALM;
        }
        if (Minecraft.getInstance().hasSingleplayerServer()) {
            return TelemetryProperty.ServerType.LOCAL;
        }
        return TelemetryProperty.ServerType.OTHER;
    }

    public boolean send(TelemetryEventSender telemetryEventSender) {
        if (this.eventSent || this.gameMode == null || this.serverBrand == null) {
            return false;
        }
        this.eventSent = true;
        telemetryEventSender.send(TelemetryEventType.WORLD_LOADED, builder -> {
            builder.put(TelemetryProperty.GAME_MODE, this.gameMode);
            if (this.minigameName != null) {
                builder.put(TelemetryProperty.REALMS_MAP_CONTENT, this.minigameName);
            }
        });
        return true;
    }

    public void setGameMode(GameType gameType, boolean bl) {
        this.gameMode = switch (gameType) {
            default -> throw new MatchException(null, null);
            case GameType.SURVIVAL -> {
                if (bl) {
                    yield TelemetryProperty.GameMode.HARDCORE;
                }
                yield TelemetryProperty.GameMode.SURVIVAL;
            }
            case GameType.CREATIVE -> TelemetryProperty.GameMode.CREATIVE;
            case GameType.ADVENTURE -> TelemetryProperty.GameMode.ADVENTURE;
            case GameType.SPECTATOR -> TelemetryProperty.GameMode.SPECTATOR;
        };
    }

    public void setServerBrand(String string) {
        this.serverBrand = string;
    }
}

