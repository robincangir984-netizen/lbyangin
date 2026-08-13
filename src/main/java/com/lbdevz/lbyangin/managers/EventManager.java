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
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class EventManager {

    private final LBYangin plugin;
    private boolean eventActive = false;
    private final List<Ghast> activeGhasts = new ArrayList<>();
    private final Set<Player> participants = new HashSet<>();
    private final Random random = new Random();
    private BukkitTask shootTask;

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
        participants.clear();

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

        // ETKİNLİK BAŞLADIĞI AN RASTGELE MAGMA BLOĞU FIRLATMA GÖREVİNİ BAŞLAT (Her 1.5 saniyede bir fırlatır)
        startRandomShootingTask();

        String startMsg = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ") + "&eYangın etkinliği başladı!";
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', startMsg));

        if (plugin.getConfig().getBoolean("discord.enabled", false)) {
            DiscordWebhook.sendStartNotification(plugin);
        }
    }

    private void startRandomShootingTask() {
        if (shootTask != null) {
            shootTask.cancel();
        }

        shootTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!eventActive || activeGhasts.isEmpty()) {
                    cancel();
                    return;
                }

                for (Ghast ghast : activeGhasts) {
                    if (ghast != null && !ghast.isDead()) {
                        // Rastgele Açı ve Hız Vektörü (3D Rastgele Yön)
                        double vx = (random.nextDouble() - 0.5) * 1.5;
                        double vy = random.nextDouble() * 0.8 + 0.2; // Yukarı/İleri kavisli atış
                        double vz = (random.nextDouble() - 0.5) * 1.5;
                        Vector velocity = new Vector(vx, vy, vz);

                        // Glowing Magma Bloğu Oluştur
                        FallingBlock magmaBlock = ghast.getWorld().spawnFallingBlock(
                                ghast.getLocation().add(0, -0.5, 0),
                                Material.MAGMA_BLOCK.createBlockData()
                        );

                        magmaBlock.setGlowing(true);
                        magmaBlock.setVelocity(velocity);
                        magmaBlock.setDropItem(false);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 30L); // Etkinlik başlar başlamaz 1. sn sonra başlar, her 1.5 saniyede (30 tick) tekrarlar
    }

    public void stopEvent() {
        if (!eventActive) return;

        if (shootTask != null) {
            shootTask.cancel();
            shootTask = null;
        }

        for (Ghast ghast : new ArrayList<>(activeGhasts)) {
            if (ghast != null && !ghast.isDead()) {
                ghast.remove();
            }
        }
        activeGhasts.clear();

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

        if (ghast.getKiller() != null) {
            addParticipant(ghast.getKiller());
        }

        if (activeGhasts.isEmpty() && eventActive) {
            stopEvent();
        }
    }

    public void updateGhastNameTag(Ghast ghast) {
        if (ghast == null) return;

        double currentHealth = Math.max(0, ghast.getHealth());
        double maxHealth = ghast.getMaxHealth();
        double healthPercent = currentHealth / maxHealth;

        int totalBars = 15;
        int filledBars = (int) Math.round(healthPercent * totalBars);

        String healthColor;
        if (healthPercent > 0.5) {
            healthColor = "&a";
        } else if (healthPercent > 0.25) {
            healthColor = "&e";
        } else {
            healthColor = "&c";
        }

        StringBuilder barBuilder = new StringBuilder();
        for (int i = 0; i < totalBars; i++) {
            if (i < filledBars) {
                barBuilder.append("█");
            } else {
                barBuilder.append("&7░");
            }
        }

        String tag = "&c&lAlev Ghast'ı &8| " + healthColor + barBuilder.toString() + " &f(" + (int) currentHealth + "/" + (int) maxHealth + ")";

        ghast.setCustomName(ChatColor.translateAlternateColorCodes('&', tag));
        ghast.setCustomNameVisible(true);
    }

    public void trackBlockChange(Block block) {
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