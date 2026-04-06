/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 *  it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SocialInteractionsPlayerList
extends ContainerObjectSelectionList<PlayerEntry> {
    private final SocialInteractionsScreen socialInteractionsScreen;
    private final List<PlayerEntry> players = Lists.newArrayList();
    private @Nullable String filter;

    public SocialInteractionsPlayerList(SocialInteractionsScreen socialInteractionsScreen, Minecraft minecraft, int i, int j, int k, int l) {
        super(minecraft, i, j, k, l);
        this.socialInteractionsScreen = socialInteractionsScreen;
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
    }

    @Override
    protected void enableScissor(GuiGraphics guiGraphics) {
        guiGraphics.enableScissor(this.getX(), this.getY() + 4, this.getRight(), this.getBottom());
    }

    public void updatePlayerList(Collection<UUID> collection, double d, boolean bl) {
        HashMap<UUID, PlayerEntry> map = new HashMap<UUID, PlayerEntry>();
        this.addOnlinePlayers(collection, map);
        if (bl) {
            this.addSeenPlayers(map);
        }
        this.updatePlayersFromChatLog(map, bl);
        this.updateFiltersAndScroll(map.values(), d);
    }

    private void addOnlinePlayers(Collection<UUID> collection, Map<UUID, PlayerEntry> map) {
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        for (UUID uUID : collection) {
            PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(uUID);
            if (playerInfo == null) continue;
            PlayerEntry playerEntry = this.makePlayerEntry(uUID, playerInfo);
            map.put(uUID, playerEntry);
        }
    }

    private void addSeenPlayers(Map<UUID, PlayerEntry> map) {
        Map<UUID, PlayerInfo> map2 = this.minecraft.player.connection.getSeenPlayers();
        for (Map.Entry<UUID, PlayerInfo> entry : map2.entrySet()) {
            map.computeIfAbsent(entry.getKey(), uUID -> {
                PlayerEntry playerEntry = this.makePlayerEntry((UUID)uUID, (PlayerInfo)entry.getValue());
                playerEntry.setRemoved(true);
                return playerEntry;
            });
        }
    }

    private PlayerEntry makePlayerEntry(UUID uUID, PlayerInfo playerInfo) {
        return new PlayerEntry(this.minecraft, this.socialInteractionsScreen, uUID, playerInfo.getProfile().name(), playerInfo::getSkin, playerInfo.hasVerifiableChat());
    }

    private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> map, boolean bl) {
        Map<UUID, GameProfile> map2 = SocialInteractionsPlayerList.collectProfilesFromChatLog(this.minecraft.getReportingContext().chatLog());
        map2.forEach((uUID2, gameProfile) -> {
            PlayerEntry playerEntry;
            if (bl) {
                playerEntry = map.computeIfAbsent((UUID)uUID2, uUID -> {
                    PlayerEntry playerEntry = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, gameProfile.id(), gameProfile.name(), this.minecraft.getSkinManager().createLookup((GameProfile)gameProfile, true), true);
                    playerEntry.setRemoved(true);
                    return playerEntry;
                });
            } else {
                playerEntry = (PlayerEntry)map.get(uUID2);
                if (playerEntry == null) {
                    return;
                }
            }
            playerEntry.setHasRecentMessages(true);
        });
    }

    private static Map<UUID, GameProfile> collectProfilesFromChatLog(ChatLog chatLog) {
        Object2ObjectLinkedOpenHashMap map = new Object2ObjectLinkedOpenHashMap();
        for (int i = chatLog.end(); i >= chatLog.start(); --i) {
            LoggedChatMessage.Player player;
            LoggedChatEvent loggedChatEvent = chatLog.lookup(i);
            if (!(loggedChatEvent instanceof LoggedChatMessage.Player) || !(player = (LoggedChatMessage.Player)loggedChatEvent).message().hasSignature()) continue;
            map.put(player.profileId(), player.profile());
        }
        return map;
    }

    private void sortPlayerEntries() {
        this.players.sort(Comparator.comparing(playerEntry -> {
            if (this.minecraft.isLocalPlayer(playerEntry.getPlayerId())) {
                return 0;
            }
            if (this.minecraft.getReportingContext().hasDraftReportFor(playerEntry.getPlayerId())) {
                return 1;
            }
            if (playerEntry.getPlayerId().version() == 2) {
                return 4;
            }
            if (playerEntry.hasRecentMessages()) {
                return 2;
            }
            return 3;
        }).thenComparing(playerEntry -> {
            int i;
            if (!playerEntry.getPlayerName().isBlank() && ((i = playerEntry.getPlayerName().codePointAt(0)) == 95 || i >= 97 && i <= 122 || i >= 65 && i <= 90 || i >= 48 && i <= 57)) {
                return 0;
            }
            return 1;
        }).thenComparing(PlayerEntry::getPlayerName, String::compareToIgnoreCase));
    }

    private void updateFiltersAndScroll(Collection<PlayerEntry> collection, double d) {
        this.players.clear();
        this.players.addAll(collection);
        this.sortPlayerEntries();
        this.updateFilteredPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(d);
    }

    private void updateFilteredPlayers() {
        if (this.filter != null) {
            this.players.removeIf(playerEntry -> !playerEntry.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.players);
        }
    }

    public void setFilter(String string) {
        this.filter = string;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public void addPlayer(PlayerInfo playerInfo, SocialInteractionsScreen.Page page) {
        UUID uUID = playerInfo.getProfile().id();
        for (PlayerEntry playerEntry : this.players) {
            if (!playerEntry.getPlayerId().equals(uUID)) continue;
            playerEntry.setRemoved(false);
            return;
        }
        if ((page == SocialInteractionsScreen.Page.ALL || this.minecraft.getPlayerSocialManager().shouldHideMessageFrom(uUID)) && (Strings.isNullOrEmpty((String)this.filter) || playerInfo.getProfile().name().toLowerCase(Locale.ROOT).contains(this.filter))) {
            PlayerEntry playerEntry;
            boolean bl = playerInfo.hasVerifiableChat();
            playerEntry = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, playerInfo.getProfile().id(), playerInfo.getProfile().name(), playerInfo::getSkin, bl);
            this.addEntry(playerEntry);
            this.players.add(playerEntry);
        }
    }

    public void removePlayer(UUID uUID) {
        for (PlayerEntry playerEntry : this.players) {
            if (!playerEntry.getPlayerId().equals(uUID)) continue;
            playerEntry.setRemoved(true);
            return;
        }
    }

    public void refreshHasDraftReport() {
        this.players.forEach(playerEntry -> playerEntry.refreshHasDraftReport(this.minecraft.getReportingContext()));
    }
}

