/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util.valueproviders;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProviderType;

public abstract class IntProvider {
    private static final Codec<Either<Integer, IntProvider>> CONSTANT_OR_DISPATCH_CODEC = Codec.either((Codec)Codec.INT, (Codec)BuiltInRegistries.INT_PROVIDER_TYPE.byNameCodec().dispatch(IntProvider::getType, IntProviderType::codec));
    public static final Codec<IntProvider> CODEC = CONSTANT_OR_DISPATCH_CODEC.xmap(either -> (IntProvider)either.map(ConstantInt::of, intProvider -> intProvider), intProvider -> intProvider.getType() == IntProviderType.CONSTANT ? Either.left((Object)((ConstantInt)intProvider).getValue()) : Either.right((Object)intProvider));
    public static final Codec<IntProvider> NON_NEGATIVE_CODEC = IntProvider.codec(0, Integer.MAX_VALUE);
    public static final Codec<IntProvider> POSITIVE_CODEC = IntProvider.codec(1, Integer.MAX_VALUE);

    public static Codec<IntProvider> codec(int i, int j) {
        return IntProvider.validateCodec(i, j, CODEC);
    }

    public static <T extends IntProvider> Codec<T> validateCodec(int i, int j, Codec<T> codec) {
        return codec.validate(intProvider -> IntProvider.validate(i, j, intProvider));
    }

    private static <T extends IntProvider> DataResult<T> validate(int i, int j, T intProvider) {
        if (intProvider.getMinValue() < i) {
            return DataResult.error(() -> "Value provider too low: " + i + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]");
        }
        if (intProvider.getMaxValue() > j) {
            return DataResult.error(() -> "Value provider too high: " + j + " [" + intProvider.getMinValue() + "-" + intProvider.getMaxValue() + "]");
        }
        return DataResult.success(intProvider);
    }

    public abstract int sample(RandomSource var1);

    public abstract int getMinValue();

    public abstract int getMaxValue();

    public abstract IntProviderType<?> getType();
}

