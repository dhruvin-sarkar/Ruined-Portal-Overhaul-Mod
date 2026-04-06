/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class LivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends EntityRenderer<T, S>
implements RenderLayerParent<S, M> {
    private static final float EYE_BED_OFFSET = 0.1f;
    protected M model;
    protected final ItemModelResolver itemModelResolver;
    protected final List<RenderLayer<S, M>> layers = Lists.newArrayList();

    public LivingEntityRenderer(EntityRendererProvider.Context context, M entityModel, float f) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.model = entityModel;
        this.shadowRadius = f;
    }

    protected final boolean addLayer(RenderLayer<S, M> renderLayer) {
        return this.layers.add(renderLayer);
    }

    @Override
    public M getModel() {
        return this.model;
    }

    @Override
    protected AABB getBoundingBoxForCulling(T livingEntity) {
        AABB aABB = super.getBoundingBoxForCulling(livingEntity);
        if (((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.HEAD).is(Items.DRAGON_HEAD)) {
            float f = 0.5f;
            return aABB.inflate(0.5, 0.5, 0.5);
        }
        return aABB;
    }

    @Override
    public void submit(S livingEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Direction direction;
        poseStack.pushPose();
        if (((LivingEntityRenderState)livingEntityRenderState).hasPose(Pose.SLEEPING) && (direction = ((LivingEntityRenderState)livingEntityRenderState).bedOrientation) != null) {
            float f = ((LivingEntityRenderState)livingEntityRenderState).eyeHeight - 0.1f;
            poseStack.translate((float)(-direction.getStepX()) * f, 0.0f, (float)(-direction.getStepZ()) * f);
        }
        float g = ((LivingEntityRenderState)livingEntityRenderState).scale;
        poseStack.scale(g, g, g);
        this.setupRotations(livingEntityRenderState, poseStack, ((LivingEntityRenderState)livingEntityRenderState).bodyRot, g);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.scale(livingEntityRenderState, poseStack);
        poseStack.translate(0.0f, -1.501f, 0.0f);
        boolean bl = this.isBodyVisible(livingEntityRenderState);
        boolean bl2 = !bl && !((LivingEntityRenderState)livingEntityRenderState).isInvisibleToPlayer;
        RenderType renderType = this.getRenderType(livingEntityRenderState, bl, bl2, ((EntityRenderState)livingEntityRenderState).appearsGlowing());
        if (renderType != null) {
            int i = LivingEntityRenderer.getOverlayCoords(livingEntityRenderState, this.getWhiteOverlayProgress(livingEntityRenderState));
            int j = bl2 ? 0x26FFFFFF : -1;
            int k = ARGB.multiply(j, this.getModelTint(livingEntityRenderState));
            submitNodeCollector.submitModel(this.model, livingEntityRenderState, poseStack, renderType, ((LivingEntityRenderState)livingEntityRenderState).lightCoords, i, k, (TextureAtlasSprite)null, ((LivingEntityRenderState)livingEntityRenderState).outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
        }
        if (this.shouldRenderLayers(livingEntityRenderState) && !this.layers.isEmpty()) {
            ((Model)this.model).setupAnim(livingEntityRenderState);
            for (RenderLayer<S, M> renderLayer : this.layers) {
                renderLayer.submit(poseStack, submitNodeCollector, ((LivingEntityRenderState)livingEntityRenderState).lightCoords, livingEntityRenderState, ((LivingEntityRenderState)livingEntityRenderState).yRot, ((LivingEntityRenderState)livingEntityRenderState).xRot);
            }
        }
        poseStack.popPose();
        super.submit(livingEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    protected boolean shouldRenderLayers(S livingEntityRenderState) {
        return true;
    }

    protected int getModelTint(S livingEntityRenderState) {
        return -1;
    }

    public abstract Identifier getTextureLocation(S var1);

    protected @Nullable RenderType getRenderType(S livingEntityRenderState, boolean bl, boolean bl2, boolean bl3) {
        Identifier identifier = this.getTextureLocation(livingEntityRenderState);
        if (bl2) {
            return RenderTypes.itemEntityTranslucentCull(identifier);
        }
        if (bl) {
            return ((Model)this.model).renderType(identifier);
        }
        if (bl3) {
            return RenderTypes.outline(identifier);
        }
        return null;
    }

    public static int getOverlayCoords(LivingEntityRenderState livingEntityRenderState, float f) {
        return OverlayTexture.pack(OverlayTexture.u(f), OverlayTexture.v(livingEntityRenderState.hasRedOverlay));
    }

    protected boolean isBodyVisible(S livingEntityRenderState) {
        return !((LivingEntityRenderState)livingEntityRenderState).isInvisible;
    }

    private static float sleepDirectionToRotation(Direction direction) {
        switch (direction) {
            case SOUTH: {
                return 90.0f;
            }
            case WEST: {
                return 0.0f;
            }
            case NORTH: {
                return 270.0f;
            }
            case EAST: {
                return 180.0f;
            }
        }
        return 0.0f;
    }

    protected boolean isShaking(S livingEntityRenderState) {
        return ((LivingEntityRenderState)livingEntityRenderState).isFullyFrozen;
    }

    protected void setupRotations(S livingEntityRenderState, PoseStack poseStack, float f, float g) {
        if (this.isShaking(livingEntityRenderState)) {
            f += (float)(Math.cos((float)Mth.floor(((LivingEntityRenderState)livingEntityRenderState).ageInTicks) * 3.25f) * Math.PI * (double)0.4f);
        }
        if (!((LivingEntityRenderState)livingEntityRenderState).hasPose(Pose.SLEEPING)) {
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - f));
        }
        if (((LivingEntityRenderState)livingEntityRenderState).deathTime > 0.0f) {
            float h = (((LivingEntityRenderState)livingEntityRenderState).deathTime - 1.0f) / 20.0f * 1.6f;
            if ((h = Mth.sqrt(h)) > 1.0f) {
                h = 1.0f;
            }
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(h * this.getFlipDegrees()));
        } else if (((LivingEntityRenderState)livingEntityRenderState).isAutoSpinAttack) {
            poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-90.0f - ((LivingEntityRenderState)livingEntityRenderState).xRot));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(((LivingEntityRenderState)livingEntityRenderState).ageInTicks * -75.0f));
        } else if (((LivingEntityRenderState)livingEntityRenderState).hasPose(Pose.SLEEPING)) {
            Direction direction = ((LivingEntityRenderState)livingEntityRenderState).bedOrientation;
            float i = direction != null ? LivingEntityRenderer.sleepDirectionToRotation(direction) : f;
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(i));
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(this.getFlipDegrees()));
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(270.0f));
        } else if (((LivingEntityRenderState)livingEntityRenderState).isUpsideDown) {
            poseStack.translate(0.0f, (((LivingEntityRenderState)livingEntityRenderState).boundingBoxHeight + 0.1f) / g, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
        }
    }

    protected float getFlipDegrees() {
        return 90.0f;
    }

    protected float getWhiteOverlayProgress(S livingEntityRenderState) {
        return 0.0f;
    }

    protected void scale(S livingEntityRenderState, PoseStack poseStack) {
    }

    @Override
    protected boolean shouldShowName(T livingEntity, double d) {
        boolean bl;
        if (((Entity)livingEntity).isDiscrete()) {
            float f = 32.0f;
            if (d >= 1024.0) {
                return false;
            }
        }
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer localPlayer = minecraft.player;
        boolean bl2 = bl = !((Entity)livingEntity).isInvisibleTo(localPlayer);
        if (livingEntity != localPlayer) {
            PlayerTeam team = ((Entity)livingEntity).getTeam();
            PlayerTeam team2 = localPlayer.getTeam();
            if (team != null) {
                Team.Visibility visibility = ((Team)team).getNameTagVisibility();
                switch (visibility) {
                    case ALWAYS: {
                        return bl;
                    }
                    case NEVER: {
                        return false;
                    }
                    case HIDE_FOR_OTHER_TEAMS: {
                        return team2 == null ? bl : team.isAlliedTo(team2) && (((Team)team).canSeeFriendlyInvisibles() || bl);
                    }
                    case HIDE_FOR_OWN_TEAM: {
                        return team2 == null ? bl : !team.isAlliedTo(team2) && bl;
                    }
                }
                return true;
            }
        }
        return Minecraft.renderNames() && livingEntity != minecraft.getCameraEntity() && bl && !((Entity)livingEntity).isVehicle();
    }

    public boolean isEntityUpsideDown(T livingEntity) {
        Component component = ((Entity)livingEntity).getCustomName();
        return component != null && LivingEntityRenderer.isUpsideDownName(component.getString());
    }

    protected static boolean isUpsideDownName(String string) {
        return "Dinnerbone".equals(string) || "Grumm".equals(string);
    }

    @Override
    protected float getShadowRadius(S livingEntityRenderState) {
        return super.getShadowRadius(livingEntityRenderState) * ((LivingEntityRenderState)livingEntityRenderState).scale;
    }

    @Override
    public void extractRenderState(T livingEntity, S livingEntityRenderState, float f) {
        BlockItem blockItem;
        super.extractRenderState(livingEntity, livingEntityRenderState, f);
        float g = Mth.rotLerp(f, ((LivingEntity)livingEntity).yHeadRotO, ((LivingEntity)livingEntity).yHeadRot);
        ((LivingEntityRenderState)livingEntityRenderState).bodyRot = LivingEntityRenderer.solveBodyRot(livingEntity, g, f);
        ((LivingEntityRenderState)livingEntityRenderState).yRot = Mth.wrapDegrees(g - ((LivingEntityRenderState)livingEntityRenderState).bodyRot);
        ((LivingEntityRenderState)livingEntityRenderState).xRot = ((Entity)livingEntity).getXRot(f);
        ((LivingEntityRenderState)livingEntityRenderState).isUpsideDown = this.isEntityUpsideDown(livingEntity);
        if (((LivingEntityRenderState)livingEntityRenderState).isUpsideDown) {
            ((LivingEntityRenderState)livingEntityRenderState).xRot *= -1.0f;
            ((LivingEntityRenderState)livingEntityRenderState).yRot *= -1.0f;
        }
        if (!((Entity)livingEntity).isPassenger() && ((LivingEntity)livingEntity).isAlive()) {
            ((LivingEntityRenderState)livingEntityRenderState).walkAnimationPos = ((LivingEntity)livingEntity).walkAnimation.position(f);
            ((LivingEntityRenderState)livingEntityRenderState).walkAnimationSpeed = ((LivingEntity)livingEntity).walkAnimation.speed(f);
        } else {
            ((LivingEntityRenderState)livingEntityRenderState).walkAnimationPos = 0.0f;
            ((LivingEntityRenderState)livingEntityRenderState).walkAnimationSpeed = 0.0f;
        }
        Entity entity = ((Entity)livingEntity).getVehicle();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            ((LivingEntityRenderState)livingEntityRenderState).wornHeadAnimationPos = livingEntity2.walkAnimation.position(f);
        } else {
            ((LivingEntityRenderState)livingEntityRenderState).wornHeadAnimationPos = ((LivingEntityRenderState)livingEntityRenderState).walkAnimationPos;
        }
        ((LivingEntityRenderState)livingEntityRenderState).scale = ((LivingEntity)livingEntity).getScale();
        ((LivingEntityRenderState)livingEntityRenderState).ageScale = ((LivingEntity)livingEntity).getAgeScale();
        ((LivingEntityRenderState)livingEntityRenderState).pose = ((Entity)livingEntity).getPose();
        ((LivingEntityRenderState)livingEntityRenderState).bedOrientation = ((LivingEntity)livingEntity).getBedOrientation();
        if (((LivingEntityRenderState)livingEntityRenderState).bedOrientation != null) {
            ((LivingEntityRenderState)livingEntityRenderState).eyeHeight = ((Entity)livingEntity).getEyeHeight(Pose.STANDING);
        }
        ((LivingEntityRenderState)livingEntityRenderState).isFullyFrozen = ((Entity)livingEntity).isFullyFrozen();
        ((LivingEntityRenderState)livingEntityRenderState).isBaby = ((LivingEntity)livingEntity).isBaby();
        ((LivingEntityRenderState)livingEntityRenderState).isInWater = ((Entity)livingEntity).isInWater();
        ((LivingEntityRenderState)livingEntityRenderState).isAutoSpinAttack = ((LivingEntity)livingEntity).isAutoSpinAttack();
        ((LivingEntityRenderState)livingEntityRenderState).ticksSinceKineticHitFeedback = ((LivingEntity)livingEntity).getTicksSinceLastKineticHitFeedback(f);
        ((LivingEntityRenderState)livingEntityRenderState).hasRedOverlay = ((LivingEntity)livingEntity).hurtTime > 0 || ((LivingEntity)livingEntity).deathTime > 0;
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.HEAD);
        FeatureElement featureElement = itemStack.getItem();
        if (featureElement instanceof BlockItem && (featureElement = (blockItem = (BlockItem)featureElement).getBlock()) instanceof AbstractSkullBlock) {
            AbstractSkullBlock abstractSkullBlock = (AbstractSkullBlock)featureElement;
            ((LivingEntityRenderState)livingEntityRenderState).wornHeadType = abstractSkullBlock.getType();
            ((LivingEntityRenderState)livingEntityRenderState).wornHeadProfile = itemStack.get(DataComponents.PROFILE);
            ((LivingEntityRenderState)livingEntityRenderState).headItem.clear();
        } else {
            ((LivingEntityRenderState)livingEntityRenderState).wornHeadType = null;
            ((LivingEntityRenderState)livingEntityRenderState).wornHeadProfile = null;
            if (!HumanoidArmorLayer.shouldRender(itemStack, EquipmentSlot.HEAD)) {
                this.itemModelResolver.updateForLiving(((LivingEntityRenderState)livingEntityRenderState).headItem, itemStack, ItemDisplayContext.HEAD, (LivingEntity)livingEntity);
            } else {
                ((LivingEntityRenderState)livingEntityRenderState).headItem.clear();
            }
        }
        ((LivingEntityRenderState)livingEntityRenderState).deathTime = ((LivingEntity)livingEntity).deathTime > 0 ? (float)((LivingEntity)livingEntity).deathTime + f : 0.0f;
        Minecraft minecraft = Minecraft.getInstance();
        ((LivingEntityRenderState)livingEntityRenderState).isInvisibleToPlayer = ((LivingEntityRenderState)livingEntityRenderState).isInvisible && ((Entity)livingEntity).isInvisibleTo(minecraft.player);
    }

    private static float solveBodyRot(LivingEntity livingEntity, float f, float g) {
        Entity entity = livingEntity.getVehicle();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity2 = (LivingEntity)entity;
            float h = Mth.rotLerp(g, livingEntity2.yBodyRotO, livingEntity2.yBodyRot);
            float i = 85.0f;
            float j = Mth.clamp(Mth.wrapDegrees(f - h), -85.0f, 85.0f);
            h = f - j;
            if (Math.abs(j) > 50.0f) {
                h += j * 0.2f;
            }
            return h;
        }
        return Mth.rotLerp(g, livingEntity.yBodyRotO, livingEntity.yBodyRot);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((S)((LivingEntityRenderState)entityRenderState));
    }
}

