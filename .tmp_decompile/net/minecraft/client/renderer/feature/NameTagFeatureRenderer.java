/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class NameTagFeatureRenderer {
    public void render(SubmitNodeCollection submitNodeCollection, MultiBufferSource.BufferSource bufferSource, Font font) {
        Storage storage = submitNodeCollection.getNameTagSubmits();
        storage.nameTagSubmitsSeethrough.sort(Comparator.comparing(SubmitNodeStorage.NameTagSubmit::distanceToCameraSq).reversed());
        for (SubmitNodeStorage.NameTagSubmit nameTagSubmit : storage.nameTagSubmitsSeethrough) {
            font.drawInBatch(nameTagSubmit.text(), nameTagSubmit.x(), nameTagSubmit.y(), nameTagSubmit.color(), false, nameTagSubmit.pose(), (MultiBufferSource)bufferSource, Font.DisplayMode.SEE_THROUGH, nameTagSubmit.backgroundColor(), nameTagSubmit.lightCoords());
        }
        for (SubmitNodeStorage.NameTagSubmit nameTagSubmit : storage.nameTagSubmitsNormal) {
            font.drawInBatch(nameTagSubmit.text(), nameTagSubmit.x(), nameTagSubmit.y(), nameTagSubmit.color(), false, nameTagSubmit.pose(), (MultiBufferSource)bufferSource, Font.DisplayMode.NORMAL, nameTagSubmit.backgroundColor(), nameTagSubmit.lightCoords());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class Storage {
        final List<SubmitNodeStorage.NameTagSubmit> nameTagSubmitsSeethrough = new ArrayList<SubmitNodeStorage.NameTagSubmit>();
        final List<SubmitNodeStorage.NameTagSubmit> nameTagSubmitsNormal = new ArrayList<SubmitNodeStorage.NameTagSubmit>();

        public void add(PoseStack poseStack, @Nullable Vec3 vec3, int i, Component component, boolean bl, int j, double d, CameraRenderState cameraRenderState) {
            if (vec3 == null) {
                return;
            }
            Minecraft minecraft = Minecraft.getInstance();
            poseStack.pushPose();
            poseStack.translate(vec3.x, vec3.y + 0.5, vec3.z);
            poseStack.mulPose((Quaternionfc)cameraRenderState.orientation);
            poseStack.scale(0.025f, -0.025f, 0.025f);
            Matrix4f matrix4f = new Matrix4f((Matrix4fc)poseStack.last().pose());
            float f = (float)(-minecraft.font.width(component)) / 2.0f;
            int k = (int)(minecraft.options.getBackgroundOpacity(0.25f) * 255.0f) << 24;
            if (bl) {
                this.nameTagSubmitsNormal.add(new SubmitNodeStorage.NameTagSubmit(matrix4f, f, i, component, LightTexture.lightCoordsWithEmission(j, 2), -1, 0, d));
                this.nameTagSubmitsSeethrough.add(new SubmitNodeStorage.NameTagSubmit(matrix4f, f, i, component, j, -2130706433, k, d));
            } else {
                this.nameTagSubmitsNormal.add(new SubmitNodeStorage.NameTagSubmit(matrix4f, f, i, component, j, -2130706433, k, d));
            }
            poseStack.popPose();
        }

        public void clear() {
            this.nameTagSubmitsNormal.clear();
            this.nameTagSubmitsSeethrough.clear();
        }
    }
}

