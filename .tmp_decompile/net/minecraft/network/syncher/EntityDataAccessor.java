/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.syncher;

import net.minecraft.network.syncher.EntityDataSerializer;

public record EntityDataAccessor<T>(int id, EntityDataSerializer<T> serializer) {
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || ((Object)((Object)this)).getClass() != object.getClass()) {
            return false;
        }
        EntityDataAccessor entityDataAccessor = (EntityDataAccessor)((Object)object);
        return this.id == entityDataAccessor.id;
    }

    public int hashCode() {
        return this.id;
    }

    public String toString() {
        return "<entity data: " + this.id + ">";
    }
}

