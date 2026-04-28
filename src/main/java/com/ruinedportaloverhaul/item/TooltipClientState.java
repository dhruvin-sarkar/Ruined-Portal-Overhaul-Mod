package com.ruinedportaloverhaul.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class TooltipClientState {
    private static final long MISSING_TIME = Long.MIN_VALUE;
    private static final boolean CLIENT_ENVIRONMENT = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;

    private TooltipClientState() {
    }

    public static boolean isShiftDown() {
        if (!CLIENT_ENVIRONMENT) {
            return false;
        }

        try {
            Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
            return Boolean.TRUE.equals(screenClass.getMethod("hasShiftDown").invoke(null));
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    public static long currentClientGameTime() {
        if (!CLIENT_ENVIRONMENT) {
            return MISSING_TIME;
        }

        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            Object level = minecraftClass.getField("level").get(minecraft);
            if (level == null) {
                return MISSING_TIME;
            }

            return ((Number) level.getClass().getMethod("getGameTime").invoke(level)).longValue();
        } catch (ReflectiveOperationException ignored) {
            return MISSING_TIME;
        }
    }

    public static int currentCorruptedArmorPieces() {
        if (!CLIENT_ENVIRONMENT) {
            return 0;
        }

        try {
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            Object minecraft = minecraftClass.getMethod("getInstance").invoke(null);
            Object rawPlayer = minecraftClass.getField("player").get(minecraft);
            if (!(rawPlayer instanceof Player player)) {
                return 0;
            }

            int count = 0;
            for (EquipmentSlot slot : CorruptedNetheriteArmorItem.ARMOR_SLOTS) {
                ItemStack stack = player.getItemBySlot(slot);
                if (CorruptedNetheriteArmorItem.isCorruptedNetherite(stack)) {
                    count++;
                }
            }
            return count;
        } catch (ReflectiveOperationException ignored) {
            return 0;
        }
    }
}
