/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.realmsclient.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record UploadResult(int statusCode, @Nullable String errorMessage) {
    public @Nullable String getSimplifiedErrorMessage() {
        if (this.statusCode < 200 || this.statusCode >= 300) {
            if (this.statusCode == 400 && this.errorMessage != null) {
                return this.errorMessage;
            }
            return String.valueOf(this.statusCode);
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private int statusCode = -1;
        private @Nullable String errorMessage;

        public Builder withStatusCode(int i) {
            this.statusCode = i;
            return this;
        }

        public Builder withErrorMessage(@Nullable String string) {
            this.errorMessage = string;
            return this;
        }

        public UploadResult build() {
            return new UploadResult(this.statusCode, this.errorMessage);
        }
    }
}

