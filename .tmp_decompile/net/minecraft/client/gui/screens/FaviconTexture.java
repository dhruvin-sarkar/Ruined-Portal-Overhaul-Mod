/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.Hashing
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class FaviconTexture
implements AutoCloseable {
    private static final Identifier MISSING_LOCATION = Identifier.withDefaultNamespace("textures/misc/unknown_server.png");
    private static final int WIDTH = 64;
    private static final int HEIGHT = 64;
    private final TextureManager textureManager;
    private final Identifier textureLocation;
    private @Nullable DynamicTexture texture;
    private boolean closed;

    private FaviconTexture(TextureManager textureManager, Identifier identifier) {
        this.textureManager = textureManager;
        this.textureLocation = identifier;
    }

    public static FaviconTexture forWorld(TextureManager textureManager, String string) {
        return new FaviconTexture(textureManager, Identifier.withDefaultNamespace("worlds/" + Util.sanitizeName(string, Identifier::validPathChar) + "/" + String.valueOf(Hashing.sha1().hashUnencodedChars((CharSequence)string)) + "/icon"));
    }

    public static FaviconTexture forServer(TextureManager textureManager, String string) {
        return new FaviconTexture(textureManager, Identifier.withDefaultNamespace("servers/" + String.valueOf(Hashing.sha1().hashUnencodedChars((CharSequence)string)) + "/icon"));
    }

    public void upload(NativeImage nativeImage) {
        if (nativeImage.getWidth() != 64 || nativeImage.getHeight() != 64) {
            nativeImage.close();
            throw new IllegalArgumentException("Icon must be 64x64, but was " + nativeImage.getWidth() + "x" + nativeImage.getHeight());
        }
        try {
            this.checkOpen();
            if (this.texture == null) {
                this.texture = new DynamicTexture(() -> "Favicon " + String.valueOf(this.textureLocation), nativeImage);
            } else {
                this.texture.setPixels(nativeImage);
                this.texture.upload();
            }
            this.textureManager.register(this.textureLocation, this.texture);
        }
        catch (Throwable throwable) {
            nativeImage.close();
            this.clear();
            throw throwable;
        }
    }

    public void clear() {
        this.checkOpen();
        if (this.texture != null) {
            this.textureManager.release(this.textureLocation);
            this.texture.close();
            this.texture = null;
        }
    }

    public Identifier textureLocation() {
        return this.texture != null ? this.textureLocation : MISSING_LOCATION;
    }

    @Override
    public void close() {
        this.clear();
        this.closed = true;
    }

    public boolean isClosed() {
        return this.closed;
    }

    private void checkOpen() {
        if (this.closed) {
            throw new IllegalStateException("Icon already closed");
        }
    }
}

