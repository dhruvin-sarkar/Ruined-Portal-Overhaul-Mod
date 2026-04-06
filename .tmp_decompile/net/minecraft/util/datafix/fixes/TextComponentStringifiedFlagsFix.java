/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class TextComponentStringifiedFlagsFix
extends DataFix {
    public TextComponentStringifiedFlagsFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.TEXT_COMPONENT);
        return this.fixTypeEverywhere("TextComponentStringyFlagsFix", type, dynamicOps -> pair -> pair.mapSecond(either -> either.mapRight(pair -> pair.mapSecond(pair2 -> pair2.mapSecond(pair -> pair.mapSecond(dynamic -> dynamic.update("bold", TextComponentStringifiedFlagsFix::stringToBool).update("italic", TextComponentStringifiedFlagsFix::stringToBool).update("underlined", TextComponentStringifiedFlagsFix::stringToBool).update("strikethrough", TextComponentStringifiedFlagsFix::stringToBool).update("obfuscated", TextComponentStringifiedFlagsFix::stringToBool)))))));
    }

    private static <T> Dynamic<T> stringToBool(Dynamic<T> dynamic) {
        Optional optional = dynamic.asString().result();
        if (optional.isPresent()) {
            return dynamic.createBoolean(Boolean.parseBoolean((String)optional.get()));
        }
        return dynamic;
    }
}

