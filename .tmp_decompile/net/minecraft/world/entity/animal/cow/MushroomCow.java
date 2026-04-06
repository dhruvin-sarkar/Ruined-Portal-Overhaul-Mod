/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.entity.animal.cow;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SpellParticleOption;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.animal.cow.AbstractCow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SuspiciousEffectHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jspecify.annotations.Nullable;

public class MushroomCow
extends AbstractCow
implements Shearable {
    private static final EntityDataAccessor<Integer> DATA_TYPE = SynchedEntityData.defineId(MushroomCow.class, EntityDataSerializers.INT);
    private static final int MUTATE_CHANCE = 1024;
    private static final String TAG_STEW_EFFECTS = "stew_effects";
    private @Nullable SuspiciousStewEffects stewEffects;
    private @Nullable UUID lastLightningBoltUUID;

    public MushroomCow(EntityType<? extends MushroomCow> entityType, Level level) {
        super((EntityType<? extends AbstractCow>)entityType, level);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos.below()).is(Blocks.MYCELIUM)) {
            return 10.0f;
        }
        return levelReader.getPathfindingCostFromLightLevels(blockPos);
    }

    public static boolean checkMushroomSpawnRules(EntityType<MushroomCow> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.MOOSHROOMS_SPAWNABLE_ON) && MushroomCow.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        UUID uUID = lightningBolt.getUUID();
        if (!uUID.equals(this.lastLightningBoltUUID)) {
            this.setVariant(this.getVariant() == Variant.RED ? Variant.BROWN : Variant.RED);
            this.lastLightningBoltUUID = uUID;
            this.playSound(SoundEvents.MOOSHROOM_CONVERT, 2.0f, 1.0f);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE, Variant.DEFAULT.id);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(Items.BOWL) && !this.isBaby()) {
            ItemStack itemStack2;
            boolean bl = false;
            if (this.stewEffects != null) {
                bl = true;
                itemStack2 = new ItemStack(Items.SUSPICIOUS_STEW);
                itemStack2.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, this.stewEffects);
                this.stewEffects = null;
            } else {
                itemStack2 = new ItemStack(Items.MUSHROOM_STEW);
            }
            ItemStack itemStack3 = ItemUtils.createFilledResult(itemStack, player, itemStack2, false);
            player.setItemInHand(interactionHand, itemStack3);
            SoundEvent soundEvent = bl ? SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY : SoundEvents.MOOSHROOM_MILK;
            this.playSound(soundEvent, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        if (itemStack.is(Items.SHEARS) && this.readyForShearing()) {
            Level bl = this.level();
            if (bl instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)bl;
                this.shear(serverLevel, SoundSource.PLAYERS, itemStack);
                this.gameEvent(GameEvent.SHEAR, player);
                itemStack.hurtAndBreak(1, (LivingEntity)player, interactionHand.asEquipmentSlot());
            }
            return InteractionResult.SUCCESS;
        }
        if (this.getVariant() == Variant.BROWN) {
            Optional<SuspiciousStewEffects> optional = this.getEffectsFromItemStack(itemStack);
            if (optional.isEmpty()) {
                return super.mobInteract(player, interactionHand);
            }
            if (this.stewEffects != null) {
                for (int i = 0; i < 2; ++i) {
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX() + this.random.nextDouble() / 2.0, this.getY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
            } else {
                itemStack.consume(1, player);
                SpellParticleOption spellParticleOption = SpellParticleOption.create(ParticleTypes.EFFECT, -1, 1.0f);
                for (int j = 0; j < 4; ++j) {
                    this.level().addParticle(spellParticleOption, this.getX() + this.random.nextDouble() / 2.0, this.getY(0.5), this.getZ() + this.random.nextDouble() / 2.0, 0.0, this.random.nextDouble() / 5.0, 0.0);
                }
                this.stewEffects = optional.get();
                this.playSound(SoundEvents.MOOSHROOM_EAT, 2.0f, 1.0f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    public void shear(ServerLevel serverLevel, SoundSource soundSource, ItemStack itemStack) {
        serverLevel.playSound(null, this, SoundEvents.MOOSHROOM_SHEAR, soundSource, 1.0f, 1.0f);
        this.convertTo(EntityType.COW, ConversionParams.single(this, false, false), cow -> {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(0.5), this.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
            this.dropFromShearingLootTable(serverLevel, BuiltInLootTables.SHEAR_MOOSHROOM, itemStack, (serverLevel, itemStack) -> {
                for (int i = 0; i < itemStack.getCount(); ++i) {
                    serverLevel.addFreshEntity(new ItemEntity(this.level(), this.getX(), this.getY(1.0), this.getZ(), itemStack.copyWithCount(1)));
                }
            });
        });
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isBaby();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("Type", Variant.CODEC, this.getVariant());
        valueOutput.storeNullable(TAG_STEW_EFFECTS, SuspiciousStewEffects.CODEC, this.stewEffects);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setVariant(valueInput.read("Type", Variant.CODEC).orElse(Variant.DEFAULT));
        this.stewEffects = valueInput.read(TAG_STEW_EFFECTS, SuspiciousStewEffects.CODEC).orElse(null);
    }

    private Optional<SuspiciousStewEffects> getEffectsFromItemStack(ItemStack itemStack) {
        SuspiciousEffectHolder suspiciousEffectHolder = SuspiciousEffectHolder.tryGet(itemStack.getItem());
        if (suspiciousEffectHolder != null) {
            return Optional.of(suspiciousEffectHolder.getSuspiciousEffects());
        }
        return Optional.empty();
    }

    private void setVariant(Variant variant) {
        this.entityData.set(DATA_TYPE, variant.id);
    }

    public Variant getVariant() {
        return Variant.byId(this.entityData.get(DATA_TYPE));
    }

    @Override
    public <T> @Nullable T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.MOOSHROOM_VARIANT) {
            return MushroomCow.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.MOOSHROOM_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T object) {
        if (dataComponentType == DataComponents.MOOSHROOM_VARIANT) {
            this.setVariant(MushroomCow.castComponentValue(DataComponents.MOOSHROOM_VARIANT, object));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, object);
    }

    @Override
    public @Nullable MushroomCow getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        MushroomCow mushroomCow = EntityType.MOOSHROOM.create(serverLevel, EntitySpawnReason.BREEDING);
        if (mushroomCow != null) {
            mushroomCow.setVariant(this.getOffspringVariant((MushroomCow)ageableMob));
        }
        return mushroomCow;
    }

    private Variant getOffspringVariant(MushroomCow mushroomCow) {
        Variant variant2;
        Variant variant = this.getVariant();
        Variant variant3 = variant == (variant2 = mushroomCow.getVariant()) && this.random.nextInt(1024) == 0 ? (variant == Variant.BROWN ? Variant.RED : Variant.BROWN) : (this.random.nextBoolean() ? variant : variant2);
        return variant3;
    }

    @Override
    public /* synthetic */ @Nullable AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    public static enum Variant implements StringRepresentable
    {
        RED("red", 0, Blocks.RED_MUSHROOM.defaultBlockState()),
        BROWN("brown", 1, Blocks.BROWN_MUSHROOM.defaultBlockState());

        public static final Variant DEFAULT;
        public static final Codec<Variant> CODEC;
        private static final IntFunction<Variant> BY_ID;
        public static final StreamCodec<ByteBuf, Variant> STREAM_CODEC;
        private final String type;
        final int id;
        private final BlockState blockState;

        private Variant(String string2, int j, BlockState blockState) {
            this.type = string2;
            this.id = j;
            this.blockState = blockState;
        }

        public BlockState getBlockState() {
            return this.blockState;
        }

        @Override
        public String getSerializedName() {
            return this.type;
        }

        private int id() {
            return this.id;
        }

        static Variant byId(int i) {
            return BY_ID.apply(i);
        }

        static {
            DEFAULT = RED;
            CODEC = StringRepresentable.fromEnum(Variant::values);
            BY_ID = ByIdMap.continuous(Variant::id, Variant.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Variant::id);
        }
    }
}

