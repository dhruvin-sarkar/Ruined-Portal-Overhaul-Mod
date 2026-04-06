/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 */
package net.minecraft.client.model.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class PlayerCapeModel
extends PlayerModel {
    private static final String CAPE = "cape";
    private final ModelPart cape;

    public PlayerCapeModel(ModelPart modelPart) {
        super(modelPart, false);
        this.cape = this.body.getChild(CAPE);
    }

    public static LayerDefinition createCapeLayer() {
        MeshDefinition meshDefinition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition partDefinition = meshDefinition.getRoot().clearRecursively();
        PartDefinition partDefinition2 = partDefinition.getChild("body");
        partDefinition2.addOrReplaceChild(CAPE, CubeListBuilder.create().texOffs(0, 0).addBox(-5.0f, 0.0f, -1.0f, 10.0f, 16.0f, 1.0f, CubeDeformation.NONE, 1.0f, 0.5f), PartPose.offsetAndRotation(0.0f, 0.0f, 2.0f, 0.0f, (float)Math.PI, 0.0f));
        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(AvatarRenderState avatarRenderState) {
        super.setupAnim(avatarRenderState);
        this.cape.rotateBy(new Quaternionf().rotateY((float)(-Math.PI)).rotateX((6.0f + avatarRenderState.capeLean / 2.0f + avatarRenderState.capeFlap) * ((float)Math.PI / 180)).rotateZ(avatarRenderState.capeLean2 / 2.0f * ((float)Math.PI / 180)).rotateY((180.0f - avatarRenderState.capeLean2 / 2.0f) * ((float)Math.PI / 180)));
    }
}

