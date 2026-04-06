/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractBoatRenderer
extends EntityRenderer<AbstractBoat, BoatRenderState> {
    public AbstractBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8f;
    }

    @Override
    public void submit(BoatRenderState boatRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.375f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - boatRenderState.yRot));
        float f = boatRenderState.hurtTime;
        if (f > 0.0f) {
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.sin(f) * f * boatRenderState.damageTime / 10.0f * (float)boatRenderState.hurtDir));
        }
        if (!boatRenderState.isUnderWater && !Mth.equal(boatRenderState.bubbleAngle, 0.0f)) {
            poseStack.mulPose((Quaternionfc)new Quaternionf().setAngleAxis(boatRenderState.bubbleAngle * ((float)Math.PI / 180), 1.0f, 0.0f, 1.0f));
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
        submitNodeCollector.submitModel(this.model(), boatRenderState, poseStack, this.renderType(), boatRenderState.lightCoords, OverlayTexture.NO_OVERLAY, boatRenderState.outlineColor, null);
        this.submitTypeAdditions(boatRenderState, poseStack, submitNodeCollector, boatRenderState.lightCoords);
        poseStack.popPose();
        super.submit(boatRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    protected void submitTypeAdditions(BoatRenderState boatRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i) {
    }

    protected abstract EntityModel<BoatRenderState> model();

    protected abstract RenderType renderType();

    @Override
    public BoatRenderState createRenderState() {
        return new BoatRenderState();
    }

    @Override
    public void extractRenderState(AbstractBoat abstractBoat, BoatRenderState boatRenderState, float f) {
        super.extractRenderState(abstractBoat, boatRenderState, f);
        boatRenderState.yRot = abstractBoat.getYRot(f);
        boatRenderState.hurtTime = (float)abstractBoat.getHurtTime() - f;
        boatRenderState.hurtDir = abstractBoat.getHurtDir();
        boatRenderState.damageTime = Math.max(abstractBoat.getDamage() - f, 0.0f);
        boatRenderState.bubbleAngle = abstractBoat.getBubbleAngle(f);
        boatRenderState.isUnderWater = abstractBoat.isUnderWater();
        boatRenderState.rowingTimeLeft = abstractBoat.getRowingTime(0, f);
        boatRenderState.rowingTimeRight = abstractBoat.getRowingTime(1, f);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

