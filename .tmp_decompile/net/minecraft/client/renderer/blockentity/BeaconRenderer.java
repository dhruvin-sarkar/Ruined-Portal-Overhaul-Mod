/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BeaconRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BeaconRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T, BeaconRenderState> {
    public static final Identifier BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 2048;
    private static final float BEAM_SCALE_THRESHOLD = 96.0f;
    public static final float SOLID_BEAM_RADIUS = 0.2f;
    public static final float BEAM_GLOW_RADIUS = 0.25f;

    @Override
    public BeaconRenderState createRenderState() {
        return new BeaconRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, BeaconRenderState beaconRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, beaconRenderState, f, vec3, crumblingOverlay);
        BeaconRenderer.extract(blockEntity, beaconRenderState, f, vec3);
    }

    public static <T extends BlockEntity> void extract(T blockEntity, BeaconRenderState beaconRenderState, float f, Vec3 vec3) {
        beaconRenderState.animationTime = blockEntity.getLevel() != null ? (float)Math.floorMod((long)blockEntity.getLevel().getGameTime(), (int)40) + f : 0.0f;
        beaconRenderState.sections = ((BeaconBeamOwner)((Object)blockEntity)).getBeamSections().stream().map(section -> new BeaconRenderState.Section(section.getColor(), section.getHeight())).toList();
        float g = (float)vec3.subtract(beaconRenderState.blockPos.getCenter()).horizontalDistance();
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        beaconRenderState.beamRadiusScale = localPlayer != null && localPlayer.isScoping() ? 1.0f : Math.max(1.0f, g / 96.0f);
    }

    @Override
    public void submit(BeaconRenderState beaconRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        int i = 0;
        for (int j = 0; j < beaconRenderState.sections.size(); ++j) {
            BeaconRenderState.Section section = beaconRenderState.sections.get(j);
            BeaconRenderer.submitBeaconBeam(poseStack, submitNodeCollector, beaconRenderState.beamRadiusScale, beaconRenderState.animationTime, i, j == beaconRenderState.sections.size() - 1 ? 2048 : section.height(), section.color());
            i += section.height();
        }
    }

    private static void submitBeaconBeam(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, float f, float g, int i, int j, int k) {
        BeaconRenderer.submitBeaconBeam(poseStack, submitNodeCollector, BEAM_LOCATION, 1.0f, g, i, j, k, 0.2f * f, 0.25f * f);
    }

    public static void submitBeaconBeam(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Identifier identifier, float f, float g, int i, int j, int k, float h, float l) {
        int m = i + j;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        float n = j < 0 ? g : -g;
        float o = Mth.frac(n * 0.2f - (float)Mth.floor(n * 0.1f));
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(g * 2.25f - 45.0f));
        float p = 0.0f;
        float q = h;
        float r = h;
        float s = 0.0f;
        float t = -h;
        float u = 0.0f;
        float v = 0.0f;
        float w = -h;
        float x = 0.0f;
        float y = 1.0f;
        float z = -1.0f + o;
        float aa = (float)j * f * (0.5f / h) + z;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.beaconBeam(identifier, false), (pose, vertexConsumer) -> BeaconRenderer.renderPart(pose, vertexConsumer, k, i, m, 0.0f, q, r, 0.0f, t, 0.0f, 0.0f, w, 0.0f, 1.0f, aa, z));
        poseStack.popPose();
        p = -l;
        q = -l;
        r = l;
        s = -l;
        t = -l;
        u = l;
        v = l;
        w = l;
        x = 0.0f;
        y = 1.0f;
        z = -1.0f + o;
        aa = (float)j * f + z;
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.beaconBeam(identifier, true), (pose, vertexConsumer) -> BeaconRenderer.renderPart(pose, vertexConsumer, ARGB.color(32, k), i, m, p, q, r, s, t, u, v, w, 0.0f, 1.0f, aa, z));
        poseStack.popPose();
    }

    private static void renderPart(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, int k, float f, float g, float h, float l, float m, float n, float o, float p, float q, float r, float s, float t) {
        BeaconRenderer.renderQuad(pose, vertexConsumer, i, j, k, f, g, h, l, q, r, s, t);
        BeaconRenderer.renderQuad(pose, vertexConsumer, i, j, k, o, p, m, n, q, r, s, t);
        BeaconRenderer.renderQuad(pose, vertexConsumer, i, j, k, h, l, o, p, q, r, s, t);
        BeaconRenderer.renderQuad(pose, vertexConsumer, i, j, k, m, n, f, g, q, r, s, t);
    }

    private static void renderQuad(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, int k, float f, float g, float h, float l, float m, float n, float o, float p) {
        BeaconRenderer.addVertex(pose, vertexConsumer, i, k, f, g, n, o);
        BeaconRenderer.addVertex(pose, vertexConsumer, i, j, f, g, n, p);
        BeaconRenderer.addVertex(pose, vertexConsumer, i, j, h, l, m, p);
        BeaconRenderer.addVertex(pose, vertexConsumer, i, k, h, l, m, o);
    }

    private static void addVertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h, float k) {
        vertexConsumer.addVertex(pose, f, (float)j, g).setColor(i).setUv(h, k).setOverlay(OverlayTexture.NO_OVERLAY).setLight(0xF000F0).setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
    }

    @Override
    public boolean shouldRender(T blockEntity, Vec3 vec3) {
        return Vec3.atCenterOf(((BlockEntity)blockEntity).getBlockPos()).multiply(1.0, 0.0, 1.0).closerThan(vec3.multiply(1.0, 0.0, 1.0), this.getViewDistance());
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

