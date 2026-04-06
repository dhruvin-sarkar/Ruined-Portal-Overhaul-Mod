/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsSlot;
import com.mojang.realmsclient.util.RealmsTextureManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class RealmsWorldSlotButton
extends Button {
    private static final Identifier SLOT_FRAME_SPRITE = Identifier.withDefaultNamespace("widget/slot_frame");
    public static final Identifier EMPTY_SLOT_LOCATION = Identifier.withDefaultNamespace("textures/gui/realms/empty_frame.png");
    public static final Identifier DEFAULT_WORLD_SLOT_1 = Identifier.withDefaultNamespace("textures/gui/title/background/panorama_0.png");
    public static final Identifier DEFAULT_WORLD_SLOT_2 = Identifier.withDefaultNamespace("textures/gui/title/background/panorama_2.png");
    public static final Identifier DEFAULT_WORLD_SLOT_3 = Identifier.withDefaultNamespace("textures/gui/title/background/panorama_3.png");
    private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
    private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
    static final Component MINIGAME = Component.translatable("mco.worldSlot.minigame");
    private static final int WORLD_NAME_MAX_WIDTH = 64;
    private static final String DOTS = "...";
    private final int slotIndex;
    private State state;

    public RealmsWorldSlotButton(int i, int j, int k, int l, int m, RealmsServer realmsServer, Button.OnPress onPress) {
        super(i, j, k, l, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.slotIndex = m;
        this.state = this.setServerData(realmsServer);
    }

    public State getState() {
        return this.state;
    }

    public State setServerData(RealmsServer realmsServer) {
        this.state = new State(realmsServer, this.slotIndex);
        this.setTooltipAndNarration(this.state, realmsServer.minigameName);
        return this.state;
    }

    private void setTooltipAndNarration(State state, @Nullable String string) {
        Component component;
        switch (state.action.ordinal()) {
            case 1: {
                Component component2;
                if (state.minigame) {
                    component2 = SWITCH_TO_MINIGAME_SLOT_TOOLTIP;
                    break;
                }
                component2 = SWITCH_TO_WORLD_SLOT_TOOLTIP;
                break;
            }
            default: {
                Component component2 = component = null;
            }
        }
        if (component != null) {
            this.setTooltip(Tooltip.create(component));
        }
        MutableComponent mutableComponent = Component.literal(state.slotName);
        if (state.minigame && string != null) {
            mutableComponent = mutableComponent.append(CommonComponents.SPACE).append(string);
        }
        this.setMessage(mutableComponent);
    }

    static Action getAction(boolean bl, boolean bl2, boolean bl3) {
        if (!(bl || bl2 && bl3)) {
            return Action.SWITCH_SLOT;
        }
        return Action.NOTHING;
    }

    @Override
    public boolean isActive() {
        return this.state.action != Action.NOTHING && super.isActive();
    }

    @Override
    public void renderContents(GuiGraphics guiGraphics, int i, int j, float f) {
        Object string;
        Font font;
        int k = this.getX();
        int l = this.getY();
        boolean bl = this.isHoveredOrFocused();
        Identifier identifier = this.state.minigame ? RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image) : (this.state.empty ? EMPTY_SLOT_LOCATION : (this.state.image != null && this.state.imageId != -1L ? RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image) : (this.slotIndex == 1 ? DEFAULT_WORLD_SLOT_1 : (this.slotIndex == 2 ? DEFAULT_WORLD_SLOT_2 : (this.slotIndex == 3 ? DEFAULT_WORLD_SLOT_3 : EMPTY_SLOT_LOCATION)))));
        int m = -1;
        if (!this.state.activeSlot) {
            m = ARGB.colorFromFloat(1.0f, 0.56f, 0.56f, 0.56f);
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, identifier, k + 1, l + 1, 0.0f, 0.0f, this.width - 2, this.height - 2, 74, 74, 74, 74, m);
        if (bl && this.state.action != Action.NOTHING) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FRAME_SPRITE, k, l, this.width, this.height);
        } else if (this.state.activeSlot) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FRAME_SPRITE, k, l, this.width, this.height, ARGB.colorFromFloat(1.0f, 0.8f, 0.8f, 0.8f));
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FRAME_SPRITE, k, l, this.width, this.height, ARGB.colorFromFloat(1.0f, 0.56f, 0.56f, 0.56f));
        }
        if (this.state.hardcore) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, RealmsMainScreen.HARDCORE_MODE_SPRITE, k + 3, l + 4, 9, 8);
        }
        if ((font = Minecraft.getInstance().font).width((String)(string = this.state.slotName)) > 64) {
            string = font.plainSubstrByWidth((String)string, 64 - font.width(DOTS)) + DOTS;
        }
        guiGraphics.drawCenteredString(font, (String)string, k + this.width / 2, l + this.height - 14, -1);
        if (this.state.activeSlot) {
            guiGraphics.drawCenteredString(font, RealmsMainScreen.getVersionComponent(this.state.slotVersion, this.state.compatibility.isCompatible()), k + this.width / 2, l + this.height + 2, -1);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class State {
        final String slotName;
        final String slotVersion;
        final RealmsServer.Compatibility compatibility;
        final long imageId;
        final @Nullable String image;
        public final boolean empty;
        public final boolean minigame;
        public final Action action;
        public final boolean hardcore;
        public final boolean activeSlot;

        public State(RealmsServer realmsServer, int i) {
            boolean bl = this.minigame = i == 4;
            if (this.minigame) {
                this.slotName = MINIGAME.getString();
                this.imageId = realmsServer.minigameId;
                this.image = realmsServer.minigameImage;
                this.empty = realmsServer.minigameId == -1;
                this.slotVersion = "";
                this.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
                this.hardcore = false;
                this.activeSlot = realmsServer.isMinigameActive();
            } else {
                RealmsSlot realmsSlot = realmsServer.slots.get(i);
                this.slotName = realmsSlot.options.getSlotName(i);
                this.imageId = realmsSlot.options.templateId;
                this.image = realmsSlot.options.templateImage;
                this.empty = realmsSlot.options.empty;
                this.slotVersion = realmsSlot.options.version;
                this.compatibility = realmsSlot.options.compatibility;
                this.hardcore = realmsSlot.isHardcore();
                this.activeSlot = realmsServer.activeSlot == i && !realmsServer.isMinigameActive();
            }
            this.action = RealmsWorldSlotButton.getAction(this.activeSlot, this.empty, realmsServer.expired);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Action {
        NOTHING,
        SWITCH_SLOT;

    }
}

