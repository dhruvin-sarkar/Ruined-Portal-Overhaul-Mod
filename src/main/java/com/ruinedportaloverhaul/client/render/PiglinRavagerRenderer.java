package com.ruinedportaloverhaul.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.Identifier;

public class PiglinRavagerRenderer extends RavagerRenderer {
    private final Identifier texture;

    public PiglinRavagerRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public Identifier getTextureLocation(RavagerRenderState state) {
        return this.texture;
    }
}
