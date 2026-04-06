/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.wolf;

import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class WolfVariants {
    public static final ResourceKey<WolfVariant> PALE = WolfVariants.createKey("pale");
    public static final ResourceKey<WolfVariant> SPOTTED = WolfVariants.createKey("spotted");
    public static final ResourceKey<WolfVariant> SNOWY = WolfVariants.createKey("snowy");
    public static final ResourceKey<WolfVariant> BLACK = WolfVariants.createKey("black");
    public static final ResourceKey<WolfVariant> ASHEN = WolfVariants.createKey("ashen");
    public static final ResourceKey<WolfVariant> RUSTY = WolfVariants.createKey("rusty");
    public static final ResourceKey<WolfVariant> WOODS = WolfVariants.createKey("woods");
    public static final ResourceKey<WolfVariant> CHESTNUT = WolfVariants.createKey("chestnut");
    public static final ResourceKey<WolfVariant> STRIPED = WolfVariants.createKey("striped");
    public static final ResourceKey<WolfVariant> DEFAULT = PALE;

    private static ResourceKey<WolfVariant> createKey(String string) {
        return ResourceKey.create(Registries.WOLF_VARIANT, Identifier.withDefaultNamespace(string));
    }

    private static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, ResourceKey<Biome> resourceKey2) {
        WolfVariants.register(bootstrapContext, resourceKey, string, WolfVariants.highPrioBiome(HolderSet.direct(bootstrapContext.lookup(Registries.BIOME).getOrThrow(resourceKey2))));
    }

    private static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, TagKey<Biome> tagKey) {
        WolfVariants.register(bootstrapContext, resourceKey, string, WolfVariants.highPrioBiome(bootstrapContext.lookup(Registries.BIOME).getOrThrow(tagKey)));
    }

    private static SpawnPrioritySelectors highPrioBiome(HolderSet<Biome> holderSet) {
        return SpawnPrioritySelectors.single(new BiomeCheck(holderSet), 1);
    }

    private static void register(BootstrapContext<WolfVariant> bootstrapContext, ResourceKey<WolfVariant> resourceKey, String string, SpawnPrioritySelectors spawnPrioritySelectors) {
        Identifier identifier = Identifier.withDefaultNamespace("entity/wolf/" + string);
        Identifier identifier2 = Identifier.withDefaultNamespace("entity/wolf/" + string + "_tame");
        Identifier identifier3 = Identifier.withDefaultNamespace("entity/wolf/" + string + "_angry");
        bootstrapContext.register(resourceKey, new WolfVariant(new WolfVariant.AssetInfo(new ClientAsset.ResourceTexture(identifier), new ClientAsset.ResourceTexture(identifier2), new ClientAsset.ResourceTexture(identifier3)), spawnPrioritySelectors));
    }

    public static void bootstrap(BootstrapContext<WolfVariant> bootstrapContext) {
        WolfVariants.register(bootstrapContext, PALE, "wolf", SpawnPrioritySelectors.fallback(0));
        WolfVariants.register(bootstrapContext, SPOTTED, "wolf_spotted", BiomeTags.IS_SAVANNA);
        WolfVariants.register(bootstrapContext, SNOWY, "wolf_snowy", Biomes.GROVE);
        WolfVariants.register(bootstrapContext, BLACK, "wolf_black", Biomes.OLD_GROWTH_PINE_TAIGA);
        WolfVariants.register(bootstrapContext, ASHEN, "wolf_ashen", Biomes.SNOWY_TAIGA);
        WolfVariants.register(bootstrapContext, RUSTY, "wolf_rusty", BiomeTags.IS_JUNGLE);
        WolfVariants.register(bootstrapContext, WOODS, "wolf_woods", Biomes.FOREST);
        WolfVariants.register(bootstrapContext, CHESTNUT, "wolf_chestnut", Biomes.OLD_GROWTH_SPRUCE_TAIGA);
        WolfVariants.register(bootstrapContext, STRIPED, "wolf_striped", BiomeTags.IS_BADLANDS);
    }
}

