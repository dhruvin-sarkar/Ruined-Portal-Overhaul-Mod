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
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.DrownedOuterLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class DrownedRenderer
extends AbstractZombieRenderer<Drowned, ZombieRenderState, DrownedModel> {
    private static final Identifier DROWNED_LOCATION = Identifier.withDefaultNamespace("textures/entity/zombie/drowned.png");

    public DrownedRenderer(EntityRendererProvider.Context context) {
        super(context, new DrownedModel(context.bakeLayer(ModelLayers.DROWNED)), new DrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY)), ArmorModelSet.bake(ModelLayers.DROWNED_ARMOR, context.getModelSet(), DrownedModel::new), ArmorModelSet.bake(ModelLayers.DROWNED_BABY_ARMOR, context.getModelSet(), DrownedModel::new));
        this.addLayer(new DrownedOuterLayer(this, context.getModelSet()));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState zombieRenderState) {
        return DROWNED_LOCATION;
    }

    @Override
    protected void setupRotations(ZombieRenderState zombieRenderState, PoseStack poseStack, float f, float g) {
        super.setupRotations(zombieRenderState, poseStack, f, g);
        float h = zombieRenderState.swimAmount;
        if (h > 0.0f) {
            float i = -10.0f - zombieRenderState.xRot;
            float j = Mth.lerp(h, 0.0f, i);
            poseStack.rotateAround((Quaternionfc)Axis.XP.rotationDegrees(j), 0.0f, zombieRenderState.boundingBoxHeight / 2.0f / g, 0.0f);
        }
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(Drowned drowned, HumanoidArm humanoidArm) {
        ItemStack itemStack = drowned.getItemHeldByArm(humanoidArm);
        if (drowned.getMainArm() == humanoidArm && drowned.isAggressive() && itemStack.is(Items.TRIDENT)) {
            return HumanoidModel.ArmPose.THROW_TRIDENT;
        }
        return super.getArmPose(drowned, humanoidArm);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombieRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

