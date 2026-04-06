/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.cow;

import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.TemperatureVariants;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;

public class CowVariants {
    public static final ResourceKey<CowVariant> TEMPERATE = CowVariants.createKey(TemperatureVariants.TEMPERATE);
    public static final ResourceKey<CowVariant> WARM = CowVariants.createKey(TemperatureVariants.WARM);
    public static final ResourceKey<CowVariant> COLD = CowVariants.createKey(TemperatureVariants.COLD);
    public static final ResourceKey<CowVariant> DEFAULT = TEMPERATE;

    private static ResourceKey<CowVariant> createKey(Identifier identifier) {
        return ResourceKey.create(Registries.COW_VARIANT, identifier);
    }

    public static void bootstrap(BootstrapContext<CowVariant> bootstrapContext) {
        CowVariants.register(bootstrapContext, TEMPERATE, CowVariant.ModelType.NORMAL, "temperate_cow", SpawnPrioritySelectors.fallback(0));
        CowVariants.register(bootstrapContext, WARM, CowVariant.ModelType.WARM, "warm_cow", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
        CowVariants.register(bootstrapContext, COLD, CowVariant.ModelType.COLD, "cold_cow", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
    }

    private static void register(BootstrapContext<CowVariant> bootstrapContext, ResourceKey<CowVariant> resourceKey, CowVariant.ModelType modelType, String string, TagKey<Biome> tagKey) {
        HolderSet.Named<Biome> holderSet = bootstrapContext.lookup(Registries.BIOME).getOrThrow(tagKey);
        CowVariants.register(bootstrapContext, resourceKey, modelType, string, SpawnPrioritySelectors.single(new BiomeCheck(holderSet), 1));
    }

    private static void register(BootstrapContext<CowVariant> bootstrapContext, ResourceKey<CowVariant> resourceKey, CowVariant.ModelType modelType, String string, SpawnPrioritySelectors spawnPrioritySelectors) {
        Identifier identifier = Identifier.withDefaultNamespace("entity/cow/" + string);
        bootstrapContext.register(resourceKey, new CowVariant(new ModelAndTexture<CowVariant.ModelType>(modelType, identifier), spawnPrioritySelectors));
    }
}

