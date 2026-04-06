/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TrialSpawnerRenderer
implements BlockEntityRenderer<TrialSpawnerBlockEntity, SpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public TrialSpawnerRenderer(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public SpawnerRenderState createRenderState() {
        return new SpawnerRenderState();
    }

    @Override
    public void extractRenderState(TrialSpawnerBlockEntity trialSpawnerBlockEntity, SpawnerRenderState spawnerRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(trialSpawnerBlockEntity, spawnerRenderState, f, vec3, crumblingOverlay);
        if (trialSpawnerBlockEntity.getLevel() == null) {
            return;
        }
        TrialSpawner trialSpawner = trialSpawnerBlockEntity.getTrialSpawner();
        TrialSpawnerStateData trialSpawnerStateData = trialSpawner.getStateData();
        Entity entity = trialSpawnerStateData.getOrCreateDisplayEntity(trialSpawner, trialSpawnerBlockEntity.getLevel(), trialSpawner.getState());
        TrialSpawnerRenderer.extractSpawnerData(spawnerRenderState, f, entity, this.entityRenderer, trialSpawnerStateData.getOSpin(), trialSpawnerStateData.getSpin());
    }

    static void extractSpawnerData(SpawnerRenderState spawnerRenderState, float f, @Nullable Entity entity, EntityRenderDispatcher entityRenderDispatcher, double d, double e) {
        if (entity == null) {
            return;
        }
        spawnerRenderState.displayEntity = entityRenderDispatcher.extractEntity(entity, f);
        spawnerRenderState.displayEntity.lightCoords = spawnerRenderState.lightCoords;
        spawnerRenderState.spin = (float)Mth.lerp((double)f, d, e) * 10.0f;
        spawnerRenderState.scale = 0.53125f;
        float g = Math.max(entity.getBbWidth(), entity.getBbHeight());
        if ((double)g > 1.0) {
            spawnerRenderState.scale /= g;
        }
    }

    @Override
    public void submit(SpawnerRenderState spawnerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (spawnerRenderState.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(poseStack, submitNodeCollector, spawnerRenderState.displayEntity, this.entityRenderer, spawnerRenderState.spin, spawnerRenderState.scale, cameraRenderState);
        }
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

