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
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.rendertype.RenderType;

@Environment(value=EnvType.CLIENT)
public class CustomFeatureRenderer {
    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource) {
        Storage storage = submitNodeCollection.getCustomGeometrySubmits();
        for (Map.Entry<RenderType, List<SubmitNodeStorage.CustomGeometrySubmit>> entry : storage.customGeometrySubmits.entrySet()) {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(entry.getKey());
            for (SubmitNodeStorage.CustomGeometrySubmit customGeometrySubmit : entry.getValue()) {
                customGeometrySubmit.customGeometryRenderer().render(customGeometrySubmit.pose(), vertexConsumer);
            }
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Storage {
        final Map<RenderType, List<SubmitNodeStorage.CustomGeometrySubmit>> customGeometrySubmits = new HashMap<RenderType, List<SubmitNodeStorage.CustomGeometrySubmit>>();
        private final Set<RenderType> customGeometrySubmitsUsage = new ObjectOpenHashSet();

        public void add(PoseStack poseStack, RenderType renderType2, SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) {
            List list = this.customGeometrySubmits.computeIfAbsent(renderType2, renderType -> new ArrayList());
            list.add(new SubmitNodeStorage.CustomGeometrySubmit(poseStack.last().copy(), customGeometryRenderer));
        }

        public void clear() {
            for (Map.Entry<RenderType, List<SubmitNodeStorage.CustomGeometrySubmit>> entry : this.customGeometrySubmits.entrySet()) {
                if (entry.getValue().isEmpty()) continue;
                this.customGeometrySubmitsUsage.add(entry.getKey());
                entry.getValue().clear();
            }
        }

        public void endFrame() {
            this.customGeometrySubmits.keySet().removeIf(renderType -> !this.customGeometrySubmitsUsage.contains(renderType));
            this.customGeometrySubmitsUsage.clear();
        }
    }
}

