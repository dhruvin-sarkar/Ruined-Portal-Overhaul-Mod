/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.axolotl.AxolotlModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.AxolotlRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.animal.axolotl.Axolotl;

@Environment(value=EnvType.CLIENT)
public class AxolotlRenderer
extends AgeableMobRenderer<Axolotl, AxolotlRenderState, AxolotlModel> {
    private static final Map<Axolotl.Variant, Identifier> TEXTURE_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        for (Axolotl.Variant variant : Axolotl.Variant.values()) {
            hashMap.put(variant, Identifier.withDefaultNamespace(String.format(Locale.ROOT, "textures/entity/axolotl/axolotl_%s.png", variant.getName())));
        }
    });

    public AxolotlRenderer(EntityRendererProvider.Context context) {
        super(context, new AxolotlModel(context.bakeLayer(ModelLayers.AXOLOTL)), new AxolotlModel(context.bakeLayer(ModelLayers.AXOLOTL_BABY)), 0.5f);
    }

    @Override
    public Identifier getTextureLocation(AxolotlRenderState axolotlRenderState) {
        return TEXTURE_BY_TYPE.get(axolotlRenderState.variant);
    }

    @Override
    public AxolotlRenderState createRenderState() {
        return new AxolotlRenderState();
    }

    @Override
    public void extractRenderState(Axolotl axolotl, AxolotlRenderState axolotlRenderState, float f) {
        super.extractRenderState(axolotl, axolotlRenderState, f);
        axolotlRenderState.variant = axolotl.getVariant();
        axolotlRenderState.playingDeadFactor = axolotl.playingDeadAnimator.getFactor(f);
        axolotlRenderState.inWaterFactor = axolotl.inWaterAnimator.getFactor(f);
        axolotlRenderState.onGroundFactor = axolotl.onGroundAnimator.getFactor(f);
        axolotlRenderState.movingFactor = axolotl.movingAnimator.getFactor(f);
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((AxolotlRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

