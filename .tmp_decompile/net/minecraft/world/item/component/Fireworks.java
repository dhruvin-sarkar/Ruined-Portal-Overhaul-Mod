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
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.TooltipProvider;

public record Fireworks(int flightDuration, List<FireworkExplosion> explosions) implements TooltipProvider
{
    public static final int MAX_EXPLOSIONS = 256;
    public static final Codec<Fireworks> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.UNSIGNED_BYTE.optionalFieldOf("flight_duration", (Object)0).forGetter(Fireworks::flightDuration), (App)FireworkExplosion.CODEC.sizeLimitedListOf(256).optionalFieldOf("explosions", (Object)List.of()).forGetter(Fireworks::explosions)).apply((Applicative)instance, Fireworks::new));
    public static final StreamCodec<ByteBuf, Fireworks> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Fireworks::flightDuration, FireworkExplosion.STREAM_CODEC.apply(ByteBufCodecs.list(256)), Fireworks::explosions, Fireworks::new);

    public Fireworks {
        if (list.size() > 256) {
            throw new IllegalArgumentException("Got " + list.size() + " explosions, but maximum is 256");
        }
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        if (this.flightDuration > 0) {
            consumer.accept(Component.translatable("item.minecraft.firework_rocket.flight").append(CommonComponents.SPACE).append(String.valueOf(this.flightDuration)).withStyle(ChatFormatting.GRAY));
        }
        FireworkExplosion fireworkExplosion = null;
        int i = 0;
        for (FireworkExplosion fireworkExplosion2 : this.explosions) {
            if (fireworkExplosion == null) {
                fireworkExplosion = fireworkExplosion2;
                i = 1;
                continue;
            }
            if (fireworkExplosion.equals(fireworkExplosion2)) {
                ++i;
                continue;
            }
            Fireworks.addExplosionTooltip(consumer, fireworkExplosion, i);
            fireworkExplosion = fireworkExplosion2;
            i = 1;
        }
        if (fireworkExplosion != null) {
            Fireworks.addExplosionTooltip(consumer, fireworkExplosion, i);
        }
    }

    private static void addExplosionTooltip(Consumer<Component> consumer, FireworkExplosion fireworkExplosion, int i) {
        MutableComponent component2 = fireworkExplosion.shape().getName();
        if (i == 1) {
            consumer.accept(Component.translatable("item.minecraft.firework_rocket.single_star", component2).withStyle(ChatFormatting.GRAY));
        } else {
            consumer.accept(Component.translatable("item.minecraft.firework_rocket.multiple_stars", i, component2).withStyle(ChatFormatting.GRAY));
        }
        fireworkExplosion.addAdditionalTooltip(component -> consumer.accept(Component.literal("  ").append((Component)component)));
    }
}

