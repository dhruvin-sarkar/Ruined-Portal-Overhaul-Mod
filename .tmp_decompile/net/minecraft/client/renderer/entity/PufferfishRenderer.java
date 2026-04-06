/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.fish.PufferfishBigModel;
import net.minecraft.client.model.animal.fish.PufferfishMidModel;
import net.minecraft.client.model.animal.fish.PufferfishSmallModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PufferfishRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.fish.Pufferfish;

@Environment(value=EnvType.CLIENT)
public class PufferfishRenderer
extends MobRenderer<Pufferfish, PufferfishRenderState, EntityModel<EntityRenderState>> {
    private static final Identifier PUFFER_LOCATION = Identifier.withDefaultNamespace("textures/entity/fish/pufferfish.png");
    private final EntityModel<EntityRenderState> small;
    private final EntityModel<EntityRenderState> mid;
    private final EntityModel<EntityRenderState> big = this.getModel();

    public PufferfishRenderer(EntityRendererProvider.Context context) {
        super(context, new PufferfishBigModel(context.bakeLayer(ModelLayers.PUFFERFISH_BIG)), 0.2f);
        this.mid = new PufferfishMidModel(context.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
        this.small = new PufferfishSmallModel(context.bakeLayer(ModelLayers.PUFFERFISH_SMALL));
    }

    @Override
    public Identifier getTextureLocation(PufferfishRenderState pufferfishRenderState) {
        return PUFFER_LOCATION;
    }

    @Override
    public PufferfishRenderState createRenderState() {
        return new PufferfishRenderState();
    }

    @Override
    protected float getShadowRadius(PufferfishRenderState pufferfishRenderState) {
        return 0.1f + 0.1f * (float)pufferfishRenderState.puffState;
    }

    @Override
    public void submit(PufferfishRenderState pufferfishRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        this.model = switch (pufferfishRenderState.puffState) {
            case 0 -> this.small;
            case 1 -> this.mid;
            default -> this.big;
        };
        super.submit(pufferfishRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    public void extractRenderState(Pufferfish pufferfish, PufferfishRenderState pufferfishRenderState, float f) {
        super.extractRenderState(pufferfish, pufferfishRenderState, f);
        pufferfishRenderState.puffState = pufferfish.getPuffState();
    }

    @Override
    protected void setupRotations(PufferfishRenderState pufferfishRenderState, PoseStack poseStack, float f, float g) {
        poseStack.translate(0.0f, Mth.cos(pufferfishRenderState.ageInTicks * 0.05f) * 0.08f, 0.0f);
        super.setupRotations(pufferfishRenderState, poseStack, f, g);
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntityRenderState livingEntityRenderState) {
        return this.getShadowRadius((PufferfishRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PufferfishRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((PufferfishRenderState)entityRenderState);
    }
}

