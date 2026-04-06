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
package com.mojang.realmsclient.util;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.PlayerSkinRenderCache;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
    private static final int MINUTES = 60;
    private static final int HOURS = 3600;
    private static final int DAYS = 86400;

    public static Component convertToAgePresentation(long l) {
        if (l < 0L) {
            return RIGHT_NOW;
        }
        long m = l / 1000L;
        if (m < 60L) {
            return Component.translatable("mco.time.secondsAgo", m);
        }
        if (m < 3600L) {
            long n = m / 60L;
            return Component.translatable("mco.time.minutesAgo", n);
        }
        if (m < 86400L) {
            long n = m / 3600L;
            return Component.translatable("mco.time.hoursAgo", n);
        }
        long n = m / 86400L;
        return Component.translatable("mco.time.daysAgo", n);
    }

    public static Component convertToAgePresentationFromInstant(Instant instant) {
        return RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - instant.toEpochMilli());
    }

    public static void renderPlayerFace(GuiGraphics guiGraphics, int i, int j, int k, UUID uUID) {
        PlayerSkinRenderCache.RenderInfo renderInfo = Minecraft.getInstance().playerSkinRenderCache().getOrDefault(ResolvableProfile.createUnresolved(uUID));
        PlayerFaceRenderer.draw(guiGraphics, renderInfo.playerSkin(), i, j, k);
    }

    public static <T> CompletableFuture<T> supplyAsync(RealmsIoFunction<T> realmsIoFunction, @Nullable Consumer<RealmsServiceException> consumer) {
        return CompletableFuture.supplyAsync(() -> {
            RealmsClient realmsClient = RealmsClient.getOrCreate();
            try {
                return realmsIoFunction.apply(realmsClient);
            }
            catch (Throwable throwable) {
                if (throwable instanceof RealmsServiceException) {
                    RealmsServiceException realmsServiceException = (RealmsServiceException)throwable;
                    if (consumer != null) {
                        consumer.accept(realmsServiceException);
                    }
                } else {
                    LOGGER.error("Unhandled exception", throwable);
                }
                throw new RuntimeException(throwable);
            }
        }, Util.nonCriticalIoPool());
    }

    public static CompletableFuture<Void> runAsync(RealmsIoConsumer realmsIoConsumer, @Nullable Consumer<RealmsServiceException> consumer) {
        return RealmsUtil.supplyAsync(realmsIoConsumer, consumer);
    }

    public static Consumer<RealmsServiceException> openScreenOnFailure(Function<RealmsServiceException, Screen> function) {
        Minecraft minecraft = Minecraft.getInstance();
        return realmsServiceException -> minecraft.execute(() -> minecraft.setScreen((Screen)function.apply((RealmsServiceException)realmsServiceException)));
    }

    public static Consumer<RealmsServiceException> openScreenAndLogOnFailure(Function<RealmsServiceException, Screen> function, String string) {
        return RealmsUtil.openScreenOnFailure(function).andThen(realmsServiceException -> LOGGER.error(string, (Throwable)realmsServiceException));
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface RealmsIoFunction<T> {
        public T apply(RealmsClient var1) throws RealmsServiceException;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface RealmsIoConsumer
    extends RealmsIoFunction<Void> {
        public void accept(RealmsClient var1) throws RealmsServiceException;

        @Override
        default public Void apply(RealmsClient realmsClient) throws RealmsServiceException {
            this.accept(realmsClient);
            return null;
        }
    }
}

