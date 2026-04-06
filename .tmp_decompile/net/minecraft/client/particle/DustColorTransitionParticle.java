/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class DustColorTransitionParticle
extends DustParticleBase<DustColorTransitionOptions> {
    private final Vector3f fromColor;
    private final Vector3f toColor;

    protected DustColorTransitionParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, DustColorTransitionOptions dustColorTransitionOptions, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, g, h, i, dustColorTransitionOptions, spriteSet);
        float j = this.random.nextFloat() * 0.4f + 0.6f;
        this.fromColor = this.randomizeColor(dustColorTransitionOptions.getFromColor(), j);
        this.toColor = this.randomizeColor(dustColorTransitionOptions.getToColor(), j);
    }

    private Vector3f randomizeColor(Vector3f vector3f, float f) {
        return new Vector3f(this.randomizeColor(vector3f.x(), f), this.randomizeColor(vector3f.y(), f), this.randomizeColor(vector3f.z(), f));
    }

    private void lerpColors(float f) {
        float g = ((float)this.age + f) / ((float)this.lifetime + 1.0f);
        Vector3f vector3f = new Vector3f((Vector3fc)this.fromColor).lerp((Vector3fc)this.toColor, g);
        this.rCol = vector3f.x();
        this.gCol = vector3f.y();
        this.bCol = vector3f.z();
    }

    @Override
    public void extract(QuadParticleRenderState quadParticleRenderState, Camera camera, float f) {
        this.lerpColors(f);
        super.extract(quadParticleRenderState, camera, f);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<DustColorTransitionOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(DustColorTransitionOptions dustColorTransitionOptions, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new DustColorTransitionParticle(clientLevel, d, e, f, g, h, i, dustColorTransitionOptions, this.sprites);
        }
    }
}

