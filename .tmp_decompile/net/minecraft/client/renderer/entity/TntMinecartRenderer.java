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
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractMinecartRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class TntMinecartRenderer
extends AbstractMinecartRenderer<MinecartTNT, MinecartTntRenderState> {
    public TntMinecartRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.TNT_MINECART);
    }

    @Override
    protected void submitMinecartContents(MinecartTntRenderState minecartTntRenderState, BlockState blockState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i) {
        float f = minecartTntRenderState.fuseRemainingInTicks;
        if (f > -1.0f && f < 10.0f) {
            float g = 1.0f - f / 10.0f;
            g = Mth.clamp(g, 0.0f, 1.0f);
            g *= g;
            g *= g;
            float h = 1.0f + g * 0.3f;
            poseStack.scale(h, h, h);
        }
        TntMinecartRenderer.submitWhiteSolidBlock(blockState, poseStack, submitNodeCollector, i, f > -1.0f && (int)f / 5 % 2 == 0, minecartTntRenderState.outlineColor);
    }

    public static void submitWhiteSolidBlock(BlockState blockState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, boolean bl, int j) {
        int k = bl ? OverlayTexture.pack(OverlayTexture.u(1.0f), 10) : OverlayTexture.NO_OVERLAY;
        submitNodeCollector.submitBlock(poseStack, blockState, i, k, j);
    }

    @Override
    public MinecartTntRenderState createRenderState() {
        return new MinecartTntRenderState();
    }

    @Override
    public void extractRenderState(MinecartTNT minecartTNT, MinecartTntRenderState minecartTntRenderState, float f) {
        super.extractRenderState(minecartTNT, minecartTntRenderState, f);
        minecartTntRenderState.fuseRemainingInTicks = minecartTNT.getFuse() > -1 ? (float)minecartTNT.getFuse() - f + 1.0f : -1.0f;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

