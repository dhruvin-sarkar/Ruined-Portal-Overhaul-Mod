/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DSL$TypeReference
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class AddFieldFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference type;
    private final String fieldName;
    private final String[] path;
    private final Function<Dynamic<?>, Dynamic<?>> fieldGenerator;

    public AddFieldFix(Schema schema, DSL.TypeReference typeReference, String string, Function<Dynamic<?>, Dynamic<?>> function, String ... strings) {
        super(schema, false);
        this.name = "Adding field `" + string + "` to type `" + typeReference.typeName().toLowerCase(Locale.ROOT) + "`";
        this.type = typeReference;
        this.fieldName = string;
        this.path = strings;
        this.fieldGenerator = function;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(this.type), this.getOutputSchema().getType(this.type), typed -> typed.update(DSL.remainderFinder(), dynamic -> this.addField((Dynamic<?>)dynamic, 0)));
    }

    private Dynamic<?> addField(Dynamic<?> dynamic, int i) {
        if (i >= this.path.length) {
            return dynamic.set(this.fieldName, this.fieldGenerator.apply(dynamic));
        }
        Optional optional = dynamic.get(this.path[i]).result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        return this.addField((Dynamic)optional.get(), i + 1);
    }
}

