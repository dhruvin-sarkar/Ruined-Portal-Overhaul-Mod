/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Arrays;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.IllusionerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class IllusionerRenderer
extends IllagerRenderer<Illusioner, IllusionerRenderState> {
    private static final Identifier ILLUSIONER = Identifier.withDefaultNamespace("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.ILLUSIONER)), 0.5f);
        this.addLayer(new ItemInHandLayer<IllusionerRenderState, IllagerModel<IllusionerRenderState>>(this, (RenderLayerParent)this){

            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, IllusionerRenderState illusionerRenderState, float f, float g) {
                if (illusionerRenderState.isCastingSpell || illusionerRenderState.isAggressive) {
                    super.submit(poseStack, submitNodeCollector, i, illusionerRenderState, f, g);
                }
            }
        });
        ((IllagerModel)this.model).getHat().visible = true;
    }

    @Override
    public Identifier getTextureLocation(IllusionerRenderState illusionerRenderState) {
        return ILLUSIONER;
    }

    @Override
    public IllusionerRenderState createRenderState() {
        return new IllusionerRenderState();
    }

    @Override
    public void extractRenderState(Illusioner illusioner, IllusionerRenderState illusionerRenderState, float f) {
        super.extractRenderState(illusioner, illusionerRenderState, f);
        Vec3[] vec3s = illusioner.getIllusionOffsets(f);
        illusionerRenderState.illusionOffsets = Arrays.copyOf(vec3s, vec3s.length);
        illusionerRenderState.isCastingSpell = illusioner.isCastingSpell();
    }

    @Override
    public void submit(IllusionerRenderState illusionerRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (illusionerRenderState.isInvisible) {
            Vec3[] vec3s = illusionerRenderState.illusionOffsets;
            for (int i = 0; i < vec3s.length; ++i) {
                poseStack.pushPose();
                poseStack.translate(vec3s[i].x + (double)Mth.cos((float)i + illusionerRenderState.ageInTicks * 0.5f) * 0.025, vec3s[i].y + (double)Mth.cos((float)i + illusionerRenderState.ageInTicks * 0.75f) * 0.0125, vec3s[i].z + (double)Mth.cos((float)i + illusionerRenderState.ageInTicks * 0.7f) * 0.025);
                super.submit(illusionerRenderState, poseStack, submitNodeCollector, cameraRenderState);
                poseStack.popPose();
            }
        } else {
            super.submit(illusionerRenderState, poseStack, submitNodeCollector, cameraRenderState);
        }
    }

    @Override
    protected boolean isBodyVisible(IllusionerRenderState illusionerRenderState) {
        return true;
    }

    @Override
    protected AABB getBoundingBoxForCulling(Illusioner illusioner) {
        return super.getBoundingBoxForCulling(illusioner).inflate(3.0, 0.0, 3.0);
    }

    @Override
    protected /* synthetic */ boolean isBodyVisible(LivingEntityRenderState livingEntityRenderState) {
        return this.isBodyVisible((IllusionerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((IllusionerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

