/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.chars.CharArraySet
 *  it.unimi.dsi.fastutil.chars.CharSet
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.item.crafting;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.chars.CharArraySet;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;

public final class ShapedRecipePattern {
    private static final int MAX_SIZE = 3;
    public static final char EMPTY_SLOT = ' ';
    public static final MapCodec<ShapedRecipePattern> MAP_CODEC = Data.MAP_CODEC.flatXmap(ShapedRecipePattern::unpack, shapedRecipePattern -> shapedRecipePattern.data.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Cannot encode unpacked recipe")));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedRecipePattern> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, shapedRecipePattern -> shapedRecipePattern.width, ByteBufCodecs.VAR_INT, shapedRecipePattern -> shapedRecipePattern.height, Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), shapedRecipePattern -> shapedRecipePattern.ingredients, ShapedRecipePattern::createFromNetwork);
    private final int width;
    private final int height;
    private final List<Optional<Ingredient>> ingredients;
    private final Optional<Data> data;
    private final int ingredientCount;
    private final boolean symmetrical;

    public ShapedRecipePattern(int i, int j, List<Optional<Ingredient>> list, Optional<Data> optional) {
        this.width = i;
        this.height = j;
        this.ingredients = list;
        this.data = optional;
        this.ingredientCount = (int)list.stream().flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).count();
        this.symmetrical = Util.isSymmetrical(i, j, list);
    }

    private static ShapedRecipePattern createFromNetwork(Integer integer, Integer integer2, List<Optional<Ingredient>> list) {
        return new ShapedRecipePattern(integer, integer2, list, Optional.empty());
    }

    public static ShapedRecipePattern of(Map<Character, Ingredient> map, String ... strings) {
        return ShapedRecipePattern.of(map, List.of((Object[])strings));
    }

    public static ShapedRecipePattern of(Map<Character, Ingredient> map, List<String> list) {
        Data data = new Data(map, list);
        return (ShapedRecipePattern)ShapedRecipePattern.unpack(data).getOrThrow();
    }

    private static DataResult<ShapedRecipePattern> unpack(Data data) {
        String[] strings = ShapedRecipePattern.shrink(data.pattern);
        int i = strings[0].length();
        int j = strings.length;
        ArrayList<Optional<Ingredient>> list = new ArrayList<Optional<Ingredient>>(i * j);
        CharArraySet charSet = new CharArraySet(data.key.keySet());
        for (String string : strings) {
            for (int k = 0; k < string.length(); ++k) {
                Optional<Object> optional;
                char c = string.charAt(k);
                if (c == ' ') {
                    optional = Optional.empty();
                } else {
                    Ingredient ingredient = data.key.get(Character.valueOf(c));
                    if (ingredient == null) {
                        return DataResult.error(() -> "Pattern references symbol '" + c + "' but it's not defined in the key");
                    }
                    optional = Optional.of(ingredient);
                }
                charSet.remove(c);
                list.add(optional);
            }
        }
        if (!charSet.isEmpty()) {
            return DataResult.error(() -> ShapedRecipePattern.method_55082((CharSet)charSet));
        }
        return DataResult.success((Object)new ShapedRecipePattern(i, j, list, Optional.of(data)));
    }

    @VisibleForTesting
    static String[] shrink(List<String> list) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;
        for (int m = 0; m < list.size(); ++m) {
            String string = list.get(m);
            i = Math.min(i, ShapedRecipePattern.firstNonEmpty(string));
            int n = ShapedRecipePattern.lastNonEmpty(string);
            j = Math.max(j, n);
            if (n < 0) {
                if (k == m) {
                    ++k;
                }
                ++l;
                continue;
            }
            l = 0;
        }
        if (list.size() == l) {
            return new String[0];
        }
        String[] strings = new String[list.size() - l - k];
        for (int o = 0; o < strings.length; ++o) {
            strings[o] = list.get(o + k).substring(i, j + 1);
        }
        return strings;
    }

    private static int firstNonEmpty(String string) {
        int i;
        for (i = 0; i < string.length() && string.charAt(i) == ' '; ++i) {
        }
        return i;
    }

    private static int lastNonEmpty(String string) {
        int i;
        for (i = string.length() - 1; i >= 0 && string.charAt(i) == ' '; --i) {
        }
        return i;
    }

    public boolean matches(CraftingInput craftingInput) {
        if (craftingInput.ingredientCount() != this.ingredientCount) {
            return false;
        }
        if (craftingInput.width() == this.width && craftingInput.height() == this.height) {
            if (!this.symmetrical && this.matches(craftingInput, true)) {
                return true;
            }
            if (this.matches(craftingInput, false)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(CraftingInput craftingInput, boolean bl) {
        for (int i = 0; i < this.height; ++i) {
            for (int j = 0; j < this.width; ++j) {
                ItemStack itemStack;
                Optional<Ingredient> optional = bl ? this.ingredients.get(this.width - j - 1 + i * this.width) : this.ingredients.get(j + i * this.width);
                if (Ingredient.testOptionalIngredient(optional, itemStack = craftingInput.getItem(j, i))) continue;
                return false;
            }
        }
        return true;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }

    public List<Optional<Ingredient>> ingredients() {
        return this.ingredients;
    }

    private static /* synthetic */ String method_55082(CharSet charSet) {
        return "Key defines symbols that aren't used in pattern: " + String.valueOf(charSet);
    }

    public static final class Data
    extends Record {
        final Map<Character, Ingredient> key;
        final List<String> pattern;
        private static final Codec<List<String>> PATTERN_CODEC = Codec.STRING.listOf().comapFlatMap(list -> {
            if (list.size() > 3) {
                return DataResult.error(() -> "Invalid pattern: too many rows, 3 is maximum");
            }
            if (list.isEmpty()) {
                return DataResult.error(() -> "Invalid pattern: empty pattern not allowed");
            }
            int i = ((String)list.getFirst()).length();
            for (String string : list) {
                if (string.length() > 3) {
                    return DataResult.error(() -> "Invalid pattern: too many columns, 3 is maximum");
                }
                if (i == string.length()) continue;
                return DataResult.error(() -> "Invalid pattern: each row must be the same width");
            }
            return DataResult.success((Object)list);
        }, Function.identity());
        private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap(string -> {
            if (string.length() != 1) {
                return DataResult.error(() -> "Invalid key entry: '" + string + "' is an invalid symbol (must be 1 character only).");
            }
            if (" ".equals(string)) {
                return DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.");
            }
            return DataResult.success((Object)Character.valueOf(string.charAt(0)));
        }, String::valueOf);
        public static final MapCodec<Data> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.strictUnboundedMap(SYMBOL_CODEC, Ingredient.CODEC).fieldOf("key").forGetter(data -> data.key), (App)PATTERN_CODEC.fieldOf("pattern").forGetter(data -> data.pattern)).apply((Applicative)instance, Data::new));

        public Data(Map<Character, Ingredient> map, List<String> list) {
            this.key = map;
            this.pattern = list;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Data.class, "key;pattern", "key", "pattern"}, this, object);
        }

        public Map<Character, Ingredient> key() {
            return this.key;
        }

        public List<String> pattern() {
            return this.pattern;
        }
    }
}

