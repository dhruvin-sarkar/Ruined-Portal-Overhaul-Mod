/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.waypoints.TrackedWaypoint;
import net.minecraft.world.waypoints.TrackedWaypointManager;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointManager;

public record ClientboundTrackedWaypointPacket(Operation operation, TrackedWaypoint waypoint) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundTrackedWaypointPacket> STREAM_CODEC = StreamCodec.composite(Operation.STREAM_CODEC, ClientboundTrackedWaypointPacket::operation, TrackedWaypoint.STREAM_CODEC, ClientboundTrackedWaypointPacket::waypoint, ClientboundTrackedWaypointPacket::new);

    public static ClientboundTrackedWaypointPacket removeWaypoint(UUID uUID) {
        return new ClientboundTrackedWaypointPacket(Operation.UNTRACK, TrackedWaypoint.empty(uUID));
    }

    public static ClientboundTrackedWaypointPacket addWaypointPosition(UUID uUID, Waypoint.Icon icon, Vec3i vec3i) {
        return new ClientboundTrackedWaypointPacket(Operation.TRACK, TrackedWaypoint.setPosition(uUID, icon, vec3i));
    }

    public static ClientboundTrackedWaypointPacket updateWaypointPosition(UUID uUID, Waypoint.Icon icon, Vec3i vec3i) {
        return new ClientboundTrackedWaypointPacket(Operation.UPDATE, TrackedWaypoint.setPosition(uUID, icon, vec3i));
    }

    public static ClientboundTrackedWaypointPacket addWaypointChunk(UUID uUID, Waypoint.Icon icon, ChunkPos chunkPos) {
        return new ClientboundTrackedWaypointPacket(Operation.TRACK, TrackedWaypoint.setChunk(uUID, icon, chunkPos));
    }

    public static ClientboundTrackedWaypointPacket updateWaypointChunk(UUID uUID, Waypoint.Icon icon, ChunkPos chunkPos) {
        return new ClientboundTrackedWaypointPacket(Operation.UPDATE, TrackedWaypoint.setChunk(uUID, icon, chunkPos));
    }

    public static ClientboundTrackedWaypointPacket addWaypointAzimuth(UUID uUID, Waypoint.Icon icon, float f) {
        return new ClientboundTrackedWaypointPacket(Operation.TRACK, TrackedWaypoint.setAzimuth(uUID, icon, f));
    }

    public static ClientboundTrackedWaypointPacket updateWaypointAzimuth(UUID uUID, Waypoint.Icon icon, float f) {
        return new ClientboundTrackedWaypointPacket(Operation.UPDATE, TrackedWaypoint.setAzimuth(uUID, icon, f));
    }

    @Override
    public PacketType<ClientboundTrackedWaypointPacket> type() {
        return GamePacketTypes.CLIENTBOUND_WAYPOINT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleWaypoint(this);
    }

    public void apply(TrackedWaypointManager trackedWaypointManager) {
        this.operation.action.accept(trackedWaypointManager, this.waypoint);
    }

    static enum Operation {
        TRACK(WaypointManager::trackWaypoint),
        UNTRACK(WaypointManager::untrackWaypoint),
        UPDATE(WaypointManager::updateWaypoint);

        final BiConsumer<TrackedWaypointManager, TrackedWaypoint> action;
        public static final IntFunction<Operation> BY_ID;
        public static final StreamCodec<ByteBuf, Operation> STREAM_CODEC;

        private Operation(BiConsumer<TrackedWaypointManager, TrackedWaypoint> biConsumer) {
            this.action = biConsumer;
        }

        static {
            BY_ID = ByIdMap.continuous(Enum::ordinal, Operation.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
        }
    }
}

