/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class BannerEntityCustomNameToOverrideComponentFix
extends DataFix {
    public BannerEntityCustomNameToOverrideComponentFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder opticFinder = type.findField("CustomName");
        OpticFinder opticFinder2 = DSL.typeFinder((Type)this.getInputSchema().getType(References.TEXT_COMPONENT));
        return this.fixTypeEverywhereTyped("Banner entity custom_name to item_name component fix", type, typed -> {
            Object object = ((Pair)typed.get(taggedChoiceType.finder())).getFirst();
            return object.equals("minecraft:banner") ? this.fix((Typed<?>)typed, (OpticFinder<Pair<String, String>>)opticFinder2, (OpticFinder<?>)opticFinder) : typed;
        });
    }

    private Typed<?> fix(Typed<?> typed2, OpticFinder<Pair<String, String>> opticFinder, OpticFinder<?> opticFinder2) {
        Optional optional = typed2.getOptionalTyped(opticFinder2).flatMap(typed -> typed.getOptional(opticFinder).map(Pair::getSecond));
        boolean bl = optional.flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter(string -> string.equals("block.minecraft.ominous_banner")).isPresent();
        if (bl) {
            return Util.writeAndReadTypedOrThrow(typed2, typed2.getType(), dynamic -> {
                Dynamic dynamic2 = dynamic.createMap(Map.of((Object)dynamic.createString("minecraft:item_name"), (Object)dynamic.createString((String)optional.get()), (Object)dynamic.createString("minecraft:hide_additional_tooltip"), (Object)dynamic.emptyMap()));
                return dynamic.set("components", dynamic2).remove("CustomName");
            });
        }
        return typed2;
    }
}

