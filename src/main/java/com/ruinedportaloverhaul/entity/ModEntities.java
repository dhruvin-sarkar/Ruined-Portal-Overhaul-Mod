package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
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

public final class ModEntities {
    public static final Identifier PIGLIN_PILLAGER_ID = id("piglin_pillager");
    public static final Identifier PIGLIN_VINDICATOR_ID = id("piglin_vindicator");
    public static final Identifier PIGLIN_BRUTE_PILLAGER_ID = id("piglin_brute_pillager");
    public static final Identifier PIGLIN_ILLUSIONER_ID = id("piglin_illusioner");
    public static final Identifier PIGLIN_EVOKER_ID = id("piglin_evoker");
    public static final Identifier PIGLIN_RAVAGER_ID = id("piglin_ravager");
    public static final Identifier PIGLIN_VEX_ID = id("piglin_vex");
    public static final Identifier EXILED_PIGLIN_ID = id("exiled_piglin");
    public static final Identifier NETHER_CRYSTAL_ID = id("nether_crystal");
    public static final Identifier NETHER_DRAGON_ID = id("nether_dragon");

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
    public static final EntityType<PiglinVexEntity> PIGLIN_VEX = registerMonster(
        PIGLIN_VEX_ID,
        PiglinVexEntity::new,
        PiglinVexEntity::createAttributes,
        0.4f,
        0.8f
    );
    public static final EntityType<ExiledPiglinTraderEntity> EXILED_PIGLIN = registerMob(
        EXILED_PIGLIN_ID,
        ExiledPiglinTraderEntity::new,
        ExiledPiglinTraderEntity::createAttributes,
        MobCategory.CREATURE,
        0.6f,
        1.95f
    );
    public static final EntityType<NetherCrystalEntity> NETHER_CRYSTAL = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        NETHER_CRYSTAL_ID,
        EntityType.Builder.<NetherCrystalEntity>of(NetherCrystalEntity::new, MobCategory.MISC)
            .sized(2.0f, 2.0f)
            .clientTrackingRange(16)
            .updateInterval(Integer.MAX_VALUE)
            .fireImmune()
            .build(ResourceKey.create(Registries.ENTITY_TYPE, NETHER_CRYSTAL_ID))
    );
    public static final EntityType<NetherDragonEntity> NETHER_DRAGON = Registry.register(
        BuiltInRegistries.ENTITY_TYPE,
        NETHER_DRAGON_ID,
        EntityType.Builder.<NetherDragonEntity>of(NetherDragonEntity::new, MobCategory.MONSTER)
            .sized(16.0f, 8.0f)
            .clientTrackingRange(10)
            .updateInterval(3)
            .fireImmune()
            .build(ResourceKey.create(Registries.ENTITY_TYPE, NETHER_DRAGON_ID))
    );

    private ModEntities() {
    }

    public static void initialize() {
        // Fix: the Nether Dragon is built with the plain entity builder, so its attributes now register explicitly instead of relying on createMob defaults that never run for this type.
        FabricDefaultAttributeRegistry.register(NETHER_DRAGON, NetherDragonEntity.createAttributes());
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

}
