package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public final class ModEntities {
    public static final Identifier PIGLIN_PILLAGER_ID = id("piglin_pillager");
    public static final Identifier PIGLIN_VINDICATOR_ID = id("piglin_vindicator");
    public static final Identifier PIGLIN_BRUTE_PILLAGER_ID = id("piglin_brute_pillager");
    public static final Identifier PIGLIN_ILLUSIONER_ID = id("piglin_illusioner");
    public static final Identifier PIGLIN_EVOKER_ID = id("piglin_evoker");
    public static final Identifier PIGLIN_RAVAGER_ID = id("piglin_ravager");
    public static final Identifier PIGLIN_PILLAGER_VEX_ID = id("piglin_pillager_vex");
    public static final Identifier EXILED_PIGLIN_TRADER_ID = id("exiled_piglin_trader");

    public static final EntityType<PiglinPillagerEntity> PIGLIN_PILLAGER = registerMonster(
        PIGLIN_PILLAGER_ID,
        PiglinPillagerEntity::new,
        PiglinPillagerEntity::createAttributes,
        0.6f,
        1.95f
    );
    public static final EntityType<PiglinVindicatorEntity> PIGLIN_VINDICATOR = registerMonster(
        PIGLIN_VINDICATOR_ID,
        PiglinVindicatorEntity::new,
        PiglinVindicatorEntity::createAttributes,
        0.6f,
        1.95f
    );
    public static final EntityType<PiglinBrutePillagerEntity> PIGLIN_BRUTE_PILLAGER = registerMonster(
        PIGLIN_BRUTE_PILLAGER_ID,
        PiglinBrutePillagerEntity::new,
        PiglinBrutePillagerEntity::createAttributes,
        0.6f,
        1.95f
    );
    public static final EntityType<PiglinIllusionerEntity> PIGLIN_ILLUSIONER = registerMonster(
        PIGLIN_ILLUSIONER_ID,
        PiglinIllusionerEntity::new,
        PiglinIllusionerEntity::createAttributes,
        0.6f,
        1.95f
    );
    public static final EntityType<PiglinEvokerEntity> PIGLIN_EVOKER = registerMonster(
        PIGLIN_EVOKER_ID,
        PiglinEvokerEntity::new,
        PiglinEvokerEntity::createAttributes,
        0.6f,
        1.95f
    );
    public static final EntityType<PiglinRavagerEntity> PIGLIN_RAVAGER = registerMonster(
        PIGLIN_RAVAGER_ID,
        PiglinRavagerEntity::new,
        PiglinRavagerEntity::createAttributes,
        1.95f,
        2.2f
    );
    public static final EntityType<PiglinVexEntity> PIGLIN_PILLAGER_VEX = registerMonster(
        PIGLIN_PILLAGER_VEX_ID,
        PiglinVexEntity::new,
        PiglinVexEntity::createAttributes,
        0.4f,
        0.8f
    );
    public static final EntityType<ExiledPiglinTraderEntity> EXILED_PIGLIN_TRADER = registerMob(
        EXILED_PIGLIN_TRADER_ID,
        ExiledPiglinTraderEntity::new,
        ExiledPiglinTraderEntity::createAttributes,
        MobCategory.CREATURE,
        0.6f,
        1.95f
    );

    public static final Item PIGLIN_PILLAGER_SPAWN_EGG = registerSpawnEgg(
        "piglin_pillager_spawn_egg",
        PIGLIN_PILLAGER
    );
    public static final Item PIGLIN_VINDICATOR_SPAWN_EGG = registerSpawnEgg(
        "piglin_vindicator_spawn_egg",
        PIGLIN_VINDICATOR
    );
    public static final Item PIGLIN_BRUTE_PILLAGER_SPAWN_EGG = registerSpawnEgg(
        "piglin_brute_pillager_spawn_egg",
        PIGLIN_BRUTE_PILLAGER
    );
    public static final Item PIGLIN_ILLUSIONER_SPAWN_EGG = registerSpawnEgg(
        "piglin_illusioner_spawn_egg",
        PIGLIN_ILLUSIONER
    );
    public static final Item PIGLIN_EVOKER_SPAWN_EGG = registerSpawnEgg(
        "piglin_evoker_spawn_egg",
        PIGLIN_EVOKER
    );
    public static final Item PIGLIN_RAVAGER_SPAWN_EGG = registerSpawnEgg(
        "piglin_ravager_spawn_egg",
        PIGLIN_RAVAGER
    );
    public static final Item PIGLIN_PILLAGER_VEX_SPAWN_EGG = registerSpawnEgg(
        "piglin_pillager_vex_spawn_egg",
        PIGLIN_PILLAGER_VEX
    );

    private ModEntities() {
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.SPAWN_EGGS).register(entries -> {
            entries.accept(PIGLIN_PILLAGER_SPAWN_EGG);
            entries.accept(PIGLIN_VINDICATOR_SPAWN_EGG);
            entries.accept(PIGLIN_BRUTE_PILLAGER_SPAWN_EGG);
            entries.accept(PIGLIN_ILLUSIONER_SPAWN_EGG);
            entries.accept(PIGLIN_EVOKER_SPAWN_EGG);
            entries.accept(PIGLIN_RAVAGER_SPAWN_EGG);
            entries.accept(PIGLIN_PILLAGER_VEX_SPAWN_EGG);
        });

        RuinedPortalOverhaul.LOGGER.info("Registered piglin raid entity hooks");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }

    private static <T extends Mob> EntityType<T> registerMonster(
        Identifier identifier,
        EntityType.EntityFactory<T> factory,
        Supplier<AttributeSupplier.Builder> attributes,
        float width,
        float height
    ) {
        return registerMob(identifier, factory, attributes, MobCategory.MONSTER, width, height);
    }

    private static <T extends Mob> EntityType<T> registerMob(
        Identifier identifier,
        EntityType.EntityFactory<T> factory,
        Supplier<AttributeSupplier.Builder> attributes,
        MobCategory category,
        float width,
        float height
    ) {
        return Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            identifier,
            FabricEntityType.Builder.createMob(factory, category, mob -> mob.defaultAttributes(attributes))
                .sized(width, height)
                .clientTrackingRange(8)
                .updateInterval(3)
                .fireImmune()
                .build(ResourceKey.create(Registries.ENTITY_TYPE, identifier))
        );
    }

    private static Item registerSpawnEgg(String path, EntityType<? extends Mob> entityType) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            id(path),
            new SpawnEggItem(new Item.Properties().spawnEgg(entityType))
        );
    }
}
