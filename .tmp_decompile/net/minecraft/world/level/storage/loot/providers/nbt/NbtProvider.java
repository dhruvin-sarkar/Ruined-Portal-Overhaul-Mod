/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage.loot.providers.nbt;

import java.util.Set;
import net.minecraft.nbt.Tag;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import org.jspecify.annotations.Nullable;

public interface NbtProvider {
    public @Nullable Tag get(LootContext var1);

    public Set<ContextKey<?>> getReferencedContextParams();

    public LootNbtProviderType getType();
}

