/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BoatSplitFix
extends DataFix {
    public BoatSplitFix(Schema schema) {
        super(schema, true);
    }

    private static boolean isNormalBoat(String string) {
        return string.equals("minecraft:boat");
    }

    private static boolean isChestBoat(String string) {
        return string.equals("minecraft:chest_boat");
    }

    private static boolean isAnyBoat(String string) {
        return BoatSplitFix.isNormalBoat(string) || BoatSplitFix.isChestBoat(string);
    }

    private static String mapVariantToNormalBoat(String string) {
        return switch (string) {
            default -> "minecraft:oak_boat";
            case "spruce" -> "minecraft:spruce_boat";
            case "birch" -> "minecraft:birch_boat";
            case "jungle" -> "minecraft:jungle_boat";
            case "acacia" -> "minecraft:acacia_boat";
            case "cherry" -> "minecraft:cherry_boat";
            case "dark_oak" -> "minecraft:dark_oak_boat";
            case "mangrove" -> "minecraft:mangrove_boat";
            case "bamboo" -> "minecraft:bamboo_raft";
        };
    }

    private static String mapVariantToChestBoat(String string) {
        return switch (string) {
            default -> "minecraft:oak_chest_boat";
            case "spruce" -> "minecraft:spruce_chest_boat";
            case "birch" -> "minecraft:birch_chest_boat";
            case "jungle" -> "minecraft:jungle_chest_boat";
            case "acacia" -> "minecraft:acacia_chest_boat";
            case "cherry" -> "minecraft:cherry_chest_boat";
            case "dark_oak" -> "minecraft:dark_oak_chest_boat";
            case "mangrove" -> "minecraft:mangrove_chest_boat";
            case "bamboo" -> "minecraft:bamboo_chest_raft";
        };
    }

    public TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        Type type = this.getInputSchema().getType(References.ENTITY);
        Type type2 = this.getOutputSchema().getType(References.ENTITY);
        return this.fixTypeEverywhereTyped("BoatSplitFix", type, type2, typed -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && BoatSplitFix.isAnyBoat((String)optional.get())) {
                Dynamic dynamic2 = (Dynamic)typed.getOrCreate(DSL.remainderFinder());
                Optional optional2 = dynamic2.get("Type").asString().result();
                String string = BoatSplitFix.isChestBoat((String)optional.get()) ? optional2.map(BoatSplitFix::mapVariantToChestBoat).orElse("minecraft:oak_chest_boat") : optional2.map(BoatSplitFix::mapVariantToNormalBoat).orElse("minecraft:oak_boat");
                return ExtraDataFixUtils.cast(type2, typed).update(DSL.remainderFinder(), dynamic -> dynamic.remove("Type")).set(opticFinder, (Object)string);
            }
            return ExtraDataFixUtils.cast(type2, typed);
        });
    }
}

