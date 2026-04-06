/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public abstract class TemplateStructurePiece
extends StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected final String templateName;
    protected StructureTemplate template;
    protected StructurePlaceSettings placeSettings;
    protected BlockPos templatePosition;

    public TemplateStructurePiece(StructurePieceType structurePieceType, int i, StructureTemplateManager structureTemplateManager, Identifier identifier, String string, StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        super(structurePieceType, i, structureTemplateManager.getOrCreate(identifier).getBoundingBox(structurePlaceSettings, blockPos));
        this.setOrientation(Direction.NORTH);
        this.templateName = string;
        this.templatePosition = blockPos;
        this.template = structureTemplateManager.getOrCreate(identifier);
        this.placeSettings = structurePlaceSettings;
    }

    public TemplateStructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag, StructureTemplateManager structureTemplateManager, Function<Identifier, StructurePlaceSettings> function) {
        super(structurePieceType, compoundTag);
        this.setOrientation(Direction.NORTH);
        this.templateName = compoundTag.getStringOr("Template", "");
        this.templatePosition = new BlockPos(compoundTag.getIntOr("TPX", 0), compoundTag.getIntOr("TPY", 0), compoundTag.getIntOr("TPZ", 0));
        Identifier identifier = this.makeTemplateLocation();
        this.template = structureTemplateManager.getOrCreate(identifier);
        this.placeSettings = function.apply(identifier);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
    }

    protected Identifier makeTemplateLocation() {
        return Identifier.parse(this.templateName);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        compoundTag.putInt("TPX", this.templatePosition.getX());
        compoundTag.putInt("TPY", this.templatePosition.getY());
        compoundTag.putInt("TPZ", this.templatePosition.getZ());
        compoundTag.putString("Template", this.templateName);
    }

    @Override
    public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
        this.placeSettings.setBoundingBox(boundingBox);
        this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
        if (this.template.placeInWorld(worldGenLevel, this.templatePosition, blockPos, this.placeSettings, randomSource, 2)) {
            List<StructureTemplate.StructureBlockInfo> list = this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.STRUCTURE_BLOCK);
            for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
                StructureMode structureMode;
                if (structureBlockInfo.nbt() == null || (structureMode = (StructureMode)structureBlockInfo.nbt().read("mode", StructureMode.LEGACY_CODEC).orElseThrow()) != StructureMode.DATA) continue;
                this.handleDataMarker(structureBlockInfo.nbt().getStringOr("metadata", ""), structureBlockInfo.pos(), worldGenLevel, randomSource, boundingBox);
            }
            List<StructureTemplate.StructureBlockInfo> list2 = this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.JIGSAW);
            for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : list2) {
                if (structureBlockInfo2.nbt() == null) continue;
                String string = structureBlockInfo2.nbt().getStringOr("final_state", "minecraft:air");
                BlockState blockState = Blocks.AIR.defaultBlockState();
                try {
                    blockState = BlockStateParser.parseForBlock(worldGenLevel.holderLookup(Registries.BLOCK), string, true).blockState();
                }
                catch (CommandSyntaxException commandSyntaxException) {
                    LOGGER.error("Error while parsing blockstate {} in jigsaw block @ {}", (Object)string, (Object)structureBlockInfo2.pos());
                }
                worldGenLevel.setBlock(structureBlockInfo2.pos(), blockState, 3);
            }
        }
    }

    protected abstract void handleDataMarker(String var1, BlockPos var2, ServerLevelAccessor var3, RandomSource var4, BoundingBox var5);

    @Override
    @Deprecated
    public void move(int i, int j, int k) {
        super.move(i, j, k);
        this.templatePosition = this.templatePosition.offset(i, j, k);
    }

    @Override
    public Rotation getRotation() {
        return this.placeSettings.getRotation();
    }

    public StructureTemplate template() {
        return this.template;
    }

    public BlockPos templatePosition() {
        return this.templatePosition;
    }

    public StructurePlaceSettings placeSettings() {
        return this.placeSettings;
    }
}

