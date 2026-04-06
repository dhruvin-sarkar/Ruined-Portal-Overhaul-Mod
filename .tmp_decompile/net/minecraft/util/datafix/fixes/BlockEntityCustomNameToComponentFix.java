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
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BlockEntityCustomNameToComponentFix
extends DataFix {
    private static final Set<String> NAMEABLE_BLOCK_ENTITIES = Set.of((Object[])new String[]{"minecraft:beacon", "minecraft:banner", "minecraft:brewing_stand", "minecraft:chest", "minecraft:trapped_chest", "minecraft:dispenser", "minecraft:dropper", "minecraft:enchanting_table", "minecraft:furnace", "minecraft:hopper", "minecraft:shulker_box"});

    public BlockEntityCustomNameToComponentFix(Schema schema) {
        super(schema, true);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", NamespacedSchema.namespacedString());
        Type type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type type2 = this.getOutputSchema().getType(References.BLOCK_ENTITY);
        Type<?> type3 = ExtraDataFixUtils.patchSubType(type, type, type2);
        return this.fixTypeEverywhereTyped("BlockEntityCustomNameToComponentFix", type, type2, typed -> {
            Optional optional = typed.getOptional(opticFinder);
            if (optional.isPresent() && !NAMEABLE_BLOCK_ENTITIES.contains(optional.get())) {
                return ExtraDataFixUtils.cast(type2, typed);
            }
            return Util.writeAndReadTypedOrThrow(ExtraDataFixUtils.cast(type3, typed), type2, BlockEntityCustomNameToComponentFix::fixTagCustomName);
        });
    }

    public static <T> Dynamic<T> fixTagCustomName(Dynamic<T> dynamic) {
        String string = dynamic.get("CustomName").asString("");
        if (string.isEmpty()) {
            return dynamic.remove("CustomName");
        }
        return dynamic.set("CustomName", LegacyComponentDataFixUtils.createPlainTextComponent(dynamic.getOps(), string));
    }
}

