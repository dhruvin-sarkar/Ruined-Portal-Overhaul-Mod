/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.DragonHeadModel;
import net.minecraft.client.model.object.skull.PiglinHeadModel;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.SkullBlockRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkullBlockRenderer
implements BlockEntityRenderer<SkullBlockEntity, SkullBlockRenderState> {
    private final Function<SkullBlock.Type, SkullModelBase> modelByType;
    private static final Map<SkullBlock.Type, Identifier> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(SkullBlock.Types.SKELETON, Identifier.withDefaultNamespace("textures/entity/skeleton/skeleton.png"));
        hashMap.put(SkullBlock.Types.WITHER_SKELETON, Identifier.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png"));
        hashMap.put(SkullBlock.Types.ZOMBIE, Identifier.withDefaultNamespace("textures/entity/zombie/zombie.png"));
        hashMap.put(SkullBlock.Types.CREEPER, Identifier.withDefaultNamespace("textures/entity/creeper/creeper.png"));
        hashMap.put(SkullBlock.Types.DRAGON, Identifier.withDefaultNamespace("textures/entity/enderdragon/dragon.png"));
        hashMap.put(SkullBlock.Types.PIGLIN, Identifier.withDefaultNamespace("textures/entity/piglin/piglin.png"));
        hashMap.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultTexture());
    });
    private final PlayerSkinRenderCache playerSkinRenderCache;

    public static @Nullable SkullModelBase createModel(EntityModelSet entityModelSet, SkullBlock.Type type) {
        if (type instanceof SkullBlock.Types) {
            SkullBlock.Types types = (SkullBlock.Types)type;
            return switch (types) {
                default -> throw new MatchException(null, null);
                case SkullBlock.Types.SKELETON -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.SKELETON_SKULL));
                case SkullBlock.Types.WITHER_SKELETON -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL));
                case SkullBlock.Types.PLAYER -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_HEAD));
                case SkullBlock.Types.ZOMBIE -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.ZOMBIE_HEAD));
                case SkullBlock.Types.CREEPER -> new SkullModel(entityModelSet.bakeLayer(ModelLayers.CREEPER_HEAD));
                case SkullBlock.Types.DRAGON -> new DragonHeadModel(entityModelSet.bakeLayer(ModelLayers.DRAGON_SKULL));
                case SkullBlock.Types.PIGLIN -> new PiglinHeadModel(entityModelSet.bakeLayer(ModelLayers.PIGLIN_HEAD));
            };
        }
        return null;
    }

    public SkullBlockRenderer(BlockEntityRendererProvider.Context context) {
        EntityModelSet entityModelSet = context.entityModelSet();
        this.playerSkinRenderCache = context.playerSkinRenderCache();
        this.modelByType = Util.memoize(type -> SkullBlockRenderer.createModel(entityModelSet, type));
    }

    @Override
    public SkullBlockRenderState createRenderState() {
        return new SkullBlockRenderState();
    }

    @Override
    public void extractRenderState(SkullBlockEntity skullBlockEntity, SkullBlockRenderState skullBlockRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(skullBlockEntity, skullBlockRenderState, f, vec3, crumblingOverlay);
        skullBlockRenderState.animationProgress = skullBlockEntity.getAnimation(f);
        BlockState blockState = skullBlockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        skullBlockRenderState.direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
        int i = bl ? RotationSegment.convertToSegment(skullBlockRenderState.direction.getOpposite()) : blockState.getValue(SkullBlock.ROTATION);
        skullBlockRenderState.rotationDegrees = RotationSegment.convertToDegrees(i);
        skullBlockRenderState.skullType = ((AbstractSkullBlock)blockState.getBlock()).getType();
        skullBlockRenderState.renderType = this.resolveSkullRenderType(skullBlockRenderState.skullType, skullBlockEntity);
    }

    @Override
    public void submit(SkullBlockRenderState skullBlockRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        SkullModelBase skullModelBase = this.modelByType.apply(skullBlockRenderState.skullType);
        SkullBlockRenderer.submitSkull(skullBlockRenderState.direction, skullBlockRenderState.rotationDegrees, skullBlockRenderState.animationProgress, poseStack, submitNodeCollector, skullBlockRenderState.lightCoords, skullModelBase, skullBlockRenderState.renderType, 0, skullBlockRenderState.breakProgress);
    }

    public static void submitSkull(@Nullable Direction direction, float f, float g, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, SkullModelBase skullModelBase, RenderType renderType, int j,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        poseStack.pushPose();
        if (direction == null) {
            poseStack.translate(0.5f, 0.0f, 0.5f);
        } else {
            float h = 0.25f;
            poseStack.translate(0.5f - (float)direction.getStepX() * 0.25f, 0.25f, 0.5f - (float)direction.getStepZ() * 0.25f);
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        SkullModelBase.State state = new SkullModelBase.State();
        state.animationPos = g;
        state.yRot = f;
        submitNodeCollector.submitModel(skullModelBase, state, poseStack, renderType, i, OverlayTexture.NO_OVERLAY, j, crumblingOverlay);
        poseStack.popPose();
    }

    private RenderType resolveSkullRenderType(SkullBlock.Type type, SkullBlockEntity skullBlockEntity) {
        ResolvableProfile resolvableProfile;
        if (type == SkullBlock.Types.PLAYER && (resolvableProfile = skullBlockEntity.getOwnerProfile()) != null) {
            return this.playerSkinRenderCache.getOrDefault(resolvableProfile).renderType();
        }
        return SkullBlockRenderer.getSkullRenderType(type, null);
    }

    public static RenderType getSkullRenderType(SkullBlock.Type type, @Nullable Identifier identifier) {
        return RenderTypes.entityCutoutNoCullZOffset(identifier != null ? identifier : SKIN_BY_TYPE.get(type));
    }

    public static RenderType getPlayerSkinRenderType(Identifier identifier) {
        return RenderTypes.entityTranslucent(identifier);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

