/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity, S extends EntityRenderState> {
    private static final float SHADOW_POWER_FALLOFF_Y = 0.5f;
    private static final float MAX_SHADOW_RADIUS = 32.0f;
    public static final float NAMETAG_SCALE = 0.025f;
    protected final EntityRenderDispatcher entityRenderDispatcher;
    private final Font font;
    protected float shadowRadius;
    protected float shadowStrength = 1.0f;

    protected EntityRenderer(EntityRendererProvider.Context context) {
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
        this.font = context.getFont();
    }

    public final int getPackedLightCoords(T entity, float f) {
        BlockPos blockPos = BlockPos.containing(((Entity)entity).getLightProbePosition(f));
        return LightTexture.pack(this.getBlockLightLevel(entity, blockPos), this.getSkyLightLevel(entity, blockPos));
    }

    protected int getSkyLightLevel(T entity, BlockPos blockPos) {
        return ((Entity)entity).level().getBrightness(LightLayer.SKY, blockPos);
    }

    protected int getBlockLightLevel(T entity, BlockPos blockPos) {
        if (((Entity)entity).isOnFire()) {
            return 15;
        }
        return ((Entity)entity).level().getBrightness(LightLayer.BLOCK, blockPos);
    }

    public boolean shouldRender(T entity, Frustum frustum, double d, double e, double f) {
        Leashable leashable;
        Entity entity2;
        if (!((Entity)entity).shouldRender(d, e, f)) {
            return false;
        }
        if (!this.affectedByCulling(entity)) {
            return true;
        }
        AABB aABB = this.getBoundingBoxForCulling(entity).inflate(0.5);
        if (aABB.hasNaN() || aABB.getSize() == 0.0) {
            aABB = new AABB(((Entity)entity).getX() - 2.0, ((Entity)entity).getY() - 2.0, ((Entity)entity).getZ() - 2.0, ((Entity)entity).getX() + 2.0, ((Entity)entity).getY() + 2.0, ((Entity)entity).getZ() + 2.0);
        }
        if (frustum.isVisible(aABB)) {
            return true;
        }
        if (entity instanceof Leashable && (entity2 = (leashable = (Leashable)entity).getLeashHolder()) != null) {
            AABB aABB2 = this.entityRenderDispatcher.getRenderer(entity2).getBoundingBoxForCulling(entity2);
            return frustum.isVisible(aABB2) || frustum.isVisible(aABB.minmax(aABB2));
        }
        return false;
    }

    protected AABB getBoundingBoxForCulling(T entity) {
        return ((Entity)entity).getBoundingBox();
    }

    protected boolean affectedByCulling(T entity) {
        return true;
    }

    public Vec3 getRenderOffset(S entityRenderState) {
        if (((EntityRenderState)entityRenderState).passengerOffset != null) {
            return ((EntityRenderState)entityRenderState).passengerOffset;
        }
        return Vec3.ZERO;
    }

    public void submit(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (((EntityRenderState)entityRenderState).leashStates != null) {
            for (EntityRenderState.LeashState leashState : ((EntityRenderState)entityRenderState).leashStates) {
                submitNodeCollector.submitLeash(poseStack, leashState);
            }
        }
        this.submitNameTag(entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
    }

    protected boolean shouldShowName(T entity, double d) {
        return ((Entity)entity).shouldShowName() || ((Entity)entity).hasCustomName() && entity == this.entityRenderDispatcher.crosshairPickEntity;
    }

    public Font getFont() {
        return this.font;
    }

    protected void submitNameTag(S entityRenderState, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        if (((EntityRenderState)entityRenderState).nameTag != null) {
            submitNodeCollector.submitNameTag(poseStack, ((EntityRenderState)entityRenderState).nameTagAttachment, 0, ((EntityRenderState)entityRenderState).nameTag, !((EntityRenderState)entityRenderState).isDiscrete, ((EntityRenderState)entityRenderState).lightCoords, ((EntityRenderState)entityRenderState).distanceToCameraSq, cameraRenderState);
        }
    }

    protected @Nullable Component getNameTag(T entity) {
        return ((Entity)entity).getDisplayName();
    }

    protected float getShadowRadius(S entityRenderState) {
        return this.shadowRadius;
    }

    protected float getShadowStrength(S entityRenderState) {
        return this.shadowStrength;
    }

    public abstract S createRenderState();

    public final S createRenderState(T entity, float f) {
        S entityRenderState = this.createRenderState();
        this.extractRenderState(entity, entityRenderState, f);
        this.finalizeRenderState(entity, entityRenderState);
        return entityRenderState;
    }

    public void extractRenderState(T entity, S entityRenderState, float f) {
        Leashable leashable;
        Entity entity2;
        NewMinecartBehavior newMinecartBehavior;
        AbstractMinecart abstractMinecart;
        Object object;
        ((EntityRenderState)entityRenderState).entityType = ((Entity)entity).getType();
        ((EntityRenderState)entityRenderState).x = Mth.lerp((double)f, ((Entity)entity).xOld, ((Entity)entity).getX());
        ((EntityRenderState)entityRenderState).y = Mth.lerp((double)f, ((Entity)entity).yOld, ((Entity)entity).getY());
        ((EntityRenderState)entityRenderState).z = Mth.lerp((double)f, ((Entity)entity).zOld, ((Entity)entity).getZ());
        ((EntityRenderState)entityRenderState).isInvisible = ((Entity)entity).isInvisible();
        ((EntityRenderState)entityRenderState).ageInTicks = (float)((Entity)entity).tickCount + f;
        ((EntityRenderState)entityRenderState).boundingBoxWidth = ((Entity)entity).getBbWidth();
        ((EntityRenderState)entityRenderState).boundingBoxHeight = ((Entity)entity).getBbHeight();
        ((EntityRenderState)entityRenderState).eyeHeight = ((Entity)entity).getEyeHeight();
        if (((Entity)entity).isPassenger() && (object = ((Entity)entity).getVehicle()) instanceof AbstractMinecart && (object = (abstractMinecart = (AbstractMinecart)object).getBehavior()) instanceof NewMinecartBehavior && (newMinecartBehavior = (NewMinecartBehavior)object).cartHasPosRotLerp()) {
            double d = Mth.lerp((double)f, abstractMinecart.xOld, abstractMinecart.getX());
            double e = Mth.lerp((double)f, abstractMinecart.yOld, abstractMinecart.getY());
            double g = Mth.lerp((double)f, abstractMinecart.zOld, abstractMinecart.getZ());
            ((EntityRenderState)entityRenderState).passengerOffset = newMinecartBehavior.getCartLerpPosition(f).subtract(new Vec3(d, e, g));
        } else {
            ((EntityRenderState)entityRenderState).passengerOffset = null;
        }
        if (this.entityRenderDispatcher.camera != null) {
            boolean bl;
            ((EntityRenderState)entityRenderState).distanceToCameraSq = this.entityRenderDispatcher.distanceToSqr((Entity)entity);
            boolean bl2 = bl = ((EntityRenderState)entityRenderState).distanceToCameraSq < 4096.0 && this.shouldShowName(entity, ((EntityRenderState)entityRenderState).distanceToCameraSq);
            if (bl) {
                ((EntityRenderState)entityRenderState).nameTag = this.getNameTag(entity);
                ((EntityRenderState)entityRenderState).nameTagAttachment = ((Entity)entity).getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, ((Entity)entity).getYRot(f));
            } else {
                ((EntityRenderState)entityRenderState).nameTag = null;
            }
        }
        ((EntityRenderState)entityRenderState).isDiscrete = ((Entity)entity).isDiscrete();
        Level level = ((Entity)entity).level();
        if (entity instanceof Leashable && (entity2 = (leashable = (Leashable)entity).getLeashHolder()) instanceof Entity) {
            int m;
            Entity entity22 = entity2;
            float h = ((Entity)entity).getPreciseBodyRotation(f) * ((float)Math.PI / 180);
            Vec3 vec3 = leashable.getLeashOffset(f);
            BlockPos blockPos = BlockPos.containing(((Entity)entity).getEyePosition(f));
            BlockPos blockPos2 = BlockPos.containing(entity22.getEyePosition(f));
            int i = this.getBlockLightLevel(entity, blockPos);
            int j = this.entityRenderDispatcher.getRenderer(entity22).getBlockLightLevel(entity22, blockPos2);
            int k = level.getBrightness(LightLayer.SKY, blockPos);
            int l = level.getBrightness(LightLayer.SKY, blockPos2);
            boolean bl2 = entity22.supportQuadLeashAsHolder() && leashable.supportQuadLeash();
            int n = m = bl2 ? 4 : 1;
            if (((EntityRenderState)entityRenderState).leashStates == null || ((EntityRenderState)entityRenderState).leashStates.size() != m) {
                ((EntityRenderState)entityRenderState).leashStates = new ArrayList<EntityRenderState.LeashState>(m);
                for (int n2 = 0; n2 < m; ++n2) {
                    ((EntityRenderState)entityRenderState).leashStates.add(new EntityRenderState.LeashState());
                }
            }
            if (bl2) {
                float o = entity22.getPreciseBodyRotation(f) * ((float)Math.PI / 180);
                Vec3 vec32 = entity22.getPosition(f);
                Vec3[] vec3s = leashable.getQuadLeashOffsets();
                Vec3[] vec3s2 = entity22.getQuadLeashHolderOffsets();
                for (int p = 0; p < m; ++p) {
                    EntityRenderState.LeashState leashState = ((EntityRenderState)entityRenderState).leashStates.get(p);
                    leashState.offset = vec3s[p].yRot(-h);
                    leashState.start = ((Entity)entity).getPosition(f).add(leashState.offset);
                    leashState.end = vec32.add(vec3s2[p].yRot(-o));
                    leashState.startBlockLight = i;
                    leashState.endBlockLight = j;
                    leashState.startSkyLight = k;
                    leashState.endSkyLight = l;
                    leashState.slack = false;
                }
            } else {
                Vec3 vec33 = vec3.yRot(-h);
                EntityRenderState.LeashState leashState2 = (EntityRenderState.LeashState)((EntityRenderState)entityRenderState).leashStates.getFirst();
                leashState2.offset = vec33;
                leashState2.start = ((Entity)entity).getPosition(f).add(vec33);
                leashState2.end = entity22.getRopeHoldPosition(f);
                leashState2.startBlockLight = i;
                leashState2.endBlockLight = j;
                leashState2.startSkyLight = k;
                leashState2.endSkyLight = l;
            }
        } else {
            ((EntityRenderState)entityRenderState).leashStates = null;
        }
        ((EntityRenderState)entityRenderState).displayFireAnimation = ((Entity)entity).displayFireAnimation();
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl3 = minecraft.shouldEntityAppearGlowing((Entity)entity);
        ((EntityRenderState)entityRenderState).outlineColor = bl3 ? ARGB.opaque(((Entity)entity).getTeamColor()) : 0;
        ((EntityRenderState)entityRenderState).lightCoords = this.getPackedLightCoords(entity, f);
    }

    protected void finalizeRenderState(T entity, S entityRenderState) {
        Minecraft minecraft = Minecraft.getInstance();
        Level level = ((Entity)entity).level();
        this.extractShadow(entityRenderState, minecraft, level);
    }

    private void extractShadow(S entityRenderState, Minecraft minecraft, Level level) {
        ((EntityRenderState)entityRenderState).shadowPieces.clear();
        if (minecraft.options.entityShadows().get().booleanValue() && !((EntityRenderState)entityRenderState).isInvisible) {
            double d;
            float g;
            float f;
            ((EntityRenderState)entityRenderState).shadowRadius = f = Math.min(this.getShadowRadius(entityRenderState), 32.0f);
            if (f > 0.0f && (g = (float)((1.0 - (d = ((EntityRenderState)entityRenderState).distanceToCameraSq) / 256.0) * (double)this.getShadowStrength(entityRenderState))) > 0.0f) {
                int i = Mth.floor(((EntityRenderState)entityRenderState).x - (double)f);
                int j = Mth.floor(((EntityRenderState)entityRenderState).x + (double)f);
                int k = Mth.floor(((EntityRenderState)entityRenderState).z - (double)f);
                int l = Mth.floor(((EntityRenderState)entityRenderState).z + (double)f);
                float h = Math.min(g / 0.5f - 1.0f, f);
                int m = Mth.floor(((EntityRenderState)entityRenderState).y - (double)h);
                int n = Mth.floor(((EntityRenderState)entityRenderState).y);
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
                for (int o = k; o <= l; ++o) {
                    for (int p = i; p <= j; ++p) {
                        mutableBlockPos.set(p, 0, o);
                        ChunkAccess chunkAccess = level.getChunk(mutableBlockPos);
                        for (int q = m; q <= n; ++q) {
                            mutableBlockPos.setY(q);
                            this.extractShadowPiece(entityRenderState, level, g, mutableBlockPos, chunkAccess);
                        }
                    }
                }
            }
        } else {
            ((EntityRenderState)entityRenderState).shadowRadius = 0.0f;
        }
    }

    private void extractShadowPiece(S entityRenderState, Level level, float f, BlockPos.MutableBlockPos mutableBlockPos, ChunkAccess chunkAccess) {
        float g = f - (float)(((EntityRenderState)entityRenderState).y - (double)mutableBlockPos.getY()) * 0.5f;
        Vec3i blockPos = mutableBlockPos.below();
        BlockState blockState = chunkAccess.getBlockState((BlockPos)blockPos);
        if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }
        int i = level.getMaxLocalRawBrightness(mutableBlockPos);
        if (i <= 3) {
            return;
        }
        if (!blockState.isCollisionShapeFullBlock(chunkAccess, (BlockPos)blockPos)) {
            return;
        }
        VoxelShape voxelShape = blockState.getShape(chunkAccess, (BlockPos)blockPos);
        if (voxelShape.isEmpty()) {
            return;
        }
        float h = Mth.clamp(g * 0.5f * LightTexture.getBrightness(level.dimensionType(), i), 0.0f, 1.0f);
        float j = (float)((double)mutableBlockPos.getX() - ((EntityRenderState)entityRenderState).x);
        float k = (float)((double)mutableBlockPos.getY() - ((EntityRenderState)entityRenderState).y);
        float l = (float)((double)mutableBlockPos.getZ() - ((EntityRenderState)entityRenderState).z);
        ((EntityRenderState)entityRenderState).shadowPieces.add(new EntityRenderState.ShadowPiece(j, k, l, voxelShape, h));
    }

    private static @Nullable Entity getServerSideEntity(Entity entity) {
        ServerLevel serverLevel;
        IntegratedServer integratedServer = Minecraft.getInstance().getSingleplayerServer();
        if (integratedServer != null && (serverLevel = integratedServer.getLevel(entity.level().dimension())) != null) {
            return serverLevel.getEntity(entity.getId());
        }
        return null;
    }
}

