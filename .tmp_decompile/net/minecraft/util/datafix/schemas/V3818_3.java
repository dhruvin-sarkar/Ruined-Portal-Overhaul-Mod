/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  java.util.SequencedMap
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V3818_3
extends NamespacedSchema {
    public V3818_3(int i, Schema schema) {
        super(i, schema);
    }

    public static SequencedMap<String, Supplier<TypeTemplate>> components(Schema schema) {
        LinkedHashMap sequencedMap = new LinkedHashMap();
        sequencedMap.put("minecraft:bees", () -> DSL.list((TypeTemplate)DSL.optionalFields((String)"entity_data", (TypeTemplate)References.ENTITY_TREE.in(schema))));
        sequencedMap.put("minecraft:block_entity_data", () -> References.BLOCK_ENTITY.in(schema));
        sequencedMap.put("minecraft:bundle_contents", () -> DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)));
        sequencedMap.put("minecraft:can_break", () -> DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema)))))));
        sequencedMap.put("minecraft:can_place_on", () -> DSL.optionalFields((String)"predicates", (TypeTemplate)DSL.list((TypeTemplate)DSL.optionalFields((String)"blocks", (TypeTemplate)DSL.or((TypeTemplate)References.BLOCK_NAME.in(schema), (TypeTemplate)DSL.list((TypeTemplate)References.BLOCK_NAME.in(schema)))))));
        sequencedMap.put("minecraft:charged_projectiles", () -> DSL.list((TypeTemplate)References.ITEM_STACK.in(schema)));
        sequencedMap.put("minecraft:container", () -> DSL.list((TypeTemplate)DSL.optionalFields((String)"item", (TypeTemplate)References.ITEM_STACK.in(schema))));
        sequencedMap.put("minecraft:entity_data", () -> References.ENTITY_TREE.in(schema));
        sequencedMap.put("minecraft:pot_decorations", () -> DSL.list((TypeTemplate)References.ITEM_NAME.in(schema)));
        sequencedMap.put("minecraft:food", () -> DSL.optionalFields((String)"using_converts_to", (TypeTemplate)References.ITEM_STACK.in(schema)));
        sequencedMap.put("minecraft:custom_name", () -> References.TEXT_COMPONENT.in(schema));
        sequencedMap.put("minecraft:item_name", () -> References.TEXT_COMPONENT.in(schema));
        sequencedMap.put("minecraft:lore", () -> DSL.list((TypeTemplate)References.TEXT_COMPONENT.in(schema)));
        sequencedMap.put("minecraft:written_book_content", () -> DSL.optionalFields((String)"pages", (TypeTemplate)DSL.list((TypeTemplate)DSL.or((TypeTemplate)DSL.optionalFields((String)"raw", (TypeTemplate)References.TEXT_COMPONENT.in(schema), (String)"filtered", (TypeTemplate)References.TEXT_COMPONENT.in(schema)), (TypeTemplate)References.TEXT_COMPONENT.in(schema)))));
        return sequencedMap;
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.DATA_COMPONENTS, () -> DSL.optionalFieldsLazy(V3818_3.components(schema)));
    }
}

