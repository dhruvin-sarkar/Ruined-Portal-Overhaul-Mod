/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.render.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.gui.render.state.ScreenArea;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class GuiRenderState {
    private static final int DEBUG_RECTANGLE_COLOR = 0x774444FF;
    private final List<Node> strata = new ArrayList<Node>();
    private int firstStratumAfterBlur = Integer.MAX_VALUE;
    private Node current;
    private final Set<Object> itemModelIdentities = new HashSet<Object>();
    private @Nullable ScreenRectangle lastElementBounds;

    public GuiRenderState() {
        this.nextStratum();
    }

    public void nextStratum() {
        this.current = new Node(null);
        this.strata.add(this.current);
    }

    public void blurBeforeThisStratum() {
        if (this.firstStratumAfterBlur != Integer.MAX_VALUE) {
            throw new IllegalStateException("Can only blur once per frame");
        }
        this.firstStratumAfterBlur = this.strata.size() - 1;
    }

    public void up() {
        if (this.current.up == null) {
            this.current.up = new Node(this.current);
        }
        this.current = this.current.up;
    }

    public void submitItem(GuiItemRenderState guiItemRenderState) {
        if (!this.findAppropriateNode(guiItemRenderState)) {
            return;
        }
        this.itemModelIdentities.add(guiItemRenderState.itemStackRenderState().getModelIdentity());
        this.current.submitItem(guiItemRenderState);
        this.sumbitDebugRectangleIfEnabled(guiItemRenderState.bounds());
    }

    public void submitText(GuiTextRenderState guiTextRenderState) {
        if (!this.findAppropriateNode(guiTextRenderState)) {
            return;
        }
        this.current.submitText(guiTextRenderState);
        this.sumbitDebugRectangleIfEnabled(guiTextRenderState.bounds());
    }

    public void submitPicturesInPictureState(PictureInPictureRenderState pictureInPictureRenderState) {
        if (!this.findAppropriateNode(pictureInPictureRenderState)) {
            return;
        }
        this.current.submitPicturesInPictureState(pictureInPictureRenderState);
        this.sumbitDebugRectangleIfEnabled(pictureInPictureRenderState.bounds());
    }

    public void submitGuiElement(GuiElementRenderState guiElementRenderState) {
        if (!this.findAppropriateNode(guiElementRenderState)) {
            return;
        }
        this.current.submitGuiElement(guiElementRenderState);
        this.sumbitDebugRectangleIfEnabled(guiElementRenderState.bounds());
    }

    private void sumbitDebugRectangleIfEnabled(@Nullable ScreenRectangle screenRectangle) {
        if (!SharedConstants.DEBUG_RENDER_UI_LAYERING_RECTANGLES || screenRectangle == null) {
            return;
        }
        this.up();
        this.current.submitGuiElement(new ColoredRectangleRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), (Matrix3x2fc)new Matrix3x2f(), 0, 0, 10000, 10000, 0x774444FF, 0x774444FF, screenRectangle));
    }

    private boolean findAppropriateNode(ScreenArea screenArea) {
        ScreenRectangle screenRectangle = screenArea.bounds();
        if (screenRectangle == null) {
            return false;
        }
        if (this.lastElementBounds != null && this.lastElementBounds.encompasses(screenRectangle)) {
            this.up();
        } else {
            this.navigateToAboveHighestElementWithIntersectingBounds(screenRectangle);
        }
        this.lastElementBounds = screenRectangle;
        return true;
    }

    private void navigateToAboveHighestElementWithIntersectingBounds(ScreenRectangle screenRectangle) {
        Node node = (Node)this.strata.getLast();
        while (node.up != null) {
            node = node.up;
        }
        boolean bl = false;
        while (!bl) {
            boolean bl2 = bl = this.hasIntersection(screenRectangle, node.elementStates) || this.hasIntersection(screenRectangle, node.itemStates) || this.hasIntersection(screenRectangle, node.textStates) || this.hasIntersection(screenRectangle, node.picturesInPictureStates);
            if (node.parent == null) break;
            if (bl) continue;
            node = node.parent;
        }
        this.current = node;
        if (bl) {
            this.up();
        }
    }

    private boolean hasIntersection(ScreenRectangle screenRectangle, @Nullable List<? extends ScreenArea> list) {
        if (list != null) {
            for (ScreenArea screenArea : list) {
                ScreenRectangle screenRectangle2 = screenArea.bounds();
                if (screenRectangle2 == null || !screenRectangle2.intersects(screenRectangle)) continue;
                return true;
            }
        }
        return false;
    }

    public void submitBlitToCurrentLayer(BlitRenderState blitRenderState) {
        this.current.submitGuiElement(blitRenderState);
    }

    public void submitGlyphToCurrentLayer(GuiElementRenderState guiElementRenderState) {
        this.current.submitGlyph(guiElementRenderState);
    }

    public Set<Object> getItemModelIdentities() {
        return this.itemModelIdentities;
    }

    public void forEachElement(Consumer<GuiElementRenderState> consumer, TraverseRange traverseRange) {
        this.traverse((Node node) -> {
            if (node.elementStates == null && node.glyphStates == null) {
                return;
            }
            if (node.elementStates != null) {
                for (GuiElementRenderState guiElementRenderState : node.elementStates) {
                    consumer.accept(guiElementRenderState);
                }
            }
            if (node.glyphStates != null) {
                for (GuiElementRenderState guiElementRenderState : node.glyphStates) {
                    consumer.accept(guiElementRenderState);
                }
            }
        }, traverseRange);
    }

    public void forEachItem(Consumer<GuiItemRenderState> consumer) {
        Node node2 = this.current;
        this.traverse((Node node) -> {
            if (node.itemStates != null) {
                this.current = node;
                for (GuiItemRenderState guiItemRenderState : node.itemStates) {
                    consumer.accept(guiItemRenderState);
                }
            }
        }, TraverseRange.ALL);
        this.current = node2;
    }

    public void forEachText(Consumer<GuiTextRenderState> consumer) {
        Node node2 = this.current;
        this.traverse((Node node) -> {
            if (node.textStates != null) {
                for (GuiTextRenderState guiTextRenderState : node.textStates) {
                    this.current = node;
                    consumer.accept(guiTextRenderState);
                }
            }
        }, TraverseRange.ALL);
        this.current = node2;
    }

    public void forEachPictureInPicture(Consumer<PictureInPictureRenderState> consumer) {
        Node node2 = this.current;
        this.traverse((Node node) -> {
            if (node.picturesInPictureStates != null) {
                this.current = node;
                for (PictureInPictureRenderState pictureInPictureRenderState : node.picturesInPictureStates) {
                    consumer.accept(pictureInPictureRenderState);
                }
            }
        }, TraverseRange.ALL);
        this.current = node2;
    }

    public void sortElements(Comparator<GuiElementRenderState> comparator) {
        this.traverse((Node node) -> {
            if (node.elementStates != null) {
                if (SharedConstants.DEBUG_SHUFFLE_UI_RENDERING_ORDER) {
                    Collections.shuffle(node.elementStates);
                }
                node.elementStates.sort(comparator);
            }
        }, TraverseRange.ALL);
    }

    private void traverse(Consumer<Node> consumer, TraverseRange traverseRange) {
        int i = 0;
        int j = this.strata.size();
        if (traverseRange == TraverseRange.BEFORE_BLUR) {
            j = Math.min(this.firstStratumAfterBlur, this.strata.size());
        } else if (traverseRange == TraverseRange.AFTER_BLUR) {
            i = this.firstStratumAfterBlur;
        }
        for (int k = i; k < j; ++k) {
            Node node = this.strata.get(k);
            this.traverse(node, consumer);
        }
    }

    private void traverse(Node node, Consumer<Node> consumer) {
        consumer.accept(node);
        if (node.up != null) {
            this.traverse(node.up, consumer);
        }
    }

    public void reset() {
        this.itemModelIdentities.clear();
        this.strata.clear();
        this.firstStratumAfterBlur = Integer.MAX_VALUE;
        this.nextStratum();
    }

    @Environment(value=EnvType.CLIENT)
    static class Node {
        public final @Nullable Node parent;
        public @Nullable Node up;
        public @Nullable List<GuiElementRenderState> elementStates;
        public @Nullable List<GuiElementRenderState> glyphStates;
        public @Nullable List<GuiItemRenderState> itemStates;
        public @Nullable List<GuiTextRenderState> textStates;
        public @Nullable List<PictureInPictureRenderState> picturesInPictureStates;

        Node(@Nullable Node node) {
            this.parent = node;
        }

        public void submitItem(GuiItemRenderState guiItemRenderState) {
            if (this.itemStates == null) {
                this.itemStates = new ArrayList<GuiItemRenderState>();
            }
            this.itemStates.add(guiItemRenderState);
        }

        public void submitText(GuiTextRenderState guiTextRenderState) {
            if (this.textStates == null) {
                this.textStates = new ArrayList<GuiTextRenderState>();
            }
            this.textStates.add(guiTextRenderState);
        }

        public void submitPicturesInPictureState(PictureInPictureRenderState pictureInPictureRenderState) {
            if (this.picturesInPictureStates == null) {
                this.picturesInPictureStates = new ArrayList<PictureInPictureRenderState>();
            }
            this.picturesInPictureStates.add(pictureInPictureRenderState);
        }

        public void submitGuiElement(GuiElementRenderState guiElementRenderState) {
            if (this.elementStates == null) {
                this.elementStates = new ArrayList<GuiElementRenderState>();
            }
            this.elementStates.add(guiElementRenderState);
        }

        public void submitGlyph(GuiElementRenderState guiElementRenderState) {
            if (this.glyphStates == null) {
                this.glyphStates = new ArrayList<GuiElementRenderState>();
            }
            this.glyphStates.add(guiElementRenderState);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum TraverseRange {
        ALL,
        BEFORE_BLUR,
        AFTER_BLUR;

    }
}

