package com.ruinedportaloverhaul.loot;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.config.ModConfigManager;
import com.ruinedportaloverhaul.entity.PiglinEvokerEntity;
import com.ruinedportaloverhaul.entity.PiglinIllusionerEntity;
import com.ruinedportaloverhaul.entity.PiglinRavagerEntity;
import com.ruinedportaloverhaul.item.ModItems;
import java.util.List;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class ModLootEvents {
    private static final double EVOKER_NETHER_STAR_CHANCE = 0.05;
    private static final double RAVAGER_NETHER_STAR_CHANCE = 0.03;
    private static final double ILLUSIONER_NETHER_STAR_CHANCE = 0.01;
    private static final double EVOKER_NETHER_TIDE_DISC_CHANCE = 0.05;
    private static final ResourceKey<LootTable> PORTAL_BOSS_REWARD = ResourceKey.create(
        Registries.LOOT_TABLE,
        Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, "chests/portal_boss_reward")
    );

    private ModLootEvents() {
    }

    public static void initialize() {
        // Fix: Nether Star odds were hardcoded in loot JSON, so the runtime config slider could not affect raid drops.
        LootTableEvents.MODIFY_DROPS.register((entry, context, drops) -> {
            addConfiguredNetherStarDrop(context, drops);
            addEvokerNetherTideDiscDrop(context, drops);
            addOptionalPatchouliGuide(entry, drops);
        });
    }

    private static void addConfiguredNetherStarDrop(LootContext context, List<ItemStack> drops) {
        if (context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER) == null) {
            return;
        }

        Object rawEntity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(rawEntity instanceof Entity entity)) {
            return;
        }

        double baseChance = baseNetherStarChance(entity);
        if (baseChance <= 0.0) {
            return;
        }

        double chance = Math.clamp(baseChance * ModConfigManager.netherStarDropRate(), 0.0, 1.0);
        if (chance > 0.0 && context.getRandom().nextDouble() < chance) {
            drops.add(new ItemStack(Items.NETHER_STAR));
        }
    }

    private static double baseNetherStarChance(Entity entity) {
        if (entity instanceof PiglinEvokerEntity) {
            return EVOKER_NETHER_STAR_CHANCE;
        }
        if (entity instanceof PiglinRavagerEntity) {
            return RAVAGER_NETHER_STAR_CHANCE;
        }
        if (entity instanceof PiglinIllusionerEntity) {
            return ILLUSIONER_NETHER_STAR_CHANCE;
        }
        return 0.0;
    }

    private static void addEvokerNetherTideDiscDrop(LootContext context, List<ItemStack> drops) {
        if (context.getOptionalParameter(LootContextParams.LAST_DAMAGE_PLAYER) == null) {
            return;
        }

        Object rawEntity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (rawEntity instanceof PiglinEvokerEntity && context.getRandom().nextDouble() < EVOKER_NETHER_TIDE_DISC_CHANCE) {
            drops.add(new ItemStack(ModItems.MUSIC_DISC_NETHER_TIDE));
        }
    }

    private static void addOptionalPatchouliGuide(Holder<LootTable> entry, List<ItemStack> drops) {
        if (!entry.is(PORTAL_BOSS_REWARD) || !FabricLoader.getInstance().isModLoaded("patchouli")) {
            return;
        }

        Identifier guideBookId = Identifier.fromNamespaceAndPath("patchouli", "guide_book");
        if (!BuiltInRegistries.ITEM.containsKey(guideBookId)) {
            return;
        }

        Item guideBook = BuiltInRegistries.ITEM.getValue(guideBookId);
        ItemStack stack = new ItemStack(guideBook);
        CompoundTag tag = new CompoundTag();
        tag.putString("patchouli:book", RuinedPortalOverhaul.MOD_ID + ":corrupted_chronicle");
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        drops.add(stack);
    }
}
