/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.breeze;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.BreezeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.BreezeRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

@Environment(value=EnvType.CLIENT)
public class BreezeModel
extends EntityModel<BreezeRenderState> {
    private static final float WIND_TOP_SPEED = 0.6f;
    private static final float WIND_MIDDLE_SPEED = 0.8f;
    private static final float WIND_BOTTOM_SPEED = 1.0f;
    private final ModelPart head;
    private final ModelPart eyes;
    private final ModelPart wind;
    private final ModelPart windTop;
    private final ModelPart windMid;
    private final ModelPart windBottom;
    private final ModelPart rods;
    private final KeyframeAnimation idleAnimation;
    private final KeyframeAnimation shootAnimation;
    private final KeyframeAnimation slideAnimation;
    private final KeyframeAnimation slideBackAnimation;
    private final KeyframeAnimation inhaleAnimation;
    private final KeyframeAnimation jumpAnimation;

    public BreezeModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entityTranslucent);
        this.wind = modelPart.getChild("wind_body");
        this.windBottom = this.wind.getChild("wind_bottom");
        this.windMid = this.windBottom.getChild("wind_mid");
        this.windTop = this.windMid.getChild("wind_top");
        this.head = modelPart.getChild("body").getChild("head");
        this.eyes = this.head.getChild("eyes");
        this.rods = modelPart.getChild("body").getChild("rods");
        this.idleAnimation = BreezeAnimation.IDLE.bake(modelPart);
        this.shootAnimation = BreezeAnimation.SHOOT.bake(modelPart);
        this.slideAnimation = BreezeAnimation.SLIDE.bake(modelPart);
        this.slideBackAnimation = BreezeAnimation.SLIDE_BACK.bake(modelPart);
        this.inhaleAnimation = BreezeAnimation.INHALE.bake(modelPart);
        this.jumpAnimation = BreezeAnimation.JUMP.bake(modelPart);
    }

    private static MeshDefinition createBaseMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("rods", CubeListBuilder.create(), PartPose.offset(0.0f, 8.0f, 0.0f));
        partDefinition3.addOrReplaceChild("rod_1", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(2.5981f, -3.0f, 1.5f, -2.7489f, -1.0472f, 3.1416f));
        partDefinition3.addOrReplaceChild("rod_2", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(-2.5981f, -3.0f, 1.5f, -2.7489f, 1.0472f, 3.1416f));
        partDefinition3.addOrReplaceChild("rod_3", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0f, 0.0f, -3.0f, 2.0f, 8.0f, 2.0f, new CubeDeformation(0.0f)), PartPose.offsetAndRotation(0.0f, -3.0f, -3.0f, 0.3927f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(4, 24).addBox(-5.0f, -5.0f, -4.2f, 10.0f, 3.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 4.0f, 0.0f));
        partDefinition4.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(4, 24).addBox(-5.0f, -5.0f, -4.2f, 10.0f, 3.0f, 4.0f, new CubeDeformation(0.0f)).texOffs(0, 0).addBox(-4.0f, -8.0f, -4.0f, 8.0f, 8.0f, 8.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition partDefinition5 = partDefinition.addOrReplaceChild("wind_body", CubeListBuilder.create(), PartPose.offset(0.0f, 0.0f, 0.0f));
        PartDefinition partDefinition6 = partDefinition5.addOrReplaceChild("wind_bottom", CubeListBuilder.create().texOffs(1, 83).addBox(-2.5f, -7.0f, -2.5f, 5.0f, 7.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition partDefinition7 = partDefinition6.addOrReplaceChild("wind_mid", CubeListBuilder.create().texOffs(74, 28).addBox(-6.0f, -6.0f, -6.0f, 12.0f, 6.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(78, 32).addBox(-4.0f, -6.0f, -4.0f, 8.0f, 6.0f, 8.0f, new CubeDeformation(0.0f)).texOffs(49, 71).addBox(-2.5f, -6.0f, -2.5f, 5.0f, 6.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -7.0f, 0.0f));
        partDefinition7.addOrReplaceChild("wind_top", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0f, -8.0f, -9.0f, 18.0f, 8.0f, 18.0f, new CubeDeformation(0.0f)).texOffs(6, 6).addBox(-6.0f, -8.0f, -6.0f, 12.0f, 8.0f, 12.0f, new CubeDeformation(0.0f)).texOffs(105, 57).addBox(-2.5f, -8.0f, -2.5f, 5.0f, 8.0f, 5.0f, new CubeDeformation(0.0f)), PartPose.offset(0.0f, -6.0f, 0.0f));
        return meshDefinition;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = BreezeModel.createBaseMesh();
        meshDefinition.getRoot().retainPartsAndChildren(Set.of((Object)"head", (Object)"rods"));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    public static LayerDefinition createWindLayer() {
        MeshDefinition meshDefinition = BreezeModel.createBaseMesh();
        meshDefinition.getRoot().retainPartsAndChildren(Set.of((Object)"wind_body"));
        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    public static LayerDefinition createEyesLayer() {
        MeshDefinition meshDefinition = BreezeModel.createBaseMesh();
        meshDefinition.getRoot().retainPartsAndChildren(Set.of((Object)"eyes"));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(BreezeRenderState breezeRenderState) {
        super.setupAnim(breezeRenderState);
        this.idleAnimation.apply(breezeRenderState.idle, breezeRenderState.ageInTicks);
        this.shootAnimation.apply(breezeRenderState.shoot, breezeRenderState.ageInTicks);
        this.slideAnimation.apply(breezeRenderState.slide, breezeRenderState.ageInTicks);
        this.slideBackAnimation.apply(breezeRenderState.slideBack, breezeRenderState.ageInTicks);
        this.inhaleAnimation.apply(breezeRenderState.inhale, breezeRenderState.ageInTicks);
        this.jumpAnimation.apply(breezeRenderState.longJump, breezeRenderState.ageInTicks);
    }

    public ModelPart head() {
        return this.head;
    }

    public ModelPart eyes() {
        return this.eyes;
    }

    public ModelPart rods() {
        return this.rods;
    }

    public ModelPart wind() {
        return this.wind;
    }
}

