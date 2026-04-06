/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BlockDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.DisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemDisplayEntityRenderState;
import net.minecraft.client.renderer.entity.state.TextDisplayEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public abstract class DisplayRenderer<T extends Display, S, ST extends DisplayEntityRenderState>
extends EntityRenderer<T, ST> {
    private final EntityRenderDispatcher entityRenderDispatcher;

    protected DisplayRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
    }

    @Override
    protected AABB getBoundingBoxForCulling(T display) {
        return ((Display)display).getBoundingBoxForCulling();
    }

    @Override
    protected boolean affectedByCulling(T display) {
        return ((Display)display).affectedByCulling();
    }

    private static int getBrightnessOverride(Display display) {
        Display.RenderState renderState = display.renderState();
        return renderState != null ? renderState.brightnessOverride() : -1;
    }

    @Override
    protected int getSkyLightLevel(T display, BlockPos blockPos) {
        int i = DisplayRenderer.getBrightnessOverride(display);
        if (i != -1) {
            return LightTexture.sky(i);
        }
        return super.getSkyLightLevel(display, blockPos);
    }

    @Override
    protected int getBlockLightLevel(T display, BlockPos blockPos) {
        int i = DisplayRenderer.getBrightnessOverride(display);
        if (i != -1) {
            return LightTexture.block(i);
        }
        return super.getBlockLightLevel(display, blockPos);
    }

    @Override
    protected float getShadowRadius(ST displayEntityRenderState) {
        Display.RenderState renderState = ((DisplayEntityRenderState)displayEntityRenderState).renderState;
        if (renderState == null) {
            return 0.0f;
        }
        return renderState.shadowRadius().get(((DisplayEntityRenderState)displayEntityRenderState).interpolationProgress);
    }

    @Override
    protected float getShadowStrength(ST displayEntityRenderState) {
        Display.RenderState renderState = ((DisplayEntityRenderState)displayEntityRenderState).renderState;
        if (renderState == null) {
            return 0.0f;
        }
        return renderState.shadowStrength().get(((DisplayEntityRenderState)displayEntityRenderState).interpolationProgress);
    }

    @Override
    public void submit(ST displayEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        Display.RenderState renderState = ((DisplayEntityRenderState)displayEntityRenderState).renderState;
        if (renderState == null || !((DisplayEntityRenderState)displayEntityRenderState).hasSubState()) {
            return;
        }
        float f = ((DisplayEntityRenderState)displayEntityRenderState).interpolationProgress;
        super.submit(displayEntityRenderState, poseStack, submitNodeCollector, cameraRenderState);
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)this.calculateOrientation(renderState, displayEntityRenderState, new Quaternionf()));
        Transformation transformation = renderState.transformation().get(f);
        poseStack.mulPose(transformation.getMatrix());
        this.submitInner(displayEntityRenderState, poseStack, submitNodeCollector, ((DisplayEntityRenderState)displayEntityRenderState).lightCoords, f);
        poseStack.popPose();
    }

    private Quaternionf calculateOrientation(Display.RenderState renderState, ST displayEntityRenderState, Quaternionf quaternionf) {
        return switch (renderState.billboardConstraints()) {
            default -> throw new MatchException(null, null);
            case Display.BillboardConstraints.FIXED -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * ((DisplayEntityRenderState)displayEntityRenderState).entityYRot, (float)Math.PI / 180 * ((DisplayEntityRenderState)displayEntityRenderState).entityXRot, 0.0f);
            case Display.BillboardConstraints.HORIZONTAL -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * ((DisplayEntityRenderState)displayEntityRenderState).entityYRot, (float)Math.PI / 180 * DisplayRenderer.transformXRot(((DisplayEntityRenderState)displayEntityRenderState).cameraXRot), 0.0f);
            case Display.BillboardConstraints.VERTICAL -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * DisplayRenderer.transformYRot(((DisplayEntityRenderState)displayEntityRenderState).cameraYRot), (float)Math.PI / 180 * ((DisplayEntityRenderState)displayEntityRenderState).entityXRot, 0.0f);
            case Display.BillboardConstraints.CENTER -> quaternionf.rotationYXZ((float)(-Math.PI) / 180 * DisplayRenderer.transformYRot(((DisplayEntityRenderState)displayEntityRenderState).cameraYRot), (float)Math.PI / 180 * DisplayRenderer.transformXRot(((DisplayEntityRenderState)displayEntityRenderState).cameraXRot), 0.0f);
        };
    }

    private static float transformYRot(float f) {
        return f - 180.0f;
    }

    private static float transformXRot(float f) {
        return -f;
    }

    private static <T extends Display> float entityYRot(T display, float f) {
        return display.getYRot(f);
    }

    private static <T extends Display> float entityXRot(T display, float f) {
        return display.getXRot(f);
    }

    protected abstract void submitInner(ST var1, PoseStack var2, SubmitNodeCollector var3, int var4, float var5);

    @Override
    public void extractRenderState(T display, ST displayEntityRenderState, float f) {
        super.extractRenderState(display, displayEntityRenderState, f);
        ((DisplayEntityRenderState)displayEntityRenderState).renderState = ((Display)display).renderState();
        ((DisplayEntityRenderState)displayEntityRenderState).interpolationProgress = ((Display)display).calculateInterpolationProgress(f);
        ((DisplayEntityRenderState)displayEntityRenderState).entityYRot = DisplayRenderer.entityYRot(display, f);
        ((DisplayEntityRenderState)displayEntityRenderState).entityXRot = DisplayRenderer.entityXRot(display, f);
        Camera camera = this.entityRenderDispatcher.camera;
        ((DisplayEntityRenderState)displayEntityRenderState).cameraXRot = camera.xRot();
        ((DisplayEntityRenderState)displayEntityRenderState).cameraYRot = camera.yRot();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((ST)((DisplayEntityRenderState)entityRenderState));
    }

    @Override
    protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
        return this.getBlockLightLevel((T)((Display)entity), blockPos);
    }

    @Override
    protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
        return this.getSkyLightLevel((T)((Display)entity), blockPos);
    }

    @Environment(value=EnvType.CLIENT)
    public static class TextDisplayRenderer
    extends DisplayRenderer<Display.TextDisplay, Display.TextDisplay.TextRenderState, TextDisplayEntityRenderState> {
        private final Font font;

        protected TextDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.font = context.getFont();
        }

        @Override
        public TextDisplayEntityRenderState createRenderState() {
            return new TextDisplayEntityRenderState();
        }

        @Override
        public void extractRenderState(Display.TextDisplay textDisplay, TextDisplayEntityRenderState textDisplayEntityRenderState, float f) {
            super.extractRenderState(textDisplay, textDisplayEntityRenderState, f);
            textDisplayEntityRenderState.textRenderState = textDisplay.textRenderState();
            textDisplayEntityRenderState.cachedInfo = textDisplay.cacheDisplay(this::splitLines);
        }

        private Display.TextDisplay.CachedInfo splitLines(Component component, int i) {
            List<FormattedCharSequence> list = this.font.split(component, i);
            ArrayList<Display.TextDisplay.CachedLine> list2 = new ArrayList<Display.TextDisplay.CachedLine>(list.size());
            int j = 0;
            for (FormattedCharSequence formattedCharSequence : list) {
                int k = this.font.width(formattedCharSequence);
                j = Math.max(j, k);
                list2.add(new Display.TextDisplay.CachedLine(formattedCharSequence, k));
            }
            return new Display.TextDisplay.CachedInfo(list2, j);
        }

        @Override
        public void submitInner(TextDisplayEntityRenderState textDisplayEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, float f) {
            int j;
            float g;
            Display.TextDisplay.TextRenderState textRenderState = textDisplayEntityRenderState.textRenderState;
            byte b = textRenderState.flags();
            boolean bl = (b & 2) != 0;
            boolean bl2 = (b & 4) != 0;
            boolean bl3 = (b & 1) != 0;
            Display.TextDisplay.Align align = Display.TextDisplay.getAlign(b);
            byte c = (byte)textRenderState.textOpacity().get(f);
            if (bl2) {
                g = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
                j = (int)(g * 255.0f) << 24;
            } else {
                j = textRenderState.backgroundColor().get(f);
            }
            g = 0.0f;
            Matrix4f matrix4f = poseStack.last().pose();
            matrix4f.rotate((float)Math.PI, 0.0f, 1.0f, 0.0f);
            matrix4f.scale(-0.025f, -0.025f, -0.025f);
            Display.TextDisplay.CachedInfo cachedInfo = textDisplayEntityRenderState.cachedInfo;
            boolean k = true;
            int l = this.font.lineHeight + 1;
            int m = cachedInfo.width();
            int n = cachedInfo.lines().size() * l - 1;
            matrix4f.translate(1.0f - (float)m / 2.0f, (float)(-n), 0.0f);
            if (j != 0) {
                submitNodeCollector.submitCustomGeometry(poseStack, bl ? RenderTypes.textBackgroundSeeThrough() : RenderTypes.textBackground(), (pose, vertexConsumer) -> {
                    vertexConsumer.addVertex(pose, -1.0f, -1.0f, 0.0f).setColor(j).setLight(i);
                    vertexConsumer.addVertex(pose, -1.0f, (float)n, 0.0f).setColor(j).setLight(i);
                    vertexConsumer.addVertex(pose, (float)m, (float)n, 0.0f).setColor(j).setLight(i);
                    vertexConsumer.addVertex(pose, (float)m, -1.0f, 0.0f).setColor(j).setLight(i);
                });
            }
            OrderedSubmitNodeCollector orderedSubmitNodeCollector = submitNodeCollector.order(j != 0 ? 1 : 0);
            for (Display.TextDisplay.CachedLine cachedLine : cachedInfo.lines()) {
                float h = switch (align) {
                    default -> throw new MatchException(null, null);
                    case Display.TextDisplay.Align.LEFT -> 0.0f;
                    case Display.TextDisplay.Align.RIGHT -> m - cachedLine.width();
                    case Display.TextDisplay.Align.CENTER -> (float)m / 2.0f - (float)cachedLine.width() / 2.0f;
                };
                orderedSubmitNodeCollector.submitText(poseStack, h, g, cachedLine.contents(), bl3, bl ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET, i, c << 24 | 0xFFFFFF, 0, 0);
                g += (float)l;
            }
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
            return super.getShadowRadius((DisplayEntityRenderState)entityRenderState);
        }

        @Override
        protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
            return super.getBlockLightLevel((Display)entity, blockPos);
        }

        @Override
        protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
            return super.getSkyLightLevel((Display)entity, blockPos);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class ItemDisplayRenderer
    extends DisplayRenderer<Display.ItemDisplay, Display.ItemDisplay.ItemRenderState, ItemDisplayEntityRenderState> {
        private final ItemModelResolver itemModelResolver;

        protected ItemDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
            this.itemModelResolver = context.getItemModelResolver();
        }

        @Override
        public ItemDisplayEntityRenderState createRenderState() {
            return new ItemDisplayEntityRenderState();
        }

        @Override
        public void extractRenderState(Display.ItemDisplay itemDisplay, ItemDisplayEntityRenderState itemDisplayEntityRenderState, float f) {
            super.extractRenderState(itemDisplay, itemDisplayEntityRenderState, f);
            Display.ItemDisplay.ItemRenderState itemRenderState = itemDisplay.itemRenderState();
            if (itemRenderState != null) {
                this.itemModelResolver.updateForNonLiving(itemDisplayEntityRenderState.item, itemRenderState.itemStack(), itemRenderState.itemTransform(), itemDisplay);
            } else {
                itemDisplayEntityRenderState.item.clear();
            }
        }

        @Override
        public void submitInner(ItemDisplayEntityRenderState itemDisplayEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, float f) {
            if (itemDisplayEntityRenderState.item.isEmpty()) {
                return;
            }
            poseStack.mulPose((Quaternionfc)Axis.YP.rotation((float)Math.PI));
            itemDisplayEntityRenderState.item.submit(poseStack, submitNodeCollector, i, OverlayTexture.NO_OVERLAY, itemDisplayEntityRenderState.outlineColor);
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
            return super.getShadowRadius((DisplayEntityRenderState)entityRenderState);
        }

        @Override
        protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
            return super.getBlockLightLevel((Display)entity, blockPos);
        }

        @Override
        protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
            return super.getSkyLightLevel((Display)entity, blockPos);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static class BlockDisplayRenderer
    extends DisplayRenderer<Display.BlockDisplay, Display.BlockDisplay.BlockRenderState, BlockDisplayEntityRenderState> {
        protected BlockDisplayRenderer(EntityRendererProvider.Context context) {
            super(context);
        }

        @Override
        public BlockDisplayEntityRenderState createRenderState() {
            return new BlockDisplayEntityRenderState();
        }

        @Override
        public void extractRenderState(Display.BlockDisplay blockDisplay, BlockDisplayEntityRenderState blockDisplayEntityRenderState, float f) {
            super.extractRenderState(blockDisplay, blockDisplayEntityRenderState, f);
            blockDisplayEntityRenderState.blockRenderState = blockDisplay.blockRenderState();
        }

        @Override
        public void submitInner(BlockDisplayEntityRenderState blockDisplayEntityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, float f) {
            submitNodeCollector.submitBlock(poseStack, blockDisplayEntityRenderState.blockRenderState.blockState(), i, OverlayTexture.NO_OVERLAY, blockDisplayEntityRenderState.outlineColor);
        }

        @Override
        public /* synthetic */ EntityRenderState createRenderState() {
            return this.createRenderState();
        }

        @Override
        protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
            return super.getShadowRadius((DisplayEntityRenderState)entityRenderState);
        }

        @Override
        protected /* synthetic */ int getBlockLightLevel(Entity entity, BlockPos blockPos) {
            return super.getBlockLightLevel((Display)entity, blockPos);
        }

        @Override
        protected /* synthetic */ int getSkyLightLevel(Entity entity, BlockPos blockPos) {
            return super.getSkyLightLevel((Display)entity, blockPos);
        }
    }
}

