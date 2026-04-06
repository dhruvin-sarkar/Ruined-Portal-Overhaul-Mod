/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer.block.model;

import com.mojang.serialization.Codec;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RandomSource;

@Environment(value=EnvType.CLIENT)
public class SingleVariant
implements BlockStateModel {
    private final BlockModelPart model;

    public SingleVariant(BlockModelPart blockModelPart) {
        this.model = blockModelPart;
    }

    @Override
    public void collectParts(RandomSource randomSource, List<BlockModelPart> list) {
        list.add(this.model);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return this.model.particleIcon();
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Variant variant) implements BlockStateModel.Unbaked
    {
        public static final Codec<Unbaked> CODEC = Variant.CODEC.xmap(Unbaked::new, Unbaked::variant);

        @Override
        public BlockStateModel bake(ModelBaker modelBaker) {
            return new SingleVariant(this.variant.bake(modelBaker));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.variant.resolveDependencies(resolver);
        }
    }
}

