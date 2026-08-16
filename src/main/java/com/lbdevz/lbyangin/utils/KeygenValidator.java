package com.lbdevz.lbyangin.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;

public class KeygenValidator {

    private final JavaPlugin plugin;
    private final String ACCOUNT_ID = "d0ec9aa8-d84f-4574-be8d-323a36ec647c";

    public KeygenValidator(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void validate() {
        String licenseKey = plugin.getConfig().getString("license-key", "").trim();

        if (licenseKey.isEmpty()) {
            shutdown("config.yml icinde 'license-key' bos birakilmis!");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String hwid = generateHWID();
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

                // 1. ADIM: Lisansi ve HWID'yi kontrol et
                HttpResponse<String> response = validateKey(client, licenseKey, hwid);

                if (response.statusCode() == 200) {
                    if (response.body().contains("\"valid\":true")) {
                        plugin.getLogger().info("[LB-Yangin] Lisans ve HWID basariyla dogrulandi!");
                        return;
                    }

                    // 2. ADIM: Makine henüz kayitli degilse (NO_MACHINE hatasi) otomatik kaydet
                    if (response.body().contains("\"code\":\"NO_MACHINE\"")) {
                        plugin.getLogger().info("[LB-Yangin] Ilk kullanım tespit edildi. Makine Keygen'e otomatik kaydediliyor...");

                        String licenseId = extractLicenseId(response.body());

                        if (licenseId != null && registerMachine(client, licenseKey, licenseId, hwid)) {
                            plugin.getLogger().info("[LB-Yangin] Makine kaydi basarili! Eklenti aktiflestirildi.");
                            return;
                        } else {
                            shutdown("Makine kaydi basarisiz! Lisans baska bir sunucuda kullaniliyor olabilir.");
                            return;
                        }
                    }
                }

                shutdown("Gecersiz lisans, suresi dolmus veya baska bir makinede kullaniliyor!");

            } catch (Exception e) {
                plugin.getLogger().severe("[LB-Yangin] Baglanti Hatasi: " + e.getMessage());
                shutdown("Lisans sunucusuna baglanilamadi!");
            }
        });
    }

    private HttpResponse<String> validateKey(HttpClient client, String licenseKey, String hwid) throws Exception {
        String endpoint = String.format("https://api.keygen.sh/v1/accounts/%s/licenses/actions/validate-key", ACCOUNT_ID);
        String jsonPayload = String.format("{\"meta\":{\"key\":\"%s\",\"scope\":{\"fingerprint\":\"%s\"}}}", licenseKey, hwid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/vnd.api+json")
                .header("Accept", "application/vnd.api+json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(5))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean registerMachine(HttpClient client, String licenseKey, String licenseId, String hwid) {
        try {
            String endpoint = String.format("https://api.keygen.sh/v1/accounts/%s/machines", ACCOUNT_ID);
            String jsonPayload = String.format(
                    "{\"data\":{\"type\":\"machines\",\"attributes\":{\"fingerprint\":\"%s\"},\"relationships\":{\"license\":{\"data\":{\"type\":\"licenses\",\"id\":\"%s\"}}}}}",
                    hwid, licenseId
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + licenseKey)
                    .header("Content-Type", "application/vnd.api+json")
                    .header("Accept", "application/vnd.api+json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .timeout(Duration.ofSeconds(5))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 201; // 201 Created (Kayıt Başarılı)
        } catch (Exception e) {
            plugin.getLogger().severe("[LB-Yangin] Otomatik makine kaydi hatasi: " + e.getMessage());
            return false;
        }
    }

    private String extractLicenseId(String jsonResponse) {
        try {
            int idIndex = jsonResponse.indexOf("\"id\":\"");
            if (idIndex != -1) {
                int start = idIndex + 6;
                int end = jsonResponse.indexOf("\"", start);
                return jsonResponse.substring(start, end);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String generateHWID() {
        try {
            String raw = System.getenv("COMPUTERNAME") + 
                         System.getProperty("user.name") + 
                         System.getProperty("os.name") + 
                         Runtime.getRuntime().availableProcessors();
                         
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().substring(0, 32);
        } catch (Exception e) {
            return "default-server-hwid";
        }
    }

    private void shutdown(String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().severe("========================================");
            plugin.getLogger().severe("[LB-Yangin] LISANS HATASI: " + reason);
            plugin.getLogger().severe("[LB-Yangin] Eklenti devredisi birakiliyor...");
            plugin.getLogger().severe("========================================");
            
            Bukkit.getPluginManager().disablePlugin(plugin);
        });
    }
}