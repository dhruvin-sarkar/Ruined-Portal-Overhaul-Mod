/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

@Environment(value=EnvType.CLIENT)
public class SlimeRenderer
extends MobRenderer<Slime, SlimeRenderState, SlimeModel> {
    public static final Identifier SLIME_LOCATION = Identifier.withDefaultNamespace("textures/entity/slime/slime.png");

    public SlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25f);
        this.addLayer(new SlimeOuterLayer(this, context.getModelSet()));
    }

    @Override
    protected float getShadowRadius(SlimeRenderState slimeRenderState) {
        return (float)slimeRenderState.size * 0.25f;
    }

    @Override
    protected void scale(SlimeRenderState slimeRenderState, PoseStack poseStack) {
        float f = 0.999f;
        poseStack.scale(0.999f, 0.999f, 0.999f);
        poseStack.translate(0.0f, 0.001f, 0.0f);
        float g = slimeRenderState.size;
        float h = slimeRenderState.squish / (g * 0.5f + 1.0f);
        float i = 1.0f / (h + 1.0f);
        poseStack.scale(i * g, 1.0f / i * g, i * g);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState slimeRenderState) {
        return SLIME_LOCATION;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(Slime slime, SlimeRenderState slimeRenderState, float f) {
        super.extractRenderState(slime, slimeRenderState, f);
        slimeRenderState.squish = Mth.lerp(f, slime.oSquish, slime.squish);
        slimeRenderState.size = slime.getSize();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntityRenderState livingEntityRenderState) {
        return this.getShadowRadius((SlimeRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((SlimeRenderState)entityRenderState);
    }
}

