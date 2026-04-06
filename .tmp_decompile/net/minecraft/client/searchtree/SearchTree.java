/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.searchtree;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.SuffixArray;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface SearchTree<T> {
    public static <T> SearchTree<T> empty() {
        return string -> List.of();
    }

    public static <T> SearchTree<T> plainText(List<T> list, Function<T, Stream<String>> function) {
        if (list.isEmpty()) {
            return SearchTree.empty();
        }
        SuffixArray suffixArray = new SuffixArray();
        for (Object object : list) {
            function.apply(object).forEach(string -> suffixArray.add(object, string.toLowerCase(Locale.ROOT)));
        }
        suffixArray.generate();
        return suffixArray::search;
    }

    public List<T> search(String var1);
}

