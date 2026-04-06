/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;

public class TextComponentHoverAndClickEventFix
extends DataFix {
    public TextComponentHoverAndClickEventFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.TEXT_COMPONENT).findFieldType("hoverEvent");
        return this.createFixer(this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT), this.getOutputSchema().getType(References.TEXT_COMPONENT), type);
    }

    private <C1, C2, H extends Pair<String, ?>> TypeRewriteRule createFixer(Type<C1> type, Type<C2> type2, Type<H> type3) {
        Type type4 = DSL.named((String)References.TEXT_COMPONENT.typeName(), (Type)DSL.or((Type)DSL.or((Type)DSL.string(), (Type)DSL.list(type)), (Type)DSL.and((Type)DSL.optional((Type)DSL.field((String)"extra", (Type)DSL.list(type))), (Type)DSL.optional((Type)DSL.field((String)"separator", type)), (Type)DSL.optional((Type)DSL.field((String)"hoverEvent", type3)), (Type)DSL.remainderType())));
        if (!type4.equals((Object)this.getInputSchema().getType(References.TEXT_COMPONENT))) {
            throw new IllegalStateException("Text component type did not match, expected " + String.valueOf(type4) + " but got " + String.valueOf(this.getInputSchema().getType(References.TEXT_COMPONENT)));
        }
        Type<?> type5 = ExtraDataFixUtils.patchSubType(type4, type4, type2);
        return this.fixTypeEverywhere("TextComponentHoverAndClickEventFix", type4, type2, dynamicOps -> pair2 -> {
            boolean bl = (Boolean)((Either)pair2.getSecond()).map(either -> false, pair -> {
                Pair pair2 = (Pair)((Pair)pair.getSecond()).getSecond();
                boolean bl = ((Either)pair2.getFirst()).left().isPresent();
                boolean bl2 = ((Dynamic)pair2.getSecond()).get("clickEvent").result().isPresent();
                return bl || bl2;
            });
            if (!bl) {
                return pair2;
            }
            return Util.writeAndReadTypedOrThrow(ExtraDataFixUtils.cast(type5, pair2, dynamicOps), type2, TextComponentHoverAndClickEventFix::fixTextComponent).getValue();
        });
    }

    private static Dynamic<?> fixTextComponent(Dynamic<?> dynamic) {
        return dynamic.renameAndFixField("hoverEvent", "hover_event", TextComponentHoverAndClickEventFix::fixHoverEvent).renameAndFixField("clickEvent", "click_event", TextComponentHoverAndClickEventFix::fixClickEvent);
    }

    private static Dynamic<?> copyFields(Dynamic<?> dynamic, Dynamic<?> dynamic2, String ... strings) {
        for (String string : strings) {
            dynamic = Dynamic.copyField(dynamic2, (String)string, dynamic, (String)string);
        }
        return dynamic;
    }

    private static Dynamic<?> fixHoverEvent(Dynamic<?> dynamic) {
        String string;
        return switch (string = dynamic.get("action").asString("")) {
            case "show_text" -> dynamic.renameField("contents", "value");
            case "show_item" -> {
                Dynamic dynamic2 = dynamic.get("contents").orElseEmptyMap();
                Optional optional = dynamic2.asString().result();
                if (optional.isPresent()) {
                    yield dynamic.renameField("contents", "id");
                }
                yield TextComponentHoverAndClickEventFix.copyFields(dynamic.remove("contents"), dynamic2, "id", "count", "components");
            }
            case "show_entity" -> {
                Dynamic dynamic2 = dynamic.get("contents").orElseEmptyMap();
                yield TextComponentHoverAndClickEventFix.copyFields(dynamic.remove("contents"), dynamic2, "id", "type", "name").renameField("id", "uuid").renameField("type", "id");
            }
            default -> dynamic;
        };
    }

    private static <T> @Nullable Dynamic<T> fixClickEvent(Dynamic<T> dynamic) {
        String string = dynamic.get("action").asString("");
        String string2 = dynamic.get("value").asString("");
        return switch (string) {
            case "open_url" -> {
                if (!TextComponentHoverAndClickEventFix.validateUri(string2)) {
                    yield null;
                }
                yield dynamic.renameField("value", "url");
            }
            case "open_file" -> dynamic.renameField("value", "path");
            case "run_command", "suggest_command" -> {
                if (!TextComponentHoverAndClickEventFix.validateChat(string2)) {
                    yield null;
                }
                yield dynamic.renameField("value", "command");
            }
            case "change_page" -> {
                Integer integer = dynamic.get("value").result().map(TextComponentHoverAndClickEventFix::parseOldPage).orElse(null);
                if (integer == null) {
                    yield null;
                }
                int i = Math.max(integer, 1);
                yield dynamic.remove("value").set("page", dynamic.createInt(i));
            }
            default -> dynamic;
        };
    }

    private static @Nullable Integer parseOldPage(Dynamic<?> dynamic) {
        Optional optional = dynamic.asNumber().result();
        if (optional.isPresent()) {
            return ((Number)optional.get()).intValue();
        }
        try {
            return Integer.parseInt(dynamic.asString(""));
        }
        catch (Exception exception) {
            return null;
        }
    }

    private static boolean validateUri(String string) {
        try {
            URI uRI = new URI(string);
            String string2 = uRI.getScheme();
            if (string2 == null) {
                return false;
            }
            String string3 = string2.toLowerCase(Locale.ROOT);
            return "http".equals(string3) || "https".equals(string3);
        }
        catch (URISyntaxException uRISyntaxException) {
            return false;
        }
    }

    private static boolean validateChat(String string) {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c != '\u00a7' && c >= ' ' && c != '\u007f') continue;
            return false;
        }
        return true;
    }
}

