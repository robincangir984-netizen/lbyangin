package com.lbdevz.lbyangin.listeners;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Ghast;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class GhastDeathListener implements Listener {

    private final LBYangin plugin;

    public GhastDeathListener(LBYangin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onGhastDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Ghast ghast) {
            if (plugin.getEventManager().isGhastFromEvent(ghast)) {
                // Hasar hesaplandıktan hemen sonra ismi güncelle
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (ghast.isValid() && !ghast.isDead()) {
                        plugin.getEventManager().updateGhastNameTag(ghast);
                    }
                });
            }
        }
    }
}