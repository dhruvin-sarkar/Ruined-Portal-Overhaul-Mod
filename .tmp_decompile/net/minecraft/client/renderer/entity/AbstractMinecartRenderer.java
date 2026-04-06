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
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.cart.MinecartModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.MinecartRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractMinecartRenderer<T extends AbstractMinecart, S extends MinecartRenderState>
extends EntityRenderer<T, S> {
    private static final Identifier MINECART_LOCATION = Identifier.withDefaultNamespace("textures/entity/minecart.png");
    private static final float DISPLAY_BLOCK_SCALE = 0.75f;
    protected final MinecartModel model;

    public AbstractMinecartRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation) {
        super(context);
        this.shadowRadius = 0.7f;
        this.model = new MinecartModel(context.bakeLayer(modelLayerLocation));
    }

    @Override
    public void submit(S minecartRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        BlockState blockState;
        super.submit(minecartRenderState, poseStack, submitNodeCollector, cameraRenderState);
        poseStack.pushPose();
        long l = ((MinecartRenderState)minecartRenderState).offsetSeed;
        float f = (((float)(l >> 16 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float g = (((float)(l >> 20 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float h = (((float)(l >> 24 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        poseStack.translate(f, g, h);
        if (((MinecartRenderState)minecartRenderState).isNewRender) {
            AbstractMinecartRenderer.newRender(minecartRenderState, poseStack);
        } else {
            AbstractMinecartRenderer.oldRender(minecartRenderState, poseStack);
        }
        float i = ((MinecartRenderState)minecartRenderState).hurtTime;
        if (i > 0.0f) {
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(Mth.sin(i) * i * ((MinecartRenderState)minecartRenderState).damageTime / 10.0f * (float)((MinecartRenderState)minecartRenderState).hurtDir));
        }
        if ((blockState = ((MinecartRenderState)minecartRenderState).displayBlockState).getRenderShape() != RenderShape.INVISIBLE) {
            poseStack.pushPose();
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.translate(-0.5f, (float)(((MinecartRenderState)minecartRenderState).displayOffset - 8) / 16.0f, 0.5f);
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
            this.submitMinecartContents(minecartRenderState, blockState, poseStack, submitNodeCollector, ((MinecartRenderState)minecartRenderState).lightCoords);
            poseStack.popPose();
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        submitNodeCollector.submitModel(this.model, minecartRenderState, poseStack, this.model.renderType(MINECART_LOCATION), ((MinecartRenderState)minecartRenderState).lightCoords, OverlayTexture.NO_OVERLAY, ((MinecartRenderState)minecartRenderState).outlineColor, null);
        poseStack.popPose();
    }

    private static <S extends MinecartRenderState> void newRender(S minecartRenderState, PoseStack poseStack) {
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(minecartRenderState.yRot));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-minecartRenderState.xRot));
        poseStack.translate(0.0f, 0.375f, 0.0f);
    }

    private static <S extends MinecartRenderState> void oldRender(S minecartRenderState, PoseStack poseStack) {
        double d = minecartRenderState.x;
        double e = minecartRenderState.y;
        double f = minecartRenderState.z;
        float g = minecartRenderState.xRot;
        float h = minecartRenderState.yRot;
        if (minecartRenderState.posOnRail != null && minecartRenderState.frontPos != null && minecartRenderState.backPos != null) {
            Vec3 vec3 = minecartRenderState.frontPos;
            Vec3 vec32 = minecartRenderState.backPos;
            poseStack.translate(minecartRenderState.posOnRail.x - d, (vec3.y + vec32.y) / 2.0 - e, minecartRenderState.posOnRail.z - f);
            Vec3 vec33 = vec32.add(-vec3.x, -vec3.y, -vec3.z);
            if (vec33.length() != 0.0) {
                vec33 = vec33.normalize();
                h = (float)(Math.atan2(vec33.z, vec33.x) * 180.0 / Math.PI);
                g = (float)(Math.atan(vec33.y) * 73.0);
            }
        }
        poseStack.translate(0.0f, 0.375f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - h));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-g));
    }

    @Override
    public void extractRenderState(T abstractMinecart, S minecartRenderState, float f) {
        super.extractRenderState(abstractMinecart, minecartRenderState, f);
        MinecartBehavior minecartBehavior = ((AbstractMinecart)abstractMinecart).getBehavior();
        if (minecartBehavior instanceof NewMinecartBehavior) {
            NewMinecartBehavior newMinecartBehavior = (NewMinecartBehavior)minecartBehavior;
            AbstractMinecartRenderer.newExtractState(abstractMinecart, newMinecartBehavior, minecartRenderState, f);
            ((MinecartRenderState)minecartRenderState).isNewRender = true;
        } else {
            minecartBehavior = ((AbstractMinecart)abstractMinecart).getBehavior();
            if (minecartBehavior instanceof OldMinecartBehavior) {
                OldMinecartBehavior oldMinecartBehavior = (OldMinecartBehavior)minecartBehavior;
                AbstractMinecartRenderer.oldExtractState(abstractMinecart, oldMinecartBehavior, minecartRenderState, f);
                ((MinecartRenderState)minecartRenderState).isNewRender = false;
            }
        }
        long l = (long)((Entity)abstractMinecart).getId() * 493286711L;
        ((MinecartRenderState)minecartRenderState).offsetSeed = l * l * 4392167121L + l * 98761L;
        ((MinecartRenderState)minecartRenderState).hurtTime = (float)((VehicleEntity)abstractMinecart).getHurtTime() - f;
        ((MinecartRenderState)minecartRenderState).hurtDir = ((VehicleEntity)abstractMinecart).getHurtDir();
        ((MinecartRenderState)minecartRenderState).damageTime = Math.max(((VehicleEntity)abstractMinecart).getDamage() - f, 0.0f);
        ((MinecartRenderState)minecartRenderState).displayOffset = ((AbstractMinecart)abstractMinecart).getDisplayOffset();
        ((MinecartRenderState)minecartRenderState).displayBlockState = ((AbstractMinecart)abstractMinecart).getDisplayBlockState();
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void newExtractState(T abstractMinecart, NewMinecartBehavior newMinecartBehavior, S minecartRenderState, float f) {
        if (newMinecartBehavior.cartHasPosRotLerp()) {
            minecartRenderState.renderPos = newMinecartBehavior.getCartLerpPosition(f);
            minecartRenderState.xRot = newMinecartBehavior.getCartLerpXRot(f);
            minecartRenderState.yRot = newMinecartBehavior.getCartLerpYRot(f);
        } else {
            minecartRenderState.renderPos = null;
            minecartRenderState.xRot = abstractMinecart.getXRot();
            minecartRenderState.yRot = abstractMinecart.getYRot();
        }
    }

    private static <T extends AbstractMinecart, S extends MinecartRenderState> void oldExtractState(T abstractMinecart, OldMinecartBehavior oldMinecartBehavior, S minecartRenderState, float f) {
        float g = 0.3f;
        minecartRenderState.xRot = abstractMinecart.getXRot(f);
        minecartRenderState.yRot = abstractMinecart.getYRot(f);
        double d = minecartRenderState.x;
        double e = minecartRenderState.y;
        double h = minecartRenderState.z;
        Vec3 vec3 = oldMinecartBehavior.getPos(d, e, h);
        if (vec3 != null) {
            minecartRenderState.posOnRail = vec3;
            Vec3 vec32 = oldMinecartBehavior.getPosOffs(d, e, h, 0.3f);
            Vec3 vec33 = oldMinecartBehavior.getPosOffs(d, e, h, -0.3f);
            minecartRenderState.frontPos = (Vec3)Objects.requireNonNullElse((Object)vec32, (Object)vec3);
            minecartRenderState.backPos = (Vec3)Objects.requireNonNullElse((Object)vec33, (Object)vec3);
        } else {
            minecartRenderState.posOnRail = null;
            minecartRenderState.frontPos = null;
            minecartRenderState.backPos = null;
        }
    }

    protected void submitMinecartContents(S minecartRenderState, BlockState blockState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i) {
        submitNodeCollector.submitBlock(poseStack, blockState, i, OverlayTexture.NO_OVERLAY, ((MinecartRenderState)minecartRenderState).outlineColor);
    }

    @Override
    protected AABB getBoundingBoxForCulling(T abstractMinecart) {
        AABB aABB = super.getBoundingBoxForCulling(abstractMinecart);
        if (!((AbstractMinecart)abstractMinecart).getDisplayBlockState().isAir()) {
            return aABB.expandTowards(0.0, (float)((AbstractMinecart)abstractMinecart).getDisplayOffset() * 0.75f / 16.0f, 0.0);
        }
        return aABB;
    }

    @Override
    public Vec3 getRenderOffset(S minecartRenderState) {
        Vec3 vec3 = super.getRenderOffset(minecartRenderState);
        if (((MinecartRenderState)minecartRenderState).isNewRender && ((MinecartRenderState)minecartRenderState).renderPos != null) {
            return vec3.add(((MinecartRenderState)minecartRenderState).renderPos.x - ((MinecartRenderState)minecartRenderState).x, ((MinecartRenderState)minecartRenderState).renderPos.y - ((MinecartRenderState)minecartRenderState).y, ((MinecartRenderState)minecartRenderState).renderPos.z - ((MinecartRenderState)minecartRenderState).z);
        }
        return vec3;
    }
}

