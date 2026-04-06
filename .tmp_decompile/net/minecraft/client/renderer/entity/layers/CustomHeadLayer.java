/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class CustomHeadLayer<S extends LivingEntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private static final float ITEM_SCALE = 0.625f;
    private static final float SKULL_SCALE = 1.1875f;
    private final Transforms transforms;
    private final Function<SkullBlock.Type, SkullModelBase> skullModels;
    private final PlayerSkinRenderCache playerSkinRenderCache;

    public CustomHeadLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, PlayerSkinRenderCache playerSkinRenderCache) {
        this(renderLayerParent, entityModelSet, playerSkinRenderCache, Transforms.DEFAULT);
    }

    public CustomHeadLayer(RenderLayerParent<S, M> renderLayerParent, EntityModelSet entityModelSet, PlayerSkinRenderCache playerSkinRenderCache, Transforms transforms) {
        super(renderLayerParent);
        this.transforms = transforms;
        this.skullModels = Util.memoize(type -> SkullBlockRenderer.createModel(entityModelSet, type));
        this.playerSkinRenderCache = playerSkinRenderCache;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S livingEntityRenderState, float f, float g) {
        if (((LivingEntityRenderState)livingEntityRenderState).headItem.isEmpty() && ((LivingEntityRenderState)livingEntityRenderState).wornHeadType == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(this.transforms.horizontalScale(), 1.0f, this.transforms.horizontalScale());
        Object entityModel = this.getParentModel();
        ((Model)entityModel).root().translateAndRotate(poseStack);
        ((HeadedModel)entityModel).translateToHead(poseStack);
        if (((LivingEntityRenderState)livingEntityRenderState).wornHeadType != null) {
            poseStack.translate(0.0f, this.transforms.skullYOffset(), 0.0f);
            poseStack.scale(1.1875f, -1.1875f, -1.1875f);
            poseStack.translate(-0.5, 0.0, -0.5);
            SkullBlock.Type type = ((LivingEntityRenderState)livingEntityRenderState).wornHeadType;
            SkullModelBase skullModelBase = this.skullModels.apply(type);
            RenderType renderType = this.resolveSkullRenderType((LivingEntityRenderState)livingEntityRenderState, type);
            SkullBlockRenderer.submitSkull(null, 180.0f, ((LivingEntityRenderState)livingEntityRenderState).wornHeadAnimationPos, poseStack, submitNodeCollector, i, skullModelBase, renderType, ((LivingEntityRenderState)livingEntityRenderState).outlineColor, null);
        } else {
            CustomHeadLayer.translateToHead(poseStack, this.transforms);
            ((LivingEntityRenderState)livingEntityRenderState).headItem.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, ((LivingEntityRenderState)livingEntityRenderState).outlineColor);
        }
        poseStack.popPose();
    }

    private RenderType resolveSkullRenderType(LivingEntityRenderState livingEntityRenderState, SkullBlock.Type type) {
        ResolvableProfile resolvableProfile;
        if (type == SkullBlock.Types.PLAYER && (resolvableProfile = livingEntityRenderState.wornHeadProfile) != null) {
            return this.playerSkinRenderCache.getOrDefault(resolvableProfile).renderType();
        }
        return SkullBlockRenderer.getSkullRenderType(type, null);
    }

    public static void translateToHead(PoseStack poseStack, Transforms transforms) {
        poseStack.translate(0.0f, -0.25f + transforms.yOffset(), 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
    }

    @Environment(value=EnvType.CLIENT)
    public record Transforms(float yOffset, float skullYOffset, float horizontalScale) {
        public static final Transforms DEFAULT = new Transforms(0.0f, 0.0f, 1.0f);
    }
}

