/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.object.statue;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;

@Environment(value=EnvType.CLIENT)
public class CopperGolemStatueModel
extends Model<Direction> {
    public CopperGolemStatueModel(ModelPart modelPart) {
        super(modelPart, RenderTypes::entityCutoutNoCull);
    }

    @Override
    public void setupAnim(Direction direction) {
        this.root.y = 0.0f;
        this.root.yRot = direction.getOpposite().toYRot() * ((float)Math.PI / 180);
        this.root.zRot = (float)Math.PI;
    }
}

