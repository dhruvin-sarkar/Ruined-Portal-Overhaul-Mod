/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.chat;

import com.mojang.logging.LogUtils;
import java.util.function.BooleanSupplier;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.SignatureValidator;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@FunctionalInterface
public interface SignedMessageValidator {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SignedMessageValidator ACCEPT_UNSIGNED = PlayerChatMessage::removeSignature;
    public static final SignedMessageValidator REJECT_ALL = playerChatMessage -> {
        LOGGER.error("Received chat message from {}, but they have no chat session initialized and secure chat is enforced", (Object)playerChatMessage.sender());
        return null;
    };

    public @Nullable PlayerChatMessage updateAndValidate(PlayerChatMessage var1);

    public static class KeyBased
    implements SignedMessageValidator {
        private final SignatureValidator validator;
        private final BooleanSupplier expired;
        private @Nullable PlayerChatMessage lastMessage;
        private boolean isChainValid = true;

        public KeyBased(SignatureValidator signatureValidator, BooleanSupplier booleanSupplier) {
            this.validator = signatureValidator;
            this.expired = booleanSupplier;
        }

        private boolean validateChain(PlayerChatMessage playerChatMessage) {
            if (playerChatMessage.equals((Object)this.lastMessage)) {
                return true;
            }
            if (this.lastMessage != null && !playerChatMessage.link().isDescendantOf(this.lastMessage.link())) {
                LOGGER.error("Received out-of-order chat message from {}: expected index > {} for session {}, but was {} for session {}", new Object[]{playerChatMessage.sender(), this.lastMessage.link().index(), this.lastMessage.link().sessionId(), playerChatMessage.link().index(), playerChatMessage.link().sessionId()});
                return false;
            }
            return true;
        }

        private boolean validate(PlayerChatMessage playerChatMessage) {
            if (this.expired.getAsBoolean()) {
                LOGGER.error("Received message with expired profile public key from {} with session {}", (Object)playerChatMessage.sender(), (Object)playerChatMessage.link().sessionId());
                return false;
            }
            if (!playerChatMessage.verify(this.validator)) {
                LOGGER.error("Received message with invalid signature (is the session wrong, or signature cache out of sync?): {}", (Object)PlayerChatMessage.describeSigned(playerChatMessage));
                return false;
            }
            return this.validateChain(playerChatMessage);
        }

        @Override
        public @Nullable PlayerChatMessage updateAndValidate(PlayerChatMessage playerChatMessage) {
            boolean bl = this.isChainValid = this.isChainValid && this.validate(playerChatMessage);
            if (!this.isChainValid) {
                return null;
            }
            this.lastMessage = playerChatMessage;
            return playerChatMessage;
        }
    }
}

