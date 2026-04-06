/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.entity;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.animal.squid.SquidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.AxolotlRenderer;
import net.minecraft.client.renderer.entity.BatRenderer;
import net.minecraft.client.renderer.entity.BeeRenderer;
import net.minecraft.client.renderer.entity.BlazeRenderer;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.BoggedRenderer;
import net.minecraft.client.renderer.entity.BreezeRenderer;
import net.minecraft.client.renderer.entity.CamelHuskRenderer;
import net.minecraft.client.renderer.entity.CamelRenderer;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.CaveSpiderRenderer;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.CodRenderer;
import net.minecraft.client.renderer.entity.CopperGolemRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.client.renderer.entity.CreakingRenderer;
import net.minecraft.client.renderer.entity.CreeperRenderer;
import net.minecraft.client.renderer.entity.DisplayRenderer;
import net.minecraft.client.renderer.entity.DolphinRenderer;
import net.minecraft.client.renderer.entity.DonkeyRenderer;
import net.minecraft.client.renderer.entity.DragonFireballRenderer;
import net.minecraft.client.renderer.entity.DrownedRenderer;
import net.minecraft.client.renderer.entity.ElderGuardianRenderer;
import net.minecraft.client.renderer.entity.EndCrystalRenderer;
import net.minecraft.client.renderer.entity.EnderDragonRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.EndermiteRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EvokerFangsRenderer;
import net.minecraft.client.renderer.entity.EvokerRenderer;
import net.minecraft.client.renderer.entity.ExperienceOrbRenderer;
import net.minecraft.client.renderer.entity.FallingBlockRenderer;
import net.minecraft.client.renderer.entity.FireworkEntityRenderer;
import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.entity.FoxRenderer;
import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.client.renderer.entity.GhastRenderer;
import net.minecraft.client.renderer.entity.GiantMobRenderer;
import net.minecraft.client.renderer.entity.GlowSquidRenderer;
import net.minecraft.client.renderer.entity.GoatRenderer;
import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.HoglinRenderer;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.client.renderer.entity.HuskRenderer;
import net.minecraft.client.renderer.entity.IllusionerRenderer;
import net.minecraft.client.renderer.entity.IronGolemRenderer;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.LeashKnotRenderer;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.client.renderer.entity.LlamaRenderer;
import net.minecraft.client.renderer.entity.LlamaSpitRenderer;
import net.minecraft.client.renderer.entity.MagmaCubeRenderer;
import net.minecraft.client.renderer.entity.MinecartRenderer;
import net.minecraft.client.renderer.entity.MushroomCowRenderer;
import net.minecraft.client.renderer.entity.NautilusRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.OcelotRenderer;
import net.minecraft.client.renderer.entity.OminousItemSpawnerRenderer;
import net.minecraft.client.renderer.entity.PaintingRenderer;
import net.minecraft.client.renderer.entity.PandaRenderer;
import net.minecraft.client.renderer.entity.ParchedRenderer;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.PhantomRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.PillagerRenderer;
import net.minecraft.client.renderer.entity.PolarBearRenderer;
import net.minecraft.client.renderer.entity.PufferfishRenderer;
import net.minecraft.client.renderer.entity.RabbitRenderer;
import net.minecraft.client.renderer.entity.RaftRenderer;
import net.minecraft.client.renderer.entity.RavagerRenderer;
import net.minecraft.client.renderer.entity.SalmonRenderer;
import net.minecraft.client.renderer.entity.SheepRenderer;
import net.minecraft.client.renderer.entity.ShulkerBulletRenderer;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.SilverfishRenderer;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.SnifferRenderer;
import net.minecraft.client.renderer.entity.SnowGolemRenderer;
import net.minecraft.client.renderer.entity.SpectralArrowRenderer;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.client.renderer.entity.SquidRenderer;
import net.minecraft.client.renderer.entity.StrayRenderer;
import net.minecraft.client.renderer.entity.StriderRenderer;
import net.minecraft.client.renderer.entity.TadpoleRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.entity.TntRenderer;
import net.minecraft.client.renderer.entity.TropicalFishRenderer;
import net.minecraft.client.renderer.entity.TurtleRenderer;
import net.minecraft.client.renderer.entity.UndeadHorseRenderer;
import net.minecraft.client.renderer.entity.VexRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.VindicatorRenderer;
import net.minecraft.client.renderer.entity.WanderingTraderRenderer;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.client.renderer.entity.WindChargeRenderer;
import net.minecraft.client.renderer.entity.WitchRenderer;
import net.minecraft.client.renderer.entity.WitherBossRenderer;
import net.minecraft.client.renderer.entity.WitherSkeletonRenderer;
import net.minecraft.client.renderer.entity.WitherSkullRenderer;
import net.minecraft.client.renderer.entity.WolfRenderer;
import net.minecraft.client.renderer.entity.ZoglinRenderer;
import net.minecraft.client.renderer.entity.ZombieNautilusRenderer;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.client.renderer.entity.ZombieVillagerRenderer;
import net.minecraft.client.renderer.entity.ZombifiedPiglinRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.PlayerModelType;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class EntityRenderers {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<EntityType<?>, EntityRendererProvider<?>> PROVIDERS = new Object2ObjectOpenHashMap();

    private static <T extends Entity> void register(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider) {
        PROVIDERS.put(entityType, entityRendererProvider);
    }

    public static Map<EntityType<?>, EntityRenderer<?, ?>> createEntityRenderers(EntityRendererProvider.Context context) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        PROVIDERS.forEach((entityType, entityRendererProvider) -> {
            try {
                builder.put(entityType, entityRendererProvider.create(context));
            }
            catch (Exception exception) {
                throw new IllegalArgumentException("Failed to create model for " + String.valueOf(BuiltInRegistries.ENTITY_TYPE.getKey((EntityType<?>)entityType)), exception);
            }
        });
        return builder.build();
    }

    public static <T extends Avatar> Map<PlayerModelType, AvatarRenderer<T>> createAvatarRenderers(EntityRendererProvider.Context context) {
        try {
            return Map.of((Object)PlayerModelType.WIDE, new AvatarRenderer(context, false), (Object)PlayerModelType.SLIM, new AvatarRenderer(context, true));
        }
        catch (Exception exception) {
            throw new IllegalArgumentException("Failed to create avatar models", exception);
        }
    }

    public static boolean validateRegistrations() {
        boolean bl = true;
        for (EntityType entityType : BuiltInRegistries.ENTITY_TYPE) {
            if (entityType == EntityType.PLAYER || entityType == EntityType.MANNEQUIN || PROVIDERS.containsKey(entityType)) continue;
            LOGGER.warn("No renderer registered for {}", (Object)BuiltInRegistries.ENTITY_TYPE.getKey(entityType));
            bl = false;
        }
        return !bl;
    }

    static {
        EntityRenderers.register(EntityType.ACACIA_BOAT, context -> new BoatRenderer(context, ModelLayers.ACACIA_BOAT));
        EntityRenderers.register(EntityType.ACACIA_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.ACACIA_CHEST_BOAT));
        EntityRenderers.register(EntityType.ALLAY, AllayRenderer::new);
        EntityRenderers.register(EntityType.AREA_EFFECT_CLOUD, NoopRenderer::new);
        EntityRenderers.register(EntityType.ARMADILLO, ArmadilloRenderer::new);
        EntityRenderers.register(EntityType.ARMOR_STAND, ArmorStandRenderer::new);
        EntityRenderers.register(EntityType.ARROW, TippableArrowRenderer::new);
        EntityRenderers.register(EntityType.AXOLOTL, AxolotlRenderer::new);
        EntityRenderers.register(EntityType.BAMBOO_CHEST_RAFT, context -> new RaftRenderer(context, ModelLayers.BAMBOO_CHEST_RAFT));
        EntityRenderers.register(EntityType.BAMBOO_RAFT, context -> new RaftRenderer(context, ModelLayers.BAMBOO_RAFT));
        EntityRenderers.register(EntityType.BAT, BatRenderer::new);
        EntityRenderers.register(EntityType.BEE, BeeRenderer::new);
        EntityRenderers.register(EntityType.BIRCH_BOAT, context -> new BoatRenderer(context, ModelLayers.BIRCH_BOAT));
        EntityRenderers.register(EntityType.BIRCH_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.BIRCH_CHEST_BOAT));
        EntityRenderers.register(EntityType.BLAZE, BlazeRenderer::new);
        EntityRenderers.register(EntityType.BLOCK_DISPLAY, DisplayRenderer.BlockDisplayRenderer::new);
        EntityRenderers.register(EntityType.BOGGED, BoggedRenderer::new);
        EntityRenderers.register(EntityType.BREEZE, BreezeRenderer::new);
        EntityRenderers.register(EntityType.BREEZE_WIND_CHARGE, WindChargeRenderer::new);
        EntityRenderers.register(EntityType.CAMEL, CamelRenderer::new);
        EntityRenderers.register(EntityType.CAMEL_HUSK, CamelHuskRenderer::new);
        EntityRenderers.register(EntityType.CAT, CatRenderer::new);
        EntityRenderers.register(EntityType.CAVE_SPIDER, CaveSpiderRenderer::new);
        EntityRenderers.register(EntityType.CHERRY_BOAT, context -> new BoatRenderer(context, ModelLayers.CHERRY_BOAT));
        EntityRenderers.register(EntityType.CHERRY_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.CHERRY_CHEST_BOAT));
        EntityRenderers.register(EntityType.CHEST_MINECART, context -> new MinecartRenderer(context, ModelLayers.CHEST_MINECART));
        EntityRenderers.register(EntityType.CHICKEN, ChickenRenderer::new);
        EntityRenderers.register(EntityType.COD, CodRenderer::new);
        EntityRenderers.register(EntityType.COMMAND_BLOCK_MINECART, context -> new MinecartRenderer(context, ModelLayers.COMMAND_BLOCK_MINECART));
        EntityRenderers.register(EntityType.COPPER_GOLEM, CopperGolemRenderer::new);
        EntityRenderers.register(EntityType.COW, CowRenderer::new);
        EntityRenderers.register(EntityType.CREAKING, CreakingRenderer::new);
        EntityRenderers.register(EntityType.CREEPER, CreeperRenderer::new);
        EntityRenderers.register(EntityType.DARK_OAK_BOAT, context -> new BoatRenderer(context, ModelLayers.DARK_OAK_BOAT));
        EntityRenderers.register(EntityType.DARK_OAK_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.DARK_OAK_CHEST_BOAT));
        EntityRenderers.register(EntityType.DOLPHIN, DolphinRenderer::new);
        EntityRenderers.register(EntityType.DONKEY, context -> new DonkeyRenderer(context, DonkeyRenderer.Type.DONKEY));
        EntityRenderers.register(EntityType.DRAGON_FIREBALL, DragonFireballRenderer::new);
        EntityRenderers.register(EntityType.DROWNED, DrownedRenderer::new);
        EntityRenderers.register(EntityType.EGG, ThrownItemRenderer::new);
        EntityRenderers.register(EntityType.ELDER_GUARDIAN, ElderGuardianRenderer::new);
        EntityRenderers.register(EntityType.ENDERMAN, EndermanRenderer::new);
        EntityRenderers.register(EntityType.ENDERMITE, EndermiteRenderer::new);
        EntityRenderers.register(EntityType.ENDER_DRAGON, EnderDragonRenderer::new);
        EntityRenderers.register(EntityType.ENDER_PEARL, ThrownItemRenderer::new);
        EntityRenderers.register(EntityType.END_CRYSTAL, EndCrystalRenderer::new);
        EntityRenderers.register(EntityType.EVOKER, EvokerRenderer::new);
        EntityRenderers.register(EntityType.EVOKER_FANGS, EvokerFangsRenderer::new);
        EntityRenderers.register(EntityType.EXPERIENCE_BOTTLE, ThrownItemRenderer::new);
        EntityRenderers.register(EntityType.EXPERIENCE_ORB, ExperienceOrbRenderer::new);
        EntityRenderers.register(EntityType.EYE_OF_ENDER, context -> new ThrownItemRenderer(context, 1.0f, true));
        EntityRenderers.register(EntityType.FALLING_BLOCK, FallingBlockRenderer::new);
        EntityRenderers.register(EntityType.FIREBALL, context -> new ThrownItemRenderer(context, 3.0f, true));
        EntityRenderers.register(EntityType.FIREWORK_ROCKET, FireworkEntityRenderer::new);
        EntityRenderers.register(EntityType.FISHING_BOBBER, FishingHookRenderer::new);
        EntityRenderers.register(EntityType.FOX, FoxRenderer::new);
        EntityRenderers.register(EntityType.FROG, FrogRenderer::new);
        EntityRenderers.register(EntityType.FURNACE_MINECART, context -> new MinecartRenderer(context, ModelLayers.FURNACE_MINECART));
        EntityRenderers.register(EntityType.GHAST, GhastRenderer::new);
        EntityRenderers.register(EntityType.HAPPY_GHAST, HappyGhastRenderer::new);
        EntityRenderers.register(EntityType.GIANT, context -> new GiantMobRenderer(context, 6.0f));
        EntityRenderers.register(EntityType.GLOW_ITEM_FRAME, ItemFrameRenderer::new);
        EntityRenderers.register(EntityType.GLOW_SQUID, context -> new GlowSquidRenderer(context, new SquidModel(context.bakeLayer(ModelLayers.GLOW_SQUID)), new SquidModel(context.bakeLayer(ModelLayers.GLOW_SQUID_BABY))));
        EntityRenderers.register(EntityType.GOAT, GoatRenderer::new);
        EntityRenderers.register(EntityType.GUARDIAN, GuardianRenderer::new);
        EntityRenderers.register(EntityType.HOGLIN, HoglinRenderer::new);
        EntityRenderers.register(EntityType.HOPPER_MINECART, context -> new MinecartRenderer(context, ModelLayers.HOPPER_MINECART));
        EntityRenderers.register(EntityType.HORSE, HorseRenderer::new);
        EntityRenderers.register(EntityType.HUSK, HuskRenderer::new);
        EntityRenderers.register(EntityType.ILLUSIONER, IllusionerRenderer::new);
        EntityRenderers.register(EntityType.INTERACTION, NoopRenderer::new);
        EntityRenderers.register(EntityType.IRON_GOLEM, IronGolemRenderer::new);
        EntityRenderers.register(EntityType.ITEM, ItemEntityRenderer::new);
        EntityRenderers.register(EntityType.ITEM_DISPLAY, DisplayRenderer.ItemDisplayRenderer::new);
        EntityRenderers.register(EntityType.ITEM_FRAME, ItemFrameRenderer::new);
        EntityRenderers.register(EntityType.JUNGLE_BOAT, context -> new BoatRenderer(context, ModelLayers.JUNGLE_BOAT));
        EntityRenderers.register(EntityType.JUNGLE_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.JUNGLE_CHEST_BOAT));
        EntityRenderers.register(EntityType.LEASH_KNOT, LeashKnotRenderer::new);
        EntityRenderers.register(EntityType.LIGHTNING_BOLT, LightningBoltRenderer::new);
        EntityRenderers.register(EntityType.LINGERING_POTION, ThrownItemRenderer::new);
        EntityRenderers.register(EntityType.LLAMA, context -> new LlamaRenderer(context, ModelLayers.LLAMA, ModelLayers.LLAMA_BABY));
        EntityRenderers.register(EntityType.LLAMA_SPIT, LlamaSpitRenderer::new);
        EntityRenderers.register(EntityType.MAGMA_CUBE, MagmaCubeRenderer::new);
        EntityRenderers.register(EntityType.MANGROVE_BOAT, context -> new BoatRenderer(context, ModelLayers.MANGROVE_BOAT));
        EntityRenderers.register(EntityType.MANGROVE_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.MANGROVE_CHEST_BOAT));
        EntityRenderers.register(EntityType.MARKER, NoopRenderer::new);
        EntityRenderers.register(EntityType.MINECART, context -> new MinecartRenderer(context, ModelLayers.MINECART));
        EntityRenderers.register(EntityType.MOOSHROOM, MushroomCowRenderer::new);
        EntityRenderers.register(EntityType.MULE, context -> new DonkeyRenderer(context, DonkeyRenderer.Type.MULE));
        EntityRenderers.register(EntityType.NAUTILUS, NautilusRenderer::new);
        EntityRenderers.register(EntityType.OAK_BOAT, context -> new BoatRenderer(context, ModelLayers.OAK_BOAT));
        EntityRenderers.register(EntityType.OAK_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.OAK_CHEST_BOAT));
        EntityRenderers.register(EntityType.OCELOT, OcelotRenderer::new);
        EntityRenderers.register(EntityType.OMINOUS_ITEM_SPAWNER, OminousItemSpawnerRenderer::new);
        EntityRenderers.register(EntityType.PAINTING, PaintingRenderer::new);
        EntityRenderers.register(EntityType.PALE_OAK_BOAT, context -> new BoatRenderer(context, ModelLayers.PALE_OAK_BOAT));
        EntityRenderers.register(EntityType.PALE_OAK_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.PALE_OAK_CHEST_BOAT));
        EntityRenderers.register(EntityType.PANDA, PandaRenderer::new);
        EntityRenderers.register(EntityType.PARCHED, ParchedRenderer::new);
        EntityRenderers.register(EntityType.PARROT, ParrotRenderer::new);
        EntityRenderers.register(EntityType.PHANTOM, PhantomRenderer::new);
        EntityRenderers.register(EntityType.PIG, PigRenderer::new);
        EntityRenderers.register(EntityType.PIGLIN, context -> new PiglinRenderer(context, ModelLayers.PIGLIN, ModelLayers.PIGLIN_BABY, ModelLayers.PIGLIN_ARMOR, ModelLayers.PIGLIN_BABY_ARMOR));
        EntityRenderers.register(EntityType.PIGLIN_BRUTE, context -> new PiglinRenderer(context, ModelLayers.PIGLIN_BRUTE, ModelLayers.PIGLIN_BRUTE, ModelLayers.PIGLIN_BRUTE_ARMOR, ModelLayers.PIGLIN_BRUTE_ARMOR));
        EntityRenderers.register(EntityType.PILLAGER, PillagerRenderer::new);
        EntityRenderers.register(EntityType.POLAR_BEAR, PolarBearRenderer::new);
        EntityRenderers.register(EntityType.PUFFERFISH, PufferfishRenderer::new);
        EntityRenderers.register(EntityType.RABBIT, RabbitRenderer::new);
        EntityRenderers.register(EntityType.RAVAGER, RavagerRenderer::new);
        EntityRenderers.register(EntityType.SALMON, SalmonRenderer::new);
        EntityRenderers.register(EntityType.SHEEP, SheepRenderer::new);
        EntityRenderers.register(EntityType.SHULKER, ShulkerRenderer::new);
        EntityRenderers.register(EntityType.SHULKER_BULLET, ShulkerBulletRenderer::new);
        EntityRenderers.register(EntityType.SILVERFISH, SilverfishRenderer::new);
        EntityRenderers.register(EntityType.SKELETON, SkeletonRenderer::new);
        EntityRenderers.register(EntityType.SKELETON_HORSE, context -> new UndeadHorseRenderer(context, UndeadHorseRenderer.Type.SKELETON));
        EntityRenderers.register(EntityType.SLIME, SlimeRenderer::new);
        EntityRenderers.register(EntityType.SMALL_FIREBALL, context -> new ThrownItemRenderer(context, 0.75f, true));
        EntityRenderers.register(EntityType.SNIFFER, SnifferRenderer::new);
        EntityRenderers.register(EntityType.SNOWBALL, ThrownItemRenderer::new);
        EntityRenderers.register(EntityType.SNOW_GOLEM, SnowGolemRenderer::new);
        EntityRenderers.register(EntityType.SPAWNER_MINECART, context -> new MinecartRenderer(context, ModelLayers.SPAWNER_MINECART));
        EntityRenderers.register(EntityType.SPECTRAL_ARROW, SpectralArrowRenderer::new);
        EntityRenderers.register(EntityType.SPIDER, SpiderRenderer::new);
        EntityRenderers.register(EntityType.SPLASH_POTION, ThrownItemRenderer::new);
        EntityRenderers.register(EntityType.SPRUCE_BOAT, context -> new BoatRenderer(context, ModelLayers.SPRUCE_BOAT));
        EntityRenderers.register(EntityType.SPRUCE_CHEST_BOAT, context -> new BoatRenderer(context, ModelLayers.SPRUCE_CHEST_BOAT));
        EntityRenderers.register(EntityType.SQUID, context -> new SquidRenderer(context, new SquidModel(context.bakeLayer(ModelLayers.SQUID)), new SquidModel(context.bakeLayer(ModelLayers.SQUID_BABY))));
        EntityRenderers.register(EntityType.STRAY, StrayRenderer::new);
        EntityRenderers.register(EntityType.STRIDER, StriderRenderer::new);
        EntityRenderers.register(EntityType.TADPOLE, TadpoleRenderer::new);
        EntityRenderers.register(EntityType.TEXT_DISPLAY, DisplayRenderer.TextDisplayRenderer::new);
        EntityRenderers.register(EntityType.TNT, TntRenderer::new);
        EntityRenderers.register(EntityType.TNT_MINECART, TntMinecartRenderer::new);
        EntityRenderers.register(EntityType.TRADER_LLAMA, context -> new LlamaRenderer(context, ModelLayers.TRADER_LLAMA, ModelLayers.TRADER_LLAMA_BABY));
        EntityRenderers.register(EntityType.TRIDENT, ThrownTridentRenderer::new);
        EntityRenderers.register(EntityType.TROPICAL_FISH, TropicalFishRenderer::new);
        EntityRenderers.register(EntityType.TURTLE, TurtleRenderer::new);
        EntityRenderers.register(EntityType.VEX, VexRenderer::new);
        EntityRenderers.register(EntityType.VILLAGER, VillagerRenderer::new);
        EntityRenderers.register(EntityType.VINDICATOR, VindicatorRenderer::new);
        EntityRenderers.register(EntityType.WANDERING_TRADER, WanderingTraderRenderer::new);
        EntityRenderers.register(EntityType.WARDEN, WardenRenderer::new);
        EntityRenderers.register(EntityType.WIND_CHARGE, WindChargeRenderer::new);
        EntityRenderers.register(EntityType.WITCH, WitchRenderer::new);
        EntityRenderers.register(EntityType.WITHER, WitherBossRenderer::new);
        EntityRenderers.register(EntityType.WITHER_SKELETON, WitherSkeletonRenderer::new);
        EntityRenderers.register(EntityType.WITHER_SKULL, WitherSkullRenderer::new);
        EntityRenderers.register(EntityType.WOLF, WolfRenderer::new);
        EntityRenderers.register(EntityType.ZOGLIN, ZoglinRenderer::new);
        EntityRenderers.register(EntityType.ZOMBIE, ZombieRenderer::new);
        EntityRenderers.register(EntityType.ZOMBIE_HORSE, context -> new UndeadHorseRenderer(context, UndeadHorseRenderer.Type.ZOMBIE));
        EntityRenderers.register(EntityType.ZOMBIE_NAUTILUS, ZombieNautilusRenderer::new);
        EntityRenderers.register(EntityType.ZOMBIE_VILLAGER, ZombieVillagerRenderer::new);
        EntityRenderers.register(EntityType.ZOMBIFIED_PIGLIN, context -> new ZombifiedPiglinRenderer(context, ModelLayers.ZOMBIFIED_PIGLIN, ModelLayers.ZOMBIFIED_PIGLIN_BABY, ModelLayers.ZOMBIFIED_PIGLIN_ARMOR, ModelLayers.ZOMBIFIED_PIGLIN_BABY_ARMOR));
    }
}

