/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class ModelBlockRenderer {
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BlockColors blockColors;
    private static final int CACHE_SIZE = 100;
    static final ThreadLocal<Cache> CACHE = ThreadLocal.withInitial(Cache::new);

    public ModelBlockRenderer(BlockColors blockColors) {
        this.blockColors = blockColors;
    }

    public void tesselateBlock(BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> list, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int i) {
        if (list.isEmpty()) {
            return;
        }
        boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && ((BlockModelPart)list.getFirst()).useAmbientOcclusion();
        poseStack.translate(blockState.getOffset(blockPos));
        try {
            if (bl2) {
                this.tesselateWithAO(blockAndTintGetter, list, blockState, blockPos, poseStack, vertexConsumer, bl, i);
            } else {
                this.tesselateWithoutAO(blockAndTintGetter, list, blockState, blockPos, poseStack, vertexConsumer, bl, i);
            }
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block model");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
            crashReportCategory.setDetail("Using AO", bl2);
            throw new ReportedException(crashReport);
        }
    }

    private static boolean shouldRenderFace(BlockAndTintGetter blockAndTintGetter, BlockState blockState, boolean bl, Direction direction, BlockPos blockPos) {
        if (!bl) {
            return true;
        }
        BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos);
        return Block.shouldRenderFace(blockState, blockState2, direction);
    }

    public void tesselateWithAO(BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> list, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int i) {
        AmbientOcclusionRenderStorage ambientOcclusionRenderStorage = new AmbientOcclusionRenderStorage();
        int j = 0;
        int k = 0;
        for (BlockModelPart blockModelPart : list) {
            for (Direction direction : DIRECTIONS) {
                List<BakedQuad> list2;
                boolean bl3;
                int l = 1 << direction.ordinal();
                boolean bl2 = (j & l) == 1;
                boolean bl4 = bl3 = (k & l) == 1;
                if (bl2 && !bl3 || (list2 = blockModelPart.getQuads(direction)).isEmpty()) continue;
                if (!bl2) {
                    bl3 = ModelBlockRenderer.shouldRenderFace(blockAndTintGetter, blockState, bl, direction, ambientOcclusionRenderStorage.scratchPos.setWithOffset((Vec3i)blockPos, direction));
                    j |= l;
                    if (bl3) {
                        k |= l;
                    }
                }
                if (!bl3) continue;
                this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list2, ambientOcclusionRenderStorage, i);
            }
            List<BakedQuad> list3 = blockModelPart.getQuads(null);
            if (list3.isEmpty()) continue;
            this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list3, ambientOcclusionRenderStorage, i);
        }
    }

    public void tesselateWithoutAO(BlockAndTintGetter blockAndTintGetter, List<BlockModelPart> list, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, int i) {
        CommonRenderStorage commonRenderStorage = new CommonRenderStorage();
        int j = 0;
        int k = 0;
        for (BlockModelPart blockModelPart : list) {
            for (Direction direction : DIRECTIONS) {
                List<BakedQuad> list2;
                boolean bl3;
                int l = 1 << direction.ordinal();
                boolean bl2 = (j & l) == 1;
                boolean bl4 = bl3 = (k & l) == 1;
                if (bl2 && !bl3 || (list2 = blockModelPart.getQuads(direction)).isEmpty()) continue;
                BlockPos.MutableBlockPos blockPos2 = commonRenderStorage.scratchPos.setWithOffset((Vec3i)blockPos, direction);
                if (!bl2) {
                    bl3 = ModelBlockRenderer.shouldRenderFace(blockAndTintGetter, blockState, bl, direction, blockPos2);
                    j |= l;
                    if (bl3) {
                        k |= l;
                    }
                }
                if (!bl3) continue;
                int m = commonRenderStorage.cache.getLightColor(blockState, blockAndTintGetter, blockPos2);
                this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, m, i, false, poseStack, vertexConsumer, list2, commonRenderStorage);
            }
            List<BakedQuad> list3 = blockModelPart.getQuads(null);
            if (list3.isEmpty()) continue;
            this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, -1, i, true, poseStack, vertexConsumer, list3, commonRenderStorage);
        }
    }

    private void renderModelFaceAO(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, AmbientOcclusionRenderStorage ambientOcclusionRenderStorage, int i) {
        for (BakedQuad bakedQuad : list) {
            ModelBlockRenderer.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad, ambientOcclusionRenderStorage);
            ambientOcclusionRenderStorage.calculate(blockAndTintGetter, blockState, blockPos, bakedQuad.direction(), bakedQuad.shade());
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, ambientOcclusionRenderStorage, i);
        }
    }

    private void putQuadData(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, VertexConsumer vertexConsumer, PoseStack.Pose pose, BakedQuad bakedQuad, CommonRenderStorage commonRenderStorage, int i) {
        float h;
        float g;
        float f;
        int j = bakedQuad.tintIndex();
        if (j != -1) {
            int k;
            if (commonRenderStorage.tintCacheIndex == j) {
                k = commonRenderStorage.tintCacheValue;
            } else {
                k = this.blockColors.getColor(blockState, blockAndTintGetter, blockPos, j);
                commonRenderStorage.tintCacheIndex = j;
                commonRenderStorage.tintCacheValue = k;
            }
            f = ARGB.redFloat(k);
            g = ARGB.greenFloat(k);
            h = ARGB.blueFloat(k);
        } else {
            f = 1.0f;
            g = 1.0f;
            h = 1.0f;
        }
        vertexConsumer.putBulkData(pose, bakedQuad, commonRenderStorage.brightness, f, g, h, 1.0f, commonRenderStorage.lightmap, i);
    }

    private static void calculateShape(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, BakedQuad bakedQuad, CommonRenderStorage commonRenderStorage) {
        float f = 32.0f;
        float g = 32.0f;
        float h = 32.0f;
        float i = -32.0f;
        float j = -32.0f;
        float k = -32.0f;
        for (int l = 0; l < 4; ++l) {
            Vector3fc vector3fc = bakedQuad.position(l);
            float m = vector3fc.x();
            float n = vector3fc.y();
            float o = vector3fc.z();
            f = Math.min(f, m);
            g = Math.min(g, n);
            h = Math.min(h, o);
            i = Math.max(i, m);
            j = Math.max(j, n);
            k = Math.max(k, o);
        }
        if (commonRenderStorage instanceof AmbientOcclusionRenderStorage) {
            AmbientOcclusionRenderStorage ambientOcclusionRenderStorage = (AmbientOcclusionRenderStorage)commonRenderStorage;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.WEST.index] = f;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.EAST.index] = i;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.DOWN.index] = g;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.UP.index] = j;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.NORTH.index] = h;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.SOUTH.index] = k;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_WEST.index] = 1.0f - f;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_EAST.index] = 1.0f - i;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_DOWN.index] = 1.0f - g;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_UP.index] = 1.0f - j;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_NORTH.index] = 1.0f - h;
            ambientOcclusionRenderStorage.faceShape[SizeInfo.FLIP_SOUTH.index] = 1.0f - k;
        }
        float p = 1.0E-4f;
        float q = 0.9999f;
        commonRenderStorage.facePartial = switch (bakedQuad.direction()) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN, Direction.UP -> {
                if (f >= 1.0E-4f || h >= 1.0E-4f || i <= 0.9999f || k <= 0.9999f) {
                    yield true;
                }
                yield false;
            }
            case Direction.NORTH, Direction.SOUTH -> {
                if (f >= 1.0E-4f || g >= 1.0E-4f || i <= 0.9999f || j <= 0.9999f) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST, Direction.EAST -> g >= 1.0E-4f || h >= 1.0E-4f || j <= 0.9999f || k <= 0.9999f;
        };
        commonRenderStorage.faceCubic = switch (bakedQuad.direction()) {
            default -> throw new MatchException(null, null);
            case Direction.DOWN -> {
                if (g == j && (g < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.UP -> {
                if (g == j && (j > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.NORTH -> {
                if (h == k && (h < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.SOUTH -> {
                if (h == k && (k > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.WEST -> {
                if (f == i && (f < 1.0E-4f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos))) {
                    yield true;
                }
                yield false;
            }
            case Direction.EAST -> f == i && (i > 0.9999f || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos));
        };
    }

    private void renderModelFaceFlat(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int i, int j, boolean bl, PoseStack poseStack, VertexConsumer vertexConsumer, List<BakedQuad> list, CommonRenderStorage commonRenderStorage) {
        for (BakedQuad bakedQuad : list) {
            float f;
            if (bl) {
                ModelBlockRenderer.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad, commonRenderStorage);
                BlockPos blockPos2 = commonRenderStorage.faceCubic ? commonRenderStorage.scratchPos.setWithOffset((Vec3i)blockPos, bakedQuad.direction()) : blockPos;
                i = commonRenderStorage.cache.getLightColor(blockState, blockAndTintGetter, blockPos2);
            }
            commonRenderStorage.brightness[0] = f = blockAndTintGetter.getShade(bakedQuad.direction(), bakedQuad.shade());
            commonRenderStorage.brightness[1] = f;
            commonRenderStorage.brightness[2] = f;
            commonRenderStorage.brightness[3] = f;
            commonRenderStorage.lightmap[0] = i;
            commonRenderStorage.lightmap[1] = i;
            commonRenderStorage.lightmap[2] = i;
            commonRenderStorage.lightmap[3] = i;
            this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, commonRenderStorage, j);
        }
    }

    public static void renderModel(PoseStack.Pose pose, VertexConsumer vertexConsumer, BlockStateModel blockStateModel, float f, float g, float h, int i, int j) {
        for (BlockModelPart blockModelPart : blockStateModel.collectParts(RandomSource.create(42L))) {
            for (Direction direction : DIRECTIONS) {
                ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, g, h, blockModelPart.getQuads(direction), i, j);
            }
            ModelBlockRenderer.renderQuadList(pose, vertexConsumer, f, g, h, blockModelPart.getQuads(null), i, j);
        }
    }

    private static void renderQuadList(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, List<BakedQuad> list, int i, int j) {
        for (BakedQuad bakedQuad : list) {
            float m;
            float l;
            float k;
            if (bakedQuad.isTinted()) {
                k = Mth.clamp(f, 0.0f, 1.0f);
                l = Mth.clamp(g, 0.0f, 1.0f);
                m = Mth.clamp(h, 0.0f, 1.0f);
            } else {
                k = 1.0f;
                l = 1.0f;
                m = 1.0f;
            }
            vertexConsumer.putBulkData(pose, bakedQuad, k, l, m, 1.0f, i, j);
        }
    }

    public static void enableCaching() {
        CACHE.get().enable();
    }

    public static void clearCache() {
        CACHE.get().disable();
    }

    @Environment(value=EnvType.CLIENT)
    static class AmbientOcclusionRenderStorage
    extends CommonRenderStorage {
        final float[] faceShape = new float[SizeInfo.COUNT];

        public void calculate(BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, Direction direction, boolean bl) {
            float x;
            int u;
            float t;
            int s;
            float r;
            int q;
            float p;
            int o;
            float n;
            BlockState blockState10;
            boolean bl5;
            BlockPos blockPos2 = this.faceCubic ? blockPos.relative(direction) : blockPos;
            AdjacencyInfo adjacencyInfo = AdjacencyInfo.fromFacing(direction);
            BlockPos.MutableBlockPos mutableBlockPos = this.scratchPos;
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]);
            BlockState blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int i = this.cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
            float f = this.cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]);
            BlockState blockState3 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int j = this.cache.getLightColor(blockState3, blockAndTintGetter, mutableBlockPos);
            float g = this.cache.getShadeBrightness(blockState3, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]);
            BlockState blockState4 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int k = this.cache.getLightColor(blockState4, blockAndTintGetter, mutableBlockPos);
            float h = this.cache.getShadeBrightness(blockState4, blockAndTintGetter, mutableBlockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]);
            BlockState blockState5 = blockAndTintGetter.getBlockState(mutableBlockPos);
            int l = this.cache.getLightColor(blockState5, blockAndTintGetter, mutableBlockPos);
            float m = this.cache.getShadeBrightness(blockState5, blockAndTintGetter, mutableBlockPos);
            BlockState blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(direction));
            boolean bl2 = !blockState6.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState6.getLightBlock() == 0;
            BlockState blockState7 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(direction));
            boolean bl3 = !blockState7.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState7.getLightBlock() == 0;
            BlockState blockState8 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[2]).move(direction));
            boolean bl4 = !blockState8.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState8.getLightBlock() == 0;
            BlockState blockState9 = blockAndTintGetter.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[3]).move(direction));
            boolean bl6 = bl5 = !blockState9.isViewBlocking(blockAndTintGetter, mutableBlockPos) || blockState9.getLightBlock() == 0;
            if (bl4 || bl2) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
                blockState10 = blockAndTintGetter.getBlockState(mutableBlockPos);
                n = this.cache.getShadeBrightness(blockState10, blockAndTintGetter, mutableBlockPos);
                o = this.cache.getLightColor(blockState10, blockAndTintGetter, mutableBlockPos);
            } else {
                n = f;
                o = i;
            }
            if (bl5 || bl2) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
                blockState10 = blockAndTintGetter.getBlockState(mutableBlockPos);
                p = this.cache.getShadeBrightness(blockState10, blockAndTintGetter, mutableBlockPos);
                q = this.cache.getLightColor(blockState10, blockAndTintGetter, mutableBlockPos);
            } else {
                p = f;
                q = i;
            }
            if (bl4 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
                blockState10 = blockAndTintGetter.getBlockState(mutableBlockPos);
                r = this.cache.getShadeBrightness(blockState10, blockAndTintGetter, mutableBlockPos);
                s = this.cache.getLightColor(blockState10, blockAndTintGetter, mutableBlockPos);
            } else {
                r = f;
                s = i;
            }
            if (bl5 || bl3) {
                mutableBlockPos.setWithOffset((Vec3i)blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
                blockState10 = blockAndTintGetter.getBlockState(mutableBlockPos);
                t = this.cache.getShadeBrightness(blockState10, blockAndTintGetter, mutableBlockPos);
                u = this.cache.getLightColor(blockState10, blockAndTintGetter, mutableBlockPos);
            } else {
                t = f;
                u = i;
            }
            int v = this.cache.getLightColor(blockState, blockAndTintGetter, blockPos);
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            BlockState blockState11 = blockAndTintGetter.getBlockState(mutableBlockPos);
            if (this.faceCubic || !blockState11.isSolidRender()) {
                v = this.cache.getLightColor(blockState11, blockAndTintGetter, mutableBlockPos);
            }
            float w = this.faceCubic ? this.cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos2), blockAndTintGetter, blockPos2) : this.cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos), blockAndTintGetter, blockPos);
            AmbientVertexRemap ambientVertexRemap = AmbientVertexRemap.fromFacing(direction);
            if (!this.facePartial || !adjacencyInfo.doNonCubicWeight) {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                this.lightmap[ambientVertexRemap.vert0] = AmbientOcclusionRenderStorage.blend(l, i, q, v);
                this.lightmap[ambientVertexRemap.vert1] = AmbientOcclusionRenderStorage.blend(k, i, o, v);
                this.lightmap[ambientVertexRemap.vert2] = AmbientOcclusionRenderStorage.blend(k, j, s, v);
                this.lightmap[ambientVertexRemap.vert3] = AmbientOcclusionRenderStorage.blend(l, j, u, v);
                this.brightness[ambientVertexRemap.vert0] = x;
                this.brightness[ambientVertexRemap.vert1] = y;
                this.brightness[ambientVertexRemap.vert2] = z;
                this.brightness[ambientVertexRemap.vert3] = aa;
            } else {
                x = (m + f + p + w) * 0.25f;
                y = (h + f + n + w) * 0.25f;
                z = (h + g + r + w) * 0.25f;
                aa = (m + g + t + w) * 0.25f;
                float ab = this.faceShape[adjacencyInfo.vert0Weights[0].index] * this.faceShape[adjacencyInfo.vert0Weights[1].index];
                float ac = this.faceShape[adjacencyInfo.vert0Weights[2].index] * this.faceShape[adjacencyInfo.vert0Weights[3].index];
                float ad = this.faceShape[adjacencyInfo.vert0Weights[4].index] * this.faceShape[adjacencyInfo.vert0Weights[5].index];
                float ae = this.faceShape[adjacencyInfo.vert0Weights[6].index] * this.faceShape[adjacencyInfo.vert0Weights[7].index];
                float af = this.faceShape[adjacencyInfo.vert1Weights[0].index] * this.faceShape[adjacencyInfo.vert1Weights[1].index];
                float ag = this.faceShape[adjacencyInfo.vert1Weights[2].index] * this.faceShape[adjacencyInfo.vert1Weights[3].index];
                float ah = this.faceShape[adjacencyInfo.vert1Weights[4].index] * this.faceShape[adjacencyInfo.vert1Weights[5].index];
                float ai = this.faceShape[adjacencyInfo.vert1Weights[6].index] * this.faceShape[adjacencyInfo.vert1Weights[7].index];
                float aj = this.faceShape[adjacencyInfo.vert2Weights[0].index] * this.faceShape[adjacencyInfo.vert2Weights[1].index];
                float ak = this.faceShape[adjacencyInfo.vert2Weights[2].index] * this.faceShape[adjacencyInfo.vert2Weights[3].index];
                float al = this.faceShape[adjacencyInfo.vert2Weights[4].index] * this.faceShape[adjacencyInfo.vert2Weights[5].index];
                float am = this.faceShape[adjacencyInfo.vert2Weights[6].index] * this.faceShape[adjacencyInfo.vert2Weights[7].index];
                float an = this.faceShape[adjacencyInfo.vert3Weights[0].index] * this.faceShape[adjacencyInfo.vert3Weights[1].index];
                float ao = this.faceShape[adjacencyInfo.vert3Weights[2].index] * this.faceShape[adjacencyInfo.vert3Weights[3].index];
                float ap = this.faceShape[adjacencyInfo.vert3Weights[4].index] * this.faceShape[adjacencyInfo.vert3Weights[5].index];
                float aq = this.faceShape[adjacencyInfo.vert3Weights[6].index] * this.faceShape[adjacencyInfo.vert3Weights[7].index];
                this.brightness[ambientVertexRemap.vert0] = Math.clamp((float)(x * ab + y * ac + z * ad + aa * ae), (float)0.0f, (float)1.0f);
                this.brightness[ambientVertexRemap.vert1] = Math.clamp((float)(x * af + y * ag + z * ah + aa * ai), (float)0.0f, (float)1.0f);
                this.brightness[ambientVertexRemap.vert2] = Math.clamp((float)(x * aj + y * ak + z * al + aa * am), (float)0.0f, (float)1.0f);
                this.brightness[ambientVertexRemap.vert3] = Math.clamp((float)(x * an + y * ao + z * ap + aa * aq), (float)0.0f, (float)1.0f);
                int ar = AmbientOcclusionRenderStorage.blend(l, i, q, v);
                int as = AmbientOcclusionRenderStorage.blend(k, i, o, v);
                int at = AmbientOcclusionRenderStorage.blend(k, j, s, v);
                int au = AmbientOcclusionRenderStorage.blend(l, j, u, v);
                this.lightmap[ambientVertexRemap.vert0] = AmbientOcclusionRenderStorage.blend(ar, as, at, au, ab, ac, ad, ae);
                this.lightmap[ambientVertexRemap.vert1] = AmbientOcclusionRenderStorage.blend(ar, as, at, au, af, ag, ah, ai);
                this.lightmap[ambientVertexRemap.vert2] = AmbientOcclusionRenderStorage.blend(ar, as, at, au, aj, ak, al, am);
                this.lightmap[ambientVertexRemap.vert3] = AmbientOcclusionRenderStorage.blend(ar, as, at, au, an, ao, ap, aq);
            }
            x = blockAndTintGetter.getShade(direction, bl);
            int av = 0;
            while (av < this.brightness.length) {
                int n2 = av++;
                this.brightness[n2] = this.brightness[n2] * x;
            }
        }

        private static int blend(int i, int j, int k, int l) {
            if (i == 0) {
                i = l;
            }
            if (j == 0) {
                j = l;
            }
            if (k == 0) {
                k = l;
            }
            return i + j + k + l >> 2 & 0xFF00FF;
        }

        private static int blend(int i, int j, int k, int l, float f, float g, float h, float m) {
            int n = (int)((float)(i >> 16 & 0xFF) * f + (float)(j >> 16 & 0xFF) * g + (float)(k >> 16 & 0xFF) * h + (float)(l >> 16 & 0xFF) * m) & 0xFF;
            int o = (int)((float)(i & 0xFF) * f + (float)(j & 0xFF) * g + (float)(k & 0xFF) * h + (float)(l & 0xFF) * m) & 0xFF;
            return n << 16 | o;
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class CommonRenderStorage {
        public final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
        public boolean faceCubic;
        public boolean facePartial;
        public final float[] brightness = new float[4];
        public final int[] lightmap = new int[4];
        public int tintCacheIndex = -1;
        public int tintCacheValue;
        public final Cache cache = CACHE.get();

        CommonRenderStorage() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    static class Cache {
        private boolean enabled;
        private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
            Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25f){

                protected void rehash(int i) {
                }
            };
            long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
            return long2IntLinkedOpenHashMap;
        });
        private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
            Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25f){

                protected void rehash(int i) {
                }
            };
            long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
            return long2FloatLinkedOpenHashMap;
        });
        private final LevelRenderer.BrightnessGetter cachedBrightnessGetter = (blockAndTintGetter, blockPos) -> {
            long l = blockPos.asLong();
            int i = this.colorCache.get(l);
            if (i != Integer.MAX_VALUE) {
                return i;
            }
            int j = LevelRenderer.BrightnessGetter.DEFAULT.packedBrightness(blockAndTintGetter, blockPos);
            if (this.colorCache.size() == 100) {
                this.colorCache.removeFirstInt();
            }
            this.colorCache.put(l, j);
            return j;
        };

        private Cache() {
        }

        public void enable() {
            this.enabled = true;
        }

        public void disable() {
            this.enabled = false;
            this.colorCache.clear();
            this.brightnessCache.clear();
        }

        public int getLightColor(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            return LevelRenderer.getLightColor(this.enabled ? this.cachedBrightnessGetter : LevelRenderer.BrightnessGetter.DEFAULT, blockAndTintGetter, blockState, blockPos);
        }

        public float getShadeBrightness(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
            float f;
            long l = blockPos.asLong();
            if (this.enabled && !Float.isNaN(f = this.brightnessCache.get(l))) {
                return f;
            }
            f = blockState.getShadeBrightness(blockAndTintGetter, blockPos);
            if (this.enabled) {
                if (this.brightnessCache.size() == 100) {
                    this.brightnessCache.removeFirstFloat();
                }
                this.brightnessCache.put(l, f);
            }
            return f;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum SizeInfo {
        DOWN(0),
        UP(1),
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5),
        FLIP_DOWN(6),
        FLIP_UP(7),
        FLIP_NORTH(8),
        FLIP_SOUTH(9),
        FLIP_WEST(10),
        FLIP_EAST(11);

        public static final int COUNT;
        final int index;

        private SizeInfo(int j) {
            this.index = j;
        }

        static {
            COUNT = SizeInfo.values().length;
        }
    }

    @Environment(value=EnvType.CLIENT)
    protected static enum AdjacencyInfo {
        DOWN(new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}, 0.5f, true, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.WEST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_WEST, SizeInfo.NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_EAST, SizeInfo.SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.EAST, SizeInfo.SOUTH}),
        UP(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}, 1.0f, true, new SizeInfo[]{SizeInfo.EAST, SizeInfo.SOUTH, SizeInfo.EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_EAST, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.EAST, SizeInfo.NORTH, SizeInfo.EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_EAST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.NORTH, SizeInfo.WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_WEST, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.WEST, SizeInfo.SOUTH, SizeInfo.WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_WEST, SizeInfo.SOUTH}),
        NORTH(new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST}),
        SOUTH(new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP}, 0.8f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.FLIP_WEST, SizeInfo.FLIP_UP, SizeInfo.WEST, SizeInfo.UP, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_WEST, SizeInfo.FLIP_DOWN, SizeInfo.WEST, SizeInfo.DOWN, SizeInfo.WEST}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_EAST, SizeInfo.FLIP_DOWN, SizeInfo.EAST, SizeInfo.DOWN, SizeInfo.EAST}, new SizeInfo[]{SizeInfo.UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.FLIP_EAST, SizeInfo.FLIP_UP, SizeInfo.EAST, SizeInfo.UP, SizeInfo.EAST}),
        WEST(new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.UP, SizeInfo.SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_UP, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.UP, SizeInfo.NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.FLIP_DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.DOWN, SizeInfo.SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.SOUTH}),
        EAST(new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH}, 0.6f, true, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.SOUTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.FLIP_SOUTH, SizeInfo.DOWN, SizeInfo.SOUTH}, new SizeInfo[]{SizeInfo.FLIP_DOWN, SizeInfo.NORTH, SizeInfo.FLIP_DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.FLIP_NORTH, SizeInfo.DOWN, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.NORTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.FLIP_NORTH, SizeInfo.UP, SizeInfo.NORTH}, new SizeInfo[]{SizeInfo.FLIP_UP, SizeInfo.SOUTH, SizeInfo.FLIP_UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.FLIP_SOUTH, SizeInfo.UP, SizeInfo.SOUTH});

        final Direction[] corners;
        final boolean doNonCubicWeight;
        final SizeInfo[] vert0Weights;
        final SizeInfo[] vert1Weights;
        final SizeInfo[] vert2Weights;
        final SizeInfo[] vert3Weights;
        private static final AdjacencyInfo[] BY_FACING;

        private AdjacencyInfo(Direction[] directions, float f, boolean bl, SizeInfo[] sizeInfos, SizeInfo[] sizeInfos2, SizeInfo[] sizeInfos3, SizeInfo[] sizeInfos4) {
            this.corners = directions;
            this.doNonCubicWeight = bl;
            this.vert0Weights = sizeInfos;
            this.vert1Weights = sizeInfos2;
            this.vert2Weights = sizeInfos3;
            this.vert3Weights = sizeInfos4;
        }

        public static AdjacencyInfo fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AdjacencyInfo[6], adjacencyInfos -> {
                adjacencyInfos[Direction.DOWN.get3DDataValue()] = DOWN;
                adjacencyInfos[Direction.UP.get3DDataValue()] = UP;
                adjacencyInfos[Direction.NORTH.get3DDataValue()] = NORTH;
                adjacencyInfos[Direction.SOUTH.get3DDataValue()] = SOUTH;
                adjacencyInfos[Direction.WEST.get3DDataValue()] = WEST;
                adjacencyInfos[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }

    @Environment(value=EnvType.CLIENT)
    static enum AmbientVertexRemap {
        DOWN(0, 1, 2, 3),
        UP(2, 3, 0, 1),
        NORTH(3, 0, 1, 2),
        SOUTH(0, 1, 2, 3),
        WEST(3, 0, 1, 2),
        EAST(1, 2, 3, 0);

        final int vert0;
        final int vert1;
        final int vert2;
        final int vert3;
        private static final AmbientVertexRemap[] BY_FACING;

        private AmbientVertexRemap(int j, int k, int l, int m) {
            this.vert0 = j;
            this.vert1 = k;
            this.vert2 = l;
            this.vert3 = m;
        }

        public static AmbientVertexRemap fromFacing(Direction direction) {
            return BY_FACING[direction.get3DDataValue()];
        }

        static {
            BY_FACING = Util.make(new AmbientVertexRemap[6], ambientVertexRemaps -> {
                ambientVertexRemaps[Direction.DOWN.get3DDataValue()] = DOWN;
                ambientVertexRemaps[Direction.UP.get3DDataValue()] = UP;
                ambientVertexRemaps[Direction.NORTH.get3DDataValue()] = NORTH;
                ambientVertexRemaps[Direction.SOUTH.get3DDataValue()] = SOUTH;
                ambientVertexRemaps[Direction.WEST.get3DDataValue()] = WEST;
                ambientVertexRemaps[Direction.EAST.get3DDataValue()] = EAST;
            });
        }
    }
}

