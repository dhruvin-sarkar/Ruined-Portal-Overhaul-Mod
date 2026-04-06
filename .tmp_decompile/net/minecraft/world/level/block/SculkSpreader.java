/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SculkSpreader {
    public static final int MAX_GROWTH_RATE_RADIUS = 24;
    public static final int MAX_CHARGE = 1000;
    public static final float MAX_DECAY_FACTOR = 0.5f;
    private static final int MAX_CURSORS = 32;
    public static final int SHRIEKER_PLACEMENT_RATE = 11;
    public static final int MAX_CURSOR_DISTANCE = 1024;
    final boolean isWorldGeneration;
    private final TagKey<Block> replaceableBlocks;
    private final int growthSpawnCost;
    private final int noGrowthRadius;
    private final int chargeDecayRate;
    private final int additionalDecayRate;
    private List<ChargeCursor> cursors = new ArrayList<ChargeCursor>();

    public SculkSpreader(boolean bl, TagKey<Block> tagKey, int i, int j, int k, int l) {
        this.isWorldGeneration = bl;
        this.replaceableBlocks = tagKey;
        this.growthSpawnCost = i;
        this.noGrowthRadius = j;
        this.chargeDecayRate = k;
        this.additionalDecayRate = l;
    }

    public static SculkSpreader createLevelSpreader() {
        return new SculkSpreader(false, BlockTags.SCULK_REPLACEABLE, 10, 4, 10, 5);
    }

    public static SculkSpreader createWorldGenSpreader() {
        return new SculkSpreader(true, BlockTags.SCULK_REPLACEABLE_WORLD_GEN, 50, 1, 5, 10);
    }

    public TagKey<Block> replaceableBlocks() {
        return this.replaceableBlocks;
    }

    public int growthSpawnCost() {
        return this.growthSpawnCost;
    }

    public int noGrowthRadius() {
        return this.noGrowthRadius;
    }

    public int chargeDecayRate() {
        return this.chargeDecayRate;
    }

    public int additionalDecayRate() {
        return this.additionalDecayRate;
    }

    public boolean isWorldGeneration() {
        return this.isWorldGeneration;
    }

    @VisibleForTesting
    public List<ChargeCursor> getCursors() {
        return this.cursors;
    }

    public void clear() {
        this.cursors.clear();
    }

    public void load(ValueInput valueInput) {
        this.cursors.clear();
        valueInput.read("cursors", ChargeCursor.CODEC.sizeLimitedListOf(32)).orElse(List.of()).forEach(this::addCursor);
    }

    public void save(ValueOutput valueOutput) {
        valueOutput.store("cursors", ChargeCursor.CODEC.listOf(), this.cursors);
        if (SharedConstants.DEBUG_SCULK_CATALYST) {
            int i = this.getCursors().stream().map(ChargeCursor::getCharge).reduce(0, Integer::sum);
            int j = this.getCursors().stream().map(chargeCursor -> 1).reduce(0, Integer::sum);
            int k = this.getCursors().stream().map(ChargeCursor::getCharge).reduce(0, Math::max);
            valueOutput.putInt("stats.total", i);
            valueOutput.putInt("stats.count", j);
            valueOutput.putInt("stats.max", k);
            valueOutput.putInt("stats.avg", i / (j + 1));
        }
    }

    public void addCursors(BlockPos blockPos, int i) {
        while (i > 0) {
            int j = Math.min(i, 1000);
            this.addCursor(new ChargeCursor(blockPos, j));
            i -= j;
        }
    }

    private void addCursor(ChargeCursor chargeCursor) {
        if (this.cursors.size() >= 32) {
            return;
        }
        this.cursors.add(chargeCursor);
    }

    public void updateCursors(LevelAccessor levelAccessor, BlockPos blockPos2, RandomSource randomSource, boolean bl) {
        BlockPos blockPos22;
        if (this.cursors.isEmpty()) {
            return;
        }
        ArrayList<ChargeCursor> list = new ArrayList<ChargeCursor>();
        HashMap<BlockPos, ChargeCursor> map = new HashMap<BlockPos, ChargeCursor>();
        Object2IntOpenHashMap object2IntMap = new Object2IntOpenHashMap();
        for (ChargeCursor chargeCursor : this.cursors) {
            if (chargeCursor.isPosUnreasonable(blockPos2)) continue;
            chargeCursor.update(levelAccessor, blockPos2, randomSource, this, bl);
            if (chargeCursor.charge <= 0) {
                levelAccessor.levelEvent(3006, chargeCursor.getPos(), 0);
                continue;
            }
            blockPos22 = chargeCursor.getPos();
            object2IntMap.computeInt((Object)blockPos22, (blockPos, integer) -> (integer == null ? 0 : integer) + chargeCursor.charge);
            ChargeCursor chargeCursor2 = (ChargeCursor)map.get(blockPos22);
            if (chargeCursor2 == null) {
                map.put(blockPos22, chargeCursor);
                list.add(chargeCursor);
                continue;
            }
            if (!this.isWorldGeneration() && chargeCursor.charge + chargeCursor2.charge <= 1000) {
                chargeCursor2.mergeWith(chargeCursor);
                continue;
            }
            list.add(chargeCursor);
            if (chargeCursor.charge >= chargeCursor2.charge) continue;
            map.put(blockPos22, chargeCursor);
        }
        for (Object2IntMap.Entry entry : object2IntMap.object2IntEntrySet()) {
            Set<Direction> collection;
            blockPos22 = (BlockPos)entry.getKey();
            int i = entry.getIntValue();
            ChargeCursor chargeCursor3 = (ChargeCursor)map.get(blockPos22);
            Set<Direction> set = collection = chargeCursor3 == null ? null : chargeCursor3.getFacingData();
            if (i <= 0 || collection == null) continue;
            int j = (int)(Math.log1p(i) / (double)2.3f) + 1;
            int k = (j << 6) + MultifaceBlock.pack(collection);
            levelAccessor.levelEvent(3006, blockPos22, k);
        }
        this.cursors = list;
    }

    public static class ChargeCursor {
        private static final ObjectArrayList<Vec3i> NON_CORNER_NEIGHBOURS = Util.make(new ObjectArrayList(18), objectArrayList -> BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).filter(blockPos -> (blockPos.getX() == 0 || blockPos.getY() == 0 || blockPos.getZ() == 0) && !blockPos.equals(BlockPos.ZERO)).map(BlockPos::immutable).forEach(arg_0 -> ((ObjectArrayList)objectArrayList).add(arg_0)));
        public static final int MAX_CURSOR_DECAY_DELAY = 1;
        private BlockPos pos;
        int charge;
        private int updateDelay;
        private int decayDelay;
        private @Nullable Set<Direction> facings;
        private static final Codec<Set<Direction>> DIRECTION_SET = Direction.CODEC.listOf().xmap(list -> Sets.newEnumSet((Iterable)list, Direction.class), Lists::newArrayList);
        public static final Codec<ChargeCursor> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(ChargeCursor::getPos), (App)Codec.intRange((int)0, (int)1000).fieldOf("charge").orElse((Object)0).forGetter(ChargeCursor::getCharge), (App)Codec.intRange((int)0, (int)1).fieldOf("decay_delay").orElse((Object)1).forGetter(ChargeCursor::getDecayDelay), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).fieldOf("update_delay").orElse((Object)0).forGetter(chargeCursor -> chargeCursor.updateDelay), (App)DIRECTION_SET.lenientOptionalFieldOf("facings").forGetter(chargeCursor -> Optional.ofNullable(chargeCursor.getFacingData()))).apply((Applicative)instance, ChargeCursor::new));

        private ChargeCursor(BlockPos blockPos, int i, int j, int k, Optional<Set<Direction>> optional) {
            this.pos = blockPos;
            this.charge = i;
            this.decayDelay = j;
            this.updateDelay = k;
            this.facings = optional.orElse(null);
        }

        public ChargeCursor(BlockPos blockPos, int i) {
            this(blockPos, i, 1, 0, Optional.empty());
        }

        public BlockPos getPos() {
            return this.pos;
        }

        boolean isPosUnreasonable(BlockPos blockPos) {
            return this.pos.distChessboard(blockPos) > 1024;
        }

        public int getCharge() {
            return this.charge;
        }

        public int getDecayDelay() {
            return this.decayDelay;
        }

        public @Nullable Set<Direction> getFacingData() {
            return this.facings;
        }

        private boolean shouldUpdate(LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
            if (this.charge <= 0) {
                return false;
            }
            if (bl) {
                return true;
            }
            if (levelAccessor instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)levelAccessor;
                return serverLevel.shouldTickBlocksAt(blockPos);
            }
            return false;
        }

        public void update(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, SculkSpreader sculkSpreader, boolean bl) {
            if (!this.shouldUpdate(levelAccessor, blockPos, sculkSpreader.isWorldGeneration)) {
                return;
            }
            if (this.updateDelay > 0) {
                --this.updateDelay;
                return;
            }
            BlockState blockState = levelAccessor.getBlockState(this.pos);
            SculkBehaviour sculkBehaviour = ChargeCursor.getBlockBehaviour(blockState);
            if (bl && sculkBehaviour.attemptSpreadVein(levelAccessor, this.pos, blockState, this.facings, sculkSpreader.isWorldGeneration())) {
                if (sculkBehaviour.canChangeBlockStateOnSpread()) {
                    blockState = levelAccessor.getBlockState(this.pos);
                    sculkBehaviour = ChargeCursor.getBlockBehaviour(blockState);
                }
                levelAccessor.playSound(null, this.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            this.charge = sculkBehaviour.attemptUseCharge(this, levelAccessor, blockPos, randomSource, sculkSpreader, bl);
            if (this.charge <= 0) {
                sculkBehaviour.onDischarged(levelAccessor, blockState, this.pos, randomSource);
                return;
            }
            BlockPos blockPos2 = ChargeCursor.getValidMovementPos(levelAccessor, this.pos, randomSource);
            if (blockPos2 != null) {
                sculkBehaviour.onDischarged(levelAccessor, blockState, this.pos, randomSource);
                this.pos = blockPos2.immutable();
                if (sculkSpreader.isWorldGeneration() && !this.pos.closerThan(new Vec3i(blockPos.getX(), this.pos.getY(), blockPos.getZ()), 15.0)) {
                    this.charge = 0;
                    return;
                }
                blockState = levelAccessor.getBlockState(blockPos2);
            }
            if (blockState.getBlock() instanceof SculkBehaviour) {
                this.facings = MultifaceBlock.availableFaces(blockState);
            }
            this.decayDelay = sculkBehaviour.updateDecayDelay(this.decayDelay);
            this.updateDelay = sculkBehaviour.getSculkSpreadDelay();
        }

        void mergeWith(ChargeCursor chargeCursor) {
            this.charge += chargeCursor.charge;
            chargeCursor.charge = 0;
            this.updateDelay = Math.min(this.updateDelay, chargeCursor.updateDelay);
        }

        private static SculkBehaviour getBlockBehaviour(BlockState blockState) {
            SculkBehaviour sculkBehaviour;
            Block block = blockState.getBlock();
            return block instanceof SculkBehaviour ? (sculkBehaviour = (SculkBehaviour)((Object)block)) : SculkBehaviour.DEFAULT;
        }

        private static List<Vec3i> getRandomizedNonCornerNeighbourOffsets(RandomSource randomSource) {
            return Util.shuffledCopy(NON_CORNER_NEIGHBOURS, randomSource);
        }

        private static @Nullable BlockPos getValidMovementPos(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
            BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();
            for (Vec3i vec3i : ChargeCursor.getRandomizedNonCornerNeighbourOffsets(randomSource)) {
                mutableBlockPos2.setWithOffset((Vec3i)blockPos, vec3i);
                BlockState blockState = levelAccessor.getBlockState(mutableBlockPos2);
                if (!(blockState.getBlock() instanceof SculkBehaviour) || !ChargeCursor.isMovementUnobstructed(levelAccessor, blockPos, mutableBlockPos2)) continue;
                mutableBlockPos.set(mutableBlockPos2);
                if (!SculkVeinBlock.hasSubstrateAccess(levelAccessor, blockState, mutableBlockPos2)) continue;
                break;
            }
            return mutableBlockPos.equals(blockPos) ? null : mutableBlockPos;
        }

        private static boolean isMovementUnobstructed(LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
            if (blockPos.distManhattan(blockPos2) == 1) {
                return true;
            }
            BlockPos blockPos3 = blockPos2.subtract(blockPos);
            Direction direction = Direction.fromAxisAndDirection(Direction.Axis.X, blockPos3.getX() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction2 = Direction.fromAxisAndDirection(Direction.Axis.Y, blockPos3.getY() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction3 = Direction.fromAxisAndDirection(Direction.Axis.Z, blockPos3.getZ() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            if (blockPos3.getX() == 0) {
                return ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction2) || ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction3);
            }
            if (blockPos3.getY() == 0) {
                return ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction) || ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction3);
            }
            return ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction) || ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction2);
        }

        private static boolean isUnobstructed(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction) {
            BlockPos blockPos2 = blockPos.relative(direction);
            return !levelAccessor.getBlockState(blockPos2).isFaceSturdy(levelAccessor, blockPos2, direction.getOpposite());
        }
    }
}

