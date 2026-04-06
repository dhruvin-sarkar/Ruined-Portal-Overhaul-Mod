/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.client.RealmsError;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.realms.RealmsScreen;

@Environment(value=EnvType.CLIENT)
public class RealmsGenericErrorScreen
extends RealmsScreen {
    private static final Component GENERIC_TITLE = Component.translatable("mco.errorMessage.generic");
    private final Screen nextScreen;
    private final Component detail;
    private MultiLineLabel splitDetail = MultiLineLabel.EMPTY;

    public RealmsGenericErrorScreen(RealmsServiceException realmsServiceException, Screen screen) {
        this(ErrorMessage.forServiceError(realmsServiceException), screen);
    }

    public RealmsGenericErrorScreen(Component component, Screen screen) {
        this(new ErrorMessage(GENERIC_TITLE, component), screen);
    }

    public RealmsGenericErrorScreen(Component component, Component component2, Screen screen) {
        this(new ErrorMessage(component, component2), screen);
    }

    private RealmsGenericErrorScreen(ErrorMessage errorMessage, Screen screen) {
        super(errorMessage.title);
        this.nextScreen = screen;
        this.detail = ComponentUtils.mergeStyles(errorMessage.detail, Style.EMPTY.withColor(-2142128));
    }

    @Override
    public void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, button -> this.onClose()).bounds(this.width / 2 - 100, this.height - 52, 200, 20).build());
        this.splitDetail = MultiLineLabel.create(this.font, this.detail, this.width * 3 / 4);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.nextScreen);
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.detail);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int i, int j, float f) {
        super.render(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 80, -1);
        ActiveTextCollector activeTextCollector = guiGraphics.textRenderer();
        this.splitDetail.visitLines(TextAlignment.CENTER, this.width / 2, 100, this.minecraft.font.lineHeight, activeTextCollector);
    }

    @Environment(value=EnvType.CLIENT)
    static final class ErrorMessage
    extends Record {
        final Component title;
        final Component detail;

        ErrorMessage(Component component, Component component2) {
            this.title = component;
            this.detail = component2;
        }

        static ErrorMessage forServiceError(RealmsServiceException realmsServiceException) {
            RealmsError realmsError = realmsServiceException.realmsError;
            return new ErrorMessage(Component.translatable("mco.errorMessage.realmsService.realmsError", realmsError.errorCode()), realmsError.errorMessage());
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ErrorMessage.class, "title;detail", "title", "detail"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ErrorMessage.class, "title;detail", "title", "detail"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ErrorMessage.class, "title;detail", "title", "detail"}, this, object);
        }

        public Component title() {
            return this.title;
        }

        public Component detail() {
            return this.detail;
        }
    }
}

