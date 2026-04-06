/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  java.lang.runtime.SwitchBootstraps
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.joml.Quaternionf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.lang.runtime.SwitchBootstraps;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.entity.ClientMannequin;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MapRenderer;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class EntityRenderDispatcher
implements ResourceManagerReloadListener {
    private Map<EntityType<?>, EntityRenderer<?, ?>> renderers = ImmutableMap.of();
    private Map<PlayerModelType, AvatarRenderer<AbstractClientPlayer>> playerRenderers = Map.of();
    private Map<PlayerModelType, AvatarRenderer<ClientMannequin>> mannequinRenderers = Map.of();
    public final TextureManager textureManager;
    public @Nullable Camera camera;
    public Entity crosshairPickEntity;
    private final ItemModelResolver itemModelResolver;
    private final MapRenderer mapRenderer;
    private final BlockRenderDispatcher blockRenderDispatcher;
    private final ItemInHandRenderer itemInHandRenderer;
    private final AtlasManager atlasManager;
    private final Font font;
    public final Options options;
    private final Supplier<EntityModelSet> entityModels;
    private final EquipmentAssetManager equipmentAssets;
    private final PlayerSkinRenderCache playerSkinRenderCache;

    public <E extends Entity> int getPackedLightCoords(E entity, float f) {
        return this.getRenderer((EntityRenderState)((Object)entity)).getPackedLightCoords(entity, f);
    }

    public EntityRenderDispatcher(Minecraft minecraft, TextureManager textureManager, ItemModelResolver itemModelResolver, MapRenderer mapRenderer, BlockRenderDispatcher blockRenderDispatcher, AtlasManager atlasManager, Font font, Options options, Supplier<EntityModelSet> supplier, EquipmentAssetManager equipmentAssetManager, PlayerSkinRenderCache playerSkinRenderCache) {
        this.textureManager = textureManager;
        this.itemModelResolver = itemModelResolver;
        this.mapRenderer = mapRenderer;
        this.atlasManager = atlasManager;
        this.playerSkinRenderCache = playerSkinRenderCache;
        this.itemInHandRenderer = new ItemInHandRenderer(minecraft, this, itemModelResolver);
        this.blockRenderDispatcher = blockRenderDispatcher;
        this.font = font;
        this.options = options;
        this.entityModels = supplier;
        this.equipmentAssets = equipmentAssetManager;
    }

    public <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity) {
        T t = entity;
        Objects.requireNonNull(t);
        T t2 = t;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AbstractClientPlayer.class, ClientMannequin.class}, t2, (int)n)) {
            case 0 -> {
                AbstractClientPlayer abstractClientPlayer = (AbstractClientPlayer)t2;
                yield this.getAvatarRenderer(this.playerRenderers, abstractClientPlayer);
            }
            case 1 -> {
                ClientMannequin clientMannequin = (ClientMannequin)t2;
                yield this.getAvatarRenderer(this.mannequinRenderers, clientMannequin);
            }
            default -> this.renderers.get(entity.getType());
        };
    }

    public AvatarRenderer<AbstractClientPlayer> getPlayerRenderer(AbstractClientPlayer abstractClientPlayer) {
        return this.getAvatarRenderer(this.playerRenderers, abstractClientPlayer);
    }

    private <T extends Avatar> AvatarRenderer<T> getAvatarRenderer(Map<PlayerModelType, AvatarRenderer<T>> map, T avatar) {
        PlayerModelType playerModelType = ((ClientAvatarEntity)((Object)avatar)).getSkin().model();
        AvatarRenderer<T> avatarRenderer = map.get(playerModelType);
        if (avatarRenderer != null) {
            return avatarRenderer;
        }
        return map.get(PlayerModelType.WIDE);
    }

    public <S extends EntityRenderState> EntityRenderer<?, ? super S> getRenderer(S entityRenderState) {
        if (entityRenderState instanceof AvatarRenderState) {
            AvatarRenderState avatarRenderState = (AvatarRenderState)entityRenderState;
            PlayerModelType playerModelType = avatarRenderState.skin.model();
            EntityRenderer entityRenderer = this.playerRenderers.get(playerModelType);
            if (entityRenderer != null) {
                return entityRenderer;
            }
            return this.playerRenderers.get(PlayerModelType.WIDE);
        }
        return this.renderers.get(entityRenderState.entityType);
    }

    public void prepare(Camera camera, Entity entity) {
        this.camera = camera;
        this.crosshairPickEntity = entity;
    }

    public <E extends Entity> boolean shouldRender(E entity, Frustum frustum, double d, double e, double f) {
        EntityRenderer<?, E> entityRenderer = this.getRenderer((EntityRenderState)((Object)entity));
        return entityRenderer.shouldRender(entity, frustum, d, e, f);
    }

    public <E extends Entity> EntityRenderState extractEntity(E entity, float f) {
        EntityRenderer<?, E> entityRenderer = this.getRenderer((EntityRenderState)((Object)entity));
        try {
            return entityRenderer.createRenderState(entity, f);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Extracting render state for an entity in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Entity being extracted");
            entity.fillCrashReportCategory(crashReportCategory);
            CrashReportCategory crashReportCategory2 = this.fillRendererDetails(entityRenderer, crashReport);
            crashReportCategory2.setDetail("Delta", Float.valueOf(f));
            throw new ReportedException(crashReport);
        }
    }

    public <S extends EntityRenderState> void submit(S entityRenderState, CameraRenderState cameraRenderState, double d, double e, double f, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
        EntityRenderer<?, S> entityRenderer = this.getRenderer(entityRenderState);
        try {
            Vec3 vec3 = entityRenderer.getRenderOffset(entityRenderState);
            double g = d + vec3.x();
            double h = e + vec3.y();
            double i = f + vec3.z();
            poseStack.pushPose();
            poseStack.translate(g, h, i);
            entityRenderer.submit(entityRenderState, poseStack, submitNodeCollector, cameraRenderState);
            if (entityRenderState.displayFireAnimation) {
                submitNodeCollector.submitFlame(poseStack, entityRenderState, Mth.rotationAroundAxis(Mth.Y_AXIS, cameraRenderState.orientation, new Quaternionf()));
            }
            if (entityRenderState instanceof AvatarRenderState) {
                poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            }
            if (!entityRenderState.shadowPieces.isEmpty()) {
                submitNodeCollector.submitShadow(poseStack, entityRenderState.shadowRadius, entityRenderState.shadowPieces);
            }
            if (!(entityRenderState instanceof AvatarRenderState)) {
                poseStack.translate(-vec3.x(), -vec3.y(), -vec3.z());
            }
            poseStack.popPose();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering entity in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("EntityRenderState being rendered");
            entityRenderState.fillCrashReportCategory(crashReportCategory);
            this.fillRendererDetails(entityRenderer, crashReport);
            throw new ReportedException(crashReport);
        }
    }

    private <S extends EntityRenderState> CrashReportCategory fillRendererDetails(EntityRenderer<?, S> entityRenderer, CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Renderer details");
        crashReportCategory.setDetail("Assigned renderer", entityRenderer);
        return crashReportCategory;
    }

    public void resetCamera() {
        this.camera = null;
    }

    public double distanceToSqr(Entity entity) {
        return this.camera.position().distanceToSqr(entity.position());
    }

    public ItemInHandRenderer getItemInHandRenderer() {
        return this.itemInHandRenderer;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        EntityRendererProvider.Context context = new EntityRendererProvider.Context(this, this.itemModelResolver, this.mapRenderer, this.blockRenderDispatcher, resourceManager, this.entityModels.get(), this.equipmentAssets, this.atlasManager, this.font, this.playerSkinRenderCache);
        this.renderers = EntityRenderers.createEntityRenderers(context);
        this.playerRenderers = EntityRenderers.createAvatarRenderers(context);
        this.mannequinRenderers = EntityRenderers.createAvatarRenderers(context);
    }
}

