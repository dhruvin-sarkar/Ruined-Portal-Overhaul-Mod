/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphBitmap;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import java.nio.file.Path;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedSheetGlyph;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FontTexture
extends AbstractTexture
implements Dumpable {
    private static final int SIZE = 256;
    private final GlyphRenderTypes renderTypes;
    private final boolean colored;
    private final Node root;

    public FontTexture(Supplier<String> supplier, GlyphRenderTypes glyphRenderTypes, boolean bl) {
        this.colored = bl;
        this.root = new Node(0, 0, 256, 256);
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.texture = gpuDevice.createTexture(supplier, 7, bl ? TextureFormat.RGBA8 : TextureFormat.RED8, 256, 256, 1, 1);
        this.sampler = RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST);
        this.textureView = gpuDevice.createTextureView(this.texture);
        this.renderTypes = glyphRenderTypes;
    }

    public @Nullable BakedSheetGlyph add(GlyphInfo glyphInfo, GlyphBitmap glyphBitmap) {
        if (glyphBitmap.isColored() != this.colored) {
            return null;
        }
        Node node = this.root.insert(glyphBitmap);
        if (node != null) {
            glyphBitmap.upload(node.x, node.y, this.getTexture());
            float f = 256.0f;
            float g = 256.0f;
            float h = 0.01f;
            return new BakedSheetGlyph(glyphInfo, this.renderTypes, this.getTextureView(), ((float)node.x + 0.01f) / 256.0f, ((float)node.x - 0.01f + (float)glyphBitmap.getPixelWidth()) / 256.0f, ((float)node.y + 0.01f) / 256.0f, ((float)node.y - 0.01f + (float)glyphBitmap.getPixelHeight()) / 256.0f, glyphBitmap.getLeft(), glyphBitmap.getRight(), glyphBitmap.getTop(), glyphBitmap.getBottom());
        }
        return null;
    }

    @Override
    public void dumpContents(Identifier identifier, Path path) {
        if (this.texture == null) {
            return;
        }
        String string = identifier.toDebugFileName();
        TextureUtil.writeAsPNG(path, string, this.texture, 0, i -> (i & 0xFF000000) == 0 ? -16777216 : i);
    }

    @Environment(value=EnvType.CLIENT)
    static class Node {
        final int x;
        final int y;
        private final int width;
        private final int height;
        private @Nullable Node left;
        private @Nullable Node right;
        private boolean occupied;

        Node(int i, int j, int k, int l) {
            this.x = i;
            this.y = j;
            this.width = k;
            this.height = l;
        }

        @Nullable Node insert(GlyphBitmap glyphBitmap) {
            if (this.left != null && this.right != null) {
                Node node = this.left.insert(glyphBitmap);
                if (node == null) {
                    node = this.right.insert(glyphBitmap);
                }
                return node;
            }
            if (this.occupied) {
                return null;
            }
            int i = glyphBitmap.getPixelWidth();
            int j = glyphBitmap.getPixelHeight();
            if (i > this.width || j > this.height) {
                return null;
            }
            if (i == this.width && j == this.height) {
                this.occupied = true;
                return this;
            }
            int k = this.width - i;
            int l = this.height - j;
            if (k > l) {
                this.left = new Node(this.x, this.y, i, this.height);
                this.right = new Node(this.x + i + 1, this.y, this.width - i - 1, this.height);
            } else {
                this.left = new Node(this.x, this.y, this.width, j);
                this.right = new Node(this.x, this.y + j + 1, this.width, this.height - j - 1);
            }
            return this.left.insert(glyphBitmap);
        }
    }
}

