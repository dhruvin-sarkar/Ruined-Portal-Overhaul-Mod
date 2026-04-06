/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityAttachments {
    private final Map<EntityAttachment, List<Vec3>> attachments;

    EntityAttachments(Map<EntityAttachment, List<Vec3>> map) {
        this.attachments = map;
    }

    public static EntityAttachments createDefault(float f, float g) {
        return EntityAttachments.builder().build(f, g);
    }

    public static Builder builder() {
        return new Builder();
    }

    public EntityAttachments scale(float f, float g, float h) {
        return new EntityAttachments(Util.makeEnumMap(EntityAttachment.class, entityAttachment -> {
            ArrayList<Vec3> list = new ArrayList<Vec3>();
            for (Vec3 vec3 : this.attachments.get(entityAttachment)) {
                list.add(vec3.multiply(f, g, h));
            }
            return list;
        }));
    }

    public @Nullable Vec3 getNullable(EntityAttachment entityAttachment, int i, float f) {
        List<Vec3> list = this.attachments.get((Object)entityAttachment);
        if (i < 0 || i >= list.size()) {
            return null;
        }
        return EntityAttachments.transformPoint(list.get(i), f);
    }

    public Vec3 get(EntityAttachment entityAttachment, int i, float f) {
        Vec3 vec3 = this.getNullable(entityAttachment, i, f);
        if (vec3 == null) {
            throw new IllegalStateException("Had no attachment point of type: " + String.valueOf((Object)entityAttachment) + " for index: " + i);
        }
        return vec3;
    }

    public Vec3 getAverage(EntityAttachment entityAttachment) {
        List<Vec3> list = this.attachments.get((Object)entityAttachment);
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("No attachment points of type: PASSENGER");
        }
        Vec3 vec3 = Vec3.ZERO;
        for (Vec3 vec32 : list) {
            vec3 = vec3.add(vec32);
        }
        return vec3.scale(1.0f / (float)list.size());
    }

    public Vec3 getClamped(EntityAttachment entityAttachment, int i, float f) {
        List<Vec3> list = this.attachments.get((Object)entityAttachment);
        if (list.isEmpty()) {
            throw new IllegalStateException("Had no attachment points of type: " + String.valueOf((Object)entityAttachment));
        }
        Vec3 vec3 = list.get(Mth.clamp(i, 0, list.size() - 1));
        return EntityAttachments.transformPoint(vec3, f);
    }

    private static Vec3 transformPoint(Vec3 vec3, float f) {
        return vec3.yRot(-f * ((float)Math.PI / 180));
    }

    public static class Builder {
        private final Map<EntityAttachment, List<Vec3>> attachments = new EnumMap<EntityAttachment, List<Vec3>>(EntityAttachment.class);

        Builder() {
        }

        public Builder attach(EntityAttachment entityAttachment, float f, float g, float h) {
            return this.attach(entityAttachment, new Vec3(f, g, h));
        }

        public Builder attach(EntityAttachment entityAttachment2, Vec3 vec3) {
            this.attachments.computeIfAbsent(entityAttachment2, entityAttachment -> new ArrayList(1)).add(vec3);
            return this;
        }

        public EntityAttachments build(float f, float g) {
            Map<EntityAttachment, List<Vec3>> map = Util.makeEnumMap(EntityAttachment.class, entityAttachment -> {
                List<Vec3> list = this.attachments.get(entityAttachment);
                return list == null ? entityAttachment.createFallbackPoints(f, g) : List.copyOf(list);
            });
            return new EntityAttachments(map);
        }
    }
}

