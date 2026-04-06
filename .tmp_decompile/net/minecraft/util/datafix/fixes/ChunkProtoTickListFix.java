/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  org.apache.commons.lang3.mutable.MutableInt
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.ChunkHeightAndBiomeFix;
import net.minecraft.util.datafix.fixes.References;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jspecify.annotations.Nullable;

public class ChunkProtoTickListFix
extends DataFix {
    private static final int SECTION_WIDTH = 16;
    private static final ImmutableSet<String> ALWAYS_WATERLOGGED = ImmutableSet.of((Object)"minecraft:bubble_column", (Object)"minecraft:kelp", (Object)"minecraft:kelp_plant", (Object)"minecraft:seagrass", (Object)"minecraft:tall_seagrass");

    public ChunkProtoTickListFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder opticFinder = type.findField("Level");
        OpticFinder opticFinder2 = opticFinder.type().findField("Sections");
        OpticFinder opticFinder3 = ((List.ListType)opticFinder2.type()).getElement().finder();
        OpticFinder opticFinder4 = opticFinder3.type().findField("block_states");
        OpticFinder opticFinder5 = opticFinder3.type().findField("biomes");
        OpticFinder opticFinder6 = opticFinder4.type().findField("palette");
        OpticFinder opticFinder7 = opticFinder.type().findField("TileTicks");
        return this.fixTypeEverywhereTyped("ChunkProtoTickListFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> {
            typed = typed.update(DSL.remainderFinder(), dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.get("LiquidTicks").result().map(dynamic2 -> dynamic.set("fluid_ticks", dynamic2).remove("LiquidTicks")), (Object)dynamic));
            Dynamic dynamic3 = (Dynamic)typed.get(DSL.remainderFinder());
            MutableInt mutableInt = new MutableInt();
            Int2ObjectArrayMap int2ObjectMap = new Int2ObjectArrayMap();
            typed.getOptionalTyped(opticFinder2).ifPresent(arg_0 -> ChunkProtoTickListFix.method_39248(opticFinder3, opticFinder5, mutableInt, opticFinder4, (Int2ObjectMap)int2ObjectMap, opticFinder6, arg_0));
            byte b = mutableInt.byteValue();
            typed = typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("yPos", dynamic -> dynamic.createByte(b)));
            if (typed.getOptionalTyped(opticFinder7).isPresent() || dynamic3.get("fluid_ticks").result().isPresent()) {
                return typed;
            }
            int i = dynamic3.get("xPos").asInt(0);
            int j = dynamic3.get("zPos").asInt(0);
            Dynamic<?> dynamic22 = this.makeTickList(dynamic3, (Int2ObjectMap<Supplier<PoorMansPalettedContainer>>)int2ObjectMap, b, i, j, "LiquidsToBeTicked", ChunkProtoTickListFix::getLiquid);
            Dynamic<?> dynamic32 = this.makeTickList(dynamic3, (Int2ObjectMap<Supplier<PoorMansPalettedContainer>>)int2ObjectMap, b, i, j, "ToBeTicked", ChunkProtoTickListFix::getBlock);
            Optional optional = opticFinder7.type().readTyped(dynamic32).result();
            if (optional.isPresent()) {
                typed = typed.set(opticFinder7, (Typed)((Pair)optional.get()).getFirst());
            }
            return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.remove("ToBeTicked").remove("LiquidsToBeTicked").set("fluid_ticks", dynamic22));
        }));
    }

    private Dynamic<?> makeTickList(Dynamic<?> dynamic2, Int2ObjectMap<Supplier<PoorMansPalettedContainer>> int2ObjectMap, byte b, int i2, int j, String string, Function<Dynamic<?>, String> function) {
        Stream<Object> stream = Stream.empty();
        List list = dynamic2.get(string).asList(Function.identity());
        for (int k = 0; k < list.size(); ++k) {
            int l = k + b;
            Supplier supplier = (Supplier)int2ObjectMap.get(l);
            Stream<Dynamic> stream2 = ((Dynamic)list.get(k)).asStream().mapToInt(dynamic -> dynamic.asShort((short)-1)).filter(i -> i > 0).mapToObj(arg_0 -> this.method_39256(dynamic2, (Supplier)supplier, i2, l, j, function, arg_0));
            stream = Stream.concat(stream, stream2);
        }
        return dynamic2.createList(stream);
    }

    private static String getBlock(@Nullable Dynamic<?> dynamic) {
        return dynamic != null ? dynamic.get("Name").asString("minecraft:air") : "minecraft:air";
    }

    private static String getLiquid(@Nullable Dynamic<?> dynamic) {
        if (dynamic == null) {
            return "minecraft:empty";
        }
        String string = dynamic.get("Name").asString("");
        if ("minecraft:water".equals(string)) {
            return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:water" : "minecraft:flowing_water";
        }
        if ("minecraft:lava".equals(string)) {
            return dynamic.get("Properties").get("level").asInt(0) == 0 ? "minecraft:lava" : "minecraft:flowing_lava";
        }
        if (ALWAYS_WATERLOGGED.contains((Object)string) || dynamic.get("Properties").get("waterlogged").asBoolean(false)) {
            return "minecraft:water";
        }
        return "minecraft:empty";
    }

    private Dynamic<?> createTick(Dynamic<?> dynamic, @Nullable Supplier<PoorMansPalettedContainer> supplier, int i, int j, int k, int l, Function<Dynamic<?>, String> function) {
        int m = l & 0xF;
        int n = l >>> 4 & 0xF;
        int o = l >>> 8 & 0xF;
        String string = function.apply(supplier != null ? supplier.get().get(m, n, o) : null);
        return dynamic.createMap((Map)ImmutableMap.builder().put((Object)dynamic.createString("i"), (Object)dynamic.createString(string)).put((Object)dynamic.createString("x"), (Object)dynamic.createInt(i * 16 + m)).put((Object)dynamic.createString("y"), (Object)dynamic.createInt(j * 16 + n)).put((Object)dynamic.createString("z"), (Object)dynamic.createInt(k * 16 + o)).put((Object)dynamic.createString("t"), (Object)dynamic.createInt(0)).put((Object)dynamic.createString("p"), (Object)dynamic.createInt(0)).build());
    }

    private /* synthetic */ Dynamic method_39256(Dynamic dynamic, Supplier supplier, int i, int j, int k, Function function, int l) {
        return this.createTick(dynamic, supplier, i, j, k, l, function);
    }

    private static /* synthetic */ void method_39248(OpticFinder opticFinder, OpticFinder opticFinder2, MutableInt mutableInt, OpticFinder opticFinder3, Int2ObjectMap int2ObjectMap, OpticFinder opticFinder4, Typed typed) {
        typed.getAllTyped(opticFinder).forEach(typed2 -> {
            Dynamic dynamic = (Dynamic)typed2.get(DSL.remainderFinder());
            int i = dynamic.get("Y").asInt(Integer.MAX_VALUE);
            if (i == Integer.MAX_VALUE) {
                return;
            }
            if (typed2.getOptionalTyped(opticFinder2).isPresent()) {
                mutableInt.setValue(Math.min(i, mutableInt.intValue()));
            }
            typed2.getOptionalTyped(opticFinder3).ifPresent(typed -> int2ObjectMap.put(i, (Object)Suppliers.memoize(() -> {
                List list = typed.getOptionalTyped(opticFinder4).map(typed -> typed.write().result().map(dynamic -> dynamic.asList(Function.identity())).orElse(Collections.emptyList())).orElse(Collections.emptyList());
                long[] ls = ((Dynamic)typed.get(DSL.remainderFinder())).get("data").asLongStream().toArray();
                return new PoorMansPalettedContainer(list, ls);
            })));
        });
    }

    public static final class PoorMansPalettedContainer {
        private static final long SIZE_BITS = 4L;
        private final List<? extends Dynamic<?>> palette;
        private final long[] data;
        private final int bits;
        private final long mask;
        private final int valuesPerLong;

        public PoorMansPalettedContainer(List<? extends Dynamic<?>> list, long[] ls) {
            this.palette = list;
            this.data = ls;
            this.bits = Math.max(4, ChunkHeightAndBiomeFix.ceillog2(list.size()));
            this.mask = (1L << this.bits) - 1L;
            this.valuesPerLong = (char)(64 / this.bits);
        }

        public @Nullable Dynamic<?> get(int i, int j, int k) {
            int l = this.palette.size();
            if (l < 1) {
                return null;
            }
            if (l == 1) {
                return (Dynamic)this.palette.getFirst();
            }
            int m = this.getIndex(i, j, k);
            int n = m / this.valuesPerLong;
            if (n < 0 || n >= this.data.length) {
                return null;
            }
            long o = this.data[n];
            int p = (m - n * this.valuesPerLong) * this.bits;
            int q = (int)(o >> p & this.mask);
            if (q < 0 || q >= l) {
                return null;
            }
            return this.palette.get(q);
        }

        private int getIndex(int i, int j, int k) {
            return (j << 4 | k) << 4 | i;
        }

        public List<? extends Dynamic<?>> palette() {
            return this.palette;
        }

        public long[] data() {
            return this.data;
        }
    }
}

