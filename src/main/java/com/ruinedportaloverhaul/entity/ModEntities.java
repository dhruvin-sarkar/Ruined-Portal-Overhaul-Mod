package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModEntities {
    public static final Identifier PIGLIN_ILLAGER_RANGED_ID = id("piglin_illager_ranged");
    public static final Identifier PIGLIN_ILLAGER_BRUTE_ID = id("piglin_illager_brute");
    public static final Identifier PIGLIN_ILLAGER_CHIEF_ID = id("piglin_illager_chief");

    public static final EntityType<PiglinIllagerEntity> PIGLIN_ILLAGER_RANGED = registerMonster(
        PIGLIN_ILLAGER_RANGED_ID,
        PiglinIllagerEntity::new,
        PiglinIllagerEntity::createAttributes
    );
    public static final EntityType<PiglinIllagerBruteEntity> PIGLIN_ILLAGER_BRUTE = registerMonster(
        PIGLIN_ILLAGER_BRUTE_ID,
        PiglinIllagerBruteEntity::new,
        PiglinIllagerBruteEntity::createAttributes
    );
    public static final EntityType<PiglinIllagerChiefEntity> PIGLIN_ILLAGER_CHIEF = registerMonster(
        PIGLIN_ILLAGER_CHIEF_ID,
        PiglinIllagerChiefEntity::new,
        PiglinIllagerChiefEntity::createAttributes
    );

    public static final Item PIGLIN_ILLAGER_RANGED_SPAWN_EGG = registerSpawnEgg(
        "piglin_illager_ranged_spawn_egg",
        PIGLIN_ILLAGER_RANGED
    );
    public static final Item PIGLIN_ILLAGER_BRUTE_SPAWN_EGG = registerSpawnEgg(
        "piglin_illager_brute_spawn_egg",
        PIGLIN_ILLAGER_BRUTE
    );
    public static final Item PIGLIN_ILLAGER_CHIEF_SPAWN_EGG = registerSpawnEgg(
        "piglin_illager_chief_spawn_egg",
        PIGLIN_ILLAGER_CHIEF
    );

    private ModEntities() {
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(PIGLIN_ILLAGER_RANGED_SPAWN_EGG);
            entries.accept(PIGLIN_ILLAGER_BRUTE_SPAWN_EGG);
            entries.accept(PIGLIN_ILLAGER_CHIEF_SPAWN_EGG);
        });

        RuinedPortalOverhaul.LOGGER.info("Registered piglin illager entity hooks");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }

    private static <T extends PiglinIllagerEntity> EntityType<T> registerMonster(
        Identifier identifier,
        EntityType.EntityFactory<T> factory,
        Supplier<AttributeSupplier.Builder> attributes
    ) {
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            identifier,
            FabricEntityTypeBuilder.<T>createMob()
                .spawnGroup(MobCategory.MONSTER)
                .entityFactory(factory)
                .dimensions(EntityDimensions.scalable(0.6f, 1.95f))
                .trackRangeBlocks(8)
                .trackedUpdateRate(3)
                .fireImmune()
                .defaultAttributes(attributes)
                .build(ResourceKey.create(Registries.ENTITY_TYPE, identifier))
        );
    }

    private static Item registerSpawnEgg(String path, EntityType<? extends PiglinIllagerEntity> entityType) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            id(path),
            new SpawnEggItem(new Item.Properties().spawnEgg(entityType))
        );
    }
}
