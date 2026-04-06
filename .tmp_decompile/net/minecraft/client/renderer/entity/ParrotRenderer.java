/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.parrot.ParrotModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.parrot.Parrot;

@Environment(value=EnvType.CLIENT)
public class ParrotRenderer
extends MobRenderer<Parrot, ParrotRenderState, ParrotModel> {
    private static final Identifier RED_BLUE = Identifier.withDefaultNamespace("textures/entity/parrot/parrot_red_blue.png");
    private static final Identifier BLUE = Identifier.withDefaultNamespace("textures/entity/parrot/parrot_blue.png");
    private static final Identifier GREEN = Identifier.withDefaultNamespace("textures/entity/parrot/parrot_green.png");
    private static final Identifier YELLOW_BLUE = Identifier.withDefaultNamespace("textures/entity/parrot/parrot_yellow_blue.png");
    private static final Identifier GREY = Identifier.withDefaultNamespace("textures/entity/parrot/parrot_grey.png");

    public ParrotRenderer(EntityRendererProvider.Context context) {
        super(context, new ParrotModel(context.bakeLayer(ModelLayers.PARROT)), 0.3f);
    }

    @Override
    public Identifier getTextureLocation(ParrotRenderState parrotRenderState) {
        return ParrotRenderer.getVariantTexture(parrotRenderState.variant);
    }

    @Override
    public ParrotRenderState createRenderState() {
        return new ParrotRenderState();
    }

    @Override
    public void extractRenderState(Parrot parrot, ParrotRenderState parrotRenderState, float f) {
        super.extractRenderState(parrot, parrotRenderState, f);
        parrotRenderState.variant = parrot.getVariant();
        float g = Mth.lerp(f, parrot.oFlap, parrot.flap);
        float h = Mth.lerp(f, parrot.oFlapSpeed, parrot.flapSpeed);
        parrotRenderState.flapAngle = (Mth.sin(g) + 1.0f) * h;
        parrotRenderState.pose = ParrotModel.getPose(parrot);
    }

    public static Identifier getVariantTexture(Parrot.Variant variant) {
        return switch (variant) {
            default -> throw new MatchException(null, null);
            case Parrot.Variant.RED_BLUE -> RED_BLUE;
            case Parrot.Variant.BLUE -> BLUE;
            case Parrot.Variant.GREEN -> GREEN;
            case Parrot.Variant.YELLOW_BLUE -> YELLOW_BLUE;
            case Parrot.Variant.GRAY -> GREY;
        };
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ParrotRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

