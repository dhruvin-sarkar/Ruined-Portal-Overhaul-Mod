/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.ServicesKeySet
 *  com.mojang.authlib.yggdrasil.ServicesKeyType
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.logging.LogUtils;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Collection;
import net.minecraft.util.SignatureUpdater;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public interface SignatureValidator {
    public static final SignatureValidator NO_VALIDATION = (signatureUpdater, bs) -> true;
    public static final Logger LOGGER = LogUtils.getLogger();

    public boolean validate(SignatureUpdater var1, byte[] var2);

    default public boolean validate(byte[] bs, byte[] cs) {
        return this.validate(output -> output.update(bs), cs);
    }

    private static boolean verifySignature(SignatureUpdater signatureUpdater, byte[] bs, Signature signature) throws SignatureException {
        signatureUpdater.update(signature::update);
        return signature.verify(bs);
    }

    public static SignatureValidator from(PublicKey publicKey, String string) {
        return (signatureUpdater, bs) -> {
            try {
                Signature signature = Signature.getInstance(string);
                signature.initVerify(publicKey);
                return SignatureValidator.verifySignature(signatureUpdater, bs, signature);
            }
            catch (Exception exception) {
                LOGGER.error("Failed to verify signature", (Throwable)exception);
                return false;
            }
        };
    }

    public static @Nullable SignatureValidator from(ServicesKeySet servicesKeySet, ServicesKeyType servicesKeyType) {
        Collection collection = servicesKeySet.keys(servicesKeyType);
        if (collection.isEmpty()) {
            return null;
        }
        return (signatureUpdater, bs) -> collection.stream().anyMatch(servicesKeyInfo -> {
            Signature signature = servicesKeyInfo.signature();
            try {
                return SignatureValidator.verifySignature(signatureUpdater, bs, signature);
            }
            catch (SignatureException signatureException) {
                LOGGER.error("Failed to verify Services signature", (Throwable)signatureException);
                return false;
            }
        });
    }
}

