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
import net.minecraft.client.model.monster.enderman.EndermanModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CarriedBlockLayer;
import net.minecraft.client.renderer.entity.layers.EnderEyesLayer;
import net.minecraft.client.renderer.entity.state.EndermanRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class EndermanRenderer
extends MobRenderer<EnderMan, EndermanRenderState, EndermanModel<EndermanRenderState>> {
    private static final Identifier ENDERMAN_LOCATION = Identifier.withDefaultNamespace("textures/entity/enderman/enderman.png");
    private final RandomSource random = RandomSource.create();

    public EndermanRenderer(EntityRendererProvider.Context context) {
        super(context, new EndermanModel(context.bakeLayer(ModelLayers.ENDERMAN)), 0.5f);
        this.addLayer(new EnderEyesLayer(this));
        this.addLayer(new CarriedBlockLayer(this));
    }

    @Override
    public Vec3 getRenderOffset(EndermanRenderState endermanRenderState) {
        Vec3 vec3 = super.getRenderOffset(endermanRenderState);
        if (endermanRenderState.isCreepy) {
            double d = 0.02 * (double)endermanRenderState.scale;
            return vec3.add(this.random.nextGaussian() * d, 0.0, this.random.nextGaussian() * d);
        }
        return vec3;
    }

    @Override
    public Identifier getTextureLocation(EndermanRenderState endermanRenderState) {
        return ENDERMAN_LOCATION;
    }

    @Override
    public EndermanRenderState createRenderState() {
        return new EndermanRenderState();
    }

    @Override
    public void extractRenderState(EnderMan enderMan, EndermanRenderState endermanRenderState, float f) {
        super.extractRenderState(enderMan, endermanRenderState, f);
        HumanoidMobRenderer.extractHumanoidRenderState(enderMan, endermanRenderState, f, this.itemModelResolver);
        endermanRenderState.isCreepy = enderMan.isCreepy();
        endermanRenderState.carriedBlock = enderMan.getCarriedBlock();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

