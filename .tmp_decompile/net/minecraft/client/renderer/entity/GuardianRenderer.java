/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.guardian.GuardianModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.GuardianRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GuardianRenderer
extends MobRenderer<Guardian, GuardianRenderState, GuardianModel> {
    private static final Identifier GUARDIAN_LOCATION = Identifier.withDefaultNamespace("textures/entity/guardian.png");
    private static final Identifier GUARDIAN_BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/guardian_beam.png");
    private static final RenderType BEAM_RENDER_TYPE = RenderTypes.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

    public GuardianRenderer(EntityRendererProvider.Context context) {
        this(context, 0.5f, ModelLayers.GUARDIAN);
    }

    protected GuardianRenderer(EntityRendererProvider.Context context, float f, ModelLayerLocation modelLayerLocation) {
        super(context, new GuardianModel(context.bakeLayer(modelLayerLocation)), f);
    }

    @Override
    public boolean shouldRender(Guardian guardian, Frustum frustum, double d, double e, double f) {
        LivingEntity livingEntity;
        if (super.shouldRender(guardian, frustum, d, e, f)) {
            return true;
        }
        if (guardian.hasActiveAttackTarget() && (livingEntity = guardian.getActiveAttackTarget()) != null) {
            Vec3 vec3 = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, 1.0f);
            Vec3 vec32 = this.getPosition(guardian, guardian.getEyeHeight(), 1.0f);
            return frustum.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z));
        }
        return false;
    }

    private Vec3 getPosition(LivingEntity livingEntity, double d, float f) {
        double e = Mth.lerp((double)f, livingEntity.xOld, livingEntity.getX());
        double g = Mth.lerp((double)f, livingEntity.yOld, livingEntity.getY()) + d;
        double h = Mth.lerp((double)f, livingEntity.zOld, livingEntity.getZ());
        return new Vec3(e, g, h);
    }

    @Override
    public void submit(GuardianRenderState guardianRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        super.submit(guardianRenderState, poseStack, submitNodeCollector, cameraRenderState);
        Vec3 vec3 = guardianRenderState.attackTargetPosition;
        if (vec3 != null) {
            float f = guardianRenderState.attackTime * 0.5f % 1.0f;
            poseStack.pushPose();
            poseStack.translate(0.0f, guardianRenderState.eyeHeight, 0.0f);
            GuardianRenderer.renderBeam(poseStack, submitNodeCollector, vec3.subtract(guardianRenderState.eyePosition), guardianRenderState.attackTime, guardianRenderState.attackScale, f);
            poseStack.popPose();
        }
    }

    private static void renderBeam(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Vec3 vec3, float f, float g, float h) {
        float i = (float)(vec3.length() + 1.0);
        vec3 = vec3.normalize();
        float j = (float)Math.acos(vec3.y);
        float k = 1.5707964f - (float)Math.atan2(vec3.z, vec3.x);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(k * 57.295776f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(j * 57.295776f));
        float l = f * 0.05f * -1.5f;
        float m = g * g;
        int n = 64 + (int)(m * 191.0f);
        int o = 32 + (int)(m * 191.0f);
        int p = 128 - (int)(m * 64.0f);
        float q = 0.2f;
        float r = 0.282f;
        float s = Mth.cos(l + 2.3561945f) * 0.282f;
        float t = Mth.sin(l + 2.3561945f) * 0.282f;
        float u = Mth.cos(l + 0.7853982f) * 0.282f;
        float v = Mth.sin(l + 0.7853982f) * 0.282f;
        float w = Mth.cos(l + 3.926991f) * 0.282f;
        float x = Mth.sin(l + 3.926991f) * 0.282f;
        float y = Mth.cos(l + 5.4977875f) * 0.282f;
        float z = Mth.sin(l + 5.4977875f) * 0.282f;
        float aa = Mth.cos(l + (float)Math.PI) * 0.2f;
        float ab = Mth.sin(l + (float)Math.PI) * 0.2f;
        float ac = Mth.cos(l + 0.0f) * 0.2f;
        float ad = Mth.sin(l + 0.0f) * 0.2f;
        float ae = Mth.cos(l + 1.5707964f) * 0.2f;
        float af = Mth.sin(l + 1.5707964f) * 0.2f;
        float ag = Mth.cos(l + 4.712389f) * 0.2f;
        float ah = Mth.sin(l + 4.712389f) * 0.2f;
        float ai = i;
        float aj = 0.0f;
        float ak = 0.4999f;
        float al = -1.0f + h;
        float am = al + i * 2.5f;
        submitNodeCollector.submitCustomGeometry(poseStack, BEAM_RENDER_TYPE, (pose, vertexConsumer) -> {
            GuardianRenderer.vertex(vertexConsumer, pose, aa, ai, ab, n, o, p, 0.4999f, am);
            GuardianRenderer.vertex(vertexConsumer, pose, aa, 0.0f, ab, n, o, p, 0.4999f, al);
            GuardianRenderer.vertex(vertexConsumer, pose, ac, 0.0f, ad, n, o, p, 0.0f, al);
            GuardianRenderer.vertex(vertexConsumer, pose, ac, ai, ad, n, o, p, 0.0f, am);
            GuardianRenderer.vertex(vertexConsumer, pose, ae, ai, af, n, o, p, 0.4999f, am);
            GuardianRenderer.vertex(vertexConsumer, pose, ae, 0.0f, af, n, o, p, 0.4999f, al);
            GuardianRenderer.vertex(vertexConsumer, pose, ag, 0.0f, ah, n, o, p, 0.0f, al);
            GuardianRenderer.vertex(vertexConsumer, pose, ag, ai, ah, n, o, p, 0.0f, am);
            float ac = Mth.floor(f) % 2 == 0 ? 0.5f : 0.0f;
            GuardianRenderer.vertex(vertexConsumer, pose, s, ai, t, n, o, p, 0.5f, ac + 0.5f);
            GuardianRenderer.vertex(vertexConsumer, pose, u, ai, v, n, o, p, 1.0f, ac + 0.5f);
            GuardianRenderer.vertex(vertexConsumer, pose, y, ai, z, n, o, p, 1.0f, ac);
            GuardianRenderer.vertex(vertexConsumer, pose, w, ai, x, n, o, p, 0.5f, ac);
        });
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, float f, float g, float h, int i, int j, int k, float l, float m) {
        vertexConsumer.addVertex(pose, f, g, h).setColor(i, j, k, 255).setUv(l, m).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public Identifier getTextureLocation(GuardianRenderState guardianRenderState) {
        return GUARDIAN_LOCATION;
    }

    @Override
    public GuardianRenderState createRenderState() {
        return new GuardianRenderState();
    }

    @Override
    public void extractRenderState(Guardian guardian, GuardianRenderState guardianRenderState, float f) {
        super.extractRenderState(guardian, guardianRenderState, f);
        guardianRenderState.spikesAnimation = guardian.getSpikesAnimation(f);
        guardianRenderState.tailAnimation = guardian.getTailAnimation(f);
        guardianRenderState.eyePosition = guardian.getEyePosition(f);
        Entity entity = GuardianRenderer.getEntityToLookAt(guardian);
        if (entity != null) {
            guardianRenderState.lookDirection = guardian.getViewVector(f);
            guardianRenderState.lookAtPosition = entity.getEyePosition(f);
        } else {
            guardianRenderState.lookDirection = null;
            guardianRenderState.lookAtPosition = null;
        }
        LivingEntity livingEntity = guardian.getActiveAttackTarget();
        if (livingEntity != null) {
            guardianRenderState.attackScale = guardian.getAttackAnimationScale(f);
            guardianRenderState.attackTime = guardian.getClientSideAttackTime() + f;
            guardianRenderState.attackTargetPosition = this.getPosition(livingEntity, (double)livingEntity.getBbHeight() * 0.5, f);
        } else {
            guardianRenderState.attackTargetPosition = null;
        }
    }

    private static @Nullable Entity getEntityToLookAt(Guardian guardian) {
        Entity entity = Minecraft.getInstance().getCameraEntity();
        if (guardian.hasActiveAttackTarget()) {
            return guardian.getActiveAttackTarget();
        }
        return entity;
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((GuardianRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

