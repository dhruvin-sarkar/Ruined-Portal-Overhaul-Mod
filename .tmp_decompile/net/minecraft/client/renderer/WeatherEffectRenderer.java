/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.WeatherRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(value=EnvType.CLIENT)
public class WeatherEffectRenderer {
    private static final float RAIN_PARTICLES_PER_BLOCK = 0.225f;
    private static final int RAIN_RADIUS = 10;
    private static final Identifier RAIN_LOCATION = Identifier.withDefaultNamespace("textures/environment/rain.png");
    private static final Identifier SNOW_LOCATION = Identifier.withDefaultNamespace("textures/environment/snow.png");
    private static final int RAIN_TABLE_SIZE = 32;
    private static final int HALF_RAIN_TABLE_SIZE = 16;
    private int rainSoundTime;
    private final float[] columnSizeX = new float[1024];
    private final float[] columnSizeZ = new float[1024];

    public WeatherEffectRenderer() {
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = j - 16;
                float g = i - 16;
                float h = Mth.length(f, g);
                this.columnSizeX[i * 32 + j] = -g / h;
                this.columnSizeZ[i * 32 + j] = f / h;
            }
        }
    }

    public void extractRenderState(Level level, int i, float f, Vec3 vec3, WeatherRenderState weatherRenderState) {
        weatherRenderState.intensity = level.getRainLevel(f);
        if (weatherRenderState.intensity <= 0.0f) {
            return;
        }
        weatherRenderState.radius = Minecraft.getInstance().options.weatherRadius().get();
        int j = Mth.floor(vec3.x);
        int k = Mth.floor(vec3.y);
        int l = Mth.floor(vec3.z);
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        RandomSource randomSource = RandomSource.create();
        for (int m = l - weatherRenderState.radius; m <= l + weatherRenderState.radius; ++m) {
            for (int n = j - weatherRenderState.radius; n <= j + weatherRenderState.radius; ++n) {
                Biome.Precipitation precipitation;
                int o = level.getHeight(Heightmap.Types.MOTION_BLOCKING, n, m);
                int p = Math.max(k - weatherRenderState.radius, o);
                int q = Math.max(k + weatherRenderState.radius, o);
                if (q - p == 0 || (precipitation = this.getPrecipitationAt(level, mutableBlockPos.set(n, k, m))) == Biome.Precipitation.NONE) continue;
                int r = n * n * 3121 + n * 45238971 ^ m * m * 418711 + m * 13761;
                randomSource.setSeed(r);
                int s = Math.max(k, o);
                int t = LevelRenderer.getLightColor(level, mutableBlockPos.set(n, s, m));
                if (precipitation == Biome.Precipitation.RAIN) {
                    weatherRenderState.rainColumns.add(this.createRainColumnInstance(randomSource, i, n, p, q, m, t, f));
                    continue;
                }
                if (precipitation != Biome.Precipitation.SNOW) continue;
                weatherRenderState.snowColumns.add(this.createSnowColumnInstance(randomSource, i, n, p, q, m, t, f));
            }
        }
    }

    public void render(MultiBufferSource multiBufferSource, Vec3 vec3, WeatherRenderState weatherRenderState) {
        RenderType renderType;
        if (!weatherRenderState.rainColumns.isEmpty()) {
            renderType = RenderTypes.weather(RAIN_LOCATION, Minecraft.useShaderTransparency());
            this.renderInstances(multiBufferSource.getBuffer(renderType), weatherRenderState.rainColumns, vec3, 1.0f, weatherRenderState.radius, weatherRenderState.intensity);
        }
        if (!weatherRenderState.snowColumns.isEmpty()) {
            renderType = RenderTypes.weather(SNOW_LOCATION, Minecraft.useShaderTransparency());
            this.renderInstances(multiBufferSource.getBuffer(renderType), weatherRenderState.snowColumns, vec3, 0.8f, weatherRenderState.radius, weatherRenderState.intensity);
        }
    }

    private ColumnInstance createRainColumnInstance(RandomSource randomSource, int i, int j, int k, int l, int m, int n, float f) {
        int o = i & 0x1FFFF;
        int p = j * j * 3121 + j * 45238971 + m * m * 418711 + m * 13761 & 0xFF;
        float g = 3.0f + randomSource.nextFloat();
        float h = -((float)(o + p) + f) / 32.0f * g;
        float q = h % 32.0f;
        return new ColumnInstance(j, m, k, l, 0.0f, q, n);
    }

    private ColumnInstance createSnowColumnInstance(RandomSource randomSource, int i, int j, int k, int l, int m, int n, float f) {
        float g = (float)i + f;
        float h = (float)(randomSource.nextDouble() + (double)(g * 0.01f * (float)randomSource.nextGaussian()));
        float o = (float)(randomSource.nextDouble() + (double)(g * (float)randomSource.nextGaussian() * 0.001f));
        float p = -((float)(i & 0x1FF) + f) / 512.0f;
        int q = LightTexture.pack((LightTexture.block(n) * 3 + 15) / 4, (LightTexture.sky(n) * 3 + 15) / 4);
        return new ColumnInstance(j, m, k, l, h, p + o, q);
    }

    private void renderInstances(VertexConsumer vertexConsumer, List<ColumnInstance> list, Vec3 vec3, float f, int i, float g) {
        float h = i * i;
        for (ColumnInstance columnInstance : list) {
            float j = (float)((double)columnInstance.x + 0.5 - vec3.x);
            float k = (float)((double)columnInstance.z + 0.5 - vec3.z);
            float l = (float)Mth.lengthSquared(j, k);
            float m = Mth.lerp(Math.min(l / h, 1.0f), f, 0.5f) * g;
            int n = ARGB.white(m);
            int o = (columnInstance.z - Mth.floor(vec3.z) + 16) * 32 + columnInstance.x - Mth.floor(vec3.x) + 16;
            float p = this.columnSizeX[o] / 2.0f;
            float q = this.columnSizeZ[o] / 2.0f;
            float r = j - p;
            float s = j + p;
            float t = (float)((double)columnInstance.topY - vec3.y);
            float u = (float)((double)columnInstance.bottomY - vec3.y);
            float v = k - q;
            float w = k + q;
            float x = columnInstance.uOffset + 0.0f;
            float y = columnInstance.uOffset + 1.0f;
            float z = (float)columnInstance.bottomY * 0.25f + columnInstance.vOffset;
            float aa = (float)columnInstance.topY * 0.25f + columnInstance.vOffset;
            vertexConsumer.addVertex(r, t, v).setUv(x, z).setColor(n).setLight(columnInstance.lightCoords);
            vertexConsumer.addVertex(s, t, w).setUv(y, z).setColor(n).setLight(columnInstance.lightCoords);
            vertexConsumer.addVertex(s, u, w).setUv(y, aa).setColor(n).setLight(columnInstance.lightCoords);
            vertexConsumer.addVertex(r, u, v).setUv(x, aa).setColor(n).setLight(columnInstance.lightCoords);
        }
    }

    public void tickRainParticles(ClientLevel clientLevel, Camera camera, int i, ParticleStatus particleStatus, int j) {
        float f = clientLevel.getRainLevel(1.0f);
        if (f <= 0.0f) {
            return;
        }
        RandomSource randomSource = RandomSource.create((long)i * 312987231L);
        BlockPos blockPos = BlockPos.containing(camera.position());
        Vec3i blockPos2 = null;
        int k = 2 * j + 1;
        int l = k * k;
        int m = (int)(0.225f * (float)l * f * f) / (particleStatus == ParticleStatus.DECREASED ? 2 : 1);
        for (int n = 0; n < m; ++n) {
            int p;
            int o = randomSource.nextInt(k) - j;
            BlockPos blockPos3 = clientLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(o, 0, p = randomSource.nextInt(k) - j));
            if (blockPos3.getY() <= clientLevel.getMinY() || blockPos3.getY() > blockPos.getY() + 10 || blockPos3.getY() < blockPos.getY() - 10 || this.getPrecipitationAt(clientLevel, blockPos3) != Biome.Precipitation.RAIN) continue;
            blockPos2 = blockPos3.below();
            if (particleStatus == ParticleStatus.MINIMAL) break;
            double d = randomSource.nextDouble();
            double e = randomSource.nextDouble();
            BlockState blockState = clientLevel.getBlockState((BlockPos)blockPos2);
            FluidState fluidState = clientLevel.getFluidState((BlockPos)blockPos2);
            VoxelShape voxelShape = blockState.getCollisionShape(clientLevel, (BlockPos)blockPos2);
            double g = voxelShape.max(Direction.Axis.Y, d, e);
            double h = fluidState.getHeight(clientLevel, (BlockPos)blockPos2);
            double q = Math.max(g, h);
            SimpleParticleType particleOptions = fluidState.is(FluidTags.LAVA) || blockState.is(Blocks.MAGMA_BLOCK) || CampfireBlock.isLitCampfire(blockState) ? ParticleTypes.SMOKE : ParticleTypes.RAIN;
            clientLevel.addParticle(particleOptions, (double)blockPos2.getX() + d, (double)blockPos2.getY() + q, (double)blockPos2.getZ() + e, 0.0, 0.0, 0.0);
        }
        if (blockPos2 != null && randomSource.nextInt(3) < this.rainSoundTime++) {
            this.rainSoundTime = 0;
            if (blockPos2.getY() > blockPos.getY() + 1 && clientLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() > Mth.floor(blockPos.getY())) {
                clientLevel.playLocalSound((BlockPos)blockPos2, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1f, 0.5f, false);
            } else {
                clientLevel.playLocalSound((BlockPos)blockPos2, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2f, 1.0f, false);
            }
        }
    }

    private Biome.Precipitation getPrecipitationAt(Level level, BlockPos blockPos) {
        if (!level.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()))) {
            return Biome.Precipitation.NONE;
        }
        Biome biome = level.getBiome(blockPos).value();
        return biome.getPrecipitationAt(blockPos, level.getSeaLevel());
    }

    @Environment(value=EnvType.CLIENT)
    public static final class ColumnInstance
    extends Record {
        final int x;
        final int z;
        final int bottomY;
        final int topY;
        final float uOffset;
        final float vOffset;
        final int lightCoords;

        public ColumnInstance(int i, int j, int k, int l, float f, float g, int m) {
            this.x = i;
            this.z = j;
            this.bottomY = k;
            this.topY = l;
            this.uOffset = f;
            this.vOffset = g;
            this.lightCoords = m;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ColumnInstance.class, "x;z;bottomY;topY;uOffset;vOffset;lightCoords", "x", "z", "bottomY", "topY", "uOffset", "vOffset", "lightCoords"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ColumnInstance.class, "x;z;bottomY;topY;uOffset;vOffset;lightCoords", "x", "z", "bottomY", "topY", "uOffset", "vOffset", "lightCoords"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ColumnInstance.class, "x;z;bottomY;topY;uOffset;vOffset;lightCoords", "x", "z", "bottomY", "topY", "uOffset", "vOffset", "lightCoords"}, this, object);
        }

        public int x() {
            return this.x;
        }

        public int z() {
            return this.z;
        }

        public int bottomY() {
            return this.bottomY;
        }

        public int topY() {
            return this.topY;
        }

        public float uOffset() {
            return this.uOffset;
        }

        public float vOffset() {
            return this.vOffset;
        }

        public int lightCoords() {
            return this.lightCoords;
        }
    }
}

