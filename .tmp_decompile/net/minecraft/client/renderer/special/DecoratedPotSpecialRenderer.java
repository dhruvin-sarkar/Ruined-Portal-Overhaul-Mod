/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.PotDecorations;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DecoratedPotSpecialRenderer
implements SpecialModelRenderer<PotDecorations> {
    private final DecoratedPotRenderer decoratedPotRenderer;

    public DecoratedPotSpecialRenderer(DecoratedPotRenderer decoratedPotRenderer) {
        this.decoratedPotRenderer = decoratedPotRenderer;
    }

    @Override
    public @Nullable PotDecorations extractArgument(ItemStack itemStack) {
        return itemStack.get(DataComponents.POT_DECORATIONS);
    }

    @Override
    public void submit(@Nullable PotDecorations potDecorations, ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
        this.decoratedPotRenderer.submit(poseStack, submitNodeCollector, i, j, (PotDecorations)Objects.requireNonNullElse((Object)potDecorations, (Object)PotDecorations.EMPTY), k);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        this.decoratedPotRenderer.getExtents(consumer);
    }

    @Override
    public /* synthetic */ @Nullable Object extractArgument(ItemStack itemStack) {
        return this.extractArgument(itemStack);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked() implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit((Object)new Unbaked());

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
            return new DecoratedPotSpecialRenderer(new DecoratedPotRenderer(bakingContext));
        }
    }
}

