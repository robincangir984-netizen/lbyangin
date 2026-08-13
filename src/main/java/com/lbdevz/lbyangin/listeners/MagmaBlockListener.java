package com.lbdevz.lbyangin.listeners;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

public class MagmaBlockListener implements Listener {

    private final LBYangin plugin;

    public MagmaBlockListener(LBYangin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMagmaLand(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK && event.getTo() == Material.MAGMA_BLOCK) {
            Location landLoc = event.getBlock().getLocation();
            
            int radius = plugin.getConfig().getInt("settings.fire-radius", 1);
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    for (int y = -1; y <= 1; y++) {
                        Block targetBlock = landLoc.clone().add(x, y, z).getBlock();
                        if (targetBlock.getType() == Material.AIR && targetBlock.getRelative(0, -1, 0).getType().isSolid()) {
                            targetBlock.setType(Material.FIRE);
                        }
                    }
                }
            }
        }
    }
}