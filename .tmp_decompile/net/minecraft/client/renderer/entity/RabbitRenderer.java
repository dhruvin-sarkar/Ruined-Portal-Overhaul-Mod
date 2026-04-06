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
import net.minecraft.client.model.animal.rabbit.RabbitModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.RabbitRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.rabbit.Rabbit;

@Environment(value=EnvType.CLIENT)
public class RabbitRenderer
extends AgeableMobRenderer<Rabbit, RabbitRenderState, RabbitModel> {
    private static final Identifier RABBIT_BROWN_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/brown.png");
    private static final Identifier RABBIT_WHITE_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/white.png");
    private static final Identifier RABBIT_BLACK_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/black.png");
    private static final Identifier RABBIT_GOLD_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/gold.png");
    private static final Identifier RABBIT_SALT_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/salt.png");
    private static final Identifier RABBIT_WHITE_SPLOTCHED_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/white_splotched.png");
    private static final Identifier RABBIT_TOAST_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/toast.png");
    private static final Identifier RABBIT_EVIL_LOCATION = Identifier.withDefaultNamespace("textures/entity/rabbit/caerbannog.png");

    public RabbitRenderer(EntityRendererProvider.Context context) {
        super(context, new RabbitModel(context.bakeLayer(ModelLayers.RABBIT)), new RabbitModel(context.bakeLayer(ModelLayers.RABBIT_BABY)), 0.3f);
    }

    @Override
    public Identifier getTextureLocation(RabbitRenderState rabbitRenderState) {
        if (rabbitRenderState.isToast) {
            return RABBIT_TOAST_LOCATION;
        }
        return switch (rabbitRenderState.variant) {
            default -> throw new MatchException(null, null);
            case Rabbit.Variant.BROWN -> RABBIT_BROWN_LOCATION;
            case Rabbit.Variant.WHITE -> RABBIT_WHITE_LOCATION;
            case Rabbit.Variant.BLACK -> RABBIT_BLACK_LOCATION;
            case Rabbit.Variant.GOLD -> RABBIT_GOLD_LOCATION;
            case Rabbit.Variant.SALT -> RABBIT_SALT_LOCATION;
            case Rabbit.Variant.WHITE_SPLOTCHED -> RABBIT_WHITE_SPLOTCHED_LOCATION;
            case Rabbit.Variant.EVIL -> RABBIT_EVIL_LOCATION;
        };
    }

    @Override
    public RabbitRenderState createRenderState() {
        return new RabbitRenderState();
    }

    @Override
    public void extractRenderState(Rabbit rabbit, RabbitRenderState rabbitRenderState, float f) {
        super.extractRenderState(rabbit, rabbitRenderState, f);
        rabbitRenderState.jumpCompletion = rabbit.getJumpCompletion(f);
        rabbitRenderState.isToast = RabbitRenderer.checkMagicName(rabbit, "Toast");
        rabbitRenderState.variant = rabbit.getVariant();
    }

    @Override
    public /* synthetic */ Identifier getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((RabbitRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

