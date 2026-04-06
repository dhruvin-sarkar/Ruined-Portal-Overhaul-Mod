/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.storage.loot.providers.score;

import java.util.Set;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.score.LootScoreProviderType;
import net.minecraft.world.scores.ScoreHolder;
import org.jspecify.annotations.Nullable;

public interface ScoreboardNameProvider {
    public @Nullable ScoreHolder getScoreHolder(LootContext var1);

    public LootScoreProviderType getType();

    public Set<ContextKey<?>> getReferencedContextParams();
}

