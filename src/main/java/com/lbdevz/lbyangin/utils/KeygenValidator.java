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
                String endpoint = String.format("https://api.keygen.sh/v1/accounts/%s/licenses/actions/validate-key", ACCOUNT_ID);
                
                // Keygen'e hem lisans anahtarini hem de sunucunun donanim kimligini (fingerprint) gonderiyoruz
                String jsonPayload = String.format("{\"meta\":{\"key\":\"%s\",\"scope\":{\"fingerprint\":\"%s\"}}}", licenseKey, hwid);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/vnd.api+json")
                        .header("Accept", "application/vnd.api+json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .timeout(Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200 && response.body().contains("\"valid\":true")) {
                    plugin.getLogger().info("[LB-Yangin] Lisans ve Donanim Kimligi (HWID) basariyla dogrulandi!");
                } else {
                    plugin.getLogger().severe("[LB-Yangin] Keygen HTTP Kodu: " + response.statusCode());
                    plugin.getLogger().severe("[LB-Yangin] Keygen Cevabi: " + response.body());
                    shutdown("Gecersiz lisans, suresi dolmus veya baska bir makinede kullaniliyor!");
                }

            } catch (Exception e) {
                plugin.getLogger().severe("[LB-Yangin] Baglanti Hatasi Detayi: " + e.getMessage());
                shutdown("Lisans sunucusuna baglanilamadi!");
            }
        });
    }

    /**
     * Sunucunun VDS/Makine ozelliklerinden benzersiz bir HWID (Fingerprint) uretir.
     */
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
            return hexString.toString().substring(0, 32); // 32 karakterlik HWID
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