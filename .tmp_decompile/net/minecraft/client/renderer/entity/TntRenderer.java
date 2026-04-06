/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class TntRenderer
extends EntityRenderer<PrimedTnt, TntRenderState> {
    public TntRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void submit(TntRenderState tntRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.5f, 0.0f);
        float f = tntRenderState.fuseRemainingInTicks;
        if (tntRenderState.fuseRemainingInTicks < 10.0f) {
            float g = 1.0f - tntRenderState.fuseRemainingInTicks / 10.0f;
            g = Mth.clamp(g, 0.0f, 1.0f);
            g *= g;
            g *= g;
            float h = 1.0f + g * 0.3f;
            poseStack.scale(h, h, h);
        }
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5f, -0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(90.0f));
        if (tntRenderState.blockState != null) {
            TntMinecartRenderer.submitWhiteSolidBlock(tntRenderState.blockState, poseStack, submitNodeCollector, tntRenderState.lightCoords, (int)f / 5 % 2 == 0, tntRenderState.outlineColor);
        }
        poseStack.popPose();
        super.submit(tntRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public TntRenderState createRenderState() {
        return new TntRenderState();
    }

    @Override
    public void extractRenderState(PrimedTnt primedTnt, TntRenderState tntRenderState, float f) {
        super.extractRenderState(primedTnt, tntRenderState, f);
        tntRenderState.fuseRemainingInTicks = (float)primedTnt.getFuse() - f + 1.0f;
        tntRenderState.blockState = primedTnt.getBlockState();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

