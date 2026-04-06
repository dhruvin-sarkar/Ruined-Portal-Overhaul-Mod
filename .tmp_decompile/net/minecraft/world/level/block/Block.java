/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.SupportType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Block
extends BlockBehaviour
implements ItemLike {
    public static final MapCodec<Block> CODEC = Block.simpleCodec(Block::new);
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Holder.Reference<Block> builtInRegistryHolder = BuiltInRegistries.BLOCK.createIntrusiveHolder(this);
    public static final IdMapper<BlockState> BLOCK_STATE_REGISTRY = new IdMapper();
    private static final LoadingCache<VoxelShape, Boolean> SHAPE_FULL_BLOCK_CACHE = CacheBuilder.newBuilder().maximumSize(512L).weakKeys().build((CacheLoader)new CacheLoader<VoxelShape, Boolean>(){

        public Boolean load(VoxelShape voxelShape) {
            return !Shapes.joinIsNotEmpty(Shapes.block(), voxelShape, BooleanOp.NOT_SAME);
        }

        public /* synthetic */ Object load(Object object) throws Exception {
            return this.load((VoxelShape)object);
        }
    });
    public static final int UPDATE_NEIGHBORS = 1;
    public static final int UPDATE_CLIENTS = 2;
    public static final int UPDATE_INVISIBLE = 4;
    public static final int UPDATE_IMMEDIATE = 8;
    public static final int UPDATE_KNOWN_SHAPE = 16;
    public static final int UPDATE_SUPPRESS_DROPS = 32;
    public static final int UPDATE_MOVE_BY_PISTON = 64;
    public static final int UPDATE_SKIP_SHAPE_UPDATE_ON_WIRE = 128;
    public static final int UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS = 256;
    public static final int UPDATE_SKIP_ON_PLACE = 512;
    @UpdateFlags
    public static final int UPDATE_NONE = 260;
    @UpdateFlags
    public static final int UPDATE_ALL = 3;
    @UpdateFlags
    public static final int UPDATE_ALL_IMMEDIATE = 11;
    @UpdateFlags
    public static final int UPDATE_SKIP_ALL_SIDEEFFECTS = 816;
    public static final float INDESTRUCTIBLE = -1.0f;
    public static final float INSTANT = 0.0f;
    public static final int UPDATE_LIMIT = 512;
    protected final StateDefinition<Block, BlockState> stateDefinition;
    private BlockState defaultBlockState;
    private @Nullable Item item;
    private static final int CACHE_SIZE = 256;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<ShapePairKey>> OCCLUSION_CACHE = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<ShapePairKey> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<ShapePairKey>(256, 0.25f){

            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    public static int getId(@Nullable BlockState blockState) {
        if (blockState == null) {
            return 0;
        }
        int i = BLOCK_STATE_REGISTRY.getId(blockState);
        return i == -1 ? 0 : i;
    }

    public static BlockState stateById(int i) {
        BlockState blockState = BLOCK_STATE_REGISTRY.byId(i);
        return blockState == null ? Blocks.AIR.defaultBlockState() : blockState;
    }

    public static Block byItem(@Nullable Item item) {
        if (item instanceof BlockItem) {
            return ((BlockItem)item).getBlock();
        }
        return Blocks.AIR;
    }

    public static BlockState pushEntitiesUp(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos) {
        VoxelShape voxelShape = Shapes.joinUnoptimized(blockState.getCollisionShape(levelAccessor, blockPos), blockState2.getCollisionShape(levelAccessor, blockPos), BooleanOp.ONLY_SECOND).move(blockPos);
        if (voxelShape.isEmpty()) {
            return blockState2;
        }
        List<Entity> list = levelAccessor.getEntities(null, voxelShape.bounds());
        for (Entity entity : list) {
            double d = Shapes.collide(Direction.Axis.Y, entity.getBoundingBox().move(0.0, 1.0, 0.0), List.of((Object)voxelShape), -1.0);
            entity.teleportRelative(0.0, 1.0 + d, 0.0);
        }
        return blockState2;
    }

    public static VoxelShape box(double d, double e, double f, double g, double h, double i) {
        return Shapes.box(d / 16.0, e / 16.0, f / 16.0, g / 16.0, h / 16.0, i / 16.0);
    }

    public static VoxelShape[] boxes(int i, IntFunction<VoxelShape> intFunction) {
        return (VoxelShape[])IntStream.rangeClosed(0, i).mapToObj(intFunction).toArray(VoxelShape[]::new);
    }

    public static VoxelShape cube(double d) {
        return Block.cube(d, d, d);
    }

    public static VoxelShape cube(double d, double e, double f) {
        double g = e / 2.0;
        return Block.column(d, f, 8.0 - g, 8.0 + g);
    }

    public static VoxelShape column(double d, double e, double f) {
        return Block.column(d, d, e, f);
    }

    public static VoxelShape column(double d, double e, double f, double g) {
        double h = d / 2.0;
        double i = e / 2.0;
        return Block.box(8.0 - h, f, 8.0 - i, 8.0 + h, g, 8.0 + i);
    }

    public static VoxelShape boxZ(double d, double e, double f) {
        return Block.boxZ(d, d, e, f);
    }

    public static VoxelShape boxZ(double d, double e, double f, double g) {
        double h = e / 2.0;
        return Block.boxZ(d, 8.0 - h, 8.0 + h, f, g);
    }

    public static VoxelShape boxZ(double d, double e, double f, double g, double h) {
        double i = d / 2.0;
        return Block.box(8.0 - i, e, g, 8.0 + i, f, h);
    }

    public static BlockState updateFromNeighbourShapes(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos) {
        BlockState blockState2 = blockState;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Direction direction : UPDATE_SHAPE_ORDER) {
            mutableBlockPos.setWithOffset((Vec3i)blockPos, direction);
            blockState2 = blockState2.updateShape(levelAccessor, levelAccessor, blockPos, direction, mutableBlockPos, levelAccessor.getBlockState(mutableBlockPos), levelAccessor.getRandom());
        }
        return blockState2;
    }

    public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, @UpdateFlags int i) {
        Block.updateOrDestroy(blockState, blockState2, levelAccessor, blockPos, i, 512);
    }

    public static void updateOrDestroy(BlockState blockState, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, @UpdateFlags int i, int j) {
        if (blockState2 != blockState) {
            if (blockState2.isAir()) {
                if (!levelAccessor.isClientSide()) {
                    levelAccessor.destroyBlock(blockPos, (i & 0x20) == 0, null, j);
                }
            } else {
                levelAccessor.setBlock(blockPos, blockState2, i & 0xFFFFFFDF, j);
            }
        }
    }

    public Block(BlockBehaviour.Properties properties) {
        super(properties);
        String string;
        StateDefinition.Builder<Block, BlockState> builder = new StateDefinition.Builder<Block, BlockState>(this);
        this.createBlockStateDefinition(builder);
        this.stateDefinition = builder.create(Block::defaultBlockState, BlockState::new);
        this.registerDefaultState(this.stateDefinition.any());
        if (SharedConstants.IS_RUNNING_IN_IDE && !(string = this.getClass().getSimpleName()).endsWith("Block")) {
            LOGGER.error("Block classes should end with Block and {} doesn't.", (Object)string);
        }
    }

    public static boolean isExceptionForConnection(BlockState blockState) {
        return blockState.getBlock() instanceof LeavesBlock || blockState.is(Blocks.BARRIER) || blockState.is(Blocks.CARVED_PUMPKIN) || blockState.is(Blocks.JACK_O_LANTERN) || blockState.is(Blocks.MELON) || blockState.is(Blocks.PUMPKIN) || blockState.is(BlockTags.SHULKER_BOXES);
    }

    protected static boolean dropFromBlockInteractLootTable(ServerLevel serverLevel, ResourceKey<LootTable> resourceKey, BlockState blockState, @Nullable BlockEntity blockEntity, @Nullable ItemStack itemStack, @Nullable Entity entity, BiConsumer<ServerLevel, ItemStack> biConsumer) {
        return Block.dropFromLootTable(serverLevel, resourceKey, builder -> builder.withParameter(LootContextParams.BLOCK_STATE, blockState).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity).withOptionalParameter(LootContextParams.INTERACTING_ENTITY, entity).withOptionalParameter(LootContextParams.TOOL, itemStack).create(LootContextParamSets.BLOCK_INTERACT), biConsumer);
    }

    protected static boolean dropFromLootTable(ServerLevel serverLevel, ResourceKey<LootTable> resourceKey, Function<LootParams.Builder, LootParams> function, BiConsumer<ServerLevel, ItemStack> biConsumer) {
        LootParams lootParams;
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(resourceKey);
        ObjectArrayList<ItemStack> list = lootTable.getRandomItems(lootParams = function.apply(new LootParams.Builder(serverLevel)));
        if (!list.isEmpty()) {
            list.forEach(itemStack -> biConsumer.accept(serverLevel, (ItemStack)itemStack));
            return true;
        }
        return false;
    }

    public static boolean shouldRenderFace(BlockState blockState, BlockState blockState2, Direction direction) {
        VoxelShape voxelShape = blockState2.getFaceOcclusionShape(direction.getOpposite());
        if (voxelShape == Shapes.block()) {
            return false;
        }
        if (blockState.skipRendering(blockState2, direction)) {
            return false;
        }
        if (voxelShape == Shapes.empty()) {
            return true;
        }
        VoxelShape voxelShape2 = blockState.getFaceOcclusionShape(direction);
        if (voxelShape2 == Shapes.empty()) {
            return true;
        }
        ShapePairKey shapePairKey = new ShapePairKey(voxelShape2, voxelShape);
        Object2ByteLinkedOpenHashMap<ShapePairKey> object2ByteLinkedOpenHashMap = OCCLUSION_CACHE.get();
        byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst((Object)shapePairKey);
        if (b != 127) {
            return b != 0;
        }
        boolean bl = Shapes.joinIsNotEmpty(voxelShape2, voxelShape, BooleanOp.ONLY_FIRST);
        if (object2ByteLinkedOpenHashMap.size() == 256) {
            object2ByteLinkedOpenHashMap.removeLastByte();
        }
        object2ByteLinkedOpenHashMap.putAndMoveToFirst((Object)shapePairKey, (byte)(bl ? 1 : 0));
        return bl;
    }

    public static boolean canSupportRigidBlock(BlockGetter blockGetter, BlockPos blockPos) {
        return blockGetter.getBlockState(blockPos).isFaceSturdy(blockGetter, blockPos, Direction.UP, SupportType.RIGID);
    }

    public static boolean canSupportCenter(LevelReader levelReader, BlockPos blockPos, Direction direction) {
        BlockState blockState = levelReader.getBlockState(blockPos);
        if (direction == Direction.DOWN && blockState.is(BlockTags.UNSTABLE_BOTTOM_CENTER)) {
            return false;
        }
        return blockState.isFaceSturdy(levelReader, blockPos, direction, SupportType.CENTER);
    }

    public static boolean isFaceFull(VoxelShape voxelShape, Direction direction) {
        VoxelShape voxelShape2 = voxelShape.getFaceShape(direction);
        return Block.isShapeFullBlock(voxelShape2);
    }

    public static boolean isShapeFullBlock(VoxelShape voxelShape) {
        return (Boolean)SHAPE_FULL_BLOCK_CACHE.getUnchecked((Object)voxelShape);
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
    }

    public void destroy(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
    }

    public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        LootParams.Builder builder = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, ItemStack.EMPTY).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        return blockState.getDrops(builder);
    }

    public static List<ItemStack> getDrops(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack) {
        LootParams.Builder builder = new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos)).withParameter(LootContextParams.TOOL, itemStack).withOptionalParameter(LootContextParams.THIS_ENTITY, entity).withOptionalParameter(LootContextParams.BLOCK_ENTITY, blockEntity);
        return blockState.getDrops(builder);
    }

    public static void dropResources(BlockState blockState, Level level, BlockPos blockPos) {
        if (level instanceof ServerLevel) {
            Block.getDrops(blockState, (ServerLevel)level, blockPos, null).forEach(itemStack -> Block.popResource(level, blockPos, itemStack));
            blockState.spawnAfterBreak((ServerLevel)level, blockPos, ItemStack.EMPTY, true);
        }
    }

    public static void dropResources(BlockState blockState, LevelAccessor levelAccessor, BlockPos blockPos, @Nullable BlockEntity blockEntity) {
        if (levelAccessor instanceof ServerLevel) {
            Block.getDrops(blockState, (ServerLevel)levelAccessor, blockPos, blockEntity).forEach(itemStack -> Block.popResource((Level)((ServerLevel)levelAccessor), blockPos, itemStack));
            blockState.spawnAfterBreak((ServerLevel)levelAccessor, blockPos, ItemStack.EMPTY, true);
        }
    }

    public static void dropResources(BlockState blockState, Level level, BlockPos blockPos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack itemStack2) {
        if (level instanceof ServerLevel) {
            Block.getDrops(blockState, (ServerLevel)level, blockPos, blockEntity, entity, itemStack2).forEach(itemStack -> Block.popResource(level, blockPos, itemStack));
            blockState.spawnAfterBreak((ServerLevel)level, blockPos, itemStack2, true);
        }
    }

    public static void popResource(Level level, BlockPos blockPos, ItemStack itemStack) {
        double d = (double)EntityType.ITEM.getHeight() / 2.0;
        double e = (double)blockPos.getX() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
        double f = (double)blockPos.getY() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25) - d;
        double g = (double)blockPos.getZ() + 0.5 + Mth.nextDouble(level.random, -0.25, 0.25);
        Block.popResource(level, () -> new ItemEntity(level, e, f, g, itemStack), itemStack);
    }

    public static void popResourceFromFace(Level level, BlockPos blockPos, Direction direction, ItemStack itemStack) {
        int i = direction.getStepX();
        int j = direction.getStepY();
        int k = direction.getStepZ();
        double d = (double)EntityType.ITEM.getWidth() / 2.0;
        double e = (double)EntityType.ITEM.getHeight() / 2.0;
        double f = (double)blockPos.getX() + 0.5 + (i == 0 ? Mth.nextDouble(level.random, -0.25, 0.25) : (double)i * (0.5 + d));
        double g = (double)blockPos.getY() + 0.5 + (j == 0 ? Mth.nextDouble(level.random, -0.25, 0.25) : (double)j * (0.5 + e)) - e;
        double h = (double)blockPos.getZ() + 0.5 + (k == 0 ? Mth.nextDouble(level.random, -0.25, 0.25) : (double)k * (0.5 + d));
        double l = i == 0 ? Mth.nextDouble(level.random, -0.1, 0.1) : (double)i * 0.1;
        double m = j == 0 ? Mth.nextDouble(level.random, 0.0, 0.1) : (double)j * 0.1 + 0.1;
        double n = k == 0 ? Mth.nextDouble(level.random, -0.1, 0.1) : (double)k * 0.1;
        Block.popResource(level, () -> new ItemEntity(level, f, g, h, itemStack, l, m, n), itemStack);
    }

    private static void popResource(Level level, Supplier<ItemEntity> supplier, ItemStack itemStack) {
        block3: {
            block2: {
                if (!(level instanceof ServerLevel)) break block2;
                ServerLevel serverLevel = (ServerLevel)level;
                if (!itemStack.isEmpty() && serverLevel.getGameRules().get(GameRules.BLOCK_DROPS).booleanValue()) break block3;
            }
            return;
        }
        ItemEntity itemEntity = supplier.get();
        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    protected void popExperience(ServerLevel serverLevel, BlockPos blockPos, int i) {
        if (serverLevel.getGameRules().get(GameRules.BLOCK_DROPS).booleanValue()) {
            ExperienceOrb.award(serverLevel, Vec3.atCenterOf(blockPos), i);
        }
    }

    public float getExplosionResistance() {
        return this.explosionResistance;
    }

    public void wasExploded(ServerLevel serverLevel, BlockPos blockPos, Explosion explosion) {
    }

    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
    }

    public @Nullable BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return this.defaultBlockState();
    }

    public void playerDestroy(Level level, Player player, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, ItemStack itemStack) {
        player.awardStat(Stats.BLOCK_MINED.get(this));
        player.causeFoodExhaustion(0.005f);
        Block.dropResources(blockState, level, blockPos, blockEntity, player, itemStack);
    }

    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
    }

    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return !blockState.isSolid() && !blockState.liquid();
    }

    public MutableComponent getName() {
        return Component.translatable(this.getDescriptionId());
    }

    public void fallOn(Level level, BlockState blockState, BlockPos blockPos, Entity entity, double d) {
        entity.causeFallDamage(d, 1.0f, entity.damageSources().fall());
    }

    public void updateEntityMovementAfterFallOn(BlockGetter blockGetter, Entity entity) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
    }

    public float getFriction() {
        return this.friction;
    }

    public float getSpeedFactor() {
        return this.speedFactor;
    }

    public float getJumpFactor() {
        return this.jumpFactor;
    }

    protected void spawnDestroyParticles(Level level, Player player, BlockPos blockPos, BlockState blockState) {
        level.levelEvent(player, 2001, blockPos, Block.getId(blockState));
    }

    public BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        this.spawnDestroyParticles(level, player, blockPos, blockState);
        if (blockState.is(BlockTags.GUARDED_BY_PIGLINS) && level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            PiglinAi.angerNearbyPiglins(serverLevel, player, false);
        }
        level.gameEvent(GameEvent.BLOCK_DESTROY, blockPos, GameEvent.Context.of(player, blockState));
        return blockState;
    }

    public void handlePrecipitation(BlockState blockState, Level level, BlockPos blockPos, Biome.Precipitation precipitation) {
    }

    public boolean dropFromExplosion(Explosion explosion) {
        return true;
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    public StateDefinition<Block, BlockState> getStateDefinition() {
        return this.stateDefinition;
    }

    protected final void registerDefaultState(BlockState blockState) {
        this.defaultBlockState = blockState;
    }

    public final BlockState defaultBlockState() {
        return this.defaultBlockState;
    }

    public final BlockState withPropertiesOf(BlockState blockState) {
        BlockState blockState2 = this.defaultBlockState();
        for (Property<?> property : blockState.getBlock().getStateDefinition().getProperties()) {
            if (!blockState2.hasProperty(property)) continue;
            blockState2 = Block.copyProperty(blockState, blockState2, property);
        }
        return blockState2;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState blockState, BlockState blockState2, Property<T> property) {
        return (BlockState)blockState2.setValue(property, blockState.getValue(property));
    }

    @Override
    public Item asItem() {
        if (this.item == null) {
            this.item = Item.byBlock(this);
        }
        return this.item;
    }

    public boolean hasDynamicShape() {
        return this.dynamicShape;
    }

    public String toString() {
        return "Block{" + BuiltInRegistries.BLOCK.wrapAsHolder(this).getRegisteredName() + "}";
    }

    @Override
    protected Block asBlock() {
        return this;
    }

    protected Function<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> function) {
        return arg_0 -> ((ImmutableMap)((ImmutableMap)this.stateDefinition.getPossibleStates().stream().collect(ImmutableMap.toImmutableMap(Function.identity(), function)))).get(arg_0);
    }

    protected Function<BlockState, VoxelShape> getShapeForEachState(Function<BlockState, VoxelShape> function, Property<?> ... propertys) {
        Map<Property, Object> map = Arrays.stream(propertys).collect(Collectors.toMap(property -> property, property -> property.getPossibleValues().getFirst()));
        ImmutableMap immutableMap = (ImmutableMap)this.stateDefinition.getPossibleStates().stream().filter(blockState -> map.entrySet().stream().allMatch(entry -> blockState.getValue((Property)entry.getKey()) == entry.getValue())).collect(ImmutableMap.toImmutableMap(Function.identity(), function));
        return blockState -> {
            for (Map.Entry entry : map.entrySet()) {
                blockState = Block.setValueHelper(blockState, (Property)entry.getKey(), entry.getValue());
            }
            return (VoxelShape)immutableMap.get(blockState);
        };
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S stateHolder, Property<T> property, Object object) {
        return (S)((StateHolder)stateHolder.setValue(property, (Comparable)((Comparable)object)));
    }

    @Deprecated
    public Holder.Reference<Block> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    protected void tryDropExperience(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, IntProvider intProvider) {
        int i = EnchantmentHelper.processBlockExperience(serverLevel, itemStack, intProvider.sample(serverLevel.getRandom()));
        if (i > 0) {
            this.popExperience(serverLevel, blockPos, i);
        }
    }

    record ShapePairKey(VoxelShape first, VoxelShape second) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        public boolean equals(Object object) {
            if (!(object instanceof ShapePairKey)) return false;
            ShapePairKey shapePairKey = (ShapePairKey)((Object)object);
            if (this.first != shapePairKey.first) return false;
            if (this.second != shapePairKey.second) return false;
            return true;
        }

        public int hashCode() {
            return System.identityHashCode(this.first) * 31 + System.identityHashCode(this.second);
        }
    }

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE_USE})
    public static @interface UpdateFlags {
    }
}

