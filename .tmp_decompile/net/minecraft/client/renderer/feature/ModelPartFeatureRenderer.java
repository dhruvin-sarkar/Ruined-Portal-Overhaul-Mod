/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.ModelBakery;

@Environment(value=EnvType.CLIENT)
public class ModelPartFeatureRenderer {
    private final PoseStack poseStack = new PoseStack();

    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, MultiBufferSource.BufferSource bufferSource2) {
        Storage storage = submitNodeCollection.getModelPartSubmits();
        for (Map.Entry<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> entry : storage.modelPartSubmits.entrySet()) {
            RenderType renderType = entry.getKey();
            List<SubmitNodeStorage.ModelPartSubmit> list = entry.getValue();
            VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
            for (SubmitNodeStorage.ModelPartSubmit modelPartSubmit : list) {
                VertexConsumer vertexConsumer3;
                VertexConsumer vertexConsumer2 = modelPartSubmit.sprite() != null ? (modelPartSubmit.hasFoil() ? modelPartSubmit.sprite().wrap(ItemRenderer.getFoilBuffer(bufferSource, renderType, modelPartSubmit.sheeted(), true)) : modelPartSubmit.sprite().wrap(vertexConsumer)) : (modelPartSubmit.hasFoil() ? ItemRenderer.getFoilBuffer(bufferSource, renderType, modelPartSubmit.sheeted(), true) : vertexConsumer);
                this.poseStack.last().set(modelPartSubmit.pose());
                modelPartSubmit.modelPart().render(this.poseStack, vertexConsumer2, modelPartSubmit.lightCoords(), modelPartSubmit.overlayCoords(), modelPartSubmit.tintedColor());
                if (modelPartSubmit.outlineColor() != 0 && (renderType.outline().isPresent() || renderType.isOutline())) {
                    outlineBufferSource.setColor(modelPartSubmit.outlineColor());
                    vertexConsumer3 = outlineBufferSource.getBuffer(renderType);
                    modelPartSubmit.modelPart().render(this.poseStack, modelPartSubmit.sprite() == null ? vertexConsumer3 : modelPartSubmit.sprite().wrap(vertexConsumer3), modelPartSubmit.lightCoords(), modelPartSubmit.overlayCoords(), modelPartSubmit.tintedColor());
                }
                if (modelPartSubmit.crumblingOverlay() == null) continue;
                vertexConsumer3 = new SheetedDecalTextureGenerator(bufferSource2.getBuffer(ModelBakery.DESTROY_TYPES.get(modelPartSubmit.crumblingOverlay().progress())), modelPartSubmit.crumblingOverlay().cameraPose(), 1.0f);
                modelPartSubmit.modelPart().render(this.poseStack, vertexConsumer3, modelPartSubmit.lightCoords(), modelPartSubmit.overlayCoords(), modelPartSubmit.tintedColor());
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Storage {
        final Map<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> modelPartSubmits = new HashMap<RenderType, List<SubmitNodeStorage.ModelPartSubmit>>();
        private final Set<RenderType> modelPartSubmitsUsage = new ObjectOpenHashSet();

        public void add(RenderType renderType2, SubmitNodeStorage.ModelPartSubmit modelPartSubmit) {
            this.modelPartSubmits.computeIfAbsent(renderType2, renderType -> new ArrayList()).add(modelPartSubmit);
        }

        public void clear() {
            for (Map.Entry<RenderType, List<SubmitNodeStorage.ModelPartSubmit>> entry : this.modelPartSubmits.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                this.modelPartSubmitsUsage.add(entry.getKey());
                entry.getValue().clear();
            }
        }

        public void endFrame() {
            this.modelPartSubmits.keySet().removeIf(renderType -> !this.modelPartSubmitsUsage.contains(renderType));
            this.modelPartSubmitsUsage.clear();
        }
    }
}

