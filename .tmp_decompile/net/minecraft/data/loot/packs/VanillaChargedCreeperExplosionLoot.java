/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.data.loot.packs;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public record VanillaChargedCreeperExplosionLoot(HolderLookup.Provider registries) implements LootTableSubProvider
{
    private static final List<Entry> ENTRIES = List.of((Object)((Object)new Entry(BuiltInLootTables.CHARGED_CREEPER_PIGLIN, EntityType.PIGLIN, Items.PIGLIN_HEAD)), (Object)((Object)new Entry(BuiltInLootTables.CHARGED_CREEPER_CREEPER, EntityType.CREEPER, Items.CREEPER_HEAD)), (Object)((Object)new Entry(BuiltInLootTables.CHARGED_CREEPER_SKELETON, EntityType.SKELETON, Items.SKELETON_SKULL)), (Object)((Object)new Entry(BuiltInLootTables.CHARGED_CREEPER_WITHER_SKELETON, EntityType.WITHER_SKELETON, Items.WITHER_SKELETON_SKULL)), (Object)((Object)new Entry(BuiltInLootTables.CHARGED_CREEPER_ZOMBIE, EntityType.ZOMBIE, Items.ZOMBIE_HEAD)));

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
        HolderGetter holderGetter = this.registries.lookupOrThrow(Registries.ENTITY_TYPE);
        ArrayList list = new ArrayList(ENTRIES.size());
        for (Entry entry : ENTRIES) {
            biConsumer.accept(entry.lootTable, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(LootItem.lootTableItem(entry.item))));
            LootItemCondition.Builder builder = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().entityType(EntityTypePredicate.of(holderGetter, entry.entityType)));
            list.add(NestedLootTable.lootTableReference(entry.lootTable).when(builder));
        }
        biConsumer.accept(BuiltInLootTables.CHARGED_CREEPER, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(AlternativesEntry.alternatives((LootPoolEntryContainer.Builder[])list.toArray(LootPoolEntryContainer.Builder[]::new)))));
    }

    static final class Entry
    extends Record {
        final ResourceKey<LootTable> lootTable;
        final EntityType<?> entityType;
        final Item item;

        Entry(ResourceKey<LootTable> resourceKey, EntityType<?> entityType, Item item) {
            this.lootTable = resourceKey;
            this.entityType = entityType;
            this.item = item;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "lootTable;entityType;item", "lootTable", "entityType", "item"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "lootTable;entityType;item", "lootTable", "entityType", "item"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "lootTable;entityType;item", "lootTable", "entityType", "item"}, this, object);
        }

        public ResourceKey<LootTable> lootTable() {
            return this.lootTable;
        }

        public EntityType<?> entityType() {
            return this.entityType;
        }

        public Item item() {
            return this.item;
        }
    }
}

