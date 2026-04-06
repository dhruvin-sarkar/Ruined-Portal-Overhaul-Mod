/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.chat;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public interface HoverEvent {
    public static final Codec<HoverEvent> CODEC = Action.CODEC.dispatch("action", HoverEvent::action, action -> action.codec);

    public Action action();

    public static enum Action implements StringRepresentable
    {
        SHOW_TEXT("show_text", true, ShowText.CODEC),
        SHOW_ITEM("show_item", true, ShowItem.CODEC),
        SHOW_ENTITY("show_entity", true, ShowEntity.CODEC);

        public static final Codec<Action> UNSAFE_CODEC;
        public static final Codec<Action> CODEC;
        private final String name;
        private final boolean allowFromServer;
        final MapCodec<? extends HoverEvent> codec;

        private Action(String string2, boolean bl, MapCodec<? extends HoverEvent> mapCodec) {
            this.name = string2;
            this.allowFromServer = bl;
            this.codec = mapCodec;
        }

        public boolean isAllowedFromServer() {
            return this.allowFromServer;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public String toString() {
            return "<action " + this.name + ">";
        }

        private static DataResult<Action> filterForSerialization(Action action) {
            if (!action.isAllowedFromServer()) {
                return DataResult.error(() -> "Action not allowed: " + String.valueOf(action));
            }
            return DataResult.success((Object)action, (Lifecycle)Lifecycle.stable());
        }

        static {
            UNSAFE_CODEC = StringRepresentable.fromValues(Action::values);
            CODEC = UNSAFE_CODEC.validate(Action::filterForSerialization);
        }
    }

    public static class EntityTooltipInfo {
        public static final MapCodec<EntityTooltipInfo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("id").forGetter(entityTooltipInfo -> entityTooltipInfo.type), (App)UUIDUtil.LENIENT_CODEC.fieldOf("uuid").forGetter(entityTooltipInfo -> entityTooltipInfo.uuid), (App)ComponentSerialization.CODEC.optionalFieldOf("name").forGetter(entityTooltipInfo -> entityTooltipInfo.name)).apply((Applicative)instance, EntityTooltipInfo::new));
        public final EntityType<?> type;
        public final UUID uuid;
        public final Optional<Component> name;
        private @Nullable List<Component> linesCache;

        public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, @Nullable Component component) {
            this(entityType, uUID, Optional.ofNullable(component));
        }

        public EntityTooltipInfo(EntityType<?> entityType, UUID uUID, Optional<Component> optional) {
            this.type = entityType;
            this.uuid = uUID;
            this.name = optional;
        }

        public List<Component> getTooltipLines() {
            if (this.linesCache == null) {
                this.linesCache = new ArrayList<Component>();
                this.name.ifPresent(this.linesCache::add);
                this.linesCache.add(Component.translatable("gui.entity_tooltip.type", this.type.getDescription()));
                this.linesCache.add(Component.literal(this.uuid.toString()));
            }
            return this.linesCache;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            EntityTooltipInfo entityTooltipInfo = (EntityTooltipInfo)object;
            return this.type.equals(entityTooltipInfo.type) && this.uuid.equals(entityTooltipInfo.uuid) && this.name.equals(entityTooltipInfo.name);
        }

        public int hashCode() {
            int i = this.type.hashCode();
            i = 31 * i + this.uuid.hashCode();
            i = 31 * i + this.name.hashCode();
            return i;
        }
    }

    public record ShowEntity(EntityTooltipInfo entity) implements HoverEvent
    {
        public static final MapCodec<ShowEntity> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)EntityTooltipInfo.CODEC.forGetter(ShowEntity::entity)).apply((Applicative)instance, ShowEntity::new));

        @Override
        public Action action() {
            return Action.SHOW_ENTITY;
        }
    }

    public record ShowItem(ItemStack item) implements HoverEvent
    {
        public static final MapCodec<ShowItem> CODEC = ItemStack.MAP_CODEC.xmap(ShowItem::new, ShowItem::item);

        public ShowItem(ItemStack itemStack) {
            this.item = itemStack = itemStack.copy();
        }

        @Override
        public Action action() {
            return Action.SHOW_ITEM;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (!(object instanceof ShowItem)) return false;
            ShowItem showItem = (ShowItem)object;
            if (!ItemStack.matches(this.item, showItem.item)) return false;
            return true;
        }

        public int hashCode() {
            return ItemStack.hashItemAndComponents(this.item);
        }
    }

    public record ShowText(Component value) implements HoverEvent
    {
        public static final MapCodec<ShowText> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ComponentSerialization.CODEC.fieldOf("value").forGetter(ShowText::value)).apply((Applicative)instance, ShowText::new));

        @Override
        public Action action() {
            return Action.SHOW_TEXT;
        }
    }
}

