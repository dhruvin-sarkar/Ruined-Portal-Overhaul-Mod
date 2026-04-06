/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

@Environment(value=EnvType.CLIENT)
public class InBedChatScreen
extends ChatScreen {
    private Button leaveBedButton;

    public InBedChatScreen(String string, boolean bl) {
        super(string, bl);
    }

    @Override
    protected void init() {
        super.init();
        this.leaveBedButton = Button.builder(Component.translatable("multiplayer.stopSleeping"), button -> this.sendWakeUp()).bounds(this.width / 2 - 100, this.height - 40, 200, 20).build();
        this.addRenderableWidget(this.leaveBedButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            this.leaveBedButton.render(guiGraphics, i, j, f);
            return;
        }
        super.render(guiGraphics, i, j, f);
    }

    @Override
    public void onClose() {
        this.sendWakeUp();
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            return true;
        }
        return super.charTyped(characterEvent);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (keyEvent.isEscape()) {
            this.sendWakeUp();
        }
        if (!this.minecraft.getChatStatus().isChatAllowed(this.minecraft.isLocalServer())) {
            return true;
        }
        if (keyEvent.isConfirmation()) {
            this.handleChatInput(this.input.getValue(), true);
            this.input.setValue("");
            this.minecraft.gui.getChat().resetChatScroll();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    private void sendWakeUp() {
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        clientPacketListener.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    }

    public void onPlayerWokeUp() {
        String string = this.input.getValue();
        if (this.isDraft || string.isEmpty()) {
            this.exitReason = ChatScreen.ExitReason.INTERRUPTED;
            this.minecraft.setScreen(null);
        } else {
            this.exitReason = ChatScreen.ExitReason.DONE;
            this.minecraft.setScreen(new ChatScreen(string, false));
        }
    }
}

