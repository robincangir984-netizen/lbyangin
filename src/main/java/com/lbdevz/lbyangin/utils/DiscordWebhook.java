package com.lbdevz.lbyangin.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    private final String url;

    public DiscordWebhook(String url) {
        this.url = url;
    }

    public void sendEmbed(String title, String description, int color) {
        if (url == null || url.isEmpty() || url.equalsIgnoreCase("WEBHOOK_URL_BURAYA")) {
            return;
        }

        String jsonPayload = "{"
                + "\"embeds\": [{"
                + "\"title\": \"" + escapeJson(title) + "\","
                + "\"description\": \"" + escapeJson(description) + "\","
                + "\"color\": " + color
                + "}]"
                + "}";

        new Thread(() -> {
            try {
                URL webhookUrl = new URL(url);
                HttpsURLConnection connection = (HttpsURLConnection) webhookUrl.openConnection();
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("User-Agent", "LBYangin-Plugin");
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");

                try (OutputStream stream = connection.getOutputStream()) {
                    stream.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
                    stream.flush();
                }

                connection.getInputStream().close();
                connection.disconnect();
            } catch (Exception ignored) {
                // Sunucu akışının bozulmaması için hatalar yoksayılır
            }
        }).start();
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\b", "\\b")
                   .replace("\f", "\\f")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}