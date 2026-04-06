/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  ca.weblite.objc.Client
 *  ca.weblite.objc.NSObject
 *  com.sun.jna.Pointer
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.glfw.GLFWNativeCocoa
 */
package com.mojang.blaze3d.platform;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import com.mojang.blaze3d.platform.Window;
import com.sun.jna.Pointer;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.packs.resources.IoSupplier;
import org.lwjgl.glfw.GLFWNativeCocoa;

@Environment(value=EnvType.CLIENT)
public class MacosUtil {
    public static final boolean IS_MACOS = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac");
    private static final int NS_RESIZABLE_WINDOW_MASK = 8;
    private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

    public static void exitNativeFullscreen(Window window) {
        MacosUtil.getNsWindow(window).filter(MacosUtil::isInNativeFullscreen).ifPresent(MacosUtil::toggleNativeFullscreen);
    }

    public static void clearResizableBit(Window window) {
        MacosUtil.getNsWindow(window).ifPresent(nSObject -> {
            long l = MacosUtil.getStyleMask(nSObject);
            nSObject.send("setStyleMask:", new Object[]{l & 0xFFFFFFFFFFFFFFF7L});
        });
    }

    private static Optional<NSObject> getNsWindow(Window window) {
        long l = GLFWNativeCocoa.glfwGetCocoaWindow((long)window.handle());
        if (l != 0L) {
            return Optional.of(new NSObject(new Pointer(l)));
        }
        return Optional.empty();
    }

    private static boolean isInNativeFullscreen(NSObject nSObject) {
        return (MacosUtil.getStyleMask(nSObject) & 0x4000L) != 0L;
    }

    private static long getStyleMask(NSObject nSObject) {
        return (Long)nSObject.sendRaw("styleMask", new Object[0]);
    }

    private static void toggleNativeFullscreen(NSObject nSObject) {
        nSObject.send("toggleFullScreen:", new Object[]{Pointer.NULL});
    }

    public static void loadIcon(IoSupplier<InputStream> ioSupplier) throws IOException {
        try (InputStream inputStream = ioSupplier.get();){
            String string = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
            Client client = Client.getInstance();
            Object object = client.sendProxy("NSData", "alloc", new Object[0]).send("initWithBase64Encoding:", new Object[]{string});
            Object object2 = client.sendProxy("NSImage", "alloc", new Object[0]).send("initWithData:", new Object[]{object});
            client.sendProxy("NSApplication", "sharedApplication", new Object[0]).send("setApplicationIconImage:", new Object[]{object2});
        }
    }
}

