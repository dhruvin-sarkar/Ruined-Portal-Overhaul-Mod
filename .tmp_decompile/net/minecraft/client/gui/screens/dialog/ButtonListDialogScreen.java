/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.dialog;

import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.gui.screens.dialog.DialogControlSet;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.ButtonListDialog;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class ButtonListDialogScreen<T extends ButtonListDialog>
extends DialogScreen<T> {
    public static final int FOOTER_MARGIN = 5;

    public ButtonListDialogScreen(@Nullable Screen screen, T buttonListDialog, DialogConnectionAccess dialogConnectionAccess) {
        super(screen, buttonListDialog, dialogConnectionAccess);
    }

    @Override
    protected void populateBodyElements(LinearLayout linearLayout, DialogControlSet dialogControlSet, T buttonListDialog, DialogConnectionAccess dialogConnectionAccess) {
        super.populateBodyElements(linearLayout, dialogControlSet, buttonListDialog, dialogConnectionAccess);
        List list = this.createListActions(buttonListDialog, dialogConnectionAccess).map(actionButton -> dialogControlSet.createActionButton((ActionButton)((Object)actionButton)).build()).toList();
        linearLayout.addChild(ButtonListDialogScreen.packControlsIntoColumns(list, buttonListDialog.columns()));
    }

    protected abstract Stream<ActionButton> createListActions(T var1, DialogConnectionAccess var2);

    @Override
    protected void updateHeaderAndFooter(HeaderAndFooterLayout headerAndFooterLayout, DialogControlSet dialogControlSet, T buttonListDialog, DialogConnectionAccess dialogConnectionAccess) {
        super.updateHeaderAndFooter(headerAndFooterLayout, dialogControlSet, buttonListDialog, dialogConnectionAccess);
        buttonListDialog.exitAction().ifPresentOrElse(actionButton -> headerAndFooterLayout.addToFooter(dialogControlSet.createActionButton((ActionButton)((Object)actionButton)).build()), () -> headerAndFooterLayout.setFooterHeight(5));
    }
}

