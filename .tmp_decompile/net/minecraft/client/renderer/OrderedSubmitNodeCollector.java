/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface OrderedSubmitNodeCollector {
    public void submitShadow(PoseStack var1, float var2, List<EntityRenderState.ShadowPiece> var3);

    public void submitNameTag(PoseStack var1, @Nullable Vec3 var2, int var3, Component var4, boolean var5, int var6, double var7, CameraRenderState var9);

    public void submitText(PoseStack var1, float var2, float var3, FormattedCharSequence var4, boolean var5, Font.DisplayMode var6, int var7, int var8, int var9, int var10);

    public void submitFlame(PoseStack var1, EntityRenderState var2, Quaternionf var3);

    public void submitLeash(PoseStack var1, EntityRenderState.LeashState var2);

    public <S> void submitModel(Model<? super S> var1, S var2, PoseStack var3, RenderType var4, int var5, int var6, int var7, @Nullable TextureAtlasSprite var8, int var9,  @Nullable ModelFeatureRenderer.CrumblingOverlay var10);

    default public <S> void submitModel(Model<? super S> model, S object, PoseStack poseStack, RenderType renderType, int i, int j, int k,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        this.submitModel(model, object, poseStack, renderType, i, j, -1, null, k, crumblingOverlay);
    }

    default public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite) {
        this.submitModelPart(modelPart, poseStack, renderType, i, j, textureAtlasSprite, false, false, -1, null, 0);
    }

    default public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, int k,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        this.submitModelPart(modelPart, poseStack, renderType, i, j, textureAtlasSprite, false, false, k, crumblingOverlay, 0);
    }

    default public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int i, int j, @Nullable TextureAtlasSprite textureAtlasSprite, boolean bl, boolean bl2) {
        this.submitModelPart(modelPart, poseStack, renderType, i, j, textureAtlasSprite, bl, bl2, -1, null, 0);
    }

    public void submitModelPart(ModelPart var1, PoseStack var2, RenderType var3, int var4, int var5, @Nullable TextureAtlasSprite var6, boolean var7, boolean var8, int var9,  @Nullable ModelFeatureRenderer.CrumblingOverlay var10, int var11);

    public void submitBlock(PoseStack var1, BlockState var2, int var3, int var4, int var5);

    public void submitMovingBlock(PoseStack var1, MovingBlockRenderState var2);

    public void submitBlockModel(PoseStack var1, RenderType var2, BlockStateModel var3, float var4, float var5, float var6, int var7, int var8, int var9);

    public void submitItem(PoseStack var1, ItemDisplayContext var2, int var3, int var4, int var5, int[] var6, List<BakedQuad> var7, RenderType var8, ItemStackRenderState.FoilType var9);

    public void submitCustomGeometry(PoseStack var1, RenderType var2, SubmitNodeCollector.CustomGeometryRenderer var3);

    public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer var1);
}

