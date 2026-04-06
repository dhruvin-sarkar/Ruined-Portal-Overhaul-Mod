/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.SlotSources;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryType;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class SlotLoot
extends LootPoolSingletonContainer {
    public static final MapCodec<SlotLoot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SlotSources.CODEC.fieldOf("slot_source").forGetter(slotLoot -> slotLoot.slotSource)).and(SlotLoot.singletonFields(instance)).apply((Applicative)instance, SlotLoot::new));
    private final SlotSource slotSource;

    private SlotLoot(SlotSource slotSource, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2) {
        super(i, j, list, list2);
        this.slotSource = slotSource;
    }

    @Override
    public LootPoolEntryType getType() {
        return LootPoolEntries.SLOTS;
    }

    @Override
    public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
        this.slotSource.provide(lootContext).itemCopies().filter(itemStack -> !itemStack.isEmpty()).forEach(consumer);
    }

    @Override
    public void validate(ValidationContext validationContext) {
        super.validate(validationContext);
        this.slotSource.validate(validationContext.forChild(new ProblemReporter.FieldPathElement("slot_source")));
    }
}

