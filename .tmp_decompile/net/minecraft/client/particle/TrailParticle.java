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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class TrailParticle
extends SingleQuadParticle {
    private final Vec3 target;

    TrailParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, Vec3 vec3, int j, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, g, h, i, textureAtlasSprite);
        j = ARGB.scaleRGB(j, 0.875f + this.random.nextFloat() * 0.25f, 0.875f + this.random.nextFloat() * 0.25f, 0.875f + this.random.nextFloat() * 0.25f);
        this.rCol = (float)ARGB.red(j) / 255.0f;
        this.gCol = (float)ARGB.green(j) / 255.0f;
        this.bCol = (float)ARGB.blue(j) / 255.0f;
        this.quadSize = 0.26f;
        this.target = vec3;
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        int i = this.lifetime - this.age;
        double d = 1.0 / (double)i;
        this.x = Mth.lerp(d, this.x, this.target.x());
        this.y = Mth.lerp(d, this.y, this.target.y());
        this.z = Mth.lerp(d, this.z, this.target.z());
    }

    @Override
    public int getLightColor(float f) {
        return 0xF000F0;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<TrailParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(TrailParticleOption trailParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            TrailParticle trailParticle = new TrailParticle(clientLevel, d, e, f, g, h, i, trailParticleOption.target(), trailParticleOption.color(), this.sprite.get(randomSource));
            trailParticle.setLifetime(trailParticleOption.duration());
            return trailParticle;
        }
    }
}

