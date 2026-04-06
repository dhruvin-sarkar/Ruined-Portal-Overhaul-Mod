/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.feline;

import java.util.List;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.entity.animal.feline.CatVariant;
import net.minecraft.world.entity.variant.MoonBrightnessCheck;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.entity.variant.StructureCheck;
import net.minecraft.world.level.levelgen.structure.Structure;

public interface CatVariants {
    public static final ResourceKey<CatVariant> TABBY = CatVariants.createKey("tabby");
    public static final ResourceKey<CatVariant> BLACK = CatVariants.createKey("black");
    public static final ResourceKey<CatVariant> RED = CatVariants.createKey("red");
    public static final ResourceKey<CatVariant> SIAMESE = CatVariants.createKey("siamese");
    public static final ResourceKey<CatVariant> BRITISH_SHORTHAIR = CatVariants.createKey("british_shorthair");
    public static final ResourceKey<CatVariant> CALICO = CatVariants.createKey("calico");
    public static final ResourceKey<CatVariant> PERSIAN = CatVariants.createKey("persian");
    public static final ResourceKey<CatVariant> RAGDOLL = CatVariants.createKey("ragdoll");
    public static final ResourceKey<CatVariant> WHITE = CatVariants.createKey("white");
    public static final ResourceKey<CatVariant> JELLIE = CatVariants.createKey("jellie");
    public static final ResourceKey<CatVariant> ALL_BLACK = CatVariants.createKey("all_black");

    private static ResourceKey<CatVariant> createKey(String string) {
        return ResourceKey.create(Registries.CAT_VARIANT, Identifier.withDefaultNamespace(string));
    }

    public static void bootstrap(BootstrapContext<CatVariant> bootstrapContext) {
        HolderGetter<Structure> holderGetter = bootstrapContext.lookup(Registries.STRUCTURE);
        CatVariants.registerForAnyConditions(bootstrapContext, TABBY, "entity/cat/tabby");
        CatVariants.registerForAnyConditions(bootstrapContext, BLACK, "entity/cat/black");
        CatVariants.registerForAnyConditions(bootstrapContext, RED, "entity/cat/red");
        CatVariants.registerForAnyConditions(bootstrapContext, SIAMESE, "entity/cat/siamese");
        CatVariants.registerForAnyConditions(bootstrapContext, BRITISH_SHORTHAIR, "entity/cat/british_shorthair");
        CatVariants.registerForAnyConditions(bootstrapContext, CALICO, "entity/cat/calico");
        CatVariants.registerForAnyConditions(bootstrapContext, PERSIAN, "entity/cat/persian");
        CatVariants.registerForAnyConditions(bootstrapContext, RAGDOLL, "entity/cat/ragdoll");
        CatVariants.registerForAnyConditions(bootstrapContext, WHITE, "entity/cat/white");
        CatVariants.registerForAnyConditions(bootstrapContext, JELLIE, "entity/cat/jellie");
        CatVariants.register(bootstrapContext, ALL_BLACK, "entity/cat/all_black", new SpawnPrioritySelectors(List.of(new PriorityProvider.Selector(new StructureCheck(holderGetter.getOrThrow(StructureTags.CATS_SPAWN_AS_BLACK)), 1), new PriorityProvider.Selector(new MoonBrightnessCheck(MinMaxBounds.Doubles.atLeast(0.9)), 0))));
    }

    private static void registerForAnyConditions(BootstrapContext<CatVariant> bootstrapContext, ResourceKey<CatVariant> resourceKey, String string) {
        CatVariants.register(bootstrapContext, resourceKey, string, SpawnPrioritySelectors.fallback(0));
    }

    private static void register(BootstrapContext<CatVariant> bootstrapContext, ResourceKey<CatVariant> resourceKey, String string, SpawnPrioritySelectors spawnPrioritySelectors) {
        bootstrapContext.register(resourceKey, new CatVariant(new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace(string)), spawnPrioritySelectors));
    }
}

