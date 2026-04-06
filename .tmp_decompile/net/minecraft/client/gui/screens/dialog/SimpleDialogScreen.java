/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.dialog;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.gui.screens.dialog.DialogControlSet;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.SimpleDialog;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SimpleDialogScreen<T extends SimpleDialog>
extends DialogScreen<T> {
    public SimpleDialogScreen(@Nullable Screen screen, T simpleDialog, DialogConnectionAccess dialogConnectionAccess) {
        super(screen, simpleDialog, dialogConnectionAccess);
    }

    @Override
    protected void updateHeaderAndFooter(HeaderAndFooterLayout headerAndFooterLayout, DialogControlSet dialogControlSet, T simpleDialog, DialogConnectionAccess dialogConnectionAccess) {
        super.updateHeaderAndFooter(headerAndFooterLayout, dialogControlSet, simpleDialog, dialogConnectionAccess);
        LinearLayout linearLayout = LinearLayout.horizontal().spacing(8);
        for (ActionButton actionButton : simpleDialog.mainActions()) {
            linearLayout.addChild(dialogControlSet.createActionButton(actionButton).build());
        }
        headerAndFooterLayout.addToFooter(linearLayout);
    }
}

