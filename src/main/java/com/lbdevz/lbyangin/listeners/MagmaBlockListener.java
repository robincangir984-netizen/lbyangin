package com.lbdevz.lbyangin.listeners;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class MagmaBlockListener implements Listener {

    private final LBYangin plugin;

    public MagmaBlockListener(LBYangin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMagmaLand(EntityChangeBlockEvent event) {
        if (!plugin.getEventManager().isEventActive()) return;

        if (event.getEntityType() == EntityType.FALLING_BLOCK && event.getTo() == Material.MAGMA_BLOCK) {
            Block landBlock = event.getBlock();
            Location landLoc = landBlock.getLocation();

            plugin.getEventManager().trackBlockChange(landBlock);

            boolean explosionEnabled = plugin.getConfig().getBoolean("settings.explosion-enabled", true);
            float explosionPower = (float) plugin.getConfig().getDouble("settings.explosion-power", 3.0);

            if (explosionEnabled) {
                landLoc.getWorld().createExplosion(landLoc, explosionPower, true, true);
            }

            int radius = plugin.getConfig().getInt("settings.fire-radius", 2);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -1; y <= 1; y++) {
                        Block targetBlock = landLoc.clone().add(x, y, z).getBlock();
                        if (targetBlock.getType() == Material.AIR && targetBlock.getRelative(0, -1, 0).getType().isSolid()) {
                            plugin.getEventManager().trackBlockChange(targetBlock);
                            targetBlock.setType(Material.FIRE);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getEventManager().isEventActive()) return;

        for (Block block : event.blockList()) {
            plugin.getEventManager().trackBlockChange(block);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.getEventManager().isEventActive()) return;

        for (Block block : event.blockList()) {
            plugin.getEventManager().trackBlockChange(block);
        }
    }
}