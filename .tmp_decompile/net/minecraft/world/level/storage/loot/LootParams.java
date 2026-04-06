/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class LootParams {
    private final ServerLevel level;
    private final ContextMap params;
    private final Map<Identifier, DynamicDrop> dynamicDrops;
    private final float luck;

    public LootParams(ServerLevel serverLevel, ContextMap contextMap, Map<Identifier, DynamicDrop> map, float f) {
        this.level = serverLevel;
        this.params = contextMap;
        this.dynamicDrops = map;
        this.luck = f;
    }

    public ServerLevel getLevel() {
        return this.level;
    }

    public ContextMap contextMap() {
        return this.params;
    }

    public void addDynamicDrops(Identifier identifier, Consumer<ItemStack> consumer) {
        DynamicDrop dynamicDrop = this.dynamicDrops.get(identifier);
        if (dynamicDrop != null) {
            dynamicDrop.add(consumer);
        }
    }

    public float getLuck() {
        return this.luck;
    }

    @FunctionalInterface
    public static interface DynamicDrop {
        public void add(Consumer<ItemStack> var1);
    }

    public static class Builder {
        private final ServerLevel level;
        private final ContextMap.Builder params = new ContextMap.Builder();
        private final Map<Identifier, DynamicDrop> dynamicDrops = Maps.newHashMap();
        private float luck;

        public Builder(ServerLevel serverLevel) {
            this.level = serverLevel;
        }

        public ServerLevel getLevel() {
            return this.level;
        }

        public <T> Builder withParameter(ContextKey<T> contextKey, T object) {
            this.params.withParameter(contextKey, object);
            return this;
        }

        public <T> Builder withOptionalParameter(ContextKey<T> contextKey, @Nullable T object) {
            this.params.withOptionalParameter(contextKey, object);
            return this;
        }

        public <T> T getParameter(ContextKey<T> contextKey) {
            return this.params.getParameter(contextKey);
        }

        public <T> @Nullable T getOptionalParameter(ContextKey<T> contextKey) {
            return this.params.getOptionalParameter(contextKey);
        }

        public Builder withDynamicDrop(Identifier identifier, DynamicDrop dynamicDrop) {
            DynamicDrop dynamicDrop2 = this.dynamicDrops.put(identifier, dynamicDrop);
            if (dynamicDrop2 != null) {
                throw new IllegalStateException("Duplicated dynamic drop '" + String.valueOf(this.dynamicDrops) + "'");
            }
            return this;
        }

        public Builder withLuck(float f) {
            this.luck = f;
            return this;
        }

        public LootParams create(ContextKeySet contextKeySet) {
            ContextMap contextMap = this.params.create(contextKeySet);
            return new LootParams(this.level, contextMap, this.dynamicDrops, this.luck);
        }
    }
}

