/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.BlockEntitySignDoubleSidedEditableTextFix;
import net.minecraft.util.datafix.fixes.References;

public class DropInvalidSignDataFix
extends DataFix {
    private final String entityName;

    public DropInvalidSignDataFix(Schema schema, String string) {
        super(schema, false);
        this.entityName = string;
    }

    private <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        dynamic = dynamic.update("front_text", DropInvalidSignDataFix::fixText);
        dynamic = dynamic.update("back_text", DropInvalidSignDataFix::fixText);
        for (String string : BlockEntitySignDoubleSidedEditableTextFix.FIELDS_TO_DROP) {
            dynamic = dynamic.remove(string);
        }
        return dynamic;
    }

    private static <T> Dynamic<T> fixText(Dynamic<T> dynamic) {
        Optional optional = dynamic.get("filtered_messages").asStreamOpt().result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        Dynamic dynamic22 = LegacyComponentDataFixUtils.createEmptyComponent(dynamic.getOps());
        List list = dynamic.get("messages").asStreamOpt().result().orElse(Stream.of(new Dynamic[0])).toList();
        List list2 = Streams.mapWithIndex((Stream)((Stream)optional.get()), (dynamic2, l) -> {
            Dynamic dynamic3 = l < (long)list.size() ? (Dynamic)list.get((int)l) : dynamic22;
            return dynamic2.equals((Object)dynamic22) ? dynamic3 : dynamic2;
        }).toList();
        if (list2.equals(list)) {
            return dynamic.remove("filtered_messages");
        }
        return dynamic.set("filtered_messages", dynamic.createList(list2.stream()));
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type type2 = this.getInputSchema().getChoiceType(References.BLOCK_ENTITY, this.entityName);
        OpticFinder opticFinder = DSL.namedChoice((String)this.entityName, (Type)type2);
        return this.fixTypeEverywhereTyped("DropInvalidSignDataFix for " + this.entityName, type, typed2 -> typed2.updateTyped(opticFinder, type2, typed -> {
            boolean bl = ((Dynamic)typed.get(DSL.remainderFinder())).get("_filtered_correct").asBoolean(false);
            if (bl) {
                return typed.update(DSL.remainderFinder(), dynamic -> dynamic.remove("_filtered_correct"));
            }
            return Util.writeAndReadTypedOrThrow(typed, type2, this::fix);
        }));
    }
}

