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
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public interface IdentifierSearchTree<T> {
    public static <T> IdentifierSearchTree<T> empty() {
        return new IdentifierSearchTree<T>(){

            @Override
            public List<T> searchNamespace(String string) {
                return List.of();
            }

            @Override
            public List<T> searchPath(String string) {
                return List.of();
            }
        };
    }

    public static <T> IdentifierSearchTree<T> create(List<T> list, Function<T, Stream<Identifier>> function) {
        if (list.isEmpty()) {
            return IdentifierSearchTree.empty();
        }
        final SuffixArray suffixArray = new SuffixArray();
        final SuffixArray suffixArray2 = new SuffixArray();
        for (Object object : list) {
            function.apply(object).forEach(identifier -> {
                suffixArray.add(object, identifier.getNamespace().toLowerCase(Locale.ROOT));
                suffixArray2.add(object, identifier.getPath().toLowerCase(Locale.ROOT));
            });
        }
        suffixArray.generate();
        suffixArray2.generate();
        return new IdentifierSearchTree<T>(){

            @Override
            public List<T> searchNamespace(String string) {
                return suffixArray.search(string);
            }

            @Override
            public List<T> searchPath(String string) {
                return suffixArray2.search(string);
            }
        };
    }

    public List<T> searchNamespace(String var1);

    public List<T> searchPath(String var1);
}

