/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.color.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record Firework(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<Firework> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(Firework::defaultColor)).apply((Applicative)instance, Firework::new));

    public Firework() {
        this(-7697782);
    }

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        FireworkExplosion fireworkExplosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION);
        IntList intList = fireworkExplosion != null ? fireworkExplosion.colors() : IntList.of();
        int i = intList.size();
        if (i == 0) {
            return this.defaultColor;
        }
        if (i == 1) {
            return ARGB.opaque(intList.getInt(0));
        }
        int j = 0;
        int k = 0;
        int l = 0;
        for (int m = 0; m < i; ++m) {
            int n = intList.getInt(m);
            j += ARGB.red(n);
            k += ARGB.green(n);
            l += ARGB.blue(n);
        }
        return ARGB.color(j / i, k / i, l / i);
    }

    public MapCodec<Firework> type() {
        return MAP_CODEC;
    }
}

