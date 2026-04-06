/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public class SpawnArmorTrimsCommand {
    private static final List<ResourceKey<TrimPattern>> VANILLA_TRIM_PATTERNS = List.of((Object[])new ResourceKey[]{TrimPatterns.SENTRY, TrimPatterns.DUNE, TrimPatterns.COAST, TrimPatterns.WILD, TrimPatterns.WARD, TrimPatterns.EYE, TrimPatterns.VEX, TrimPatterns.TIDE, TrimPatterns.SNOUT, TrimPatterns.RIB, TrimPatterns.SPIRE, TrimPatterns.WAYFINDER, TrimPatterns.SHAPER, TrimPatterns.SILENCE, TrimPatterns.RAISER, TrimPatterns.HOST, TrimPatterns.FLOW, TrimPatterns.BOLT});
    private static final List<ResourceKey<TrimMaterial>> VANILLA_TRIM_MATERIALS = List.of((Object[])new ResourceKey[]{TrimMaterials.QUARTZ, TrimMaterials.IRON, TrimMaterials.NETHERITE, TrimMaterials.REDSTONE, TrimMaterials.COPPER, TrimMaterials.GOLD, TrimMaterials.EMERALD, TrimMaterials.DIAMOND, TrimMaterials.LAPIS, TrimMaterials.AMETHYST, TrimMaterials.RESIN});
    private static final ToIntFunction<ResourceKey<TrimPattern>> TRIM_PATTERN_ORDER = Util.createIndexLookup(VANILLA_TRIM_PATTERNS);
    private static final ToIntFunction<ResourceKey<TrimMaterial>> TRIM_MATERIAL_ORDER = Util.createIndexLookup(VANILLA_TRIM_MATERIALS);
    private static final DynamicCommandExceptionType ERROR_INVALID_PATTERN = new DynamicCommandExceptionType(object -> Component.translatableEscape("Invalid pattern", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("spawn_armor_trims").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))).then(Commands.literal("*_lag_my_game").executes(commandContext -> SpawnArmorTrimsCommand.spawnAllArmorTrims((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getPlayerOrException())))).then(Commands.argument("pattern", ResourceKeyArgument.key(Registries.TRIM_PATTERN)).executes(commandContext -> SpawnArmorTrimsCommand.spawnArmorTrim((CommandSourceStack)commandContext.getSource(), ((CommandSourceStack)commandContext.getSource()).getPlayerOrException(), ResourceKeyArgument.getRegistryKey((CommandContext<CommandSourceStack>)commandContext, "pattern", Registries.TRIM_PATTERN, ERROR_INVALID_PATTERN)))));
    }

    private static int spawnAllArmorTrims(CommandSourceStack commandSourceStack, Player player) {
        return SpawnArmorTrimsCommand.spawnArmorTrims(commandSourceStack, player, commandSourceStack.getServer().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).listElements());
    }

    private static int spawnArmorTrim(CommandSourceStack commandSourceStack, Player player, ResourceKey<TrimPattern> resourceKey) {
        return SpawnArmorTrimsCommand.spawnArmorTrims(commandSourceStack, player, Stream.of((Holder.Reference)commandSourceStack.getServer().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).get(resourceKey).orElseThrow()));
    }

    private static int spawnArmorTrims(CommandSourceStack commandSourceStack, Player player, Stream<Holder.Reference<TrimPattern>> stream) {
        ServerLevel serverLevel = commandSourceStack.getLevel();
        List list = stream.sorted(Comparator.comparing(reference -> TRIM_PATTERN_ORDER.applyAsInt(reference.key()))).toList();
        List list2 = serverLevel.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).listElements().sorted(Comparator.comparing(reference -> TRIM_MATERIAL_ORDER.applyAsInt(reference.key()))).toList();
        List<Holder.Reference<Item>> list3 = SpawnArmorTrimsCommand.findEquippableItemsWithAssets(serverLevel.registryAccess().lookupOrThrow(Registries.ITEM));
        BlockPos blockPos = player.blockPosition().relative(player.getDirection(), 5);
        double d = 3.0;
        for (int i = 0; i < list2.size(); ++i) {
            Holder.Reference reference2 = (Holder.Reference)list2.get(i);
            for (int j = 0; j < list.size(); ++j) {
                Holder.Reference reference22 = (Holder.Reference)list.get(j);
                ArmorTrim armorTrim = new ArmorTrim(reference2, reference22);
                for (int k = 0; k < list3.size(); ++k) {
                    Holder.Reference<Item> reference3 = list3.get(k);
                    double e = (double)blockPos.getX() + 0.5 - (double)k * 3.0;
                    double f = (double)blockPos.getY() + 0.5 + (double)i * 3.0;
                    double g = (double)blockPos.getZ() + 0.5 + (double)(j * 10);
                    ArmorStand armorStand = new ArmorStand(serverLevel, e, f, g);
                    armorStand.setYRot(180.0f);
                    armorStand.setNoGravity(true);
                    ItemStack itemStack = new ItemStack(reference3);
                    Equippable equippable = Objects.requireNonNull(itemStack.get(DataComponents.EQUIPPABLE));
                    itemStack.set(DataComponents.TRIM, armorTrim);
                    armorStand.setItemSlot(equippable.slot(), itemStack);
                    if (k == 0) {
                        armorStand.setCustomName(armorTrim.pattern().value().copyWithStyle(armorTrim.material()).copy().append(" & ").append(armorTrim.material().value().description()));
                        armorStand.setCustomNameVisible(true);
                    } else {
                        armorStand.setInvisible(true);
                    }
                    serverLevel.addFreshEntity(armorStand);
                }
            }
        }
        commandSourceStack.sendSuccess(() -> Component.literal("Armorstands with trimmed armor spawned around you"), true);
        return 1;
    }

    private static List<Holder.Reference<Item>> findEquippableItemsWithAssets(HolderLookup<Item> holderLookup) {
        ArrayList<Holder.Reference<Item>> list = new ArrayList<Holder.Reference<Item>>();
        holderLookup.listElements().forEach(reference -> {
            Equippable equippable = ((Item)reference.value()).components().get(DataComponents.EQUIPPABLE);
            if (equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR && equippable.assetId().isPresent()) {
                list.add((Holder.Reference<Item>)reference);
            }
        });
        return list;
    }
}

