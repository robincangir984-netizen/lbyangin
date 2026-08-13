package com.lbdevz.lbyangin.managers;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class EventManager {

    private final LBYangin plugin;
    private final Set<UUID> activeGhasts = new HashSet<>();
    private final Map<Block, BlockState> changedBlocks = new HashMap<>();
    private boolean eventActive = false;

    public EventManager(LBYangin plugin) {
        this.plugin = plugin;
    }

    public boolean isEventActive() {
        return eventActive;
    }

    // Hem parametresiz hem de Location alan startEvent aşırı yüklemeleri (overload)
    public void startEvent() {
        this.startEvent(null);
    }

    public void startEvent(Location spawnLocation) {
        this.eventActive = true;
        this.activeGhasts.clear();
        this.changedBlocks.clear();
    }

    public void trackBlockChange(Block block) {
        if (!changedBlocks.containsKey(block)) {
            changedBlocks.put(block, block.getState());
        }
    }

    public void registerGhast(Ghast ghast) {
        if (ghast == null) return;

        activeGhasts.add(ghast.getUniqueId());

        // Config'den can ayarını çek (settings.ghast-health)
        double maxHealth = plugin.getConfig().getDouble("settings.ghast-health", 100.0);
        AttributeInstance healthAttr = ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(maxHealth);
            ghast.setHealth(maxHealth);
        }

        // Config'den glowing ayarını çek (settings.ghast-glowing)
        boolean glowing = plugin.getConfig().getBoolean("settings.ghast-glowing", true);
        ghast.setGlowing(glowing);

        updateGhastNameTag(ghast);
    }

    public boolean isGhastFromEvent(Ghast ghast) {
        return ghast != null && activeGhasts.contains(ghast.getUniqueId());
    }

    // GhastDeathListener'ın çağırdığı eksik metod
    public void handleGhastDeath(Ghast ghast) {
        if (ghast != null) {
            activeGhasts.remove(ghast.getUniqueId());
            checkEventCompletion();
        }
    }

    public void removeGhast(Ghast ghast) {
        handleGhastDeath(ghast);
    }

    private void checkEventCompletion() {
        if (!eventActive) return;

        activeGhasts.removeIf(uuid -> Bukkit.getEntity(uuid) == null || Bukkit.getEntity(uuid).isDead());

        if (activeGhasts.isEmpty()) {
            finishEventWithRewards();
        }
    }

    private void finishEventWithRewards() {
        this.eventActive = false;

        String prefix = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ");
        String endMsg = plugin.getConfig().getString("messages.event-end", "Etkinlik bitti!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + endMsg));

        // Ödül dağıtımı (Bütün oyunculara config komutu çalıştırma)
        String rewardCmd = plugin.getConfig().getString("rewards.command", "");
        if (rewardCmd != null && !rewardCmd.isEmpty()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                String cmdToExecute = rewardCmd.replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdToExecute);
            }
        }

        restoreBlocks();
    }

    public void stopEvent() {
        this.eventActive = false;
        for (UUID uuid : activeGhasts) {
            if (Bukkit.getEntity(uuid) != null) {
                Bukkit.getEntity(uuid).remove();
            }
        }
        this.activeGhasts.clear();
        restoreBlocks();
    }

    private void restoreBlocks() {
        for (Map.Entry<Block, BlockState> entry : changedBlocks.entrySet()) {
            entry.getValue().update(true, false);
        }
        changedBlocks.clear();
    }

    public void updateGhastNameTag(Ghast ghast) {
        if (ghast == null || !ghast.isValid()) return;

        AttributeInstance attr = ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = attr != null ? attr.getValue() : ghast.getMaxHealth();
        double currentHealth = ghast.getHealth();

        ghast.setCustomName(ChatColor.translateAlternateColorCodes('&', 
            "&c&lYangın Ghastı &7[" + (int) currentHealth + "/" + (int) maxHealth + "]"));
        ghast.setCustomNameVisible(true);
    }
}