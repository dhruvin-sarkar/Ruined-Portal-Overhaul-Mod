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
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.data.AtlasIds;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value=EnvType.CLIENT)
public class BreakingItemParticle
extends SingleQuadParticle {
    private final float uo;
    private final float vo;
    private final SingleQuadParticle.Layer layer;

    BreakingItemParticle(ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, TextureAtlasSprite textureAtlasSprite) {
        this(clientLevel, d, e, f, textureAtlasSprite);
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        this.xd += g;
        this.yd += h;
        this.zd += i;
    }

    protected BreakingItemParticle(ClientLevel clientLevel, double d, double e, double f, TextureAtlasSprite textureAtlasSprite) {
        super(clientLevel, d, e, f, 0.0, 0.0, 0.0, textureAtlasSprite);
        this.gravity = 1.0f;
        this.quadSize /= 2.0f;
        this.uo = this.random.nextFloat() * 3.0f;
        this.vo = this.random.nextFloat() * 3.0f;
        this.layer = textureAtlasSprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS) ? SingleQuadParticle.Layer.TERRAIN : SingleQuadParticle.Layer.ITEMS;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU((this.uo + 1.0f) / 4.0f);
    }

    @Override
    protected float getU1() {
        return this.sprite.getU(this.uo / 4.0f);
    }

    @Override
    protected float getV0() {
        return this.sprite.getV(this.vo / 4.0f);
    }

    @Override
    protected float getV1() {
        return this.sprite.getV((this.vo + 1.0f) / 4.0f);
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return this.layer;
    }

    @Environment(value=EnvType.CLIENT)
    public static class SnowballProvider
    extends ItemParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new BreakingItemParticle(clientLevel, d, e, f, this.getSprite(new ItemStack(Items.SNOWBALL), clientLevel, randomSource));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class CobwebProvider
    extends ItemParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new BreakingItemParticle(clientLevel, d, e, f, this.getSprite(new ItemStack(Items.COBWEB), clientLevel, randomSource));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class SlimeProvider
    extends ItemParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new BreakingItemParticle(clientLevel, d, e, f, this.getSprite(new ItemStack(Items.SLIME_BALL), clientLevel, randomSource));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Provider
    extends ItemParticleProvider<ItemParticleOption> {
        @Override
        public Particle createParticle(ItemParticleOption itemParticleOption, ClientLevel clientLevel, double d, double e, double f, double g, double h, double i, RandomSource randomSource) {
            return new BreakingItemParticle(clientLevel, d, e, f, g, h, i, this.getSprite(itemParticleOption.getItem(), clientLevel, randomSource));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static abstract class ItemParticleProvider<T extends ParticleOptions>
    implements ParticleProvider<T> {
        private final ItemStackRenderState scratchRenderState = new ItemStackRenderState();

        protected TextureAtlasSprite getSprite(ItemStack itemStack, ClientLevel clientLevel, RandomSource randomSource) {
            Minecraft.getInstance().getItemModelResolver().updateForTopItem(this.scratchRenderState, itemStack, ItemDisplayContext.GROUND, clientLevel, null, 0);
            TextureAtlasSprite textureAtlasSprite = this.scratchRenderState.pickParticleIcon(randomSource);
            return textureAtlasSprite != null ? textureAtlasSprite : Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.ITEMS).missingSprite();
        }
    }
}

