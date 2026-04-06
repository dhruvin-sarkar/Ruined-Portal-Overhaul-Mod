/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class RarityFilter
extends PlacementFilter {
    public static final MapCodec<RarityFilter> CODEC = ExtraCodecs.POSITIVE_INT.fieldOf("chance").xmap(RarityFilter::new, rarityFilter -> rarityFilter.chance);
    private final int chance;

    private RarityFilter(int i) {
        this.chance = i;
    }

    public static RarityFilter onAverageOnceEvery(int i) {
        return new RarityFilter(i);
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        return randomSource.nextFloat() < 1.0f / (float)this.chance;
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.RARITY_FILTER;
    }
}

