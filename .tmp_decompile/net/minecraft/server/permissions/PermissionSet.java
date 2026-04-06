/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.permissions;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSetUnion;

public interface PermissionSet {
    public static final PermissionSet NO_PERMISSIONS = permission -> false;
    public static final PermissionSet ALL_PERMISSIONS = permission -> true;

    public boolean hasPermission(Permission var1);

    default public PermissionSet union(PermissionSet permissionSet) {
        if (permissionSet instanceof PermissionSetUnion) {
            return permissionSet.union(this);
        }
        return new PermissionSetUnion(this, permissionSet);
    }
}

