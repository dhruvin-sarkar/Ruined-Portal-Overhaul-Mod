/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 */
package net.minecraft.world.entity.player;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;

public class Abilities {
    private static final boolean DEFAULT_INVULNERABLE = false;
    private static final boolean DEFAULY_FLYING = false;
    private static final boolean DEFAULT_MAY_FLY = false;
    private static final boolean DEFAULT_INSTABUILD = false;
    private static final boolean DEFAULT_MAY_BUILD = true;
    private static final float DEFAULT_FLYING_SPEED = 0.05f;
    private static final float DEFAULT_WALKING_SPEED = 0.1f;
    public boolean invulnerable;
    public boolean flying;
    public boolean mayfly;
    public boolean instabuild;
    public boolean mayBuild = true;
    private float flyingSpeed = 0.05f;
    private float walkingSpeed = 0.1f;

    public float getFlyingSpeed() {
        return this.flyingSpeed;
    }

    public void setFlyingSpeed(float f) {
        this.flyingSpeed = f;
    }

    public float getWalkingSpeed() {
        return this.walkingSpeed;
    }

    public void setWalkingSpeed(float f) {
        this.walkingSpeed = f;
    }

    public Packed pack() {
        return new Packed(this.invulnerable, this.flying, this.mayfly, this.instabuild, this.mayBuild, this.flyingSpeed, this.walkingSpeed);
    }

    public void apply(Packed packed) {
        this.invulnerable = packed.invulnerable;
        this.flying = packed.flying;
        this.mayfly = packed.mayFly;
        this.instabuild = packed.instabuild;
        this.mayBuild = packed.mayBuild;
        this.flyingSpeed = packed.flyingSpeed;
        this.walkingSpeed = packed.walkingSpeed;
    }

    public static final class Packed
    extends Record {
        final boolean invulnerable;
        final boolean flying;
        final boolean mayFly;
        final boolean instabuild;
        final boolean mayBuild;
        final float flyingSpeed;
        final float walkingSpeed;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.fieldOf("invulnerable").orElse((Object)false).forGetter(Packed::invulnerable), (App)Codec.BOOL.fieldOf("flying").orElse((Object)false).forGetter(Packed::flying), (App)Codec.BOOL.fieldOf("mayfly").orElse((Object)false).forGetter(Packed::mayFly), (App)Codec.BOOL.fieldOf("instabuild").orElse((Object)false).forGetter(Packed::instabuild), (App)Codec.BOOL.fieldOf("mayBuild").orElse((Object)true).forGetter(Packed::mayBuild), (App)Codec.FLOAT.fieldOf("flySpeed").orElse((Object)Float.valueOf(0.05f)).forGetter(Packed::flyingSpeed), (App)Codec.FLOAT.fieldOf("walkSpeed").orElse((Object)Float.valueOf(0.1f)).forGetter(Packed::walkingSpeed)).apply((Applicative)instance, Packed::new));

        public Packed(boolean bl, boolean bl2, boolean bl3, boolean bl4, boolean bl5, float f, float g) {
            this.invulnerable = bl;
            this.flying = bl2;
            this.mayFly = bl3;
            this.instabuild = bl4;
            this.mayBuild = bl5;
            this.flyingSpeed = f;
            this.walkingSpeed = g;
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "invulnerable;flying;mayFly;instabuild;mayBuild;flyingSpeed;walkingSpeed", "invulnerable", "flying", "mayFly", "instabuild", "mayBuild", "flyingSpeed", "walkingSpeed"}, this, object);
        }

        public boolean invulnerable() {
            return this.invulnerable;
        }

        public boolean flying() {
            return this.flying;
        }

        public boolean mayFly() {
            return this.mayFly;
        }

        public boolean instabuild() {
            return this.instabuild;
        }

        public boolean mayBuild() {
            return this.mayBuild;
        }

        public float flyingSpeed() {
            return this.flyingSpeed;
        }

        public float walkingSpeed() {
            return this.walkingSpeed;
        }
    }
}

