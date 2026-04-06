/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface SpriteResourceLoader {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static SpriteResourceLoader create(Set<MetadataSectionType<?>> set) {
        return (identifier, resource) -> {
            FrameSize frameSize;
            NativeImage nativeImage;
            List<MetadataSectionType.WithValue<?>> list;
            Optional<TextureMetadataSection> optional2;
            Optional<AnimationMetadataSection> optional;
            try {
                ResourceMetadata resourceMetadata = resource.metadata();
                optional = resourceMetadata.getSection(AnimationMetadataSection.TYPE);
                optional2 = resourceMetadata.getSection(TextureMetadataSection.TYPE);
                list = resourceMetadata.getTypedSections(set);
            }
            catch (Exception exception) {
                LOGGER.error("Unable to parse metadata from {}", (Object)identifier, (Object)exception);
                return null;
            }
            try (InputStream inputStream = resource.open();){
                nativeImage = NativeImage.read(inputStream);
            }
            catch (IOException iOException) {
                LOGGER.error("Using missing texture, unable to load {}", (Object)identifier, (Object)iOException);
                return null;
            }
            if (optional.isPresent()) {
                frameSize = optional.get().calculateFrameSize(nativeImage.getWidth(), nativeImage.getHeight());
                if (!Mth.isMultipleOf(nativeImage.getWidth(), frameSize.width()) || !Mth.isMultipleOf(nativeImage.getHeight(), frameSize.height())) {
                    LOGGER.error("Image {} size {},{} is not multiple of frame size {},{}", new Object[]{identifier, nativeImage.getWidth(), nativeImage.getHeight(), frameSize.width(), frameSize.height()});
                    nativeImage.close();
                    return null;
                }
            } else {
                frameSize = new FrameSize(nativeImage.getWidth(), nativeImage.getHeight());
            }
            return new SpriteContents(identifier, frameSize, nativeImage, optional, list, optional2);
        };
    }

    public @Nullable SpriteContents loadSprite(Identifier var1, Resource var2);
}

