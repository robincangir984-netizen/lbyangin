package com.lbdevz.lbyangin.utils;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Bukkit;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    public static void sendStartNotification(LBYangin plugin) {
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        String title = plugin.getConfig().getString("discord.start-title", "Yangın Etkinliği Başladı!");
        String description = plugin.getConfig().getString("discord.start-description", "Etkinlik başladı!");
        sendEmbed(plugin, webhookUrl, title, description, 16711680); // Kırmızı renk
    }

    public static void sendEndNotification(LBYangin plugin) {
        String webhookUrl = plugin.getConfig().getString("discord.webhook-url", "");
        String title = plugin.getConfig().getString("discord.end-title", "Yangın Etkinliği Sona Erdi!");
        String description = plugin.getConfig().getString("discord.end-description", "Etkinlik bitti!");
        sendEmbed(plugin, webhookUrl, title, description, 65280); // Yeşil renk
    }

    private static void sendEmbed(LBYangin plugin, String webhookUrl, String title, String description, int color) {
        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.equals("YOUR_WEBHOOK_URL")) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                URL url = new URL(webhookUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                String jsonPayload = "{"
                        + "\"embeds\": [{"
                        + "\"title\": \"" + escapeJson(title) + "\","
                        + "\"description\": \"" + escapeJson(description) + "\","
                        + "\"color\": " + color
                        + "}]"
                        + "}";

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                connection.getResponseCode();
                connection.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Discord Webhook gönderilirken hata oluştu: " + e.getMessage());
            }
        });
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}