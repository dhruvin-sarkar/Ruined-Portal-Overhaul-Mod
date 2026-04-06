/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.GameModeCommand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class GameModeSwitcherScreen
extends Screen {
    static final Identifier SLOT_SPRITE = Identifier.withDefaultNamespace("gamemode_switcher/slot");
    static final Identifier SELECTION_SPRITE = Identifier.withDefaultNamespace("gamemode_switcher/selection");
    private static final Identifier GAMEMODE_SWITCHER_LOCATION = Identifier.withDefaultNamespace("textures/gui/container/gamemode_switcher.png");
    private static final int SPRITE_SHEET_WIDTH = 128;
    private static final int SPRITE_SHEET_HEIGHT = 128;
    private static final int SLOT_AREA = 26;
    private static final int SLOT_PADDING = 5;
    private static final int SLOT_AREA_PADDED = 31;
    private static final int HELP_TIPS_OFFSET_Y = 5;
    private static final int ALL_SLOTS_WIDTH = GameModeIcon.values().length * 31 - 5;
    private final GameModeIcon previousHovered;
    private GameModeIcon currentlyHovered;
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    private final List<GameModeSlot> slots = Lists.newArrayList();

    public GameModeSwitcherScreen() {
        super(GameNarrator.NO_TITLE);
        this.currentlyHovered = this.previousHovered = GameModeIcon.getFromGameType(this.getDefaultSelected());
    }

    private GameType getDefaultSelected() {
        MultiPlayerGameMode multiPlayerGameMode = Minecraft.getInstance().gameMode;
        GameType gameType = multiPlayerGameMode.getPreviousPlayerMode();
        if (gameType != null) {
            return gameType;
        }
        return multiPlayerGameMode.getPlayerMode() == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
    }

    @Override
    protected void init() {
        super.init();
        this.slots.clear();
        this.currentlyHovered = this.previousHovered;
        for (int i = 0; i < GameModeIcon.VALUES.length; ++i) {
            GameModeIcon gameModeIcon = GameModeIcon.VALUES[i];
            this.slots.add(new GameModeSlot(gameModeIcon, this.width / 2 - ALL_SLOTS_WIDTH / 2 + i * 31, this.height / 2 - 31));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        guiGraphics.drawCenteredString(this.font, this.currentlyHovered.name, this.width / 2, this.height / 2 - 31 - 20, -1);
        MutableComponent mutableComponent = Component.translatable("debug.gamemodes.select_next", this.minecraft.options.keyDebugSwitchGameMode.getTranslatedKeyMessage().copy().withStyle(ChatFormatting.AQUA));
        guiGraphics.drawCenteredString(this.font, mutableComponent, this.width / 2, this.height / 2 + 5, -1);
        if (!this.setFirstMousePos) {
            this.firstMouseX = i;
            this.firstMouseY = j;
            this.setFirstMousePos = true;
        }
        boolean bl = this.firstMouseX == i && this.firstMouseY == j;
        for (GameModeSlot gameModeSlot : this.slots) {
            gameModeSlot.render(guiGraphics, i, j, f);
            gameModeSlot.setSelected(this.currentlyHovered == gameModeSlot.icon);
            if (bl || !gameModeSlot.isHoveredOrFocused()) continue;
            this.currentlyHovered = gameModeSlot.icon;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
        int k = this.width / 2 - 62;
        int l = this.height / 2 - 31 - 27;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GAMEMODE_SWITCHER_LOCATION, k, l, 0.0f, 0.0f, 125, 75, 128, 128);
    }

    private void switchToHoveredGameMode() {
        GameModeSwitcherScreen.switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
    }

    private static void switchToHoveredGameMode(Minecraft minecraft, GameModeIcon gameModeIcon) {
        if (!minecraft.canSwitchGameMode()) {
            return;
        }
        GameModeIcon gameModeIcon2 = GameModeIcon.getFromGameType(minecraft.gameMode.getPlayerMode());
        if (gameModeIcon != gameModeIcon2 && GameModeCommand.PERMISSION_CHECK.check(minecraft.player.permissions())) {
            minecraft.player.connection.send(new ServerboundChangeGameModePacket(gameModeIcon.mode));
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (this.minecraft.options.keyDebugSwitchGameMode.matches(keyEvent)) {
            this.setFirstMousePos = false;
            this.currentlyHovered = this.currentlyHovered.getNext();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        if (this.minecraft.options.keyDebugModifier.matches(keyEvent)) {
            this.switchToHoveredGameMode();
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyReleased(keyEvent);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (this.minecraft.options.keyDebugModifier.matchesMouse(mouseButtonEvent)) {
            this.switchToHoveredGameMode();
            this.minecraft.setScreen(null);
            return true;
        }
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    static enum GameModeIcon {
        CREATIVE(Component.translatable("gameMode.creative"), GameType.CREATIVE, new ItemStack(Blocks.GRASS_BLOCK)),
        SURVIVAL(Component.translatable("gameMode.survival"), GameType.SURVIVAL, new ItemStack(Items.IRON_SWORD)),
        ADVENTURE(Component.translatable("gameMode.adventure"), GameType.ADVENTURE, new ItemStack(Items.MAP)),
        SPECTATOR(Component.translatable("gameMode.spectator"), GameType.SPECTATOR, new ItemStack(Items.ENDER_EYE));

        static final GameModeIcon[] VALUES;
        private static final int ICON_AREA = 16;
        private static final int ICON_TOP_LEFT = 5;
        final Component name;
        final GameType mode;
        private final ItemStack renderStack;

        private GameModeIcon(Component component, GameType gameType, ItemStack itemStack) {
            this.name = component;
            this.mode = gameType;
            this.renderStack = itemStack;
        }

        void drawIcon(GuiGraphics guiGraphics, int i, int j) {
            guiGraphics.renderItem(this.renderStack, i, j);
        }

        GameModeIcon getNext() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> SURVIVAL;
                case 1 -> ADVENTURE;
                case 2 -> SPECTATOR;
                case 3 -> CREATIVE;
            };
        }

        static GameModeIcon getFromGameType(GameType gameType) {
            return switch (gameType) {
                default -> throw new MatchException(null, null);
                case GameType.SPECTATOR -> SPECTATOR;
                case GameType.SURVIVAL -> SURVIVAL;
                case GameType.CREATIVE -> CREATIVE;
                case GameType.ADVENTURE -> ADVENTURE;
            };
        }

        static {
            VALUES = GameModeIcon.values();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class GameModeSlot
    extends AbstractWidget {
        final GameModeIcon icon;
        private boolean isSelected;

        public GameModeSlot(GameModeIcon gameModeIcon, int i, int j) {
            super(i, j, 26, 26, gameModeIcon.name);
            this.icon = gameModeIcon;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
            this.drawSlot(guiGraphics);
            if (this.isSelected) {
                this.drawSelection(guiGraphics);
            }
            this.icon.drawIcon(guiGraphics, this.getX() + 5, this.getY() + 5);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public boolean isHoveredOrFocused() {
            return super.isHoveredOrFocused() || this.isSelected;
        }

        public void setSelected(boolean bl) {
            this.isSelected = bl;
        }

        private void drawSlot(GuiGraphics guiGraphics) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, this.getX(), this.getY(), 26, 26);
        }

        private void drawSelection(GuiGraphics guiGraphics) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECTION_SPRITE, this.getX(), this.getY(), 26, 26);
        }
    }
}

