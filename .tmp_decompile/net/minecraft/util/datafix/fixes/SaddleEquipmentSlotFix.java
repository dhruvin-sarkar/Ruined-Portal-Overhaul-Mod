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
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class SaddleEquipmentSlotFix
extends DataFix {
    private static final Set<String> ENTITIES_WITH_SADDLE_ITEM = Set.of((Object)"minecraft:horse", (Object)"minecraft:skeleton_horse", (Object)"minecraft:zombie_horse", (Object)"minecraft:donkey", (Object)"minecraft:mule", (Object)"minecraft:camel", (Object)"minecraft:llama", (Object)"minecraft:trader_llama");
    private static final Set<String> ENTITIES_WITH_SADDLE_FLAG = Set.of((Object)"minecraft:pig", (Object)"minecraft:strider");
    private static final String SADDLE_FLAG = "Saddle";
    private static final String NEW_SADDLE = "saddle";

    public SaddleEquipmentSlotFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(References.ENTITY);
        OpticFinder opticFinder = DSL.typeFinder((Type)taggedChoiceType);
        Type type = this.getInputSchema().getType(References.ENTITY);
        Type type2 = this.getOutputSchema().getType(References.ENTITY);
        Type<?> type3 = ExtraDataFixUtils.patchSubType(type, type, type2);
        return this.fixTypeEverywhereTyped("SaddleEquipmentSlotFix", type, type2, typed -> {
            String string = typed.getOptional(opticFinder).map(Pair::getFirst).map(NamespacedSchema::ensureNamespaced).orElse("");
            Typed typed2 = ExtraDataFixUtils.cast(type3, typed);
            if (ENTITIES_WITH_SADDLE_ITEM.contains(string)) {
                return Util.writeAndReadTypedOrThrow(typed2, type2, SaddleEquipmentSlotFix::fixEntityWithSaddleItem);
            }
            if (ENTITIES_WITH_SADDLE_FLAG.contains(string)) {
                return Util.writeAndReadTypedOrThrow(typed2, type2, SaddleEquipmentSlotFix::fixEntityWithSaddleFlag);
            }
            return ExtraDataFixUtils.cast(type2, typed);
        });
    }

    private static Dynamic<?> fixEntityWithSaddleItem(Dynamic<?> dynamic) {
        if (dynamic.get("SaddleItem").result().isEmpty()) {
            return dynamic;
        }
        return SaddleEquipmentSlotFix.fixDropChances(dynamic.renameField("SaddleItem", NEW_SADDLE));
    }

    private static Dynamic<?> fixEntityWithSaddleFlag(Dynamic<?> dynamic) {
        boolean bl = dynamic.get(SADDLE_FLAG).asBoolean(false);
        dynamic = dynamic.remove(SADDLE_FLAG);
        if (!bl) {
            return dynamic;
        }
        Dynamic dynamic2 = dynamic.emptyMap().set("id", dynamic.createString("minecraft:saddle")).set("count", dynamic.createInt(1));
        return SaddleEquipmentSlotFix.fixDropChances(dynamic.set(NEW_SADDLE, dynamic2));
    }

    private static Dynamic<?> fixDropChances(Dynamic<?> dynamic) {
        Dynamic dynamic2 = dynamic.get("drop_chances").orElseEmptyMap().set(NEW_SADDLE, dynamic.createFloat(2.0f));
        return dynamic.set("drop_chances", dynamic2);
    }
}

