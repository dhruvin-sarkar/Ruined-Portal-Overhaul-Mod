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
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class ReversePortalParticle
extends PortalParticle {
    ReversePortalParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, g, h, i, textureAtlasSprite);
        this.quadSize *= 1.5f;
        this.lifetime = (int)(this.random.nextFloat() * 2.0f) + 60;
    }

    @Override
    public float getQuadSize(float f) {
        float g = 1.0f - ((float)this.age + f) / ((float)this.lifetime * 1.5f);
        return this.quadSize * g;
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
        float f = (float)this.age / (float)this.lifetime;
        this.x += this.xd * (double)f;
        this.y += this.yd * (double)f;
        this.z += this.zd * (double)f;
    }

    @Environment(value=EnvType.CLIENT)
    public static class ReversePortalProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public ReversePortalProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            ReversePortalParticle reversePortalParticle = new ReversePortalParticle(clientLevel, d, e, f, g, h, i, this.sprite.get(randomSource));
            return reversePortalParticle;
        }
    }
}

