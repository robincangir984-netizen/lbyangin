package com.lbdevz.lbyangin.managers;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Ghast;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class EventManager {

    private final LBYangin plugin;
    private boolean active = false;
    private final List<Ghast> eventGhasts = new ArrayList<>();
    private final Map<Location, BlockData> originalBlocks = new HashMap<>();
    private BukkitTask shootTask;
    private final Random random = new Random();

    public EventManager(LBYangin plugin) {
        this.plugin = plugin;
    }

    public void startEvent(Location warpLocation) {
        if (active) return;
        this.active = true;
        originalBlocks.clear();

        int ghastAmount = plugin.getConfig().getInt("settings.ghast-amount", 4);
        double ghastHealth = plugin.getConfig().getDouble("settings.ghast-health", 100.0);
        boolean ghastGlowing = plugin.getConfig().getBoolean("settings.ghast-glowing", true);

        for (int i = 0; i < ghastAmount; i++) {
            Location spawnLoc = warpLocation.clone().add(
                    random.nextInt(20) - 10,
                    10 + random.nextInt(5),
                    random.nextInt(20) - 10
            );
            Ghast ghast = (Ghast) warpLocation.getWorld().spawnEntity(spawnLoc, EntityType.GHAST);
            ghast.setCustomName("§c§lEtkinlik Ghast'ı");
            ghast.setCustomNameVisible(true);
            ghast.setGlowing(ghastGlowing);

            AttributeInstance healthAttr = ghast.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                healthAttr.setBaseValue(ghastHealth);
                ghast.setHealth(ghastHealth);
            }

            eventGhasts.add(ghast);
        }

        int interval = plugin.getConfig().getInt("settings.shoot-interval-seconds", 3);
        shootTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Ghast ghast : new ArrayList<>(eventGhasts)) {
                if (ghast.isValid() && !ghast.isDead()) {
                    spawnGlowingMagmaBlock(ghast);
                }
            }
        }, 20L * interval, 20L * interval);
    }

    private void spawnGlowingMagmaBlock(Ghast ghast) {
        Location origin = ghast.getLocation();
        
        FallingBlock magma = origin.getWorld().spawnFallingBlock(
                origin, 
                Material.MAGMA_BLOCK.createBlockData()
        );

        magma.setGlowing(true);
        magma.setDropItem(false);
        magma.setHurtEntities(true);

        Vector velocity = new Vector(
                (random.nextDouble() - 0.5) * 0.8,
                -0.2,
                (random.nextDouble() - 0.5) * 0.8
        );
        magma.setVelocity(velocity);
    }

    // Blok değişmeden veya patlamadan önceki orijinal durumunu kaydeder
    public void trackBlockChange(Block block) {
        if (!active) return;
        Location loc = block.getLocation();
        if (!originalBlocks.containsKey(loc)) {
            originalBlocks.put(loc, block.getBlockData().clone());
        }
    }

    public void handleGhastDeath(Ghast ghast) {
        if (!active) return;

        eventGhasts.remove(ghast);

        if (eventGhasts.isEmpty()) {
            stopEvent();
            Bukkit.broadcastMessage("§e§l[ETKİNLİK] §aTüm Ghast'lar öldürüldü! Yangın etkinliği bitti, harita eski haline getirildi.");
        }
    }

    public void stopEvent() {
        if (!active) return;
        this.active = false;

        if (shootTask != null) shootTask.cancel();

        for (Ghast ghast : eventGhasts) {
            if (ghast.isValid()) ghast.remove();
        }
        eventGhasts.clear();

        // Haritayı eski orijinal haline çevirir
        restoreTerrain();
    }

    private void restoreTerrain() {
        for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
            Location loc = entry.getKey();
            BlockData originalData = entry.getValue();
            loc.getBlock().setBlockData(originalData, false);
        }
        originalBlocks.clear();
    }

    public boolean isGhastFromEvent(Ghast ghast) {
        return eventGhasts.contains(ghast);
    }

    public boolean isEventActive() {
        return active;
    }
}