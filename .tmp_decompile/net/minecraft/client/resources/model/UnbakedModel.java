/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface UnbakedModel {
    public static final String PARTICLE_TEXTURE_REFERENCE = "particle";

    default public @Nullable Boolean ambientOcclusion() {
        return null;
    }

    default public @Nullable GuiLight guiLight() {
        return null;
    }

    default public @Nullable ItemTransforms transforms() {
        return null;
    }

    default public TextureSlots.Data textureSlots() {
        return TextureSlots.Data.EMPTY;
    }

    default public @Nullable UnbakedGeometry geometry() {
        return null;
    }

    default public @Nullable Identifier parent() {
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public static enum GuiLight {
        FRONT("front"),
        SIDE("side");

        private final String name;

        private GuiLight(String string2) {
            this.name = string2;
        }

        public static GuiLight getByName(String string) {
            for (GuiLight guiLight : GuiLight.values()) {
                if (!guiLight.name.equals(string)) continue;
                return guiLight;
            }
            throw new IllegalArgumentException("Invalid gui light: " + string);
        }

        public boolean lightLikeBlock() {
            return this == SIDE;
        }
    }
}

