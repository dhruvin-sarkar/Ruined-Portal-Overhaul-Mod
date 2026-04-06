/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Message
 *  com.mojang.datafixers.util.Either
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.mojang.brigadier.Message;
import com.mojang.datafixers.util.Either;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.NbtContents;
import net.minecraft.network.chat.contents.ObjectContents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.ScoreContents;
import net.minecraft.network.chat.contents.SelectorContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.chat.contents.data.DataSource;
import net.minecraft.network.chat.contents.objects.ObjectInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.ChunkPos;
import org.jspecify.annotations.Nullable;

public interface Component
extends Message,
FormattedText {
    public Style getStyle();

    public ComponentContents getContents();

    @Override
    default public String getString() {
        return FormattedText.super.getString();
    }

    default public String getString(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int j = i - stringBuilder.length();
            if (j <= 0) {
                return STOP_ITERATION;
            }
            stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
            return Optional.empty();
        });
        return stringBuilder.toString();
    }

    public List<Component> getSiblings();

    default public @Nullable String tryCollapseToString() {
        ComponentContents componentContents = this.getContents();
        if (componentContents instanceof PlainTextContents) {
            PlainTextContents plainTextContents = (PlainTextContents)componentContents;
            if (this.getSiblings().isEmpty() && this.getStyle().isEmpty()) {
                return plainTextContents.text();
            }
        }
        return null;
    }

    default public MutableComponent plainCopy() {
        return MutableComponent.create(this.getContents());
    }

    default public MutableComponent copy() {
        return new MutableComponent(this.getContents(), new ArrayList<Component>(this.getSiblings()), this.getStyle());
    }

    public FormattedCharSequence getVisualOrderText();

    @Override
    default public <T> Optional<T> visit(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style) {
        Style style2 = this.getStyle().applyTo(style);
        Optional<T> optional = this.getContents().visit(styledContentConsumer, style2);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(styledContentConsumer, style2);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    @Override
    default public <T> Optional<T> visit(FormattedText.ContentConsumer<T> contentConsumer) {
        Optional<T> optional = this.getContents().visit(contentConsumer);
        if (optional.isPresent()) {
            return optional;
        }
        for (Component component : this.getSiblings()) {
            Optional<T> optional2 = component.visit(contentConsumer);
            if (!optional2.isPresent()) continue;
            return optional2;
        }
        return Optional.empty();
    }

    default public List<Component> toFlatList() {
        return this.toFlatList(Style.EMPTY);
    }

    default public List<Component> toFlatList(Style style2) {
        ArrayList list = Lists.newArrayList();
        this.visit((style, string) -> {
            if (!string.isEmpty()) {
                list.add(Component.literal(string).withStyle(style));
            }
            return Optional.empty();
        }, style2);
        return list;
    }

    default public boolean contains(Component component) {
        List<Component> list2;
        if (this.equals(component)) {
            return true;
        }
        List<Component> list = this.toFlatList();
        return Collections.indexOfSubList(list, list2 = component.toFlatList(this.getStyle())) != -1;
    }

    public static Component nullToEmpty(@Nullable String string) {
        return string != null ? Component.literal(string) : CommonComponents.EMPTY;
    }

    public static MutableComponent literal(String string) {
        return MutableComponent.create(PlainTextContents.create(string));
    }

    public static MutableComponent translatable(String string) {
        return MutableComponent.create(new TranslatableContents(string, null, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatable(String string, Object ... objects) {
        return MutableComponent.create(new TranslatableContents(string, null, objects));
    }

    public static MutableComponent translatableEscape(String string, Object ... objects) {
        for (int i = 0; i < objects.length; ++i) {
            Object object = objects[i];
            if (TranslatableContents.isAllowedPrimitiveArgument(object) || object instanceof Component) continue;
            objects[i] = String.valueOf(object);
        }
        return Component.translatable(string, objects);
    }

    public static MutableComponent translatableWithFallback(String string, @Nullable String string2) {
        return MutableComponent.create(new TranslatableContents(string, string2, TranslatableContents.NO_ARGS));
    }

    public static MutableComponent translatableWithFallback(String string, @Nullable String string2, Object ... objects) {
        return MutableComponent.create(new TranslatableContents(string, string2, objects));
    }

    public static MutableComponent empty() {
        return MutableComponent.create(PlainTextContents.EMPTY);
    }

    public static MutableComponent keybind(String string) {
        return MutableComponent.create(new KeybindContents(string));
    }

    public static MutableComponent nbt(String string, boolean bl, Optional<Component> optional, DataSource dataSource) {
        return MutableComponent.create(new NbtContents(string, bl, optional, dataSource));
    }

    public static MutableComponent score(SelectorPattern selectorPattern, String string) {
        return MutableComponent.create(new ScoreContents((Either<SelectorPattern, String>)Either.left((Object)((Object)selectorPattern)), string));
    }

    public static MutableComponent score(String string, String string2) {
        return MutableComponent.create(new ScoreContents((Either<SelectorPattern, String>)Either.right((Object)string), string2));
    }

    public static MutableComponent selector(SelectorPattern selectorPattern, Optional<Component> optional) {
        return MutableComponent.create(new SelectorContents(selectorPattern, optional));
    }

    public static MutableComponent object(ObjectInfo objectInfo) {
        return MutableComponent.create(new ObjectContents(objectInfo));
    }

    public static Component translationArg(Date date) {
        return Component.literal(date.toString());
    }

    public static Component translationArg(Message message) {
        Component component;
        if (message instanceof Component) {
            Component component2 = (Component)message;
            component = component2;
        } else {
            component = Component.literal(message.getString());
        }
        return component;
    }

    public static Component translationArg(UUID uUID) {
        return Component.literal(uUID.toString());
    }

    public static Component translationArg(Identifier identifier) {
        return Component.literal(identifier.toString());
    }

    public static Component translationArg(ChunkPos chunkPos) {
        return Component.literal(chunkPos.toString());
    }

    public static Component translationArg(URI uRI) {
        return Component.literal(uRI.toString());
    }
}

