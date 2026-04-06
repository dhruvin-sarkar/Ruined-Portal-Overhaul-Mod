/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.model.monster.piglin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.piglin.AbstractPiglinModel;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;

@Environment(value=EnvType.CLIENT)
public class ZombifiedPiglinModel
extends AbstractPiglinModel<ZombifiedPiglinRenderState> {
    public ZombifiedPiglinModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(ZombifiedPiglinRenderState zombifiedPiglinRenderState) {
        super.setupAnim(zombifiedPiglinRenderState);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, zombifiedPiglinRenderState.isAggressive, zombifiedPiglinRenderState);
    }

    @Override
    public void setAllVisible(boolean bl) {
        super.setAllVisible(bl);
        this.leftSleeve.visible = bl;
        this.rightSleeve.visible = bl;
        this.leftPants.visible = bl;
        this.rightPants.visible = bl;
        this.jacket.visible = bl;
    }
}

