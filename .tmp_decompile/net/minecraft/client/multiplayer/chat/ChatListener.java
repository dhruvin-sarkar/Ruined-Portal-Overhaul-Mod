/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  com.mojang.authlib.GameProfile
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.lang3.StringUtils
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.multiplayer.chat;

import com.google.common.collect.Queues;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import java.util.Deque;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.util.StringDecomposer;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class ChatListener {
    private static final Component CHAT_VALIDATION_ERROR = Component.translatable("chat.validation_error").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC);
    private final Minecraft minecraft;
    private final Deque<Message> delayedMessageQueue = Queues.newArrayDeque();
    private long messageDelay;
    private long previousMessageTime;

    public ChatListener(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void tick() {
        if (this.minecraft.isPaused()) {
            if (this.messageDelay > 0L) {
                this.previousMessageTime += 50L;
            }
            return;
        }
        if (this.messageDelay == 0L) {
            if (!this.delayedMessageQueue.isEmpty()) {
                this.flushQueue();
            }
        } else if (Util.getMillis() >= this.previousMessageTime + this.messageDelay) {
            Message message;
            while ((message = this.delayedMessageQueue.poll()) != null && !message.accept()) {
            }
        }
    }

    public void setMessageDelay(double d) {
        long l = (long)(d * 1000.0);
        if (l == 0L && this.messageDelay > 0L && !this.minecraft.isPaused()) {
            this.flushQueue();
        }
        this.messageDelay = l;
    }

    public void acceptNextDelayedMessage() {
        this.delayedMessageQueue.remove().accept();
    }

    public long queueSize() {
        return this.delayedMessageQueue.size();
    }

    public void flushQueue() {
        this.delayedMessageQueue.forEach(Message::accept);
        this.delayedMessageQueue.clear();
        this.previousMessageTime = 0L;
    }

    public boolean removeFromDelayedMessageQueue(MessageSignature messageSignature) {
        return this.delayedMessageQueue.removeIf(message -> messageSignature.equals((Object)message.signature()));
    }

    private boolean willDelayMessages() {
        return this.messageDelay > 0L && Util.getMillis() < this.previousMessageTime + this.messageDelay;
    }

    private void handleMessage(@Nullable MessageSignature messageSignature, BooleanSupplier booleanSupplier) {
        if (this.willDelayMessages()) {
            this.delayedMessageQueue.add(new Message(messageSignature, booleanSupplier));
        } else {
            booleanSupplier.getAsBoolean();
        }
    }

    public void handlePlayerChatMessage(PlayerChatMessage playerChatMessage, GameProfile gameProfile, ChatType.Bound bound) {
        boolean bl = this.minecraft.options.onlyShowSecureChat().get();
        PlayerChatMessage playerChatMessage2 = bl ? playerChatMessage.removeUnsignedContent() : playerChatMessage;
        Component component = bound.decorate(playerChatMessage2.decoratedContent());
        Instant instant = Instant.now();
        this.handleMessage(playerChatMessage.signature(), () -> {
            boolean bl2 = this.showMessageToPlayer(bound, playerChatMessage, component, gameProfile, bl, instant);
            ClientPacketListener clientPacketListener = this.minecraft.getConnection();
            if (clientPacketListener != null && playerChatMessage.signature() != null) {
                clientPacketListener.markMessageAsProcessed(playerChatMessage.signature(), bl2);
            }
            return bl2;
        });
    }

    public void handleChatMessageError(UUID uUID, @Nullable MessageSignature messageSignature, ChatType.Bound bound) {
        this.handleMessage(null, () -> {
            ClientPacketListener clientPacketListener = this.minecraft.getConnection();
            if (clientPacketListener != null && messageSignature != null) {
                clientPacketListener.markMessageAsProcessed(messageSignature, false);
            }
            if (this.minecraft.isBlocked(uUID)) {
                return false;
            }
            Component component = bound.decorate(CHAT_VALIDATION_ERROR);
            this.minecraft.gui.getChat().addMessage(component, null, GuiMessageTag.chatError());
            this.minecraft.getNarrator().saySystemChatQueued(bound.decorateNarration(CHAT_VALIDATION_ERROR));
            this.previousMessageTime = Util.getMillis();
            return true;
        });
    }

    public void handleDisguisedChatMessage(Component component, ChatType.Bound bound) {
        Instant instant = Instant.now();
        this.handleMessage(null, () -> {
            Component component2 = bound.decorate(component);
            this.minecraft.gui.getChat().addMessage(component2);
            this.narrateChatMessage(bound, component);
            this.logSystemMessage(component2, instant);
            this.previousMessageTime = Util.getMillis();
            return true;
        });
    }

    private boolean showMessageToPlayer(ChatType.Bound bound, PlayerChatMessage playerChatMessage, Component component, GameProfile gameProfile, boolean bl, Instant instant) {
        ChatTrustLevel chatTrustLevel = this.evaluateTrustLevel(playerChatMessage, component, instant);
        if (bl && chatTrustLevel.isNotSecure()) {
            return false;
        }
        if (this.minecraft.isBlocked(playerChatMessage.sender()) || playerChatMessage.isFullyFiltered()) {
            return false;
        }
        GuiMessageTag guiMessageTag = chatTrustLevel.createTag(playerChatMessage);
        MessageSignature messageSignature = playerChatMessage.signature();
        FilterMask filterMask = playerChatMessage.filterMask();
        if (filterMask.isEmpty()) {
            this.minecraft.gui.getChat().addMessage(component, messageSignature, guiMessageTag);
            this.narrateChatMessage(bound, playerChatMessage.decoratedContent());
        } else {
            Component component2 = filterMask.applyWithFormatting(playerChatMessage.signedContent());
            if (component2 != null) {
                this.minecraft.gui.getChat().addMessage(bound.decorate(component2), messageSignature, guiMessageTag);
                this.narrateChatMessage(bound, component2);
            }
        }
        this.logPlayerMessage(playerChatMessage, gameProfile, chatTrustLevel);
        this.previousMessageTime = Util.getMillis();
        return true;
    }

    private void narrateChatMessage(ChatType.Bound bound, Component component) {
        this.minecraft.getNarrator().sayChatQueued(bound.decorateNarration(component));
    }

    private ChatTrustLevel evaluateTrustLevel(PlayerChatMessage playerChatMessage, Component component, Instant instant) {
        if (this.isSenderLocalPlayer(playerChatMessage.sender())) {
            return ChatTrustLevel.SECURE;
        }
        return ChatTrustLevel.evaluate(playerChatMessage, component, instant);
    }

    private void logPlayerMessage(PlayerChatMessage playerChatMessage, GameProfile gameProfile, ChatTrustLevel chatTrustLevel) {
        ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
        chatLog.push(LoggedChatMessage.player(gameProfile, playerChatMessage, chatTrustLevel));
    }

    private void logSystemMessage(Component component, Instant instant) {
        ChatLog chatLog = this.minecraft.getReportingContext().chatLog();
        chatLog.push(LoggedChatMessage.system(component, instant));
    }

    public void handleSystemMessage(Component component, boolean bl) {
        if (this.minecraft.options.hideMatchedNames().get().booleanValue() && this.minecraft.isBlocked(this.guessChatUUID(component))) {
            return;
        }
        if (bl) {
            this.minecraft.gui.setOverlayMessage(component, false);
            this.minecraft.getNarrator().saySystemQueued(component);
        } else {
            this.minecraft.gui.getChat().addMessage(component);
            this.logSystemMessage(component, Instant.now());
            this.minecraft.getNarrator().saySystemChatQueued(component);
        }
    }

    private UUID guessChatUUID(Component component) {
        String string = StringDecomposer.getPlainText(component);
        String string2 = StringUtils.substringBetween((String)string, (String)"<", (String)">");
        if (string2 == null) {
            return Util.NIL_UUID;
        }
        return this.minecraft.getPlayerSocialManager().getDiscoveredUUID(string2);
    }

    private boolean isSenderLocalPlayer(UUID uUID) {
        if (this.minecraft.isLocalServer() && this.minecraft.player != null) {
            UUID uUID2 = this.minecraft.player.getGameProfile().id();
            return uUID2.equals(uUID);
        }
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    record Message(@Nullable MessageSignature signature, BooleanSupplier handler) {
        public boolean accept() {
            return this.handler.getAsBoolean();
        }
    }
}

