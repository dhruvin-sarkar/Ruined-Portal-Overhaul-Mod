/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.entity.variant;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;

public record SpawnPrioritySelectors(List<PriorityProvider.Selector<SpawnContext, SpawnCondition>> selectors) {
    public static final SpawnPrioritySelectors EMPTY = new SpawnPrioritySelectors(List.of());
    public static final Codec<SpawnPrioritySelectors> CODEC = PriorityProvider.Selector.codec(SpawnCondition.CODEC).listOf().xmap(SpawnPrioritySelectors::new, SpawnPrioritySelectors::selectors);

    public static SpawnPrioritySelectors single(SpawnCondition spawnCondition, int i) {
        return new SpawnPrioritySelectors(PriorityProvider.single(spawnCondition, i));
    }

    public static SpawnPrioritySelectors fallback(int i) {
        return new SpawnPrioritySelectors(PriorityProvider.alwaysTrue(i));
    }
}

