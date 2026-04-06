/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.object.banner;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Unit;

@Environment(value=EnvType.CLIENT)
public class BannerModel
extends Model<Unit> {
    public static final int BANNER_WIDTH = 20;
    public static final int BANNER_HEIGHT = 40;
    public static final String FLAG = "flag";
    private static final String POLE = "pole";
    private static final String BAR = "bar";

    public BannerModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entitySolid);
    }

    public static LayerDefinition createBodyLayer(boolean bl) {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        if (bl) {
            partDefinition.addOrReplaceChild(POLE, CubeListBuilder.create().texOffs(44, 0).addBox(-1.0f, -42.0f, -1.0f, 2.0f, 42.0f, 2.0f), PartPose.ZERO);
        }
        partDefinition.addOrReplaceChild(BAR, CubeListBuilder.create().texOffs(0, 42).addBox(-10.0f, bl ? -44.0f : -20.5f, bl ? -1.0f : 9.5f, 20.0f, 2.0f, 2.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 64);
    }
}

