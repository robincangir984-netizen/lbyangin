package com.lbdevz.lbyangin;

import com.lbdevz.lbyangin.commands.YanginCommand;
import com.lbdevz.lbyangin.listeners.GhastDamageListener;
import com.lbdevz.lbyangin.listeners.GhastDeathListener;
import com.lbdevz.lbyangin.listeners.GhastShootListener;
import com.lbdevz.lbyangin.listeners.MagmaBlockListener;
import com.lbdevz.lbyangin.managers.AutoSchedulerManager;
import com.lbdevz.lbyangin.managers.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class LBYangin extends JavaPlugin {

    private EventManager eventManager;
    private AutoSchedulerManager autoSchedulerManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        sendBanner();

        this.eventManager = new EventManager(this);
        this.autoSchedulerManager = new AutoSchedulerManager(this);
        this.autoSchedulerManager.startScheduler();

        YanginCommand yanginCmd = new YanginCommand(this);
        if (getCommand("yangin") != null) {
            getCommand("yangin").setExecutor(yanginCmd);
            getCommand("yangin").setTabCompleter(yanginCmd);
        }

        // Listener kayıtları
        getServer().getPluginManager().registerEvents(new MagmaBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new GhastDeathListener(this), this);
        getServer().getPluginManager().registerEvents(new GhastDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new GhastShootListener(this), this);
    }

    private void sendBanner() {
        String[] lines = new String[]{
            "&c  _     ____   __   ___  _   _  ____ ___ _   _",
            "&c | |   | __ )   \\ \\ / / \\| \\ | |/ ___|_ _| \\ | |",
            "&c | |   |  _ \\   \\ V / _ \\|  \\| | |  _ | ||  \\| |",
            "&c | |___| |_) |   | / ___ \\ |\\  | |_| || || |\\  |",
            "&c |_____|____/    |_/_/   \\_\\_| \\_\\____|___|_| \\_| &7v" + getDescription().getVersion(),
            "",
            "   &e» Geliştirici: &fRob1nss25",
            ""
        };

        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    @Override
    public void onDisable() {
        if (autoSchedulerManager != null) {
            autoSchedulerManager.stopScheduler();
        }
        if (eventManager != null && eventManager.isEventActive()) {
            eventManager.stopEvent();
        }
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public AutoSchedulerManager getAutoSchedulerManager() {
        return autoSchedulerManager;
    }
}