/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jspecify.annotations.Nullable;

public record EntityEquipmentPredicate(Optional<ItemPredicate> head, Optional<ItemPredicate> chest, Optional<ItemPredicate> legs, Optional<ItemPredicate> feet, Optional<ItemPredicate> body, Optional<ItemPredicate> mainhand, Optional<ItemPredicate> offhand) {
    public static final Codec<EntityEquipmentPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ItemPredicate.CODEC.optionalFieldOf("head").forGetter(EntityEquipmentPredicate::head), (App)ItemPredicate.CODEC.optionalFieldOf("chest").forGetter(EntityEquipmentPredicate::chest), (App)ItemPredicate.CODEC.optionalFieldOf("legs").forGetter(EntityEquipmentPredicate::legs), (App)ItemPredicate.CODEC.optionalFieldOf("feet").forGetter(EntityEquipmentPredicate::feet), (App)ItemPredicate.CODEC.optionalFieldOf("body").forGetter(EntityEquipmentPredicate::body), (App)ItemPredicate.CODEC.optionalFieldOf("mainhand").forGetter(EntityEquipmentPredicate::mainhand), (App)ItemPredicate.CODEC.optionalFieldOf("offhand").forGetter(EntityEquipmentPredicate::offhand)).apply((Applicative)instance, EntityEquipmentPredicate::new));

    public static EntityEquipmentPredicate captainPredicate(HolderGetter<Item> holderGetter, HolderGetter<BannerPattern> holderGetter2) {
        return Builder.equipment().head(ItemPredicate.Builder.item().of(holderGetter, Items.WHITE_BANNER).withComponents(DataComponentMatchers.Builder.components().exact(DataComponentExactPredicate.someOf(Raid.getOminousBannerInstance(holderGetter2).getComponents(), DataComponents.BANNER_PATTERNS, DataComponents.ITEM_NAME)).build())).build();
    }

    public boolean matches(@Nullable Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity livingEntity = (LivingEntity)entity;
        if (this.head.isPresent() && !this.head.get().test(livingEntity.getItemBySlot(EquipmentSlot.HEAD))) {
            return false;
        }
        if (this.chest.isPresent() && !this.chest.get().test(livingEntity.getItemBySlot(EquipmentSlot.CHEST))) {
            return false;
        }
        if (this.legs.isPresent() && !this.legs.get().test(livingEntity.getItemBySlot(EquipmentSlot.LEGS))) {
            return false;
        }
        if (this.feet.isPresent() && !this.feet.get().test(livingEntity.getItemBySlot(EquipmentSlot.FEET))) {
            return false;
        }
        if (this.body.isPresent() && !this.body.get().test(livingEntity.getItemBySlot(EquipmentSlot.BODY))) {
            return false;
        }
        if (this.mainhand.isPresent() && !this.mainhand.get().test(livingEntity.getItemBySlot(EquipmentSlot.MAINHAND))) {
            return false;
        }
        return !this.offhand.isPresent() || this.offhand.get().test(livingEntity.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    public static class Builder {
        private Optional<ItemPredicate> head = Optional.empty();
        private Optional<ItemPredicate> chest = Optional.empty();
        private Optional<ItemPredicate> legs = Optional.empty();
        private Optional<ItemPredicate> feet = Optional.empty();
        private Optional<ItemPredicate> body = Optional.empty();
        private Optional<ItemPredicate> mainhand = Optional.empty();
        private Optional<ItemPredicate> offhand = Optional.empty();

        public static Builder equipment() {
            return new Builder();
        }

        public Builder head(ItemPredicate.Builder builder) {
            this.head = Optional.of(builder.build());
            return this;
        }

        public Builder chest(ItemPredicate.Builder builder) {
            this.chest = Optional.of(builder.build());
            return this;
        }

        public Builder legs(ItemPredicate.Builder builder) {
            this.legs = Optional.of(builder.build());
            return this;
        }

        public Builder feet(ItemPredicate.Builder builder) {
            this.feet = Optional.of(builder.build());
            return this;
        }

        public Builder body(ItemPredicate.Builder builder) {
            this.body = Optional.of(builder.build());
            return this;
        }

        public Builder mainhand(ItemPredicate.Builder builder) {
            this.mainhand = Optional.of(builder.build());
            return this;
        }

        public Builder offhand(ItemPredicate.Builder builder) {
            this.offhand = Optional.of(builder.build());
            return this;
        }

        public EntityEquipmentPredicate build() {
            return new EntityEquipmentPredicate(this.head, this.chest, this.legs, this.feet, this.body, this.mainhand, this.offhand);
        }
    }
}

