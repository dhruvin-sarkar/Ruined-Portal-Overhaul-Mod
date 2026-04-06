/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.block.model;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface BlockModelPart {
    public List<BakedQuad> getQuads(@Nullable Direction var1);

    public boolean useAmbientOcclusion();

    public TextureAtlasSprite particleIcon();

    @Environment(value=EnvType.CLIENT)
    public static interface Unbaked
    extends ResolvableModel {
        public BlockModelPart bake(ModelBaker var1);
    }
}

