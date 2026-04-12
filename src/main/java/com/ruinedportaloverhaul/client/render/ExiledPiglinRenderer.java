package com.ruinedportaloverhaul.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.Identifier;

public final class ExiledPiglinRenderer extends WanderingTraderRenderer {
    private final Identifier texture;

    public ExiledPiglinRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public Identifier getTextureLocation(VillagerRenderState villagerRenderState) {
        return this.texture;
    }
}
