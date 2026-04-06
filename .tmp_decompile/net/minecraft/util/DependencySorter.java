/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 */
package net.minecraft.util;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class DependencySorter<K, V extends Entry<K>> {
    private final Map<K, V> contents = new HashMap();

    public DependencySorter<K, V> addEntry(K object, V entry) {
        this.contents.put(object, entry);
        return this;
    }

    private void visitDependenciesAndElement(Multimap<K, K> multimap, Set<K> set, K object2, BiConsumer<K, V> biConsumer) {
        if (!set.add(object2)) {
            return;
        }
        multimap.get(object2).forEach(object -> this.visitDependenciesAndElement(multimap, set, object, biConsumer));
        Entry entry = (Entry)this.contents.get(object2);
        if (entry != null) {
            biConsumer.accept(object2, entry);
        }
    }

    private static <K> boolean isCyclic(Multimap<K, K> multimap, K object, K object22) {
        Collection collection = multimap.get(object22);
        if (collection.contains(object)) {
            return true;
        }
        return collection.stream().anyMatch(object2 -> DependencySorter.isCyclic(multimap, object, object2));
    }

    private static <K> void addDependencyIfNotCyclic(Multimap<K, K> multimap, K object, K object2) {
        if (!DependencySorter.isCyclic(multimap, object, object2)) {
            multimap.put(object, object2);
        }
    }

    public void orderByDependencies(BiConsumer<K, V> biConsumer) {
        HashMultimap multimap = HashMultimap.create();
        this.contents.forEach((arg_0, arg_1) -> DependencySorter.method_51488((Multimap)multimap, arg_0, arg_1));
        this.contents.forEach((arg_0, arg_1) -> DependencySorter.method_51482((Multimap)multimap, arg_0, arg_1));
        HashSet set = new HashSet();
        this.contents.keySet().forEach(arg_0 -> this.method_51485((Multimap)multimap, set, biConsumer, arg_0));
    }

    private /* synthetic */ void method_51485(Multimap multimap, Set set, BiConsumer biConsumer, Object object) {
        this.visitDependenciesAndElement(multimap, set, object, biConsumer);
    }

    private static /* synthetic */ void method_51482(Multimap multimap, Object object, Entry entry) {
        entry.visitOptionalDependencies(object2 -> DependencySorter.addDependencyIfNotCyclic(multimap, object, object2));
    }

    private static /* synthetic */ void method_51488(Multimap multimap, Object object, Entry entry) {
        entry.visitRequiredDependencies(object2 -> DependencySorter.addDependencyIfNotCyclic(multimap, object, object2));
    }

    public static interface Entry<K> {
        public void visitRequiredDependencies(Consumer<K> var1);

        public void visitOptionalDependencies(Consumer<K> var1);
    }
}

