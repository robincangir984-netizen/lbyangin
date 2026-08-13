package com.lbdevz.lbyangin.commands;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class YanginCommand implements CommandExecutor {

    private final LBYangin plugin;

    public YanginCommand(LBYangin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§cKullanım: /yangin <setwarp|start|stop>");
            return true;
        }

        if (args[0].equalsIgnoreCase("setwarp")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("§cBu komut sadece oyundan kullanılabilir.");
                return true;
            }

            Location loc = player.getLocation();
            FileConfiguration config = plugin.getConfig();
            
            config.set("warp-location.world", loc.getWorld().getName());
            config.set("warp-location.x", loc.getX());
            config.set("warp-location.y", loc.getY());
            config.set("warp-location.z", loc.getZ());
            config.set("warp-location.yaw", loc.getYaw());
            config.set("warp-location.pitch", loc.getPitch());
            plugin.saveConfig();

            player.sendMessage("§a[LBYangin] Etkinlik merkezi başarıyla ayarlandı!");
            return true;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (plugin.getEventManager().isEventActive()) {
                sender.sendMessage("§cEtkinlik zaten aktif!");
                return true;
            }

            FileConfiguration config = plugin.getConfig();
            String worldName = config.getString("warp-location.world");
            
            if (worldName == null) {
                sender.sendMessage("§cEtkinlik konumu ayarlanmamış! Önce /yangin setwarp kullanın.");
                return true;
            }

            World world = Bukkit.getWorld(worldName);
            double x = config.getDouble("warp-location.x");
            double y = config.getDouble("warp-location.y");
            double z = config.getDouble("warp-location.z");
            float yaw = (float) config.getDouble("warp-location.yaw");
            float pitch = (float) config.getDouble("warp-location.pitch");

            Location warpLoc = new Location(world, x, y, z, yaw, pitch);
            
            plugin.getEventManager().startEvent(warpLoc);
            Bukkit.broadcastMessage("§e§l[ETKİNLİK] §c/warp yangin §ebölgesinde yangın etkinliği başladı!");
            return true;
        }

        if (args[0].equalsIgnoreCase("stop")) {
            if (!plugin.getEventManager().isEventActive()) {
                sender.sendMessage("§cAktif bir etkinlik yok!");
                return true;
            }

            plugin.getEventManager().stopEvent();
            Bukkit.broadcastMessage("§e§l[ETKİNLİK] §aYangın etkinliği sona erdi.");
            return true;
        }

        sender.sendMessage("§cBilinmeyen alt komut.");
        return true;
    }
}