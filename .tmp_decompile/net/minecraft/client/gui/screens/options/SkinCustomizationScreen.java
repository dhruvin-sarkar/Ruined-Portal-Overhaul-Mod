/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.gui.screens.options;

import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(value=EnvType.CLIENT)
public class SkinCustomizationScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.skinCustomisation.title");

    public SkinCustomizationScreen(Screen screen, Options options) {
        super(screen, options, TITLE);
    }

    @Override
    protected void addOptions() {
        ArrayList<AbstractWidget> list = new ArrayList<AbstractWidget>();
        for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
            list.add(CycleButton.onOffBuilder(this.options.isModelPartEnabled(playerModelPart)).create(playerModelPart.getName(), (cycleButton, boolean_) -> this.options.setModelPart(playerModelPart, (boolean)boolean_)));
        }
        list.add(this.options.mainHand().createButton(this.options));
        this.list.addSmall(list);
    }
}

