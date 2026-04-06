/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.chunk;

import java.util.Arrays;
import net.minecraft.util.Util;
import net.minecraft.util.VisibleForDebug;
import org.jspecify.annotations.Nullable;

public class DataLayer {
    public static final int LAYER_COUNT = 16;
    public static final int LAYER_SIZE = 128;
    public static final int SIZE = 2048;
    private static final int NIBBLE_SIZE = 4;
    protected byte @Nullable [] data;
    private int defaultValue;

    public DataLayer() {
        this(0);
    }

    public DataLayer(int i) {
        this.defaultValue = i;
    }

    public DataLayer(byte[] bs) {
        this.data = bs;
        this.defaultValue = 0;
        if (bs.length != 2048) {
            throw Util.pauseInIde(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + bs.length));
        }
    }

    public int get(int i, int j, int k) {
        return this.get(DataLayer.getIndex(i, j, k));
    }

    public void set(int i, int j, int k, int l) {
        this.set(DataLayer.getIndex(i, j, k), l);
    }

    private static int getIndex(int i, int j, int k) {
        return j << 8 | k << 4 | i;
    }

    private int get(int i) {
        if (this.data == null) {
            return this.defaultValue;
        }
        int j = DataLayer.getByteIndex(i);
        int k = DataLayer.getNibbleIndex(i);
        return this.data[j] >> 4 * k & 0xF;
    }

    private void set(int i, int j) {
        byte[] bs = this.getData();
        int k = DataLayer.getByteIndex(i);
        int l = DataLayer.getNibbleIndex(i);
        int m = ~(15 << 4 * l);
        int n = (j & 0xF) << 4 * l;
        bs[k] = (byte)(bs[k] & m | n);
    }

    private static int getNibbleIndex(int i) {
        return i & 1;
    }

    private static int getByteIndex(int i) {
        return i >> 1;
    }

    public void fill(int i) {
        this.defaultValue = i;
        this.data = null;
    }

    private static byte packFilled(int i) {
        byte b = (byte)i;
        for (int j = 4; j < 8; j += 4) {
            b = (byte)(b | i << j);
        }
        return b;
    }

    public byte[] getData() {
        if (this.data == null) {
            this.data = new byte[2048];
            if (this.defaultValue != 0) {
                Arrays.fill(this.data, DataLayer.packFilled(this.defaultValue));
            }
        }
        return this.data;
    }

    public DataLayer copy() {
        if (this.data == null) {
            return new DataLayer(this.defaultValue);
        }
        return new DataLayer((byte[])this.data.clone());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4096; ++i) {
            stringBuilder.append(Integer.toHexString(this.get(i)));
            if ((i & 0xF) == 15) {
                stringBuilder.append("\n");
            }
            if ((i & 0xFF) != 255) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    @VisibleForDebug
    public String layerToString(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < 256; ++j) {
            stringBuilder.append(Integer.toHexString(this.get(j)));
            if ((j & 0xF) != 15) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public boolean isDefinitelyHomogenous() {
        return this.data == null;
    }

    public boolean isDefinitelyFilledWith(int i) {
        return this.data == null && this.defaultValue == i;
    }

    public boolean isEmpty() {
        return this.data == null && this.defaultValue == 0;
    }
}

