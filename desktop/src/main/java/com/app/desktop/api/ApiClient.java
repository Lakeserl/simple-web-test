package com.app.desktop.api;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for communicating with the Spring Boot REST API.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api/v1";
    private final HttpClient client;
    private final Gson gson;

    public ApiClient() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                        (json, type, ctx) -> LocalDateTime.parse(json.getAsString(),
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                        (src, type, ctx) -> new JsonPrimitive(src.format(
                                DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .create();
    }

    // ─── Products ──────────────────────────────────────────────

    public List<Map<String, Object>> getProducts() throws IOException, InterruptedException {
        return getList("/products");
    }

    public List<Map<String, Object>> searchProducts(String query) throws IOException, InterruptedException {
        return getList("/products?search=" + query);
    }

    public Map<String, Object> getProduct(long id) throws IOException, InterruptedException {
        return getMap("/products/" + id);
    }

    public Map<String, Object> createProduct(Map<String, Object> data) throws IOException, InterruptedException {
        return post("/products", data);
    }

    public Map<String, Object> updateProduct(long id, Map<String, Object> data) throws IOException, InterruptedException {
        return put("/products/" + id, data);
    }

    public void deleteProduct(long id) throws IOException, InterruptedException {
        delete("/products/" + id);
    }

    // ─── Orders ────────────────────────────────────────────────

    public List<Map<String, Object>> getOrders() throws IOException, InterruptedException {
        return getList("/orders");
    }

    public Map<String, Object> getOrder(long id) throws IOException, InterruptedException {
        return getMap("/orders/" + id);
    }

    public Map<String, Object> createOrder(Map<String, Object> data) throws IOException, InterruptedException {
        return post("/orders", data);
    }

    public Map<String, Object> updateOrderStatus(long id, String status) throws IOException, InterruptedException {
        return patch("/orders/" + id + "/status", Map.of("status", status));
    }

    // ─── HTTP helpers ──────────────────────────────────────────

    private List<Map<String, Object>> getList(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .header("Accept", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return gson.fromJson(response.body(), new TypeToken<List<Map<String, Object>>>() {}.getType());
    }

    private Map<String, Object> getMap(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .header("Accept", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return gson.fromJson(response.body(), new TypeToken<Map<String, Object>>() {}.getType());
    }

    private Map<String, Object> post(String path, Object body) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return gson.fromJson(response.body(), new TypeToken<Map<String, Object>>() {}.getType());
    }

    private Map<String, Object> put(String path, Object body) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return gson.fromJson(response.body(), new TypeToken<Map<String, Object>>() {}.getType());
    }

    private Map<String, Object> patch(String path, Object body) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
        return gson.fromJson(response.body(), new TypeToken<Map<String, Object>>() {}.getType());
    }

    private void delete(String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        checkStatus(response);
    }

    private void checkStatus(HttpResponse<String> response) throws IOException {
        if (response.statusCode() >= 400) {
            throw new IOException("API Error " + response.statusCode() + ": " + response.body());
        }
    }
}
