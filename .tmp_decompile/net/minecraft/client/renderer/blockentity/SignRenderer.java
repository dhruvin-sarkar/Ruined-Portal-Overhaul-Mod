/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class SignRenderer
extends AbstractSignRenderer {
    public static final float RENDER_SCALE = 0.6666667f;
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.3333333432674408, 0.046666666865348816);
    private final Map<WoodType, Models> signModels = (Map)WoodType.values().collect(ImmutableMap.toImmutableMap(woodType -> woodType, woodType -> new Models(SignRenderer.createSignModel(context.entityModelSet(), woodType, true), SignRenderer.createSignModel(context.entityModelSet(), woodType, false))));

    public SignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Model.Simple getSignModel(BlockState blockState, WoodType woodType) {
        Models models = this.signModels.get((Object)woodType);
        return blockState.getBlock() instanceof StandingSignBlock ? models.standing() : models.wall();
    }

    @Override
    protected Material getSignMaterial(WoodType woodType) {
        return Sheets.getSignMaterial(woodType);
    }

    @Override
    protected float getSignModelRenderScale() {
        return 0.6666667f;
    }

    @Override
    protected float getSignTextRenderScale() {
        return 0.6666667f;
    }

    private static void translateBase(PoseStack poseStack, float f) {
        poseStack.translate(0.5f, 0.5f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
    }

    @Override
    protected void translateSign(PoseStack poseStack, float f, BlockState blockState) {
        SignRenderer.translateBase(poseStack, f);
        if (!(blockState.getBlock() instanceof StandingSignBlock)) {
            poseStack.translate(0.0f, -0.3125f, -0.4375f);
        }
    }

    @Override
    protected Vec3 getTextOffset() {
        return TEXT_OFFSET;
    }

    public static void submitSpecial(MaterialSet materialSet, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, Model.Simple simple, Material material) {
        poseStack.pushPose();
        SignRenderer.applyInHandTransforms(poseStack);
        submitNodeCollector.submitModel(simple, Unit.INSTANCE, poseStack, material.renderType(simple::renderType), i, j, -1, materialSet.get(material), 0, null);
        poseStack.popPose();
    }

    public static void applyInHandTransforms(PoseStack poseStack) {
        SignRenderer.translateBase(poseStack, 0.0f);
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
    }

    public static Model.Simple createSignModel(EntityModelSet entityModelSet, WoodType woodType, boolean bl) {
        ModelLayerLocation modelLayerLocation = bl ? ModelLayers.createStandingSignModelName(woodType) : ModelLayers.createWallSignModelName(woodType);
        return new Model.Simple(entityModelSet.bakeLayer(modelLayerLocation), RenderTypes::entityCutoutNoCull);
    }

    public static LayerDefinition createSignLayer(boolean bl) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0f, -14.0f, -1.0f, 24.0f, 12.0f, 2.0f), PartPose.ZERO);
        if (bl) {
            partDefinition.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 14.0f, 2.0f), PartPose.ZERO);
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    record Models(Model.Simple standing, Model.Simple wall) {
    }
}

