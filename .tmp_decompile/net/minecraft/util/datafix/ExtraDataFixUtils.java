/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.RewriteResult
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.View
 *  com.mojang.datafixers.functions.PointFreeRule
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;

public class ExtraDataFixUtils {
    public static Dynamic<?> fixBlockPos(Dynamic<?> dynamic) {
        Optional optional = dynamic.get("X").asNumber().result();
        Optional optional2 = dynamic.get("Y").asNumber().result();
        Optional optional3 = dynamic.get("Z").asNumber().result();
        if (optional.isEmpty() || optional2.isEmpty() || optional3.isEmpty()) {
            return dynamic;
        }
        return ExtraDataFixUtils.createBlockPos(dynamic, ((Number)optional.get()).intValue(), ((Number)optional2.get()).intValue(), ((Number)optional3.get()).intValue());
    }

    public static Dynamic<?> fixInlineBlockPos(Dynamic<?> dynamic, String string, String string2, String string3, String string4) {
        Optional optional = dynamic.get(string).asNumber().result();
        Optional optional2 = dynamic.get(string2).asNumber().result();
        Optional optional3 = dynamic.get(string3).asNumber().result();
        if (optional.isEmpty() || optional2.isEmpty() || optional3.isEmpty()) {
            return dynamic;
        }
        return dynamic.remove(string).remove(string2).remove(string3).set(string4, ExtraDataFixUtils.createBlockPos(dynamic, ((Number)optional.get()).intValue(), ((Number)optional2.get()).intValue(), ((Number)optional3.get()).intValue()));
    }

    public static Dynamic<?> createBlockPos(Dynamic<?> dynamic, int i, int j, int k) {
        return dynamic.createIntList(IntStream.of(i, j, k));
    }

    public static <T, R> Typed<R> cast(Type<R> type, Typed<T> typed) {
        return new Typed(type, typed.getOps(), typed.getValue());
    }

    public static <T> Typed<T> cast(Type<T> type, Object object, DynamicOps<?> dynamicOps) {
        return new Typed(type, dynamicOps, object);
    }

    public static Type<?> patchSubType(Type<?> type, Type<?> type2, Type<?> type3) {
        return type.all(ExtraDataFixUtils.typePatcher(type2, type3), true, false).view().newType();
    }

    private static <A, B> TypeRewriteRule typePatcher(Type<A> type, Type<B> type2) {
        RewriteResult rewriteResult = RewriteResult.create((View)View.create((String)"Patcher", type, type2, dynamicOps -> object -> {
            throw new UnsupportedOperationException();
        }), (BitSet)new BitSet());
        return TypeRewriteRule.everywhere((TypeRewriteRule)TypeRewriteRule.ifSame(type, (RewriteResult)rewriteResult), (PointFreeRule)PointFreeRule.nop(), (boolean)true, (boolean)true);
    }

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>> ... functions) {
        return typed -> {
            for (Function function : functions) {
                typed = (Typed)function.apply(typed);
            }
            return typed;
        };
    }

    public static Dynamic<?> blockState(String string, Map<String, String> map) {
        Dynamic dynamic = new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)new CompoundTag());
        Dynamic dynamic2 = dynamic.set("Name", dynamic.createString(string));
        if (!map.isEmpty()) {
            dynamic2 = dynamic2.set("Properties", dynamic.createMap(map.entrySet().stream().collect(Collectors.toMap(entry -> dynamic.createString((String)entry.getKey()), entry -> dynamic.createString((String)entry.getValue())))));
        }
        return dynamic2;
    }

    public static Dynamic<?> blockState(String string) {
        return ExtraDataFixUtils.blockState(string, Map.of());
    }

    public static Dynamic<?> fixStringField(Dynamic<?> dynamic, String string, UnaryOperator<String> unaryOperator) {
        return dynamic.update(string, dynamic2 -> (Dynamic)DataFixUtils.orElse((Optional)dynamic2.asString().map((Function)unaryOperator).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)).result(), (Object)dynamic2));
    }

    public static String dyeColorIdToName(int i) {
        return switch (i) {
            default -> "white";
            case 1 -> "orange";
            case 2 -> "magenta";
            case 3 -> "light_blue";
            case 4 -> "yellow";
            case 5 -> "lime";
            case 6 -> "pink";
            case 7 -> "gray";
            case 8 -> "light_gray";
            case 9 -> "cyan";
            case 10 -> "purple";
            case 11 -> "blue";
            case 12 -> "brown";
            case 13 -> "green";
            case 14 -> "red";
            case 15 -> "black";
        };
    }

    public static <T> Typed<?> readAndSet(Typed<?> typed, OpticFinder<T> opticFinder, Dynamic<?> dynamic) {
        return typed.set(opticFinder, Util.readTypedOrThrow(opticFinder.type(), dynamic, true));
    }
}

