/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.model.object.armorstand.ArmorStandModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ArmorStandRenderer
extends LivingEntityRenderer<ArmorStand, ArmorStandRenderState, ArmorStandArmorModel> {
    public static final Identifier DEFAULT_SKIN_LOCATION = Identifier.withDefaultNamespace("textures/entity/armorstand/wood.png");
    private final ArmorStandArmorModel bigModel = (ArmorStandArmorModel)this.getModel();
    private final ArmorStandArmorModel smallModel;

    public ArmorStandRenderer(EntityRendererProvider.Context context) {
        super(context, new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND)), 0.0f);
        this.smallModel = new ArmorStandModel(context.bakeLayer(ModelLayers.ARMOR_STAND_SMALL));
        this.addLayer(new HumanoidArmorLayer<ArmorStandRenderState, ArmorStandArmorModel, ArmorStandArmorModel>(this, ArmorModelSet.bake(ModelLayers.ARMOR_STAND_ARMOR, context.getModelSet(), ArmorStandArmorModel::new), ArmorModelSet.bake(ModelLayers.ARMOR_STAND_SMALL_ARMOR, context.getModelSet(), ArmorStandArmorModel::new), context.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer<ArmorStandRenderState, ArmorStandArmorModel>(this));
        this.addLayer(new WingsLayer<ArmorStandRenderState, ArmorStandArmorModel>(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new CustomHeadLayer<ArmorStandRenderState, ArmorStandArmorModel>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
    }

    @Override
    public Identifier getTextureLocation(ArmorStandRenderState armorStandRenderState) {
        return DEFAULT_SKIN_LOCATION;
    }

    @Override
    public ArmorStandRenderState createRenderState() {
        return new ArmorStandRenderState();
    }

    @Override
    public void extractRenderState(ArmorStand armorStand, ArmorStandRenderState armorStandRenderState, float f) {
        super.extractRenderState(armorStand, armorStandRenderState, f);
        HumanoidMobRenderer.extractHumanoidRenderState(armorStand, armorStandRenderState, f, this.itemModelResolver);
        armorStandRenderState.yRot = Mth.rotLerp(f, armorStand.yRotO, armorStand.getYRot());
        armorStandRenderState.isMarker = armorStand.isMarker();
        armorStandRenderState.isSmall = armorStand.isSmall();
        armorStandRenderState.showArms = armorStand.showArms();
        armorStandRenderState.showBasePlate = armorStand.showBasePlate();
        armorStandRenderState.bodyPose = armorStand.getBodyPose();
        armorStandRenderState.headPose = armorStand.getHeadPose();
        armorStandRenderState.leftArmPose = armorStand.getLeftArmPose();
        armorStandRenderState.rightArmPose = armorStand.getRightArmPose();
        armorStandRenderState.leftLegPose = armorStand.getLeftLegPose();
        armorStandRenderState.rightLegPose = armorStand.getRightLegPose();
        armorStandRenderState.wiggle = (float)(armorStand.level().getGameTime() - armorStand.lastHit) + f;
    }

    @Override
    public void submit(ArmorStandRenderState armorStandRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.model = armorStandRenderState.isSmall ? this.smallModel : this.bigModel;
        super.submit(armorStandRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    protected void setupRotations(ArmorStandRenderState armorStandRenderState, PoseStack poseStack, float f, float g) {
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - f));
        if (armorStandRenderState.wiggle < 5.0f) {
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(Mth.sin(armorStandRenderState.wiggle / 1.5f * (float)Math.PI) * 3.0f));
        }
    }

    @Override
    protected boolean shouldShowName(ArmorStand armorStand, double d) {
        return armorStand.isCustomNameVisible();
    }

    @Override
    protected @Nullable RenderType getRenderType(ArmorStandRenderState armorStandRenderState, boolean bl, boolean bl2, boolean bl3) {
        if (!armorStandRenderState.isMarker) {
            return super.getRenderType(armorStandRenderState, bl, bl2, bl3);
        }
        Identifier identifier = this.getTextureLocation(armorStandRenderState);
        if (bl2) {
            return RenderTypes.entityTranslucent(identifier, false);
        }
        if (bl) {
            return RenderTypes.entityCutoutNoCull(identifier, false);
        }
        return null;
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ArmorStandRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

