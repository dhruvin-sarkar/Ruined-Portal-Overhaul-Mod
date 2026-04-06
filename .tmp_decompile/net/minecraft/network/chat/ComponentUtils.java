/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.datafixers.DataFixUtils
 *  java.lang.MatchException
 *  javax.annotation.CheckReturnValue
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.DataFixUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.CheckReturnValue;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public class ComponentUtils {
    public static final String DEFAULT_SEPARATOR_TEXT = ", ";
    public static final Component DEFAULT_SEPARATOR = Component.literal(", ").withStyle(ChatFormatting.GRAY);
    public static final Component DEFAULT_NO_STYLE_SEPARATOR = Component.literal(", ");

    @CheckReturnValue
    public static MutableComponent mergeStyles(MutableComponent mutableComponent, Style style) {
        if (style.isEmpty()) {
            return mutableComponent;
        }
        Style style2 = mutableComponent.getStyle();
        if (style2.isEmpty()) {
            return mutableComponent.setStyle(style);
        }
        if (style2.equals(style)) {
            return mutableComponent;
        }
        return mutableComponent.setStyle(style2.applyTo(style));
    }

    @CheckReturnValue
    public static Component mergeStyles(Component component, Style style) {
        if (style.isEmpty()) {
            return component;
        }
        Style style2 = component.getStyle();
        if (style2.isEmpty()) {
            return component.copy().setStyle(style);
        }
        if (style2.equals(style)) {
            return component;
        }
        return component.copy().setStyle(style2.applyTo(style));
    }

    public static Optional<MutableComponent> updateForEntity(@Nullable CommandSourceStack commandSourceStack, Optional<Component> optional, @Nullable Entity entity, int i) throws CommandSyntaxException {
        return optional.isPresent() ? Optional.of(ComponentUtils.updateForEntity(commandSourceStack, optional.get(), entity, i)) : Optional.empty();
    }

    public static MutableComponent updateForEntity(@Nullable CommandSourceStack commandSourceStack, Component component, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (i > 100) {
            return component.copy();
        }
        MutableComponent mutableComponent = component.getContents().resolve(commandSourceStack, entity, i + 1);
        for (Component component2 : component.getSiblings()) {
            mutableComponent.append(ComponentUtils.updateForEntity(commandSourceStack, component2, entity, i + 1));
        }
        return mutableComponent.withStyle(ComponentUtils.resolveStyle(commandSourceStack, component.getStyle(), entity, i));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static Style resolveStyle(@Nullable CommandSourceStack commandSourceStack, Style style, @Nullable Entity entity, int i) throws CommandSyntaxException {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (!(hoverEvent instanceof HoverEvent.ShowText)) return style;
        HoverEvent.ShowText showText = (HoverEvent.ShowText)hoverEvent;
        try {
            Component component;
            Component component2 = component = showText.value();
            HoverEvent.ShowText hoverEvent2 = new HoverEvent.ShowText(ComponentUtils.updateForEntity(commandSourceStack, component2, entity, i + 1));
            return style.withHoverEvent(hoverEvent2);
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    public static Component formatList(Collection<String> collection) {
        return ComponentUtils.formatAndSortList(collection, string -> Component.literal(string).withStyle(ChatFormatting.GREEN));
    }

    public static <T extends Comparable<T>> Component formatAndSortList(Collection<T> collection, Function<T, Component> function) {
        if (collection.isEmpty()) {
            return CommonComponents.EMPTY;
        }
        if (collection.size() == 1) {
            return function.apply((Comparable)collection.iterator().next());
        }
        ArrayList list = Lists.newArrayList(collection);
        list.sort(Comparable::compareTo);
        return ComponentUtils.formatList(list, function);
    }

    public static <T> Component formatList(Collection<? extends T> collection, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, DEFAULT_SEPARATOR, function);
    }

    public static <T> MutableComponent formatList(Collection<? extends T> collection, Optional<? extends Component> optional, Function<T, Component> function) {
        return ComponentUtils.formatList(collection, (Component)DataFixUtils.orElse(optional, (Object)DEFAULT_SEPARATOR), function);
    }

    public static Component formatList(Collection<? extends Component> collection, Component component) {
        return ComponentUtils.formatList(collection, component, Function.identity());
    }

    public static <T> MutableComponent formatList(Collection<? extends T> collection, Component component, Function<T, Component> function) {
        if (collection.isEmpty()) {
            return Component.empty();
        }
        if (collection.size() == 1) {
            return function.apply(collection.iterator().next()).copy();
        }
        MutableComponent mutableComponent = Component.empty();
        boolean bl = true;
        for (T object : collection) {
            if (!bl) {
                mutableComponent.append(component);
            }
            mutableComponent.append(function.apply(object));
            bl = false;
        }
        return mutableComponent;
    }

    public static MutableComponent wrapInSquareBrackets(Component component) {
        return Component.translatable("chat.square_brackets", component);
    }

    public static Component fromMessage(Message message) {
        if (message instanceof Component) {
            Component component = (Component)message;
            return component;
        }
        return Component.literal(message.getString());
    }

    public static boolean isTranslationResolvable(@Nullable Component component) {
        ComponentContents componentContents;
        if (component != null && (componentContents = component.getContents()) instanceof TranslatableContents) {
            TranslatableContents translatableContents = (TranslatableContents)componentContents;
            String string = translatableContents.getKey();
            String string2 = translatableContents.getFallback();
            return string2 != null || Language.getInstance().has(string);
        }
        return true;
    }

    public static MutableComponent copyOnClickText(String string) {
        return ComponentUtils.wrapInSquareBrackets(Component.literal(string).withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent.CopyToClipboard(string)).withHoverEvent(new HoverEvent.ShowText(Component.translatable("chat.copy.click"))).withInsertion(string)));
    }
}

