/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.screens.AddRealmPopupScreen;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class TextureManager
implements PreparableReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Identifier INTENTIONAL_MISSING_TEXTURE = Identifier.withDefaultNamespace("");
    private final Map<Identifier, AbstractTexture> byPath = new HashMap<Identifier, AbstractTexture>();
    private final Set<TickableTexture> tickableTextures = new HashSet<TickableTexture>();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        NativeImage nativeImage = MissingTextureAtlasSprite.generateMissingImage();
        this.register(MissingTextureAtlasSprite.getLocation(), new DynamicTexture(() -> "(intentionally-)Missing Texture", nativeImage));
    }

    public void registerAndLoad(Identifier identifier, ReloadableTexture reloadableTexture) {
        try {
            reloadableTexture.apply(this.loadContentsSafe(identifier, reloadableTexture));
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Uploading texture");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Uploaded texture");
            crashReportCategory.setDetail("Resource location", reloadableTexture.resourceId());
            crashReportCategory.setDetail("Texture id", identifier);
            throw new ReportedException(crashReport);
        }
        this.register(identifier, reloadableTexture);
    }

    private TextureContents loadContentsSafe(Identifier identifier, ReloadableTexture reloadableTexture) {
        try {
            return TextureManager.loadContents(this.resourceManager, identifier, reloadableTexture);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load texture {} into slot {}", new Object[]{reloadableTexture.resourceId(), identifier, exception});
            return TextureContents.createMissing();
        }
    }

    public void registerForNextReload(Identifier identifier) {
        this.register(identifier, new SimpleTexture(identifier));
    }

    public void register(Identifier identifier, AbstractTexture abstractTexture) {
        AbstractTexture abstractTexture2 = this.byPath.put(identifier, abstractTexture);
        if (abstractTexture2 != abstractTexture) {
            if (abstractTexture2 != null) {
                this.safeClose(identifier, abstractTexture2);
            }
            if (abstractTexture instanceof TickableTexture) {
                TickableTexture tickableTexture = (TickableTexture)((Object)abstractTexture);
                this.tickableTextures.add(tickableTexture);
            }
        }
    }

    private void safeClose(Identifier identifier, AbstractTexture abstractTexture) {
        this.tickableTextures.remove(abstractTexture);
        try {
            abstractTexture.close();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to close texture {}", (Object)identifier, (Object)exception);
        }
    }

    public AbstractTexture getTexture(Identifier identifier) {
        AbstractTexture abstractTexture = this.byPath.get(identifier);
        if (abstractTexture != null) {
            return abstractTexture;
        }
        SimpleTexture simpleTexture = new SimpleTexture(identifier);
        this.registerAndLoad(identifier, simpleTexture);
        return simpleTexture;
    }

    public void tick() {
        for (TickableTexture tickableTexture : this.tickableTextures) {
            tickableTexture.tick();
        }
    }

    public void release(Identifier identifier) {
        AbstractTexture abstractTexture = this.byPath.remove(identifier);
        if (abstractTexture != null) {
            this.safeClose(identifier, abstractTexture);
        }
    }

    @Override
    public void close() {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier preparationBarrier, Executor executor2) {
        ResourceManager resourceManager = sharedState.resourceManager();
        ArrayList list = new ArrayList();
        this.byPath.forEach((identifier, abstractTexture) -> {
            if (abstractTexture instanceof ReloadableTexture) {
                ReloadableTexture reloadableTexture = (ReloadableTexture)abstractTexture;
                list.add(TextureManager.scheduleLoad(resourceManager, identifier, reloadableTexture, executor));
            }
        });
        return ((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])list.stream().map(PendingReload::newContents).toArray(CompletableFuture[]::new)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(void_ -> {
            AddRealmPopupScreen.updateCarouselImages(this.resourceManager);
            for (PendingReload pendingReload : list) {
                pendingReload.texture.apply(pendingReload.newContents.join());
            }
        }, executor2);
    }

    public void dumpAllSheets(Path path) {
        try {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to create directory {}", (Object)path, (Object)iOException);
            return;
        }
        this.byPath.forEach((identifier, abstractTexture) -> {
            if (abstractTexture instanceof Dumpable) {
                Dumpable dumpable = (Dumpable)((Object)abstractTexture);
                try {
                    dumpable.dumpContents((Identifier)identifier, path);
                }
                catch (Exception exception) {
                    LOGGER.error("Failed to dump texture {}", identifier, (Object)exception);
                }
            }
        });
    }

    private static TextureContents loadContents(ResourceManager resourceManager, Identifier identifier, ReloadableTexture reloadableTexture) throws IOException {
        try {
            return reloadableTexture.loadContents(resourceManager);
        }
        catch (FileNotFoundException fileNotFoundException) {
            if (identifier != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Missing resource {} referenced from {}", (Object)reloadableTexture.resourceId(), (Object)identifier);
            }
            return TextureContents.createMissing();
        }
    }

    private static PendingReload scheduleLoad(ResourceManager resourceManager, Identifier identifier, ReloadableTexture reloadableTexture, Executor executor) {
        return new PendingReload(reloadableTexture, CompletableFuture.supplyAsync(() -> {
            try {
                return TextureManager.loadContents(resourceManager, identifier, reloadableTexture);
            }
            catch (IOException iOException) {
                throw new UncheckedIOException(iOException);
            }
        }, executor));
    }

    @Environment(value=EnvType.CLIENT)
    static final class PendingReload
    extends Record {
        final ReloadableTexture texture;
        final CompletableFuture<TextureContents> newContents;

        PendingReload(ReloadableTexture reloadableTexture, CompletableFuture<TextureContents> completableFuture) {
            this.texture = reloadableTexture;
            this.newContents = completableFuture;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PendingReload.class, "texture;newContents", "texture", "newContents"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PendingReload.class, "texture;newContents", "texture", "newContents"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PendingReload.class, "texture;newContents", "texture", "newContents"}, this, object);
        }

        public ReloadableTexture texture() {
            return this.texture;
        }

        public CompletableFuture<TextureContents> newContents() {
            return this.newContents;
        }
    }
}

