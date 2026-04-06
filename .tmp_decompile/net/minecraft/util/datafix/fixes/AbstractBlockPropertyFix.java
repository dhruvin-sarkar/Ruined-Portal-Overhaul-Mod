/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
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
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class AbstractBlockPropertyFix
extends DataFix {
    private final String name;

    public AbstractBlockPropertyFix(Schema schema, String string) {
        super(schema, false);
        this.name = string;
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(References.BLOCK_STATE), typed -> typed.update(DSL.remainderFinder(), this::fixBlockState));
    }

    private Dynamic<?> fixBlockState(Dynamic<?> dynamic2) {
        Optional<String> optional = dynamic2.get("Name").asString().result().map(NamespacedSchema::ensureNamespaced);
        if (optional.isPresent() && this.shouldFix(optional.get())) {
            return dynamic2.update("Properties", dynamic -> this.fixProperties((String)optional.get(), (Dynamic)dynamic));
        }
        return dynamic2;
    }

    protected abstract boolean shouldFix(String var1);

    protected abstract <T> Dynamic<T> fixProperties(String var1, Dynamic<T> var2);
}

