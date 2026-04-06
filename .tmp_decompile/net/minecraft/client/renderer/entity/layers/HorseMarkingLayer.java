/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.layers;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.equine.Markings;

@Environment(value=EnvType.CLIENT)
public class HorseMarkingLayer
extends RenderLayer<HorseRenderState, HorseModel> {
    private static final Identifier INVISIBLE_TEXTURE = Identifier.withDefaultNamespace("invisible");
    private static final Map<Markings, Identifier> TEXTURE_BY_MARKINGS = Maps.newEnumMap((Map)Map.of((Object)((Object)Markings.NONE), (Object)INVISIBLE_TEXTURE, (Object)((Object)Markings.WHITE), (Object)Identifier.withDefaultNamespace("textures/entity/horse/horse_markings_white.png"), (Object)((Object)Markings.WHITE_FIELD), (Object)Identifier.withDefaultNamespace("textures/entity/horse/horse_markings_whitefield.png"), (Object)((Object)Markings.WHITE_DOTS), (Object)Identifier.withDefaultNamespace("textures/entity/horse/horse_markings_whitedots.png"), (Object)((Object)Markings.BLACK_DOTS), (Object)Identifier.withDefaultNamespace("textures/entity/horse/horse_markings_blackdots.png")));

    public HorseMarkingLayer(RenderLayerParent<HorseRenderState, HorseModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, HorseRenderState horseRenderState, float f, float g) {
        Identifier identifier = TEXTURE_BY_MARKINGS.get((Object)horseRenderState.markings);
        if (identifier == INVISIBLE_TEXTURE || horseRenderState.isInvisible) {
            return;
        }
        submitNodeCollector.order(1).submitModel(this.getParentModel(), horseRenderState, poseStack, RenderTypes.entityTranslucent(identifier), i, LivingEntityRenderer.getOverlayCoords(horseRenderState, 0.0f), -1, (TextureAtlasSprite)null, horseRenderState.outlineColor, (ModelFeatureRenderer.CrumblingOverlay)null);
    }
}

