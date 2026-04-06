/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute.modifier;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record FloatWithAlpha(float value, float alpha) {
    private static final Codec<FloatWithAlpha> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("value").forGetter(FloatWithAlpha::value), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("alpha", (Object)Float.valueOf(1.0f)).forGetter(FloatWithAlpha::alpha)).apply((Applicative)instance, FloatWithAlpha::new));
    public static final Codec<FloatWithAlpha> CODEC = Codec.either((Codec)Codec.FLOAT, FULL_CODEC).xmap(either -> (FloatWithAlpha)((Object)((Object)either.map(FloatWithAlpha::new, floatWithAlpha -> floatWithAlpha))), floatWithAlpha -> floatWithAlpha.alpha() == 1.0f ? Either.left((Object)Float.valueOf(floatWithAlpha.value())) : Either.right((Object)floatWithAlpha));

    public FloatWithAlpha(float f) {
        this(f, 1.0f);
    }
}

