/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ItemPickupParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class ItemPickupParticleGroup
extends ParticleGroup<ItemPickupParticle> {
    public ItemPickupParticleGroup(ParticleEngine particleEngine) {
        super(particleEngine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float f) {
        return new State(this.particles.stream().map(itemPickupParticle -> ParticleInstance.fromParticle(itemPickupParticle, camera, f)).toList());
    }

    @Environment(value=EnvType.CLIENT)
    record State(List<ParticleInstance> instances) implements ParticleGroupRenderState
    {
        @Override
        public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
            PoseStack poseStack = new PoseStack();
            EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            for (ParticleInstance particleInstance : this.instances) {
                entityRenderDispatcher.submit(particleInstance.itemRenderState, cameraRenderState, particleInstance.xOffset, particleInstance.yOffset, particleInstance.zOffset, poseStack, submitNodeCollector);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class ParticleInstance
    extends Record {
        final EntityRenderState itemRenderState;
        final double xOffset;
        final double yOffset;
        final double zOffset;

        private ParticleInstance(EntityRenderState entityRenderState, double d, double e, double f) {
            this.itemRenderState = entityRenderState;
            this.xOffset = d;
            this.yOffset = e;
            this.zOffset = f;
        }

        public static ParticleInstance fromParticle(ItemPickupParticle itemPickupParticle, Camera camera, float f) {
            float g = ((float)itemPickupParticle.life + f) / 3.0f;
            g *= g;
            double d = Mth.lerp((double)f, itemPickupParticle.targetXOld, itemPickupParticle.targetX);
            double e = Mth.lerp((double)f, itemPickupParticle.targetYOld, itemPickupParticle.targetY);
            double h = Mth.lerp((double)f, itemPickupParticle.targetZOld, itemPickupParticle.targetZ);
            double i = Mth.lerp((double)g, itemPickupParticle.itemRenderState.x, d);
            double j = Mth.lerp((double)g, itemPickupParticle.itemRenderState.y, e);
            double k = Mth.lerp((double)g, itemPickupParticle.itemRenderState.z, h);
            Vec3 vec3 = camera.position();
            return new ParticleInstance(itemPickupParticle.itemRenderState, i - vec3.x(), j - vec3.y(), k - vec3.z());
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ParticleInstance.class, "itemRenderState;xOffset;yOffset;zOffset", "itemRenderState", "xOffset", "yOffset", "zOffset"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ParticleInstance.class, "itemRenderState;xOffset;yOffset;zOffset", "itemRenderState", "xOffset", "yOffset", "zOffset"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ParticleInstance.class, "itemRenderState;xOffset;yOffset;zOffset", "itemRenderState", "xOffset", "yOffset", "zOffset"}, this, object);
        }

        public EntityRenderState itemRenderState() {
            return this.itemRenderState;
        }

        public double xOffset() {
            return this.xOffset;
        }

        public double yOffset() {
            return this.yOffset;
        }

        public double zOffset() {
            return this.zOffset;
        }
    }
}

