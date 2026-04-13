package com.ruinedportaloverhaul.entity;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import java.util.function.Supplier;
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

    private ModEntities() {
    }

    public static void initialize() {
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
