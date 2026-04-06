/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;

@Environment(value=EnvType.CLIENT)
public class NoRenderParticleGroup
extends ParticleGroup<NoRenderParticle> {
    private static final ParticleGroupRenderState EMPTY_RENDER_STATE = (submitNodeCollector, cameraRenderState) -> {};

    public NoRenderParticleGroup(ParticleEngine particleEngine) {
        super(particleEngine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float f) {
        return EMPTY_RENDER_STATE;
    }
}

