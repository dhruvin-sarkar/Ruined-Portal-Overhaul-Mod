/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.multiplayer.chat;

import com.mojang.serialization.Codec;
import java.time.Instant;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public enum ChatTrustLevel implements StringRepresentable
{
    SECURE("secure"),
    MODIFIED("modified"),
    NOT_SECURE("not_secure");

    public static final Codec<ChatTrustLevel> CODEC;
    private final String serializedName;

    private ChatTrustLevel(String string2) {
        this.serializedName = string2;
    }

    public static ChatTrustLevel evaluate(PlayerChatMessage playerChatMessage, Component component, Instant instant) {
        if (!playerChatMessage.hasSignature() || playerChatMessage.hasExpiredClient(instant)) {
            return NOT_SECURE;
        }
        if (ChatTrustLevel.isModified(playerChatMessage, component)) {
            return MODIFIED;
        }
        return SECURE;
    }

    private static boolean isModified(PlayerChatMessage playerChatMessage, Component component) {
        if (!component.getString().contains(playerChatMessage.signedContent())) {
            return true;
        }
        Component component2 = playerChatMessage.unsignedContent();
        if (component2 == null) {
            return false;
        }
        return ChatTrustLevel.containsModifiedStyle(component2);
    }

    private static boolean containsModifiedStyle(Component component) {
        return component.visit((style, string) -> {
            if (ChatTrustLevel.isModifiedStyle(style)) {
                return Optional.of(true);
            }
            return Optional.empty();
        }, Style.EMPTY).orElse(false);
    }

    private static boolean isModifiedStyle(Style style) {
        return !style.getFont().equals(FontDescription.DEFAULT);
    }

    public boolean isNotSecure() {
        return this == NOT_SECURE;
    }

    public @Nullable GuiMessageTag createTag(PlayerChatMessage playerChatMessage) {
        return switch (this.ordinal()) {
            case 1 -> GuiMessageTag.chatModified(playerChatMessage.signedContent());
            case 2 -> GuiMessageTag.chatNotSecure();
            default -> null;
        };
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    static {
        CODEC = StringRepresentable.fromEnum(ChatTrustLevel::values);
    }
}

