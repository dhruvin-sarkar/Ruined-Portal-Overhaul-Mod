/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jspecify.annotations.Nullable;

public class AppendLoot
implements RuleBlockEntityModifier {
    public static final MapCodec<AppendLoot> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)LootTable.KEY_CODEC.fieldOf("loot_table").forGetter(appendLoot -> appendLoot.lootTable)).apply((Applicative)instance, AppendLoot::new));
    private final ResourceKey<LootTable> lootTable;

    public AppendLoot(ResourceKey<LootTable> resourceKey) {
        this.lootTable = resourceKey;
    }

    @Override
    public CompoundTag apply(RandomSource randomSource, @Nullable CompoundTag compoundTag) {
        CompoundTag compoundTag2 = compoundTag == null ? new CompoundTag() : compoundTag.copy();
        compoundTag2.store("LootTable", LootTable.KEY_CODEC, this.lootTable);
        compoundTag2.putLong("LootTableSeed", randomSource.nextLong());
        return compoundTag2;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.APPEND_LOOT;
    }
}

