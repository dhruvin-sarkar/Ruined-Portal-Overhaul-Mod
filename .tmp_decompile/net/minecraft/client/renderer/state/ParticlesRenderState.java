/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.state;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;

@Environment(value=EnvType.CLIENT)
public class ParticlesRenderState {
    public final List<ParticleGroupRenderState> particles = new ArrayList<ParticleGroupRenderState>();

    public void reset() {
        this.particles.forEach(ParticleGroupRenderState::clear);
        this.particles.clear();
    }

    public void add(ParticleGroupRenderState particleGroupRenderState) {
        this.particles.add(particleGroupRenderState);
    }

    public void submit(SubmitNodeStorage submitNodeStorage, CameraRenderState cameraRenderState) {
        for (ParticleGroupRenderState particleGroupRenderState : this.particles) {
            particleGroupRenderState.submit(submitNodeStorage, cameraRenderState);
        }
    }
}

