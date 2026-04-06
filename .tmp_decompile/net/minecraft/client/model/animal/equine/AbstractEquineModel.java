/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.animal.equine;

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
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractEquineModel<T extends EquineRenderState>
extends EntityModel<T> {
    private static final float DEG_125 = 2.1816616f;
    private static final float DEG_60 = 1.0471976f;
    private static final float DEG_45 = 0.7853982f;
    private static final float DEG_30 = 0.5235988f;
    private static final float DEG_15 = 0.2617994f;
    protected static final String HEAD_PARTS = "head_parts";
    protected static final MeshTransformer BABY_TRANSFORMER = new BabyModelTransform(true, 16.2f, 1.36f, 2.7272f, 2.0f, 20.0f, Set.of((Object)"head_parts"));
    protected final ModelPart body;
    protected final ModelPart headParts;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;

    public AbstractEquineModel(ModelPart modelPart) {
        super(modelPart);
        this.body = modelPart.getChild("body");
        this.headParts = modelPart.getChild(HEAD_PARTS);
        this.rightHindLeg = modelPart.getChild("right_hind_leg");
        this.leftHindLeg = modelPart.getChild("left_hind_leg");
        this.rightFrontLeg = modelPart.getChild("right_front_leg");
        this.leftFrontLeg = modelPart.getChild("left_front_leg");
        this.tail = this.body.getChild("tail");
    }

    public static MeshDefinition createBodyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(-5.0f, -8.0f, -17.0f, 10.0f, 10.0f, 22.0f, new CubeDeformation(0.05f)), PartPose.offset(0.0f, 11.0f, 5.0f));
        PartDefinition partDefinition3 = partDefinition.addOrReplaceChild(HEAD_PARTS, CubeListBuilder.create().texOffs(0, 35).addBox(-2.05f, -6.0f, -2.0f, 4.0f, 12.0f, 7.0f), PartPose.offsetAndRotation(0.0f, 4.0f, -12.0f, 0.5235988f, 0.0f, 0.0f));
        PartDefinition partDefinition4 = partDefinition3.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 13).addBox(-3.0f, -11.0f, -2.0f, 6.0f, 5.0f, 7.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(56, 36).addBox(-1.0f, -11.0f, 5.01f, 2.0f, 16.0f, 2.0f, cubeDeformation), PartPose.ZERO);
        partDefinition3.addOrReplaceChild("upper_mouth", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0f, -11.0f, -7.0f, 4.0f, 5.0f, 5.0f, cubeDeformation), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(-4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(4.0f, 14.0f, -10.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation), PartPose.offset(-4.0f, 14.0f, -10.0f));
        partDefinition2.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 36).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 14.0f, 4.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, -5.0f, 2.0f, 0.5235988f, 0.0f, 0.0f));
        partDefinition4.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(19, 16).addBox(0.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new CubeDeformation(-0.001f)), PartPose.ZERO);
        partDefinition4.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(19, 16).addBox(-2.55f, -13.0f, 4.0f, 2.0f, 3.0f, 1.0f, new CubeDeformation(-0.001f)), PartPose.ZERO);
        return meshDefinition;
    }

    public static MeshDefinition createBabyMesh(CubeDeformation cubeDeformation) {
        return BABY_TRANSFORMER.apply(AbstractEquineModel.createFullScaleBabyMesh(cubeDeformation));
    }

    protected static MeshDefinition createFullScaleBabyMesh(CubeDeformation cubeDeformation) {
        MeshDefinition meshDefinition = AbstractEquineModel.createBodyMesh(cubeDeformation);
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation2 = cubeDeformation.extend(0.0f, 5.5f, 0.0f);
        partDefinition.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.0f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(-4.0f, 14.0f, 7.0f));
        partDefinition.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(48, 21).mirror().addBox(-3.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(4.0f, 14.0f, -10.0f));
        partDefinition.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(48, 21).addBox(-1.0f, -1.01f, -1.9f, 4.0f, 11.0f, 4.0f, cubeDeformation2), PartPose.offset(-4.0f, 14.0f, -10.0f));
        return meshDefinition;
    }

    @Override
    public void setupAnim(T equineRenderState) {
        super.setupAnim(equineRenderState);
        float f = Mth.clamp(((EquineRenderState)equineRenderState).yRot, -20.0f, 20.0f);
        float g = ((EquineRenderState)equineRenderState).xRot * ((float)Math.PI / 180);
        float h = ((EquineRenderState)equineRenderState).walkAnimationSpeed;
        float i = ((EquineRenderState)equineRenderState).walkAnimationPos;
        if (h > 0.2f) {
            g += Mth.cos(i * 0.8f) * 0.15f * h;
        }
        float j = ((EquineRenderState)equineRenderState).eatAnimation;
        float k = ((EquineRenderState)equineRenderState).standAnimation;
        float l = 1.0f - k;
        float m = ((EquineRenderState)equineRenderState).feedingAnimation;
        boolean bl = ((EquineRenderState)equineRenderState).animateTail;
        this.headParts.xRot = 0.5235988f + g;
        this.headParts.yRot = f * ((float)Math.PI / 180);
        float n = ((EquineRenderState)equineRenderState).isInWater ? 0.2f : 1.0f;
        float o = Mth.cos(n * i * 0.6662f + (float)Math.PI);
        float p = o * 0.8f * h;
        float q = (1.0f - Math.max(k, j)) * (0.5235988f + g + m * Mth.sin(((EquineRenderState)equineRenderState).ageInTicks) * 0.05f);
        this.headParts.xRot = k * (0.2617994f + g) + j * (2.1816616f + Mth.sin(((EquineRenderState)equineRenderState).ageInTicks) * 0.05f) + q;
        this.headParts.yRot = k * f * ((float)Math.PI / 180) + (1.0f - Math.max(k, j)) * this.headParts.yRot;
        float r = ((EquineRenderState)equineRenderState).ageScale;
        this.headParts.y += Mth.lerp(j, Mth.lerp(k, 0.0f, -8.0f * r), 7.0f * r);
        this.headParts.z = Mth.lerp(k, this.headParts.z, -4.0f * r);
        this.body.xRot = k * -0.7853982f + l * this.body.xRot;
        float s = 0.2617994f * k;
        float t = Mth.cos(((EquineRenderState)equineRenderState).ageInTicks * 0.6f + (float)Math.PI);
        this.leftFrontLeg.y -= 12.0f * r * k;
        this.leftFrontLeg.z += 4.0f * r * k;
        this.rightFrontLeg.y = this.leftFrontLeg.y;
        this.rightFrontLeg.z = this.leftFrontLeg.z;
        float u = (-1.0471976f + t) * k + p * l;
        float v = (-1.0471976f - t) * k - p * l;
        this.leftHindLeg.xRot = s - o * 0.5f * h * l;
        this.rightHindLeg.xRot = s + o * 0.5f * h * l;
        this.leftFrontLeg.xRot = u;
        this.rightFrontLeg.xRot = v;
        this.tail.xRot = 0.5235988f + h * 0.75f;
        this.tail.y += h * r;
        this.tail.z += h * 2.0f * r;
        this.tail.yRot = bl ? Mth.cos(((EquineRenderState)equineRenderState).ageInTicks * 0.7f) : 0.0f;
    }
}

