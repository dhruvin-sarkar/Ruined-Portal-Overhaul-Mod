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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class InlineBlockPosFormatFix
extends DataFix {
    public InlineBlockPosFormatFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        OpticFinder<?> opticFinder = this.entityFinder("minecraft:vex");
        OpticFinder<?> opticFinder2 = this.entityFinder("minecraft:phantom");
        OpticFinder<?> opticFinder3 = this.entityFinder("minecraft:turtle");
        List list = List.of(this.entityFinder("minecraft:item_frame"), this.entityFinder("minecraft:glow_item_frame"), this.entityFinder("minecraft:painting"), this.entityFinder("minecraft:leash_knot"));
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("InlineBlockPosFormatFix - player", this.getInputSchema().getType(References.PLAYER), typed -> typed.update(DSL.remainderFinder(), this::fixPlayer)), (TypeRewriteRule)this.fixTypeEverywhereTyped("InlineBlockPosFormatFix - entity", this.getInputSchema().getType(References.ENTITY), typed2 -> {
            typed2 = typed2.update(DSL.remainderFinder(), this::fixLivingEntity).updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::fixVex)).updateTyped(opticFinder2, typed -> typed.update(DSL.remainderFinder(), this::fixPhantom)).updateTyped(opticFinder3, typed -> typed.update(DSL.remainderFinder(), this::fixTurtle));
            for (OpticFinder opticFinder4 : list) {
                typed2 = typed2.updateTyped(opticFinder4, typed -> typed.update(DSL.remainderFinder(), this::fixBlockAttached));
            }
            return typed2;
        }));
    }

    private OpticFinder<?> entityFinder(String string) {
        return DSL.namedChoice((String)string, (Type)this.getInputSchema().getChoiceType(References.ENTITY, string));
    }

    private Dynamic<?> fixPlayer(Dynamic<?> dynamic) {
        Optional optional4;
        dynamic = this.fixLivingEntity(dynamic);
        Optional optional = dynamic.get("SpawnX").asNumber().result();
        Optional optional2 = dynamic.get("SpawnY").asNumber().result();
        Optional optional3 = dynamic.get("SpawnZ").asNumber().result();
        if (optional.isPresent() && optional2.isPresent() && optional3.isPresent()) {
            Dynamic dynamic2 = dynamic.createMap(Map.of((Object)dynamic.createString("pos"), ExtraDataFixUtils.createBlockPos(dynamic, ((Number)optional.get()).intValue(), ((Number)optional2.get()).intValue(), ((Number)optional3.get()).intValue())));
            dynamic2 = Dynamic.copyField(dynamic, (String)"SpawnAngle", (Dynamic)dynamic2, (String)"angle");
            dynamic2 = Dynamic.copyField(dynamic, (String)"SpawnDimension", (Dynamic)dynamic2, (String)"dimension");
            dynamic2 = Dynamic.copyField(dynamic, (String)"SpawnForced", (Dynamic)dynamic2, (String)"forced");
            dynamic = dynamic.remove("SpawnX").remove("SpawnY").remove("SpawnZ").remove("SpawnAngle").remove("SpawnDimension").remove("SpawnForced");
            dynamic = dynamic.set("respawn", dynamic2);
        }
        if ((optional4 = dynamic.get("enteredNetherPosition").result()).isPresent()) {
            dynamic = dynamic.remove("enteredNetherPosition").set("entered_nether_pos", dynamic.createList(Stream.of(dynamic.createDouble(((Dynamic)optional4.get()).get("x").asDouble(0.0)), dynamic.createDouble(((Dynamic)optional4.get()).get("y").asDouble(0.0)), dynamic.createDouble(((Dynamic)optional4.get()).get("z").asDouble(0.0)))));
        }
        return dynamic;
    }

    private Dynamic<?> fixLivingEntity(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic, "SleepingX", "SleepingY", "SleepingZ", "sleeping_pos");
    }

    private Dynamic<?> fixVex(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic.renameField("LifeTicks", "life_ticks"), "BoundX", "BoundY", "BoundZ", "bound_pos");
    }

    private Dynamic<?> fixPhantom(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic.renameField("Size", "size"), "AX", "AY", "AZ", "anchor_pos");
    }

    private Dynamic<?> fixTurtle(Dynamic<?> dynamic) {
        dynamic = dynamic.remove("TravelPosX").remove("TravelPosY").remove("TravelPosZ");
        dynamic = ExtraDataFixUtils.fixInlineBlockPos(dynamic, "HomePosX", "HomePosY", "HomePosZ", "home_pos");
        return dynamic.renameField("HasEgg", "has_egg");
    }

    private Dynamic<?> fixBlockAttached(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic, "TileX", "TileY", "TileZ", "block_pos");
    }
}

