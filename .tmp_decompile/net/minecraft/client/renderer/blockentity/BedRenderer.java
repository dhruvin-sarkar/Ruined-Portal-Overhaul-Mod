/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntFunction
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BedRenderer
implements BlockEntityRenderer<BedBlockEntity, BedRenderState> {
    private final MaterialSet materials;
    private final Model.Simple headModel;
    private final Model.Simple footModel;

    public BedRenderer(BlockEntityRendererProvider.Context context) {
        this(context.materials(), context.entityModelSet());
    }

    public BedRenderer(SpecialModelRenderer.BakingContext bakingContext) {
        this(bakingContext.materials(), bakingContext.entityModelSet());
    }

    public BedRenderer(MaterialSet materialSet, EntityModelSet entityModelSet) {
        this.materials = materialSet;
        this.headModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_HEAD), RenderTypes::entitySolid);
        this.footModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_FOOT), RenderTypes::entitySolid);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 6).addBox(0.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 1.5707964f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 18).addBox(-16.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, (float)Math.PI));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f), PartPose.ZERO);
        partDefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 0).addBox(0.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 12).addBox(-16.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f), PartPose.rotation(1.5707964f, 0.0f, 4.712389f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public BedRenderState createRenderState() {
        return new BedRenderState();
    }

    @Override
    public void extractRenderState(BedBlockEntity bedBlockEntity, BedRenderState bedRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(bedBlockEntity, bedRenderState, f, vec3, crumblingOverlay);
        bedRenderState.color = bedBlockEntity.getColor();
        bedRenderState.facing = (Direction)bedBlockEntity.getBlockState().getValue(BedBlock.FACING);
        boolean bl = bedRenderState.isHead = bedBlockEntity.getBlockState().getValue(BedBlock.PART) == BedPart.HEAD;
        if (bedBlockEntity.getLevel() != null) {
            DoubleBlockCombiner.NeighborCombineResult<BedBlockEntity> neighborCombineResult = DoubleBlockCombiner.combineWithNeigbour(BlockEntityType.BED, BedBlock::getBlockType, BedBlock::getConnectedDirection, ChestBlock.FACING, bedBlockEntity.getBlockState(), bedBlockEntity.getLevel(), bedBlockEntity.getBlockPos(), (levelAccessor, blockPos) -> false);
            bedRenderState.lightCoords = ((Int2IntFunction)neighborCombineResult.apply(new BrightnessCombiner())).get(bedRenderState.lightCoords);
        }
    }

    @Override
    public void submit(BedRenderState bedRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Material material = Sheets.getBedMaterial(bedRenderState.color);
        this.submitPiece(poseStack, submitNodeCollector, bedRenderState.isHead ? this.headModel : this.footModel, bedRenderState.facing, material, bedRenderState.lightCoords, OverlayTexture.NO_OVERLAY, false, bedRenderState.breakProgress, 0);
    }

    public void submitSpecial(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, Material material, int k) {
        this.submitPiece(poseStack, submitNodeCollector, this.headModel, Direction.SOUTH, material, i, j, false, null, k);
        this.submitPiece(poseStack, submitNodeCollector, this.footModel, Direction.SOUTH, material, i, j, true, null, k);
    }

    private void submitPiece(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, Model.Simple simple, Direction direction, Material material, int i, int j, boolean bl,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int k) {
        poseStack.pushPose();
        BedRenderer.preparePose(poseStack, bl, direction);
        submitNodeCollector.submitModel(simple, Unit.INSTANCE, poseStack, material.renderType(RenderTypes::entitySolid), i, j, -1, this.materials.get(material), k, crumblingOverlay);
        poseStack.popPose();
    }

    private static void preparePose(PoseStack poseStack, boolean bl, Direction direction) {
        poseStack.translate(0.0f, 0.5625f, bl ? -1.0f : 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f + direction.toYRot()));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

    public void getExtents(Consumer<Vector3fc> consumer) {
        PoseStack poseStack = new PoseStack();
        BedRenderer.preparePose(poseStack, false, Direction.SOUTH);
        this.headModel.root().getExtentsForGui(poseStack, consumer);
        poseStack.setIdentity();
        BedRenderer.preparePose(poseStack, true, Direction.SOUTH);
        this.footModel.root().getExtentsForGui(poseStack, consumer);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

