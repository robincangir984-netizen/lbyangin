package com.lbdevz.lbyangin;

import com.lbdevz.lbyangin.commands.YanginCommand;
import com.lbdevz.lbyangin.listeners.GhastDamageListener;
import com.lbdevz.lbyangin.listeners.GhastDeathListener;
import com.lbdevz.lbyangin.listeners.GhastShootListener;
import com.lbdevz.lbyangin.listeners.MagmaBlockListener;
import com.lbdevz.lbyangin.managers.EventManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class LBYangin extends JavaPlugin {

    private EventManager eventManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        sendBanner();

        this.eventManager = new EventManager(this);

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
            "&c         ____",
            "&c    ____| __ )  ___  ___ ___",
            "&c   / ___|  _ \\ / _ \\/ __/ __|",
            "&c  | |___| |_) | (_) \\__ \\__ \\",
            "&c   \\____|____/ \\___/|___/___/ &7v" + getDescription().getVersion(),
            "",
            "   &e» Geliştirici: &fxCatyy",
            ""
        };

        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
    }

    @Override
    public void onDisable() {
        if (eventManager != null && eventManager.isEventActive()) {
            eventManager.stopEvent();
        }
    }

    public EventManager getEventManager() {
        return eventManager;
    }
}