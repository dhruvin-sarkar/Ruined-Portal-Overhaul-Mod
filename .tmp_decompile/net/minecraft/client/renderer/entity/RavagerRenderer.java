/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.ravager.RavagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.RavagerRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Ravager;

@Environment(value=EnvType.CLIENT)
public class RavagerRenderer
extends MobRenderer<Ravager, RavagerRenderState, RavagerModel> {
    private static final Identifier TEXTURE_LOCATION = Identifier.withDefaultNamespace("textures/entity/illager/ravager.png");

    public RavagerRenderer(EntityRendererProvider.Context context) {
        super(context, new RavagerModel(context.bakeLayer(ModelLayers.RAVAGER)), 1.1f);
    }

    @Override
    public Identifier getTextureLocation(RavagerRenderState ravagerRenderState) {
        return TEXTURE_LOCATION;
    }

    @Override
    public RavagerRenderState createRenderState() {
        return new RavagerRenderState();
    }

    @Override
    public void extractRenderState(Ravager ravager, RavagerRenderState ravagerRenderState, float f) {
        super.extractRenderState(ravager, ravagerRenderState, f);
        ravagerRenderState.stunnedTicksRemaining = (float)ravager.getStunnedTick() > 0.0f ? (float)ravager.getStunnedTick() - f : 0.0f;
        ravagerRenderState.attackTicksRemaining = (float)ravager.getAttackTick() > 0.0f ? (float)ravager.getAttackTick() - f : 0.0f;
        ravagerRenderState.roarAnimation = ravager.getRoarTick() > 0 ? ((float)(20 - ravager.getRoarTick()) + f) / 20.0f : 0.0f;
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((RavagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

