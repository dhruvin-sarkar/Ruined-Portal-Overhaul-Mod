/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.spectator;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.util.ARGB;

@Environment(value=EnvType.CLIENT)
public class PlayerMenuItem
implements SpectatorMenuItem {
    private final PlayerInfo playerInfo;
    private final Component name;

    public PlayerMenuItem(PlayerInfo playerInfo) {
        this.playerInfo = playerInfo;
        this.name = Component.literal(playerInfo.getProfile().name());
    }

    @Override
    public void selectItem(SpectatorMenu spectatorMenu) {
        Minecraft.getInstance().getConnection().send(new ServerboundTeleportToEntityPacket(this.playerInfo.getProfile().id()));
    }

    @Override
    public Component getName() {
        return this.name;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float f, float g) {
        PlayerFaceRenderer.draw(guiGraphics, this.playerInfo.getSkin(), 2, 2, 12, ARGB.white(g));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

