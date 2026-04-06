/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.animal.wolf.WolfModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.WolfRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

@Environment(value=EnvType.CLIENT)
public class WolfArmorLayer
extends RenderLayer<WolfRenderState, WolfModel> {
    private final WolfModel adultModel;
    private final WolfModel babyModel;
    private final EquipmentLayerRenderer equipmentRenderer;
    private static final Map<Crackiness.Level, Identifier> ARMOR_CRACK_LOCATIONS = Map.of((Object)((Object)Crackiness.Level.LOW), (Object)Identifier.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_low.png"), (Object)((Object)Crackiness.Level.MEDIUM), (Object)Identifier.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_medium.png"), (Object)((Object)Crackiness.Level.HIGH), (Object)Identifier.withDefaultNamespace("textures/entity/wolf/wolf_armor_crackiness_high.png"));

    public WolfArmorLayer(RenderLayerParent<WolfRenderState, WolfModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentLayerRenderer equipmentLayerRenderer) {
        super(renderLayerParent);
        this.adultModel = new WolfModel(entityModelSet.bakeLayer(ModelLayers.WOLF_ARMOR));
        this.babyModel = new WolfModel(entityModelSet.bakeLayer(ModelLayers.WOLF_BABY_ARMOR));
        this.equipmentRenderer = equipmentLayerRenderer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, WolfRenderState wolfRenderState, float f, float g) {
        ItemStack itemStack = wolfRenderState.bodyArmorItem;
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return;
        }
        WolfModel wolfModel = wolfRenderState.isBaby ? this.babyModel : this.adultModel;
        this.equipmentRenderer.renderLayers(EquipmentClientInfo.LayerType.WOLF_BODY, equippable.assetId().get(), wolfModel, wolfRenderState, itemStack, poseStack, submitNodeCollector, i, wolfRenderState.outlineColor);
        this.maybeRenderCracks(poseStack, submitNodeCollector, i, itemStack, wolfModel, wolfRenderState);
    }

    private void maybeRenderCracks(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, ItemStack itemStack, Model<WolfRenderState> model, WolfRenderState wolfRenderState) {
        Crackiness.Level level = Crackiness.WOLF_ARMOR.byDamage(itemStack);
        if (level == Crackiness.Level.NONE) {
            return;
        }
        Identifier identifier = ARMOR_CRACK_LOCATIONS.get((Object)level);
        submitNodeCollector.submitModel(model, wolfRenderState, poseStack, RenderTypes.armorTranslucent(identifier), i, OverlayTexture.NO_OVERLAY, wolfRenderState.outlineColor, null);
    }
}

