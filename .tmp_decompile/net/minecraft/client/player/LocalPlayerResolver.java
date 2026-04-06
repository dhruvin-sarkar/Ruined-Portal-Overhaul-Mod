/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.server.players.ProfileResolver;

@Environment(value=EnvType.CLIENT)
public class LocalPlayerResolver
implements ProfileResolver {
    private final Minecraft minecraft;
    private final ProfileResolver parentResolver;

    public LocalPlayerResolver(Minecraft minecraft, ProfileResolver profileResolver) {
        this.minecraft = minecraft;
        this.parentResolver = profileResolver;
    }

    @Override
    public Optional<GameProfile> fetchByName(String string) {
        PlayerInfo playerInfo;
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null && (playerInfo = clientPacketListener.getPlayerInfoIgnoreCase(string)) != null) {
            return Optional.of(playerInfo.getProfile());
        }
        return this.parentResolver.fetchByName(string);
    }

    @Override
    public Optional<GameProfile> fetchById(UUID uUID) {
        PlayerInfo playerInfo;
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null && (playerInfo = clientPacketListener.getPlayerInfo(uUID)) != null) {
            return Optional.of(playerInfo.getProfile());
        }
        return this.parentResolver.fetchById(uUID);
    }
}

