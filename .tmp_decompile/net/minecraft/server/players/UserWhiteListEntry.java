/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;

public class UserWhiteListEntry
extends StoredUserEntry<NameAndId> {
    public UserWhiteListEntry(NameAndId nameAndId) {
        super(nameAndId);
    }

    public UserWhiteListEntry(JsonObject jsonObject) {
        super(NameAndId.fromJson(jsonObject));
    }

    @Override
    protected void serialize(JsonObject jsonObject) {
        if (this.getUser() == null) {
            return;
        }
        ((NameAndId)((Object)this.getUser())).appendTo(jsonObject);
    }
}

