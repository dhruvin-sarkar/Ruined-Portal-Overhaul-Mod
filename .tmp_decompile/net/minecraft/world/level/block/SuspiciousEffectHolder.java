/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public interface SuspiciousEffectHolder {
    public SuspiciousStewEffects getSuspiciousEffects();

    public static List<SuspiciousEffectHolder> getAllEffectHolders() {
        return BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static @Nullable SuspiciousEffectHolder tryGet(ItemLike itemLike) {
        BlockItem blockItem;
        FeatureElement featureElement = itemLike.asItem();
        if (featureElement instanceof BlockItem && (featureElement = (blockItem = (BlockItem)featureElement).getBlock()) instanceof SuspiciousEffectHolder) {
            SuspiciousEffectHolder suspiciousEffectHolder = (SuspiciousEffectHolder)((Object)featureElement);
            return suspiciousEffectHolder;
        }
        Item item = itemLike.asItem();
        if (item instanceof SuspiciousEffectHolder) {
            SuspiciousEffectHolder suspiciousEffectHolder2 = (SuspiciousEffectHolder)((Object)item);
            return suspiciousEffectHolder2;
        }
        return null;
    }
}

