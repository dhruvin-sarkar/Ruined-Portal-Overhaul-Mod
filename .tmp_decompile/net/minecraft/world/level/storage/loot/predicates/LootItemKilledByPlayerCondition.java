/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class LootItemKilledByPlayerCondition
implements LootItemCondition {
    private static final LootItemKilledByPlayerCondition INSTANCE = new LootItemKilledByPlayerCondition();
    public static final MapCodec<LootItemKilledByPlayerCondition> CODEC = MapCodec.unit((Object)INSTANCE);

    private LootItemKilledByPlayerCondition() {
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.KILLED_BY_PLAYER;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return Set.of(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    @Override
    public boolean test(LootContext lootContext) {
        return lootContext.hasParameter(LootContextParams.LAST_DAMAGE_PLAYER);
    }

    public static LootItemCondition.Builder killedByPlayer() {
        return () -> INSTANCE;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }
}

