/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.EvictingQueue
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  java.util.HexFormat
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.ARBDebugOutput
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.opengl.GLDebugMessageARBCallback
 *  org.lwjgl.opengl.GLDebugMessageARBCallbackI
 *  org.lwjgl.opengl.GLDebugMessageCallback
 *  org.lwjgl.opengl.GLDebugMessageCallbackI
 *  org.lwjgl.opengl.KHRDebug
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.opengl;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.platform.DebugMemoryUntracker;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBDebugOutput;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLDebugMessageARBCallback;
import org.lwjgl.opengl.GLDebugMessageARBCallbackI;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.lwjgl.opengl.GLDebugMessageCallbackI;
import org.lwjgl.opengl.KHRDebug;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class GlDebug {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CIRCULAR_LOG_SIZE = 10;
    private final Queue<LogEntry> MESSAGE_BUFFER = EvictingQueue.create((int)10);
    private volatile @Nullable LogEntry lastEntry;
    private static final List<Integer> DEBUG_LEVELS = ImmutableList.of((Object)37190, (Object)37191, (Object)37192, (Object)33387);
    private static final List<Integer> DEBUG_LEVELS_ARB = ImmutableList.of((Object)37190, (Object)37191, (Object)37192);

    private static String printUnknownToken(int i) {
        return "Unknown (0x" + HexFormat.of().withUpperCase().toHexDigits(i) + ")";
    }

    public static String sourceToString(int i) {
        switch (i) {
            case 33350: {
                return "API";
            }
            case 33351: {
                return "WINDOW SYSTEM";
            }
            case 33352: {
                return "SHADER COMPILER";
            }
            case 33353: {
                return "THIRD PARTY";
            }
            case 33354: {
                return "APPLICATION";
            }
            case 33355: {
                return "OTHER";
            }
        }
        return GlDebug.printUnknownToken(i);
    }

    public static String typeToString(int i) {
        switch (i) {
            case 33356: {
                return "ERROR";
            }
            case 33357: {
                return "DEPRECATED BEHAVIOR";
            }
            case 33358: {
                return "UNDEFINED BEHAVIOR";
            }
            case 33359: {
                return "PORTABILITY";
            }
            case 33360: {
                return "PERFORMANCE";
            }
            case 33361: {
                return "OTHER";
            }
            case 33384: {
                return "MARKER";
            }
        }
        return GlDebug.printUnknownToken(i);
    }

    public static String severityToString(int i) {
        switch (i) {
            case 37190: {
                return "HIGH";
            }
            case 37191: {
                return "MEDIUM";
            }
            case 37192: {
                return "LOW";
            }
            case 33387: {
                return "NOTIFICATION";
            }
        }
        return GlDebug.printUnknownToken(i);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void printDebugLog(int i, int j, int k, int l, int m, long n, long o) {
        LogEntry logEntry;
        String string = GLDebugMessageCallback.getMessage((int)m, (long)n);
        Queue<LogEntry> queue = this.MESSAGE_BUFFER;
        synchronized (queue) {
            logEntry = this.lastEntry;
            if (logEntry == null || !logEntry.isSame(i, j, k, l, string)) {
                logEntry = new LogEntry(i, j, k, l, string);
                this.MESSAGE_BUFFER.add(logEntry);
                this.lastEntry = logEntry;
            } else {
                ++logEntry.count;
            }
        }
        LOGGER.info("OpenGL debug message: {}", (Object)logEntry);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public List<String> getLastOpenGlDebugMessages() {
        Queue<LogEntry> queue = this.MESSAGE_BUFFER;
        synchronized (queue) {
            ArrayList list = Lists.newArrayListWithCapacity((int)this.MESSAGE_BUFFER.size());
            for (LogEntry logEntry : this.MESSAGE_BUFFER) {
                list.add(String.valueOf(logEntry) + " x " + logEntry.count);
            }
            return list;
        }
    }

    public static @Nullable GlDebug enableDebugCallback(int i, boolean bl, Set<String> set) {
        if (i <= 0) {
            return null;
        }
        GLCapabilities gLCapabilities = GL.getCapabilities();
        if (gLCapabilities.GL_KHR_debug && GlDevice.USE_GL_KHR_debug) {
            GlDebug glDebug = new GlDebug();
            set.add("GL_KHR_debug");
            GL11.glEnable((int)37600);
            if (bl) {
                GL11.glEnable((int)33346);
            }
            for (int j = 0; j < DEBUG_LEVELS.size(); ++j) {
                boolean bl2 = j < i;
                KHRDebug.glDebugMessageControl((int)4352, (int)4352, (int)DEBUG_LEVELS.get(j), (int[])null, (boolean)bl2);
            }
            KHRDebug.glDebugMessageCallback((GLDebugMessageCallbackI)((GLDebugMessageCallbackI)GLX.make(GLDebugMessageCallback.create(glDebug::printDebugLog), DebugMemoryUntracker::untrack)), (long)0L);
            return glDebug;
        }
        if (gLCapabilities.GL_ARB_debug_output && GlDevice.USE_GL_ARB_debug_output) {
            GlDebug glDebug = new GlDebug();
            set.add("GL_ARB_debug_output");
            if (bl) {
                GL11.glEnable((int)33346);
            }
            for (int j = 0; j < DEBUG_LEVELS_ARB.size(); ++j) {
                boolean bl2 = j < i;
                ARBDebugOutput.glDebugMessageControlARB((int)4352, (int)4352, (int)DEBUG_LEVELS_ARB.get(j), (int[])null, (boolean)bl2);
            }
            ARBDebugOutput.glDebugMessageCallbackARB((GLDebugMessageARBCallbackI)((GLDebugMessageARBCallbackI)GLX.make(GLDebugMessageARBCallback.create(glDebug::printDebugLog), DebugMemoryUntracker::untrack)), (long)0L);
            return glDebug;
        }
        return null;
    }

    @Environment(value=EnvType.CLIENT)
    static class LogEntry {
        private final int id;
        private final int source;
        private final int type;
        private final int severity;
        private final String message;
        int count = 1;

        LogEntry(int i, int j, int k, int l, String string) {
            this.id = k;
            this.source = i;
            this.type = j;
            this.severity = l;
            this.message = string;
        }

        boolean isSame(int i, int j, int k, int l, String string) {
            return j == this.type && i == this.source && k == this.id && l == this.severity && string.equals(this.message);
        }

        public String toString() {
            return "id=" + this.id + ", source=" + GlDebug.sourceToString(this.source) + ", type=" + GlDebug.typeToString(this.type) + ", severity=" + GlDebug.severityToString(this.severity) + ", message='" + this.message + "'";
        }
    }
}

