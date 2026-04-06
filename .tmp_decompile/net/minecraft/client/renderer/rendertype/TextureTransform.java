/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 */
package net.minecraft.client.renderer.rendertype;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class TextureTransform {
    public static final double MAX_ENCHANTMENT_GLINT_SPEED_MILLIS = 8.0;
    private final String name;
    private final Supplier<Matrix4f> supplier;
    public static final TextureTransform DEFAULT_TEXTURING = new TextureTransform("default_texturing", Matrix4f::new);
    public static final TextureTransform GLINT_TEXTURING = new TextureTransform("glint_texturing", () -> TextureTransform.setupGlintTexturing(8.0f));
    public static final TextureTransform ENTITY_GLINT_TEXTURING = new TextureTransform("entity_glint_texturing", () -> TextureTransform.setupGlintTexturing(0.5f));
    public static final TextureTransform ARMOR_ENTITY_GLINT_TEXTURING = new TextureTransform("armor_entity_glint_texturing", () -> TextureTransform.setupGlintTexturing(0.16f));

    public TextureTransform(String string, Supplier<Matrix4f> supplier) {
        this.name = string;
        this.supplier = supplier;
    }

    public Matrix4f getMatrix() {
        return this.supplier.get();
    }

    public String toString() {
        return "TexturingStateShard[" + this.name + "]";
    }

    private static Matrix4f setupGlintTexturing(float f) {
        long l = (long)((double)Util.getMillis() * Minecraft.getInstance().options.glintSpeed().get() * 8.0);
        float g = (float)(l % 110000L) / 110000.0f;
        float h = (float)(l % 30000L) / 30000.0f;
        Matrix4f matrix4f = new Matrix4f().translation(-g, h, 0.0f);
        matrix4f.rotateZ(0.17453292f).scale(f);
        return matrix4f;
    }

    @Environment(value=EnvType.CLIENT)
    public static final class OffsetTextureTransform
    extends TextureTransform {
        public OffsetTextureTransform(float f, float g) {
            super("offset_texturing", () -> new Matrix4f().translation(f, g, 0.0f));
        }
    }
}

