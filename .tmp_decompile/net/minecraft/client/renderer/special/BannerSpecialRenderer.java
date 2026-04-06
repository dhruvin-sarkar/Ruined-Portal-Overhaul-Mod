/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BannerSpecialRenderer
implements SpecialModelRenderer<BannerPatternLayers> {
    private final BannerRenderer bannerRenderer;
    private final DyeColor baseColor;

    public BannerSpecialRenderer(DyeColor dyeColor, BannerRenderer bannerRenderer) {
        this.bannerRenderer = bannerRenderer;
        this.baseColor = dyeColor;
    }

    @Override
    public @Nullable BannerPatternLayers extractArgument(ItemStack itemStack) {
        return itemStack.get(DataComponents.BANNER_PATTERNS);
    }

    @Override
    public void submit(@Nullable BannerPatternLayers bannerPatternLayers, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
        this.bannerRenderer.submitSpecial(poseStack, submitNodeCollector, i, j, this.baseColor, (BannerPatternLayers)Objects.requireNonNullElse((Object)bannerPatternLayers, (Object)BannerPatternLayers.EMPTY), k);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        this.bannerRenderer.getExtents(consumer);
    }

    @Override
    public /* synthetic */ @Nullable Object extractArgument(ItemStack itemStack) {
        return this.extractArgument(itemStack);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(DyeColor baseColor) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)DyeColor.CODEC.fieldOf("color").forGetter(Unbaked::baseColor)).apply((Applicative)instance, Unbaked::new));

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
            return new BannerSpecialRenderer(this.baseColor, new BannerRenderer(bakingContext));
        }
    }
}

