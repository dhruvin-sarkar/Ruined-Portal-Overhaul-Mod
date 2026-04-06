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
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EvokerRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.illager.SpellcasterIllager;

@Environment(value=EnvType.CLIENT)
public class EvokerRenderer<T extends SpellcasterIllager>
extends IllagerRenderer<T, EvokerRenderState> {
    private static final Identifier EVOKER_ILLAGER = Identifier.withDefaultNamespace("textures/entity/illager/evoker.png");

    public EvokerRenderer(EntityRendererProvider.Context context) {
        super(context, new IllagerModel(context.bakeLayer(ModelLayers.EVOKER)), 0.5f);
        this.addLayer(new ItemInHandLayer<EvokerRenderState, IllagerModel<EvokerRenderState>>(this, (RenderLayerParent)this){

            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, EvokerRenderState evokerRenderState, float f, float g) {
                if (evokerRenderState.isCastingSpell) {
                    super.submit(poseStack, submitNodeCollector, i, evokerRenderState, f, g);
                }
            }
        });
    }

    @Override
    public Identifier getTextureLocation(EvokerRenderState evokerRenderState) {
        return EVOKER_ILLAGER;
    }

    @Override
    public EvokerRenderState createRenderState() {
        return new EvokerRenderState();
    }

    @Override
    public void extractRenderState(T spellcasterIllager, EvokerRenderState evokerRenderState, float f) {
        super.extractRenderState(spellcasterIllager, evokerRenderState, f);
        evokerRenderState.isCastingSpell = ((SpellcasterIllager)spellcasterIllager).isCastingSpell();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((EvokerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

