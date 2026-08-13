package com.lbdevz.lbyangin.listeners;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class GhastDeathListener implements Listener {

    private final LBYangin plugin;

    public GhastDeathListener(LBYangin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGhastDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Ghast ghast) {
            if (plugin.getEventManager().isGhastFromEvent(ghast)) {
                plugin.getEventManager().handleGhastDeath(ghast);
            }
        }
    }
}