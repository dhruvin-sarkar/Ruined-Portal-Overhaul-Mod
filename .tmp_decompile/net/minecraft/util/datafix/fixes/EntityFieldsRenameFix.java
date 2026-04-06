/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

public class EntityFieldsRenameFix
extends NamedEntityFix {
    private final Map<String, String> renames;

    public EntityFieldsRenameFix(Schema schema, String string, String string2, Map<String, String> map) {
        super(schema, false, string, References.ENTITY, string2);
        this.renames = map;
    }

    public Dynamic<?> fixTag(Dynamic<?> dynamic) {
        for (Map.Entry<String, String> entry : this.renames.entrySet()) {
            dynamic = dynamic.renameField(entry.getKey(), entry.getValue());
        }
        return dynamic;
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}

