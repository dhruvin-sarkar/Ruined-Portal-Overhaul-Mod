/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.debug.DebugBrainDump;
import net.minecraft.util.debug.DebugGoalInfo;
import net.minecraft.util.debug.DebugPathInfo;
import net.minecraft.util.debug.DebugSubscriptions;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.DropChances;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentTable;
import net.minecraft.world.entity.EquipmentUser;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Targeting;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.sensing.Sensing;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.ContainerSingleItem;
import org.jspecify.annotations.Nullable;

public abstract class Mob
extends LivingEntity
implements EquipmentUser,
Leashable,
Targeting {
    private static final EntityDataAccessor<Byte> DATA_MOB_FLAGS_ID = SynchedEntityData.defineId(Mob.class, EntityDataSerializers.BYTE);
    private static final int MOB_FLAG_NO_AI = 1;
    private static final int MOB_FLAG_LEFTHANDED = 2;
    private static final int MOB_FLAG_AGGRESSIVE = 4;
    protected static final int PICKUP_REACH = 1;
    private static final Vec3i ITEM_PICKUP_REACH = new Vec3i(1, 0, 1);
    private static final List<EquipmentSlot> EQUIPMENT_POPULATION_ORDER = List.of((Object)EquipmentSlot.HEAD, (Object)EquipmentSlot.CHEST, (Object)EquipmentSlot.LEGS, (Object)EquipmentSlot.FEET);
    public static final float MAX_WEARING_ARMOR_CHANCE = 0.15f;
    public static final float WEARING_ARMOR_UPGRADE_MATERIAL_CHANCE = 0.1087f;
    public static final float WEARING_ARMOR_UPGRADE_MATERIAL_ATTEMPTS = 3.0f;
    public static final float MAX_PICKUP_LOOT_CHANCE = 0.55f;
    public static final float MAX_ENCHANTED_ARMOR_CHANCE = 0.5f;
    public static final float MAX_ENCHANTED_WEAPON_CHANCE = 0.25f;
    public static final int UPDATE_GOAL_SELECTOR_EVERY_N_TICKS = 2;
    private static final double DEFAULT_ATTACK_REACH = Math.sqrt(2.04f) - (double)0.6f;
    private static final boolean DEFAULT_CAN_PICK_UP_LOOT = false;
    private static final boolean DEFAULT_PERSISTENCE_REQUIRED = false;
    private static final boolean DEFAULT_LEFT_HANDED = false;
    private static final boolean DEFAULT_NO_AI = false;
    protected static final Identifier RANDOM_SPAWN_BONUS_ID = Identifier.withDefaultNamespace("random_spawn_bonus");
    public static final String TAG_DROP_CHANCES = "drop_chances";
    public static final String TAG_LEFT_HANDED = "LeftHanded";
    public static final String TAG_CAN_PICK_UP_LOOT = "CanPickUpLoot";
    public static final String TAG_NO_AI = "NoAI";
    public int ambientSoundTime;
    protected int xpReward;
    protected LookControl lookControl;
    protected MoveControl moveControl;
    protected JumpControl jumpControl;
    private final BodyRotationControl bodyRotationControl;
    protected PathNavigation navigation;
    protected final GoalSelector goalSelector;
    protected final GoalSelector targetSelector;
    private @Nullable LivingEntity target;
    private final Sensing sensing;
    private DropChances dropChances = DropChances.DEFAULT;
    private boolean canPickUpLoot = false;
    private boolean persistenceRequired = false;
    private final Map<PathType, Float> pathfindingMalus = Maps.newEnumMap(PathType.class);
    private Optional<ResourceKey<LootTable>> lootTable = Optional.empty();
    private long lootTableSeed;
    private @Nullable Leashable.LeashData leashData;
    private BlockPos homePosition = BlockPos.ZERO;
    private int homeRadius = -1;

    protected Mob(EntityType<? extends Mob> entityType, Level level) {
        super((EntityType<? extends LivingEntity>)entityType, level);
        this.goalSelector = new GoalSelector();
        this.targetSelector = new GoalSelector();
        this.lookControl = new LookControl(this);
        this.moveControl = new MoveControl(this);
        this.jumpControl = new JumpControl(this);
        this.bodyRotationControl = this.createBodyControl();
        this.navigation = this.createNavigation(level);
        this.sensing = new Sensing(this);
        if (level instanceof ServerLevel) {
            this.registerGoals();
        }
    }

    protected void registerGoals() {
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
    }

    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    protected boolean shouldPassengersInheritMalus() {
        return false;
    }

    public float getPathfindingMalus(PathType pathType) {
        Mob mob;
        Entity entity = this.getControlledVehicle();
        Mob mob2 = entity instanceof Mob && (mob = (Mob)entity).shouldPassengersInheritMalus() ? mob : this;
        Float float_ = mob2.pathfindingMalus.get((Object)pathType);
        return float_ == null ? pathType.getMalus() : float_.floatValue();
    }

    public void setPathfindingMalus(PathType pathType, float f) {
        this.pathfindingMalus.put(pathType, Float.valueOf(f));
    }

    public void onPathfindingStart() {
    }

    public void onPathfindingDone() {
    }

    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    public LookControl getLookControl() {
        return this.lookControl;
    }

    public MoveControl getMoveControl() {
        Entity entity = this.getControlledVehicle();
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            return mob.getMoveControl();
        }
        return this.moveControl;
    }

    public JumpControl getJumpControl() {
        return this.jumpControl;
    }

    public PathNavigation getNavigation() {
        Entity entity = this.getControlledVehicle();
        if (entity instanceof Mob) {
            Mob mob = (Mob)entity;
            return mob.getNavigation();
        }
        return this.navigation;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (this.isNoAi()) return null;
        if (!(entity instanceof Mob)) return null;
        Mob mob = (Mob)entity;
        if (!entity.canControlVehicle()) return null;
        Mob mob2 = mob;
        return mob2;
    }

    public Sensing getSensing() {
        return this.sensing;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.target;
    }

    protected final @Nullable LivingEntity getTargetFromBrain() {
        return this.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    public void setTarget(@Nullable LivingEntity livingEntity) {
        this.target = livingEntity;
    }

    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return entityType != EntityType.GHAST;
    }

    public boolean canUseNonMeleeWeapon(ItemStack itemStack) {
        return false;
    }

    public void ate() {
        this.gameEvent(GameEvent.EAT);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MOB_FLAGS_ID, (byte)0);
    }

    public int getAmbientSoundInterval() {
        return 80;
    }

    public void playAmbientSound() {
        this.makeSound(this.getAmbientSound());
    }

    @Override
    public void baseTick() {
        super.baseTick();
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("mobBaseTick");
        if (this.isAlive() && this.random.nextInt(1000) < this.ambientSoundTime++) {
            this.resetAmbientSoundTime();
            this.playAmbientSound();
        }
        profilerFiller.pop();
    }

    @Override
    protected void playHurtSound(DamageSource damageSource) {
        this.resetAmbientSoundTime();
        super.playHurtSound(damageSource);
    }

    private void resetAmbientSoundTime() {
        this.ambientSoundTime = -this.getAmbientSoundInterval();
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel serverLevel) {
        if (this.xpReward > 0) {
            int i = this.xpReward;
            for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                ItemStack itemStack;
                if (!equipmentSlot.canIncreaseExperience() || (itemStack = this.getItemBySlot(equipmentSlot)).isEmpty() || !(this.dropChances.byEquipment(equipmentSlot) <= 1.0f)) continue;
                i += 1 + this.random.nextInt(3);
            }
            return i;
        }
        return this.xpReward;
    }

    public void spawnAnim() {
        if (this.level().isClientSide()) {
            this.makePoofParticles();
        } else {
            this.level().broadcastEntityEvent(this, (byte)20);
        }
    }

    @Override
    public void handleEntityEvent(byte b) {
        if (b == 20) {
            this.spawnAnim();
        } else {
            super.handleEntityEvent(b);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount % 5 == 0) {
            this.updateControlFlags();
        }
    }

    protected void updateControlFlags() {
        boolean bl = !(this.getControllingPassenger() instanceof Mob);
        boolean bl2 = !(this.getVehicle() instanceof AbstractBoat);
        this.goalSelector.setControlFlag(Goal.Flag.MOVE, bl);
        this.goalSelector.setControlFlag(Goal.Flag.JUMP, bl && bl2);
        this.goalSelector.setControlFlag(Goal.Flag.LOOK, bl);
    }

    @Override
    protected void tickHeadTurn(float f) {
        this.bodyRotationControl.clientTick();
    }

    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean(TAG_CAN_PICK_UP_LOOT, this.canPickUpLoot());
        valueOutput.putBoolean("PersistenceRequired", this.persistenceRequired);
        if (!this.dropChances.equals((Object)DropChances.DEFAULT)) {
            valueOutput.store(TAG_DROP_CHANCES, DropChances.CODEC, this.dropChances);
        }
        this.writeLeashData(valueOutput, this.leashData);
        if (this.hasHome()) {
            valueOutput.putInt("home_radius", this.homeRadius);
            valueOutput.store("home_pos", BlockPos.CODEC, this.homePosition);
        }
        valueOutput.putBoolean(TAG_LEFT_HANDED, this.isLeftHanded());
        this.lootTable.ifPresent(resourceKey -> valueOutput.store("DeathLootTable", LootTable.KEY_CODEC, resourceKey));
        if (this.lootTableSeed != 0L) {
            valueOutput.putLong("DeathLootTableSeed", this.lootTableSeed);
        }
        if (this.isNoAi()) {
            valueOutput.putBoolean(TAG_NO_AI, this.isNoAi());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setCanPickUpLoot(valueInput.getBooleanOr(TAG_CAN_PICK_UP_LOOT, false));
        this.persistenceRequired = valueInput.getBooleanOr("PersistenceRequired", false);
        this.dropChances = valueInput.read(TAG_DROP_CHANCES, DropChances.CODEC).orElse(DropChances.DEFAULT);
        this.readLeashData(valueInput);
        this.homeRadius = valueInput.getIntOr("home_radius", -1);
        if (this.homeRadius >= 0) {
            this.homePosition = valueInput.read("home_pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
        }
        this.setLeftHanded(valueInput.getBooleanOr(TAG_LEFT_HANDED, false));
        this.lootTable = valueInput.read("DeathLootTable", LootTable.KEY_CODEC);
        this.lootTableSeed = valueInput.getLongOr("DeathLootTableSeed", 0L);
        this.setNoAi(valueInput.getBooleanOr(TAG_NO_AI, false));
    }

    @Override
    protected void dropFromLootTable(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        super.dropFromLootTable(serverLevel, damageSource, bl);
        this.lootTable = Optional.empty();
    }

    @Override
    public final Optional<ResourceKey<LootTable>> getLootTable() {
        if (this.lootTable.isPresent()) {
            return this.lootTable;
        }
        return super.getLootTable();
    }

    @Override
    public long getLootTableSeed() {
        return this.lootTableSeed;
    }

    public void setZza(float f) {
        this.zza = f;
    }

    public void setYya(float f) {
        this.yya = f;
    }

    public void setXxa(float f) {
        this.xxa = f;
    }

    @Override
    public void setSpeed(float f) {
        super.setSpeed(f);
        this.setZza(f);
    }

    public void stopInPlace() {
        this.getNavigation().stop();
        this.setXxa(0.0f);
        this.setYya(0.0f);
        this.setSpeed(0.0f);
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.resetAngularLeashMomentum();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getType().is(EntityTypeTags.BURN_IN_DAYLIGHT)) {
            this.burnUndead();
        }
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("looting");
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.canPickUpLoot() && this.isAlive() && !this.dead && serverLevel.getGameRules().get(GameRules.MOB_GRIEFING).booleanValue()) {
                Vec3i vec3i = this.getPickupReach();
                List<ItemEntity> list = this.level().getEntitiesOfClass(ItemEntity.class, this.getBoundingBox().inflate(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
                for (ItemEntity itemEntity : list) {
                    if (itemEntity.isRemoved() || itemEntity.getItem().isEmpty() || itemEntity.hasPickUpDelay() || !this.wantsToPickUp(serverLevel, itemEntity.getItem())) continue;
                    this.pickUpItem(serverLevel, itemEntity);
                }
            }
        }
        profilerFiller.pop();
    }

    protected EquipmentSlot sunProtectionSlot() {
        return EquipmentSlot.HEAD;
    }

    private void burnUndead() {
        if (!this.isAlive() || !this.isSunBurnTick()) {
            return;
        }
        EquipmentSlot equipmentSlot = this.sunProtectionSlot();
        ItemStack itemStack = this.getItemBySlot(equipmentSlot);
        if (!itemStack.isEmpty()) {
            if (itemStack.isDamageableItem()) {
                Item item = itemStack.getItem();
                itemStack.setDamageValue(itemStack.getDamageValue() + this.random.nextInt(2));
                if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                    this.onEquippedItemBroken(item, equipmentSlot);
                    this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
                }
            }
            return;
        }
        this.igniteForSeconds(8.0f);
    }

    private boolean isSunBurnTick() {
        if (!this.level().isClientSide() && this.level().environmentAttributes().getValue(EnvironmentAttributes.MONSTERS_BURN, this.position()).booleanValue()) {
            boolean bl;
            float f = this.getLightLevelDependentMagicValue();
            BlockPos blockPos = BlockPos.containing(this.getX(), this.getEyeY(), this.getZ());
            boolean bl2 = bl = this.isInWaterOrRain() || this.isInPowderSnow || this.wasInPowderSnow;
            if (f > 0.5f && this.random.nextFloat() * 30.0f < (f - 0.4f) * 2.0f && !bl && this.level().canSeeSky(blockPos)) {
                return true;
            }
        }
        return false;
    }

    protected Vec3i getPickupReach() {
        return ITEM_PICKUP_REACH;
    }

    protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getItem();
        ItemStack itemStack2 = this.equipItemIfPossible(serverLevel, itemStack.copy());
        if (!itemStack2.isEmpty()) {
            this.onItemPickup(itemEntity);
            this.take(itemEntity, itemStack2.getCount());
            itemStack.shrink(itemStack2.getCount());
            if (itemStack.isEmpty()) {
                itemEntity.discard();
            }
        }
    }

    public ItemStack equipItemIfPossible(ServerLevel serverLevel, ItemStack itemStack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
        if (!this.isEquippableInSlot(itemStack, equipmentSlot)) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
        boolean bl = this.canReplaceCurrentItem(itemStack, itemStack2, equipmentSlot);
        if (equipmentSlot.isArmor() && !bl) {
            equipmentSlot = EquipmentSlot.MAINHAND;
            itemStack2 = this.getItemBySlot(equipmentSlot);
            bl = itemStack2.isEmpty();
        }
        if (bl && this.canHoldItem(itemStack)) {
            double d = this.dropChances.byEquipment(equipmentSlot);
            if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.spawnAtLocation(serverLevel, itemStack2);
            }
            ItemStack itemStack3 = equipmentSlot.limit(itemStack);
            this.setItemSlotAndDropWhenKilled(equipmentSlot, itemStack3);
            return itemStack3;
        }
        return ItemStack.EMPTY;
    }

    protected void setItemSlotAndDropWhenKilled(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.setItemSlot(equipmentSlot, itemStack);
        this.setGuaranteedDrop(equipmentSlot);
        this.persistenceRequired = true;
    }

    protected boolean canShearEquipment(Player player) {
        return !this.isVehicle();
    }

    public void setGuaranteedDrop(EquipmentSlot equipmentSlot) {
        this.dropChances = this.dropChances.withGuaranteedDrop(equipmentSlot);
    }

    protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2, EquipmentSlot equipmentSlot) {
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (equipmentSlot.isArmor()) {
            return this.compareArmor(itemStack, itemStack2, equipmentSlot);
        }
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            return this.compareWeapons(itemStack, itemStack2, equipmentSlot);
        }
        return false;
    }

    private boolean compareArmor(ItemStack itemStack, ItemStack itemStack2, EquipmentSlot equipmentSlot) {
        if (EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        double d = this.getApproximateAttributeWith(itemStack, Attributes.ARMOR, equipmentSlot);
        double e = this.getApproximateAttributeWith(itemStack2, Attributes.ARMOR, equipmentSlot);
        double f = this.getApproximateAttributeWith(itemStack, Attributes.ARMOR_TOUGHNESS, equipmentSlot);
        double g = this.getApproximateAttributeWith(itemStack2, Attributes.ARMOR_TOUGHNESS, equipmentSlot);
        if (d != e) {
            return d > e;
        }
        if (f != g) {
            return f > g;
        }
        return this.canReplaceEqualItem(itemStack, itemStack2);
    }

    private boolean compareWeapons(ItemStack itemStack, ItemStack itemStack2, EquipmentSlot equipmentSlot) {
        double e;
        double d;
        TagKey<Item> tagKey = this.getPreferredWeaponType();
        if (tagKey != null) {
            if (itemStack2.is(tagKey) && !itemStack.is(tagKey)) {
                return false;
            }
            if (!itemStack2.is(tagKey) && itemStack.is(tagKey)) {
                return true;
            }
        }
        if ((d = this.getApproximateAttributeWith(itemStack, Attributes.ATTACK_DAMAGE, equipmentSlot)) != (e = this.getApproximateAttributeWith(itemStack2, Attributes.ATTACK_DAMAGE, equipmentSlot))) {
            return d > e;
        }
        return this.canReplaceEqualItem(itemStack, itemStack2);
    }

    private double getApproximateAttributeWith(ItemStack itemStack, Holder<Attribute> holder, EquipmentSlot equipmentSlot) {
        double d = this.getAttributes().hasAttribute(holder) ? this.getAttributeBaseValue(holder) : 0.0;
        ItemAttributeModifiers itemAttributeModifiers = itemStack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        return itemAttributeModifiers.compute(holder, d, equipmentSlot);
    }

    public boolean canReplaceEqualItem(ItemStack itemStack, ItemStack itemStack2) {
        int j;
        Set<Object2IntMap.Entry<Holder<Enchantment>>> set = itemStack2.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet();
        Set<Object2IntMap.Entry<Holder<Enchantment>>> set2 = itemStack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet();
        if (set2.size() != set.size()) {
            return set2.size() > set.size();
        }
        int i = itemStack.getDamageValue();
        if (i != (j = itemStack2.getDamageValue())) {
            return i < j;
        }
        return itemStack.has(DataComponents.CUSTOM_NAME) && !itemStack2.has(DataComponents.CUSTOM_NAME);
    }

    public boolean canHoldItem(ItemStack itemStack) {
        return true;
    }

    public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack itemStack) {
        return this.canHoldItem(itemStack);
    }

    public @Nullable TagKey<Item> getPreferredWeaponType() {
        return null;
    }

    public boolean removeWhenFarAway(double d) {
        return true;
    }

    public boolean requiresCustomPersistence() {
        return this.isPassenger();
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL && !this.getType().isAllowedInPeaceful()) {
            this.discard();
            return;
        }
        if (this.isPersistenceRequired() || this.requiresCustomPersistence()) {
            this.noActionTime = 0;
            return;
        }
        Player entity = this.level().getNearestPlayer(this, -1.0);
        if (entity != null) {
            int i;
            int j;
            double d = entity.distanceToSqr(this);
            if (d > (double)(j = (i = this.getType().getCategory().getDespawnDistance()) * i) && this.removeWhenFarAway(d)) {
                this.discard();
            }
            int k = this.getType().getCategory().getNoDespawnDistance();
            int l = k * k;
            if (this.noActionTime > 600 && this.random.nextInt(800) == 0 && d > (double)l && this.removeWhenFarAway(d)) {
                this.discard();
            } else if (d < (double)l) {
                this.noActionTime = 0;
            }
        }
    }

    @Override
    protected final void serverAiStep() {
        ++this.noActionTime;
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("sensing");
        this.sensing.tick();
        profilerFiller.pop();
        int i = this.tickCount + this.getId();
        if (i % 2 == 0 || this.tickCount <= 1) {
            profilerFiller.push("targetSelector");
            this.targetSelector.tick();
            profilerFiller.pop();
            profilerFiller.push("goalSelector");
            this.goalSelector.tick();
            profilerFiller.pop();
        } else {
            profilerFiller.push("targetSelector");
            this.targetSelector.tickRunningGoals(false);
            profilerFiller.pop();
            profilerFiller.push("goalSelector");
            this.goalSelector.tickRunningGoals(false);
            profilerFiller.pop();
        }
        profilerFiller.push("navigation");
        this.navigation.tick();
        profilerFiller.pop();
        profilerFiller.push("mob tick");
        this.customServerAiStep((ServerLevel)this.level());
        profilerFiller.pop();
        profilerFiller.push("controls");
        profilerFiller.push("move");
        this.moveControl.tick();
        profilerFiller.popPush("look");
        this.lookControl.tick();
        profilerFiller.popPush("jump");
        this.jumpControl.tick();
        profilerFiller.pop();
        profilerFiller.pop();
    }

    protected void customServerAiStep(ServerLevel serverLevel) {
    }

    public int getMaxHeadXRot() {
        return 40;
    }

    public int getMaxHeadYRot() {
        return 75;
    }

    protected void clampHeadRotationToBody() {
        float f = this.getMaxHeadYRot();
        float g = this.getYHeadRot();
        float h = Mth.wrapDegrees(this.yBodyRot - g);
        float i = Mth.clamp(Mth.wrapDegrees(this.yBodyRot - g), -f, f);
        float j = g + h - i;
        this.setYHeadRot(j);
    }

    public int getHeadRotSpeed() {
        return 10;
    }

    public void lookAt(Entity entity, float f, float g) {
        double h;
        double d = entity.getX() - this.getX();
        double e = entity.getZ() - this.getZ();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            h = livingEntity.getEyeY() - this.getEyeY();
        } else {
            h = (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0 - this.getEyeY();
        }
        double i = Math.sqrt(d * d + e * e);
        float j = (float)(Mth.atan2(e, d) * 57.2957763671875) - 90.0f;
        float k = (float)(-(Mth.atan2(h, i) * 57.2957763671875));
        this.setXRot(this.rotlerp(this.getXRot(), k, g));
        this.setYRot(this.rotlerp(this.getYRot(), j, f));
    }

    private float rotlerp(float f, float g, float h) {
        float i = Mth.wrapDegrees(g - f);
        if (i > h) {
            i = h;
        }
        if (i < -h) {
            i = -h;
        }
        return f + i;
    }

    public static boolean checkMobSpawnRules(EntityType<? extends Mob> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        BlockPos blockPos2 = blockPos.below();
        return EntitySpawnReason.isSpawner(entitySpawnReason) || levelAccessor.getBlockState(blockPos2).isValidSpawn(levelAccessor, blockPos2, entityType);
    }

    public boolean checkSpawnRules(LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason) {
        return true;
    }

    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return !levelReader.containsAnyLiquid(this.getBoundingBox()) && levelReader.isUnobstructed(this);
    }

    public int getMaxSpawnClusterSize() {
        return 4;
    }

    public boolean isMaxGroupSizeReached(int i) {
        return false;
    }

    @Override
    public int getMaxFallDistance() {
        if (this.getTarget() == null) {
            return this.getComfortableFallDistance(0.0f);
        }
        int i = (int)(this.getHealth() - this.getMaxHealth() * 0.33f);
        if ((i -= (3 - this.level().getDifficulty().getId()) * 4) < 0) {
            i = 0;
        }
        return this.getComfortableFallDistance(i);
    }

    public ItemStack getBodyArmorItem() {
        return this.getItemBySlot(EquipmentSlot.BODY);
    }

    public boolean isSaddled() {
        return this.hasValidEquippableItemForSlot(EquipmentSlot.SADDLE);
    }

    public boolean isWearingBodyArmor() {
        return this.hasValidEquippableItemForSlot(EquipmentSlot.BODY);
    }

    private boolean hasValidEquippableItemForSlot(EquipmentSlot equipmentSlot) {
        return this.hasItemInSlot(equipmentSlot) && this.isEquippableInSlot(this.getItemBySlot(equipmentSlot), equipmentSlot);
    }

    public void setBodyArmorItem(ItemStack itemStack) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.BODY, itemStack);
    }

    public Container createEquipmentSlotContainer(final EquipmentSlot equipmentSlot) {
        return new ContainerSingleItem(){

            @Override
            public ItemStack getTheItem() {
                return Mob.this.getItemBySlot(equipmentSlot);
            }

            @Override
            public void setTheItem(ItemStack itemStack) {
                Mob.this.setItemSlot(equipmentSlot, itemStack);
                if (!itemStack.isEmpty()) {
                    Mob.this.setGuaranteedDrop(equipmentSlot);
                    Mob.this.setPersistenceRequired();
                }
            }

            @Override
            public void setChanged() {
            }

            @Override
            public boolean stillValid(Player player) {
                return player.getVehicle() == Mob.this || player.isWithinEntityInteractionRange(Mob.this, 4.0);
            }
        };
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        super.dropCustomDeathLoot(serverLevel, damageSource, bl);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            float f = this.dropChances.byEquipment(equipmentSlot);
            if (f == 0.0f) continue;
            boolean bl2 = this.dropChances.isPreserved(equipmentSlot);
            Object object = damageSource.getEntity();
            if (object instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)object;
                object = this.level();
                if (object instanceof ServerLevel) {
                    ServerLevel serverLevel2 = (ServerLevel)object;
                    f = EnchantmentHelper.processEquipmentDropChance(serverLevel2, livingEntity, damageSource, f);
                }
            }
            if (itemStack.isEmpty() || EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) || !bl && !bl2 || !(this.random.nextFloat() < f)) continue;
            if (!bl2 && itemStack.isDamageableItem()) {
                itemStack.setDamageValue(itemStack.getMaxDamage() - this.random.nextInt(1 + this.random.nextInt(Math.max(itemStack.getMaxDamage() - 3, 1))));
            }
            this.spawnAtLocation(serverLevel, itemStack);
            this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
        }
    }

    public DropChances getDropChances() {
        return this.dropChances;
    }

    public void dropPreservedEquipment(ServerLevel serverLevel) {
        this.dropPreservedEquipment(serverLevel, itemStack -> true);
    }

    public Set<EquipmentSlot> dropPreservedEquipment(ServerLevel serverLevel, Predicate<ItemStack> predicate) {
        HashSet<EquipmentSlot> set = new HashSet<EquipmentSlot>();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (itemStack.isEmpty()) continue;
            if (!predicate.test(itemStack)) {
                set.add(equipmentSlot);
                continue;
            }
            if (!this.dropChances.isPreserved(equipmentSlot)) continue;
            this.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            this.spawnAtLocation(serverLevel, itemStack);
        }
        return set;
    }

    private LootParams createEquipmentParams(ServerLevel serverLevel) {
        return new LootParams.Builder(serverLevel).withParameter(LootContextParams.ORIGIN, this.position()).withParameter(LootContextParams.THIS_ENTITY, this).create(LootContextParamSets.EQUIPMENT);
    }

    public void equip(EquipmentTable equipmentTable) {
        this.equip(equipmentTable.lootTable(), equipmentTable.slotDropChances());
    }

    public void equip(ResourceKey<LootTable> resourceKey, Map<EquipmentSlot, Float> map) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.equip(resourceKey, this.createEquipmentParams(serverLevel), map);
        }
    }

    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        if (randomSource.nextFloat() < 0.15f * difficultyInstance.getSpecialMultiplier()) {
            int i = randomSource.nextInt(3);
            int j = 1;
            while ((float)j <= 3.0f) {
                if (randomSource.nextFloat() < 0.1087f) {
                    ++i;
                }
                ++j;
            }
            float f = this.level().getDifficulty() == Difficulty.HARD ? 0.1f : 0.25f;
            boolean bl = true;
            for (EquipmentSlot equipmentSlot : EQUIPMENT_POPULATION_ORDER) {
                Item item;
                ItemStack itemStack = this.getItemBySlot(equipmentSlot);
                if (!bl && randomSource.nextFloat() < f) break;
                bl = false;
                if (!itemStack.isEmpty() || (item = Mob.getEquipmentForSlot(equipmentSlot, i)) == null) continue;
                this.setItemSlot(equipmentSlot, new ItemStack(item));
            }
        }
    }

    public static @Nullable Item getEquipmentForSlot(EquipmentSlot equipmentSlot, int i) {
        switch (equipmentSlot) {
            case HEAD: {
                if (i == 0) {
                    return Items.LEATHER_HELMET;
                }
                if (i == 1) {
                    return Items.COPPER_HELMET;
                }
                if (i == 2) {
                    return Items.GOLDEN_HELMET;
                }
                if (i == 3) {
                    return Items.CHAINMAIL_HELMET;
                }
                if (i == 4) {
                    return Items.IRON_HELMET;
                }
                if (i == 5) {
                    return Items.DIAMOND_HELMET;
                }
            }
            case CHEST: {
                if (i == 0) {
                    return Items.LEATHER_CHESTPLATE;
                }
                if (i == 1) {
                    return Items.COPPER_CHESTPLATE;
                }
                if (i == 2) {
                    return Items.GOLDEN_CHESTPLATE;
                }
                if (i == 3) {
                    return Items.CHAINMAIL_CHESTPLATE;
                }
                if (i == 4) {
                    return Items.IRON_CHESTPLATE;
                }
                if (i == 5) {
                    return Items.DIAMOND_CHESTPLATE;
                }
            }
            case LEGS: {
                if (i == 0) {
                    return Items.LEATHER_LEGGINGS;
                }
                if (i == 1) {
                    return Items.COPPER_LEGGINGS;
                }
                if (i == 2) {
                    return Items.GOLDEN_LEGGINGS;
                }
                if (i == 3) {
                    return Items.CHAINMAIL_LEGGINGS;
                }
                if (i == 4) {
                    return Items.IRON_LEGGINGS;
                }
                if (i == 5) {
                    return Items.DIAMOND_LEGGINGS;
                }
            }
            case FEET: {
                if (i == 0) {
                    return Items.LEATHER_BOOTS;
                }
                if (i == 1) {
                    return Items.COPPER_BOOTS;
                }
                if (i == 2) {
                    return Items.GOLDEN_BOOTS;
                }
                if (i == 3) {
                    return Items.CHAINMAIL_BOOTS;
                }
                if (i == 4) {
                    return Items.IRON_BOOTS;
                }
                if (i != 5) break;
                return Items.DIAMOND_BOOTS;
            }
        }
        return null;
    }

    protected void populateDefaultEquipmentEnchantments(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.enchantSpawnedWeapon(serverLevelAccessor, randomSource, difficultyInstance);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            this.enchantSpawnedArmor(serverLevelAccessor, randomSource, equipmentSlot, difficultyInstance);
        }
    }

    protected void enchantSpawnedWeapon(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, DifficultyInstance difficultyInstance) {
        this.enchantSpawnedEquipment(serverLevelAccessor, EquipmentSlot.MAINHAND, randomSource, 0.25f, difficultyInstance);
    }

    protected void enchantSpawnedArmor(ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, EquipmentSlot equipmentSlot, DifficultyInstance difficultyInstance) {
        this.enchantSpawnedEquipment(serverLevelAccessor, equipmentSlot, randomSource, 0.5f, difficultyInstance);
    }

    private void enchantSpawnedEquipment(ServerLevelAccessor serverLevelAccessor, EquipmentSlot equipmentSlot, RandomSource randomSource, float f, DifficultyInstance difficultyInstance) {
        ItemStack itemStack = this.getItemBySlot(equipmentSlot);
        if (!itemStack.isEmpty() && randomSource.nextFloat() < f * difficultyInstance.getSpecialMultiplier()) {
            EnchantmentHelper.enchantItemFromProvider(itemStack, serverLevelAccessor.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficultyInstance, randomSource);
            this.setItemSlot(equipmentSlot, itemStack);
        }
    }

    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        AttributeInstance attributeInstance = Objects.requireNonNull(this.getAttribute(Attributes.FOLLOW_RANGE));
        if (!attributeInstance.hasModifier(RANDOM_SPAWN_BONUS_ID)) {
            attributeInstance.addPermanentModifier(new AttributeModifier(RANDOM_SPAWN_BONUS_ID, randomSource.triangle(0.0, 0.11485000000000001), AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        this.setLeftHanded(randomSource.nextFloat() < 0.05f);
        return spawnGroupData;
    }

    public void setPersistenceRequired() {
        this.persistenceRequired = true;
    }

    @Override
    public void setDropChance(EquipmentSlot equipmentSlot, float f) {
        this.dropChances = this.dropChances.withEquipmentChance(equipmentSlot, f);
    }

    @Override
    public boolean canPickUpLoot() {
        return this.canPickUpLoot;
    }

    public void setCanPickUpLoot(boolean bl) {
        this.canPickUpLoot = bl;
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
        return this.canPickUpLoot();
    }

    public boolean isPersistenceRequired() {
        return this.persistenceRequired;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (!this.isAlive()) {
            return InteractionResult.PASS;
        }
        InteractionResult interactionResult = this.checkAndHandleImportantInteractions(player, interactionHand);
        if (interactionResult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return interactionResult;
        }
        InteractionResult interactionResult2 = super.interact(player, interactionHand);
        if (interactionResult2 != InteractionResult.PASS) {
            return interactionResult2;
        }
        interactionResult = this.mobInteract(player, interactionHand);
        if (interactionResult.consumesAction()) {
            this.gameEvent(GameEvent.ENTITY_INTERACT, player);
            return interactionResult;
        }
        return InteractionResult.PASS;
    }

    private InteractionResult checkAndHandleImportantInteractions(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.NAME_TAG) && (interactionResult = itemStack.interactLivingEntity(player, this, interactionHand)).consumesAction()) {
            return interactionResult;
        }
        Item item = itemStack.getItem();
        if (item instanceof SpawnEggItem) {
            SpawnEggItem spawnEggItem = (SpawnEggItem)item;
            if (this.level() instanceof ServerLevel) {
                Optional<Mob> optional = spawnEggItem.spawnOffspringFromSpawnEgg(player, this, this.getType(), (ServerLevel)this.level(), this.position(), itemStack);
                optional.ifPresent(mob -> this.onOffspringSpawnedFromEgg(player, (Mob)mob));
                if (optional.isEmpty()) {
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    protected void onOffspringSpawnedFromEgg(Player player, Mob mob) {
    }

    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        return InteractionResult.PASS;
    }

    protected void usePlayerItem(Player player, InteractionHand interactionHand, ItemStack itemStack) {
        int i = itemStack.getCount();
        UseRemainder useRemainder = itemStack.get(DataComponents.USE_REMAINDER);
        itemStack.consume(1, player);
        if (useRemainder != null) {
            ItemStack itemStack2 = useRemainder.convertIntoRemainder(itemStack, i, player.hasInfiniteMaterials(), player::handleExtraItemsCreatedOnUse);
            player.setItemInHand(interactionHand, itemStack2);
        }
    }

    public boolean isWithinHome() {
        return this.isWithinHome(this.blockPosition());
    }

    public boolean isWithinHome(BlockPos blockPos) {
        if (this.homeRadius == -1) {
            return true;
        }
        return this.homePosition.distSqr(blockPos) < (double)(this.homeRadius * this.homeRadius);
    }

    public boolean isWithinHome(Vec3 vec3) {
        if (this.homeRadius == -1) {
            return true;
        }
        return this.homePosition.distToCenterSqr(vec3) < (double)(this.homeRadius * this.homeRadius);
    }

    public void setHomeTo(BlockPos blockPos, int i) {
        this.homePosition = blockPos;
        this.homeRadius = i;
    }

    public BlockPos getHomePosition() {
        return this.homePosition;
    }

    public int getHomeRadius() {
        return this.homeRadius;
    }

    public void clearHome() {
        this.homeRadius = -1;
    }

    public boolean hasHome() {
        return this.homeRadius != -1;
    }

    public <T extends Mob> @Nullable T convertTo(EntityType<T> entityType, ConversionParams conversionParams, EntitySpawnReason entitySpawnReason, ConversionParams.AfterConversion<T> afterConversion) {
        if (this.isRemoved()) {
            return null;
        }
        Mob mob = (Mob)entityType.create(this.level(), entitySpawnReason);
        if (mob == null) {
            return null;
        }
        conversionParams.type().convert(this, mob, conversionParams);
        afterConversion.finalizeConversion(mob);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            serverLevel.addFreshEntity(mob);
        }
        if (conversionParams.type().shouldDiscardAfterConversion()) {
            this.discard();
        }
        return (T)mob;
    }

    public <T extends Mob> @Nullable T convertTo(EntityType<T> entityType, ConversionParams conversionParams, ConversionParams.AfterConversion<T> afterConversion) {
        return this.convertTo(entityType, conversionParams, EntitySpawnReason.CONVERSION, afterConversion);
    }

    @Override
    public @Nullable Leashable.LeashData getLeashData() {
        return this.leashData;
    }

    private void resetAngularLeashMomentum() {
        if (this.leashData != null) {
            this.leashData.angularMomentum = 0.0;
        }
    }

    @Override
    public void setLeashData(@Nullable Leashable.LeashData leashData) {
        this.leashData = leashData;
    }

    @Override
    public void onLeashRemoved() {
        if (this.getLeashData() == null) {
            this.clearHome();
        }
    }

    @Override
    public void leashTooFarBehaviour() {
        Leashable.super.leashTooFarBehaviour();
        this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
    }

    @Override
    public boolean canBeLeashed() {
        return !(this instanceof Enemy);
    }

    @Override
    public boolean startRiding(Entity entity, boolean bl, boolean bl2) {
        boolean bl3 = super.startRiding(entity, bl, bl2);
        if (bl3 && this.isLeashed()) {
            this.dropLeash();
        }
        return bl3;
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !this.isNoAi();
    }

    public void setNoAi(boolean bl) {
        byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 1) : (byte)(b & 0xFFFFFFFE));
    }

    public void setLeftHanded(boolean bl) {
        byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 2) : (byte)(b & 0xFFFFFFFD));
    }

    public void setAggressive(boolean bl) {
        byte b = this.entityData.get(DATA_MOB_FLAGS_ID);
        this.entityData.set(DATA_MOB_FLAGS_ID, bl ? (byte)(b | 4) : (byte)(b & 0xFFFFFFFB));
    }

    public boolean isNoAi() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 1) != 0;
    }

    public boolean isLeftHanded() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 2) != 0;
    }

    public boolean isAggressive() {
        return (this.entityData.get(DATA_MOB_FLAGS_ID) & 4) != 0;
    }

    public void setBaby(boolean bl) {
    }

    @Override
    public HumanoidArm getMainArm() {
        return this.isLeftHanded() ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
    }

    public boolean isWithinMeleeAttackRange(LivingEntity livingEntity) {
        double e;
        double d;
        AttackRange attackRange = this.getActiveItem().get(DataComponents.ATTACK_RANGE);
        if (attackRange == null) {
            d = DEFAULT_ATTACK_REACH;
            e = 0.0;
        } else {
            d = attackRange.effectiveMaxRange(this);
            e = attackRange.effectiveMinRange(this);
        }
        AABB aABB = livingEntity.getHitbox();
        return this.getAttackBoundingBox(d).intersects(aABB) && (e <= 0.0 || !this.getAttackBoundingBox(e).intersects(aABB));
    }

    protected AABB getAttackBoundingBox(double d) {
        AABB aABB3;
        Entity entity = this.getVehicle();
        if (entity != null) {
            AABB aABB = entity.getBoundingBox();
            AABB aABB2 = this.getBoundingBox();
            aABB3 = new AABB(Math.min(aABB2.minX, aABB.minX), aABB2.minY, Math.min(aABB2.minZ, aABB.minZ), Math.max(aABB2.maxX, aABB.maxX), aABB2.maxY, Math.max(aABB2.maxZ, aABB.maxZ));
        } else {
            aABB3 = this.getBoundingBox();
        }
        return aABB3.inflate(d, 0.0, d);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        float f = (float)this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        ItemStack itemStack = this.getWeaponItem();
        DamageSource damageSource = itemStack.getDamageSource(this, () -> this.damageSources().mobAttack(this));
        f = EnchantmentHelper.modifyDamage(serverLevel, itemStack, entity, damageSource, f);
        f += itemStack.getItem().getAttackDamageBonus(entity, f, damageSource);
        Vec3 vec3 = entity.getDeltaMovement();
        boolean bl = entity.hurtServer(serverLevel, damageSource, f);
        if (bl) {
            this.causeExtraKnockback(entity, this.getKnockback(entity, damageSource), vec3);
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                itemStack.hurtEnemy(livingEntity, this);
            }
            EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
            this.setLastHurtMob(entity);
            this.playAttackSound();
        }
        this.lungeForwardMaybe();
        return bl;
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> tagKey) {
        if (this.getNavigation().canFloat()) {
            super.jumpInLiquid(tagKey);
        } else {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.3, 0.0));
        }
    }

    @VisibleForTesting
    public void removeFreeWill() {
        this.removeAllGoals(goal -> true);
        this.getBrain().removeAllBehaviors();
    }

    public void removeAllGoals(Predicate<Goal> predicate) {
        this.goalSelector.removeAllGoals(predicate);
    }

    @Override
    protected void removeAfterChangingDimensions() {
        super.removeAfterChangingDimensions();
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.getItemBySlot(equipmentSlot);
            if (itemStack.isEmpty()) continue;
            itemStack.setCount(0);
        }
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        SpawnEggItem spawnEggItem = SpawnEggItem.byId(this.getType());
        if (spawnEggItem == null) {
            return null;
        }
        return new ItemStack(spawnEggItem);
    }

    @Override
    protected void onAttributeUpdated(Holder<Attribute> holder) {
        super.onAttributeUpdated(holder);
        if (holder.is(Attributes.FOLLOW_RANGE) || holder.is(Attributes.TEMPT_RANGE)) {
            this.getNavigation().updatePathfinderMaxVisitedNodes();
        }
    }

    @Override
    public void registerDebugValues(ServerLevel serverLevel, DebugValueSource.Registration registration) {
        registration.register(DebugSubscriptions.ENTITY_PATHS, () -> {
            Path path = this.getNavigation().getPath();
            if (path != null && path.debugData() != null) {
                return new DebugPathInfo(path.copy(), this.getNavigation().getMaxDistanceToWaypoint());
            }
            return null;
        });
        registration.register(DebugSubscriptions.GOAL_SELECTORS, () -> {
            Set<WrappedGoal> set = this.goalSelector.getAvailableGoals();
            ArrayList<DebugGoalInfo.DebugGoal> list = new ArrayList<DebugGoalInfo.DebugGoal>(set.size());
            set.forEach(wrappedGoal -> list.add(new DebugGoalInfo.DebugGoal(wrappedGoal.getPriority(), wrappedGoal.isRunning(), wrappedGoal.getGoal().getClass().getSimpleName())));
            return new DebugGoalInfo(list);
        });
        if (!this.brain.isBrainDead()) {
            registration.register(DebugSubscriptions.BRAINS, () -> DebugBrainDump.takeBrainDump(serverLevel, this));
        }
    }

    public float chargeSpeedModifier() {
        return 1.0f;
    }
}

