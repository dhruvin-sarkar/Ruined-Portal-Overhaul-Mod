/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.minecraft.client.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import net.minecraft.network.protocol.ping.ServerboundPingRequestPacket;
import net.minecraft.util.Util;
import net.minecraft.util.debugchart.LocalSampleLogger;

@Environment(value=EnvType.CLIENT)
public class PingDebugMonitor {
    private final ClientPacketListener connection;
    private final LocalSampleLogger delayTimer;

    public PingDebugMonitor(ClientPacketListener clientPacketListener, LocalSampleLogger localSampleLogger) {
        this.connection = clientPacketListener;
        this.delayTimer = localSampleLogger;
    }

    public void tick() {
        this.connection.send(new ServerboundPingRequestPacket(Util.getMillis()));
    }

    public void onPongReceived(ClientboundPongResponsePacket clientboundPongResponsePacket) {
        this.delayTimer.logSample(Util.getMillis() - clientboundPongResponsePacket.time());
    }
}

