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
import net.minecraft.client.model.animal.golem.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.IronGolemRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.golem.IronGolem;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class IronGolemRenderer
extends MobRenderer<IronGolem, IronGolemRenderState, IronGolemModel> {
    private static final Identifier GOLEM_LOCATION = Identifier.withDefaultNamespace("textures/entity/iron_golem/iron_golem.png");

    public IronGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7f);
        this.addLayer(new IronGolemCrackinessLayer(this));
        this.addLayer(new IronGolemFlowerLayer(this));
    }

    @Override
    public Identifier getTextureLocation(IronGolemRenderState ironGolemRenderState) {
        return GOLEM_LOCATION;
    }

    @Override
    public IronGolemRenderState createRenderState() {
        return new IronGolemRenderState();
    }

    @Override
    public void extractRenderState(IronGolem ironGolem, IronGolemRenderState ironGolemRenderState, float f) {
        super.extractRenderState(ironGolem, ironGolemRenderState, f);
        ironGolemRenderState.attackTicksRemaining = (float)ironGolem.getAttackAnimationTick() > 0.0f ? (float)ironGolem.getAttackAnimationTick() - f : 0.0f;
        ironGolemRenderState.offerFlowerTick = ironGolem.getOfferFlowerTick();
        ironGolemRenderState.crackiness = ironGolem.getCrackiness();
    }

    @Override
    protected void setupRotations(IronGolemRenderState ironGolemRenderState, PoseStack poseStack, float f, float g) {
        super.setupRotations(ironGolemRenderState, poseStack, f, g);
        if ((double)ironGolemRenderState.walkAnimationSpeed < 0.01) {
            return;
        }
        float h = 13.0f;
        float i = ironGolemRenderState.walkAnimationPos + 6.0f;
        float j = (Math.abs(i % 13.0f - 6.5f) - 3.25f) / 3.25f;
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(6.5f * j));
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((IronGolemRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

