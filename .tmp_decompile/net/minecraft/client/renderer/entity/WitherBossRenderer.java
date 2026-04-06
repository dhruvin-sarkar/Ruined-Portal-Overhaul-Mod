/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.wither.WitherBossModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.WitherArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(value=EnvType.CLIENT)
public class WitherBossRenderer
extends MobRenderer<WitherBoss, WitherRenderState, WitherBossModel> {
    private static final Identifier WITHER_INVULNERABLE_LOCATION = Identifier.withDefaultNamespace("textures/entity/wither/wither_invulnerable.png");
    private static final Identifier WITHER_LOCATION = Identifier.withDefaultNamespace("textures/entity/wither/wither.png");

    public WitherBossRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherBossModel(context.bakeLayer(ModelLayers.WITHER)), 1.0f);
        this.addLayer(new WitherArmorLayer(this, context.getModelSet()));
    }

    @Override
    protected int getBlockLightLevel(WitherBoss witherBoss, BlockPos blockPos) {
        return 15;
    }

    @Override
    public Identifier getTextureLocation(WitherRenderState witherRenderState) {
        int i = Mth.floor(witherRenderState.invulnerableTicks);
        if (i <= 0 || i <= 80 && i / 5 % 2 == 1) {
            return WITHER_LOCATION;
        }
        return WITHER_INVULNERABLE_LOCATION;
    }

    @Override
    public WitherRenderState createRenderState() {
        return new WitherRenderState();
    }

    @Override
    protected void scale(WitherRenderState witherRenderState, PoseStack poseStack) {
        float f = 2.0f;
        if (witherRenderState.invulnerableTicks > 0.0f) {
            f -= witherRenderState.invulnerableTicks / 220.0f * 0.5f;
        }
        poseStack.scale(f, f, f);
    }

    @Override
    public void extractRenderState(WitherBoss witherBoss, WitherRenderState witherRenderState, float f) {
        super.extractRenderState(witherBoss, witherRenderState, f);
        int i = witherBoss.getInvulnerableTicks();
        witherRenderState.invulnerableTicks = i > 0 ? (float)i - f : 0.0f;
        System.arraycopy(witherBoss.getHeadXRots(), 0, witherRenderState.xHeadRots, 0, witherRenderState.xHeadRots.length);
        System.arraycopy(witherBoss.getHeadYRots(), 0, witherRenderState.yHeadRots, 0, witherRenderState.yHeadRots.length);
        witherRenderState.isPowered = witherBoss.isPowered();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((WitherRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

