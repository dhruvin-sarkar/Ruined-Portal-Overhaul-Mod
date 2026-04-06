/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.alchemy;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;

public record PotionContents(Optional<Holder<Potion>> potion, Optional<Integer> customColor, List<MobEffectInstance> customEffects, Optional<String> customName) implements ConsumableListener,
TooltipProvider
{
    private final List<MobEffectInstance> customEffects;
    public static final PotionContents EMPTY = new PotionContents(Optional.empty(), Optional.empty(), List.of(), Optional.empty());
    private static final Component NO_EFFECT = Component.translatable("effect.none").withStyle(ChatFormatting.GRAY);
    public static final int BASE_POTION_COLOR = -13083194;
    private static final Codec<PotionContents> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Potion.CODEC.optionalFieldOf("potion").forGetter(PotionContents::potion), (App)Codec.INT.optionalFieldOf("custom_color").forGetter(PotionContents::customColor), (App)MobEffectInstance.CODEC.listOf().optionalFieldOf("custom_effects", (Object)List.of()).forGetter(PotionContents::customEffects), (App)Codec.STRING.optionalFieldOf("custom_name").forGetter(PotionContents::customName)).apply((Applicative)instance, PotionContents::new));
    public static final Codec<PotionContents> CODEC = Codec.withAlternative(FULL_CODEC, Potion.CODEC, PotionContents::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, PotionContents> STREAM_CODEC = StreamCodec.composite(Potion.STREAM_CODEC.apply(ByteBufCodecs::optional), PotionContents::potion, ByteBufCodecs.INT.apply(ByteBufCodecs::optional), PotionContents::customColor, MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()), PotionContents::customEffects, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional), PotionContents::customName, PotionContents::new);

    public PotionContents(Holder<Potion> holder) {
        this(Optional.of(holder), Optional.empty(), List.of(), Optional.empty());
    }

    public static ItemStack createItemStack(Item item, Holder<Potion> holder) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder));
        return itemStack;
    }

    public boolean is(Holder<Potion> holder) {
        return this.potion.isPresent() && this.potion.get().is(holder) && this.customEffects.isEmpty();
    }

    public Iterable<MobEffectInstance> getAllEffects() {
        if (this.potion.isEmpty()) {
            return this.customEffects;
        }
        if (this.customEffects.isEmpty()) {
            return this.potion.get().value().getEffects();
        }
        return Iterables.concat(this.potion.get().value().getEffects(), this.customEffects);
    }

    public void forEachEffect(Consumer<MobEffectInstance> consumer, float f) {
        if (this.potion.isPresent()) {
            for (MobEffectInstance mobEffectInstance : this.potion.get().value().getEffects()) {
                consumer.accept(mobEffectInstance.withScaledDuration(f));
            }
        }
        for (MobEffectInstance mobEffectInstance : this.customEffects) {
            consumer.accept(mobEffectInstance.withScaledDuration(f));
        }
    }

    public PotionContents withPotion(Holder<Potion> holder) {
        return new PotionContents(Optional.of(holder), this.customColor, this.customEffects, this.customName);
    }

    public PotionContents withEffectAdded(MobEffectInstance mobEffectInstance) {
        return new PotionContents(this.potion, this.customColor, Util.copyAndAdd(this.customEffects, mobEffectInstance), this.customName);
    }

    public int getColor() {
        return this.getColorOr(-13083194);
    }

    public int getColorOr(int i) {
        if (this.customColor.isPresent()) {
            return this.customColor.get();
        }
        return PotionContents.getColorOptional(this.getAllEffects()).orElse(i);
    }

    public Component getName(String string) {
        String string2 = this.customName.or(() -> this.potion.map(holder -> ((Potion)holder.value()).name())).orElse("empty");
        return Component.translatable(string + string2);
    }

    public static OptionalInt getColorOptional(Iterable<MobEffectInstance> iterable) {
        int i = 0;
        int j = 0;
        int k = 0;
        int l = 0;
        for (MobEffectInstance mobEffectInstance : iterable) {
            if (!mobEffectInstance.isVisible()) continue;
            int m = mobEffectInstance.getEffect().value().getColor();
            int n = mobEffectInstance.getAmplifier() + 1;
            i += n * ARGB.red(m);
            j += n * ARGB.green(m);
            k += n * ARGB.blue(m);
            l += n;
        }
        if (l == 0) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(ARGB.color(i / l, j / l, k / l));
    }

    public boolean hasEffects() {
        if (!this.customEffects.isEmpty()) {
            return true;
        }
        return this.potion.isPresent() && !this.potion.get().value().getEffects().isEmpty();
    }

    public List<MobEffectInstance> customEffects() {
        return Lists.transform(this.customEffects, MobEffectInstance::new);
    }

    public void applyToLivingEntity(LivingEntity livingEntity, float f) {
        Player player;
        Level level = livingEntity.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        Player player2 = livingEntity instanceof Player ? (player = (Player)livingEntity) : null;
        this.forEachEffect(mobEffectInstance -> {
            if (mobEffectInstance.getEffect().value().isInstantenous()) {
                mobEffectInstance.getEffect().value().applyInstantenousEffect(serverLevel, player2, player2, livingEntity, mobEffectInstance.getAmplifier(), 1.0);
            } else {
                livingEntity.addEffect((MobEffectInstance)mobEffectInstance);
            }
        }, f);
    }

    public static void addPotionTooltip(Iterable<MobEffectInstance> iterable, Consumer<Component> consumer, float f, float g) {
        ArrayList list = Lists.newArrayList();
        boolean bl = true;
        for (MobEffectInstance mobEffectInstance : iterable) {
            bl = false;
            Holder<MobEffect> holder2 = mobEffectInstance.getEffect();
            int i = mobEffectInstance.getAmplifier();
            holder2.value().createModifiers(i, (holder, attributeModifier) -> list.add(new Pair(holder, (Object)attributeModifier)));
            MutableComponent mutableComponent = PotionContents.getPotionDescription(holder2, i);
            if (!mobEffectInstance.endsWithin(20)) {
                mutableComponent = Component.translatable("potion.withDuration", mutableComponent, MobEffectUtil.formatDuration(mobEffectInstance, f, g));
            }
            consumer.accept(mutableComponent.withStyle(holder2.value().getCategory().getTooltipFormatting()));
        }
        if (bl) {
            consumer.accept(NO_EFFECT);
        }
        if (!list.isEmpty()) {
            consumer.accept(CommonComponents.EMPTY);
            consumer.accept(Component.translatable("potion.whenDrank").withStyle(ChatFormatting.DARK_PURPLE));
            for (Pair pair : list) {
                AttributeModifier attributeModifier2 = (AttributeModifier)((Object)pair.getSecond());
                double d = attributeModifier2.amount();
                double e = attributeModifier2.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE || attributeModifier2.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL ? attributeModifier2.amount() * 100.0 : attributeModifier2.amount();
                if (d > 0.0) {
                    consumer.accept(Component.translatable("attribute.modifier.plus." + attributeModifier2.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e), Component.translatable(((Attribute)((Holder)pair.getFirst()).value()).getDescriptionId())).withStyle(ChatFormatting.BLUE));
                    continue;
                }
                if (!(d < 0.0)) continue;
                consumer.accept(Component.translatable("attribute.modifier.take." + attributeModifier2.operation().id(), ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(e *= -1.0), Component.translatable(((Attribute)((Holder)pair.getFirst()).value()).getDescriptionId())).withStyle(ChatFormatting.RED));
            }
        }
    }

    public static MutableComponent getPotionDescription(Holder<MobEffect> holder, int i) {
        MutableComponent mutableComponent = Component.translatable(holder.value().getDescriptionId());
        if (i > 0) {
            return Component.translatable("potion.withAmplifier", mutableComponent, Component.translatable("potion.potency." + i));
        }
        return mutableComponent;
    }

    @Override
    public void onConsume(Level level, LivingEntity livingEntity, ItemStack itemStack, Consumable consumable) {
        this.applyToLivingEntity(livingEntity, itemStack.getOrDefault(DataComponents.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue());
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        PotionContents.addPotionTooltip(this.getAllEffects(), consumer, dataComponentGetter.getOrDefault(DataComponents.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue(), tooltipContext.tickRate());
    }
}

