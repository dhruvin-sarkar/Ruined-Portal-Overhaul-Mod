/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  io.netty.buffer.ByteBuf
 *  org.apache.commons.lang3.function.TriFunction
 *  org.slf4j.Logger
 */
package net.minecraft.world.waypoints;

import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.Waypoint;
import org.apache.commons.lang3.function.TriFunction;
import org.slf4j.Logger;

public abstract class TrackedWaypoint
implements Waypoint {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final StreamCodec<ByteBuf, TrackedWaypoint> STREAM_CODEC = StreamCodec.ofMember(TrackedWaypoint::write, TrackedWaypoint::read);
    protected final Either<UUID, String> identifier;
    private final Waypoint.Icon icon;
    private final Type type;

    TrackedWaypoint(Either<UUID, String> either, Waypoint.Icon icon, Type type) {
        this.identifier = either;
        this.icon = icon;
        this.type = type;
    }

    public Either<UUID, String> id() {
        return this.identifier;
    }

    public abstract void update(TrackedWaypoint var1);

    public void write(ByteBuf byteBuf) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
        friendlyByteBuf.writeEither(this.identifier, UUIDUtil.STREAM_CODEC, FriendlyByteBuf::writeUtf);
        Waypoint.Icon.STREAM_CODEC.encode(friendlyByteBuf, this.icon);
        friendlyByteBuf.writeEnum(this.type);
        this.writeContents(byteBuf);
    }

    public abstract void writeContents(ByteBuf var1);

    private static TrackedWaypoint read(ByteBuf byteBuf) {
        FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
        Either<UUID, String> either = friendlyByteBuf.readEither(UUIDUtil.STREAM_CODEC, FriendlyByteBuf::readUtf);
        Waypoint.Icon icon = (Waypoint.Icon)Waypoint.Icon.STREAM_CODEC.decode(friendlyByteBuf);
        Type type = friendlyByteBuf.readEnum(Type.class);
        return (TrackedWaypoint)type.constructor.apply(either, (Object)icon, (Object)friendlyByteBuf);
    }

    public static TrackedWaypoint setPosition(UUID uUID, Waypoint.Icon icon, Vec3i vec3i) {
        return new Vec3iWaypoint(uUID, icon, vec3i);
    }

    public static TrackedWaypoint setChunk(UUID uUID, Waypoint.Icon icon, ChunkPos chunkPos) {
        return new ChunkWaypoint(uUID, icon, chunkPos);
    }

    public static TrackedWaypoint setAzimuth(UUID uUID, Waypoint.Icon icon, float f) {
        return new AzimuthWaypoint(uUID, icon, f);
    }

    public static TrackedWaypoint empty(UUID uUID) {
        return new EmptyWaypoint(uUID);
    }

    public abstract double yawAngleToCamera(Level var1, Camera var2, PartialTickSupplier var3);

    public abstract PitchDirection pitchDirectionToCamera(Level var1, Projector var2, PartialTickSupplier var3);

    public abstract double distanceSquared(Entity var1);

    public Waypoint.Icon icon() {
        return this.icon;
    }

    static enum Type {
        EMPTY((TriFunction<Either<UUID, String>, Waypoint.Icon, FriendlyByteBuf, TrackedWaypoint>)((TriFunction)EmptyWaypoint::new)),
        VEC3I((TriFunction<Either<UUID, String>, Waypoint.Icon, FriendlyByteBuf, TrackedWaypoint>)((TriFunction)Vec3iWaypoint::new)),
        CHUNK((TriFunction<Either<UUID, String>, Waypoint.Icon, FriendlyByteBuf, TrackedWaypoint>)((TriFunction)ChunkWaypoint::new)),
        AZIMUTH((TriFunction<Either<UUID, String>, Waypoint.Icon, FriendlyByteBuf, TrackedWaypoint>)((TriFunction)AzimuthWaypoint::new));

        final TriFunction<Either<UUID, String>, Waypoint.Icon, FriendlyByteBuf, TrackedWaypoint> constructor;

        private Type(TriFunction<Either<UUID, String>, Waypoint.Icon, FriendlyByteBuf, TrackedWaypoint> triFunction) {
            this.constructor = triFunction;
        }
    }

    static class Vec3iWaypoint
    extends TrackedWaypoint {
        private Vec3i vector;

        public Vec3iWaypoint(UUID uUID, Waypoint.Icon icon, Vec3i vec3i) {
            super((Either<UUID, String>)Either.left((Object)uUID), icon, Type.VEC3I);
            this.vector = vec3i;
        }

        public Vec3iWaypoint(Either<UUID, String> either, Waypoint.Icon icon, FriendlyByteBuf friendlyByteBuf) {
            super(either, icon, Type.VEC3I);
            this.vector = new Vec3i(friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt());
        }

        @Override
        public void update(TrackedWaypoint trackedWaypoint) {
            if (trackedWaypoint instanceof Vec3iWaypoint) {
                Vec3iWaypoint vec3iWaypoint = (Vec3iWaypoint)trackedWaypoint;
                this.vector = vec3iWaypoint.vector;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", trackedWaypoint.getClass());
            }
        }

        @Override
        public void writeContents(ByteBuf byteBuf) {
            VarInt.write(byteBuf, this.vector.getX());
            VarInt.write(byteBuf, this.vector.getY());
            VarInt.write(byteBuf, this.vector.getZ());
        }

        private Vec3 position(Level level, PartialTickSupplier partialTickSupplier) {
            return this.identifier.left().map(level::getEntity).map(entity -> {
                if (entity.blockPosition().distManhattan(this.vector) > 3) {
                    return null;
                }
                return entity.getEyePosition(partialTickSupplier.apply((Entity)entity));
            }).orElseGet(() -> Vec3.atCenterOf(this.vector));
        }

        @Override
        public double yawAngleToCamera(Level level, Camera camera, PartialTickSupplier partialTickSupplier) {
            Vec3 vec3 = camera.position().subtract(this.position(level, partialTickSupplier)).rotateClockwise90();
            float f = (float)Mth.atan2(vec3.z(), vec3.x()) * 57.295776f;
            return Mth.degreesDifference(camera.yaw(), f);
        }

        @Override
        public PitchDirection pitchDirectionToCamera(Level level, Projector projector, PartialTickSupplier partialTickSupplier) {
            double d;
            Vec3 vec3 = projector.projectPointToScreen(this.position(level, partialTickSupplier));
            boolean bl = vec3.z > 1.0;
            double d2 = d = bl ? -vec3.y : vec3.y;
            if (d < -1.0) {
                return PitchDirection.DOWN;
            }
            if (d > 1.0) {
                return PitchDirection.UP;
            }
            if (bl) {
                if (vec3.y > 0.0) {
                    return PitchDirection.UP;
                }
                if (vec3.y < 0.0) {
                    return PitchDirection.DOWN;
                }
            }
            return PitchDirection.NONE;
        }

        @Override
        public double distanceSquared(Entity entity) {
            return entity.distanceToSqr(Vec3.atCenterOf(this.vector));
        }
    }

    static class ChunkWaypoint
    extends TrackedWaypoint {
        private ChunkPos chunkPos;

        public ChunkWaypoint(UUID uUID, Waypoint.Icon icon, ChunkPos chunkPos) {
            super((Either<UUID, String>)Either.left((Object)uUID), icon, Type.CHUNK);
            this.chunkPos = chunkPos;
        }

        public ChunkWaypoint(Either<UUID, String> either, Waypoint.Icon icon, FriendlyByteBuf friendlyByteBuf) {
            super(either, icon, Type.CHUNK);
            this.chunkPos = new ChunkPos(friendlyByteBuf.readVarInt(), friendlyByteBuf.readVarInt());
        }

        @Override
        public void update(TrackedWaypoint trackedWaypoint) {
            if (trackedWaypoint instanceof ChunkWaypoint) {
                ChunkWaypoint chunkWaypoint = (ChunkWaypoint)trackedWaypoint;
                this.chunkPos = chunkWaypoint.chunkPos;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", trackedWaypoint.getClass());
            }
        }

        @Override
        public void writeContents(ByteBuf byteBuf) {
            VarInt.write(byteBuf, this.chunkPos.x);
            VarInt.write(byteBuf, this.chunkPos.z);
        }

        private Vec3 position(double d) {
            return Vec3.atCenterOf(this.chunkPos.getMiddleBlockPosition((int)d));
        }

        @Override
        public double yawAngleToCamera(Level level, Camera camera, PartialTickSupplier partialTickSupplier) {
            Vec3 vec3 = camera.position();
            Vec3 vec32 = vec3.subtract(this.position(vec3.y())).rotateClockwise90();
            float f = (float)Mth.atan2(vec32.z(), vec32.x()) * 57.295776f;
            return Mth.degreesDifference(camera.yaw(), f);
        }

        @Override
        public PitchDirection pitchDirectionToCamera(Level level, Projector projector, PartialTickSupplier partialTickSupplier) {
            double d = projector.projectHorizonToScreen();
            if (d < -1.0) {
                return PitchDirection.DOWN;
            }
            if (d > 1.0) {
                return PitchDirection.UP;
            }
            return PitchDirection.NONE;
        }

        @Override
        public double distanceSquared(Entity entity) {
            return entity.distanceToSqr(Vec3.atCenterOf(this.chunkPos.getMiddleBlockPosition(entity.getBlockY())));
        }
    }

    static class AzimuthWaypoint
    extends TrackedWaypoint {
        private float angle;

        public AzimuthWaypoint(UUID uUID, Waypoint.Icon icon, float f) {
            super((Either<UUID, String>)Either.left((Object)uUID), icon, Type.AZIMUTH);
            this.angle = f;
        }

        public AzimuthWaypoint(Either<UUID, String> either, Waypoint.Icon icon, FriendlyByteBuf friendlyByteBuf) {
            super(either, icon, Type.AZIMUTH);
            this.angle = friendlyByteBuf.readFloat();
        }

        @Override
        public void update(TrackedWaypoint trackedWaypoint) {
            if (trackedWaypoint instanceof AzimuthWaypoint) {
                AzimuthWaypoint azimuthWaypoint = (AzimuthWaypoint)trackedWaypoint;
                this.angle = azimuthWaypoint.angle;
            } else {
                LOGGER.warn("Unsupported Waypoint update operation: {}", trackedWaypoint.getClass());
            }
        }

        @Override
        public void writeContents(ByteBuf byteBuf) {
            byteBuf.writeFloat(this.angle);
        }

        @Override
        public double yawAngleToCamera(Level level, Camera camera, PartialTickSupplier partialTickSupplier) {
            return Mth.degreesDifference(camera.yaw(), this.angle * 57.295776f);
        }

        @Override
        public PitchDirection pitchDirectionToCamera(Level level, Projector projector, PartialTickSupplier partialTickSupplier) {
            double d = projector.projectHorizonToScreen();
            if (d < -1.0) {
                return PitchDirection.DOWN;
            }
            if (d > 1.0) {
                return PitchDirection.UP;
            }
            return PitchDirection.NONE;
        }

        @Override
        public double distanceSquared(Entity entity) {
            return Double.POSITIVE_INFINITY;
        }
    }

    static class EmptyWaypoint
    extends TrackedWaypoint {
        private EmptyWaypoint(Either<UUID, String> either, Waypoint.Icon icon, FriendlyByteBuf friendlyByteBuf) {
            super(either, icon, Type.EMPTY);
        }

        EmptyWaypoint(UUID uUID) {
            super((Either<UUID, String>)Either.left((Object)uUID), Waypoint.Icon.NULL, Type.EMPTY);
        }

        @Override
        public void update(TrackedWaypoint trackedWaypoint) {
        }

        @Override
        public void writeContents(ByteBuf byteBuf) {
        }

        @Override
        public double yawAngleToCamera(Level level, Camera camera, PartialTickSupplier partialTickSupplier) {
            return Double.NaN;
        }

        @Override
        public PitchDirection pitchDirectionToCamera(Level level, Projector projector, PartialTickSupplier partialTickSupplier) {
            return PitchDirection.NONE;
        }

        @Override
        public double distanceSquared(Entity entity) {
            return Double.POSITIVE_INFINITY;
        }
    }

    public static interface Camera {
        public float yaw();

        public Vec3 position();
    }

    public static interface Projector {
        public Vec3 projectPointToScreen(Vec3 var1);

        public double projectHorizonToScreen();
    }

    public static enum PitchDirection {
        NONE,
        UP,
        DOWN;

    }
}

