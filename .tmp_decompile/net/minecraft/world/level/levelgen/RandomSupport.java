/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.hash.HashFunction
 *  com.google.common.hash.Hashing
 *  com.google.common.primitives.Longs
 */
package net.minecraft.world.level.levelgen;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

public final class RandomSupport {
    public static final long GOLDEN_RATIO_64 = -7046029254386353131L;
    public static final long SILVER_RATIO_64 = 7640891576956012809L;
    private static final HashFunction MD5_128 = Hashing.md5();
    private static final AtomicLong SEED_UNIQUIFIER = new AtomicLong(8682522807148012L);

    @VisibleForTesting
    public static long mixStafford13(long l) {
        l = (l ^ l >>> 30) * -4658895280553007687L;
        l = (l ^ l >>> 27) * -7723592293110705685L;
        return l ^ l >>> 31;
    }

    public static Seed128bit upgradeSeedTo128bitUnmixed(long l) {
        long m = l ^ 0x6A09E667F3BCC909L;
        long n = m + -7046029254386353131L;
        return new Seed128bit(m, n);
    }

    public static Seed128bit upgradeSeedTo128bit(long l) {
        return RandomSupport.upgradeSeedTo128bitUnmixed(l).mixed();
    }

    public static Seed128bit seedFromHashOf(String string) {
        byte[] bs = MD5_128.hashString((CharSequence)string, StandardCharsets.UTF_8).asBytes();
        long l = Longs.fromBytes((byte)bs[0], (byte)bs[1], (byte)bs[2], (byte)bs[3], (byte)bs[4], (byte)bs[5], (byte)bs[6], (byte)bs[7]);
        long m = Longs.fromBytes((byte)bs[8], (byte)bs[9], (byte)bs[10], (byte)bs[11], (byte)bs[12], (byte)bs[13], (byte)bs[14], (byte)bs[15]);
        return new Seed128bit(l, m);
    }

    public static long generateUniqueSeed() {
        return SEED_UNIQUIFIER.updateAndGet(l -> l * 1181783497276652981L) ^ System.nanoTime();
    }

    public record Seed128bit(long seedLo, long seedHi) {
        public Seed128bit xor(long l, long m) {
            return new Seed128bit(this.seedLo ^ l, this.seedHi ^ m);
        }

        public Seed128bit xor(Seed128bit seed128bit) {
            return this.xor(seed128bit.seedLo, seed128bit.seedHi);
        }

        public Seed128bit mixed() {
            return new Seed128bit(RandomSupport.mixStafford13(this.seedLo), RandomSupport.mixStafford13(this.seedHi));
        }
    }
}

