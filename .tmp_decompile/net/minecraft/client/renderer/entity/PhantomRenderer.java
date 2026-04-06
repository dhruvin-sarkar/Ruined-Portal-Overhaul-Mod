/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.phantom.PhantomModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PhantomRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Phantom;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class PhantomRenderer
extends MobRenderer<Phantom, PhantomRenderState, PhantomModel> {
    private static final Identifier PHANTOM_LOCATION = Identifier.withDefaultNamespace("textures/entity/phantom.png");

    public PhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new PhantomModel(context.bakeLayer(ModelLayers.PHANTOM)), 0.75f);
        this.addLayer(new PhantomEyesLayer(this));
    }

    @Override
    public Identifier getTextureLocation(PhantomRenderState phantomRenderState) {
        return PHANTOM_LOCATION;
    }

    @Override
    public PhantomRenderState createRenderState() {
        return new PhantomRenderState();
    }

    @Override
    public void extractRenderState(Phantom phantom, PhantomRenderState phantomRenderState, float f) {
        super.extractRenderState(phantom, phantomRenderState, f);
        phantomRenderState.flapTime = (float)phantom.getUniqueFlapTickOffset() + phantomRenderState.ageInTicks;
        phantomRenderState.size = phantom.getPhantomSize();
    }

    @Override
    protected void scale(PhantomRenderState phantomRenderState, PoseStack poseStack) {
        float f = 1.0f + 0.15f * (float)phantomRenderState.size;
        poseStack.scale(f, f, f);
        poseStack.translate(0.0f, 1.3125f, 0.1875f);
    }

    @Override
    protected void setupRotations(PhantomRenderState phantomRenderState, PoseStack poseStack, float f, float g) {
        super.setupRotations(phantomRenderState, poseStack, f, g);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(phantomRenderState.xRot));
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PhantomRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

