/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.MaterialSet;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ScreenEffectRenderer {
    private static final Identifier UNDERWATER_LOCATION = Identifier.withDefaultNamespace("textures/misc/underwater.png");
    private final Minecraft minecraft;
    private final MaterialSet materials;
    private final MultiBufferSource bufferSource;
    public static final int ITEM_ACTIVATION_ANIMATION_LENGTH = 40;
    private @Nullable ItemStack itemActivationItem;
    private int itemActivationTicks;
    private float itemActivationOffX;
    private float itemActivationOffY;

    public ScreenEffectRenderer(Minecraft minecraft, MaterialSet materialSet, MultiBufferSource multiBufferSource) {
        this.minecraft = minecraft;
        this.materials = materialSet;
        this.bufferSource = multiBufferSource;
    }

    public void tick() {
        if (this.itemActivationTicks > 0) {
            --this.itemActivationTicks;
            if (this.itemActivationTicks == 0) {
                this.itemActivationItem = null;
            }
        }
    }

    public void renderScreenEffect(boolean bl, float f, SubmitNodeCollector submitNodeCollector) {
        PoseStack poseStack = new PoseStack();
        LocalPlayer player = this.minecraft.player;
        if (this.minecraft.options.getCameraType().isFirstPerson() && !bl) {
            BlockState blockState;
            if (!player.noPhysics && (blockState = ScreenEffectRenderer.getViewBlockingState(player)) != null) {
                ScreenEffectRenderer.renderTex(this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(blockState), poseStack, this.bufferSource);
            }
            if (!this.minecraft.player.isSpectator()) {
                if (this.minecraft.player.isEyeInFluid(FluidTags.WATER)) {
                    ScreenEffectRenderer.renderWater(this.minecraft, poseStack, this.bufferSource);
                }
                if (this.minecraft.player.isOnFire()) {
                    TextureAtlasSprite textureAtlasSprite = this.materials.get(ModelBakery.FIRE_1);
                    ScreenEffectRenderer.renderFire(poseStack, this.bufferSource, textureAtlasSprite);
                }
            }
        }
        if (!this.minecraft.options.hideGui) {
            this.renderItemActivationAnimation(poseStack, f, submitNodeCollector);
        }
    }

    private void renderItemActivationAnimation(PoseStack poseStack, float f, SubmitNodeCollector submitNodeCollector) {
        if (this.itemActivationItem == null || this.itemActivationTicks <= 0) {
            return;
        }
        int i = 40 - this.itemActivationTicks;
        float g = ((float)i + f) / 40.0f;
        float h = g * g;
        float j = g * h;
        float k = 10.25f * j * h - 24.95f * h * h + 25.5f * j - 13.8f * h + 4.0f * g;
        float l = k * (float)Math.PI;
        float m = (float)this.minecraft.getWindow().getWidth() / (float)this.minecraft.getWindow().getHeight();
        float n = this.itemActivationOffX * 0.3f * m;
        float o = this.itemActivationOffY * 0.3f;
        poseStack.pushPose();
        poseStack.translate(n * Mth.abs(Mth.sin(l * 2.0f)), o * Mth.abs(Mth.sin(l * 2.0f)), -10.0f + 9.0f * Mth.sin(l));
        float p = 0.8f;
        poseStack.scale(0.8f, 0.8f, 0.8f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(900.0f * Mth.abs(Mth.sin(l))));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(6.0f * Mth.cos(g * 8.0f)));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(6.0f * Mth.cos(g * 8.0f)));
        this.minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
        this.minecraft.getItemModelResolver().updateForTopItem(itemStackRenderState, this.itemActivationItem, ItemDisplayContext.FIXED, this.minecraft.level, null, 0);
        itemStackRenderState.submit(poseStack, submitNodeCollector, 0xF000F0, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public void resetItemActivation() {
        this.itemActivationItem = null;
    }

    public void displayItemActivation(ItemStack itemStack, RandomSource randomSource) {
        this.itemActivationItem = itemStack;
        this.itemActivationTicks = 40;
        this.itemActivationOffX = randomSource.nextFloat() * 2.0f - 1.0f;
        this.itemActivationOffY = randomSource.nextFloat() * 2.0f - 1.0f;
    }

    private static @Nullable BlockState getViewBlockingState(Player player) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 8; ++i) {
            double d = player.getX() + (double)(((float)((i >> 0) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            double e = player.getEyeY() + (double)(((float)((i >> 1) % 2) - 0.5f) * 0.1f * player.getScale());
            double f = player.getZ() + (double)(((float)((i >> 2) % 2) - 0.5f) * player.getBbWidth() * 0.8f);
            mutableBlockPos.set(d, e, f);
            BlockState blockState = player.level().getBlockState(mutableBlockPos);
            if (blockState.getRenderShape() == RenderShape.INVISIBLE || !blockState.isViewBlocking(player.level(), mutableBlockPos)) continue;
            return blockState;
        }
        return null;
    }

    private static void renderTex(TextureAtlasSprite textureAtlasSprite, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        float f = 0.1f;
        int i = ARGB.colorFromFloat(1.0f, 0.1f, 0.1f, 0.1f);
        float g = -1.0f;
        float h = 1.0f;
        float j = -1.0f;
        float k = 1.0f;
        float l = -0.5f;
        float m = textureAtlasSprite.getU0();
        float n = textureAtlasSprite.getU1();
        float o = textureAtlasSprite.getV0();
        float p = textureAtlasSprite.getV1();
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.blockScreenEffect(textureAtlasSprite.atlasLocation()));
        vertexConsumer.addVertex((Matrix4fc)matrix4f, -1.0f, -1.0f, -0.5f).setUv(n, p).setColor(i);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, 1.0f, -1.0f, -0.5f).setUv(m, p).setColor(i);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, 1.0f, 1.0f, -0.5f).setUv(m, o).setColor(i);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, -1.0f, 1.0f, -0.5f).setUv(n, o).setColor(i);
    }

    private static void renderWater(Minecraft minecraft, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        BlockPos blockPos = BlockPos.containing(minecraft.player.getX(), minecraft.player.getEyeY(), minecraft.player.getZ());
        float f = LightTexture.getBrightness(minecraft.player.level().dimensionType(), minecraft.player.level().getMaxLocalRawBrightness(blockPos));
        int i = ARGB.colorFromFloat(0.1f, f, f, f);
        float g = 4.0f;
        float h = -1.0f;
        float j = 1.0f;
        float k = -1.0f;
        float l = 1.0f;
        float m = -0.5f;
        float n = -minecraft.player.getYRot() / 64.0f;
        float o = minecraft.player.getXRot() / 64.0f;
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.blockScreenEffect(UNDERWATER_LOCATION));
        vertexConsumer.addVertex((Matrix4fc)matrix4f, -1.0f, -1.0f, -0.5f).setUv(4.0f + n, 4.0f + o).setColor(i);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, 1.0f, -1.0f, -0.5f).setUv(0.0f + n, 4.0f + o).setColor(i);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, 1.0f, 1.0f, -0.5f).setUv(0.0f + n, 0.0f + o).setColor(i);
        vertexConsumer.addVertex((Matrix4fc)matrix4f, -1.0f, 1.0f, -0.5f).setUv(4.0f + n, 0.0f + o).setColor(i);
    }

    private static void renderFire(PoseStack poseStack, MultiBufferSource multiBufferSource, TextureAtlasSprite textureAtlasSprite) {
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderTypes.fireScreenEffect(textureAtlasSprite.atlasLocation()));
        float f = textureAtlasSprite.getU0();
        float g = textureAtlasSprite.getU1();
        float h = textureAtlasSprite.getV0();
        float i = textureAtlasSprite.getV1();
        float j = 1.0f;
        for (int k = 0; k < 2; ++k) {
            poseStack.pushPose();
            float l = -0.5f;
            float m = 0.5f;
            float n = -0.5f;
            float o = 0.5f;
            float p = -0.5f;
            poseStack.translate((float)(-(k * 2 - 1)) * 0.24f, -0.3f, 0.0f);
            poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)(k * 2 - 1) * 10.0f));
            Matrix4f matrix4f = poseStack.last().pose();
            vertexConsumer.addVertex((Matrix4fc)matrix4f, -0.5f, -0.5f, -0.5f).setUv(g, i).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, 0.5f, -0.5f, -0.5f).setUv(f, i).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, 0.5f, 0.5f, -0.5f).setUv(f, h).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            vertexConsumer.addVertex((Matrix4fc)matrix4f, -0.5f, 0.5f, -0.5f).setUv(g, h).setColor(1.0f, 1.0f, 1.0f, 0.9f);
            poseStack.popPose();
        }
    }
}

