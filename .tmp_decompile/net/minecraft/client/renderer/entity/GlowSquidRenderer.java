/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SquidRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.squid.GlowSquid;

@Environment(value=EnvType.CLIENT)
public class GlowSquidRenderer
extends SquidRenderer<GlowSquid> {
    private static final Identifier GLOW_SQUID_LOCATION = Identifier.withDefaultNamespace("textures/entity/squid/glow_squid.png");

    public GlowSquidRenderer(EntityRendererProvider.Context context, SquidModel squidModel, SquidModel squidModel2) {
        super(context, squidModel, squidModel2);
    }

    @Override
    public Identifier getTextureLocation(SquidRenderState squidRenderState) {
        return GLOW_SQUID_LOCATION;
    }

    @Override
    protected int getBlockLightLevel(GlowSquid glowSquid, BlockPos blockPos) {
        int i = (int)Mth.clampedLerp(1.0f - (float)glowSquid.getDarkTicksRemaining() / 10.0f, 0.0f, 15.0f);
        if (i == 15) {
            return 15;
        }
        return Math.max(i, super.getBlockLightLevel(glowSquid, blockPos));
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SquidRenderState)livingEntityRenderState);
    }
}

