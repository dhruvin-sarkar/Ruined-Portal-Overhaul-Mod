/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URI;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class ConfirmLinkScreen
extends ConfirmScreen {
    private static final Component WARNING_TEXT = Component.translatable("chat.link.warning").withColor(-13108);
    private static final int BUTTON_WIDTH = 100;
    private final String url;
    private final boolean showWarning;

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, String string, boolean bl) {
        this(booleanConsumer, (Component)ConfirmLinkScreen.confirmMessage(bl), (Component)Component.literal(string), string, bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, bl);
    }

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, String string, boolean bl) {
        this(booleanConsumer, component, (Component)ConfirmLinkScreen.confirmMessage(bl, string), string, bl ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_NO, bl);
    }

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, URI uRI, boolean bl) {
        this(booleanConsumer, component, uRI.toString(), bl);
    }

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, Component component2, URI uRI, Component component3, boolean bl) {
        this(booleanConsumer, component, component2, uRI.toString(), component3, true);
    }

    public ConfirmLinkScreen(BooleanConsumer booleanConsumer, Component component, Component component2, String string, Component component3, boolean bl) {
        super(booleanConsumer, component, component2);
        this.yesButtonComponent = bl ? CommonComponents.GUI_OPEN_IN_BROWSER : CommonComponents.GUI_YES;
        this.noButtonComponent = component3;
        this.showWarning = !bl;
        this.url = string;
    }

    protected static MutableComponent confirmMessage(boolean bl, String string) {
        return ConfirmLinkScreen.confirmMessage(bl).append(CommonComponents.SPACE).append(Component.literal(string));
    }

    protected static MutableComponent confirmMessage(boolean bl) {
        return Component.translatable(bl ? "chat.link.confirmTrusted" : "chat.link.confirm");
    }

    @Override
    protected void addAdditionalText() {
        if (this.showWarning) {
            this.layout.addChild(new StringWidget(WARNING_TEXT, this.font));
        }
    }

    @Override
    protected void addButtons(LinearLayout linearLayout) {
        this.yesButton = linearLayout.addChild(Button.builder(this.yesButtonComponent, button -> this.callback.accept(true)).width(100).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_COPY_TO_CLIPBOARD, button -> {
            this.copyToClipboard();
            this.callback.accept(false);
        }).width(100).build());
        this.noButton = linearLayout.addChild(Button.builder(this.noButtonComponent, button -> this.callback.accept(false)).width(100).build());
    }

    public void copyToClipboard() {
        this.minecraft.keyboardHandler.setClipboard(this.url);
    }

    public static void confirmLinkNow(Screen screen, String string, boolean bl2) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(string);
            }
            minecraft.setScreen(screen);
        }, string, bl2));
    }

    public static void confirmLinkNow(Screen screen, URI uRI, boolean bl2) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(uRI);
            }
            minecraft.setScreen(screen);
        }, uRI.toString(), bl2));
    }

    public static void confirmLinkNow(Screen screen, URI uRI) {
        ConfirmLinkScreen.confirmLinkNow(screen, uRI, true);
    }

    public static void confirmLinkNow(Screen screen, String string) {
        ConfirmLinkScreen.confirmLinkNow(screen, string, true);
    }

    public static Button.OnPress confirmLink(Screen screen, String string, boolean bl) {
        return button -> ConfirmLinkScreen.confirmLinkNow(screen, string, bl);
    }

    public static Button.OnPress confirmLink(Screen screen, URI uRI, boolean bl) {
        return button -> ConfirmLinkScreen.confirmLinkNow(screen, uRI, bl);
    }

    public static Button.OnPress confirmLink(Screen screen, String string) {
        return ConfirmLinkScreen.confirmLink(screen, string, true);
    }

    public static Button.OnPress confirmLink(Screen screen, URI uRI) {
        return ConfirmLinkScreen.confirmLink(screen, uRI, true);
    }
}

