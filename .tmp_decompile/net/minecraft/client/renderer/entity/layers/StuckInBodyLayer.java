/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public abstract class StuckInBodyLayer<M extends PlayerModel, S>
extends RenderLayer<AvatarRenderState, M> {
    private final Model<S> model;
    private final S modelState;
    private final Identifier texture;
    private final PlacementStyle placementStyle;

    public StuckInBodyLayer(LivingEntityRenderer<?, AvatarRenderState, M> livingEntityRenderer, Model<S> model, S object, Identifier identifier, PlacementStyle placementStyle) {
        super(livingEntityRenderer);
        this.model = model;
        this.modelState = object;
        this.texture = identifier;
        this.placementStyle = placementStyle;
    }

    protected abstract int numStuck(AvatarRenderState var1);

    private void submitStuckItem(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, float f, float g, float h, int j) {
        float k = Mth.sqrt(f * f + h * h);
        float l = (float)(Math.atan2(f, h) * 57.2957763671875);
        float m = (float)(Math.atan2(g, k) * 57.2957763671875);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(l - 90.0f));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(m));
        submitNodeCollector.submitModel(this.model, this.modelState, poseStack, this.model.renderType(this.texture), i, OverlayTexture.NO_OVERLAY, j, null);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState avatarRenderState, float f, float g) {
        int j = this.numStuck(avatarRenderState);
        if (j <= 0) {
            return;
        }
        RandomSource randomSource = RandomSource.create(avatarRenderState.id);
        for (int k = 0; k < j; ++k) {
            poseStack.pushPose();
            ModelPart modelPart = ((PlayerModel)this.getParentModel()).getRandomBodyPart(randomSource);
            ModelPart.Cube cube = modelPart.getRandomCube(randomSource);
            modelPart.translateAndRotate(poseStack);
            float h = randomSource.nextFloat();
            float l = randomSource.nextFloat();
            float m = randomSource.nextFloat();
            if (this.placementStyle == PlacementStyle.ON_SURFACE) {
                int n = randomSource.nextInt(3);
                switch (n) {
                    case 0: {
                        h = StuckInBodyLayer.snapToFace(h);
                        break;
                    }
                    case 1: {
                        l = StuckInBodyLayer.snapToFace(l);
                        break;
                    }
                    default: {
                        m = StuckInBodyLayer.snapToFace(m);
                    }
                }
            }
            poseStack.translate(Mth.lerp(h, cube.minX, cube.maxX) / 16.0f, Mth.lerp(l, cube.minY, cube.maxY) / 16.0f, Mth.lerp(m, cube.minZ, cube.maxZ) / 16.0f);
            this.submitStuckItem(poseStack, submitNodeCollector, i, -(h * 2.0f - 1.0f), -(l * 2.0f - 1.0f), -(m * 2.0f - 1.0f), avatarRenderState.outlineColor);
            poseStack.popPose();
        }
    }

    private static float snapToFace(float f) {
        return f > 0.5f ? 1.0f : 0.5f;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum PlacementStyle {
        IN_CUBE,
        ON_SURFACE;

    }
}

