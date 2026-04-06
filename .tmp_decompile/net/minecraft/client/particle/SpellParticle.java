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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class SpellParticle
extends SingleQuadParticle {
    private static final RandomSource RANDOM = RandomSource.create();
    private final SpriteSet sprites;
    private float originalAlpha = 1.0f;

    SpellParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, SpriteSet spriteSet) {
        super(clientLevel, d, e, f, 0.5 - RANDOM.nextDouble(), h, 0.5 - RANDOM.nextDouble(), spriteSet.first());
        this.friction = 0.96f;
        this.gravity = -0.1f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = spriteSet;
        this.yd *= (double)0.2f;
        if (g == 0.0 && i == 0.0) {
            this.xd *= (double)0.1f;
            this.zd *= (double)0.1f;
        }
        this.quadSize *= 0.75f;
        this.lifetime = (int)(8.0 / ((double)this.random.nextFloat() * 0.8 + 0.2));
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
        if (this.isCloseToScopingPlayer()) {
            this.setAlpha(0.0f);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
        this.alpha = this.isCloseToScopingPlayer() ? 0.0f : Mth.lerp(0.05f, this.alpha, this.originalAlpha);
    }

    @Override
    protected void setAlpha(float f) {
        super.setAlpha(f);
        this.originalAlpha = f;
    }

    private boolean isCloseToScopingPlayer() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        return localPlayer != null && localPlayer.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0 && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping();
    }

    @Environment(value=EnvType.CLIENT)
    public static class InstantProvider
    implements ParticleProvider<SpellParticleOption> {
        private final SpriteSet sprite;

        public InstantProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SpellParticleOption spellParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            spellParticle.setColor(spellParticleOption.getRed(), spellParticleOption.getGreen(), spellParticleOption.getBlue());
            spellParticle.setPower(spellParticleOption.getPower());
            return spellParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class WitchProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public WitchProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            float j = randomSource.nextFloat() * 0.5f + 0.35f;
            spellParticle.setColor(1.0f * j, 0.0f * j, 1.0f * j);
            return spellParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class MobEffectProvider
    implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprite;

        public MobEffectProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(ColorParticleOption colorParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            SpellParticle spellParticle = new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
            spellParticle.setColor(colorParticleOption.getRed(), colorParticleOption.getGreen(), colorParticleOption.getBlue());
            spellParticle.setAlpha(colorParticleOption.getAlpha());
            return spellParticle;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new SpellParticle(clientLevel, d, e, f, g, h, i, this.sprite);
        }
    }
}

