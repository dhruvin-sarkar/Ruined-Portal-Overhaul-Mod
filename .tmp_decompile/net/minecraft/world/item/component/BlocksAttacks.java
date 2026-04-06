/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record BlocksAttacks(float blockDelaySeconds, float disableCooldownScale, List<DamageReduction> damageReductions, ItemDamageFunction itemDamage, Optional<TagKey<DamageType>> bypassedBy, Optional<Holder<SoundEvent>> blockSound, Optional<Holder<SoundEvent>> disableSound) {
    public static final Codec<BlocksAttacks> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("block_delay_seconds", (Object)Float.valueOf(0.0f)).forGetter(BlocksAttacks::blockDelaySeconds), (App)ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("disable_cooldown_scale", (Object)Float.valueOf(1.0f)).forGetter(BlocksAttacks::disableCooldownScale), (App)DamageReduction.CODEC.listOf().optionalFieldOf("damage_reductions", (Object)List.of((Object)((Object)new DamageReduction(90.0f, Optional.empty(), 0.0f, 1.0f)))).forGetter(BlocksAttacks::damageReductions), (App)ItemDamageFunction.CODEC.optionalFieldOf("item_damage", (Object)ItemDamageFunction.DEFAULT).forGetter(BlocksAttacks::itemDamage), (App)TagKey.hashedCodec(Registries.DAMAGE_TYPE).optionalFieldOf("bypassed_by").forGetter(BlocksAttacks::bypassedBy), (App)SoundEvent.CODEC.optionalFieldOf("block_sound").forGetter(BlocksAttacks::blockSound), (App)SoundEvent.CODEC.optionalFieldOf("disabled_sound").forGetter(BlocksAttacks::disableSound)).apply((Applicative)instance, BlocksAttacks::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, BlocksAttacks> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, BlocksAttacks::blockDelaySeconds, ByteBufCodecs.FLOAT, BlocksAttacks::disableCooldownScale, DamageReduction.STREAM_CODEC.apply(ByteBufCodecs.list()), BlocksAttacks::damageReductions, ItemDamageFunction.STREAM_CODEC, BlocksAttacks::itemDamage, TagKey.streamCodec(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional), BlocksAttacks::bypassedBy, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), BlocksAttacks::blockSound, SoundEvent.STREAM_CODEC.apply(ByteBufCodecs::optional), BlocksAttacks::disableSound, BlocksAttacks::new);

    public void onBlocked(ServerLevel serverLevel, LivingEntity livingEntity) {
        this.blockSound.ifPresent(holder -> serverLevel.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (Holder<SoundEvent>)holder, livingEntity.getSoundSource(), 1.0f, 0.8f + serverLevel.random.nextFloat() * 0.4f));
    }

    public void disable(ServerLevel serverLevel, LivingEntity livingEntity, float f, ItemStack itemStack) {
        int i = this.disableBlockingForTicks(f);
        if (i > 0) {
            if (livingEntity instanceof Player) {
                Player player = (Player)livingEntity;
                player.getCooldowns().addCooldown(itemStack, i);
            }
            livingEntity.stopUsingItem();
            this.disableSound.ifPresent(holder -> serverLevel.playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), (Holder<SoundEvent>)holder, livingEntity.getSoundSource(), 0.8f, 0.8f + serverLevel.random.nextFloat() * 0.4f));
        }
    }

    public void hurtBlockingItem(Level level, ItemStack itemStack, LivingEntity livingEntity, InteractionHand interactionHand, float f) {
        int i;
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        if (!level.isClientSide()) {
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
        }
        if ((i = this.itemDamage.apply(f)) > 0) {
            itemStack.hurtAndBreak(i, livingEntity, interactionHand.asEquipmentSlot());
        }
    }

    private int disableBlockingForTicks(float f) {
        float g = f * this.disableCooldownScale;
        if (g > 0.0f) {
            return Math.round(g * 20.0f);
        }
        return 0;
    }

    public int blockDelayTicks() {
        return Math.round(this.blockDelaySeconds * 20.0f);
    }

    public float resolveBlockedDamage(DamageSource damageSource, float f, double d) {
        float g = 0.0f;
        for (DamageReduction damageReduction : this.damageReductions) {
            g += damageReduction.resolve(damageSource, f, d);
        }
        return Mth.clamp(g, 0.0f, f);
    }

    public record ItemDamageFunction(float threshold, float base, float factor) {
        public static final Codec<ItemDamageFunction> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("threshold").forGetter(ItemDamageFunction::threshold), (App)Codec.FLOAT.fieldOf("base").forGetter(ItemDamageFunction::base), (App)Codec.FLOAT.fieldOf("factor").forGetter(ItemDamageFunction::factor)).apply((Applicative)instance, ItemDamageFunction::new));
        public static final StreamCodec<ByteBuf, ItemDamageFunction> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, ItemDamageFunction::threshold, ByteBufCodecs.FLOAT, ItemDamageFunction::base, ByteBufCodecs.FLOAT, ItemDamageFunction::factor, ItemDamageFunction::new);
        public static final ItemDamageFunction DEFAULT = new ItemDamageFunction(1.0f, 0.0f, 1.0f);

        public int apply(float f) {
            if (f < this.threshold) {
                return 0;
            }
            return Mth.floor(this.base + this.factor * f);
        }
    }

    public record DamageReduction(float horizontalBlockingAngle, Optional<HolderSet<DamageType>> type, float base, float factor) {
        public static final Codec<DamageReduction> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("horizontal_blocking_angle", (Object)Float.valueOf(90.0f)).forGetter(DamageReduction::horizontalBlockingAngle), (App)RegistryCodecs.homogeneousList(Registries.DAMAGE_TYPE).optionalFieldOf("type").forGetter(DamageReduction::type), (App)Codec.FLOAT.fieldOf("base").forGetter(DamageReduction::base), (App)Codec.FLOAT.fieldOf("factor").forGetter(DamageReduction::factor)).apply((Applicative)instance, DamageReduction::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, DamageReduction> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, DamageReduction::horizontalBlockingAngle, ByteBufCodecs.holderSet(Registries.DAMAGE_TYPE).apply(ByteBufCodecs::optional), DamageReduction::type, ByteBufCodecs.FLOAT, DamageReduction::base, ByteBufCodecs.FLOAT, DamageReduction::factor, DamageReduction::new);

        public float resolve(DamageSource damageSource, float f, double d) {
            if (d > (double)((float)Math.PI / 180 * this.horizontalBlockingAngle)) {
                return 0.0f;
            }
            if (this.type.isPresent() && !this.type.get().contains(damageSource.typeHolder())) {
                return 0.0f;
            }
            return Mth.clamp(this.base + this.factor * f, 0.0f, f);
        }
    }
}

