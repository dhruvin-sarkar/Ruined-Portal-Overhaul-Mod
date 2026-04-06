/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.TrialSpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SpawnerRenderer
implements BlockEntityRenderer<SpawnerBlockEntity, SpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public SpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public SpawnerRenderState createRenderState() {
        return new SpawnerRenderState();
    }

    @Override
    public void extractRenderState(SpawnerBlockEntity spawnerBlockEntity, SpawnerRenderState spawnerRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(spawnerBlockEntity, spawnerRenderState, f, vec3, crumblingOverlay);
        if (spawnerBlockEntity.getLevel() == null) {
            return;
        }
        BaseSpawner baseSpawner = spawnerBlockEntity.getSpawner();
        Entity entity = baseSpawner.getOrCreateDisplayEntity(spawnerBlockEntity.getLevel(), spawnerBlockEntity.getBlockPos());
        TrialSpawnerRenderer.extractSpawnerData(spawnerRenderState, f, entity, this.entityRenderer, baseSpawner.getOSpin(), baseSpawner.getSpin());
    }

    @Override
    public void submit(SpawnerRenderState spawnerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (spawnerRenderState.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(poseStack, submitNodeCollector, spawnerRenderState.displayEntity, this.entityRenderer, spawnerRenderState.spin, spawnerRenderState.scale, cameraRenderState);
        }
    }

    public static void submitEntityInSpawner(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, EntityRenderState entityRenderState, EntityRenderDispatcher entityRenderDispatcher, float f, float g, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        poseStack.translate(0.5f, 0.4f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(f));
        poseStack.translate(0.0f, -0.2f, 0.0f);
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-30.0f));
        poseStack.scale(g, g, g);
        entityRenderDispatcher.submit(entityRenderState, cameraRenderState, 0.0, 0.0, 0.0, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

