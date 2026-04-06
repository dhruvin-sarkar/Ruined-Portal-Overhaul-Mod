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
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record UseCooldown(float seconds, Optional<Identifier> cooldownGroup) {
    public static final Codec<UseCooldown> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.POSITIVE_FLOAT.fieldOf("seconds").forGetter(UseCooldown::seconds), (App)Identifier.CODEC.optionalFieldOf("cooldown_group").forGetter(UseCooldown::cooldownGroup)).apply((Applicative)instance, UseCooldown::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, UseCooldown> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT, UseCooldown::seconds, Identifier.STREAM_CODEC.apply(ByteBufCodecs::optional), UseCooldown::cooldownGroup, UseCooldown::new);

    public UseCooldown(float f) {
        this(f, Optional.empty());
    }

    public int ticks() {
        return (int)(this.seconds * 20.0f);
    }

    public void apply(ItemStack itemStack, LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            player.getCooldowns().addCooldown(itemStack, this.ticks());
        }
    }
}

