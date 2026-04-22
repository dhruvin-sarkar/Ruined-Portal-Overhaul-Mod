package com.ruinedportaloverhaul.client.render.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ruinedportaloverhaul.client.render.geo.model.PiglinRavagerGeoModel;
import com.ruinedportaloverhaul.entity.PiglinRavagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinRavagerGeoRenderer<R extends RavagerRenderState & GeoRenderState> extends GeoEntityRenderer<PiglinRavagerEntity, R> {
    public PiglinRavagerGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinRavagerGeoModel());
        // The ravager has no native saddle slot, so a dedicated renderer layer mounts the saddle item to the body bone as a visual read.
        this.withRenderLayer(new MountedItemGeoLayer<>(
            this,
            "body",
            EquipmentSlot.CHEST,
            ItemDisplayContext.FIXED,
            entity -> new ItemStack(Items.SADDLE),
            PiglinRavagerGeoRenderer::poseSaddle
        ));
    }

    private static void poseSaddle(PoseStack poseStack, ItemStack stack) {
        // The saddle sits as a flat back-mounted marker, so the pose rotates it onto the ravager's spine and enlarges it for the wider body.
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
        poseStack.translate(0.0f, -0.78f, -0.08f);
        poseStack.scale(1.95f, 1.95f, 1.95f);
    }
}
