package com.lbdevz.lbyangin.managers;

import com.lbdevz.lbyangin.LBYangin;
import com.lbdevz.lbyangin.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EventManager {

    private final LBYangin plugin;
    private boolean eventActive = false;
    private final List<Ghast> activeGhasts = new ArrayList<>();
    private final Random random = new Random();

    public EventManager(LBYangin plugin) {
        this.plugin = plugin;
    }

    // Otomatik/Parametresiz tetikleme: Doğrudan Warp lokasyonunu kullanır
    public void startEvent() {
        startEvent(getWarpLocation());
    }

    // Asıl Etkinlik Başlatma Metodu
    public void startEvent(Location location) {
        if (eventActive) return;

        // Lokasyon geçersizse veya verilmediyse Warp lokasyonunu al
        if (location == null || location.getWorld() == null) {
            location = getWarpLocation();
            if (location == null || location.getWorld() == null) {
                plugin.getLogger().severe("Yangın etkinliği başlatılamadı: Warp konumu veya dünya geçersiz!");
                return;
            }
        }

        eventActive = true;
        activeGhasts.clear();

        int amount = plugin.getConfig().getInt("settings.ghast-amount", 4);
        double health = plugin.getConfig().getDouble("settings.ghast-health", 100.0);
        boolean glowing = plugin.getConfig().getBoolean("settings.ghast-glowing", true);

        for (int i = 0; i < amount; i++) {
            double offsetX = (random.nextDouble() - 0.5) * 10;
            double offsetZ = (random.nextDouble() - 0.5) * 10;
            Location spawnLoc = location.clone().add(offsetX, 2, offsetZ);

            Ghast ghast = (Ghast) location.getWorld().spawnEntity(spawnLoc, EntityType.GHAST);
            ghast.setMaxHealth(health);
            ghast.setHealth(health);
            ghast.setGlowing(glowing);
            updateGhastNameTag(ghast);

            activeGhasts.add(ghast);
        }

        String startMsg = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ") + "&eYangın etkinliği başladı!";
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMsg));

        // Discord Webhook Başlangıç Bildirimi
        if (plugin.getConfig().getBoolean("discord.enabled", false)) {
            DiscordWebhook.sendStartNotification(plugin);
        }
    }

    public void stopEvent() {
        if (!eventActive) return;

        for (Ghast ghast : new ArrayList<>(activeGhasts)) {
            if (ghast != null && !ghast.isDead()) {
                ghast.remove();
            }
        }
        activeGhasts.clear();
        eventActive = false;

        String endMsg = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ") + plugin.getConfig().getString("messages.event-end", "&eYangın etkinliği sona erdi!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMsg));

        // Discord Webhook Bitiş Bildirimi
        if (plugin.getConfig().getBoolean("discord.enabled", false)) {
            DiscordWebhook.sendEndNotification(plugin);
        }
    }

    public boolean isGhastFromEvent(Ghast ghast) {
        return activeGhasts.contains(ghast);
    }

    public void handleGhastDeath(Ghast ghast) {
        activeGhasts.remove(ghast);
        if (activeGhasts.isEmpty() && eventActive) {
            stopEvent();
        }
    }

    public void updateGhastNameTag(Ghast ghast) {
        if (ghast == null) return;
        int currentHealth = (int) Math.max(0, ghast.getHealth());
        int maxHealth = (int) ghast.getMaxHealth();
        ghast.setCustomName(ChatColor.translateAlternateColorCodes('&', "&c&lAlev Ghast'ı &7[" + currentHealth + "/" + maxHealth + "]"));
        ghast.setCustomNameVisible(true);
    }

    public void trackBlockChange(Block block) {
        // Blok takip mantığı
    }

    public Location getWarpLocation() {
        String worldName = plugin.getConfig().getString("warp-location.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = plugin.getConfig().getDouble("warp-location.x");
        double y = plugin.getConfig().getDouble("warp-location.y");
        double z = plugin.getConfig().getDouble("warp-location.z");
        float yaw = (float) plugin.getConfig().getDouble("warp-location.yaw");
        float pitch = (float) plugin.getConfig().getDouble("warp-location.pitch");

        return new Location(world, x, y, z, yaw, pitch);
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public List<Ghast> getActiveGhasts() {
        return activeGhasts;
    }
}