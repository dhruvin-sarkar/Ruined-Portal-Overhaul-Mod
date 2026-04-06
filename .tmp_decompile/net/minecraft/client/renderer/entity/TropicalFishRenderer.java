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
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.fish.TropicalFishLargeModel;
import net.minecraft.client.model.animal.fish.TropicalFishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.TropicalFishPatternLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.TropicalFishRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class TropicalFishRenderer
extends MobRenderer<TropicalFish, TropicalFishRenderState, EntityModel<TropicalFishRenderState>> {
    private final EntityModel<TropicalFishRenderState> smallModel = this.getModel();
    private final EntityModel<TropicalFishRenderState> largeModel;
    private static final Identifier SMALL_TEXTURE = Identifier.withDefaultNamespace("textures/entity/fish/tropical_a.png");
    private static final Identifier LARGE_TEXTURE = Identifier.withDefaultNamespace("textures/entity/fish/tropical_b.png");

    public TropicalFishRenderer(EntityRendererProvider.Context context) {
        super(context, new TropicalFishSmallModel(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL)), 0.15f);
        this.largeModel = new TropicalFishLargeModel(context.bakeLayer(ModelLayers.TROPICAL_FISH_LARGE));
        this.addLayer(new TropicalFishPatternLayer(this, context.getModelSet()));
    }

    @Override
    public Identifier getTextureLocation(TropicalFishRenderState tropicalFishRenderState) {
        return switch (tropicalFishRenderState.pattern.base()) {
            default -> throw new MatchException(null, null);
            case TropicalFish.Base.SMALL -> SMALL_TEXTURE;
            case TropicalFish.Base.LARGE -> LARGE_TEXTURE;
        };
    }

    @Override
    public TropicalFishRenderState createRenderState() {
        return new TropicalFishRenderState();
    }

    @Override
    public void extractRenderState(TropicalFish tropicalFish, TropicalFishRenderState tropicalFishRenderState, float f) {
        super.extractRenderState(tropicalFish, tropicalFishRenderState, f);
        tropicalFishRenderState.pattern = tropicalFish.getPattern();
        tropicalFishRenderState.baseColor = tropicalFish.getBaseColor().getTextureDiffuseColor();
        tropicalFishRenderState.patternColor = tropicalFish.getPatternColor().getTextureDiffuseColor();
    }

    @Override
    public void submit(TropicalFishRenderState tropicalFishRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.model = switch (tropicalFishRenderState.pattern.base()) {
            default -> throw new MatchException(null, null);
            case TropicalFish.Base.SMALL -> this.smallModel;
            case TropicalFish.Base.LARGE -> this.largeModel;
        };
        super.submit(tropicalFishRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    protected int getModelTint(TropicalFishRenderState tropicalFishRenderState) {
        return tropicalFishRenderState.baseColor;
    }

    @Override
    protected void setupRotations(TropicalFishRenderState tropicalFishRenderState, PoseStack poseStack, float f, float g) {
        super.setupRotations(tropicalFishRenderState, poseStack, f, g);
        float h = 4.3f * Mth.sin(0.6f * tropicalFishRenderState.ageInTicks);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(h));
        if (!tropicalFishRenderState.isInWater) {
            poseStack.translate(0.2f, 0.1f, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(90.0f));
        }
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((TropicalFishRenderState)livingEntityRenderState);
    }

    @Override
    protected /* synthetic */ int getModelTint(LivingEntityRenderState livingEntityRenderState) {
        return this.getModelTint((TropicalFishRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

