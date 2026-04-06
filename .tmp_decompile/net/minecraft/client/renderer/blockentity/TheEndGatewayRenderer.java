/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractEndPortalRenderer;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.EndGatewayRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TheEndGatewayRenderer
extends AbstractEndPortalRenderer<TheEndGatewayBlockEntity, EndGatewayRenderState> {
    private static final Identifier BEAM_LOCATION = Identifier.withDefaultNamespace("textures/entity/end_gateway_beam.png");

    @Override
    public EndGatewayRenderState createRenderState() {
        return new EndGatewayRenderState();
    }

    @Override
    public void extractRenderState(TheEndGatewayBlockEntity theEndGatewayBlockEntity, EndGatewayRenderState endGatewayRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        super.extractRenderState(theEndGatewayBlockEntity, endGatewayRenderState, f, vec3, crumblingOverlay);
        Level level = theEndGatewayBlockEntity.getLevel();
        if (theEndGatewayBlockEntity.isSpawning() || theEndGatewayBlockEntity.isCoolingDown() && level != null) {
            endGatewayRenderState.scale = theEndGatewayBlockEntity.isSpawning() ? theEndGatewayBlockEntity.getSpawnPercent(f) : theEndGatewayBlockEntity.getCooldownPercent(f);
            double d = theEndGatewayBlockEntity.isSpawning() ? (double)theEndGatewayBlockEntity.getLevel().getMaxY() : 50.0;
            endGatewayRenderState.scale = Mth.sin(endGatewayRenderState.scale * (float)Math.PI);
            endGatewayRenderState.height = Mth.floor((double)endGatewayRenderState.scale * d);
            endGatewayRenderState.color = theEndGatewayBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColor() : DyeColor.PURPLE.getTextureDiffuseColor();
            endGatewayRenderState.animationTime = theEndGatewayBlockEntity.getLevel() != null ? (float)Math.floorMod((long)theEndGatewayBlockEntity.getLevel().getGameTime(), (int)40) + f : 0.0f;
        } else {
            endGatewayRenderState.height = 0;
        }
    }

    @Override
    public void submit(EndGatewayRenderState endGatewayRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (endGatewayRenderState.height > 0) {
            BeaconRenderer.submitBeaconBeam(poseStack, submitNodeCollector, BEAM_LOCATION, endGatewayRenderState.scale, endGatewayRenderState.animationTime, -endGatewayRenderState.height, endGatewayRenderState.height * 2, endGatewayRenderState.color, 0.15f, 0.175f);
        }
        super.submit(endGatewayRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    @Override
    protected float getOffsetUp() {
        return 1.0f;
    }

    @Override
    protected float getOffsetDown() {
        return 0.0f;
    }

    @Override
    protected RenderType renderType() {
        return RenderTypes.endGateway();
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

