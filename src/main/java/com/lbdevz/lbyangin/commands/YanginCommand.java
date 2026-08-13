package com.lbdevz.lbyangin.commands;

import com.lbdevz.lbyangin.LBYangin;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class YanginCommand implements CommandExecutor, TabCompleter {

    private final LBYangin plugin;

    public YanginCommand(LBYangin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Kullanım: /yangin <start|stop|setwarp|reload>");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                if (!sender.hasPermission("lbyangin.admin")) {
                    sender.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok!");
                    return true;
                }
                if (plugin.getEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "Yangın etkinliği zaten devam ediyor!");
                    return true;
                }

                // ARTIK OYUNCUNUN KONUMUNU DEĞİL, WARP KONUMUNU KULLANIYOR
                plugin.getEventManager().startEvent();
                sender.sendMessage(ChatColor.GREEN + "Yangın etkinliği Warp bölgesinde başlatıldı!");
                break;

            case "stop":
                if (!sender.hasPermission("lbyangin.admin")) {
                    sender.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok!");
                    return true;
                }
                if (!plugin.getEventManager().isEventActive()) {
                    sender.sendMessage(ChatColor.RED + "Aktif bir yangın etkinliği yok!");
                    return true;
                }
                plugin.getEventManager().stopEvent();
                sender.sendMessage(ChatColor.GREEN + "Yangın etkinliği durduruldu!");
                break;

            case "setwarp":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "Bu komutu sadece oyuncular kullanabilir!");
                    return true;
                }
                Player player = (Player) sender;
                if (!player.hasPermission("lbyangin.admin")) {
                    player.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok!");
                    return true;
                }

                Location loc = player.getLocation();
                plugin.getConfig().set("warp-location.world", loc.getWorld().getName());
                plugin.getConfig().set("warp-location.x", loc.getX());
                plugin.getConfig().set("warp-location.y", loc.getY());
                plugin.getConfig().set("warp-location.z", loc.getZ());
                plugin.getConfig().set("warp-location.yaw", loc.getYaw());
                plugin.getConfig().set("warp-location.pitch", loc.getPitch());
                plugin.saveConfig();

                player.sendMessage(ChatColor.GREEN + "Yangın etkinliği doğma noktası (Warp) bulunduğunuz konum olarak ayarlandı!");
                break;

            case "reload":
                if (!sender.hasPermission("lbyangin.admin")) {
                    sender.sendMessage(ChatColor.RED + "Bu komutu kullanmak için yetkiniz yok!");
                    return true;
                }
                plugin.reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "LB-Yangin konfigürasyonu yeniden yüklendi!");
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Bilinmeyen komut! Kullanım: /yangin <start|stop|setwarp|reload>");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("start");
            completions.add("stop");
            completions.add("setwarp");
            completions.add("reload");
        }
        return completions;
    }
}