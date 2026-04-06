/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.MatchException
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
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.statue.CopperGolemStatueModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.golem.CopperGolemOxidationLevels;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class CopperGolemStatueSpecialRenderer
implements NoDataSpecialModelRenderer {
    private static final Direction MODEL_STATE = Direction.SOUTH;
    private final CopperGolemStatueModel model;
    private final Identifier texture;

    public CopperGolemStatueSpecialRenderer(CopperGolemStatueModel copperGolemStatueModel, Identifier identifier) {
        this.model = copperGolemStatueModel;
        this.texture = identifier;
    }

    @Override
    public void submit(ItemDisplayContext itemDisplayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, int j, boolean bl, int k) {
        CopperGolemStatueSpecialRenderer.positionModel(poseStack);
        submitNodeCollector.submitModel(this.model, Direction.SOUTH, poseStack, RenderTypes.entityCutoutNoCull(this.texture), i, j, -1, null, k, null);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> consumer) {
        PoseStack poseStack = new PoseStack();
        CopperGolemStatueSpecialRenderer.positionModel(poseStack);
        this.model.setupAnim(MODEL_STATE);
        this.model.root().getExtentsForGui(poseStack, consumer);
    }

    private static void positionModel(PoseStack poseStack) {
        poseStack.translate(0.5f, 1.5f, 0.5f);
        poseStack.scale(-1.0f, -1.0f, 1.0f);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked(Identifier texture, CopperGolemStatueBlock.Pose pose) implements SpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Identifier.CODEC.fieldOf("texture").forGetter(Unbaked::texture), (App)CopperGolemStatueBlock.Pose.CODEC.fieldOf("pose").forGetter(Unbaked::pose)).apply((Applicative)instance, Unbaked::new));

        public Unbaked(WeatheringCopper.WeatherState weatherState, CopperGolemStatueBlock.Pose pose) {
            this(CopperGolemOxidationLevels.getOxidationLevel(weatherState).texture(), pose);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext bakingContext) {
            CopperGolemStatueModel copperGolemStatueModel = new CopperGolemStatueModel(bakingContext.entityModelSet().bakeLayer(Unbaked.getModel(this.pose)));
            return new CopperGolemStatueSpecialRenderer(copperGolemStatueModel, this.texture);
        }

        private static ModelLayerLocation getModel(CopperGolemStatueBlock.Pose pose) {
            return switch (pose) {
                default -> throw new MatchException(null, null);
                case CopperGolemStatueBlock.Pose.STANDING -> ModelLayers.COPPER_GOLEM;
                case CopperGolemStatueBlock.Pose.SITTING -> ModelLayers.COPPER_GOLEM_SITTING;
                case CopperGolemStatueBlock.Pose.STAR -> ModelLayers.COPPER_GOLEM_STAR;
                case CopperGolemStatueBlock.Pose.RUNNING -> ModelLayers.COPPER_GOLEM_RUNNING;
            };
        }
    }
}

