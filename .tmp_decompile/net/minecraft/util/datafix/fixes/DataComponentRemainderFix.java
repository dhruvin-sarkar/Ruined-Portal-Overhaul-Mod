/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import org.jspecify.annotations.Nullable;

public abstract class DataComponentRemainderFix
extends DataFix {
    private final String name;
    private final String componentId;
    private final String newComponentId;

    public DataComponentRemainderFix(Schema schema, String string, String string2) {
        this(schema, string, string2, string2);
    }

    public DataComponentRemainderFix(Schema schema, String string, String string2, String string3) {
        super(schema, false);
        this.name = string;
        this.componentId = string2;
        this.newComponentId = string3;
    }

    public final TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.DATA_COMPONENTS);
        return this.fixTypeEverywhereTyped(this.name, type, typed -> typed.update(DSL.remainderFinder(), dynamic -> {
            Optional optional = dynamic.get(this.componentId).result();
            if (optional.isEmpty()) {
                return dynamic;
            }
            Dynamic dynamic2 = this.fixComponent((Dynamic)optional.get());
            return dynamic.remove(this.componentId).setFieldIfPresent(this.newComponentId, Optional.ofNullable(dynamic2));
        }));
    }

    protected abstract <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> var1);
}

