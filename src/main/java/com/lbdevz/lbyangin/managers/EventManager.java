package com.lbdevz.lbyangin.managers;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

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
    private BukkitTask shootTask;

    public EventManager(LBYangin plugin) {
        this.plugin = plugin;
    }

    public boolean isEventActive() {
        return eventActive;
    }

    public void startEvent() {
        startEvent(getWarpLocationFromConfig());
    }

    public void startEvent(Location spawnLocation) {
        this.eventActive = true;
        this.activeGhasts.clear();
        this.changedBlocks.clear();

        Location loc = spawnLocation != null ? spawnLocation : getWarpLocationFromConfig();
        if (loc != null && loc.getWorld() != null) {
            spawnGhastsAtLocation(loc);
        }

        startShootingTask();
    }

    /**
     * Ghast'ların belirlenen saniyede bir Magma Bloğu fırlatmasını sağlayan zamanlayıcı
     */
    private void startShootingTask() {
        if (shootTask != null) {
            shootTask.cancel();
        }

        int intervalSeconds = plugin.getConfig().getInt("settings.shoot-interval-seconds", 3);
        long ticks = Math.max(1, intervalSeconds) * 20L;

        shootTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!eventActive || activeGhasts.isEmpty()) {
                    cancel();
                    return;
                }

                for (UUID uuid : new HashSet<>(activeGhasts)) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity instanceof Ghast ghast && ghast.isValid() && !ghast.isDead()) {
                        throwMagmaBlockFromGhast(ghast);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, ticks);
    }

    private void throwMagmaBlockFromGhast(Ghast ghast) {
        Location spawnLoc = ghast.getLocation().add(0, -1, 0);

        // Yakındaki oyuncuyu bul, yoksa rastgele yöne fırlat
        Player target = null;
        double minDistance = 40.0;
        for (Player p : ghast.getWorld().getPlayers()) {
            double dist = p.getLocation().distance(spawnLoc);
            if (dist < minDistance) {
                minDistance = dist;
                target = p;
            }
        }

        Vector velocity;
        if (target != null) {
            Location targetLoc = target.getLocation();
            Vector direction = targetLoc.toVector().subtract(spawnLoc.toVector());
            double distance = targetLoc.distance(spawnLoc);
            velocity = direction.normalize().multiply(1.1).setY(Math.min(0.4, distance * 0.025));
        } else {
            double vx = (Math.random() - 0.5) * 0.8;
            double vz = (Math.random() - 0.5) * 0.8;
            velocity = new Vector(vx, 0.2, vz);
        }

        FallingBlock magmaBlock = ghast.getWorld().spawnFallingBlock(spawnLoc, Material.MAGMA_BLOCK.createBlockData());
        magmaBlock.setDropItem(false);
        magmaBlock.setHurtEntities(true);
        magmaBlock.setVelocity(velocity);
    }

    public Location getWarpLocationFromConfig() {
        String worldName = plugin.getConfig().getString("warp-location.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;

        double x = plugin.getConfig().getDouble("warp-location.x", 0.5);
        double y = plugin.getConfig().getDouble("warp-location.y", 64.0);
        double z = plugin.getConfig().getDouble("warp-location.z", 0.5);
        float yaw = (float) plugin.getConfig().getDouble("warp-location.yaw", 0.0);
        float pitch = (float) plugin.getConfig().getDouble("warp-location.pitch", 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    private void spawnGhastsAtLocation(Location loc) {
        int amount = plugin.getConfig().getInt("settings.ghast-amount", 4);
        for (int i = 0; i < amount; i++) {
            Ghast ghast = loc.getWorld().spawn(loc, Ghast.class);
            registerGhast(ghast);
        }
    }

    public void trackBlockChange(Block block) {
        if (!changedBlocks.containsKey(block)) {
            changedBlocks.put(block, block.getState());
        }
    }

    public void registerGhast(Ghast ghast) {
        if (ghast == null) return;

        activeGhasts.add(ghast.getUniqueId());

        double maxHealth = plugin.getConfig().getDouble("settings.ghast-health", 100.0);
        AttributeInstance healthAttr = ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.setBaseValue(maxHealth);
            ghast.setHealth(maxHealth);
        }

        boolean glowing = plugin.getConfig().getBoolean("settings.ghast-glowing", true);
        ghast.setGlowing(glowing);

        updateGhastNameTag(ghast);
    }

    public boolean isGhastFromEvent(Ghast ghast) {
        return ghast != null && activeGhasts.contains(ghast.getUniqueId());
    }

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
        if (shootTask != null) {
            shootTask.cancel();
        }

        String prefix = plugin.getConfig().getString("messages.prefix", "&a[LB-Yangin] ");
        String endMsg = plugin.getConfig().getString("messages.event-end", "Etkinlik bitti!");
        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', prefix + endMsg));

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
        if (shootTask != null) {
            shootTask.cancel();
        }

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