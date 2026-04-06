/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.fish.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SalmonRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Salmon;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class SalmonRenderer
extends MobRenderer<Salmon, SalmonRenderState, SalmonModel> {
    private static final Identifier SALMON_LOCATION = Identifier.withDefaultNamespace("textures/entity/fish/salmon.png");
    private final SalmonModel smallSalmonModel;
    private final SalmonModel mediumSalmonModel;
    private final SalmonModel largeSalmonModel;

    public SalmonRenderer(EntityRendererProvider.Context context) {
        super(context, new SalmonModel(context.bakeLayer(ModelLayers.SALMON)), 0.4f);
        this.smallSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON_SMALL));
        this.mediumSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON));
        this.largeSalmonModel = new SalmonModel(context.bakeLayer(ModelLayers.SALMON_LARGE));
    }

    @Override
    public void extractRenderState(Salmon salmon, SalmonRenderState salmonRenderState, float f) {
        super.extractRenderState(salmon, salmonRenderState, f);
        salmonRenderState.variant = salmon.getVariant();
    }

    @Override
    public Identifier getTextureLocation(SalmonRenderState salmonRenderState) {
        return SALMON_LOCATION;
    }

    @Override
    public SalmonRenderState createRenderState() {
        return new SalmonRenderState();
    }

    @Override
    protected void setupRotations(SalmonRenderState salmonRenderState, PoseStack poseStack, float f, float g) {
        super.setupRotations(salmonRenderState, poseStack, f, g);
        float h = 1.0f;
        float i = 1.0f;
        if (!salmonRenderState.isInWater) {
            h = 1.3f;
            i = 1.7f;
        }
        float j = h * 4.3f * Mth.sin(i * 0.6f * salmonRenderState.ageInTicks);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(j));
        if (!salmonRenderState.isInWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(90.0f));
        }
    }

    @Override
    public void submit(SalmonRenderState salmonRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.model = switch (salmonRenderState.variant) {
            default -> throw new MatchException(null, null);
            case Salmon.Variant.SMALL -> this.smallSalmonModel;
            case Salmon.Variant.MEDIUM -> this.mediumSalmonModel;
            case Salmon.Variant.LARGE -> this.largeSalmonModel;
        };
        super.submit(salmonRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SalmonRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

