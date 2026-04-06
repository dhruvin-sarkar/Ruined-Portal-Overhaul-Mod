/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.events;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface GuiEventListener
extends TabOrderedElement {
    default public void mouseMoved(double d, double e) {
    }

    default public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        return false;
    }

    default public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        return false;
    }

    default public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
        return false;
    }

    default public boolean mouseScrolled(double d, double e, double f, double g) {
        return false;
    }

    default public boolean keyPressed(KeyEvent keyEvent) {
        return false;
    }

    default public boolean keyReleased(KeyEvent keyEvent) {
        return false;
    }

    default public boolean charTyped(CharacterEvent characterEvent) {
        return false;
    }

    default public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }

    default public boolean isMouseOver(double d, double e) {
        return false;
    }

    public void setFocused(boolean var1);

    public boolean isFocused();

    default public boolean shouldTakeFocusAfterInteraction() {
        return true;
    }

    default public @Nullable ComponentPath getCurrentFocusPath() {
        if (this.isFocused()) {
            return ComponentPath.leaf(this);
        }
        return null;
    }

    default public ScreenRectangle getRectangle() {
        return ScreenRectangle.empty();
    }

    default public ScreenRectangle getBorderForArrowNavigation(ScreenDirection screenDirection) {
        return this.getRectangle().getBorder(screenDirection);
    }
}

