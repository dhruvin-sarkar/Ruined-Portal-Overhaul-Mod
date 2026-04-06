/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package net.minecraft.server.players;

import com.google.gson.JsonObject;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.StoredUserEntry;

public class ServerOpListEntry
extends StoredUserEntry<NameAndId> {
    private final LevelBasedPermissionSet permissions;
    private final boolean bypassesPlayerLimit;

    public ServerOpListEntry(NameAndId nameAndId, LevelBasedPermissionSet levelBasedPermissionSet, boolean bl) {
        super(nameAndId);
        this.permissions = levelBasedPermissionSet;
        this.bypassesPlayerLimit = bl;
    }

    public ServerOpListEntry(JsonObject jsonObject) {
        super(NameAndId.fromJson(jsonObject));
        PermissionLevel permissionLevel = jsonObject.has("level") ? PermissionLevel.byId(jsonObject.get("level").getAsInt()) : PermissionLevel.ALL;
        this.permissions = LevelBasedPermissionSet.forLevel(permissionLevel);
        this.bypassesPlayerLimit = jsonObject.has("bypassesPlayerLimit") && jsonObject.get("bypassesPlayerLimit").getAsBoolean();
    }

    public LevelBasedPermissionSet permissions() {
        return this.permissions;
    }

    public boolean getBypassesPlayerLimit() {
        return this.bypassesPlayerLimit;
    }

    @Override
    protected void serialize(JsonObject jsonObject) {
        if (this.getUser() == null) {
            return;
        }
        ((NameAndId)((Object)this.getUser())).appendTo(jsonObject);
        jsonObject.addProperty("level", (Number)this.permissions.level().id());
        jsonObject.addProperty("bypassesPlayerLimit", Boolean.valueOf(this.bypassesPlayerLimit));
    }
}

