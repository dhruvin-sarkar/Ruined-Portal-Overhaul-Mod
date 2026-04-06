/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item;

import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class MissingItemModel
implements ItemModel {
    private final List<BakedQuad> quads;
    private final Supplier<Vector3fc[]> extents;
    private final ModelRenderProperties properties;

    public MissingItemModel(List<BakedQuad> list, ModelRenderProperties modelRenderProperties) {
        this.quads = list;
        this.properties = modelRenderProperties;
        this.extents = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(this.quads));
    }

    @Override
    public void update(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemModelResolver itemModelResolver, ItemDisplayContext itemDisplayContext, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
        itemStackRenderState.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layerRenderState = itemStackRenderState.newLayer();
        layerRenderState.setRenderType(Sheets.cutoutBlockSheet());
        this.properties.applyToLayer(layerRenderState, itemDisplayContext);
        layerRenderState.setExtents(this.extents);
        layerRenderState.prepareQuadList().addAll(this.quads);
    }
}

