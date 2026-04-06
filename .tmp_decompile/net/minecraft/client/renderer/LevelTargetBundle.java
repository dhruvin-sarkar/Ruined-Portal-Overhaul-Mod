/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class LevelTargetBundle
implements PostChain.TargetBundle {
    public static final Identifier MAIN_TARGET_ID = PostChain.MAIN_TARGET_ID;
    public static final Identifier TRANSLUCENT_TARGET_ID = Identifier.withDefaultNamespace("translucent");
    public static final Identifier ITEM_ENTITY_TARGET_ID = Identifier.withDefaultNamespace("item_entity");
    public static final Identifier PARTICLES_TARGET_ID = Identifier.withDefaultNamespace("particles");
    public static final Identifier WEATHER_TARGET_ID = Identifier.withDefaultNamespace("weather");
    public static final Identifier CLOUDS_TARGET_ID = Identifier.withDefaultNamespace("clouds");
    public static final Identifier ENTITY_OUTLINE_TARGET_ID = Identifier.withDefaultNamespace("entity_outline");
    public static final Set<Identifier> MAIN_TARGETS = Set.of((Object)MAIN_TARGET_ID);
    public static final Set<Identifier> OUTLINE_TARGETS = Set.of((Object)MAIN_TARGET_ID, (Object)ENTITY_OUTLINE_TARGET_ID);
    public static final Set<Identifier> SORTING_TARGETS = Set.of((Object)MAIN_TARGET_ID, (Object)TRANSLUCENT_TARGET_ID, (Object)ITEM_ENTITY_TARGET_ID, (Object)PARTICLES_TARGET_ID, (Object)WEATHER_TARGET_ID, (Object)CLOUDS_TARGET_ID);
    public ResourceHandle<RenderTarget> main = ResourceHandle.invalid();
    public @Nullable ResourceHandle<RenderTarget> translucent;
    public @Nullable ResourceHandle<RenderTarget> itemEntity;
    public @Nullable ResourceHandle<RenderTarget> particles;
    public @Nullable ResourceHandle<RenderTarget> weather;
    public @Nullable ResourceHandle<RenderTarget> clouds;
    public @Nullable ResourceHandle<RenderTarget> entityOutline;

    @Override
    public void replace(Identifier identifier, ResourceHandle<RenderTarget> resourceHandle) {
        if (identifier.equals(MAIN_TARGET_ID)) {
            this.main = resourceHandle;
        } else if (identifier.equals(TRANSLUCENT_TARGET_ID)) {
            this.translucent = resourceHandle;
        } else if (identifier.equals(ITEM_ENTITY_TARGET_ID)) {
            this.itemEntity = resourceHandle;
        } else if (identifier.equals(PARTICLES_TARGET_ID)) {
            this.particles = resourceHandle;
        } else if (identifier.equals(WEATHER_TARGET_ID)) {
            this.weather = resourceHandle;
        } else if (identifier.equals(CLOUDS_TARGET_ID)) {
            this.clouds = resourceHandle;
        } else if (identifier.equals(ENTITY_OUTLINE_TARGET_ID)) {
            this.entityOutline = resourceHandle;
        } else {
            throw new IllegalArgumentException("No target with id " + String.valueOf(identifier));
        }
    }

    @Override
    public @Nullable ResourceHandle<RenderTarget> get(Identifier identifier) {
        if (identifier.equals(MAIN_TARGET_ID)) {
            return this.main;
        }
        if (identifier.equals(TRANSLUCENT_TARGET_ID)) {
            return this.translucent;
        }
        if (identifier.equals(ITEM_ENTITY_TARGET_ID)) {
            return this.itemEntity;
        }
        if (identifier.equals(PARTICLES_TARGET_ID)) {
            return this.particles;
        }
        if (identifier.equals(WEATHER_TARGET_ID)) {
            return this.weather;
        }
        if (identifier.equals(CLOUDS_TARGET_ID)) {
            return this.clouds;
        }
        if (identifier.equals(ENTITY_OUTLINE_TARGET_ID)) {
            return this.entityOutline;
        }
        return null;
    }

    public void clear() {
        this.main = ResourceHandle.invalid();
        this.translucent = null;
        this.itemEntity = null;
        this.particles = null;
        this.weather = null;
        this.clouds = null;
        this.entityOutline = null;
    }
}

