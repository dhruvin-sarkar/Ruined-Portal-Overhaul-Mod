/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelDebugName;

@Environment(value=EnvType.CLIENT)
public interface SpriteGetter {
    public TextureAtlasSprite get(Material var1, ModelDebugName var2);

    public TextureAtlasSprite reportMissingReference(String var1, ModelDebugName var2);

    default public TextureAtlasSprite resolveSlot(TextureSlots textureSlots, String string, ModelDebugName modelDebugName) {
        Material material = textureSlots.getMaterial(string);
        return material != null ? this.get(material, modelDebugName) : this.reportMissingReference(string, modelDebugName);
    }
}

