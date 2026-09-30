package site.uptify.bridge.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import site.uptify.bridge.UptifyBridge;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ApiClient {

    private final UptifyBridge plugin;
    private final HttpClient httpClient;
    private final Gson gson;

    public ApiClient(UptifyBridge plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    private String getBaseUrl() {
        String url = plugin.getConfig().getString("api-base-url", "https://uptify.site/api");
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * Request a one-time link session token for a player.
     */
    public CompletableFuture<ApiResponse> createLinkSession(UUID playerUuid, String playerName) {
        String endpoint = getBaseUrl() + "/integrations/minecraft/link-session";

        JsonObject body = new JsonObject();
        body.addProperty("playerUuid", playerUuid.toString());
        body.addProperty("playerName", playerName);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .header("User-Agent", "UptifyBridge/" + plugin.getDescription().getVersion())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> new ApiResponse(res.statusCode(), parseJson(res.body())));
    }

    /**
     * Query the status of an active link session token.
     */
    public CompletableFuture<ApiResponse> checkLinkStatus(String token) {
        String endpoint = getBaseUrl() + "/integrations/minecraft/link-status?token=" + token;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "UptifyBridge/" + plugin.getDescription().getVersion())
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> new ApiResponse(res.statusCode(), parseJson(res.body())));
    }

    /**
     * Submit user feedback or suggestion to the Uptify board.
     */
    public CompletableFuture<ApiResponse> submitFeedback(UUID playerUuid, String playerName, String projectId, String content) {
        String endpoint = getBaseUrl() + "/integrations/minecraft/feedback";

        JsonObject body = new JsonObject();
        body.addProperty("playerUuid", playerUuid.toString());
        body.addProperty("playerName", playerName);
        body.addProperty("projectId", projectId);
        body.addProperty("content", content);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/json")
                .header("User-Agent", "UptifyBridge/" + plugin.getDescription().getVersion())
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> new ApiResponse(res.statusCode(), parseJson(res.body())));
    }

    /**
     * Fetch live status metrics and active incidents for the configured status project.
     */
    public CompletableFuture<ApiResponse> getStatus(String projectId) {
        String endpoint = getBaseUrl() + "/integrations/minecraft/status?projectId=" + projectId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(10))
                .header("User-Agent", "UptifyBridge/" + plugin.getDescription().getVersion())
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> new ApiResponse(res.statusCode(), parseJson(res.body())));
    }

    private JsonObject parseJson(String responseBody) {
        try {
            if (responseBody == null || responseBody.trim().isEmpty()) {
                return new JsonObject();
            }
            return JsonParser.parseString(responseBody).getAsJsonObject();
        } catch (Exception e) {
            JsonObject err = new JsonObject();
            err.addProperty("error", "Invalid server response: " + responseBody);
            return err;
        }
    }

    public static class ApiResponse {
        private final int statusCode;
        private final JsonObject data;

        public ApiResponse(int statusCode, JsonObject data) {
            this.statusCode = statusCode;
            this.data = data;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public JsonObject getData() {
            return data;
        }

        public boolean isSuccess() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
