package com.ruinedportaloverhaul.world;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.structure.PortalDungeonPiece;
import com.ruinedportaloverhaul.structure.PortalDungeonStructure;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

public final class ModStructures {
    public static final Identifier PORTAL_DUNGEON_ID = id("portal_dungeon");
    public static final Identifier PORTAL_DUNGEON_PIECE_ID = id("portal_dungeon_piece");

    public static final StructureType<PortalDungeonStructure> PORTAL_DUNGEON_TYPE = Registry.register(
        BuiltInRegistries.STRUCTURE_TYPE,
        PORTAL_DUNGEON_ID,
        () -> PortalDungeonStructure.CODEC
    );

    public static final StructurePieceType PORTAL_DUNGEON_PIECE = Registry.register(
        BuiltInRegistries.STRUCTURE_PIECE,
        PORTAL_DUNGEON_PIECE_ID,
        (StructurePieceType.ContextlessType) PortalDungeonPiece::new
    );

    private ModStructures() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered portal dungeon structure hooks");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }
}
