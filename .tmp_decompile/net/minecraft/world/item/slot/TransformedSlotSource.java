/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.Products$P1
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Instance
 *  com.mojang.serialization.codecs.RecordCodecBuilder$Mu
 */
package net.minecraft.world.item.slot;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.SlotSources;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class TransformedSlotSource
implements SlotSource {
    protected final SlotSource slotSource;

    protected TransformedSlotSource(SlotSource slotSource) {
        this.slotSource = slotSource;
    }

    public abstract MapCodec<? extends TransformedSlotSource> codec();

    protected static <T extends TransformedSlotSource> Products.P1<RecordCodecBuilder.Mu<T>, SlotSource> commonFields(RecordCodecBuilder.Instance<T> instance) {
        return instance.group((App)SlotSources.CODEC.fieldOf("slot_source").forGetter(transformedSlotSource -> transformedSlotSource.slotSource));
    }

    protected abstract SlotCollection transform(SlotCollection var1);

    @Override
    public final SlotCollection provide(LootContext lootContext) {
        return this.transform(this.slotSource.provide(lootContext));
    }

    @Override
    public void validate(ValidationContext validationContext) {
        SlotSource.super.validate(validationContext);
        this.slotSource.validate(validationContext.forChild(new ProblemReporter.FieldPathElement("slot_source")));
    }
}

