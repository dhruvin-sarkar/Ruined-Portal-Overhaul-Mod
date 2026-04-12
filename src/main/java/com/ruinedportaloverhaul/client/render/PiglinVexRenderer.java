package com.ruinedportaloverhaul.client.render;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.state.VexRenderState;
import net.minecraft.resources.Identifier;

public class PiglinVexRenderer extends VexRenderer {
    private final Identifier texture;

    public PiglinVexRenderer(EntityRendererProvider.Context context, Identifier texture) {
        super(context);
        this.texture = texture;
    }

    @Override
    public Identifier getTextureLocation(VexRenderState state) {
        return this.texture;
    }
}
