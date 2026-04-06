/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 */
package net.minecraft.client.model.object.crystal;

import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState;
import org.joml.Quaternionf;

@Environment(value=EnvType.CLIENT)
public class EndCrystalModel
extends EntityModel<EndCrystalRenderState> {
    private static final String OUTER_GLASS = "outer_glass";
    private static final String INNER_GLASS = "inner_glass";
    private static final String BASE = "base";
    private static final float SIN_45 = (float)Math.sin(0.7853981633974483);
    public final ModelPart base;
    public final ModelPart outerGlass;
    public final ModelPart innerGlass;
    public final ModelPart cube;

    public EndCrystalModel(ModelPart modelPart) {
        super(modelPart);
        this.base = modelPart.getChild(BASE);
        this.outerGlass = modelPart.getChild(OUTER_GLASS);
        this.innerGlass = this.outerGlass.getChild(INNER_GLASS);
        this.cube = this.innerGlass.getChild("cube");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();
        float f = 0.875f;
        CubeListBuilder cubeListBuilder = CubeListBuilder.create().texOffs(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f);
        PartDefinition partDefinition2 = partDefinition.addOrReplaceChild(OUTER_GLASS, cubeListBuilder, PartPose.offset(0.0f, 24.0f, 0.0f));
        PartDefinition partDefinition3 = partDefinition2.addOrReplaceChild(INNER_GLASS, cubeListBuilder, PartPose.ZERO.withScale(0.875f));
        partDefinition3.addOrReplaceChild("cube", CubeListBuilder.create().texOffs(32, 0).addBox(-4.0f, -4.0f, -4.0f, 8.0f, 8.0f, 8.0f), PartPose.ZERO.withScale(0.765625f));
        partDefinition.addOrReplaceChild(BASE, CubeListBuilder.create().texOffs(0, 16).addBox(-6.0f, 0.0f, -6.0f, 12.0f, 4.0f, 12.0f), PartPose.ZERO);
        return LayerDefinition.create(meshDefinition, 64, 32);
    }

    @Override
    public void setupAnim(EndCrystalRenderState endCrystalRenderState) {
        super.setupAnim(endCrystalRenderState);
        this.base.visible = endCrystalRenderState.showsBottom;
        float f = endCrystalRenderState.ageInTicks * 3.0f;
        float g = EndCrystalRenderer.getY(endCrystalRenderState.ageInTicks) * 16.0f;
        this.outerGlass.y += g / 2.0f;
        this.outerGlass.rotateBy(Axis.YP.rotationDegrees(f).rotateAxis(1.0471976f, SIN_45, 0.0f, SIN_45));
        this.innerGlass.rotateBy(new Quaternionf().setAngleAxis(1.0471976f, SIN_45, 0.0f, SIN_45).rotateY(f * ((float)Math.PI / 180)));
        this.cube.rotateBy(new Quaternionf().setAngleAxis(1.0471976f, SIN_45, 0.0f, SIN_45).rotateY(f * ((float)Math.PI / 180)));
    }
}

