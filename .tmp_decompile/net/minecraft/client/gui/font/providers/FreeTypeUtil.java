/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.util.freetype.FT_Vector
 *  org.lwjgl.util.freetype.FreeType
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font.providers;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class FreeTypeUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Object LIBRARY_LOCK = new Object();
    private static long library = 0L;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long getLibrary() {
        Object object = LIBRARY_LOCK;
        synchronized (object) {
            if (library == 0L) {
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
                    FreeTypeUtil.assertError(FreeType.FT_Init_FreeType((PointerBuffer)pointerBuffer), "Initializing FreeType library");
                    library = pointerBuffer.get();
                }
            }
            return library;
        }
    }

    public static void assertError(int i, String string) {
        if (i != 0) {
            throw new IllegalStateException("FreeType error: " + FreeTypeUtil.describeError(i) + " (" + string + ")");
        }
    }

    public static boolean checkError(int i, String string) {
        if (i != 0) {
            LOGGER.error("FreeType error: {} ({})", (Object)FreeTypeUtil.describeError(i), (Object)string);
            return true;
        }
        return false;
    }

    private static String describeError(int i) {
        String string = FreeType.FT_Error_String((int)i);
        if (string != null) {
            return string;
        }
        return "Unrecognized error: 0x" + Integer.toHexString(i);
    }

    public static FT_Vector setVector(FT_Vector fT_Vector, float f, float g) {
        long l = Math.round(f * 64.0f);
        long m = Math.round(g * 64.0f);
        return fT_Vector.set(l, m);
    }

    public static float x(FT_Vector fT_Vector) {
        return (float)fT_Vector.x() / 64.0f;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void destroy() {
        Object object = LIBRARY_LOCK;
        synchronized (object) {
            if (library != 0L) {
                FreeType.FT_Done_Library((long)library);
                library = 0L;
            }
        }
    }
}

