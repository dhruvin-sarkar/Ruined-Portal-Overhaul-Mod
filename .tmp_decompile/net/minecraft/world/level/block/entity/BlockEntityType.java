/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block.entity;

import com.mojang.logging.LogUtils;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BellBlockEntity;
import net.minecraft.world.level.block.entity.BlastFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.CalibratedSculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.entity.DaylightDetectorBlockEntity;
import net.minecraft.world.level.block.entity.DecoratedPotBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.DropperBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.entity.SmokerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;
import net.minecraft.world.level.block.entity.TrappedChestBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class BlockEntityType<T extends BlockEntity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final BlockEntityType<FurnaceBlockEntity> FURNACE = BlockEntityType.register("furnace", FurnaceBlockEntity::new, Blocks.FURNACE);
    public static final BlockEntityType<ChestBlockEntity> CHEST = BlockEntityType.register("chest", ChestBlockEntity::new, Blocks.CHEST, Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER_CHEST, Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST, Blocks.WAXED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST, Blocks.WAXED_WEATHERED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST);
    public static final BlockEntityType<TrappedChestBlockEntity> TRAPPED_CHEST = BlockEntityType.register("trapped_chest", TrappedChestBlockEntity::new, Blocks.TRAPPED_CHEST);
    public static final BlockEntityType<EnderChestBlockEntity> ENDER_CHEST = BlockEntityType.register("ender_chest", EnderChestBlockEntity::new, Blocks.ENDER_CHEST);
    public static final BlockEntityType<JukeboxBlockEntity> JUKEBOX = BlockEntityType.register("jukebox", JukeboxBlockEntity::new, Blocks.JUKEBOX);
    public static final BlockEntityType<DispenserBlockEntity> DISPENSER = BlockEntityType.register("dispenser", DispenserBlockEntity::new, Blocks.DISPENSER);
    public static final BlockEntityType<DropperBlockEntity> DROPPER = BlockEntityType.register("dropper", DropperBlockEntity::new, Blocks.DROPPER);
    public static final BlockEntityType<SignBlockEntity> SIGN = BlockEntityType.register("sign", SignBlockEntity::new, Blocks.OAK_SIGN, Blocks.SPRUCE_SIGN, Blocks.BIRCH_SIGN, Blocks.ACACIA_SIGN, Blocks.CHERRY_SIGN, Blocks.JUNGLE_SIGN, Blocks.DARK_OAK_SIGN, Blocks.PALE_OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.PALE_OAK_WALL_SIGN, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN, Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN);
    public static final BlockEntityType<HangingSignBlockEntity> HANGING_SIGN = BlockEntityType.register("hanging_sign", HangingSignBlockEntity::new, Blocks.OAK_HANGING_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.PALE_OAK_HANGING_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
    public static final BlockEntityType<SpawnerBlockEntity> MOB_SPAWNER = BlockEntityType.register("mob_spawner", SpawnerBlockEntity::new, Blocks.SPAWNER);
    public static final BlockEntityType<CreakingHeartBlockEntity> CREAKING_HEART = BlockEntityType.register("creaking_heart", CreakingHeartBlockEntity::new, Blocks.CREAKING_HEART);
    public static final BlockEntityType<PistonMovingBlockEntity> PISTON = BlockEntityType.register("piston", PistonMovingBlockEntity::new, Blocks.MOVING_PISTON);
    public static final BlockEntityType<BrewingStandBlockEntity> BREWING_STAND = BlockEntityType.register("brewing_stand", BrewingStandBlockEntity::new, Blocks.BREWING_STAND);
    public static final BlockEntityType<EnchantingTableBlockEntity> ENCHANTING_TABLE = BlockEntityType.register("enchanting_table", EnchantingTableBlockEntity::new, Blocks.ENCHANTING_TABLE);
    public static final BlockEntityType<TheEndPortalBlockEntity> END_PORTAL = BlockEntityType.register("end_portal", TheEndPortalBlockEntity::new, Blocks.END_PORTAL);
    public static final BlockEntityType<BeaconBlockEntity> BEACON = BlockEntityType.register("beacon", BeaconBlockEntity::new, Blocks.BEACON);
    public static final BlockEntityType<SkullBlockEntity> SKULL = BlockEntityType.register("skull", SkullBlockEntity::new, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD);
    public static final BlockEntityType<DaylightDetectorBlockEntity> DAYLIGHT_DETECTOR = BlockEntityType.register("daylight_detector", DaylightDetectorBlockEntity::new, Blocks.DAYLIGHT_DETECTOR);
    public static final BlockEntityType<HopperBlockEntity> HOPPER = BlockEntityType.register("hopper", HopperBlockEntity::new, Blocks.HOPPER);
    public static final BlockEntityType<ComparatorBlockEntity> COMPARATOR = BlockEntityType.register("comparator", ComparatorBlockEntity::new, Blocks.COMPARATOR);
    public static final BlockEntityType<BannerBlockEntity> BANNER = BlockEntityType.register("banner", BannerBlockEntity::new, Blocks.WHITE_BANNER, Blocks.ORANGE_BANNER, Blocks.MAGENTA_BANNER, Blocks.LIGHT_BLUE_BANNER, Blocks.YELLOW_BANNER, Blocks.LIME_BANNER, Blocks.PINK_BANNER, Blocks.GRAY_BANNER, Blocks.LIGHT_GRAY_BANNER, Blocks.CYAN_BANNER, Blocks.PURPLE_BANNER, Blocks.BLUE_BANNER, Blocks.BROWN_BANNER, Blocks.GREEN_BANNER, Blocks.RED_BANNER, Blocks.BLACK_BANNER, Blocks.WHITE_WALL_BANNER, Blocks.ORANGE_WALL_BANNER, Blocks.MAGENTA_WALL_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER, Blocks.YELLOW_WALL_BANNER, Blocks.LIME_WALL_BANNER, Blocks.PINK_WALL_BANNER, Blocks.GRAY_WALL_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER, Blocks.CYAN_WALL_BANNER, Blocks.PURPLE_WALL_BANNER, Blocks.BLUE_WALL_BANNER, Blocks.BROWN_WALL_BANNER, Blocks.GREEN_WALL_BANNER, Blocks.RED_WALL_BANNER, Blocks.BLACK_WALL_BANNER);
    public static final BlockEntityType<StructureBlockEntity> STRUCTURE_BLOCK = BlockEntityType.register("structure_block", StructureBlockEntity::new, Blocks.STRUCTURE_BLOCK);
    public static final BlockEntityType<TheEndGatewayBlockEntity> END_GATEWAY = BlockEntityType.register("end_gateway", TheEndGatewayBlockEntity::new, Blocks.END_GATEWAY);
    public static final BlockEntityType<CommandBlockEntity> COMMAND_BLOCK = BlockEntityType.register("command_block", CommandBlockEntity::new, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK);
    public static final BlockEntityType<ShulkerBoxBlockEntity> SHULKER_BOX = BlockEntityType.register("shulker_box", ShulkerBoxBlockEntity::new, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX);
    public static final BlockEntityType<BedBlockEntity> BED = BlockEntityType.register("bed", BedBlockEntity::new, Blocks.RED_BED, Blocks.BLACK_BED, Blocks.BLUE_BED, Blocks.BROWN_BED, Blocks.CYAN_BED, Blocks.GRAY_BED, Blocks.GREEN_BED, Blocks.LIGHT_BLUE_BED, Blocks.LIGHT_GRAY_BED, Blocks.LIME_BED, Blocks.MAGENTA_BED, Blocks.ORANGE_BED, Blocks.PINK_BED, Blocks.PURPLE_BED, Blocks.WHITE_BED, Blocks.YELLOW_BED);
    public static final BlockEntityType<ConduitBlockEntity> CONDUIT = BlockEntityType.register("conduit", ConduitBlockEntity::new, Blocks.CONDUIT);
    public static final BlockEntityType<BarrelBlockEntity> BARREL = BlockEntityType.register("barrel", BarrelBlockEntity::new, Blocks.BARREL);
    public static final BlockEntityType<SmokerBlockEntity> SMOKER = BlockEntityType.register("smoker", SmokerBlockEntity::new, Blocks.SMOKER);
    public static final BlockEntityType<BlastFurnaceBlockEntity> BLAST_FURNACE = BlockEntityType.register("blast_furnace", BlastFurnaceBlockEntity::new, Blocks.BLAST_FURNACE);
    public static final BlockEntityType<LecternBlockEntity> LECTERN = BlockEntityType.register("lectern", LecternBlockEntity::new, Blocks.LECTERN);
    public static final BlockEntityType<BellBlockEntity> BELL = BlockEntityType.register("bell", BellBlockEntity::new, Blocks.BELL);
    public static final BlockEntityType<JigsawBlockEntity> JIGSAW = BlockEntityType.register("jigsaw", JigsawBlockEntity::new, Blocks.JIGSAW);
    public static final BlockEntityType<CampfireBlockEntity> CAMPFIRE = BlockEntityType.register("campfire", CampfireBlockEntity::new, Blocks.CAMPFIRE, Blocks.SOUL_CAMPFIRE);
    public static final BlockEntityType<BeehiveBlockEntity> BEEHIVE = BlockEntityType.register("beehive", BeehiveBlockEntity::new, Blocks.BEE_NEST, Blocks.BEEHIVE);
    public static final BlockEntityType<SculkSensorBlockEntity> SCULK_SENSOR = BlockEntityType.register("sculk_sensor", SculkSensorBlockEntity::new, Blocks.SCULK_SENSOR);
    public static final BlockEntityType<CalibratedSculkSensorBlockEntity> CALIBRATED_SCULK_SENSOR = BlockEntityType.register("calibrated_sculk_sensor", CalibratedSculkSensorBlockEntity::new, Blocks.CALIBRATED_SCULK_SENSOR);
    public static final BlockEntityType<SculkCatalystBlockEntity> SCULK_CATALYST = BlockEntityType.register("sculk_catalyst", SculkCatalystBlockEntity::new, Blocks.SCULK_CATALYST);
    public static final BlockEntityType<SculkShriekerBlockEntity> SCULK_SHRIEKER = BlockEntityType.register("sculk_shrieker", SculkShriekerBlockEntity::new, Blocks.SCULK_SHRIEKER);
    public static final BlockEntityType<ChiseledBookShelfBlockEntity> CHISELED_BOOKSHELF = BlockEntityType.register("chiseled_bookshelf", ChiseledBookShelfBlockEntity::new, Blocks.CHISELED_BOOKSHELF);
    public static final BlockEntityType<ShelfBlockEntity> SHELF = BlockEntityType.register("shelf", ShelfBlockEntity::new, Blocks.ACACIA_SHELF, Blocks.BAMBOO_SHELF, Blocks.BIRCH_SHELF, Blocks.CHERRY_SHELF, Blocks.CRIMSON_SHELF, Blocks.DARK_OAK_SHELF, Blocks.JUNGLE_SHELF, Blocks.MANGROVE_SHELF, Blocks.OAK_SHELF, Blocks.PALE_OAK_SHELF, Blocks.SPRUCE_SHELF, Blocks.WARPED_SHELF);
    public static final BlockEntityType<BrushableBlockEntity> BRUSHABLE_BLOCK = BlockEntityType.register("brushable_block", BrushableBlockEntity::new, Blocks.SUSPICIOUS_SAND, Blocks.SUSPICIOUS_GRAVEL);
    public static final BlockEntityType<DecoratedPotBlockEntity> DECORATED_POT = BlockEntityType.register("decorated_pot", DecoratedPotBlockEntity::new, Blocks.DECORATED_POT);
    public static final BlockEntityType<CrafterBlockEntity> CRAFTER = BlockEntityType.register("crafter", CrafterBlockEntity::new, Blocks.CRAFTER);
    public static final BlockEntityType<TrialSpawnerBlockEntity> TRIAL_SPAWNER = BlockEntityType.register("trial_spawner", TrialSpawnerBlockEntity::new, Blocks.TRIAL_SPAWNER);
    public static final BlockEntityType<VaultBlockEntity> VAULT = BlockEntityType.register("vault", VaultBlockEntity::new, Blocks.VAULT);
    public static final BlockEntityType<TestBlockEntity> TEST_BLOCK = BlockEntityType.register("test_block", TestBlockEntity::new, Blocks.TEST_BLOCK);
    public static final BlockEntityType<TestInstanceBlockEntity> TEST_INSTANCE_BLOCK = BlockEntityType.register("test_instance_block", TestInstanceBlockEntity::new, Blocks.TEST_INSTANCE_BLOCK);
    public static final BlockEntityType<CopperGolemStatueBlockEntity> COPPER_GOLEM_STATUE = BlockEntityType.register("copper_golem_statue", CopperGolemStatueBlockEntity::new, Blocks.COPPER_GOLEM_STATUE, Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.OXIDIZED_COPPER_GOLEM_STATUE, Blocks.WAXED_COPPER_GOLEM_STATUE, Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE, Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE, Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE);
    private static final Set<BlockEntityType<?>> OP_ONLY_CUSTOM_DATA = Set.of(COMMAND_BLOCK, LECTERN, SIGN, HANGING_SIGN, MOB_SPAWNER, TRIAL_SPAWNER);
    private final BlockEntitySupplier<? extends T> factory;
    private final Set<Block> validBlocks;
    private final Holder.Reference<BlockEntityType<?>> builtInRegistryHolder = BuiltInRegistries.BLOCK_ENTITY_TYPE.createIntrusiveHolder(this);

    public static @Nullable Identifier getKey(BlockEntityType<?> blockEntityType) {
        return BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType);
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String string, BlockEntitySupplier<? extends T> blockEntitySupplier, Block ... blocks) {
        if (blocks.length == 0) {
            LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", (Object)string);
        }
        Util.fetchChoiceType(References.BLOCK_ENTITY, string);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, string, new BlockEntityType<T>(blockEntitySupplier, Set.of((Object[])blocks)));
    }

    private BlockEntityType(BlockEntitySupplier<? extends T> blockEntitySupplier, Set<Block> set) {
        this.factory = blockEntitySupplier;
        this.validBlocks = set;
    }

    public T create(BlockPos blockPos, BlockState blockState) {
        return this.factory.create(blockPos, blockState);
    }

    public boolean isValid(BlockState blockState) {
        return this.validBlocks.contains(blockState.getBlock());
    }

    @Deprecated
    public Holder.Reference<BlockEntityType<?>> builtInRegistryHolder() {
        return this.builtInRegistryHolder;
    }

    public @Nullable T getBlockEntity(BlockGetter blockGetter, BlockPos blockPos) {
        BlockEntity blockEntity = blockGetter.getBlockEntity(blockPos);
        if (blockEntity == null || blockEntity.getType() != this) {
            return null;
        }
        return (T)blockEntity;
    }

    public boolean onlyOpCanSetNbt() {
        return OP_ONLY_CUSTOM_DATA.contains(this);
    }

    @FunctionalInterface
    static interface BlockEntitySupplier<T extends BlockEntity> {
        public T create(BlockPos var1, BlockState var2);
    }
}

