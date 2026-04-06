/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class LightTexture
implements AutoCloseable {
    public static final int FULL_BRIGHT = 0xF000F0;
    public static final int FULL_SKY = 0xF00000;
    public static final int FULL_BLOCK = 240;
    private static final int TEXTURE_SIZE = 16;
    private static final int LIGHTMAP_UBO_SIZE = new Std140SizeCalculator().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().putFloat().putVec3().putVec3().get();
    private final GpuTexture texture;
    private final GpuTextureView textureView;
    private boolean updateLightTexture;
    private float blockLightRedFlicker;
    private final GameRenderer renderer;
    private final Minecraft minecraft;
    private final MappableRingBuffer ubo;
    private final RandomSource randomSource = RandomSource.create();

    public LightTexture(GameRenderer gameRenderer, Minecraft minecraft) {
        this.renderer = gameRenderer;
        this.minecraft = minecraft;
        GpuDevice gpuDevice = RenderSystem.getDevice();
        this.texture = gpuDevice.createTexture("Light Texture", 12, TextureFormat.RGBA8, 16, 16, 1, 1);
        this.textureView = gpuDevice.createTextureView(this.texture);
        gpuDevice.createCommandEncoder().clearColorTexture(this.texture, -1);
        this.ubo = new MappableRingBuffer(() -> "Lightmap UBO", 130, LIGHTMAP_UBO_SIZE);
    }

    public GpuTextureView getTextureView() {
        return this.textureView;
    }

    @Override
    public void close() {
        this.texture.close();
        this.textureView.close();
        this.ubo.close();
    }

    public void tick() {
        this.blockLightRedFlicker += (this.randomSource.nextFloat() - this.randomSource.nextFloat()) * this.randomSource.nextFloat() * this.randomSource.nextFloat() * 0.1f;
        this.blockLightRedFlicker *= 0.9f;
        this.updateLightTexture = true;
    }

    private float calculateDarknessScale(LivingEntity livingEntity, float f, float g) {
        float h = 0.45f * f;
        return Math.max(0.0f, Mth.cos(((float)livingEntity.tickCount - g) * (float)Math.PI * 0.025f) * h);
    }

    public void updateLightTexture(float f) {
        float j;
        Vector3f vector3f;
        if (!this.updateLightTexture) {
            return;
        }
        this.updateLightTexture = false;
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("lightTex");
        ClientLevel clientLevel = this.minecraft.level;
        if (clientLevel == null) {
            return;
        }
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        int i = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_LIGHT_COLOR, f);
        float g = clientLevel.dimensionType().ambientLight();
        float h = camera.attributeProbe().getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, f).floatValue();
        EndFlashState endFlashState = clientLevel.endFlashState();
        if (endFlashState != null) {
            vector3f = new Vector3f(0.99f, 1.12f, 1.0f);
            if (!this.minecraft.options.hideLightningFlash().get().booleanValue()) {
                j = endFlashState.getIntensity(f);
                h = this.minecraft.gui.getBossOverlay().shouldCreateWorldFog() ? (h += j / 3.0f) : (h += j);
            }
        } else {
            vector3f = new Vector3f(1.0f, 1.0f, 1.0f);
        }
        j = this.minecraft.options.darknessEffectScale().get().floatValue();
        float k = this.minecraft.player.getEffectBlendFactor(MobEffects.DARKNESS, f) * j;
        float l = this.calculateDarknessScale(this.minecraft.player, k, f) * j;
        float m = this.minecraft.player.getWaterVision();
        float n = this.minecraft.player.hasEffect(MobEffects.NIGHT_VISION) ? GameRenderer.getNightVisionScale(this.minecraft.player, f) : (m > 0.0f && this.minecraft.player.hasEffect(MobEffects.CONDUIT_POWER) ? m : 0.0f);
        float o = this.blockLightRedFlicker + 1.5f;
        float p = this.minecraft.options.gamma().get().floatValue();
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(this.ubo.currentBuffer(), false, true);){
            Std140Builder.intoBuffer(mappedView.data()).putFloat(g).putFloat(h).putFloat(o).putFloat(n).putFloat(l).putFloat(this.renderer.getDarkenWorldAmount(f)).putFloat(Math.max(0.0f, p - k)).putVec3((Vector3fc)ARGB.vector3fFromRGB24(i)).putVec3((Vector3fc)vector3f);
        }
        try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Update light", this.textureView, OptionalInt.empty());){
            renderPass.setPipeline(RenderPipelines.LIGHTMAP);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("LightmapInfo", this.ubo.currentBuffer());
            renderPass.draw(0, 3);
        }
        this.ubo.rotate();
        profilerFiller.pop();
    }

    public static float getBrightness(DimensionType dimensionType, int i) {
        return LightTexture.getBrightness(dimensionType.ambientLight(), i);
    }

    public static float getBrightness(float f, int i) {
        float g = (float)i / 15.0f;
        float h = g / (4.0f - 3.0f * g);
        return Mth.lerp(f, h, 1.0f);
    }

    public static int pack(int i, int j) {
        return i << 4 | j << 20;
    }

    public static int block(int i) {
        return i >>> 4 & 0xF;
    }

    public static int sky(int i) {
        return i >>> 20 & 0xF;
    }

    public static int lightCoordsWithEmission(int i, int j) {
        if (j == 0) {
            return i;
        }
        int k = Math.max(LightTexture.sky(i), j);
        int l = Math.max(LightTexture.block(i), j);
        return LightTexture.pack(l, k);
    }
}

