package com.lbdevz.lbyangin.listeners;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class GhastShootListener implements Listener {

    private final LBYangin plugin;

    public GhastShootListener(LBYangin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Fireball fireball)) return;

        if (fireball.getShooter() instanceof Ghast ghast) {
            if (!plugin.getEventManager().isGhastFromEvent(ghast)) return;

            Location hitLoc = fireball.getLocation();
            if (event.getHitBlock() != null) {
                hitLoc = event.getHitBlock().getLocation();
            }

            int radius = plugin.getConfig().getInt("settings.fire-radius", 2);
            boolean explosionEnabled = plugin.getConfig().getBoolean("settings.explosion-enabled", true);
            float explosionPower = (float) plugin.getConfig().getDouble("settings.explosion-power", 3.0);

            if (explosionEnabled) {
                hitLoc.getWorld().createExplosion(hitLoc, explosionPower, false, false);
            }

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block targetBlock = hitLoc.getBlock().getRelative(x, y, z);
                        
                        if (targetBlock.getType() != Material.BEDROCK && targetBlock.getType() != Material.AIR) {
                            plugin.getEventManager().trackBlockChange(targetBlock);
                            
                            if (Math.random() < 0.6) {
                                targetBlock.setType(Material.MAGMA_BLOCK);
                            } else {
                                targetBlock.setType(Material.FIRE);
                            }
                        } else if (targetBlock.getType() == Material.AIR && targetBlock.getRelative(0, -1, 0).getType().isSolid()) {
                            plugin.getEventManager().trackBlockChange(targetBlock);
                            targetBlock.setType(Material.FIRE);
                        }
                    }
                }
            }
        }
    }
}