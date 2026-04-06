/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.io.Files
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.players;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import net.minecraft.server.notifications.NotificationService;
import net.minecraft.server.players.StoredUserEntry;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class StoredUserList<K, V extends StoredUserEntry<K>> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final File file;
    private final Map<String, V> map = Maps.newHashMap();
    protected final NotificationService notificationService;

    public StoredUserList(File file, NotificationService notificationService) {
        this.file = file;
        this.notificationService = notificationService;
    }

    public File getFile() {
        return this.file;
    }

    public boolean add(V storedUserEntry) {
        String string = this.getKeyForUser(((StoredUserEntry)storedUserEntry).getUser());
        StoredUserEntry storedUserEntry2 = (StoredUserEntry)this.map.get(string);
        if (storedUserEntry.equals(storedUserEntry2)) {
            return false;
        }
        this.map.put(string, storedUserEntry);
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after adding a user.", (Throwable)iOException);
        }
        return true;
    }

    public @Nullable V get(K object) {
        this.removeExpired();
        return (V)((StoredUserEntry)this.map.get(this.getKeyForUser(object)));
    }

    public boolean remove(K object) {
        StoredUserEntry storedUserEntry = (StoredUserEntry)this.map.remove(this.getKeyForUser(object));
        if (storedUserEntry == null) {
            return false;
        }
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after removing a user.", (Throwable)iOException);
        }
        return true;
    }

    public boolean remove(StoredUserEntry<K> storedUserEntry) {
        return this.remove(Objects.requireNonNull(storedUserEntry.getUser()));
    }

    public void clear() {
        this.map.clear();
        try {
            this.save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Could not save the list after removing a user.", (Throwable)iOException);
        }
    }

    public String[] getUserList() {
        return this.map.keySet().toArray(new String[0]);
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    protected String getKeyForUser(K object) {
        return object.toString();
    }

    protected boolean contains(K object) {
        return this.map.containsKey(this.getKeyForUser(object));
    }

    private void removeExpired() {
        ArrayList list = Lists.newArrayList();
        for (StoredUserEntry storedUserEntry : this.map.values()) {
            if (!storedUserEntry.hasExpired()) continue;
            list.add(storedUserEntry.getUser());
        }
        for (Object object : list) {
            this.map.remove(this.getKeyForUser(object));
        }
    }

    protected abstract StoredUserEntry<K> createEntry(JsonObject var1);

    public Collection<V> getEntries() {
        return this.map.values();
    }

    public void save() throws IOException {
        JsonArray jsonArray = new JsonArray();
        this.map.values().stream().map(storedUserEntry -> Util.make(new JsonObject(), storedUserEntry::serialize)).forEach(arg_0 -> ((JsonArray)jsonArray).add(arg_0));
        try (BufferedWriter bufferedWriter = Files.newWriter((File)this.file, (Charset)StandardCharsets.UTF_8);){
            GSON.toJson((JsonElement)jsonArray, GSON.newJsonWriter((Writer)bufferedWriter));
        }
    }

    public void load() throws IOException {
        if (!this.file.exists()) {
            return;
        }
        try (BufferedReader bufferedReader = Files.newReader((File)this.file, (Charset)StandardCharsets.UTF_8);){
            this.map.clear();
            JsonArray jsonArray = (JsonArray)GSON.fromJson((Reader)bufferedReader, JsonArray.class);
            if (jsonArray == null) {
                return;
            }
            for (JsonElement jsonElement : jsonArray) {
                JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
                StoredUserEntry<K> storedUserEntry = this.createEntry(jsonObject);
                if (storedUserEntry.getUser() == null) continue;
                this.map.put(this.getKeyForUser(storedUserEntry.getUser()), storedUserEntry);
            }
        }
    }
}

