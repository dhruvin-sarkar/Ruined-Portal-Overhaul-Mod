package com.ruinedportaloverhaul.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.ruinedportaloverhaul.network.NetherFireballPayload;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

public final class NetherFireballKeybinds {
    private static final KeyMapping USE_NETHER_FIREBALL = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        "key.ruined_portal_overhaul.use_nether_fireball",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_G,
        KeyMapping.Category.MISC
    ));

    private NetherFireballKeybinds() {
    }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (USE_NETHER_FIREBALL.consumeClick()) {
                if (client.player != null && ClientPlayNetworking.canSend(NetherFireballPayload.TYPE)) {
                    Vec3 look = client.player.getLookAngle();
                    ClientPlayNetworking.send(new NetherFireballPayload(look.x, look.y, look.z));
                }
            }
        });
    }
}
