/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.item.component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.network.Filterable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.BookContent;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;

public record WrittenBookContent(Filterable<String> title, String author, int generation, List<Filterable<Component>> pages, boolean resolved) implements BookContent<Component, WrittenBookContent>,
TooltipProvider
{
    public static final WrittenBookContent EMPTY = new WrittenBookContent(Filterable.passThrough(""), "", 0, List.of(), true);
    public static final int PAGE_LENGTH = Short.MAX_VALUE;
    public static final int TITLE_LENGTH = 16;
    public static final int TITLE_MAX_LENGTH = 32;
    public static final int MAX_GENERATION = 3;
    public static final int MAX_CRAFTABLE_GENERATION = 2;
    public static final Codec<Component> CONTENT_CODEC = ComponentSerialization.flatRestrictedCodec(Short.MAX_VALUE);
    public static final Codec<List<Filterable<Component>>> PAGES_CODEC = WrittenBookContent.pagesCodec(CONTENT_CODEC);
    public static final Codec<WrittenBookContent> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Filterable.codec(Codec.string((int)0, (int)32)).fieldOf("title").forGetter(WrittenBookContent::title), (App)Codec.STRING.fieldOf("author").forGetter(WrittenBookContent::author), (App)ExtraCodecs.intRange(0, 3).optionalFieldOf("generation", (Object)0).forGetter(WrittenBookContent::generation), (App)PAGES_CODEC.optionalFieldOf("pages", (Object)List.of()).forGetter(WrittenBookContent::pages), (App)Codec.BOOL.optionalFieldOf("resolved", (Object)false).forGetter(WrittenBookContent::resolved)).apply((Applicative)instance, WrittenBookContent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, WrittenBookContent> STREAM_CODEC = StreamCodec.composite(Filterable.streamCodec(ByteBufCodecs.stringUtf8(32)), WrittenBookContent::title, ByteBufCodecs.STRING_UTF8, WrittenBookContent::author, ByteBufCodecs.VAR_INT, WrittenBookContent::generation, Filterable.streamCodec(ComponentSerialization.STREAM_CODEC).apply(ByteBufCodecs.list()), WrittenBookContent::pages, ByteBufCodecs.BOOL, WrittenBookContent::resolved, WrittenBookContent::new);

    public WrittenBookContent {
        if (i < 0 || i > 3) {
            throw new IllegalArgumentException("Generation was " + i + ", but must be between 0 and 3");
        }
    }

    private static Codec<Filterable<Component>> pageCodec(Codec<Component> codec) {
        return Filterable.codec(codec);
    }

    public static Codec<List<Filterable<Component>>> pagesCodec(Codec<Component> codec) {
        return WrittenBookContent.pageCodec(codec).listOf();
    }

    public @Nullable WrittenBookContent tryCraftCopy() {
        if (this.generation >= 2) {
            return null;
        }
        return new WrittenBookContent(this.title, this.author, this.generation + 1, this.pages, this.resolved);
    }

    public static boolean resolveForItem(ItemStack itemStack, CommandSourceStack commandSourceStack, @Nullable Player player) {
        WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
        if (writtenBookContent != null && !writtenBookContent.resolved()) {
            WrittenBookContent writtenBookContent2 = writtenBookContent.resolve(commandSourceStack, player);
            if (writtenBookContent2 != null) {
                itemStack.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContent2);
                return true;
            }
            itemStack.set(DataComponents.WRITTEN_BOOK_CONTENT, writtenBookContent.markResolved());
        }
        return false;
    }

    public @Nullable WrittenBookContent resolve(CommandSourceStack commandSourceStack, @Nullable Player player) {
        if (this.resolved) {
            return null;
        }
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)this.pages.size());
        for (Filterable<Component> filterable : this.pages) {
            Optional<Filterable<Component>> optional = WrittenBookContent.resolvePage(commandSourceStack, player, filterable);
            if (optional.isEmpty()) {
                return null;
            }
            builder.add(optional.get());
        }
        return new WrittenBookContent(this.title, this.author, this.generation, (List<Filterable<Component>>)builder.build(), true);
    }

    public WrittenBookContent markResolved() {
        return new WrittenBookContent(this.title, this.author, this.generation, this.pages, true);
    }

    private static Optional<Filterable<Component>> resolvePage(CommandSourceStack commandSourceStack, @Nullable Player player, Filterable<Component> filterable) {
        return filterable.resolve(component -> {
            try {
                MutableComponent component2 = ComponentUtils.updateForEntity(commandSourceStack, component, (Entity)player, 0);
                if (WrittenBookContent.isPageTooLarge(component2, commandSourceStack.registryAccess())) {
                    return Optional.empty();
                }
                return Optional.of(component2);
            }
            catch (Exception exception) {
                return Optional.of(component);
            }
        });
    }

    private static boolean isPageTooLarge(Component component, HolderLookup.Provider provider) {
        DataResult dataResult = ComponentSerialization.CODEC.encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), (Object)component);
        return dataResult.isSuccess() && GsonHelper.encodesLongerThan((JsonElement)dataResult.getOrThrow(), Short.MAX_VALUE);
    }

    public List<Component> getPages(boolean bl) {
        return Lists.transform(this.pages, filterable -> (Component)filterable.get(bl));
    }

    @Override
    public WrittenBookContent withReplacedPages(List<Filterable<Component>> list) {
        return new WrittenBookContent(this.title, this.author, this.generation, list, false);
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        if (!StringUtil.isBlank(this.author)) {
            consumer.accept(Component.translatable("book.byAuthor", this.author).withStyle(ChatFormatting.GRAY));
        }
        consumer.accept(Component.translatable("book.generation." + this.generation).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public /* synthetic */ Object withReplacedPages(List list) {
        return this.withReplacedPages(list);
    }
}

