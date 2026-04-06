package com.ruinedportaloverhaul.client.render;

import com.ruinedportaloverhaul.entity.PiglinIllagerEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.resources.Identifier;

public class PiglinIllagerRenderer<T extends PiglinIllagerEntity> extends IllagerRenderer<T, IllagerRenderState> {
    private final Identifier texture;

    public PiglinIllagerRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.PILLAGER)), 0.5f);
        this.texture = texture;
        this.addLayer(new ItemInHandLayer<IllagerRenderState, IllagerModel<IllagerRenderState>>(this));
    }

    @Override
    public Identifier getTextureLocation(IllagerRenderState illagerRenderState) {
        return this.texture;
    }

    @Override
    public IllagerRenderState createRenderState() {
        return new IllagerRenderState();
    }
}
