/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  java.lang.runtime.SwitchBootstraps
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.debug;

import java.lang.invoke.LambdaMetafactory;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringUtil;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.schedule.Activity;
import org.jspecify.annotations.Nullable;

public record DebugBrainDump(String name, String profession, int xp, float health, float maxHealth, String inventory, boolean wantsGolem, int angerLevel, List<String> activities, List<String> behaviors, List<String> memories, List<String> gossips, Set<BlockPos> pois, Set<BlockPos> potentialPois) {
    public static final StreamCodec<FriendlyByteBuf, DebugBrainDump> STREAM_CODEC = StreamCodec.of((friendlyByteBuf, debugBrainDump) -> debugBrainDump.write((FriendlyByteBuf)((Object)friendlyByteBuf)), DebugBrainDump::new);

    public DebugBrainDump(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readUtf(), friendlyByteBuf.readUtf(), friendlyByteBuf.readInt(), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readUtf(), friendlyByteBuf.readBoolean(), friendlyByteBuf.readInt(), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readList(FriendlyByteBuf::readUtf), friendlyByteBuf.readCollection(HashSet::new, BlockPos.STREAM_CODEC), friendlyByteBuf.readCollection(HashSet::new, BlockPos.STREAM_CODEC));
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(this.name);
        friendlyByteBuf.writeUtf(this.profession);
        friendlyByteBuf.writeInt(this.xp);
        friendlyByteBuf.writeFloat(this.health);
        friendlyByteBuf.writeFloat(this.maxHealth);
        friendlyByteBuf.writeUtf(this.inventory);
        friendlyByteBuf.writeBoolean(this.wantsGolem);
        friendlyByteBuf.writeInt(this.angerLevel);
        friendlyByteBuf.writeCollection(this.activities, FriendlyByteBuf::writeUtf);
        friendlyByteBuf.writeCollection(this.behaviors, FriendlyByteBuf::writeUtf);
        friendlyByteBuf.writeCollection(this.memories, FriendlyByteBuf::writeUtf);
        friendlyByteBuf.writeCollection(this.gossips, FriendlyByteBuf::writeUtf);
        friendlyByteBuf.writeCollection(this.pois, BlockPos.STREAM_CODEC);
        friendlyByteBuf.writeCollection(this.potentialPois, BlockPos.STREAM_CODEC);
    }

    public static DebugBrainDump takeBrainDump(ServerLevel serverLevel, LivingEntity livingEntity) {
        List<String> list;
        int n;
        Villager villager2;
        boolean bl;
        InventoryCarrier inventoryCarrier;
        SimpleContainer container;
        int i;
        String string2;
        String string3 = DebugEntityNameGenerator.getEntityName(livingEntity);
        if (livingEntity instanceof Villager) {
            Villager villager = (Villager)livingEntity;
            string2 = villager.getVillagerData().profession().getRegisteredName();
            i = villager.getVillagerXp();
        } else {
            string2 = "";
            i = 0;
        }
        float f = livingEntity.getHealth();
        float g = livingEntity.getMaxHealth();
        Brain<?> brain = livingEntity.getBrain();
        long l = livingEntity.level().getGameTime();
        String string32 = livingEntity instanceof InventoryCarrier ? ((container = (inventoryCarrier = (InventoryCarrier)((Object)livingEntity)).getInventory()).isEmpty() ? "" : ((Object)container).toString()) : "";
        boolean bl2 = bl = livingEntity instanceof Villager && (villager2 = (Villager)livingEntity).wantsToSpawnGolem(l);
        if (livingEntity instanceof Warden) {
            Warden warden = (Warden)livingEntity;
            n = warden.getClientAngerLevel();
        } else {
            n = -1;
        }
        int j = n;
        List list2 = brain.getActiveActivities().stream().map(Activity::getName).toList();
        List list22 = brain.getRunningBehaviors().stream().map(BehaviorControl::debugString).toList();
        List list3 = DebugBrainDump.getMemoryDescriptions(serverLevel, livingEntity, l).map(string -> StringUtil.truncateStringIfNecessary(string, 255, true)).toList();
        Set<BlockPos> set = DebugBrainDump.getKnownBlockPositions(brain, MemoryModuleType.JOB_SITE, MemoryModuleType.HOME, MemoryModuleType.MEETING_POINT);
        Set<BlockPos> set2 = DebugBrainDump.getKnownBlockPositions(brain, MemoryModuleType.POTENTIAL_JOB_SITE);
        if (livingEntity instanceof Villager) {
            Villager villager3 = (Villager)livingEntity;
            list = DebugBrainDump.getVillagerGossips(villager3);
        } else {
            list = List.of();
        }
        List<String> list4 = list;
        return new DebugBrainDump(string3, string2, i, f, g, string32, bl, j, list2, list22, list3, list4, set, set2);
    }

    @SafeVarargs
    private static Set<BlockPos> getKnownBlockPositions(Brain<?> brain, MemoryModuleType<GlobalPos> ... memoryModuleTypes) {
        return Stream.of(memoryModuleTypes).filter(brain::hasMemoryValue).map(brain::getMemory).flatMap((Function<Optional, Stream>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, stream(), (Ljava/util/Optional;)Ljava/util/stream/Stream;)()).map(GlobalPos::pos).collect(Collectors.toSet());
    }

    private static List<String> getVillagerGossips(Villager villager) {
        ArrayList<String> list = new ArrayList<String>();
        villager.getGossips().getGossipEntries().forEach((uUID, object2IntMap) -> {
            String string = DebugEntityNameGenerator.getEntityName(uUID);
            object2IntMap.forEach((gossipType, i) -> list.add(string + ": " + String.valueOf(gossipType) + ": " + i));
        });
        return list;
    }

    private static Stream<String> getMemoryDescriptions(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return livingEntity.getBrain().getMemories().entrySet().stream().map(entry -> {
            MemoryModuleType memoryModuleType = (MemoryModuleType)entry.getKey();
            Optional optional = (Optional)entry.getValue();
            return DebugBrainDump.getMemoryDescription(serverLevel, l, memoryModuleType, optional);
        }).sorted();
    }

    private static String getMemoryDescription(ServerLevel serverLevel, long l, MemoryModuleType<?> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
        Object string;
        if (optional.isPresent()) {
            ExpirableValue<?> expirableValue = optional.get();
            Object object = expirableValue.getValue();
            if (memoryModuleType == MemoryModuleType.HEARD_BELL_TIME) {
                long m = l - (Long)object;
                string = m + " ticks ago";
            } else {
                string = expirableValue.canExpire() ? DebugBrainDump.getShortDescription(serverLevel, object) + " (ttl: " + expirableValue.getTimeToLive() + ")" : DebugBrainDump.getShortDescription(serverLevel, object);
            }
        } else {
            string = "-";
        }
        return BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(memoryModuleType).getPath() + ": " + (String)string;
    }

    private static String getShortDescription(ServerLevel serverLevel, @Nullable Object object2) {
        Object object3 = object2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{UUID.class, Entity.class, WalkTarget.class, EntityTracker.class, GlobalPos.class, BlockPosTracker.class, DamageSource.class, Collection.class}, (Object)object3, (int)n)) {
            case -1 -> "-";
            case 0 -> {
                UUID uUID = (UUID)object3;
                yield DebugBrainDump.getShortDescription(serverLevel, serverLevel.getEntity(uUID));
            }
            case 1 -> {
                Entity entity = (Entity)object3;
                yield DebugEntityNameGenerator.getEntityName(entity);
            }
            case 2 -> {
                WalkTarget walkTarget = (WalkTarget)object3;
                yield DebugBrainDump.getShortDescription(serverLevel, walkTarget.getTarget());
            }
            case 3 -> {
                EntityTracker entityTracker = (EntityTracker)object3;
                yield DebugBrainDump.getShortDescription(serverLevel, entityTracker.getEntity());
            }
            case 4 -> {
                GlobalPos globalPos = (GlobalPos)((Object)object3);
                yield DebugBrainDump.getShortDescription(serverLevel, globalPos.pos());
            }
            case 5 -> {
                BlockPosTracker blockPosTracker = (BlockPosTracker)object3;
                yield DebugBrainDump.getShortDescription(serverLevel, blockPosTracker.currentBlockPosition());
            }
            case 6 -> {
                DamageSource damageSource = (DamageSource)object3;
                Entity entity2 = damageSource.getEntity();
                if (entity2 == null) {
                    yield object2.toString();
                }
                yield DebugBrainDump.getShortDescription(serverLevel, entity2);
            }
            case 7 -> {
                Collection collection = (Collection)object3;
                yield "[" + collection.stream().map(object -> DebugBrainDump.getShortDescription(serverLevel, object)).collect(Collectors.joining(", ")) + "]";
            }
            default -> object2.toString();
        };
    }

    public boolean hasPoi(BlockPos blockPos) {
        return this.pois.contains(blockPos);
    }

    public boolean hasPotentialPoi(BlockPos blockPos) {
        return this.potentialPois.contains(blockPos);
    }
}

