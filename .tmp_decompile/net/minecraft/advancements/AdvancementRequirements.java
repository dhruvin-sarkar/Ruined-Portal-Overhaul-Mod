/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 */
package net.minecraft.advancements;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.invoke.LambdaMetafactory;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.network.FriendlyByteBuf;

public record AdvancementRequirements(List<List<String>> requirements) {
    public static final Codec<AdvancementRequirements> CODEC = Codec.STRING.listOf().listOf().xmap(AdvancementRequirements::new, AdvancementRequirements::requirements);
    public static final AdvancementRequirements EMPTY = new AdvancementRequirements(List.of());

    public AdvancementRequirements(FriendlyByteBuf friendlyByteBuf2) {
        this(friendlyByteBuf2.readList(friendlyByteBuf -> friendlyByteBuf.readList(FriendlyByteBuf::readUtf)));
    }

    public void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeCollection(this.requirements, (friendlyByteBuf, list) -> friendlyByteBuf.writeCollection(list, FriendlyByteBuf::writeUtf));
    }

    public static AdvancementRequirements allOf(Collection<String> collection) {
        return new AdvancementRequirements(collection.stream().map((Function<String, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, of(java.lang.Object ), (Ljava/lang/String;)Ljava/util/List;)()).toList());
    }

    public static AdvancementRequirements anyOf(Collection<String> collection) {
        return new AdvancementRequirements(List.of((Object)List.copyOf(collection)));
    }

    public int size() {
        return this.requirements.size();
    }

    public boolean test(Predicate<String> predicate) {
        if (this.requirements.isEmpty()) {
            return false;
        }
        for (List<String> list : this.requirements) {
            if (AdvancementRequirements.anyMatch(list, predicate)) continue;
            return false;
        }
        return true;
    }

    public int count(Predicate<String> predicate) {
        int i = 0;
        for (List<String> list : this.requirements) {
            if (!AdvancementRequirements.anyMatch(list, predicate)) continue;
            ++i;
        }
        return i;
    }

    private static boolean anyMatch(List<String> list, Predicate<String> predicate) {
        for (String string : list) {
            if (!predicate.test(string)) continue;
            return true;
        }
        return false;
    }

    public DataResult<AdvancementRequirements> validate(Set<String> set) {
        ObjectOpenHashSet set2 = new ObjectOpenHashSet();
        for (List<String> list : this.requirements) {
            if (list.isEmpty() && set.isEmpty()) {
                return DataResult.error(() -> "Requirement entry cannot be empty");
            }
            set2.addAll(list);
        }
        if (!set.equals(set2)) {
            Sets.SetView set3 = Sets.difference(set, (Set)set2);
            Sets.SetView set4 = Sets.difference((Set)set2, set);
            return DataResult.error(() -> AdvancementRequirements.method_54926((Set)set3, (Set)set4));
        }
        return DataResult.success((Object)((Object)this));
    }

    public boolean isEmpty() {
        return this.requirements.isEmpty();
    }

    public String toString() {
        return this.requirements.toString();
    }

    public Set<String> names() {
        ObjectOpenHashSet set = new ObjectOpenHashSet();
        for (List<String> list : this.requirements) {
            set.addAll(list);
        }
        return set;
    }

    private static /* synthetic */ String method_54926(Set set, Set set2) {
        return "Advancement completion requirements did not exactly match specified criteria. Missing: " + String.valueOf(set) + ". Unknown: " + String.valueOf(set2);
    }

    public static interface Strategy {
        public static final Strategy AND = AdvancementRequirements::allOf;
        public static final Strategy OR = AdvancementRequirements::anyOf;

        public AdvancementRequirements create(Collection<String> var1);
    }
}

