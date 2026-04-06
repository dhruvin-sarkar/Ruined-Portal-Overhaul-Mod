/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.npc.villager;

import com.google.common.collect.ImmutableSet;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

public record VillagerProfession(Component name, Predicate<Holder<PoiType>> heldJobSite, Predicate<Holder<PoiType>> acquirableJobSite, ImmutableSet<Item> requestedItems, ImmutableSet<Block> secondaryPoi, @Nullable SoundEvent workSound) {
    public static final Predicate<Holder<PoiType>> ALL_ACQUIRABLE_JOBS = holder -> holder.is(PoiTypeTags.ACQUIRABLE_JOB_SITE);
    public static final ResourceKey<VillagerProfession> NONE = VillagerProfession.createKey("none");
    public static final ResourceKey<VillagerProfession> ARMORER = VillagerProfession.createKey("armorer");
    public static final ResourceKey<VillagerProfession> BUTCHER = VillagerProfession.createKey("butcher");
    public static final ResourceKey<VillagerProfession> CARTOGRAPHER = VillagerProfession.createKey("cartographer");
    public static final ResourceKey<VillagerProfession> CLERIC = VillagerProfession.createKey("cleric");
    public static final ResourceKey<VillagerProfession> FARMER = VillagerProfession.createKey("farmer");
    public static final ResourceKey<VillagerProfession> FISHERMAN = VillagerProfession.createKey("fisherman");
    public static final ResourceKey<VillagerProfession> FLETCHER = VillagerProfession.createKey("fletcher");
    public static final ResourceKey<VillagerProfession> LEATHERWORKER = VillagerProfession.createKey("leatherworker");
    public static final ResourceKey<VillagerProfession> LIBRARIAN = VillagerProfession.createKey("librarian");
    public static final ResourceKey<VillagerProfession> MASON = VillagerProfession.createKey("mason");
    public static final ResourceKey<VillagerProfession> NITWIT = VillagerProfession.createKey("nitwit");
    public static final ResourceKey<VillagerProfession> SHEPHERD = VillagerProfession.createKey("shepherd");
    public static final ResourceKey<VillagerProfession> TOOLSMITH = VillagerProfession.createKey("toolsmith");
    public static final ResourceKey<VillagerProfession> WEAPONSMITH = VillagerProfession.createKey("weaponsmith");

    private static ResourceKey<VillagerProfession> createKey(String string) {
        return ResourceKey.create(Registries.VILLAGER_PROFESSION, Identifier.withDefaultNamespace(string));
    }

    private static VillagerProfession register(Registry<VillagerProfession> registry, ResourceKey<VillagerProfession> resourceKey, ResourceKey<PoiType> resourceKey2, @Nullable SoundEvent soundEvent) {
        return VillagerProfession.register(registry, resourceKey, holder -> holder.is(resourceKey2), holder -> holder.is(resourceKey2), soundEvent);
    }

    private static VillagerProfession register(Registry<VillagerProfession> registry, ResourceKey<VillagerProfession> resourceKey, Predicate<Holder<PoiType>> predicate, Predicate<Holder<PoiType>> predicate2, @Nullable SoundEvent soundEvent) {
        return VillagerProfession.register(registry, resourceKey, predicate, predicate2, (ImmutableSet<Item>)ImmutableSet.of(), (ImmutableSet<Block>)ImmutableSet.of(), soundEvent);
    }

    private static VillagerProfession register(Registry<VillagerProfession> registry, ResourceKey<VillagerProfession> resourceKey, ResourceKey<PoiType> resourceKey2, ImmutableSet<Item> immutableSet, ImmutableSet<Block> immutableSet2, @Nullable SoundEvent soundEvent) {
        return VillagerProfession.register(registry, resourceKey, holder -> holder.is(resourceKey2), holder -> holder.is(resourceKey2), immutableSet, immutableSet2, soundEvent);
    }

    private static VillagerProfession register(Registry<VillagerProfession> registry, ResourceKey<VillagerProfession> resourceKey, Predicate<Holder<PoiType>> predicate, Predicate<Holder<PoiType>> predicate2, ImmutableSet<Item> immutableSet, ImmutableSet<Block> immutableSet2, @Nullable SoundEvent soundEvent) {
        return Registry.register(registry, resourceKey, new VillagerProfession(Component.translatable("entity." + resourceKey.identifier().getNamespace() + ".villager." + resourceKey.identifier().getPath()), predicate, predicate2, immutableSet, immutableSet2, soundEvent));
    }

    public static VillagerProfession bootstrap(Registry<VillagerProfession> registry) {
        VillagerProfession.register(registry, NONE, PoiType.NONE, ALL_ACQUIRABLE_JOBS, null);
        VillagerProfession.register(registry, ARMORER, PoiTypes.ARMORER, SoundEvents.VILLAGER_WORK_ARMORER);
        VillagerProfession.register(registry, BUTCHER, PoiTypes.BUTCHER, SoundEvents.VILLAGER_WORK_BUTCHER);
        VillagerProfession.register(registry, CARTOGRAPHER, PoiTypes.CARTOGRAPHER, SoundEvents.VILLAGER_WORK_CARTOGRAPHER);
        VillagerProfession.register(registry, CLERIC, PoiTypes.CLERIC, SoundEvents.VILLAGER_WORK_CLERIC);
        VillagerProfession.register(registry, FARMER, PoiTypes.FARMER, (ImmutableSet<Item>)ImmutableSet.of((Object)Items.WHEAT, (Object)Items.WHEAT_SEEDS, (Object)Items.BEETROOT_SEEDS, (Object)Items.BONE_MEAL), (ImmutableSet<Block>)ImmutableSet.of((Object)Blocks.FARMLAND), SoundEvents.VILLAGER_WORK_FARMER);
        VillagerProfession.register(registry, FISHERMAN, PoiTypes.FISHERMAN, SoundEvents.VILLAGER_WORK_FISHERMAN);
        VillagerProfession.register(registry, FLETCHER, PoiTypes.FLETCHER, SoundEvents.VILLAGER_WORK_FLETCHER);
        VillagerProfession.register(registry, LEATHERWORKER, PoiTypes.LEATHERWORKER, SoundEvents.VILLAGER_WORK_LEATHERWORKER);
        VillagerProfession.register(registry, LIBRARIAN, PoiTypes.LIBRARIAN, SoundEvents.VILLAGER_WORK_LIBRARIAN);
        VillagerProfession.register(registry, MASON, PoiTypes.MASON, SoundEvents.VILLAGER_WORK_MASON);
        VillagerProfession.register(registry, NITWIT, PoiType.NONE, PoiType.NONE, null);
        VillagerProfession.register(registry, SHEPHERD, PoiTypes.SHEPHERD, SoundEvents.VILLAGER_WORK_SHEPHERD);
        VillagerProfession.register(registry, TOOLSMITH, PoiTypes.TOOLSMITH, SoundEvents.VILLAGER_WORK_TOOLSMITH);
        return VillagerProfession.register(registry, WEAPONSMITH, PoiTypes.WEAPONSMITH, SoundEvents.VILLAGER_WORK_WEAPONSMITH);
    }
}

