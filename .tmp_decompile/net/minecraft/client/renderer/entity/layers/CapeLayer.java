/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

@Environment(value=EnvType.CLIENT)
public class CapeLayer
extends RenderLayer<AvatarRenderState, PlayerModel> {
    private final HumanoidModel<AvatarRenderState> model;
    private final EquipmentAssetManager equipmentAssets;

    public CapeLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet, EquipmentAssetManager equipmentAssetManager) {
        super(renderLayerParent);
        this.model = new PlayerCapeModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_CAPE));
        this.equipmentAssets = equipmentAssetManager;
    }

    private boolean hasLayer(ItemStack itemStack, EquipmentClientInfo.LayerType layerType) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return false;
        }
        EquipmentClientInfo equipmentClientInfo = this.equipmentAssets.get(equippable.assetId().get());
        return !equipmentClientInfo.getLayers(layerType).isEmpty();
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, AvatarRenderState avatarRenderState, float f, float g) {
        if (avatarRenderState.isInvisible || !avatarRenderState.showCape) {
            return;
        }
        PlayerSkin playerSkin = avatarRenderState.skin;
        if (playerSkin.cape() == null) {
            return;
        }
        if (this.hasLayer(avatarRenderState.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
            return;
        }
        poseStack.pushPose();
        if (this.hasLayer(avatarRenderState.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
            poseStack.translate(0.0f, -0.053125f, 0.06875f);
        }
        submitNodeCollector.submitModel(this.model, avatarRenderState, poseStack, RenderTypes.entitySolid(playerSkin.cape().texturePath()), i, OverlayTexture.NO_OVERLAY, avatarRenderState.outlineColor, null);
        poseStack.popPose();
    }
}

