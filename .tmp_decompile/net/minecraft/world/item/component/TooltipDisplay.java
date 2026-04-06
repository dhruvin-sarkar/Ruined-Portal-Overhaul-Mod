/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
 *  it.unimi.dsi.fastutil.objects.ReferenceSortedSets
 *  java.util.SequencedSet
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import java.lang.invoke.LambdaMetafactory;
import java.util.List;
import java.util.SequencedSet;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record TooltipDisplay(boolean hideTooltip, SequencedSet<DataComponentType<?>> hiddenComponents) {
    private static final Codec<SequencedSet<DataComponentType<?>>> COMPONENT_SET_CODEC = DataComponentType.CODEC.listOf().xmap(ReferenceLinkedOpenHashSet::new, (Function<SequencedSet, List>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, copyOf(java.util.Collection ), (Ljava/util/SequencedSet;)Ljava/util/List;)());
    public static final Codec<TooltipDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("hide_tooltip", (Object)false).forGetter(TooltipDisplay::hideTooltip), (App)COMPONENT_SET_CODEC.optionalFieldOf("hidden_components", (Object)ReferenceSortedSets.emptySet()).forGetter(TooltipDisplay::hiddenComponents)).apply((Applicative)instance, TooltipDisplay::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, TooltipDisplay> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.BOOL, TooltipDisplay::hideTooltip, DataComponentType.STREAM_CODEC.apply(ByteBufCodecs.collection(ReferenceLinkedOpenHashSet::new)), TooltipDisplay::hiddenComponents, TooltipDisplay::new);
    public static final TooltipDisplay DEFAULT = new TooltipDisplay(false, (SequencedSet<DataComponentType<?>>)ReferenceSortedSets.emptySet());

    public TooltipDisplay withHidden(DataComponentType<?> dataComponentType, boolean bl) {
        if (this.hiddenComponents.contains(dataComponentType) == bl) {
            return this;
        }
        ReferenceLinkedOpenHashSet sequencedSet = new ReferenceLinkedOpenHashSet(this.hiddenComponents);
        if (bl) {
            sequencedSet.add(dataComponentType);
        } else {
            sequencedSet.remove(dataComponentType);
        }
        return new TooltipDisplay(this.hideTooltip, (SequencedSet<DataComponentType<?>>)sequencedSet);
    }

    public boolean shows(DataComponentType<?> dataComponentType) {
        return !this.hideTooltip && !this.hiddenComponents.contains(dataComponentType);
    }
}

