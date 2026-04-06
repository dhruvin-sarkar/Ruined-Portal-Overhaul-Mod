/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class TrialSpawnerConfigFix
extends NamedEntityWriteReadFix {
    public TrialSpawnerConfigFix(Schema schema) {
        super(schema, true, "Trial Spawner config tag fixer", References.BLOCK_ENTITY, "minecraft:trial_spawner");
    }

    private static <T> Dynamic<T> moveToConfigTag(Dynamic<T> dynamic) {
        List list = List.of((Object)"spawn_range", (Object)"total_mobs", (Object)"simultaneous_mobs", (Object)"total_mobs_added_per_player", (Object)"simultaneous_mobs_added_per_player", (Object)"ticks_between_spawn", (Object)"spawn_potentials", (Object)"loot_tables_to_eject", (Object)"items_to_drop_when_ominous");
        HashMap<Dynamic, Dynamic> map = new HashMap<Dynamic, Dynamic>(list.size());
        for (String string : list) {
            Optional optional = dynamic.get(string).get().result();
            if (!optional.isPresent()) continue;
            map.put(dynamic.createString(string), (Dynamic)optional.get());
            dynamic = dynamic.remove(string);
        }
        return map.isEmpty() ? dynamic : dynamic.set("normal_config", dynamic.createMap(map));
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        return TrialSpawnerConfigFix.moveToConfigTag(dynamic);
    }
}

