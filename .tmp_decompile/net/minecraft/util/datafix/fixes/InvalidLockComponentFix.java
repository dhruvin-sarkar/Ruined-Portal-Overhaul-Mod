/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.datafix.fixes.DataComponentRemainderFix;
import org.jspecify.annotations.Nullable;

public class InvalidLockComponentFix
extends DataComponentRemainderFix {
    private static final Optional<String> INVALID_LOCK_CUSTOM_NAME = Optional.of("\"\"");

    public InvalidLockComponentFix(Schema schema) {
        super(schema, "InvalidLockComponentPredicateFix", "minecraft:lock");
    }

    @Override
    protected <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> dynamic) {
        return InvalidLockComponentFix.fixLock(dynamic);
    }

    public static <T> @Nullable Dynamic<T> fixLock(Dynamic<T> dynamic) {
        return InvalidLockComponentFix.isBrokenLock(dynamic) ? null : dynamic;
    }

    private static <T> boolean isBrokenLock(Dynamic<T> dynamic) {
        return InvalidLockComponentFix.isMapWithOneField(dynamic, "components", dynamic2 -> InvalidLockComponentFix.isMapWithOneField(dynamic2, "minecraft:custom_name", dynamic -> dynamic.asString().result().equals(INVALID_LOCK_CUSTOM_NAME)));
    }

    private static <T> boolean isMapWithOneField(Dynamic<T> dynamic, String string, Predicate<Dynamic<T>> predicate) {
        Optional optional = dynamic.getMapValues().result();
        if (optional.isEmpty() || ((Map)optional.get()).size() != 1) {
            return false;
        }
        return dynamic.get(string).result().filter(predicate).isPresent();
    }
}

