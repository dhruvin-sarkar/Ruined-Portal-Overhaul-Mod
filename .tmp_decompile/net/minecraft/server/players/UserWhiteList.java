/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;
import net.minecraft.server.players.UserWhiteListEntry;

public class UserWhiteList
extends StoredUserList<NameAndId, UserWhiteListEntry> {
    public UserWhiteList(File file, NotificationService notificationService) {
        super(file, notificationService);
    }

    @Override
    protected StoredUserEntry<NameAndId> createEntry(JsonObject jsonObject) {
        return new UserWhiteListEntry(jsonObject);
    }

    public boolean isWhiteListed(NameAndId nameAndId) {
        return this.contains(nameAndId);
    }

    @Override
    public boolean add(UserWhiteListEntry userWhiteListEntry) {
        if (super.add(userWhiteListEntry)) {
            if (userWhiteListEntry.getUser() != null) {
                this.notificationService.playerAddedToAllowlist((NameAndId)((Object)userWhiteListEntry.getUser()));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(NameAndId nameAndId) {
        if (super.remove(nameAndId)) {
            this.notificationService.playerRemovedFromAllowlist(nameAndId);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (UserWhiteListEntry userWhiteListEntry : this.getEntries()) {
            if (userWhiteListEntry.getUser() == null) continue;
            this.notificationService.playerRemovedFromAllowlist((NameAndId)((Object)userWhiteListEntry.getUser()));
        }
        super.clear();
    }

    @Override
    public String[] getUserList() {
        return (String[])this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(NameAndId::name).toArray(String[]::new);
    }

    @Override
    protected String getKeyForUser(NameAndId nameAndId) {
        return nameAndId.id().toString();
    }

    @Override
    protected /* synthetic */ String getKeyForUser(Object object) {
        return this.getKeyForUser((NameAndId)((Object)object));
    }

    @Override
    public /* synthetic */ boolean remove(Object object) {
        return this.remove((NameAndId)((Object)object));
    }
}

