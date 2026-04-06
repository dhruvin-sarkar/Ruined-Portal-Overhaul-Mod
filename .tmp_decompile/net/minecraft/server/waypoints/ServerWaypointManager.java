/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashBasedTable
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.google.common.collect.Table
 *  com.google.common.collect.Tables
 */
package net.minecraft.server.waypoints;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.waypoints.Waypoint;
import net.minecraft.world.waypoints.WaypointManager;
import net.minecraft.world.waypoints.WaypointTransmitter;

public class ServerWaypointManager
implements WaypointManager<WaypointTransmitter> {
    private final Set<WaypointTransmitter> waypoints = new HashSet<WaypointTransmitter>();
    private final Set<ServerPlayer> players = new HashSet<ServerPlayer>();
    private final Table<ServerPlayer, WaypointTransmitter, WaypointTransmitter.Connection> connections = HashBasedTable.create();

    @Override
    public void trackWaypoint(WaypointTransmitter waypointTransmitter) {
        this.waypoints.add(waypointTransmitter);
        for (ServerPlayer serverPlayer : this.players) {
            this.createConnection(serverPlayer, waypointTransmitter);
        }
    }

    @Override
    public void updateWaypoint(WaypointTransmitter waypointTransmitter) {
        if (!this.waypoints.contains(waypointTransmitter)) {
            return;
        }
        Map map = Tables.transpose(this.connections).row((Object)waypointTransmitter);
        Sets.SetView setView = Sets.difference(this.players, map.keySet());
        for (Map.Entry entry : ImmutableSet.copyOf(map.entrySet())) {
            this.updateConnection((ServerPlayer)entry.getKey(), waypointTransmitter, (WaypointTransmitter.Connection)entry.getValue());
        }
        for (ServerPlayer serverPlayer : setView) {
            this.createConnection(serverPlayer, waypointTransmitter);
        }
    }

    @Override
    public void untrackWaypoint(WaypointTransmitter waypointTransmitter) {
        this.connections.column((Object)waypointTransmitter).forEach((serverPlayer, connection) -> connection.disconnect());
        Tables.transpose(this.connections).row((Object)waypointTransmitter).clear();
        this.waypoints.remove(waypointTransmitter);
    }

    public void addPlayer(ServerPlayer serverPlayer) {
        this.players.add(serverPlayer);
        for (WaypointTransmitter waypointTransmitter : this.waypoints) {
            this.createConnection(serverPlayer, waypointTransmitter);
        }
        if (serverPlayer.isTransmittingWaypoint()) {
            this.trackWaypoint(serverPlayer);
        }
    }

    public void updatePlayer(ServerPlayer serverPlayer) {
        Map map = this.connections.row((Object)serverPlayer);
        Sets.SetView setView = Sets.difference(this.waypoints, map.keySet());
        for (Map.Entry entry : ImmutableSet.copyOf(map.entrySet())) {
            this.updateConnection(serverPlayer, (WaypointTransmitter)entry.getKey(), (WaypointTransmitter.Connection)entry.getValue());
        }
        for (WaypointTransmitter waypointTransmitter : setView) {
            this.createConnection(serverPlayer, waypointTransmitter);
        }
    }

    public void removePlayer(ServerPlayer serverPlayer) {
        this.connections.row((Object)serverPlayer).values().removeIf(connection -> {
            connection.disconnect();
            return true;
        });
        this.untrackWaypoint(serverPlayer);
        this.players.remove(serverPlayer);
    }

    public void breakAllConnections() {
        this.connections.values().forEach(WaypointTransmitter.Connection::disconnect);
        this.connections.clear();
    }

    public void remakeConnections(WaypointTransmitter waypointTransmitter) {
        for (ServerPlayer serverPlayer : this.players) {
            this.createConnection(serverPlayer, waypointTransmitter);
        }
    }

    public Set<WaypointTransmitter> transmitters() {
        return this.waypoints;
    }

    private static boolean isLocatorBarEnabledFor(ServerPlayer serverPlayer) {
        return serverPlayer.level().getGameRules().get(GameRules.LOCATOR_BAR);
    }

    private void createConnection(ServerPlayer serverPlayer, WaypointTransmitter waypointTransmitter) {
        if (serverPlayer == waypointTransmitter) {
            return;
        }
        if (!ServerWaypointManager.isLocatorBarEnabledFor(serverPlayer)) {
            return;
        }
        waypointTransmitter.makeWaypointConnectionWith(serverPlayer).ifPresentOrElse(connection -> {
            this.connections.put((Object)serverPlayer, (Object)waypointTransmitter, connection);
            connection.connect();
        }, () -> {
            WaypointTransmitter.Connection connection = (WaypointTransmitter.Connection)this.connections.remove((Object)serverPlayer, (Object)waypointTransmitter);
            if (connection != null) {
                connection.disconnect();
            }
        });
    }

    private void updateConnection(ServerPlayer serverPlayer, WaypointTransmitter waypointTransmitter, WaypointTransmitter.Connection connection2) {
        if (serverPlayer == waypointTransmitter) {
            return;
        }
        if (!ServerWaypointManager.isLocatorBarEnabledFor(serverPlayer)) {
            return;
        }
        if (!connection2.isBroken()) {
            connection2.update();
            return;
        }
        waypointTransmitter.makeWaypointConnectionWith(serverPlayer).ifPresentOrElse(connection -> {
            connection.connect();
            this.connections.put((Object)serverPlayer, (Object)waypointTransmitter, connection);
        }, () -> {
            connection2.disconnect();
            this.connections.remove((Object)serverPlayer, (Object)waypointTransmitter);
        });
    }

    @Override
    public /* synthetic */ void untrackWaypoint(Waypoint waypoint) {
        this.untrackWaypoint((WaypointTransmitter)waypoint);
    }

    @Override
    public /* synthetic */ void trackWaypoint(Waypoint waypoint) {
        this.trackWaypoint((WaypointTransmitter)waypoint);
    }
}

