package com.lbdevz.lbyangin.listeners;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

                // Hasar veren oyuncu mu (Doğrudan vuruş veya Ok/Mermi atışı) kontrolü
                if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                    Player attacker = null;

                    if (damageByEntityEvent.getDamager() instanceof Player player) {
                        attacker = player;
                    } else if (damageByEntityEvent.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
                        attacker = player;
                    }

                    if (attacker != null) {
                        plugin.getEventManager().addParticipant(attacker);
                    }
                }

                // Can barını (NameTag) güncelle
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    plugin.getEventManager().updateGhastNameTag(ghast);
                }, 1L);
            }
        }
    }
}