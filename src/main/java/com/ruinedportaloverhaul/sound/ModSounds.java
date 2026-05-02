package com.ruinedportaloverhaul.sound;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class ModSounds {
    public static final SoundEvent WEATHER_RED_STORM_MUSIC = register("weather.red_storm.music");
    public static final SoundEvent WEATHER_RED_STORM_RUMBLE = register("weather.red_storm.rumble");
    public static final SoundEvent WEATHER_RED_THUNDER = register("weather.red_thunder");
    public static final SoundEvent WEATHER_RED_THUNDER_LOW = register("weather.red_thunder_low");
    public static final SoundEvent WEATHER_RED_THUNDER_PORTAL = register("weather.red_thunder_portal");
    public static final SoundEvent AMBIENT_PORTAL_LAVA = register("ambient.portal_lava");
    public static final SoundEvent AMBIENT_PORTAL_GHAST = register("ambient.portal_ghast");

    public static final SoundEvent BLOCK_NETHER_CONDUIT_AMBIENT = register("block.nether_conduit.ambient");
    public static final SoundEvent BLOCK_NETHER_CONDUIT_ACTIVATE = register("block.nether_conduit.activate");
    public static final SoundEvent BLOCK_NETHER_CONDUIT_DEACTIVATE = register("block.nether_conduit.deactivate");

    public static final SoundEvent ENTITY_PIGLIN_PILLAGER_AMBIENT = register("entity.piglin_pillager.ambient");
    public static final SoundEvent ENTITY_PIGLIN_PILLAGER_HURT = register("entity.piglin_pillager.hurt");
    public static final SoundEvent ENTITY_PIGLIN_PILLAGER_DEATH = register("entity.piglin_pillager.death");
    public static final SoundEvent ENTITY_PIGLIN_PILLAGER_ATTACK = register("entity.piglin_pillager.attack");
    public static final SoundEvent ENTITY_PIGLIN_VINDICATOR_AMBIENT = register("entity.piglin_vindicator.ambient");
    public static final SoundEvent ENTITY_PIGLIN_VINDICATOR_HURT = register("entity.piglin_vindicator.hurt");
    public static final SoundEvent ENTITY_PIGLIN_VINDICATOR_DEATH = register("entity.piglin_vindicator.death");
    public static final SoundEvent ENTITY_PIGLIN_BRUTE_PILLAGER_AMBIENT = register("entity.piglin_brute_pillager.ambient");
    public static final SoundEvent ENTITY_PIGLIN_BRUTE_PILLAGER_HURT = register("entity.piglin_brute_pillager.hurt");
    public static final SoundEvent ENTITY_PIGLIN_BRUTE_PILLAGER_DEATH = register("entity.piglin_brute_pillager.death");
    public static final SoundEvent ENTITY_PIGLIN_BRUTE_PILLAGER_ATTACK = register("entity.piglin_brute_pillager.attack");
    public static final SoundEvent ENTITY_PIGLIN_ILLUSIONER_AMBIENT = register("entity.piglin_illusioner.ambient");
    public static final SoundEvent ENTITY_PIGLIN_ILLUSIONER_HURT = register("entity.piglin_illusioner.hurt");
    public static final SoundEvent ENTITY_PIGLIN_ILLUSIONER_DEATH = register("entity.piglin_illusioner.death");
    public static final SoundEvent ENTITY_PIGLIN_ILLUSIONER_ATTACK = register("entity.piglin_illusioner.attack");
    public static final SoundEvent ENTITY_PIGLIN_EVOKER_AMBIENT = register("entity.piglin_evoker.ambient");
    public static final SoundEvent ENTITY_PIGLIN_EVOKER_HURT = register("entity.piglin_evoker.hurt");
    public static final SoundEvent ENTITY_PIGLIN_EVOKER_DEATH = register("entity.piglin_evoker.death");
    public static final SoundEvent ENTITY_PIGLIN_EVOKER_CAST_SPELL = register("entity.piglin_evoker.cast_spell");
    public static final SoundEvent ENTITY_PIGLIN_RAVAGER_AMBIENT = register("entity.piglin_ravager.ambient");
    public static final SoundEvent ENTITY_PIGLIN_RAVAGER_HURT = register("entity.piglin_ravager.hurt");
    public static final SoundEvent ENTITY_PIGLIN_RAVAGER_DEATH = register("entity.piglin_ravager.death");
    public static final SoundEvent ENTITY_PIGLIN_RAVAGER_ROAR = register("entity.piglin_ravager.roar");
    public static final SoundEvent ENTITY_PIGLIN_VEX_AMBIENT = register("entity.piglin_vex.ambient");
    public static final SoundEvent ENTITY_PIGLIN_VEX_HURT = register("entity.piglin_vex.hurt");
    public static final SoundEvent ENTITY_PIGLIN_VEX_DEATH = register("entity.piglin_vex.death");
    public static final SoundEvent ENTITY_EXILED_PIGLIN_AMBIENT = register("entity.exiled_piglin.ambient");
    public static final SoundEvent ENTITY_EXILED_PIGLIN_HURT = register("entity.exiled_piglin.hurt");
    public static final SoundEvent ENTITY_EXILED_PIGLIN_DEATH = register("entity.exiled_piglin.death");
    public static final SoundEvent ENTITY_NETHER_DRAGON_AMBIENT = register("entity.nether_dragon.ambient");
    public static final SoundEvent ENTITY_NETHER_DRAGON_GROWL = register("entity.nether_dragon.growl");
    public static final SoundEvent ENTITY_NETHER_DRAGON_PHASE2 = register("entity.nether_dragon.phase2");

    public static final SoundEvent ITEM_GHAST_TEAR_NECKLACE_FIREBALL = register("item.ghast_tear_necklace.fireball");
    public static final SoundEvent ITEM_PORTAL_SHARD_LOCATE = register("item.portal_shard.locate");
    public static final SoundEvent MUSIC_DISC_NETHER_TIDE = register("music.disc.nether_tide");

    public static final SoundEvent RAID_APPROACH = register("raid.approach");
    public static final SoundEvent RAID_START = register("raid.start");
    public static final SoundEvent RAID_WAVE_COMPLETE = register("raid.wave_complete");
    public static final SoundEvent RAID_COMPLETE = register("raid.complete");
    public static final SoundEvent RITUAL_VICTORY = register("ritual.victory");
    public static final SoundEvent RITUAL_CRYSTAL_PLACE = register("ritual.crystal_place");
    public static final SoundEvent RITUAL_DRAGON_SUMMON = register("ritual.dragon_summon");
    public static final SoundEvent RITUAL_PEDESTAL_SHATTER = register("ritual.pedestal_shatter");

    private ModSounds() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered custom sound events");
    }

    private static SoundEvent register(String path) {
        Identifier id = Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}
