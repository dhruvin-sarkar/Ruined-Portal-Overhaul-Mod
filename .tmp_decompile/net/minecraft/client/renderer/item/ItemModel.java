/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item;

import com.mojang.serialization.MapCodec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RegistryContextSwapper;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface ItemModel {
    public void update(ItemStackRenderState var1, ItemStack var2, ItemModelResolver var3, ItemDisplayContext var4, @Nullable ClientLevel var5, @Nullable ItemOwner var6, int var7);

    @Environment(value=EnvType.CLIENT)
    public record BakingContext(ModelBaker blockModelBaker, EntityModelSet entityModelSet, MaterialSet materials, PlayerSkinRenderCache playerSkinRenderCache, ItemModel missingItemModel, @Nullable RegistryContextSwapper contextSwapper) implements SpecialModelRenderer.BakingContext
    {
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Unbaked
    extends ResolvableModel {
        public MapCodec<? extends Unbaked> type();

        public ItemModel bake(BakingContext var1);
    }
}

