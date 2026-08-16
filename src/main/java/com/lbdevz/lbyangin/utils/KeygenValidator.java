package com.lbdevz.lbyangin.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class KeygenValidator {

    private final JavaPlugin plugin;
    // Keygen.sh Account ID
    private final String ACCOUNT_ID = "d0ec9aa8-d84f-4574-be8d-323a36ec647c";

    public KeygenValidator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void validate() {
        String licenseKey = plugin.getConfig().getString("license-key", "").trim();

        if (licenseKey.isEmpty()) {
            shutdown("config.yml icinde 'license-key' bulunamadi!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String endpoint = String.format("https://api.keygen.sh/v1/accounts/%s/licenses/actions/validate-key", ACCOUNT_ID);
                String jsonPayload = String.format("{\"meta\":{\"key\":\"%s\"}}", licenseKey);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/vnd.api+json")
                        .header("Accept", "application/vnd.api+json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body().contains("\"valid\":true")) {
                    plugin.getLogger().info("[LB-Yangin] Lisans basariyla dogrulandi!");
                } else {
                    shutdown("Gecersiz veya suresi dolmus lisans anahtari!");
                }

            } catch (Exception e) {
                shutdown("Lisans sunucusuna baglanilamadi!");
            }
        });
    }

    private void shutdown(String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().severe("========================================");
            plugin.getLogger().severe("[LB-Yangin] LISANS HATASI: " + reason);
            plugin.getLogger().severe("[LB-Yangin] Sunucu kapatiliyor...");
            plugin.getLogger().severe("========================================");
            System.exit(0);
        });
    }
}