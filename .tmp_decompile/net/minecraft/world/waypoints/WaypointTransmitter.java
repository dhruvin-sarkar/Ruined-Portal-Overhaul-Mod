/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.waypoints;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundTrackedWaypointPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.waypoints.Waypoint;

public interface WaypointTransmitter
extends Waypoint {
    public static final int REALLY_FAR_DISTANCE = 332;

    public boolean isTransmittingWaypoint();

    public Optional<Connection> makeWaypointConnectionWith(ServerPlayer var1);

    public Waypoint.Icon waypointIcon();

    public static boolean doesSourceIgnoreReceiver(LivingEntity livingEntity, ServerPlayer serverPlayer) {
        if (serverPlayer.isSpectator()) {
            return false;
        }
        if (livingEntity.isSpectator() || livingEntity.hasIndirectPassenger(serverPlayer)) {
            return true;
        }
        double d = Math.min(livingEntity.getAttributeValue(Attributes.WAYPOINT_TRANSMIT_RANGE), serverPlayer.getAttributeValue(Attributes.WAYPOINT_RECEIVE_RANGE));
        return (double)livingEntity.distanceTo(serverPlayer) >= d;
    }

    public static boolean isChunkVisible(ChunkPos chunkPos, ServerPlayer serverPlayer) {
        return serverPlayer.getChunkTrackingView().isInViewDistance(chunkPos.x, chunkPos.z);
    }

    public static boolean isReallyFar(LivingEntity livingEntity, ServerPlayer serverPlayer) {
        return livingEntity.distanceTo(serverPlayer) > 332.0f;
    }

    public static class EntityAzimuthConnection
    implements Connection {
        private final LivingEntity source;
        private final Waypoint.Icon icon;
        private final ServerPlayer receiver;
        private float lastAngle;

        public EntityAzimuthConnection(LivingEntity livingEntity, Waypoint.Icon icon, ServerPlayer serverPlayer) {
            this.source = livingEntity;
            this.icon = icon;
            this.receiver = serverPlayer;
            Vec3 vec3 = serverPlayer.position().subtract(livingEntity.position()).rotateClockwise90();
            this.lastAngle = (float)Mth.atan2(vec3.z(), vec3.x());
        }

        @Override
        public boolean isBroken() {
            return WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver) || WaypointTransmitter.isChunkVisible(this.source.chunkPosition(), this.receiver) || !WaypointTransmitter.isReallyFar(this.source, this.receiver);
        }

        @Override
        public void connect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointAzimuth(this.source.getUUID(), this.icon, this.lastAngle));
        }

        @Override
        public void disconnect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
        }

        @Override
        public void update() {
            Vec3 vec3 = this.receiver.position().subtract(this.source.position()).rotateClockwise90();
            float f = (float)Mth.atan2(vec3.z(), vec3.x());
            if (Mth.abs(f - this.lastAngle) > (float)Math.PI / 360) {
                this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointAzimuth(this.source.getUUID(), this.icon, f));
                this.lastAngle = f;
            }
        }
    }

    public static class EntityChunkConnection
    implements ChunkConnection {
        private final LivingEntity source;
        private final Waypoint.Icon icon;
        private final ServerPlayer receiver;
        private ChunkPos lastPosition;

        public EntityChunkConnection(LivingEntity livingEntity, Waypoint.Icon icon, ServerPlayer serverPlayer) {
            this.source = livingEntity;
            this.icon = icon;
            this.receiver = serverPlayer;
            this.lastPosition = livingEntity.chunkPosition();
        }

        @Override
        public int distanceChessboard() {
            return this.lastPosition.getChessboardDistance(this.source.chunkPosition());
        }

        @Override
        public void connect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointChunk(this.source.getUUID(), this.icon, this.lastPosition));
        }

        @Override
        public void disconnect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
        }

        @Override
        public void update() {
            ChunkPos chunkPos = this.source.chunkPosition();
            if (chunkPos.getChessboardDistance(this.lastPosition) > 0) {
                this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointChunk(this.source.getUUID(), this.icon, chunkPos));
                this.lastPosition = chunkPos;
            }
        }

        @Override
        public boolean isBroken() {
            if (ChunkConnection.super.isBroken() || WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver)) {
                return true;
            }
            return WaypointTransmitter.isChunkVisible(this.lastPosition, this.receiver);
        }
    }

    public static interface ChunkConnection
    extends Connection {
        public int distanceChessboard();

        @Override
        default public boolean isBroken() {
            return this.distanceChessboard() > 1;
        }
    }

    public static class EntityBlockConnection
    implements BlockConnection {
        private final LivingEntity source;
        private final Waypoint.Icon icon;
        private final ServerPlayer receiver;
        private BlockPos lastPosition;

        public EntityBlockConnection(LivingEntity livingEntity, Waypoint.Icon icon, ServerPlayer serverPlayer) {
            this.source = livingEntity;
            this.receiver = serverPlayer;
            this.icon = icon;
            this.lastPosition = livingEntity.blockPosition();
        }

        @Override
        public void connect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.addWaypointPosition(this.source.getUUID(), this.icon, this.lastPosition));
        }

        @Override
        public void disconnect() {
            this.receiver.connection.send(ClientboundTrackedWaypointPacket.removeWaypoint(this.source.getUUID()));
        }

        @Override
        public void update() {
            BlockPos blockPos = this.source.blockPosition();
            if (blockPos.distManhattan(this.lastPosition) > 0) {
                this.receiver.connection.send(ClientboundTrackedWaypointPacket.updateWaypointPosition(this.source.getUUID(), this.icon, blockPos));
                this.lastPosition = blockPos;
            }
        }

        @Override
        public int distanceManhattan() {
            return this.lastPosition.distManhattan(this.source.blockPosition());
        }

        @Override
        public boolean isBroken() {
            return BlockConnection.super.isBroken() || WaypointTransmitter.doesSourceIgnoreReceiver(this.source, this.receiver);
        }
    }

    public static interface BlockConnection
    extends Connection {
        public int distanceManhattan();

        @Override
        default public boolean isBroken() {
            return this.distanceManhattan() > 1;
        }
    }

    public static interface Connection {
        public void connect();

        public void disconnect();

        public void update();

        public boolean isBroken();
    }
}

