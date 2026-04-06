/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.client;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsHttpException;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public interface RealmsError {
    public static final Component NO_MESSAGE = Component.translatable("mco.errorMessage.noDetails");
    public static final Logger LOGGER = LogUtils.getLogger();

    public int errorCode();

    public Component errorMessage();

    public String logMessage();

    public static RealmsError parse(int i, String string) {
        if (i == 429) {
            return CustomError.SERVICE_BUSY;
        }
        if (Strings.isNullOrEmpty((String)string)) {
            return CustomError.noPayload(i);
        }
        try {
            JsonObject jsonObject = LenientJsonParser.parse(string).getAsJsonObject();
            String string2 = GsonHelper.getAsString(jsonObject, "reason", null);
            String string3 = GsonHelper.getAsString(jsonObject, "errorMsg", null);
            int j = GsonHelper.getAsInt(jsonObject, "errorCode", -1);
            if (string3 != null || string2 != null || j != -1) {
                return new ErrorWithJsonPayload(i, j != -1 ? j : i, string2, string3);
            }
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse RealmsError", (Throwable)exception);
        }
        return new ErrorWithRawPayload(i, string);
    }

    @Environment(value=EnvType.CLIENT)
    public record CustomError(int httpCode, @Nullable Component payload) implements RealmsError
    {
        public static final CustomError SERVICE_BUSY = new CustomError(429, Component.translatable("mco.errorMessage.serviceBusy"));
        public static final Component RETRY_MESSAGE = Component.translatable("mco.errorMessage.retry");
        public static final String BODY_TAG = "<body>";
        public static final String CLOSING_BODY_TAG = "</body>";

        public static CustomError unknownCompatibilityResponse(String string) {
            return new CustomError(500, Component.translatable("mco.errorMessage.realmsService.unknownCompatibility", string));
        }

        public static CustomError configurationError() {
            return new CustomError(500, Component.translatable("mco.errorMessage.realmsService.configurationError"));
        }

        public static CustomError connectivityError(RealmsHttpException realmsHttpException) {
            return new CustomError(500, Component.translatable("mco.errorMessage.realmsService.connectivity", realmsHttpException.getMessage()));
        }

        public static CustomError retry(int i) {
            return new CustomError(i, RETRY_MESSAGE);
        }

        public static CustomError noPayload(int i) {
            return new CustomError(i, null);
        }

        public static CustomError htmlPayload(int i, String string) {
            int j = string.indexOf(BODY_TAG);
            int k = string.indexOf(CLOSING_BODY_TAG);
            if (j >= 0 && k > j) {
                return new CustomError(i, Component.literal(string.substring(j + BODY_TAG.length(), k).trim()));
            }
            LOGGER.error("Got an error with an unreadable html body {}", (Object)string);
            return new CustomError(i, null);
        }

        @Override
        public int errorCode() {
            return this.httpCode;
        }

        @Override
        public Component errorMessage() {
            return this.payload != null ? this.payload : NO_MESSAGE;
        }

        @Override
        public String logMessage() {
            if (this.payload != null) {
                return String.format(Locale.ROOT, "Realms service error (%d) with message '%s'", this.httpCode, this.payload.getString());
            }
            return String.format(Locale.ROOT, "Realms service error (%d) with no payload", this.httpCode);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record ErrorWithJsonPayload(int httpCode, int code, @Nullable String reason, @Nullable String message) implements RealmsError
    {
        @Override
        public int errorCode() {
            return this.code;
        }

        @Override
        public Component errorMessage() {
            String string2;
            String string = "mco.errorMessage." + this.code;
            if (I18n.exists(string)) {
                return Component.translatable(string);
            }
            if (this.reason != null && I18n.exists(string2 = "mco.errorReason." + this.reason)) {
                return Component.translatable(string2);
            }
            return this.message != null ? Component.literal(this.message) : NO_MESSAGE;
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms service error (%d/%d/%s) with message '%s'", this.httpCode, this.code, this.reason, this.message);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record ErrorWithRawPayload(int httpCode, String payload) implements RealmsError
    {
        @Override
        public int errorCode() {
            return this.httpCode;
        }

        @Override
        public Component errorMessage() {
            return Component.literal(this.payload);
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms service error (%d) with raw payload '%s'", this.httpCode, this.payload);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record AuthenticationError(String message) implements RealmsError
    {
        public static final int ERROR_CODE = 401;

        @Override
        public int errorCode() {
            return 401;
        }

        @Override
        public Component errorMessage() {
            return Component.literal(this.message);
        }

        @Override
        public String logMessage() {
            return String.format(Locale.ROOT, "Realms authentication error with message '%s'", this.message);
        }
    }
}

