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
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FishingHookRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class FishingHookRenderer
extends EntityRenderer<FishingHook, FishingHookRenderState> {
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutout(TEXTURE_LOCATION);
    private static final double VIEW_BOBBING_SCALE = 960.0;

    public FishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRender(FishingHook fishingHook, Frustum frustum, double d, double e, double f) {
        return super.shouldRender(fishingHook, frustum, d, e, f) && fishingHook.getPlayerOwner() != null;
    }

    @Override
    public void submit(FishingHookRenderState fishingHookRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.pushPose();
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
        submitNodeCollector.submitCustomGeometry(poseStack, RENDER_TYPE, (pose, vertexConsumer) -> {
            FishingHookRenderer.vertex(vertexConsumer, pose, fishingHookRenderState.lightCoords, 0.0f, 0, 0, 1);
            FishingHookRenderer.vertex(vertexConsumer, pose, fishingHookRenderState.lightCoords, 1.0f, 0, 1, 1);
            FishingHookRenderer.vertex(vertexConsumer, pose, fishingHookRenderState.lightCoords, 1.0f, 1, 1, 0);
            FishingHookRenderer.vertex(vertexConsumer, pose, fishingHookRenderState.lightCoords, 0.0f, 1, 0, 0);
        });
        poseStack.popPose();
        float f = (float)fishingHookRenderState.lineOriginOffset.x;
        float g = (float)fishingHookRenderState.lineOriginOffset.y;
        float h = (float)fishingHookRenderState.lineOriginOffset.z;
        float i = Minecraft.getInstance().getWindow().getAppropriateLineWidth();
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines(), (pose, vertexConsumer) -> {
            int j = 16;
            for (int k = 0; k < 16; ++k) {
                float l = FishingHookRenderer.fraction(k, 16);
                float m = FishingHookRenderer.fraction(k + 1, 16);
                FishingHookRenderer.stringVertex(f, g, h, vertexConsumer, pose, l, m, i);
                FishingHookRenderer.stringVertex(f, g, h, vertexConsumer, pose, m, l, i);
            }
        });
        poseStack.popPose();
        super.submit(fishingHookRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    public static HumanoidArm getHoldingArm(Player player) {
        return player.getMainHandItem().getItem() instanceof FishingRodItem ? player.getMainArm() : player.getMainArm().getOpposite();
    }

    private Vec3 getPlayerHandPos(Player player, float f, float g) {
        int i;
        int n = i = FishingHookRenderer.getHoldingArm(player) == HumanoidArm.RIGHT ? 1 : -1;
        if (!this.entityRenderDispatcher.options.getCameraType().isFirstPerson() || player != Minecraft.getInstance().player) {
            float h = Mth.lerp(g, player.yBodyRotO, player.yBodyRot) * ((float)Math.PI / 180);
            double d = Mth.sin(h);
            double e = Mth.cos(h);
            float j = player.getScale();
            double k = (double)i * 0.35 * (double)j;
            double l = 0.8 * (double)j;
            float m = player.isCrouching() ? -0.1875f : 0.0f;
            return player.getEyePosition(g).add(-e * k - d * l, (double)m - 0.45 * (double)j, -d * k + e * l);
        }
        double n2 = 960.0 / (double)this.entityRenderDispatcher.options.fov().get().intValue();
        Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float)i * 0.525f, -0.1f).scale(n2).yRot(f * 0.5f).xRot(-f * 0.7f);
        return player.getEyePosition(g).add(vec3);
    }

    private static float fraction(int i, int j) {
        return (float)i / (float)j;
    }

    private static void vertex(VertexConsumer vertexConsumer, PoseStack.Pose pose, int i, float f, int j, int k, int l) {
        vertexConsumer.addVertex(pose, f - 0.5f, (float)j - 0.5f, 0.0f).setColor(-1).setUv(k, l).setOverlay(OverlayTexture.NO_OVERLAY).setLight(i).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    private static void stringVertex(float f, float g, float h, VertexConsumer vertexConsumer, PoseStack.Pose pose, float i, float j, float k) {
        float l = f * i;
        float m = g * (i * i + i) * 0.5f + 0.25f;
        float n = h * i;
        float o = f * j - l;
        float p = g * (j * j + j) * 0.5f + 0.25f - m;
        float q = h * j - n;
        float r = Mth.sqrt(o * o + p * p + q * q);
        vertexConsumer.addVertex(pose, l, m, n).setColor(-16777216).setNormal(pose, o /= r, p /= r, q /= r).setLineWidth(k);
    }

    @Override
    public FishingHookRenderState createRenderState() {
        return new FishingHookRenderState();
    }

    @Override
    public void extractRenderState(FishingHook fishingHook, FishingHookRenderState fishingHookRenderState, float f) {
        super.extractRenderState(fishingHook, fishingHookRenderState, f);
        Player player = fishingHook.getPlayerOwner();
        if (player == null) {
            fishingHookRenderState.lineOriginOffset = Vec3.ZERO;
            return;
        }
        float g = player.getAttackAnim(f);
        float h = Mth.sin(Mth.sqrt(g) * (float)Math.PI);
        Vec3 vec3 = this.getPlayerHandPos(player, h, f);
        Vec3 vec32 = fishingHook.getPosition(f).add(0.0, 0.25, 0.0);
        fishingHookRenderState.lineOriginOffset = vec3.subtract(vec32);
    }

    @Override
    protected boolean affectedByCulling(FishingHook fishingHook) {
        return false;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ boolean affectedByCulling(Entity entity) {
        return this.affectedByCulling((FishingHook)entity);
    }
}

