/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.searchtree;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.searchtree.IdentifierSearchTree;
import net.minecraft.client.searchtree.IntersectionIterator;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

@Environment(value=EnvType.CLIENT)
public class IdSearchTree<T>
implements SearchTree<T> {
    protected final Comparator<T> additionOrder;
    protected final IdentifierSearchTree<T> identifierSearchTree;

    public IdSearchTree(Function<T, Stream<Identifier>> function, List<T> list) {
        ToIntFunction<T> toIntFunction = Util.createIndexLookup(list);
        this.additionOrder = Comparator.comparingInt(toIntFunction);
        this.identifierSearchTree = IdentifierSearchTree.create(list, function);
    }

    @Override
    public List<T> search(String string) {
        int i = string.indexOf(58);
        if (i == -1) {
            return this.searchPlainText(string);
        }
        return this.searchIdentifier(string.substring(0, i).trim(), string.substring(i + 1).trim());
    }

    protected List<T> searchPlainText(String string) {
        return this.identifierSearchTree.searchPath(string);
    }

    protected List<T> searchIdentifier(String string, String string2) {
        List<T> list = this.identifierSearchTree.searchNamespace(string);
        List<T> list2 = this.identifierSearchTree.searchPath(string2);
        return ImmutableList.copyOf(new IntersectionIterator<T>(list.iterator(), list2.iterator(), this.additionOrder));
    }
}

