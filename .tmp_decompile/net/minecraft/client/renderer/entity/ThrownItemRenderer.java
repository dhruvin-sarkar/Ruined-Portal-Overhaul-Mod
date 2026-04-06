/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity>
extends EntityRenderer<T, ThrownItemRenderState> {
    private final ItemModelResolver itemModelResolver;
    private final float scale;
    private final boolean fullBright;

    public ThrownItemRenderer(EntityRendererProvider.Context context, float f, boolean bl) {
        super(context);
        this.itemModelResolver = context.getItemModelResolver();
        this.scale = f;
        this.fullBright = bl;
    }

    public ThrownItemRenderer(EntityRendererProvider.Context context) {
        this(context, 1.0f, false);
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos blockPos) {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, blockPos);
    }

    @Override
    public void submit(ThrownItemRenderState thrownItemRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, this.scale);
        poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
        thrownItemRenderState.item.submit(poseStack, submitNodeCollector, thrownItemRenderState.lightCoords, OverlayTexture.NO_OVERLAY, thrownItemRenderState.outlineColor);
        poseStack.popPose();
        super.submit(thrownItemRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public ThrownItemRenderState createRenderState() {
        return new ThrownItemRenderState();
    }

    @Override
    public void extractRenderState(T entity, ThrownItemRenderState thrownItemRenderState, float f) {
        super.extractRenderState(entity, thrownItemRenderState, f);
        this.itemModelResolver.updateForNonLiving(thrownItemRenderState.item, ((ItemSupplier)entity).getItem(), ItemDisplayContext.GROUND, (Entity)entity);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

