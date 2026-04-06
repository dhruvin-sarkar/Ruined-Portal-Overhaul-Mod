/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWErrorCallback
 *  org.lwjgl.glfw.GLFWErrorCallbackI
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.platform.Window;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.StringDecomposer;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class ClipboardManager {
    public static final int FORMAT_UNAVAILABLE = 65545;
    private final ByteBuffer clipboardScratchBuffer = BufferUtils.createByteBuffer((int)8192);

    public String getClipboard(Window window, GLFWErrorCallbackI gLFWErrorCallbackI) {
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((GLFWErrorCallbackI)gLFWErrorCallbackI);
        String string = GLFW.glfwGetClipboardString((long)window.handle());
        string = string != null ? StringDecomposer.filterBrokenSurrogates(string) : "";
        GLFWErrorCallback gLFWErrorCallback2 = GLFW.glfwSetErrorCallback((GLFWErrorCallbackI)gLFWErrorCallback);
        if (gLFWErrorCallback2 != null) {
            gLFWErrorCallback2.free();
        }
        return string;
    }

    private static void pushClipboard(Window window, ByteBuffer byteBuffer, byte[] bs) {
        byteBuffer.clear();
        byteBuffer.put(bs);
        byteBuffer.put((byte)0);
        byteBuffer.flip();
        GLFW.glfwSetClipboardString((long)window.handle(), (ByteBuffer)byteBuffer);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setClipboard(Window window, String string) {
        byte[] bs = string.getBytes(StandardCharsets.UTF_8);
        int i = bs.length + 1;
        if (i < this.clipboardScratchBuffer.capacity()) {
            ClipboardManager.pushClipboard(window, this.clipboardScratchBuffer, bs);
        } else {
            ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)i);
            try {
                ClipboardManager.pushClipboard(window, byteBuffer, bs);
            }
            finally {
                MemoryUtil.memFree((Buffer)byteBuffer);
            }
        }
    }
}

