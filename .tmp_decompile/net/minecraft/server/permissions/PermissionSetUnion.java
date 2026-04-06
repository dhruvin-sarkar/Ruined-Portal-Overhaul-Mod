/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.objects.ReferenceArraySet
 *  it.unimi.dsi.fastutil.objects.ReferenceSet
 */
package net.minecraft.server.permissions;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionSet;

public class PermissionSetUnion
implements PermissionSet {
    private final ReferenceSet<PermissionSet> permissions = new ReferenceArraySet();

    PermissionSetUnion(PermissionSet permissionSet, PermissionSet permissionSet2) {
        this.permissions.add((Object)permissionSet);
        this.permissions.add((Object)permissionSet2);
        this.ensureNoUnionsWithinUnions();
    }

    private PermissionSetUnion(ReferenceSet<PermissionSet> referenceSet, PermissionSet permissionSet) {
        this.permissions.addAll(referenceSet);
        this.permissions.add((Object)permissionSet);
        this.ensureNoUnionsWithinUnions();
    }

    private PermissionSetUnion(ReferenceSet<PermissionSet> referenceSet, ReferenceSet<PermissionSet> referenceSet2) {
        this.permissions.addAll(referenceSet);
        this.permissions.addAll(referenceSet2);
        this.ensureNoUnionsWithinUnions();
    }

    @Override
    public boolean hasPermission(Permission permission) {
        for (PermissionSet permissionSet : this.permissions) {
            if (!permissionSet.hasPermission(permission)) continue;
            return true;
        }
        return false;
    }

    @Override
    public PermissionSet union(PermissionSet permissionSet) {
        if (permissionSet instanceof PermissionSetUnion) {
            PermissionSetUnion permissionSetUnion = (PermissionSetUnion)permissionSet;
            return new PermissionSetUnion(this.permissions, permissionSetUnion.permissions);
        }
        return new PermissionSetUnion(this.permissions, permissionSet);
    }

    @VisibleForTesting
    public ReferenceSet<PermissionSet> getPermissions() {
        return new ReferenceArraySet(this.permissions);
    }

    private void ensureNoUnionsWithinUnions() {
        for (PermissionSet permissionSet : this.permissions) {
            if (!(permissionSet instanceof PermissionSetUnion)) continue;
            throw new IllegalArgumentException("Cannot have PermissionSetUnion within another PermissionSetUnion");
        }
    }
}

