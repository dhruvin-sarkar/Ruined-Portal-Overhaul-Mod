/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.microsoft.aad.msal4j.ClientCredentialFactory
 *  com.microsoft.aad.msal4j.ClientCredentialParameters
 *  com.microsoft.aad.msal4j.ConfidentialClientApplication
 *  com.microsoft.aad.msal4j.ConfidentialClientApplication$Builder
 *  com.microsoft.aad.msal4j.IAuthenticationResult
 *  com.microsoft.aad.msal4j.IClientCertificate
 *  com.microsoft.aad.msal4j.IClientCredential
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.network;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.ClientCredentialFactory;
import com.microsoft.aad.msal4j.ClientCredentialParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.IClientCertificate;
import com.microsoft.aad.msal4j.IClientCredential;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import net.minecraft.server.network.FilteredText;
import net.minecraft.server.network.ServerTextFilter;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

public class PlayerSafetyServiceTextFilter
extends ServerTextFilter {
    private final ConfidentialClientApplication client;
    private final ClientCredentialParameters clientParameters;
    private final Set<String> fullyFilteredEvents;
    private final int connectionReadTimeoutMs;

    private PlayerSafetyServiceTextFilter(URL uRL, ServerTextFilter.MessageEncoder messageEncoder, ServerTextFilter.IgnoreStrategy ignoreStrategy, ExecutorService executorService, ConfidentialClientApplication confidentialClientApplication, ClientCredentialParameters clientCredentialParameters, Set<String> set, int i) {
        super(uRL, messageEncoder, ignoreStrategy, executorService);
        this.client = confidentialClientApplication;
        this.clientParameters = clientCredentialParameters;
        this.fullyFilteredEvents = set;
        this.connectionReadTimeoutMs = i;
    }

    public static @Nullable ServerTextFilter createTextFilterFromConfig(String string) {
        ConfidentialClientApplication confidentialClientApplication;
        IClientCertificate iClientCertificate;
        URL uRL;
        JsonObject jsonObject = GsonHelper.parse(string);
        URI uRI = URI.create(GsonHelper.getAsString(jsonObject, "apiServer"));
        String string2 = GsonHelper.getAsString(jsonObject, "apiPath");
        String string32 = GsonHelper.getAsString(jsonObject, "scope");
        String string4 = GsonHelper.getAsString(jsonObject, "serverId", "");
        String string5 = GsonHelper.getAsString(jsonObject, "applicationId");
        String string6 = GsonHelper.getAsString(jsonObject, "tenantId");
        String string7 = GsonHelper.getAsString(jsonObject, "roomId", "Java:Chat");
        String string8 = GsonHelper.getAsString(jsonObject, "certificatePath");
        String string9 = GsonHelper.getAsString(jsonObject, "certificatePassword", "");
        int i = GsonHelper.getAsInt(jsonObject, "hashesToDrop", -1);
        int j = GsonHelper.getAsInt(jsonObject, "maxConcurrentRequests", 7);
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "fullyFilteredEvents");
        HashSet<String> set = new HashSet<String>();
        jsonArray.forEach(jsonElement -> set.add(GsonHelper.convertToString(jsonElement, "filteredEvent")));
        int k = GsonHelper.getAsInt(jsonObject, "connectionReadTimeoutMs", 2000);
        try {
            uRL = uRI.resolve(string2).toURL();
        }
        catch (MalformedURLException malformedURLException) {
            throw new RuntimeException(malformedURLException);
        }
        ServerTextFilter.MessageEncoder messageEncoder = (gameProfile, string3) -> {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("userId", gameProfile.id().toString());
            jsonObject.addProperty("userDisplayName", gameProfile.name());
            jsonObject.addProperty("server", string4);
            jsonObject.addProperty("room", string7);
            jsonObject.addProperty("area", "JavaChatRealms");
            jsonObject.addProperty("data", string3);
            jsonObject.addProperty("language", "*");
            return jsonObject;
        };
        ServerTextFilter.IgnoreStrategy ignoreStrategy = ServerTextFilter.IgnoreStrategy.select(i);
        ExecutorService executorService = PlayerSafetyServiceTextFilter.createWorkerPool(j);
        try (InputStream inputStream = Files.newInputStream(Path.of((String)string8, (String[])new String[0]), new OpenOption[0]);){
            iClientCertificate = ClientCredentialFactory.createFromCertificate((InputStream)inputStream, (String)string9);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to open certificate file");
            return null;
        }
        try {
            confidentialClientApplication = ((ConfidentialClientApplication.Builder)((ConfidentialClientApplication.Builder)ConfidentialClientApplication.builder((String)string5, (IClientCredential)iClientCertificate).sendX5c(true).executorService(executorService)).authority(String.format(Locale.ROOT, "https://login.microsoftonline.com/%s/", string6))).build();
        }
        catch (Exception exception2) {
            LOGGER.warn("Failed to create confidential client application");
            return null;
        }
        ClientCredentialParameters clientCredentialParameters = ClientCredentialParameters.builder((Set)Set.of((Object)string32)).build();
        return new PlayerSafetyServiceTextFilter(uRL, messageEncoder, ignoreStrategy, executorService, confidentialClientApplication, clientCredentialParameters, set, k);
    }

    private IAuthenticationResult aquireIAuthenticationResult() {
        return (IAuthenticationResult)this.client.acquireToken(this.clientParameters).join();
    }

    @Override
    protected void setAuthorizationProperty(HttpURLConnection httpURLConnection) {
        IAuthenticationResult iAuthenticationResult = this.aquireIAuthenticationResult();
        httpURLConnection.setRequestProperty("Authorization", "Bearer " + iAuthenticationResult.accessToken());
    }

    @Override
    protected FilteredText filterText(String string, ServerTextFilter.IgnoreStrategy ignoreStrategy, JsonObject jsonObject) {
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "result", null);
        if (jsonObject2 == null) {
            return FilteredText.fullyFiltered(string);
        }
        boolean bl = GsonHelper.getAsBoolean(jsonObject2, "filtered", true);
        if (!bl) {
            return FilteredText.passThrough(string);
        }
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject2, "events", new JsonArray());
        for (JsonElement jsonElement : jsonArray) {
            JsonObject jsonObject3 = jsonElement.getAsJsonObject();
            String string2 = GsonHelper.getAsString(jsonObject3, "id", "");
            if (!this.fullyFilteredEvents.contains(string2)) continue;
            return FilteredText.fullyFiltered(string);
        }
        JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject2, "redactedTextIndex", new JsonArray());
        return new FilteredText(string, this.parseMask(string, jsonArray2, ignoreStrategy));
    }

    @Override
    protected int connectionReadTimeout() {
        return this.connectionReadTimeoutMs;
    }
}

