/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector2i
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.components.events;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ContainerEventHandler
extends GuiEventListener {
    public List<? extends GuiEventListener> children();

    default public Optional<GuiEventListener> getChildAt(double d, double e) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!guiEventListener.isMouseOver(d, e)) continue;
            return Optional.of(guiEventListener);
        }
        return Optional.empty();
    }

    @Override
    default public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        Optional<GuiEventListener> optional = this.getChildAt(mouseButtonEvent.x(), mouseButtonEvent.y());
        if (optional.isEmpty()) {
            return false;
        }
        GuiEventListener guiEventListener = optional.get();
        if (guiEventListener.mouseClicked(mouseButtonEvent, bl) && guiEventListener.shouldTakeFocusAfterInteraction()) {
            this.setFocused(guiEventListener);
            if (mouseButtonEvent.button() == 0) {
                this.setDragging(true);
            }
        }
        return true;
    }

    @Override
    default public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() == 0 && this.isDragging()) {
            this.setDragging(false);
            if (this.getFocused() != null) {
                return this.getFocused().mouseReleased(mouseButtonEvent);
            }
        }
        return false;
    }

    @Override
    default public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double d, double e) {
        if (this.getFocused() != null && this.isDragging() && mouseButtonEvent.button() == 0) {
            return this.getFocused().mouseDragged(mouseButtonEvent, d, e);
        }
        return false;
    }

    public boolean isDragging();

    public void setDragging(boolean var1);

    @Override
    default public boolean mouseScrolled(double d, double e, double f, double g) {
        return this.getChildAt(d, e).filter(guiEventListener -> guiEventListener.mouseScrolled(d, e, f, g)).isPresent();
    }

    @Override
    default public boolean keyPressed(KeyEvent keyEvent) {
        return this.getFocused() != null && this.getFocused().keyPressed(keyEvent);
    }

    @Override
    default public boolean keyReleased(KeyEvent keyEvent) {
        return this.getFocused() != null && this.getFocused().keyReleased(keyEvent);
    }

    @Override
    default public boolean charTyped(CharacterEvent characterEvent) {
        return this.getFocused() != null && this.getFocused().charTyped(characterEvent);
    }

    public @Nullable GuiEventListener getFocused();

    public void setFocused(@Nullable GuiEventListener var1);

    @Override
    default public void setFocused(boolean bl) {
    }

    @Override
    default public boolean isFocused() {
        return this.getFocused() != null;
    }

    @Override
    default public @Nullable ComponentPath getCurrentFocusPath() {
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener != null) {
            return ComponentPath.path(this, guiEventListener.getCurrentFocusPath());
        }
        return null;
    }

    @Override
    default public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        ComponentPath componentPath;
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener != null && (componentPath = guiEventListener.nextFocusPath(focusNavigationEvent)) != null) {
            return ComponentPath.path(this, componentPath);
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation) {
            FocusNavigationEvent.TabNavigation tabNavigation = (FocusNavigationEvent.TabNavigation)focusNavigationEvent;
            return this.handleTabNavigation(tabNavigation);
        }
        if (focusNavigationEvent instanceof FocusNavigationEvent.ArrowNavigation) {
            FocusNavigationEvent.ArrowNavigation arrowNavigation = (FocusNavigationEvent.ArrowNavigation)focusNavigationEvent;
            return this.handleArrowNavigation(arrowNavigation);
        }
        return null;
    }

    private @Nullable ComponentPath handleTabNavigation(FocusNavigationEvent.TabNavigation tabNavigation) {
        Supplier<GuiEventListener> supplier;
        BooleanSupplier booleanSupplier;
        boolean bl = tabNavigation.forward();
        GuiEventListener guiEventListener2 = this.getFocused();
        ArrayList<? extends GuiEventListener> list = new ArrayList<GuiEventListener>(this.children());
        Collections.sort(list, Comparator.comparingInt(guiEventListener -> guiEventListener.getTabOrderGroup()));
        int i = list.indexOf(guiEventListener2);
        int j = guiEventListener2 != null && i >= 0 ? i + (bl ? 1 : 0) : (bl ? 0 : list.size());
        ListIterator listIterator = list.listIterator(j);
        BooleanSupplier booleanSupplier2 = bl ? listIterator::hasNext : (booleanSupplier = listIterator::hasPrevious);
        Supplier<GuiEventListener> supplier2 = bl ? listIterator::next : (supplier = listIterator::previous);
        while (booleanSupplier.getAsBoolean()) {
            GuiEventListener guiEventListener22 = supplier.get();
            ComponentPath componentPath = guiEventListener22.nextFocusPath(tabNavigation);
            if (componentPath == null) continue;
            return ComponentPath.path(this, componentPath);
        }
        return null;
    }

    private @Nullable ComponentPath handleArrowNavigation(FocusNavigationEvent.ArrowNavigation arrowNavigation) {
        GuiEventListener guiEventListener = this.getFocused();
        if (guiEventListener == null) {
            ScreenDirection screenDirection = arrowNavigation.direction();
            ScreenRectangle screenRectangle = this.getBorderForArrowNavigation(screenDirection.getOpposite());
            return ComponentPath.path(this, this.nextFocusPathInDirection(screenRectangle, screenDirection, null, arrowNavigation));
        }
        ScreenRectangle screenRectangle2 = guiEventListener.getRectangle();
        return ComponentPath.path(this, this.nextFocusPathInDirection(screenRectangle2, arrowNavigation.direction(), guiEventListener, arrowNavigation));
    }

    private @Nullable ComponentPath nextFocusPathInDirection(ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener2, FocusNavigationEvent focusNavigationEvent) {
        ScreenAxis screenAxis = screenDirection.getAxis();
        ScreenAxis screenAxis2 = screenAxis.orthogonal();
        ScreenDirection screenDirection2 = screenAxis2.getPositive();
        int i = screenRectangle.getBoundInDirection(screenDirection.getOpposite());
        ArrayList<GuiEventListener> list = new ArrayList<GuiEventListener>();
        for (GuiEventListener guiEventListener3 : this.children()) {
            ScreenRectangle screenRectangle2;
            if (guiEventListener3 == guiEventListener2 || !(screenRectangle2 = guiEventListener3.getRectangle()).overlapsInAxis(screenRectangle, screenAxis2)) continue;
            int j = screenRectangle2.getBoundInDirection(screenDirection.getOpposite());
            if (screenDirection.isAfter(j, i)) {
                list.add(guiEventListener3);
                continue;
            }
            if (j != i || !screenDirection.isAfter(screenRectangle2.getBoundInDirection(screenDirection), screenRectangle.getBoundInDirection(screenDirection))) continue;
            list.add(guiEventListener3);
        }
        Comparator<GuiEventListener> comparator = Comparator.comparing(guiEventListener -> guiEventListener.getRectangle().getBoundInDirection(screenDirection.getOpposite()), screenDirection.coordinateValueComparator());
        Comparator<GuiEventListener> comparator2 = Comparator.comparing(guiEventListener -> guiEventListener.getRectangle().getBoundInDirection(screenDirection2.getOpposite()), screenDirection2.coordinateValueComparator());
        list.sort(comparator.thenComparing(comparator2));
        for (GuiEventListener guiEventListener3 : list) {
            ComponentPath componentPath = guiEventListener3.nextFocusPath(focusNavigationEvent);
            if (componentPath == null) continue;
            return componentPath;
        }
        return this.nextFocusPathVaguelyInDirection(screenRectangle, screenDirection, guiEventListener2, focusNavigationEvent);
    }

    private @Nullable ComponentPath nextFocusPathVaguelyInDirection(ScreenRectangle screenRectangle, ScreenDirection screenDirection, @Nullable GuiEventListener guiEventListener, FocusNavigationEvent focusNavigationEvent) {
        ScreenAxis screenAxis = screenDirection.getAxis();
        ScreenAxis screenAxis2 = screenAxis.orthogonal();
        ArrayList<Pair> list = new ArrayList<Pair>();
        ScreenPosition screenPosition = ScreenPosition.of(screenAxis, screenRectangle.getBoundInDirection(screenDirection), screenRectangle.getCenterInAxis(screenAxis2));
        for (GuiEventListener guiEventListener2 : this.children()) {
            ScreenRectangle screenRectangle2;
            ScreenPosition screenPosition2;
            if (guiEventListener2 == guiEventListener || !screenDirection.isAfter((screenPosition2 = ScreenPosition.of(screenAxis, (screenRectangle2 = guiEventListener2.getRectangle()).getBoundInDirection(screenDirection.getOpposite()), screenRectangle2.getCenterInAxis(screenAxis2))).getCoordinate(screenAxis), screenPosition.getCoordinate(screenAxis))) continue;
            long l = Vector2i.distanceSquared((int)screenPosition.x(), (int)screenPosition.y(), (int)screenPosition2.x(), (int)screenPosition2.y());
            list.add(Pair.of((Object)guiEventListener2, (Object)l));
        }
        list.sort(Comparator.comparingDouble(Pair::getSecond));
        for (Pair pair : list) {
            ComponentPath componentPath = ((GuiEventListener)pair.getFirst()).nextFocusPath(focusNavigationEvent);
            if (componentPath == null) continue;
            return componentPath;
        }
        return null;
    }
}

