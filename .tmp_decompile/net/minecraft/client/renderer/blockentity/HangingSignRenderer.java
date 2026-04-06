/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
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
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class HangingSignRenderer
extends AbstractSignRenderer {
    private static final String PLANK = "plank";
    private static final String V_CHAINS = "vChains";
    private static final String NORMAL_CHAINS = "normalChains";
    private static final String CHAIN_L_1 = "chainL1";
    private static final String CHAIN_L_2 = "chainL2";
    private static final String CHAIN_R_1 = "chainR1";
    private static final String CHAIN_R_2 = "chainR2";
    private static final String BOARD = "board";
    public static final float MODEL_RENDER_SCALE = 1.0f;
    private static final float TEXT_RENDER_SCALE = 0.9f;
    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, -0.32f, 0.073f);
    private final Map<ModelKey, Model.Simple> hangingSignModels;

    public HangingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        Stream stream = WoodType.values().flatMap(woodType -> Arrays.stream(AttachmentType.values()).map(attachmentType -> new ModelKey((WoodType)((Object)woodType), (AttachmentType)attachmentType)));
        this.hangingSignModels = (Map)stream.collect(ImmutableMap.toImmutableMap(modelKey -> modelKey, modelKey -> HangingSignRenderer.createSignModel(context.entityModelSet(), modelKey.woodType, modelKey.attachmentType)));
    }

    public static Model.Simple createSignModel(EntityModelSet entityModelSet, WoodType woodType, AttachmentType attachmentType) {
        return new Model.Simple(entityModelSet.bakeLayer(ModelLayers.createHangingSignModelName(woodType, attachmentType)), RenderTypes::entityCutoutNoCull);
    }

    @Override
    protected float getSignModelRenderScale() {
        return 1.0f;
    }

    @Override
    protected float getSignTextRenderScale() {
        return 0.9f;
    }

    public static void translateBase(PoseStack poseStack, float f) {
        poseStack.translate(0.5, 0.9375, 0.5);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
        poseStack.translate(0.0f, -0.3125f, 0.0f);
    }

    @Override
    protected void translateSign(PoseStack poseStack, float f, BlockState blockState) {
        HangingSignRenderer.translateBase(poseStack, f);
    }

    @Override
    protected Model.Simple getSignModel(BlockState blockState, WoodType woodType) {
        AttachmentType attachmentType = AttachmentType.byBlockState(blockState);
        return this.hangingSignModels.get((Object)new ModelKey(woodType, attachmentType));
    }

    @Override
    protected Material getSignMaterial(WoodType woodType) {
        return Sheets.getHangingSignMaterial(woodType);
    }

    @Override
    protected Vec3 getTextOffset() {
        return TEXT_OFFSET;
    }

    public static void submitSpecial(MaterialSet materialSet, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, Model.Simple simple, Material material) {
        poseStack.pushPose();
        HangingSignRenderer.translateBase(poseStack, 0.0f);
        poseStack.scale(1.0f, -1.0f, -1.0f);
        submitNodeCollector.submitModel(simple, Unit.INSTANCE, poseStack, material.renderType(simple::renderType), i, j, -1, materialSet.get(material), OverlayTexture.NO_OVERLAY, null);
        poseStack.popPose();
    }

    public static LayerDefinition createHangingSignLayer(AttachmentType attachmentType) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        partDefinition.addOrReplaceChild(BOARD, CubeListBuilder.create().texOffs(0, 12).addBox(-7.0f, 0.0f, -1.0f, 14.0f, 10.0f, 2.0f), PartPose.ZERO);
        if (attachmentType == AttachmentType.WALL) {
            partDefinition.addOrReplaceChild(PLANK, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -6.0f, -2.0f, 16.0f, 2.0f, 4.0f), PartPose.ZERO);
        }
        if (attachmentType == AttachmentType.WALL || attachmentType == AttachmentType.CEILING) {
            PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(NORMAL_CHAINS, CubeListBuilder.create(), PartPose.ZERO);
            partDefinition2.addOrReplaceChild(CHAIN_L_1, CubeListBuilder.create().texOffs(0, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(-5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
            partDefinition2.addOrReplaceChild(CHAIN_L_2, CubeListBuilder.create().texOffs(6, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(-5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
            partDefinition2.addOrReplaceChild(CHAIN_R_1, CubeListBuilder.create().texOffs(0, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
            partDefinition2.addOrReplaceChild(CHAIN_R_2, CubeListBuilder.create().texOffs(6, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        }
        if (attachmentType == AttachmentType.CEILING_MIDDLE) {
            partDefinition.addOrReplaceChild(V_CHAINS, CubeListBuilder.create().texOffs(14, 6).addBox(-6.0f, -6.0f, 0.0f, 12.0f, 6.0f, 0.0f), PartPose.ZERO);
        }
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum AttachmentType implements StringRepresentable
    {
        WALL("wall"),
        CEILING("ceiling"),
        CEILING_MIDDLE("ceiling_middle");

        private final String name;

        private AttachmentType(String string2) {
            this.name = string2;
        }

        public static AttachmentType byBlockState(BlockState blockState) {
            if (blockState.getBlock() instanceof CeilingHangingSignBlock) {
                return blockState.getValue(BlockStateProperties.ATTACHED) != false ? CEILING_MIDDLE : CEILING;
            }
            return WALL;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static final class ModelKey
    extends Record {
        final WoodType woodType;
        final AttachmentType attachmentType;

        public ModelKey(WoodType woodType, AttachmentType attachmentType) {
            this.woodType = woodType;
            this.attachmentType = attachmentType;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ModelKey.class, "woodType;attachmentType", "woodType", "attachmentType"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ModelKey.class, "woodType;attachmentType", "woodType", "attachmentType"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ModelKey.class, "woodType;attachmentType", "woodType", "attachmentType"}, this, object);
        }

        public WoodType woodType() {
            return this.woodType;
        }

        public AttachmentType attachmentType() {
            return this.attachmentType;
        }
    }
}

