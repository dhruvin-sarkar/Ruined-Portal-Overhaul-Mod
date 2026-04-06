/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ItemStackRenderState {
    ItemDisplayContext displayContext = ItemDisplayContext.NONE;
    private int activeLayerCount;
    private boolean animated;
    private boolean oversizedInGui;
    private @Nullable AABB cachedModelBoundingBox;
    private LayerRenderState[] layers = new LayerRenderState[]{new LayerRenderState()};

    public void ensureCapacity(int i) {
        int k = this.activeLayerCount + i;
        int j = this.layers.length;
        if (k > j) {
            this.layers = Arrays.copyOf(this.layers, k);
            for (int l = j; l < k; ++l) {
                this.layers[l] = new LayerRenderState();
            }
        }
    }

    public LayerRenderState newLayer() {
        this.ensureCapacity(1);
        return this.layers[this.activeLayerCount++];
    }

    public void clear() {
        this.displayContext = ItemDisplayContext.NONE;
        for (int i = 0; i < this.activeLayerCount; ++i) {
            this.layers[i].clear();
        }
        this.activeLayerCount = 0;
        this.animated = false;
        this.oversizedInGui = false;
        this.cachedModelBoundingBox = null;
    }

    public void setAnimated() {
        this.animated = true;
    }

    public boolean isAnimated() {
        return this.animated;
    }

    public void appendModelIdentityElement(Object object) {
    }

    private LayerRenderState firstLayer() {
        return this.layers[0];
    }

    public boolean isEmpty() {
        return this.activeLayerCount == 0;
    }

    public boolean usesBlockLight() {
        return this.firstLayer().usesBlockLight;
    }

    public @Nullable TextureAtlasSprite pickParticleIcon(RandomSource randomSource) {
        if (this.activeLayerCount == 0) {
            return null;
        }
        return this.layers[randomSource.nextInt((int)this.activeLayerCount)].particleIcon;
    }

    public void visitExtents(Consumer<Vector3fc> consumer) {
        Vector3f vector3f = new Vector3f();
        PoseStack.Pose pose = new PoseStack.Pose();
        for (int i = 0; i < this.activeLayerCount; ++i) {
            Vector3fc[] vector3fcs;
            LayerRenderState layerRenderState = this.layers[i];
            layerRenderState.transform.apply(this.displayContext.leftHand(), pose);
            Matrix4f matrix4f = pose.pose();
            for (Vector3fc vector3fc : vector3fcs = layerRenderState.extents.get()) {
                consumer.accept((Vector3fc)vector3f.set(vector3fc).mulPosition((Matrix4fc)matrix4f));
            }
            pose.setIdentity();
        }
    }

    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, int k) {
        for (int l = 0; l < this.activeLayerCount; ++l) {
            this.layers[l].submit(poseStack, submitNodeCollector, i, j, k);
        }
    }

    public AABB getModelBoundingBox() {
        AABB aABB;
        if (this.cachedModelBoundingBox != null) {
            return this.cachedModelBoundingBox;
        }
        AABB.Builder builder = new AABB.Builder();
        this.visitExtents(builder::include);
        this.cachedModelBoundingBox = aABB = builder.build();
        return aABB;
    }

    public void setOversizedInGui(boolean bl) {
        this.oversizedInGui = bl;
    }

    public boolean isOversizedInGui() {
        return this.oversizedInGui;
    }

    @Environment(value=EnvType.CLIENT)
    public class LayerRenderState {
        private static final Vector3fc[] NO_EXTENTS = new Vector3fc[0];
        public static final Supplier<Vector3fc[]> NO_EXTENTS_SUPPLIER = () -> NO_EXTENTS;
        private final List<BakedQuad> quads = new ArrayList<BakedQuad>();
        boolean usesBlockLight;
        @Nullable TextureAtlasSprite particleIcon;
        ItemTransform transform = ItemTransform.NO_TRANSFORM;
        private @Nullable RenderType renderType;
        private FoilType foilType = FoilType.NONE;
        private int[] tintLayers = new int[0];
        private @Nullable SpecialModelRenderer<Object> specialRenderer;
        private @Nullable Object argumentForSpecialRendering;
        Supplier<Vector3fc[]> extents = NO_EXTENTS_SUPPLIER;

        public void clear() {
            this.quads.clear();
            this.renderType = null;
            this.foilType = FoilType.NONE;
            this.specialRenderer = null;
            this.argumentForSpecialRendering = null;
            Arrays.fill(this.tintLayers, -1);
            this.usesBlockLight = false;
            this.particleIcon = null;
            this.transform = ItemTransform.NO_TRANSFORM;
            this.extents = NO_EXTENTS_SUPPLIER;
        }

        public List<BakedQuad> prepareQuadList() {
            return this.quads;
        }

        public void setRenderType(RenderType renderType) {
            this.renderType = renderType;
        }

        public void setUsesBlockLight(boolean bl) {
            this.usesBlockLight = bl;
        }

        public void setExtents(Supplier<Vector3fc[]> supplier) {
            this.extents = supplier;
        }

        public void setParticleIcon(TextureAtlasSprite textureAtlasSprite) {
            this.particleIcon = textureAtlasSprite;
        }

        public void setTransform(ItemTransform itemTransform) {
            this.transform = itemTransform;
        }

        public <T> void setupSpecialModel(SpecialModelRenderer<T> specialModelRenderer, @Nullable T object) {
            this.specialRenderer = LayerRenderState.eraseSpecialRenderer(specialModelRenderer);
            this.argumentForSpecialRendering = object;
        }

        private static SpecialModelRenderer<Object> eraseSpecialRenderer(SpecialModelRenderer<?> specialModelRenderer) {
            return specialModelRenderer;
        }

        public void setFoilType(FoilType foilType) {
            this.foilType = foilType;
        }

        public int[] prepareTintLayers(int i) {
            if (i > this.tintLayers.length) {
                this.tintLayers = new int[i];
                Arrays.fill(this.tintLayers, -1);
            }
            return this.tintLayers;
        }

        void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, int k) {
            poseStack.pushPose();
            this.transform.apply(ItemStackRenderState.this.displayContext.leftHand(), poseStack.last());
            if (this.specialRenderer != null) {
                this.specialRenderer.submit(this.argumentForSpecialRendering, ItemStackRenderState.this.displayContext, poseStack, submitNodeCollector, i, j, this.foilType != FoilType.NONE, k);
            } else if (this.renderType != null) {
                submitNodeCollector.submitItem(poseStack, ItemStackRenderState.this.displayContext, i, j, k, this.tintLayers, this.quads, this.renderType, this.foilType);
            }
            poseStack.popPose();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum FoilType {
        NONE,
        STANDARD,
        SPECIAL;

    }
}

