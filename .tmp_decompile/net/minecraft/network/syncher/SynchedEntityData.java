/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  io.netty.handler.codec.DecoderException
 *  io.netty.handler.codec.EncoderException
 *  java.lang.Record
 *  java.lang.runtime.ObjectMethods
 *  org.apache.commons.lang3.ObjectUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SyncedDataHolder;
import net.minecraft.util.ClassTreeIdRegistry;
import org.apache.commons.lang3.ObjectUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class SynchedEntityData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_ID_VALUE = 254;
    static final ClassTreeIdRegistry ID_REGISTRY = new ClassTreeIdRegistry();
    private final SyncedDataHolder entity;
    private final DataItem<?>[] itemsById;
    private boolean isDirty;

    SynchedEntityData(SyncedDataHolder syncedDataHolder, DataItem<?>[] dataItems) {
        this.entity = syncedDataHolder;
        this.itemsById = dataItems;
    }

    public static <T> EntityDataAccessor<T> defineId(Class<? extends SyncedDataHolder> class_, EntityDataSerializer<T> entityDataSerializer) {
        int i;
        if (LOGGER.isDebugEnabled()) {
            try {
                Class<?> class2 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!class2.equals(class_)) {
                    LOGGER.debug("defineId called for: {} from {}", new Object[]{class_, class2, new RuntimeException()});
                }
            }
            catch (ClassNotFoundException class2) {
                // empty catch block
            }
        }
        if ((i = ID_REGISTRY.define(class_)) > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is 254)");
        }
        return entityDataSerializer.createAccessor(i);
    }

    private <T> DataItem<T> getItem(EntityDataAccessor<T> entityDataAccessor) {
        return this.itemsById[entityDataAccessor.id()];
    }

    public <T> T get(EntityDataAccessor<T> entityDataAccessor) {
        return this.getItem(entityDataAccessor).getValue();
    }

    public <T> void set(EntityDataAccessor<T> entityDataAccessor, T object) {
        this.set(entityDataAccessor, object, false);
    }

    public <T> void set(EntityDataAccessor<T> entityDataAccessor, T object, boolean bl) {
        DataItem<T> dataItem = this.getItem(entityDataAccessor);
        if (bl || ObjectUtils.notEqual(object, dataItem.getValue())) {
            dataItem.setValue(object);
            this.entity.onSyncedDataUpdated(entityDataAccessor);
            dataItem.setDirty(true);
            this.isDirty = true;
        }
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public @Nullable List<DataValue<?>> packDirty() {
        if (!this.isDirty) {
            return null;
        }
        this.isDirty = false;
        ArrayList list = new ArrayList();
        for (DataItem<?> dataItem : this.itemsById) {
            if (!dataItem.isDirty()) continue;
            dataItem.setDirty(false);
            list.add(dataItem.value());
        }
        return list;
    }

    public @Nullable List<DataValue<?>> getNonDefaultValues() {
        ArrayList list = null;
        for (DataItem<?> dataItem : this.itemsById) {
            if (dataItem.isSetToDefault()) continue;
            if (list == null) {
                list = new ArrayList();
            }
            list.add(dataItem.value());
        }
        return list;
    }

    public void assignValues(List<DataValue<?>> list) {
        for (DataValue<?> dataValue : list) {
            DataItem<?> dataItem = this.itemsById[dataValue.id];
            this.assignValue(dataItem, dataValue);
            this.entity.onSyncedDataUpdated(dataItem.getAccessor());
        }
        this.entity.onSyncedDataUpdated(list);
    }

    private <T> void assignValue(DataItem<T> dataItem, DataValue<?> dataValue) {
        if (!Objects.equals(dataValue.serializer(), dataItem.accessor.serializer())) {
            throw new IllegalStateException(String.format(Locale.ROOT, "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)", dataItem.accessor.id(), this.entity, dataItem.value, dataItem.value.getClass(), dataValue.value, dataValue.value.getClass()));
        }
        dataItem.setValue(dataValue.value);
    }

    public static class DataItem<T> {
        final EntityDataAccessor<T> accessor;
        T value;
        private final T initialValue;
        private boolean dirty;

        public DataItem(EntityDataAccessor<T> entityDataAccessor, T object) {
            this.accessor = entityDataAccessor;
            this.initialValue = object;
            this.value = object;
        }

        public EntityDataAccessor<T> getAccessor() {
            return this.accessor;
        }

        public void setValue(T object) {
            this.value = object;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean bl) {
            this.dirty = bl;
        }

        public boolean isSetToDefault() {
            return this.initialValue.equals(this.value);
        }

        public DataValue<T> value() {
            return DataValue.create(this.accessor, this.value);
        }
    }

    public static final class DataValue<T>
    extends Record {
        final int id;
        private final EntityDataSerializer<T> serializer;
        final T value;

        public DataValue(int i, EntityDataSerializer<T> entityDataSerializer, T object) {
            this.id = i;
            this.serializer = entityDataSerializer;
            this.value = object;
        }

        public static <T> DataValue<T> create(EntityDataAccessor<T> entityDataAccessor, T object) {
            EntityDataSerializer<T> entityDataSerializer = entityDataAccessor.serializer();
            return new DataValue<T>(entityDataAccessor.id(), entityDataSerializer, entityDataSerializer.copy(object));
        }

        public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            int i = EntityDataSerializers.getSerializedId(this.serializer);
            if (i < 0) {
                throw new EncoderException("Unknown serializer type " + String.valueOf(this.serializer));
            }
            registryFriendlyByteBuf.writeByte(this.id);
            registryFriendlyByteBuf.writeVarInt(i);
            this.serializer.codec().encode(registryFriendlyByteBuf, this.value);
        }

        public static DataValue<?> read(RegistryFriendlyByteBuf registryFriendlyByteBuf, int i) {
            int j = registryFriendlyByteBuf.readVarInt();
            EntityDataSerializer<?> entityDataSerializer = EntityDataSerializers.getSerializer(j);
            if (entityDataSerializer == null) {
                throw new DecoderException("Unknown serializer type " + j);
            }
            return DataValue.read(registryFriendlyByteBuf, i, entityDataSerializer);
        }

        private static <T> DataValue<T> read(RegistryFriendlyByteBuf registryFriendlyByteBuf, int i, EntityDataSerializer<T> entityDataSerializer) {
            return new DataValue<T>(i, entityDataSerializer, entityDataSerializer.codec().decode(registryFriendlyByteBuf));
        }

        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this);
        }

        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this);
        }

        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DataValue.class, "id;serializer;value", "id", "serializer", "value"}, this, object);
        }

        public int id() {
            return this.id;
        }

        public EntityDataSerializer<T> serializer() {
            return this.serializer;
        }

        public T value() {
            return this.value;
        }
    }

    public static class Builder {
        private final SyncedDataHolder entity;
        private final @Nullable DataItem<?>[] itemsById;

        public Builder(SyncedDataHolder syncedDataHolder) {
            this.entity = syncedDataHolder;
            this.itemsById = new DataItem[ID_REGISTRY.getCount(syncedDataHolder.getClass())];
        }

        public <T> Builder define(EntityDataAccessor<T> entityDataAccessor, T object) {
            int i = entityDataAccessor.id();
            if (i > this.itemsById.length) {
                throw new IllegalArgumentException("Data value id is too big with " + i + "! (Max is " + this.itemsById.length + ")");
            }
            if (this.itemsById[i] != null) {
                throw new IllegalArgumentException("Duplicate id value for " + i + "!");
            }
            if (EntityDataSerializers.getSerializedId(entityDataAccessor.serializer()) < 0) {
                throw new IllegalArgumentException("Unregistered serializer " + String.valueOf(entityDataAccessor.serializer()) + " for " + i + "!");
            }
            this.itemsById[entityDataAccessor.id()] = new DataItem<T>(entityDataAccessor, object);
            return this;
        }

        public SynchedEntityData build() {
            for (int i = 0; i < this.itemsById.length; ++i) {
                if (this.itemsById[i] != null) continue;
                throw new IllegalStateException("Entity " + String.valueOf(this.entity.getClass()) + " has not defined synched data value " + i);
            }
            return new SynchedEntityData(this.entity, this.itemsById);
        }
    }
}

