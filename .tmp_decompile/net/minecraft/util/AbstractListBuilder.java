/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.ListBuilder
 */
package net.minecraft.util;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.ListBuilder;
import java.util.function.UnaryOperator;

abstract class AbstractListBuilder<T, B>
implements ListBuilder<T> {
    private final DynamicOps<T> ops;
    protected DataResult<B> builder = DataResult.success(this.initBuilder(), (Lifecycle)Lifecycle.stable());

    protected AbstractListBuilder(DynamicOps<T> dynamicOps) {
        this.ops = dynamicOps;
    }

    public DynamicOps<T> ops() {
        return this.ops;
    }

    protected abstract B initBuilder();

    protected abstract B append(B var1, T var2);

    protected abstract DataResult<T> build(B var1, T var2);

    public ListBuilder<T> add(T object) {
        this.builder = this.builder.map(object2 -> this.append(object2, object));
        return this;
    }

    public ListBuilder<T> add(DataResult<T> dataResult) {
        this.builder = this.builder.apply2stable(this::append, dataResult);
        return this;
    }

    public ListBuilder<T> withErrorsFrom(DataResult<?> dataResult) {
        this.builder = this.builder.flatMap(object -> dataResult.map(object2 -> object));
        return this;
    }

    public ListBuilder<T> mapError(UnaryOperator<String> unaryOperator) {
        this.builder = this.builder.mapError(unaryOperator);
        return this;
    }

    public DataResult<T> build(T object) {
        DataResult dataResult = this.builder.flatMap(object2 -> this.build(object2, object));
        this.builder = DataResult.success(this.initBuilder(), (Lifecycle)Lifecycle.stable());
        return dataResult;
    }
}

