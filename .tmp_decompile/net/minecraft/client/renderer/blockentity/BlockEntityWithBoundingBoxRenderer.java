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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityWithBoundingBoxRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BlockEntityWithBoundingBoxRenderer<T extends BlockEntity>
implements BlockEntityRenderer<T, BlockEntityWithBoundingBoxRenderState> {
    public static final int STRUCTURE_VOIDS_COLOR = ARGB.colorFromFloat(0.2f, 0.75f, 0.75f, 1.0f);

    @Override
    public BlockEntityWithBoundingBoxRenderState createRenderState() {
        return new BlockEntityWithBoundingBoxRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, BlockEntityWithBoundingBoxRenderState blockEntityWithBoundingBoxRenderState, float f, Vec3 vec3,  @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, blockEntityWithBoundingBoxRenderState, f, vec3, crumblingOverlay);
        BlockEntityWithBoundingBoxRenderer.extract(blockEntity, blockEntityWithBoundingBoxRenderState);
    }

    public static <T extends BlockEntity> void extract(T blockEntity, BlockEntityWithBoundingBoxRenderState blockEntityWithBoundingBoxRenderState) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;
        blockEntityWithBoundingBoxRenderState.isVisible = localPlayer.canUseGameMasterBlocks() || localPlayer.isSpectator();
        blockEntityWithBoundingBoxRenderState.box = ((BoundingBoxRenderable)((Object)blockEntity)).getRenderableBox();
        blockEntityWithBoundingBoxRenderState.mode = ((BoundingBoxRenderable)((Object)blockEntity)).renderMode();
        BlockPos blockPos = blockEntityWithBoundingBoxRenderState.box.localPos();
        Vec3i vec3i = blockEntityWithBoundingBoxRenderState.box.size();
        BlockPos blockPos2 = blockEntityWithBoundingBoxRenderState.blockPos;
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        if (blockEntityWithBoundingBoxRenderState.isVisible && blockEntity.getLevel() != null && blockEntityWithBoundingBoxRenderState.mode == BoundingBoxRenderable.Mode.BOX_AND_INVISIBLE_BLOCKS) {
            blockEntityWithBoundingBoxRenderState.invisibleBlocks = new BlockEntityWithBoundingBoxRenderState.InvisibleBlockType[vec3i.getX() * vec3i.getY() * vec3i.getZ()];
            for (int i = 0; i < vec3i.getX(); ++i) {
                for (int j = 0; j < vec3i.getY(); ++j) {
                    for (int k = 0; k < vec3i.getZ(); ++k) {
                        int l = k * vec3i.getX() * vec3i.getY() + j * vec3i.getX() + i;
                        BlockState blockState = blockEntity.getLevel().getBlockState(blockPos3.offset(i, j, k));
                        if (blockState.isAir()) {
                            blockEntityWithBoundingBoxRenderState.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.AIR;
                            continue;
                        }
                        if (blockState.is(Blocks.STRUCTURE_VOID)) {
                            blockEntityWithBoundingBoxRenderState.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.STRUCTURE_VOID;
                            continue;
                        }
                        if (blockState.is(Blocks.BARRIER)) {
                            blockEntityWithBoundingBoxRenderState.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.BARRIER;
                            continue;
                        }
                        if (!blockState.is(Blocks.LIGHT)) continue;
                        blockEntityWithBoundingBoxRenderState.invisibleBlocks[l] = BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.LIGHT;
                    }
                }
            }
        } else {
            blockEntityWithBoundingBoxRenderState.invisibleBlocks = null;
        }
        if (blockEntityWithBoundingBoxRenderState.isVisible) {
            // empty if block
        }
        blockEntityWithBoundingBoxRenderState.structureVoids = null;
    }

    @Override
    public void submit(BlockEntityWithBoundingBoxRenderState blockEntityWithBoundingBoxRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (!blockEntityWithBoundingBoxRenderState.isVisible) {
            return;
        }
        BoundingBoxRenderable.Mode mode = blockEntityWithBoundingBoxRenderState.mode;
        if (mode == BoundingBoxRenderable.Mode.NONE) {
            return;
        }
        BoundingBoxRenderable.RenderableBox renderableBox = blockEntityWithBoundingBoxRenderState.box;
        BlockPos blockPos = renderableBox.localPos();
        Vec3i vec3i = renderableBox.size();
        if (vec3i.getX() < 1 || vec3i.getY() < 1 || vec3i.getZ() < 1) {
            return;
        }
        float f = 1.0f;
        float g = 0.9f;
        BlockPos blockPos2 = blockPos.offset(vec3i);
        Gizmos.cuboid(new AABB(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos2.getX(), blockPos2.getY(), blockPos2.getZ()).move(blockEntityWithBoundingBoxRenderState.blockPos), GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 0.9f, 0.9f, 0.9f)), true);
        this.renderInvisibleBlocks(blockEntityWithBoundingBoxRenderState, blockPos, vec3i);
    }

    private void renderInvisibleBlocks(BlockEntityWithBoundingBoxRenderState blockEntityWithBoundingBoxRenderState, BlockPos blockPos, Vec3i vec3i) {
        if (blockEntityWithBoundingBoxRenderState.invisibleBlocks == null) {
            return;
        }
        BlockPos blockPos2 = blockEntityWithBoundingBoxRenderState.blockPos;
        BlockPos blockPos3 = blockPos2.offset(blockPos);
        for (int i = 0; i < vec3i.getX(); ++i) {
            for (int j = 0; j < vec3i.getY(); ++j) {
                for (int k = 0; k < vec3i.getZ(); ++k) {
                    int l = k * vec3i.getX() * vec3i.getY() + j * vec3i.getX() + i;
                    BlockEntityWithBoundingBoxRenderState.InvisibleBlockType invisibleBlockType = blockEntityWithBoundingBoxRenderState.invisibleBlocks[l];
                    if (invisibleBlockType == null) continue;
                    float f = invisibleBlockType == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.AIR ? 0.05f : 0.0f;
                    double d = (float)(blockPos3.getX() + i) + 0.45f - f;
                    double e = (float)(blockPos3.getY() + j) + 0.45f - f;
                    double g = (float)(blockPos3.getZ() + k) + 0.45f - f;
                    double h = (float)(blockPos3.getX() + i) + 0.55f + f;
                    double m = (float)(blockPos3.getY() + j) + 0.55f + f;
                    double n = (float)(blockPos3.getZ() + k) + 0.55f + f;
                    AABB aABB = new AABB(d, e, g, h, m, n);
                    if (invisibleBlockType == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.AIR) {
                        Gizmos.cuboid(aABB, GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 0.5f, 0.5f, 1.0f)));
                        continue;
                    }
                    if (invisibleBlockType == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.STRUCTURE_VOID) {
                        Gizmos.cuboid(aABB, GizmoStyle.stroke(ARGB.colorFromFloat(1.0f, 1.0f, 0.75f, 0.75f)));
                        continue;
                    }
                    if (invisibleBlockType == BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.BARRIER) {
                        Gizmos.cuboid(aABB, GizmoStyle.stroke(-65536));
                        continue;
                    }
                    if (invisibleBlockType != BlockEntityWithBoundingBoxRenderState.InvisibleBlockType.LIGHT) continue;
                    Gizmos.cuboid(aABB, GizmoStyle.stroke(-256));
                }
            }
        }
    }

    private void renderStructureVoids(BlockEntityWithBoundingBoxRenderState blockEntityWithBoundingBoxRenderState, BlockPos blockPos, Vec3i vec3i) {
        if (blockEntityWithBoundingBoxRenderState.structureVoids == null) {
            return;
        }
        BitSetDiscreteVoxelShape discreteVoxelShape = new BitSetDiscreteVoxelShape(vec3i.getX(), vec3i.getY(), vec3i.getZ());
        for (int i2 = 0; i2 < vec3i.getX(); ++i2) {
            for (int j2 = 0; j2 < vec3i.getY(); ++j2) {
                for (int k2 = 0; k2 < vec3i.getZ(); ++k2) {
                    int l = k2 * vec3i.getX() * vec3i.getY() + j2 * vec3i.getX() + i2;
                    if (!blockEntityWithBoundingBoxRenderState.structureVoids[l]) continue;
                    ((DiscreteVoxelShape)discreteVoxelShape).fill(i2, j2, k2);
                }
            }
        }
        discreteVoxelShape.forAllFaces((direction, i, j, k) -> {
            float f = 0.48f;
            float g = (float)(i + blockPos.getX()) + 0.5f - 0.48f;
            float h = (float)(j + blockPos.getY()) + 0.5f - 0.48f;
            float l = (float)(k + blockPos.getZ()) + 0.5f - 0.48f;
            float m = (float)(i + blockPos.getX()) + 0.5f + 0.48f;
            float n = (float)(j + blockPos.getY()) + 0.5f + 0.48f;
            float o = (float)(k + blockPos.getZ()) + 0.5f + 0.48f;
            Gizmos.rect(new Vec3(g, h, l), new Vec3(m, n, o), direction, GizmoStyle.fill(STRUCTURE_VOIDS_COLOR));
        });
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 96;
    }

    @Override
    public /* synthetic */ BlockEntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

