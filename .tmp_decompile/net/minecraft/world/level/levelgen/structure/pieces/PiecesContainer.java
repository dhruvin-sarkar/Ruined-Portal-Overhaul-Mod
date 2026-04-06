/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.pieces;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import org.slf4j.Logger;

public record PiecesContainer(List<StructurePiece> pieces) {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier JIGSAW_RENAME = Identifier.withDefaultNamespace("jigsaw");
    private static final Map<Identifier, Identifier> RENAMES = ImmutableMap.builder().put((Object)Identifier.withDefaultNamespace("nvi"), (Object)JIGSAW_RENAME).put((Object)Identifier.withDefaultNamespace("pcp"), (Object)JIGSAW_RENAME).put((Object)Identifier.withDefaultNamespace("bastionremnant"), (Object)JIGSAW_RENAME).put((Object)Identifier.withDefaultNamespace("runtime"), (Object)JIGSAW_RENAME).build();

    public PiecesContainer(List<StructurePiece> list) {
        this.pieces = List.copyOf(list);
    }

    public boolean isEmpty() {
        return this.pieces.isEmpty();
    }

    public boolean isInsidePiece(BlockPos blockPos) {
        for (StructurePiece structurePiece : this.pieces) {
            if (!structurePiece.getBoundingBox().isInside(blockPos)) continue;
            return true;
        }
        return false;
    }

    public Tag save(StructurePieceSerializationContext structurePieceSerializationContext) {
        ListTag listTag = new ListTag();
        for (StructurePiece structurePiece : this.pieces) {
            listTag.add(structurePiece.createTag(structurePieceSerializationContext));
        }
        return listTag;
    }

    public static PiecesContainer load(ListTag listTag, StructurePieceSerializationContext structurePieceSerializationContext) {
        ArrayList list = Lists.newArrayList();
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompoundOrEmpty(i);
            String string = compoundTag.getStringOr("id", "").toLowerCase(Locale.ROOT);
            Identifier identifier = Identifier.parse(string);
            Identifier identifier2 = RENAMES.getOrDefault(identifier, identifier);
            StructurePieceType structurePieceType = BuiltInRegistries.STRUCTURE_PIECE.getValue(identifier2);
            if (structurePieceType == null) {
                LOGGER.error("Unknown structure piece id: {}", (Object)identifier2);
                continue;
            }
            try {
                StructurePiece structurePiece = structurePieceType.load(structurePieceSerializationContext, compoundTag);
                list.add(structurePiece);
                continue;
            }
            catch (Exception exception) {
                LOGGER.error("Exception loading structure piece with id {}", (Object)identifier2, (Object)exception);
            }
        }
        return new PiecesContainer(list);
    }

    public BoundingBox calculateBoundingBox() {
        return StructurePiece.createBoundingBox(this.pieces.stream());
    }
}

