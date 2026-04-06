/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.block.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.IdentifierException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BoundingBoxRenderable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class StructureBlockEntity
extends BlockEntity
implements BoundingBoxRenderable {
    private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
    public static final int MAX_OFFSET_PER_AXIS = 48;
    public static final int MAX_SIZE_PER_AXIS = 48;
    public static final String AUTHOR_TAG = "author";
    private static final String DEFAULT_AUTHOR = "";
    private static final String DEFAULT_METADATA = "";
    private static final BlockPos DEFAULT_POS = new BlockPos(0, 1, 0);
    private static final Vec3i DEFAULT_SIZE = Vec3i.ZERO;
    private static final Rotation DEFAULT_ROTATION = Rotation.NONE;
    private static final Mirror DEFAULT_MIRROR = Mirror.NONE;
    private static final boolean DEFAULT_IGNORE_ENTITIES = true;
    private static final boolean DEFAULT_STRICT = false;
    private static final boolean DEFAULT_POWERED = false;
    private static final boolean DEFAULT_SHOW_AIR = false;
    private static final boolean DEFAULT_SHOW_BOUNDING_BOX = true;
    private static final float DEFAULT_INTEGRITY = 1.0f;
    private static final long DEFAULT_SEED = 0L;
    private @Nullable Identifier structureName;
    private String author = "";
    private String metaData = "";
    private BlockPos structurePos = DEFAULT_POS;
    private Vec3i structureSize = DEFAULT_SIZE;
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private StructureMode mode;
    private boolean ignoreEntities = true;
    private boolean strict = false;
    private boolean powered = false;
    private boolean showAir = false;
    private boolean showBoundingBox = true;
    private float integrity = 1.0f;
    private long seed = 0L;

    public StructureBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.STRUCTURE_BLOCK, blockPos, blockState);
        this.mode = blockState.getValue(StructureBlock.MODE);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putString("name", this.getStructureName());
        valueOutput.putString(AUTHOR_TAG, this.author);
        valueOutput.putString("metadata", this.metaData);
        valueOutput.putInt("posX", this.structurePos.getX());
        valueOutput.putInt("posY", this.structurePos.getY());
        valueOutput.putInt("posZ", this.structurePos.getZ());
        valueOutput.putInt("sizeX", this.structureSize.getX());
        valueOutput.putInt("sizeY", this.structureSize.getY());
        valueOutput.putInt("sizeZ", this.structureSize.getZ());
        valueOutput.store("rotation", Rotation.LEGACY_CODEC, this.rotation);
        valueOutput.store("mirror", Mirror.LEGACY_CODEC, this.mirror);
        valueOutput.store("mode", StructureMode.LEGACY_CODEC, this.mode);
        valueOutput.putBoolean("ignoreEntities", this.ignoreEntities);
        valueOutput.putBoolean("strict", this.strict);
        valueOutput.putBoolean("powered", this.powered);
        valueOutput.putBoolean("showair", this.showAir);
        valueOutput.putBoolean("showboundingbox", this.showBoundingBox);
        valueOutput.putFloat("integrity", this.integrity);
        valueOutput.putLong("seed", this.seed);
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.setStructureName(valueInput.getStringOr("name", ""));
        this.author = valueInput.getStringOr(AUTHOR_TAG, "");
        this.metaData = valueInput.getStringOr("metadata", "");
        int i = Mth.clamp(valueInput.getIntOr("posX", DEFAULT_POS.getX()), -48, 48);
        int j = Mth.clamp(valueInput.getIntOr("posY", DEFAULT_POS.getY()), -48, 48);
        int k = Mth.clamp(valueInput.getIntOr("posZ", DEFAULT_POS.getZ()), -48, 48);
        this.structurePos = new BlockPos(i, j, k);
        int l = Mth.clamp(valueInput.getIntOr("sizeX", DEFAULT_SIZE.getX()), 0, 48);
        int m = Mth.clamp(valueInput.getIntOr("sizeY", DEFAULT_SIZE.getY()), 0, 48);
        int n = Mth.clamp(valueInput.getIntOr("sizeZ", DEFAULT_SIZE.getZ()), 0, 48);
        this.structureSize = new Vec3i(l, m, n);
        this.rotation = valueInput.read("rotation", Rotation.LEGACY_CODEC).orElse(DEFAULT_ROTATION);
        this.mirror = valueInput.read("mirror", Mirror.LEGACY_CODEC).orElse(DEFAULT_MIRROR);
        this.mode = valueInput.read("mode", StructureMode.LEGACY_CODEC).orElse(StructureMode.DATA);
        this.ignoreEntities = valueInput.getBooleanOr("ignoreEntities", true);
        this.strict = valueInput.getBooleanOr("strict", false);
        this.powered = valueInput.getBooleanOr("powered", false);
        this.showAir = valueInput.getBooleanOr("showair", false);
        this.showBoundingBox = valueInput.getBooleanOr("showboundingbox", true);
        this.integrity = valueInput.getFloatOr("integrity", 1.0f);
        this.seed = valueInput.getLongOr("seed", 0L);
        this.updateBlockState();
    }

    private void updateBlockState() {
        if (this.level == null) {
            return;
        }
        BlockPos blockPos = this.getBlockPos();
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(blockPos, (BlockState)blockState.setValue(StructureBlock.MODE, this.mode), 2);
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveCustomOnly(provider);
    }

    public boolean usedBy(Player player) {
        if (!player.canUseGameMasterBlocks()) {
            return false;
        }
        if (player.level().isClientSide()) {
            player.openStructureBlock(this);
        }
        return true;
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String string) {
        this.setStructureName(StringUtil.isNullOrEmpty(string) ? null : Identifier.tryParse(string));
    }

    public void setStructureName(@Nullable Identifier identifier) {
        this.structureName = identifier;
    }

    public void createdBy(LivingEntity livingEntity) {
        this.author = livingEntity.getPlainTextName();
    }

    public BlockPos getStructurePos() {
        return this.structurePos;
    }

    public void setStructurePos(BlockPos blockPos) {
        this.structurePos = blockPos;
    }

    public Vec3i getStructureSize() {
        return this.structureSize;
    }

    public void setStructureSize(Vec3i vec3i) {
        this.structureSize = vec3i;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public void setMirror(Mirror mirror) {
        this.mirror = mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public void setRotation(Rotation rotation) {
        this.rotation = rotation;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String string) {
        this.metaData = string;
    }

    public StructureMode getMode() {
        return this.mode;
    }

    public void setMode(StructureMode structureMode) {
        this.mode = structureMode;
        BlockState blockState = this.level.getBlockState(this.getBlockPos());
        if (blockState.is(Blocks.STRUCTURE_BLOCK)) {
            this.level.setBlock(this.getBlockPos(), (BlockState)blockState.setValue(StructureBlock.MODE, structureMode), 2);
        }
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public boolean isStrict() {
        return this.strict;
    }

    public void setIgnoreEntities(boolean bl) {
        this.ignoreEntities = bl;
    }

    public void setStrict(boolean bl) {
        this.strict = bl;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float f) {
        this.integrity = f;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long l) {
        this.seed = l;
    }

    public boolean detectSize() {
        if (this.mode != StructureMode.SAVE) {
            return false;
        }
        BlockPos blockPos = this.getBlockPos();
        int i = 80;
        BlockPos blockPos2 = new BlockPos(blockPos.getX() - 80, this.level.getMinY(), blockPos.getZ() - 80);
        BlockPos blockPos3 = new BlockPos(blockPos.getX() + 80, this.level.getMaxY(), blockPos.getZ() + 80);
        Stream<BlockPos> stream = this.getRelatedCorners(blockPos2, blockPos3);
        return StructureBlockEntity.calculateEnclosingBoundingBox(blockPos, stream).filter(boundingBox -> {
            int i = boundingBox.maxX() - boundingBox.minX();
            int j = boundingBox.maxY() - boundingBox.minY();
            int k = boundingBox.maxZ() - boundingBox.minZ();
            if (i > 1 && j > 1 && k > 1) {
                this.structurePos = new BlockPos(boundingBox.minX() - blockPos.getX() + 1, boundingBox.minY() - blockPos.getY() + 1, boundingBox.minZ() - blockPos.getZ() + 1);
                this.structureSize = new Vec3i(i - 1, j - 1, k - 1);
                this.setChanged();
                BlockState blockState = this.level.getBlockState(blockPos);
                this.level.sendBlockUpdated(blockPos, blockState, blockState, 3);
                return true;
            }
            return false;
        }).isPresent();
    }

    private Stream<BlockPos> getRelatedCorners(BlockPos blockPos2, BlockPos blockPos22) {
        return BlockPos.betweenClosedStream(blockPos2, blockPos22).filter(blockPos -> this.level.getBlockState((BlockPos)blockPos).is(Blocks.STRUCTURE_BLOCK)).map(this.level::getBlockEntity).filter(blockEntity -> blockEntity instanceof StructureBlockEntity).map(blockEntity -> (StructureBlockEntity)blockEntity).filter(structureBlockEntity -> structureBlockEntity.mode == StructureMode.CORNER && Objects.equals(this.structureName, structureBlockEntity.structureName)).map(BlockEntity::getBlockPos);
    }

    private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos blockPos, Stream<BlockPos> stream) {
        Iterator iterator = stream.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }
        BlockPos blockPos2 = (BlockPos)iterator.next();
        BoundingBox boundingBox = new BoundingBox(blockPos2);
        if (iterator.hasNext()) {
            iterator.forEachRemaining(boundingBox::encapsulate);
        } else {
            boundingBox.encapsulate(blockPos);
        }
        return Optional.of(boundingBox);
    }

    public boolean saveStructure() {
        if (this.mode != StructureMode.SAVE) {
            return false;
        }
        return this.saveStructure(true);
    }

    public boolean saveStructure(boolean bl) {
        Level level;
        if (this.structureName == null || !((level = this.level) instanceof ServerLevel)) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        BlockPos blockPos = this.getBlockPos().offset(this.structurePos);
        return StructureBlockEntity.saveStructure(serverLevel, this.structureName, blockPos, this.structureSize, this.ignoreEntities, this.author, bl, List.of());
    }

    public static boolean saveStructure(ServerLevel serverLevel, Identifier identifier, BlockPos blockPos, Vec3i vec3i, boolean bl, String string, boolean bl2, List<Block> list) {
        StructureTemplate structureTemplate;
        StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
        try {
            structureTemplate = structureTemplateManager.getOrCreate(identifier);
        }
        catch (IdentifierException identifierException) {
            return false;
        }
        structureTemplate.fillFromWorld(serverLevel, blockPos, vec3i, !bl, Stream.concat(list.stream(), Stream.of(Blocks.STRUCTURE_VOID)).toList());
        structureTemplate.setAuthor(string);
        if (bl2) {
            try {
                return structureTemplateManager.save(identifier);
            }
            catch (IdentifierException identifierException) {
                return false;
            }
        }
        return true;
    }

    public static RandomSource createRandom(long l) {
        if (l == 0L) {
            return RandomSource.create(Util.getMillis());
        }
        return RandomSource.create(l);
    }

    public boolean placeStructureIfSameSize(ServerLevel serverLevel) {
        if (this.mode != StructureMode.LOAD || this.structureName == null) {
            return false;
        }
        StructureTemplate structureTemplate = serverLevel.getStructureManager().get(this.structureName).orElse(null);
        if (structureTemplate == null) {
            return false;
        }
        if (structureTemplate.getSize().equals(this.structureSize)) {
            this.placeStructure(serverLevel, structureTemplate);
            return true;
        }
        this.loadStructureInfo(structureTemplate);
        return false;
    }

    public boolean loadStructureInfo(ServerLevel serverLevel) {
        StructureTemplate structureTemplate = this.getStructureTemplate(serverLevel);
        if (structureTemplate == null) {
            return false;
        }
        this.loadStructureInfo(structureTemplate);
        return true;
    }

    private void loadStructureInfo(StructureTemplate structureTemplate) {
        this.author = !StringUtil.isNullOrEmpty(structureTemplate.getAuthor()) ? structureTemplate.getAuthor() : "";
        this.structureSize = structureTemplate.getSize();
        this.setChanged();
    }

    public void placeStructure(ServerLevel serverLevel) {
        StructureTemplate structureTemplate = this.getStructureTemplate(serverLevel);
        if (structureTemplate != null) {
            this.placeStructure(serverLevel, structureTemplate);
        }
    }

    private @Nullable StructureTemplate getStructureTemplate(ServerLevel serverLevel) {
        if (this.structureName == null) {
            return null;
        }
        return serverLevel.getStructureManager().get(this.structureName).orElse(null);
    }

    private void placeStructure(ServerLevel serverLevel, StructureTemplate structureTemplate) {
        this.loadStructureInfo(structureTemplate);
        StructurePlaceSettings structurePlaceSettings = new StructurePlaceSettings().setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities).setKnownShape(this.strict);
        if (this.integrity < 1.0f) {
            structurePlaceSettings.clearProcessors().addProcessor(new BlockRotProcessor(Mth.clamp(this.integrity, 0.0f, 1.0f))).setRandom(StructureBlockEntity.createRandom(this.seed));
        }
        BlockPos blockPos2 = this.getBlockPos().offset(this.structurePos);
        if (SharedConstants.DEBUG_STRUCTURE_EDIT_MODE) {
            BlockPos.betweenClosed(blockPos2, blockPos2.offset(this.structureSize)).forEach(blockPos -> serverLevel.setBlock((BlockPos)blockPos, Blocks.STRUCTURE_VOID.defaultBlockState(), 2));
        }
        structureTemplate.placeInWorld(serverLevel, blockPos2, blockPos2, structurePlaceSettings, StructureBlockEntity.createRandom(this.seed), 2 | (this.strict ? 816 : 0));
    }

    public void unloadStructure() {
        if (this.structureName == null) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)this.level;
        StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
        structureTemplateManager.remove(this.structureName);
    }

    public boolean isStructureLoadable() {
        if (this.mode != StructureMode.LOAD || this.level.isClientSide() || this.structureName == null) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)this.level;
        StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
        try {
            return structureTemplateManager.get(this.structureName).isPresent();
        }
        catch (IdentifierException identifierException) {
            return false;
        }
    }

    public boolean isPowered() {
        return this.powered;
    }

    public void setPowered(boolean bl) {
        this.powered = bl;
    }

    public boolean getShowAir() {
        return this.showAir;
    }

    public void setShowAir(boolean bl) {
        this.showAir = bl;
    }

    public boolean getShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean bl) {
        this.showBoundingBox = bl;
    }

    @Override
    public BoundingBoxRenderable.Mode renderMode() {
        if (this.mode != StructureMode.SAVE && this.mode != StructureMode.LOAD) {
            return BoundingBoxRenderable.Mode.NONE;
        }
        if (this.mode == StructureMode.SAVE && this.showAir) {
            return BoundingBoxRenderable.Mode.BOX_AND_INVISIBLE_BLOCKS;
        }
        if (this.mode == StructureMode.SAVE || this.showBoundingBox) {
            return BoundingBoxRenderable.Mode.BOX;
        }
        return BoundingBoxRenderable.Mode.NONE;
    }

    @Override
    public BoundingBoxRenderable.RenderableBox getRenderableBox() {
        int q;
        int p;
        int o;
        int m;
        BlockPos blockPos = this.getStructurePos();
        Vec3i vec3i = this.getStructureSize();
        int i = blockPos.getX();
        int j = blockPos.getZ();
        int k = blockPos.getY();
        int l = k + vec3i.getY();
        return BoundingBoxRenderable.RenderableBox.fromCorners(o, k, p, q, l, switch (this.rotation) {
            case Rotation.CLOCKWISE_90 -> {
                o = (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? i : i + 1;
                p = m < 0 ? j + 1 : j;
                q = o - (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
                yield p + m;
            }
            case Rotation.CLOCKWISE_180 -> {
                o = m < 0 ? i : i + 1;
                p = (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? j : j + 1;
                q = o - m;
                yield p - (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
            }
            case Rotation.COUNTERCLOCKWISE_90 -> {
                o = (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? i + 1 : i;
                p = m < 0 ? j : j + 1;
                q = o + (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
                yield p - m;
            }
            default -> {
                o = m < 0 ? i + 1 : i;
                p = (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                }) < 0 ? j + 1 : j;
                q = o + m;
                yield p + (switch (this.mirror) {
                    case Mirror.LEFT_RIGHT -> {
                        m = vec3i.getX();
                        yield -vec3i.getZ();
                    }
                    case Mirror.FRONT_BACK -> {
                        m = -vec3i.getX();
                        yield vec3i.getZ();
                    }
                    default -> {
                        m = vec3i.getX();
                        yield vec3i.getZ();
                    }
                });
            }
        });
    }

    public /* synthetic */ Packet getUpdatePacket() {
        return this.getUpdatePacket();
    }

    public static enum UpdateType {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA;

    }
}

