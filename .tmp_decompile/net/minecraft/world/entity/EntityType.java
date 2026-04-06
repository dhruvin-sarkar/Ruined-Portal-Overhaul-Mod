/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityProcessor;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Interaction;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.bee.Bee;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.camel.CamelHusk;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.MushroomCow;
import net.minecraft.world.entity.animal.dolphin.Dolphin;
import net.minecraft.world.entity.animal.equine.Donkey;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.equine.TraderLlama;
import net.minecraft.world.entity.animal.equine.ZombieHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.feline.Ocelot;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.fish.Pufferfish;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.frog.Tadpole;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.animal.nautilus.Nautilus;
import net.minecraft.world.entity.animal.nautilus.ZombieNautilus;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.animal.polarbear.PolarBear;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.entity.animal.squid.GlowSquid;
import net.minecraft.world.entity.animal.squid.Squid;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.decoration.Mannequin;
import net.minecraft.world.entity.decoration.painting.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Giant;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Strider;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.monster.creaking.Creaking;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.Illusioner;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.Vindicator;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.entity.monster.skeleton.Parched;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.skeleton.Stray;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.entity.monster.spider.CaveSpider;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.LlamaSpit;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.arrow.SpectralArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.BreezeWindCharge;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownExperienceBottle;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownLingeringPotion;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.minecraft.world.entity.vehicle.boat.ChestRaft;
import net.minecraft.world.entity.vehicle.boat.Raft;
import net.minecraft.world.entity.vehicle.minecart.Minecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartChest;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.entity.vehicle.minecart.MinecartHopper;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.entity.vehicle.minecart.MinecartTNT;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class EntityType<T extends Entity>
implements FeatureElement,
EntityTypeTest<Entity, T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Holder.Reference<EntityType<?>> builtInRegistryHolder = BuiltInRegistries.ENTITY_TYPE.createIntrusiveHolder(this);
    public static final Codec<EntityType<?>> CODEC = BuiltInRegistries.ENTITY_TYPE.byNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityType<?>> STREAM_CODEC = ByteBufCodecs.registry(Registries.ENTITY_TYPE);
    private static final float MAGIC_HORSE_WIDTH = 1.3964844f;
    private static final int DISPLAY_TRACKING_RANGE = 10;
    public static final EntityType<Boat> ACACIA_BOAT = EntityType.register("acacia_boat", Builder.of(EntityType.boatFactory(() -> Items.ACACIA_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> ACACIA_CHEST_BOAT = EntityType.register("acacia_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.ACACIA_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Allay> ALLAY = EntityType.register("allay", Builder.of(Allay::new, MobCategory.CREATURE).sized(0.35f, 0.6f).eyeHeight(0.36f).ridingOffset(0.04f).clientTrackingRange(8).updateInterval(2));
    public static final EntityType<AreaEffectCloud> AREA_EFFECT_CLOUD = EntityType.register("area_effect_cloud", Builder.of(AreaEffectCloud::new, MobCategory.MISC).noLootTable().fireImmune().sized(6.0f, 0.5f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
    public static final EntityType<Armadillo> ARMADILLO = EntityType.register("armadillo", Builder.of(Armadillo::new, MobCategory.CREATURE).sized(0.7f, 0.65f).eyeHeight(0.26f).clientTrackingRange(10));
    public static final EntityType<ArmorStand> ARMOR_STAND = EntityType.register("armor_stand", Builder.of(ArmorStand::new, MobCategory.MISC).sized(0.5f, 1.975f).eyeHeight(1.7775f).clientTrackingRange(10));
    public static final EntityType<Arrow> ARROW = EntityType.register("arrow", Builder.of(Arrow::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).eyeHeight(0.13f).clientTrackingRange(4).updateInterval(20));
    public static final EntityType<Axolotl> AXOLOTL = EntityType.register("axolotl", Builder.of(Axolotl::new, MobCategory.AXOLOTLS).sized(0.75f, 0.42f).eyeHeight(0.2751f).clientTrackingRange(10));
    public static final EntityType<ChestRaft> BAMBOO_CHEST_RAFT = EntityType.register("bamboo_chest_raft", Builder.of(EntityType.chestRaftFactory(() -> Items.BAMBOO_CHEST_RAFT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Raft> BAMBOO_RAFT = EntityType.register("bamboo_raft", Builder.of(EntityType.raftFactory(() -> Items.BAMBOO_RAFT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Bat> BAT = EntityType.register("bat", Builder.of(Bat::new, MobCategory.AMBIENT).sized(0.5f, 0.9f).eyeHeight(0.45f).clientTrackingRange(5));
    public static final EntityType<Bee> BEE = EntityType.register("bee", Builder.of(Bee::new, MobCategory.CREATURE).sized(0.7f, 0.6f).eyeHeight(0.3f).clientTrackingRange(8));
    public static final EntityType<Boat> BIRCH_BOAT = EntityType.register("birch_boat", Builder.of(EntityType.boatFactory(() -> Items.BIRCH_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> BIRCH_CHEST_BOAT = EntityType.register("birch_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.BIRCH_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Blaze> BLAZE = EntityType.register("blaze", Builder.of(Blaze::new, MobCategory.MONSTER).fireImmune().sized(0.6f, 1.8f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Display.BlockDisplay> BLOCK_DISPLAY = EntityType.register("block_display", Builder.of(Display.BlockDisplay::new, MobCategory.MISC).noLootTable().sized(0.0f, 0.0f).clientTrackingRange(10).updateInterval(1));
    public static final EntityType<Bogged> BOGGED = EntityType.register("bogged", Builder.of(Bogged::new, MobCategory.MONSTER).sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Breeze> BREEZE = EntityType.register("breeze", Builder.of(Breeze::new, MobCategory.MONSTER).sized(0.6f, 1.77f).eyeHeight(1.3452f).clientTrackingRange(10).notInPeaceful());
    public static final EntityType<BreezeWindCharge> BREEZE_WIND_CHARGE = EntityType.register("breeze_wind_charge", Builder.of(BreezeWindCharge::new, MobCategory.MISC).noLootTable().sized(0.3125f, 0.3125f).eyeHeight(0.0f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<Camel> CAMEL = EntityType.register("camel", Builder.of(Camel::new, MobCategory.CREATURE).sized(1.7f, 2.375f).eyeHeight(2.275f).clientTrackingRange(10));
    public static final EntityType<CamelHusk> CAMEL_HUSK = EntityType.register("camel_husk", Builder.of(CamelHusk::new, MobCategory.MONSTER).sized(1.7f, 2.375f).eyeHeight(2.275f).clientTrackingRange(10));
    public static final EntityType<Cat> CAT = EntityType.register("cat", Builder.of(Cat::new, MobCategory.CREATURE).sized(0.6f, 0.7f).eyeHeight(0.35f).passengerAttachments(0.5125f).clientTrackingRange(8));
    public static final EntityType<CaveSpider> CAVE_SPIDER = EntityType.register("cave_spider", Builder.of(CaveSpider::new, MobCategory.MONSTER).sized(0.7f, 0.5f).eyeHeight(0.45f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Boat> CHERRY_BOAT = EntityType.register("cherry_boat", Builder.of(EntityType.boatFactory(() -> Items.CHERRY_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> CHERRY_CHEST_BOAT = EntityType.register("cherry_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.CHERRY_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<MinecartChest> CHEST_MINECART = EntityType.register("chest_minecart", Builder.of(MinecartChest::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.7f).passengerAttachments(0.1875f).clientTrackingRange(8));
    public static final EntityType<Chicken> CHICKEN = EntityType.register("chicken", Builder.of(Chicken::new, MobCategory.CREATURE).sized(0.4f, 0.7f).eyeHeight(0.644f).passengerAttachments(new Vec3(0.0, 0.7, -0.1)).clientTrackingRange(10));
    public static final EntityType<Cod> COD = EntityType.register("cod", Builder.of(Cod::new, MobCategory.WATER_AMBIENT).sized(0.5f, 0.3f).eyeHeight(0.195f).clientTrackingRange(4));
    public static final EntityType<CopperGolem> COPPER_GOLEM = EntityType.register("copper_golem", Builder.of(CopperGolem::new, MobCategory.MISC).sized(0.49f, 0.98f).eyeHeight(0.8125f).clientTrackingRange(10));
    public static final EntityType<MinecartCommandBlock> COMMAND_BLOCK_MINECART = EntityType.register("command_block_minecart", Builder.of(MinecartCommandBlock::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.7f).passengerAttachments(0.1875f).clientTrackingRange(8));
    public static final EntityType<Cow> COW = EntityType.register("cow", Builder.of(Cow::new, MobCategory.CREATURE).sized(0.9f, 1.4f).eyeHeight(1.3f).passengerAttachments(1.36875f).clientTrackingRange(10));
    public static final EntityType<Creaking> CREAKING = EntityType.register("creaking", Builder.of(Creaking::new, MobCategory.MONSTER).sized(0.9f, 2.7f).eyeHeight(2.3f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Creeper> CREEPER = EntityType.register("creeper", Builder.of(Creeper::new, MobCategory.MONSTER).sized(0.6f, 1.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Boat> DARK_OAK_BOAT = EntityType.register("dark_oak_boat", Builder.of(EntityType.boatFactory(() -> Items.DARK_OAK_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> DARK_OAK_CHEST_BOAT = EntityType.register("dark_oak_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.DARK_OAK_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Dolphin> DOLPHIN = EntityType.register("dolphin", Builder.of(Dolphin::new, MobCategory.WATER_CREATURE).sized(0.9f, 0.6f).eyeHeight(0.3f));
    public static final EntityType<Donkey> DONKEY = EntityType.register("donkey", Builder.of(Donkey::new, MobCategory.CREATURE).sized(1.3964844f, 1.5f).eyeHeight(1.425f).passengerAttachments(1.1125f).clientTrackingRange(10));
    public static final EntityType<DragonFireball> DRAGON_FIREBALL = EntityType.register("dragon_fireball", Builder.of(DragonFireball::new, MobCategory.MISC).noLootTable().sized(1.0f, 1.0f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<Drowned> DROWNED = EntityType.register("drowned", Builder.of(Drowned::new, MobCategory.MONSTER).sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.0125f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<ThrownEgg> EGG = EntityType.register("egg", Builder.of(ThrownEgg::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<ElderGuardian> ELDER_GUARDIAN = EntityType.register("elder_guardian", Builder.of(ElderGuardian::new, MobCategory.MONSTER).sized(1.9975f, 1.9975f).eyeHeight(0.99875f).passengerAttachments(2.350625f).clientTrackingRange(10).notInPeaceful());
    public static final EntityType<EnderMan> ENDERMAN = EntityType.register("enderman", Builder.of(EnderMan::new, MobCategory.MONSTER).sized(0.6f, 2.9f).eyeHeight(2.55f).passengerAttachments(2.80625f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Endermite> ENDERMITE = EntityType.register("endermite", Builder.of(Endermite::new, MobCategory.MONSTER).sized(0.4f, 0.3f).eyeHeight(0.13f).passengerAttachments(0.2375f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<EnderDragon> ENDER_DRAGON = EntityType.register("ender_dragon", Builder.of(EnderDragon::new, MobCategory.MONSTER).fireImmune().sized(16.0f, 8.0f).passengerAttachments(3.0f).clientTrackingRange(10));
    public static final EntityType<ThrownEnderpearl> ENDER_PEARL = EntityType.register("ender_pearl", Builder.of(ThrownEnderpearl::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<EndCrystal> END_CRYSTAL = EntityType.register("end_crystal", Builder.of(EndCrystal::new, MobCategory.MISC).noLootTable().fireImmune().sized(2.0f, 2.0f).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE));
    public static final EntityType<Evoker> EVOKER = EntityType.register("evoker", Builder.of(Evoker::new, MobCategory.MONSTER).sized(0.6f, 1.95f).passengerAttachments(2.0f).ridingOffset(-0.6f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<EvokerFangs> EVOKER_FANGS = EntityType.register("evoker_fangs", Builder.of(EvokerFangs::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.8f).clientTrackingRange(6).updateInterval(2));
    public static final EntityType<ThrownExperienceBottle> EXPERIENCE_BOTTLE = EntityType.register("experience_bottle", Builder.of(ThrownExperienceBottle::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<ExperienceOrb> EXPERIENCE_ORB = EntityType.register("experience_orb", Builder.of(ExperienceOrb::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).clientTrackingRange(6).updateInterval(20));
    public static final EntityType<EyeOfEnder> EYE_OF_ENDER = EntityType.register("eye_of_ender", Builder.of(EyeOfEnder::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(4));
    public static final EntityType<FallingBlockEntity> FALLING_BLOCK = EntityType.register("falling_block", Builder.of(FallingBlockEntity::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.98f).clientTrackingRange(10).updateInterval(20));
    public static final EntityType<LargeFireball> FIREBALL = EntityType.register("fireball", Builder.of(LargeFireball::new, MobCategory.MISC).noLootTable().sized(1.0f, 1.0f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<FireworkRocketEntity> FIREWORK_ROCKET = EntityType.register("firework_rocket", Builder.of(FireworkRocketEntity::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<Fox> FOX = EntityType.register("fox", Builder.of(Fox::new, MobCategory.CREATURE).sized(0.6f, 0.7f).eyeHeight(0.4f).passengerAttachments(new Vec3(0.0, 0.6375, -0.25)).clientTrackingRange(8).immuneTo(Blocks.SWEET_BERRY_BUSH));
    public static final EntityType<Frog> FROG = EntityType.register("frog", Builder.of(Frog::new, MobCategory.CREATURE).sized(0.5f, 0.5f).passengerAttachments(new Vec3(0.0, 0.375, -0.25)).clientTrackingRange(10));
    public static final EntityType<MinecartFurnace> FURNACE_MINECART = EntityType.register("furnace_minecart", Builder.of(MinecartFurnace::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.7f).passengerAttachments(0.1875f).clientTrackingRange(8));
    public static final EntityType<Ghast> GHAST = EntityType.register("ghast", Builder.of(Ghast::new, MobCategory.MONSTER).fireImmune().sized(4.0f, 4.0f).eyeHeight(2.6f).passengerAttachments(4.0625f).ridingOffset(0.5f).clientTrackingRange(10).notInPeaceful());
    public static final EntityType<HappyGhast> HAPPY_GHAST = EntityType.register("happy_ghast", Builder.of(HappyGhast::new, MobCategory.CREATURE).sized(4.0f, 4.0f).eyeHeight(2.6f).passengerAttachments(new Vec3(0.0, 4.0, 1.7), new Vec3(-1.7, 4.0, 0.0), new Vec3(0.0, 4.0, -1.7), new Vec3(1.7, 4.0, 0.0)).ridingOffset(0.5f).clientTrackingRange(10));
    public static final EntityType<Giant> GIANT = EntityType.register("giant", Builder.of(Giant::new, MobCategory.MONSTER).sized(3.6f, 12.0f).eyeHeight(10.44f).ridingOffset(-3.75f).clientTrackingRange(10).notInPeaceful());
    public static final EntityType<GlowItemFrame> GLOW_ITEM_FRAME = EntityType.register("glow_item_frame", Builder.of(GlowItemFrame::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).eyeHeight(0.0f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
    public static final EntityType<GlowSquid> GLOW_SQUID = EntityType.register("glow_squid", Builder.of(GlowSquid::new, MobCategory.UNDERGROUND_WATER_CREATURE).sized(0.8f, 0.8f).eyeHeight(0.4f).clientTrackingRange(10));
    public static final EntityType<Goat> GOAT = EntityType.register("goat", Builder.of(Goat::new, MobCategory.CREATURE).sized(0.9f, 1.3f).passengerAttachments(1.1125f).clientTrackingRange(10));
    public static final EntityType<Guardian> GUARDIAN = EntityType.register("guardian", Builder.of(Guardian::new, MobCategory.MONSTER).sized(0.85f, 0.85f).eyeHeight(0.425f).passengerAttachments(0.975f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Hoglin> HOGLIN = EntityType.register("hoglin", Builder.of(Hoglin::new, MobCategory.MONSTER).sized(1.3964844f, 1.4f).passengerAttachments(1.49375f).clientTrackingRange(8));
    public static final EntityType<MinecartHopper> HOPPER_MINECART = EntityType.register("hopper_minecart", Builder.of(MinecartHopper::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.7f).passengerAttachments(0.1875f).clientTrackingRange(8));
    public static final EntityType<Horse> HORSE = EntityType.register("horse", Builder.of(Horse::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.44375f).clientTrackingRange(10));
    public static final EntityType<Husk> HUSK = EntityType.register("husk", Builder.of(Husk::new, MobCategory.MONSTER).sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.075f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Illusioner> ILLUSIONER = EntityType.register("illusioner", Builder.of(Illusioner::new, MobCategory.MONSTER).sized(0.6f, 1.95f).passengerAttachments(2.0f).ridingOffset(-0.6f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Interaction> INTERACTION = EntityType.register("interaction", Builder.of(Interaction::new, MobCategory.MISC).noLootTable().sized(0.0f, 0.0f).clientTrackingRange(10));
    public static final EntityType<IronGolem> IRON_GOLEM = EntityType.register("iron_golem", Builder.of(IronGolem::new, MobCategory.MISC).sized(1.4f, 2.7f).clientTrackingRange(10));
    public static final EntityType<ItemEntity> ITEM = EntityType.register("item", Builder.of(ItemEntity::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).eyeHeight(0.2125f).clientTrackingRange(6).updateInterval(20));
    public static final EntityType<Display.ItemDisplay> ITEM_DISPLAY = EntityType.register("item_display", Builder.of(Display.ItemDisplay::new, MobCategory.MISC).noLootTable().sized(0.0f, 0.0f).clientTrackingRange(10).updateInterval(1));
    public static final EntityType<ItemFrame> ITEM_FRAME = EntityType.register("item_frame", Builder.of(ItemFrame::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).eyeHeight(0.0f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
    public static final EntityType<Boat> JUNGLE_BOAT = EntityType.register("jungle_boat", Builder.of(EntityType.boatFactory(() -> Items.JUNGLE_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> JUNGLE_CHEST_BOAT = EntityType.register("jungle_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.JUNGLE_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<LeashFenceKnotEntity> LEASH_KNOT = EntityType.register("leash_knot", Builder.of(LeashFenceKnotEntity::new, MobCategory.MISC).noLootTable().noSave().sized(0.375f, 0.5f).eyeHeight(0.0625f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
    public static final EntityType<LightningBolt> LIGHTNING_BOLT = EntityType.register("lightning_bolt", Builder.of(LightningBolt::new, MobCategory.MISC).noLootTable().noSave().sized(0.0f, 0.0f).clientTrackingRange(16).updateInterval(Integer.MAX_VALUE));
    public static final EntityType<Llama> LLAMA = EntityType.register("llama", Builder.of(Llama::new, MobCategory.CREATURE).sized(0.9f, 1.87f).eyeHeight(1.7765f).passengerAttachments(new Vec3(0.0, 1.37, -0.3)).clientTrackingRange(10));
    public static final EntityType<LlamaSpit> LLAMA_SPIT = EntityType.register("llama_spit", Builder.of(LlamaSpit::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<MagmaCube> MAGMA_CUBE = EntityType.register("magma_cube", Builder.of(MagmaCube::new, MobCategory.MONSTER).fireImmune().sized(0.52f, 0.52f).eyeHeight(0.325f).spawnDimensionsScale(4.0f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Boat> MANGROVE_BOAT = EntityType.register("mangrove_boat", Builder.of(EntityType.boatFactory(() -> Items.MANGROVE_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> MANGROVE_CHEST_BOAT = EntityType.register("mangrove_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.MANGROVE_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Mannequin> MANNEQUIN = EntityType.register("mannequin", Builder.of(Mannequin::create, MobCategory.MISC).sized(0.6f, 1.8f).eyeHeight(1.62f).vehicleAttachment(Avatar.DEFAULT_VEHICLE_ATTACHMENT).clientTrackingRange(32).updateInterval(2));
    public static final EntityType<Marker> MARKER = EntityType.register("marker", Builder.of(Marker::new, MobCategory.MISC).noLootTable().sized(0.0f, 0.0f).clientTrackingRange(0));
    public static final EntityType<Minecart> MINECART = EntityType.register("minecart", Builder.of(Minecart::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.7f).passengerAttachments(0.1875f).clientTrackingRange(8));
    public static final EntityType<MushroomCow> MOOSHROOM = EntityType.register("mooshroom", Builder.of(MushroomCow::new, MobCategory.CREATURE).sized(0.9f, 1.4f).eyeHeight(1.3f).passengerAttachments(1.36875f).clientTrackingRange(10));
    public static final EntityType<Mule> MULE = EntityType.register("mule", Builder.of(Mule::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.2125f).clientTrackingRange(8));
    public static final EntityType<Nautilus> NAUTILUS = EntityType.register("nautilus", Builder.of(Nautilus::new, MobCategory.WATER_CREATURE).sized(0.875f, 0.95f).passengerAttachments(1.1375f).eyeHeight(0.2751f).clientTrackingRange(10));
    public static final EntityType<Boat> OAK_BOAT = EntityType.register("oak_boat", Builder.of(EntityType.boatFactory(() -> Items.OAK_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> OAK_CHEST_BOAT = EntityType.register("oak_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.OAK_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Ocelot> OCELOT = EntityType.register("ocelot", Builder.of(Ocelot::new, MobCategory.CREATURE).sized(0.6f, 0.7f).passengerAttachments(0.6375f).clientTrackingRange(10));
    public static final EntityType<OminousItemSpawner> OMINOUS_ITEM_SPAWNER = EntityType.register("ominous_item_spawner", Builder.of(OminousItemSpawner::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(8));
    public static final EntityType<Painting> PAINTING = EntityType.register("painting", Builder.of(Painting::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).clientTrackingRange(10).updateInterval(Integer.MAX_VALUE));
    public static final EntityType<Boat> PALE_OAK_BOAT = EntityType.register("pale_oak_boat", Builder.of(EntityType.boatFactory(() -> Items.PALE_OAK_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> PALE_OAK_CHEST_BOAT = EntityType.register("pale_oak_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.PALE_OAK_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Panda> PANDA = EntityType.register("panda", Builder.of(Panda::new, MobCategory.CREATURE).sized(1.3f, 1.25f).clientTrackingRange(10));
    public static final EntityType<Parched> PARCHED = EntityType.register("parched", Builder.of(Parched::new, MobCategory.MONSTER).sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Parrot> PARROT = EntityType.register("parrot", Builder.of(Parrot::new, MobCategory.CREATURE).sized(0.5f, 0.9f).eyeHeight(0.54f).passengerAttachments(0.4625f).clientTrackingRange(8));
    public static final EntityType<Phantom> PHANTOM = EntityType.register("phantom", Builder.of(Phantom::new, MobCategory.MONSTER).sized(0.9f, 0.5f).eyeHeight(0.175f).passengerAttachments(0.3375f).ridingOffset(-0.125f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Pig> PIG = EntityType.register("pig", Builder.of(Pig::new, MobCategory.CREATURE).sized(0.9f, 0.9f).passengerAttachments(0.86875f).clientTrackingRange(10));
    public static final EntityType<Piglin> PIGLIN = EntityType.register("piglin", Builder.of(Piglin::new, MobCategory.MONSTER).sized(0.6f, 1.95f).eyeHeight(1.79f).passengerAttachments(2.0125f).ridingOffset(-0.7f).clientTrackingRange(8));
    public static final EntityType<PiglinBrute> PIGLIN_BRUTE = EntityType.register("piglin_brute", Builder.of(PiglinBrute::new, MobCategory.MONSTER).sized(0.6f, 1.95f).eyeHeight(1.79f).passengerAttachments(2.0125f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Pillager> PILLAGER = EntityType.register("pillager", Builder.of(Pillager::new, MobCategory.MONSTER).canSpawnFarFromPlayer().sized(0.6f, 1.95f).passengerAttachments(2.0f).ridingOffset(-0.6f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<PolarBear> POLAR_BEAR = EntityType.register("polar_bear", Builder.of(PolarBear::new, MobCategory.CREATURE).immuneTo(Blocks.POWDER_SNOW).sized(1.4f, 1.4f).clientTrackingRange(10));
    public static final EntityType<ThrownSplashPotion> SPLASH_POTION = EntityType.register("splash_potion", Builder.of(ThrownSplashPotion::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<ThrownLingeringPotion> LINGERING_POTION = EntityType.register("lingering_potion", Builder.of(ThrownLingeringPotion::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<Pufferfish> PUFFERFISH = EntityType.register("pufferfish", Builder.of(Pufferfish::new, MobCategory.WATER_AMBIENT).sized(0.7f, 0.7f).eyeHeight(0.455f).clientTrackingRange(4));
    public static final EntityType<Rabbit> RABBIT = EntityType.register("rabbit", Builder.of(Rabbit::new, MobCategory.CREATURE).sized(0.4f, 0.5f).clientTrackingRange(8));
    public static final EntityType<Ravager> RAVAGER = EntityType.register("ravager", Builder.of(Ravager::new, MobCategory.MONSTER).sized(1.95f, 2.2f).passengerAttachments(new Vec3(0.0, 2.2625, -0.0625)).clientTrackingRange(10).notInPeaceful());
    public static final EntityType<Salmon> SALMON = EntityType.register("salmon", Builder.of(Salmon::new, MobCategory.WATER_AMBIENT).sized(0.7f, 0.4f).eyeHeight(0.26f).clientTrackingRange(4));
    public static final EntityType<Sheep> SHEEP = EntityType.register("sheep", Builder.of(Sheep::new, MobCategory.CREATURE).sized(0.9f, 1.3f).eyeHeight(1.235f).passengerAttachments(1.2375f).clientTrackingRange(10));
    public static final EntityType<Shulker> SHULKER = EntityType.register("shulker", Builder.of(Shulker::new, MobCategory.MONSTER).fireImmune().canSpawnFarFromPlayer().sized(1.0f, 1.0f).eyeHeight(0.5f).clientTrackingRange(10));
    public static final EntityType<ShulkerBullet> SHULKER_BULLET = EntityType.register("shulker_bullet", Builder.of(ShulkerBullet::new, MobCategory.MISC).noLootTable().sized(0.3125f, 0.3125f).clientTrackingRange(8));
    public static final EntityType<Silverfish> SILVERFISH = EntityType.register("silverfish", Builder.of(Silverfish::new, MobCategory.MONSTER).sized(0.4f, 0.3f).eyeHeight(0.13f).passengerAttachments(0.2375f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Skeleton> SKELETON = EntityType.register("skeleton", Builder.of(Skeleton::new, MobCategory.MONSTER).sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<SkeletonHorse> SKELETON_HORSE = EntityType.register("skeleton_horse", Builder.of(SkeletonHorse::new, MobCategory.CREATURE).sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.31875f).clientTrackingRange(10));
    public static final EntityType<Slime> SLIME = EntityType.register("slime", Builder.of(Slime::new, MobCategory.MONSTER).sized(0.52f, 0.52f).eyeHeight(0.325f).spawnDimensionsScale(4.0f).clientTrackingRange(10).notInPeaceful());
    public static final EntityType<SmallFireball> SMALL_FIREBALL = EntityType.register("small_fireball", Builder.of(SmallFireball::new, MobCategory.MISC).noLootTable().sized(0.3125f, 0.3125f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<Sniffer> SNIFFER = EntityType.register("sniffer", Builder.of(Sniffer::new, MobCategory.CREATURE).sized(1.9f, 1.75f).eyeHeight(1.05f).passengerAttachments(2.09375f).nameTagOffset(2.05f).clientTrackingRange(10));
    public static final EntityType<Snowball> SNOWBALL = EntityType.register("snowball", Builder.of(Snowball::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<SnowGolem> SNOW_GOLEM = EntityType.register("snow_golem", Builder.of(SnowGolem::new, MobCategory.MISC).immuneTo(Blocks.POWDER_SNOW).sized(0.7f, 1.9f).eyeHeight(1.7f).clientTrackingRange(8));
    public static final EntityType<MinecartSpawner> SPAWNER_MINECART = EntityType.register("spawner_minecart", Builder.of(MinecartSpawner::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.7f).passengerAttachments(0.1875f).clientTrackingRange(8));
    public static final EntityType<SpectralArrow> SPECTRAL_ARROW = EntityType.register("spectral_arrow", Builder.of(SpectralArrow::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).eyeHeight(0.13f).clientTrackingRange(4).updateInterval(20));
    public static final EntityType<Spider> SPIDER = EntityType.register("spider", Builder.of(Spider::new, MobCategory.MONSTER).sized(1.4f, 0.9f).eyeHeight(0.65f).passengerAttachments(0.765f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Boat> SPRUCE_BOAT = EntityType.register("spruce_boat", Builder.of(EntityType.boatFactory(() -> Items.SPRUCE_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<ChestBoat> SPRUCE_CHEST_BOAT = EntityType.register("spruce_chest_boat", Builder.of(EntityType.chestBoatFactory(() -> Items.SPRUCE_CHEST_BOAT), MobCategory.MISC).noLootTable().sized(1.375f, 0.5625f).eyeHeight(0.5625f).clientTrackingRange(10));
    public static final EntityType<Squid> SQUID = EntityType.register("squid", Builder.of(Squid::new, MobCategory.WATER_CREATURE).sized(0.8f, 0.8f).eyeHeight(0.4f).clientTrackingRange(8));
    public static final EntityType<Stray> STRAY = EntityType.register("stray", Builder.of(Stray::new, MobCategory.MONSTER).sized(0.6f, 1.99f).eyeHeight(1.74f).ridingOffset(-0.7f).immuneTo(Blocks.POWDER_SNOW).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Strider> STRIDER = EntityType.register("strider", Builder.of(Strider::new, MobCategory.CREATURE).fireImmune().sized(0.9f, 1.7f).clientTrackingRange(10));
    public static final EntityType<Tadpole> TADPOLE = EntityType.register("tadpole", Builder.of(Tadpole::new, MobCategory.CREATURE).sized(0.4f, 0.3f).eyeHeight(0.19500001f).clientTrackingRange(10));
    public static final EntityType<Display.TextDisplay> TEXT_DISPLAY = EntityType.register("text_display", Builder.of(Display.TextDisplay::new, MobCategory.MISC).noLootTable().sized(0.0f, 0.0f).clientTrackingRange(10).updateInterval(1));
    public static final EntityType<PrimedTnt> TNT = EntityType.register("tnt", Builder.of(PrimedTnt::new, MobCategory.MISC).noLootTable().fireImmune().sized(0.98f, 0.98f).eyeHeight(0.15f).clientTrackingRange(10).updateInterval(10));
    public static final EntityType<MinecartTNT> TNT_MINECART = EntityType.register("tnt_minecart", Builder.of(MinecartTNT::new, MobCategory.MISC).noLootTable().sized(0.98f, 0.7f).passengerAttachments(0.1875f).clientTrackingRange(8));
    public static final EntityType<TraderLlama> TRADER_LLAMA = EntityType.register("trader_llama", Builder.of(TraderLlama::new, MobCategory.CREATURE).sized(0.9f, 1.87f).eyeHeight(1.7765f).passengerAttachments(new Vec3(0.0, 1.37, -0.3)).clientTrackingRange(10));
    public static final EntityType<ThrownTrident> TRIDENT = EntityType.register("trident", Builder.of(ThrownTrident::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).eyeHeight(0.13f).clientTrackingRange(4).updateInterval(20));
    public static final EntityType<TropicalFish> TROPICAL_FISH = EntityType.register("tropical_fish", Builder.of(TropicalFish::new, MobCategory.WATER_AMBIENT).sized(0.5f, 0.4f).eyeHeight(0.26f).clientTrackingRange(4));
    public static final EntityType<Turtle> TURTLE = EntityType.register("turtle", Builder.of(Turtle::new, MobCategory.CREATURE).sized(1.2f, 0.4f).passengerAttachments(new Vec3(0.0, 0.55625, -0.25)).clientTrackingRange(10));
    public static final EntityType<Vex> VEX = EntityType.register("vex", Builder.of(Vex::new, MobCategory.MONSTER).fireImmune().sized(0.4f, 0.8f).eyeHeight(0.51875f).passengerAttachments(0.7375f).ridingOffset(0.04f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Villager> VILLAGER = EntityType.register("villager", Builder.of(Villager::new, MobCategory.MISC).sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10));
    public static final EntityType<Vindicator> VINDICATOR = EntityType.register("vindicator", Builder.of(Vindicator::new, MobCategory.MONSTER).sized(0.6f, 1.95f).passengerAttachments(2.0f).ridingOffset(-0.6f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<WanderingTrader> WANDERING_TRADER = EntityType.register("wandering_trader", Builder.of(WanderingTrader::new, MobCategory.CREATURE).sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10));
    public static final EntityType<Warden> WARDEN = EntityType.register("warden", Builder.of(Warden::new, MobCategory.MONSTER).sized(0.9f, 2.9f).passengerAttachments(3.15f).attach(EntityAttachment.WARDEN_CHEST, 0.0f, 1.6f, 0.0f).clientTrackingRange(16).fireImmune().notInPeaceful());
    public static final EntityType<WindCharge> WIND_CHARGE = EntityType.register("wind_charge", Builder.of(WindCharge::new, MobCategory.MISC).noLootTable().sized(0.3125f, 0.3125f).eyeHeight(0.0f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<Witch> WITCH = EntityType.register("witch", Builder.of(Witch::new, MobCategory.MONSTER).sized(0.6f, 1.95f).eyeHeight(1.62f).passengerAttachments(2.2625f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<WitherBoss> WITHER = EntityType.register("wither", Builder.of(WitherBoss::new, MobCategory.MONSTER).fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.9f, 3.5f).clientTrackingRange(10).notInPeaceful());
    public static final EntityType<WitherSkeleton> WITHER_SKELETON = EntityType.register("wither_skeleton", Builder.of(WitherSkeleton::new, MobCategory.MONSTER).fireImmune().immuneTo(Blocks.WITHER_ROSE).sized(0.7f, 2.4f).eyeHeight(2.1f).ridingOffset(-0.875f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<WitherSkull> WITHER_SKULL = EntityType.register("wither_skull", Builder.of(WitherSkull::new, MobCategory.MISC).noLootTable().sized(0.3125f, 0.3125f).clientTrackingRange(4).updateInterval(10));
    public static final EntityType<Wolf> WOLF = EntityType.register("wolf", Builder.of(Wolf::new, MobCategory.CREATURE).sized(0.6f, 0.85f).eyeHeight(0.68f).passengerAttachments(new Vec3(0.0, 0.81875, -0.0625)).clientTrackingRange(10));
    public static final EntityType<Zoglin> ZOGLIN = EntityType.register("zoglin", Builder.of(Zoglin::new, MobCategory.MONSTER).fireImmune().sized(1.3964844f, 1.4f).passengerAttachments(1.49375f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Zombie> ZOMBIE = EntityType.register("zombie", Builder.of(Zombie::new, MobCategory.MONSTER).sized(0.6f, 1.95f).eyeHeight(1.74f).passengerAttachments(2.0125f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<ZombieHorse> ZOMBIE_HORSE = EntityType.register("zombie_horse", Builder.of(ZombieHorse::new, MobCategory.MONSTER).sized(1.3964844f, 1.6f).eyeHeight(1.52f).passengerAttachments(1.31875f).clientTrackingRange(10));
    public static final EntityType<ZombieNautilus> ZOMBIE_NAUTILUS = EntityType.register("zombie_nautilus", Builder.of(ZombieNautilus::new, MobCategory.MONSTER).sized(0.875f, 0.95f).passengerAttachments(1.1375f).eyeHeight(0.2751f).clientTrackingRange(10));
    public static final EntityType<ZombieVillager> ZOMBIE_VILLAGER = EntityType.register("zombie_villager", Builder.of(ZombieVillager::new, MobCategory.MONSTER).sized(0.6f, 1.95f).passengerAttachments(2.125f).ridingOffset(-0.7f).eyeHeight(1.74f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<ZombifiedPiglin> ZOMBIFIED_PIGLIN = EntityType.register("zombified_piglin", Builder.of(ZombifiedPiglin::new, MobCategory.MONSTER).fireImmune().sized(0.6f, 1.95f).eyeHeight(1.79f).passengerAttachments(2.0f).ridingOffset(-0.7f).clientTrackingRange(8).notInPeaceful());
    public static final EntityType<Player> PLAYER = EntityType.register("player", Builder.createNothing(MobCategory.MISC).noSave().noSummon().sized(0.6f, 1.8f).eyeHeight(1.62f).vehicleAttachment(Avatar.DEFAULT_VEHICLE_ATTACHMENT).clientTrackingRange(32).updateInterval(2));
    public static final EntityType<FishingHook> FISHING_BOBBER = EntityType.register("fishing_bobber", Builder.of(FishingHook::new, MobCategory.MISC).noLootTable().noSave().noSummon().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(5));
    private static final Set<EntityType<?>> OP_ONLY_CUSTOM_DATA = Set.of(FALLING_BLOCK, COMMAND_BLOCK_MINECART, SPAWNER_MINECART);
    private final EntityFactory<T> factory;
    private final MobCategory category;
    private final ImmutableSet<Block> immuneTo;
    private final boolean serialize;
    private final boolean summon;
    private final boolean fireImmune;
    private final boolean canSpawnFarFromPlayer;
    private final int clientTrackingRange;
    private final int updateInterval;
    private final String descriptionId;
    private @Nullable Component description;
    private final Optional<ResourceKey<LootTable>> lootTable;
    private final EntityDimensions dimensions;
    private final float spawnDimensionsScale;
    private final FeatureFlagSet requiredFeatures;
    private final boolean allowedInPeaceful;

    private static <T extends Entity> EntityType<T> register(ResourceKey<EntityType<?>> resourceKey, Builder<T> builder) {
        return Registry.register(BuiltInRegistries.ENTITY_TYPE, resourceKey, builder.build(resourceKey));
    }

    private static ResourceKey<EntityType<?>> vanillaEntityId(String string) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace(string));
    }

    private static <T extends Entity> EntityType<T> register(String string, Builder<T> builder) {
        return EntityType.register(EntityType.vanillaEntityId(string), builder);
    }

    public static Identifier getKey(EntityType<?> entityType) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    }

    public static Optional<EntityType<?>> byString(String string) {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(Identifier.tryParse(string));
    }

    public EntityType(EntityFactory<T> entityFactory, MobCategory mobCategory, boolean bl, boolean bl2, boolean bl3, boolean bl4, ImmutableSet<Block> immutableSet, EntityDimensions entityDimensions, float f, int i, int j, String string, Optional<ResourceKey<LootTable>> optional, FeatureFlagSet featureFlagSet, boolean bl5) {
        this.factory = entityFactory;
        this.category = mobCategory;
        this.canSpawnFarFromPlayer = bl4;
        this.serialize = bl;
        this.summon = bl2;
        this.fireImmune = bl3;
        this.immuneTo = immutableSet;
        this.dimensions = entityDimensions;
        this.spawnDimensionsScale = f;
        this.clientTrackingRange = i;
        this.updateInterval = j;
        this.descriptionId = string;
        this.lootTable = optional;
        this.requiredFeatures = featureFlagSet;
        this.allowedInPeaceful = bl5;
    }

    public @Nullable T spawn(ServerLevel serverLevel, @Nullable ItemStack itemStack, @Nullable LivingEntity livingEntity, BlockPos blockPos, EntitySpawnReason entitySpawnReason, boolean bl, boolean bl2) {
        Consumer<Entity> consumer = itemStack != null ? EntityType.createDefaultStackConfig(serverLevel, itemStack, livingEntity) : entity -> {};
        return (T)this.spawn(serverLevel, consumer, blockPos, entitySpawnReason, bl, bl2);
    }

    public static <T extends Entity> Consumer<T> createDefaultStackConfig(Level level, ItemStack itemStack, @Nullable LivingEntity livingEntity) {
        return EntityType.appendDefaultStackConfig(entity -> {}, level, itemStack, livingEntity);
    }

    public static <T extends Entity> Consumer<T> appendDefaultStackConfig(Consumer<T> consumer, Level level, ItemStack itemStack, @Nullable LivingEntity livingEntity) {
        return EntityType.appendCustomEntityStackConfig(EntityType.appendComponentsConfig(consumer, itemStack), level, itemStack, livingEntity);
    }

    public static <T extends Entity> Consumer<T> appendComponentsConfig(Consumer<T> consumer, ItemStack itemStack) {
        return consumer.andThen(entity -> entity.applyComponentsFromItemStack(itemStack));
    }

    public static <T extends Entity> Consumer<T> appendCustomEntityStackConfig(Consumer<T> consumer, Level level, ItemStack itemStack, @Nullable LivingEntity livingEntity) {
        TypedEntityData<EntityType<?>> typedEntityData = itemStack.get(DataComponents.ENTITY_DATA);
        if (typedEntityData != null) {
            return consumer.andThen(entity -> EntityType.updateCustomEntityTag(level, livingEntity, entity, typedEntityData));
        }
        return consumer;
    }

    public @Nullable T spawn(ServerLevel serverLevel, BlockPos blockPos, EntitySpawnReason entitySpawnReason) {
        return this.spawn(serverLevel, null, blockPos, entitySpawnReason, false, false);
    }

    public @Nullable T spawn(ServerLevel serverLevel, @Nullable Consumer<T> consumer, BlockPos blockPos, EntitySpawnReason entitySpawnReason, boolean bl, boolean bl2) {
        T entity = this.create(serverLevel, consumer, blockPos, entitySpawnReason, bl, bl2);
        if (entity != null) {
            serverLevel.addFreshEntityWithPassengers((Entity)entity);
            if (entity instanceof Mob) {
                Mob mob = (Mob)entity;
                mob.playAmbientSound();
            }
        }
        return entity;
    }

    public @Nullable T create(ServerLevel serverLevel, @Nullable Consumer<T> consumer, BlockPos blockPos, EntitySpawnReason entitySpawnReason, boolean bl, boolean bl2) {
        double d;
        T entity = this.create(serverLevel, entitySpawnReason);
        if (entity == null) {
            return null;
        }
        if (bl) {
            ((Entity)entity).setPos((double)blockPos.getX() + 0.5, blockPos.getY() + 1, (double)blockPos.getZ() + 0.5);
            d = EntityType.getYOffset(serverLevel, blockPos, bl2, ((Entity)entity).getBoundingBox());
        } else {
            d = 0.0;
        }
        ((Entity)entity).snapTo((double)blockPos.getX() + 0.5, (double)blockPos.getY() + d, (double)blockPos.getZ() + 0.5, Mth.wrapDegrees(serverLevel.random.nextFloat() * 360.0f), 0.0f);
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            mob.yHeadRot = mob.getYRot();
            mob.yBodyRot = mob.getYRot();
            mob.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(mob.blockPosition()), entitySpawnReason, null);
        }
        if (consumer != null) {
            consumer.accept(entity);
        }
        return entity;
    }

    protected static double getYOffset(LevelReader levelReader, BlockPos blockPos, boolean bl, AABB aABB) {
        AABB aABB2 = new AABB(blockPos);
        if (bl) {
            aABB2 = aABB2.expandTowards(0.0, -1.0, 0.0);
        }
        Iterable<VoxelShape> iterable = levelReader.getCollisions(null, aABB2);
        return 1.0 + Shapes.collide(Direction.Axis.Y, aABB, iterable, bl ? -2.0 : -1.0);
    }

    public static void updateCustomEntityTag(Level level, @Nullable LivingEntity livingEntity, @Nullable Entity entity, TypedEntityData<EntityType<?>> typedEntityData) {
        block5: {
            block6: {
                MinecraftServer minecraftServer = level.getServer();
                if (minecraftServer == null || entity == null) {
                    return;
                }
                if (entity.getType() != typedEntityData.type()) {
                    return;
                }
                if (level.isClientSide() || !entity.getType().onlyOpCanSetNbt()) break block5;
                if (!(livingEntity instanceof Player)) break block6;
                Player player = (Player)livingEntity;
                if (minecraftServer.getPlayerList().isOp(player.nameAndId())) break block5;
            }
            return;
        }
        typedEntityData.loadInto(entity);
    }

    public boolean canSerialize() {
        return this.serialize;
    }

    public boolean canSummon() {
        return this.summon;
    }

    public boolean fireImmune() {
        return this.fireImmune;
    }

    public boolean canSpawnFarFromPlayer() {
        return this.canSpawnFarFromPlayer;
    }

    public MobCategory getCategory() {
        return this.category;
    }

    public String getDescriptionId() {
        return this.descriptionId;
    }

    public Component getDescription() {
        if (this.description == null) {
            this.description = Component.translatable(this.getDescriptionId());
        }
        return this.description;
    }

    public String toString() {
        return this.getDescriptionId();
    }

    public String toShortString() {
        int i = this.getDescriptionId().lastIndexOf(46);
        return i == -1 ? this.getDescriptionId() : this.getDescriptionId().substring(i + 1);
    }

    public Optional<ResourceKey<LootTable>> getDefaultLootTable() {
        return this.lootTable;
    }

    public float getWidth() {
        return this.dimensions.width();
    }

    public float getHeight() {
        return this.dimensions.height();
    }

    @Override
    public FeatureFlagSet requiredFeatures() {
        return this.requiredFeatures;
    }

    public @Nullable T create(Level level, EntitySpawnReason entitySpawnReason) {
        if (!this.isEnabled(level.enabledFeatures())) {
            return null;
        }
        return this.factory.create(this, level);
    }

    public static Optional<Entity> create(ValueInput valueInput, Level level, EntitySpawnReason entitySpawnReason) {
        return Util.ifElse(EntityType.by(valueInput).map(entityType -> entityType.create(level, entitySpawnReason)), entity -> entity.load(valueInput), () -> LOGGER.warn("Skipping Entity with id {}", (Object)valueInput.getStringOr("id", "[invalid]")));
    }

    public static Optional<Entity> create(EntityType<?> entityType, ValueInput valueInput, Level level, EntitySpawnReason entitySpawnReason) {
        Optional<Entity> optional = Optional.ofNullable(entityType.create(level, entitySpawnReason));
        optional.ifPresent(entity -> entity.load(valueInput));
        return optional;
    }

    public AABB getSpawnAABB(double d, double e, double f) {
        float g = this.spawnDimensionsScale * this.getWidth() / 2.0f;
        float h = this.spawnDimensionsScale * this.getHeight();
        return new AABB(d - (double)g, e, f - (double)g, d + (double)g, e + (double)h, f + (double)g);
    }

    public boolean isBlockDangerous(BlockState blockState) {
        if (this.immuneTo.contains((Object)blockState.getBlock())) {
            return false;
        }
        if (!this.fireImmune && NodeEvaluator.isBurningBlock(blockState)) {
            return true;
        }
        return blockState.is(Blocks.WITHER_ROSE) || blockState.is(Blocks.SWEET_BERRY_BUSH) || blockState.is(Blocks.CACTUS) || blockState.is(Blocks.POWDER_SNOW);
    }

    public EntityDimensions getDimensions() {
        return this.dimensions;
    }

    public static Optional<EntityType<?>> by(ValueInput valueInput) {
        return valueInput.read("id", CODEC);
    }

    public static @Nullable Entity loadEntityRecursive(CompoundTag compoundTag, Level level, EntitySpawnReason entitySpawnReason, EntityProcessor entityProcessor) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER);){
            Entity entity = EntityType.loadEntityRecursive(TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)level.registryAccess(), compoundTag), level, entitySpawnReason, entityProcessor);
            return entity;
        }
    }

    public static @Nullable Entity loadEntityRecursive(EntityType<?> entityType, CompoundTag compoundTag, Level level, EntitySpawnReason entitySpawnReason, EntityProcessor entityProcessor) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(LOGGER);){
            Entity entity = EntityType.loadEntityRecursive(entityType, TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)level.registryAccess(), compoundTag), level, entitySpawnReason, entityProcessor);
            return entity;
        }
    }

    public static @Nullable Entity loadEntityRecursive(ValueInput valueInput, Level level, EntitySpawnReason entitySpawnReason, EntityProcessor entityProcessor) {
        return EntityType.loadStaticEntity(valueInput, level, entitySpawnReason).map(entityProcessor::process).map(entity -> EntityType.loadPassengersRecursive(entity, valueInput, level, entitySpawnReason, entityProcessor)).orElse(null);
    }

    public static @Nullable Entity loadEntityRecursive(EntityType<?> entityType, ValueInput valueInput, Level level, EntitySpawnReason entitySpawnReason, EntityProcessor entityProcessor) {
        return EntityType.loadStaticEntity(entityType, valueInput, level, entitySpawnReason).map(entityProcessor::process).map(entity -> EntityType.loadPassengersRecursive(entity, valueInput, level, entitySpawnReason, entityProcessor)).orElse(null);
    }

    private static Entity loadPassengersRecursive(Entity entity, ValueInput valueInput, Level level, EntitySpawnReason entitySpawnReason, EntityProcessor entityProcessor) {
        for (ValueInput valueInput2 : valueInput.childrenListOrEmpty("Passengers")) {
            Entity entity2 = EntityType.loadEntityRecursive(valueInput2, level, entitySpawnReason, entityProcessor);
            if (entity2 == null) continue;
            entity2.startRiding(entity, true, false);
        }
        return entity;
    }

    public static Stream<Entity> loadEntitiesRecursive(ValueInput.ValueInputList valueInputList, Level level, EntitySpawnReason entitySpawnReason) {
        return valueInputList.stream().mapMulti((valueInput, consumer) -> EntityType.loadEntityRecursive(valueInput, level, entitySpawnReason, (Entity entity) -> {
            consumer.accept(entity);
            return entity;
        }));
    }

    private static Optional<Entity> loadStaticEntity(ValueInput valueInput, Level level, EntitySpawnReason entitySpawnReason) {
        try {
            return EntityType.create(valueInput, level, entitySpawnReason);
        }
        catch (RuntimeException runtimeException) {
            LOGGER.warn("Exception loading entity: ", (Throwable)runtimeException);
            return Optional.empty();
        }
    }

    private static Optional<Entity> loadStaticEntity(EntityType<?> entityType, ValueInput valueInput, Level level, EntitySpawnReason entitySpawnReason) {
        try {
            return EntityType.create(entityType, valueInput, level, entitySpawnReason);
        }
        catch (RuntimeException runtimeException) {
            LOGGER.warn("Exception loading entity: ", (Throwable)runtimeException);
            return Optional.empty();
        }
    }

    public int clientTrackingRange() {
        return this.clientTrackingRange;
    }

    public int updateInterval() {
        return this.updateInterval;
    }

    public boolean trackDeltas() {
        return this != PLAYER && this != LLAMA_SPIT && this != WITHER && this != BAT && this != ITEM_FRAME && this != GLOW_ITEM_FRAME && this != LEASH_KNOT && this != PAINTING && this != END_CRYSTAL && this != EVOKER_FANGS;
    }

    public boolean is(TagKey<EntityType<?>> tagKey) {
        return this.builtInRegistryHolder.is(tagKey);
    }

    public boolean is(HolderSet<EntityType<?>> holderSet) {
        return holderSet.contains(this.builtInRegistryHolder);
    }

    @Override
    public @Nullable T tryCast(Entity entity) {
        return (T)(entity.getType() == this ? entity : null);
    }

    @Override
    public Class<? extends Entity> getBaseClass() {
        return Entity.class;
    }

    @Deprecated
    public Holder.Reference<EntityType<?>> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public boolean isAllowedInPeaceful() {
        return this.allowedInPeaceful;
    }

    private static EntityFactory<Boat> boatFactory(Supplier<Item> supplier) {
        return (entityType, level) -> new Boat(entityType, level, supplier);
    }

    private static EntityFactory<ChestBoat> chestBoatFactory(Supplier<Item> supplier) {
        return (entityType, level) -> new ChestBoat(entityType, level, supplier);
    }

    private static EntityFactory<Raft> raftFactory(Supplier<Item> supplier) {
        return (entityType, level) -> new Raft(entityType, level, supplier);
    }

    private static EntityFactory<ChestRaft> chestRaftFactory(Supplier<Item> supplier) {
        return (entityType, level) -> new ChestRaft(entityType, level, supplier);
    }

    public boolean onlyOpCanSetNbt() {
        return OP_ONLY_CUSTOM_DATA.contains(this);
    }

    public static class Builder<T extends Entity> {
        private final EntityFactory<T> factory;
        private final MobCategory category;
        private ImmutableSet<Block> immuneTo = ImmutableSet.of();
        private boolean serialize = true;
        private boolean summon = true;
        private boolean fireImmune;
        private boolean canSpawnFarFromPlayer;
        private int clientTrackingRange = 5;
        private int updateInterval = 3;
        private EntityDimensions dimensions = EntityDimensions.scalable(0.6f, 1.8f);
        private float spawnDimensionsScale = 1.0f;
        private EntityAttachments.Builder attachments = EntityAttachments.builder();
        private FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
        private DependantName<EntityType<?>, Optional<ResourceKey<LootTable>>> lootTable = resourceKey -> Optional.of(ResourceKey.create(Registries.LOOT_TABLE, resourceKey.identifier().withPrefix("entities/")));
        private final DependantName<EntityType<?>, String> descriptionId = resourceKey -> Util.makeDescriptionId("entity", resourceKey.identifier());
        private boolean allowedInPeaceful = true;

        private Builder(EntityFactory<T> entityFactory, MobCategory mobCategory) {
            this.factory = entityFactory;
            this.category = mobCategory;
            this.canSpawnFarFromPlayer = mobCategory == MobCategory.CREATURE || mobCategory == MobCategory.MISC;
        }

        public static <T extends Entity> Builder<T> of(EntityFactory<T> entityFactory, MobCategory mobCategory) {
            return new Builder<T>(entityFactory, mobCategory);
        }

        public static <T extends Entity> Builder<T> createNothing(MobCategory mobCategory) {
            return new Builder<Entity>((entityType, level) -> null, mobCategory);
        }

        public Builder<T> sized(float f, float g) {
            this.dimensions = EntityDimensions.scalable(f, g);
            return this;
        }

        public Builder<T> spawnDimensionsScale(float f) {
            this.spawnDimensionsScale = f;
            return this;
        }

        public Builder<T> eyeHeight(float f) {
            this.dimensions = this.dimensions.withEyeHeight(f);
            return this;
        }

        public Builder<T> passengerAttachments(float ... fs) {
            for (float f : fs) {
                this.attachments = this.attachments.attach(EntityAttachment.PASSENGER, 0.0f, f, 0.0f);
            }
            return this;
        }

        public Builder<T> passengerAttachments(Vec3 ... vec3s) {
            for (Vec3 vec3 : vec3s) {
                this.attachments = this.attachments.attach(EntityAttachment.PASSENGER, vec3);
            }
            return this;
        }

        public Builder<T> vehicleAttachment(Vec3 vec3) {
            return this.attach(EntityAttachment.VEHICLE, vec3);
        }

        public Builder<T> ridingOffset(float f) {
            return this.attach(EntityAttachment.VEHICLE, 0.0f, -f, 0.0f);
        }

        public Builder<T> nameTagOffset(float f) {
            return this.attach(EntityAttachment.NAME_TAG, 0.0f, f, 0.0f);
        }

        public Builder<T> attach(EntityAttachment entityAttachment, float f, float g, float h) {
            this.attachments = this.attachments.attach(entityAttachment, f, g, h);
            return this;
        }

        public Builder<T> attach(EntityAttachment entityAttachment, Vec3 vec3) {
            this.attachments = this.attachments.attach(entityAttachment, vec3);
            return this;
        }

        public Builder<T> noSummon() {
            this.summon = false;
            return this;
        }

        public Builder<T> noSave() {
            this.serialize = false;
            return this;
        }

        public Builder<T> fireImmune() {
            this.fireImmune = true;
            return this;
        }

        public Builder<T> immuneTo(Block ... blocks) {
            this.immuneTo = ImmutableSet.copyOf((Object[])blocks);
            return this;
        }

        public Builder<T> canSpawnFarFromPlayer() {
            this.canSpawnFarFromPlayer = true;
            return this;
        }

        public Builder<T> clientTrackingRange(int i) {
            this.clientTrackingRange = i;
            return this;
        }

        public Builder<T> updateInterval(int i) {
            this.updateInterval = i;
            return this;
        }

        public Builder<T> requiredFeatures(FeatureFlag ... featureFlags) {
            this.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
            return this;
        }

        public Builder<T> noLootTable() {
            this.lootTable = DependantName.fixed(Optional.empty());
            return this;
        }

        public Builder<T> notInPeaceful() {
            this.allowedInPeaceful = false;
            return this;
        }

        public EntityType<T> build(ResourceKey<EntityType<?>> resourceKey) {
            if (this.serialize) {
                Util.fetchChoiceType(References.ENTITY_TREE, resourceKey.identifier().toString());
            }
            return new EntityType<T>(this.factory, this.category, this.serialize, this.summon, this.fireImmune, this.canSpawnFarFromPlayer, this.immuneTo, this.dimensions.withAttachments(this.attachments), this.spawnDimensionsScale, this.clientTrackingRange, this.updateInterval, this.descriptionId.get(resourceKey), this.lootTable.get(resourceKey), this.requiredFeatures, this.allowedInPeaceful);
        }
    }

    @FunctionalInterface
    public static interface EntityFactory<T extends Entity> {
        public @Nullable T create(EntityType<T> var1, Level var2);
    }
}

