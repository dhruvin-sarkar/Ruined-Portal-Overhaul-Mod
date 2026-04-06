/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.OptionalDynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.OptionalDynamic;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;

public class TooltipDisplayComponentFix
extends DataFix {
    private static final List<String> CONVERTED_ADDITIONAL_TOOLTIP_TYPES = List.of((Object[])new String[]{"minecraft:banner_patterns", "minecraft:bees", "minecraft:block_entity_data", "minecraft:block_state", "minecraft:bundle_contents", "minecraft:charged_projectiles", "minecraft:container", "minecraft:container_loot", "minecraft:firework_explosion", "minecraft:fireworks", "minecraft:instrument", "minecraft:map_id", "minecraft:painting/variant", "minecraft:pot_decorations", "minecraft:potion_contents", "minecraft:tropical_fish/pattern", "minecraft:written_book_content"});

    public TooltipDisplayComponentFix(Schema schema) {
        super(schema, true);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.DATA_COMPONENTS);
        Type type2 = this.getOutputSchema().getType(References.DATA_COMPONENTS);
        OpticFinder opticFinder = type.findField("minecraft:can_place_on");
        OpticFinder opticFinder2 = type.findField("minecraft:can_break");
        Type type3 = type2.findFieldType("minecraft:can_place_on");
        Type type4 = type2.findFieldType("minecraft:can_break");
        return this.fixTypeEverywhereTyped("TooltipDisplayComponentFix", type, type2, typed -> TooltipDisplayComponentFix.fix(typed, opticFinder, opticFinder2, type3, type4));
    }

    private static Typed<?> fix(Typed<?> typed, OpticFinder<?> opticFinder, OpticFinder<?> opticFinder2, Type<?> type, Type<?> type2) {
        HashSet<String> set = new HashSet<String>();
        typed = TooltipDisplayComponentFix.fixAdventureModePredicate(typed, opticFinder, type, "minecraft:can_place_on", set);
        typed = TooltipDisplayComponentFix.fixAdventureModePredicate(typed, opticFinder2, type2, "minecraft:can_break", set);
        return typed.update(DSL.remainderFinder(), dynamic -> {
            dynamic = TooltipDisplayComponentFix.fixSimpleComponent(dynamic, "minecraft:trim", set);
            dynamic = TooltipDisplayComponentFix.fixSimpleComponent(dynamic, "minecraft:unbreakable", set);
            dynamic = TooltipDisplayComponentFix.fixComponentAndUnwrap(dynamic, "minecraft:dyed_color", "rgb", set);
            dynamic = TooltipDisplayComponentFix.fixComponentAndUnwrap(dynamic, "minecraft:attribute_modifiers", "modifiers", set);
            dynamic = TooltipDisplayComponentFix.fixComponentAndUnwrap(dynamic, "minecraft:enchantments", "levels", set);
            dynamic = TooltipDisplayComponentFix.fixComponentAndUnwrap(dynamic, "minecraft:stored_enchantments", "levels", set);
            dynamic = TooltipDisplayComponentFix.fixComponentAndUnwrap(dynamic, "minecraft:jukebox_playable", "song", set);
            boolean bl = dynamic.get("minecraft:hide_tooltip").result().isPresent();
            dynamic = dynamic.remove("minecraft:hide_tooltip");
            boolean bl2 = dynamic.get("minecraft:hide_additional_tooltip").result().isPresent();
            dynamic = dynamic.remove("minecraft:hide_additional_tooltip");
            if (bl2) {
                for (String string : CONVERTED_ADDITIONAL_TOOLTIP_TYPES) {
                    if (!dynamic.get(string).result().isPresent()) continue;
                    set.add(string);
                }
            }
            if (set.isEmpty() && !bl) {
                return dynamic;
            }
            return dynamic.set("minecraft:tooltip_display", dynamic.createMap(Map.of((Object)dynamic.createString("hide_tooltip"), (Object)dynamic.createBoolean(bl), (Object)dynamic.createString("hidden_components"), (Object)dynamic.createList(set.stream().map(arg_0 -> ((Dynamic)dynamic).createString(arg_0))))));
        });
    }

    private static Dynamic<?> fixSimpleComponent(Dynamic<?> dynamic, String string, Set<String> set) {
        return TooltipDisplayComponentFix.fixRemainderComponent(dynamic, string, set, UnaryOperator.identity());
    }

    private static Dynamic<?> fixComponentAndUnwrap(Dynamic<?> dynamic2, String string, String string2, Set<String> set) {
        return TooltipDisplayComponentFix.fixRemainderComponent(dynamic2, string, set, dynamic -> (Dynamic)DataFixUtils.orElse((Optional)dynamic.get(string2).result(), (Object)dynamic));
    }

    private static Dynamic<?> fixRemainderComponent(Dynamic<?> dynamic2, String string, Set<String> set, UnaryOperator<Dynamic<?>> unaryOperator) {
        return dynamic2.update(string, dynamic -> {
            boolean bl = dynamic.get("show_in_tooltip").asBoolean(true);
            if (!bl) {
                set.add(string);
            }
            return (Dynamic)unaryOperator.apply(dynamic.remove("show_in_tooltip"));
        });
    }

    private static Typed<?> fixAdventureModePredicate(Typed<?> typed2, OpticFinder<?> opticFinder, Type<?> type, String string, Set<String> set) {
        return typed2.updateTyped(opticFinder, type, typed -> Util.writeAndReadTypedOrThrow(typed, type, dynamic -> {
            OptionalDynamic optionalDynamic = dynamic.get("predicates");
            if (optionalDynamic.result().isEmpty()) {
                return dynamic;
            }
            boolean bl = dynamic.get("show_in_tooltip").asBoolean(true);
            if (!bl) {
                set.add(string);
            }
            return (Dynamic)optionalDynamic.result().get();
        }));
    }
}

