/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.EndFlashState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributeProbe;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.dimension.DimensionType;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

@Environment(value=EnvType.CLIENT)
public class SkyRenderer
implements AutoCloseable {
    private static final Identifier SUN_SPRITE = Identifier.withDefaultNamespace("sun");
    private static final Identifier END_FLASH_SPRITE = Identifier.withDefaultNamespace("end_flash");
    private static final Identifier END_SKY_LOCATION = Identifier.withDefaultNamespace("textures/environment/end_sky.png");
    private static final float SKY_DISC_RADIUS = 512.0f;
    private static final int SKY_VERTICES = 10;
    private static final int STAR_COUNT = 1500;
    private static final float SUN_SIZE = 30.0f;
    private static final float SUN_HEIGHT = 100.0f;
    private static final float MOON_SIZE = 20.0f;
    private static final float MOON_HEIGHT = 100.0f;
    private static final int SUNRISE_STEPS = 16;
    private static final int END_SKY_QUAD_COUNT = 6;
    private static final float END_FLASH_HEIGHT = 100.0f;
    private static final float END_FLASH_SCALE = 60.0f;
    private final TextureAtlas celestialsAtlas;
    private final GpuBuffer starBuffer;
    private final GpuBuffer topSkyBuffer;
    private final GpuBuffer bottomSkyBuffer;
    private final GpuBuffer endSkyBuffer;
    private final GpuBuffer sunBuffer;
    private final GpuBuffer moonBuffer;
    private final GpuBuffer sunriseBuffer;
    private final GpuBuffer endFlashBuffer;
    private final RenderSystem.AutoStorageIndexBuffer quadIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
    private final AbstractTexture endSkyTexture;
    private int starIndexCount;

    public SkyRenderer(TextureManager textureManager, AtlasManager atlasManager) {
        this.celestialsAtlas = atlasManager.getAtlasOrThrow(AtlasIds.CELESTIALS);
        this.starBuffer = this.buildStars();
        this.endSkyBuffer = SkyRenderer.buildEndSky();
        this.endSkyTexture = this.getTexture(textureManager, END_SKY_LOCATION);
        this.endFlashBuffer = SkyRenderer.buildEndFlashQuad(this.celestialsAtlas);
        this.sunBuffer = SkyRenderer.buildSunQuad(this.celestialsAtlas);
        this.moonBuffer = SkyRenderer.buildMoonPhases(this.celestialsAtlas);
        this.sunriseBuffer = this.buildSunriseFan();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(10 * DefaultVertexFormat.POSITION.getVertexSize());){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            this.buildSkyDisc(bufferBuilder, 16.0f);
            try (MeshData meshData = bufferBuilder.buildOrThrow();){
                this.topSkyBuffer = RenderSystem.getDevice().createBuffer(() -> "Top sky vertex buffer", 32, meshData.vertexBuffer());
            }
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            this.buildSkyDisc(bufferBuilder, -16.0f);
            meshData = bufferBuilder.buildOrThrow();
            try {
                this.bottomSkyBuffer = RenderSystem.getDevice().createBuffer(() -> "Bottom sky vertex buffer", 32, meshData.vertexBuffer());
            }
            finally {
                if (meshData != null) {
                    meshData.close();
                }
            }
        }
    }

    private AbstractTexture getTexture(TextureManager textureManager, Identifier identifier) {
        return textureManager.getTexture(identifier);
    }

    private GpuBuffer buildSunriseFan() {
        int i = 18;
        int j = DefaultVertexFormat.POSITION_COLOR.getVertexSize();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(18 * j);){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
            int k = ARGB.white(1.0f);
            int l = ARGB.white(0.0f);
            bufferBuilder.addVertex(0.0f, 100.0f, 0.0f).setColor(k);
            for (int m = 0; m <= 16; ++m) {
                float f = (float)m * ((float)Math.PI * 2) / 16.0f;
                float g = Mth.sin(f);
                float h = Mth.cos(f);
                bufferBuilder.addVertex(g * 120.0f, h * 120.0f, -h * 40.0f).setColor(l);
            }
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Sunrise/Sunset fan", 32, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return gpuBuffer;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private static GpuBuffer buildSunQuad(TextureAtlas textureAtlas) {
        return SkyRenderer.buildCelestialQuad("Sun quad", textureAtlas.getSprite(SUN_SPRITE));
    }

    private static GpuBuffer buildEndFlashQuad(TextureAtlas textureAtlas) {
        return SkyRenderer.buildCelestialQuad("End flash quad", textureAtlas.getSprite(END_FLASH_SPRITE));
    }

    private static GpuBuffer buildCelestialQuad(String string, TextureAtlasSprite textureAtlasSprite) {
        VertexFormat vertexFormat = DefaultVertexFormat.POSITION_TEX;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(4 * vertexFormat.getVertexSize());){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, vertexFormat);
            bufferBuilder.addVertex(-1.0f, 0.0f, -1.0f).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV0());
            bufferBuilder.addVertex(1.0f, 0.0f, -1.0f).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV0());
            bufferBuilder.addVertex(1.0f, 0.0f, 1.0f).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV1());
            bufferBuilder.addVertex(-1.0f, 0.0f, 1.0f).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV1());
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> string, 32, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return gpuBuffer;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private static GpuBuffer buildMoonPhases(TextureAtlas textureAtlas) {
        MoonPhase[] moonPhases = MoonPhase.values();
        VertexFormat vertexFormat = DefaultVertexFormat.POSITION_TEX;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(moonPhases.length * 4 * vertexFormat.getVertexSize());){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, vertexFormat);
            for (MoonPhase moonPhase : moonPhases) {
                TextureAtlasSprite textureAtlasSprite = textureAtlas.getSprite(Identifier.withDefaultNamespace("moon/" + moonPhase.getSerializedName()));
                bufferBuilder.addVertex(-1.0f, 0.0f, -1.0f).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV1());
                bufferBuilder.addVertex(1.0f, 0.0f, -1.0f).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV1());
                bufferBuilder.addVertex(1.0f, 0.0f, 1.0f).setUv(textureAtlasSprite.getU0(), textureAtlasSprite.getV0());
                bufferBuilder.addVertex(-1.0f, 0.0f, 1.0f).setUv(textureAtlasSprite.getU1(), textureAtlasSprite.getV0());
            }
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Moon phases", 32, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return gpuBuffer;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private GpuBuffer buildStars() {
        RandomSource randomSource = RandomSource.create(10842L);
        float f = 100.0f;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 1500 * 4);){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            for (int i = 0; i < 1500; ++i) {
                float g = randomSource.nextFloat() * 2.0f - 1.0f;
                float h = randomSource.nextFloat() * 2.0f - 1.0f;
                float j = randomSource.nextFloat() * 2.0f - 1.0f;
                float k = 0.15f + randomSource.nextFloat() * 0.1f;
                float l = Mth.lengthSquared(g, h, j);
                if (l <= 0.010000001f || l >= 1.0f) continue;
                Vector3f vector3f = new Vector3f(g, h, j).normalize(100.0f);
                float m = (float)(randomSource.nextDouble() * 3.1415927410125732 * 2.0);
                Matrix3f matrix3f = new Matrix3f().rotateTowards((Vector3fc)new Vector3f((Vector3fc)vector3f).negate(), (Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f)).rotateZ(-m);
                bufferBuilder.addVertex((Vector3fc)new Vector3f(k, -k, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
                bufferBuilder.addVertex((Vector3fc)new Vector3f(k, k, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
                bufferBuilder.addVertex((Vector3fc)new Vector3f(-k, k, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
                bufferBuilder.addVertex((Vector3fc)new Vector3f(-k, -k, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
            }
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                this.starIndexCount = meshData.drawState().indexCount();
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Stars vertex buffer", 40, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return gpuBuffer;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private void buildSkyDisc(VertexConsumer vertexConsumer, float f) {
        float g = Math.signum(f) * 512.0f;
        vertexConsumer.addVertex(0.0f, f, 0.0f);
        for (int i = -180; i <= 180; i += 45) {
            vertexConsumer.addVertex(g * Mth.cos((float)i * ((float)Math.PI / 180)), f, 512.0f * Mth.sin((float)i * ((float)Math.PI / 180)));
        }
    }

    private static GpuBuffer buildEndSky() {
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize());){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (int i = 0; i < 6; ++i) {
                Matrix4f matrix4f = new Matrix4f();
                switch (i) {
                    case 1: {
                        matrix4f.rotationX(1.5707964f);
                        break;
                    }
                    case 2: {
                        matrix4f.rotationX(-1.5707964f);
                        break;
                    }
                    case 3: {
                        matrix4f.rotationX((float)Math.PI);
                        break;
                    }
                    case 4: {
                        matrix4f.rotationZ(1.5707964f);
                        break;
                    }
                    case 5: {
                        matrix4f.rotationZ(-1.5707964f);
                    }
                }
                bufferBuilder.addVertex((Matrix4fc)matrix4f, -100.0f, -100.0f, -100.0f).setUv(0.0f, 0.0f).setColor(-14145496);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, -100.0f, -100.0f, 100.0f).setUv(0.0f, 16.0f).setColor(-14145496);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, 100.0f, -100.0f, 100.0f).setUv(16.0f, 16.0f).setColor(-14145496);
                bufferBuilder.addVertex((Matrix4fc)matrix4f, 100.0f, -100.0f, -100.0f).setUv(16.0f, 0.0f).setColor(-14145496);
            }
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "End sky vertex buffer", 40, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return gpuBuffer;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    public void renderSkyDisc(int i) {
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)ARGB.vector4fFromARGB32(i), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky disc", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.topSkyBuffer);
            renderPass.draw(0, 10);
        }
    }

    public void extractRenderState(ClientLevel clientLevel, float f, Camera camera, SkyRenderState skyRenderState) {
        skyRenderState.skybox = clientLevel.dimensionType().skybox();
        if (skyRenderState.skybox == DimensionType.Skybox.NONE) {
            return;
        }
        if (skyRenderState.skybox == DimensionType.Skybox.END) {
            EndFlashState endFlashState = clientLevel.endFlashState();
            if (endFlashState == null) {
                return;
            }
            skyRenderState.endFlashIntensity = endFlashState.getIntensity(f);
            skyRenderState.endFlashXAngle = endFlashState.getXAngle();
            skyRenderState.endFlashYAngle = endFlashState.getYAngle();
            return;
        }
        EnvironmentAttributeProbe environmentAttributeProbe = camera.attributeProbe();
        skyRenderState.sunAngle = environmentAttributeProbe.getValue(EnvironmentAttributes.SUN_ANGLE, f).floatValue() * ((float)Math.PI / 180);
        skyRenderState.moonAngle = environmentAttributeProbe.getValue(EnvironmentAttributes.MOON_ANGLE, f).floatValue() * ((float)Math.PI / 180);
        skyRenderState.starAngle = environmentAttributeProbe.getValue(EnvironmentAttributes.STAR_ANGLE, f).floatValue() * ((float)Math.PI / 180);
        skyRenderState.rainBrightness = 1.0f - clientLevel.getRainLevel(f);
        skyRenderState.starBrightness = environmentAttributeProbe.getValue(EnvironmentAttributes.STAR_BRIGHTNESS, f).floatValue();
        skyRenderState.sunriseAndSunsetColor = camera.attributeProbe().getValue(EnvironmentAttributes.SUNRISE_SUNSET_COLOR, f);
        skyRenderState.moonPhase = environmentAttributeProbe.getValue(EnvironmentAttributes.MOON_PHASE, f);
        skyRenderState.skyColor = environmentAttributeProbe.getValue(EnvironmentAttributes.SKY_COLOR, f);
        skyRenderState.shouldRenderDarkDisc = this.shouldRenderDarkDisc(f, clientLevel);
    }

    private boolean shouldRenderDarkDisc(float f, ClientLevel clientLevel) {
        return Minecraft.getInstance().player.getEyePosition((float)f).y - clientLevel.getLevelData().getHorizonHeight(clientLevel) < 0.0;
    }

    public void renderDarkDisc() {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translate(0.0f, 12.0f, 0.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky dark", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.bottomSkyBuffer);
            renderPass.draw(0, 10);
        }
        matrix4fStack.popMatrix();
    }

    public void renderSunMoonAndStars(PoseStack poseStack, float f, float g, float h, MoonPhase moonPhase, float i, float j) {
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-90.0f));
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation(f));
        this.renderSun(i, poseStack);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.XP.rotation(g));
        this.renderMoon(moonPhase, i, poseStack);
        poseStack.popPose();
        if (j > 0.0f) {
            poseStack.pushPose();
            poseStack.mulPose((Quaternionfc)Axis.XP.rotation(h));
            this.renderStars(j, poseStack);
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    private void renderSun(float f, PoseStack poseStack) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul((Matrix4fc)poseStack.last().pose());
        matrix4fStack.translate(0.0f, 100.0f, 0.0f);
        matrix4fStack.scale(30.0f, 1.0f, 30.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBuffer gpuBuffer = this.quadIndices.getBuffer(6);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky sun", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, this.sunBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.quadIndices.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }
        matrix4fStack.popMatrix();
    }

    private void renderMoon(MoonPhase moonPhase, float f, PoseStack poseStack) {
        int i = moonPhase.index() * 4;
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul((Matrix4fc)poseStack.last().pose());
        matrix4fStack.translate(0.0f, 100.0f, 0.0f);
        matrix4fStack.scale(20.0f, 1.0f, 20.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBuffer gpuBuffer = this.quadIndices.getBuffer(6);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky moon", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, this.moonBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.quadIndices.type());
            renderPass.drawIndexed(i, 0, 6, 1);
        }
        matrix4fStack.popMatrix();
    }

    private void renderStars(float f, PoseStack poseStack) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul((Matrix4fc)poseStack.last().pose());
        RenderPipeline renderPipeline = RenderPipelines.STARS;
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBuffer gpuBuffer = this.quadIndices.getBuffer(this.starIndexCount);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(f, f, f, f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stars", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.starBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.quadIndices.type());
            renderPass.drawIndexed(0, 0, this.starIndexCount, 1);
        }
        matrix4fStack.popMatrix();
    }

    public void renderSunriseAndSunset(PoseStack poseStack, float f, int i) {
        float g = ARGB.alphaFloat(i);
        if (g <= 0.001f) {
            return;
        }
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        float h = Mth.sin(f) < 0.0f ? 180.0f : 0.0f;
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(h + 90.0f));
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul((Matrix4fc)poseStack.last().pose());
        matrix4fStack.scale(1.0f, 1.0f, g);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)ARGB.vector4fFromARGB32(i), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sunrise sunset", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.SUNRISE_SUNSET);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.sunriseBuffer);
            renderPass.draw(0, 18);
        }
        matrix4fStack.popMatrix();
        poseStack.popPose();
    }

    public void renderEndSky() {
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(36);
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "End sky", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.END_SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindTexture("Sampler0", this.endSkyTexture.getTextureView(), this.endSkyTexture.getSampler());
            renderPass.setVertexBuffer(0, this.endSkyBuffer);
            renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            renderPass.drawIndexed(0, 0, 36, 1);
        }
    }

    public void renderEndFlash(PoseStack poseStack, float f, float g, float h) {
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(180.0f - h));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-90.0f - g));
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul((Matrix4fc)poseStack.last().pose());
        matrix4fStack.translate(0.0f, 100.0f, 0.0f);
        matrix4fStack.scale(60.0f, 1.0f, 60.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(f, f, f, f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f());
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBuffer gpuBuffer = this.quadIndices.getBuffer(6);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "End flash", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.CELESTIAL);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindTexture("Sampler0", this.celestialsAtlas.getTextureView(), this.celestialsAtlas.getSampler());
            renderPass.setVertexBuffer(0, this.endFlashBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.quadIndices.type());
            renderPass.drawIndexed(0, 0, 6, 1);
        }
        matrix4fStack.popMatrix();
    }

    @Override
    public void close() {
        this.sunBuffer.close();
        this.moonBuffer.close();
        this.starBuffer.close();
        this.topSkyBuffer.close();
        this.bottomSkyBuffer.close();
        this.endSkyBuffer.close();
        this.sunriseBuffer.close();
        this.endFlashBuffer.close();
    }
}

