package com.lbdevz.lbyangin.managers;

import com.lbdevz.lbyangin.LBYangin;
import com.lbdevz.lbyangin.utils.DiscordWebhook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EventManager {

    private final LBYangin plugin;
    private boolean eventActive = false;
    private final List<Ghast> activeGhasts = new ArrayList<>();
    // Etkinliğe katılan (hasar veren) oyuncuları tutan liste
    private final Set<Player> participants = new HashSet<>();
    private final Random random = new Random();

    public EventManager(LBYangin plugin) {
        this.plugin = plugin;
    }

    public void startEvent() {
        startEvent(getWarpLocation());
    }

    public void startEvent(Location location) {
        if (eventActive) return;

        if (location == null || location.getWorld() == null) {
            location = getWarpLocation();
            if (location == null || location.getWorld() == null) {
                plugin.getLogger().severe("Yangın etkinliği başlatılamadı: Warp konumu veya dünya geçersiz!");
                return;
            }
        }

        eventActive = true;
        activeGhasts.clear();
        participants.clear(); // Katılımcı listesini sıfırla

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

        if (plugin.getConfig().getBoolean("discord.enabled", false)) {
            DiscordWebhook.sendStartNotification(plugin);
        }
    }

    public void stopEvent() {
        if (!eventActive) return;

        // Kalan Ghast'ları temizle
        for (Ghast ghast : new ArrayList<>(activeGhasts)) {
            if (ghast != null && !ghast.isDead()) {
                ghast.remove();
            }
        }
        activeGhasts.clear();

        // ETKİNLİĞE KATILAN HERKESE ÖDÜL VER
        giveRewardsToAll();

        eventActive = false;

        String endMsg = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ") + plugin.getConfig().getString("messages.event-end", "&eYangın etkinliği sona erdi!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', endMsg));

        if (plugin.getConfig().getBoolean("discord.enabled", false)) {
            DiscordWebhook.sendEndNotification(plugin);
        }
    }

    private void giveRewardsToAll() {
        for (Player player : participants) {
            if (player != null && player.isOnline()) {
                player.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a&lÖDÜL! &eYangın etkinliğine katıldığın için 1x Zümrüt kazandın!"));
            }
        }
        participants.clear();
    }

    // Ghast'a vurulduğunda vurana kaydeder
    public void addParticipant(Player player) {
        if (eventActive && player != null) {
            participants.add(player);
        }
    }

    public boolean isGhastFromEvent(Ghast ghast) {
        return activeGhasts.contains(ghast);
    }

    public void handleGhastDeath(Ghast ghast) {
        activeGhasts.remove(ghast);

        // Son vuran oyuncuyu da katılımcılara ekle
        if (ghast.getKiller() != null) {
            addParticipant(ghast.getKiller());
        }

        // Tüm Ghast'lar öldüyse etkinliği bitir (Bitişte herkese ödül dağıtılacak)
        if (activeGhasts.isEmpty() && eventActive) {
            stopEvent();
        }
    }

    public void updateGhastNameTag(Ghast ghast) {
        if (ghast == null) return;

        double currentHealth = Math.max(0, ghast.getHealth());
        double maxHealth = ghast.getMaxHealth();
        double healthPercent = currentHealth / maxHealth;

        // Can barı uzunluğu (15 blokluk geniş gösterge)
        int totalBars = 15;
        int filledBars = (int) Math.round(healthPercent * totalBars);

        // Orana göre renk geçişi
        String healthColor;
        if (healthPercent > 0.5) {
            healthColor = "&a"; // %50 üzeri Yeşil
        } else if (healthPercent > 0.25) {
            healthColor = "&e"; // %25 - %50 arası Sarı
        } else {
            healthColor = "&c"; // %25 altı Kırmızı
        }

        StringBuilder barBuilder = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                barBuilder.append("█");
            } else {
                barBuilder.append("&7░");
            }
        }

        // Tag Yapısı: Alev Ghast'ı | [██████████░░░░░] (80/100)
        String tag = "&c&lAlev Ghast'ı &8| " + healthColor + barBuilder.toString() + " &f(" + (int) currentHealth + "/" + (int) maxHealth + ")";

        ghast.setCustomName(ChatColor.translateAlternateColorCodes('&', tag));
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