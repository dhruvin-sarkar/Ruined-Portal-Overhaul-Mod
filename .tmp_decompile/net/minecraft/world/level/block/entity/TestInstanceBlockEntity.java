/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 *  java.lang.MatchException
 */
package net.minecraft.world.level.block.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.structures.NbtToSnbt;
import net.minecraft.gametest.framework.FailedTestTracker;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.GameTestRunner;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.RetryOptions;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.gametest.framework.TestCommand;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BeaconBeamOwner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class TestInstanceBlockEntity
extends BlockEntity
implements BeaconBeamOwner,
BoundingBoxRenderable {
    private static final Component INVALID_TEST_NAME = Component.translatable("test_instance_block.invalid_test");
    private static final List<BeaconBeamOwner.Section> BEAM_CLEARED = List.of();
    private static final List<BeaconBeamOwner.Section> BEAM_RUNNING = List.of((Object)new BeaconBeamOwner.Section(ARGB.color(128, 128, 128)));
    private static final List<BeaconBeamOwner.Section> BEAM_SUCCESS = List.of((Object)new BeaconBeamOwner.Section(ARGB.color(0, 255, 0)));
    private static final List<BeaconBeamOwner.Section> BEAM_REQUIRED_FAILED = List.of((Object)new BeaconBeamOwner.Section(ARGB.color(255, 0, 0)));
    private static final List<BeaconBeamOwner.Section> BEAM_OPTIONAL_FAILED = List.of((Object)new BeaconBeamOwner.Section(ARGB.color(255, 128, 0)));
    private static final Vec3i STRUCTURE_OFFSET = new Vec3i(0, 1, 1);
    private Data data;
    private final List<ErrorMarker> errorMarkers = new ArrayList<ErrorMarker>();

    public TestInstanceBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.TEST_INSTANCE_BLOCK, blockPos, blockState);
        this.data = new Data(Optional.empty(), Vec3i.ZERO, Rotation.NONE, false, Status.CLEARED, Optional.empty());
    }

    public void set(Data data) {
        this.data = data;
        this.setChanged();
    }

    public static Optional<Vec3i> getStructureSize(ServerLevel serverLevel, ResourceKey<GameTestInstance> resourceKey) {
        return TestInstanceBlockEntity.getStructureTemplate(serverLevel, resourceKey).map(StructureTemplate::getSize);
    }

    public BoundingBox getStructureBoundingBox() {
        BlockPos blockPos = this.getStructurePos();
        BlockPos blockPos2 = blockPos.offset(this.getTransformedSize()).offset(-1, -1, -1);
        return BoundingBox.fromCorners(blockPos, blockPos2);
    }

    public AABB getStructureBounds() {
        return AABB.of(this.getStructureBoundingBox());
    }

    private static Optional<StructureTemplate> getStructureTemplate(ServerLevel serverLevel, ResourceKey<GameTestInstance> resourceKey) {
        return serverLevel.registryAccess().get(resourceKey).map(reference -> ((GameTestInstance)reference.value()).structure()).flatMap(identifier -> serverLevel.getStructureManager().get((Identifier)identifier));
    }

    public Optional<ResourceKey<GameTestInstance>> test() {
        return this.data.test();
    }

    public Component getTestName() {
        return this.test().map(resourceKey -> Component.literal(resourceKey.identifier().toString())).orElse(INVALID_TEST_NAME);
    }

    private Optional<Holder.Reference<GameTestInstance>> getTestHolder() {
        return this.test().flatMap(this.level.registryAccess()::get);
    }

    public boolean ignoreEntities() {
        return this.data.ignoreEntities();
    }

    public Vec3i getSize() {
        return this.data.size();
    }

    public Rotation getRotation() {
        return this.getTestHolder().map(Holder::value).map(GameTestInstance::rotation).orElse(Rotation.NONE).getRotated(this.data.rotation());
    }

    public Optional<Component> errorMessage() {
        return this.data.errorMessage();
    }

    public void setErrorMessage(Component component) {
        this.set(this.data.withError(component));
    }

    public void setSuccess() {
        this.set(this.data.withStatus(Status.FINISHED));
    }

    public void setRunning() {
        this.set(this.data.withStatus(Status.RUNNING));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level instanceof ServerLevel) {
            this.level.sendBlockUpdated(this.getBlockPos(), Blocks.AIR.defaultBlockState(), this.getBlockState(), 3);
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        valueInput.read("data", Data.CODEC).ifPresent(this::set);
        this.errorMarkers.clear();
        this.errorMarkers.addAll((Collection<ErrorMarker>)valueInput.read("errors", ErrorMarker.LIST_CODEC).orElse(List.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        valueOutput.store("data", Data.CODEC, this.data);
        if (!this.errorMarkers.isEmpty()) {
            valueOutput.store("errors", ErrorMarker.LIST_CODEC, this.errorMarkers);
        }
    }

    @Override
    public BoundingBoxRenderable.Mode renderMode() {
        return BoundingBoxRenderable.Mode.BOX;
    }

    public BlockPos getStructurePos() {
        return TestInstanceBlockEntity.getStructurePos(this.getBlockPos());
    }

    public static BlockPos getStructurePos(BlockPos blockPos) {
        return blockPos.offset(STRUCTURE_OFFSET);
    }

    @Override
    public BoundingBoxRenderable.RenderableBox getRenderableBox() {
        return new BoundingBoxRenderable.RenderableBox(new BlockPos(STRUCTURE_OFFSET), this.getTransformedSize());
    }

    @Override
    public List<BeaconBeamOwner.Section> getBeamSections() {
        return switch (this.data.status().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> BEAM_CLEARED;
            case 1 -> BEAM_RUNNING;
            case 2 -> this.errorMessage().isEmpty() ? BEAM_SUCCESS : (this.getTestHolder().map(Holder::value).map(GameTestInstance::required).orElse(true) != false ? BEAM_REQUIRED_FAILED : BEAM_OPTIONAL_FAILED);
        };
    }

    private Vec3i getTransformedSize() {
        Vec3i vec3i = this.getSize();
        Rotation rotation = this.getRotation();
        boolean bl = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
        int i = bl ? vec3i.getZ() : vec3i.getX();
        int j = bl ? vec3i.getX() : vec3i.getZ();
        return new Vec3i(i, vec3i.getY(), j);
    }

    public void resetTest(Consumer<Component> consumer) {
        this.removeBarriers();
        this.clearErrorMarkers();
        boolean bl = this.placeStructure();
        if (bl) {
            consumer.accept(Component.translatable("test_instance_block.reset_success", this.getTestName()).withStyle(ChatFormatting.GREEN));
        }
        this.set(this.data.withStatus(Status.CLEARED));
    }

    public Optional<Identifier> saveTest(Consumer<Component> consumer) {
        Optional<Holder.Reference<GameTestInstance>> optional = this.getTestHolder();
        Optional<Identifier> optional2 = optional.isPresent() ? Optional.of(optional.get().value().structure()) : this.test().map(ResourceKey::identifier);
        if (optional2.isEmpty()) {
            BlockPos blockPos = this.getBlockPos();
            consumer.accept(Component.translatable("test_instance_block.error.unable_to_save", blockPos.getX(), blockPos.getY(), blockPos.getZ()).withStyle(ChatFormatting.RED));
            return optional2;
        }
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            StructureBlockEntity.saveStructure(serverLevel, optional2.get(), this.getStructurePos(), this.getSize(), this.ignoreEntities(), "", true, List.of((Object)Blocks.AIR));
        }
        return optional2;
    }

    public boolean exportTest(Consumer<Component> consumer) {
        Level level;
        Optional<Identifier> optional = this.saveTest(consumer);
        if (optional.isEmpty() || !((level = this.level) instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        return TestInstanceBlockEntity.export(serverLevel, optional.get(), consumer);
    }

    public static boolean export(ServerLevel serverLevel, Identifier identifier, Consumer<Component> consumer) {
        Path path = StructureUtils.testStructuresDir;
        Path path2 = serverLevel.getStructureManager().createAndValidatePathToGeneratedStructure(identifier, ".nbt");
        Path path3 = NbtToSnbt.convertStructure(CachedOutput.NO_CACHE, path2, identifier.getPath(), path.resolve(identifier.getNamespace()).resolve("structure"));
        if (path3 == null) {
            consumer.accept(Component.literal("Failed to export " + String.valueOf(path2)).withStyle(ChatFormatting.RED));
            return true;
        }
        try {
            FileUtil.createDirectoriesSafe(path3.getParent());
        }
        catch (IOException iOException) {
            consumer.accept(Component.literal("Could not create folder " + String.valueOf(path3.getParent())).withStyle(ChatFormatting.RED));
            return true;
        }
        consumer.accept(Component.literal("Exported " + String.valueOf(identifier) + " to " + String.valueOf(path3.toAbsolutePath())));
        return false;
    }

    public void runTest(Consumer<Component> consumer) {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        Optional<Holder.Reference<GameTestInstance>> optional = this.getTestHolder();
        BlockPos blockPos = this.getBlockPos();
        if (optional.isEmpty()) {
            consumer.accept(Component.translatable("test_instance_block.error.no_test", blockPos.getX(), blockPos.getY(), blockPos.getZ()).withStyle(ChatFormatting.RED));
            return;
        }
        if (!this.placeStructure()) {
            consumer.accept(Component.translatable("test_instance_block.error.no_test_structure", blockPos.getX(), blockPos.getY(), blockPos.getZ()).withStyle(ChatFormatting.RED));
            return;
        }
        this.clearErrorMarkers();
        GameTestTicker.SINGLETON.clear();
        FailedTestTracker.forgetFailedTests();
        consumer.accept(Component.translatable("test_instance_block.starting", optional.get().getRegisteredName()));
        GameTestInfo gameTestInfo = new GameTestInfo(optional.get(), this.data.rotation(), serverLevel, RetryOptions.noRetries());
        gameTestInfo.setTestBlockPos(blockPos);
        GameTestRunner gameTestRunner = GameTestRunner.Builder.fromInfo(List.of((Object)gameTestInfo), serverLevel).build();
        TestCommand.trackAndStartRunner(serverLevel.getServer().createCommandSourceStack(), gameTestRunner);
    }

    public boolean placeStructure() {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            Optional optional = this.data.test().flatMap(resourceKey -> TestInstanceBlockEntity.getStructureTemplate(serverLevel, resourceKey));
            if (optional.isPresent()) {
                this.placeStructure(serverLevel, (StructureTemplate)optional.get());
                return true;
            }
        }
        return false;
    }

    private void placeStructure(ServerLevel serverLevel, StructureTemplate structureTemplate) {
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setRotation(this.getRotation()).setIgnoreEntities(this.data.ignoreEntities()).setKnownShape(true);
        BlockPos blockPos = this.getStartCorner();
        this.forceLoadChunks();
        StructureUtils.clearSpaceForStructure(this.getStructureBoundingBox(), serverLevel);
        this.removeEntities();
        structureTemplate.placeInWorld(serverLevel, blockPos, blockPos, structurePlaceSettings, serverLevel.getRandom(), 818);
    }

    private void removeEntities() {
        this.level.getEntities(null, this.getStructureBounds()).stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::discard);
    }

    private void forceLoadChunks() {
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.getStructureBoundingBox().intersectingChunks().forEach(chunkPos -> serverLevel.setChunkForced(chunkPos.x, chunkPos.z, true));
        }
    }

    public BlockPos getStartCorner() {
        Vec3i vec3i = this.getSize();
        Rotation rotation = this.getRotation();
        BlockPos blockPos = this.getStructurePos();
        return switch (rotation) {
            default -> throw new MatchException(null, null);
            case Rotation.NONE -> blockPos;
            case Rotation.CLOCKWISE_90 -> blockPos.offset(vec3i.getZ() - 1, 0, 0);
            case Rotation.CLOCKWISE_180 -> blockPos.offset(vec3i.getX() - 1, 0, vec3i.getZ() - 1);
            case Rotation.COUNTERCLOCKWISE_90 -> blockPos.offset(0, 0, vec3i.getX() - 1);
        };
    }

    public void encaseStructure() {
        this.processStructureBoundary(blockPos -> {
            if (!this.level.getBlockState((BlockPos)blockPos).is(Blocks.TEST_INSTANCE_BLOCK)) {
                this.level.setBlockAndUpdate((BlockPos)blockPos, Blocks.BARRIER.defaultBlockState());
            }
        });
    }

    public void removeBarriers() {
        this.processStructureBoundary(blockPos -> {
            if (this.level.getBlockState((BlockPos)blockPos).is(Blocks.BARRIER)) {
                this.level.setBlockAndUpdate((BlockPos)blockPos, Blocks.AIR.defaultBlockState());
            }
        });
    }

    public void processStructureBoundary(Consumer<BlockPos> consumer) {
        AABB aABB = this.getStructureBounds();
        boolean bl = this.getTestHolder().map(reference -> ((GameTestInstance)reference.value()).skyAccess()).orElse(false) == false;
        BlockPos blockPos = BlockPos.containing(aABB.minX, aABB.minY, aABB.minZ).offset(-1, -1, -1);
        BlockPos blockPos2 = BlockPos.containing(aABB.maxX, aABB.maxY, aABB.maxZ);
        BlockPos.betweenClosedStream(blockPos, blockPos2).forEach(blockPos3 -> {
            boolean bl3;
            boolean bl2 = blockPos3.getX() == blockPos.getX() || blockPos3.getX() == blockPos2.getX() || blockPos3.getZ() == blockPos.getZ() || blockPos3.getZ() == blockPos2.getZ() || blockPos3.getY() == blockPos.getY();
            boolean bl4 = bl3 = blockPos3.getY() == blockPos2.getY();
            if (bl2 || bl3 && bl) {
                consumer.accept((BlockPos)blockPos3);
            }
        });
    }

    public void markError(BlockPos blockPos, Component component) {
        this.errorMarkers.add(new ErrorMarker(blockPos, component));
        this.setChanged();
    }

    public void clearErrorMarkers() {
        if (!this.errorMarkers.isEmpty()) {
            this.errorMarkers.clear();
            this.setChanged();
        }
    }

    public List<ErrorMarker> getErrorMarkers() {
        return this.errorMarkers;
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    public record Data(Optional<ResourceKey<GameTestInstance>> test, Vec3i size, Rotation rotation, boolean ignoreEntities, Status status, Optional<Component> errorMessage) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceKey.codec(Registries.TEST_INSTANCE).optionalFieldOf("test").forGetter(Data::test), (App)Vec3i.CODEC.fieldOf("size").forGetter(Data::size), (App)Rotation.CODEC.fieldOf("rotation").forGetter(Data::rotation), (App)Codec.BOOL.fieldOf("ignore_entities").forGetter(Data::ignoreEntities), (App)Status.CODEC.fieldOf("status").forGetter(Data::status), (App)ComponentSerialization.CODEC.optionalFieldOf("error_message").forGetter(Data::errorMessage)).apply((Applicative)instance, Data::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.optional(ResourceKey.streamCodec(Registries.TEST_INSTANCE)), Data::test, Vec3i.STREAM_CODEC, Data::size, Rotation.STREAM_CODEC, Data::rotation, ByteBufCodecs.BOOL, Data::ignoreEntities, Status.STREAM_CODEC, Data::status, ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), Data::errorMessage, Data::new);

        public Data withSize(Vec3i vec3i) {
            return new Data(this.test, vec3i, this.rotation, this.ignoreEntities, this.status, this.errorMessage);
        }

        public Data withStatus(Status status) {
            return new Data(this.test, this.size, this.rotation, this.ignoreEntities, status, Optional.empty());
        }

        public Data withError(Component component) {
            return new Data(this.test, this.size, this.rotation, this.ignoreEntities, Status.FINISHED, Optional.of(component));
        }
    }

    public static enum Status implements StringRepresentable
    {
        CLEARED("cleared", 0),
        RUNNING("running", 1),
        FINISHED("finished", 2);

        private static final IntFunction<Status> ID_MAP;
        public static final Codec<Status> CODEC;
        public static final StreamCodec<ByteBuf, Status> STREAM_CODEC;
        private final String id;
        private final int index;

        private Status(String string2, int j) {
            this.id = string2;
            this.index = j;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        public static Status byIndex(int i) {
            return ID_MAP.apply(i);
        }

        static {
            ID_MAP = ByIdMap.continuous(status -> status.index, Status.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
            CODEC = StringRepresentable.fromEnum(Status::values);
            STREAM_CODEC = ByteBufCodecs.idMapper(Status::byIndex, status -> status.index);
        }
    }

    public record ErrorMarker(BlockPos pos, Component text) {
        public static final Codec<ErrorMarker> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(ErrorMarker::pos), (App)ComponentSerialization.CODEC.fieldOf("text").forGetter(ErrorMarker::text)).apply((Applicative)instance, ErrorMarker::new));
        public static final Codec<List<ErrorMarker>> LIST_CODEC = CODEC.listOf();
    }
}

