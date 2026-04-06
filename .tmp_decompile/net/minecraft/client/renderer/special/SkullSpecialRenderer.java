/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
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
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.SkullBlock;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkullSpecialRenderer
implements NoDataSpecialModelRenderer {
    private final SkullModelBase model;
    private final float animation;
    private final RenderType renderType;

    public SkullSpecialRenderer(SkullModelBase skullModelBase, float f, RenderType renderType) {
        this.model = skullModelBase;
        this.animation = f;
        this.renderType = renderType;
    }

    @Override
    public void submit(ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
        SkullBlockRenderer.submitSkull(null, 180.0f, this.animation, poseStack, submitNodeCollector, i, this.model, this.renderType, k, null);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(0.5f, 0.0f, 0.5f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        SkullModelBase.State state = new SkullModelBase.State();
        state.animationPos = this.animation;
        state.yRot = 180.0f;
        this.model.setupAnim(state);
        this.model.root().getExtentsForGui(poseStack, consumer);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(SkullBlock.Type kind, Optional<Identifier> textureOverride, float animation) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)SkullBlock.Type.CODEC.fieldOf("kind").forGetter(Unbaked::kind), (App)Identifier.CODEC.optionalFieldOf("texture").forGetter(Unbaked::textureOverride), (App)Codec.FLOAT.optionalFieldOf("animation", (Object)Float.valueOf(0.0f)).forGetter(Unbaked::animation)).apply((Applicative)instance, Unbaked::new));

        public Unbaked(SkullBlock.Type type) {
            this(type, Optional.empty(), 0.0f);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public @Nullable SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
            SkullModelBase skullModelBase = SkullBlockRenderer.createModel(bakingContext.entityModelSet(), this.kind);
            Identifier identifier2 = this.textureOverride.map(identifier -> identifier.withPath(string -> "textures/entity/" + string + ".png")).orElse(null);
            if (skullModelBase == null) {
                return null;
            }
            RenderType renderType = SkullBlockRenderer.getSkullRenderType(this.kind, identifier2);
            return new SkullSpecialRenderer(skullModelBase, this.animation, renderType);
        }
    }
}

