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
import net.minecraft.server.players.UserBanListEntry;

public class UserBanList
extends StoredUserList<NameAndId, UserBanListEntry> {
    public UserBanList(File file, NotificationService notificationService) {
        super(file, notificationService);
    }

    @Override
    protected StoredUserEntry<NameAndId> createEntry(JsonObject jsonObject) {
        return new UserBanListEntry(jsonObject);
    }

    public boolean isBanned(NameAndId nameAndId) {
        return this.contains(nameAndId);
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
    public boolean add(UserBanListEntry userBanListEntry) {
        if (super.add(userBanListEntry)) {
            if (userBanListEntry.getUser() != null) {
                this.notificationService.playerBanned(userBanListEntry);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(NameAndId nameAndId) {
        if (super.remove(nameAndId)) {
            this.notificationService.playerUnbanned(nameAndId);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (UserBanListEntry userBanListEntry : this.getEntries()) {
            if (userBanListEntry.getUser() == null) continue;
            this.notificationService.playerUnbanned((NameAndId)((Object)userBanListEntry.getUser()));
        }
        super.clear();
    }

    @Override
    public /* synthetic */ boolean remove(Object object) {
        return this.remove((NameAndId)((Object)object));
    }
}

