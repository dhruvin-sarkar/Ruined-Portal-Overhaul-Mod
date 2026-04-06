/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class RandomSequences
extends SavedData {
    public static final Codec<RandomSequences> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.INT.fieldOf("salt").forGetter(RandomSequences::salt), (App)Codec.BOOL.optionalFieldOf("include_world_seed", (Object)true).forGetter(RandomSequences::includeWorldSeed), (App)Codec.BOOL.optionalFieldOf("include_sequence_id", (Object)true).forGetter(RandomSequences::includeSequenceId), (App)Codec.unboundedMap(Identifier.CODEC, RandomSequence.CODEC).fieldOf("sequences").forGetter(randomSequences -> randomSequences.sequences)).apply((Applicative)instance, RandomSequences::new));
    public static final SavedDataType<RandomSequences> TYPE = new SavedDataType<RandomSequences>("random_sequences", RandomSequences::new, CODEC, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<Identifier, RandomSequence> sequences = new Object2ObjectOpenHashMap();

    public RandomSequences() {
    }

    private RandomSequences(int i, boolean bl, boolean bl2, Map<Identifier, RandomSequence> map) {
        this.salt = i;
        this.includeWorldSeed = bl;
        this.includeSequenceId = bl2;
        this.sequences.putAll(map);
    }

    public RandomSource get(Identifier identifier2, long l) {
        RandomSource randomSource = this.sequences.computeIfAbsent(identifier2, identifier -> this.createSequence((Identifier)identifier, l)).random();
        return new DirtyMarkingRandomSource(randomSource);
    }

    private RandomSequence createSequence(Identifier identifier, long l) {
        return this.createSequence(identifier, l, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(Identifier identifier, long l, int i, boolean bl, boolean bl2) {
        long m = (bl ? l : 0L) ^ (long)i;
        return new RandomSequence(m, bl2 ? Optional.of(identifier) : Optional.empty());
    }

    public void forAllSequences(BiConsumer<Identifier, RandomSequence> biConsumer) {
        this.sequences.forEach(biConsumer);
    }

    public void setSeedDefaults(int i, boolean bl, boolean bl2) {
        this.salt = i;
        this.includeWorldSeed = bl;
        this.includeSequenceId = bl2;
    }

    public int clear() {
        int i = this.sequences.size();
        this.sequences.clear();
        return i;
    }

    public void reset(Identifier identifier, long l) {
        this.sequences.put(identifier, this.createSequence(identifier, l));
    }

    public void reset(Identifier identifier, long l, int i, boolean bl, boolean bl2) {
        this.sequences.put(identifier, this.createSequence(identifier, l, i, bl, bl2));
    }

    private int salt() {
        return this.salt;
    }

    private boolean includeWorldSeed() {
        return this.includeWorldSeed;
    }

    private boolean includeSequenceId() {
        return this.includeSequenceId;
    }

    class DirtyMarkingRandomSource
    implements RandomSource {
        private final RandomSource random;

        DirtyMarkingRandomSource(RandomSource randomSource) {
            this.random = randomSource;
        }

        @Override
        public RandomSource fork() {
            RandomSequences.this.setDirty();
            return this.random.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return this.random.forkPositional();
        }

        @Override
        public void setSeed(long l) {
            RandomSequences.this.setDirty();
            this.random.setSeed(l);
        }

        @Override
        public int nextInt() {
            RandomSequences.this.setDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int i) {
            RandomSequences.this.setDirty();
            return this.random.nextInt(i);
        }

        @Override
        public long nextLong() {
            RandomSequences.this.setDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequences.this.setDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequences.this.setDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequences.this.setDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof DirtyMarkingRandomSource) {
                DirtyMarkingRandomSource dirtyMarkingRandomSource = (DirtyMarkingRandomSource)object;
                return this.random.equals(dirtyMarkingRandomSource.random);
            }
            return false;
        }
    }
}

