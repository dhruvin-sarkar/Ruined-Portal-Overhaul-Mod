/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.feline;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.MeshTransformer;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.FelineRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class FelineModel<T extends FelineRenderState>
extends EntityModel<T> {
    public static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 10.0f, 4.0f, Set.of((Object)"head"));
    private static final float XO = 0.0f;
    private static final float YO = 16.0f;
    private static final float ZO = -9.0f;
    protected static final float BACK_LEG_Y = 18.0f;
    protected static final float BACK_LEG_Z = 5.0f;
    protected static final float FRONT_LEG_Y = 14.1f;
    private static final float FRONT_LEG_Z = -5.0f;
    private static final String TAIL_1 = "tail1";
    private static final String TAIL_2 = "tail2";
    protected final ModelPart leftHindLeg;
    protected final ModelPart rightHindLeg;
    protected final ModelPart leftFrontLeg;
    protected final ModelPart rightFrontLeg;
    protected final ModelPart tail1;
    protected final ModelPart tail2;
    protected final ModelPart head;
    protected final ModelPart body;

    public FelineModel(ModelPart modelPart) {
        super(modelPart);
        this.head = modelPart.getChild("head");
        this.body = modelPart.getChild("body");
        this.tail1 = modelPart.getChild(TAIL_1);
        this.tail2 = modelPart.getChild(TAIL_2);
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
    }

    public static MeshDefinition createBodyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation2 = new CubeDeformation(-0.02f);
        partDefinition.addOrReplaceChild("head", CubeListBuilder.create().addBox("main", -2.5f, -2.0f, -3.0f, 5.0f, 4.0f, 5.0f, cubeDeformation).addBox("nose", -1.5f, -0.001f, -4.0f, 3, 2, 2, cubeDeformation, 0, 24).addBox("ear1", -2.0f, -3.0f, 0.0f, 1, 1, 2, cubeDeformation, 0, 10).addBox("ear2", 1.0f, -3.0f, 0.0f, 1, 1, 2, cubeDeformation, 6, 10), PartPose.offset(0.0f, 15.0f, -9.0f));
        partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(20, 0).addBox(-2.0f, 3.0f, -8.0f, 4.0f, 16.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 12.0f, -10.0f, 1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(TAIL_1, CubeListBuilder.create().texOffs(0, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 15.0f, 8.0f, 0.9f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(TAIL_2, CubeListBuilder.create().texOffs(4, 15).addBox(-0.5f, 0.0f, 0.0f, 1.0f, 8.0f, 1.0f, cubeDeformation2), PartPose.offset(0.0f, 20.0f, 14.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(8, 13).addBox(-1.0f, 0.0f, 1.0f, 2.0f, 6.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("left_hind_leg", cubeListBuilder, PartPose.offset(1.1f, 18.0f, 5.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", cubeListBuilder, PartPose.offset(-1.1f, 18.0f, 5.0f));
        CubeListBuilder cubeListBuilder2 = CubeListBuilder.create().texOffs(40, 0).addBox(-1.0f, 0.0f, 0.0f, 2.0f, 10.0f, 2.0f, cubeDeformation);
        partDefinition.addOrReplaceChild("left_front_leg", cubeListBuilder2, PartPose.offset(1.2f, 14.1f, -5.0f));
        partDefinition.addOrReplaceChild("right_front_leg", cubeListBuilder2, PartPose.offset(-1.2f, 14.1f, -5.0f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(T felineRenderState) {
        super.setupAnim(felineRenderState);
        float f = ((FelineRenderState)felineRenderState).ageScale;
        if (((FelineRenderState)felineRenderState).isCrouching) {
            this.body.y += 1.0f * f;
            this.head.y += 2.0f * f;
            this.tail1.y += 1.0f * f;
            this.tail2.y += -4.0f * f;
            this.tail2.z += 2.0f * f;
            this.tail1.xRot = 1.5707964f;
            this.tail2.xRot = 1.5707964f;
        } else if (((FelineRenderState)felineRenderState).isSprinting) {
            this.tail2.y = this.tail1.y;
            this.tail2.z += 2.0f * f;
            this.tail1.xRot = 1.5707964f;
            this.tail2.xRot = 1.5707964f;
        }
        this.head.xRot = ((FelineRenderState)felineRenderState).xRot * ((float)Math.PI / 180);
        this.head.yRot = ((FelineRenderState)felineRenderState).yRot * ((float)Math.PI / 180);
        if (!((FelineRenderState)felineRenderState).isSitting) {
            this.body.xRot = 1.5707964f;
            float g = ((FelineRenderState)felineRenderState).walkAnimationSpeed;
            float h = ((FelineRenderState)felineRenderState).walkAnimationPos;
            if (((FelineRenderState)felineRenderState).isSprinting) {
                this.leftHindLeg.xRot = Mth.cos(h * 0.6662f) * g;
                this.rightHindLeg.xRot = Mth.cos(h * 0.6662f + 0.3f) * g;
                this.leftFrontLeg.xRot = Mth.cos(h * 0.6662f + (float)Math.PI + 0.3f) * g;
                this.rightFrontLeg.xRot = Mth.cos(h * 0.6662f + (float)Math.PI) * g;
                this.tail2.xRot = 1.7278761f + 0.31415927f * Mth.cos(h) * g;
            } else {
                this.leftHindLeg.xRot = Mth.cos(h * 0.6662f) * g;
                this.rightHindLeg.xRot = Mth.cos(h * 0.6662f + (float)Math.PI) * g;
                this.leftFrontLeg.xRot = Mth.cos(h * 0.6662f + (float)Math.PI) * g;
                this.rightFrontLeg.xRot = Mth.cos(h * 0.6662f) * g;
                this.tail2.xRot = !((FelineRenderState)felineRenderState).isCrouching ? 1.7278761f + 0.7853982f * Mth.cos(h) * g : 1.7278761f + 0.47123894f * Mth.cos(h) * g;
            }
        }
        if (((FelineRenderState)felineRenderState).isSitting) {
            this.body.xRot = 0.7853982f;
            this.body.y += -4.0f * f;
            this.body.z += 5.0f * f;
            this.head.y += -3.3f * f;
            this.head.z += 1.0f * f;
            this.tail1.y += 8.0f * f;
            this.tail1.z += -2.0f * f;
            this.tail2.y += 2.0f * f;
            this.tail2.z += -0.8f * f;
            this.tail1.xRot = 1.7278761f;
            this.tail2.xRot = 2.670354f;
            this.leftFrontLeg.xRot = -0.15707964f;
            this.leftFrontLeg.y += 2.0f * f;
            this.leftFrontLeg.z -= 2.0f * f;
            this.rightFrontLeg.xRot = -0.15707964f;
            this.rightFrontLeg.y += 2.0f * f;
            this.rightFrontLeg.z -= 2.0f * f;
            this.leftHindLeg.xRot = -1.5707964f;
            this.leftHindLeg.y += 3.0f * f;
            this.leftHindLeg.z -= 4.0f * f;
            this.rightHindLeg.xRot = -1.5707964f;
            this.rightHindLeg.y += 3.0f * f;
            this.rightHindLeg.z -= 4.0f * f;
        }
        if (((FelineRenderState)felineRenderState).lieDownAmount > 0.0f) {
            this.head.zRot = Mth.rotLerp(((FelineRenderState)felineRenderState).lieDownAmount, this.head.zRot, -1.2707963f);
            this.head.yRot = Mth.rotLerp(((FelineRenderState)felineRenderState).lieDownAmount, this.head.yRot, 1.2707963f);
            this.leftFrontLeg.xRot = -1.2707963f;
            this.rightFrontLeg.xRot = -0.47079635f;
            this.rightFrontLeg.zRot = -0.2f;
            this.rightFrontLeg.x += f;
            this.leftHindLeg.xRot = -0.4f;
            this.rightHindLeg.xRot = 0.5f;
            this.rightHindLeg.zRot = -0.5f;
            this.rightHindLeg.x += 0.8f * f;
            this.rightHindLeg.y += 2.0f * f;
            this.tail1.xRot = Mth.rotLerp(((FelineRenderState)felineRenderState).lieDownAmountTail, this.tail1.xRot, 0.8f);
            this.tail2.xRot = Mth.rotLerp(((FelineRenderState)felineRenderState).lieDownAmountTail, this.tail2.xRot, -0.4f);
        }
        if (((FelineRenderState)felineRenderState).relaxStateOneAmount > 0.0f) {
            this.head.xRot = Mth.rotLerp(((FelineRenderState)felineRenderState).relaxStateOneAmount, this.head.xRot, -0.58177644f);
        }
    }
}

