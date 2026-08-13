package com.lbdevz.lbyangin;

import com.lbdevz.lbyangin.commands.YanginCommand;
import com.lbdevz.lbyangin.listeners.GhastDeathListener;
import com.lbdevz.lbyangin.listeners.MagmaBlockListener;
import com.lbdevz.lbyangin.managers.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class LBYangin extends JavaPlugin {

    private EventManager eventManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        this.eventManager = new EventManager(this);
        
        getCommand("yangin").setExecutor(new YanginCommand(this));
        getServer().getPluginManager().registerEvents(new MagmaBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new GhastDeathListener(this), this);

        getLogger().info("LB Yangin Eklentisi Aktif!");
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