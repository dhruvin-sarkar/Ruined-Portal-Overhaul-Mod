/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionBridgePools {
    public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
        HolderGetter<StructureProcessorList> holderGetter = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
        Holder.Reference<StructureProcessorList> holder = holderGetter.getOrThrow(ProcessorLists.ENTRANCE_REPLACEMENT);
        Holder.Reference<StructureProcessorList> holder2 = holderGetter.getOrThrow(ProcessorLists.BASTION_GENERIC_DEGRADATION);
        Holder.Reference<StructureProcessorList> holder3 = holderGetter.getOrThrow(ProcessorLists.BRIDGE);
        Holder.Reference<StructureProcessorList> holder4 = holderGetter.getOrThrow(ProcessorLists.RAMPART_DEGRADATION);
        HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> holder5 = holderGetter2.getOrThrow(Pools.EMPTY);
        Pools.register(bootstrapContext, "bastion/bridge/starting_pieces", new StructureTemplatePool(holder5, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance", holder), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/starting_pieces/entrance_face", holder2), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/bridge/bridge_pieces", new StructureTemplatePool(holder5, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/bridge_pieces/bridge", holder3), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/bridge/legs", new StructureTemplatePool(holder5, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/legs/leg_0", holder2), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/legs/leg_1", holder2), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/bridge/walls", new StructureTemplatePool(holder5, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/walls/wall_base_0", holder4), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/walls/wall_base_1", holder4), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/bridge/ramparts", new StructureTemplatePool(holder5, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/ramparts/rampart_0", holder4), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/ramparts/rampart_1", holder4), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/bridge/rampart_plates", new StructureTemplatePool(holder5, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/rampart_plates/plate_0", holder4), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/bridge/connectors", new StructureTemplatePool(holder5, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/bridge/connectors/back_bridge_top", holder2), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/bridge/connectors/back_bridge_bottom", holder2), (Object)1)), StructureTemplatePool.Projection.RIGID));
    }
}

