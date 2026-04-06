/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package com.mojang.realmsclient.exception;

import com.mojang.realmsclient.client.RealmsError;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RealmsServiceException
extends Exception {
    public final RealmsError realmsError;

    public RealmsServiceException(RealmsError realmsError) {
        this.realmsError = realmsError;
    }

    @Override
    public String getMessage() {
        return this.realmsError.logMessage();
    }
}

