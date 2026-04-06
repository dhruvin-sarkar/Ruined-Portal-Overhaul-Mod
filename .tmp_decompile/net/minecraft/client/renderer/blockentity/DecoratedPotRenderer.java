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
import com.mojang.math.Axis;
import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.DecoratedPotRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotPatterns;
import net.minecraft.world.level.block.entity.PotDecorations;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DecoratedPotRenderer
implements BlockEntityRenderer<DecoratedPotBlockEntity, DecoratedPotRenderState> {
    private final MaterialSet materials;
    private static final String NECK = "neck";
    private static final String FRONT = "front";
    private static final String BACK = "back";
    private static final String LEFT = "left";
    private static final String RIGHT = "right";
    private static final String TOP = "top";
    private static final String BOTTOM = "bottom";
    private final ModelPart neck;
    private final ModelPart frontSide;
    private final ModelPart backSide;
    private final ModelPart leftSide;
    private final ModelPart rightSide;
    private final ModelPart top;
    private final ModelPart bottom;
    private static final float WOBBLE_AMPLITUDE = 0.125f;

    public DecoratedPotRenderer(BlockEntityRendererProvider.Context context) {
        this(context.entityModelSet(), context.materials());
    }

    public DecoratedPotRenderer(SpecialModelRenderer.BakingContext bakingContext) {
        this(bakingContext.entityModelSet(), bakingContext.materials());
    }

    public DecoratedPotRenderer(EntityModelSet entityModelSet, MaterialSet materialSet) {
        this.materials = materialSet;
        ModelPart modelPart = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_BASE);
        this.neck = modelPart.getChild(NECK);
        this.top = modelPart.getChild(TOP);
        this.bottom = modelPart.getChild(BOTTOM);
        ModelPart modelPart2 = entityModelSet.bakeLayer(ModelLayers.DECORATED_POT_SIDES);
        this.frontSide = modelPart2.getChild(FRONT);
        this.backSide = modelPart2.getChild(BACK);
        this.leftSide = modelPart2.getChild(LEFT);
        this.rightSide = modelPart2.getChild(RIGHT);
    }

    public static LayerDefinition createBaseLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeDeformation cubeDeformation = new CubeDeformation(0.2f);
        CubeDeformation cubeDeformation2 = new CubeDeformation(-0.1f);
        partDefinition.addOrReplaceChild(NECK, CubeListBuilder.create().texOffs(0, 0).addBox(4.0f, 17.0f, 4.0f, 8.0f, 3.0f, 8.0f, cubeDeformation2).texOffs(0, 5).addBox(5.0f, 20.0f, 5.0f, 6.0f, 1.0f, 6.0f, cubeDeformation), PartPose.offsetAndRotation(0.0f, 37.0f, 16.0f, (float)Math.PI, 0.0f, 0.0f));
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(-14, 13).addBox(0.0f, 0.0f, 0.0f, 14.0f, 0.0f, 14.0f);
        partDefinition.addOrReplaceChild(TOP, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        partDefinition.addOrReplaceChild(BOTTOM, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    public static LayerDefinition createSidesLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(1, 0).addBox(0.0f, 0.0f, 0.0f, 14.0f, 16.0f, 0.0f, EnumSet.of(Direction.NORTH));
        partDefinition.addOrReplaceChild(BACK, cubeListBuilder, PartPose.offsetAndRotation(15.0f, 16.0f, 1.0f, 0.0f, 0.0f, (float)Math.PI));
        partDefinition.addOrReplaceChild(LEFT, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 1.0f, 0.0f, -1.5707964f, (float)Math.PI));
        partDefinition.addOrReplaceChild(RIGHT, cubeListBuilder, PartPose.offsetAndRotation(15.0f, 16.0f, 15.0f, 0.0f, 1.5707964f, (float)Math.PI));
        partDefinition.addOrReplaceChild(FRONT, cubeListBuilder, PartPose.offsetAndRotation(1.0f, 16.0f, 15.0f, (float)Math.PI, 0.0f, 0.0f));
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    private static Material getSideMaterial(Optional<Item> optional) {
        Material material;
        if (optional.isPresent() && (material = Sheets.getDecoratedPotMaterial(DecoratedPotPatterns.getPatternFromItem(optional.get()))) != null) {
            return material;
        }
        return Sheets.DECORATED_POT_SIDE;
    }

    @Override
    public DecoratedPotRenderState createRenderState() {
        return new DecoratedPotRenderState();
    }

    @Override
    public void extractRenderState(DecoratedPotBlockEntity decoratedPotBlockEntity, DecoratedPotRenderState decoratedPotRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(decoratedPotBlockEntity, decoratedPotRenderState, f, vec3, crumblingOverlay);
        decoratedPotRenderState.decorations = decoratedPotBlockEntity.getDecorations();
        decoratedPotRenderState.direction = decoratedPotBlockEntity.getDirection();
        DecoratedPotBlockEntity.WobbleStyle wobbleStyle = decoratedPotBlockEntity.lastWobbleStyle;
        decoratedPotRenderState.wobbleProgress = wobbleStyle != null && decoratedPotBlockEntity.getLevel() != null ? ((float)(decoratedPotBlockEntity.getLevel().getGameTime() - decoratedPotBlockEntity.wobbleStartedAtTick) + f) / (float)wobbleStyle.duration : 0.0f;
    }

    @Override
    public void submit(DecoratedPotRenderState decoratedPotRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        Direction direction = decoratedPotRenderState.direction;
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - direction.toYRot()));
        poseStack.translate(-0.5, 0.0, -0.5);
        if (decoratedPotRenderState.wobbleProgress >= 0.0f && decoratedPotRenderState.wobbleProgress <= 1.0f) {
            if (decoratedPotRenderState.wobbleStyle == DecoratedPotBlockEntity.WobbleStyle.POSITIVE) {
                float f = 0.015625f;
                float g = decoratedPotRenderState.wobbleProgress * ((float)Math.PI * 2);
                float h = -1.5f * (Mth.cos(g) + 0.5f) * Mth.sin(g / 2.0f);
                poseStack.rotateAround((Quaternionfc)Axis.XP.rotation(h * 0.015625f), 0.5f, 0.0f, 0.5f);
                float i = Mth.sin(g);
                poseStack.rotateAround((Quaternionfc)Axis.ZP.rotation(i * 0.015625f), 0.5f, 0.0f, 0.5f);
            } else {
                float f = Mth.sin(-decoratedPotRenderState.wobbleProgress * 3.0f * (float)Math.PI) * 0.125f;
                float g = 1.0f - decoratedPotRenderState.wobbleProgress;
                poseStack.rotateAround((Quaternionfc)Axis.YP.rotation(f * g), 0.5f, 0.0f, 0.5f);
            }
        }
        this.submit(poseStack, submitNodeCollector, decoratedPotRenderState.lightCoords, OverlayTexture.NO_OVERLAY, decoratedPotRenderState.decorations, 0);
        poseStack.popPose();
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, PotDecorations potDecorations, int k) {
        RenderType renderType = Sheets.DECORATED_POT_BASE.renderType(RenderTypes::entitySolid);
        TextureAtlasSprite textureAtlasSprite = this.materials.get(Sheets.DECORATED_POT_BASE);
        submitNodeCollector.submitModelPart(this.neck, poseStack, renderType, i, j, textureAtlasSprite, false, false, -1, null, k);
        submitNodeCollector.submitModelPart(this.top, poseStack, renderType, i, j, textureAtlasSprite, false, false, -1, null, k);
        submitNodeCollector.submitModelPart(this.bottom, poseStack, renderType, i, j, textureAtlasSprite, false, false, -1, null, k);
        Material material = DecoratedPotRenderer.getSideMaterial(potDecorations.front());
        submitNodeCollector.submitModelPart(this.frontSide, poseStack, material.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material), false, false, -1, null, k);
        Material material2 = DecoratedPotRenderer.getSideMaterial(potDecorations.back());
        submitNodeCollector.submitModelPart(this.backSide, poseStack, material2.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material2), false, false, -1, null, k);
        Material material3 = DecoratedPotRenderer.getSideMaterial(potDecorations.left());
        submitNodeCollector.submitModelPart(this.leftSide, poseStack, material3.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material3), false, false, -1, null, k);
        Material material4 = DecoratedPotRenderer.getSideMaterial(potDecorations.right());
        submitNodeCollector.submitModelPart(this.rightSide, poseStack, material4.renderType(RenderTypes::entitySolid), i, j, this.materials.get(material4), false, false, -1, null, k);
    }

    public void getExtents(Consumer<Vector3fc> consumer) {
        PoseStack poseStack = new PoseStack();
        this.neck.getExtentsForGui(poseStack, consumer);
        this.top.getExtentsForGui(poseStack, consumer);
        this.bottom.getExtentsForGui(poseStack, consumer);
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

