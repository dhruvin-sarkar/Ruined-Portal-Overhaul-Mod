/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.blockentity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface BlockEntityRendererProvider<T extends BlockEntity, S extends BlockEntityRenderState> {
    public BlockEntityRenderer<T, S> create(Context var1);

    @Environment(value=EnvType.CLIENT)
    public record Context(BlockEntityRenderDispatcher blockEntityRenderDispatcher, BlockRenderDispatcher blockRenderDispatcher, ItemModelResolver itemModelResolver, ItemRenderer itemRenderer, EntityRenderDispatcher entityRenderer, EntityModelSet entityModelSet, Font font, MaterialSet materials, PlayerSkinRenderCache playerSkinRenderCache) {
        public ModelPart bakeLayer(ModelLayerLocation modelLayerLocation) {
            return this.entityModelSet.bakeLayer(modelLayerLocation);
        }
    }
}

