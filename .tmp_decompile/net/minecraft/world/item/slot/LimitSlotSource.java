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
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.slot.SlotCollection;
import net.minecraft.world.item.slot.SlotSource;
import net.minecraft.world.item.slot.TransformedSlotSource;

public class LimitSlotSource
extends TransformedSlotSource {
    public static final MapCodec<LimitSlotSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> LimitSlotSource.commonFields(instance).and((App)ExtraCodecs.POSITIVE_INT.fieldOf("limit").forGetter(limitSlotSource -> limitSlotSource.limit)).apply((Applicative)instance, LimitSlotSource::new));
    private final int limit;

    private LimitSlotSource(SlotSource slotSource, int i) {
        super(slotSource);
        this.limit = i;
    }

    public MapCodec<LimitSlotSource> codec() {
        return MAP_CODEC;
    }

    @Override
    protected SlotCollection transform(SlotCollection slotCollection) {
        return slotCollection.limit(this.limit);
    }
}

