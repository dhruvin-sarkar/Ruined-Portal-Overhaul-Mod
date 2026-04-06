/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.EndPortalRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class AbstractEndPortalRenderer<T extends TheEndPortalBlockEntity, S extends EndPortalRenderState>
implements BlockEntityRenderer<T, S> {
    public static final Identifier END_SKY_LOCATION = Identifier.withDefaultNamespace("textures/environment/end_sky.png");
    public static final Identifier END_PORTAL_LOCATION = Identifier.withDefaultNamespace("textures/entity/end_portal.png");

    @Override
    public void extractRenderState(T theEndPortalBlockEntity, S endPortalRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(theEndPortalBlockEntity, endPortalRenderState, f, vec3, crumblingOverlay);
        ((EndPortalRenderState)endPortalRenderState).facesToShow.clear();
        for (Direction direction : Direction.values()) {
            if (!((TheEndPortalBlockEntity)theEndPortalBlockEntity).shouldRenderFace(direction)) continue;
            ((EndPortalRenderState)endPortalRenderState).facesToShow.add(direction);
        }
    }

    @Override
    public void submit(S endPortalRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        submitNodeCollector.submitCustomGeometry(poseStack, this.renderType(), (pose, vertexConsumer) -> this.renderCube(endPortalRenderState.facesToShow, pose.pose(), vertexConsumer));
    }

    private void renderCube(EnumSet<Direction> enumSet, Matrix4f matrix4f, VertexConsumer vertexConsumer) {
        float f = this.getOffsetDown();
        float g = this.getOffsetUp();
        this.renderFace(enumSet, matrix4f, vertexConsumer, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, Direction.SOUTH);
        this.renderFace(enumSet, matrix4f, vertexConsumer, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, Direction.NORTH);
        this.renderFace(enumSet, matrix4f, vertexConsumer, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.EAST);
        this.renderFace(enumSet, matrix4f, vertexConsumer, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, Direction.WEST);
        this.renderFace(enumSet, matrix4f, vertexConsumer, 0.0f, 1.0f, f, f, 0.0f, 0.0f, 1.0f, 1.0f, Direction.DOWN);
        this.renderFace(enumSet, matrix4f, vertexConsumer, 0.0f, 1.0f, g, g, 1.0f, 1.0f, 0.0f, 0.0f, Direction.UP);
    }

    private void renderFace(EnumSet<Direction> enumSet, Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, Direction direction) {
        if (enumSet.contains(direction)) {
            vertexConsumer.addVertex((Matrix4fc)matrix4f, f, h, j);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, g, h, k);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, g, i, l);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, f, i, m);
        }
    }

    protected float getOffsetUp() {
        return 0.75f;
    }

    protected float getOffsetDown() {
        return 0.375f;
    }

    protected RenderType renderType() {
        return RenderTypes.endPortal();
    }
}

