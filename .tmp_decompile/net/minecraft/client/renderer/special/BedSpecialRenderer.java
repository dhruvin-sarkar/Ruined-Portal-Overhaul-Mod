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
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class BedSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final BedRenderer bedRenderer;
    private final Material material;

    public BedSpecialRenderer(BedRenderer bedRenderer, Material material) {
        this.bedRenderer = bedRenderer;
        this.material = material;
    }

    @Override
    public void submit(ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
        this.bedRenderer.submitSpecial(poseStack, submitNodeCollector, i, j, this.material, k);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        this.bedRenderer.getExtents(consumer);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier texture) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture)).apply((Applicative)instance, Unbaked::new));

        public Unbaked(DyeColor dyeColor) {
            this(Sheets.colorToResourceMaterial(dyeColor));
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
            return new BedSpecialRenderer(new BedRenderer(bakingContext), Sheets.BED_MAPPER.apply(this.texture));
        }
    }
}

