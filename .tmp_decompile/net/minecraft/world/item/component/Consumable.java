/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.PlaySoundConsumeEffect;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public record Consumable(float consumeSeconds, ItemUseAnimation animation, Holder<SoundEvent> sound, boolean hasConsumeParticles, List<ConsumeEffect> onConsumeEffects) {
    public static final float DEFAULT_CONSUME_SECONDS = 1.6f;
    private static final int CONSUME_EFFECTS_INTERVAL = 4;
    private static final float CONSUME_EFFECTS_START_FRACTION = 0.21875f;
    public static final Codec<Consumable> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("consume_seconds", (Object)Float.valueOf(1.6f)).forGetter(Consumable::consumeSeconds), (App)ItemUseAnimation.CODEC.optionalFieldOf("animation", (Object)ItemUseAnimation.EAT).forGetter(Consumable::animation), (App)SoundEvent.CODEC.optionalFieldOf("sound", SoundEvents.GENERIC_EAT).forGetter(Consumable::sound), (App)Codec.BOOL.optionalFieldOf("has_consume_particles", (Object)true).forGetter(Consumable::hasConsumeParticles), (App)ConsumeEffect.CODEC.listOf().optionalFieldOf("on_consume_effects", (Object)List.of()).forGetter(Consumable::onConsumeEffects)).apply((Applicative)instance, Consumable::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Consumable> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, Consumable::consumeSeconds, ItemUseAnimation.STREAM_CODEC, Consumable::animation, SoundEvent.STREAM_CODEC, Consumable::sound, ByteBufCodecs.BOOL, Consumable::hasConsumeParticles, ConsumeEffect.STREAM_CODEC.apply(ByteBufCodecs.list()), Consumable::onConsumeEffects, Consumable::new);

    public InteractionResult startConsuming(LivingEntity livingEntity, ItemStack itemStack, InteractionHand interactionHand) {
        boolean bl;
        if (!this.canConsume(livingEntity, itemStack)) {
            return InteractionResult.FAIL;
        }
        boolean bl2 = bl = this.consumeTicks() > 0;
        if (bl) {
            livingEntity.startUsingItem(interactionHand);
            return InteractionResult.CONSUME;
        }
        ItemStack itemStack2 = this.onConsume(livingEntity.level(), livingEntity, itemStack);
        return InteractionResult.CONSUME.heldItemTransformedTo(itemStack2);
    }

    public ItemStack onConsume(Level level, LivingEntity livingEntity, ItemStack itemStack) {
        RandomSource randomSource = livingEntity.getRandom();
        this.emitParticlesAndSounds(randomSource, livingEntity, itemStack, 16);
        if (livingEntity instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer)livingEntity;
            serverPlayer.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, itemStack);
        }
        itemStack.getAllOfType(ConsumableListener.class).forEach(consumableListener -> consumableListener.onConsume(level, livingEntity, itemStack, this));
        if (!level.isClientSide()) {
            this.onConsumeEffects.forEach(consumeEffect -> consumeEffect.apply(level, itemStack, livingEntity));
        }
        livingEntity.gameEvent(this.animation == ItemUseAnimation.DRINK ? GameEvent.DRINK : GameEvent.EAT);
        itemStack.consume(1, livingEntity);
        return itemStack;
    }

    public boolean canConsume(LivingEntity livingEntity, ItemStack itemStack) {
        FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
        if (foodProperties != null && livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            return player.canEat(foodProperties.canAlwaysEat());
        }
        return true;
    }

    public int consumeTicks() {
        return (int)(this.consumeSeconds * 20.0f);
    }

    public void emitParticlesAndSounds(RandomSource randomSource, LivingEntity livingEntity, ItemStack itemStack, int i) {
        SoundEvent soundEvent;
        float l;
        float f = randomSource.nextBoolean() ? 0.5f : 1.0f;
        float g = randomSource.triangle(1.0f, 0.2f);
        float h = 0.5f;
        float j = Mth.randomBetween(randomSource, 0.9f, 1.0f);
        float k = this.animation == ItemUseAnimation.DRINK ? 0.5f : f;
        float f2 = l = this.animation == ItemUseAnimation.DRINK ? j : g;
        if (this.hasConsumeParticles) {
            livingEntity.spawnItemParticles(itemStack, i);
        }
        if (livingEntity instanceof OverrideConsumeSound) {
            OverrideConsumeSound overrideConsumeSound = (OverrideConsumeSound)((Object)livingEntity);
            soundEvent = overrideConsumeSound.getConsumeSound(itemStack);
        } else {
            soundEvent = this.sound.value();
        }
        SoundEvent soundEvent2 = soundEvent;
        livingEntity.playSound(soundEvent2, k, l);
    }

    public boolean shouldEmitParticlesAndSounds(int i) {
        int k;
        int j = this.consumeTicks() - i;
        boolean bl = j > (k = (int)((float)this.consumeTicks() * 0.21875f));
        return bl && i % 4 == 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static interface OverrideConsumeSound {
        public SoundEvent getConsumeSound(ItemStack var1);
    }

    public static class Builder {
        private float consumeSeconds = 1.6f;
        private ItemUseAnimation animation = ItemUseAnimation.EAT;
        private Holder<SoundEvent> sound = SoundEvents.GENERIC_EAT;
        private boolean hasConsumeParticles = true;
        private final List<ConsumeEffect> onConsumeEffects = new ArrayList<ConsumeEffect>();

        Builder() {
        }

        public Builder consumeSeconds(float f) {
            this.consumeSeconds = f;
            return this;
        }

        public Builder animation(ItemUseAnimation itemUseAnimation) {
            this.animation = itemUseAnimation;
            return this;
        }

        public Builder sound(Holder<SoundEvent> holder) {
            this.sound = holder;
            return this;
        }

        public Builder soundAfterConsume(Holder<SoundEvent> holder) {
            return this.onConsume(new PlaySoundConsumeEffect(holder));
        }

        public Builder hasConsumeParticles(boolean bl) {
            this.hasConsumeParticles = bl;
            return this;
        }

        public Builder onConsume(ConsumeEffect consumeEffect) {
            this.onConsumeEffects.add(consumeEffect);
            return this;
        }

        public Consumable build() {
            return new Consumable(this.consumeSeconds, this.animation, this.sound, this.hasConsumeParticles, this.onConsumeEffects);
        }
    }
}

