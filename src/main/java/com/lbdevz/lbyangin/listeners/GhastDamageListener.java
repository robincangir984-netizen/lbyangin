package com.lbdevz.lbyangin.listeners;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class GhastDamageListener implements Listener {

    private final LBYangin plugin;

    public GhastDamageListener(LBYangin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGhastDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Ghast ghast) {
            if (plugin.getEventManager().isGhastFromEvent(ghast)) {
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getEventManager().updateGhastNameTag(ghast);
                }, 1L);
            }
        }
    }
}