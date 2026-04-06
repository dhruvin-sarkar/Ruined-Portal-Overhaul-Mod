/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.equipment.trim;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ProvidesTrimMaterial;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimMaterial;

public class TrimMaterials {
    public static final ResourceKey<TrimMaterial> QUARTZ = TrimMaterials.registryKey("quartz");
    public static final ResourceKey<TrimMaterial> IRON = TrimMaterials.registryKey("iron");
    public static final ResourceKey<TrimMaterial> NETHERITE = TrimMaterials.registryKey("netherite");
    public static final ResourceKey<TrimMaterial> REDSTONE = TrimMaterials.registryKey("redstone");
    public static final ResourceKey<TrimMaterial> COPPER = TrimMaterials.registryKey("copper");
    public static final ResourceKey<TrimMaterial> GOLD = TrimMaterials.registryKey("gold");
    public static final ResourceKey<TrimMaterial> EMERALD = TrimMaterials.registryKey("emerald");
    public static final ResourceKey<TrimMaterial> DIAMOND = TrimMaterials.registryKey("diamond");
    public static final ResourceKey<TrimMaterial> LAPIS = TrimMaterials.registryKey("lapis");
    public static final ResourceKey<TrimMaterial> AMETHYST = TrimMaterials.registryKey("amethyst");
    public static final ResourceKey<TrimMaterial> RESIN = TrimMaterials.registryKey("resin");

    public static void bootstrap(BootstrapContext<TrimMaterial> bootstrapContext) {
        TrimMaterials.register(bootstrapContext, QUARTZ, Style.EMPTY.withColor(14931140), MaterialAssetGroup.QUARTZ);
        TrimMaterials.register(bootstrapContext, IRON, Style.EMPTY.withColor(0xECECEC), MaterialAssetGroup.IRON);
        TrimMaterials.register(bootstrapContext, NETHERITE, Style.EMPTY.withColor(6445145), MaterialAssetGroup.NETHERITE);
        TrimMaterials.register(bootstrapContext, REDSTONE, Style.EMPTY.withColor(9901575), MaterialAssetGroup.REDSTONE);
        TrimMaterials.register(bootstrapContext, COPPER, Style.EMPTY.withColor(11823181), MaterialAssetGroup.COPPER);
        TrimMaterials.register(bootstrapContext, GOLD, Style.EMPTY.withColor(14594349), MaterialAssetGroup.GOLD);
        TrimMaterials.register(bootstrapContext, EMERALD, Style.EMPTY.withColor(1155126), MaterialAssetGroup.EMERALD);
        TrimMaterials.register(bootstrapContext, DIAMOND, Style.EMPTY.withColor(7269586), MaterialAssetGroup.DIAMOND);
        TrimMaterials.register(bootstrapContext, LAPIS, Style.EMPTY.withColor(4288151), MaterialAssetGroup.LAPIS);
        TrimMaterials.register(bootstrapContext, AMETHYST, Style.EMPTY.withColor(10116294), MaterialAssetGroup.AMETHYST);
        TrimMaterials.register(bootstrapContext, RESIN, Style.EMPTY.withColor(16545810), MaterialAssetGroup.RESIN);
    }

    public static Optional<Holder<TrimMaterial>> getFromIngredient(HolderLookup.Provider provider, ItemStack itemStack) {
        ProvidesTrimMaterial providesTrimMaterial = itemStack.get(DataComponents.PROVIDES_TRIM_MATERIAL);
        return providesTrimMaterial != null ? providesTrimMaterial.unwrap(provider) : Optional.empty();
    }

    private static void register(BootstrapContext<TrimMaterial> bootstrapContext, ResourceKey<TrimMaterial> resourceKey, Style style, MaterialAssetGroup materialAssetGroup) {
        MutableComponent component = Component.translatable(Util.makeDescriptionId("trim_material", resourceKey.identifier())).withStyle(style);
        bootstrapContext.register(resourceKey, new TrimMaterial(materialAssetGroup, component));
    }

    private static ResourceKey<TrimMaterial> registryKey(String string) {
        return ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.withDefaultNamespace(string));
    }
}

