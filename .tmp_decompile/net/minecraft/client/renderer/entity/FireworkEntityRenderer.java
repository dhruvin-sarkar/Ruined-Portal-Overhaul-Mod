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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FireworkRocketRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class FireworkEntityRenderer
extends EntityRenderer<FireworkRocketEntity, FireworkRocketRenderState> {
    private final ItemModelResolver itemModelResolver;

    public FireworkEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
    }

    @Override
    public void submit(FireworkRocketRenderState fireworkRocketRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
        if (fireworkRocketRenderState.isShotAtAngle) {
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        }
        fireworkRocketRenderState.item.submit(poseStack, submitNodeCollector, fireworkRocketRenderState.lightCoords, OverlayTexture.NO_OVERLAY, fireworkRocketRenderState.outlineColor);
        poseStack.popPose();
        super.submit(fireworkRocketRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public FireworkRocketRenderState createRenderState() {
        return new FireworkRocketRenderState();
    }

    @Override
    public void extractRenderState(FireworkRocketEntity fireworkRocketEntity, FireworkRocketRenderState fireworkRocketRenderState, float f) {
        super.extractRenderState(fireworkRocketEntity, fireworkRocketRenderState, f);
        fireworkRocketRenderState.isShotAtAngle = fireworkRocketEntity.isShotAtAngle();
        this.itemModelResolver.updateForNonLiving(fireworkRocketRenderState.item, fireworkRocketEntity.getItem(), ItemDisplayContext.GROUND, fireworkRocketEntity);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

