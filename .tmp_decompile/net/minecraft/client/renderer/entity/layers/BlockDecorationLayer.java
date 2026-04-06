/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.FlowerBedBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

@Environment(value=EnvType.CLIENT)
public class BlockDecorationLayer<S extends EntityRenderState, M extends EntityModel<S>>
extends RenderLayer<S, M> {
    private final Function<S, Optional<BlockState>> blockState;
    private final Consumer<PoseStack> transform;

    public BlockDecorationLayer(RenderLayerParent<S, M> renderLayerParent, Function<S, Optional<BlockState>> function, Consumer<PoseStack> consumer) {
        super(renderLayerParent);
        this.blockState = function;
        this.transform = consumer;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int i, S entityRenderState, float f, float g) {
        Optional<BlockState> optional = this.blockState.apply(entityRenderState);
        if (optional.isEmpty()) {
            return;
        }
        BlockState blockState = optional.get();
        Block block = blockState.getBlock();
        boolean bl = block instanceof CopperGolemStatueBlock;
        poseStack.pushPose();
        this.transform.accept(poseStack);
        if (!bl) {
            poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(180.0f));
        }
        if (bl || block instanceof AbstractSkullBlock || block instanceof AbstractBannerBlock || block instanceof AbstractChestBlock) {
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f));
        }
        if (block instanceof FlowerBedBlock) {
            poseStack.translate(-0.25, -1.5, -0.25);
        } else if (!bl) {
            poseStack.translate(-0.5, -1.5, -0.5);
        } else {
            poseStack.translate(-0.5, 0.0, -0.5);
        }
        submitNodeCollector.submitBlock(poseStack, blockState, i, OverlayTexture.NO_OVERLAY, ((EntityRenderState)entityRenderState).outlineColor);
        poseStack.popPose();
    }
}

