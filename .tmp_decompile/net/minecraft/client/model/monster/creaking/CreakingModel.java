/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.creaking;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.animation.definitions.CreakingAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;

@Environment(value=EnvType.CLIENT)
public class CreakingModel
extends EntityModel<CreakingRenderState> {
    private final ModelPart head;
    private final KeyframeAnimation walkAnimation;
    private final KeyframeAnimation attackAnimation;
    private final KeyframeAnimation invulnerableAnimation;
    private final KeyframeAnimation deathAnimation;

    public CreakingModel(ModelPart modelPart) {
        super(modelPart);
        ModelPart modelPart2 = modelPart.getChild("root");
        ModelPart modelPart3 = modelPart2.getChild("upper_body");
        this.head = modelPart3.getChild("head");
        this.walkAnimation = CreakingAnimation.CREAKING_WALK.bake(modelPart2);
        this.attackAnimation = CreakingAnimation.CREAKING_ATTACK.bake(modelPart2);
        this.invulnerableAnimation = CreakingAnimation.CREAKING_INVULNERABLE.bake(modelPart2);
        this.deathAnimation = CreakingAnimation.CREAKING_DEATH.bake(modelPart2);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild("upper_body", CubeListBuilder.create(), PartPose.offset(-1.0f, -19.0f, 0.0f));
        partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -10.0f, -3.0f, 6.0f, 10.0f, 6.0f).texOffs(28, 31).addBox(-3.0f, -13.0f, -3.0f, 6.0f, 3.0f, 6.0f).texOffs(12, 40).addBox(3.0f, -13.0f, 0.0f, 9.0f, 14.0f, 0.0f).texOffs(34, 12).addBox(-12.0f, -14.0f, 0.0f, 9.0f, 14.0f, 0.0f), PartPose.offset(-3.0f, -11.0f, 0.0f));
        partDefinition3.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(0.0f, -3.0f, -3.0f, 6.0f, 13.0f, 5.0f).texOffs(24, 0).addBox(-6.0f, -4.0f, -3.0f, 6.0f, 7.0f, 5.0f), PartPose.offset(0.0f, -7.0f, 1.0f));
        partDefinition3.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(22, 13).addBox(-2.0f, -1.5f, -1.5f, 3.0f, 21.0f, 3.0f).texOffs(46, 0).addBox(-2.0f, 19.5f, -1.5f, 3.0f, 4.0f, 3.0f), PartPose.offset(-7.0f, -9.5f, 1.5f));
        partDefinition3.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(30, 40).addBox(0.0f, -1.0f, -1.5f, 3.0f, 16.0f, 3.0f).texOffs(52, 12).addBox(0.0f, -5.0f, -1.5f, 3.0f, 4.0f, 3.0f).texOffs(52, 19).addBox(0.0f, 15.0f, -1.5f, 3.0f, 4.0f, 3.0f), PartPose.offset(6.0f, -9.0f, 0.5f));
        partDefinition2.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(42, 40).addBox(-1.5f, 0.0f, -1.5f, 3.0f, 16.0f, 3.0f).texOffs(45, 55).addBox(-1.5f, 15.7f, -4.5f, 5.0f, 0.0f, 9.0f), PartPose.offset(1.5f, -16.0f, 0.5f));
        partDefinition2.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0f, -1.5f, -1.5f, 3.0f, 19.0f, 3.0f).texOffs(45, 46).addBox(-5.0f, 17.2f, -4.5f, 5.0f, 0.0f, 9.0f).texOffs(12, 34).addBox(-3.0f, -4.5f, -1.5f, 3.0f, 3.0f, 3.0f), PartPose.offset(-1.0f, -17.5f, 0.5f));
        return meshDefinition;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = CreakingModel.createMesh();
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createEyesLayer() {
        MeshDefinition meshDefinition = CreakingModel.createMesh();
        meshDefinition.getRoot().retainExactParts(Set.of((Object)"head"));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(CreakingRenderState creakingRenderState) {
        super.setupAnim(creakingRenderState);
        this.head.xRot = creakingRenderState.xRot * ((float)Math.PI / 180);
        this.head.yRot = creakingRenderState.yRot * ((float)Math.PI / 180);
        if (creakingRenderState.canMove) {
            this.walkAnimation.applyWalk(creakingRenderState.walkAnimationPos, creakingRenderState.walkAnimationSpeed, 1.0f, 1.0f);
        }
        this.attackAnimation.apply(creakingRenderState.attackAnimationState, creakingRenderState.ageInTicks);
        this.invulnerableAnimation.apply(creakingRenderState.invulnerabilityAnimationState, creakingRenderState.ageInTicks);
        this.deathAnimation.apply(creakingRenderState.deathAnimationState, creakingRenderState.ageInTicks);
    }
}

