/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.MatchException
 */
package net.minecraft.server.permissions;

import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;

public interface LevelBasedPermissionSet
extends PermissionSet {
    @Deprecated
    public static final LevelBasedPermissionSet ALL = LevelBasedPermissionSet.create(PermissionLevel.ALL);
    public static final LevelBasedPermissionSet MODERATOR = LevelBasedPermissionSet.create(PermissionLevel.MODERATORS);
    public static final LevelBasedPermissionSet GAMEMASTER = LevelBasedPermissionSet.create(PermissionLevel.GAMEMASTERS);
    public static final LevelBasedPermissionSet ADMIN = LevelBasedPermissionSet.create(PermissionLevel.ADMINS);
    public static final LevelBasedPermissionSet OWNER = LevelBasedPermissionSet.create(PermissionLevel.OWNERS);

    public PermissionLevel level();

    @Override
    default public boolean hasPermission(Permission permission) {
        if (permission instanceof Permission.HasCommandLevel) {
            Permission.HasCommandLevel hasCommandLevel = (Permission.HasCommandLevel)permission;
            return this.level().isEqualOrHigherThan(hasCommandLevel.level());
        }
        if (permission.equals(Permissions.COMMANDS_ENTITY_SELECTORS)) {
            return this.level().isEqualOrHigherThan(PermissionLevel.GAMEMASTERS);
        }
        return false;
    }

    @Override
    default public PermissionSet union(PermissionSet permissionSet) {
        if (permissionSet instanceof LevelBasedPermissionSet) {
            LevelBasedPermissionSet levelBasedPermissionSet = (LevelBasedPermissionSet)permissionSet;
            if (this.level().isEqualOrHigherThan(levelBasedPermissionSet.level())) {
                return levelBasedPermissionSet;
            }
            return this;
        }
        return PermissionSet.super.union(permissionSet);
    }

    public static LevelBasedPermissionSet forLevel(PermissionLevel permissionLevel) {
        return switch (permissionLevel) {
            default -> throw new MatchException(null, null);
            case PermissionLevel.ALL -> ALL;
            case PermissionLevel.MODERATORS -> MODERATOR;
            case PermissionLevel.GAMEMASTERS -> GAMEMASTER;
            case PermissionLevel.ADMINS -> ADMIN;
            case PermissionLevel.OWNERS -> OWNER;
        };
    }

    private static LevelBasedPermissionSet create(final PermissionLevel permissionLevel) {
        return new LevelBasedPermissionSet(){

            @Override
            public PermissionLevel level() {
                return permissionLevel;
            }

            public String toString() {
                return "permission level: " + permissionLevel.name();
            }
        };
    }
}

