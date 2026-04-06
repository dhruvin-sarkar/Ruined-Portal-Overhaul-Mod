/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.model.Model;
import net.minecraft.client.particle.ElderGuardianParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class ElderGuardianParticleGroup
extends ParticleGroup<ElderGuardianParticle> {
    public ElderGuardianParticleGroup(ParticleEngine particleEngine) {
        super(particleEngine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float f) {
        return new State(this.particles.stream().map(elderGuardianParticle -> ElderGuardianParticleRenderState.fromParticle(elderGuardianParticle, camera, f)).toList());
    }

    @Environment(value=EnvType.CLIENT)
    record State(List<ElderGuardianParticleRenderState> states) implements ParticleGroupRenderState
    {
        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
            for (ElderGuardianParticleRenderState elderGuardianParticleRenderState : this.states) {
                submitNodeCollector.submitModel(elderGuardianParticleRenderState.model, Unit.INSTANCE, elderGuardianParticleRenderState.poseStack, elderGuardianParticleRenderState.renderType, 0xF000F0, OverlayTexture.NO_OVERLAY, elderGuardianParticleRenderState.color, null, 0, null);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ElderGuardianParticleRenderState
    extends Record {
        final Model<Unit> model;
        final PoseStack poseStack;
        final RenderType renderType;
        final int color;

        private ElderGuardianParticleRenderState(Model<Unit> model, PoseStack poseStack, RenderType renderType, int i) {
            this.model = model;
            this.poseStack = poseStack;
            this.renderType = renderType;
            this.color = i;
        }

        public static ElderGuardianParticleRenderState fromParticle(ElderGuardianParticle elderGuardianParticle, Camera camera, float f) {
            float g = ((float)elderGuardianParticle.age + f) / (float)elderGuardianParticle.lifetime;
            float h = 0.05f + 0.5f * Mth.sin(g * (float)Math.PI);
            int i = ARGB.colorFromFloat(h, 1.0f, 1.0f, 1.0f);
            PoseStack poseStack = new PoseStack();
            poseStack.pushPose();
            poseStack.mulPose((Quaternionfc)camera.rotation());
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(60.0f - 150.0f * g));
            float j = 0.42553192f;
            poseStack.scale(0.42553192f, -0.42553192f, -0.42553192f);
            poseStack.translate(0.0f, -0.56f, 3.5f);
            return new ElderGuardianParticleRenderState(elderGuardianParticle.model, poseStack, elderGuardianParticle.renderType, i);
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ElderGuardianParticleRenderState.class, "model;poseStack;renderType;color", "model", "poseStack", "renderType", "color"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ElderGuardianParticleRenderState.class, "model;poseStack;renderType;color", "model", "poseStack", "renderType", "color"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ElderGuardianParticleRenderState.class, "model;poseStack;renderType;color", "model", "poseStack", "renderType", "color"}, this, object);
        }

        public Model<Unit> model() {
            return this.model;
        }

        public PoseStack poseStack() {
            return this.poseStack;
        }

        public RenderType renderType() {
            return this.renderType;
        }

        public int color() {
            return this.color;
        }
    }
}

