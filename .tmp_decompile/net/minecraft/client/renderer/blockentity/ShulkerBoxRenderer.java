/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.ShulkerBoxRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ShulkerBoxRenderer
implements BlockEntityRenderer<ShulkerBoxBlockEntity, ShulkerBoxRenderState> {
    private final MaterialSet materials;
    private final ShulkerBoxModel model;

    public ShulkerBoxRenderer(BlockEntityRendererProvider.Context context) {
        this(context.entityModelSet(), context.materials());
    }

    public ShulkerBoxRenderer(SpecialModelRenderer.BakingContext bakingContext) {
        this(bakingContext.entityModelSet(), bakingContext.materials());
    }

    public ShulkerBoxRenderer(EntityModelSet entityModelSet, MaterialSet materialSet) {
        this.materials = materialSet;
        this.model = new ShulkerBoxModel(entityModelSet.bakeLayer(ModelLayers.SHULKER_BOX));
    }

    @Override
    public ShulkerBoxRenderState createRenderState() {
        return new ShulkerBoxRenderState();
    }

    @Override
    public void extractRenderState(ShulkerBoxBlockEntity shulkerBoxBlockEntity, ShulkerBoxRenderState shulkerBoxRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(shulkerBoxBlockEntity, shulkerBoxRenderState, f, vec3, crumblingOverlay);
        shulkerBoxRenderState.direction = shulkerBoxBlockEntity.getBlockState().getValueOrElse(ShulkerBoxBlock.FACING, Direction.UP);
        shulkerBoxRenderState.color = shulkerBoxBlockEntity.getColor();
        shulkerBoxRenderState.progress = shulkerBoxBlockEntity.getProgress(f);
    }

    @Override
    public void submit(ShulkerBoxRenderState shulkerBoxRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        DyeColor dyeColor = shulkerBoxRenderState.color;
        Material material = dyeColor == null ? Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION : Sheets.getShulkerBoxMaterial(dyeColor);
        this.submit(poseStack, submitNodeCollector, shulkerBoxRenderState.lightCoords, OverlayTexture.NO_OVERLAY, shulkerBoxRenderState.direction, shulkerBoxRenderState.progress, shulkerBoxRenderState.breakProgress, material, 0);
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, Direction direction, float f,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, Material material, int k) {
        poseStack.pushPose();
        this.prepareModel(poseStack, direction, f);
        submitNodeCollector.submitModel(this.model, Float.valueOf(f), poseStack, material.renderType(this.model::renderType), i, j, -1, this.materials.get(material), k, crumblingOverlay);
        poseStack.popPose();
    }

    private void prepareModel(PoseStack poseStack, Direction direction, float f) {
        poseStack.translate(0.5f, 0.5f, 0.5f);
        float g = 0.9995f;
        poseStack.scale(0.9995f, 0.9995f, 0.9995f);
        poseStack.mulPose((Quaternionfc)direction.getRotation());
        poseStack.scale(1.0f, -1.0f, -1.0f);
        poseStack.translate(0.0f, -1.0f, 0.0f);
        this.model.setupAnim(Float.valueOf(f));
    }

    public void getExtents(Direction direction, float f, Consumer<Vector3fc> consumer) {
        PoseStack poseStack = new PoseStack();
        this.prepareModel(poseStack, direction, f);
        this.model.root().getExtentsForGui(poseStack, consumer);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Environment(value=EnvType.CLIENT)
    static class ShulkerBoxModel
    extends Model<Float> {
        private final ModelPart lid;

        public ShulkerBoxModel(ModelPart modelPart) {
            super(modelPart, RenderTypes::entityCutoutNoCull);
            this.lid = modelPart.getChild("lid");
        }

        @Override
        public void setupAnim(Float float_) {
            super.setupAnim(float_);
            this.lid.setPos(0.0f, 24.0f - float_.floatValue() * 0.5f * 16.0f, 0.0f);
            this.lid.yRot = 270.0f * float_.floatValue() * ((float)Math.PI / 180);
        }
    }
}

