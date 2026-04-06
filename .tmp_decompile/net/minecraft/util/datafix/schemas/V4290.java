/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TypeTemplate
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V4290
extends NamespacedSchema {
    public V4290(int i, Schema schema) {
        super(i, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(true, References.TEXT_COMPONENT, () -> DSL.or((TypeTemplate)DSL.or((TypeTemplate)DSL.constType((Type)DSL.string()), (TypeTemplate)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in(schema))), (TypeTemplate)DSL.optionalFields((String)"extra", (TypeTemplate)DSL.list((TypeTemplate)References.TEXT_COMPONENT.in(schema)), (String)"separator", (TypeTemplate)References.TEXT_COMPONENT.in(schema), (String)"hoverEvent", (TypeTemplate)DSL.taggedChoice((String)"action", (Type)DSL.string(), (Map)Map.of((Object)"show_text", (Object)DSL.optionalFields((String)"contents", (TypeTemplate)References.TEXT_COMPONENT.in(schema)), (Object)"show_item", (Object)DSL.optionalFields((String)"contents", (TypeTemplate)DSL.or((TypeTemplate)References.ITEM_STACK.in(schema), (TypeTemplate)References.ITEM_NAME.in(schema))), (Object)"show_entity", (Object)DSL.optionalFields((String)"type", (TypeTemplate)References.ENTITY_NAME.in(schema), (String)"name", (TypeTemplate)References.TEXT_COMPONENT.in(schema)))))));
    }
}

