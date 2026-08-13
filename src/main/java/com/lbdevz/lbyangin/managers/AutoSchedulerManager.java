package com.lbdevz.lbyangin.managers;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.LocalTime;

public class AutoSchedulerManager {

    private final LBYangin plugin;
    private BukkitTask task;
    private int lastTriggerHour = -1;
    private int lastTriggerMinute = -1;

    public AutoSchedulerManager(LBYangin plugin) {
        this.plugin = plugin;
    }

    public void startScheduler() {
        if (task != null) {
            task.cancel();
        }

        task = new BukkitRunnable() {
            @Override
            public void run() {
                boolean enabled = plugin.getConfig().getBoolean("auto-start.enabled", false);
                if (!enabled) return;

                LocalTime now = LocalTime.now();
                int targetHour = plugin.getConfig().getInt("auto-start.hour", 19);
                int targetMinute = plugin.getConfig().getInt("auto-start.minute", 0);

                if (now.getHour() == targetHour && now.getMinute() == targetMinute) {
                    if (lastTriggerHour != targetHour || lastTriggerMinute != targetMinute) {
                        lastTriggerHour = targetHour;
                        lastTriggerMinute = targetMinute;

                        if (!plugin.getEventManager().isEventActive()) {
                            plugin.getEventManager().startEvent();
                            plugin.getLogger().info("Zamanlanmış Yangın Etkinliği otomatik olarak başlatıldı.");
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 200L);
    }

    public void stopScheduler() {
        if (task != null) {
            task.cancel();
        }
    }
}