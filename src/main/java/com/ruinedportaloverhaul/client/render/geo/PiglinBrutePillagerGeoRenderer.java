package com.ruinedportaloverhaul.client.render.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.ruinedportaloverhaul.client.render.geo.model.PiglinBrutePillagerGeoModel;
import com.ruinedportaloverhaul.entity.PiglinBrutePillagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public final class PiglinBrutePillagerGeoRenderer<R extends IllagerRenderState & GeoRenderState> extends AbstractIllagerGeoRenderer<PiglinBrutePillagerEntity, R> {
    public PiglinBrutePillagerGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new PiglinBrutePillagerGeoModel());
        // GeckoLib's built-in armor layer is disabled in 5.4.5, so the brute's chestplate is mounted through the renderer's body bone instead.
        this.withRenderLayer(new MountedItemGeoLayer<>(
            this,
            "body",
            EquipmentSlot.CHEST,
            ItemDisplayContext.FIXED,
            entity -> entity.getItemBySlot(EquipmentSlot.CHEST),
            PiglinBrutePillagerGeoRenderer::poseChestplate
        ));
    }

    private static void poseChestplate(PoseStack poseStack, net.minecraft.world.item.ItemStack stack) {
        // The chestplate item model renders flat by default, so this pose reorients and scales it to read as worn armor on the torso.
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f));
        poseStack.translate(0.0f, -0.32f, -0.08f);
        poseStack.scale(1.22f, 1.22f, 1.22f);
    }
}
