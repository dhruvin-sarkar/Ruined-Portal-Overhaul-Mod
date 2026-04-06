/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.slot;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.SlotProvider;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootContextArg;

public class RangeSlotSource
implements SlotSource {
    public static final MapCodec<RangeSlotSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootContextArg.ENTITY_OR_BLOCK.fieldOf("source").forGetter(rangeSlotSource -> rangeSlotSource.source), (App)SlotRanges.CODEC.fieldOf("slots").forGetter(rangeSlotSource -> rangeSlotSource.slotRange)).apply((Applicative)instance, RangeSlotSource::new));
    private final LootContextArg<Object> source;
    private final SlotRange slotRange;

    private RangeSlotSource(LootContextArg<Object> lootContextArg, SlotRange slotRange) {
        this.source = lootContextArg;
        this.slotRange = slotRange;
    }

    public MapCodec<RangeSlotSource> codec() {
        return MAP_CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(this.source.contextParam());
    }

    @Override
    public final SlotCollection provide(LootContext lootContext) {
        Object object = this.source.get(lootContext);
        if (object instanceof SlotProvider) {
            SlotProvider slotProvider = (SlotProvider)object;
            return slotProvider.getSlotsFromRange(this.slotRange.slots());
        }
        return SlotCollection.EMPTY;
    }
}

