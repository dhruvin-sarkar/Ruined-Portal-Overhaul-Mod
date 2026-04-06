/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.chest.ChestModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.util.SpecialDates;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChestRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T, ChestRenderState> {
    private final MaterialSet materials;
    private final ChestModel singleModel;
    private final ChestModel doubleLeftModel;
    private final ChestModel doubleRightModel;
    private final boolean xmasTextures;

    public ChestRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.xmasTextures = ChestRenderer.xmasTextures();
        this.singleModel = new ChestModel(context.bakeLayer(ModelLayers.CHEST));
        this.doubleLeftModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT));
        this.doubleRightModel = new ChestModel(context.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT));
    }

    public static boolean xmasTextures() {
        return SpecialDates.isExtendedChristmas();
    }

    @Override
    public ChestRenderState createRenderState() {
        return new ChestRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, ChestRenderState chestRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        DoubleBlockCombiner.NeighborCombineResult<Object> neighborCombineResult;
        Block block;
        BlockEntityRenderer.super.extractRenderState(blockEntity, chestRenderState, f, vec3, crumblingOverlay);
        boolean bl = ((BlockEntity)blockEntity).getLevel() != null;
        BlockState blockState = bl ? ((BlockEntity)blockEntity).getBlockState() : (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        chestRenderState.type = blockState.hasProperty(ChestBlock.TYPE) ? blockState.getValue(ChestBlock.TYPE) : ChestType.SINGLE;
        chestRenderState.angle = blockState.getValue(ChestBlock.FACING).toYRot();
        chestRenderState.material = this.getChestMaterial((BlockEntity)blockEntity, this.xmasTextures);
        if (bl && (block = blockState.getBlock()) instanceof ChestBlock) {
            ChestBlock chestBlock = (ChestBlock)block;
            neighborCombineResult = chestBlock.combine(blockState, ((BlockEntity)blockEntity).getLevel(), ((BlockEntity)blockEntity).getBlockPos(), true);
        } else {
            neighborCombineResult = DoubleBlockCombiner.Combiner::acceptNone;
        }
        chestRenderState.open = neighborCombineResult.apply(ChestBlock.opennessCombiner((LidBlockEntity)blockEntity)).get(f);
        if (chestRenderState.type != ChestType.SINGLE) {
            chestRenderState.lightCoords = ((Int2IntFunction)neighborCombineResult.apply(new BrightnessCombiner())).applyAsInt(chestRenderState.lightCoords);
        }
    }

    @Override
    public void submit(ChestRenderState chestRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-chestRenderState.angle));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        float f = chestRenderState.open;
        f = 1.0f - f;
        f = 1.0f - f * f * f;
        Material material = Sheets.chooseMaterial(chestRenderState.material, chestRenderState.type);
        RenderType renderType = material.renderType(RenderTypes::entityCutout);
        TextureAtlasSprite textureAtlasSprite = this.materials.get(material);
        if (chestRenderState.type != ChestType.SINGLE) {
            if (chestRenderState.type == ChestType.LEFT) {
                submitNodeCollector.submitModel(this.doubleLeftModel, Float.valueOf(f), poseStack, renderType, chestRenderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, textureAtlasSprite, 0, chestRenderState.breakProgress);
            } else {
                submitNodeCollector.submitModel(this.doubleRightModel, Float.valueOf(f), poseStack, renderType, chestRenderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, textureAtlasSprite, 0, chestRenderState.breakProgress);
            }
        } else {
            submitNodeCollector.submitModel(this.singleModel, Float.valueOf(f), poseStack, renderType, chestRenderState.lightCoords, OverlayTexture.NO_OVERLAY, -1, textureAtlasSprite, 0, chestRenderState.breakProgress);
        }
        poseStack.popPose();
    }

    private ChestRenderState.ChestMaterialType getChestMaterial(BlockEntity blockEntity, boolean bl) {
        if (blockEntity instanceof EnderChestBlockEntity) {
            return ChestRenderState.ChestMaterialType.ENDER_CHEST;
        }
        if (bl) {
            return ChestRenderState.ChestMaterialType.CHRISTMAS;
        }
        if (blockEntity instanceof TrappedChestBlockEntity) {
            return ChestRenderState.ChestMaterialType.TRAPPED;
        }
        Block block = blockEntity.getBlockState().getBlock();
        if (block instanceof CopperChestBlock) {
            CopperChestBlock copperChestBlock = (CopperChestBlock)block;
            return switch (copperChestBlock.getState()) {
                default -> throw new MatchException(null, null);
                case WeatheringCopper.WeatherState.UNAFFECTED -> ChestRenderState.ChestMaterialType.COPPER_UNAFFECTED;
                case WeatheringCopper.WeatherState.EXPOSED -> ChestRenderState.ChestMaterialType.COPPER_EXPOSED;
                case WeatheringCopper.WeatherState.WEATHERED -> ChestRenderState.ChestMaterialType.COPPER_WEATHERED;
                case WeatheringCopper.WeatherState.OXIDIZED -> ChestRenderState.ChestMaterialType.COPPER_OXIDIZED;
            };
        }
        return ChestRenderState.ChestMaterialType.REGULAR;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

