/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientAvatarState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.BeeStingerLayer;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.Deadmau5EarsLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ParrotOnShoulderLayer;
import net.minecraft.client.renderer.entity.layers.PlayerItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.SpinAttackEffectLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class AvatarRenderer<AvatarlikeEntity extends Avatar>
extends LivingEntityRenderer<AvatarlikeEntity, AvatarRenderState, PlayerModel> {
    public AvatarRenderer(EntityRendererProvider.Context context, boolean bl) {
        super(context, new PlayerModel(context.bakeLayer(bl ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER), bl), 0.5f);
        this.addLayer(new HumanoidArmorLayer<AvatarRenderState, PlayerModel, PlayerModel>(this, ArmorModelSet.bake(bl ? ModelLayers.PLAYER_SLIM_ARMOR : ModelLayers.PLAYER_ARMOR, context.getModelSet(), modelPart -> new PlayerModel((ModelPart)modelPart, bl)), context.getEquipmentRenderer()));
        this.addLayer(new PlayerItemInHandLayer<AvatarRenderState, PlayerModel>(this));
        this.addLayer(new ArrowLayer(this, context));
        this.addLayer(new Deadmau5EarsLayer(this, context.getModelSet()));
        this.addLayer(new CapeLayer(this, context.getModelSet(), context.getEquipmentAssets()));
        this.addLayer(new CustomHeadLayer<AvatarRenderState, PlayerModel>(this, context.getModelSet(), context.getPlayerSkinRenderCache()));
        this.addLayer(new WingsLayer<AvatarRenderState, PlayerModel>(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new ParrotOnShoulderLayer(this, context.getModelSet()));
        this.addLayer(new SpinAttackEffectLayer(this, context.getModelSet()));
        this.addLayer(new BeeStingerLayer(this, context));
    }

    @Override
    protected boolean shouldRenderLayers(AvatarRenderState avatarRenderState) {
        return !avatarRenderState.isSpectator;
    }

    @Override
    public Vec3 getRenderOffset(AvatarRenderState avatarRenderState) {
        Vec3 vec3 = super.getRenderOffset(avatarRenderState);
        if (avatarRenderState.isCrouching) {
            return vec3.add(0.0, (double)(avatarRenderState.scale * -2.0f) / 16.0, 0.0);
        }
        return vec3;
    }

    private static HumanoidModel.ArmPose getArmPose(Avatar avatar, HumanoidArm humanoidArm) {
        ItemStack itemStack = avatar.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack itemStack2 = avatar.getItemInHand(InteractionHand.OFF_HAND);
        HumanoidModel.ArmPose armPose = AvatarRenderer.getArmPose(avatar, itemStack, InteractionHand.MAIN_HAND);
        HumanoidModel.ArmPose armPose2 = AvatarRenderer.getArmPose(avatar, itemStack2, InteractionHand.OFF_HAND);
        if (armPose.isTwoHanded()) {
            HumanoidModel.ArmPose armPose3 = armPose2 = itemStack2.isEmpty() ? HumanoidModel.ArmPose.EMPTY : HumanoidModel.ArmPose.ITEM;
        }
        if (avatar.getMainArm() == humanoidArm) {
            return armPose;
        }
        return armPose2;
    }

    private static HumanoidModel.ArmPose getArmPose(Avatar avatar, ItemStack itemStack, InteractionHand interactionHand) {
        SwingAnimation swingAnimation;
        if (itemStack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        }
        if (!avatar.swinging && itemStack.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemStack)) {
            return HumanoidModel.ArmPose.CROSSBOW_HOLD;
        }
        if (avatar.getUsedItemHand() == interactionHand && avatar.getUseItemRemainingTicks() > 0) {
            ItemUseAnimation itemUseAnimation = itemStack.getUseAnimation();
            if (itemUseAnimation == ItemUseAnimation.BLOCK) {
                return HumanoidModel.ArmPose.BLOCK;
            }
            if (itemUseAnimation == ItemUseAnimation.BOW) {
                return HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
            if (itemUseAnimation == ItemUseAnimation.TRIDENT) {
                return HumanoidModel.ArmPose.THROW_TRIDENT;
            }
            if (itemUseAnimation == ItemUseAnimation.CROSSBOW) {
                return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
            }
            if (itemUseAnimation == ItemUseAnimation.SPYGLASS) {
                return HumanoidModel.ArmPose.SPYGLASS;
            }
            if (itemUseAnimation == ItemUseAnimation.TOOT_HORN) {
                return HumanoidModel.ArmPose.TOOT_HORN;
            }
            if (itemUseAnimation == ItemUseAnimation.BRUSH) {
                return HumanoidModel.ArmPose.BRUSH;
            }
            if (itemUseAnimation == ItemUseAnimation.SPEAR) {
                return HumanoidModel.ArmPose.SPEAR;
            }
        }
        if ((swingAnimation = itemStack.get(DataComponents.SWING_ANIMATION)) != null && swingAnimation.type() == SwingAnimationType.STAB && avatar.swinging) {
            return HumanoidModel.ArmPose.SPEAR;
        }
        if (itemStack.is(ItemTags.SPEARS)) {
            return HumanoidModel.ArmPose.SPEAR;
        }
        return HumanoidModel.ArmPose.ITEM;
    }

    @Override
    public Identifier getTextureLocation(AvatarRenderState avatarRenderState) {
        return avatarRenderState.skin.body().texturePath();
    }

    @Override
    protected void scale(AvatarRenderState avatarRenderState, PoseStack poseStack) {
        float f = 0.9375f;
        poseStack.scale(0.9375f, 0.9375f, 0.9375f);
    }

    @Override
    protected void submitNameTag(AvatarRenderState avatarRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        int i;
        poseStack.pushPose();
        int n = i = avatarRenderState.showExtraEars ? -10 : 0;
        if (avatarRenderState.scoreText != null) {
            submitNodeCollector.submitNameTag(poseStack, avatarRenderState.nameTagAttachment, i, avatarRenderState.scoreText, !avatarRenderState.isDiscrete, avatarRenderState.lightCoords, avatarRenderState.distanceToCameraSq, cameraRenderState);
            Objects.requireNonNull(this.getFont());
            poseStack.translate(0.0f, 9.0f * 1.15f * 0.025f, 0.0f);
        }
        if (avatarRenderState.nameTag != null) {
            submitNodeCollector.submitNameTag(poseStack, avatarRenderState.nameTagAttachment, i, avatarRenderState.nameTag, !avatarRenderState.isDiscrete, avatarRenderState.lightCoords, avatarRenderState.distanceToCameraSq, cameraRenderState);
        }
        poseStack.popPose();
    }

    @Override
    public AvatarRenderState createRenderState() {
        return new AvatarRenderState();
    }

    @Override
    public void extractRenderState(AvatarlikeEntity avatar, AvatarRenderState avatarRenderState, float f) {
        ItemStack itemStack;
        super.extractRenderState(avatar, avatarRenderState, f);
        HumanoidMobRenderer.extractHumanoidRenderState(avatar, avatarRenderState, f, this.itemModelResolver);
        avatarRenderState.leftArmPose = AvatarRenderer.getArmPose(avatar, HumanoidArm.LEFT);
        avatarRenderState.rightArmPose = AvatarRenderer.getArmPose(avatar, HumanoidArm.RIGHT);
        avatarRenderState.skin = ((ClientAvatarEntity)avatar).getSkin();
        avatarRenderState.arrowCount = ((LivingEntity)avatar).getArrowCount();
        avatarRenderState.stingerCount = ((LivingEntity)avatar).getStingerCount();
        avatarRenderState.isSpectator = ((Entity)avatar).isSpectator();
        avatarRenderState.showHat = ((Avatar)avatar).isModelPartShown(PlayerModelPart.HAT);
        avatarRenderState.showJacket = ((Avatar)avatar).isModelPartShown(PlayerModelPart.JACKET);
        avatarRenderState.showLeftPants = ((Avatar)avatar).isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        avatarRenderState.showRightPants = ((Avatar)avatar).isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        avatarRenderState.showLeftSleeve = ((Avatar)avatar).isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        avatarRenderState.showRightSleeve = ((Avatar)avatar).isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        avatarRenderState.showCape = ((Avatar)avatar).isModelPartShown(PlayerModelPart.CAPE);
        this.extractFlightData(avatar, avatarRenderState, f);
        this.extractCapeState(avatar, avatarRenderState, f);
        avatarRenderState.scoreText = avatarRenderState.distanceToCameraSq < 100.0 ? ((ClientAvatarEntity)avatar).belowNameDisplay() : null;
        avatarRenderState.parrotOnLeftShoulder = ((ClientAvatarEntity)avatar).getParrotVariantOnShoulder(true);
        avatarRenderState.parrotOnRightShoulder = ((ClientAvatarEntity)avatar).getParrotVariantOnShoulder(false);
        avatarRenderState.id = ((Entity)avatar).getId();
        avatarRenderState.showExtraEars = ((ClientAvatarEntity)avatar).showExtraEars();
        avatarRenderState.heldOnHead.clear();
        if (avatarRenderState.isUsingItem && (itemStack = ((LivingEntity)avatar).getItemInHand(avatarRenderState.useItemHand)).is(Items.SPYGLASS)) {
            this.itemModelResolver.updateForLiving(avatarRenderState.heldOnHead, itemStack, ItemDisplayContext.HEAD, (LivingEntity)avatar);
        }
    }

    @Override
    protected boolean shouldShowName(AvatarlikeEntity avatar, double d) {
        return super.shouldShowName(avatar, d) && (((LivingEntity)avatar).shouldShowName() || ((Entity)avatar).hasCustomName() && avatar == this.entityRenderDispatcher.crosshairPickEntity);
    }

    private void extractFlightData(AvatarlikeEntity avatar, AvatarRenderState avatarRenderState, float f) {
        avatarRenderState.fallFlyingTimeInTicks = (float)((LivingEntity)avatar).getFallFlyingTicks() + f;
        Vec3 vec3 = ((Entity)avatar).getViewVector(f);
        Vec3 vec32 = ((ClientAvatarEntity)avatar).avatarState().deltaMovementOnPreviousTick().lerp(((Entity)avatar).getDeltaMovement(), f);
        if (vec32.horizontalDistanceSqr() > (double)1.0E-5f && vec3.horizontalDistanceSqr() > (double)1.0E-5f) {
            avatarRenderState.shouldApplyFlyingYRot = true;
            double d = vec32.horizontal().normalize().dot(vec3.horizontal().normalize());
            double e = vec32.x * vec3.z - vec32.z * vec3.x;
            avatarRenderState.flyingYRot = (float)(Math.signum(e) * Math.acos(Math.min(1.0, Math.abs(d))));
        } else {
            avatarRenderState.shouldApplyFlyingYRot = false;
            avatarRenderState.flyingYRot = 0.0f;
        }
    }

    private void extractCapeState(AvatarlikeEntity avatar, AvatarRenderState avatarRenderState, float f) {
        ClientAvatarState clientAvatarState = ((ClientAvatarEntity)avatar).avatarState();
        double d = clientAvatarState.getInterpolatedCloakX(f) - Mth.lerp((double)f, ((Avatar)avatar).xo, ((Entity)avatar).getX());
        double e = clientAvatarState.getInterpolatedCloakY(f) - Mth.lerp((double)f, ((Avatar)avatar).yo, ((Entity)avatar).getY());
        double g = clientAvatarState.getInterpolatedCloakZ(f) - Mth.lerp((double)f, ((Avatar)avatar).zo, ((Entity)avatar).getZ());
        float h = Mth.rotLerp(f, ((Avatar)avatar).yBodyRotO, ((Avatar)avatar).yBodyRot);
        double i = Mth.sin(h * ((float)Math.PI / 180));
        double j = -Mth.cos(h * ((float)Math.PI / 180));
        avatarRenderState.capeFlap = (float)e * 10.0f;
        avatarRenderState.capeFlap = Mth.clamp(avatarRenderState.capeFlap, -6.0f, 32.0f);
        avatarRenderState.capeLean = (float)(d * i + g * j) * 100.0f;
        avatarRenderState.capeLean *= 1.0f - avatarRenderState.fallFlyingScale();
        avatarRenderState.capeLean = Mth.clamp(avatarRenderState.capeLean, 0.0f, 150.0f);
        avatarRenderState.capeLean2 = (float)(d * j - g * i) * 100.0f;
        avatarRenderState.capeLean2 = Mth.clamp(avatarRenderState.capeLean2, -20.0f, 20.0f);
        float k = clientAvatarState.getInterpolatedBob(f);
        float l = clientAvatarState.getInterpolatedWalkDistance(f);
        avatarRenderState.capeFlap += Mth.sin(l * 6.0f) * 32.0f * k;
    }

    public void renderRightHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, Identifier identifier, boolean bl) {
        this.renderHand(poseStack, submitNodeCollector, i, identifier, ((PlayerModel)this.model).rightArm, bl);
    }

    public void renderLeftHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, Identifier identifier, boolean bl) {
        this.renderHand(poseStack, submitNodeCollector, i, identifier, ((PlayerModel)this.model).leftArm, bl);
    }

    private void renderHand(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, Identifier identifier, ModelPart modelPart, boolean bl) {
        PlayerModel playerModel = (PlayerModel)this.getModel();
        modelPart.resetPose();
        modelPart.visible = true;
        playerModel.leftSleeve.visible = bl;
        playerModel.rightSleeve.visible = bl;
        playerModel.leftArm.zRot = -0.1f;
        playerModel.rightArm.zRot = 0.1f;
        submitNodeCollector.submitModelPart(modelPart, poseStack, RenderTypes.entityTranslucent(identifier), i, OverlayTexture.NO_OVERLAY, null);
    }

    @Override
    protected void setupRotations(AvatarRenderState avatarRenderState, PoseStack poseStack, float f, float g) {
        float h = avatarRenderState.swimAmount;
        float i = avatarRenderState.xRot;
        if (avatarRenderState.isFallFlying) {
            super.setupRotations(avatarRenderState, poseStack, f, g);
            float j = avatarRenderState.fallFlyingScale();
            if (!avatarRenderState.isAutoSpinAttack) {
                poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(j * (-90.0f - i)));
            }
            if (avatarRenderState.shouldApplyFlyingYRot) {
                poseStack.mulPose((Quaternionfc)Axis.YP.rotation(avatarRenderState.flyingYRot));
            }
        } else if (h > 0.0f) {
            super.setupRotations(avatarRenderState, poseStack, f, g);
            float j = avatarRenderState.isInWater ? -90.0f - i : -90.0f;
            float k = Mth.lerp(h, 0.0f, j);
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(k));
            if (avatarRenderState.isVisuallySwimming) {
                poseStack.translate(0.0f, -1.0f, 0.3f);
            }
        } else {
            super.setupRotations(avatarRenderState, poseStack, f, g);
        }
    }

    @Override
    public boolean isEntityUpsideDown(AvatarlikeEntity avatar) {
        if (((Avatar)avatar).isModelPartShown(PlayerModelPart.CAPE)) {
            if (avatar instanceof Player) {
                Player player = (Player)avatar;
                return AvatarRenderer.isPlayerUpsideDown(player);
            }
            return super.isEntityUpsideDown(avatar);
        }
        return false;
    }

    public static boolean isPlayerUpsideDown(Player player) {
        return AvatarRenderer.isUpsideDownName(player.getGameProfile().name());
    }

    @Override
    public /* synthetic */ boolean isEntityUpsideDown(LivingEntity livingEntity) {
        return this.isEntityUpsideDown((AvatarlikeEntity)((Avatar)livingEntity));
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((AvatarRenderState)livingEntityRenderState);
    }

    @Override
    protected /* synthetic */ boolean shouldRenderLayers(LivingEntityRenderState livingEntityRenderState) {
        return this.shouldRenderLayers((AvatarRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ void submitNameTag(EntityRenderState entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.submitNameTag((AvatarRenderState)entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public /* synthetic */ Vec3 getRenderOffset(EntityRenderState entityRenderState) {
        return this.getRenderOffset((AvatarRenderState)entityRenderState);
    }
}

