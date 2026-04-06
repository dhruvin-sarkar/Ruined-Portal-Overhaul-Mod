/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  java.lang.MatchException
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsClientOutdatedScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;
import com.mojang.realmsclient.gui.screens.RealmsParentalConsentScreen;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class RealmsAvailability {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static @Nullable CompletableFuture<Result> future;

    public static CompletableFuture<Result> get() {
        if (future == null || RealmsAvailability.shouldRefresh(future)) {
            future = RealmsAvailability.check();
        }
        return future;
    }

    private static boolean shouldRefresh(CompletableFuture<Result> completableFuture) {
        Result result = completableFuture.getNow(null);
        return result != null && result.exception() != null;
    }

    private static CompletableFuture<Result> check() {
        if (Minecraft.getInstance().isOfflineDeveloperMode()) {
            return CompletableFuture.completedFuture(new Result(Type.AUTHENTICATION_ERROR));
        }
        if (SharedConstants.DEBUG_BYPASS_REALMS_VERSION_CHECK) {
            return CompletableFuture.completedFuture(new Result(Type.SUCCESS));
        }
        return CompletableFuture.supplyAsync(() -> {
            RealmsClient realmsClient = RealmsClient.getOrCreate();
            try {
                if (realmsClient.clientCompatible() != RealmsClient.CompatibleVersionResponse.COMPATIBLE) {
                    return new Result(Type.INCOMPATIBLE_CLIENT);
                }
                if (!realmsClient.hasParentalConsent()) {
                    return new Result(Type.NEEDS_PARENTAL_CONSENT);
                }
                return new Result(Type.SUCCESS);
            }
            catch (RealmsServiceException realmsServiceException) {
                LOGGER.error("Couldn't connect to realms", (Throwable)realmsServiceException);
                if (realmsServiceException.realmsError.errorCode() == 401) {
                    return new Result(Type.AUTHENTICATION_ERROR);
                }
                return new Result(realmsServiceException);
            }
        }, Util.ioPool());
    }

    @Environment(value=EnvType.CLIENT)
    public record Result(Type type, @Nullable RealmsServiceException exception) {
        public Result(Type type) {
            this(type, null);
        }

        public Result(RealmsServiceException realmsServiceException) {
            this(Type.UNEXPECTED_ERROR, realmsServiceException);
        }

        public @Nullable Screen createErrorScreen(Screen screen) {
            return switch (this.type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> null;
                case 1 -> new RealmsClientOutdatedScreen(screen);
                case 2 -> new RealmsParentalConsentScreen(screen);
                case 3 -> new RealmsGenericErrorScreen(Component.translatable("mco.error.invalid.session.title"), Component.translatable("mco.error.invalid.session.message"), screen);
                case 4 -> new RealmsGenericErrorScreen(Objects.requireNonNull(this.exception), screen);
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Type {
        SUCCESS,
        INCOMPATIBLE_CLIENT,
        NEEDS_PARENTAL_CONSENT,
        AUTHENTICATION_ERROR,
        UNEXPECTED_ERROR;

    }
}

