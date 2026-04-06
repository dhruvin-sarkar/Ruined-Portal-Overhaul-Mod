/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.dragon.EnderDragonModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EnderDragonRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class EnderDragonRenderer
extends EntityRenderer<EnderDragon, EnderDragonRenderState> {
    public static final Identifier CRYSTAL_BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal_beam.png");
    private static final Identifier DRAGON_EXPLODING_LOCATION = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_exploding.png");
    private static final Identifier DRAGON_LOCATION = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png");
    private static final Identifier DRAGON_EYES_LOCATION = Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon_eyes.png");
    private static final RenderType RENDER_TYPE = RenderTypes.entityCutoutNoCull(DRAGON_LOCATION);
    private static final RenderType DECAL = RenderTypes.entityDecal(DRAGON_LOCATION);
    private static final RenderType EYES = RenderTypes.eyes(DRAGON_EYES_LOCATION);
    private static final RenderType BEAM = RenderTypes.entitySmoothCutout(CRYSTAL_BEAM_LOCATION);
    private static final float HALF_SQRT_3 = (float)(Math.sqrt(3.0) / 2.0);
    private final EnderDragonModel model;

    public EnderDragonRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
        this.model = new EnderDragonModel(context.bakeLayer(ModelLayers.ENDER_DRAGON));
    }

    @Override
    public void submit(EnderDragonRenderState enderDragonRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        float f = enderDragonRenderState.getHistoricalPos(7).yRot();
        float g = (float)(enderDragonRenderState.getHistoricalPos(5).y() - enderDragonRenderState.getHistoricalPos(10).y());
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(g * 10.0f));
        poseStack.translate(0.0f, 0.0f, 1.0f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        int i = OverlayTexture.pack(0.0f, enderDragonRenderState.hasRedOverlay);
        if (enderDragonRenderState.deathTime > 0.0f) {
            int j = ARGB.white(enderDragonRenderState.deathTime / 200.0f);
            submitNodeCollector.order(0).submitModel(this.model, enderDragonRenderState, poseStack, RenderTypes.dragonExplosionAlpha(DRAGON_EXPLODING_LOCATION), enderDragonRenderState.lightCoords, OverlayTexture.NO_OVERLAY, j, null, enderDragonRenderState.outlineColor, null);
            submitNodeCollector.order(1).submitModel(this.model, enderDragonRenderState, poseStack, DECAL, enderDragonRenderState.lightCoords, i, -1, null, enderDragonRenderState.outlineColor, null);
        } else {
            submitNodeCollector.order(0).submitModel(this.model, enderDragonRenderState, poseStack, RENDER_TYPE, enderDragonRenderState.lightCoords, i, -1, null, enderDragonRenderState.outlineColor, null);
        }
        submitNodeCollector.submitModel(this.model, enderDragonRenderState, poseStack, EYES, enderDragonRenderState.lightCoords, OverlayTexture.NO_OVERLAY, enderDragonRenderState.outlineColor, null);
        if (enderDragonRenderState.deathTime > 0.0f) {
            float h = enderDragonRenderState.deathTime / 200.0f;
            poseStack.pushPose();
            poseStack.translate(0.0f, -1.0f, -2.0f);
            EnderDragonRenderer.submitRays(poseStack, h, submitNodeCollector, RenderTypes.dragonRays());
            EnderDragonRenderer.submitRays(poseStack, h, submitNodeCollector, RenderTypes.dragonRaysDepth());
            poseStack.popPose();
        }
        poseStack.popPose();
        if (enderDragonRenderState.beamOffset != null) {
            EnderDragonRenderer.submitCrystalBeams((float)enderDragonRenderState.beamOffset.x, (float)enderDragonRenderState.beamOffset.y, (float)enderDragonRenderState.beamOffset.z, enderDragonRenderState.ageInTicks, poseStack, submitNodeCollector, enderDragonRenderState.lightCoords);
        }
        super.submit(enderDragonRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    private static void submitRays(PoseStack poseStack, float f, SubmitNodeCollector submitNodeCollector, RenderType renderType) {
        submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            float g = Math.min(f > 0.8f ? (f - 0.8f) / 0.2f : 0.0f, 1.0f);
            int i = ARGB.colorFromFloat(1.0f - g, 1.0f, 1.0f, 1.0f);
            int j = 0xFF00FF;
            RandomSource randomSource = RandomSource.create(432L);
            Vector3f vector3f = new Vector3f();
            Vector3f vector3f2 = new Vector3f();
            Vector3f vector3f3 = new Vector3f();
            Vector3f vector3f4 = new Vector3f();
            Quaternionf quaternionf = new Quaternionf();
            int k = Mth.floor((f + f * f) / 2.0f * 60.0f);
            for (int l = 0; l < k; ++l) {
                quaternionf.rotationXYZ(randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2)).rotateXYZ(randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2), randomSource.nextFloat() * ((float)Math.PI * 2) + f * 1.5707964f);
                pose.rotate((Quaternionfc)quaternionf);
                float h = randomSource.nextFloat() * 20.0f + 5.0f + g * 10.0f;
                float m = randomSource.nextFloat() * 2.0f + 1.0f + g * 2.0f;
                vector3f2.set(-HALF_SQRT_3 * m, h, -0.5f * m);
                vector3f3.set(HALF_SQRT_3 * m, h, -0.5f * m);
                vector3f4.set(0.0f, h, m);
                vertexConsumer.addVertex(pose, vector3f).setColor(i);
                vertexConsumer.addVertex(pose, vector3f2).setColor(0xFF00FF);
                vertexConsumer.addVertex(pose, vector3f3).setColor(0xFF00FF);
                vertexConsumer.addVertex(pose, vector3f).setColor(i);
                vertexConsumer.addVertex(pose, vector3f3).setColor(0xFF00FF);
                vertexConsumer.addVertex(pose, vector3f4).setColor(0xFF00FF);
                vertexConsumer.addVertex(pose, vector3f).setColor(i);
                vertexConsumer.addVertex(pose, vector3f4).setColor(0xFF00FF);
                vertexConsumer.addVertex(pose, vector3f2).setColor(0xFF00FF);
            }
        });
    }

    public static void submitCrystalBeams(float f, float g, float h, float i, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int j) {
        float k = Mth.sqrt(f * f + h * h);
        float l = Mth.sqrt(f * f + g * g + h * h);
        poseStack.pushPose();
        poseStack.translate(0.0f, 2.0f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotation((float)(-Math.atan2(h, f)) - 1.5707964f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation((float)(-Math.atan2(k, g)) - 1.5707964f));
        float m = 0.0f - i * 0.01f;
        float n = l / 32.0f - i * 0.01f;
        submitNodeCollector.submitCustomGeometry(poseStack, BEAM, (pose, vertexConsumer) -> {
            int j = 8;
            float k = 0.0f;
            float l = 0.75f;
            float m = 0.0f;
            for (int n = 1; n <= 8; ++n) {
                float o = Mth.sin((float)n * ((float)Math.PI * 2) / 8.0f) * 0.75f;
                float p = Mth.cos((float)n * ((float)Math.PI * 2) / 8.0f) * 0.75f;
                float q = (float)n / 8.0f;
                vertexConsumer.addVertex(pose, k * 0.2f, l * 0.2f, 0.0f).setColor(-16777216).setUv(m, m).setOverlay(OverlayTexture.NO_OVERLAY).setLight(j).setNormal(pose, 0.0f, -1.0f, 0.0f);
                vertexConsumer.addVertex(pose, k, l, l).setColor(-1).setUv(m, n).setOverlay(OverlayTexture.NO_OVERLAY).setLight(j).setNormal(pose, 0.0f, -1.0f, 0.0f);
                vertexConsumer.addVertex(pose, o, p, l).setColor(-1).setUv(q, n).setOverlay(OverlayTexture.NO_OVERLAY).setLight(j).setNormal(pose, 0.0f, -1.0f, 0.0f);
                vertexConsumer.addVertex(pose, o * 0.2f, p * 0.2f, 0.0f).setColor(-16777216).setUv(q, m).setOverlay(OverlayTexture.NO_OVERLAY).setLight(j).setNormal(pose, 0.0f, -1.0f, 0.0f);
                k = o;
                l = p;
                m = q;
            }
        });
        poseStack.popPose();
    }

    @Override
    public EnderDragonRenderState createRenderState() {
        return new EnderDragonRenderState();
    }

    @Override
    public void extractRenderState(EnderDragon enderDragon, EnderDragonRenderState enderDragonRenderState, float f) {
        super.extractRenderState(enderDragon, enderDragonRenderState, f);
        enderDragonRenderState.flapTime = Mth.lerp(f, enderDragon.oFlapTime, enderDragon.flapTime);
        enderDragonRenderState.deathTime = enderDragon.dragonDeathTime > 0 ? (float)enderDragon.dragonDeathTime + f : 0.0f;
        enderDragonRenderState.hasRedOverlay = enderDragon.hurtTime > 0;
        EndCrystal endCrystal = enderDragon.nearestCrystal;
        if (endCrystal != null) {
            Vec3 vec3 = endCrystal.getPosition(f).add(0.0, EndCrystalRenderer.getY((float)endCrystal.time + f), 0.0);
            enderDragonRenderState.beamOffset = vec3.subtract(enderDragon.getPosition(f));
        } else {
            enderDragonRenderState.beamOffset = null;
        }
        DragonPhaseInstance dragonPhaseInstance = enderDragon.getPhaseManager().getCurrentPhase();
        enderDragonRenderState.isLandingOrTakingOff = dragonPhaseInstance == EnderDragonPhase.LANDING || dragonPhaseInstance == EnderDragonPhase.TAKEOFF;
        enderDragonRenderState.isSitting = dragonPhaseInstance.isSitting();
        BlockPos blockPos = enderDragon.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(enderDragon.getFightOrigin()));
        enderDragonRenderState.distanceToEgg = blockPos.distToCenterSqr(enderDragon.position());
        enderDragonRenderState.partialTicks = enderDragon.isDeadOrDying() ? 0.0f : f;
        enderDragonRenderState.flightHistory.copyFrom(enderDragon.flightHistory);
    }

    @Override
    protected boolean affectedByCulling(EnderDragon enderDragon) {
        return false;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ boolean affectedByCulling(Entity entity) {
        return this.affectedByCulling((EnderDragon)entity);
    }
}

