/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.sniffer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.SnifferAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.SnifferRenderState;

@Environment(value=EnvType.CLIENT)
public class SnifferModel
extends EntityModel<SnifferRenderState> {
    public static final MeshTransformer BABY_TRANSFORMER = MeshTransformer.scaling(0.5f);
    private static final float WALK_ANIMATION_SPEED_MAX = 9.0f;
    private static final float WALK_ANIMATION_SCALE_FACTOR = 100.0f;
    private final ModelPart head;
    private final KeyframeAnimation sniffSearchAnimation;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation digAnimation;
    private final KeyframeAnimation longSniffAnimation;
    private final KeyframeAnimation standUpAnimation;
    private final KeyframeAnimation happyAnimation;
    private final KeyframeAnimation sniffSniffAnimation;
    private final KeyframeAnimation babyTransform;

    public SnifferModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("bone").getChild("body").getChild("head");
        this.sniffSearchAnimation = SnifferAnimation.SNIFFER_SNIFF_SEARCH.bake(modelPart);
        this.walkAnimation = SnifferAnimation.SNIFFER_WALK.bake(modelPart);
        this.digAnimation = SnifferAnimation.SNIFFER_DIG.bake(modelPart);
        this.longSniffAnimation = SnifferAnimation.SNIFFER_LONGSNIFF.bake(modelPart);
        this.standUpAnimation = SnifferAnimation.SNIFFER_STAND_UP.bake(modelPart);
        this.happyAnimation = SnifferAnimation.SNIFFER_HAPPY.bake(modelPart);
        this.sniffSniffAnimation = SnifferAnimation.SNIFFER_SNIFFSNIFF.bake(modelPart);
        this.babyTransform = SnifferAnimation.BABY_TRANSFORM.bake(modelPart);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0f, 5.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("body", CubeListBuilder.create().texOffs(62, 68).addBox(-12.5f, -14.0f, -20.0f, 25.0f, 29.0f, 40.0f, new CubeDeformation(0.0f)).texOffs(62, 0).addBox(-12.5f, -14.0f, -20.0f, 25.0f, 24.0f, 40.0f, new CubeDeformation(0.5f)).texOffs(87, 68).addBox(-12.5f, 12.0f, -20.0f, 25.0f, 0.0f, 40.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        partDefinition2.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(32, 87).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-7.5f, 10.0f, -15.0f));
        partDefinition2.addOrReplaceChild("right_mid_leg", CubeListBuilder.create().texOffs(32, 105).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-7.5f, 10.0f, 0.0f));
        partDefinition2.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(32, 123).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(-7.5f, 10.0f, 15.0f));
        partDefinition2.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 87).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(7.5f, 10.0f, -15.0f));
        partDefinition2.addOrReplaceChild("left_mid_leg", CubeListBuilder.create().texOffs(0, 105).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(7.5f, 10.0f, 0.0f));
        partDefinition2.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 123).addBox(-3.5f, -1.0f, -4.0f, 7.0f, 10.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(7.5f, 10.0f, 15.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(8, 15).addBox(-6.5f, -7.5f, -11.5f, 13.0f, 18.0f, 11.0f, new CubeDeformation(0.0f)).texOffs(8, 4).addBox(-6.5f, 7.5f, -11.5f, 13.0f, 0.0f, 11.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 6.5f, -19.48f));
        partDefinition4.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(2, 0).addBox(0.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset(6.51f, -7.5f, -4.51f));
        partDefinition4.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0f, 0.0f, -3.0f, 1.0f, 19.0f, 7.0f, new CubeDeformation(0.0f)), PartPose.offset(-6.51f, -7.5f, -4.51f));
        partDefinition4.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(10, 45).addBox(-6.5f, -2.0f, -9.0f, 13.0f, 2.0f, 9.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -4.5f, -11.5f));
        partDefinition4.addOrReplaceChild("lower_beak", CubeListBuilder.create().texOffs(10, 57).addBox(-6.5f, -7.0f, -8.0f, 13.0f, 12.0f, 9.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 2.5f, -12.5f));
        return LayerDefinition.create(meshDefinition, 192, 192);
    }

    @Override
    public void setupAnim(SnifferRenderState snifferRenderState) {
        super.setupAnim(snifferRenderState);
        this.head.xRot = snifferRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = snifferRenderState.yRot * ((float)Math.PI / 180);
        if (snifferRenderState.isSearching) {
            this.sniffSearchAnimation.applyWalk(snifferRenderState.walkAnimationPos, snifferRenderState.walkAnimationSpeed, 9.0f, 100.0f);
        } else {
            this.walkAnimation.applyWalk(snifferRenderState.walkAnimationPos, snifferRenderState.walkAnimationSpeed, 9.0f, 100.0f);
        }
        this.digAnimation.apply(snifferRenderState.diggingAnimationState, snifferRenderState.ageInTicks);
        this.longSniffAnimation.apply(snifferRenderState.sniffingAnimationState, snifferRenderState.ageInTicks);
        this.standUpAnimation.apply(snifferRenderState.risingAnimationState, snifferRenderState.ageInTicks);
        this.happyAnimation.apply(snifferRenderState.feelingHappyAnimationState, snifferRenderState.ageInTicks);
        this.sniffSniffAnimation.apply(snifferRenderState.scentingAnimationState, snifferRenderState.ageInTicks);
        if (snifferRenderState.isBaby) {
            this.babyTransform.applyStatic();
        }
    }
}

