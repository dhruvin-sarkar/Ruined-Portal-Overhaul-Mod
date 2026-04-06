/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.internal.Streams
 *  com.google.gson.stream.JsonWriter
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.LegacyTextFilter;
import net.minecraft.server.network.PlayerSafetyServiceTextFilter;
import net.minecraft.server.network.TextFilter;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import net.minecraft.util.thread.ConsecutiveExecutor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class ServerTextFilter
implements AutoCloseable {
    protected static final Logger LOGGER = LogUtils.getLogger();
    private static final AtomicInteger WORKER_COUNT = new AtomicInteger(1);
    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("Chat-Filter-Worker-" + WORKER_COUNT.getAndIncrement());
        return thread;
    };
    private final URL chatEndpoint;
    private final MessageEncoder chatEncoder;
    final IgnoreStrategy chatIgnoreStrategy;
    final ExecutorService workerPool;

    protected static ExecutorService createWorkerPool(int i) {
        return Executors.newFixedThreadPool(i, THREAD_FACTORY);
    }

    protected ServerTextFilter(URL uRL, MessageEncoder messageEncoder, IgnoreStrategy ignoreStrategy, ExecutorService executorService) {
        this.chatIgnoreStrategy = ignoreStrategy;
        this.workerPool = executorService;
        this.chatEndpoint = uRL;
        this.chatEncoder = messageEncoder;
    }

    protected static URL getEndpoint(URI uRI, @Nullable JsonObject jsonObject, String string, String string2) throws MalformedURLException {
        String string3 = ServerTextFilter.getEndpointFromConfig(jsonObject, string, string2);
        return uRI.resolve("/" + string3).toURL();
    }

    protected static String getEndpointFromConfig(@Nullable JsonObject jsonObject, String string, String string2) {
        return jsonObject != null ? GsonHelper.getAsString(jsonObject, string, string2) : string2;
    }

    public static @Nullable ServerTextFilter createFromConfig(DedicatedServerProperties dedicatedServerProperties) {
        String string = dedicatedServerProperties.textFilteringConfig;
        if (StringUtil.isBlank(string)) {
            return null;
        }
        return switch (dedicatedServerProperties.textFilteringVersion) {
            case 0 -> LegacyTextFilter.createTextFilterFromConfig(string);
            case 1 -> PlayerSafetyServiceTextFilter.createTextFilterFromConfig(string);
            default -> {
                LOGGER.warn("Could not create text filter - unsupported text filtering version used");
                yield null;
            }
        };
    }

    protected CompletableFuture<FilteredText> requestMessageProcessing(GameProfile gameProfile, String string, IgnoreStrategy ignoreStrategy, Executor executor) {
        if (string.isEmpty()) {
            return CompletableFuture.completedFuture(FilteredText.EMPTY);
        }
        return CompletableFuture.supplyAsync(() -> {
            JsonObject jsonObject = this.chatEncoder.encode(gameProfile, string);
            try {
                JsonObject jsonObject2 = this.processRequestResponse(jsonObject, this.chatEndpoint);
                return this.filterText(string, ignoreStrategy, jsonObject2);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to validate message '{}'", (Object)string, (Object)exception);
                return FilteredText.fullyFiltered(string);
            }
        }, executor);
    }

    protected abstract FilteredText filterText(String var1, IgnoreStrategy var2, JsonObject var3);

    protected FilterMask parseMask(String string, JsonArray jsonArray, IgnoreStrategy ignoreStrategy) {
        if (jsonArray.isEmpty()) {
            return FilterMask.PASS_THROUGH;
        }
        if (ignoreStrategy.shouldIgnore(string, jsonArray.size())) {
            return FilterMask.FULLY_FILTERED;
        }
        FilterMask filterMask = new FilterMask(string.length());
        for (int i = 0; i < jsonArray.size(); ++i) {
            filterMask.setFiltered(jsonArray.get(i).getAsInt());
        }
        return filterMask;
    }

    @Override
    public void close() {
        this.workerPool.shutdownNow();
    }

    protected void drainStream(InputStream inputStream) throws IOException {
        byte[] bs = new byte[1024];
        while (inputStream.read(bs) != -1) {
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private JsonObject processRequestResponse(JsonObject jsonObject, URL uRL) throws IOException {
        HttpURLConnection httpURLConnection = this.makeRequest(jsonObject, uRL);
        try (InputStream inputStream = httpURLConnection.getInputStream();){
            JsonObject jsonObject2;
            if (httpURLConnection.getResponseCode() == 204) {
                JsonObject jsonObject3 = new JsonObject();
                return jsonObject3;
            }
            try {
                jsonObject2 = LenientJsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).getAsJsonObject();
            }
            catch (Throwable throwable) {
                this.drainStream(inputStream);
                throw throwable;
            }
            this.drainStream(inputStream);
            return jsonObject2;
        }
    }

    protected HttpURLConnection makeRequest(JsonObject jsonObject, URL uRL) throws IOException {
        HttpURLConnection httpURLConnection = this.getURLConnection(uRL);
        this.setAuthorizationProperty(httpURLConnection);
        try (OutputStreamWriter outputStreamWriter = new OutputStreamWriter(httpURLConnection.getOutputStream(), StandardCharsets.UTF_8);
             JsonWriter jsonWriter = new JsonWriter((Writer)outputStreamWriter);){
            Streams.write((JsonElement)jsonObject, (JsonWriter)jsonWriter);
        }
        int i = httpURLConnection.getResponseCode();
        if (i < 200 || i >= 300) {
            throw new RequestFailedException(i + " " + httpURLConnection.getResponseMessage());
        }
        return httpURLConnection;
    }

    protected abstract void setAuthorizationProperty(HttpURLConnection var1);

    protected int connectionReadTimeout() {
        return 2000;
    }

    protected HttpURLConnection getURLConnection(URL uRL) throws IOException {
        HttpURLConnection httpURLConnection = (HttpURLConnection)uRL.openConnection();
        httpURLConnection.setConnectTimeout(15000);
        httpURLConnection.setReadTimeout(this.connectionReadTimeout());
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setDoInput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        httpURLConnection.setRequestProperty("Accept", "application/json");
        httpURLConnection.setRequestProperty("User-Agent", "Minecraft server" + SharedConstants.getCurrentVersion().name());
        return httpURLConnection;
    }

    public TextFilter createContext(GameProfile gameProfile) {
        return new PlayerContext(gameProfile);
    }

    @FunctionalInterface
    public static interface IgnoreStrategy {
        public static final IgnoreStrategy NEVER_IGNORE = (string, i) -> false;
        public static final IgnoreStrategy IGNORE_FULLY_FILTERED = (string, i) -> string.length() == i;

        public static IgnoreStrategy ignoreOverThreshold(int i) {
            return (string, j) -> j >= i;
        }

        public static IgnoreStrategy select(int i) {
            return switch (i) {
                case -1 -> NEVER_IGNORE;
                case 0 -> IGNORE_FULLY_FILTERED;
                default -> IgnoreStrategy.ignoreOverThreshold(i);
            };
        }

        public boolean shouldIgnore(String var1, int var2);
    }

    @FunctionalInterface
    protected static interface MessageEncoder {
        public JsonObject encode(GameProfile var1, String var2);
    }

    protected static class RequestFailedException
    extends RuntimeException {
        protected RequestFailedException(String string) {
            super(string);
        }
    }

    protected class PlayerContext
    implements TextFilter {
        protected final GameProfile profile;
        protected final Executor streamExecutor;

        protected PlayerContext(GameProfile gameProfile) {
            this.profile = gameProfile;
            ConsecutiveExecutor consecutiveExecutor = new ConsecutiveExecutor(ServerTextFilter.this.workerPool, "chat stream for " + gameProfile.name());
            this.streamExecutor = consecutiveExecutor::schedule;
        }

        @Override
        public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
            List list2 = (List)list.stream().map(string -> ServerTextFilter.this.requestMessageProcessing(this.profile, (String)string, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor)).collect(ImmutableList.toImmutableList());
            return Util.sequenceFailFast(list2).exceptionally(throwable -> ImmutableList.of());
        }

        @Override
        public CompletableFuture<FilteredText> processStreamMessage(String string) {
            return ServerTextFilter.this.requestMessageProcessing(this.profile, string, ServerTextFilter.this.chatIgnoreStrategy, this.streamExecutor);
        }
    }
}

