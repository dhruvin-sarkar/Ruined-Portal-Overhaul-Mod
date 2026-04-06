/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonElement;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;

public class LegacyHoverEventFix
extends DataFix {
    public LegacyHoverEventFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.TEXT_COMPONENT).findFieldType("hoverEvent");
        return this.createFixer(this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT), type);
    }

    private <C, H extends Pair<String, ?>> TypeRewriteRule createFixer(Type<C> type, Type<H> type2) {
        Type type3 = DSL.named((String)References.TEXT_COMPONENT.typeName(), (Type)DSL.or((Type)DSL.or((Type)DSL.string(), (Type)DSL.list(type)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"extra", (Type)DSL.list(type))), (Type)DSL.optional((Type)DSL.field((String)"separator", type)), (Type)DSL.optional((Type)DSL.field((String)"hoverEvent", type2)), (Type)DSL.remainderType())));
        if (!type3.equals((Object)this.getInputSchema().getType(References.TEXT_COMPONENT))) {
            throw new IllegalStateException("Text component type did not match, expected " + String.valueOf(type3) + " but got " + String.valueOf(this.getInputSchema().getType(References.TEXT_COMPONENT)));
        }
        return this.fixTypeEverywhere("LegacyHoverEventFix", type3, dynamicOps -> pair -> pair.mapSecond(either -> either.mapRight(pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> {
            Dynamic dynamic = (Dynamic)pair.getSecond();
            Optional optional = dynamic.get("hoverEvent").result();
            if (optional.isEmpty()) {
                return pair;
            }
            Optional optional2 = ((Dynamic)optional.get()).get("value").result();
            if (optional2.isEmpty()) {
                return pair;
            }
            String string = ((Either)pair.getFirst()).left().map(Pair::getFirst).orElse("");
            Pair pair2 = (Pair)this.fixHoverEvent(type2, string, (Dynamic)optional.get());
            return pair.mapFirst(either -> Either.left((Object)pair2));
        })))));
    }

    private <H> H fixHoverEvent(Type<H> type, String string, Dynamic<?> dynamic) {
        if ("show_text".equals(string)) {
            return LegacyHoverEventFix.fixShowTextHover(type, dynamic);
        }
        return LegacyHoverEventFix.createPlaceholderHover(type, dynamic);
    }

    private static <H> H fixShowTextHover(Type<H> type, Dynamic<?> dynamic) {
        Dynamic dynamic2 = dynamic.renameField("value", "contents");
        return (H)Util.readTypedOrThrow(type, dynamic2).getValue();
    }

    private static <H> H createPlaceholderHover(Type<H> type, Dynamic<?> dynamic) {
        JsonElement jsonElement = (JsonElement)dynamic.convert((DynamicOps)JsonOps.INSTANCE).getValue();
        Dynamic dynamic2 = new Dynamic((DynamicOps)JavaOps.INSTANCE, (Object)Map.of((Object)"action", (Object)"show_text", (Object)"contents", (Object)Map.of((Object)"text", (Object)("Legacy hoverEvent: " + GsonHelper.toStableString(jsonElement)))));
        return (H)Util.readTypedOrThrow(type, dynamic2).getValue();
    }
}

