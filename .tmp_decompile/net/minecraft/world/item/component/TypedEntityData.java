/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  io.netty.buffer.ByteBuf
 *  org.slf4j.Logger
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public final class TypedEntityData<IdType>
implements TooltipProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TYPE_TAG = "id";
    final IdType type;
    final CompoundTag tag;

    public static <T> Codec<TypedEntityData<T>> codec(final Codec<T> codec) {
        return new Codec<TypedEntityData<T>>(){

            public <V> DataResult<Pair<TypedEntityData<T>, V>> decode(DynamicOps<V> dynamicOps, V object) {
                return CustomData.COMPOUND_TAG_CODEC.decode(dynamicOps, object).flatMap(pair -> {
                    CompoundTag compoundTag = ((CompoundTag)pair.getFirst()).copy();
                    Tag tag = compoundTag.remove(TypedEntityData.TYPE_TAG);
                    if (tag == null) {
                        return DataResult.error(() -> "Expected 'id' field in " + String.valueOf(object));
                    }
                    return codec.parse(1.asNbtOps(dynamicOps), (Object)tag).map(object -> Pair.of(new TypedEntityData<Object>(object, compoundTag), (Object)pair.getSecond()));
                });
            }

            public <V> DataResult<V> encode(TypedEntityData<T> typedEntityData, DynamicOps<V> dynamicOps, V object) {
                return codec.encodeStart(1.asNbtOps(dynamicOps), typedEntityData.type).flatMap(tag -> {
                    CompoundTag compoundTag = typedEntityData.tag.copy();
                    compoundTag.put(TypedEntityData.TYPE_TAG, (Tag)tag);
                    return CustomData.COMPOUND_TAG_CODEC.encode((Object)compoundTag, dynamicOps, object);
                });
            }

            private static <T> DynamicOps<Tag> asNbtOps(DynamicOps<T> dynamicOps) {
                if (dynamicOps instanceof RegistryOps) {
                    RegistryOps registryOps = (RegistryOps)dynamicOps;
                    return registryOps.withParent(NbtOps.INSTANCE);
                }
                return NbtOps.INSTANCE;
            }

            public /* synthetic */ DataResult encode(Object object, DynamicOps dynamicOps, Object object2) {
                return this.encode((TypedEntityData)object, dynamicOps, object2);
            }
        };
    }

    public static <B extends ByteBuf, T> StreamCodec<B, TypedEntityData<T>> streamCodec(StreamCodec<B, T> streamCodec) {
        return StreamCodec.composite(streamCodec, TypedEntityData::type, ByteBufCodecs.COMPOUND_TAG, TypedEntityData::tag, TypedEntityData::new);
    }

    TypedEntityData(IdType object, CompoundTag compoundTag) {
        this.type = object;
        this.tag = TypedEntityData.stripId(compoundTag);
    }

    public static <T> TypedEntityData<T> of(T object, CompoundTag compoundTag) {
        return new TypedEntityData<T>(object, compoundTag);
    }

    private static CompoundTag stripId(CompoundTag compoundTag) {
        if (compoundTag.contains(TYPE_TAG)) {
            CompoundTag compoundTag2 = compoundTag.copy();
            compoundTag2.remove(TYPE_TAG);
            return compoundTag2;
        }
        return compoundTag;
    }

    public IdType type() {
        return this.type;
    }

    public boolean contains(String string) {
        return this.tag.contains(string);
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof TypedEntityData) {
            TypedEntityData typedEntityData = (TypedEntityData)object;
            return this.type == typedEntityData.type && this.tag.equals(typedEntityData.tag);
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.type.hashCode() + this.tag.hashCode();
    }

    public String toString() {
        return String.valueOf(this.type) + " " + String.valueOf(this.tag);
    }

    public void loadInto(Entity entity) {
        try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);){
            TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
            entity.saveWithoutId(tagValueOutput);
            CompoundTag compoundTag = tagValueOutput.buildResult();
            UUID uUID = entity.getUUID();
            compoundTag.merge(this.getUnsafe());
            entity.load(TagValueInput.create((ProblemReporter)scopedCollector, (HolderLookup.Provider)entity.registryAccess(), compoundTag));
            entity.setUUID(uUID);
        }
    }

    /*
     * Exception decompiling
     */
    public boolean loadInto(BlockEntity blockEntity, HolderLookup.Provider provider) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Tried to end blocks [5[CATCHBLOCK]], but top level block is 2[TRYBLOCK]
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.processEndingBlocks(Op04StructuredStatement.java:435)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:484)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private CompoundTag tag() {
        return this.tag;
    }

    @Deprecated
    public CompoundTag getUnsafe() {
        return this.tag;
    }

    public CompoundTag copyTagWithoutId() {
        return this.tag.copy();
    }

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        if (this.type.getClass() == EntityType.class) {
            EntityType entityType = (EntityType)this.type;
            if (tooltipContext.isPeaceful() && !entityType.isAllowedInPeaceful()) {
                consumer.accept(Component.translatable("item.spawn_egg.peaceful").withStyle(ChatFormatting.RED));
            }
        }
    }

    private static /* synthetic */ String method_72542() {
        return "(rollback)";
    }
}

