package com.ruinedportaloverhaul.item;

import com.ruinedportaloverhaul.RuinedPortalOverhaul;
import com.ruinedportaloverhaul.component.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;

public final class ModItems {
    public static final Identifier GHAST_TEAR_NECKLACE_ID = id("ghast_tear_necklace");
    public static final Identifier NETHER_CRYSTAL_ID = id("nether_crystal");
    public static final Identifier CORRUPTED_NETHERITE_INGOT_ID = id("corrupted_netherite_ingot");
    public static final Identifier CORRUPTED_NETHERITE_HELMET_ID = id("corrupted_netherite_helmet");
    public static final Identifier CORRUPTED_NETHERITE_CHESTPLATE_ID = id("corrupted_netherite_chestplate");
    public static final Identifier CORRUPTED_NETHERITE_LEGGINGS_ID = id("corrupted_netherite_leggings");
    public static final Identifier CORRUPTED_NETHERITE_BOOTS_ID = id("corrupted_netherite_boots");
    public static final Identifier PORTAL_SHARD_ID = id("portal_shard");
    public static final Identifier NETHER_DRAGON_SCALE_ID = id("nether_dragon_scale");
    public static final Identifier MUSIC_DISC_NETHER_TIDE_ID = id("music_disc_nether_tide");
    public static final Identifier CORRUPTED_PORTAL_KEY_ID = id("corrupted_portal_key");
    public static final Identifier SHARD_OF_THE_NETHER_ID = id("shard_of_the_nether");
    public static final Identifier CORRUPTED_RAVAGER_HIDE_ID = id("corrupted_ravager_hide");
    public static final Identifier EMBERED_GRIMOIRE_ID = id("embered_grimoire");
    public static final Identifier VOIDASH_POWDER_ID = id("voidash_powder");
    public static final ResourceKey<JukeboxSong> NETHER_TIDE_SONG_KEY = ResourceKey.create(Registries.JUKEBOX_SONG, id("nether_tide"));

    public static final GhastTearNecklaceItem GHAST_TEAR_NECKLACE = Registry.register(
        BuiltInRegistries.ITEM,
        GHAST_TEAR_NECKLACE_ID,
        new GhastTearNecklaceItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, GHAST_TEAR_NECKLACE_ID))
            .stacksTo(1)
            .fireResistant())
    );
    public static final NetherCrystalItem NETHER_CRYSTAL = Registry.register(
        BuiltInRegistries.ITEM,
        NETHER_CRYSTAL_ID,
        new NetherCrystalItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, NETHER_CRYSTAL_ID))
            .fireResistant())
    );
    public static final CorruptedNetheriteIngotItem CORRUPTED_NETHERITE_INGOT = Registry.register(
        BuiltInRegistries.ITEM,
        CORRUPTED_NETHERITE_INGOT_ID,
        new CorruptedNetheriteIngotItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, CORRUPTED_NETHERITE_INGOT_ID))
            .component(DataComponents.CUSTOM_DATA, dragonInfusedIngotData())
            .fireResistant()
            .rarity(Rarity.EPIC))
    );
    public static final CorruptedNetheriteArmorItem CORRUPTED_NETHERITE_HELMET = registerCorruptedArmor(
        CORRUPTED_NETHERITE_HELMET_ID,
        ArmorType.HELMET
    );
    public static final CorruptedNetheriteArmorItem CORRUPTED_NETHERITE_CHESTPLATE = registerCorruptedArmor(
        CORRUPTED_NETHERITE_CHESTPLATE_ID,
        ArmorType.CHESTPLATE
    );
    public static final CorruptedNetheriteArmorItem CORRUPTED_NETHERITE_LEGGINGS = registerCorruptedArmor(
        CORRUPTED_NETHERITE_LEGGINGS_ID,
        ArmorType.LEGGINGS
    );
    public static final CorruptedNetheriteArmorItem CORRUPTED_NETHERITE_BOOTS = registerCorruptedArmor(
        CORRUPTED_NETHERITE_BOOTS_ID,
        ArmorType.BOOTS
    );
    public static final PortalShardItem PORTAL_SHARD = Registry.register(
        BuiltInRegistries.ITEM,
        PORTAL_SHARD_ID,
        new PortalShardItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, PORTAL_SHARD_ID))
            .stacksTo(1)
            .rarity(Rarity.RARE))
    );
    public static final DragonScaleItem NETHER_DRAGON_SCALE = Registry.register(
        BuiltInRegistries.ITEM,
        NETHER_DRAGON_SCALE_ID,
        new DragonScaleItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, NETHER_DRAGON_SCALE_ID))
            .stacksTo(1)
            .fireResistant()
            .rarity(Rarity.EPIC))
    );
    public static final NetherTideDiscItem MUSIC_DISC_NETHER_TIDE = Registry.register(
        BuiltInRegistries.ITEM,
        MUSIC_DISC_NETHER_TIDE_ID,
        new NetherTideDiscItem(new Item.Properties()
            .setId(ResourceKey.create(Registries.ITEM, MUSIC_DISC_NETHER_TIDE_ID))
            .jukeboxPlayable(NETHER_TIDE_SONG_KEY)
            .stacksTo(1)
            .rarity(Rarity.RARE))
    );
    public static final LoreArtifactItem CORRUPTED_PORTAL_KEY = registerLoreArtifact(
        CORRUPTED_PORTAL_KEY_ID,
        "corrupted_portal_key",
        Rarity.RARE,
        ChatFormatting.DARK_RED
    );
    public static final LoreArtifactItem SHARD_OF_THE_NETHER = registerLoreArtifact(
        SHARD_OF_THE_NETHER_ID,
        "shard_of_the_nether",
        Rarity.EPIC,
        ChatFormatting.DARK_PURPLE
    );
    public static final LoreArtifactItem CORRUPTED_RAVAGER_HIDE = registerLoreArtifact(
        CORRUPTED_RAVAGER_HIDE_ID,
        "corrupted_ravager_hide",
        Rarity.RARE,
        ChatFormatting.DARK_RED
    );
    public static final LoreArtifactItem EMBERED_GRIMOIRE = registerLoreArtifact(
        EMBERED_GRIMOIRE_ID,
        "embered_grimoire",
        Rarity.RARE,
        ChatFormatting.GOLD
    );
    public static final LoreArtifactItem VOIDASH_POWDER = registerLoreArtifact(
        VOIDASH_POWDER_ID,
        "voidash_powder",
        Rarity.RARE,
        ChatFormatting.DARK_PURPLE
    );

    private ModItems() {
    }

    public static void initialize() {
        RuinedPortalOverhaul.LOGGER.info("Registered ruined portal overhaul items");
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(RuinedPortalOverhaul.MOD_ID, path);
    }

    private static CorruptedNetheriteArmorItem registerCorruptedArmor(Identifier id, ArmorType armorType) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            id,
            new CorruptedNetheriteArmorItem(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .humanoidArmor(ArmorMaterials.NETHERITE, armorType)
                .component(ModDataComponents.CORRUPTED_NETHERITE, true)
                .component(DataComponents.CUSTOM_DATA, corruptedArmorData())
                .fireResistant()
                .rarity(Rarity.EPIC))
        );
    }

    private static LoreArtifactItem registerLoreArtifact(Identifier id, String itemPath, Rarity rarity, ChatFormatting accent) {
        return Registry.register(
            BuiltInRegistries.ITEM,
            id,
            new LoreArtifactItem(new Item.Properties()
                .setId(ResourceKey.create(Registries.ITEM, id))
                .fireResistant()
                .rarity(rarity), itemPath, accent)
        );
    }

    private static CustomData corruptedArmorData() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(RuinedPortalOverhaul.MOD_ID + ":corrupted", true);
        return CustomData.of(tag);
    }

    private static CustomData dragonInfusedIngotData() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(RuinedPortalOverhaul.MOD_ID + ":dragon_infused", true);
        return CustomData.of(tag);
    }
}
