/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.core.component.predicates;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Function;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.SingleComponentItemPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public abstract class EnchantmentsPredicate
implements SingleComponentItemPredicate<ItemEnchantments> {
    private final List<EnchantmentPredicate> enchantments;

    protected EnchantmentsPredicate(List<EnchantmentPredicate> list) {
        this.enchantments = list;
    }

    public static <T extends EnchantmentsPredicate> Codec<T> codec(Function<List<EnchantmentPredicate>, T> function) {
        return EnchantmentPredicate.CODEC.listOf().xmap(function, EnchantmentsPredicate::enchantments);
    }

    protected List<EnchantmentPredicate> enchantments() {
        return this.enchantments;
    }

    @Override
    public boolean matches(ItemEnchantments itemEnchantments) {
        for (EnchantmentPredicate enchantmentPredicate : this.enchantments) {
            if (enchantmentPredicate.containedIn(itemEnchantments)) continue;
            return false;
        }
        return true;
    }

    public static Enchantments enchantments(List<EnchantmentPredicate> list) {
        return new Enchantments(list);
    }

    public static StoredEnchantments storedEnchantments(List<EnchantmentPredicate> list) {
        return new StoredEnchantments(list);
    }

    public static class Enchantments
    extends EnchantmentsPredicate {
        public static final Codec<Enchantments> CODEC = Enchantments.codec(Enchantments::new);

        protected Enchantments(List<EnchantmentPredicate> list) {
            super(list);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.ENCHANTMENTS;
        }
    }

    public static class StoredEnchantments
    extends EnchantmentsPredicate {
        public static final Codec<StoredEnchantments> CODEC = StoredEnchantments.codec(StoredEnchantments::new);

        protected StoredEnchantments(List<EnchantmentPredicate> list) {
            super(list);
        }

        @Override
        public DataComponentType<ItemEnchantments> componentType() {
            return DataComponents.STORED_ENCHANTMENTS;
        }
    }
}

