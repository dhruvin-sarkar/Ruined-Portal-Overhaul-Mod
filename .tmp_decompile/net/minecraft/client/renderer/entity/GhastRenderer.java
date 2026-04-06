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
import net.minecraft.client.model.monster.ghast.GhastModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.GhastRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.Ghast;

@Environment(value=EnvType.CLIENT)
public class GhastRenderer
extends MobRenderer<Ghast, GhastRenderState, GhastModel> {
    private static final Identifier GHAST_LOCATION = Identifier.withDefaultNamespace("textures/entity/ghast/ghast.png");
    private static final Identifier GHAST_SHOOTING_LOCATION = Identifier.withDefaultNamespace("textures/entity/ghast/ghast_shooting.png");

    public GhastRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel(context.bakeLayer(ModelLayers.GHAST)), 1.5f);
    }

    @Override
    public Identifier getTextureLocation(GhastRenderState ghastRenderState) {
        if (ghastRenderState.isCharging) {
            return GHAST_SHOOTING_LOCATION;
        }
        return GHAST_LOCATION;
    }

    @Override
    public GhastRenderState createRenderState() {
        return new GhastRenderState();
    }

    @Override
    public void extractRenderState(Ghast ghast, GhastRenderState ghastRenderState, float f) {
        super.extractRenderState(ghast, ghastRenderState, f);
        ghastRenderState.isCharging = ghast.isCharging();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((GhastRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

