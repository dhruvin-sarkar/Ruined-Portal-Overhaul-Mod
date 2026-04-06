/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MaterialMapper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.CondiutRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ConduitRenderer
implements BlockEntityRenderer<ConduitBlockEntity, CondiutRenderState> {
    public static final MaterialMapper MAPPER = new MaterialMapper(TextureAtlas.LOCATION_BLOCKS, "entity/conduit");
    public static final Material SHELL_TEXTURE = MAPPER.defaultNamespaceApply("base");
    public static final Material ACTIVE_SHELL_TEXTURE = MAPPER.defaultNamespaceApply("cage");
    public static final Material WIND_TEXTURE = MAPPER.defaultNamespaceApply("wind");
    public static final Material VERTICAL_WIND_TEXTURE = MAPPER.defaultNamespaceApply("wind_vertical");
    public static final Material OPEN_EYE_TEXTURE = MAPPER.defaultNamespaceApply("open_eye");
    public static final Material CLOSED_EYE_TEXTURE = MAPPER.defaultNamespaceApply("closed_eye");
    private final MaterialSet materials;
    private final ModelPart eye;
    private final ModelPart wind;
    private final ModelPart shell;
    private final ModelPart cage;

    public ConduitRenderer(BlockEntityRendererProvider.Context context) {
        this.materials = context.materials();
        this.eye = context.bakeLayer(ModelLayers.CONDUIT_EYE);
        this.wind = context.bakeLayer(ModelLayers.CONDUIT_WIND);
        this.shell = context.bakeLayer(ModelLayers.CONDUIT_SHELL);
        this.cage = context.bakeLayer(ModelLayers.CONDUIT_CAGE);
    }

    public static LayerDefinition createEyeLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, 0.0f, 8.0f, 8.0f, 0.0f, new CubeDeformation(0.01f)), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    public static LayerDefinition createWindLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("wind", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -8.0f, -8.0f, 16.0f, 16.0f, 16.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    public static LayerDefinition createShellLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0f, -3.0f, -3.0f, 6.0f, 6.0f, 6.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 32, 16);
    }

    public static LayerDefinition createCageLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 32, 16);
    }

    @Override
    public CondiutRenderState createRenderState() {
        return new CondiutRenderState();
    }

    @Override
    public void extractRenderState(ConduitBlockEntity conduitBlockEntity, CondiutRenderState condiutRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(conduitBlockEntity, condiutRenderState, f, vec3, crumblingOverlay);
        condiutRenderState.isActive = conduitBlockEntity.isActive();
        condiutRenderState.activeRotation = conduitBlockEntity.getActiveRotation(conduitBlockEntity.isActive() ? f : 0.0f);
        condiutRenderState.animTime = (float)conduitBlockEntity.tickCount + f;
        condiutRenderState.animationPhase = conduitBlockEntity.tickCount / 66 % 3;
        condiutRenderState.isHunting = conduitBlockEntity.isHunting();
    }

    @Override
    public void submit(CondiutRenderState condiutRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!condiutRenderState.isActive) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.mulPose((Quaternionfc)new Quaternionf().rotationY(condiutRenderState.activeRotation * ((float)Math.PI / 180)));
            submitNodeCollector.submitModelPart(this.shell, poseStack, SHELL_TEXTURE.renderType(RenderTypes::entitySolid), condiutRenderState.lightCoords, OverlayTexture.NO_OVERLAY, this.materials.get(SHELL_TEXTURE), -1, condiutRenderState.breakProgress);
            poseStack.popPose();
            return;
        }
        float f = condiutRenderState.activeRotation * 57.295776f;
        float g = Mth.sin(condiutRenderState.animTime * 0.1f) / 2.0f + 0.5f;
        g = g * g + g;
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.3f + g * 0.2f, 0.5f);
        Vector3f vector3f = new Vector3f(0.5f, 1.0f, 0.5f).normalize();
        poseStack.mulPose((Quaternionfc)new Quaternionf().rotationAxis(f * ((float)Math.PI / 180), (Vector3fc)vector3f));
        submitNodeCollector.submitModelPart(this.cage, poseStack, ACTIVE_SHELL_TEXTURE.renderType(RenderTypes::entityCutoutNoCull), condiutRenderState.lightCoords, OverlayTexture.NO_OVERLAY, this.materials.get(ACTIVE_SHELL_TEXTURE), -1, condiutRenderState.breakProgress);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        if (condiutRenderState.animationPhase == 1) {
            poseStack.mulPose((Quaternionfc)new Quaternionf().rotationX(1.5707964f));
        } else if (condiutRenderState.animationPhase == 2) {
            poseStack.mulPose((Quaternionfc)new Quaternionf().rotationZ(1.5707964f));
        }
        Material material = condiutRenderState.animationPhase == 1 ? VERTICAL_WIND_TEXTURE : WIND_TEXTURE;
        RenderType renderType = material.renderType(RenderTypes::entityCutoutNoCull);
        TextureAtlasSprite textureAtlasSprite = this.materials.get(material);
        submitNodeCollector.submitModelPart(this.wind, poseStack, renderType, condiutRenderState.lightCoords, OverlayTexture.NO_OVERLAY, textureAtlasSprite);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.scale(0.875f, 0.875f, 0.875f);
        poseStack.mulPose((Quaternionfc)new Quaternionf().rotationXYZ((float)Math.PI, 0.0f, (float)Math.PI));
        submitNodeCollector.submitModelPart(this.wind, poseStack, renderType, condiutRenderState.lightCoords, OverlayTexture.NO_OVERLAY, textureAtlasSprite);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.3f + g * 0.2f, 0.5f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
        poseStack.mulPose((Quaternionfc)new Quaternionf().rotationZ((float)Math.PI).rotateY((float)Math.PI));
        float h = 1.3333334f;
        poseStack.scale(1.3333334f, 1.3333334f, 1.3333334f);
        Material material2 = condiutRenderState.isHunting ? OPEN_EYE_TEXTURE : CLOSED_EYE_TEXTURE;
        submitNodeCollector.submitModelPart(this.eye, poseStack, material2.renderType(RenderTypes::entityCutoutNoCull), condiutRenderState.lightCoords, OverlayTexture.NO_OVERLAY, this.materials.get(material2));
        poseStack.popPose();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

