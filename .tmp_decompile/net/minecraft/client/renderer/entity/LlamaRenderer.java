/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.llama.LlamaModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.LlamaDecorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LlamaRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Llama;

@Environment(value=EnvType.CLIENT)
public class LlamaRenderer
extends AgeableMobRenderer<Llama, LlamaRenderState, LlamaModel> {
    private static final Identifier CREAMY = Identifier.withDefaultNamespace("textures/entity/llama/creamy.png");
    private static final Identifier WHITE = Identifier.withDefaultNamespace("textures/entity/llama/white.png");
    private static final Identifier BROWN = Identifier.withDefaultNamespace("textures/entity/llama/brown.png");
    private static final Identifier GRAY = Identifier.withDefaultNamespace("textures/entity/llama/gray.png");

    public LlamaRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2) {
        super(context, new LlamaModel(context.bakeLayer(modelLayerLocation)), new LlamaModel(context.bakeLayer(modelLayerLocation2)), 0.7f);
        this.addLayer(new LlamaDecorLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
    }

    @Override
    public Identifier getTextureLocation(LlamaRenderState llamaRenderState) {
        return switch (llamaRenderState.variant) {
            default -> throw new MatchException(null, null);
            case Llama.Variant.CREAMY -> CREAMY;
            case Llama.Variant.WHITE -> WHITE;
            case Llama.Variant.BROWN -> BROWN;
            case Llama.Variant.GRAY -> GRAY;
        };
    }

    @Override
    public LlamaRenderState createRenderState() {
        return new LlamaRenderState();
    }

    @Override
    public void extractRenderState(Llama llama, LlamaRenderState llamaRenderState, float f) {
        super.extractRenderState(llama, llamaRenderState, f);
        llamaRenderState.variant = llama.getVariant();
        llamaRenderState.hasChest = !llama.isBaby() && llama.hasChest();
        llamaRenderState.bodyItem = llama.getBodyArmorItem();
        llamaRenderState.isTraderLlama = llama.isTraderLlama();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((LlamaRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

