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
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.server.players.StoredUserList;

public class ServerOpList
extends StoredUserList<NameAndId, ServerOpListEntry> {
    public ServerOpList(File file, NotificationService notificationService) {
        super(file, notificationService);
    }

    @Override
    protected StoredUserEntry<NameAndId> createEntry(JsonObject jsonObject) {
        return new ServerOpListEntry(jsonObject);
    }

    @Override
    public String[] getUserList() {
        return (String[])this.getEntries().stream().map(StoredUserEntry::getUser).filter(Objects::nonNull).map(NameAndId::name).toArray(String[]::new);
    }

    @Override
    public boolean add(ServerOpListEntry serverOpListEntry) {
        if (super.add(serverOpListEntry)) {
            if (serverOpListEntry.getUser() != null) {
                this.notificationService.playerOped(serverOpListEntry);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(NameAndId nameAndId) {
        ServerOpListEntry serverOpListEntry = (ServerOpListEntry)this.get(nameAndId);
        if (super.remove(nameAndId)) {
            if (serverOpListEntry != null) {
                this.notificationService.playerDeoped(serverOpListEntry);
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (ServerOpListEntry serverOpListEntry : this.getEntries()) {
            if (serverOpListEntry.getUser() == null) continue;
            this.notificationService.playerDeoped(serverOpListEntry);
        }
        super.clear();
    }

    public boolean canBypassPlayerLimit(NameAndId nameAndId) {
        ServerOpListEntry serverOpListEntry = (ServerOpListEntry)this.get(nameAndId);
        if (serverOpListEntry != null) {
            return serverOpListEntry.getBypassesPlayerLimit();
        }
        return false;
    }

    @Override
    protected String getKeyForUser(NameAndId nameAndId) {
        return nameAndId.id().toString();
    }

    @Override
    protected /* synthetic */ String getKeyForUser(Object object) {
        return this.getKeyForUser((NameAndId)((Object)object));
    }
}

