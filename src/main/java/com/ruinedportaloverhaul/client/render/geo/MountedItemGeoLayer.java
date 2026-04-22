package com.ruinedportaloverhaul.client.render.geo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.model.BakedGeoModel;
import software.bernie.geckolib.cache.model.GeoBone;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.renderer.base.GeoRenderer;
import software.bernie.geckolib.renderer.layer.builtin.BlockAndItemGeoLayer;

final class MountedItemGeoLayer<T extends LivingEntity & GeoAnimatable, R extends GeoRenderState> extends BlockAndItemGeoLayer<T, Void, R> {
    private final EquipmentSlot slotKey;
    private final Function<T, ItemStack> itemResolver;
    private final ItemPoseTransformer poseTransformer;
    private final List<RenderData<R>> relevantBones;

    MountedItemGeoLayer(GeoRenderer<T, Void, R> renderer, String boneName, EquipmentSlot slotKey, ItemDisplayContext displayContext,
                        Function<T, ItemStack> itemResolver, ItemPoseTransformer poseTransformer) {
        super(renderer);
        this.slotKey = slotKey;
        this.itemResolver = itemResolver;
        this.poseTransformer = poseTransformer;
        this.relevantBones = List.of(new RenderData<>(
            boneName,
            displayContext,
            // GeckoLib stores equipment data in an erased map, so the stack has to be cast back before Either infers the correct item branch.
            (bone, renderState) -> Either.left((ItemStack)renderState.getGeckolibData(DataTickets.EQUIPMENT_BY_SLOT).getOrDefault(this.slotKey, ItemStack.EMPTY))
        ));
    }

    @Override
    protected List<RenderData<R>> getRelevantBones(R renderState, BakedGeoModel model) {
        // GeckoLib 5.4.5 ships a disabled armor layer, so this project-owned layer targets the requested bone directly instead.
        return this.relevantBones;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void addRenderData(T animatable, @Nullable Void relatedObject, R renderState, float partialTick) {
        // GeckoLib discards the live entity before per-bone rendering, so the mounted stack has to be persisted into render-state data first.
        EnumMap<EquipmentSlot, ItemStack> equipment = renderState.getOrDefaultGeckolibData(
            DataTickets.EQUIPMENT_BY_SLOT,
            (Supplier<EnumMap>) () -> new EnumMap<>(EquipmentSlot.class)
        );
        equipment.put(this.slotKey, this.itemResolver.apply(animatable));
        renderState.addGeckolibData(DataTickets.EQUIPMENT_BY_SLOT, equipment);
    }

    @Override
    protected void submitItemStackRender(PoseStack poseStack, GeoBone bone, ItemStack stack, ItemDisplayContext displayContext, R renderState,
                                         SubmitNodeCollector renderTasks, CameraRenderState cameraState, int packedLight, int packedOverlay, int renderColor) {
        // The renderer needs a custom torso/back pose here because these items are being mounted to bones instead of held in a hand.
        poseStack.pushPose();
        this.poseTransformer.transform(poseStack, stack);
        super.submitItemStackRender(poseStack, bone, stack, displayContext, renderState, renderTasks, cameraState, packedLight, packedOverlay, renderColor);
        poseStack.popPose();
    }

    @FunctionalInterface
    interface ItemPoseTransformer {
        void transform(PoseStack poseStack, ItemStack stack);
    }
}
