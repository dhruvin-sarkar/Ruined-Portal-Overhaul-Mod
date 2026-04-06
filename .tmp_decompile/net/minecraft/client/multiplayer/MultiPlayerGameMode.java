/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.primitives.Shorts
 *  com.google.common.primitives.SignedBytes
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.primitives.Shorts;
import com.google.common.primitives.SignedBytes;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerSlotStateChangedPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.PiercingWeapon;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class MultiPlayerGameMode {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private final ClientPacketListener connection;
    private BlockPos destroyBlockPos = new BlockPos(-1, -1, -1);
    private ItemStack destroyingItem = ItemStack.EMPTY;
    private float destroyProgress;
    private float destroyTicks;
    private int destroyDelay;
    private boolean isDestroying;
    private GameType localPlayerMode = GameType.DEFAULT_MODE;
    private @Nullable GameType previousLocalPlayerMode;
    private int carriedIndex;

    public MultiPlayerGameMode(Minecraft minecraft, ClientPacketListener clientPacketListener) {
        this.minecraft = minecraft;
        this.connection = clientPacketListener;
    }

    public void adjustPlayer(Player player) {
        this.localPlayerMode.updatePlayerAbilities(player.getAbilities());
    }

    public void setLocalMode(GameType gameType, @Nullable GameType gameType2) {
        this.localPlayerMode = gameType;
        this.previousLocalPlayerMode = gameType2;
        this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.getAbilities());
    }

    public void setLocalMode(GameType gameType) {
        if (gameType != this.localPlayerMode) {
            this.previousLocalPlayerMode = this.localPlayerMode;
        }
        this.localPlayerMode = gameType;
        this.localPlayerMode.updatePlayerAbilities(this.minecraft.player.getAbilities());
    }

    public boolean canHurtPlayer() {
        return this.localPlayerMode.isSurvival();
    }

    public boolean destroyBlock(BlockPos blockPos) {
        if (this.minecraft.player.blockActionRestricted(this.minecraft.level, blockPos, this.localPlayerMode)) {
            return false;
        }
        ClientLevel level = this.minecraft.level;
        BlockState blockState = level.getBlockState(blockPos);
        if (!this.minecraft.player.getMainHandItem().canDestroyBlock(blockState, level, blockPos, this.minecraft.player)) {
            return false;
        }
        Block block = blockState.getBlock();
        if (block instanceof GameMasterBlock && !this.minecraft.player.canUseGameMasterBlocks()) {
            return false;
        }
        if (blockState.isAir()) {
            return false;
        }
        block.playerWillDestroy(level, blockPos, blockState, this.minecraft.player);
        FluidState fluidState = level.getFluidState(blockPos);
        boolean bl = level.setBlock(blockPos, fluidState.createLegacyBlock(), 11);
        if (bl) {
            block.destroy(level, blockPos, blockState);
        }
        if (SharedConstants.DEBUG_BLOCK_BREAK) {
            LOGGER.error("client broke {} {} -> {}", new Object[]{blockPos, blockState, level.getBlockState(blockPos)});
        }
        return bl;
    }

    public boolean startDestroyBlock(BlockPos blockPos, Direction direction) {
        if (this.minecraft.player.blockActionRestricted(this.minecraft.level, blockPos, this.localPlayerMode)) {
            return false;
        }
        if (!this.minecraft.level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        if (this.minecraft.player.getAbilities().instabuild) {
            BlockState blockState = this.minecraft.level.getBlockState(blockPos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockPos, blockState, 1.0f);
            if (SharedConstants.DEBUG_BLOCK_BREAK) {
                LOGGER.info("Creative start {} {}", (Object)blockPos, (Object)blockState);
            }
            this.startPrediction(this.minecraft.level, i -> {
                this.destroyBlock(blockPos);
                return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, direction, i);
            });
            this.destroyDelay = 5;
        } else if (!this.isDestroying || !this.sameDestroyTarget(blockPos)) {
            if (this.isDestroying) {
                if (SharedConstants.DEBUG_BLOCK_BREAK) {
                    LOGGER.info("Abort old break {} {}", (Object)blockPos, (Object)this.minecraft.level.getBlockState(blockPos));
                }
                this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, direction));
            }
            BlockState blockState = this.minecraft.level.getBlockState(blockPos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockPos, blockState, 0.0f);
            if (SharedConstants.DEBUG_BLOCK_BREAK) {
                LOGGER.info("Start break {} {}", (Object)blockPos, (Object)blockState);
            }
            this.startPrediction(this.minecraft.level, i -> {
                boolean bl;
                boolean bl2 = bl = !blockState.isAir();
                if (bl && this.destroyProgress == 0.0f) {
                    blockState.attack(this.minecraft.level, blockPos, this.minecraft.player);
                }
                if (bl && blockState.getDestroyProgress(this.minecraft.player, this.minecraft.player.level(), blockPos) >= 1.0f) {
                    this.destroyBlock(blockPos);
                } else {
                    this.isDestroying = true;
                    this.destroyBlockPos = blockPos;
                    this.destroyingItem = this.minecraft.player.getMainHandItem();
                    this.destroyProgress = 0.0f;
                    this.destroyTicks = 0.0f;
                    this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, this.getDestroyStage());
                }
                return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, direction, i);
            });
        }
        return true;
    }

    public void stopDestroyBlock() {
        if (this.isDestroying) {
            BlockState blockState = this.minecraft.level.getBlockState(this.destroyBlockPos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, this.destroyBlockPos, blockState, -1.0f);
            if (SharedConstants.DEBUG_BLOCK_BREAK) {
                LOGGER.info("Stop dest {} {}", (Object)this.destroyBlockPos, (Object)blockState);
            }
            this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, Direction.DOWN));
            this.isDestroying = false;
            this.destroyProgress = 0.0f;
            this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, -1);
            this.minecraft.player.resetAttackStrengthTicker();
        }
    }

    public boolean continueDestroyBlock(BlockPos blockPos, Direction direction) {
        this.ensureHasSentCarriedItem();
        if (this.destroyDelay > 0) {
            --this.destroyDelay;
            return true;
        }
        if (this.minecraft.player.getAbilities().instabuild && this.minecraft.level.getWorldBorder().isWithinBounds(blockPos)) {
            this.destroyDelay = 5;
            BlockState blockState = this.minecraft.level.getBlockState(blockPos);
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockPos, blockState, 1.0f);
            if (SharedConstants.DEBUG_BLOCK_BREAK) {
                LOGGER.info("Creative cont {} {}", (Object)blockPos, (Object)blockState);
            }
            this.startPrediction(this.minecraft.level, i -> {
                this.destroyBlock(blockPos);
                return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockPos, direction, i);
            });
            return true;
        }
        if (this.sameDestroyTarget(blockPos)) {
            BlockState blockState = this.minecraft.level.getBlockState(blockPos);
            if (blockState.isAir()) {
                this.isDestroying = false;
                return false;
            }
            this.destroyProgress += blockState.getDestroyProgress(this.minecraft.player, this.minecraft.player.level(), blockPos);
            if (this.destroyTicks % 4.0f == 0.0f) {
                SoundType soundType = blockState.getSoundType();
                this.minecraft.getSoundManager().play(new SimpleSoundInstance(soundType.getHitSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 8.0f, soundType.getPitch() * 0.5f, SoundInstance.createUnseededRandom(), blockPos));
            }
            this.destroyTicks += 1.0f;
            this.minecraft.getTutorial().onDestroyBlock(this.minecraft.level, blockPos, blockState, Mth.clamp(this.destroyProgress, 0.0f, 1.0f));
            if (this.destroyProgress >= 1.0f) {
                this.isDestroying = false;
                if (SharedConstants.DEBUG_BLOCK_BREAK) {
                    LOGGER.info("Finished breaking {} {}", (Object)blockPos, (Object)blockState);
                }
                this.startPrediction(this.minecraft.level, i -> {
                    this.destroyBlock(blockPos);
                    return new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockPos, direction, i);
                });
                this.destroyProgress = 0.0f;
                this.destroyTicks = 0.0f;
                this.destroyDelay = 5;
            }
        } else {
            return this.startDestroyBlock(blockPos, direction);
        }
        this.minecraft.level.destroyBlockProgress(this.minecraft.player.getId(), this.destroyBlockPos, this.getDestroyStage());
        return true;
    }

    private void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction) {
        try (BlockStatePredictionHandler blockStatePredictionHandler = clientLevel.getBlockStatePredictionHandler().startPredicting();){
            int i = blockStatePredictionHandler.currentSequence();
            Packet<ServerGamePacketListener> packet = predictiveAction.predict(i);
            this.connection.send(packet);
        }
    }

    public void tick() {
        this.ensureHasSentCarriedItem();
        if (this.connection.getConnection().isConnected()) {
            this.connection.getConnection().tick();
        } else {
            this.connection.getConnection().handleDisconnection();
        }
    }

    private boolean sameDestroyTarget(BlockPos blockPos) {
        ItemStack itemStack = this.minecraft.player.getMainHandItem();
        return blockPos.equals(this.destroyBlockPos) && ItemStack.isSameItemSameComponents(itemStack, this.destroyingItem);
    }

    private void ensureHasSentCarriedItem() {
        int i = this.minecraft.player.getInventory().getSelectedSlot();
        if (i != this.carriedIndex) {
            this.carriedIndex = i;
            this.connection.send(new ServerboundSetCarriedItemPacket(this.carriedIndex));
        }
    }

    public InteractionResult useItemOn(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        this.ensureHasSentCarriedItem();
        if (!this.minecraft.level.getWorldBorder().isWithinBounds(blockHitResult.getBlockPos())) {
            return InteractionResult.FAIL;
        }
        MutableObject mutableObject = new MutableObject();
        this.startPrediction(this.minecraft.level, i -> {
            mutableObject.setValue((Object)this.performUseItemOn(localPlayer, interactionHand, blockHitResult));
            return new ServerboundUseItemOnPacket(interactionHand, blockHitResult, i);
        });
        return (InteractionResult)mutableObject.get();
    }

    private InteractionResult performUseItemOn(LocalPlayer localPlayer, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        InteractionResult interactionResult3;
        boolean bl2;
        BlockPos blockPos = blockHitResult.getBlockPos();
        ItemStack itemStack = localPlayer.getItemInHand(interactionHand);
        if (this.localPlayerMode == GameType.SPECTATOR) {
            return InteractionResult.CONSUME;
        }
        boolean bl = !localPlayer.getMainHandItem().isEmpty() || !localPlayer.getOffhandItem().isEmpty();
        boolean bl3 = bl2 = localPlayer.isSecondaryUseActive() && bl;
        if (!bl2) {
            InteractionResult interactionResult2;
            BlockState blockState = this.minecraft.level.getBlockState(blockPos);
            if (!this.connection.isFeatureEnabled(blockState.getBlock().requiredFeatures())) {
                return InteractionResult.FAIL;
            }
            InteractionResult interactionResult = blockState.useItemOn(localPlayer.getItemInHand(interactionHand), this.minecraft.level, localPlayer, interactionHand, blockHitResult);
            if (interactionResult.consumesAction()) {
                return interactionResult;
            }
            if (interactionResult instanceof InteractionResult.TryEmptyHandInteraction && interactionHand == InteractionHand.MAIN_HAND && (interactionResult2 = blockState.useWithoutItem(this.minecraft.level, localPlayer, blockHitResult)).consumesAction()) {
                return interactionResult2;
            }
        }
        if (itemStack.isEmpty() || localPlayer.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.PASS;
        }
        UseOnContext useOnContext = new UseOnContext(localPlayer, interactionHand, blockHitResult);
        if (localPlayer.hasInfiniteMaterials()) {
            int i = itemStack.getCount();
            interactionResult3 = itemStack.useOn(useOnContext);
            itemStack.setCount(i);
        } else {
            interactionResult3 = itemStack.useOn(useOnContext);
        }
        return interactionResult3;
    }

    public InteractionResult useItem(Player player, InteractionHand interactionHand) {
        if (this.localPlayerMode == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        }
        this.ensureHasSentCarriedItem();
        MutableObject mutableObject = new MutableObject();
        this.startPrediction(this.minecraft.level, i -> {
            ItemStack itemStack2;
            ServerboundUseItemPacket serverboundUseItemPacket = new ServerboundUseItemPacket(interactionHand, i, player.getYRot(), player.getXRot());
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (player.getCooldowns().isOnCooldown(itemStack)) {
                mutableObject.setValue((Object)InteractionResult.PASS);
                return serverboundUseItemPacket;
            }
            InteractionResult interactionResult = itemStack.use(this.minecraft.level, player, interactionHand);
            if (interactionResult instanceof InteractionResult.Success) {
                InteractionResult.Success success = (InteractionResult.Success)interactionResult;
                itemStack2 = (ItemStack)Objects.requireNonNullElseGet((Object)success.heldItemTransformedTo(), () -> player.getItemInHand(interactionHand));
            } else {
                itemStack2 = player.getItemInHand(interactionHand);
            }
            if (itemStack2 != itemStack) {
                player.setItemInHand(interactionHand, itemStack2);
            }
            mutableObject.setValue((Object)interactionResult);
            return serverboundUseItemPacket;
        });
        return (InteractionResult)mutableObject.get();
    }

    public LocalPlayer createPlayer(ClientLevel clientLevel, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook) {
        return this.createPlayer(clientLevel, statsCounter, clientRecipeBook, Input.EMPTY, false);
    }

    public LocalPlayer createPlayer(ClientLevel clientLevel, StatsCounter statsCounter, ClientRecipeBook clientRecipeBook, Input input, boolean bl) {
        return new LocalPlayer(this.minecraft, clientLevel, this.connection, statsCounter, clientRecipeBook, input, bl);
    }

    public void attack(Player player, Entity entity) {
        this.ensureHasSentCarriedItem();
        this.connection.send(ServerboundInteractPacket.createAttackPacket(entity, player.isShiftKeyDown()));
        if (this.localPlayerMode != GameType.SPECTATOR) {
            player.attack(entity);
            player.resetAttackStrengthTicker();
        }
    }

    public InteractionResult interact(Player player, Entity entity, InteractionHand interactionHand) {
        this.ensureHasSentCarriedItem();
        this.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), interactionHand));
        if (this.localPlayerMode == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        }
        return player.interactOn(entity, interactionHand);
    }

    public InteractionResult interactAt(Player player, Entity entity, EntityHitResult entityHitResult, InteractionHand interactionHand) {
        this.ensureHasSentCarriedItem();
        Vec3 vec3 = entityHitResult.getLocation().subtract(entity.getX(), entity.getY(), entity.getZ());
        this.connection.send(ServerboundInteractPacket.createInteractionPacket(entity, player.isShiftKeyDown(), interactionHand, vec3));
        if (this.localPlayerMode == GameType.SPECTATOR) {
            return InteractionResult.PASS;
        }
        return entity.interactAt(player, vec3, interactionHand);
    }

    public void handleInventoryMouseClick(int i, int j, int k, ClickType clickType, Player player) {
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        if (i != abstractContainerMenu.containerId) {
            LOGGER.warn("Ignoring click in mismatching container. Click in {}, player has {}.", (Object)i, (Object)abstractContainerMenu.containerId);
            return;
        }
        NonNullList<Slot> nonNullList = abstractContainerMenu.slots;
        int l = nonNullList.size();
        ArrayList list = Lists.newArrayListWithCapacity((int)l);
        for (Slot slot : nonNullList) {
            list.add(slot.getItem().copy());
        }
        abstractContainerMenu.clicked(j, k, clickType, player);
        Int2ObjectOpenHashMap int2ObjectMap = new Int2ObjectOpenHashMap();
        for (int m = 0; m < l; ++m) {
            ItemStack itemStack2;
            ItemStack itemStack = (ItemStack)list.get(m);
            if (ItemStack.matches(itemStack, itemStack2 = nonNullList.get(m).getItem())) continue;
            int2ObjectMap.put(m, (Object)HashedStack.create(itemStack2, this.connection.decoratedHashOpsGenenerator()));
        }
        HashedStack hashedStack = HashedStack.create(abstractContainerMenu.getCarried(), this.connection.decoratedHashOpsGenenerator());
        this.connection.send(new ServerboundContainerClickPacket(i, abstractContainerMenu.getStateId(), Shorts.checkedCast((long)j), SignedBytes.checkedCast((long)k), clickType, (Int2ObjectMap<HashedStack>)int2ObjectMap, hashedStack));
    }

    public void handlePlaceRecipe(int i, RecipeDisplayId recipeDisplayId, boolean bl) {
        this.connection.send(new ServerboundPlaceRecipePacket(i, recipeDisplayId, bl));
    }

    public void handleInventoryButtonClick(int i, int j) {
        this.connection.send(new ServerboundContainerButtonClickPacket(i, j));
    }

    public void handleCreativeModeItemAdd(ItemStack itemStack, int i) {
        if (this.minecraft.player.hasInfiniteMaterials() && this.connection.isFeatureEnabled(itemStack.getItem().requiredFeatures())) {
            this.connection.send(new ServerboundSetCreativeModeSlotPacket(i, itemStack));
        }
    }

    public void handleCreativeModeItemDrop(ItemStack itemStack) {
        boolean bl;
        boolean bl2 = bl = this.minecraft.screen instanceof AbstractContainerScreen && !(this.minecraft.screen instanceof CreativeModeInventoryScreen);
        if (this.minecraft.player.hasInfiniteMaterials() && !bl && !itemStack.isEmpty() && this.connection.isFeatureEnabled(itemStack.getItem().requiredFeatures())) {
            this.connection.send(new ServerboundSetCreativeModeSlotPacket(-1, itemStack));
            this.minecraft.player.getDropSpamThrottler().increment();
        }
    }

    public void releaseUsingItem(Player player) {
        this.ensureHasSentCarriedItem();
        this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM, BlockPos.ZERO, Direction.DOWN));
        player.releaseUsingItem();
    }

    public void piercingAttack(PiercingWeapon piercingWeapon) {
        this.ensureHasSentCarriedItem();
        this.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STAB, BlockPos.ZERO, Direction.DOWN));
        this.minecraft.player.onAttack();
        this.minecraft.player.lungeForwardMaybe();
        piercingWeapon.makeSound(this.minecraft.player);
    }

    public boolean hasExperience() {
        return this.localPlayerMode.isSurvival();
    }

    public boolean hasMissTime() {
        return !this.localPlayerMode.isCreative();
    }

    public boolean isServerControlledInventory() {
        return this.minecraft.player.isPassenger() && this.minecraft.player.getVehicle() instanceof HasCustomInventoryScreen;
    }

    public boolean isSpectator() {
        return this.localPlayerMode == GameType.SPECTATOR;
    }

    public @Nullable GameType getPreviousPlayerMode() {
        return this.previousLocalPlayerMode;
    }

    public GameType getPlayerMode() {
        return this.localPlayerMode;
    }

    public boolean isDestroying() {
        return this.isDestroying;
    }

    public int getDestroyStage() {
        return this.destroyProgress > 0.0f ? (int)(this.destroyProgress * 10.0f) : -1;
    }

    public void handlePickItemFromBlock(BlockPos blockPos, boolean bl) {
        this.connection.send(new ServerboundPickItemFromBlockPacket(blockPos, bl));
    }

    public void handlePickItemFromEntity(Entity entity, boolean bl) {
        this.connection.send(new ServerboundPickItemFromEntityPacket(entity.getId(), bl));
    }

    public void handleSlotStateChanged(int i, int j, boolean bl) {
        this.connection.send(new ServerboundContainerSlotStateChangedPacket(i, j, bl));
    }
}

